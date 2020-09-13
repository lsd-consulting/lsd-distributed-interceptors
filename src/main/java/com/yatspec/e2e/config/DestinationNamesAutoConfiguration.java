package com.yatspec.e2e.config;

import com.yatspec.e2e.captor.http.mapper.DestinationNameMappings;
import com.yatspec.e2e.captor.http.mapper.RegexResolvingNameMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = {"yatspec.lsd.db.connectionstring"})
public class DestinationNamesAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "destinationNameMappings")
    public DestinationNameMappings destinationNameMappings() {
        return new RegexResolvingNameMapper();
    }
}