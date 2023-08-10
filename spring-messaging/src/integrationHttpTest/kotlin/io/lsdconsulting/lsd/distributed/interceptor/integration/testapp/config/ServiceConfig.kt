package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config

import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.handler.Input
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.handler.InputOutputHandler
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.handler.Output
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.util.function.Function

@TestConfiguration
open class ServiceConfig(
    private val inputOutputHandler: InputOutputHandler
) {
    @Bean
    open fun inputOutputHandlerFunction() =
        Function<Input, Output> {
            inputOutputHandler.handle(it)
        }
}
