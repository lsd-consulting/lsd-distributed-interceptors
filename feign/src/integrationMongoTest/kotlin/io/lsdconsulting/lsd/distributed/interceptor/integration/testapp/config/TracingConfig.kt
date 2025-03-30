package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config

import io.micrometer.tracing.Tracer
import io.micrometer.tracing.test.simple.SimpleTracer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Suppress("unused")
@Configuration
class TracingConfig {
    @Bean
    fun tracer(): Tracer = SimpleTracer()
}