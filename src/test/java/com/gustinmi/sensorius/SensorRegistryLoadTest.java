package com.gustinmi.sensorius;

import static com.gustinmi.sensorius.SensorDataTests.currentTimeMillis;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * With this class we are generating load on sensor registry
 * We kind of abuse unitest for simulation
 */
@SpringBootTest
public class SensorRegistryLoadTest {
	
	/* SENSOR1 load settings */
	public static final int SENS1_MIN_DELAY = 5_00; // lower means faster
	public static final int SENS1_MAX_DELAY = 7_00; // lower means faster
	public static final int SENS1_SAMPLE_NUM = 10;
	
	/* SENSOR2 load settings */
	public static final int SENS2_MIN_DELAY = 2_00; // lower means faster
	public static final int SENS2_MAX_DELAY = 3_00; // lower means faster
	public static final int SENS2_SAMPLE_NUM = 20;
	
	private static final boolean FLUSH_ON = true;
	
	public static final Logger logger = LoggerFactory.getLogger(App.class);
	
	static {
		SensorRegistry.INSTANCE.initialize(FLUSH_ON);
	}
	
	@BeforeEach 
	public void setupTest() {
		SensorRegistry.INSTANCE.clear();	
	}
	
	public static String getSensorOne(long timeMillis, Float temperature) {
		final StringBuilder reading = new StringBuilder();
		reading.append("{");
		
		// ID 
		reading.append("\"longitude\": \"46°04'53.6\\\"N\"");
		reading.append(",\"latitude\": \"14°29'43.5\\\"E\"");
		reading.append(",\"elevation\": " + String.format("%s", 200));
		
		// DATA
		reading.append(",\"timestamp\": " + String.format("%s", timeMillis));
		reading.append(",\"temperature\": " + temperature.toString());
		
		reading.append("}");
		return reading.toString();
	}
	
	public static String getSensorTwo(long timeMillis, Float temperature) {
		final StringBuilder reading = new StringBuilder();
		reading.append("{");
		
		// ID 
		reading.append("\"longitude\": \"46°04'53.6\\\"N\"");
		reading.append(",\"latitude\": \"14°29'43.5\\\"E\"");
		reading.append(",\"elevation\": " + String.format("%s", 300));
		
		// DATA
		reading.append(",\"timestamp\": " + String.format("%s", timeMillis));
		reading.append(",\"temperature\": " + temperature.toString());
		
		reading.append("}");
		return reading.toString();
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
	        		final String json = getSensorOne(currentTimeMillis(), Randomizer.generateRandomFloat());
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
	        		final String json = getSensorTwo(currentTimeMillis(), Randomizer.generateRandomFloat());
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
