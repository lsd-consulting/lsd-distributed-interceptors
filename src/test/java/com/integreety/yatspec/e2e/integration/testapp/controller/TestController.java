package com.integreety.yatspec.e2e.integration.testapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    private final RabbitTemplate rabbitTemplate;

    public TestController(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/api")
    public ResponseEntity<String> getObjectByMessage(@RequestParam final String message) {
        log.info("Received message:{}", message);
        rabbitTemplate.convertAndSend("exchange", null, "from_controller");
        return ResponseEntity.ok("response_from_controller");
    }
}

