package io.lsdconsulting.lsd.distributed.interceptor.config

import brave.Tracer
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
open class CommonLibraryConfig(
    private val tracer: Tracer
) {
    @Bean
    open fun propertyServiceNameDeriver(@Value("\${info.app.name}") appName: String) =
        PropertyServiceNameDeriver(appName)

    @Bean
    open fun obfuscator(@Value("\${lsd.dist.obfuscator.sensitiveHeaders:#{null}}") sensitiveHeaders: String) =
        Obfuscator(sensitiveHeaders)

    @Bean
    open fun traceIdRetriever() = TraceIdRetriever(tracer)

    @Bean
    open fun queueService(
        @Value("\${lsd.dist.threadPool.size:16}") queueLength: Int,
        interceptedDocumentRepository: InterceptedDocumentRepository
    ) = RepositoryService(queueLength, interceptedDocumentRepository)
}
