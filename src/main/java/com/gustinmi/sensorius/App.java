package com.gustinmi.sensorius;


import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.gustinmi.sensorius.utils.LoggingFactory;

@SpringBootApplication
public class App {
	
	public static final Logger logger = LoggingFactory.loggerForThisClass();
	
	public static final String SENSORIUS_TOPIC_NAME_CFG = "com.gustinmi.sensorius.topic-name";
	public static final String SESNSORIUS_GROUP_NAME_CFG = "com.gustinmi.sensorius.group-name"; 
	

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		
		return (args) -> {

			logger.info("Running app in console mode ");

		};
		
	}
	
	
}
