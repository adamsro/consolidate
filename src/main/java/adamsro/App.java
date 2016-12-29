package adamsro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;

import java.io.InputStreamReader;
import java.util.*;
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
 * Created by Robert 'Marshall' Adams on 12/18/16.`
 */
public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private final OrKeySet<String, String, Entry> DATA = new OrKeySet<>();

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
            Set<Entry> oldEntries = DATA.add(newEntry);
            for (Entry oldEntry : oldEntries) {
                // A replacement was made
                LOGGER.info(String.format(
                        "Fields updated for entry with id %s: %s => %s",
                        oldEntry.get_id(), newEntry, oldEntry
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
    public class OrKeySet<K1, K2, E extends MultiKey<K1, K2>> {
        private final HashMap<K1, E> map1 = new HashMap<>();
        private final HashMap<K2, E> map2 = new HashMap<>();

        public OrKeySet() {}

        public Set<E> add(E e) {
            Set<E> oldList = new HashSet<>(2);
            E e1 = map1.get(e.getKey1());
            E e2 = map2.get(e.getKey2());
            if (e1 == null && e2 == null) {
                // Value doesn't exist in the uniques collection.
                put(e.getKey1(), e.getKey2(), e);
            } else if (e1 != null && e2 == null && e1.compareTo(e) < 0) {
                // null < e1 < v : Replace e1 with v
                oldList.add(removeByKey1(e.getKey1()));
                put(e.getKey1(), e.getKey2(), e);
            } else if (e1 == null && e2 != null && e2.compareTo(e) < 0) {
                // null < e2 < v : replace e2 with v
                oldList.add(removeByKey2(e.getKey2()));
                put(e.getKey1(), e.getKey2(), e);
            } else if (e2.compareTo(e) < 0 && e1.compareTo(e) < 0) {
                // (e1 && e2) < v : Replace e1 and e2 with v
                oldList.add(removeByKey2(e.getKey2()));
                oldList.add(removeByKey1(e.getKey1()));
                put(e.getKey1(), e.getKey2(), e);
            }
            // All cases of: (e1 || e2) > v
            return oldList;
        }

        private E removeByKey1(K1 key1) {
            E old = map1.remove(key1);
            map2.remove(old.getKey2());
            return old;
        }

        private E removeByKey2(K2 key2) {
            E old = map2.remove(key2);
            map1.remove(old.getKey1());
            return old;
        }

        private void put(K1 key1, K2 key2, E v) {
            map1.put(key1, v);
            map2.put(key2, v);
        }

        public Collection<E> values() {
            return map1.values();
        }

        // TODO Have MultiKeySet implement collection
    }
}

