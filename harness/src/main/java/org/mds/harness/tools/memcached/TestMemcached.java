package org.mds.harness.tools.memcached;

import net.spy.memcached.*;
import net.spy.memcached.transcoders.SerializingTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class TestMemcached {
    protected final static Logger log = LoggerFactory.getLogger(TestMemcached.class);

    public static void main(String args[]) {
        int timeoutthreshold = 5;
        try {
            timeoutthreshold = Integer.parseInt(args[0]);
        } catch (Exception ex) {

        }
        log.info("Set timeout threshold: " + timeoutthreshold);

        String servers = "192.168.205.101:11211 192.168.205.102:11211";
        ConnectionFactoryBuilder factoryBuilder = new ConnectionFactoryBuilder();

        SerializingTranscoder transcoder = new SerializingTranscoder();
        transcoder.setCompressionThreshold(1024);

        factoryBuilder.setProtocol(ConnectionFactoryBuilder.Protocol.BINARY);
        factoryBuilder.setTranscoder(transcoder);
        factoryBuilder.setOpTimeout(1000);
        factoryBuilder.setTimeoutExceptionThreshold(timeoutthreshold);
        factoryBuilder.setLocatorType(ConnectionFactoryBuilder.Locator.CONSISTENT);
        factoryBuilder.setFailureMode(FailureMode.Redistribute);
        factoryBuilder.setUseNagleAlgorithm(false);
        factoryBuilder.setHashAlg(DefaultHashAlgorithm.KETAMA_HASH);
        Random random = new Random();
        try {
            MemcachedClient client = new MemcachedClient(factoryBuilder.build(),
                    AddrUtil.getAddresses(servers));
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
                    Object value=client.get(key);
                    getCounter++;
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
