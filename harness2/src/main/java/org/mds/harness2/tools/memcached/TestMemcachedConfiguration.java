package org.mds.harness2.tools.memcached;

import org.mds.harness.common2.perf.PerfConfig;

public class TestMemcachedConfiguration extends PerfConfig {
    String memcachedAddress = "127.0.0.1:11211";
    int itemCount = 300000;
    boolean binary = false;
    int bulkCount = 100;
    int connectionPoolSize = 10;
    int compressionThreshold = 1024;
    int keyLen = 10;
    int dataLen = 10;
    boolean assureSet = false;
    int getterCount = 1;
    int setterCount = 1;
    int getterType = 0;
    boolean useBucket = true;
    boolean asyncSet = true;
    boolean enableCache = true;
}
