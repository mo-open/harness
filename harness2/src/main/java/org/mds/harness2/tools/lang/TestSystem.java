package org.mds.harness2.tools.lang;

import org.mds.harness.common2.perf.PerfConfig;
import org.mds.harness.common2.perf.PerfTester;
import org.mds.harness.common2.runner.RunnerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by modongsong on 14-7-2.
 */
public class TestSystem {
    private final static Logger log = LoggerFactory.getLogger(TestSystem.class);

    public void runSysTime(final SystemConfiguration conf) throws Exception {
        log.info("Start test .....");
        new PerfTester("Test SysTime", conf).run((config, index) -> {
            try {
                for (int i = 0; i < conf.times; i++) {
                    System.currentTimeMillis();
                }
            } catch (Exception ex) {
                log.error("Failed to execute method:" + ex);
            }
            return conf.times;
        });
    }

    public static void main(String args[]) throws Exception {
        RunnerHelper.run(args, TestSystem.class,
                SystemConfiguration.class, "system.conf");
    }
}
