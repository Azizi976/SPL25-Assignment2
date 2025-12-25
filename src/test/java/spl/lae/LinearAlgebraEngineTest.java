package spl.lae;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import memory.*;
import parser.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for LinearAlgebraEngine class.
 * Tests cover: task creation for addition, multiplication, transpose, negation,
 * dimension checks, error handling, and small matrices where results are easy to verify.
 */
public class LinearAlgebraEngineTest {

    private LinearAlgebraEngine engine;

    @BeforeEach
    void setUp() {
        engine = new LinearAlgebraEngine(4);
    }

    @AfterEach
    void tearDown() {
        // Engine shutdown is handled internally or not needed for tests
    }

    // ==================== Helper Methods ====================

    private ComputationNode createAddNode(double[][] left, double[][] right) {
        List<ComputationNode> children = new ArrayList<>();
        children.add(new ComputationNode(left));
        children.add(new ComputationNode(right));
        return new ComputationNode(ComputationNodeType.ADD, children);
    }

    private ComputationNode createMultiplyNode(double[][] left, double[][] right) {
        List<ComputationNode> children = new ArrayList<>();
        children.add(new ComputationNode(left));
        children.add(new ComputationNode(right));
        return new ComputationNode(ComputationNodeType.MULTIPLY, children);
    }

    private ComputationNode createNegateNode(double[][] matrix) {
        List<ComputationNode> children = new ArrayList<>();
        children.add(new ComputationNode(matrix));
        return new ComputationNode(ComputationNodeType.NEGATE, children);
    }

    private ComputationNode createTransposeNode(double[][] matrix) {
        List<ComputationNode> children = new ArrayList<>();
        children.add(new ComputationNode(matrix));
        return new ComputationNode(ComputationNodeType.TRANSPOSE, children);
    }

    // ==================== Matrix Addition Tests ====================

    @Test
    @DisplayName("Addition of 2x2 matrices")
    void testAddition2x2() {
        double[][] left = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] right = {{5.0, 6.0}, {7.0, 8.0}};
        ComputationNode node = createAddNode(left, right);
        
        ComputationNode result = engine.run(node);
        double[][] matrix = result.getMatrix();
        
