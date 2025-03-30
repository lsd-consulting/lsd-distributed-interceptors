package io.lsdconsulting.lsd.distributed.interceptor.kafkaintegration.testapp

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import

@Import(AppConfig::class)
@SpringBootApplication
class TestApplication

fun main(args: Array<String>) {
    SpringApplication.run(TestApplication::class.java, *args)
}
