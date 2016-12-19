package adamsro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;

import java.io.InputStreamReader;
import java.util.TreeSet;

/**
 * Hello world!
 */
public class App {

//    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private static final TreeSet<Entry> DATA = new TreeSet<Entry>();

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
            DATA.add(gson.fromJson(reader, Entry.class));
        }
        reader.close();
    }


    public String storageToJson() throws Exception {
        String json = "{n\"leads\": ";
        json += new GsonBuilder().setPrettyPrinting().create().toJson(DATA);
        return json + "\n}";
    }

}

