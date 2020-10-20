package com.integreety.yatspec.e2e.integration.testapp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestListener {

    @RabbitListener(queues = "queue")
    public void consume(final String message) {
        log.info("Consuming message={}", message);
    }
}
