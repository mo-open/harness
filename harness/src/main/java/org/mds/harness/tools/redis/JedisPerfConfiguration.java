package org.mds.harness.tools.redis;

import org.mds.harness.common.perf.PerfConfig;

/**
 * Created by modongsong on 14-5-27.
 */
public class JedisPerfConfiguration extends PerfConfig{
    String redisAddress="127.0.0.1:11211";
    int keyLen=10;
    int dataLen=10;
    int pipeMultiSize=10;
    int pipeNumber=10;
}
