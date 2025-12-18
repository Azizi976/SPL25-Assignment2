package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        // Initiallizing workers array
        this.workers = new TiredThread[numThreads];

        // Creating numThreads number of threads
        for (int i = 0; i < numThreads; i++) {
            // Random fatigue factor for each one
            double fatigueFactor = 0.5 + Math.random();
            TiredThread thread = new TiredThread(i, fatigueFactor);
            // Inserting the thread to workers
            this.workers[i] = thread;
            // Starting the thread
            thread.start();
            // Add thread into the queue
            idleMinHeap.add(thread);
        }
    }

    public void submit(Runnable task) {
        try {
            // Take a thread from priority queue
            TiredThread worker = idleMinHeap.take();
            // Update inflight
            inFlight.incrementAndGet();

            // Wrap the task to return worker to queue when done
            Runnable wrappedTask = () -> {
                try {
                    task.run();
                } finally {
                    // Return worker to idle queue
                    idleMinHeap.add(worker);
                    inFlight.decrementAndGet();
                }
            };

            // Give worker theh wrapped task
            worker.newTask(wrappedTask);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {
        // TODO: submit tasks one by one and wait until all finish
    }

    public void shutdown() throws InterruptedException {
        // TODO
    }

    public synchronized String getWorkerReport() {
        // TODO: return readable statistics for each worker
        return null;
    }
}
