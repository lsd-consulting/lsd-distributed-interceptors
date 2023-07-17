package io.lsdconsulting.lsd.distributed.interceptor.config

import brave.Tracer
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.http.SourceTargetDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
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
    open fun obfuscator(@Value("\${lsd.dist.obfuscator.sensitiveHeaders:#{null}}") sensitiveHeaders: String?) =
        Obfuscator(sensitiveHeaders)

    @Bean
    open fun traceIdRetriever(tracer: Tracer) = TraceIdRetriever(tracer)

    // TODO Add test for these conditions
    @Bean
    @ConditionalOnExpression("#{!'\${lsd.dist.connectionString:}'.startsWith('mongodb://') " +
            "and !'\${lsd.dist.connectionString:}'.startsWith('http') " +
            "and !'\${lsd.dist.connectionString:}'.startsWith('jdbc:postgresql://') " +
            "and !'\${lsd.dist.connectionString:}'.startsWith('dataSource')}")
    open fun interceptedDocumentRepository(@Value("\${lsd.dist.connectionString}") connectionString: String): InterceptedDocumentRepository {
        throw IllegalArgumentException("Wrong connection string: $connectionString. Make sure it starts with 'http(s)://' or 'mongodb://' or 'jdbc:postgresql://' or 'dataSource'")
    }

    @Bean
    open fun queueService(
        @Value("\${lsd.dist.threadPool.size:16}") queueLength: Int,
        interceptedDocumentRepository: InterceptedDocumentRepository
    ) = RepositoryService(queueLength, interceptedDocumentRepository)
}
