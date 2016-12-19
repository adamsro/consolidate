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

/**
 * Hello world!
 */
public class App {

//    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private final MultiKeySet<String, String, Entry> DATA = new MultiKeySet<String, String, Entry>();

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

    public void jsonToStorage(InputStreamReader in) throws Exception {
        JsonReader reader = new JsonReader(in);
        Gson gson = new GsonBuilder().create();

        reader.beginObject();
        reader.nextName(); // Consume `leads` object name.
        reader.beginArray(); // Consume `leads` array character.

        while (reader.hasNext()) {
            Entry e = gson.fromJson(reader, Entry.class);
            DATA.add(e.get_id(), e.getEmail(), e);
        }
        reader.close();
    }

    public String storageToJson() throws Exception {
        Map<String, Collection<Entry>> leads = new HashMap<>();
        leads.put("leads", DATA.values());
        return new GsonBuilder().setPrettyPrinting().create().toJson(leads);
    }

    /**
     * {@see http://codereview.stackexchange.com/questions/27148/tips-on-multiple-key-map-wrapper}
     */
    public class MultiKeySet<K1, K2, V extends  MultiKey> {
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
                    || (e1 != null && e2 != null && e1.compareTo(e2) <= 0)) {
                // Found el with same key1 and older date.
                // Remove by key 1
                V old = map1.remove(key1);
                map2.remove(old.getKey2());
                map1.put(key1, v);
                map2.put(key2, v);
                return old;
            } else if ((e1 == null && e2 != null && v.compareTo(e2) >= 0)
                    || (e1 != null && e2 != null)) {
                // Found el with same key2 and older date.
                // Remove by key 2
                V old = map2.remove(key2);
                map1.remove(old.getKey1());
                map1.put(key1, v);
                map2.put(key2, v);
                return old;
            }
            return null;
        }
        public Collection<V> values() {
            return map1.values();
        }
    }
}

