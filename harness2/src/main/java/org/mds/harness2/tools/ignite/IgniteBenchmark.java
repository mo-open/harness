package org.mds.harness2.tools.ignite;

import org.mds.harness.common2.runner.JMHInternalRunner;
import org.mds.harness.common2.runner.JMHMainRunner;
import org.openjdk.jmh.annotations.Benchmark;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class IgniteBenchmark extends JMHMainRunner<IgniteConfig> {

    public static class TestData {
        private int id;
        private long count;
        private String name = "";
    }

    public static class PrepareData extends JMHInternalRunner<IgniteConfig> {
        Object originValue;
        AtomicInteger index = new AtomicInteger();
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

                    }
                }
            });
            executor.shutdownNow();
        }
    }

    @Benchmark
    public void benchInsert(PrepareData prepareData) {
        int nextIndex = prepareData.index.incrementAndGet();

    }

    @Benchmark
    public void benchGet(PrepareData prepareData) {
        int nextIndex = prepareData.index.incrementAndGet();
    }

    @Benchmark
    public void benchRemove(PrepareData prepareData) {
        int nextIndex = prepareData.index.incrementAndGet();
    }

    @Benchmark
    public void benchUpdate(PrepareData prepareData) {

    }

    @Benchmark
    public void benchInsertAndGet(PrepareData prepareData) {
        int nextIndex = prepareData.index.incrementAndGet();

    }
}
