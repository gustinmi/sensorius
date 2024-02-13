package com.gustinmi.sensorius;

import java.io.IOException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.gustinmi.sensorius.healtcheck.HealthServer;
import com.gustinmi.sensorius.utils.LoggingFactory;

import jakarta.annotation.PreDestroy;

/**
 * Bootstrap application class
 * @author mgustin
 */
@SpringBootApplication
public class App {
	
	public static final Logger logger = LoggingFactory.loggerForThisClass();
	
	private static HealthServer server = null;
	
	@Value("${com.gustinmi.sensorius.health-port}")
	private Integer health_port;
	
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}
	
	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		
		return (args) -> {
			
			logger.info("Running app in console mode ...");

			int port = 8097; //TODO move to properties
			
			try {
				server = new HealthServer(port);
			} catch (IOException e) {
				System.err.println(e);
			}
			
			try {
				if (server != null) server.start();
			} catch (IOException e) {
				System.err.println(e);
			}

		};
		
	}
	
	@PreDestroy
	public void shutdown() {
		if (server != null) server.stop();
		logger.info("Shuting down app");
	}
	
	
}
