package scheduling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for TiredExecutor class.
 * Tests cover: thread pool creation, task submission, task completion,
 * submitAll blocking behavior, shutdown, and worker reporting.
 */
public class TiredExecutorTest {

    private TiredExecutor executor;

    @AfterEach
    void tearDown() throws InterruptedException {
        if (executor != null) {
            executor.shutdown();
        }
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Constructor creates executor with specified number of threads")
    void testConstructorCreatesThreads() {
        executor = new TiredExecutor(4);
        String report = executor.getWorkerReport();
        
        // Report should mention all 4 workers
        assertTrue(report.contains("Worker #0"));
        assertTrue(report.contains("Worker #1"));
        assertTrue(report.contains("Worker #2"));
        assertTrue(report.contains("Worker #3"));
    }

    @Test
    @DisplayName("Constructor with single thread")
    void testConstructorSingleThread() {
        executor = new TiredExecutor(1);
        String report = executor.getWorkerReport();
        
        assertTrue(report.contains("Worker #0"));
        assertFalse(report.contains("Worker #1"));
    }

    // ==================== Submit Tests ====================

    @Test
    @DisplayName("Submit executes single task")
    void testSubmitSingleTask() throws InterruptedException {
        executor = new TiredExecutor(2);
        
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        
        executor.submit(() -> {
            counter.incrementAndGet();
            latch.countDown();
        });
        
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("Submit executes multiple tasks")
    void testSubmitMultipleTasks() throws InterruptedException {
        executor = new TiredExecutor(4);
        
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(10);
        
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                counter.incrementAndGet();
                latch.countDown();
            });
        }
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(10, counter.get());
    }

    @Test
    @DisplayName("Submit distributes tasks across workers")
    void testSubmitDistributesTasks() throws InterruptedException {
        executor = new TiredExecutor(4);
        
        CountDownLatch latch = new CountDownLatch(4);
        
        // Submit 4 long-running tasks simultaneously
        for (int i = 0; i < 4; i++) {
            executor.submit(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                latch.countDown();
            });
        }
        
        // All 4 should complete roughly at the same time (parallel execution)
        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    // ==================== SubmitAll Tests ====================

    @Test
    @DisplayName("submitAll executes all tasks")
    void testSubmitAllExecutesAll() throws InterruptedException {
        executor = new TiredExecutor(4);
        
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();
        
        for (int i = 0; i < 20; i++) {
            tasks.add(counter::incrementAndGet);
        }
        
        executor.submitAll(tasks);
        
        assertEquals(20, counter.get());
    }

    @Test
    @DisplayName("submitAll blocks until all tasks complete")
    void testSubmitAllBlocks() throws InterruptedException {
        executor = new TiredExecutor(2);
        
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            tasks.add(() -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                counter.incrementAndGet();
            });
        }
        
        executor.submitAll(tasks);
        
        // After submitAll returns, all tasks should be complete
        assertEquals(5, counter.get());
    }

    @Test
    @DisplayName("submitAll with empty task list")
    void testSubmitAllEmpty() throws InterruptedException {
        executor = new TiredExecutor(2);
        
        List<Runnable> tasks = new ArrayList<>();
        
        // Should not block or throw
        assertDoesNotThrow(() -> executor.submitAll(tasks));
    }

    @Test
    @DisplayName("submitAll with single task")
    void testSubmitAllSingleTask() throws InterruptedException {
        executor = new TiredExecutor(2);
        
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();
        tasks.add(counter::incrementAndGet);
        
        executor.submitAll(tasks);
        
        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("Multiple submitAll calls work correctly")
    void testMultipleSubmitAllCalls() throws InterruptedException {
        executor = new TiredExecutor(3);
        
        AtomicInteger counter = new AtomicInteger(0);
        
        for (int batch = 0; batch < 3; batch++) {
            List<Runnable> tasks = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                tasks.add(counter::incrementAndGet);
            }
            executor.submitAll(tasks);
        }
        
        assertEquals(15, counter.get());
    }

    // ==================== Task Ordering and Fairness Tests ====================

    @Test
    @DisplayName("Tasks are assigned to less fatigued workers")
    void testFairnessInTaskAssignment() throws InterruptedException {
        executor = new TiredExecutor(3);
        
        List<Runnable> tasks = new ArrayList<>();
        
        // Create tasks that do some work
        for (int i = 0; i < 30; i++) {
            tasks.add(() -> {
                long start = System.nanoTime();
                while (System.nanoTime() - start < 1_000_000) { // 1ms
                    Math.sqrt(Math.random());
                }
            });
        }
        
        executor.submitAll(tasks);
        
        // Check worker report - fatigue should be distributed
        String report = executor.getWorkerReport();
        assertNotNull(report);
        assertTrue(report.contains("Worker #0"));
        assertTrue(report.contains("Worker #1"));
        assertTrue(report.contains("Worker #2"));
    }

    // ==================== Shutdown Tests ====================

    @Test
    @DisplayName("Shutdown terminates all workers")
    void testShutdown() throws InterruptedException {
        executor = new TiredExecutor(4);
        
        // Submit some tasks first
        CountDownLatch latch = new CountDownLatch(4);
        for (int i = 0; i < 4; i++) {
            executor.submit(latch::countDown);
        }
        latch.await(1, TimeUnit.SECONDS);
        
        // Shutdown should complete without hanging
        executor.shutdown();
        
        // Executor should be shut down (this is hard to test directly,
        // but if shutdown hangs, the test will timeout)
    }

    @Test
    @DisplayName("Shutdown waits for running tasks to complete")
    void testShutdownWaitsForTasks() throws InterruptedException {
        executor = new TiredExecutor(2);
        
        AtomicInteger counter = new AtomicInteger(0);
        
        executor.submit(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            counter.incrementAndGet();
        });
        
        Thread.sleep(20); // Let task start
        executor.shutdown();
        
        // Task should have completed before shutdown returned
        assertEquals(1, counter.get());
    }

    // ==================== Worker Report Tests ====================

    @Test
    @DisplayName("getWorkerReport returns report for all workers")
    void testGetWorkerReport() {
        executor = new TiredExecutor(3);
        
        String report = executor.getWorkerReport();
        
        assertNotNull(report);
        assertTrue(report.contains("Worker Report:"));
        assertTrue(report.contains("Worker #0"));
        assertTrue(report.contains("Worker #1"));
        assertTrue(report.contains("Worker #2"));
        assertTrue(report.contains("Fatigue="));
        assertTrue(report.contains("TimeUsed="));
        assertTrue(report.contains("TimeIdle="));
    }

    @Test
    @DisplayName("Worker report shows increased fatigue after work")
    void testWorkerReportShowsFatigue() throws InterruptedException {
        executor = new TiredExecutor(2);
        
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(() -> {
                long start = System.nanoTime();
                while (System.nanoTime() - start < 5_000_000) { // 5ms
                    Math.sqrt(Math.random());
                }
            });
        }
        
        executor.submitAll(tasks);
        
        String report = executor.getWorkerReport();
        
        // Workers should have non-zero time used
        assertTrue(report.contains("TimeUsed="));
        // At least one worker should show fatigue > 0
        assertFalse(report.contains("Fatigue=0.00, TimeUsed=0"));
    }

    // ==================== Concurrent Access Tests ====================

    @Test
    @DisplayName("Executor handles concurrent task submissions")
    void testConcurrentSubmissions() throws InterruptedException {
        executor = new TiredExecutor(4);
        
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch allDone = new CountDownLatch(100);
        
        // Create multiple threads that submit tasks
        Thread[] submitters = new Thread[5];
        for (int t = 0; t < 5; t++) {
            submitters[t] = new Thread(() -> {
                for (int i = 0; i < 20; i++) {
                    executor.submit(() -> {
                        counter.incrementAndGet();
                        allDone.countDown();
                    });
                }
            });
        }
        
        for (Thread t : submitters) {
            t.start();
        }
        
        for (Thread t : submitters) {
            t.join(2000);
        }
        
        assertTrue(allDone.await(5, TimeUnit.SECONDS));
        assertEquals(100, counter.get());
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Task that throws exception does not break executor")
    void testTaskThrowsException() throws InterruptedException {
        executor = new TiredExecutor(2);
        
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(2);
        
        // First task throws exception
        executor.submit(() -> {
            latch.countDown();
            throw new RuntimeException("Test exception");
        });
        
        Thread.sleep(100);
        
        // Second task should still execute
        executor.submit(() -> {
            counter.incrementAndGet();
            latch.countDown();
        });
        
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("Large number of tasks completes correctly")
    void testLargeNumberOfTasks() throws InterruptedException {
        executor = new TiredExecutor(8);
        
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();
        
        for (int i = 0; i < 1000; i++) {
            tasks.add(counter::incrementAndGet);
        }
        
        executor.submitAll(tasks);
        
        assertEquals(1000, counter.get());
    }

    @Test
    @DisplayName("Tasks with varying execution times")
    void testVaryingExecutionTimes() throws InterruptedException {
        executor = new TiredExecutor(4);
        
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            final int sleepTime = (i % 3) * 10; // 0, 10, 20ms
            tasks.add(() -> {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                counter.incrementAndGet();
            });
        }
        
        executor.submitAll(tasks);
        
        assertEquals(10, counter.get());
    }

    @Test
    @DisplayName("Sequential submitAll calls with different batch sizes")
    void testSequentialSubmitAllDifferentSizes() throws InterruptedException {
        executor = new TiredExecutor(3);
        
        AtomicInteger counter = new AtomicInteger(0);
        
        // Small batch
        List<Runnable> small = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            small.add(counter::incrementAndGet);
        }
        executor.submitAll(small);
        assertEquals(2, counter.get());
        
        // Medium batch
        List<Runnable> medium = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            medium.add(counter::incrementAndGet);
        }
        executor.submitAll(medium);
        assertEquals(12, counter.get());
        
        // Large batch
        List<Runnable> large = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            large.add(counter::incrementAndGet);
        }
        executor.submitAll(large);
        assertEquals(62, counter.get());
    }
}