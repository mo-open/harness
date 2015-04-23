package org.mds.harness2.tools.mapdb;

import org.mds.harness.common2.perf.PerfConfig;

/**
 * Created by modongsong on 2015/4/22.
 */
public class TestMapDBConfig extends PerfConfig {
    public double maxSizeInG = 0.1;
    public String storeType = "dmem";
    public String storeFile = "/tmp/mapdb";
    public String dataStruct = "hashMap";
    public int cacheSize = -1;
    public boolean valueCompress;
    public int asyncWriteDelayMs = 0;
    public int asyncWriteQueueSize = 32000;
    public long expireMaxSize = 10000;
    public double expireStoreSize = 0.1;
    public int btreeNodeSize = 5;
    public boolean useLock;
    public long queueSize;
    public int valueLen = 20;
    //0:remove,1:set, other: mixed
    public int opMode = 0;

    public String[] dbMaker;
    public String[] htreeMap;
    public String[] htreeSet;
    public String[] btreeMap;
    public String[] btreeSet;


    public enum StoreType {
        FILE, TEMPFILE, HEAP, MEM, DMEM;
    }

    public enum OpMode {
        SET, REMOVE, MIXED
    }

    public enum DataStruct {
        HASHMAP, TREEMAP, STACK, QUEUE, HASHSET, TREESET;
    }
}
