package io.lsdconsulting.lsd.distributed.teststate;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

import static java.lang.Long.MAX_VALUE;

@Slf4j
public class TraceIdGenerator {
    private static final long MIN_VALID_TRACE_ID_VALUE = 1152921504606846976L;

    public static String generate() {
        final String traceId = Long.toHexString(RandomUtils.nextLong(MIN_VALID_TRACE_ID_VALUE, MAX_VALUE));
        log.info("Generated new traceId:{}", traceId);
        return traceId;
    }
}
