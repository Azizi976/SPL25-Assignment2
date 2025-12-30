package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        // Creating an empty array
        this.vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        // Making vectors the length of the matric we got
        this.vectors = new SharedVector[matrix.length];
        // Creating a sharedvector for each array in matrix and storing it as a vector
        // in vectors
        for (int i = 0; i < matrix.length; i++) {
            SharedVector current_vec = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
            vectors[i] = current_vec;
        }
    }

    // We dont use the functions of acquire and release because in our implemtation it's not nedded  
    public void loadRowMajor(double[][] matrix) {
        // Updating the lentgh of vectors
        this.vectors = new SharedVector[matrix.length];
        // Creating a sharedvector for each array in matrix and storing it as a vector
        // in vectors
        for (int i = 0; i < matrix.length; i++) {
            SharedVector current_vec = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
            vectors[i] = current_vec;
        }
    }

    public void loadColumnMajor(double[][] matrix) {
        // Initialize vectors array with size equal to number of columns in matrix
        this.vectors = new SharedVector[matrix[0].length];

        // Iterate over each column index
        for (int i = 0; i < matrix[0].length; i++) {
            // Create array to hold the column values
            double[] current_vec = new double[matrix.length];

            // Extract column i from the matrix by iterating over each row
            for (int j = 0; j < matrix.length; j++) {
                current_vec[j] = matrix[j][i];
            }

            // Create a new SharedVector with column-major orientation and store it
            SharedVector new_vec = new SharedVector(current_vec, VectorOrientation.COLUMN_MAJOR);
            vectors[i] = new_vec;
        }
    }

    public double[][] readRowMajor() {
        if (vectors.length == 0) {
            return new double[0][0];
        }

        acquireAllVectorReadLocks(this.vectors);
        try {
            // If stored as row-major, return directly
            if (vectors[0].getOrientation() == VectorOrientation.ROW_MAJOR) {
                double[][] result = new double[vectors.length][vectors[0].length()];
                for (int i = 0; i < vectors.length; i++) {
                    for (int j = 0; j < vectors[i].length(); j++) {
                        result[i][j] = vectors[i].get(j);
                    }
                }
                return result;
            }
            // If stored as column-major, transpose back to row-major
            else {
                int numRows = vectors[0].length();
                int numCols = vectors.length;
                double[][] result = new double[numRows][numCols];
                for (int col = 0; col < numCols; col++) {
                    for (int row = 0; row < numRows; row++) {
                        result[row][col] = vectors[col].get(row);
                    }
                }
                return result;
            }
        } finally {
            releaseAllVectorReadLocks(this.vectors);
        }

    }

    public SharedVector get(int index) {
        return vectors[index];
    }

    public int length() {
        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        if (vectors.length == 0) {
            throw new IllegalArgumentException("this is an empty matrix");
        }
        return vectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        for (SharedVector vec : vecs) {
            vec.readLock();
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        for (SharedVector vec : vecs) {
            vec.readUnlock();
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        for (SharedVector vec : vecs) {
            vec.writeLock();
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        for (SharedVector vec : vecs) {
            vec.writeUnlock();
        }
    }
}