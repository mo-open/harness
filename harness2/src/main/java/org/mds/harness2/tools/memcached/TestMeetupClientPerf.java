package org.mds.harness2.tools.memcached;

import com.meetup.memcached.SockIOPool;
import com.meetup.memcached.MemcachedClient;
import org.apache.commons.lang3.StringUtils;
import org.mds.harness.common2.perf.PerfConfig;
import org.mds.harness.common2.perf.PerfTester;
import org.mds.harness.common2.runner.RunnerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by modongsong on 14-4-1.
 */
public class TestMeetupClientPerf {
    private final static Logger log = LoggerFactory.getLogger(TestMeetupClientPerf.class);

    private static String KEY_PREFIX = "key-";
    private static String DATA_PREFIX = "DATA-";
    MemcachedClient memcachedClient;
    SockIOPool pool;

    public void beforeRun(final TestMemcachedConfiguration conf) {
        SockIOPool pool = SockIOPool.getInstance();
        pool.setServers(new String[]{conf.memcachedAddress});
        pool.setInitConn(5);
        pool.setMinConn(5);
        pool.setMaxConn(50);
        pool.setMaintSleep(30);

        pool.setNagle(false);
        pool.initialize();
        memcachedClient = new MemcachedClient();
        KEY_PREFIX = KEY_PREFIX + StringUtils.repeat("1", conf.keyLen);
        DATA_PREFIX = DATA_PREFIX + StringUtils.repeat("1", conf.dataLen);
    }

    public void afterRun(final TestMemcachedConfiguration conf) {
        if (pool != null)
            pool.shutDown();
    }

    public void runSet(final TestMemcachedConfiguration conf) {
        new PerfTester("Meetup  memcache client Perftest", conf).run((config, index) -> {
            memcachedClient.set(KEY_PREFIX + index, DATA_PREFIX + index, 10000);
            return 1;
        });
    }

    public void runGet(final TestMemcachedConfiguration conf) {
        new PerfTester("Meetup  memcache client Perftest", conf).run((config, index) -> {
            memcachedClient.get(KEY_PREFIX + index);
            return 1;
        });
    }

    public void runGetBulk(final TestMemcachedConfiguration conf) {
        new PerfTester<PerfTester.BatchTask>("Meetup  memcache client Perftest", conf).run((config, indexes) -> {
            List<String> keys = new ArrayList<String>();
            for (Long index : indexes) {
                keys.add(KEY_PREFIX + index);
            }
            memcachedClient.getMultiArray(keys.toArray(new String[]{}));
            return 1;
        });
    }

    public static void main(String args[]) throws Exception {
        RunnerHelper.newInvoker()
                .setArgs(args)
                .setMainClass(TestMeetupClientPerf.class)
                .setConfigClass(TestMemcachedConfiguration.class)
                .setConfigFile("testMemcached.yml")
                .invoke();
    }
}
