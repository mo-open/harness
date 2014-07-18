package org.mds.harness.tools.redis;

import org.apache.commons.lang3.StringUtils;
import org.mds.harness.common.perf.PerfConfig;
import org.mds.harness.common.perf.PerfTester;
import org.mds.harness.common.runner.RunnerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;


public class TestJedisPerf {
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

    private interface JedisAction {
        public void act(Jedis jedis, long index);
    }

    private interface JedisMListAction {
        public void act(Jedis jedis, String[] keyOrValues);
    }

    private interface PipeAction {
        public void act(Pipeline pipeline, long index);
    }

    private interface PipeMlistAction {
        public void act(Pipeline pipeline, String[] keyOrValues);
    }

    private void doJedisTest(final String actionName, final JedisPerfConfiguration conf, final JedisAction action) {
        new PerfTester("Jedis " + actionName, conf, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                Jedis jedis = jedisPool.getResource();
                try {
                    action.act(jedis, index);
                } catch (Exception ex) {
                    log.error("Failed to {}:", actionName, ex);
                } finally {
                    jedisPool.returnResource(jedis);
                }
                return 1;
            }
        }).run();
    }

    private class DataMaker {
        private String dataPrefix;

        public DataMaker(String dataPrefix) {
            this.dataPrefix = dataPrefix;
        }

        private List<String> makeValues(List<Long> indexes) {
            List<String> values = new ArrayList<String>();
            for (Long index : indexes) {
                values.add(dataPrefix + index);
            }
            return values;
        }
    }

    private void doJedisMListTest(final String actionName,
                                  final JedisPerfConfiguration conf,
                                  final DataMaker dataMaker,
                                  final JedisMListAction action) {
        new PerfTester("Jedis " + actionName, conf, new PerfTester.BatchTask() {
            @Override
            public int run(PerfConfig configuration, List<Long> indexes) {
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
            }
        }).run();
    }

    private void doPipeTest(final String actionName, final JedisPerfConfiguration conf, final PipeAction action) {
        new PerfTester("Jedis " + actionName, conf, new PerfTester.BatchTask() {
            @Override
            public int run(PerfConfig configuration, List<Long> indexes) {
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
        }).run();
    }

    public void runSet(final JedisPerfConfiguration conf) {
        doJedisTest("set", conf, new JedisAction() {
            @Override
            public void act(Jedis jedis, long index) {
                jedis.setex(KEY_PREFIX + index, 180, DATA_PREFIX + index);
            }
        });
    }

    public void runPipeSet(final JedisPerfConfiguration conf) {
        doPipeTest("pipeSet", conf, new PipeAction() {
            @Override
            public void act(Pipeline pipeline, long index) {
                pipeline.setex(KEY_PREFIX + index, 180, DATA_PREFIX + index);
            }
        });
    }

    public void runHset(final JedisPerfConfiguration conf) {
        doJedisTest("hset", conf, new JedisAction() {
            @Override
            public void act(Jedis jedis, long index) {
                jedis.hset(HASH_KEY, KEY_PREFIX + index, DATA_PREFIX + index);
            }
        });
    }

    public void runPipeHset(final JedisPerfConfiguration conf) {
        doPipeTest("pipeHSet", conf, new PipeAction() {
            @Override
            public void act(Pipeline pipeline, long index) {
                pipeline.hset(HASH_KEY, KEY_PREFIX + index, DATA_PREFIX + index);
            }
        });
    }

    public void runHmset(final JedisPerfConfiguration conf) {
        new PerfTester("Jedis hmset", conf, new PerfTester.BatchTask() {
            @Override
            public int run(PerfConfig configuration, List<Long> indexes) {
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
        }).run();
    }

    public void runPipeHmset(final JedisPerfConfiguration conf) {
        new PerfTester("Jedis pipe hmset", conf, new PerfTester.BatchTask() {
            @Override
            public int run(PerfConfig configuration, List<Long> indexes) {
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
        }).run();
    }

    public void runLset(final JedisPerfConfiguration conf) {
        doJedisTest("lset", conf, new JedisAction() {
            @Override
            public void act(Jedis jedis, long index) {
                jedis.lpush(LIST_KEY, DATA_PREFIX + index);
            }
        });
    }

    public void runPipeLset(final JedisPerfConfiguration conf) {
        doPipeTest("pipeLSet", conf, new PipeAction() {
            @Override
            public void act(Pipeline pipeline, long index) {
                pipeline.lpush(LIST_KEY, DATA_PREFIX + index);
            }
        });
    }

    public void runLmset(final JedisPerfConfiguration conf) {
        doJedisMListTest("lmset", conf, new DataMaker(DATA_PREFIX), new JedisMListAction() {
            @Override
            public void act(Jedis jedis, String[] keyOrValues) {
                jedis.rpush(LIST_KEY, keyOrValues);
            }
        });
    }

    public void runPipeLmset(final JedisPerfConfiguration conf) {
        new PerfTester("Jedis pipe hmset", conf, new PerfTester.BatchTask() {
            @Override
            public int run(PerfConfig configuration, List<Long> indexes) {
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
        }).run();
    }

    public void runMset(final JedisPerfConfiguration conf) {
        new PerfTester("Jedis mset", conf, new PerfTester.BatchTask() {
            @Override
            public int run(PerfConfig configuration, List<Long> indexes) {
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
        }).run();
    }

    public void runPipeMset(final JedisPerfConfiguration conf) {
        new PerfTester("Jedis pipe mset", conf, new PerfTester.BatchTask() {
            @Override
            public int run(PerfConfig configuration, List<Long> indexes) {
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
        }).run();
    }

    public void runGet(final JedisPerfConfiguration conf) {
        doJedisTest("get", conf, new JedisAction() {
            @Override
            public void act(Jedis jedis, long index) {
                jedis.get(KEY_PREFIX + index);
            }
        });
    }

    public void runPipeGet(final JedisPerfConfiguration conf) {
        doPipeTest("pipeGet", conf, new PipeAction() {
            @Override
            public void act(Pipeline pipeline, long index) {
                pipeline.get(KEY_PREFIX + index);
            }
        });
    }

    public void runMget(final JedisPerfConfiguration conf) {
        doJedisMListTest("mget",conf,new DataMaker(KEY_PREFIX),new JedisMListAction() {
            @Override
            public void act(Jedis jedis, String[] keyOrValues) {
                jedis.mget(keyOrValues);
            }
        });
    }

    public void runPipeMget(final JedisPerfConfiguration conf) {
        new PerfTester("Jedis pipe mget", conf, new PerfTester.BatchTask() {
            @Override
            public int run(PerfConfig configuration, List<Long> indexes) {
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
            }
        }).run();
    }

    public void runHget(final JedisPerfConfiguration conf) {
        doJedisTest("hget",conf,new JedisAction() {
            @Override
            public void act(Jedis jedis, long index) {
                jedis.hget(HASH_KEY, KEY_PREFIX + index);
            }
        });
    }

    public void runPipeHget(final JedisPerfConfiguration conf) {
        doPipeTest("pipeHGet",conf,new PipeAction() {
            @Override
            public void act(Pipeline pipeline, long index) {
                pipeline.hget(HASH_KEY, KEY_PREFIX + index);
            }
        });
    }

    public void runHmget(final JedisPerfConfiguration conf) {
        doJedisMListTest("hmget",conf,new DataMaker(KEY_PREFIX),new JedisMListAction() {
            @Override
            public void act(Jedis jedis, String[] keyOrValues) {
                jedis.hmget(HASH_KEY, keyOrValues);
            }
        });
    }

    public void runPipeHmget(final JedisPerfConfiguration conf) {
        new PerfTester("Jedis pipe hmget", conf, new PerfTester.BatchTask() {
            @Override
            public int run(PerfConfig configuration, List<Long> indexes) {
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
            }
        }).run();
    }

    public void runPipeLmget(final JedisPerfConfiguration conf) {
        new PerfTester("Jedis pipe lmget", conf, new PerfTester.BatchTask() {
            @Override
            public int run(PerfConfig configuration, List<Long> indexes) {
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
            }
        }).run();
    }

    public void runLmget(final JedisPerfConfiguration conf) {
        new PerfTester("Jedis lmget", conf, new PerfTester.BatchTask() {

            @Override
            public int run(PerfConfig configuration, List<Long> indexes) {
                Jedis jedis = jedisPool.getResource();
                try {
                    jedis.lrange(LIST_KEY, indexes.get(0), indexes.get(indexes.size() - 1));
                } catch (Exception ex) {
                    log.error("Failed to lmget:", ex);
                } finally {
                    jedisPool.returnResource(jedis);
                }
                return indexes.size();
            }
        }).run();
    }

    public void runDel(final JedisPerfConfiguration conf) {
        doJedisTest("del",conf,new JedisAction() {
            @Override
            public void act(Jedis jedis, long index) {
                jedis.del(KEY_PREFIX + index);
            }
        });
    }

    public void runPipeDel(final JedisPerfConfiguration conf) {
        doPipeTest("pipeDel",conf,new PipeAction() {
            @Override
            public void act(Pipeline pipeline, long index) {
                pipeline.del(HASH_KEY, KEY_PREFIX + index);
            }
        });
    }

    public void runIncr(final JedisPerfConfiguration conf) {
        new PerfTester("Jedis increase", conf, new PerfTester.Task() {

            @Override
            public int run(PerfConfig configuration, int index) {
                Jedis jedis = jedisPool.getResource();
                try {
                    jedis.incr("test-inc");
                } catch (Exception ex) {
                    log.error("Failed to increase:", ex);
                } finally {
                    jedisPool.returnResource(jedis);
                }
                return 1;
            }
        }).run();
    }

    public void runPublish(final JedisPerfConfiguration conf) {
        doJedisTest("publish",conf,new JedisAction() {
            @Override
            public void act(Jedis jedis, long index) {
                jedis.publish(TOPIC, DATA_PREFIX + index);
            }
        });
    }

    public void runPipePublish(final JedisPerfConfiguration conf) {
        doPipeTest("pipePublish",conf,new PipeAction() {
            @Override
            public void act(Pipeline pipeline, long index) {
                pipeline.publish(TOPIC, DATA_PREFIX + index);
            }
        });
    }

    public void runSubscribe(final JedisPerfConfiguration conf) {
        final AtomicLong accepted = new AtomicLong();
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String s, String s2) {
                    accepted.incrementAndGet();
                }

                @Override
                public void onPMessage(String s, String s2, String s3) {

                }

                @Override
                public void onSubscribe(String s, int i) {

                }

                @Override
                public void onUnsubscribe(String s, int i) {

                }

                @Override
                public void onPUnsubscribe(String s, int i) {

                }

                @Override
                public void onPSubscribe(String s, int i) {

                }
            }, TOPIC);
        } catch (Exception ex) {
            log.error("Failed to subscribe:", ex);
        } finally {
            jedisPool.returnResource(jedis);
        }
        PerfTester.waitRun("Redis subscribe", conf, accepted);
    }

    public static void main(String args[]) throws Exception {
        RunnerHelper.run(args, TestJedisPerf.class,
                JedisPerfConfiguration.class,
                "redis-perf.conf");
    }
}
