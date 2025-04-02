package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import brave.Tracing
import com.lsd.core.properties.LsdProperties
import feign.Request
import feign.Response
import feign.slf4j.Slf4jLogger
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.http.config.CONNECTION_TIMEOUT_MILLIS_DEFAULT
import io.lsdconsulting.lsd.distributed.http.repository.InterceptedDocumentHttpRepository
import io.lsdconsulting.lsd.distributed.interceptor.captor.FeignHttpHeaderRetriever
import io.lsdconsulting.lsd.distributed.interceptor.captor.FeignRequestCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.FeignResponseCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.print
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver
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
import java.io.IOException

class LsdFeignLoggerInterceptor(
    private val feignRequestCaptor: FeignRequestCaptor,
    private val feignResponseCaptor: FeignResponseCaptor
) :
    Slf4jLogger(LsdFeignLoggerInterceptor::class.java) {

    public override fun logRequest(configKey: String, level: Level, request: Request) {
        super.logRequest(configKey, level, request)
        try {
            feignRequestCaptor.captureRequestInteraction(request)
        } catch (t: Throwable) {
            log().error(t.message, t)
        }
    }

    @Throws(IOException::class)
    public override fun logAndRebufferResponse(
        configKey: String,
        logLevel: Level,
        response: Response,
        elapsedTime: Long
    ): Response {
        val body = if (response.body() != null) print(response.body().asInputStream()) else null
        val convertedResponse = resetBodyData(response, body?.toByteArray()) ?: response
        super.logAndRebufferResponse(configKey, logLevel, convertedResponse, elapsedTime)
        try {
            feignResponseCaptor.captureResponseInteraction(convertedResponse, body, elapsedTime)
        } catch (t: Throwable) {
            log().error(t.message, t)
            return convertedResponse
        }
        return convertedResponse
    }

    private fun resetBodyData(response: Response, bodyData: ByteArray?) =
        response.toBuilder().body(bodyData).build()


    companion object {
        fun instance(): LsdFeignLoggerInterceptor? {
            val connectionString = LsdProperties["lsd.dist.connectionString", ""]
            if (connectionString.isBlank()) return null
            val appName = LsdProperties["info.app.name", ""]
            val sensitiveHeaders = LsdProperties["lsd.dist.obfuscator.sensitiveHeaders", ""]
            val profile = LsdProperties["spring.profiles.active", ""]
            val threadPoolSize: Int = LsdProperties.getInt("lsd.dist.threadPool.size", 16)

            val repository = buildInterceptedDocumentRepository(connectionString)
            val repositoryService = RepositoryService(threadPoolSize, repository)
            repositoryService.start()
            val sourceTargetDeriver = SourceTargetDeriver(PropertyServiceNameDeriver(appName))
            val traceIdRetriever = TraceIdRetriever(BraveTracer(Tracing.newBuilder().build().tracer(), CurrentTraceContext.NOOP))
            val feignHttpHeaderRetriever = FeignHttpHeaderRetriever(Obfuscator(sensitiveHeaders))
            val feignRequestCaptor = FeignRequestCaptor(
                repositoryService,
                sourceTargetDeriver,
                traceIdRetriever,
                feignHttpHeaderRetriever,
                profile
            )
            val feignResponseCaptor = FeignResponseCaptor(
                repositoryService,
                sourceTargetDeriver,
                traceIdRetriever,
                feignHttpHeaderRetriever,
                profile
            )
            return LsdFeignLoggerInterceptor(feignRequestCaptor, feignResponseCaptor)
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
