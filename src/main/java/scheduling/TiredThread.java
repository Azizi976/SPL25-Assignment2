package scheduling;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TiredThread extends Thread implements Comparable<TiredThread> {

    private static final Runnable POISON_PILL = () -> {
    }; // Special task to signal shutdown

    private final int id; // Worker index assigned by the executor
    private final double fatigueFactor; // Multiplier for fatigue calculation

    private final AtomicBoolean alive = new AtomicBoolean(true); // Indicates if the worker should keep running

    // Single-slot handoff queue; executor will put tasks here
    private final BlockingQueue<Runnable> handoff = new ArrayBlockingQueue<>(1);

    private final AtomicBoolean busy = new AtomicBoolean(false); // Indicates if the worker is currently executing a
                                                                 // task

    private final AtomicLong timeUsed = new AtomicLong(0); // Total time spent executing tasks
    private final AtomicLong timeIdle = new AtomicLong(0); // Total time spent idle
    private final AtomicLong idleStartTime = new AtomicLong(0); // Timestamp when the worker became idle

    public TiredThread(int id, double fatigueFactor) {
        this.id = id;
        this.fatigueFactor = fatigueFactor;
        this.idleStartTime.set(System.nanoTime());
        setName(String.format("FF=%.2f", fatigueFactor));
    }

    public int getWorkerId() {
        return id;
    }

    public double getFatigue() {
        return fatigueFactor * timeUsed.get();
    }

    public boolean isBusy() {
        return busy.get();
    }

    public long getTimeUsed() {
        return timeUsed.get();
    }

    public long getTimeIdle() {
        return timeIdle.get();
    }

    /**
     * Assign a task to this worker.
     * This method is non-blocking: if the worker is not ready to accept a task,
     * it throws IllegalStateException.
     */
    public void newTask(Runnable task) {
        if (this.busy.get()) {
            throw new IllegalStateException("Worker is busy");
        }
        handoff.add(task);
    }

    /**
     * Request this worker to stop after finishing current task.
     * Inserts a poison pill so the worker wakes up and exits.
     */
    public void shutdown() {
        try {
            handoff.put(POISON_PILL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        // Main worker loop - continues while thread is alive
        while (alive.get()) {
            try {
                // Block and wait for a task from the executor
                Runnable task = handoff.take();

                // Calculate idle time: from when we started waiting until now
                this.timeIdle.addAndGet(System.nanoTime() - this.idleStartTime.get());

                // Check for shutdown signal
                if (task.equals(POISON_PILL)) {
                    return;
                }

                // Mark worker as busy before executing
                busy.set(true);

                // Record task start time for fatigue calculation
                long taskStartTime = System.nanoTime();

                // Try to execute run
                try {
                    task.run();
                } catch (Exception e) {
                    // If run made it, then update the fields
                } finally {

                    // Accumulate CPU time used for fatigue calculation
                    timeUsed.addAndGet(System.nanoTime() - taskStartTime);

                    // Mark worker as available for new tasks
                    busy.set(false);

                    // Start tracking idle time from now
                    this.idleStartTime.set(System.nanoTime());
                }

            } catch (InterruptedException e) {
                // Thread was interrupted while waiting - continue loop
            }
        }

    }

    @Override
    public int compareTo(TiredThread o) {
        // -1 if the fatigue factor of this is smaller than the fatigue factor of o
        if (this.getFatigue() < o.getFatigue()) {
            return -1;
        }
        // 1 if the fatigue factor of this is greater than the fatigue factor of o
        else if (this.getFatigue() > o.getFatigue()) {
            return 1;
        }
        // If they are equal
        else {
            return 0;
        }
    }
}