package org.mds.harness2.tools.mapdb;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.mds.harness.common2.perf.PerfTester;
import org.mds.harness.common2.runner.RunnerHelper;
import org.mds.harness2.tools.memcached.TestMemcachedConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mapdb.*;

/**
 * Created by modongsong on 2015/4/22.
 */
public class TestMapDBPerf {
    private final static Logger log = LoggerFactory.getLogger(TestMapDBPerf.class);

    public void runHashMap(final TestMapDBConfig config) {
        final String baseValue = RandomStringUtils.random(config.valueLen);
        TestMapDBHelper.createHTreeMap(config).forEach(map -> {
            new PerfTester<PerfTester.SingleTask>("Test HashMap of MapDB", config)
                    .run((configuration, index) -> {
                        if (config.opMode != 0)
                            map.put("key" + index, baseValue + index);
                        if (config.opMode != 1)
                            map.remove("key" + index);
                        return 1;
                    });
        });
    }

    public void runHashSet(final TestMapDBConfig config) {
        final String baseValue = RandomStringUtils.random(config.valueLen);
        TestMapDBHelper.createHTreeSet(config).forEach(map -> {
            new PerfTester<PerfTester.SingleTask>("Test HashSet of MapDB", config)
                    .run((configuration, index) -> {
                        if (config.opMode != 0)
                            map.add(baseValue + index);
                        if (config.opMode != 1)
                            map.remove(baseValue + index);
                        return 1;
                    });
        });
    }

    public void runTreeMap(final TestMapDBConfig config) {
        final String baseValue = RandomStringUtils.random(config.valueLen);
        TestMapDBHelper.createBTreeMap(config).forEach(map -> {
            new PerfTester<PerfTester.SingleTask>("Test TreeMap of MapDB", config)
                    .run((configuration, index) -> {
                        if (config.opMode != 0)
                            map.put("key" + index, baseValue + index);
                        if (config.opMode != 1)
                            map.remove("key" + index);
                        return 1;
                    });
        });
    }

    public void runTreeSet(final TestMapDBConfig config) {
        final String baseValue = RandomStringUtils.random(config.valueLen);
        TestMapDBHelper.createBTreeSet(config).forEach(map -> {
            new PerfTester<PerfTester.SingleTask>("Test TreeSet of MapDB", config)
                    .run((configuration, index) -> {
                        if (config.opMode != 0)
                            map.add(baseValue + index);
                        if (config.opMode != 1)
                            map.remove("key" + index);
                        return 1;
                    });
        });
    }

    public void runQueue(final TestMapDBConfig config) {
        final String baseValue = RandomStringUtils.random(config.valueLen);
        TestMapDBHelper.createQueue(config).forEach(map -> {
            new PerfTester<PerfTester.SingleTask>("Test Queue of MapDB", config)
                    .run((configuration, index) -> {
                        try {
                            if (config.opMode != 0)
                                map.put(baseValue + index);
                            if (config.opMode != 1)
                                map.take();
                        } catch (Exception ex) {
                            log.error("",ex);
                        }
                        return 1;
                    });
        });
    }

    public void runCQueue(final TestMapDBConfig config) {
        final String baseValue = RandomStringUtils.random(config.valueLen);
        TestMapDBHelper.createCQueue(config).forEach(map -> {
            new PerfTester<PerfTester.SingleTask>("Test CQueue of MapDB", config)
                    .run((configuration, index) -> {
                        try {
                            if (config.opMode != 0)
                                map.put(baseValue + index);
                            if (config.opMode != 1)
                                map.take();
                        } catch (Exception ex) {
                            log.error("",ex);
                        }
                        return 1;
                    });
        });
    }

    public void runStack(final TestMapDBConfig config) {
        final String baseValue = RandomStringUtils.random(config.valueLen);
        TestMapDBHelper.createStack(config).forEach(map -> {
            new PerfTester<PerfTester.SingleTask>("Test Stack of MapDB", config)
                    .run((configuration, index) -> {
                        try {
                            if (config.opMode != 0)
                                map.put(baseValue + index);
                            if (config.opMode != 1)
                                map.take();
                        } catch (Exception ex) {
                            log.error("",ex);
                        }
                        return 1;
                    });
        });
    }


    public static void main(String args[]) throws Exception {
        RunnerHelper.run(args, TestMapDBPerf.class,
                TestMapDBConfig.class,
                "testMapDB.yml");
    }
}
