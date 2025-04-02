package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config

import io.micrometer.tracing.Tracer
import io.micrometer.tracing.test.simple.SimpleTracer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@Suppress("unused")
@TestConfiguration
class TracingConfig {
    @Bean
    fun tracer(): Tracer = SimpleTracer()
}