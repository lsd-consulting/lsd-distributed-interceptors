package com.integreety.yatspec.e2e.config;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.integreety.yatspec.e2e.captor.repository.InterceptedDocumentRepository;
import com.integreety.yatspec.e2e.teststate.TestStateCollector;
import com.integreety.yatspec.e2e.teststate.mapper.destination.RegexResolvingNameMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "yatspec.lsd.db.connectionstring")
@ConditionalOnBean(TestState.class)
public class TestStateCollectorConfig {

    @Bean
    public RegexResolvingNameMapper regexResolvingNameMapper() {
        return new RegexResolvingNameMapper();
    }

    @Bean
    public TestStateCollector testStateCollector(final TestState testState,
                                                 final InterceptedDocumentRepository interceptedDocumentRepository) {

        return new TestStateCollector(testState, interceptedDocumentRepository);
    }
}
