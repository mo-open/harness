package org.mds.harness.parallel;

import net.rubyeye.xmemcached.MemcachedClient;
import org.mds.hprocessor.memcache.MemcacheClientUtils;
import org.mds.hprocessor.memcache.MemcacheConfig;
import org.mds.hprocessor.processor.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.codahale.metrics.Timer;


/**
 * Created by modongsong on 14-6-11.
 */
public class DataRetriever implements Closeable {
    protected final static Logger log = LoggerFactory.getLogger(DataRetriever.class);

    private Timer allDataRetrieve = Metrics.timer(DataRetriever.class, "all_data_retriever");

    private Configuration configuration;
    private Processor dataProcessor;
    private MemcachedClient memcachedClient;
    private ExecutorService executorService;

    public DataRetriever(Configuration configuration, Processor dataProcessor) {
        this.configuration = configuration;
        this.dataProcessor = dataProcessor;
        this.memcachedClient = MemcacheClientUtils.createXMemcachedClient
                (new MemcacheConfig(configuration.memcacheAddress));
        this.executorService = Executors.newFixedThreadPool(configuration.retrievers);
    }

    @Override
    public void close() throws IOException {
        if (this.executorService != null) {
            this.executorService.shutdown();
        }
    }

    public void start() {
        Metrics.start(3);
        int groupCount = this.configuration.totalData / this.configuration.bulkSize + 1;
        int start;
        int end;
        for (int i = 0; i < groupCount; i++) {
            start = this.configuration.bulkSize * i;
            end = this.configuration.bulkSize * (i + 1);
            if (end >= this.configuration.totalData)
                end = this.configuration.totalData - 1;
            this.executorService.submit(new DataRetrieverTask(start, end));
        }
    }

    private class DataRetrieverTask implements Runnable {

        private int start, end;

        public DataRetrieverTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            Timer.Context context = allDataRetrieve.time();
            try {
                List<String> keys = new ArrayList();
                for (int i = this.start; i < this.end; i++) {
                    keys.add(DataPreparation.KEY_PREFIX + i);
                }

                Map<String, Object> data = memcachedClient.get(keys);
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    dataProcessor.submit(new EventData(entry.getKey(), entry.getValue().toString()));
                }
            } catch (Exception ex) {
                log.error("Failed to fetch data from memcache for ({},{})", this.start, this.end, ex);
            } finally {
                context.stop();
            }
        }
    }
}
