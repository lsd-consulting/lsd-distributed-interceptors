package com.integreety.yatspec.e2e.config;

import com.integreety.yatspec.e2e.captor.http.mapper.destination.DestinationNameMappings;
import com.integreety.yatspec.e2e.captor.http.mapper.destination.RegexResolvingNameMapper;
import com.integreety.yatspec.e2e.captor.http.mapper.source.PropertyServiceNameDeriver;
import com.integreety.yatspec.e2e.captor.http.mapper.source.SourceNameMappings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = {"yatspec.lsd.db.connectionstring"})
public class NameMappingConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "destinationNameMappings")
    public DestinationNameMappings destinationNameMappings() {
        return new RegexResolvingNameMapper();
    }

    @Bean
    @ConditionalOnMissingBean(name = "sourceNameMappings")
    public SourceNameMappings sourceNameMappings(@Value("${info.app.name}") final String appName) {
        return new PropertyServiceNameDeriver(appName);
    }
}