package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.KafkaCaptor
import io.lsdconsulting.lsd.distributed.interceptor.config.ApplicationContextProvider
import io.lsdconsulting.lsd.distributed.interceptor.config.log
import org.apache.kafka.clients.producer.ProducerInterceptor
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata

class LsdKafkaProducerInterceptor(
): ProducerInterceptor<String, Any> {

    private val kafkaCaptor: KafkaCaptor = ApplicationContextProvider.context.getBean(KafkaCaptor::class.java)

    override fun configure(configs: MutableMap<String, *>?) {}

    override fun onAcknowledgement(metadata: RecordMetadata?, exception: Exception?) {}

    override fun close() {}

    override fun onSend(record: ProducerRecord<String, Any>): ProducerRecord<String, Any> {
        log().debug("Intercepted record: {}", record)
        kafkaCaptor.capturePublishInteraction(record)
        return record
    }
}
