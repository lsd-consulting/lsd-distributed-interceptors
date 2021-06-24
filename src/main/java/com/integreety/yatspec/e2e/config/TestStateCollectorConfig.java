package com.integreety.yatspec.e2e.config;

import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.teststate.TestStateLogger;
import com.integreety.yatspec.e2e.teststate.interaction.InteractionNameGenerator;
import com.lsd.LsdContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "lsd.db.connectionstring")
public class TestStateCollectorConfig {

    @Bean
    public InteractionNameGenerator interactionNameGenerator() {
        return new InteractionNameGenerator();
    }

    @Bean
    public TestStateLogger testStateCollector(final InterceptedDocumentRepository interceptedDocumentRepository,
                                              final InteractionNameGenerator interactionNameGenerator) {

        return new TestStateLogger(interceptedDocumentRepository, interactionNameGenerator, LsdContext.getInstance());
    }
}
