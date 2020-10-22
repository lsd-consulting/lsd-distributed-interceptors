package com.integreety.yatspec.e2e.integration.testapp.repository;

import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.captor.repository.mongo.codec.ZonedDateTimeCodec;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static de.flapdoodle.embed.mongo.distribution.Version.Main.PRODUCTION;
import static de.flapdoodle.embed.process.runtime.Network.localhostIsIPv6;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Slf4j
@Component
public class TestRepository {
    private static final String MONGODB_HOST = "localhost";
    private static final int MONGODB_PORT = 27017;
    private static final String DATABASE_NAME = "lsd";
    private static final String COLLECTION_NAME = "interceptedInteraction";

    private final CodecRegistry codecRegistry = CodecRegistries.fromCodecs(new ZonedDateTimeCodec());
    private final CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), codecRegistry,
            fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    private static MongodExecutable mongodExecutable;
    private static MongoClient mongoClient;

    static {
        final MongodStarter starter = MongodStarter.getDefaultInstance();

        final IMongodConfig mongodConfig;
        try {
            mongodConfig = new MongodConfigBuilder()
                    .version(PRODUCTION)
                    .net(new Net(MONGODB_HOST, MONGODB_PORT, localhostIsIPv6()))
                    .build();

            mongodExecutable = starter.prepare(mongodConfig);
            mongodExecutable.start();

            final MongoClientSettings.Builder builder = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString("mongodb://" + MONGODB_HOST + ":" + MONGODB_PORT + ""));

            mongoClient = MongoClients.create(builder

                    .retryWrites(true)
                    .build());
        } catch (final IOException e) {
            e.printStackTrace();
        }
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
