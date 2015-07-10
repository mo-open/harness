package org.mds.harness.common2.runner;

import org.openjdk.jmh.annotations.Mode;

public abstract class JMHRunnerConfig extends ARunnerConfig {
    static {
        valueOptions.put("mode", String.join(" ", Mode.getKnown()));
    }

    private JMHConfig jmhConfig = new JMHConfig();

    public JMHConfig jmhConfig() {
        return this.jmhConfig;
    }

    public static class JMHConfig extends ARunnerConfig {
        String mode = Mode.Throughput.name();
        int threads = 1;
        int wIterations;
        int iterations = 1;
        int forks = 1;

        public Mode mode() {
            return Mode.valueOf(this.mode);
        }
    }
}
