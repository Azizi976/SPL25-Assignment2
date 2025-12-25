package scheduling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutorTest {

    private TiredExecutor exec;

    // we have to cleanup the executer after each test
    @AfterEach
    void cleanUp() {
        if (exec != null) {
            try {
                exec.shutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void testExecSize() {
        exec = new TiredExecutor(4);
        String rep = exec.getWorkerReport();

        // check all workers exist
        assertTrue(rep.contains("Worker #0"));
        assertTrue(rep.contains("Worker #3"));
    }

    @Test
    void testOneThread() {
        exec = new TiredExecutor(1);
        String s = exec.getWorkerReport();
        assertTrue(s.contains("Worker #0"));
        assertFalse(s.contains("Worker #1"));
    }

    @Test
    void testRunOne() throws InterruptedException {
        exec = new TiredExecutor(2);
        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch countdwn = new CountDownLatch(1);

        exec.submit(() -> {
            count.incrementAndGet();
            countdwn.countDown();
        });

        assertTrue(countdwn.await(1000, TimeUnit.MILLISECONDS));
        assertEquals(1, count.get());
    }

    @Test
    void testRunMultiple() throws InterruptedException {
        exec = new TiredExecutor(4);
        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch countdwn = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            exec.submit(() -> {
                count.incrementAndGet();
                countdwn.countDown();
            });
        }

        assertTrue(countdwn.await(2, TimeUnit.SECONDS));
        assertEquals(10, count.get());
    }

    @Test
    void testParallel() throws InterruptedException {
        exec = new TiredExecutor(4);
        CountDownLatch countdwn = new CountDownLatch(4);

        for (int i = 0; i < 4; i++) {
            exec.submit(() -> {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
                countdwn.countDown();
            });
        }

        // should finish fast if parallel
        assertTrue(countdwn.await(1500, TimeUnit.MILLISECONDS));
    }

    @Test
    void testSubmitAll() throws InterruptedException {
        exec = new TiredExecutor(4);
        AtomicInteger count = new AtomicInteger(0);
        List<Runnable> list = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            list.add(count::incrementAndGet);
        }

        exec.submitAll(list);
        assertEquals(20, count.get());
    }

    @Test
    void testSubmitAllBlocking() throws InterruptedException {
        exec = new TiredExecutor(2);
        AtomicInteger count = new AtomicInteger(0);
        List<Runnable> list = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            list.add(() -> {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                }
                count.incrementAndGet();
            });
        }

        exec.submitAll(list);

        // must be done after return
        assertEquals(5, count.get());
    }

    @Test
    void testEmptyList() {
        exec = new TiredExecutor(2);
        assertDoesNotThrow(() -> exec.submitAll(new ArrayList<>()));
    }

    @Test
    void testSubmitAllOneTask() throws InterruptedException {
        exec = new TiredExecutor(2);
        AtomicInteger count = new AtomicInteger(0);
        List<Runnable> l = new ArrayList<>();
        l.add(count::incrementAndGet);

        exec.submitAll(l);
        assertEquals(1, count.get());
    }

    @Test
    void testMultipleBatches() throws InterruptedException {
        exec = new TiredExecutor(3);
        AtomicInteger count = new AtomicInteger(0);

        for (int k = 0; k < 3; k++) {
            List<Runnable> l = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                l.add(count::incrementAndGet);
            }
            exec.submitAll(l);
        }
        assertEquals(15, count.get());
    }

    @Test
    void testFairness() throws InterruptedException {
        exec = new TiredExecutor(3);
        List<Runnable> l = new ArrayList<>();

        // tasks that take time
        for (int i = 0; i < 30; i++) {
            l.add(() -> {
                long start = System.nanoTime();
                while (System.nanoTime() - start < 1_000_000) {
                    Math.sqrt(Math.random());
                }
            });
        }

        exec.submitAll(l);

        String report = exec.getWorkerReport();
        assertTrue(report.contains("Worker #0"));
        assertTrue(report.contains("Worker #2"));
    }

    @Test
    void testShutdown() throws InterruptedException {
        exec = new TiredExecutor(4);
        CountDownLatch countdwn = new CountDownLatch(4);
        for (int i = 0; i < 4; i++)
            exec.submit(countdwn::countDown);
        countdwn.await(1, TimeUnit.SECONDS);

        exec.shutdown();
    }

    @Test
    void testShutdownWaits() throws InterruptedException {
        exec = new TiredExecutor(2);
        AtomicInteger count = new AtomicInteger(0);

        exec.submit(() -> {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
            count.incrementAndGet();
        });

        Thread.sleep(20);
        exec.shutdown();

        assertEquals(1, count.get());
    }

    @Test
    void testReport() {
        exec = new TiredExecutor(3);
        String s = exec.getWorkerReport();
        assertNotNull(s);
        assertTrue(s.contains("Fatigue="));
    }

    @Test
    void testReportUpdate() throws InterruptedException {
        exec = new TiredExecutor(2);
        List<Runnable> l = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            l.add(() -> {
                long s = System.nanoTime();
                while (System.nanoTime() - s < 5000000)
                    Math.sqrt(Math.random());
            });
        }
        exec.submitAll(l);

        String s = exec.getWorkerReport();
        assertTrue(s.contains("TimeUsed="));
    }

    @Test
    void testConcurrent() throws InterruptedException {
        exec = new TiredExecutor(4);
        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch countdwn = new CountDownLatch(100);

        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 20; j++) {
                    exec.submit(() -> {
                        count.incrementAndGet();
                        countdwn.countDown();
                    });
                }
            });
            threads[i].start();
        }

        for (Thread t : threads)
            t.join();
        assertTrue(countdwn.await(5, TimeUnit.SECONDS));
        assertEquals(100, count.get());
    }

    @Test
    void testExceptionTask() throws InterruptedException {
        exec = new TiredExecutor(2);
        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch countdwn = new CountDownLatch(2);

        exec.submit(() -> {
            countdwn.countDown();
            throw new RuntimeException("boom");
        });

        Thread.sleep(50);

        exec.submit(() -> {
            count.incrementAndGet();
            countdwn.countDown();
        });

        countdwn.await(2, TimeUnit.SECONDS);
        assertEquals(1, count.get());
    }

    @Test
    void testManyTasks() throws InterruptedException {
        exec = new TiredExecutor(8);
        AtomicInteger count = new AtomicInteger(0);
        List<Runnable> l = new ArrayList<>();
        for (int i = 0; i < 1000; i++)
            l.add(count::incrementAndGet);

        exec.submitAll(l);
        assertEquals(1000, count.get());
    }

}