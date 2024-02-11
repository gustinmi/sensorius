package com.gustinmi.sensorius;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import com.gustinmi.sensorius.kafka.SensorConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.VoidSerializer;

//@EmbeddedKafka(partitions = 1, topics = {"sens_data"})
@EmbeddedKafka
public class EmbeddedKafkaTests {
	
	public static final Logger logger = LoggerFactory.getLogger(EmbeddedKafkaTests.class);
	
	public static final int PARTITION_NUMBER = 1;
	public static final short REPLICATION_FACTOR = 1;
	public static final String TEMPLATE_TOPIC_NAME = "sensor-data";
	
	@Test
	public void testProducer(EmbeddedKafkaBroker embeddedKafka) throws Exception {
		
		embeddedKafka.addTopics(new NewTopic(TEMPLATE_TOPIC_NAME, PARTITION_NUMBER, REPLICATION_FACTOR));
        
		final String brokerList = embeddedKafka.getBrokersAsString();
		logger.info("Worker list: \n" + brokerList);
  
		Map<String, Object> consumerProps =  SensorConsumer.getConsumerProps(brokerList, "testT", "false"); // brokers, groupid, autocommit
		DefaultKafkaConsumerFactory<Void, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
		ContainerProperties containerProperties = new ContainerProperties(TEMPLATE_TOPIC_NAME);
		KafkaMessageListenerContainer<Void, String> msgListenerContainer = new KafkaMessageListenerContainer<>(cf, containerProperties);

		final BlockingQueue<ConsumerRecord<Void, String>> records = new LinkedBlockingQueue<>();
		
		//TODO error handling CommonErrorHandler commonErrorHandler = msgListenerContainer.getCommonErrorHandler();
		msgListenerContainer.setupMessageListener(new MessageListener<Void, String>() {

			@Override
			public void onMessage(ConsumerRecord<Void, String> msg) { // simple read, we do not commit offset
				logger.info("RECEIVED MSG " + msg.toString() + " on thread " + Thread.currentThread().threadId());
				records.add(msg);
			}

		});
		msgListenerContainer.setBeanName("embeddedKafkaTests");
		msgListenerContainer.start();

		logger.info("Partitions per topic: " + embeddedKafka.getPartitionsPerTopic());
		ContainerTestUtils.waitForAssignment(msgListenerContainer, 1); // embeddedKafka.getPartitionsPerTopic()); // must use same number of partitions
		
		Map<String, Object> producerProps = new HashMap<>();
		producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList);
		producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384");
		producerProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		producerProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "33554432");
		producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, VoidSerializer.class);
		producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);;
		
		ProducerFactory<Integer, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
		KafkaTemplate<Integer, String> template = new KafkaTemplate<>(pf);
		template.setDefaultTopic(TEMPLATE_TOPIC_NAME);
		
		// SEND  messages
		
		template.sendDefault("foo"); // send without key
		template.sendDefault("bar"); // TOPIC, partition key data
		
		AtomicBoolean msgArrived = new AtomicBoolean(false); 
		
		ExecutorService execService = Executors.newFixedThreadPool(1);
		for (int i = 0; i < 1; i++) {
	        execService.execute(() -> {
	            
	        	try {
					Thread.sleep(3500); // give it some time until messages start arriving
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        	
	        	ConsumerRecord<Void, String> record1 = null;
				try {
					record1 = records.poll(10, TimeUnit.SECONDS); // poll will block
					assertTrue(record1.key() == null);
					assertTrue(record1.value().equals("foo"));
					msgArrived.compareAndSet(false, true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
				
				try {
					record1 = records.poll(10, TimeUnit.SECONDS); // poll will block
				
					assertTrue(record1.key() == null);
					assertTrue(record1.partition() == 0);
					assertTrue(record1.value().equals("bar"));

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
	        });
	    }
		
		execService.awaitTermination(30_000, TimeUnit.MILLISECONDS);
		
		assertTrue(msgArrived.get() == true, "None of the messages arrived to consumers");
		
	}
   

}
