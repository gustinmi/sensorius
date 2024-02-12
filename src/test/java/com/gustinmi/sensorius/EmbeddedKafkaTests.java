package com.gustinmi.sensorius;

import static com.gustinmi.sensorius.SensorDataTests.currentTimeMillis;
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
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.apache.kafka.common.serialization.VoidSerializer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.gustinmi.sensorius.utils.JsonGenerators;
import com.gustinmi.sensorius.utils.LoggingFactory;

//@EmbeddedKafka(partitions = 1, topics = {"sens_data"})
@EmbeddedKafka(ports = {1234})
public class EmbeddedKafkaTests {
	
	public static final Logger logger = LoggingFactory.loggerForThisClass();
	
	public static final int PARTITION_NUMBER = 1;
	
	public static final short REPLICATION_FACTOR = 1;
	
	/** all consumers must be part of same group so that message in partitioned topic is consumed only once */
	public static final String GROUP_ID = "testT";
	
	public static final String TEMPLATE_TOPIC_NAME = "sensor-data";
	
	//TODO error handling CommonErrorHandler commonErrorHandler = msgListenerContainer.getCommonErrorHandler();
	
	
	public static Map<String, Object> getConsumerProps(String brokers, String group, String autoCommit) {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommit);
		props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "10");
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		return props;
	}
	
	
	
	@Test
	public void testProducer(EmbeddedKafkaBroker embeddedKafka) throws Exception {
		
		embeddedKafka.addTopics(new NewTopic(TEMPLATE_TOPIC_NAME, PARTITION_NUMBER, REPLICATION_FACTOR));
        
		final String brokerList = embeddedKafka.getBrokersAsString();
		logger.info("Worker list: \n" + brokerList);
  
		Map<String, Object> consumerProps =  getConsumerProps(brokerList, GROUP_ID, "false"); // brokers, groupid, autocommit
		DefaultKafkaConsumerFactory<Void, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
		ContainerProperties containerProperties = new ContainerProperties(TEMPLATE_TOPIC_NAME);
		KafkaMessageListenerContainer<Void, String> msgListenerContainer = new KafkaMessageListenerContainer<>(cf, containerProperties);

		// All messages well be read and copied into this buffer queue
		final BlockingQueue<ConsumerRecord<Void, String>> records = new LinkedBlockingQueue<>();
		
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
		
		final long ts1 = currentTimeMillis();
		final String json1 = JsonGenerators.getSensorOne(ts1, (float) 20.0);
		template.sendDefault(json1); 
		
		final long ts2 = currentTimeMillis();
		final String json2 = JsonGenerators.getSensorTwo(ts2, (float) 21.0);
		template.sendDefault(json2);
				
		final AtomicBoolean msgArrived = new AtomicBoolean(false); 
		
		ExecutorService execService = Executors.newFixedThreadPool(1);
		for (int i = 0; i < 1; i++) {
	        execService.execute(() -> {
	            
	        	try {
					Thread.sleep(3500); // give it some time until messages start arriving
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        	
	        	// poll buffer queue and block/wait until message arrives
	        	ConsumerRecord<Void, String> record1 = null;
				try {
					record1 = records.poll(10, TimeUnit.SECONDS); // poll will block
					assertTrue(record1.key() == null);
					assertTrue(record1.partition() == 0);
					
					final SensorData sd = SensorData.fromRaw(record1.value());
					assertTrue(sd.getTemperature() == (float) 20.0);
					assertTrue(sd.getTimestamp() == ts1);

					msgArrived.compareAndSet(false, true);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
				
				try {
					record1 = records.poll(10, TimeUnit.SECONDS); // poll will block
				
					assertTrue(record1.key() == null);
					assertTrue(record1.partition() == 0);
					

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				return;
	        });
	    }
		
		execService.awaitTermination(15_000, TimeUnit.MILLISECONDS);
		
		assertTrue(msgArrived.get() == true, "None of the messages arrived to consumers");
		
	}
   

}
