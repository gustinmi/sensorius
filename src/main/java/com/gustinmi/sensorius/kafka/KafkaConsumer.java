package com.gustinmi.sensorius.kafka;

import org.slf4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.gustinmi.sensorius.SensorRegistry;
import com.gustinmi.sensorius.utils.LoggingFactory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Concurrent consumer
 * */
@Component
public class KafkaConsumer {
	
	public static final Logger logger = LoggingFactory.loggerForThisClass();
		
	private static final boolean FLUSH_ON = true; // we will flush to timeseries database
	//TODO @Value("${" + App.FLUSH_ON + "}")
	//TODO private String flushOn;

	@PostConstruct
	private void postConstruct() {
		logger.info("KAFKA CONSUMER - About to initializing sensor registry");
		SensorRegistry.INSTANCE.initialize(FLUSH_ON);
	}
	
    @KafkaListener(topics = "${com.gustinmi.sensorius.topic-name}", groupId = "${com.gustinmi.sensorius.group-name}")
    public void consume(String message) {
	 	try {
	 		// we have autocommit mode, so no ack
	        logger.info("Received message: " + message);
			SensorRegistry.INSTANCE.addSensorReading(message);
	        
	    } catch (Exception e) {
	        log.warn("Fail to handle message {}.", message);
	        //TODO for now we leave all alone
	    }
        
    }
    
    @PreDestroy
    public void preDestroy() {
    	logger.info("KAFKA CONSUMER -  destoying");
    	// TODO gracefully shutdown registry, save what we have so far into persistence
    }
    
}