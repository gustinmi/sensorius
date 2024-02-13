package com.gustinmi.sensorius.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import com.gustinmi.sensorius.App;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {
	
	
	@Value("${com.gustinmi.sensorius.topic-name}")
	private String topicName;
	
	@Value("${com.gustinmi.sensorius.group-name}")
	private String groupId;
	
    // Configure Kafka consumer properties
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.VoidDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true"); // enabling autocommit, we do not commit offset by ourselfs
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "10");
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    // Configure the listener container factory for parallel consumption
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        

        /*
            By configuring the ConcurrentKafkaListenerContainerFactory with the desired concurrency level, 
            you can achieve parallel consumption of Kafka messages in a Java Spring Boot application. 
            Adjust the concurrency value based on your application's requirements and the capacity of your Kafka cluster.
        */

        factory.setConcurrency(3); // Set the number of consumer threads
        return factory;
    }
}