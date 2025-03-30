package io.lsdconsulting.lsd.distributed.interceptor.config

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.ErrorMessagePublishingCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessageConsumingCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessagePublishingCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessagingHeaderRetriever
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
@ConditionalOnClass(Message::class)
class MessagingLibraryConfig {

    @Bean
    fun messagingHeaderRetriever(obfuscator: Obfuscator): MessagingHeaderRetriever {
        return MessagingHeaderRetriever(obfuscator)
    }

    @Bean
    fun messageConsumingCaptor(
        repositoryService: RepositoryService,
        propertyServiceNameDeriver: PropertyServiceNameDeriver,
        traceIdRetriever: TraceIdRetriever,
        messagingHeaderRetriever: MessagingHeaderRetriever,
        @Value("\${spring.profiles.active:#{''}}") profile: String
    ) = MessageConsumingCaptor(
        repositoryService,
        propertyServiceNameDeriver,
        traceIdRetriever,
        messagingHeaderRetriever,
        profile
    )

    @Bean
    fun messagePublishingCaptor(
        repositoryService: RepositoryService,
        propertyServiceNameDeriver: PropertyServiceNameDeriver,
        traceIdRetriever: TraceIdRetriever,
        messagingHeaderRetriever: MessagingHeaderRetriever,
        @Value("\${spring.profiles.active:#{''}}") profile: String
    ) = MessagePublishingCaptor(
        repositoryService,
        propertyServiceNameDeriver,
        traceIdRetriever,
        messagingHeaderRetriever,
        profile
    )

    @Bean
    fun errorMessagePublishingCaptor(
        repositoryService: RepositoryService,
        propertyServiceNameDeriver: PropertyServiceNameDeriver,
        traceIdRetriever: TraceIdRetriever,
        messagingHeaderRetriever: MessagingHeaderRetriever,
        @Value("\${spring.profiles.active:#{''}}") profile: String
    ) = ErrorMessagePublishingCaptor(
        repositoryService,
        propertyServiceNameDeriver,
        traceIdRetriever,
        messagingHeaderRetriever,
        profile
    )
}
