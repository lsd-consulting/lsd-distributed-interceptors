package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.handler

import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneId

@Service
class InputOutputHandler {
    fun handle(input: Input) =
        Output(id = input.id, value = input.value, receivedDateTime = OffsetDateTime.now(ZoneId.of("UTC")))
}
