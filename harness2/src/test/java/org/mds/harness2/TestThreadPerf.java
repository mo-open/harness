package org.mds.harness2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class TestThreadPerf {
    private final static Logger log = LoggerFactory.getLogger(TestThreadPerf.class);

    public static void run(String testName, int threadCount, final int totalCount, final long sleep) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        final AtomicInteger counter = new AtomicInteger(0);
        List<Future> tasks = new ArrayList<>();
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(threadCount + 1);
        long startTime = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            tasks.add(executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (counter.incrementAndGet() < totalCount) {
                            Thread.sleep(sleep);
                        }
                    } catch (Exception ex) {

                    }
                }
            }));
        }
        for (Future future : tasks) {
            try {
                future.get();
            } catch (Exception ex) {

            }
        }
        executorService.shutdown();
        long throughout = totalCount * 1000L * 1000L * 1000L / (System.nanoTime() - startTime);
        log.info(testName + " throughout: " + throughout);
    }

    public static void main(String args[]) {
        run("Test1", 4, 500, 1);
        run("Test2", 10, 500, 1);
        run("Test3", 30, 500, 1);
        run("Test4", 100, 500, 1);
        run("Test5", 500, 500, 1);
        run("Test1", 4, 500, 10);
        run("Test2", 10, 500, 10);
        run("Test3", 30, 500, 10);
        run("Test4", 100, 500, 10);
        run("Test5", 500, 500, 10);
        run("Test1", 4, 500, 30);
        run("Test2", 10, 500, 30);
        run("Test3", 30, 500, 30);
        run("Test4", 100, 500, 30);
        run("Test5", 500, 500, 30);
    }
}
