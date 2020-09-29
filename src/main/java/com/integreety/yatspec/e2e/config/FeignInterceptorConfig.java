package com.integreety.yatspec.e2e.config;

import com.integreety.yatspec.e2e.captor.http.RequestCaptor;
import com.integreety.yatspec.e2e.captor.http.ResponseCaptor;
import com.integreety.yatspec.e2e.interceptor.LsdFeignLoggerInterceptor;
import feign.Logger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "yatspec.lsd.db.connectionstring")
@ConditionalOnClass({FeignClientBuilder.class, Logger.Level.class})
@RequiredArgsConstructor
public class FeignInterceptorConfig {

    @Bean
    @ConditionalOnMissingBean(Logger.Level.class)
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public LsdFeignLoggerInterceptor lsdFeignLoggerInterceptor(final RequestCaptor requestCaptor, final ResponseCaptor responseCaptor) {
        return new LsdFeignLoggerInterceptor(requestCaptor, responseCaptor);
    }
}