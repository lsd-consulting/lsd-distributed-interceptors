package io.lsdconsulting.lsd.distributed.interceptor.interceptor;

import io.lsdconsulting.lsd.distributed.interceptor.captor.messaging.MessagingCaptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

@Slf4j
@RequiredArgsConstructor
public class EventPublisherInterceptor implements ChannelInterceptor {

    private final MessagingCaptor captor;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        captor.capturePublishInteraction(message);
        return message;
    }
}
