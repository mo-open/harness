package org.mds.harness2.tools.memcached;

import org.mds.harness.common2.runner.dsm.DsmRunnerConfig;

public class TestMemcachedConfiguration extends DsmRunnerConfig {
    public String memcachedAddress = "127.0.0.1:11211";
    public int itemCount = 300000;
    public boolean binary = false;
    public int bulkCount = 100;
    public int connectionPoolSize = 10;
    public int compressionThreshold = 1024;
    public int keyLen = 10;
    public int dataLen = 10;
    public boolean assureSet = false;
    public int getterCount = 1;
    public int setterCount = 1;
    public int getterType = 0;
    public boolean useBucket = true;
    public boolean asyncSet = true;
    public boolean enableCache = true;
}
