package org.mds.harness2.tools.memcached;

import com.meetup.memcached.MemcachedClient;
import com.meetup.memcached.SockIOPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by modongsong on 14-4-1.
 */
public class TestMeetupClient {
    private final static Logger log = LoggerFactory.getLogger(TestMeetupClient.class);

    public static void main(String args[]) throws InterruptedException {
        SockIOPool pool = SockIOPool.getInstance();
        pool.setServers(new String[]{"192.168.205.101:11211"});
        pool.setInitConn(5);
        pool.setMinConn(5);
        pool.setMaxConn(50);
        pool.setMaintSleep(30);
        pool.setAliveCheck(true);

        pool.setNagle(false);
        pool.initialize();
        MemcachedClient memcachedClient = new MemcachedClient();
        while (true) {
            if (memcachedClient.set("aaaa", "adddaaaa")) {
                log.info("set:value.");
            }
            log.info("get: " + memcachedClient.get("aaaa"));
            memcachedClient.getMulti(new String[]{"aaaaa", "bbbbb"});
            if (memcachedClient.set("b", "b")) {
                break;
            } else {
                Thread.sleep(100);
            }

        }
        pool.shutDown();
    }
}
