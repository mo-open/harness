package org.mds.harness.tools.mongo;

import com.mongodb.*;
import org.mds.harness.common.perf.PerfConfig;
import org.mds.harness.common.perf.PerfTester;
import org.mds.harness.common.runner.RunnerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author Dongsong
 */
public class NativeDriverTest {
    protected final static Logger log = LoggerFactory.getLogger(NativeDriverTest.class);

    private static Random random = new Random();

    public void runSet1(final Configuration configuration) throws Exception {
        final MongoConfig mongoConfig = new MongoConfig(configuration);
        final MongoClient mongoClient = mongoConfig.mongo();
        final DB db = mongoClient.getDB(configuration.databaseName);
        final DBCollection collection = db.getCollection("test");
        if (configuration.dropFirst) {
            collection.drop();
            collection.createIndex(new BasicDBObject("type", 1).append("dateTime", 1));
            collection.createIndex(new BasicDBObject("dateTime", 1),
                    new BasicDBObject("expireAfterSecs", configuration.expireTime));
        }
        final WriteConcern writeConcern = new WriteConcern(configuration.writeMode);
        new PerfTester("Mongo test set1", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig config, int index) {
                String type = "type_" + random.nextInt(configuration.dataTypeCount);
                String item = type + "_dataItem_random_custom_" + random.nextInt(configuration.dataItemCount);
                BasicDBObject doc = new BasicDBObject("type", type).
                        append("item", item).
                        append("itemHash", Math.abs(item.hashCode())).
                        append("dateTime", new Date()).
                        append("info", "Information");

                collection.insert(doc, writeConcern);
                return 1;
            }
        }).run();
    }

    public void runSet2(final Configuration configuration) throws Exception {
        final MongoConfig mongoConfig = new MongoConfig(configuration);
        final MongoClient mongoClient = mongoConfig.mongo();
        DB db = mongoClient.getDB(configuration.databaseName);
        final DBCollection collection = db.getCollection("test");

        if (configuration.dropFirst) {
            collection.drop();
            collection.createIndex(new BasicDBObject("type", 1).append("dateTime", 1));
            collection.createIndex(new BasicDBObject("dateTime", 1),
                    new BasicDBObject("expireAfterSecs", configuration.expireTime));
        }
        final WriteConcern writeConcern = new WriteConcern(configuration.writeMode);
        new PerfTester("Mongo multiple set", configuration, new PerfTester.BatchTask() {

            @Override
            public int run(PerfConfig config, List<Long> indexes) {
                List<DBObject> docs = new ArrayList();
                for (Long index : indexes) {
                    String type = "type_" + random.nextInt(configuration.dataTypeCount);
                    String item = type + "_dataItem_random_custom_" + random.nextInt(configuration.dataItemCount);
                    BasicDBObject doc = new BasicDBObject("type", type).
                            append("item", item).
                            append("itemHash", Math.abs(item.hashCode())).
                            append("dateTime", new Date()).
                            append("info", "Information");
                    docs.add(doc);
                }
                collection.insert(docs, writeConcern);
                return docs.size();
            }
        }).run();
    }

    public void runGet(final Configuration configuration) throws Exception {
        final MongoConfig mongoConfig = new MongoConfig(configuration);
        final MongoClient mongoClient = mongoConfig.mongo();
        final DB db = mongoClient.getDB(configuration.databaseName);
        final DBCollection collection = db.getCollection("test");

        new PerfTester("Mongo query", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig conf, int index) {
                String type = "type_" + random.nextInt(configuration.dataTypeCount);

                BasicDBObject doc = new BasicDBObject("type", type)
                        .append("itemHash", new BasicDBObject("$mod", new Object[]{configuration.groupCount, random.nextInt(configuration.groupCount)}))
                        .append("dateTime", new BasicDBObject("$gt", System.currentTimeMillis() - configuration.dataDuration));
                DBCursor cursor = collection.find(doc);
                return 1;
            }
        }).run();
    }

    public static void main(String args[]) throws Exception {
        RunnerHelper.run(args, NativeDriverTest.class, Configuration.class, "testMongo.conf");
    }
}
