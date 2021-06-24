package io.lsdconsulting.lsd.distributed.integration.testapp.config;

import io.lsdconsulting.lsd.distributed.integration.testapp.repository.TestRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static io.lsdconsulting.lsd.distributed.integration.testapp.repository.TestRepository.setupDatabase;

@TestConfiguration
public class RepositoryConfig {

    // This is because the configs in spring.factories run always before any test configs.
    static {
        setupDatabase();
    }

    @Bean
    public TestRepository testRepository() {
        return new TestRepository();
    }
}
