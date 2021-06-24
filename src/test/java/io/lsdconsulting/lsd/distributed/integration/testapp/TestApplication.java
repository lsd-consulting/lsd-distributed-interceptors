package io.lsdconsulting.lsd.distributed.integration.testapp;

import io.lsdconsulting.lsd.distributed.integration.testapp.external.ExternalClient;
import io.lsdconsulting.lsd.distributed.integration.testapp.external.ExternalClientWithTargetHeader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients( clients = {ExternalClient.class, ExternalClientWithTargetHeader.class})
public class TestApplication {

	public static void main(final String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}
}
