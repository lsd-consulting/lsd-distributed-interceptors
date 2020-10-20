package com.integreety.yatspec.e2e.teststate;

import org.junit.jupiter.api.Test;

import static java.lang.Long.parseLong;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TraceIdGeneratorShould {

    @Test
    public void generateTraceIdOfCorrectLength() {
        for (int count = 0; count < nextInt(1000, 2000); count ++) {
            assertThat(TraceIdGenerator.generate().length(), is(16));
        }
    }

    @Test
    public void generateHexadecimalValue() {
        for (int count = 0; count < nextInt(1000, 2000); count ++) {
            parseLong(TraceIdGenerator.generate(), 16);
        }
    }
}