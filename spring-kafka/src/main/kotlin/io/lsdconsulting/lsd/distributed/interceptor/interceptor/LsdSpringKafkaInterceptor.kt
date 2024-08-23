package io.lsdconsulting.lsd.distributed.interceptor.interceptor

import io.lsdconsulting.lsd.distributed.interceptor.captor.KafkaConsumerCaptor
import io.lsdconsulting.lsd.distributed.interceptor.captor.KafkaProducerCaptor
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

    private val kafkaProducerCaptor: KafkaProducerCaptor = ApplicationContextProvider.context.getBean(KafkaProducerCaptor::class.java)
    private val kafkaConsumerCaptor: KafkaConsumerCaptor = ApplicationContextProvider.context.getBean(KafkaConsumerCaptor::class.java)

    override fun configure(configs: MutableMap<String, *>?) {}

    override fun onAcknowledgement(metadata: RecordMetadata?, exception: Exception?) {}

    override fun close() {}

    override fun onCommit(p0: MutableMap<TopicPartition, OffsetAndMetadata>?) {}

    override fun onConsume(records: ConsumerRecords<String, Any>): ConsumerRecords<String, Any> {
        log().info("Intercepted consumed records: {}", records)
        kafkaConsumerCaptor.captureConsumeInteraction(records)
        return records
    }

    override fun onSend(record: ProducerRecord<String, Any>): ProducerRecord<String, Any> {
        log().info("Intercepted published record: {}", record)
        kafkaProducerCaptor.capturePublishInteraction(record)
        return record
    }
}
