package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.repository

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoException
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.model.Filters
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import io.lsdconsulting.lsd.distributed.access.model.InteractionType
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.config.log
import io.lsdconsulting.lsd.distributed.mongo.repository.codec.ZonedDateTimeCodec
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import java.io.IOException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class TestRepository {
    private val pojoCodecRegistry = CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromCodecs(ZonedDateTimeCodec()),
        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
    )

    //    public MongoCollection<Document> getCollection() {
    //        final MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
    //        return database.getCollection(COLLECTION_NAME).withCodecRegistry(pojoCodecRegistry);
    //    }
    fun findAll(traceId: String): List<InterceptedInteraction> {
        log().info("Retrieving interceptedInteractions for traceId:{}", traceId)
        val database = mongoClient!!.getDatabase(DATABASE_NAME)
        val collection = database.getCollection(COLLECTION_NAME).withCodecRegistry(pojoCodecRegistry)
        val result: MutableList<InterceptedInteraction> = ArrayList()
        try {
            collection.find(Filters.eq("traceId", traceId), Document::class.java).iterator()
                .use { cursor ->
                    while (cursor.hasNext()) {
                        val document = cursor.next()
                        log().info("Retrieved interceptedInteraction:{}", document)
                        val requestHeaders = LinkedHashMap<String, Collection<String>>()
                        val responseHeaders = LinkedHashMap<String, Collection<String>>()
                        document.get("requestHeaders", Document::class.java)
                            .forEach { key: String, value: Any -> requestHeaders[key] = value as Collection<String> }
                        document.get("responseHeaders", Document::class.java)
                            .forEach { key: String, value: Any -> responseHeaders[key] = value as Collection<String> }
                        val interceptedInteraction = InterceptedInteraction(
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
                            ZonedDateTime.ofInstant(
                                document.get("createdAt", Date::class.java).toInstant(),
                                ZoneId.of("UTC")
                            )
                        )
                        result.add(interceptedInteraction)
                    }
                }
        } catch (e: MongoException) {
            log().error(
                "Failed to retrieve interceptedInteractions - message:{}, stackTrace:{}",
                e.message,
                e.stackTrace
            )
        }
        return result
    }

    companion object {
        const val MONGODB_HOST = "localhost"
        const val MONGODB_PORT = 27017
        private const val DATABASE_NAME = "lsd"
        private const val COLLECTION_NAME = "interceptedInteraction"
        private var mongoClient: MongoClient? = null
        private var mongodExecutable: MongodExecutable? = null
        fun setupDatabase() {
            try {
                val mongodConfig: MongodConfig = MongodConfig.builder()
                    .version(Version.Main.V5_0)
                    .net(Net(MONGODB_HOST, MONGODB_PORT, Network.localhostIsIPv6()))
                    .build()
                mongodExecutable = MongodStarter.getDefaultInstance().prepare(mongodConfig)
                mongodExecutable!!.start()
                mongoClient = MongoClients.create(
                    MongoClientSettings.builder()
                        .applyConnectionString(ConnectionString("mongodb://" + MONGODB_HOST + ":" + MONGODB_PORT))
                        .retryWrites(true)
                        .build()
                )
            } catch (e: IOException) {
                log().error(e.message, e)
            }
        }

        fun tearDownDatabase() {
            mongoClient!!.close()
            mongodExecutable!!.stop()
        }
    }
}
