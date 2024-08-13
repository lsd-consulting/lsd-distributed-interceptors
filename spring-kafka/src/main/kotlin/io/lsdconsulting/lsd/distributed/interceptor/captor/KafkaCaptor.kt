package io.lsdconsulting.lsd.distributed.interceptor.captor

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.PUBLISH
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.print
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver.Companion.SOURCE_NAME_KEY
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver.Companion.TARGET_NAME_KEY
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import lsd.format.json.objectMapper
import lsd.logging.log
import org.apache.avro.AvroRuntimeException
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.ZoneId
import java.time.ZonedDateTime

class KafkaCaptor(
    private val repositoryService: RepositoryService,
    private val propertyServiceNameDeriver: PropertyServiceNameDeriver,
    private val traceIdRetriever: TraceIdRetriever,
    private val kafkaHeaderRetriever: KafkaHeaderRetriever,
    private val profile: String,
) {
    fun captureConsumeInteraction(records: ConsumerRecords<String, Any>): List<InterceptedInteraction> {
        return records.map { record ->
            val headers = kafkaHeaderRetriever.retrieve(record.headers())
            val interceptedInteraction = InterceptedInteraction(
                traceId = traceIdRetriever.getTraceId(headers),
                body = print(record.value()) { obj ->
                    serialiseWithAvro(obj)
                },
                requestHeaders = headers,
                responseHeaders = emptyMap(),
                serviceName = propertyServiceNameDeriver.serviceName,
                target = getTarget(record),
                path = getTarget(record),
                httpStatus = null,
                httpMethod = null, interactionType = InteractionType.CONSUME,
                profile = profile,
                elapsedTime = 0L,
                createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
            )
            repositoryService.enqueue(interceptedInteraction)
            interceptedInteraction
        }
    }

    private fun getTarget(record: ConsumerRecord<String, Any>): String {
        var source = print(record.headers().headers(TARGET_NAME_KEY).firstOrNull()?.value())
        if (source.isEmpty()) {
            source = record.topic()
        }
        log().debug("found source:{}", source)
        return source
    }

    fun capturePublishInteraction(record: ProducerRecord<String, Any>): InterceptedInteraction {
        val source = print(record.headers().headers(SOURCE_NAME_KEY).firstOrNull()?.value())
        val target = print(record.headers().headers(TARGET_NAME_KEY).firstOrNull()?.value())
        val headers = kafkaHeaderRetriever.retrieve(record.headers())
        val interceptedInteraction = InterceptedInteraction(
            traceId = traceIdRetriever.getTraceId(headers),
            body = print(record.value()) { obj ->
                serialiseWithAvro(obj)
            },
            requestHeaders = headers,
            responseHeaders = emptyMap(),
            serviceName = source.ifBlank { propertyServiceNameDeriver.serviceName },
            target = target, path = target,
            httpStatus = null, httpMethod = null,
            interactionType = PUBLISH, profile = profile,
            elapsedTime = 0L, createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        repositoryService.enqueue(interceptedInteraction)
        return interceptedInteraction
    }

    private fun serialiseWithAvro(obj: Any) = try {
        objectMapper.writeValueAsString(obj)
    } catch (e: InvalidDefinitionException) {
        obj.toString()
    } catch (e: JsonMappingException) {
        if (e.cause is AvroRuntimeException) {
            obj.toString()
        } else {
            throw e
        }
    }
}
