package org.mds.harness2.tools.mapdb;

import org.apache.commons.lang3.RandomStringUtils;
import org.mds.harness.common2.runner.dsm.DsmRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Created by modongsong on 2015/4/22.
 */
public class TestMapDBPerf extends DsmRunner<TestMapDBConfig> {
    private final static Logger log = LoggerFactory.getLogger(TestMapDBPerf.class);

    private static final Random random = new Random();

    private interface Op {
        void doIt(int index) throws Exception;
    }

    private void doOp(int opMode, Op put, Op get, int index, long max) throws Exception {
        if (opMode == 0) {
            if (index < 1000) put.doIt(index);
            else get.doIt(1000);
            return;
        }
        if (opMode == 1) {
            put.doIt((int) (index % max));
            return;
        }
        put.doIt(index);
        get.doIt(index);
    }

    public void runHashMap(final TestMapDBConfig config) {
        final StringBuilder baseValue = new StringBuilder(RandomStringUtils.random(config.valueLen));

        TestMapDBHelper.createHTreeMap(config).forEach(map -> {
            this.runSingle("Test HashMap of MapDB", config, (configuration1, index) -> {
                try {
                    doOp(config.opMode,
                            index1 -> map.put("key" + index1, baseValue.toString()),
                            index1 -> map.get("key" + random.nextInt(index1)),
                            index, config.expireMaxSize);
                } catch (Exception ex) {

                }
                return 1;
            });
        });
    }

    public void runHashSet(final TestMapDBConfig config) {
        final StringBuilder baseValue = new StringBuilder(RandomStringUtils.random(config.valueLen));
        final int len = baseValue.length();
        TestMapDBHelper.createHTreeSet(config).forEach(map -> {
            this.runSingle("Test HashSet of MapDB", config, (configuration1, index) -> {
                try {
                    doOp(config.opMode,
                            index1 -> {
                                baseValue.setLength(len);
                                map.add(baseValue.append(index1).toString());
                            },
                            index1 -> {
                                baseValue.setLength(len);
                                map.remove(baseValue.append(index1).toString());
                            },
                            index, config.expireMaxSize);
                } catch (Exception ex) {

                }
                return 1;
            });
        });
    }

    public void runTreeMap(final TestMapDBConfig config) {
        final StringBuilder baseValue = new StringBuilder(RandomStringUtils.random(config.valueLen));

        TestMapDBHelper.createBTreeMap(config).forEach(map -> {
            this.runSingle("Test TreeMap of MapDB", config, (configuration1, index) -> {
                try {
                    doOp(config.opMode,
                            index1 -> map.put("key" + index1, baseValue.toString()),
                            index1 -> map.get("key" + random.nextInt(index1)),
                            index, config.expireMaxSize);
                } catch (Exception ex) {

                }
                return 1;
            });
        });
    }

    public void runTreeSet(final TestMapDBConfig config) {
        final StringBuilder baseValue = new StringBuilder(RandomStringUtils.random(config.valueLen));
        final int len = baseValue.length();
        TestMapDBHelper.createBTreeSet(config).forEach(map -> {
            this.runSingle("Test TreeSet of MapDB", config, (configuration1, index) -> {
                try {
                    doOp(config.opMode,
                            index1 -> {
                                baseValue.setLength(len);
                                map.add(baseValue.append(index1).toString());
                            },
                            index1 -> {
                                baseValue.setLength(len);
                                map.remove(baseValue.append(index1).toString());
                            },
                            index, config.expireMaxSize);
                } catch (Exception Ex) {

                }
                return 1;
            });
        });
    }

    public void runQueue(final TestMapDBConfig config) {
        final StringBuilder baseValue = new StringBuilder(RandomStringUtils.random(config.valueLen));
        final int len = baseValue.length();
        TestMapDBHelper.createQueue(config).forEach(map -> {
            this.runSingle("Test Queue of MapDB", config, (configuration1, index) -> {
                try {
                    doOp(config.opMode,
                            index1 -> {
                                baseValue.setLength(len);
                                map.put(baseValue.append(index1).toString());
                            },
                            index1 -> map.take(),
                            index, config.expireMaxSize);
                } catch (Exception ex) {
                    log.error("", ex);
                }
                return 1;
            });
        });
    }

    public void runCQueue(final TestMapDBConfig config) {
        final StringBuilder baseValue = new StringBuilder(RandomStringUtils.random(config.valueLen));
        final int len = baseValue.length();
        TestMapDBHelper.createCQueue(config).forEach(map -> {
            this.runSingle("Test CQueue of MapDB", config, (configuration1, index) -> {
                try {
                    doOp(config.opMode,
                            index1 -> {
                                baseValue.setLength(len);
                                map.put(baseValue.append(index1).toString());
                            },
                            index1 -> map.take(),
                            index, config.expireMaxSize);
                } catch (Exception ex) {
                    log.error("", ex);
                }
                return 1;
            });
        });
    }

    public void runStack(final TestMapDBConfig config) {
        final StringBuilder baseValue = new StringBuilder(RandomStringUtils.random(config.valueLen));
        final int len = baseValue.length();
        TestMapDBHelper.createStack(config).forEach(map -> {
            this.runSingle("Test Stack of MapDB", config, (configuration1, index) -> {
                try {
                    doOp(config.opMode,
                            index1 -> {
                                baseValue.setLength(len);
                                map.put(baseValue.append(index1).toString());
                            },
                            index1 -> map.take(),
                            index, config.expireMaxSize);
                } catch (Exception ex) {
                    log.error("", ex);
                }
                return 1;
            });
        });
    }
}
