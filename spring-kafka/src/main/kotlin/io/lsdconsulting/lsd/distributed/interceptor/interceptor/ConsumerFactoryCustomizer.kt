package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import org.apache.kafka.clients.producer.ProducerConfig.INTERCEPTOR_CLASSES_CONFIG
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import java.util.*

class ConsumerFactoryCustomizer : DefaultKafkaConsumerFactoryCustomizer {

    override fun customize(consumerFactory: DefaultKafkaConsumerFactory<*, *>) {
        if (Objects.nonNull(consumerFactory)) {
            val updatedInterceptors = mutableListOf<String>()

            consumerFactory.configurationProperties[INTERCEPTOR_CLASSES_CONFIG]?.let {
                updatedInterceptors.addAll(it as Collection<String>)
            }
            updatedInterceptors.add(LsdSpringKafkaInterceptor::class.java.name)
            consumerFactory.updateConfigs(mapOf(INTERCEPTOR_CLASSES_CONFIG to updatedInterceptors))
        }
    }
}
