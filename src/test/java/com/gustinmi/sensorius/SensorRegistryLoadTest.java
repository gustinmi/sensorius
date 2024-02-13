package com.gustinmi.sensorius;

import static com.gustinmi.sensorius.SensorDataTests.currentTimeMillis;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.gustinmi.sensorius.kafka.KafkaConsumer;
import com.gustinmi.sensorius.utils.JsonGenerators;
import com.gustinmi.sensorius.utils.LoggingFactory;
import com.gustinmi.sensorius.utils.Randomizer;

/**
 * With this class we are generating load on sensor registry
 * We kind of abuse unitest for simulation
 */
//@SpringBootTest
//@EnableAutoConfiguration(exclude=KafkaConsumer.class)
public class SensorRegistryLoadTest {
	
	public static final Logger logger = LoggingFactory.loggerForThisClass();
		
	/* SENSOR1 load settings */
	public static final int SENS1_MIN_DELAY = 5_00; // lower means faster
	public static final int SENS1_MAX_DELAY = 7_00; // lower means faster
	public static final int SENS1_SAMPLE_NUM = 10;
	
	/* SENSOR2 load settings */
	public static final int SENS2_MIN_DELAY = 2_00; // lower means faster
	public static final int SENS2_MAX_DELAY = 3_00; // lower means faster
	public static final int SENS2_SAMPLE_NUM = 20;
	
	private static final boolean FLUSH_ON = true;
	
	static {
		SensorRegistry.INSTANCE.initialize(FLUSH_ON);
	}
	
	@BeforeEach 
	public void setupTest() {
		SensorRegistry.INSTANCE.clear();	
	}
	
	//@Disabled
	@Test
	@SuppressWarnings("unchecked")
	public void generateAnotherLoad() {
		
		// Create an array of CompletableFuture instances
        final CompletableFuture<Integer>[] completableFutures = new CompletableFuture[1];
        for (int idx = 0; idx < completableFutures.length; idx++) {
            int index = idx;
            completableFutures[idx] = CompletableFuture.supplyAsync(() -> {
            	for (int i = 0; i < SENS1_SAMPLE_NUM; i++) {
            		logger.info("Simulating SENSOR1 reading");
	        		final String json = JsonGenerators.getSensorOne(currentTimeMillis(), Randomizer.generateRandomFloat());
	        		SensorRegistry.INSTANCE.addSensorReading(json);
	        		try {
						Thread.sleep(Randomizer.getRandomNumber(SENS1_MIN_DELAY, SENS1_MAX_DELAY));
					} catch (InterruptedException e) {}
				}
            	
                return index * 10;
            });
        }
        
        final CompletableFuture<Integer>[] completableFutures1 = new CompletableFuture[1];
        for (int idx = 0; idx < completableFutures1.length; idx++) {
            int index = idx;
            completableFutures1[idx] = CompletableFuture.supplyAsync(() -> {
            	for (int i = 0; i < SENS2_SAMPLE_NUM; i++) {
            		logger.info("Simulating SENSOR2 reading");
	        		final String json = JsonGenerators.getSensorTwo(currentTimeMillis(), Randomizer.generateRandomFloat());
	        		SensorRegistry.INSTANCE.addSensorReading(json);
	        		try {
						Thread.sleep(Randomizer.getRandomNumber(SENS2_MIN_DELAY, SENS2_MAX_DELAY));
					} catch (InterruptedException e) {}
				}
                return index * 10;
            });
        }
        
        // Join both arrays
        final CompletableFuture<Void> allOf = CompletableFuture.allOf(completableFutures)
        		.thenCombine(CompletableFuture.allOf(completableFutures1), (result1, result2) -> null);
        
        allOf.join(); // Join to block the current thread until all tasks are completed
		
	}

}
