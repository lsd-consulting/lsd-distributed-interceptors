package io.lsdconsulting.lsd.distributed.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/*
    This config adds spring-configuration-metadata.json, which enables IDEs to recognise this library's property names
*/
@Getter
@Setter
@ConditionalOnProperty(name = {"lsd.db.connectionstring"})
@Configuration
@ConfigurationProperties(prefix = "lsd.db")
public class DbPropertyConfig {

    private String connectionstring;
    private String trustStoreLocation;
    private String trustStorePassword;
}