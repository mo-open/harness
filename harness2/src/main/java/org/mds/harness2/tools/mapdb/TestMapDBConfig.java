package org.mds.harness2.tools.mapdb;

import org.mds.harness.common2.perf.PerfConfig;

/**
 * Created by modongsong on 2015/4/22.
 */
public class TestMapDBConfig extends PerfConfig {
    double maxSizeInG = 0.1;
    String storeType = "dmem";
    String storeFile = "/tmp/mapdb";
    String dataStruct = "hashMap";
    int cacheSize = -1;
    boolean enableCounter;
    boolean dbCompress;
    String cacheRef = "DEFAULT";
    boolean cacheLRU;
    boolean valueCompress;
    boolean transaction;
    boolean asyncWrite;
    boolean mmapFile;
    int asyncWriteDelayMs = 0;
    int asyncWriteQueueSize = 32000;
    long expireMaxSize = 10000;
    double expireStoreSize = 0.1;
    int btreeNodeSize = 5;
    boolean useLock;
    long queueSize;

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
