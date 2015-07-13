package org.mds.harness.common2.runner;

import org.openjdk.jmh.annotations.Mode;

public abstract class JMHRunnerConfig extends ARunnerConfig {
    static {
        valueOptions.put("jmh.mode", String.join(",", Mode.getKnown()));
    }

    public JMHConfig jmh = new JMHConfig();
    public String runs;

    public JMHConfig jmh() {
        return this.jmh;
    }

    public static class JMHConfig extends ARunnerConfig {
        public String mode = Mode.Throughput.name();
        public int threads = 1;
        public int wIterations;
        public int iterations = 1;
        public int forks = 1;

        public Mode mode() {
            return Mode.deepValueOf(this.mode);
        }
    }
}
