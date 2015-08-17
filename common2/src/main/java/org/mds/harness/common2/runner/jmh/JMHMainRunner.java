package org.mds.harness.common2.runner.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.BenchmarkParams;

@State(Scope.Benchmark)
public abstract class JMHMainRunner<C extends JMHRunnerConfig> extends JMHAbstractRunner<C> {
    @Param({""})
    private String args;

    @Setup
    public void init(BenchmarkParams params) {
        super.doInit(this.args, params);
    }

    @Override
    protected void setup(C config) {

    }
}
