package org.mds.harness.common2.runner.dsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Randall.mo
 */
public abstract class DsmRunner<C extends DsmRunnerConfig> {
    protected final static Logger log = LoggerFactory.getLogger(DsmRunner.class);

    public interface Task<C extends DsmRunnerConfig, T> {
        int run(C configuration, T index) throws Exception;
    }

    @FunctionalInterface
    public interface SingleTask<C extends DsmRunnerConfig> extends Task<C, Integer> {

    }

    @FunctionalInterface
    public interface BatchTask<C extends DsmRunnerConfig> extends Task<C, List<Long>> {

    }

    protected void runSingle(String name, C config, SingleTask<C> task) {
        this.run(name, config, task, null);
    }

    protected void runSingle(String name, C config, SingleTask<C> task, AtomicLong finishCounter) {
        this.run(name, config, task, finishCounter);
    }

    protected void runBatch(String name, C config, BatchTask<C> task) {
        this.run(name, config, task, null);
    }

    protected void runBatch(String name, C config, BatchTask<C> task, AtomicLong finishCounter) {
        this.run(name, config, task, finishCounter);
    }

    private <T extends Task> void run(String name, C configuration, T task, AtomicLong finishCounter) {
        log.info("Start " + name);

        switch (configuration.runMode) {
            case 1:
                new ThreadTestExecutor(configuration,
                        finishCounter,
                        configuration.batchSize).run(task);
                break;
            case 2:

                break;
            case 3:
                new DisrupterExecutor(configuration,
                        finishCounter,
                        configuration.batchSize).run(task);
        }
    }


}
