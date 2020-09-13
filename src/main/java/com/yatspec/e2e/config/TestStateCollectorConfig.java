package com.yatspec.e2e.config;

import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.yatspec.e2e.diagram.TestStateCollector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "yatspec.lsd.db.connectionstring")
@ConditionalOnBean(TestState.class)
public class TestStateCollectorConfig {

    @Value("${yatspec.lsd.db.connectionstring}")
    private String dbConnectionString;

    @Bean
    public TestStateCollector testStateCollector(final TestState testState) {
        return new TestStateCollector(dbConnectionString, testState);
    }
}
