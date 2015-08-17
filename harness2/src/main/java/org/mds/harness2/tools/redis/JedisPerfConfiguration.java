package org.mds.harness2.tools.redis;

import org.mds.harness.common2.runner.dsm.DsmRunnerConfig;

/**
 * Created by modongsong on 14-5-27.
 */
public class JedisPerfConfiguration extends DsmRunnerConfig{
    public String redisAddress="127.0.0.1:11211";
    public int keyLen=10;
    public int dataLen=10;
    public int pipeMultiSize=10;
    public int pipeNumber=10;
}
