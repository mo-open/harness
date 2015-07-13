package org.mds.harness.common2.runner;

import org.openjdk.jmh.annotations.*;

public class TestBenchmark extends JMHRunner<TestConfig> {
    @Benchmark
    public void benchmark1() {
        String s = new StringBuilder().append(0).toString();
    }

    @Benchmark
    public void benchmark2() {

    }
}
