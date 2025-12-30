package spl.lae;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import parser.ComputationNode;
import parser.InputParser;
import parser.OutputWriter;

public class ExampleTest {

    @Test
    void testExample1() throws Exception {
        InputParser parser = new InputParser();
        ComputationNode root = parser.parse("Examples/example1.json");
        root.associativeNesting();
        LinearAlgebraEngine engine = new LinearAlgebraEngine(8);
        double[][] result = engine.run(root).getMatrix();
        
        OutputWriter.write(result, "target/test_out1.json");
        
        // Compare file contents
        String expected = Files.readString(Path.of("Examples/out1.json"));
        String actual = Files.readString(Path.of("target/test_out1.json"));
        
        assertEquals(expected, actual);
    }

    @Test
    void testExample2() throws Exception {
        InputParser parser = new InputParser();
        ComputationNode root = parser.parse("Examples/example2.json");
        root.associativeNesting();
        LinearAlgebraEngine engine = new LinearAlgebraEngine(8);
        double[][] result = engine.run(root).getMatrix();
        
        OutputWriter.write(result, "target/test_out2.json");
        
        // Compare file contents
        String expected = Files.readString(Path.of("Examples/out2.json"));
        String actual = Files.readString(Path.of("target/test_out2.json"));
        
        assertEquals(expected, actual);
    }

    @Test
    void testExample3() throws Exception {
        InputParser parser = new InputParser();
        ComputationNode root = parser.parse("Examples/example3.json");
        root.associativeNesting();
        LinearAlgebraEngine engine = new LinearAlgebraEngine(8);
        double[][] result = engine.run(root).getMatrix();
        
        OutputWriter.write(result, "target/test_out3.json");
        
        // Compare file contents
        String expected = Files.readString(Path.of("Examples/out3.json"));
        String actual = Files.readString(Path.of("target/test_out3.json"));
        
        assertEquals(expected, actual);
    }

    @Test
    void testExample4() throws Exception {
        InputParser parser = new InputParser();
        ComputationNode root = parser.parse("Examples/example4.json");
        root.associativeNesting();
        LinearAlgebraEngine engine = new LinearAlgebraEngine(8);
        double[][] result = engine.run(root).getMatrix();
        
        OutputWriter.write(result, "target/test_out4.json");
        
        // Compare file contents
        String expected = Files.readString(Path.of("Examples/out4.json"));
        String actual = Files.readString(Path.of("target/test_out4.json"));
        
        assertEquals(expected, actual);
    }

    @Test
    void testExample5() throws Exception {
        InputParser parser = new InputParser();
        ComputationNode root = parser.parse("Examples/example5.json");
        root.associativeNesting();
        LinearAlgebraEngine engine = new LinearAlgebraEngine(8);
        double[][] result = engine.run(root).getMatrix();
        
        OutputWriter.write(result, "target/test_out5.json");
        
        // Compare file contents
        String expected = Files.readString(Path.of("Examples/out5.json"));
        String actual = Files.readString(Path.of("target/test_out5.json"));
        
        assertEquals(expected, actual);
    }

    @Test
    void testExample6() throws Exception {
        InputParser parser = new InputParser();
        ComputationNode root = parser.parse("Examples/example6.json");
        root.associativeNesting();
        LinearAlgebraEngine engine = new LinearAlgebraEngine(8);
        double[][] result = engine.run(root).getMatrix();
        
        OutputWriter.write(result, "target/test_out6.json");
        
        // Compare file contents
        String expected = Files.readString(Path.of("Examples/out6.json"));
        String actual = Files.readString(Path.of("target/test_out6.json"));
        
        assertEquals(expected, actual);
    }
}