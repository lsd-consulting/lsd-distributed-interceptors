package io.lsdconsulting.lsd.distributed.config;

import com.lsd.LsdContext;
import io.lsdconsulting.lsd.distributed.captor.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.diagram.LsdLogger;
import io.lsdconsulting.lsd.distributed.diagram.interaction.InteractionGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "lsd.dist.db.connectionString")
public class LsdLoggerConfig {

    @Bean
    public InteractionGenerator interactionGenerator() {
        return new InteractionGenerator();
    }

    @Bean
    public LsdLogger lsdLogger(final InterceptedDocumentRepository interceptedDocumentRepository,
                               final InteractionGenerator interactionGenerator) {

        return new LsdLogger(interceptedDocumentRepository, interactionGenerator, LsdContext.getInstance());
    }
}
