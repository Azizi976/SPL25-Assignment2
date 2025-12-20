package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    // Constructor
    public LinearAlgebraEngine(int numThreads) {
        this.executor = new TiredExecutor(numThreads);
        this.leftMatrix = new SharedMatrix();
        this.rightMatrix = new SharedMatrix();
    }

    public ComputationNode run(ComputationNode computationRoot) {
        while (computationRoot.getChildren() != null){
            // Find the first node to resolve
            ComputationNode operator = computationRoot.findResolvable();

            // Make a list of children
            List<ComputationNode> matrixList = operator.getChildren();

            // Get the matrix filed from each children
            leftMatrix = new SharedMatrix(matrixList.get(0).getMatrix());
            rightMatrix = new SharedMatrix(matrixList.get(1).getMatrix());
        }
        return null;
    }

    public void loadAndCompute(ComputationNode node) {
        // TODO: load operand matrices
        // TODO: create compute tasks & submit tasks to executor
    }

    public List<Runnable> createAddTasks() {
        // TODO: return tasks that perform row-wise addition
        return null;
    }

    public List<Runnable> createMultiplyTasks() {
        // TODO: return tasks that perform row × matrix multiplication
        return null;
    }

    public List<Runnable> createNegateTasks() {
        // TODO: return tasks that negate rows
        return null;
    }

    public List<Runnable> createTransposeTasks() {
        // TODO: return tasks that transpose rows
        return null;
    }

    public String getWorkerReport() {
        // TODO: return summary of worker activity
        return null;
    }
}
