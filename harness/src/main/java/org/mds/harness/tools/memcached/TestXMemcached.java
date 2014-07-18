package org.mds.harness.tools.memcached;


import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class TestXMemcached {
    protected final static Logger log = LoggerFactory.getLogger(TestMemcached.class);

    public static void main(String args[]) {
//        int timeoutthreshold = 5;
//        try {
//            timeoutthreshold = Integer.parseInt(args[0]);
//        } catch (Exception ex) {
//
//        }
//        log.info("Set timeout threshold: " + timeoutthreshold);

        String servers = "192.168.205.101:11211 192.168.205.102:11211";
        XMemcachedClientBuilder builder = new XMemcachedClientBuilder(servers);
        builder.setSessionLocator(new KetamaMemcachedSessionLocator());
        builder.setCommandFactory(new BinaryCommandFactory());
        builder.setConnectionPoolSize(2);
        builder.setConnectTimeout(3000);
        builder.setFailureMode(false);
        builder.setOpTimeout(1000);

        Random random = new Random();
        try {
            MemcachedClient client = builder.build();

            int setCounter=0;
            int setHitCounter=0;
            int getCounter=0;
            int getHitCounter=0;
            while (true) {
                String key = String.valueOf(random.nextInt(100));
                setCounter++;
                try {
                    client.set(key, 100000, key);
                    setHitCounter++;
                    Thread.sleep(10);
                } catch (Exception ex) {
                    log.error("Failed to Insert: "+key+" : "+ex);
                    continue;
                }

                try {
                    getCounter++;
                    Object value=client.get(key);
                    if(value==null)
                    {
                        log.info("Failed to get the value");
                        continue;
                    }
                    getHitCounter++;
                    client.delete(key);
                    Thread.sleep(10);
                } catch (Exception ex) {
                    log.error("Get failure: "+ex);
                }

                if(setCounter % 30 ==0){
                    log.info("Set: "+setCounter+"; set hit: "+setHitCounter);
                    log.info("Get: "+getCounter+": get hit: "+getHitCounter);
                    setCounter=0;
                    setHitCounter=0;
                    getCounter=0;
                    getHitCounter=0;
                }
            }
        } catch (Exception ex) {
            log.error("", ex);
        }
    }
}
