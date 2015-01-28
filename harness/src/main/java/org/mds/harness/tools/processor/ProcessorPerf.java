package org.mds.harness.tools.processor;

import org.mds.harness.common.perf.PerfConfig;
import org.mds.harness.common.perf.PerfTester;
import org.mds.hprocessor.processor.*;
import org.mds.harness.common.runner.RunnerHelper;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Dongsong
 */
public class ProcessorPerf {

    public void runDProcessor1(final ProcessorConfiguration conf) {
        final AtomicLong counter = new AtomicLong();
        final DisruptorProcessor<Integer> disruptorProcessor =
                DisruptorProcessor.<Integer>newBuilder().setBufferSize(conf.bufferSize)
                        .addNext(conf.workerCount,
                                new ProcessorHandler<Integer>() {
                                    @Override
                                    public void process(Integer object) {
                                        counter.incrementAndGet();
                                    }
                                }).build();
        new PerfTester("Disruptor processor", conf, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                disruptorProcessor.submit(index);
                return 1;
            }
        }).run(counter);
    }

    public void runDProcessor2(final ProcessorConfiguration conf) {
        final AtomicLong counter = new AtomicLong();
        final DisruptorProcessor<Integer> disruptorProcessor =
                DisruptorProcessor.<Integer>newBuilder().setBufferSize(conf.bufferSize)
                        .addNext(conf.workerCount,
                                new ProcessorHandler() {
                                    @Override
                                    public void process(Object object) {

                                    }
                                })
                        .addNext(conf.workerCount,
                                new ProcessorHandler() {
                                    @Override
                                    public void process(Object object) {

                                    }
                                })
                        .addNext(conf.workerCount,
                                new ProcessorHandler() {
                                    @Override
                                    public void process(Object object) {
                                        counter.incrementAndGet();
                                    }
                                }).build();
        new PerfTester("Disruptor processor", conf, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                disruptorProcessor.submit(index);
                return 1;
            }
        }).run(counter);
    }

    public void runDProcessor3(final ProcessorConfiguration conf) {
        final AtomicLong counter = new AtomicLong();
        final DisruptorProcessor2<Integer> disruptorProcessor =
                DisruptorProcessor2.<Integer>newBuilder().setBufferSize(conf.bufferSize)
                        .addNext(conf.workerCount,
                                new ProcessorHandler<Integer>() {
                                    @Override
                                    public void process(Integer object) {

                                    }
                                })
                        .addNext(conf.workerCount,
                                new ProcessorHandler<Integer>() {
                                    @Override
                                    public void process(Integer object) {

                                    }
                                })
                        .addNext(conf.workerCount,
                                new ProcessorHandler<Integer>() {
                                    @Override
                                    public void process(Integer object) {
                                        counter.incrementAndGet();
                                    }
                                }).build();
        new PerfTester("Disruptor processor", conf, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                disruptorProcessor.submit(index);
                return 1;
            }
        }).run(counter);
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
        new PerfTester("Disruptor processor", conf, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                disruptorProcessor.submit(index);
                return 1;
            }
        }).run(counter);
    }

    public void runDBProcessor2(final ProcessorConfiguration conf) {
        final AtomicLong counter = new AtomicLong();

        final DisruptorBatchProcessor<Integer> disruptorProcessor =
                DisruptorBatchProcessor.<Integer>newBuilder()
                        .addNext(2, new ProcessorHandler<Integer>() {
                            @Override
                            public void process(Integer object) {

                            }
                        }).addNext(2, conf.batchSize, new ProcessorBatchHandler<Integer>() {
                    @Override
                    public void process(List<Integer> objects) {

                    }
                }).addNext(2, new ProcessorHandler<Integer>() {
                    @Override
                    public void process(Integer object) {
                        counter.incrementAndGet();
                    }
                }).build();
        new PerfTester("Disruptor processor", conf, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                disruptorProcessor.submit(index);
                return 1;
            }
        }).run(counter);
    }

    public void runQProcessor(final ProcessorConfiguration conf) {
        final AtomicLong counter = new AtomicLong();
        final BlockingQueueProcessor<Integer> blockingQueueProcessor = BlockingQueueProcessor.<Integer>newBuilder()
                .addNext(conf.workerCount,
                        new ProcessorHandler<Integer>() {
                            @Override
                            public void process(Integer object) {
                                counter.incrementAndGet();
                            }
                        }).build();
        new PerfTester("Disruptor processor", conf, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                blockingQueueProcessor.submit(index);
                return 1;
            }
        }).run(counter);
    }

    public void runQProcessor2(final ProcessorConfiguration conf) {
        final AtomicLong counter = new AtomicLong();
        final BlockingQueueProcessor<Integer> processor = BlockingQueueProcessor.<Integer>newBuilder()
                .addNext(conf.workerCount, new ProcessorHandler<Integer>() {
                    @Override
                    public void process(Integer object) {

                    }
                }).addNext(conf.workerCount,
                        conf.queueBatchSize,
                        new ProcessorBatchHandler<Integer>() {

                            @Override
                            public void process(List<Integer> objects) {

                            }
                        })
                .addNext(conf.workerCount, new ProcessorHandler<Integer>() {
                    @Override
                    public void process(Integer object) {
                        counter.incrementAndGet();
                    }
                }).build();
        new PerfTester("Disruptor processor", conf, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                processor.submit(index);
                return 1;
            }
        }).run(counter);
    }

    public static void main(String args[]) throws Exception {
        RunnerHelper.run(args, ProcessorPerf.class,
                ProcessorConfiguration.class,
                "processor.conf");
    }
}
