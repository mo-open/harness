package org.mds.harness.tools.lang;

import org.mds.harness.common.perf.PerfConfig;
import org.mds.harness.common.perf.PerfTester;
import org.mds.harness.common.runner.RunnerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by modongsong on 14-7-2.
 */
public class TestSystem {
    private final static Logger log = LoggerFactory.getLogger(TestSystem.class);


    public void runSysTime(final SystemConfiguration conf) throws Exception {
        log.info("Start test .....");
        PerfTester perfTester = new PerfTester("Test SysTime", conf, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                try {
                    for (int i = 0; i < conf.times; i++) {
                        System.currentTimeMillis();
                    }
                } catch (Exception ex) {
                    log.error("Failed to execute method:" + ex);
                }
                return conf.times;
            }
        });
        perfTester.run();
    }

    public static void main(String args[]) throws Exception {
        RunnerHelper.run(args, TestSystem.class,
                SystemConfiguration.class, "system.conf");
    }
}
