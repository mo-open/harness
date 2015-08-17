package org.mds.harness2.tools.processor;

import org.mds.harness.common2.runner.dsm.DsmRunner;
import org.mds.hprocessor.processor.*;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Dongsong
 */
public class ProcessorPerf extends DsmRunner<ProcessorConfiguration> {

    public void runDProcessor1(final ProcessorConfiguration conf) {
        final AtomicLong counter = new AtomicLong();
        final DisruptorProcessor<Integer> disruptorProcessor =
                DisruptorProcessor.<Integer>newBuilder().setBufferSize(conf.bufferSize)
                        .addNext(conf.workerCount,
                                value -> {
                                    counter.incrementAndGet();
                                }).build();
        this.runSingle("Test DProcessor1", conf, (configuration1, index) -> {
            disruptorProcessor.submit(index);
            return 1;
        }, counter);
    }

    public void runDProcessor2(final ProcessorConfiguration conf) {
        final AtomicLong counter = new AtomicLong();
        final DisruptorProcessor<Integer> disruptorProcessor =
                DisruptorProcessor.<Integer>newBuilder().setBufferSize(conf.bufferSize)
                        .addNext(conf.workerCount,
                                value -> {
                                })
                        .addNext(conf.workerCount,
                                value -> {
                                })
                        .addNext(conf.workerCount,
                                value -> {
                                    counter.incrementAndGet();
                                }).build();
        this.runSingle("Test DProcessor2", conf, (configuration1, index) -> {
            disruptorProcessor.submit(index);
            return 1;
        }, counter);
    }

    public void runDProcessor3(final ProcessorConfiguration conf) {
        final AtomicLong counter = new AtomicLong();
        final DisruptorProcessor2<Integer> disruptorProcessor =
                DisruptorProcessor2.<Integer>newBuilder().setBufferSize(conf.bufferSize)
                        .addNext(conf.workerCount,
                                value -> {
                                })
                        .addNext(conf.workerCount,
                                value -> {
                                })
                        .addNext(conf.workerCount,
                                value -> {
                                    counter.incrementAndGet();
                                }).build();
        this.runSingle("Test Dprocessor3", conf, (configuration1, index) -> {
            disruptorProcessor.submit(index);
            return 1;
        }, counter);
    }

    public void runDBProcessor1(final ProcessorConfiguration conf) {
        final AtomicLong counter = new AtomicLong();

        final DisruptorBatchProcessor<Integer> disruptorProcessor =
                DisruptorBatchProcessor.<Integer>newBuilder()
                        .setSingleHandler(new ProcessorHandler<Integer>() {
                            @Override
                            public void process(Integer object) {
                                counter.incrementAndGet();
                            }
                        }).build();
        this.runSingle("Test DBProcessor1", conf, (configuration1, index) -> {
            disruptorProcessor.submit(index);
            return 1;
        }, counter);
    }

    public void runDBProcessor2(final ProcessorConfiguration conf) {
        final AtomicLong counter = new AtomicLong();

        final DisruptorBatchProcessor<Integer> disruptorProcessor =
                DisruptorBatchProcessor.<Integer>newBuilder()
                        .addNext(2, value -> {
                        })
                        .addNext(2, conf.batchSize, value -> {
                        })
                        .addNext(2, value -> {
                            counter.incrementAndGet();
                        }).build();
        this.runSingle("Test DBProcessor2", conf, (configuration1, index) -> {
            disruptorProcessor.submit(index);
            return 1;
        }, counter);
    }

    public void runQProcessor(final ProcessorConfiguration conf) {
        final AtomicLong counter = new AtomicLong();
        final BlockingQueueProcessor<Integer> blockingQueueProcessor = BlockingQueueProcessor.<Integer>newBuilder()
                .addNext(conf.workerCount,
                        value -> {
                            counter.incrementAndGet();
                        }).build();
        this.runSingle("Test QProcessor", conf, (configuration1, index) -> {
            blockingQueueProcessor.submit(index);
            return 1;
        }, counter);
    }

    public void runQProcessor2(final ProcessorConfiguration conf) {
        final AtomicLong counter = new AtomicLong();
        final BlockingQueueProcessor<Integer> processor = BlockingQueueProcessor.<Integer>newBuilder()
                .addNext(conf.workerCount, value -> {
                })
                .addNext(conf.workerCount,
                        conf.queueBatchSize,
                        value -> {
                        })
                .addNext(conf.workerCount, value -> {
                    counter.incrementAndGet();
                }).build();
        this.runSingle("Test QProcessor2", conf, (configuration1, index) -> {
            processor.submit(index);
            return 1;
        }, counter);
    }
}
