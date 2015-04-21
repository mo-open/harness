package org.mds.harness.common2.perf;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by modongsong on 2015/4/21.
 */
public abstract class TestExecutor<T extends PerfTester.Task> {
    protected PerfConfig configuration;
    protected AtomicLong iCounter;
    protected int batchSize;

    protected TestExecutor(PerfConfig configuration,
                           AtomicLong iCounter,
                           int batchSize) {
        this.configuration = configuration;
        this.iCounter = iCounter;
        this.batchSize = batchSize;
    }

    public abstract void run(T task);
}
