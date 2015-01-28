/*
 * Copyright 2011 LMAX Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mds.harness.tools.distruptor;

import org.mds.harness.tools.distruptor.support.*;
import com.lmax.disruptor.*;
import org.junit.Test;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.LockSupport;

import static com.lmax.disruptor.RingBuffer.createMultiProducer;

/**
 * <pre>
 * Sequence a series of events from multiple publishers going to multiple work processors.
 *
 * +----+                  +-----+
 * | P1 |---+          +-->| WP1 |
 * +----+   |  +-----+ |   +-----+
 *          +->| RB1 |-+
 * +----+   |  +-----+ |   +-----+
 * | P2 |---+          +-->| WP2 |
 * +----+                  +-----+
 *
 * P1  - Publisher 1
 * P2  - Publisher 2
 * RB  - RingBuffer
 * WP1 - EventProcessor 1
 * WP2 - EventProcessor 2
 * </pre>
 */
public final class TwoPublisherToTwoProcessorWorkProcessorThroughputTest extends AbstractPerfTestQueueVsDisruptor {
    private static final int NUM_PUBLISHERS = 2;
    private static final int WORKERS = 5;
    private static final int BUFFER_SIZE = 1024 * 64;
    private static final long ITERATIONS = 1000L * 100L * 1L;
    private static final long INTERVAL = 1L;
    private final ExecutorService executor = Executors.newFixedThreadPool(NUM_PUBLISHERS + 2);
    private final CyclicBarrier cyclicBarrier = new CyclicBarrier(NUM_PUBLISHERS + 1);

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private final RingBuffer<ValueEvent> ringBuffer =
            createMultiProducer(ValueEvent.EVENT_FACTORY, BUFFER_SIZE, new BusySpinWaitStrategy());

    private final SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();
    private final Sequence workSequence = new Sequence(-1);

    private final ValueAdditionWorkHandler[] handlers = new ValueAdditionWorkHandler[WORKERS];

    {
        for (int i = 0; i < WORKERS; i++) {
            handlers[i] = new ValueAdditionWorkHandler();
        }
    }

    @SuppressWarnings("unchecked")
    private final WorkProcessor<ValueEvent>[] workProcessors = new WorkProcessor[WORKERS];

    {
        for (int i = 0; i < WORKERS; i++) {
            workProcessors[i] = new WorkProcessor<ValueEvent>(ringBuffer, sequenceBarrier,
                    handlers[i], new IgnoreExceptionHandler(),
                    workSequence);
        }
    }

    private final ValuePublisher[] valuePublishers = new ValuePublisher[NUM_PUBLISHERS];

    {
        for (int i = 0; i < NUM_PUBLISHERS; i++) {
            valuePublishers[i] = new ValuePublisher(cyclicBarrier, ringBuffer, ITERATIONS);
        }

        Sequence[] sequences=new Sequence[WORKERS];
        for(int i=0;i<WORKERS;i++){
            sequences[i]=workProcessors[i].getSequence();
        }

        ringBuffer.addGatingSequences(sequences);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected int getRequiredProcessorCount() {
        return 4;
    }

    @Test
    @Override
    public void shouldCompareDisruptorVsQueues() throws Exception {
        testImplementations();
    }

    @Override
    protected long runQueuePass() throws Exception {
        return 0;
    }

    @Override
    protected long runDisruptorPass() throws Exception {
        long expected = ringBuffer.getCursor() + (NUM_PUBLISHERS * ITERATIONS);
        Future<?>[] futures = new Future[NUM_PUBLISHERS];
        for (int i = 0; i < NUM_PUBLISHERS; i++) {
            futures[i] = executor.submit(valuePublishers[i]);
        }

        for (WorkProcessor<ValueEvent> processor : workProcessors) {
            executor.submit(processor);
        }

        long start = System.currentTimeMillis();
        cyclicBarrier.await();

        for (int i = 0; i < NUM_PUBLISHERS; i++) {
            futures[i].get();
        }

        while (workSequence.get() < expected) {
            LockSupport.parkNanos(1L);
        }

        long opsPerSecond = (ITERATIONS * 1000L) / (System.currentTimeMillis() - start);

        Thread.sleep(1000);

        for (WorkProcessor<ValueEvent> processor : workProcessors) {
            processor.halt();
        }

        return opsPerSecond;
    }
}
