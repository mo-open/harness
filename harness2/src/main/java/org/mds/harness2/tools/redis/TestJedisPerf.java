package org.mds.harness2.tools.redis;

import org.apache.commons.lang3.StringUtils;
import org.mds.harness.common2.runner.dsm.DsmRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.io.IOException;
import java.util.*;

public class TestJedisPerf extends DsmRunner<JedisPerfConfiguration> {
    private final static Logger log = LoggerFactory.getLogger(TestJedisPerf.class);

    private static String KEY_PREFIX = "key-";
    private static String DATA_PREFIX = "DATA-";
    private static String HASH_KEY = "hash-test";
    private static String LIST_KEY = "list-test";
    private static String TOPIC = "test-topic";
    JedisPool jedisPool;

    public void beforeRun(final JedisPerfConfiguration conf) throws IOException {
        String host = "localhost";
        int port = 6379;
        try {
            String[] hostPort = conf.redisAddress.split(":");
            host = hostPort[0];
            port = Integer.parseInt(hostPort[1]);
        } catch (Exception ex) {
        }
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(100);
        jedisPool = new JedisPool(jedisPoolConfig, host, port);

        KEY_PREFIX = KEY_PREFIX + StringUtils.repeat("1", conf.keyLen);
        DATA_PREFIX = DATA_PREFIX + StringUtils.repeat("1", conf.dataLen);
    }

    public void afterRun(final JedisPerfConfiguration conf) {
        jedisPool.destroy();
    }

    @FunctionalInterface
    private interface JedisAction {
        public void act(Jedis jedis, long index);
    }

    @FunctionalInterface
    private interface JedisMListAction {
        public void act(Jedis jedis, String[] keyOrValues);
    }

    @FunctionalInterface
    private interface PipeAction {
        public void act(Pipeline pipeline, long index);
    }

    @FunctionalInterface
    private interface PipeMlistAction {
        public void act(Pipeline pipeline, String[] keyOrValues);
    }

    private void doJedisTest(final String actionName, final JedisPerfConfiguration conf, final JedisAction action) {
        this.runSingle(actionName, conf, (configuration1, index) -> {
            Jedis jedis = jedisPool.getResource();
            try {
                action.act(jedis, index);
            } catch (Exception ex) {
                log.error("Failed to {}:", actionName, ex);
            } finally {
                jedisPool.returnResource(jedis);
            }
            return 1;
        });
    }

    private class DataMaker {
        private String dataPrefix;

        public DataMaker(String dataPrefix) {
            this.dataPrefix = dataPrefix;
        }

        private List<String> makeValues(List<Long> indexes) {
            List<String> values = new ArrayList<String>();
            indexes.forEach(index -> values.add(dataPrefix + index));
            return values;
        }
    }

    private void doJedisMListTest(final String actionName,
                                  final JedisPerfConfiguration conf,
                                  final DataMaker dataMaker,
                                  final JedisMListAction action) {
        this.runBatch("Jedis " + actionName, conf, (configuration1, indexes) -> {
            List<String> values = dataMaker.makeValues(indexes);
            Jedis jedis = jedisPool.getResource();
            try {
                action.act(jedis, values.toArray(new String[]{}));
            } catch (Exception ex) {
                log.error("Failed to rpush:", ex);
            } finally {
                jedisPool.returnResource(jedis);
            }
            return 1;
        });
    }

    private void doPipeTest(final String actionName, final JedisPerfConfiguration conf, final PipeAction action) {
        this.runBatch("Jedis " + actionName, conf, (configuration1, indexes) -> {
                    Jedis jedis = jedisPool.getResource();
                    try {
                        Pipeline pipeline = jedis.pipelined();
                        int count = 0;

                        for (Long index : indexes) {
                            action.act(pipeline, index);
                            if (++count % conf.pipeNumber == 0) {
                                pipeline.syncAndReturnAll();
                                count = 0;
                            }
                        }
                        if (count > 0)
                            pipeline.syncAndReturnAll();
                    } catch (Exception ex) {
                        log.error("Failed to {}:", actionName, ex);
                    } finally {
                        jedisPool.returnResource(jedis);
                    }
                    return indexes.size();
                }
        );
    }

    public void runSet(final JedisPerfConfiguration conf) {
        doJedisTest("set", conf, (Jedis jedis, long index) -> {
            jedis.setex(KEY_PREFIX + index, 180, DATA_PREFIX + index);
        });
    }

