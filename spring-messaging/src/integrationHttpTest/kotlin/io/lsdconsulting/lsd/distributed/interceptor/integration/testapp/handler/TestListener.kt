package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.handler

import io.lsdconsulting.lsd.distributed.interceptor.config.log
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.Message
import org.springframework.stereotype.Component
import java.util.concurrent.ArrayBlockingQueue

@Component
class TestListener {
    private val outputQueue = ArrayBlockingQueue<Message<String>>(1)

    @KafkaListener(id = "outputListener", topics = ["output.topic"], groupId = "someGroup",
        clientIdPrefix = "output", properties = ["bootstrap.servers=localhost:9093"])
    fun vehicleOrderUpdatedEventListen(message: Message<String>) {
        log().info("Received in listener={}", message)
        outputQueue.add(message)
    }

    fun getOutputQueue() = outputQueue
}
