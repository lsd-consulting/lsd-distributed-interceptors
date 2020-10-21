package com.integreety.yatspec.e2e.integration.testapp.listener;

import com.integreety.yatspec.e2e.integration.testapp.external.TestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestListener {

    private final TestClient testClient;

    @RabbitListener(queues = "queue")
    public void consume(final String message) {
        log.info("Consuming message={}", message);
        testClient.post("from_listener");
    }
}
