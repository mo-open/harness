package org.mds.harness2.tools.lang;

import org.mds.harness.common2.perf.PerfConfig;
import org.mds.harness.common2.perf.PerfTester;
import org.mds.harness.common2.runner.RunnerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class TestReflect {
    private final static Logger log = LoggerFactory.getLogger(TestReflect.class);
    private static String S = "";

    public static String newTag(String line) {
        return "fdjkfldlf" + line;
    }

    public void runReflect1(PerfConfig configuration) throws Exception {
        log.info("Start test .....");
        final Method method = this.getClass().getMethod("newTag", new Class[]{String.class});
        PerfTester<PerfTester.SingleTask> perfTester = new PerfTester("Test reflect 1", configuration);
        perfTester.run((PerfConfig conf, Integer index) -> {
            try {
                method.invoke(TestReflect.class, "");
            } catch (Exception ex) {
                log.error("Failed to execute method:" + ex);
            }
            return 1;
        });
    }

    public void runReflect2(PerfConfig configuration) throws Exception {
        log.info("Start test .....");
        PerfTester<PerfTester.SingleTask> perfTester = new PerfTester("Test reflect 2", configuration);
        perfTester.run((PerfConfig conf, Integer index) -> {
            try {
                Method method = TestReflect.class.getMethod("newTag", new Class[]{String.class});
                method.invoke(TestReflect.class, "");
            } catch (Exception ex) {
                log.error("Failed to execute method:" + ex);
            }
            return 1;
        });
    }

    public void runNormal(PerfConfig configuration) throws Exception {
        log.info("Start test .....");
        PerfTester<PerfTester.SingleTask> perfTester = new PerfTester("Test normal", configuration);
        perfTester.run((PerfConfig conf, Integer index) -> {
            try {
                TestReflect.newTag("");
            } catch (Exception ex) {
                log.error("Failed to execute method:" + ex);
            }
            return 1;
        });
    }

    public static void main(String args[]) throws Exception {
        RunnerHelper.newInvoker()
                .setArgs(args)
                .setMainClass(TestReflect.class)
                .setConfigClass(PerfConfig.class)
                .setConfigFile("testReglect.yml")
                .invoke();
    }
}
