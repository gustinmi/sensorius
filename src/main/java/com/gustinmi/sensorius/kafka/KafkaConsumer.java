package com.gustinmi.sensorius.kafka;

import org.slf4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.gustinmi.sensorius.utils.LoggingFactory;

/**
 * Concurrent consumer
 * */
@Component
public class KafkaConsumer {
	
	public static final Logger logger = LoggingFactory.loggerForThisClass();
	
    @KafkaListener(topics = "${com.gustinmi.sensorius.topic-name}", groupId = "${com.gustinmi.sensorius.group-name}")
    public void consume(String message) {
        System.out.println("Received message: " + message);
    }
    
}