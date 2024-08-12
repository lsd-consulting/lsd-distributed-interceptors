package io.lsdconsulting.lsd.distributed.interceptor.kafkaintegration.testapp

import lsd.logging.log
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneId

@Service
class IncomingEventListener {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, Output>

    @Value("\${service.outgoingTopic}")
    private lateinit var outgoingTopic: String

    @Value("\${info.app.name}")
    private lateinit var serviceName: String

    @KafkaListener(id = "incomingListener", topics = ["\${service.incomingTopic}"], groupId = "someGroup",
        clientIdPrefix = "input", properties = ["bootstrap.servers=\${spring.kafka.bootstrap-servers}"])
    fun handle(input: Input) {
        val output = Output(id = input.id, value = input.value, receivedDateTime = OffsetDateTime.now(ZoneId.of("UTC")))
        kafkaTemplate.send(
            ProducerRecord(
                outgoingTopic, null, null, null, output, listOf(
                    RecordHeader("Source-Name", serviceName.toByteArray()),
                    RecordHeader("Target-Name", "NewEvent".toByteArray()),
                )
            )
        )
        log().info("Published output={}", output)
    }
}
