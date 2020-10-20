package com.integreety.yatspec.e2e.integration.testapp.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/objects")
public class TestController {

    private final RabbitTemplate rabbitTemplate;

    public TestController(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping
    public ResponseEntity<String> getObjectByMessage(@RequestParam final String message) {
        rabbitTemplate.convertAndSend("exchange", null, message);
        return ResponseEntity.ok(message);
    }
}
