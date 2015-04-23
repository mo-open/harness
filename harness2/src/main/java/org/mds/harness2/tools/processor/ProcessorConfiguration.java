package org.mds.harness2.tools.processor;

import org.mds.harness.common2.perf.PerfConfig;

/**
 * @author Dongsong
 */
public class ProcessorConfiguration extends PerfConfig {
    public int workerCount = 1;
    public int queueBatchSize = 10;
    public int bufferSize=10000;
}
