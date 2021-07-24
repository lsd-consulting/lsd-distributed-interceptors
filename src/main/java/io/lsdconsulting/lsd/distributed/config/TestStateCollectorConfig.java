package io.lsdconsulting.lsd.distributed.config;

import com.lsd.LsdContext;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.teststate.TestStateLogger;
import io.lsdconsulting.lsd.distributed.teststate.interaction.InteractionGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "lsd.db.connectionstring")
public class TestStateCollectorConfig {

    @Bean
    public InteractionGenerator interactionGenerator() {
        return new InteractionGenerator();
    }

    @Bean
    public TestStateLogger testStateCollector(final InterceptedDocumentRepository interceptedDocumentRepository,
                                              final InteractionGenerator interactionGenerator) {

        return new TestStateLogger(interceptedDocumentRepository, interactionGenerator, LsdContext.getInstance());
    }
}
