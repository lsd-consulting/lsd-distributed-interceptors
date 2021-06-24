package io.lsdconsulting.lsd.distributed.config;

import feign.Logger;
import io.lsdconsulting.lsd.distributed.captor.http.RequestCaptor;
import io.lsdconsulting.lsd.distributed.captor.http.ResponseCaptor;
import io.lsdconsulting.lsd.distributed.interceptor.LsdFeignLoggerInterceptor;
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
@ConditionalOnProperty(name = "lsd.db.connectionstring")
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