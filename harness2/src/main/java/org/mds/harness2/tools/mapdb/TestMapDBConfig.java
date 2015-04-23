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
    public boolean enableCounter;
    public boolean dbCompress;
    public String cacheRef = "DEFAULT";
    public boolean cacheLRU;
    public boolean valueCompress;
    public boolean transaction;
    public boolean asyncWrite;
    public boolean mmapFile;
    public int asyncWriteDelayMs = 0;
    public int asyncWriteQueueSize = 32000;
    public long expireMaxSize = 10000;
    public double expireStoreSize = 0.1;
    public int btreeNodeSize = 5;
    public boolean useLock;
    public long queueSize;

    String dbMaker1, dbMaker2, dbMaker3, dbMaker4, dbMaker5 = "";
    String htreeMap1, htreeMap2, htreeMap3, htreeMap4, htreeMap5 = "";
    String htreeSet1, htreeSet2, htreeSet3, htreeSet4, htreeSet5 = "";
    String btreeMap1, btreeMap2, btreeMap3, btreeMap4, btreeMap5 = "";
    String btreeSet1, btreeSet2, btreeSet3, btreeSet4, btreeSet5 = "";

    public enum StoreType {
        FILE, TEMPFILE, HEAP, MEM, DMEM;
    }

    public enum DBCacheType {
        DEFAULT, HARDREF, SOFTREF, WEAKREF;
    }

    public enum DataStruct {
        HASHMAP, TREEMAP, STACK, QUEUE, HASHSET, TREESET;
    }
}
