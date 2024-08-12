package io.lsdconsulting.lsd.distributed.interceptor.kafkaintegration.testapp

import com.fasterxml.jackson.databind.ObjectMapper
import io.lsdconsulting.lsd.distributed.interceptor.config.mapper.ObjectMapperCreator
import io.lsdconsulting.lsd.distributed.interceptor.interceptor.LsdKafkaInterceptor
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

open class AppConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean
    open fun objectMapper() = ObjectMapperCreator().objectMapper

    @Bean
    fun kafkaProducerFactory(objectMapper: ObjectMapper): ProducerFactory<*, *> {
        val jsonSerializer = JsonSerializer<Any?>(objectMapper)
        val configs = mapOf<String, Any>(
            "bootstrap.servers"  to bootstrapServers,
            "interceptor.classes" to LsdKafkaInterceptor::class.java.name
        )
        return DefaultKafkaProducerFactory<String, Any>(configs, StringSerializer(), jsonSerializer)
    }
}
