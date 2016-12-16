package adamsro;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileReader;

/**
 * Hello world!
 */
public class App {

    // private static final Logger LOGGER = Logger.getLogger(App.class.getName());

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
        JsonElement json = new Gson().fromJson(new FileReader(args[0]), JsonElement.class);
        System.out.println(json.toString());
    }
}
