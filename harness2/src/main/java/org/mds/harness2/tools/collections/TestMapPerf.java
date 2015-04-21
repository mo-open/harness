package org.mds.harness2.tools.collections;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.mds.harness.common2.perf.PerfConfig;
import org.mds.harness.common2.perf.PerfTester;
import org.mds.harness.common2.runner.RunnerHelper;
import org.mds.harness2.tools.redis.JedisPerfConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class TestMapPerf {
    private final static Logger log = LoggerFactory.getLogger(TestMapPerf.class);
    //private final static Queue queue = new ConcurrentLinkedQueue();

    private void doTest(final TestConfig conf, final Map<String, Integer> map) {
        new PerfTester<PerfTester.SingleTask>("Test ConcurrentMap Add", conf).run((c, index) -> {
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

    public static void main(String args[]) throws Exception {
        RunnerHelper.run(args, TestMapPerf.class,
                TestConfig.class,
                "test-map.conf");
    }
}
