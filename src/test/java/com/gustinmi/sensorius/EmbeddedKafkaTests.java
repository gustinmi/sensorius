package com.gustinmi.sensorius;

import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

//@EmbeddedKafka(partitions = 1, topics = {"sens_data"})
@EmbeddedKafka
public class EmbeddedKafkaTests {

	
	public static int PARTITION_NUMBER = 1;
	public static short REPLICATION_FACTOR = 1;
	public static String SD_TOPIC_NAME = "sensordata";
	
	private static final String TEMPLATE_TOPIC = "templateTopic";
	
    //@Test
    public void test(EmbeddedKafkaBroker embeddedKafka) {
    	
    	embeddedKafka.addTopics(new NewTopic(SD_TOPIC_NAME, PARTITION_NUMBER, REPLICATION_FACTOR));
    	
        final String brokerList = embeddedKafka.getBrokersAsString();
        
        System.out.println("Worker list: \n" + brokerList);
   
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        ConsumerFactory<Integer, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        Consumer<Integer, String> consumer = cf.createConsumer();
                
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, SD_TOPIC_NAME);
        
        
        
        ConsumerRecords<Integer, String> replies = KafkaTestUtils.getRecords(consumer);
        assertThat(replies.count()).isGreaterThanOrEqualTo(1);
    }
    
	@Test
	public void testProducer(EmbeddedKafkaBroker embeddedKafka) throws Exception {
		
		embeddedKafka.addTopics(new NewTopic(TEMPLATE_TOPIC, PARTITION_NUMBER, REPLICATION_FACTOR));
		
		//                                                               GROUP   AUTO COMMIT  
		Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testT", "false", embeddedKafka);
		DefaultKafkaConsumerFactory<Integer, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
		ContainerProperties containerProperties = new ContainerProperties(TEMPLATE_TOPIC);
		
		// integer string consumer
		KafkaMessageListenerContainer<Integer, String> msgListenerContainer = new KafkaMessageListenerContainer<>(cf, containerProperties);

		final BlockingQueue<ConsumerRecord<Integer, String>> records = new LinkedBlockingQueue<>();
		msgListenerContainer.setupMessageListener(new MessageListener<Integer, String>() {

			@Override
			public void onMessage(ConsumerRecord<Integer, String> record) { // simple read, we do not commit offset
				System.out.println("RECEIVED MSG " + record.toString());
				records.add(record);
			}

		});
		msgListenerContainer.setBeanName("embeddedKafkaTests");
		msgListenerContainer.start();

		System.out.println("Partitions per topic: " + embeddedKafka.getPartitionsPerTopic());
		
		ContainerTestUtils.waitForAssignment(msgListenerContainer, 1); // embeddedKafka.getPartitionsPerTopic()); // must use same number of partitions
		Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
		ProducerFactory<Integer, String> pf = new DefaultKafkaProducerFactory<>(producerProps);
		KafkaTemplate<Integer, String> template = new KafkaTemplate<>(pf);
		template.setDefaultTopic(TEMPLATE_TOPIC);
		
		template.sendDefault("foo"); // send without key
		template.sendDefault(0, 2, "bar"); // TOPIC, partition key data
		
		ExecutorService service = Executors.newFixedThreadPool(1);
		for (int i = 0; i < 1; i++) {
	        service.execute(() -> {
	            
	        	try {
					Thread.sleep(1500); // wait until messages start arriving
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        	
	        	ConsumerRecord<Integer, String> record1 = null;
				try {
					record1 = records.poll(10, TimeUnit.SECONDS); // poll will block
					assertTrue(record1.value() == "foo");
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
				
				try {
					record1 = records.poll(10, TimeUnit.SECONDS); // poll will block
				
					assertTrue(record1.key() == 2);
					assertTrue(record1.partition() == 0);
					assertTrue(record1.value() == "bar");

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
	        });
	    }

	}
   

}
