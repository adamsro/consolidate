Reduce a JSON formatted set of leads to a set with all unique ids and emails. Program uses hashmaps instead of loops for O(1) insert time.
 
Build with: `mvn clean compile assembly:single`

Run with: `java -jar target/consolidate-1.0-SNAPSHOT-jar-with-dependencies.jar path`

Test with: `mvn test` 