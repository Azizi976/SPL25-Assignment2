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

            leftMatrix = new SharedMatrix(children.get(0).getMatrix());
            rightMatrix = new SharedMatrix(children.get(1).getMatrix());

            tasks = this.createAddTasks();

            // In this case we have to save the result in antoher matrix cause we have to
            // keep both matrices alive to compute

        } else if (node.getNodeType() == ComputationNodeType.MULTIPLY) {

            leftMatrix = new SharedMatrix(children.get(0).getMatrix());
            rightMatrix = new SharedMatrix(children.get(1).getMatrix());

            tasks = this.createMultiplyTasks();

        } else if (node.getNodeType() == ComputationNodeType.NEGATE) {

            leftMatrix = new SharedMatrix(children.get(0).getMatrix());

            tasks = this.createNegateTasks();

        } else if (node.getNodeType() == ComputationNodeType.TRANSPOSE) {

            leftMatrix = new SharedMatrix(children.get(0).getMatrix());

            tasks = this.createTransposeTasks();
        }

        // Submitting all tasks
        executor.submitAll(tasks);

        // Updating the operator node to be the result
        node.resolve(leftMatrix.readRowMajor());

    }

    public List<Runnable> createAddTasks() {

        if (!(leftMatrix.length() == rightMatrix.length())) {
            throw new IllegalArgumentException("Matrices should be in the same length in order to add one to another/");
        }

        List<Runnable> tasks = new ArrayList<>();
        int rows = leftMatrix.length();

        for (int i = 0; i < rows; i++) {
            int currentRow = i;
            tasks.add(() -> {
                leftMatrix.get(currentRow).add(rightMatrix.get(currentRow));
            });
        }
        return tasks;
    }

    public List<Runnable> createMultiplyTasks() {

        List<Runnable> tasks = new ArrayList<>();
        int rows = leftMatrix.length();

        for (int i = 0; i < rows; i++) {
            int currentRow = i;
            tasks.add(() -> {
                leftMatrix.get(currentRow).vecMatMul(rightMatrix);
            });
        }
        return tasks;
    }

    public List<Runnable> createNegateTasks() {

        List<Runnable> tasks = new ArrayList<>();
        int rows = leftMatrix.length();

        for (int i = 0; i < rows; i++) {
            int currentRow = i;
            tasks.add(() -> {
                leftMatrix.get(currentRow).negate();
                ;
            });
        }
        return tasks;
    }

    public List<Runnable> createTransposeTasks() {

        List<Runnable> tasks = new ArrayList<>();
        int rows = leftMatrix.length();

        for (int i = 0; i < rows; i++) {
            int currentRow = i;
            tasks.add(() -> {
                leftMatrix.get(currentRow).transpose();
                ;
            });
        }
        return tasks;
    }

    public String getWorkerReport() {
        return executor.getWorkerReport();
    }
}