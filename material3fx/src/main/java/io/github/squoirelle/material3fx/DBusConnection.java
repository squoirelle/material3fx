package io.github.squoirelle.material3fx;

import java.io.Closeable;
import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * A minimal D-Bus client for the session bus, in pure Java (Unix domain socket, no native code,
 * no external process). It implements just what Material3FX needs: method calls with string arguments,
 * signal reception, and decoding of the basic types, structs, arrays and variants.
 * <p>
 * Protocol reference: <a href="https://dbus.freedesktop.org/doc/dbus-specification.html">D-Bus specification</a>.
 */
final class DBusConnection implements Closeable {

    static final byte METHOD_CALL = 1;
    static final byte METHOD_RETURN = 2;
    static final byte ERROR = 3;
    static final byte SIGNAL = 4;

    private static final byte FIELD_PATH = 1;
    private static final byte FIELD_INTERFACE = 2;
    private static final byte FIELD_MEMBER = 3;
    private static final byte FIELD_ERROR_NAME = 4;
    private static final byte FIELD_REPLY_SERIAL = 5;
    private static final byte FIELD_DESTINATION = 6;
    private static final byte FIELD_SIGNATURE = 8;

    /** A received message: type, header fields of interest, and decoded body values. */
    record Message(byte type, int serial, int replySerial, String path, String iface, String member,
                   String errorName, List<Object> body) {
    }

    private final SocketChannel channel;
    private int nextSerial = 1;

    private DBusConnection(SocketChannel channel) {
        this.channel = channel;
    }

