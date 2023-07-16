package io.lsdconsulting.lsd.distributed.interceptor.config

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.AmqpHeaderRetriever
import io.lsdconsulting.lsd.distributed.interceptor.captor.rabbit.RabbitCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import org.springframework.amqp.core.Message
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
open class AmqpLibraryConfig {

    @Bean
    @ConditionalOnClass(Message::class)
    open fun amqpHeaderRetriever(obfuscator: Obfuscator) = AmqpHeaderRetriever(obfuscator)

    @Bean
    @ConditionalOnBean(name = ["amqpHeaderRetriever"])
    open fun publishCaptor(
        repositoryService: RepositoryService,
        propertyServiceNameDeriver: PropertyServiceNameDeriver,
        traceIdRetriever: TraceIdRetriever,
        amqpHeaderRetriever: AmqpHeaderRetriever,
        @Value("\${spring.profiles.active:#{''}}") profile: String
    ) = RabbitCaptor(
        repositoryService,
        propertyServiceNameDeriver,
        traceIdRetriever,
        amqpHeaderRetriever,
        profile
    )
}
