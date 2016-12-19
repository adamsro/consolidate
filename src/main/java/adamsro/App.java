package adamsro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;

import java.io.InputStreamReader;
import java.util.HashMap;

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
        String json = "{\n\"leads\": ";
        json += new GsonBuilder().setPrettyPrinting().create().toJson(DATA);
        return json + "\n}";
    }

    /**
     * {@see http://codereview.stackexchange.com/questions/27148/tips-on-multiple-key-map-wrapper}
     */
    public class MultiKeySet<K1, K2, V extends Comparable> {
        private final HashMap<K1, CompoundValue<K1, K2, V>> map1 = new HashMap<>();
        private final HashMap<K2, CompoundValue<K1, K2, V>> map2 = new HashMap<>();

        public MultiKeySet() {
        }

        public V add(K1 key1, K2 key2, V value) {
            final CompoundValue<K1, K2, V> compound = new CompoundValue<>(key1, key2, value);
            CompoundValue e1 = map1.get(key1);
            CompoundValue e2 = map2.get(key2);

            if (e1 == null && e2 == null) {
                // Value doesn't exist in the uniques collection.
                map1.put(key1, compound);
                map2.put(key2, compound);
                return null;
            } else if (e1 != null && e2 == null && value.compareTo(e1.value) >= 0) {
                // Found el with same key1 and older date.
                V del = removeByKey1(key1);
                map1.put(key1, compound);
                map2.put(key2, compound);
                return del;
            } else if (e1 == null && e2 != null && value.compareTo(e2.value) >= 0) {
                // Found el with same key2 and older date.
                V del = removeByKey2(key2);
                map1.put(key1, compound);
                map2.put(key2, compound);
                return del;
            } else if (e1.value.compareTo(e2.value) <= 0) {
                V del = removeByKey1(key1);
                map1.put(key1, compound);
                map2.put(key2, compound);
                return del;
            } else {
                V del = removeByKey2(key2);
                map1.put(key1, compound);
                map2.put(key2, compound);
                return del;
            }
        }

        public V removeByKey1(final K1 key1) {
            final CompoundValue<K1, K2, V> oldCompoundValue = map1.remove(key1);
            map2.remove(oldCompoundValue.key2);
            return oldCompoundValue.value;
        }

        public V removeByKey2(final K2 key2) {
            final CompoundValue<K1, K2, V> oldCompoundValue = map2.remove(key2);
            map1.remove(oldCompoundValue.key1);
            return oldCompoundValue.value;
        }

        private class CompoundValue<K1, K2, V extends Comparable> {
            K1 key1;
            K2 key2;
            V value;

            public CompoundValue(K1 key1, K2 key2, V value) {
                this.key1 = key1;
                this.key2 = key2;
                this.value = value;
            }
        }
    }
}

