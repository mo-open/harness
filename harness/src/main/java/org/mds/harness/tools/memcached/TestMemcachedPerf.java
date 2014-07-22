package org.mds.harness.tools.memcached;

import net.rubyeye.xmemcached.utils.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.lang3.StringUtils;
import org.mds.hprocessor.memcache.*;
import org.mds.harness.common.perf.PerfConfig;
import org.mds.harness.common.perf.PerfTester;
import org.mds.hprocessor.processor.DisruptorBatchProcessor;
import org.mds.harness.common.runner.RunnerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


public class TestMemcachedPerf {
    private final static Logger log = LoggerFactory.getLogger(TestMemcachedPerf.class);

    private static String KEY_PREFIX = "key-";
    private static String DATA_PREFIX = "DATA-";
    MemcachedClient memcachedClient;

    public void beforeRun(final TestMemcachedConfiguration conf) throws IOException {
        memcachedClient = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses(conf.memcachedAddress));
        KEY_PREFIX = KEY_PREFIX + StringUtils.repeat("1", conf.keyLen);
        DATA_PREFIX = DATA_PREFIX + StringUtils.repeat("1", conf.dataLen);
    }

    public void afterRun(final TestMemcachedConfiguration conf) {
        if (memcachedClient != null) {
            memcachedClient.shutdown();
        }
    }

    public void runSet(final TestMemcachedConfiguration conf) {
        new PerfTester("Meetup  memcache client Perftest", conf, new PerfTester.Task() {

            @Override
            public int run(PerfConfig configuration, int index) {
                int vIndex = index % conf.itemCount;
                memcachedClient.set(KEY_PREFIX + vIndex, 100000, DATA_PREFIX + vIndex);
                return 1;
            }
        }).run();
    }

    public void runGet(final TestMemcachedConfiguration conf) {
        new PerfTester("Http Sync Perftest", conf, new PerfTester.Task() {

            @Override
            public int run(PerfConfig configuration, int index) {
                int vIndex = index % conf.itemCount;
                memcachedClient.get(KEY_PREFIX + vIndex);
                return 1;
            }
        }).run();
    }

    public void runGetBulk(final TestMemcachedConfiguration conf) {
        new PerfTester("Http Sync Perftest", conf, new PerfTester.BatchTask() {
            @Override
            public int run(PerfConfig configuration, List<Long> indexes) {
                List<String> keys = new ArrayList<String>();
                for (Long index : indexes) {
                    keys.add(KEY_PREFIX + index % conf.itemCount);
                }
                memcachedClient.getBulk(keys);
                return indexes.size();
            }
        }).run();
    }

    public void runGetter(final TestMemcachedConfiguration conf) {
        final SpyMemcacheGetter[] getters = SpyMemcacheGetter.buildGetters(new MemcacheConfig(conf.memcachedAddress), conf.getterCount);
        MemcacheCache memcacheCache = new MemcacheCache(new CacheConfig().setSyncThreads(4));
        final MemcacheGetProcessor getProcessor = MemcacheGetProcessor.newBuilder()
                .setBufferSize(1024 * 16)
                .setProcessorType(conf.getterType == 0 ? MemcacheProcessor.ProcessorType.DISRUPTOR : MemcacheGetProcessor.ProcessorType.QUEUE)
                .setBatchSize(conf.batchSize).setGetters(getters)
                .setCache(conf.enableCache ? memcacheCache : null)
                .build();
        final AtomicLong counter = new AtomicLong();
        new PerfTester("Http Sync Perftest", conf, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                int vIndex = index % conf.itemCount;
                getProcessor.get(KEY_PREFIX + vIndex, new MemcacheGetProcessor.GetCallback() {
                    @Override
                    public void handle(String key, Object value) {
                        counter.incrementAndGet();
                    }

                    @Override
                    public void timeout(String key) {
                        log.info("get timeout");
                    }

                    @Override
                    public void handleNull(String key) {

                    }
                });
                return 1;
            }
        }).run(counter);
    }

    public void runSetter(final TestMemcachedConfiguration conf) {
        final SpyMemcacheSetter[] setters = SpyMemcacheSetter.buildSetters(new MemcacheConfig(conf.memcachedAddress),
                conf.setterCount);
        final MemcacheSetProcessor setProcessor = MemcacheSetProcessor.newBuilder()
                .setAsync(conf.asyncSet)
                .setBufferSize(1024 * 16)
                .setProcessorType(conf.getterType == 0 ? MemcacheProcessor.ProcessorType.DISRUPTOR : MemcacheSetProcessor.ProcessorType.QUEUE)
                .setSetters(setters)
                .build();
        final AtomicLong counter = new AtomicLong();
        new PerfTester("Http Sync Perftest", conf, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                int vIndex = index % conf.itemCount;
                setProcessor.set(KEY_PREFIX + vIndex, 100000, DATA_PREFIX + vIndex,
                        new MemcacheSetProcessor.SetCallback() {
                            @Override
                            public void complete(String key) {
                                counter.incrementAndGet();
                            }

                            @Override
                            public void fail(String key) {

                            }

                            @Override
                            public void timeout(String key) {
                                log.info("get timeout");
                            }
                        });
                return 0;
            }
        }).run(counter);
    }

    public static void main(String args[]) throws Exception {
        RunnerHelper.run(args, TestMemcachedPerf.class,
                TestMemcachedConfiguration.class,
                "testMemcached.conf");
    }
}
