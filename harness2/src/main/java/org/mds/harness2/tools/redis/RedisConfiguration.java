package org.mds.harness2.tools.redis;

import org.mds.harness.common2.perf.PerfConfig;

/**
 * Created by modongsong on 14-5-27.
 */
public class RedisConfiguration extends PerfConfig {
    public String redisAddress = "localhost";

    {
        this.output = 1;
        this.checkReturn = true;
    }
}