    public void runPipeSet(final JedisPerfConfiguration conf) {
        doPipeTest("pipeSet", conf, (Pipeline pipeline, long index) -> {
            pipeline.setex(KEY_PREFIX + index, 180, DATA_PREFIX + index);
        });
    }

    public void runHset(final JedisPerfConfiguration conf) {
        doJedisTest("hset", conf, (Jedis jedis, long index) -> {
            jedis.hset(HASH_KEY, KEY_PREFIX + index, DATA_PREFIX + index);
        });
    }

    public void runPipeHset(final JedisPerfConfiguration conf) {
        doPipeTest("pipeHSet", conf, (Pipeline pipeline, long index) -> {
            pipeline.hset(HASH_KEY, KEY_PREFIX + index, DATA_PREFIX + index);
        });
    }

    public void runHmset(final JedisPerfConfiguration conf) {
        this.runBatch("Jedis HMSet", conf, (configuration1, indexes) -> {
                    Map<String, String> keysValues = new HashMap();
                    for (Long index : indexes) {
                        keysValues.put(KEY_PREFIX + index, DATA_PREFIX + index);
                    }
                    Jedis jedis = jedisPool.getResource();
                    try {
                        jedis.hmset(HASH_KEY, keysValues);
                    } catch (Exception ex) {
                        log.error("Failed to  hmset:", ex);
                    } finally {
                        jedisPool.returnResource(jedis);
                    }
                    return indexes.size();
                }
        );
    }

    public void runPipeHmset(final JedisPerfConfiguration conf) {
        this.runBatch("Jedis pipe HMSet", conf, (configuration1, indexes) -> {
                    Jedis jedis = jedisPool.getResource();

                    try {
                        Pipeline pipeline = jedis.pipelined();
                        Map<String, String> keysValues = new HashMap();
                        int count0 = 0;
                        int count = 0;
                        for (Long index : indexes) {
                            keysValues.put(KEY_PREFIX + index, DATA_PREFIX);
                            if (++count0 % conf.pipeMultiSize == 0) {
                                pipeline.hmset(HASH_KEY, keysValues);
                                keysValues.clear();
                                count0 = 0;
                                if (++count % conf.pipeNumber == 0) {
                                    pipeline.syncAndReturnAll();
                                    count = 0;
                                }
                            }
                        }
                        if (!keysValues.isEmpty()) {
                            pipeline.hmset(HASH_KEY, keysValues);
                            pipeline.syncAndReturnAll();
                        }
                    } catch (Exception ex) {
                        log.error("Failed to pipe hmset:", ex);
                    } finally {
                        jedisPool.returnResource(jedis);
                    }
                    return indexes.size();
                }
        );
    }

    public void runLset(final JedisPerfConfiguration conf) {
        doJedisTest("lset", conf, (Jedis jedis, long index) -> {
            jedis.lpush(LIST_KEY, DATA_PREFIX + index);
        });
    }

    public void runPipeLset(final JedisPerfConfiguration conf) {
        doPipeTest("pipeLSet", conf, (Pipeline pipeline, long index) -> {
            pipeline.lpush(LIST_KEY, DATA_PREFIX + index);
        });
    }

    public void runLmset(final JedisPerfConfiguration conf) {
        doJedisMListTest("lmset", conf, new DataMaker(DATA_PREFIX),
                (Jedis jedis, String[] keyOrValues) -> {
                    jedis.rpush(LIST_KEY, keyOrValues);
                });
    }

    public void runPipeLmset(final JedisPerfConfiguration conf) {
        this.runBatch("Jedis pipe lmset", conf, (configuration1, indexes) -> {
                    Jedis jedis = jedisPool.getResource();
                    try {
                        Pipeline pipeline = jedis.pipelined();
                        List<String> values = new ArrayList<String>();
                        int count0 = 0;
                        int count = 0;
                        for (Long index : indexes) {
                            values.add(DATA_PREFIX + index);
                            if (++count0 % conf.pipeMultiSize == 0) {
                                pipeline.rpush(LIST_KEY, values.toArray(new String[]{}));
                                values.clear();
                                count0 = 0;
                                if (++count % conf.pipeNumber == 0) {
                                    pipeline.syncAndReturnAll();
                                    count = 0;
                                }
                            }
                        }
                        if (!values.isEmpty()) {
                            pipeline.rpush(LIST_KEY, values.toArray(new String[]{}));
                            pipeline.syncAndReturnAll();
                        }
                    } catch (Exception ex) {
                        log.error("Failed to pipe hmset:", ex);
                    } finally {
                        jedisPool.returnResource(jedis);
                    }
                    return indexes.size();
                }
        );
    }

