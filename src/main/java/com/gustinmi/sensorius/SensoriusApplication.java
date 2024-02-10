package com.gustinmi.sensorius;


import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SensoriusApplication {
	
	public static final Logger logger = LoggerFactory.getLogger(SensoriusApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SensoriusApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		
		return (args) -> {
			
			logger.info("Let's inspect the beans provided by Spring Boot:");
			
			final String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (final String beanName : beanNames) {
				System.out.println(beanName);
			}

		};
		
	}
	
	
}
