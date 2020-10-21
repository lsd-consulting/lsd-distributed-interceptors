package com.integreety.yatspec.e2e.captor.rabbit.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;

import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class ExchangeNameDeriver {

    // TODO This method of determining the exchange name is not reliable
    private static final String TYPE_ID_HEADER = "__TypeId__";

    public String derive(final MessageProperties messageProperties, final String defaultExchangeName)  {
        if (messageProperties.getHeader(TYPE_ID_HEADER) == null) {
            return defaultExchangeName;
        }
        final String exchangeName = deriveFromMessageProperties(messageProperties);
        if (!isBlank(exchangeName)) {
            return exchangeName;
        }
        return defaultExchangeName;
    }

    private String deriveFromMessageProperties(final MessageProperties messageProperties) {
        return Arrays.stream(messageProperties.getHeader(TYPE_ID_HEADER).toString().split("\\."))
                .reduce((first, second) -> second)
                .orElse(null);
    }
}