package org.mds.harness2.tools.reactor;

import org.mds.harness.common2.perf.PerfConfig;
import org.mds.harness.common2.perf.PerfTester;
import org.mds.harness.common2.runner.RunnerHelper;
import org.mds.harness2.tools.processor.ProcessorConfiguration;
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
                .dataSupplier(() -> {
                    return new Frame();
                }).consume(frame -> {
                    if (conf.handleTime > 0)
                        LockSupport.parkNanos(conf.handleTime);
                    counter.incrementAndGet();
                }).get();

        new PerfTester<PerfTester.SingleTask>("Disruptor processor", conf).run(
                (config, index) -> {
                    Operation<Frame> op = processor.prepare();
                    Frame f = op.get();
                    f.type = index;
                    op.commit();
                    return 1;
                }, counter);
    }

    public void runReactor(final ReactorConfig conf) {
        final AtomicLong counter = new AtomicLong();

        final Reactor reactor = this.createReactor(conf);
        for (int i = 0; i < conf.eventCount; i++) {
            reactor.on($(i), event -> {
                try {
                    if (conf.handleTime > 0)
                        LockSupport.parkNanos(conf.handleTime);
                    counter.incrementAndGet();
                } catch (Exception ex) {

                }
            });
        }

        final Random random = new Random();

        new PerfTester("Disruptor processor", conf).run((config, index) -> {
            reactor.notify(random.nextInt(conf.eventCount), Event.wrap(index));
            return 1;
        }, counter);
    }

    public static void main(String args[]) throws Exception {
        RunnerHelper.newInvoker()
                .setArgs(args)
                .setMainClass(ReactorPerf.class)
                .setConfigClass(ReactorConfig.class)
                .setConfigFile("reactor.yml")
                .invoke();
    }
}
