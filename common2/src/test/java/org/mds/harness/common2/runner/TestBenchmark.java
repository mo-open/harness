package org.mds.harness.common2.runner;

import org.mds.harness.common2.runner.jmh.JMHInternalRunner;
import org.mds.harness.common2.runner.jmh.JMHMainRunner;
import org.openjdk.jmh.annotations.*;

public class TestBenchmark extends JMHMainRunner<TestConfig> {
    @Benchmark
    public void benchmark1() {
        String s = new StringBuilder().append(0).toString();
    }

    @Benchmark
    public void benchmark2() {

    }

    public static class InsertSetup extends JMHInternalRunner<TestConfig> {
        public void setup(TestConfig config) {
            log.info("setup for Insert, config: " + config);
        }
    }

    public static class GetSetup extends JMHInternalRunner<TestConfig> {
        public void setup(TestConfig config) {
            log.info("setup for Get, config: " + config);
        }
    }

    @Benchmark
    public void benchmark3(InsertSetup insertSetup) {

    }

    @Benchmark
    public void benchmark4(GetSetup getSetup) {

    }
}
