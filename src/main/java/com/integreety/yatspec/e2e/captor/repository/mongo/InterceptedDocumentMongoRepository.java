package com.integreety.yatspec.e2e.captor.repository.mongo;

import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedInteraction;
import com.integreety.yatspec.e2e.captor.repository.mongo.codec.ZonedDateTimeCodec;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.connection.SslSettings;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.ssl.SSLContextBuilder;
import org.bson.codecs.configuration.CodecRegistry;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Indexes.ascending;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.bson.codecs.configuration.CodecRegistries.*;
import static org.bson.codecs.pojo.PojoCodecProvider.builder;

@Slf4j
public class InterceptedDocumentMongoRepository implements InterceptedDocumentRepository {

    public static final String DATABASE_NAME = "lsd";
    public static final String COLLECTION_NAME = "interceptedInteraction";

    public static final CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromCodecs(new ZonedDateTimeCodec()),
            fromProviders(builder().automatic(true).build()));


    private final MongoCollection<InterceptedInteraction> interceptedInteractions;

    public InterceptedDocumentMongoRepository(final String dbConnectionString,
                                              final String trustStoreLocation,
                                              final String trustStorePassword) {

        final MongoClient mongoClient = prepareMongoClient(dbConnectionString, trustStoreLocation, trustStorePassword);
        interceptedInteractions = prepareInterceptedInteractionCollection(mongoClient);
    }

    private MongoClient prepareMongoClient(final String dbConnectionString, final String trustStoreLocation, final String trustStorePassword) {
        final MongoClientSettings.Builder builder = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(dbConnectionString));

        if (!isBlank(trustStoreLocation) && !isBlank(trustStorePassword)) {
            builder.applyToSslSettings(sslSettingsBuilder -> loadCustomTrustStore(sslSettingsBuilder, trustStoreLocation, trustStorePassword));
        }

//    TODO We should also support other AuthenticationMechanisms
//    String user = "xxxx"; // the user name
//    String database = "admin"; // the name of the database in which the user is defined
//    char[] password = "xxxx".toCharArray(); // the password as a character array
//    MongoCredential credential = MongoCredential.createCredential(user, database, password);

        return MongoClients.create(builder
//            .credential(credential)
                .retryWrites(true)
                .build());
    }

    @SneakyThrows
    private static void loadCustomTrustStore(final SslSettings.Builder builder, final String trustStoreLocation,
                                             final String trustStorePassword) {
        try (final InputStream inputStream = new ClassPathResource(trustStoreLocation).getInputStream()) {
            final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(inputStream, trustStorePassword.toCharArray());
            builder.context(new SSLContextBuilder()
                    .loadTrustMaterial(
                            trustStore, null
                    ).build()
            );
        }
    }

    private MongoCollection<InterceptedInteraction> prepareInterceptedInteractionCollection(final MongoClient mongoClient) {
        final MongoCollection<InterceptedInteraction> interceptedInteractions;
        interceptedInteractions = mongoClient.getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME, InterceptedInteraction.class).withCodecRegistry(pojoCodecRegistry);
        interceptedInteractions.createIndex(ascending("traceId"));
        final IndexOptions indexOptions = new IndexOptions().expireAfter(14L, DAYS);
        interceptedInteractions.createIndex(ascending("createdAt"), indexOptions);
        return interceptedInteractions;
    }

    @Override
    public void save(final InterceptedInteraction interceptedInteraction) {
        try {
            interceptedInteractions.insertOne(interceptedInteraction);
        } catch (final MongoException e) {
            log.error("Skipping persisting the interceptedInteraction due to exception - interceptedInteraction:{}, message:{}, stackTrace:{}", interceptedInteraction, e.getMessage(), e.getStackTrace());
        }
    }

    @Override
    public List<InterceptedInteraction> findByTraceId(final String traceId) {
        final List<InterceptedInteraction> result = new ArrayList<>();
        try (final MongoCursor<InterceptedInteraction> cursor = interceptedInteractions.find(eq("traceId", traceId), InterceptedInteraction.class).iterator()) {
            while (cursor.hasNext()) {
                result.add(cursor.next());
            }
        } catch (final MongoException e) {
            // TODO Should we swallow this exception?
            log.error("Failed to retrieve interceptedInteractions - message:{}, stackTrace:{}", e.getMessage(), e.getStackTrace());
        }
        return result;
    }
}