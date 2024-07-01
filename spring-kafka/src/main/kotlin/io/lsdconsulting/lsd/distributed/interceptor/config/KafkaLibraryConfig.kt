package io.lsdconsulting.lsd.distributed.interceptor.config

import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.KafkaCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.KafkaHeaderRetriever
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.LsdKafkaProducerInterceptor
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.ProducerFactoryCustomizer
import io.lsdconsulting.lsd.distributed.interceptor.persistence.RepositoryService
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
@ConditionalOnClass(ProducerRecord::class)
open class KafkaLibraryConfig {

    @Bean
    open fun applicationContextProvider() = ApplicationContextProvider()

    @Bean
    open fun kafkaHeaderRetriever(obfuscator: Obfuscator): KafkaHeaderRetriever {
        return KafkaHeaderRetriever(obfuscator)
    }

    @Bean
    open fun messagingCaptor(
        repositoryService: RepositoryService,
        propertyServiceNameDeriver: PropertyServiceNameDeriver,
        traceIdRetriever: TraceIdRetriever,
        kafkaHeaderRetriever: KafkaHeaderRetriever,
        @Value("\${spring.profiles.active:#{''}}") profile: String
    ): KafkaCaptor {
        return KafkaCaptor(
            repositoryService,
            propertyServiceNameDeriver,
            traceIdRetriever,
            kafkaHeaderRetriever,
            profile
        )
    }

    @Bean
    open fun producerFactoryCustomizer() = ProducerFactoryCustomizer()

    @Bean
    open fun lsdKafkaProducerInterceptor() = LsdKafkaProducerInterceptor()
}
