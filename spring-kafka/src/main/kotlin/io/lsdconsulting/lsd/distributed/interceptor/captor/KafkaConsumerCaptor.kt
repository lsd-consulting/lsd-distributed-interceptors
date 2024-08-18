package io.lsdconsulting.lsd.distributed.interceptor.captor

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.print
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver.Companion.TARGET_NAME_KEY
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import lsd.format.json.objectMapper
import lsd.logging.log
import org.apache.avro.AvroRuntimeException
import org.apache.kafka.clients.consumer.ConsumerRecords
import java.time.ZoneId
import java.time.ZonedDateTime

class KafkaConsumerCaptor(
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
                target = getTarget(headers, record.topic()),
                path = propertyServiceNameDeriver.serviceName,
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

    private fun getTarget(headers: Map<String, Collection<String>>, topic: String): String {
        var target = if (!headers[TARGET_NAME_KEY].isNullOrEmpty()) {
            val targetHeader = print(headers[TARGET_NAME_KEY]?.toList()?.first())
            print(targetHeader)
        } else null
        if (target.isNullOrEmpty()) {
            if (!headers["__TypeId__"].isNullOrEmpty()) {
                val typeIdHeader = print(headers["__TypeId__"]?.toList()?.first())
                target = getTargetFrom(typeIdHeader)
            }
        }
        if (target.isNullOrEmpty()) {
            target = topic
        }
        log().debug("found target:{}", target)
        return target
    }

    private fun getTargetFrom(typeIdHeader: String): String? {
        return if (typeIdHeader.isNotBlank()) {
            return typeIdHeader.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray<String>().last()
        }
        else null
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
