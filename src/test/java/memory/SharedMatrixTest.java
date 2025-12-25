package memory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SharedMatrix class.
 * Tests cover: construction, loading row/column major, reading, dimension checks,
 * and edge cases including small matrices where results are easy to verify.
 */
public class SharedMatrixTest {

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Default constructor creates empty matrix")
    void testDefaultConstructor() {
        SharedMatrix matrix = new SharedMatrix();
        assertEquals(0, matrix.length());
    }

    @Test
    @DisplayName("Constructor with 2D array creates correct matrix")
    void testConstructorWithArray() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        assertEquals(2, matrix.length());
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());
    }

    @Test
    @DisplayName("Constructor preserves matrix values")
    void testConstructorPreservesValues() {
        double[][] data = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        assertEquals(1.0, matrix.get(0).get(0), 1e-9);
        assertEquals(2.0, matrix.get(0).get(1), 1e-9);
        assertEquals(3.0, matrix.get(0).get(2), 1e-9);
        assertEquals(4.0, matrix.get(1).get(0), 1e-9);
        assertEquals(5.0, matrix.get(1).get(1), 1e-9);
        assertEquals(6.0, matrix.get(1).get(2), 1e-9);
    }

    // ==================== Load Row Major Tests ====================

    @Test
    @DisplayName("loadRowMajor creates row-major matrix")
    void testLoadRowMajor() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};
        
        matrix.loadRowMajor(data);
        
        assertEquals(3, matrix.length());
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());
    }

    @Test
    @DisplayName("loadRowMajor preserves values correctly")
    void testLoadRowMajorValues() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] data = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}};
        
        matrix.loadRowMajor(data);
        
        // Check first row
        assertEquals(1.0, matrix.get(0).get(0), 1e-9);
        assertEquals(2.0, matrix.get(0).get(1), 1e-9);
        assertEquals(3.0, matrix.get(0).get(2), 1e-9);
        
        // Check second row
        assertEquals(4.0, matrix.get(1).get(0), 1e-9);
        assertEquals(5.0, matrix.get(1).get(1), 1e-9);
        assertEquals(6.0, matrix.get(1).get(2), 1e-9);
    }

    @Test
    @DisplayName("loadRowMajor replaces previous data")
    void testLoadRowMajorReplaces() {
        double[][] data1 = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] data2 = {{5.0, 6.0, 7.0}};
        SharedMatrix matrix = new SharedMatrix(data1);
        
        matrix.loadRowMajor(data2);
        
        assertEquals(1, matrix.length());
        assertEquals(5.0, matrix.get(0).get(0), 1e-9);
    }

    @Test
    @DisplayName("loadRowMajor with single row matrix")
    void testLoadRowMajorSingleRow() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] data = {{1.0, 2.0, 3.0, 4.0, 5.0}};
        
        matrix.loadRowMajor(data);
        
        assertEquals(1, matrix.length());
        assertEquals(5, matrix.get(0).length());
    }

    @Test
    @DisplayName("loadRowMajor with single column matrix")
    void testLoadRowMajorSingleColumn() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] data = {{1.0}, {2.0}, {3.0}};
        
        matrix.loadRowMajor(data);
        
        assertEquals(3, matrix.length());
        assertEquals(1, matrix.get(0).length());
    }

    // ==================== Load Column Major Tests ====================

    @Test
    @DisplayName("loadColumnMajor creates column-major matrix")
    void testLoadColumnMajor() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};
        
        matrix.loadColumnMajor(data);
        
        assertEquals(2, matrix.length());  // Number of columns
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix.getOrientation());
    }

    @Test
    @DisplayName("loadColumnMajor stores columns as vectors")
    void testLoadColumnMajorValues() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};
        
        matrix.loadColumnMajor(data);
        
        // First column [1, 3, 5]
        assertEquals(1.0, matrix.get(0).get(0), 1e-9);
        assertEquals(3.0, matrix.get(0).get(1), 1e-9);
        assertEquals(5.0, matrix.get(0).get(2), 1e-9);
        
        // Second column [2, 4, 6]
        assertEquals(2.0, matrix.get(1).get(0), 1e-9);
        assertEquals(4.0, matrix.get(1).get(1), 1e-9);
        assertEquals(6.0, matrix.get(1).get(2), 1e-9);
    }

    // ==================== Read Row Major Tests ====================

    @Test
    @DisplayName("readRowMajor returns correct values for row-major matrix")
    void testReadRowMajorFromRowMajor() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        double[][] result = matrix.readRowMajor();
        
        assertEquals(2, result.length);
        assertEquals(2, result[0].length);
        assertEquals(1.0, result[0][0], 1e-9);
        assertEquals(2.0, result[0][1], 1e-9);
        assertEquals(3.0, result[1][0], 1e-9);
        assertEquals(4.0, result[1][1], 1e-9);
    }

    @Test
    @DisplayName("readRowMajor returns correct values for column-major matrix")
    void testReadRowMajorFromColumnMajor() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};
        matrix.loadColumnMajor(data);
        
        double[][] result = matrix.readRowMajor();
        
        // Original matrix (3x2) stored as columns should read back as 3x2
        assertEquals(3, result.length);
        assertEquals(2, result[0].length);
        assertEquals(1.0, result[0][0], 1e-9);
        assertEquals(2.0, result[0][1], 1e-9);
        assertEquals(3.0, result[1][0], 1e-9);
        assertEquals(4.0, result[1][1], 1e-9);
        assertEquals(5.0, result[2][0], 1e-9);
        assertEquals(6.0, result[2][1], 1e-9);
    }

    @Test
    @DisplayName("readRowMajor returns empty array for empty matrix")
    void testReadRowMajorEmpty() {
        SharedMatrix matrix = new SharedMatrix();
        
        double[][] result = matrix.readRowMajor();
        
        assertEquals(0, result.length);
    }

    // ==================== Get and Length Tests ====================

    @Test
    @DisplayName("get returns correct SharedVector at index")
    void testGet() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        SharedVector row0 = matrix.get(0);
        SharedVector row1 = matrix.get(1);
        SharedVector row2 = matrix.get(2);
        
        assertEquals(1.0, row0.get(0), 1e-9);
        assertEquals(3.0, row1.get(0), 1e-9);
        assertEquals(5.0, row2.get(0), 1e-9);
    }

    @Test
    @DisplayName("length returns number of vectors")
    void testLength() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        assertEquals(3, matrix.length());
    }

    @Test
    @DisplayName("length returns 0 for empty matrix")
    void testLengthEmpty() {
        SharedMatrix matrix = new SharedMatrix();
        
        assertEquals(0, matrix.length());
    }

    // ==================== Orientation Tests ====================

    @Test
    @DisplayName("getOrientation returns ROW_MAJOR for row-major matrix")
    void testGetOrientationRowMajor() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());
    }

    @Test
    @DisplayName("getOrientation returns COLUMN_MAJOR for column-major matrix")
    void testGetOrientationColumnMajor() {
        SharedMatrix matrix = new SharedMatrix();
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        matrix.loadColumnMajor(data);
        
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix.getOrientation());
    }

    @Test
    @DisplayName("getOrientation throws exception for empty matrix")
    void testGetOrientationEmpty() {
        SharedMatrix matrix = new SharedMatrix();
        
        assertThrows(IllegalArgumentException.class, () -> matrix.getOrientation());
    }

    // ==================== Matrix Operations (via vectors) Tests ====================

    @Test
    @DisplayName("Matrix addition via vector operations - 2x2")
    void testMatrixAdditionViaVectors() {
        double[][] data1 = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] data2 = {{5.0, 6.0}, {7.0, 8.0}};
        SharedMatrix m1 = new SharedMatrix(data1);
        SharedMatrix m2 = new SharedMatrix(data2);
        
        // Add row by row
        for (int i = 0; i < m1.length(); i++) {
            m1.get(i).writeLock();
            m2.get(i).readLock();
            try {
                m1.get(i).add(m2.get(i));
            } finally {
                m1.get(i).writeUnlock();
                m2.get(i).readUnlock();
            }
        }
        
        double[][] result = m1.readRowMajor();
        assertEquals(6.0, result[0][0], 1e-9);
        assertEquals(8.0, result[0][1], 1e-9);
        assertEquals(10.0, result[1][0], 1e-9);
        assertEquals(12.0, result[1][1], 1e-9);
    }

    @Test
    @DisplayName("Matrix negation via vector operations - 2x2")
    void testMatrixNegationViaVectors() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        // Negate row by row
        for (int i = 0; i < matrix.length(); i++) {
            matrix.get(i).writeLock();
            try {
                matrix.get(i).negate();
            } finally {
                matrix.get(i).writeUnlock();
            }
        }
        
        double[][] result = matrix.readRowMajor();
        assertEquals(-1.0, result[0][0], 1e-9);
        assertEquals(-2.0, result[0][1], 1e-9);
        assertEquals(-3.0, result[1][0], 1e-9);
        assertEquals(-4.0, result[1][1], 1e-9);
    }

    // ==================== Small Matrices - Easy to Verify ====================

    @Test
    @DisplayName("1x1 matrix operations")
    void test1x1Matrix() {
        double[][] data = {{5.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        assertEquals(1, matrix.length());
        assertEquals(1, matrix.get(0).length());
        assertEquals(5.0, matrix.get(0).get(0), 1e-9);
        
        double[][] read = matrix.readRowMajor();
        assertEquals(5.0, read[0][0], 1e-9);
    }

    @Test
    @DisplayName("2x2 identity matrix")
    void test2x2IdentityMatrix() {
        double[][] data = {{1.0, 0.0}, {0.0, 1.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        double[][] result = matrix.readRowMajor();
        assertEquals(1.0, result[0][0], 1e-9);
        assertEquals(0.0, result[0][1], 1e-9);
        assertEquals(0.0, result[1][0], 1e-9);
        assertEquals(1.0, result[1][1], 1e-9);
    }

    @Test
    @DisplayName("3x3 matrix with known values")
    void test3x3Matrix() {
        double[][] data = {
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0},
            {7.0, 8.0, 9.0}
        };
        SharedMatrix matrix = new SharedMatrix(data);
        
        assertEquals(3, matrix.length());
        
        double[][] result = matrix.readRowMajor();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(data[i][j], result[i][j], 1e-9);
            }
        }
    }

    // ==================== Non-Square Matrices ====================

    @Test
    @DisplayName("2x3 matrix (more columns than rows)")
    void test2x3Matrix() {
        double[][] data = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        assertEquals(2, matrix.length());
        assertEquals(3, matrix.get(0).length());
        
        double[][] result = matrix.readRowMajor();
        assertEquals(1.0, result[0][0], 1e-9);
        assertEquals(6.0, result[1][2], 1e-9);
    }

    @Test
    @DisplayName("3x2 matrix (more rows than columns)")
    void test3x2Matrix() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        assertEquals(3, matrix.length());
        assertEquals(2, matrix.get(0).length());
    }

    @Test
    @DisplayName("1xN row vector matrix")
    void test1xNMatrix() {
        double[][] data = {{1.0, 2.0, 3.0, 4.0, 5.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        assertEquals(1, matrix.length());
        assertEquals(5, matrix.get(0).length());
    }

    @Test
    @DisplayName("Nx1 column vector matrix")
    void testNx1Matrix() {
        double[][] data = {{1.0}, {2.0}, {3.0}, {4.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        assertEquals(4, matrix.length());
        assertEquals(1, matrix.get(0).length());
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Matrix with all zeros")
    void testZeroMatrix() {
        double[][] data = {{0.0, 0.0}, {0.0, 0.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        double[][] result = matrix.readRowMajor();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                assertEquals(0.0, result[i][j], 1e-9);
            }
        }
    }

    @Test
    @DisplayName("Matrix with negative values")
    void testNegativeMatrix() {
        double[][] data = {{-1.0, -2.0}, {-3.0, -4.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        double[][] result = matrix.readRowMajor();
        assertEquals(-1.0, result[0][0], 1e-9);
        assertEquals(-4.0, result[1][1], 1e-9);
    }

    @Test
    @DisplayName("Matrix with mixed positive and negative values")
    void testMixedMatrix() {
        double[][] data = {{-1.0, 2.0}, {3.0, -4.0}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        double[][] result = matrix.readRowMajor();
        assertEquals(-1.0, result[0][0], 1e-9);
        assertEquals(2.0, result[0][1], 1e-9);
        assertEquals(3.0, result[1][0], 1e-9);
        assertEquals(-4.0, result[1][1], 1e-9);
    }

    @Test
    @DisplayName("Matrix with floating point values")
    void testFloatingPointMatrix() {
        double[][] data = {{1.5, 2.7}, {3.14159, 2.71828}};
        SharedMatrix matrix = new SharedMatrix(data);
        
        double[][] result = matrix.readRowMajor();
        assertEquals(1.5, result[0][0], 1e-9);
        assertEquals(2.7, result[0][1], 1e-9);
        assertEquals(3.14159, result[1][0], 1e-9);
        assertEquals(2.71828, result[1][1], 1e-9);
    }

    // ==================== Reloading Tests ====================

    @Test
    @DisplayName("Reloading matrix with different dimensions")
    void testReloadDifferentDimensions() {
        double[][] data1 = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] data2 = {{5.0, 6.0, 7.0}, {8.0, 9.0, 10.0}, {11.0, 12.0, 13.0}};
        SharedMatrix matrix = new SharedMatrix(data1);
        
        assertEquals(2, matrix.length());
        
        matrix.loadRowMajor(data2);
        
        assertEquals(3, matrix.length());
        assertEquals(3, matrix.get(0).length());
    }

    @Test
    @DisplayName("Switch between row-major and column-major")
    void testSwitchOrientation() {
        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        SharedMatrix matrix = new SharedMatrix();
        
        matrix.loadRowMajor(data);
        assertEquals(VectorOrientation.ROW_MAJOR, matrix.getOrientation());
        
        matrix.loadColumnMajor(data);
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix.getOrientation());
    }
}