package com.integreety.yatspec.e2e.integration.testapp.listener;

import com.integreety.yatspec.e2e.integration.testapp.external.ExternalClientWithTargetHeader;
import com.integreety.yatspec.e2e.integration.testapp.external.ExternalClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestListener {

    private final ExternalClient externalClient;
    private final ExternalClientWithTargetHeader externalClientWithTargetHeader;

    @RabbitListener(queues = "queue-listener")
    public void consume(final String message) {
        log.info("Consuming message={}", message);
        externalClient.post("from_listener");
        externalClientWithTargetHeader.post("from_listener");
    }
}
