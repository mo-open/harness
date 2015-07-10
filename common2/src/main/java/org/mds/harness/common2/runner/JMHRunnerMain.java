package org.mds.harness.common2.runner;

public class JMHRunnerMain {
    public static void main(String[] args) throws Exception {
        String className = args[0];
        RunnerHelper.newInvoker()
                .setArgs(args)
                .setMainClassName(className)
                .setJMH(true)
                .setMethodName("bench").invoke();
    }
}
