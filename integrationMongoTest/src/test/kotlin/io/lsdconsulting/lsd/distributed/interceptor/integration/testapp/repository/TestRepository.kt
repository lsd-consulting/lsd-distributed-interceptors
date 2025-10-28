package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.repository

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoException
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.model.Filters
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker.ReachedState
import de.flapdoodle.reverse.transitions.Start
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.mongo.repository.codec.ZonedDateTimeCodec
import lsd.logging.log
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

    fun findAll(traceId: String): List<InterceptedInteraction> {
        log().info("Retrieving interceptedInteractions for traceId:{}", traceId)
        val database = mongoClient.getDatabase(DATABASE_NAME)
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
        private lateinit var mongoClient: MongoClient
        private lateinit var mongodExecutable: ReachedState<RunningMongodProcess>
        fun setupDatabase() {
            try {
                val mongod = Mongod.builder()
                    .net(
                        Start.to(Net::class.java).initializedWith(Net.defaults().withPort(MONGODB_PORT))
                    ).build()
                mongodExecutable = mongod.start(Version.Main.V5_0)
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
            mongoClient.close()
            runCatching {
                mongodExecutable.close()
            }.onFailure { println("Exception caught while tearing down database (might already be down): ${it.message}") }

        }
    }
}
