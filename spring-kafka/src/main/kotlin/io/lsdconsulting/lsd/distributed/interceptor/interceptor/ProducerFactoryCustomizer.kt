package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import org.apache.kafka.clients.producer.ProducerConfig.INTERCEPTOR_CLASSES_CONFIG
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import java.util.*

class ProducerFactoryCustomizer : DefaultKafkaProducerFactoryCustomizer {

    override fun customize(producerFactory: DefaultKafkaProducerFactory<*, *>) {
        if (Objects.nonNull(producerFactory)) {
            val updatedInterceptors = mutableListOf<String>()

            producerFactory.configurationProperties[INTERCEPTOR_CLASSES_CONFIG]?.let {
                updatedInterceptors.addAll(it as Collection<String>)
            }
            updatedInterceptors.add(LsdKafkaProducerInterceptor::class.java.name)
            producerFactory.updateConfigs(mapOf(INTERCEPTOR_CLASSES_CONFIG to updatedInterceptors))
        }
    }
}
