package org.mds.harness2.tools.processor;

import org.mds.harness.common2.runner.dsm.DsmRunnerConfig;

/**
 * @author Dongsong
 */
public class ProcessorConfiguration extends DsmRunnerConfig {
    public int workerCount = 1;
    public int queueBatchSize = 10;
    public int bufferSize=10000;
}
