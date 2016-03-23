package org.mds.harness.common2.runner.dsm;

import org.mds.harness.common2.runner.RunnerHelper;

public class DsmRunnerMain {
    public static void main(String[] args) throws Exception {
        String className = args[0];
        RunnerHelper.newInvoker()
                .setArgs(args)
                .setMainClassName(className)
                .setJMH(false)
                .setMethodName("run").invoke();
    }
}
