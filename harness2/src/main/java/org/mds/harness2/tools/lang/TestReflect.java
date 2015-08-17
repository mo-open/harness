package org.mds.harness2.tools.lang;

import org.mds.harness.common2.runner.dsm.DsmRunner;
import org.mds.harness.common2.runner.dsm.DsmRunnerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class TestReflect extends DsmRunner<DsmRunnerConfig> {
    private final static Logger log = LoggerFactory.getLogger(TestReflect.class);
    private static String S = "";

    public static String newTag(String line) {
        return "fdjkfldlf" + line;
    }

    public void runReflect1(DsmRunnerConfig configuration) throws Exception {
        log.info("Start test .....");
        final Method method = this.getClass().getMethod("newTag", new Class[]{String.class});

        this.runSingle("Test reflect 1", configuration, (configuration1, index1) -> {
            try {
                method.invoke(TestReflect.class, "");
            } catch (Exception ex) {
                log.error("Failed to execute method:" + ex);
            }
            return 1;
        });
    }

    public void runReflect2(DsmRunnerConfig configuration) throws Exception {
        log.info("Start test .....");
        this.runSingle("Test reflect 2", configuration, (configuration1, index1) -> {
            try {
                Method method = TestReflect.class.getMethod("newTag", new Class[]{String.class});
                method.invoke(TestReflect.class, "");
            } catch (Exception ex) {
                log.error("Failed to execute method:" + ex);
            }
            return 1;
        });
    }

    public void runNormal(DsmRunnerConfig configuration) throws Exception {
        log.info("Start test .....");
        this.runSingle("Test Normal", configuration, (configuration1, index1) -> {
            try {
                TestReflect.newTag("");
            } catch (Exception ex) {
                log.error("Failed to execute method:" + ex);
            }
            return 1;
        });
    }
}
