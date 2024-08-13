package io.lsdconsulting.lsd.distributed.interceptor.springkafkaintegration.testapp

import lsd.logging.log
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.Message
import org.springframework.stereotype.Component
import java.util.concurrent.ArrayBlockingQueue

@Component
class TestListener {
    private val outgoingTopic = ArrayBlockingQueue<Message<Output>>(100)

    @KafkaListener(id = "outputListener", topics = ["\${service.outgoingTopic}"], groupId = "someGroup",
        clientIdPrefix = "output", properties = ["bootstrap.servers=\${spring.kafka.bootstrap-servers}"])
    fun outputTopicListener(message: Message<Output>) {
        log().info("Received in listener={}", message)
        outgoingTopic.add(message)
    }

    fun getOutgoingTopic() = outgoingTopic
    fun clearOutgoingTopic() {
        outgoingTopic.clear()
    }
}
