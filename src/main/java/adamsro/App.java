package adamsro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Accept file path as argument. File must be JSON which structure like the following:
 * {
 * "leads": [
 * {
 * "_id": "",
 * "email": "",
 * "firstName": "",
 * "lastName": "",
 * "address": "",
 * "entryDate": "ISO 8601 formatted date"
 * },
 *
 * Program assumes data is to be processed sequentially from first entry in file to last, e.g.
 * {A, B}, {C, B}, {A, D} => {C, B},{A, D}
 *
 * Created by Robert 'Marshall' Adams on 12/18/16.`
 */
public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private final MultiKeySet<String, String, Entry> DATA = new MultiKeySet<>();

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java -jar target/consolidate-1.0-SNAPSHOT-jar-with-dependencies.jar path");
            return;
        }
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        App app = new App();
        app.jsonToStorage(new FileReader(args[0]));
        System.out.println(app.storageToJson());
    }

    /**
     * Load all JSON entries into a storage mechanism as defined by `MultiKeySet`.
     *
     * @param in An InputStream to be read by JsonReader
     */
    public void jsonToStorage(InputStreamReader in) throws Exception {
        JsonReader reader = new JsonReader(in);
        Gson gson = new GsonBuilder().create();

        reader.beginObject();
        reader.nextName(); // Consume `leads` object name.
        reader.beginArray(); // Consume `leads` array character.

        while (reader.hasNext()) {
            Entry newEntry = gson.fromJson(reader, Entry.class);
            Entry oldEntry = DATA.add(newEntry.get_id(), newEntry.getEmail(), newEntry);
            if (oldEntry != null) {
                // A replacement was made
                LOGGER.info(String.format(
                        "Fields updated for entry with id %s: %s => %s",
                        oldEntry.get_id(),
                        newEntry,
                        oldEntry
                ));
            }
        }
        reader.close();
    }

    /**
     * Retrieve all data from storage mechanism as JSON.
     *
     * @return JSON string with entries in leads array
     */
    public String storageToJson() throws Exception {
        Map<String, Collection<Entry>> leads = new HashMap<>();
        leads.put("leads", DATA.values());
        return new GsonBuilder().setPrettyPrinting().create().toJson(leads);
    }

    /**
     * Interface which forces both key1 and key2 to be unique in set on initial insert.
     * Implementation uses a HashMap for each key which points to the stored object. O(1) runtime.
     * Added as subclass for readability in this example.
     */
    public class MultiKeySet<K1, K2, V extends MultiKey> {
        private final HashMap<K1, V> map1 = new HashMap<>();
        private final HashMap<K2, V> map2 = new HashMap<>();

        public MultiKeySet() {
        }

        public V add(K1 key1, K2 key2, V v) {
            V e1 = map1.get(key1);
            V e2 = map2.get(key2);

            if (e1 == null && e2 == null) {
                // Value doesn't exist in the uniques collection.
                map1.put(key1, v);
                map2.put(key2, v);
                return null;
            } else if ((e1 != null && e2 == null && v.compareTo(e1) >= 0)
                    || (e1 != null && e2 != null && e2.compareTo(e1) <= 0 && e1.compareTo(v) <= 0)) {
                // All cases of: e2 <= e1 <= v
                V old = map1.remove(key1); // Replace smallest or oldest key.
                // Use old key's secondary key to remove secondary map ref.
                map2.remove(old.getKey2());
                map1.put(key1, v);
                map2.put(key2, v);
                return old;
            } else if ((e1 == null && e2 != null && v.compareTo(e2) >= 0)
                    || (e1 != null && e2 != null && e1.compareTo(e2) < 0 && e2.compareTo(v) <= 0)) {
                // All cases of: e1 < e2 <= v
                V old = map2.remove(key2);
                map1.remove(old.getKey1());
                map1.put(key1, v);
                map2.put(key2, v);
                return old;
            }
            // All cases of: (e1 || e2) > v
            return null;
        }

        public Collection<V> values() {
            return map1.values();
        }

        // TODO Have MultiKeySet implement collection
    }
}

