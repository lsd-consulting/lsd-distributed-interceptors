package io.lsdconsulting.lsd.distributed.interceptor.config

import brave.Tracer
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.derive.SourceTargetDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
open class CommonLibraryConfig {
    @Bean
    open fun propertyServiceNameDeriver(@Value("\${info.app.name}") appName: String) =
        PropertyServiceNameDeriver(appName)

    @Bean
    open fun sourceTargetDeriver(propertyServiceNameDeriver: PropertyServiceNameDeriver) =
        SourceTargetDeriver(propertyServiceNameDeriver)

    @Bean
    open fun obfuscator(@Value("\${lsd.dist.obfuscator.sensitiveHeaders:#{null}}") sensitiveHeaders: String) =
        Obfuscator(sensitiveHeaders)

    @Bean
    open fun traceIdRetriever(tracer: Tracer) = TraceIdRetriever(tracer)

    // TODO Add test for these conditions
    @Bean
    @ConditionalOnMissingClass("io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository")
    @ConditionalOnExpression("#{!'\${lsd.dist.connectionString:}'.startsWith('mongodb://') and !'\${lsd.dist.connectionString:}'.startsWith('http')}")
    open fun interceptedDocumentRepository(@Value("\${lsd.dist.connectionString}") connectionString: String): InterceptedDocumentRepository {
        throw IllegalArgumentException("Wrong connection string: $connectionString. Make sure it start with http(s):// or mongodb://")
    }

    @Bean
    open fun queueService(
        @Value("\${lsd.dist.threadPool.size:16}") queueLength: Int,
        interceptedDocumentRepository: InterceptedDocumentRepository
    ) = RepositoryService(queueLength, interceptedDocumentRepository)
}
