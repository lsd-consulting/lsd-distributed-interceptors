package io.lsdconsulting.lsd.distributed.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/*
    This config adds spring-configuration-metadata.json, which enables IDEs to recognise this library's property names
*/
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "lsd.dist.obfuscator")
public class ObfuscatorPropertyConfig {

    private String sensitiveHeaders;
}