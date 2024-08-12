package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import

@Import(AppConfig::class)
@SpringBootApplication
open class TestApplication

fun main(args: Array<String>) {
    SpringApplication.run(TestApplication::class.java, *args)
}
