package org.mds.harness2.tools.ignite;

import org.apache.ignite.cache.CachePeekMode;
import org.mds.harness.common2.runner.JMHInternalRunner;
import org.mds.harness.common2.runner.JMHMainRunner;
import org.openjdk.jmh.annotations.Benchmark;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.IgniteCache;

public class IgniteBenchmark extends JMHMainRunner<IgniteConfig> {

    public static class TestData {
        private int id;
        private long count;
        private String name = "";
    }

    private static class IgniteBenchSetup extends JMHInternalRunner<IgniteConfig> {
        Ignite ignite;
        IgniteCache igniteCache;

        @Override
        protected void setup(IgniteConfig config) {
            this.ignite = Ignition.start(config.cacheConfig);
            this.igniteCache = this.ignite.cache(config.cacheName);
        }

        @Override
        protected void tearDown() {
            log.info("Test finished, Current Cache size: " + this.igniteCache.size(CachePeekMode.PRIMARY));
            this.ignite.close();
        }
    }

    public static class PrepareData extends IgniteBenchSetup {
        Object originValue;
        AtomicInteger index = new AtomicInteger();
        Random random = new Random();
        boolean isStringValue;

        @Override
        protected void setup(IgniteConfig config) {
            byte[] bytes = new byte[config.length];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = 3;
            }
            if (config.dataType.equalsIgnoreCase("string")) {
                this.originValue = new String(bytes).intern();
                isStringValue = true;
            } else {
                TestData testData = new TestData();
                testData.name = new String(bytes).intern();
                this.originValue = testData;
            }
            this.prepareBaseData(config.baseCount);
        }

        private void prepareBaseData(int baseCount) {
            ExecutorService executor = Executors.newFixedThreadPool(4);
            executor.execute(() -> {
                int thisIndex = this.index.get();
                while (thisIndex < baseCount) {
                    if (index.compareAndSet(thisIndex, thisIndex++)) {
                        igniteCache.put(thisIndex, this.originValue);
                    }
                }
            });
            executor.shutdownNow();
        }
    }

    @Benchmark
    public void benchInsert(PrepareData prepareData) {
        int nextIndex = prepareData.index.incrementAndGet();
        prepareData.igniteCache.put(nextIndex, prepareData.originValue);
    }

    @Benchmark
    public void benchGet(PrepareData prepareData) {
        int nextIndex = prepareData.index.decrementAndGet();
        prepareData.igniteCache.get(nextIndex);
    }

    @Benchmark
    public void benchRemove(PrepareData prepareData) {
        int nextIndex = prepareData.index.decrementAndGet();
        prepareData.igniteCache.remove(nextIndex);
    }

    @Benchmark
    public void benchUpdate(PrepareData prepareData) {
        int getIndex = prepareData.random.nextInt(prepareData.config().baseCount);
        prepareData.igniteCache.invoke(getIndex, (entry, objects) -> {
            Object value = entry.getValue();
            if (value instanceof TestData) {
                TestData data = (TestData) value;
                data.count = 1;
            }
            return null;
        });
    }

    @Benchmark
    public void benchInsertAndGet(PrepareData prepareData) {
        int nextIndex = prepareData.index.incrementAndGet();
        int getIndex = prepareData.random.nextInt(nextIndex);
        prepareData.igniteCache.put(nextIndex, prepareData.originValue);
        prepareData.igniteCache.get(getIndex);
    }
}
