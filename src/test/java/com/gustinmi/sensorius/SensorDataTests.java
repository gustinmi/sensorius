package com.gustinmi.sensorius;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SensorDataTests {
	
	public static final Logger logger = LoggerFactory.getLogger(App.class);

	@Test
	void createSensorData() {
		logger.info("Creating demo sensor data");
		
		final String raw = JsonGenerators.getSensorReading();
		logger.info("Raw sensor data is {}", raw);
		final SensorData fromRaw = SensorData.fromRaw(raw);
		if (fromRaw == null) fail("Could not parse sensor data json");
		logger.info("Raw sensor data is {}", fromRaw.toString());
		
		final SensorData otherFromSameRaw = SensorData.fromRaw(raw);
		logger.info("Raw sensor data is {}", otherFromSameRaw.toString());
		
		assertTrue(fromRaw.equals(otherFromSameRaw));
		assertTrue(fromRaw.hashCode() == otherFromSameRaw.hashCode());
				
		
		final String rawAnother = JsonGenerators.getSensorReading();
		logger.info("Another raw sensor data is {}", rawAnother);
		final SensorData fromRawAnother = SensorData.fromRaw(rawAnother);
		assertTrue(!fromRaw.equals(fromRawAnother));
	}
	
	

	@Test
	void createSensorDataInvalidJson() {
		final String malformedRaw = JsonGenerators.getSensorReadingMalformed();
		final SensorData malformed = SensorData.fromRaw(malformedRaw);
		assertTrue(malformed == null);
	}
	
	
	public static long currentTimeMillis() {
		return System.currentTimeMillis();
	}

}
