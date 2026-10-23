package dev.m3fx;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemPreferencesTest {

    @Test
    void decodesAReadOneReplyWithAnAccentColor() {
        DBusConnection.Message m = DBusConnection.decode(message(DBusConnection.METHOD_RETURN, 7, "v",
                body -> body.signature("(ddd)").align(8).f64(0.388).f64(0.816).f64(0.875)));
        assertEquals(DBusConnection.METHOD_RETURN, m.type());
        assertEquals(7, m.replySerial());
        Color accent = SystemPreferences.accentColor(m.body().get(0));
        assertEquals(0.388, accent.getRed(), 0.001);
        assertEquals(0.816, accent.getGreen(), 0.001);
        assertEquals(0.875, accent.getBlue(), 0.001);
    }

    @Test
    void decodesTheNestedVariantOfOlderPortals() {
        DBusConnection.Message m = DBusConnection.decode(message(DBusConnection.METHOD_RETURN, 3, "v",
                body -> body.signature("v").signature("u").u32(1)));
        assertEquals(1, SystemPreferences.colorScheme(m.body().get(0)));
    }

    @Test
    void decodesSignalsAndArrays() {
        DBusConnection.Message m = DBusConnection.decode(message(DBusConnection.SIGNAL, 0, "ssvas",
                body -> {
                    body.string("org.freedesktop.appearance").string("color-scheme").signature("u").u32(2);
                    body.u32(0);   // placeholder length, patched below
                    int start = body.size();
                    body.string("a").string("bc");
                    body.patchU32(start - 4, body.size() - start);
                }));
        assertEquals(List.of("org.freedesktop.appearance", "color-scheme", 2L, List.of("a", "bc")), m.body());
    }

    @Test
    void rejectsValuesThatAreNotSettings() {
        assertNull(SystemPreferences.accentColor(List.of(-1.0, -1.0, -1.0)), "out of range means unset");
        assertNull(SystemPreferences.accentColor("nope"));
        assertNull(SystemPreferences.colorScheme(7L));
        assertNull(SystemPreferences.colorScheme(null));
    }

    @Test
    void encodesMethodCallsTheBusUnderstands() throws IOException {
        Assumptions.assumeTrue(DBusConnection.sessionBusSocket() != null, "no session bus here");
        try (DBusConnection bus = DBusConnection.openSessionBus()) {
            DBusConnection.Message reply = bus.call("org.freedesktop.DBus", "/org/freedesktop/DBus",
                    "org.freedesktop.DBus", "NameHasOwner", "org.freedesktop.DBus");
            assertEquals(List.of(true), reply.body());
        }
    }

    /** On a Linux desktop with a portal, the theme must follow the portal, not JavaFX's GTK guess. */
    @Test
    void followsThePortalWhenThereIsOne() throws Exception {
        Assumptions.assumeTrue(DBusConnection.sessionBusSocket() != null, "no session bus here");
        Integer expected;
        try (DBusConnection bus = DBusConnection.openSessionBus()) {
            expected = SystemPreferences.colorScheme(bus.call("org.freedesktop.portal.Desktop",
                    "/org/freedesktop/portal/desktop", "org.freedesktop.portal.Settings", "ReadOne",
                    "org.freedesktop.appearance", "color-scheme").body().get(0));
        } catch (IOException e) {
            expected = null;
        }
        Assumptions.assumeTrue(expected != null && expected != 0, "no portal, or no light / dark preference");
        FxTestSupport.start();
        assertEquals(expected == 1, FxTestSupport.onFx(SystemPreferences::isDark));
        assertTrue(SystemPreferences.portalScheme() != null);
    }

    /** A little-endian message with a reply serial, a body signature and a body built by {@code body}. */
    private static ByteBuffer message(byte type, int replySerial, String signature, Consumer<DBusConnection.Writer> body) {
        DBusConnection.Writer b = new DBusConnection.Writer();
        body.accept(b);
        byte[] bodyBytes = b.toByteArray();
        DBusConnection.Writer m = new DBusConnection.Writer();
        m.b((byte) 'l').b(type).b((byte) 0).b((byte) 1).u32(bodyBytes.length).u32(42);
        m.u32(0);
        int start = m.size();
        if (type == DBusConnection.SIGNAL) {
            m.align(8).b((byte) 3).signature("s").string("SettingChanged");
        } else {
            m.align(8).b((byte) 5).signature("u").u32(replySerial);
        }
        m.align(8).b((byte) 8).signature("g").signature(signature);
        m.patchU32(start - 4, m.size() - start);
        m.align(8).bytes(bodyBytes);
        return ByteBuffer.wrap(m.toByteArray()).order(ByteOrder.LITTLE_ENDIAN);
    }
}
