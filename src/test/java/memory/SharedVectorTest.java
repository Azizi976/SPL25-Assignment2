package memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SharedVectorTest {

    private final double delta = 0.000001;

    @Test
    public void testVectorAddition() {
        double[] data1 = { 1.0, 2.0, 3.0 };
        double[] data2 = { 4.0, 5.0, 6.0 };
        SharedVector v1 = new SharedVector(data1, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(data2, VectorOrientation.ROW_MAJOR);

        v1.add(v2);

        assertEquals(5.0, v1.get(0), delta);
        assertEquals(7.0, v1.get(1), delta);
        assertEquals(9.0, v1.get(2), delta);
    }

    @Test
    public void testVectorAdditionDimensionMismatch() {
        SharedVector v1 = new SharedVector(new double[] { 1, 2 }, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[] { 1, 2, 3 }, VectorOrientation.ROW_MAJOR);

        assertThrows(IllegalArgumentException.class, () -> v1.add(v2));
    }

    @Test
    public void testVectorAdditionOrientationMismatch() {
        SharedVector v1 = new SharedVector(new double[] { 1, 2 }, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[] { 1, 2 }, VectorOrientation.COLUMN_MAJOR);

        assertThrows(IllegalArgumentException.class, () -> v1.add(v2));
    }

    @Test
    public void testNegate() {
        SharedVector v = new SharedVector(new double[] { 1.0, -2.0, 0.0 }, VectorOrientation.ROW_MAJOR);
        v.negate();

        assertEquals(-1.0, v.get(0), delta);
        assertEquals(2.0, v.get(1), delta);
        assertEquals(-0.0, v.get(2), delta);
    }

    @Test
    public void testTranspose() {
        SharedVector v = new SharedVector(new double[] { 1, 2 }, VectorOrientation.ROW_MAJOR);
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());

        v.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, v.getOrientation());

        v.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
    }

    @Test
    public void testVecMatMulRowMajor() {
        // v = [1, 2]
        // M = [[1, 2], [3, 4]]
        // Result = [1*1 + 2*3, 1*2 + 2*4] = [7, 10]
        SharedVector v = new SharedVector(new double[] { 1.0, 2.0 }, VectorOrientation.ROW_MAJOR);

        double[][] matData = {
                { 1.0, 2.0 },
                { 3.0, 4.0 }
        };
        SharedMatrix m = new SharedMatrix(matData); // Constructor usually sets ROW_MAJOR

        v.vecMatMul(m);

        assertEquals(7.0, v.get(0), delta);
        assertEquals(10.0, v.get(1), delta);
    }
}
