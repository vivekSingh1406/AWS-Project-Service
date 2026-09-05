package com.example.AmazonS3RDSService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AmazonS3RdsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmazonS3RdsServiceApplication.class, args);
	}

}
