package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.handler

import io.lsdconsulting.lsd.distributed.interceptor.config.log
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.Message
import org.springframework.stereotype.Component
import java.util.concurrent.ArrayBlockingQueue

@Component
class TestListener {
    private val outputTopic = ArrayBlockingQueue<Message<String>>(1)
    private val noLsdHeadersOutputTopic = ArrayBlockingQueue<Message<String>>(1)

    @KafkaListener(id = "outputListener", topics = ["\${spring.cloud.stream.bindings.inputOutputHandlerFunction-out-0.destination}"], groupId = "someGroup",
        clientIdPrefix = "output", properties = ["bootstrap.servers=localhost:9093"])
    fun outputTopicListener(message: Message<String>) {
        log().info("Received in listener={}", message)
        outputTopic.add(message)
    }

    @KafkaListener(id = "anotherOutputListener", topics = ["\${spring.cloud.stream.bindings.noOutputLsdHeadersHandlerFunction-out-0.destination}"], groupId = "someGroup",
        clientIdPrefix = "output", properties = ["bootstrap.servers=localhost:9093"])
    fun noLsdHeadersOutputTopicListener(message: Message<String>) {
        log().info("Received in listener={}", message)
        noLsdHeadersOutputTopic.add(message)
    }

    fun getOutputTopic() = outputTopic
    fun getNoLsdHeadersOutputTopic() = noLsdHeadersOutputTopic
}
