package com.integreety.yatspec.e2e.captor.rabbit.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;

import java.util.Arrays;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class ExchangeNameDeriver {

    private static final String TYPE_ID_HEADER = "__TypeId__";
    private static final String TARGET_NAME_KEY = "Target-Name";
    private static final String UNKNOWN_EVENT = "UNKNOWN_EVENT";

    public String derive(final MessageProperties messageProperties, final String alternativeExchangeName)  {
        final String defaultExchangeName = getDefaultExchangeName(alternativeExchangeName);
        if (!isBlank(messageProperties.getHeader(TARGET_NAME_KEY))) {
            return messageProperties.getHeader(TARGET_NAME_KEY);
        }
        if (!isBlank(messageProperties.getHeader(TYPE_ID_HEADER))) {
            return deriveFromTypeIdHeader(messageProperties.getHeader(TYPE_ID_HEADER)).orElse(defaultExchangeName);
        }
        return defaultExchangeName;
    }

    private String getDefaultExchangeName(final String alternativeExchangeName) {
        return !isBlank(alternativeExchangeName) ? alternativeExchangeName : UNKNOWN_EVENT;
    }

    private Optional<String> deriveFromTypeIdHeader(final String typeIdHeader) {
        return Arrays.stream(typeIdHeader.split("\\."))
                .reduce((first, second) -> second);
    }
}