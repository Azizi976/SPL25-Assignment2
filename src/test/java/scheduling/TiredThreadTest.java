package scheduling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredThreadTest {

    private TiredThread t;

    @AfterEach
    void cleanup() {
        if (t != null && t.isAlive()) {
            t.shutdown();
            try {
                t.join(1000);
            } catch (Exception e) {
            }
        }
    }

    @Test
    void testId() {
        t = new TiredThread(5, 1.0);
        assertEquals(5, t.getWorkerId());
    }

    @Test
    void testFactor() {
        t = new TiredThread(0, 0.75);
        assertEquals(0.0, t.getFatigue());
    }

    @Test
    void testNotBusy() {
        t = new TiredThread(0, 1.0);
        assertFalse(t.isBusy());
    }

    @Test
    void testExec() throws InterruptedException {
        t = new TiredThread(0, 1.0);
        t.start();

        AtomicBoolean flag = new AtomicBoolean(false);
        CountDownLatch countdwn = new CountDownLatch(1);

        t.newTask(() -> {
            flag.set(true);
            countdwn.countDown();
        });

        assertTrue(countdwn.await(1, TimeUnit.SECONDS));
        assertTrue(flag.get());
    }

    @Test
    void testMultiExec() throws InterruptedException {
        t = new TiredThread(0, 1.0);
        t.start();

        AtomicInteger count = new AtomicInteger(0);
        CountDownLatch countdwn = new CountDownLatch(3);

        for (int i = 0; i < 3; i++) {
            Thread.sleep(20);
            t.newTask(() -> {
                count.incrementAndGet();
                countdwn.countDown();
            });
        }

        assertTrue(countdwn.await(2, TimeUnit.SECONDS));
        assertEquals(3, count.get());
    }

    @Test
    void testBusyState() throws InterruptedException {
        t = new TiredThread(0, 1.0);
        t.start();

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch end = new CountDownLatch(1);

        t.newTask(() -> {
            start.countDown();
            try {
                Thread.sleep(200);
            } catch (Exception e) {
            }
            end.countDown();
        });

        start.await();
        assertTrue(t.isBusy());

        end.await();
        Thread.sleep(50);
        assertFalse(t.isBusy());
    }

    @Test
    void testQueueFullThrows() throws InterruptedException {
        // This test assumes queue capacity is 1
        t = new TiredThread(0, 1.0);
        t.start();

        CountDownLatch start = new CountDownLatch(1);

        // 1. Task running
        t.newTask(() -> {
            start.countDown();
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }
        });

        start.await();

        // 2. Task in queue (should pass)
        t.newTask(() -> {
        });

        // 3. Queue full (should throw)
        assertThrows(IllegalStateException.class, () -> t.newTask(() -> {
        }));
    }

    @Test
    void testFatigueCalc() throws InterruptedException {
        double f = 1.5;
        t = new TiredThread(0, f);
        t.start();
        CountDownLatch countdwn = new CountDownLatch(1);

        t.newTask(() -> {
            long s = System.nanoTime();
            while (System.nanoTime() - s < 10000000)
                Math.sqrt(Math.random());
            countdwn.countDown();
        });

        countdwn.await(1, TimeUnit.SECONDS);
        Thread.sleep(50);

        assertEquals(f * t.getTimeUsed(), t.getFatigue(), 0.00001);
    }

    @Test
    void testIdle() throws InterruptedException {
        t = new TiredThread(0, 1.0);
        t.start();
        Thread.sleep(100);

        CountDownLatch countdwn = new CountDownLatch(1);
        t.newTask(countdwn::countDown);
        countdwn.await();

        assertTrue(t.getTimeIdle() > 0);
    }

    @Test
    void testShutdown() throws InterruptedException {
        t = new TiredThread(0, 1.0);
        t.start();
        assertTrue(t.isAlive());
        t.shutdown();
        t.join(1000);
        assertFalse(t.isAlive());
    }

    @Test
    void testCompare() throws InterruptedException {
        TiredThread t1 = new TiredThread(0, 0.5);
        TiredThread t2 = new TiredThread(1, 1.5); // fatigues faster
        t1.start();
        t2.start();

        CountDownLatch countdwn1 = new CountDownLatch(1);
        CountDownLatch countdwn2 = new CountDownLatch(1);
        Runnable work = () -> {
            long s = System.nanoTime();
            while (System.nanoTime() - s < 10000000)
                Math.sqrt(Math.random());
        };

        t1.newTask(() -> {
            work.run();
            countdwn1.countDown();
        });
        t2.newTask(() -> {
            work.run();
            countdwn2.countDown();
        });

        countdwn1.await();
        countdwn2.await();
        Thread.sleep(50);

        // t1 has lower factor -> lower fatigue -> comes first (-1)
        assertTrue(t1.compareTo(t2) < 0);

        t1.shutdown();
        t2.shutdown();
    }

    @Test
    void testCompareEqual() {
        TiredThread t1 = new TiredThread(0, 1.0);
        TiredThread t2 = new TiredThread(1, 1.0);
        assertEquals(0, t1.compareTo(t2));
    }

    @Test
    void testException() throws InterruptedException {
        t = new TiredThread(0, 1.0);
        t.start();
        CountDownLatch countdwn = new CountDownLatch(1);

        t.newTask(() -> {
            countdwn.countDown();
            throw new RuntimeException("err");
        });

        countdwn.await();
        Thread.sleep(50);
        assertTrue(t.isAlive());
        assertFalse(t.isBusy());
    }
}