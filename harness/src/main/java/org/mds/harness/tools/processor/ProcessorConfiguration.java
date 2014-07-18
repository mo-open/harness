package org.mds.harness.tools.processor;

import org.mds.harness.common.perf.PerfConfig;

/**
 * @author Dongsong
 */
public class ProcessorConfiguration extends PerfConfig {
    int workerCount = 1;
    int queueBatchSize = 10;
    int bufferSize=10000;
}
