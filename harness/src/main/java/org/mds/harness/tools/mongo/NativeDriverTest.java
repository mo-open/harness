package org.mds.harness.tools.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.mds.harness.common.perf.PerfConfig;
import org.mds.harness.common.perf.PerfTester;
import org.mds.harness.common.runner.RunnerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dongsong
 */
public class NativeDriverTest {
    protected final static Logger log = LoggerFactory.getLogger(NativeDriverTest.class);

    public void set1Run(Configuration configuration) throws Exception {
        final MongoConfig mongoConfig = new MongoConfig(configuration);
        final MongoClient mongoClient = mongoConfig.mongo();
        final DB db = mongoClient.getDB(configuration.databaseName);
        final DBCollection collection = db.getCollection("test");
        collection.drop();
        collection.createIndex(new BasicDBObject("count", 1));
        new PerfTester("Http Sync Perftest", configuration, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                BasicDBObject doc = new BasicDBObject("name", "MongoDB").
                        append("type", "database").
                        append("count", index).
                        append("info", new BasicDBObject("x", index + 10).append("y", 102));

                collection.insert(doc);
                return 1;
            }
        }).run();
    }

    public void set2Test(final Configuration conf) throws Exception {
        final MongoConfig mongoConfig = new MongoConfig(conf);
        final MongoClient mongoClient = mongoConfig.mongo();
        DB db = mongoClient.getDB(conf.databaseName);
        DBCollection collection = db.getCollection("test");
        collection.drop();
        collection.createIndex(new BasicDBObject("count", 1));
        new PerfTester("Http Sync Perftest", conf, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                DB db = mongoClient.getDB(conf.databaseName);
                DBCollection collection = db.getCollection("test");
                BasicDBObject doc = new BasicDBObject("name", "MongoDB").
                        append("type", "database").
                        append("count", index).
                        append("info", new BasicDBObject("x", index + 10).append("y", 102));

                collection.insert(doc);
                return 1;
            }
        }).run();
    }

    public void getTest(Configuration conf) throws Exception {
        final MongoConfig mongoConfig = new MongoConfig(conf);
        final MongoClient mongoClient = mongoConfig.mongo();
        final DB db = mongoClient.getDB(conf.databaseName);
        final DBCollection collection = db.getCollection("test");

        new PerfTester("Http Sync Perftest", conf, new PerfTester.Task() {
            @Override
            public int run(PerfConfig configuration, int index) {
                BasicDBObject doc = new BasicDBObject("count", index);
                collection.findOne(doc);
                return 1;
            }
        }).run();
    }

    public static void main(String args[]) throws Exception {
        RunnerHelper.run(args, NativeDriverTest.class, Configuration.class, "testMongo.conf");
    }
}
