package org.mds.harness2.tools.mapdb;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.io.File;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by modongsong on 2015/4/22.
 */
public class TestMapDBHelper {

    private static DB makeDB(final TestMapDBConfig config) {
        DBMaker dbMaker = null;

        switch (TestMapDBConfig.StoreType.valueOf(config.storeType.toUpperCase())) {
            case FILE:
                dbMaker = DBMaker.newFileDB(new File(config.storeFile));
                dbMaker.deleteFilesAfterClose();
                if (!config.transaction) {
                    dbMaker.transactionDisable();
                }
                break;
            case TEMPFILE:
                dbMaker = DBMaker.newTempFileDB();
                dbMaker.deleteFilesAfterClose();
                if (!config.transaction) {
                    dbMaker.transactionDisable();
                }
                break;
            case HEAP:
                dbMaker = DBMaker.newHeapDB();
                dbMaker.transactionDisable();
                break;
            case MEM:
                dbMaker = DBMaker.newMemoryDB();
                dbMaker.transactionDisable();
                break;
            case DMEM:
                dbMaker = DBMaker.newMemoryDirectDB();
                dbMaker.transactionDisable();
        }

        if (config.mmapFile)
            dbMaker.mmapFileEnableIfSupported();

        dbMaker.closeOnJvmShutdown();

        if (config.cacheSize > 0) {
            dbMaker.cacheSize(config.cacheSize);
        } else {
            dbMaker.cacheDisable();
        }
        if (config.dbCompress) {
            dbMaker.compressionEnable();
        }
        if (config.cacheLRU) {
            dbMaker.cacheLRUEnable();
        }

        if (config.cacheSize > 0) {
            switch (TestMapDBConfig.DBCacheType.valueOf(config.cacheRef.toUpperCase())) {
                case HARDREF:
                    dbMaker.cacheHardRefEnable();
                    break;
                case SOFTREF:
                    dbMaker.cacheSoftRefEnable();
                    break;
                case WEAKREF:
                    dbMaker.cacheWeakRefEnable();
            }
        }

        dbMaker.sizeLimit(config.maxSizeInG);
        if (config.asyncWrite) {
            dbMaker.asyncWriteEnable();
            dbMaker.asyncWriteFlushDelay(config.asyncWriteDelayMs);
            dbMaker.asyncWriteQueueSize(config.asyncWriteQueueSize);
        }

        return dbMaker.make();
    }

    @FunctionalInterface
    private interface DBSetting {
        void set(DBMaker dbMaker, TestMapDBConfig config);

        default String name() {
            return this.getClass().getSimpleName();
        }
    }


    DBSetting setGlobal = (dbMaker, config) ->
            dbMaker.mmapFileEnableIfSupported()
                    .sizeLimit(config.maxSizeInG)
                    .deleteFilesAfterClose()
                    .closeOnJvmShutdown();

    DBSetting enableAsync = (dbMaker, config) -> dbMaker.asyncWriteEnable()
            .asyncWriteFlushDelay(config.asyncWriteDelayMs)
            .asyncWriteQueueSize(config.asyncWriteQueueSize);

    DBSetting disableCache = (dbMaker, config) -> dbMaker.cacheDisable();

    DBSetting defaultCache = (dbMaker, config) -> dbMaker.cacheSize(config.cacheSize);

    DBSetting lruCache = (dbMaker, config) ->
            dbMaker.cacheSize(config.cacheSize).cacheLRUEnable();
    DBSetting hardCache = (dbMaker, config) ->
            dbMaker.cacheSize(config.cacheSize).cacheHardRefEnable();
    DBSetting softCache = (dbMaker, config) ->
            dbMaker.cacheSize(config.cacheSize).cacheSoftRefEnable();
    DBSetting weakCache = (dbMaker, config) ->
            dbMaker.cacheSize(config.cacheSize).cacheWeakRefEnable();

    DBSetting commitSyncDisable = (dbMaker, config) ->
            dbMaker.commitFileSyncDisable();

    DBSetting compressEnable = (dbMaker, config) -> dbMaker.compressionEnable();

    DBSetting snapshotEnable = (dbMaker, config) -> dbMaker.snapshotEnable();


    DBSetting[] level0Settings = new DBSetting[]{setGlobal};
    DBSetting[] level1Settings = new DBSetting[]{enableAsync, commitSyncDisable, commitSyncDisable, snapshotEnable};
    DBSetting[] level2Settings = new DBSetting[]{disableCache, defaultCache, lruCache, hardCache, softCache, weakCache};


