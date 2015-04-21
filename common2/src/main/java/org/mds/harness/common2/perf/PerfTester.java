package org.mds.harness.common2.perf;

import org.mds.hprocessor.processor.DisruptorProcessor;
import org.mds.hprocessor.processor.ProcessorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Randall.mo
 */
public class PerfTester<T extends PerfTester.Task> {
    protected final static Logger log = LoggerFactory.getLogger(PerfTester.class);
    private String name;
    private PerfConfig configuration;
    private Task task;
    private BatchTask batchTask;

    public interface Task<T> {
        int run(PerfConfig configuration, T index);
    }

    @FunctionalInterface
    public interface SingleTask extends Task<Integer> {

    }

    @FunctionalInterface
    public interface BatchTask extends Task<List<Long>> {

    }

    public PerfTester(String name, PerfConfig configuration) {
        this.name = name;
        this.configuration = configuration;

        if (this.configuration == null) {
            this.configuration = new PerfConfig();
        }
    }

    public void run(T task) {
        this.run(task, null);
    }

    public void run(T task, AtomicLong finishCounter) {
        log.info("Start " + this.name);
        if (batchTask == null)
            configuration.batchSize = 1;
        if (configuration.batchSize <= 0) configuration.batchSize = 1;

        switch (configuration.runMode) {
            case 1:
                new ThreadTestExecutor(this.configuration,
                        finishCounter,
                        configuration.batchSize).run(task);
                break;
            case 2:

                break;
            case 3:
                new DisrupterExecutor(this.configuration,
                        finishCounter,
                        configuration.batchSize).run(task);
        }
    }


}
