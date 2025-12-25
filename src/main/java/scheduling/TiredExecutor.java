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
                    if (inFlight.decrementAndGet() == 0) {
                        // Notify the main thread waiting in submitAll
                        synchronized (this) {
                            this.notifyAll();
                        }
                    }
                }
            };

            // Give worker theh wrapped task
            worker.newTask(wrappedTask);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {

        // Submit all the tasks in the Iterable tasks
        for (Runnable task : tasks) {
            submit(task);
        }

        // Now we'll create the barrier that controls the main thread (executer)
        synchronized (this) {
            // While tasks are working the main thread is waiting
            while (inFlight.get() > 0) {
                try {
                    this.wait();
                } catch (InterruptedException e) { // If interupted throw an exception
                    Thread.currentThread().interrupt();
                }
            }
        }

    }ww

    public void shutdown() throws InterruptedException {

        // Shutting down every worker in workers
        for (TiredThread workThread : workers) {
            workThread.shutdown();
        }

        // Waiting until all workers finish their work
        for (TiredThread workThread : workers) {
            workThread.join();
        }
    }

    public synchronized String getWorkerReport() {
        // Using string builder 
        StringBuilder report = new StringBuilder();
        report.append("Worker Report:\n");
        report.append("--------------\n");

        for (TiredThread worker : workers) {
            // Format the values to the report string builder
            report.append(String.format(
                    "Worker #%d: Fatigue=%.2f, TimeUsed=%d ms, TimeIdle=%d ms\n",
                    worker.getWorkerId(),
                    worker.getFatigue(),
                    worker.getTimeUsed(),
                    worker.getTimeIdle()));
        }

        // Return and make it a string
        return report.toString();
    }
}