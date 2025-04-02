package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp

import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config.RepositoryConfig
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config.TracingConfig
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.external.ExternalClient
import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.external.ExternalClientWithTargetHeader
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Import

@SpringBootApplication
@EnableFeignClients(clients = [ExternalClient::class, ExternalClientWithTargetHeader::class])
@Import(RepositoryConfig::class, TracingConfig::class)
class TestApplication

fun main(args: Array<String>) {
    SpringApplication.run(TestApplication::class.java, *args)
}
