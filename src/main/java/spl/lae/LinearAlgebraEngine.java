package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.ArrayList;
import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    // Constructor
    public LinearAlgebraEngine(int numThreads) {
        this.executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        // While the root is not the resolved matrix, continue to compute
        while (computationRoot.getChildren() != null) {
            // Find the first node to resolve
            ComputationNode operator = computationRoot.findResolvable();

            // Load and compute the opertator
            loadAndCompute(operator);
        }
        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {

        // Get the children list from node
        List<ComputationNode> children = node.getChildren();

        // Create a new task list that will store the list of runnables
        List<Runnable> tasks = null;

        // We'll devide to 4 cases for each oprerator and
        // Make the corresponding task list to each operator and load children
        if (node.getNodeType() == ComputationNodeType.ADD) {

            // Load children to corresponding matrix
            leftMatrix.loadRowMajor(children.get(0).getMatrix());
            rightMatrix.loadRowMajor(children.get(1).getMatrix());

            // Creating tasks for addition operation
            tasks = this.createAddTasks();

        } else if (node.getNodeType() == ComputationNodeType.MULTIPLY) {

            // Load children to corresponding matrix
            leftMatrix.loadRowMajor(children.get(0).getMatrix());
            rightMatrix.loadRowMajor(children.get(1).getMatrix());

            // Creating tasks for multiplication operation
            tasks = this.createMultiplyTasks();

        } else if (node.getNodeType() == ComputationNodeType.NEGATE) {

            // Load child to corresponding matrix
            leftMatrix.loadRowMajor(children.get(0).getMatrix());

            // Creating tasks for negation operation
            tasks = this.createNegateTasks();

        } else if (node.getNodeType() == ComputationNodeType.TRANSPOSE) {

            // Load child to corresponding matrix
            leftMatrix.loadRowMajor(children.get(0).getMatrix());

            // Creating tasks for transpose operation
            tasks = this.createTransposeTasks();
        }

        // Submitting all tasks to the executor
        executor.submitAll(tasks);

        // Updating the operator node to be the result from the left matrix
        node.resolve(leftMatrix.readRowMajor());

    }

    public List<Runnable> createAddTasks() {

        // Validating that both matrices have the same dimensions
        if (!(leftMatrix.length() == rightMatrix.length())) {
            throw new IllegalArgumentException("Matrices should be in the same length in order to add one to another/");
        }

        List<Runnable> tasks = new ArrayList<>();
        int rows = leftMatrix.length();

        // Iterating over each row to create a dedicated task
        for (int i = 0; i < rows; i++) {
            int currentRow = i;
            tasks.add(() -> {

                SharedVector target = leftMatrix.get(currentRow);
                SharedVector source = rightMatrix.get(currentRow);

                // Acquiring locks to ensure thread safety
                target.writeLock();
                source.readLock();
                try {
                    // Performing vector addition
                    target.add(source);
                } finally {
                    // Releasing locks in finally block to ensure they are always freed
                    target.writeUnlock();
                    source.readUnlock();
                }
            });
        }
        return tasks;
    }

    public List<Runnable> createMultiplyTasks() {

        List<Runnable> tasks = new ArrayList<>();
        int rows = leftMatrix.length();
        int rightLen = rightMatrix.length();

        // Iterating over each row to create a dedicated task
        for (int i = 0; i < rows; i++) {
            int currentRow = i;
            tasks.add(() -> {

                SharedVector target = leftMatrix.get(currentRow);

                // Acquiring write lock for the target row
                target.writeLock();

                // Acquiring read locks for the entire right matrix since multiplication
                // requires all of it
                for (int k = 0; k < rightLen; k++) {
                    rightMatrix.get(k).readLock();
                }

                try {
                    // Performing vector-matrix multiplication
                    target.vecMatMul(rightMatrix);
                } finally {
                    // Releasing read locks for the right matrix
                    for (int k = 0; k < rightLen; k++) {
                        rightMatrix.get(k).readUnlock();
                    }
                    // Releasing write lock for the target row
                    target.writeUnlock();
                }
            });
        }
        return tasks;
    }

    public List<Runnable> createNegateTasks() {

        List<Runnable> tasks = new ArrayList<>();
        int rows = leftMatrix.length();

        // Iterating over each row to create a dedicated task
        for (int i = 0; i < rows; i++) {
            int currentRow = i;
            tasks.add(() -> {

                SharedVector target = leftMatrix.get(currentRow);

                // Acquiring write lock since we are modifying the vector
                target.writeLock();
                try {
                    // Performing negation
                    target.negate();
                } finally {
                    // Releasing the write lock
                    target.writeUnlock();
                }
            });
        }
        return tasks;
    }

    public List<Runnable> createTransposeTasks() {

        List<Runnable> tasks = new ArrayList<>();
        int rows = leftMatrix.length();

        // Iterating over each row to create a dedicated task
        for (int i = 0; i < rows; i++) {
            int currentRow = i;
            tasks.add(() -> {

                SharedVector target = leftMatrix.get(currentRow);

                // Acquiring write lock since transpose modifies internal state
                target.writeLock();
                try {
                    // Performing transpose
                    target.transpose();
                } finally {
                    // Releasing the write lock
                    target.writeUnlock();
                }
            });
        }
        return tasks;
    }

    public String getWorkerReport() {
        return executor.getWorkerReport();
    }
}