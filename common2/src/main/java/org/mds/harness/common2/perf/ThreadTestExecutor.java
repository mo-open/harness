package org.mds.harness.common2.perf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by modongsong on 2015/4/21.
 */
public class ThreadTestExecutor extends TestExecutor {
    protected final static Logger log = LoggerFactory.getLogger(ThreadTestExecutor.class);

    protected ThreadTestExecutor(PerfConfig configuration,
                                 AtomicLong iCounter,
                                 int batchSize) {
        super(configuration, iCounter, batchSize);
    }

    @Override
    public void run(PerfTester.Task task) {
        final ExecutorService executorService = Executors.newFixedThreadPool(configuration.threadCount);

        final List<Future> tasks = new ArrayList<Future>();
        final boolean useExternalCounter = iCounter != null;
        final boolean isBatchTask = (task instanceof PerfTester.BatchTask) && batchSize > 0;
        if (iCounter == null) {
            iCounter = new AtomicLong();
        }

        final AtomicLong finishCounter = iCounter;
        final AtomicLong sendCounter = new AtomicLong();
        final AtomicBoolean stop = new AtomicBoolean(false);
        final ThroughputCalculator sendCalculator = new ThroughputCalculator("Sent", configuration, sendCounter);
        final ThroughputCalculator calculator = new ThroughputCalculator("Finished", configuration, finishCounter);
        final int interval = configuration.interval * configuration.intervalUnit;
        new Thread(() -> {

            for (int i = 0; i < configuration.testRounds; i++) {
                sendCounter.set(0);
                finishCounter.set(0);
                tasks.clear();

                sendCalculator.calculate(i);
                calculator.calculate(i);
                for (int j = 0; j < configuration.threadCount; j++) {
                    tasks.add(executorService.submit(() -> {
                        int size = 0;
                        List<Long> indexes = null;
                        if (task instanceof PerfTester.BatchTask) {
                            indexes = new ArrayList();
                        } else
                            size = 1;
                        try {
                            while (true) {
                                long index = sendCounter.incrementAndGet();
                                if (index > configuration.totalCount) {
                                    if (!useExternalCounter)
                                        finishCounter.addAndGet(size);
                                    break;
                                }
                                if (isBatchTask) {
                                    indexes.add(index);
                                    continue;
                                }

                                int returnSize = 0;
                                if (isBatchTask) {
                                    returnSize = task.run(configuration, indexes);
                                    indexes.clear();
                                    size = 0;
                                } else {
                                    returnSize = task.run(configuration, (int) index);
                                }
                                if (!configuration.checkReturn) returnSize = batchSize;
                                if (!useExternalCounter)
                                    finishCounter.addAndGet(returnSize);
                                if (interval > 0) {
                                    LockSupport.parkNanos(interval);
                                }
                            }
                        } catch (Exception ex) {
                            log.error("Failed to run task:" + ex);
                        }
                    }));
                }

                for (Future t : tasks) {
                    try {
                        t.get();
                    } catch (Exception ex) {

                    }
                }
                sendCalculator.waitFinish();
                calculator.waitFinish();
            }
            stop.set(true);
        }).start();

        while (!stop.get()) {
            try {
                Thread.sleep(300);
            } catch (Exception ex) {

            }
        }

        calculator.close();
        sendCalculator.close();
        executorService.shutdownNow();
    }
}
