package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp

import java.time.OffsetDateTime

data class Input(
    val id: String,
    val value: String
)

data class Output(
    val id: String,
    val value: String,
    val receivedDateTime: OffsetDateTime
)