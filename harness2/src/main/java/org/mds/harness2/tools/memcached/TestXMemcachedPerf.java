package org.mds.harness2.tools.memcached;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.transcoders.SerializingTranscoder;
import org.apache.commons.lang3.StringUtils;
import org.mds.hprocessor.memcache.*;
import org.mds.harness.common2.perf.PerfConfig;
import org.mds.harness.common2.perf.PerfTester;
import org.mds.harness.common2.runner.RunnerHelper;
import org.mds.hprocessor.memcache.utils.MemcacheClientUtils;
import org.mds.hprocessor.memcache.utils.MemcacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class TestXMemcachedPerf {
    private final static Logger log = LoggerFactory.getLogger(TestXMemcachedPerf.class);

    private static String KEY_PREFIX = "key-";
    private static String DATA_PREFIX = "DATA-";
    private MemcachedClient memcachedClient;

    private MemcachedClient getClient(TestMemcachedConfiguration configuration) throws IOException {
        XMemcachedClientBuilder builder = new XMemcachedClientBuilder(configuration.memcachedAddress.replace(",", " "));
        builder.setSessionLocator(new KetamaMemcachedSessionLocator());
        builder.setCommandFactory(new BinaryCommandFactory());
        builder.setConnectionPoolSize(configuration.connectionPoolSize);
        SerializingTranscoder transcoder = new SerializingTranscoder();

        transcoder.setCompressionThreshold(configuration.compressionThreshold);
        builder.setTranscoder(transcoder);
        builder.setConnectTimeout(3000);
        builder.setFailureMode(false);
        builder.setOpTimeout(1000);

        return builder.build();
        //return new XMemcachedClient("192.168.205.101",11211);
    }

    public void beforeRun(final TestMemcachedConfiguration conf) throws IOException {
        memcachedClient = this.getClient(conf);
        KEY_PREFIX = KEY_PREFIX + StringUtils.repeat("1", conf.keyLen);
        DATA_PREFIX = DATA_PREFIX + StringUtils.repeat("1", conf.dataLen);
    }

    public void runSet(final TestMemcachedConfiguration conf) {
        new PerfTester<PerfTester.SingleTask>("Meetup  memcache client Perftest", conf).run(
                (config, index) -> {
                    try {
                        int vIndex = index % conf.itemCount;
                        memcachedClient.set(KEY_PREFIX + vIndex, 100000, DATA_PREFIX + vIndex);
                    } catch (Exception ex) {

                    }
                    return 1;
                });
    }

    public void runAsyncSet(final TestMemcachedConfiguration conf) {
        new PerfTester<PerfTester.SingleTask>("Meetup  memcache client Perftest", conf).run(
                (config, index) -> {
                    try {
                        int vIndex = index % conf.itemCount;
                        memcachedClient.setWithNoReply(KEY_PREFIX + vIndex, 100000, DATA_PREFIX + vIndex);
                    } catch (Exception ex) {

                    }
                    return 1;
                });
    }

    public void runDSet(final TestMemcachedConfiguration conf) {
        new PerfTester<PerfTester.SingleTask>("Meetup  memcache client Perftest", conf).run(
                (config, index) -> {
                    try {
                        int vIndex = index % conf.itemCount;
                        memcachedClient.setWithNoReply(KEY_PREFIX + vIndex, 100000, DATA_PREFIX + vIndex);
                    } catch (Exception ex) {

                    }
                    return 1;
                });
    }

    public void runGet(final TestMemcachedConfiguration conf) {
        new PerfTester<PerfTester.SingleTask>("Http Sync Perftest", conf).run((config, index) -> {
            try {
                int vIndex = index % conf.itemCount;
                memcachedClient.get(KEY_PREFIX + vIndex);
            } catch (Exception ex) {

            }
            return 1;
        });
    }

    public void runGetBulk(final TestMemcachedConfiguration conf) {
        new PerfTester<PerfTester.BatchTask>("Http Sync Perftest", conf).run(
                (config, indexes) -> {
                    List<String> keys = new ArrayList<String>();
                    for (Long index : indexes) {
                        keys.add(KEY_PREFIX + index % conf.itemCount);
                    }
                    try {
                        memcachedClient.get(keys);
                    } catch (Exception ex) {

                    }
                    return indexes.size();
                });
    }

    public void runGetter(final TestMemcachedConfiguration conf) {
        final XMemCache[] getters = XMemCache.build(
                MemcacheClientUtils.createXMemcachedClient(
                        new MemcacheConfig(conf.memcachedAddress).setConnections(conf.connectionPoolSize)),
                conf.getterCount);
        MemcacheCache memcacheCache = new MemcacheCache(new CacheConfig().setSyncThreads(4));
        final MemcacheGetProcessor getProcessor = MemcacheGetProcessor.newBuilder()
                .setBufferSize(1024 * 16)
                .setProcessorType(conf.getterType == 0 ? MemcacheProcessor.ProcessorType.DISRUPTOR : MemcacheSetProcessor.ProcessorType.QUEUE)
                .setBatchSize(conf.batchSize).setGetters(getters)
                .setCache(conf.enableCache ? memcacheCache : null)
                .build();
        final AtomicLong counter = new AtomicLong();
        new PerfTester<PerfTester.SingleTask>("Http Sync Perftest", conf).run((config, index) -> {
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
            return 0;
        }, counter);
    }

    public void runSetter(final TestMemcachedConfiguration conf) {
        final XMemCache[] setters = XMemCache.build(
                MemcacheClientUtils.createXMemcachedClient(
                        new MemcacheConfig(conf.memcachedAddress).setConnections(conf.connectionPoolSize)),
                conf.getterCount);
        final MemcacheSetProcessor setProcessor = MemcacheSetProcessor.newBuilder()
                .setBufferSize(1024 * 16)
                .setProcessorType(conf.getterType == 0 ? MemcacheProcessor.ProcessorType.DISRUPTOR : MemcacheSetProcessor.ProcessorType.QUEUE)
                .setSetters(setters).build();
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
                            log.info("set timeout");
                        }
                    });
            return 0;
        }, counter);
    }

    public static void main(String args[]) throws Exception {
        RunnerHelper.newInvoker()
                .setArgs(args)
                .setMainClass(TestXMemcachedPerf.class)
                .setConfigClass(TestMemcachedConfiguration.class)
                .setConfigFile("testMemcached.yml")
                .invoke();
    }
}