    /** Connects and authenticates to the session bus, then registers with {@code Hello}. */
    static DBusConnection openSessionBus() throws IOException {
        Path socket = sessionBusSocket();
        if (socket == null) {
            throw new IOException("No session bus socket");
        }
        SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);
        try {
            channel.connect(UnixDomainSocketAddress.of(socket));
            DBusConnection connection = new DBusConnection(channel);
            connection.authenticate();
            connection.call("org.freedesktop.DBus", "/org/freedesktop/DBus", "org.freedesktop.DBus", "Hello");
            return connection;
        } catch (IOException | RuntimeException e) {
            channel.close();
            throw e;
        }
    }

    /** The session bus socket from DBUS_SESSION_BUS_ADDRESS ({@code unix:path=...}) or $XDG_RUNTIME_DIR/bus. */
    static Path sessionBusSocket() {
        String address = System.getenv("DBUS_SESSION_BUS_ADDRESS");
        if (address != null) {
            for (String entry : address.split(";")) {
                if (entry.startsWith("unix:")) {
                    for (String kv : entry.substring(5).split(",")) {
                        if (kv.startsWith("path=")) {
                            return Path.of(unescape(kv.substring(5)));
                        }
                    }
                }
            }
        }
        String runtime = System.getenv("XDG_RUNTIME_DIR");
        if (runtime != null && Files.exists(Path.of(runtime, "bus"))) {
            return Path.of(runtime, "bus");
        }
        return null;
    }

    private static String unescape(String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '%' && i + 2 < value.length()) {
                out.append((char) Integer.parseInt(value.substring(i + 1, i + 3), 16));
                i += 2;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    /** SASL EXTERNAL: the bus checks our Unix credentials on the socket. */
    private void authenticate() throws IOException {
        String uid = String.valueOf(Files.getAttribute(Path.of("/proc/self"), "unix:uid"));
        String hexUid = HexFormat.of().formatHex(uid.getBytes(StandardCharsets.US_ASCII));
        writeAscii("\0AUTH EXTERNAL " + hexUid + "\r\n");
        String reply = readLine();
        if (!reply.startsWith("OK")) {
            throw new IOException("D-Bus authentication rejected: " + reply);
        }
        writeAscii("BEGIN\r\n");
    }

    private void writeAscii(String text) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(text.getBytes(StandardCharsets.US_ASCII));
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    private String readLine() throws IOException {
        StringBuilder line = new StringBuilder();
        ByteBuffer one = ByteBuffer.allocate(1);
        while (true) {
            one.clear();
            if (channel.read(one) < 0) {
                throw new IOException("D-Bus connection closed during authentication");
            }
            char c = (char) one.get(0);
            if (c == '\n') {
                return line.toString().trim();
            }
            line.append(c);
        }
    }

    /**
     * Calls a method with string arguments and waits for its reply. Signals arriving meanwhile are
     * dropped. Throws {@link IOException} if the bus returns an error.
     */
    Message call(String destination, String path, String iface, String member, String... args) throws IOException {
        int serial = nextSerial++;
        send(encodeCall(serial, destination, path, iface, member, args));
        while (true) {
            Message message = readMessage();
            if ((message.type() == METHOD_RETURN || message.type() == ERROR) && message.replySerial() == serial) {
                if (message.type() == ERROR) {
                    throw new IOException("D-Bus error " + message.errorName() + ": " + message.body());
                }
                return message;
            }
        }
    }

    private void send(ByteBuffer message) throws IOException {
        while (message.hasRemaining()) {
            channel.write(message);
        }
    }

    /** Blocks until the next message arrives. */
    Message readMessage() throws IOException {
        ByteBuffer fixed = readFully(16);
        ByteOrder order = fixed.get(0) == 'B' ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        fixed.order(order);
        int bodyLength = fixed.getInt(4);
        int fieldsLength = fixed.getInt(12);
        int headerEnd = 16 + fieldsLength;
        int bodyStart = (headerEnd + 7) & ~7;
        ByteBuffer rest = readFully(bodyStart - 16 + bodyLength);
        ByteBuffer whole = ByteBuffer.allocate(bodyStart + bodyLength).order(order);
        whole.put(fixed.rewind()).put(rest).flip();
        return decode(whole);
    }

    private ByteBuffer readFully(int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        while (buffer.hasRemaining()) {
            if (channel.read(buffer) < 0) {
                throw new IOException("D-Bus connection closed");
            }
        }
        return buffer.flip();
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    // ---------------------------------------------------------------------------------------
    // Encoding

    /** Encodes a method call (package-private for tests). */
    static ByteBuffer encodeCall(int serial, String destination, String path, String iface, String member, String... args) {
        Writer body = new Writer();
        for (String arg : args) {
            body.string(arg);
        }
        Writer m = new Writer();
        m.b((byte) 'l').b(METHOD_CALL).b((byte) 0).b((byte) 1);
        m.u32(body.size()).u32(serial);
        int lengthAt = m.size();
        m.u32(0);   // header fields array length, patched below
        int fieldsStart = m.size();
        m.align(8).b(FIELD_PATH).signature("o").string(path);
        m.align(8).b(FIELD_DESTINATION).signature("s").string(destination);
        m.align(8).b(FIELD_INTERFACE).signature("s").string(iface);
        m.align(8).b(FIELD_MEMBER).signature("s").string(member);
        if (args.length > 0) {
            m.align(8).b(FIELD_SIGNATURE).signature("g").signature("s".repeat(args.length));
        }
        m.patchU32(lengthAt, m.size() - fieldsStart);
        m.align(8);
        m.bytes(body.toByteArray());
        return ByteBuffer.wrap(m.toByteArray());
    }

    /** Little-endian D-Bus marshalling writer; alignment is relative to the start of the message. */
    static final class Writer {
        private final java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        private final List<int[]> patches = new ArrayList<>();
        private byte[] patched;

        int size() {
            return out.size();
        }

        Writer b(byte value) {
            out.write(value);
            return this;
        }

        Writer align(int boundary) {
            while (out.size() % boundary != 0) {
                out.write(0);
            }
            return this;
        }

        Writer u32(int value) {
            align(4);
            out.write(value);
            out.write(value >>> 8);
            out.write(value >>> 16);
            out.write(value >>> 24);
            return this;
        }

        Writer f64(double value) {
            align(8);
            long bits = Double.doubleToLongBits(value);
            for (int i = 0; i < 8; i++) {
                out.write((int) (bits >>> (8 * i)));
            }
            return this;
        }

        Writer string(String value) {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            u32(bytes.length);
            out.writeBytes(bytes);
            out.write(0);
            return this;
        }

        Writer signature(String value) {
            byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
            out.write(bytes.length);
            out.writeBytes(bytes);
            out.write(0);
            return this;
        }

        Writer bytes(byte[] bytes) {
            out.writeBytes(bytes);
            return this;
        }

        void patchU32(int offset, int value) {
            patches.add(new int[] {offset, value});
        }

        byte[] toByteArray() {
            patched = out.toByteArray();
            for (int[] p : patches) {
                patched[p[0]] = (byte) p[1];
                patched[p[0] + 1] = (byte) (p[1] >>> 8);
                patched[p[0] + 2] = (byte) (p[1] >>> 16);
                patched[p[0] + 3] = (byte) (p[1] >>> 24);
            }
            return patched;
        }
    }

    // ---------------------------------------------------------------------------------------
    // Decoding

    /** Decodes a complete message (package-private for tests). */
    static Message decode(ByteBuffer m) {
        byte type = m.get(1);
        int bodyLength = m.getInt(4);
        int serial = m.getInt(8);
        int fieldsLength = m.getInt(12);
        int replySerial = 0;
        String path = null;
        String iface = null;
        String member = null;
        String errorName = null;
        String signature = "";
        m.position(16);
        int fieldsEnd = 16 + fieldsLength;
        while (m.position() < fieldsEnd) {
            align(m, 8);
            byte code = m.get();
            String fieldSignature = readSignature(m);
            Object value = readValue(m, fieldSignature, new int[] {0});
            switch (code) {
                case FIELD_PATH -> path = (String) value;
                case FIELD_INTERFACE -> iface = (String) value;
                case FIELD_MEMBER -> member = (String) value;
                case FIELD_ERROR_NAME -> errorName = (String) value;
                case FIELD_REPLY_SERIAL -> replySerial = ((Long) value).intValue();
                case FIELD_SIGNATURE -> signature = (String) value;
                default -> { }
            }
        }
        align(m, 8);
        List<Object> body = new ArrayList<>();
        int bodyEnd = m.position() + bodyLength;
        int[] index = {0};
        while (index[0] < signature.length() && m.position() < bodyEnd) {
            body.add(readValue(m, signature, index));
        }
        return new Message(type, serial, replySerial, path, iface, member, errorName, body);
    }

    private static void align(ByteBuffer m, int boundary) {
        int p = m.position();
        m.position((p + boundary - 1) / boundary * boundary);
    }

    private static String readSignature(ByteBuffer m) {
        int length = m.get() & 0xFF;
        byte[] bytes = new byte[length];
        m.get(bytes);
        m.get();   // nul
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    private static String readString(ByteBuffer m) {
        align(m, 4);
        int length = m.getInt();
        byte[] bytes = new byte[length];
        m.get(bytes);
        m.get();   // nul
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /** Reads one complete value of {@code signature} starting at {@code index[0]}, advancing it. */
    private static Object readValue(ByteBuffer m, String signature, int[] index) {
        char type = signature.charAt(index[0]++);
        switch (type) {
            case 'y' -> {
                return (long) (m.get() & 0xFF);
            }
            case 'b', 'u' -> {
                align(m, 4);
                long v = m.getInt() & 0xFFFFFFFFL;
                return type == 'b' ? (Object) (v != 0) : (Object) v;
            }
            case 'i' -> {
                align(m, 4);
                return (long) m.getInt();
            }
            case 'n', 'q' -> {
                align(m, 2);
                return (long) m.getShort();
            }
            case 'x', 't' -> {
                align(m, 8);
                return m.getLong();
            }
            case 'd' -> {
                align(m, 8);
                return m.getDouble();
            }
            case 's', 'o' -> {
                return readString(m);
            }
            case 'g' -> {
                return readSignature(m);
            }
            case 'v' -> {
                String inner = readSignature(m);
                return readValue(m, inner, new int[] {0});
            }
            case '(' -> {
                align(m, 8);
                List<Object> fields = new ArrayList<>();
                while (signature.charAt(index[0]) != ')') {
                    fields.add(readValue(m, signature, index));
                }
                index[0]++;
                return fields;
            }
            case 'a' -> {
                align(m, 4);
                int length = m.getInt();
                int elementStart = index[0];
                char element = signature.charAt(elementStart);
                align(m, element == '(' || element == '{' || element == 'd' || element == 'x' || element == 't' ? 8 : 1);
                int end = m.position() + length;
                List<Object> items = new ArrayList<>();
                int elementEnd = elementStart;
                while (m.position() < end) {
                    int[] i = {elementStart};
                    items.add(readValue(m, signature, i));
                    elementEnd = i[0];
                }
                index[0] = elementEnd > elementStart ? elementEnd : skipType(signature, elementStart);
                return items;
            }
            case '{' -> {
                align(m, 8);
                Object key = readValue(m, signature, index);
                Object value = readValue(m, signature, index);
                index[0]++;   // '}'
                return List.of(key, value);
            }
            default -> throw new IllegalArgumentException("Unsupported D-Bus type '" + type + "' in " + signature);
        }
    }

    /** Index just after the complete type starting at {@code i} (for empty arrays). */
    private static int skipType(String signature, int i) {
        char c = signature.charAt(i);
        if (c == 'a') {
            return skipType(signature, i + 1);
        }
        if (c == '(' || c == '{') {
            char close = c == '(' ? ')' : '}';
            int depth = 0;
            for (int j = i; j < signature.length(); j++) {
                char d = signature.charAt(j);
                if (d == '(' || d == '{') {
                    depth++;
                } else if (d == ')' || d == '}') {
                    depth--;
                    if (depth == 0 && d == close) {
                        return j + 1;
                    }
                }
            }
        }
        return i + 1;
    }
}
