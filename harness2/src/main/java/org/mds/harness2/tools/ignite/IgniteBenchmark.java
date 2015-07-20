package org.mds.harness2.tools.ignite;

import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.query.SqlQuery;
import org.mds.harness.common2.runner.JMHInternalRunner;
import org.mds.harness.common2.runner.JMHMainRunner;
import org.mds.harness.model.ignite.CommonData;
import org.mds.harness.model.ignite.IndexedData;
import org.openjdk.jmh.annotations.Benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.IgniteCache;

public class IgniteBenchmark extends JMHMainRunner<IgniteConfig> {

    private static class IgniteBenchSetup extends JMHInternalRunner<IgniteConfig> {
        Ignite ignite;
        IgniteCache igniteCache;

        @Override
        protected void setup(IgniteConfig config) {
            log.info("Starting Ignite client ....");
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
        AtomicInteger index;
        AtomicInteger maxIndex;
        Random random = new Random();
        String stringValue;

        @Override
        protected void setup(IgniteConfig config) {
            super.setup(config);
            byte[] bytes = new byte[config.length];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = 3;
            }
            this.stringValue = new String(bytes).intern();
            index = new AtomicInteger(config.baseCount);
            maxIndex=new AtomicInteger(config.maxId);
            this.prepareBaseData(config.baseCount);
        }

        private Object createValue(int index) {
            switch (config.dataType) {
                case 0:
                    return this.stringValue + index;
                case 1:
                    CommonData commonData = new CommonData();
                    commonData.setName(this.stringValue + index);
                    return commonData;
                case 2:
                    IndexedData indexedData = new IndexedData();
                    indexedData.setName(this.stringValue + index);
                    return indexedData;
            }
            return "";
        }

        private void prepareBaseData(int baseCount) {
            ExecutorService executor = Executors.newFixedThreadPool(4);
            List<Future> tasks = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                tasks.add(executor.submit(() -> {
                    int thisIndex = this.index.get();
                    while (thisIndex > 0) {
                        if (index.compareAndSet(thisIndex, --thisIndex)) {
                            igniteCache.put(String.valueOf(thisIndex), this.createValue(thisIndex));
                        }
                    }
                }));
            }
            for (Future task : tasks) {
                try {
                    task.get();
                } catch (Exception ex) {

                }
            }
            executor.shutdownNow();
        }
    }

    @Benchmark
    public void benchPut(PrepareData prepareData) {
        int nextIndex = prepareData.index.incrementAndGet();
        if (nextIndex > prepareData.config().maxId) {
            nextIndex = prepareData.random.nextInt(prepareData.config().maxId);
        }
        prepareData.igniteCache.put(String.valueOf(nextIndex), prepareData.createValue(nextIndex));
    }

    @Benchmark
    public void benchGet(PrepareData prepareData) {
        int nextIndex = prepareData.random.nextInt(prepareData.maxIndex.get());
        prepareData.igniteCache.get(String.valueOf(nextIndex));
    }

    @Benchmark
    public void benchRemove(PrepareData prepareData) {
        int nextIndex = prepareData.maxIndex.decrementAndGet();
        prepareData.igniteCache.remove(String.valueOf(nextIndex));
    }

    @Benchmark
    public void benchUpdate(PrepareData prepareData) {
        int getIndex = prepareData.random.nextInt(prepareData.config().maxId);
        prepareData.igniteCache.invoke(getIndex, (entry, objects) -> {
            Object value = entry.getValue();
            if (value instanceof CommonData) {
                CommonData data = (CommonData) value;
                data.setName(data.getName() + getIndex);
            } else if (value instanceof IndexedData) {
                IndexedData data = (IndexedData) value;
                data.setName(data.getName() + getIndex);
            }
            return null;
        });
    }

    @Benchmark
    public void benchInsertAndGet(PrepareData prepareData) {
        int nextIndex = prepareData.index.incrementAndGet();
        int getIndex = prepareData.random.nextInt(nextIndex);
        prepareData.igniteCache.put(String.valueOf(nextIndex), prepareData.createValue(nextIndex));
        prepareData.igniteCache.get(String.valueOf(getIndex));
    }

    @Benchmark
    public void benchQuery(PrepareData prepareData) {
        int queryIndex = prepareData.random.nextInt(prepareData.maxIndex.get());
        SqlQuery<String, IndexedData> query = new SqlQuery<String, IndexedData>(IndexedData.class, "id=?").setArgs(queryIndex);
        prepareData.igniteCache.query(query).getAll();
    }
}
