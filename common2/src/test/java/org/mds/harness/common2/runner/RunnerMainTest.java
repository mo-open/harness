package org.mds.harness.common2.runner;


public class RunnerMainTest {
    public static void main(String args[]) throws Exception {
        JMHRunnerMain.main(new String[]{"org.mds.harness.common2.runner.TestBenchmark", "-f", "test-runner.yaml", "a=1", "runs=mark1,mark3,mark4"});
        JMHRunnerMain.main(new String[]{"org.mds.harness.common2.runner.TestBenchmark", "-h"});
    }
}