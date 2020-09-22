package com.integreety.yatspec.e2e.captor.repository.mongo;

import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;
import java.util.List;

import static com.integreety.yatspec.e2e.captor.repository.mongo.MongoClientCreator.COLLECTION_NAME;
import static com.integreety.yatspec.e2e.captor.repository.mongo.MongoClientCreator.DATABASE_NAME;
import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Slf4j
public class InterceptedDocumentMongoRepository implements InterceptedDocumentRepository {

    private final MongoClient mongoClient;

    private final CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    public InterceptedDocumentMongoRepository(final String dbConnectionString) {
        mongoClient = MongoClientCreator.getMongoClient(new ConnectionString(dbConnectionString));
    }

    @Override
    public void save(final InterceptedCall interceptedCall) {
        final MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        final MongoCollection<InterceptedCall> interceptedCalls = database.getCollection(COLLECTION_NAME, InterceptedCall.class).withCodecRegistry(pojoCodecRegistry);
        interceptedCalls.insertOne(interceptedCall);
    }

    @Override
    public List<InterceptedCall> findByTraceId(final String traceId) {
        final MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        final MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME).withCodecRegistry(pojoCodecRegistry);

        final List<InterceptedCall> result = new ArrayList<>();
        try (final MongoCursor<InterceptedCall> cursor = collection.find(eq("traceId", traceId), InterceptedCall.class).iterator()) {
            while (cursor.hasNext()) {
                result.add(cursor.next());
            }
        }
        return result;
    }
}