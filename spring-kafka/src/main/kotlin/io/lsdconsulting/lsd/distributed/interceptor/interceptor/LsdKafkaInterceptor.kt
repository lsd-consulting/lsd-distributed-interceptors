package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import brave.Tracing
import com.lsd.core.properties.LsdProperties
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.http.config.CONNECTION_TIMEOUT_MILLIS_DEFAULT
import io.lsdconsulting.lsd.distributed.http.repository.InterceptedDocumentHttpRepository
import io.lsdconsulting.lsd.distributed.interceptor.captor.KafkaConsumerCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.KafkaHeaderRetriever
import io.lsdconsulting.lsd.distributed.interceptor.captor.KafkaProducerCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import io.lsdconsulting.lsd.distributed.mongo.repository.DEFAULT_COLLECTION_SIZE_LIMIT_MBS
import io.lsdconsulting.lsd.distributed.mongo.repository.DEFAULT_TIMEOUT_MILLIS
import io.lsdconsulting.lsd.distributed.mongo.repository.InterceptedDocumentMongoRepository
import io.lsdconsulting.lsd.distributed.mongo.repository.InterceptedInteractionCollectionBuilder
import io.lsdconsulting.lsd.distributed.postgres.repository.InterceptedDocumentPostgresRepository
import io.micrometer.tracing.CurrentTraceContext
import io.micrometer.tracing.brave.bridge.BraveTracer
import lsd.format.json.createObjectMapper
import lsd.logging.log
import org.apache.kafka.clients.consumer.ConsumerInterceptor
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.producer.ProducerInterceptor
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition

class LsdKafkaInterceptor : ProducerInterceptor<String, Any>, ConsumerInterceptor<String, Any> {

    private var kafkaProducerCaptors: Pair<KafkaProducerCaptor, KafkaConsumerCaptor> = instance()

    override fun configure(configs: MutableMap<String, *>?) {}

    override fun onAcknowledgement(metadata: RecordMetadata?, exception: Exception?) {}

    override fun close() {}

    override fun onCommit(p0: MutableMap<TopicPartition, OffsetAndMetadata>?) {}

    override fun onConsume(records: ConsumerRecords<String, Any>): ConsumerRecords<String, Any> {
        log().info("Intercepted consumed records: {}", records)
        kafkaProducerCaptors.second.captureConsumeInteraction(records)
        return records
    }

    override fun onSend(record: ProducerRecord<String, Any>): ProducerRecord<String, Any> {
        log().debug("Intercepted record: {}", record)
        kafkaProducerCaptors.first.capturePublishInteraction(record)
        return record
    }

    companion object {
        private fun instance(): Pair<KafkaProducerCaptor, KafkaConsumerCaptor> {
            val connectionString = LsdProperties["lsd.dist.connectionString", ""]
            if (connectionString.isBlank()) throw IllegalArgumentException("Missing lsd.dist.connectionString")
            val appName = LsdProperties["info.app.name", ""]
            val sensitiveHeaders = LsdProperties["lsd.dist.obfuscator.sensitiveHeaders", ""]
            val profile = LsdProperties["spring.profiles.active", ""]
            val threadPoolSize: Int = LsdProperties.getInt("lsd.dist.threadPool.size", 16)

            val repository = buildInterceptedDocumentRepository(connectionString)
            val repositoryService = RepositoryService(threadPoolSize, repository)
            repositoryService.start()
            val traceIdRetriever =
                TraceIdRetriever(BraveTracer(Tracing.newBuilder().build().tracer(), CurrentTraceContext.NOOP))
            val kafkaHeaderRetriever = KafkaHeaderRetriever(Obfuscator(sensitiveHeaders))
            val kafkaProducerCaptor = KafkaProducerCaptor(
                repositoryService,
                PropertyServiceNameDeriver(appName),
                traceIdRetriever,
                kafkaHeaderRetriever,
                profile
            )
            val kafkaConsumerCaptor = KafkaConsumerCaptor(
                repositoryService,
                PropertyServiceNameDeriver(appName),
                traceIdRetriever,
                kafkaHeaderRetriever,
                profile
            )
            return kafkaProducerCaptor to kafkaConsumerCaptor
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
                        LsdProperties.getLong(
                            "lsd.dist.db.collectionSizeLimit.megabytes",
                            DEFAULT_COLLECTION_SIZE_LIMIT_MBS
                        )
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
