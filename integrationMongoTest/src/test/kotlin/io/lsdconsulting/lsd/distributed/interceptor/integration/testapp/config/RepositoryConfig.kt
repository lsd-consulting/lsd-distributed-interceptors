package io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.config

import io.lsdconsulting.lsd.distributed.interceptor.integration.testapp.repository.TestRepository
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
open class RepositoryConfig {

    @Bean
    open fun testRepository() = TestRepository()
}
