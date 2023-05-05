package io.lsdconsulting.lsd.distributed.interceptor.config

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.header.Obfuscator
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessagingCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessagingHeaderRetriever
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.persistance.RepositoryService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
@ConditionalOnClass(Message::class)
open class MessagingLibraryConfig {
    @Bean
    open fun messagingHeaderRetriever(obfuscator: Obfuscator): MessagingHeaderRetriever {
        return MessagingHeaderRetriever(obfuscator)
    }

    @Bean
    open fun messagingCaptor(
        repositoryService: RepositoryService,
        propertyServiceNameDeriver: PropertyServiceNameDeriver,
        traceIdRetriever: TraceIdRetriever,
        messagingHeaderRetriever: MessagingHeaderRetriever,
        @Value("\${spring.profiles.active:#{''}}") profile: String
    ): MessagingCaptor {
        return MessagingCaptor(
            repositoryService,
            propertyServiceNameDeriver,
            traceIdRetriever,
            messagingHeaderRetriever,
            profile
        )
    }
}
