package adamsro;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.FileReader;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;

import jdk.nashorn.api.scripting.JSObject;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Given test data
     */
    public void testApp1() throws Exception {
        App app = new App();
        FileReader testIn = new FileReader("src/test/resources/test1-input.json");
        String testExpected = new String(Files.readAllBytes(Paths.get("src/test/resources/test1-output.json")));

        app.jsonToStorage(testIn);
        String testActual = app.storageToJson();
        JsonObject expected = new Gson().fromJson(testExpected, JsonObject.class);
        JsonObject actual = new Gson().fromJson(testActual, JsonObject.class);

        assertTrue(expected.equals(actual));
    }

    /**
     * Test for "Data from the newest date should be preferred".
     */
    public void testApp2() throws Exception {
        App app = new App();
        FileReader testIn = new FileReader("src/test/resources/test2-input.json");
        String testExpected = new String(Files.readAllBytes(Paths.get("src/test/resources/test2-output.json")));

        app.jsonToStorage(testIn);
        String testActual = app.storageToJson();
        JsonObject expected = new Gson().fromJson(testExpected, JsonObject.class);
        JsonObject actual = new Gson().fromJson(testActual, JsonObject.class);

        assertTrue(expected.equals(actual));
    }

    /**
     * Test for "If the dates are identical, the data from the record provided last in the list
     * should be preferred"
     */
    public void testApp3() throws Exception {
        App app = new App();
        FileReader testIn = new FileReader("src/test/resources/test3-input.json");
        String testExpected = new String(Files.readAllBytes(Paths.get("src/test/resources/test3-output.json")));

        app.jsonToStorage(testIn);
        String testActual = app.storageToJson();
        JsonObject expected = new Gson().fromJson(testExpected, JsonObject.class);
        JsonObject actual = new Gson().fromJson(testActual, JsonObject.class);

        assertTrue(expected.equals(actual));
    }
}
