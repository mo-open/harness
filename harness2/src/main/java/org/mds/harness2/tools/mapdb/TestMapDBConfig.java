package org.mds.harness2.tools.mapdb;

import org.mds.harness.common2.perf.PerfConfig;

/**
 * Created by modongsong on 2015/4/22.
 */
public class TestMapDBConfig extends PerfConfig {
    double maxSizeInG=0.1;
    String storeType="dmem";
    String storeFile="/tmp/mapdb";
    String dataStruct="hashMap";
    int cacheSize=-1;
    boolean enableCounter;
    boolean dbCompress;
    String cacheRef="DEFAULT";
    boolean cacheLRU;
    boolean valueCompress;
    boolean transaction;
    boolean asyncWrite;
    boolean mmapFile;
    int asyncWriteDelayMs=0;
    int asyncWriteQueueSize=32000;


    public enum StoreType {
        FILE,TEMPFILE,HEAP,MEM,DMEM;
    }
    public enum DBCacheType{
        DEFAULT,HARDREF,SOFTREF,WEAKREF;
    }

    public enum DataStruct {
        HASHMAP, TREEMAP, STACK, QUEUE, HASHSET, TREESET;
    }
}
