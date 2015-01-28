package org.mds.harness.tools.reactor;

import org.mds.harness.common.perf.PerfConfig;
import org.mds.harness.common.perf.PerfTester;
import org.mds.harness.common.runner.RunnerHelper;
import org.mds.harness.tools.processor.ProcessorConfiguration;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.processor.Operation;
import reactor.core.processor.Processor;
import reactor.core.processor.spec.ProcessorSpec;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import reactor.function.Consumer;
import reactor.function.Supplier;

import static reactor.event.selector.Selectors.$;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by modongsong on 2015/1/28.
 */
public class ReactorPerf {
    private Reactor createReactor(final ReactorConfig conf) {
        String type = Environment.EVENT_LOOP;
        switch (conf.reactorType) {
            case 1:
                type = Environment.EVENT_LOOP;
                break;
            case 2:
                type = Environment.RING_BUFFER;
                break;
            case 3:
                type = Environment.THREAD_POOL;
        }
        return Reactors.reactor()
                .env(new Environment())
                .dispatcher(type)
                .get();
    }

    public class Frame {
        int type;
        String message;
    }

    public void runProcessor(final ReactorConfig conf) {
        final AtomicLong counter = new AtomicLong();
        final Processor<Frame> processor = new ProcessorSpec<Frame>()
                .dataBufferSize(1024 * 64)
                .multiThreadedProducer()
                .dataSupplier(new Supplier<Frame>() {
                    public Frame get() {
                        return new Frame();
                    }
                }).consume(new Consumer<Frame>() {
                    public void accept(Frame frame) {
                        if (conf.handleTime > 0)
                            LockSupport.parkNanos(conf.handleTime);
                        counter.incrementAndGet();
                    }
                }).get();

        new PerfTester("Disruptor processor", conf, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                Operation<Frame> op = processor.prepare();
                Frame f = op.get();
                f.type = index;
                op.commit();
                return 1;
            }
        }).run(counter);
    }

    public void runReactor(final ReactorConfig conf) {
        final AtomicLong counter = new AtomicLong();

        final Reactor reactor = this.createReactor(conf);
        for (int i = 0; i < conf.eventCount; i++) {
            reactor.on($(i), new Consumer<Event<?>>() {
                @Override
                public void accept(Event<?> event) {
                    try {
                        if (conf.handleTime > 0)
                            LockSupport.parkNanos(conf.handleTime);
                        counter.incrementAndGet();
                    } catch (Exception ex) {

                    }
                }
            });
        }

        final Random random = new Random();


        new PerfTester("Disruptor processor", conf, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                reactor.notify(random.nextInt(conf.eventCount), Event.wrap(index));
                return 1;
            }
        }).run(counter);
    }

    public static void main(String args[]) throws Exception {
        RunnerHelper.run(args, ReactorPerf.class,
                ReactorConfig.class,
                "reactor.conf");
    }
}
