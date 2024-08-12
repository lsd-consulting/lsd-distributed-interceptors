package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp

import lsd.logging.log
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.Message
import org.springframework.stereotype.Component
import java.util.concurrent.ArrayBlockingQueue

@Component
class TestListener {
    private val outgoingTopic = ArrayBlockingQueue<Message<Output>>(100)
//    private val noLsdHeadersOutputTopic = ArrayBlockingQueue<Message<String>>(1)

    @KafkaListener(id = "outputListener", topics = ["\${service.outgoingTopic}"], groupId = "someGroup",
        clientIdPrefix = "output", properties = ["bootstrap.servers=\${spring.kafka.bootstrap-servers}"])
    fun outputTopicListener(message: Message<Output>) {
        log().info("Received in listener={}", message)
        outgoingTopic.add(message)
    }

//    @KafkaListener(id = "anotherOutputListener", topics = ["\${spring.cloud.stream.bindings.noOutputLsdHeadersHandlerFunction-out-0.destination}"], groupId = "someGroup",
//        clientIdPrefix = "output", properties = ["bootstrap.servers=localhost:9094"])
//    fun noLsdHeadersOutputTopicListener(message: Message<String>) {
//        log().info("Received in listener={}", message)
//        noLsdHeadersOutputTopic.add(message)
//    }

    fun getOutgoingTopic() = outgoingTopic
//    fun getNoLsdHeadersOutputTopic() = noLsdHeadersOutputTopic
}
