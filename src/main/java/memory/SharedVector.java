package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        return vector[index];
    }

    public int length() {
        return vector.length;
    }

    public VectorOrientation getOrientation() {
        return orientation;
    }

    public void writeLock() {
        // TODO: acquire write lock
    }

    public void writeUnlock() {
        // TODO: release write lock
    }

    public void readLock() {
        // TODO: acquire read lock
    }

    public void readUnlock() {
        // TODO: release read lock
    }

    public void transpose() {
        if (this.orientation == VectorOrientation.ROW_MAJOR) {
            this.orientation = VectorOrientation.COLUMN_MAJOR;
        } else {
            this.orientation = VectorOrientation.ROW_MAJOR;
        }
    }

    public void add(SharedVector other) {
        // Checking if the input is valid
        if (this.length() != other.length() || this.orientation != other.getOrientation()) {
            throw new IllegalArgumentException("SharedVectors must have the same length and orientation");
        }

        // Add values of other vector to this vector
        for (int i = 0; i < this.length(); i++) {
            this.vector[i] = this.vector[i] + other.get(i);
        }
    }

    public void negate() {
        // Negate the values of this vector
        for (int i = 0; i < this.length(); i++) {
            this.vector[i] = -1 * this.vector[i];
        }

    }

    public double dot(SharedVector other) {
        // Checking if the input is valid
        if (this.length() != other.length() || this.orientation != other.getOrientation()) {
            throw new IllegalArgumentException("SharedVectors must have the same length and orientation");
        }

        double result = 0;

        // Add values of other vector to this vector
        for (int i = 0; i < this.length(); i++) {
            result += this.vector[i] * other.get(i);
        }
        return result;
    }

    public void vecMatMul(SharedMatrix matrix) {

        // Row major orientation case
        if (matrix.getOrientation() == VectorOrientation.ROW_MAJOR) {
            int len = this.length();
            double[] result = new double[matrix.get(0).length()];

            // Checking if multiplication is valud
            if (matrix.length() != len) {
                throw new IllegalArgumentException("Illegal multiplication");
            }

            // For each column in output
            for (int col = 0; col < matrix.get(0).length(); col++) {
                // Dot product down the column
                for (int row = 0; row < len; row++) {
                    result[col] += this.get(row) * matrix.get(row).get(col);
                }
            }
            this.vector = result;
        }

        // Column major orientation case
        if (matrix.getOrientation() == VectorOrientation.COLUMN_MAJOR) {
            // Checking if multiplication is valud
            if (matrix.get(0).length() != this.length()) {
                throw new IllegalArgumentException("Illegal multiplication");
            }

            // Number of column vectors
            int numCols = matrix.length();
            double[] result = new double[numCols];

            // For each column in output
            for (int col = 0; col < numCols; col++) {
                // Dot product down the column
                for (int row = 0; row < this.length(); row++) {
                    result[col] += this.get(row) * matrix.get(col).get(row);
                }
            }
            this.vector = result;
        }
    }
}