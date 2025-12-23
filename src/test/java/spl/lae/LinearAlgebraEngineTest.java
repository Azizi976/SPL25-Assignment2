package spl.lae;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import parser.ComputationNode;
import parser.ComputationNodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LinearAlgebraEngineTest {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testSimpleAddition() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(2);

        ComputationNode matrixA = new ComputationNode(new double[][] { { 1.0, 2.0 }, { 3.0, 4.0 } });
        ComputationNode matrixB = new ComputationNode(new double[][] { { 5.0, 6.0 }, { 7.0, 8.0 } });

        List<ComputationNode> children = new ArrayList<>();
        children.add(matrixA);
        children.add(matrixB);

        ComputationNode addNode = new ComputationNode(ComputationNodeType.ADD, children);

        lae.loadAndCompute(addNode);

        double[][] result = addNode.getMatrix();
        assertNotNull(result, "Result matrix should not be null");
        assertEquals(6.0, result[0][0]);
        assertEquals(8.0, result[0][1]);
        assertEquals(10.0, result[1][0]);
        assertEquals(12.0, result[1][1]);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testSimpleMultiplication() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(2);

        ComputationNode matrixA = new ComputationNode(new double[][] { { 1.0, 2.0 } });
        ComputationNode matrixB = new ComputationNode(new double[][] { { 3.0 }, { 4.0 } });

        List<ComputationNode> children = new ArrayList<>();
        children.add(matrixA);
        children.add(matrixB);

        ComputationNode multNode = new ComputationNode(ComputationNodeType.MULTIPLY, children);

        lae.loadAndCompute(multNode);

        double[][] result = multNode.getMatrix();
        assertEquals(11.0, result[0][0]);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testDimensionMismatchThrowsError() {
        LinearAlgebraEngine lae = new LinearAlgebraEngine(2);

        ComputationNode matrixA = new ComputationNode(new double[][] { { 1.0, 2.0 } });
        ComputationNode matrixB = new ComputationNode(new double[][] { { 1.0, 2.0, 3.0 } });

        List<ComputationNode> children = new ArrayList<>();
        children.add(matrixA);
        children.add(matrixB);

        ComputationNode addNode = new ComputationNode(ComputationNodeType.ADD, children);

        assertThrows(IllegalArgumentException.class, () -> {
            lae.loadAndCompute(addNode);
        });
    }
}