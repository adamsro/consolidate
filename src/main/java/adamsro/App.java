package adamsro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
 * Program uses hashmaps instead of loops for O(1) insert time.
 *
 * Created by Robert 'Marshall' Adams on 12/18/16.`
 */
public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private final MultiKeySet<String, String, Entry> DATA = new MultiKeySet<>();

    /**
     * Limits max # of insertions to Long.MAX_VALUE (9223372036854775807) but this is required to
     * check insertion order when `entryDate` is identical.
     */
    private long queueNum = 0;

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
            newEntry.setQueueNum(queueNum++);
            List<Entry> oldEntries = DATA.add(newEntry.get_id(), newEntry.getEmail(), newEntry);
            for (Object oldEntry : oldEntries) {
                // A replacement was made
                LOGGER.info(String.format(
                        "Fields updated for entry with id %s: %s => %s",
                        ((Entry) oldEntry).get_id(), newEntry, oldEntry
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
        private HashMap<K1, V> map1 = new HashMap<>();
        private HashMap<K2, V> map2 = new HashMap<>();

        public MultiKeySet() {
        }

        public List<V> add(K1 key1, K2 key2, V v) {
            List<V> oldList = new ArrayList<>();
            V e1 = map1.get(key1);
            V e2 = map2.get(key2);
            if (e1 == null && e2 == null) {
                // Value doesn't exist in the uniques collection.
                put(key1, key2, v);
            } else if (e1 != null && e2 == null && e1.compareTo(v) < 0) {
                // null < e1 < v : Replace e1 with v
                oldList.add(removeByKey1(key1));
                put(key1, key2, v);
            } else if (e1 == null && e2 != null && e2.compareTo(v) < 0) {
                // null < e2 < v : replace e2 with v
                oldList.add(removeByKey2(key2));
                put(key1, key2, v);
            } else if (e2.compareTo(v) < 0 && e1.compareTo(v) < 0) {
                // (e1 && e2) < v : Replace e1 and e2 with v
                oldList.add(removeByKey2(key2));
                oldList.add(removeByKey1(key1));
                put(key1, key2, v);
            }
            // All cases of: (e1 || e2) > v
            return oldList;
        }

        private V removeByKey1(K1 key1) {
            V old = map1.remove(key1);
            map2.remove(old.getKey2());
            return old;
        }

        private V removeByKey2(K2 key2) {
            V old = map2.remove(key2);
            map1.remove(old.getKey1());
            return old;
        }

        private void put(K1 key1, K2 key2, V v) {
            map1.put(key1, v);
            map2.put(key2, v);
        }

        public Collection<V> values() {
            return map1.values();
        }

        // TODO Have MultiKeySet implement collection
    }
}

