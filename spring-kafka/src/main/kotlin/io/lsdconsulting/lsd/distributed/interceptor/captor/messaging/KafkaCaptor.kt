package io.lsdconsulting.lsd.distributed.interceptor.captor.messaging

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.PUBLISH
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.stringify
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver.Companion.SOURCE_NAME_KEY
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver.Companion.TARGET_NAME_KEY
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import lsd.format.printFlat
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.ZoneId
import java.time.ZonedDateTime

class KafkaCaptor(
    private val repositoryService: RepositoryService,
    private val propertyServiceNameDeriver: PropertyServiceNameDeriver,
    private val traceIdRetriever: TraceIdRetriever,
    private val messagingHeaderRetriever: KafkaHeaderRetriever,
    private val profile: String,
) {
//    fun capturePublishInteraction(record: ProducerRecord<String, Any>): InterceptedInteraction {
//        val headers = messagingHeaderRetriever.retrieve(record.headers())
//        val interceptedInteraction = InterceptedInteraction(
//            traceId = traceIdRetriever.getTraceId(headers),
//            body = printFlater.printFlat((record.value() as ByteArray).stringify()),
//            requestHeaders = headers,
//            responseHeaders = emptyMap(),
//            serviceName = propertyServiceNameDeriver.serviceName,
//            target = getSource(record),
//            path = propertyServiceNameDeriver.serviceName,
//            httpStatus = null,
//            httpMethod = null, interactionType = InteractionType.CONSUME,
//            profile = profile,
//            elapsedTime = 0L,
//            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
//        )
//        repositoryService.enqueue(interceptedInteraction)
//        return interceptedInteraction
//    }

//    private fun getSource(record: ProducerRecord<String, Any>): String {
//        var source = record.headers().headers(TARGET_NAME_KEY) as String?
//        if (source.isNullOrEmpty()) {
//            source = "UNKNOWN"
//        }
//        log().debug("found source:{}", source)
//        return source
//    }

    fun capturePublishInteraction(record: ProducerRecord<String, Any>): InterceptedInteraction {
        val source = record.headers().headers(SOURCE_NAME_KEY).firstOrNull()?.value()?.stringify()
        val target = record.headers().headers(TARGET_NAME_KEY).firstOrNull()?.value()?.stringify() ?: ""
        val headers = messagingHeaderRetriever.retrieve(record.headers())
        val interceptedInteraction = InterceptedInteraction(
            traceId = traceIdRetriever.getTraceId(headers),
            body = printFlat(record.value()),
            requestHeaders = headers,
            responseHeaders = emptyMap(),
            serviceName = source ?: propertyServiceNameDeriver.serviceName,
            target = target, path = target,
            httpStatus = null, httpMethod = null,
            interactionType = PUBLISH, profile = profile,
            elapsedTime = 0L, createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        repositoryService.enqueue(interceptedInteraction)
        return interceptedInteraction
    }
}
