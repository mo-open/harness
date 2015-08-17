package org.mds.harness2.tools.redis;

import org.mds.harness.common2.runner.dsm.DsmRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * Created by modongsong on 14-5-27.
 */
public class RedisProducerConsumer extends DsmRunner<RedisConfiguration>{
    private final static Logger log = LoggerFactory.getLogger(TestJedisPerf.class);

    private static String LIST_KEY = "list-test";

    public void runProducer(RedisConfiguration configuration) {
        final Jedis jedis = new Jedis(configuration.redisAddress);
        this.runSingle("Test Producer", configuration, (configuration1, index) -> {
            return jedis.lpush(LIST_KEY, "value-" + index) >= 0 ? 1 : 0;
        });
        jedis.close();
    }

    public void runConsumer(RedisConfiguration configuration) {
        final Jedis jedis = new Jedis(configuration.redisAddress);
        this.runSingle("Test Consumer", configuration, (configuration1, index) -> {
            return jedis.lpop(LIST_KEY) != null ? 1 : 0;
        });
        jedis.close();
    }

}
