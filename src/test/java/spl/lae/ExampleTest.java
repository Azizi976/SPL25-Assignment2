
package spl.lae;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import parser.*;

public class ExampleTest {

@Test
void testIntegrationComplex() throws Exception {
    InputParser parser = new InputParser();
    ComputationNode root = parser.parse("example.json");
    LinearAlgebraEngine engine = new LinearAlgebraEngine(8);
    double[][] result = engine.run(root).getMatrix();
    
    OutputWriter.write(result, "test_out.json");
    
    // Compare file contents
    String expected = Files.readString(Path.of("out.json"));
    String actual = Files.readString(Path.of("test_out.json"));
    
    assertEquals(expected, actual);
    }
}
