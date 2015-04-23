package org.mds.harness2.tools.mapdb;

import org.mapdb.*;

import javax.print.DocFlavor;

/**
 * Created by modoso on 15/4/22.
 */
interface Setting<T> {
    public void set(T dbMaker, TestMapDBConfig config);
}

enum DBMakerSetting implements Setting<DBMaker> {
    GLOBAL((dbMaker, config) ->
            dbMaker.mmapFileEnableIfSupported()
                    .sizeLimit(config.maxSizeInG)
                    .closeOnJvmShutdown()
    ),
    DISABLE_TRANS((dbMaker, config) -> dbMaker.transactionDisable()),
    DELETE_FILE((dbMaker, config) -> dbMaker.deleteFilesAfterClose()),
    ENABLE_ASYNC((dbMaker, config) -> dbMaker.asyncWriteEnable()
            .asyncWriteFlushDelay(config.asyncWriteDelayMs)
            .asyncWriteQueueSize(config.asyncWriteQueueSize)),
    DISABLE_CACHE((dbMaker, config) -> dbMaker.cacheDisable()),
    DEFAULT_CACHE((dbMaker, config) -> dbMaker.cacheSize(config.cacheSize)),
    LRU_CACHE((dbMaker, config) ->
            dbMaker.cacheSize(config.cacheSize).cacheLRUEnable()),
    HARD_CACHE((dbMaker, config) ->
            dbMaker.cacheSize(config.cacheSize).cacheHardRefEnable()),
    SOFT_CACHE((dbMaker, config) ->
            dbMaker.cacheSize(config.cacheSize).cacheSoftRefEnable()),
    WEAK_CACHE((dbMaker, config) ->
            dbMaker.cacheSize(config.cacheSize).cacheWeakRefEnable()),
    DISABLE_COMMIT_SYNC((dbMaker, config) -> dbMaker.commitFileSyncDisable()),
    ENABLE_COMPRESS((dbMaker, config) -> dbMaker.compressionEnable()),
    ENABLE_SNAPSHOT((dbMaker, config) -> dbMaker.snapshotEnable());

    private Setting<DBMaker> setting;

    private DBMakerSetting(Setting<DBMaker> setting) {
        this.setting = setting;
    }

    @Override
    public void set(DBMaker dbMaker, TestMapDBConfig config) {
        this.setting.set(dbMaker, config);
    }
}

enum HTreeMapSetting implements Setting<DB.HTreeMapMaker> {
    GLOBAL((dbMaker, config) -> dbMaker.keySerializer(Serializer.STRING)),
    ENABLE_COUNTER((dbMaker, config) -> dbMaker.counterEnable()),
    EXPIRE_MAX((dbMaker, config) -> dbMaker.expireMaxSize(config.expireMaxSize)),
    EXPIRE_STORE((dbMaker, config) -> dbMaker.expireStoreSize(config.expireStoreSize)),
    VALUE_COMPRESS((dbMaker, config) -> {
        if (config.valueCompress)
            dbMaker.valueSerializer(new Serializer.CompressionWrapper<>(Serializer.STRING));
        else
            dbMaker.valueSerializer(Serializer.STRING);
    });

    private Setting<DB.HTreeMapMaker> setting;

    private HTreeMapSetting(Setting<DB.HTreeMapMaker> setting) {
        this.setting = setting;
    }

    @Override
    public void set(DB.HTreeMapMaker dbMaker, TestMapDBConfig config) {
        this.setting.set(dbMaker, config);
    }
}

enum HTreeSetSetting implements Setting<DB.HTreeSetMaker> {
    ENABLE_COUNTER((dbMaker, config) -> dbMaker.counterEnable()),
    EXPIRE_MAX((dbMaker, config) -> dbMaker.expireMaxSize(config.expireMaxSize)),
    EXPIRE_STORE((dbMaker, config) -> dbMaker.expireStoreSize(config.expireStoreSize)),
    VALUE_COMPRESS((dbMaker, config) -> {
        if (config.valueCompress)
            dbMaker.serializer(new Serializer.CompressionWrapper<>(Serializer.STRING));
        else
            dbMaker.serializer(Serializer.STRING);
    });

    private Setting<DB.HTreeSetMaker> setting;

    private HTreeSetSetting(Setting<DB.HTreeSetMaker> setting) {
        this.setting = setting;
    }

    @Override
    public void set(DB.HTreeSetMaker dbMaker, TestMapDBConfig config) {
        this.setting.set(dbMaker, config);
    }
}

enum BTreeMapSetting implements Setting<DB.BTreeMapMaker> {
    GLOBAL((dbMaker, config) -> dbMaker.keySerializer(BTreeKeySerializer.STRING)),
    ENABLE_COUNTER((dbMaker, config) -> dbMaker.counterEnable()),
    NODES((dbMaker, config) -> dbMaker.nodeSize(config.btreeNodeSize)),
    OUTSIDE((dbMaker, config) -> dbMaker.valuesOutsideNodesEnable()),
    IGNORE_DUP((dbMaker, config) -> dbMaker.pumpIgnoreDuplicates()),
    VALUE_COMPRESS((dbMaker, config) -> dbMaker.valueSerializer(new Serializer.CompressionWrapper<>(Serializer.STRING)));

    private Setting<DB.BTreeMapMaker> setting;

    private BTreeMapSetting(Setting<DB.BTreeMapMaker> setting) {
        this.setting = setting;
    }

    @Override
    public void set(DB.BTreeMapMaker dbMaker, TestMapDBConfig config) {
        this.setting.set(dbMaker, config);
    }
}

enum BTreeSetSetting implements Setting<DB.BTreeSetMaker> {
    ENABLE_COUNTER((dbMaker, config) -> dbMaker.counterEnable()),
    NODES((dbMaker, config) -> dbMaker.nodeSize(config.btreeNodeSize)),
    IGNORE_DUP((dbMaker, config) -> dbMaker.pumpIgnoreDuplicates()),
    VALUE_COMPRESS((dbMaker, config) -> {
        dbMaker.serializer(BTreeKeySerializer.STRING);
    });

    private Setting<DB.BTreeSetMaker> setting;

    private BTreeSetSetting(Setting<DB.BTreeSetMaker> setting) {
        this.setting = setting;
    }

    @Override
    public void set(DB.BTreeSetMaker dbMaker, TestMapDBConfig config) {
        this.setting.set(dbMaker, config);
    }
}

