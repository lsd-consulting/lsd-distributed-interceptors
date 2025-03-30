package io.lsdconsulting.lsd.distributed.interceptor.config

import io.lsdconsulting.lsd.distributed.interceptor.captor.KafkaConsumerCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.KafkaHeaderRetriever
import io.lsdconsulting.lsd.distributed.interceptor.captor.KafkaProducerCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.Obfuscator
import io.lsdconsulting.lsd.distributed.interceptor.captor.common.PropertyServiceNameDeriver
import io.lsdconsulting.lsd.distributed.interceptor.captor.trace.TraceIdRetriever
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.ConsumerFactoryCustomizer
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.LsdSpringKafkaInterceptor
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
class KafkaLibraryConfig {

    @Bean
    fun applicationContextProvider() = ApplicationContextProvider()

    @Bean
    fun kafkaHeaderRetriever(obfuscator: Obfuscator): KafkaHeaderRetriever {
        return KafkaHeaderRetriever(obfuscator)
    }

    @Bean
    fun kafkaProducerCaptor(
        repositoryService: RepositoryService,
        propertyServiceNameDeriver: PropertyServiceNameDeriver,
        traceIdRetriever: TraceIdRetriever,
        kafkaHeaderRetriever: KafkaHeaderRetriever,
        @Value("\${spring.profiles.active:#{''}}") profile: String
    ): KafkaProducerCaptor {
        return KafkaProducerCaptor(
            repositoryService,
            propertyServiceNameDeriver,
            traceIdRetriever,
            kafkaHeaderRetriever,
            profile
        )
    }

    @Bean
    fun kafkaConsumerCaptor(
        repositoryService: RepositoryService,
        propertyServiceNameDeriver: PropertyServiceNameDeriver,
        traceIdRetriever: TraceIdRetriever,
        kafkaHeaderRetriever: KafkaHeaderRetriever,
        @Value("\${spring.profiles.active:#{''}}") profile: String
    ): KafkaConsumerCaptor {
        return KafkaConsumerCaptor(
            repositoryService,
            propertyServiceNameDeriver,
            traceIdRetriever,
            kafkaHeaderRetriever,
            profile
        )
    }

    @Bean
    fun producerFactoryCustomizer() = ProducerFactoryCustomizer()

    @Bean
    fun consumerFactoryCustomizer() = ConsumerFactoryCustomizer()

    @Bean
    fun lsdKafkaInterceptor() = LsdSpringKafkaInterceptor()
}
