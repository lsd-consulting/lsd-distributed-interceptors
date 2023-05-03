package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.repository;

import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import io.lsdconsulting.lsd.distributed.access.model.InteractionType;
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.mongo.repository.codec.ZonedDateTimeCodec;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static com.mongodb.MongoClientSettings.builder;
import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static de.flapdoodle.embed.mongo.distribution.Version.Main.V5_0;
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

    private final CodecRegistry pojoCodecRegistry = fromRegistries(
            getDefaultCodecRegistry(),
            fromCodecs(new ZonedDateTimeCodec()),
            fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    public static void setupDatabase() {
        try {
            final MongodConfig mongodConfig = MongodConfig.builder()
                    .version(V5_0)
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

//    public MongoCollection<Document> getCollection() {
//        final MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
//        return database.getCollection(COLLECTION_NAME).withCodecRegistry(pojoCodecRegistry);
//    }

    public List<InterceptedInteraction> findAll(final String traceId) {
        log.info("Retrieving interceptedInteractions for traceId:{}", traceId);

        final MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        final MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME).withCodecRegistry(pojoCodecRegistry);

        final List<InterceptedInteraction> result = new ArrayList<>();
        try (final MongoCursor<Document> cursor = collection.find(eq("traceId", traceId), Document.class).iterator()) {
            while (cursor.hasNext()) {
                final Document document = cursor.next();
                log.info("Retrieved interceptedInteraction:{}", document);
                var requestHeaders = new LinkedHashMap<String, Collection<String>>();
                var responseHeaders = new LinkedHashMap<String, Collection<String>>();
                document.get("requestHeaders", Document.class).forEach((key, value) -> requestHeaders.put(key, (Collection<String>) value));
                document.get("responseHeaders", Document.class).forEach((key, value) -> responseHeaders.put(key, (Collection<String>) value));
                var interceptedInteraction = new InterceptedInteraction(
                        document.getString("traceId"),
                        document.getString("body"),
                        requestHeaders,
                        responseHeaders,
                        document.getString("serviceName"),
                        document.getString("target"),
                        document.getString("path"),
                        document.getString("httpStatus"),
                        document.getString("httpMethod"),
                        InteractionType.valueOf(document.getString("interactionType")),
                        document.getString("profile"),
                        document.getLong("elapsedTime"),
                        ZonedDateTime.ofInstant(document.get("createdAt", Date.class).toInstant(), ZoneId.of("UTC"))
                );
                result.add(interceptedInteraction);
            }
        } catch (final MongoException e) {
            log.error("Failed to retrieve interceptedInteractions - message:{}, stackTrace:{}", e.getMessage(), e.getStackTrace());
        }
        return result;
    }
}
