package com.integreety.yatspec.e2e.captor.repository;

import com.integreety.yatspec.e2e.captor.repository.mongo.MongoClientCreator;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;

import static com.integreety.yatspec.e2e.captor.repository.mongo.MongoClientCreator.COLLECTION_NAME;
import static com.integreety.yatspec.e2e.captor.repository.mongo.MongoClientCreator.DATABASE_NAME;

/*
    We can consider providing other repositories (eg. InterceptedRelationalRepository) based on the connection string
*/
@Slf4j
public class InterceptedDocumentRepository {

    private final MongoClient mongoClient;

    public InterceptedDocumentRepository(final String dbConnectionString) {
        mongoClient = MongoClientCreator.getMongoClient(new ConnectionString(dbConnectionString));
    }

    public void save(final Document interceptedCall) {
        final MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        final MongoCollection<Document> interceptedCalls = database.getCollection(COLLECTION_NAME);
        interceptedCalls.insertOne(interceptedCall);
    }
}