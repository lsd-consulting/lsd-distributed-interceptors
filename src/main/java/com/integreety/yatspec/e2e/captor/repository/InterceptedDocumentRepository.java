package com.integreety.yatspec.e2e.captor.repository;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

/*
    We can consider providing other repositories (eg. InterceptedRelationalRepository) based on the connection string
*/
@Slf4j
public class InterceptedDocumentRepository {

    private static final String DATABASE_NAME = "goms"; // TODO Should we assume the db exists? Or should we attempt to create it if it doesn't?
    private static final String COLLECTION_NAME = "interceptedInteraction";

    private final MongoClient mongoClient;

    public InterceptedDocumentRepository(final String dbConnectionString) {
        final ConnectionString connString = new ConnectionString(dbConnectionString);
        mongoClient = MongoClients.create(MongoClientSettings.builder()
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

    public void save(final Document interceptedCall) {
        final MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        final MongoCollection<Document> interceptedCalls = database.getCollection(COLLECTION_NAME);
        interceptedCalls.insertOne(interceptedCall);
    }
}