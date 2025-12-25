package spl.lae;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import parser.*;
import java.util.ArrayList;
import java.util.List;

public class LinearAlgebraEngineTest {

    private LinearAlgebraEngine lae;

    @BeforeEach
    void init() {
        lae = new LinearAlgebraEngine(4);
    }

    // helper funcs
    private ComputationNode addN(double[][] l, double[][] r) {
        List<ComputationNode> list = new ArrayList<>();
        list.add(new ComputationNode(l));
        list.add(new ComputationNode(r));
        return new ComputationNode(ComputationNodeType.ADD, list);
    }

    private ComputationNode mulN(double[][] l, double[][] r) {
        List<ComputationNode> list = new ArrayList<>();
        list.add(new ComputationNode(l));
        list.add(new ComputationNode(r));
        return new ComputationNode(ComputationNodeType.MULTIPLY, list);
    }

    private ComputationNode negN(double[][] m) {
        List<ComputationNode> list = new ArrayList<>();
        list.add(new ComputationNode(m));
        return new ComputationNode(ComputationNodeType.NEGATE, list);
    }

    private ComputationNode trN(double[][] m) {
        List<ComputationNode> list = new ArrayList<>();
        list.add(new ComputationNode(m));
        return new ComputationNode(ComputationNodeType.TRANSPOSE, list);
    }

    @Test
    void testAdd() {
        double[][] m1 = { { 1, 2 }, { 3, 4 } };
        double[][] m2 = { { 5, 6 }, { 7, 8 } };

        ComputationNode res = lae.run(addN(m1, m2));
        double[][] mat = res.getMatrix();

        assertEquals(6.0, mat[0][0]);
        assertEquals(12.0, mat[1][1]);
    }

    @Test
    void testAddZero() {
        double[][] m = { { 1, 2 }, { 3, 4 } };
        double[][] z = { { 0, 0 }, { 0, 0 } };
        double[][] res = lae.run(addN(m, z)).getMatrix();
        assertEquals(1.0, res[0][0]);
    }

    @Test
    void testAddNeg() {
        double[][] m1 = { { 1 } };
        double[][] m2 = { { -1 } };
        double[][] res = lae.run(addN(m1, m2)).getMatrix();
        assertEquals(0.0, res[0][0]);
    }

    @Test
    void testAddNonSquare() {
        double[][] m1 = { { 1, 2, 3 } };
        double[][] m2 = { { 4, 5, 6 } };
        double[][] res = lae.run(addN(m1, m2)).getMatrix();
        assertEquals(5.0, res[0][0]);
        assertEquals(9.0, res[0][2]);
    }

    @Test
    void testMul() {
        double[][] m1 = { { 1, 2 }, { 3, 4 } };
        double[][] m2 = { { 5, 6 }, { 7, 8 } };
        // 19, 22, 43, 50
        double[][] res = lae.run(mulN(m1, m2)).getMatrix();
        assertEquals(19.0, res[0][0]);
        assertEquals(50.0, res[1][1]);
    }

    @Test
    void testMulIdentity() {
        double[][] m = { { 1, 2 }, { 3, 4 } };
        double[][] i = { { 1, 0 }, { 0, 1 } };
        double[][] res = lae.run(mulN(m, i)).getMatrix();
        assertEquals(1.0, res[0][0]);
        assertEquals(4.0, res[1][1]);
    }

    @Test
    void testMulRect() {
        // 2x3 * 3x2
        double[][] m1 = { { 1, 2, 3 }, { 4, 5, 6 } };
        double[][] m2 = { { 7, 8 }, { 9, 10 }, { 11, 12 } };
        double[][] res = lae.run(mulN(m1, m2)).getMatrix();

        assertEquals(2, res.length);
        assertEquals(2, res[0].length);
        assertEquals(58.0, res[0][0]);
    }

    @Test
    void testNegate() {
        double[][] m = { { 1, 2 } };
        double[][] res = lae.run(negN(m)).getMatrix();
        assertEquals(-1.0, res[0][0]);
        assertEquals(-2.0, res[0][1]);
    }

    @Test
    void testTranspose() {
        double[][] m = { { 1, 2 } };
        double[][] res = lae.run(trN(m)).getMatrix();
        assertEquals(2, res.length);
    }

    @Test
    void testNested() {
        // -(A + B)
        double[][] a = { { 1 } };
        double[][] b = { { 2 } };

        List<ComputationNode> l = new ArrayList<>();
        l.add(addN(a, b));
        ComputationNode neg = new ComputationNode(ComputationNodeType.NEGATE, l);

        double[][] res = lae.run(neg).getMatrix();
        assertEquals(-3.0, res[0][0]);
    }

    @Test
    void testErr() {
        double[][] m1 = { { 1 } };
        double[][] m2 = { { 1, 2 } };
        assertThrows(Exception.class, () -> lae.run(addN(m1, m2)));
    }

    @Test
    void testReport() {
        String s = lae.getWorkerReport();
        assertNotNull(s);
        assertTrue(s.contains("Report"));
    }
}