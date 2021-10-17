package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.controller;

import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.controller.event.SomethingDoneEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {
    private static final String NO_ROUTING_KEY = null;

    private final RabbitTemplate rabbitTemplate;

    public TestController(final RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @GetMapping("/api-listener")
    public ResponseEntity<String> getObjectByMessage(@RequestParam final String message) {
        log.info("Received message:{}", message);
        rabbitTemplate.convertAndSend("exchange-listener", NO_ROUTING_KEY, getEvent(), m -> {
            m.getMessageProperties().getHeaders().put("Authorization", "Password");
            return m;
        });
        return ResponseEntity.ok("response_from_controller");
    }

    @GetMapping("/api-rabbit-template")
    public ResponseEntity<String> get(@RequestParam final String message) {
        log.info("Received message:{}", message);
        rabbitTemplate.convertAndSend("exchange-rabbit-template", NO_ROUTING_KEY, getEvent());
        return ResponseEntity.ok("response_from_controller");
    }

    @GetMapping("/setup1")
    public ResponseEntity<String> setup1() {
        return ResponseEntity.ok("{\"setup1\":\"done\"}");
    }

    @GetMapping("/setup2")
    public ResponseEntity<String> setup2() {
        return ResponseEntity.ok("{\"setup2\":\"done\"}");
    }

    private SomethingDoneEvent getEvent() {
        return SomethingDoneEvent.builder().message("from_controller").build();
    }
}