    public void runMset(final JedisPerfConfiguration conf) {
        this.runBatch("Jedis mset", conf, (configuration1, indexes) -> {
                    List<String> keysValues = new ArrayList<String>();
                    for (Long index : indexes) {
                        keysValues.add(KEY_PREFIX + index);
                        keysValues.add(DATA_PREFIX + index);
                    }
                    Jedis jedis = jedisPool.getResource();
                    try {
                        jedis.mset(keysValues.toArray(new String[]{}));
                    } catch (Exception ex) {
                        log.error("Failed to mset:", ex);
                    } finally {
                        jedisPool.returnResource(jedis);
                    }
                    return indexes.size();
                }
        );
    }

    public void runPipeMset(final JedisPerfConfiguration conf) {
        this.runBatch("Jedis pipe mset", conf, (configuration1, indexes) -> {

                    Jedis jedis = jedisPool.getResource();

                    try {
                        Pipeline pipeline = jedis.pipelined();
                        List<String> values = new ArrayList<String>();
                        int count0 = 0;
                        int count = 0;
                        for (Long index : indexes) {
                            values.add(DATA_PREFIX + index);
                            if (++count0 % conf.pipeMultiSize == 0) {
                                pipeline.mset(values.toArray(new String[]{}));
                                values.clear();
                                if (++count % conf.pipeNumber == 0) {
                                    pipeline.syncAndReturnAll();
                                    count = 0;
                                }
                            }
                        }
                        if (!values.isEmpty()) {
                            pipeline.mset(values.toArray(new String[]{}));
                            pipeline.syncAndReturnAll();
                        }
                    } catch (Exception ex) {
                        log.error("Failed to pipe hmset:", ex);
                    } finally {
                        jedisPool.returnResource(jedis);
                    }
                    return indexes.size();
                }
        );
    }

    public void runGet(final JedisPerfConfiguration conf) {
        doJedisTest("get", conf, (Jedis jedis, long index) -> {
            jedis.get(KEY_PREFIX + index);
        });
    }

    public void runPipeGet(final JedisPerfConfiguration conf) {
        doPipeTest("pipeGet", conf, (Pipeline pipeline, long index) -> {
            pipeline.get(KEY_PREFIX + index);
        });
    }

    public void runMget(final JedisPerfConfiguration conf) {
        doJedisMListTest("mget", conf, new DataMaker(KEY_PREFIX),
                (Jedis jedis, String[] keyOrValues) -> {
                    jedis.mget(keyOrValues);
                });
    }

    public void runPipeMget(final JedisPerfConfiguration conf) {
        this.runBatch("Jedis pipe mget", conf, (configuration1, indexes) -> {
            Jedis jedis = jedisPool.getResource();

            try {
                Pipeline pipeline = jedis.pipelined();
                List<String> keys = new ArrayList<String>();
                int count0 = 0;
                int count = 0;
                for (Long index : indexes) {
                    keys.add(DATA_PREFIX + index);
                    if (+count0 % conf.pipeMultiSize == 0) {
                        pipeline.mget(keys.toArray(new String[]{}));
                        keys.clear();
                        count0 = 0;
                        if (++count % conf.pipeNumber == 0) {
                            pipeline.syncAndReturnAll();
                            count = 0;
                        }
                    }
                }
                if (!keys.isEmpty()) {
                    pipeline.mget(keys.toArray(new String[]{}));
                    pipeline.syncAndReturnAll();
                }
            } catch (Exception ex) {
                log.error("Failed to pipe mget:", ex);
            } finally {
                jedisPool.returnResource(jedis);
            }
            return indexes.size();
        });
    }

    public void runHget(final JedisPerfConfiguration conf) {
        doJedisTest("hget", conf, (Jedis jedis, long index) -> {
            jedis.hget(HASH_KEY, KEY_PREFIX + index);
        });
    }

    public void runPipeHget(final JedisPerfConfiguration conf) {
        doPipeTest("pipeHGet", conf, (Pipeline pipeline, long index) -> {
            pipeline.hget(HASH_KEY, KEY_PREFIX + index);
        });
    }

