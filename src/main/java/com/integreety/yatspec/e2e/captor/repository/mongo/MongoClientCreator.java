package com.integreety.yatspec.e2e.captor.repository.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class MongoClientCreator {

    public static final String DATABASE_NAME = "lsd";
    public static final String COLLECTION_NAME = "interceptedInteraction";

    public static MongoClient getMongoClient(ConnectionString connString) {
        return MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(connString)
//            .credential(credential)
                .retryWrites(true)
                .build());
    }

//    String user = "xxxx"; // the user name
//    String database = "admin"; // the name of the database in which the user is defined
//    char[] password = "xxxx".toCharArray(); // the password as a character array
//    MongoCredential credential = MongoCredential.createCredential(user, database, password);
//    MongoClient mongoClient = new MongoClient(new ServerAddress("xxx", 27017),
//    Arrays.asList(credential));
}
