package com.integreety.yatspec.e2e.captor.rabbit.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;

import java.util.Arrays;
import java.util.Map;

@Slf4j
public class ExchangeNameDeriver {

    private static final String TYPE_ID_HEADER = "__TypeId__";

    public String derive(final MessageProperties messageProperties)  {
        final Map<String, Object> headers = messageProperties.getHeaders();
        log.info("E2ET-LSD: headers:{}", headers);
        if (messageProperties.getHeader(TYPE_ID_HEADER) == null) {
            return "EXCHANGE_NAME_NOT_FOUND";
        }
        return Arrays.stream(messageProperties.getHeader(TYPE_ID_HEADER).toString().split("\\."))
                .reduce((first, second) -> second)
                .orElse("EXCHANGE_NAME_NOT_FOUND");
    }
}