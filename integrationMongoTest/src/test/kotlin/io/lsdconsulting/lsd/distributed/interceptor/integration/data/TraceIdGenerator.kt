package io.lsdconsulting.lsd.distributed.interceptor.integration.data

import lsd.logging.log
import org.apache.commons.lang3.RandomUtils

object TraceIdGenerator {
    private const val MIN_VALID_TRACE_ID_VALUE = 1152921504606846976L

    fun generate(): String {
        val traceId = java.lang.Long.toHexString(RandomUtils.nextLong(MIN_VALID_TRACE_ID_VALUE, Long.MAX_VALUE))
        log().info("Generated new traceId:{}", traceId)
        return traceId
    }
}
