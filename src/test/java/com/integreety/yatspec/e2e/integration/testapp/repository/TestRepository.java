package com.integreety.yatspec.e2e.integration.testapp.repository;

import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.captor.repository.mongo.codec.ZonedDateTimeCodec;
import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.MongoClientSettings.builder;
import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static de.flapdoodle.embed.mongo.distribution.Version.Main.PRODUCTION;
import static de.flapdoodle.embed.process.runtime.Network.localhostIsIPv6;
import static org.bson.codecs.configuration.CodecRegistries.*;

@Slf4j
public class TestRepository {
    public static final String MONGODB_HOST = "localhost";
    public static final int MONGODB_PORT = 27017;

    private static final String DATABASE_NAME = "lsd";
    private static final String COLLECTION_NAME = "interceptedInteraction";

    private static MongoClient mongoClient;
    private static MongodExecutable mongodExecutable;

    private final CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromCodecs(new ZonedDateTimeCodec()),
            fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    public static void setupDatabase() {
        try {
            final IMongodConfig mongodConfig = new MongodConfigBuilder()
                    .version(PRODUCTION)
                    .net(new Net(MONGODB_HOST, MONGODB_PORT, localhostIsIPv6()))
                    .build();

            mongodExecutable = MongodStarter.getDefaultInstance().prepare(mongodConfig);
            mongodExecutable.start();


            mongoClient = MongoClients.create(builder()
                    .applyConnectionString(new ConnectionString("mongodb://" + MONGODB_HOST + ":" + MONGODB_PORT))
                    .retryWrites(true)
                    .build());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void tearDownDatabase() {
        mongoClient.close();
        mongodExecutable.stop();
    }

    public MongoCollection<Document> getCollection() {
        final MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        return database.getCollection(COLLECTION_NAME).withCodecRegistry(pojoCodecRegistry);
    }

    public List<InterceptedCall> findAll(final String traceId) {
        log.info("Retrieving interceptedCalls for traceId:{}", traceId);

        final MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        final MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME).withCodecRegistry(pojoCodecRegistry);

        final List<InterceptedCall> result = new ArrayList<>();
        try (final MongoCursor<InterceptedCall> cursor = collection.find(eq("traceId", traceId), InterceptedCall.class).iterator()) {
            while (cursor.hasNext()) {
                final InterceptedCall interceptedCall = cursor.next();
                log.info("Retrieved interceptedCall:{}", interceptedCall);
                result.add(interceptedCall);
            }
        } catch (final MongoException e) {
            log.error("Failed to retrieve interceptedCalls - message:{}, stackTrace:{}", e.getMessage(), e.getStackTrace());
        }
        return result;
    }
}
