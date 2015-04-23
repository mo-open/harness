package org.mds.harness2.tools.mapdb;

import org.mapdb.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by modongsong on 2015/4/22.
 */
public class TestMapDBHelper {
    private static DBMaker createDBMaker(TestMapDBConfig config, String settings) {
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
        config(dbMaker, config, settings);
        return dbMaker;
    }

    private static void config(DBMaker dbMaker, TestMapDBConfig config, String settings) {
        Stream.of(settings.split(","))
                .map(String::toUpperCase)
                .map(DBMakerSetting::valueOf)
                .forEach(setting -> setting.set(dbMaker, config));
    }

    private static void config(DB.HTreeMapMaker dbMaker, TestMapDBConfig config, String settings) {
        Stream.of(settings.split(","))
                .map(String::toUpperCase)
                .map(HTreeMapSetting::valueOf)
                .forEach(setting -> setting.set(dbMaker, config));
    }

    private static void config(DB.HTreeSetMaker dbMaker, TestMapDBConfig config, String settings) {
        Stream.of(settings.split(","))
                .map(String::toUpperCase)
                .map(HTreeSetSetting::valueOf)
                .forEach(setting -> setting.set(dbMaker, config));
    }

    private static void config(DB.BTreeMapMaker dbMaker, TestMapDBConfig config, String settings) {
        Stream.of(settings.split(","))
                .map(String::toUpperCase)
                .map(BTreeMapSetting::valueOf)
                .forEach(setting -> setting.set(dbMaker, config));
    }

    private static void config(DB.BTreeSetMaker dbMaker, TestMapDBConfig config, String settings) {
        Stream.of(settings.split(","))
                .map(String::toUpperCase)
                .map(BTreeSetSetting::valueOf)
                .forEach(setting -> setting.set(dbMaker, config));
    }


    public static <T>Stream<HTreeMap<String, T>> createHTreeMap(TestMapDBConfig config) {
        final AtomicInteger index = new AtomicInteger(0);
        return Stream.of(config.dbMaker)
                .map(dbMakerSetting -> createDBMaker(config, dbMakerSetting))
                .flatMap(dbMaker -> Stream.of(config.htreeMap).map(settings -> {
                    DB.HTreeMapMaker maker = dbMaker.make().createHashMap("HTreeMap" + index.incrementAndGet());
                    config(maker, config, settings);
                    return maker;
                })).map(DB.HTreeMapMaker::make);
    }

    public static <T>Stream<Set<T>> createHTreeSet(TestMapDBConfig config) {
        final AtomicInteger index = new AtomicInteger(0);
        return Stream.of(config.dbMaker)
                .map(dbMakerSetting -> createDBMaker(config, dbMakerSetting))
                .flatMap(dbMaker -> Stream.of(config.htreeSet).map(settings -> {
                    DB.HTreeSetMaker maker = dbMaker.make().createHashSet("HTreeSet" + index.incrementAndGet());
                    config(maker, config, settings);
                    return maker;
                })).map(DB.HTreeSetMaker::make);
    }

    public static <T>Stream<BTreeMap<String, T>> createBTreeMap(TestMapDBConfig config) {
        final AtomicInteger index = new AtomicInteger(0);
        return Stream.of(config.dbMaker)
                .map(dbMakerSetting -> createDBMaker(config, dbMakerSetting))
                .flatMap(dbMaker -> Stream.of(config.btreeMap).map(settings -> {
                    DB.BTreeMapMaker maker = dbMaker.make().createTreeMap("BTreeMap" + index.incrementAndGet());
                    config(maker, config, settings);
                    return maker;
                })).map(DB.BTreeMapMaker::make);
    }

    public static <T>Stream<Set<T>> createBTreeSet(TestMapDBConfig config) {
        final AtomicInteger index = new AtomicInteger(0);
        return Stream.of(config.dbMaker)
                .map(dbMakerSetting -> createDBMaker(config, dbMakerSetting))
                .flatMap(dbMaker -> Stream.of(config.btreeSet).map(settings -> {
                    DB.BTreeSetMaker maker = dbMaker.make().createTreeSet("HTreeSet" + index.incrementAndGet());
                    config(maker, config, settings);
                    return maker;
                })).map(DB.BTreeSetMaker::make);
    }

    public static Stream<BlockingQueue<String>> createQueue(TestMapDBConfig config) {
        final AtomicInteger index = new AtomicInteger(0);
        return Stream.of(config.dbMaker)
                .map(dbMakerSetting -> createDBMaker(config, dbMakerSetting))
                .map(dbMaker -> dbMaker.make().createQueue("Queue" + index.incrementAndGet(), Serializer.STRING_ASCII, config.useLock));
    }

    public static Stream<BlockingQueue<String>> createCQueue(TestMapDBConfig config) {
        final AtomicInteger index = new AtomicInteger(0);
        return Stream.of(config.dbMaker)
                .map(dbMakerSetting -> createDBMaker(config, dbMakerSetting))
                .map(dbMaker -> dbMaker.make().createCircularQueue("CQueue" + index.incrementAndGet(), Serializer.STRING_ASCII, config.queueSize));
    }

    public static Stream<BlockingQueue<String>> createStack(TestMapDBConfig config) {
        final AtomicInteger index = new AtomicInteger(0);
        return Stream.of(config.dbMaker)
                .map(dbMakerSetting -> createDBMaker(config, dbMakerSetting))
                .map(dbMaker -> dbMaker.make().createStack("Stack" + index.incrementAndGet(), Serializer.STRING, config.useLock));
    }

}
