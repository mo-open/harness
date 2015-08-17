package org.mds.harness2.tools.lang;

import org.mds.harness.common2.runner.dsm.DsmRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by modongsong on 14-7-2.
 */
public class TestSystem extends DsmRunner<SystemConfiguration> {
    private final static Logger log = LoggerFactory.getLogger(TestSystem.class);

    public void runSysTime(final SystemConfiguration conf) throws Exception {
        log.info("Start test .....");
        this.runSingle("Test SysTime", conf, (configuration1, index1) -> {
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

}
