package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.listener

import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.external.ExternalClient
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.external.ExternalClientWithTargetHeader
import lsd.logging.log
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class TestListener(
    private val externalClient: ExternalClient,
    private val externalClientWithTargetHeader: ExternalClientWithTargetHeader,
) {
    @RabbitListener(queues = ["queue-listener"])
    fun consume(message: String?) {
        log().info("Consuming message={}", message)
        externalClient.post("from_listener")
        externalClientWithTargetHeader.post("from_listener")
    }
}
