package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import brave.Tracing
import com.lsd.core.properties.LsdProperties
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.http.config.CONNECTION_TIMEOUT_MILLIS_DEFAULT
import io.lsdconsulting.lsd.distributed.http.repository.InterceptedDocumentHttpRepository
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.KafkaCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.KafkaHeaderRetriever
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import io.lsdconsulting.lsd.distributed.mongo.repository.DEFAULT_COLLECTION_SIZE_LIMIT_MBS
import io.lsdconsulting.lsd.distributed.mongo.repository.DEFAULT_TIMEOUT_MILLIS
import io.lsdconsulting.lsd.distributed.mongo.repository.InterceptedDocumentMongoRepository
import io.lsdconsulting.lsd.distributed.mongo.repository.InterceptedInteractionCollectionBuilder
import io.lsdconsulting.lsd.distributed.postgres.repository.InterceptedDocumentPostgresRepository
import lsd.format.json.createObjectMapper
import lsd.logging.log
import org.apache.kafka.clients.producer.ProducerInterceptor
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata

class LsdKafkaProducerInterceptor(private val kafkaCaptor: KafkaCaptor): ProducerInterceptor<String, Any> {

    constructor() : this(instance())

    override fun configure(configs: MutableMap<String, *>?) {}

    override fun onAcknowledgement(metadata: RecordMetadata?, exception: Exception?) {}

    override fun close() {}

    override fun onSend(record: ProducerRecord<String, Any>): ProducerRecord<String, Any> {
        log().debug("Intercepted record: {}", record)
        kafkaCaptor.capturePublishInteraction(record)
        return record
    }

    companion object {
        private fun instance(): KafkaCaptor {
            val connectionString = LsdProperties["lsd.dist.connectionString", ""]
            if (connectionString.isBlank()) throw IllegalArgumentException("Missing lsd.dist.connectionString")
            val appName = LsdProperties["info.app.name", ""]
            val sensitiveHeaders = LsdProperties["lsd.dist.obfuscator.sensitiveHeaders", ""]
            val profile = LsdProperties["spring.profiles.active", ""]
            val threadPoolSize:Int = LsdProperties.getInt("lsd.dist.threadPool.size", 16)

            val repository = buildInterceptedDocumentRepository(connectionString)
            val repositoryService = RepositoryService(threadPoolSize, repository)
            repositoryService.start()
            val traceIdRetriever = TraceIdRetriever(Tracing.newBuilder().build().tracer())
            val kafkaHeaderRetriever = KafkaHeaderRetriever(Obfuscator(sensitiveHeaders))
            return KafkaCaptor(repositoryService, PropertyServiceNameDeriver(appName), traceIdRetriever, kafkaHeaderRetriever, profile)
        }

        private fun buildInterceptedDocumentRepository(connectionString: String): InterceptedDocumentRepository {
            val repository: InterceptedDocumentRepository = when {
                connectionString.startsWith("jdbc:postgresql://") -> InterceptedDocumentPostgresRepository(
                    connectionString,
                    createObjectMapper()
                )

                connectionString.startsWith("mongodb://") -> InterceptedDocumentMongoRepository(
                    InterceptedInteractionCollectionBuilder(
                        connectionString,
                        null,
                        null,
                        LsdProperties.getInt("lsd.dist.db.connectionTimeout.millis", DEFAULT_TIMEOUT_MILLIS),
                        LsdProperties.getLong("lsd.dist.db.collectionSizeLimit.megabytes", DEFAULT_COLLECTION_SIZE_LIMIT_MBS)
                    )
                )

                connectionString.startsWith("http") -> InterceptedDocumentHttpRepository(
                    connectionString,
                    LsdProperties.getInt("lsd.dist.http.connectionTimeout.millis", CONNECTION_TIMEOUT_MILLIS_DEFAULT),
                    createObjectMapper()
                )

                else -> throw IllegalArgumentException("Wrong connectionString value!")
            }
            return repository
        }
    }
}
