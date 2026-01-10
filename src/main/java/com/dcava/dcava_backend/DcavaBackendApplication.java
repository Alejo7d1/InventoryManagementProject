package com.dcava.dcava_backend;

import com.dcava.dcava_backend.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class DcavaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DcavaBackendApplication.class, args);
	}

}
