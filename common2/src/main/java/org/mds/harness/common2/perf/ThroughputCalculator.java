package org.mds.harness.common2.perf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by modongsong on 2015/4/21.
 */
public class ThroughputCalculator {
    protected final static Logger log = LoggerFactory.getLogger(ThroughputCalculator.class);
    AtomicLong counter;
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    PerfConfig configuration;
    Future task = null;
    String name = "";

    public ThroughputCalculator(String name, PerfConfig configuration, AtomicLong counter) {
        this.name = name;
        this.configuration = configuration;
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
                log.info(ThroughputCalculator.this.name + " round-" + roundNumber + " throughput: " + throughout);
            else
                log.info(ThroughputCalculator.this.name + ":" + secondCount);
        }

    }

    public ThroughputCalculator calculate(final int roundNumber) {
        this.task = this.executorService.submit(() -> {
            output(roundNumber);
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
