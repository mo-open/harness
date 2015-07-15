package org.mds.harness.common2.runner;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;

@State(Scope.Benchmark)
public abstract class JMHInternalRunner<C extends JMHRunnerConfig> extends JMHAbstractRunner<C> {

    @Param({""})
    private String args;

    @Setup
    public void init(BenchmarkParams params) {
        super.doInit(this.args, params);
    }

    @TearDown
    public void end() {
        this.tearDown();
    }

    protected void tearDown() {

    }
}
