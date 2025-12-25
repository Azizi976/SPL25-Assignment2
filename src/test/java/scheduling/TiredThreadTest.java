package scheduling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for TiredThread class.
 * Tests cover: thread lifecycle, task execution, fatigue calculation,
 * busy state, time tracking, and comparison for priority queue ordering.
 */
public class TiredThreadTest {

    private TiredThread thread;

    @AfterEach
    void tearDown() {
        if (thread != null && thread.isAlive()) {
            thread.shutdown();
            try {
                thread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Constructor initializes thread with correct id")
    void testConstructorId() {
        thread = new TiredThread(5, 1.0);
        assertEquals(5, thread.getWorkerId());
    }

    @Test
    @DisplayName("Constructor initializes thread with fatigue factor")
    void testConstructorFatigueFactor() {
        thread = new TiredThread(0, 0.75);
        // Initially fatigue should be 0 since no work done
        assertEquals(0.0, thread.getFatigue(), 1e-9);
    }

    @Test
    @DisplayName("Initial state - not busy")
    void testInitialNotBusy() {
        thread = new TiredThread(0, 1.0);
        assertFalse(thread.isBusy());
    }

    @Test
    @DisplayName("Initial time used is zero")
    void testInitialTimeUsed() {
        thread = new TiredThread(0, 1.0);
        assertEquals(0, thread.getTimeUsed());
    }

    // ==================== Task Execution Tests ====================

    @Test
    @DisplayName("Thread executes submitted task")
    void testExecutesTask() throws InterruptedException {
        thread = new TiredThread(0, 1.0);
        thread.start();
        
        AtomicBoolean taskExecuted = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        
        thread.newTask(() -> {
            taskExecuted.set(true);
            latch.countDown();
        });
        
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertTrue(taskExecuted.get());
    }

    @Test
    @DisplayName("Thread executes multiple tasks sequentially")
    void testExecutesMultipleTasks() throws InterruptedException {
        thread = new TiredThread(0, 1.0);
        thread.start();
        
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(3);
        
        for (int i = 0; i < 3; i++) {
            // Wait for thread to be ready
            Thread.sleep(50);
            thread.newTask(() -> {
                counter.incrementAndGet();
                latch.countDown();
            });
        }
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(3, counter.get());
    }

    @Test
    @DisplayName("Thread becomes busy while executing task")
    void testBusyDuringExecution() throws InterruptedException {
        thread = new TiredThread(0, 1.0);
        thread.start();
        
        CountDownLatch taskStarted = new CountDownLatch(1);
        CountDownLatch taskComplete = new CountDownLatch(1);
        
        thread.newTask(() -> {
            taskStarted.countDown();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            taskComplete.countDown();
        });
        
        taskStarted.await(1, TimeUnit.SECONDS);
        assertTrue(thread.isBusy());
        
        taskComplete.await(1, TimeUnit.SECONDS);
        Thread.sleep(50); // Allow thread to update busy state
        assertFalse(thread.isBusy());
    }

    @Test
    @DisplayName("newTask throws when worker is busy")
    void testNewTaskThrowsWhenBusy() throws InterruptedException {
        thread = new TiredThread(0, 1.0);
        thread.start();
        
        CountDownLatch taskStarted = new CountDownLatch(1);
        
        thread.newTask(() -> {
            taskStarted.countDown();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        taskStarted.await(1, TimeUnit.SECONDS);
        
        assertThrows(IllegalStateException.class, () -> thread.newTask(() -> {}));
    }

    // ==================== Fatigue and Time Tracking Tests ====================

    @Test
    @DisplayName("Time used increases after task execution")
    void testTimeUsedIncreases() throws InterruptedException {
        thread = new TiredThread(0, 1.0);
        thread.start();
        
        CountDownLatch latch = new CountDownLatch(1);
        
        thread.newTask(() -> {
            // Do some work
            long start = System.nanoTime();
            while (System.nanoTime() - start < 10_000_000) { // 10ms
                Math.sqrt(Math.random());
            }
            latch.countDown();
        });
        
        latch.await(1, TimeUnit.SECONDS);
        Thread.sleep(50); // Allow time tracking to complete
        
        assertTrue(thread.getTimeUsed() > 0);
    }

    @Test
    @DisplayName("Fatigue equals fatigue factor times time used")
    void testFatigueCalculation() throws InterruptedException {
        double fatigueFactor = 1.5;
        thread = new TiredThread(0, fatigueFactor);
        thread.start();
        
        CountDownLatch latch = new CountDownLatch(1);
        
        thread.newTask(() -> {
            // Do some work
            long start = System.nanoTime();
            while (System.nanoTime() - start < 10_000_000) { // 10ms
                Math.sqrt(Math.random());
            }
            latch.countDown();
        });
        
        latch.await(1, TimeUnit.SECONDS);
        Thread.sleep(50);
        
        double expectedFatigue = fatigueFactor * thread.getTimeUsed();
        assertEquals(expectedFatigue, thread.getFatigue(), 1e-9);
    }

    @Test
    @DisplayName("Time idle increases while waiting")
    void testTimeIdleIncreases() throws InterruptedException {
        thread = new TiredThread(0, 1.0);
        thread.start();
        
        Thread.sleep(100);
        
        // Submit a task to trigger idle time calculation
        CountDownLatch latch = new CountDownLatch(1);
        thread.newTask(latch::countDown);
        latch.await(1, TimeUnit.SECONDS);
        
        assertTrue(thread.getTimeIdle() > 0);
    }

    // ==================== Shutdown Tests ====================

    @Test
    @DisplayName("Shutdown stops the thread")
    void testShutdown() throws InterruptedException {
        thread = new TiredThread(0, 1.0);
        thread.start();
        
        assertTrue(thread.isAlive());
        
        thread.shutdown();
        thread.join(1000);
        
        assertFalse(thread.isAlive());
    }

    @Test
    @DisplayName("Shutdown after task completion")
    void testShutdownAfterTask() throws InterruptedException {
        thread = new TiredThread(0, 1.0);
        thread.start();
        
        CountDownLatch latch = new CountDownLatch(1);
        thread.newTask(latch::countDown);
        latch.await(1, TimeUnit.SECONDS);
        
        thread.shutdown();
        thread.join(1000);
        
        assertFalse(thread.isAlive());
    }

    // ==================== Comparable Tests (for priority queue) ====================

    @Test
    @DisplayName("compareTo returns negative when less fatigued")
    void testCompareToLessFatigued() throws InterruptedException {
        TiredThread thread1 = new TiredThread(0, 0.5);
        TiredThread thread2 = new TiredThread(1, 1.5);
        
        thread1.start();
        thread2.start();
        
        // Both do same amount of work
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        
        Runnable work = () -> {
            long start = System.nanoTime();
            while (System.nanoTime() - start < 10_000_000) {
                Math.sqrt(Math.random());
            }
        };
        
        thread1.newTask(() -> { work.run(); latch1.countDown(); });
        Thread.sleep(100);
        thread2.newTask(() -> { work.run(); latch2.countDown(); });
        
        latch1.await(1, TimeUnit.SECONDS);
        latch2.await(1, TimeUnit.SECONDS);
        Thread.sleep(50);
        
        // thread1 with lower fatigue factor should have lower fatigue
        assertTrue(thread1.getFatigue() < thread2.getFatigue());
        assertTrue(thread1.compareTo(thread2) < 0);
        
        thread1.shutdown();
        thread2.shutdown();
        thread1.join(1000);
        thread2.join(1000);
    }

    @Test
    @DisplayName("compareTo returns positive when more fatigued")
    void testCompareToMoreFatigued() throws InterruptedException {
        TiredThread thread1 = new TiredThread(0, 1.5);
        TiredThread thread2 = new TiredThread(1, 0.5);
        
        thread1.start();
        thread2.start();
        
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        
        Runnable work = () -> {
            long start = System.nanoTime();
            while (System.nanoTime() - start < 10_000_000) {
                Math.sqrt(Math.random());
            }
        };
        
        thread1.newTask(() -> { work.run(); latch1.countDown(); });
        Thread.sleep(100);
        thread2.newTask(() -> { work.run(); latch2.countDown(); });
        
        latch1.await(1, TimeUnit.SECONDS);
        latch2.await(1, TimeUnit.SECONDS);
        Thread.sleep(50);
        
        assertTrue(thread1.getFatigue() > thread2.getFatigue());
        assertTrue(thread1.compareTo(thread2) > 0);
        
        thread1.shutdown();
        thread2.shutdown();
        thread1.join(1000);
        thread2.join(1000);
    }

    @Test
    @DisplayName("compareTo returns zero for equal fatigue")
    void testCompareToEqual() {
        TiredThread thread1 = new TiredThread(0, 1.0);
        TiredThread thread2 = new TiredThread(1, 1.0);
        
        // Neither has done work, both have 0 fatigue
        assertEquals(0, thread1.compareTo(thread2));
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Thread handles task that throws exception")
    void testTaskThrowsException() throws InterruptedException {
        thread = new TiredThread(0, 1.0);
        thread.start();
        
        CountDownLatch latch = new CountDownLatch(1);
        
        thread.newTask(() -> {
            latch.countDown();
            throw new RuntimeException("Test exception");
        });
        
        latch.await(1, TimeUnit.SECONDS);
        Thread.sleep(100);
        
        // Thread should still be alive and able to accept new tasks
        assertTrue(thread.isAlive());
        assertFalse(thread.isBusy());
    }

    @Test
    @DisplayName("Multiple threads with different fatigue factors")
    void testMultipleThreadsDifferentFactors() {
        TiredThread[] threads = new TiredThread[5];
        double[] factors = {0.5, 0.75, 1.0, 1.25, 1.5};
        
        for (int i = 0; i < 5; i++) {
            threads[i] = new TiredThread(i, factors[i]);
            assertEquals(i, threads[i].getWorkerId());
        }
        
        // Cleanup
        for (TiredThread t : threads) {
            if (t.isAlive()) {
                t.shutdown();
            }
        }
    }

    @Test
    @DisplayName("Thread with minimum fatigue factor 0.5")
    void testMinimumFatigueFactor() throws InterruptedException {
        thread = new TiredThread(0, 0.5);
        thread.start();
        
        CountDownLatch latch = new CountDownLatch(1);
        thread.newTask(() -> {
            long start = System.nanoTime();
            while (System.nanoTime() - start < 5_000_000) {
                Math.sqrt(Math.random());
            }
            latch.countDown();
        });
        
        latch.await(1, TimeUnit.SECONDS);
        Thread.sleep(50);
        
        double fatigue = thread.getFatigue();
        long timeUsed = thread.getTimeUsed();
        assertEquals(0.5 * timeUsed, fatigue, 1e-9);
    }

    @Test
    @DisplayName("Thread with maximum fatigue factor 1.5")
    void testMaximumFatigueFactor() throws InterruptedException {
        thread = new TiredThread(0, 1.5);
        thread.start();
        
        CountDownLatch latch = new CountDownLatch(1);
        thread.newTask(() -> {
            long start = System.nanoTime();
            while (System.nanoTime() - start < 5_000_000) {
                Math.sqrt(Math.random());
            }
            latch.countDown();
        });
        
        latch.await(1, TimeUnit.SECONDS);
        Thread.sleep(50);
        
        double fatigue = thread.getFatigue();
        long timeUsed = thread.getTimeUsed();
        assertEquals(1.5 * timeUsed, fatigue, 1e-9);
    }
}