    public void runHmget(final JedisPerfConfiguration conf) {
        doJedisMListTest("hmget", conf, new DataMaker(KEY_PREFIX), (Jedis jedis, String[] keyOrValues) -> {
            jedis.hmget(HASH_KEY, keyOrValues);
        });
    }

    public void runPipeHmget(final JedisPerfConfiguration conf) {
        this.runBatch("Jedis pipe hmget", conf, (configuration1, indexes) -> {
            Jedis jedis = jedisPool.getResource();

            try {
                Pipeline pipeline = jedis.pipelined();
                List<String> keys = new ArrayList<String>();
                int count0 = 0;
                int count = 0;
                for (Long index : indexes) {
                    keys.add(DATA_PREFIX + index);
                    if (++count0 % conf.pipeMultiSize == 0) {
                        pipeline.hmget(HASH_KEY, keys.toArray(new String[]{}));
                        keys.clear();
                        count0 = 0;
                        if (++count % conf.pipeNumber == 0) {
                            pipeline.syncAndReturnAll();
                            count = 0;
                        }
                    }
                }
                if (!keys.isEmpty()) {
                    pipeline.hmget(HASH_KEY, keys.toArray(new String[]{}));
                    pipeline.syncAndReturnAll();
                }
            } catch (Exception ex) {
                log.error("Failed to pipe hmget:", ex);
            } finally {
                jedisPool.returnResource(jedis);
            }
            return indexes.size();
        });
    }

    public void runPipeLmget(final JedisPerfConfiguration conf) {
        this.runBatch("Jedis pipe lmget", conf, (configuration1, indexes) -> {
            Jedis jedis = jedisPool.getResource();

            try {
                Pipeline pipeline = jedis.pipelined();
                int total = indexes.size();
                int start = 0, end = 0;
                int count = 0;
                for (int i = 0; i < total; i++) {
                    if ((++end - start) == conf.pipeMultiSize) {
                        pipeline.lrange(LIST_KEY, indexes.get(start), indexes.get(end));
                        start = end;
                    }
                    if (++count % conf.pipeNumber == 0) {
                        pipeline.syncAndReturnAll();
                        count = 0;
                    }
                }

                if (end > start) {
                    pipeline.lrange(LIST_KEY, indexes.get(start), indexes.get(end));
                    pipeline.syncAndReturnAll();
                }
            } catch (Exception ex) {
                log.error("Failed to pipe lmget:", ex);
            } finally {
                jedisPool.returnResource(jedis);
            }
            return indexes.size();
        });
    }

    public void runLmget(final JedisPerfConfiguration conf) {
        this.runBatch("Jedis lmget", conf, (configuration1, indexes) -> {
            Jedis jedis = jedisPool.getResource();
            try {
                jedis.lrange(LIST_KEY, indexes.get(0), indexes.get(indexes.size() - 1));
            } catch (Exception ex) {
                log.error("Failed to lmget:", ex);
            } finally {
                jedisPool.returnResource(jedis);
            }
            return indexes.size();
        });
    }

    public void runDel(final JedisPerfConfiguration conf) {
        doJedisTest("del", conf, (Jedis jedis, long index) -> {
            jedis.del(KEY_PREFIX + index);
        });
    }

    public void runPipeDel(final JedisPerfConfiguration conf) {
        doPipeTest("pipeDel", conf, (Pipeline pipeline, long index) -> {
            pipeline.del(HASH_KEY, KEY_PREFIX + index);
        });
    }

    public void runIncr(final JedisPerfConfiguration conf) {
        this.runSingle("Jedis increase", conf, (configuration1, index) -> {
            Jedis jedis = jedisPool.getResource();
            try {
                jedis.incr("test-inc");
            } catch (Exception ex) {
                log.error("Failed to increase:", ex);
            } finally {
                jedisPool.returnResource(jedis);
            }
            return 1;
        });
    }

    public void runPublish(final JedisPerfConfiguration conf) {
        doJedisTest("publish", conf, (Jedis jedis, long index) -> {
            jedis.publish(TOPIC, DATA_PREFIX + index);
        });
    }

    public void runPipePublish(final JedisPerfConfiguration conf) {
        doPipeTest("pipePublish", conf, (Pipeline pipeline, long index) -> {
            pipeline.publish(TOPIC, DATA_PREFIX + index);
        });
    }
}