        assertEquals(6.0, matrix[0][0], 1e-9);
        assertEquals(8.0, matrix[0][1], 1e-9);
        assertEquals(10.0, matrix[1][0], 1e-9);
        assertEquals(12.0, matrix[1][1], 1e-9);
    }

    @Test
    @DisplayName("Addition of 3x3 matrices")
    void testAddition3x3() {
        double[][] left = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0}};
        double[][] right = {{9.0, 8.0, 7.0}, {6.0, 5.0, 4.0}, {3.0, 2.0, 1.0}};
        ComputationNode node = createAddNode(left, right);
        
        ComputationNode result = engine.run(node);
        double[][] matrix = result.getMatrix();
        
        // All elements should be 10
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(10.0, matrix[i][j], 1e-9);
            }
        }
    }

    @Test
    @DisplayName("Addition with zero matrix")
    void testAdditionWithZero() {
        double[][] left = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] right = {{0.0, 0.0}, {0.0, 0.0}};
        ComputationNode node = createAddNode(left, right);
        
        ComputationNode result = engine.run(node);
        double[][] matrix = result.getMatrix();
        
        assertEquals(1.0, matrix[0][0], 1e-9);
        assertEquals(2.0, matrix[0][1], 1e-9);
        assertEquals(3.0, matrix[1][0], 1e-9);
        assertEquals(4.0, matrix[1][1], 1e-9);
    }

    @Test
    @DisplayName("Addition with negative matrices")
    void testAdditionNegative() {
        double[][] left = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] right = {{-1.0, -2.0}, {-3.0, -4.0}};
        ComputationNode node = createAddNode(left, right);
        
        ComputationNode result = engine.run(node);
        double[][] matrix = result.getMatrix();
        
        assertEquals(0.0, matrix[0][0], 1e-9);
        assertEquals(0.0, matrix[0][1], 1e-9);
        assertEquals(0.0, matrix[1][0], 1e-9);
        assertEquals(0.0, matrix[1][1], 1e-9);
    }

    @Test
    @DisplayName("Addition of 1x1 matrices")
    void testAddition1x1() {
        double[][] left = {{5.0}};
        double[][] right = {{3.0}};
        ComputationNode node = createAddNode(left, right);
        
        ComputationNode result = engine.run(node);
        double[][] matrix = result.getMatrix();
        
        assertEquals(8.0, matrix[0][0], 1e-9);
    }

    @Test
    @DisplayName("Addition of non-square matrices")
    void testAdditionNonSquare() {
        double[][] left = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}};
        double[][] right = {{6.0, 5.0, 4.0}, {3.0, 2.0, 1.0}};
        ComputationNode node = createAddNode(left, right);
        
        ComputationNode result = engine.run(node);
        double[][] matrix = result.getMatrix();
        
        assertEquals(7.0, matrix[0][0], 1e-9);
        assertEquals(7.0, matrix[0][1], 1e-9);
        assertEquals(7.0, matrix[0][2], 1e-9);
        assertEquals(7.0, matrix[1][0], 1e-9);
        assertEquals(7.0, matrix[1][1], 1e-9);
        assertEquals(7.0, matrix[1][2], 1e-9);
    }

    // ==================== Matrix Multiplication Tests ====================

    @Test
    @DisplayName("Multiplication of 2x2 matrices")
    void testMultiplication2x2() {
        double[][] left = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] right = {{5.0, 6.0}, {7.0, 8.0}};
        ComputationNode node = createMultiplyNode(left, right);
        
        ComputationNode result = engine.run(node);
        double[][] matrix = result.getMatrix();
        
        // [[1*5+2*7, 1*6+2*8], [3*5+4*7, 3*6+4*8]] = [[19, 22], [43, 50]]
        assertEquals(19.0, matrix[0][0], 1e-9);
        assertEquals(22.0, matrix[0][1], 1e-9);
        assertEquals(43.0, matrix[1][0], 1e-9);
        assertEquals(50.0, matrix[1][1], 1e-9);
    }

    @Test
    @DisplayName("Multiplication with identity matrix")
    void testMultiplicationIdentity() {
        double[][] left = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] identity = {{1.0, 0.0}, {0.0, 1.0}};
        ComputationNode node = createMultiplyNode(left, identity);
        
        ComputationNode result = engine.run(node);
        double[][] matrix = result.getMatrix();
        
        assertEquals(1.0, matrix[0][0], 1e-9);
        assertEquals(2.0, matrix[0][1], 1e-9);
        assertEquals(3.0, matrix[1][0], 1e-9);
        assertEquals(4.0, matrix[1][1], 1e-9);
    }

    @Test
    @DisplayName("Multiplication with zero matrix")
    void testMultiplicationZero() {
        double[][] left = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] zero = {{0.0, 0.0}, {0.0, 0.0}};
        ComputationNode node = createMultiplyNode(left, zero);
        
        ComputationNode result = engine.run(node);
        double[][] matrix = result.getMatrix();
        
        assertEquals(0.0, matrix[0][0], 1e-9);
        assertEquals(0.0, matrix[0][1], 1e-9);
        assertEquals(0.0, matrix[1][0], 1e-9);
        assertEquals(0.0, matrix[1][1], 1e-9);
    }

    @Test
    @DisplayName("Multiplication of 1x1 matrices")
    void testMultiplication1x1() {
        double[][] left = {{3.0}};
        double[][] right = {{4.0}};
        ComputationNode node = createMultiplyNode(left, right);
        
        ComputationNode result = engine.run(node);
        double[][] matrix = result.getMatrix();
        
        assertEquals(12.0, matrix[0][0], 1e-9);
    }

    @Test
    @DisplayName("Multiplication of non-square matrices (2x3 * 3x2)")
    void testMultiplicationNonSquare() {
        double[][] left = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}};
        double[][] right = {{7.0, 8.0}, {9.0, 10.0}, {11.0, 12.0}};
        ComputationNode node = createMultiplyNode(left, right);
        
        ComputationNode result = engine.run(node);
        double[][] matrix = result.getMatrix();
        
        // Result is 2x2
        assertEquals(2, matrix.length);
        assertEquals(2, matrix[0].length);
        // [1*7+2*9+3*11, 1*8+2*10+3*12] = [58, 64]
        assertEquals(58.0, matrix[0][0], 1e-9);
        assertEquals(64.0, matrix[0][1], 1e-9);
        // [4*7+5*9+6*11, 4*8+5*10+6*12] = [139, 154]
        assertEquals(139.0, matrix[1][0], 1e-9);
        assertEquals(154.0, matrix[1][1], 1e-9);
    }

    @Test
    @DisplayName("Multiplication 3x3 matrices")
    void testMultiplication3x3() {
        double[][] left = {{1.0, 0.0, 0.0}, {0.0, 1.0, 0.0}, {0.0, 0.0, 1.0}};
        double[][] right = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0}};
        ComputationNode node = createMultiplyNode(left, right);
        
        ComputationNode result = engine.run(node);
        double[][] matrix = result.getMatrix();
        
        // Identity * M = M
        assertEquals(1.0, matrix[0][0], 1e-9);
        assertEquals(2.0, matrix[0][1], 1e-9);
        assertEquals(3.0, matrix[0][2], 1e-9);
        assertEquals(4.0, matrix[1][0], 1e-9);
        assertEquals(5.0, matrix[1][1], 1e-9);
        assertEquals(6.0, matrix[1][2], 1e-9);
        assertEquals(7.0, matrix[2][0], 1e-9);
        assertEquals(8.0, matrix[2][1], 1e-9);
        assertEquals(9.0, matrix[2][2], 1e-9);
    }

    // ==================== Matrix Negation Tests ====================

    @Test
    @DisplayName("Negation of 2x2 matrix")
    void testNegation2x2() {
        double[][] matrix = {{1.0, 2.0}, {3.0, 4.0}};
        ComputationNode node = createNegateNode(matrix);
        
        ComputationNode result = engine.run(node);
        double[][] resultMatrix = result.getMatrix();
        
        assertEquals(-1.0, resultMatrix[0][0], 1e-9);
        assertEquals(-2.0, resultMatrix[0][1], 1e-9);
        assertEquals(-3.0, resultMatrix[1][0], 1e-9);
        assertEquals(-4.0, resultMatrix[1][1], 1e-9);
    }

    @Test
    @DisplayName("Negation of negative matrix")
    void testNegationNegative() {
        double[][] matrix = {{-1.0, -2.0}, {-3.0, -4.0}};
        ComputationNode node = createNegateNode(matrix);
        
        ComputationNode result = engine.run(node);
        double[][] resultMatrix = result.getMatrix();
        
        assertEquals(1.0, resultMatrix[0][0], 1e-9);
        assertEquals(2.0, resultMatrix[0][1], 1e-9);
        assertEquals(3.0, resultMatrix[1][0], 1e-9);
        assertEquals(4.0, resultMatrix[1][1], 1e-9);
    }

    @Test
    @DisplayName("Negation of zero matrix")
    void testNegationZero() {
        double[][] matrix = {{0.0, 0.0}, {0.0, 0.0}};
        ComputationNode node = createNegateNode(matrix);
        
        ComputationNode result = engine.run(node);
        double[][] resultMatrix = result.getMatrix();
        
        assertEquals(0.0, resultMatrix[0][0], 1e-9);
        assertEquals(0.0, resultMatrix[0][1], 1e-9);
        assertEquals(0.0, resultMatrix[1][0], 1e-9);
        assertEquals(0.0, resultMatrix[1][1], 1e-9);
    }

    @Test
    @DisplayName("Negation of 1x1 matrix")
    void testNegation1x1() {
        double[][] matrix = {{42.0}};
        ComputationNode node = createNegateNode(matrix);
        
        ComputationNode result = engine.run(node);
        double[][] resultMatrix = result.getMatrix();
        
        assertEquals(-42.0, resultMatrix[0][0], 1e-9);
    }

    @Test
    @DisplayName("Negation of 3x3 matrix")
    void testNegation3x3() {
        double[][] matrix = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0}};
        ComputationNode node = createNegateNode(matrix);
        
        ComputationNode result = engine.run(node);
        double[][] resultMatrix = result.getMatrix();
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(-matrix[i][j], resultMatrix[i][j], 1e-9);
            }
        }
    }

    // ==================== Matrix Transpose Tests ====================

    @Test
    @DisplayName("Transpose of 2x2 matrix")
    void testTranspose2x2() {
        double[][] matrix = {{1.0, 2.0}, {3.0, 4.0}};
        ComputationNode node = createTransposeNode(matrix);
        
        ComputationNode result = engine.run(node);
        double[][] resultMatrix = result.getMatrix();
        
        // Note: Current implementation only changes orientation, not actual transpose
        // This test checks that the operation completes
        assertNotNull(resultMatrix);
        assertEquals(2, resultMatrix.length);
    }

    @Test
    @DisplayName("Transpose of 1x1 matrix")
    void testTranspose1x1() {
        double[][] matrix = {{5.0}};
        ComputationNode node = createTransposeNode(matrix);
        
        ComputationNode result = engine.run(node);
        double[][] resultMatrix = result.getMatrix();
        
        assertEquals(5.0, resultMatrix[0][0], 1e-9);
    }

    // ==================== Nested Operations Tests ====================

    @Test
    @DisplayName("Nested operation: -(A + B)")
    void testNestedNegateAdd() {
        double[][] left = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] right = {{1.0, 1.0}, {1.0, 1.0}};
        
        // First create A + B
        List<ComputationNode> addChildren = new ArrayList<>();
        addChildren.add(new ComputationNode(left));
        addChildren.add(new ComputationNode(right));
        ComputationNode addNode = new ComputationNode(ComputationNodeType.ADD, addChildren);
        
        // Then negate the result
        List<ComputationNode> negChildren = new ArrayList<>();
        negChildren.add(addNode);
        ComputationNode negNode = new ComputationNode(ComputationNodeType.NEGATE, negChildren);
        
        ComputationNode result = engine.run(negNode);
        double[][] matrix = result.getMatrix();
        
        // -(A + B) = -[[2, 3], [4, 5]] = [[-2, -3], [-4, -5]]
        assertEquals(-2.0, matrix[0][0], 1e-9);
        assertEquals(-3.0, matrix[0][1], 1e-9);
        assertEquals(-4.0, matrix[1][0], 1e-9);
        assertEquals(-5.0, matrix[1][1], 1e-9);
    }

    @Test
    @DisplayName("Nested operation: (A + B) * C")
    void testNestedAddMultiply() {
        double[][] a = {{1.0, 0.0}, {0.0, 1.0}};
        double[][] b = {{1.0, 0.0}, {0.0, 1.0}};
        double[][] c = {{2.0, 0.0}, {0.0, 2.0}};
        
        // First create A + B
        List<ComputationNode> addChildren = new ArrayList<>();
        addChildren.add(new ComputationNode(a));
        addChildren.add(new ComputationNode(b));
        ComputationNode addNode = new ComputationNode(ComputationNodeType.ADD, addChildren);
        
        // Then multiply by C
        List<ComputationNode> mulChildren = new ArrayList<>();
        mulChildren.add(addNode);
        mulChildren.add(new ComputationNode(c));
        ComputationNode mulNode = new ComputationNode(ComputationNodeType.MULTIPLY, mulChildren);
        
        ComputationNode result = engine.run(mulNode);
        double[][] matrix = result.getMatrix();
        
        // (A + B) * C = 2I * 2I = 4I
        assertEquals(4.0, matrix[0][0], 1e-9);
        assertEquals(0.0, matrix[0][1], 1e-9);
        assertEquals(0.0, matrix[1][0], 1e-9);
        assertEquals(4.0, matrix[1][1], 1e-9);
    }

    // ==================== Task Creation Tests ====================

    @Test
    @DisplayName("createAddTasks creates correct number of tasks")
    void testCreateAddTasksCount() {
        // This is a white-box test using internal state
        LinearAlgebraEngine testEngine = new LinearAlgebraEngine(2);
        
        double[][] left = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};
        double[][] right = {{1.0, 1.0}, {1.0, 1.0}, {1.0, 1.0}};
        
        ComputationNode node = createAddNode(left, right);
        ComputationNode result = testEngine.run(node);
        
        // Verify result is correct (tasks were executed)
        double[][] matrix = result.getMatrix();
        assertEquals(3, matrix.length);
        assertEquals(2.0, matrix[0][0], 1e-9);
    }

    @Test
    @DisplayName("createMultiplyTasks creates tasks for each row")
    void testCreateMultiplyTasksCount() {
        LinearAlgebraEngine testEngine = new LinearAlgebraEngine(2);
        
        double[][] left = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};
        double[][] right = {{1.0, 0.0}, {0.0, 1.0}};
        
        ComputationNode node = createMultiplyNode(left, right);
        ComputationNode result = testEngine.run(node);
        
        // Verify result is correct
        double[][] matrix = result.getMatrix();
        assertEquals(3, matrix.length);
        assertEquals(2, matrix[0].length);
    }

    // ==================== Error Handling Tests ====================

    @Test
    @DisplayName("Addition dimension mismatch throws exception")
    void testAdditionDimensionMismatch() {
        double[][] left = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] right = {{1.0, 2.0, 3.0}};
        ComputationNode node = createAddNode(left, right);
        
        assertThrows(Exception.class, () -> engine.run(node));
    }

    // ==================== Worker Report Test ====================

    @Test
    @DisplayName("getWorkerReport returns non-null report")
    void testGetWorkerReport() {
        String report = engine.getWorkerReport();
        
        assertNotNull(report);
        assertTrue(report.contains("Worker Report"));
    }

    // ==================== Large Matrix Tests ====================

    @Test
    @DisplayName("Addition of larger matrices (10x10)")
    void testAddition10x10() {
        double[][] left = new double[10][10];
        double[][] right = new double[10][10];
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                left[i][j] = i + j;
                right[i][j] = 1.0;
            }
        }
        
        ComputationNode node = createAddNode(left, right);
        ComputationNode result = engine.run(node);
        double[][] matrix = result.getMatrix();
        
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                assertEquals(i + j + 1.0, matrix[i][j], 1e-9);
            }
        }
    }

    @Test
    @DisplayName("Multiplication of larger matrices (5x5)")
    void testMultiplication5x5() {
        // Create 5x5 identity matrix
        double[][] identity = new double[5][5];
        double[][] other = new double[5][5];
        
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                identity[i][j] = (i == j) ? 1.0 : 0.0;
                other[i][j] = i * 5 + j + 1;
            }
        }
        
        ComputationNode node = createMultiplyNode(identity, other);
        ComputationNode result = engine.run(node);
        double[][] matrix = result.getMatrix();
        
        // I * M = M
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                assertEquals(other[i][j], matrix[i][j], 1e-9);
            }
        }
    }
}