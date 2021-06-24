package io.lsdconsulting.lsd.distributed.config;

import com.lsd.LsdContext;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.teststate.TestStateLogger;
import io.lsdconsulting.lsd.distributed.teststate.interaction.InteractionNameGenerator;
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
