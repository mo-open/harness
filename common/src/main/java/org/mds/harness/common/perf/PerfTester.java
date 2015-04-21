package org.mds.harness.common.perf;

import org.mds.hprocessor.processor.DisruptorProcessor;
import org.mds.hprocessor.processor.ProcessorHandler;
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
 * @author Randall.mo
 */
public class PerfTester {
    protected final static Logger log = LoggerFactory.getLogger(PerfTester.class);
    private String name;
    private PerfConfig configuration;
    private Task task;
    private BatchTask batchTask;

    public interface Task {
        int run(PerfConfig configuration, int index);
    }

    public interface BatchTask {
        int run(PerfConfig configuration, List<Long> indexes);
    }

    public PerfTester(String name, PerfConfig configuration, Task task) {
        this.name = name;
        this.configuration = configuration;
        this.task = task;
        if (this.configuration == null) {
            this.configuration = new PerfConfig();
        }
    }

    public PerfTester(String name, PerfConfig configuration, BatchTask batchTask) {
        this.name = name;
        this.configuration = configuration;
        this.batchTask = batchTask;
        if (this.configuration == null) {
            this.configuration = new PerfConfig();
        }
    }

    public void run() {
        this.run(null);
    }

    public void run(AtomicLong finishCounter) {
        log.info("Start " + this.name);
        if (batchTask == null)
            configuration.batchSize = 1;
        if (configuration.batchSize <= 0) configuration.batchSize = 1;

        switch (configuration.runMode) {
            case 1:
                this.run1(finishCounter, configuration.batchSize);
                break;
            case 2:
                this.run2(finishCounter, configuration.batchSize);
                break;
            case 3:
                this.run3(finishCounter, configuration.batchSize);
        }
    }

    public static void waitRun(String name, PerfConfig configuration, AtomicLong counter) {
        new PerfTester(name, configuration, (Task) null).newCalculator(name, counter);
    }

    private ThroughoutputCalculator newCalculator(String name, AtomicLong counter) {
        return new ThroughoutputCalculator(name, counter);
    }

    private class ThroughoutputCalculator {
        AtomicLong counter;
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future task = null;
        String name = "";

        public ThroughoutputCalculator(String name, AtomicLong counter) {
            this.name = name;
            this.counter = counter;
        }

        private void output(final int roundNumber) {
            long firstCount = 0;
            long secondCount = 0;
            int noChanges = 0;

            while (secondCount < configuration.totalCount) {
                firstCount = counter.get();
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                }
                secondCount = counter.get();
                long throughout = secondCount - firstCount;
                if (throughout == 0) {
                    noChanges++;
                } else
                    noChanges = 0;
                if (noChanges > 3) {
                    return;
                }
                if (configuration.output == 0)
                    log.info(ThroughoutputCalculator.this.name + " round-" + roundNumber + " throughput: " + throughout);
                else
                    log.info(ThroughoutputCalculator.this.name + ":" + secondCount);
            }

        }

        public ThroughoutputCalculator calculate(final int roundNumber) {
            this.task = this.executorService.submit(new Runnable() {
                @Override
                public void run() {
                    output(roundNumber);
                }
            });
            return this;
        }

        public void waitFinish() {
            if (this.task != null && !this.task.isCancelled() && !this.task.isDone()) {
                try {
                    this.task.get();
                } catch (Exception ex) {

                }
            }
        }

