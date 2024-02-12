package com.gustinmi.sensorius;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    @KafkaListener(topics = "demo-messages")
    public void consume(String message) {
        System.out.println("Received message: " + message);
    }
}