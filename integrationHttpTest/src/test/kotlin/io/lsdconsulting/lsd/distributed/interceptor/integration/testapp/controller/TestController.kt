package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.controller

import io.lsdconsulting.lsd.distributed.interceptor.config.log
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.controller.event.SomethingDoneEvent
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController(private val rabbitTemplate: RabbitTemplate) {
    @GetMapping("/api-listener")
    fun getObjectByMessage(@RequestParam message: String?): ResponseEntity<String> {
        log().info("Received message:{}", message)
        rabbitTemplate.convertAndSend("exchange-listener", NO_ROUTING_KEY, event()) { m: Message ->
            m.messageProperties.headers["Authorization"] = "Password"
            m
        }
        return ResponseEntity.ok("response_from_controller")
    }

    @GetMapping("/api-rabbit-template")
    operator fun get(@RequestParam message: String?): ResponseEntity<String> {
        log().info("Received message:{}", message)
        rabbitTemplate.convertAndSend("exchange-rabbit-template", NO_ROUTING_KEY, event())
        return ResponseEntity.ok("response_from_controller")
    }

    @GetMapping("/setup1")
    fun setup1(): ResponseEntity<String> {
        return ResponseEntity.ok("{\"setup1\":\"done\"}")
    }

    @GetMapping("/setup2")
    fun setup2(): ResponseEntity<String> {
        return ResponseEntity.ok("{\"setup2\":\"done\"}")
    }

    private fun event() = SomethingDoneEvent("from_controller")
}

private const val NO_ROUTING_KEY: String = ""
