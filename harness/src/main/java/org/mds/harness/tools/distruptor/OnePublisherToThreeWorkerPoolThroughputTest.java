package org.mds.harness.tools.distruptor;

import org.mds.harness.tools.distruptor.support.*;
import com.lmax.disruptor.FatalExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkerPool;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.util.PaddedLong;
import org.junit.Test;

import java.util.concurrent.*;

import static junit.framework.Assert.assertEquals;

public final class OnePublisherToThreeWorkerPoolThroughputTest
    extends AbstractPerfTestQueueVsDisruptor
{
    private static final int NUM_WORKERS = 3;
    private static final int BUFFER_SIZE = 1024 * 8;
    private static final long ITERATIONS = 1000L * 1000L * 10L;
    private final ExecutorService executor = Executors.newFixedThreadPool(NUM_WORKERS);

    private final PaddedLong[] counters = new PaddedLong[NUM_WORKERS];
    {
        for (int i = 0; i < NUM_WORKERS; i++)
        {
            counters[i] = new PaddedLong();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private final BlockingQueue<Long> blockingQueue = new LinkedBlockingQueue<Long>(BUFFER_SIZE);
    private final EventCountingQueueProcessor[] queueWorkers = new EventCountingQueueProcessor[NUM_WORKERS];
    {
        for (int i = 0; i < NUM_WORKERS; i++)
        {
            queueWorkers[i] = new EventCountingQueueProcessor(blockingQueue, counters, i);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private final EventCountingWorkHandler[] handlers = new EventCountingWorkHandler[NUM_WORKERS];
    {
        for (int i = 0; i < NUM_WORKERS; i++)
        {
            handlers[i] = new EventCountingWorkHandler(counters, i);
        }
    }

    private final RingBuffer<ValueEvent> ringBuffer =
            RingBuffer.createSingleProducer(ValueEvent.EVENT_FACTORY,
                                            BUFFER_SIZE,
                                            new YieldingWaitStrategy());

    private final WorkerPool<ValueEvent> workerPool =
            new WorkerPool<ValueEvent>(ringBuffer,
                                       ringBuffer.newBarrier(),
                                       new FatalExceptionHandler(),
                                       handlers);
    {
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected int getRequiredProcessorCount()
    {
        return 4;
    }

    @Test
    @Override
    public void shouldCompareDisruptorVsQueues() throws Exception
    {
        System.setProperty("com.lmax.runQueueTests", "true");
        testImplementations();
    }

    @Override
    protected long runQueuePass() throws InterruptedException
    {
        resetCounters();
        Future<?>[] futures = new Future[NUM_WORKERS];
        for (int i = 0; i < NUM_WORKERS; i++)
        {
            futures[i] = executor.submit(queueWorkers[i]);
        }

        long start = System.currentTimeMillis();

        for (long i = 0; i < ITERATIONS; i++)
        {
            blockingQueue.put(Long.valueOf(i));
        }

        while (blockingQueue.size() > 0)
        {
            // spin while queue drains
        }

        for (int i = 0; i < NUM_WORKERS; i++)
        {
            queueWorkers[i].halt();
            futures[i].cancel(true);
        }

        long opsPerSecond = (ITERATIONS * 1000L) / (System.currentTimeMillis() - start);

        assertEquals(ITERATIONS, sumCounters());

        return opsPerSecond;
    }

    @Override
    protected long runDisruptorPass() throws InterruptedException
    {

        resetCounters();
        RingBuffer<ValueEvent> ringBuffer = workerPool.start(executor);
        long start = System.currentTimeMillis();

        for (long i = 0; i < ITERATIONS; i++)
        {
            long sequence = ringBuffer.next();
            ringBuffer.get(sequence).setValue(i);
            ringBuffer.publish(sequence);
        }

        workerPool.drainAndHalt();
        long opsPerSecond = (ITERATIONS * 1000L) / (System.currentTimeMillis() - start);

        assertEquals(ITERATIONS, sumCounters());

        return opsPerSecond;
    }

    private void resetCounters()
    {
        for (int i = 0; i < NUM_WORKERS; i++)
        {
            counters[i].set(0L);
        }
    }

    private long sumCounters()
    {
        long sumJobs = 0L;
        for (int i = 0; i < NUM_WORKERS; i++)
        {
            sumJobs += counters[i].get();
        }

        return sumJobs;
    }
}
