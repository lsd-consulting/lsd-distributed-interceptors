package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.KafkaCaptor
import io.lsdconsulting.lsd.distributed.interceptor.config.ApplicationContextProvider
import lsd.logging.log
import org.apache.kafka.clients.consumer.ConsumerInterceptor
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.producer.ProducerInterceptor
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition

class LsdSpringKafkaInterceptor: ProducerInterceptor<String, Any>, ConsumerInterceptor<String, Any> {

    private val kafkaCaptor: KafkaCaptor = ApplicationContextProvider.context.getBean(KafkaCaptor::class.java)

    override fun configure(configs: MutableMap<String, *>?) {}

    override fun onAcknowledgement(metadata: RecordMetadata?, exception: Exception?) {}

    override fun close() {}

    override fun onCommit(p0: MutableMap<TopicPartition, OffsetAndMetadata>?) {}

    override fun onConsume(records: ConsumerRecords<String, Any>): ConsumerRecords<String, Any> {
        log().info("Intercepted consumed records: {}", records)
        kafkaCaptor.captureConsumeInteraction(records)
        return records
    }

    override fun onSend(record: ProducerRecord<String, Any>): ProducerRecord<String, Any> {
        log().info("Intercepted published record: {}", record)
        kafkaCaptor.capturePublishInteraction(record)
        return record
    }
}
