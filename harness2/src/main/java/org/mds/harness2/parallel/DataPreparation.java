package org.mds.harness2.parallel;

import net.rubyeye.xmemcached.MemcachedClient;
import org.mds.hprocessor.memcache.utils.MemcacheClientUtils;
import org.mds.hprocessor.memcache.utils.MemcacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by modongsong on 14-6-11.
 */
public class DataPreparation {
    protected final static Logger log = LoggerFactory.getLogger(DataPreparation.class);

    private Configuration configuration;
    private MemcachedClient memcachedClient;
    private ExecutorService executorService;

    public final static String KEY_PREFIX = "key_0123456789_0123456789_0123456789_";
    public final static String DATA_PREFIX = "data_0123456789_0123456789_0123456789_" +
            "0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_" +
            "0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_" +
            "0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_" +
            "0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_" +
            "0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_" +
            "0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_" +
            "0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_" +
            "0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_" +
            "0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_" +
            "0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_0123456789_";

    public DataPreparation(Configuration configuration) {
        this.configuration = configuration;
        this.memcachedClient = MemcacheClientUtils.createXMemcachedClient
                (new MemcacheConfig(configuration.memcacheAddress));
        this.executorService = Executors.newFixedThreadPool(20);
    }

    public void buildData() {
        log.info("Preparing data .... ");
        List<Future> tasks = new ArrayList();
        int start, end;
        int eachCount = configuration.totalData / 20;
        for (int i = 0; i <= 20; i++) {
            start = eachCount * i;
            end = eachCount * (i + 1);
            if (end > configuration.totalData) end = configuration.totalData;
            tasks.add(executorService.submit(new DataBuildTask(start, end)));
        }
        for (Future task : tasks) {
            try {
                task.get();
            } catch (Exception ex) {

            }
        }
        log.info("Data Preparation finished.");
    }

    private class DataBuildTask implements Runnable {
        private int start, end;

        DataBuildTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            for (int i = start; i < end; i++) {
                try {
                    memcachedClient.set(KEY_PREFIX + i, 1000000, DATA_PREFIX + i);
                } catch (Exception ex) {

                }
            }
        }
    }
}
