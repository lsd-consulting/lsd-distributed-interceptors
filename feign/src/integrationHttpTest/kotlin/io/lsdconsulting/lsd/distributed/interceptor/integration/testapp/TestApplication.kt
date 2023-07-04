package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp

import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.external.ExternalClient
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.external.ExternalClientWithTargetHeader
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients(clients = [ExternalClient::class, ExternalClientWithTargetHeader::class])
open class TestApplication

fun main(args: Array<String>) {
    SpringApplication.run(TestApplication::class.java, *args)
}
