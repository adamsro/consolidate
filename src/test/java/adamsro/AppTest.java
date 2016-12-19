package adamsro;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.FileReader;

import java.nio.file.Files;
import java.nio.file.Paths;

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
     * Rigourous Test :-)
     */
    public void testApp() throws Exception {

        App app1 = new App();
        FileReader test1In = new FileReader("src/test/resources/test1-input.json");
        String test1Out = new String(Files.readAllBytes(Paths.get("src/test/resources/test1-output.json")));
        app1.jsonToStorage(test1In);
        assertTrue(test1Out.equals(app1.storageToJson()));

        App app2 = new App();
        FileReader test2In = new FileReader("src/test/resources/test2-input.json");
        String test2Out = new String(Files.readAllBytes(Paths.get("src/test/resources/test2-output.json")));
        app2.jsonToStorage(test2In);
        assertTrue(test2Out.equals(app2.storageToJson()));
    }
}
