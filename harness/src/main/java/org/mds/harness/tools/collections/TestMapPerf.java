package org.mds.harness.tools.collections;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class TestMapPerf {
    private final static Logger log = LoggerFactory.getLogger(TestMapPerf.class);
    //private final static Queue queue = new ConcurrentLinkedQueue();

    private class PrepareDataTask implements Runnable {
        private String key;
        AtomicInteger counter;
        int totalCount;
        private Map map;

        public PrepareDataTask(Map map, String key, AtomicInteger counter, int totalCount) {
            this.key = key;
            this.counter = counter;
            this.totalCount = totalCount;
            this.map = map;
        }

        @Override
        public void run() {
            try {
                while (this.counter.getAndIncrement() < totalCount) {
                    map.put(key, 10000L);
                    map.get(key);
                    map.remove(key);
                }
            } catch (Exception ex) {
                log.error(ex.toString());
            }

        }
    }

    public void run(final Map map, int totalCount, int threadCount, int testRounds) {
        List<Future> prepareDataTasks = new ArrayList();
        AtomicInteger counter = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int r = 0; r < testRounds; r++) {
            long startTime = System.nanoTime();
            for (int i = 0; i < threadCount; i++) {
                prepareDataTasks.add(executor.submit(new PrepareDataTask(map, "" + i, counter, totalCount)));
            }

            for (Future future : prepareDataTasks) {
                try {
                    future.get();
                } catch (Exception ex) {

                }
            }
            log.info("Test Round " + r + ", spend time: " + (System.nanoTime() - startTime));
        }
        executor.shutdown();
    }


    public void test1() {
        this.run(new ConcurrentHashMap(), 500000, 100, 10);
        this.run(new NonBlockingHashMap(), 500000, 100, 10);
    }


    public void test2() {
        //in this test, the performance NonBlockingHashMap is better obviously.
        this.run(new ConcurrentHashMap(), 500000, 1000, 10);
        this.run(new NonBlockingHashMap(), 500000, 1000, 10);
    }

}
