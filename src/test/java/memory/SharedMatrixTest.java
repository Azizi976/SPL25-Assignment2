package memory;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SharedMatrixTest {

    @Test
    public void testLoadAndReadRowMajor() {
        double[][] data = { { 1, 2 }, { 3, 4 } };
        SharedMatrix matrix = new SharedMatrix();
        matrix.loadRowMajor(data);

        double[][] result = matrix.readRowMajor();

        assertArrayEquals(data[0], result[0], 0.0001);
        assertArrayEquals(data[1], result[1], 0.0001);
    }

    @Test
    public void testLoadColumnMajorAndReadBack() {
        // Matrix:
        // 1 2
        // 3 4
        //
        // In Column Major Memory:
        // Col 0: [1, 3]
        // Col 1: [2, 4]

        double[][] data = { { 1, 2 }, { 3, 4 } };
        SharedMatrix matrix = new SharedMatrix();
        matrix.loadColumnMajor(data);

        // Verify internal orientation is Column Major
        assertEquals(VectorOrientation.COLUMN_MAJOR, matrix.getOrientation());

        // readRowMajor should transpose it back to: [[1, 2], [3, 4]]
        double[][] result = matrix.readRowMajor();

        assertEquals(1.0, result[0][0], 0.0001);
        assertEquals(2.0, result[0][1], 0.0001);
        assertEquals(3.0, result[1][0], 0.0001);
        assertEquals(4.0, result[1][1], 0.0001);
    }
}