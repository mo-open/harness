package org.mds.harness.common2.perf;

import org.mds.hprocessor.processor.DisruptorProcessor;
import org.mds.hprocessor.processor.ProcessorHandler;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by modongsong on 2015/4/21.
 */
public class DisrupterExecutor extends TestExecutor {
    protected DisrupterExecutor(PerfConfig configuration, AtomicLong iCounter, int batchSize) {
        super(configuration, iCounter, batchSize);
    }

    @Override
    public void run(PerfTester.Task task) {
        if (iCounter == null) {
            iCounter = new AtomicLong();
        }

        final AtomicLong finishCounter = iCounter;
        final AtomicLong sendCounter = new AtomicLong();
        DisruptorProcessor<Integer> processor = DisruptorProcessor.<Integer>newBuilder()
                .setBufferSize(configuration.totalCount)
                .addNext(configuration.threadCount,
                        index -> {
                            finishCounter.incrementAndGet();
                            try {
                                task.run(configuration, index);
                            } catch (Exception ex) {

                            }
                        }).build();

        for (int i = 0; i < configuration.testRounds; i++) {
            sendCounter.set(0);
            for (int k = 0; k < configuration.totalCount; k++) {
                sendCounter.incrementAndGet();
                processor.submit(k);
            }
        }
    }
}
