package org.mds.harness2.tools.mongo;

import com.mongodb.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.data.authentication.UserCredentials;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.WriteResultChecking;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dongsong
 */
public class MongoConfig extends AbstractMongoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    Configuration configuration;

    public MongoConfig(Configuration configuration) {
        this.configuration = configuration;
    }

    public String getDatabaseName() {
        return this.configuration.databaseName;
    }

    public UserCredentials getUserCredentials() {
        UserCredentials credentials = null;
        if (StringUtils.isNotBlank(this.configuration.username) && StringUtils.isNotBlank(this.configuration.password)) {
            credentials = new UserCredentials(this.configuration.username, this.configuration.password);
        }
        return credentials;
    }

    @Override
    public SimpleMongoDbFactory mongoDbFactory() throws Exception {
        try {
            SimpleMongoDbFactory mongoDbFactory = (SimpleMongoDbFactory)super.mongoDbFactory();
            DB db = mongoDbFactory.getDb(this.getDatabaseName());
            db.getCollectionNames();
            return mongoDbFactory;
        } catch (Exception e) {
            log.error("", e);
            throw e;
        }
    }

    public static List<ServerAddress> getServerAddresses(String addressString) throws UnknownHostException {
        if (StringUtils.isBlank(addressString)) {
            throw new UnknownHostException("addressString cannot be empty");
        }
        List<ServerAddress> addresses = new ArrayList<ServerAddress>();
        String[] hosts = addressString.split(",");
        for (String host : hosts) {
            ServerAddress address = new ServerAddress(host);
            addresses.add(address);
        }
        return addresses;
    }

    @Override
    public MongoClient mongo() throws Exception {
        MongoClientOptions options = new MongoClientOptions.Builder()
                .autoConnectRetry(true)
                .connectionsPerHost(40)
                .description("MetaMore MongoClient")
                .readPreference(ReadPreference.secondaryPreferred())
                .threadsAllowedToBlockForConnectionMultiplier(1500)
                .build();

        List<ServerAddress> serverAddresses = getServerAddresses(this.configuration.replicaSet);

        MongoClient mongoClient = new MongoClient(serverAddresses, options);
        DB db = mongoClient.getDB(this.configuration.databaseName);

        if (StringUtils.isNotBlank(this.configuration.username) && StringUtils.isNotBlank(this.configuration.password)) {
            if (!db.authenticate(this.configuration.username, this.configuration.password.toCharArray())) {
                String errMsg = String.format("Authentication to database [%s] failed.  Tried %s/%s",
                        this.configuration.databaseName, this.configuration.username, this.configuration.password);
                throw new Exception(errMsg);
            }
        }

        return mongoClient;
    }

    @Override
    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate template = super.mongoTemplate();
        template.setWriteConcern(WriteConcern.NONE);
        template.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        return template;
    }
}
