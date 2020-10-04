package com.integreety.yatspec.e2e.captor.repository.mongo;

import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.captor.repository.model.InterceptedCall;
import com.integreety.yatspec.e2e.captor.repository.mongo.codec.ZonedDateTimeCodec;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.connection.SslSettings;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.ssl.SSLContextBuilder;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Slf4j
public class InterceptedDocumentMongoRepository implements InterceptedDocumentRepository {

    private static final String DATABASE_NAME = "lsd";
    private static final String COLLECTION_NAME = "interceptedInteraction";

    private final MongoClient mongoClient;

    private final CodecRegistry codecRegistry = CodecRegistries.fromCodecs(new ZonedDateTimeCodec());
    private final CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), codecRegistry,
            fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    public InterceptedDocumentMongoRepository(final String dbConnectionString,
                                              final String trustStoreLocation,
                                              final String trustStorePassword) {

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

        mongoClient = MongoClients.create(builder
//            .credential(credential)
                .retryWrites(true)
                .build());
    }

    @SneakyThrows
    private static void loadCustomTrustStore(final SslSettings.Builder builder, final String trustStoreLocation,
                                             final String trustStorePassword) {
        builder.context(new SSLContextBuilder()
                .loadTrustMaterial(
                        new File(trustStoreLocation), trustStorePassword.toCharArray()
                ).build()
        ).build();
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