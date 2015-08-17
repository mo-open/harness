package org.mds.harness2.tools.collections;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.mds.harness.common2.runner.dsm.DsmRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TestMapPerf extends DsmRunner<TestConfig> {
    private final static Logger log = LoggerFactory.getLogger(TestMapPerf.class);
    //private final static Queue queue = new ConcurrentLinkedQueue();

    private void doTest(final TestConfig conf, final Map<String, Integer> map) {
        this.runSingle("Test ConcurrentMap Add", conf, (configuration, index) -> {
            map.put("" + index, index);
            return 0;
        });
    }

    public void runConcurrentMap(final TestConfig conf) {
        final Map<String, Integer> map = new ConcurrentHashMap();
        this.doTest(conf, map);
    }

    public void runNonBlockingMap(final TestConfig conf) {
        final Map<String, Integer> map = new NonBlockingHashMap();
        this.doTest(conf, map);
    }
}
