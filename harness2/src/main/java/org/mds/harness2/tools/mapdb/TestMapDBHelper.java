package org.mds.harness2.tools.mapdb;

import org.mapdb.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by modongsong on 2015/4/22.
 */
public class TestMapDBHelper {



    private static DBMaker createDBMaker(TestMapDBConfig config) {
        DBMaker dbMaker = null;
        switch (TestMapDBConfig.StoreType.valueOf(config.storeType.toUpperCase())) {
            case FILE:
                dbMaker = DBMaker.newFileDB(new File(config.storeFile));
                break;
            case TEMPFILE:
                dbMaker = DBMaker.newTempFileDB();
                break;
            case HEAP:
                dbMaker = DBMaker.newHeapDB();
                break;
            case MEM:
                dbMaker = DBMaker.newMemoryDB();
                break;
            case DMEM:
                dbMaker = DBMaker.newMemoryDirectDB();
                break;
            default:
                dbMaker = DBMaker.newMemoryDirectDB();
        }
        configDBMaker(dbMaker, config);
        return dbMaker;
    }

    private static void configDBMaker(DBMaker dbMaker, TestMapDBConfig config) {

    }

    private static void configHTreeMap(DB.HTreeMapMaker dbMaker, TestMapDBConfig config) {

    }

    private static void configHTreeSet(DB.HTreeSetMaker dbMaker, TestMapDBConfig config) {

    }

    private static void configBTreeMap(DB.BTreeMapMaker dbMaker, TestMapDBConfig config) {

    }

    private static void configBTreeSet(DB.BTreeSetMaker dbMaker, TestMapDBConfig config) {

    }


    public static HTreeMap<String, String> createHTreeMap(TestMapDBConfig config) {
        DBMaker dbMaker = createDBMaker(config);
        DB.HTreeMapMaker mapMaker = dbMaker.make().createHashMap("HTreeMap");
        configHTreeMap(mapMaker, config);

        return mapMaker.make();
    }

    public static Set<String> createHTreeSet(TestMapDBConfig config) {
        DBMaker dbMaker = createDBMaker(config);
        DB.HTreeSetMaker mapMaker = dbMaker.make().createHashSet("HTreeSet");
        configHTreeSet(mapMaker, config);
        return mapMaker.make();
    }

    public static BTreeMap<String, String> createBTreeMap(TestMapDBConfig config) {
        DBMaker dbMaker = createDBMaker(config);
        DB.BTreeMapMaker mapMaker = dbMaker.make().createTreeMap("BTreeMap");
        configBTreeMap(mapMaker, config);
        return mapMaker.make();
    }

    public static Set<String> createBTreeSet(TestMapDBConfig config) {
        DBMaker dbMaker = createDBMaker(config);
        DB.BTreeSetMaker mapMaker = dbMaker.make().createTreeSet("HTreeSet");
        configBTreeSet(mapMaker, config);
        return mapMaker.make();
    }

    public static BlockingQueue<String> createQueue(TestMapDBConfig config) {
        DBMaker dbMaker = createDBMaker(config);
        return dbMaker.make().createQueue("Queue", Serializer.STRING, config.useLock);
    }

    public static BlockingQueue<String> createCQueue(TestMapDBConfig config) {
        DBMaker dbMaker = createDBMaker(config);
        return dbMaker.make().createCircularQueue("Queue", Serializer.STRING, config.queueSize);
    }

    public static BlockingQueue<String> createStack(TestMapDBConfig config) {
        DBMaker dbMaker = createDBMaker(config);
        return dbMaker.make().createStack("Queue", Serializer.STRING, config.useLock);
    }

}
