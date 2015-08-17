package org.mds.harness.common2.runner.dsm;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by modongsong on 2015/4/21.
 */
public abstract class TestExecutor<T extends DsmRunner.Task> {
    protected DsmRunnerConfig configuration;
    protected AtomicLong iCounter;
    protected int batchSize;

    protected TestExecutor(DsmRunnerConfig configuration,
                           AtomicLong iCounter,
                           int batchSize) {
        this.configuration = configuration;
        this.iCounter = iCounter;
        this.batchSize = batchSize;
    }

    public abstract void run(T task);
}
