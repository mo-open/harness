package org.mds.harness2.tools.memcached;

import net.spy.memcached.MemcachedClient;
import org.apache.commons.lang3.StringUtils;
import org.mds.hprocessor.memcache.*;
import org.mds.harness.common2.perf.PerfConfig;
import org.mds.harness.common2.perf.PerfTester;
import org.mds.hprocessor.memcache.utils.MemcacheClientUtils;
import org.mds.hprocessor.memcache.utils.MemcacheConfig;
import org.mds.harness.common2.runner.RunnerHelper;
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
        memcachedClient = MemcacheClientUtils.createSpyMemcachedClient(
                new MemcacheConfig(conf.memcachedAddress)
                        .setCompressionThreshold(conf.compressionThreshold));
        KEY_PREFIX = KEY_PREFIX + StringUtils.repeat("1", conf.keyLen);
        DATA_PREFIX = DATA_PREFIX + StringUtils.repeat("1", conf.dataLen);
    }

    public void afterRun(final TestMemcachedConfiguration conf) {
        if (memcachedClient != null) {
            memcachedClient.shutdown();
        }
    }

    public void runSet(final TestMemcachedConfiguration conf) {
        new PerfTester<PerfTester.SingleTask>("Meetup  memcache client Perftest", conf).run(
                (config, index) -> {
                    int vIndex = index % conf.itemCount;
                    memcachedClient.set(KEY_PREFIX + vIndex, 100000, DATA_PREFIX + vIndex);
                    return 1;
                });
    }

    public void runGet(final TestMemcachedConfiguration conf) {
        new PerfTester<PerfTester.SingleTask>("Http Sync Perftest", conf).run((config, index) -> {
            int vIndex = index % conf.itemCount;
            memcachedClient.get(KEY_PREFIX + vIndex);
            return 1;
        });
    }

    public void runGetBulk(final TestMemcachedConfiguration conf) {
        new PerfTester<PerfTester.BatchTask>("Http Sync Perftest", conf).run((config, indexes) -> {
            List<String> keys = new ArrayList<String>();
            for (Long index : indexes) {
                keys.add(KEY_PREFIX + index % conf.itemCount);
            }
            memcachedClient.getBulk(keys);
            return indexes.size();
        });
    }

    public void runGetter(final TestMemcachedConfiguration conf) {
        final SpyMemCache[] getters = SpyMemCache.build(
                MemcacheClientUtils.createSpyMemcachedClients(
                        new MemcacheConfig(conf.memcachedAddress),
                        conf.getterCount));
        MemcacheCache memcacheCache = new MemcacheCache(new CacheConfig().setSyncThreads(4));
        final MemcacheGetProcessor getProcessor = MemcacheGetProcessor.newBuilder()
                .setBufferSize(1024 * 16)
                .setProcessorType(conf.getterType == 0 ? MemcacheProcessor.ProcessorType.DISRUPTOR : MemcacheGetProcessor.ProcessorType.QUEUE)
                .setBatchSize(conf.batchSize).setGetters(getters)
                .setCache(conf.enableCache ? memcacheCache : null)
                .build();
        final AtomicLong counter = new AtomicLong();
        new PerfTester<PerfTester.SingleTask>("Http Sync Perftest", conf).run(
                (config, index) -> {
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
                }, counter);
    }

    public void runSetter(final TestMemcachedConfiguration conf) {
        final SpyMemCache[] setters = SpyMemCache.build(
                MemcacheClientUtils.createSpyMemcachedClients(
                        new MemcacheConfig(conf.memcachedAddress),
                        conf.getterCount));
        final MemcacheSetProcessor setProcessor = MemcacheSetProcessor.newBuilder()
                .setAsync(conf.asyncSet)
                .setBufferSize(1024 * 16)
                .setProcessorType(conf.getterType == 0 ? MemcacheProcessor.ProcessorType.DISRUPTOR : MemcacheSetProcessor.ProcessorType.QUEUE)
                .setSetters(setters)
                .build();
        final AtomicLong counter = new AtomicLong();
        new PerfTester<PerfTester.SingleTask>("Http Sync Perftest", conf).run((config, index) -> {
            int vIndex = index % conf.itemCount;
            setProcessor.set(KEY_PREFIX + vIndex, 100000, DATA_PREFIX + vIndex,
                    new MemcacheSetProcessor.SetCallback() {
                        @Override
                        public void complete(String key, Object value) {
                            counter.incrementAndGet();
                        }

                        @Override
                        public void timeout(String key, Object value) {
                            log.info("get timeout");
                        }
                    });
            return 0;
        }, counter);
    }

    public static void main(String args[]) throws Exception {
        RunnerHelper.run(args, TestMemcachedPerf.class,
                TestMemcachedConfiguration.class,
                "testMemcached.yml");
    }
}
