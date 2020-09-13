package com.yatspec.e2e.captor.name;

import org.springframework.amqp.core.MessageProperties;

import java.util.Arrays;

public class ExchangeNameDeriver {

    private static final String TYPE_ID_HEADER = "__TypeId__";

    public String derive(final MessageProperties messageProperties)  {
        return Arrays.stream(messageProperties.getHeader(TYPE_ID_HEADER).toString().split("\\."))
                .reduce((first, second) -> second)
                .orElse(null);
    }
}