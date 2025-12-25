package memory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SharedVector class.
 * Tests cover: addition, multiplication, transpose, negation,
 * dimension checks, error-handling logic, and edge cases.
 */
public class SharedVectorTest {

    // ==================== Constructor and Basic Operations Tests ====================

    @Test
    @DisplayName("Constructor creates vector with correct values and orientation")
    void testConstructor() {
        double[] data = {1.0, 2.0, 3.0};
        SharedVector vec = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        
        assertEquals(3, vec.length());
        assertEquals(VectorOrientation.ROW_MAJOR, vec.getOrientation());
        assertEquals(1.0, vec.get(0), 1e-9);
        assertEquals(2.0, vec.get(1), 1e-9);
        assertEquals(3.0, vec.get(2), 1e-9);
    }

    @Test
    @DisplayName("Constructor with column major orientation")
    void testConstructorColumnMajor() {
        double[] data = {4.0, 5.0, 6.0, 7.0};
        SharedVector vec = new SharedVector(data, VectorOrientation.COLUMN_MAJOR);
        
        assertEquals(4, vec.length());
        assertEquals(VectorOrientation.COLUMN_MAJOR, vec.getOrientation());
    }

    @Test
    @DisplayName("Get method returns correct values at each index")
    void testGet() {
        double[] data = {10.0, 20.0, 30.0, 40.0, 50.0};
        SharedVector vec = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], vec.get(i), 1e-9);
        }
    }

    @Test
    @DisplayName("Length method returns correct vector size")
    void testLength() {
        double[] data1 = {1.0};
        double[] data5 = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] data10 = new double[10];
        
        assertEquals(1, new SharedVector(data1, VectorOrientation.ROW_MAJOR).length());
        assertEquals(5, new SharedVector(data5, VectorOrientation.ROW_MAJOR).length());
        assertEquals(10, new SharedVector(data10, VectorOrientation.ROW_MAJOR).length());
    }

    // ==================== Addition Tests ====================

    @Test
    @DisplayName("Add two vectors of same length and orientation")
    void testAddBasic() {
        double[] data1 = {1.0, 2.0, 3.0};
        double[] data2 = {4.0, 5.0, 6.0};
        SharedVector vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector vec2 = new SharedVector(data2, VectorOrientation.ROW_MAJOR);
        
        vec1.add(vec2);
        
        assertEquals(5.0, vec1.get(0), 1e-9);
        assertEquals(7.0, vec1.get(1), 1e-9);
        assertEquals(9.0, vec1.get(2), 1e-9);
    }

    @Test
    @DisplayName("Add vectors with negative values")
    void testAddNegativeValues() {
        double[] data1 = {-1.0, -2.0, -3.0};
        double[] data2 = {1.0, 2.0, 3.0};
        SharedVector vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector vec2 = new SharedVector(data2, VectorOrientation.ROW_MAJOR);
        
        vec1.add(vec2);
        
        assertEquals(0.0, vec1.get(0), 1e-9);
        assertEquals(0.0, vec1.get(1), 1e-9);
        assertEquals(0.0, vec1.get(2), 1e-9);
    }

    @Test
    @DisplayName("Add vectors with zero values")
    void testAddZeroVector() {
        double[] data1 = {5.0, 10.0, 15.0};
        double[] data2 = {0.0, 0.0, 0.0};
        SharedVector vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector vec2 = new SharedVector(data2, VectorOrientation.ROW_MAJOR);
        
        vec1.add(vec2);
        
        assertEquals(5.0, vec1.get(0), 1e-9);
        assertEquals(10.0, vec1.get(1), 1e-9);
        assertEquals(15.0, vec1.get(2), 1e-9);
    }

    @Test
    @DisplayName("Add vectors with floating point values")
    void testAddFloatingPoint() {
        double[] data1 = {1.5, 2.7, 3.3};
        double[] data2 = {0.5, 0.3, 0.7};
        SharedVector vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector vec2 = new SharedVector(data2, VectorOrientation.ROW_MAJOR);
        
        vec1.add(vec2);
        
        assertEquals(2.0, vec1.get(0), 1e-9);
        assertEquals(3.0, vec1.get(1), 1e-9);
        assertEquals(4.0, vec1.get(2), 1e-9);
    }

    @Test
    @DisplayName("Add throws exception for different lengths")
    void testAddDifferentLengths() {
        double[] data1 = {1.0, 2.0, 3.0};
        double[] data2 = {4.0, 5.0};
        SharedVector vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector vec2 = new SharedVector(data2, VectorOrientation.ROW_MAJOR);
        
        assertThrows(IllegalArgumentException.class, () -> vec1.add(vec2));
    }

    @Test
    @DisplayName("Add throws exception for different orientations")
    void testAddDifferentOrientations() {
        double[] data1 = {1.0, 2.0, 3.0};
        double[] data2 = {4.0, 5.0, 6.0};
        SharedVector vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector vec2 = new SharedVector(data2, VectorOrientation.COLUMN_MAJOR);
        
        assertThrows(IllegalArgumentException.class, () -> vec1.add(vec2));
    }

    @Test
    @DisplayName("Add single element vectors")
    void testAddSingleElement() {
        double[] data1 = {5.0};
        double[] data2 = {3.0};
        SharedVector vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector vec2 = new SharedVector(data2, VectorOrientation.ROW_MAJOR);
        
        vec1.add(vec2);
        
        assertEquals(8.0, vec1.get(0), 1e-9);
    }

    // ==================== Negation Tests ====================

    @Test
    @DisplayName("Negate positive values")
    void testNegatePositive() {
        double[] data = {1.0, 2.0, 3.0};
        SharedVector vec = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        
        vec.negate();
        
        assertEquals(-1.0, vec.get(0), 1e-9);
        assertEquals(-2.0, vec.get(1), 1e-9);
        assertEquals(-3.0, vec.get(2), 1e-9);
    }

    @Test
    @DisplayName("Negate negative values")
    void testNegateNegative() {
        double[] data = {-1.0, -2.0, -3.0};
        SharedVector vec = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        
        vec.negate();
        
        assertEquals(1.0, vec.get(0), 1e-9);
        assertEquals(2.0, vec.get(1), 1e-9);
        assertEquals(3.0, vec.get(2), 1e-9);
    }

    @Test
    @DisplayName("Negate zero values")
    void testNegateZero() {
        double[] data = {0.0, 0.0, 0.0};
        SharedVector vec = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        
        vec.negate();
        
        assertEquals(0.0, vec.get(0), 1e-9);
        assertEquals(0.0, vec.get(1), 1e-9);
        assertEquals(0.0, vec.get(2), 1e-9);
    }

    @Test
    @DisplayName("Negate mixed values")
    void testNegateMixed() {
        double[] data = {-5.0, 0.0, 5.0};
        SharedVector vec = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        
        vec.negate();
        
        assertEquals(5.0, vec.get(0), 1e-9);
        assertEquals(0.0, vec.get(1), 1e-9);
        assertEquals(-5.0, vec.get(2), 1e-9);
    }

    @Test
    @DisplayName("Double negation returns original values")
    void testDoubleNegate() {
        double[] data = {1.0, 2.0, 3.0};
        SharedVector vec = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        
        vec.negate();
        vec.negate();
        
        assertEquals(1.0, vec.get(0), 1e-9);
        assertEquals(2.0, vec.get(1), 1e-9);
        assertEquals(3.0, vec.get(2), 1e-9);
    }

    @Test
    @DisplayName("Negate single element vector")
    void testNegateSingleElement() {
        double[] data = {42.0};
        SharedVector vec = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        
        vec.negate();
        
        assertEquals(-42.0, vec.get(0), 1e-9);
    }

    // ==================== Transpose Tests ====================

    @Test
    @DisplayName("Transpose row major to column major")
    void testTransposeRowToColumn() {
        double[] data = {1.0, 2.0, 3.0};
        SharedVector vec = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        
        vec.transpose();
        
        assertEquals(VectorOrientation.COLUMN_MAJOR, vec.getOrientation());
        assertEquals(1.0, vec.get(0), 1e-9);
        assertEquals(2.0, vec.get(1), 1e-9);
        assertEquals(3.0, vec.get(2), 1e-9);
    }

    @Test
    @DisplayName("Transpose column major to row major")
    void testTransposeColumnToRow() {
        double[] data = {1.0, 2.0, 3.0};
        SharedVector vec = new SharedVector(data, VectorOrientation.COLUMN_MAJOR);
        
        vec.transpose();
        
        assertEquals(VectorOrientation.ROW_MAJOR, vec.getOrientation());
    }

    @Test
    @DisplayName("Double transpose returns to original orientation")
    void testDoubleTranspose() {
        double[] data = {1.0, 2.0, 3.0};
        SharedVector vec = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        
        vec.transpose();
        vec.transpose();
        
        assertEquals(VectorOrientation.ROW_MAJOR, vec.getOrientation());
    }

    // ==================== Dot Product Tests ====================

    @Test
    @DisplayName("Dot product of orthogonal vectors is zero")
    void testDotProductOrthogonal() {
        double[] data1 = {1.0, 0.0};
        double[] data2 = {0.0, 1.0};
        SharedVector vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector vec2 = new SharedVector(data2, VectorOrientation.ROW_MAJOR);
        
        double result = vec1.dot(vec2);
        
        assertEquals(0.0, result, 1e-9);
    }

    @Test
    @DisplayName("Dot product basic calculation")
    void testDotProductBasic() {
        double[] data1 = {1.0, 2.0, 3.0};
        double[] data2 = {4.0, 5.0, 6.0};
        SharedVector vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector vec2 = new SharedVector(data2, VectorOrientation.ROW_MAJOR);
        
        // 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
        double result = vec1.dot(vec2);
        
        assertEquals(32.0, result, 1e-9);
    }

    @Test
    @DisplayName("Dot product with negative values")
    void testDotProductNegative() {
        double[] data1 = {1.0, -2.0, 3.0};
        double[] data2 = {-1.0, 2.0, -3.0};
        SharedVector vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector vec2 = new SharedVector(data2, VectorOrientation.ROW_MAJOR);
        
        // 1*(-1) + (-2)*2 + 3*(-3) = -1 - 4 - 9 = -14
        double result = vec1.dot(vec2);
        
        assertEquals(-14.0, result, 1e-9);
    }

    @Test
    @DisplayName("Dot product with zero vector")
    void testDotProductZero() {
        double[] data1 = {1.0, 2.0, 3.0};
        double[] data2 = {0.0, 0.0, 0.0};
        SharedVector vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector vec2 = new SharedVector(data2, VectorOrientation.ROW_MAJOR);
        
        double result = vec1.dot(vec2);
        
        assertEquals(0.0, result, 1e-9);
    }

    @Test
    @DisplayName("Dot product throws exception for different lengths")
    void testDotProductDifferentLengths() {
        double[] data1 = {1.0, 2.0, 3.0};
        double[] data2 = {4.0, 5.0};
        SharedVector vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector vec2 = new SharedVector(data2, VectorOrientation.ROW_MAJOR);
        
        assertThrows(IllegalArgumentException.class, () -> vec1.dot(vec2));
    }

    @Test
    @DisplayName("Dot product throws exception for different orientations")
    void testDotProductDifferentOrientations() {
        double[] data1 = {1.0, 2.0, 3.0};
        double[] data2 = {4.0, 5.0, 6.0};
        SharedVector vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector vec2 = new SharedVector(data2, VectorOrientation.COLUMN_MAJOR);
        
        assertThrows(IllegalArgumentException.class, () -> vec1.dot(vec2));
    }

    @Test
    @DisplayName("Dot product of single element vectors")
    void testDotProductSingleElement() {
        double[] data1 = {3.0};
        double[] data2 = {4.0};
        SharedVector vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector vec2 = new SharedVector(data2, VectorOrientation.ROW_MAJOR);
        
        double result = vec1.dot(vec2);
        
        assertEquals(12.0, result, 1e-9);
    }

    // ==================== Vector-Matrix Multiplication Tests ====================

    @Test
    @DisplayName("Vector-matrix multiplication with row major matrix - 2x2")
    void testVecMatMulRowMajor2x2() {
        // Vector [1, 2] * Matrix [[1, 2], [3, 4]]
        // Result: [1*1 + 2*3, 1*2 + 2*4] = [7, 10]
        double[] vecData = {1.0, 2.0};
        SharedVector vec = new SharedVector(vecData, VectorOrientation.ROW_MAJOR);
        
        double[][] matrixData = {{1.0, 2.0}, {3.0, 4.0}};
        SharedMatrix matrix = new SharedMatrix(matrixData);
        
        vec.vecMatMul(matrix);
        
        assertEquals(2, vec.length());
        assertEquals(7.0, vec.get(0), 1e-9);
        assertEquals(10.0, vec.get(1), 1e-9);
    }

    @Test
    @DisplayName("Vector-matrix multiplication with row major matrix - 3x3")
    void testVecMatMulRowMajor3x3() {
        // Vector [1, 0, 2] * Matrix [[1, 2, 3], [0, 1, 0], [4, 0, 1]]
        // Result: [1*1 + 0*0 + 2*4, 1*2 + 0*1 + 2*0, 1*3 + 0*0 + 2*1] = [9, 2, 5]
        double[] vecData = {1.0, 0.0, 2.0};
        SharedVector vec = new SharedVector(vecData, VectorOrientation.ROW_MAJOR);
        
        double[][] matrixData = {{1.0, 2.0, 3.0}, {0.0, 1.0, 0.0}, {4.0, 0.0, 1.0}};
        SharedMatrix matrix = new SharedMatrix(matrixData);
        
        vec.vecMatMul(matrix);
        
        assertEquals(3, vec.length());
        assertEquals(9.0, vec.get(0), 1e-9);
        assertEquals(2.0, vec.get(1), 1e-9);
        assertEquals(5.0, vec.get(2), 1e-9);
    }

    @Test
    @DisplayName("Vector-matrix multiplication changes vector size when matrix is non-square")
    void testVecMatMulChangesSize() {
        // Vector [1, 2, 3] * Matrix [[1, 2], [3, 4], [5, 6]]
        // Result: [1*1 + 2*3 + 3*5, 1*2 + 2*4 + 3*6] = [22, 28]
        double[] vecData = {1.0, 2.0, 3.0};
        SharedVector vec = new SharedVector(vecData, VectorOrientation.ROW_MAJOR);
        
        double[][] matrixData = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};
        SharedMatrix matrix = new SharedMatrix(matrixData);
        
        vec.vecMatMul(matrix);
        
        assertEquals(2, vec.length());
        assertEquals(22.0, vec.get(0), 1e-9);
        assertEquals(28.0, vec.get(1), 1e-9);
    }

    @Test
    @DisplayName("Vector-matrix multiplication with identity matrix")
    void testVecMatMulIdentity() {
        double[] vecData = {1.0, 2.0, 3.0};
        SharedVector vec = new SharedVector(vecData, VectorOrientation.ROW_MAJOR);
        
        double[][] identityData = {{1.0, 0.0, 0.0}, {0.0, 1.0, 0.0}, {0.0, 0.0, 1.0}};
        SharedMatrix identity = new SharedMatrix(identityData);
        
        vec.vecMatMul(identity);
        
        assertEquals(1.0, vec.get(0), 1e-9);
        assertEquals(2.0, vec.get(1), 1e-9);
        assertEquals(3.0, vec.get(2), 1e-9);
    }

    @Test
    @DisplayName("Vector-matrix multiplication with zero matrix")
    void testVecMatMulZeroMatrix() {
        double[] vecData = {1.0, 2.0, 3.0};
        SharedVector vec = new SharedVector(vecData, VectorOrientation.ROW_MAJOR);
        
        double[][] zeroData = {{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}};
        SharedMatrix zero = new SharedMatrix(zeroData);
        
        vec.vecMatMul(zero);
        
        assertEquals(0.0, vec.get(0), 1e-9);
        assertEquals(0.0, vec.get(1), 1e-9);
        assertEquals(0.0, vec.get(2), 1e-9);
    }

    @Test
    @DisplayName("Vector-matrix multiplication throws exception for dimension mismatch")
    void testVecMatMulDimensionMismatch() {
        double[] vecData = {1.0, 2.0};  // Length 2
        SharedVector vec = new SharedVector(vecData, VectorOrientation.ROW_MAJOR);
        
        double[][] matrixData = {{1.0, 2.0}, {3.0, 4.0}, {5.0, 6.0}};  // 3x2 matrix
        SharedMatrix matrix = new SharedMatrix(matrixData);
        
        assertThrows(IllegalArgumentException.class, () -> vec.vecMatMul(matrix));
    }

    // ==================== Locking Tests ====================

    @Test
    @DisplayName("Read lock and unlock work without exception")
    void testReadLocking() {
        double[] data = {1.0, 2.0, 3.0};
        SharedVector vec = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        
        assertDoesNotThrow(() -> {
            vec.readLock();
            double val = vec.get(0);
            vec.readUnlock();
        });
    }

    @Test
    @DisplayName("Write lock and unlock work without exception")
    void testWriteLocking() {
        double[] data = {1.0, 2.0, 3.0};
        SharedVector vec = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        
        assertDoesNotThrow(() -> {
            vec.writeLock();
            vec.negate();
            vec.writeUnlock();
        });
    }

    @Test
    @DisplayName("Multiple read locks can be acquired simultaneously")
    void testMultipleReadLocks() throws InterruptedException {
        double[] data = {1.0, 2.0, 3.0};
        SharedVector vec = new SharedVector(data, VectorOrientation.ROW_MAJOR);
        
        Thread t1 = new Thread(() -> {
            vec.readLock();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            vec.readUnlock();
        });
        
        Thread t2 = new Thread(() -> {
            vec.readLock();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            vec.readUnlock();
        });
        
        t1.start();
        t2.start();
        t1.join(200);
        t2.join(200);
        
        assertFalse(t1.isAlive());
        assertFalse(t2.isAlive());
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Operations with very large values")
    void testLargeValues() {
        double[] data1 = {1e15, 2e15, 3e15};
        double[] data2 = {1e15, 1e15, 1e15};
        SharedVector vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector vec2 = new SharedVector(data2, VectorOrientation.ROW_MAJOR);
        
        vec1.add(vec2);
        
        assertEquals(2e15, vec1.get(0), 1e6);
        assertEquals(3e15, vec1.get(1), 1e6);
        assertEquals(4e15, vec1.get(2), 1e6);
    }

    @Test
    @DisplayName("Operations with very small values")
    void testSmallValues() {
        double[] data1 = {1e-15, 2e-15, 3e-15};
        double[] data2 = {1e-15, 1e-15, 1e-15};
        SharedVector vec1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector vec2 = new SharedVector(data2, VectorOrientation.ROW_MAJOR);
        
        vec1.add(vec2);
        
        assertEquals(2e-15, vec1.get(0), 1e-24);
        assertEquals(3e-15, vec1.get(1), 1e-24);
        assertEquals(4e-15, vec1.get(2), 1e-24);
    }
}