    private DBMaker setDBMaker(DBMaker dbMaker,
                               final TestMapDBConfig config,
                               DBSetting[] confs) {

        Arrays.stream(confs).forEach(conf ->
                        conf.set(dbMaker, config)
        );
        return dbMaker;
    }


    private DBMaker setAsync(DBMaker dbMaker, final TestMapDBConfig config) {
        return dbMaker.asyncWriteEnable()
                .asyncWriteFlushDelay(config.asyncWriteDelayMs)
                .asyncWriteQueueSize(config.asyncWriteQueueSize)
                .cacheSize(config.cacheSize)
                .commitFileSyncDisable()
                .mmapFileEnableIfSupported()
                .snapshotEnable()
                .compressionEnable()
                .sizeLimit(config.maxSizeInG)
                .deleteFilesAfterClose()
                .closeOnJvmShutdown();
    }

    private DBMaker asyncCacheDBMaker(DBMaker dbMaker, final TestMapDBConfig config) {
        return dbMaker.asyncWriteEnable()
                .asyncWriteFlushDelay(config.asyncWriteDelayMs)
                .asyncWriteQueueSize(config.asyncWriteQueueSize)
                .cacheSize(config.cacheSize)
                .commitFileSyncDisable()
                .mmapFileEnableIfSupported()
                .snapshotEnable()
                .compressionEnable()
                .sizeLimit(config.maxSizeInG)
                .deleteFilesAfterClose()
                .closeOnJvmShutdown();
    }

    public static List<HTreeMap<String, String>> makeHashMap(final TestMapDBConfig config) {
        List<HTreeMap<String, String>> maps = new ArrayList<>();

        maps.add(DBMaker.newHeapDB()
                .asyncWriteEnable()
                .asyncWriteFlushDelay(config.asyncWriteDelayMs)
                .asyncWriteQueueSize(config.asyncWriteQueueSize)
                .cacheDisable());


        return maps;
    }

    private static DB makeCommonDB(final TestMapDBConfig config) {
        return DBMaker.newMemoryDirectDB()
                .transactionDisable()
                .closeOnJvmShutdown()
                .cacheSize(config.cacheSize)
                .sizeLimit(config.maxSizeInG)
                .make();
    }

    private static DB makeNoCacheDB(final TestMapDBConfig config) {
        return DBMaker.newMemoryDirectDB()
                .transactionDisable()
                .closeOnJvmShutdown()
                .cacheDisable()
                .sizeLimit(config.maxSizeInG)
                .make();
    }

    private static DB makeLRUCacheDB(final TestMapDBConfig config) {
        return DBMaker.newMemoryDirectDB()
                .transactionDisable()
                .closeOnJvmShutdown()
                .cacheLRUEnable()
                .cacheSize(config.cacheSize)
                .sizeLimit(config.maxSizeInG)
                .make();
    }

    //this cache is unbound
    private static DB makeHardCacheDB(final TestMapDBConfig config) {
        return DBMaker.newMemoryDirectDB()
                .transactionDisable()
                .closeOnJvmShutdown()
                .cacheHardRefEnable()
                .sizeLimit(config.maxSizeInG)
                .make();
    }

    //this cache is unbound
    private static DB makeSoftCacheDB(final TestMapDBConfig config) {
        return DBMaker.newMemoryDirectDB()
                .transactionDisable()
                .closeOnJvmShutdown()
                .cacheSoftRefEnable()
                .sizeLimit(config.maxSizeInG)
                .make();
    }

    public static DB createDB(final TestMapDBConfig config) {
        TestMapDBConfig.DBType type = TestMapDBConfig.DBType.valueOf(config.dbType);
        switch (type) {
            case COMMON:
                return makeCommonDB(config);
            case NOCACHE:
                return makeNoCacheDB(config);
            case LRUCACHE:
                return makeLRUCacheDB(config);
            case HARDCACHE:
                return makeHardCacheDB(config);
            case SOFTCACHE:
                return makeSoftCacheDB(config);
        }
        return null;
    }


    public static HTreeMap<String, String> createTreeMap(TestMapDBConfig config) {
        DB db = createDB(config);
        DB.HTreeMapMaker maker = db.createHashMap("HTreeMap");
        if (config.enableCounter) {
            maker.counterEnable();
        }
        maker.keySerializer(Serializer.STRING);
        if (config.compress) {
            maker.valueSerializer(new Serializer.CompressionWrapper<>(Serializer.STRING));
        }
        maker.valueSerializer(Serializer.STRING);
        return maker.makeOrGet();
    }
}
