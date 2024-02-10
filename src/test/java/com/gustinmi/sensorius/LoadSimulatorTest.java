package com.gustinmi.sensorius;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gustinmi.sensorius.SensoriusJsonTests.*;

public class LoadSimulatorTest {
	
	public static final Logger logger = LoggerFactory.getLogger(SensoriusApplication.class);
	
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
	
	
	@Test
	@SuppressWarnings("unchecked")
	public void generateAnotherLoad() {
		
		// Create an array of CompletableFuture instances
        final CompletableFuture<Integer>[] completableFutures = new CompletableFuture[1];
        for (int idx = 0; idx < completableFutures.length; idx++) {
            int index = idx;
            completableFutures[idx] = CompletableFuture.supplyAsync(() -> {
            	for (int i = 0; i < 3; i++) {
            		logger.info("Simulating SENSOR1");
	        		final String json = getSensorOne(currentTimeMillis(), Randomizer.generateRandomFloat());
	        		SensorRegistry.INSTANCE.addSensorReading(json);
	        		try {
						Thread.sleep(Randomizer.getRandomNumber(1000, 5000));
					} catch (InterruptedException e) {}
				}
            	
                return index * 10;
            });
        }
        
        final CompletableFuture<Integer>[] completableFutures1 = new CompletableFuture[1];
        for (int idx = 0; idx < completableFutures1.length; idx++) {
            int index = idx;
            completableFutures1[idx] = CompletableFuture.supplyAsync(() -> {
            	for (int i = 0; i < 3; i++) {
            		logger.info("Simulating SENSOR2");
	        		final String json = getSensorTwo(currentTimeMillis(), Randomizer.generateRandomFloat());
	        		SensorRegistry.INSTANCE.addSensorReading(json);
	        		try {
						Thread.sleep(Randomizer.getRandomNumber(1000, 5000));
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