        public void close() {
            executorService.shutdownNow();
        }
    }

    private void run1(AtomicLong iCounter, final int batchSize) {
        final ExecutorService executorService = Executors.newFixedThreadPool(configuration.threadCount);

        final List<Future> tasks = new ArrayList<Future>();
        final boolean useExternalCounter = iCounter != null;
        if (iCounter == null) {
            iCounter = new AtomicLong();
        }

        final AtomicLong finishCounter = iCounter;
        final AtomicLong sendCounter = new AtomicLong();
        final AtomicBoolean stop = new AtomicBoolean(false);
        final ThroughoutputCalculator sendCalculator = new ThroughoutputCalculator("Sent", sendCounter);
        final ThroughoutputCalculator calculator = new ThroughoutputCalculator("Finished", finishCounter);
        final int interval = configuration.interval * configuration.intervalUnit;
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < configuration.testRounds; i++) {
                    sendCounter.set(0);
                    finishCounter.set(0);
                    tasks.clear();

                    sendCalculator.calculate(i);
                    calculator.calculate(i);
                    for (int j = 0; j < configuration.threadCount; j++) {
                        tasks.add(executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                int size = 0;
                                List<Long> indexes = null;
                                if (batchTask != null) {
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
                                        if (batchTask != null && size++ < batchSize) {
                                            indexes.add(index);
                                            continue;
                                        }

                                        int returnSize = 0;
                                        if (batchTask != null) {
                                            returnSize = batchTask.run(configuration, indexes);
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
                            }
                        }));
                    }

                    for (Future task : tasks) {
                        try {
                            task.get();
                        } catch (Exception ex) {

                        }
                    }
                    sendCalculator.waitFinish();
                    calculator.waitFinish();
                }
                stop.set(true);
            }

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

    private void run2(AtomicLong iCounter, final int batchSize) {
        final ExecutorService executorService = Executors.newFixedThreadPool(configuration.threadCount);
        final AtomicBoolean stop = new AtomicBoolean(false);

        final boolean useExternalCounter = iCounter != null;
        if (iCounter == null) {
            iCounter = new AtomicLong();
        }

        final AtomicLong sendCounter = new AtomicLong();
        final ThroughoutputCalculator sendCalculator = new ThroughoutputCalculator("Sent", sendCounter);
        final AtomicLong finishCounter = iCounter;
        final ThroughoutputCalculator calculator = new ThroughoutputCalculator("Finished", finishCounter);
        final int interval = configuration.interval * 1000 * 1000;
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < configuration.testRounds; i++) {
                    sendCounter.set(0);
                    calculator.calculate(i);
                    sendCalculator.calculate(i);
                    int size = 0;
                    final List indexes = new ArrayList();

                    for (int j = 0; j < configuration.totalCount; j++) {
                        final int index = j;
                        if (batchTask != null) {
                            if (size++ < batchSize) {
                                indexes.add(index);
                                if (j < configuration.totalCount - 1)
                                    continue;
                            }
                        } else
                            size = 1;
                        final int curSize = size;
                        if (batchTask != null) size = 0;
                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                int returnSize = 0;
                                try {
                                    sendCounter.addAndGet(curSize);
                                    if (batchTask != null) {
                                        returnSize = batchTask.run(configuration, indexes);
                                        indexes.clear();
                                    } else {
                                        returnSize = task.run(configuration, index);
                                    }
                                } catch (Exception ex) {

                                }
                                if (!configuration.checkReturn) returnSize = curSize;
                                if (!useExternalCounter)
                                    finishCounter.addAndGet(returnSize);
                                if (interval > 0) {
                                    LockSupport.parkNanos(interval);
                                }
                            }
                        });
                    }
                    calculator.waitFinish();
                }
                stop.set(true);
            }
        }).start();

        while (!stop.get()) {
            try {
                Thread.sleep(300);
            } catch (Exception ex) {

            }
        }
        calculator.close();
        executorService.shutdownNow();
    }

    private void run3(AtomicLong iCounter, final int batchSize) {
        if (iCounter == null) {
            iCounter = new AtomicLong();
        }

        final AtomicLong finishCounter = iCounter;
        final AtomicLong sendCounter = new AtomicLong();
        DisruptorProcessor<Integer> processor = DisruptorProcessor.<Integer>newBuilder()
                .setBufferSize(configuration.totalCount)
                .addNext(configuration.threadCount,
                        new ProcessorHandler<Integer>() {
                            @Override
                            public void process(Integer index) {
                                finishCounter.incrementAndGet();
                                task.run(configuration, index);
                            }
                        }).build();

        for (int i = 0; i < configuration.testRounds; i++) {
            sendCounter.set(0);
            for (int k = 0; k < configuration.totalCount; k++) {
                sendCounter.incrementAndGet();
                processor.submit(k);
            }
        }
    }

    private void run4(final int batchSize) {

    }
}
