package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config

import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.repository.TestRepository
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class RepositoryConfig {

    @Bean
    fun testRepository() = TestRepository()
}
