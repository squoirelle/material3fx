package io.github.squoirelle.material3fx.sample;

import javafx.concurrent.Task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A pretend backend: generated data, with a delay on every call so the application has real
 * loading states to show. Calls run on a background thread and return JavaFX {@link Task}s.
 */
public class InventoryService {

    public static final List<String> CATEGORIES = List.of("Electronics", "Tools", "Office", "Safety", "Packaging");
    public static final List<String> LOCATIONS = List.of("Aisle A", "Aisle B", "Aisle C", "Cold room", "Yard");

    private static final String[][] NAMES = {
            {"USB-C cable", "Barcode scanner", "Label printer", "Rugged tablet", "Wi-Fi access point", "Power bank"},
            {"Cordless drill", "Torque wrench", "Utility knife", "Socket set", "Tape measure", "Pallet jack"},
            {"Printer paper", "Ballpoint pens", "Clipboard", "Shipping labels", "Stapler", "Whiteboard markers"},
            {"Safety gloves", "Hard hat", "High-visibility vest", "Ear defenders", "First aid kit", "Safety glasses"},
            {"Cardboard boxes", "Stretch film", "Packing tape", "Bubble wrap", "Pallet straps", "Void fill"},
    };

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "stockroom-service");
        thread.setDaemon(true);
        return thread;
    });
    private final long delayMillis;

    public InventoryService(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    /** Loads the whole inventory. */
    public Task<List<Item>> loadItems() {
        return submit(() -> {
            Random random = new Random(42);
            List<Item> items = new ArrayList<>();
            for (int c = 0; c < CATEGORIES.size(); c++) {
                for (int n = 0; n < NAMES[c].length; n++) {
                    int reorder = 5 + random.nextInt(20);
                    int quantity = random.nextInt(10) == 0 ? 0 : random.nextInt(reorder * 6);
                    Item item = new Item(NAMES[c][n], String.format("%s-%03d", CATEGORIES.get(c).substring(0, 3).toUpperCase(), n + 1),
                            CATEGORIES.get(c), LOCATIONS.get(random.nextInt(LOCATIONS.size())), quantity, reorder);
                    item.setReceived(LocalDate.now().minusDays(random.nextInt(120)));
                    items.add(item);
                }
            }
            return items;
        });
    }

    /** Saves an item; returns it once stored. */
    public Task<Item> save(Item item) {
        return submit(() -> item);
    }

    private <T> Task<T> submit(java.util.concurrent.Callable<T> work) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                Thread.sleep(delayMillis);
                return work.call();
            }
        };
        executor.execute(task);
        return task;
    }
}
