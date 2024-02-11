package com.gustinmi.sensorius;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import static com.gustinmi.sensorius.JsonGenerators.*;

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
		
		final String raw = getReading();
		logger.info("Raw sensor data is {}", raw);
		final SensorData fromRaw = SensorData.fromRaw(raw);
		if (fromRaw == null) fail("Could not parse sensor data json");
		logger.info("Raw sensor data is {}", fromRaw.toString());
		
		final SensorData otherFromSameRaw = SensorData.fromRaw(raw);
		logger.info("Raw sensor data is {}", otherFromSameRaw.toString());
		
		assertTrue(fromRaw.equals(otherFromSameRaw));
		assertTrue(fromRaw.hashCode() == otherFromSameRaw.hashCode());
		
		final String rawAnother = JsonGenerators.getReading();
		logger.info("Another raw sensor data is {}", rawAnother);
		final SensorData fromRawAnother = SensorData.fromRaw(rawAnother);
		assertTrue(!fromRaw.equals(fromRawAnother));
	}

	

	@Test
	void testSensorCompareTimestamp() {
		
		long now = currentTimeMillis();
		final String raw1 = getReading(now, 200, (float) 1.0);
		final SensorData sd1 = SensorData.fromRaw(raw1);
		
		final String before = getReading(now - 100, 200, (float) 1.0);
		final SensorData sdBefore = SensorData.fromRaw(before);
		assertTrue(sd1.compareTo(sdBefore) == 1);
		
		final String after = getReading(now + 100, 200, (float) 1.0);
		final SensorData sdAfter = SensorData.fromRaw(after);
		assertTrue(sd1.compareTo(sdAfter) == -1);
		
		final String raw2 = getReading(now, 200, (float) 1.0);
		final SensorData sd2 = SensorData.fromRaw(raw2);
		assertTrue(sd1.compareTo(sd2) == 0);

		
		
		
	}

	
	

	@Test
	void createSensorDataInvalidJson() {
		final String malformedRaw = getSensorReadingMalformed();
		final SensorData malformed = SensorData.fromRaw(malformedRaw);
		assertTrue(malformed == null);
	}
	
	
	public static long currentTimeMillis() {
		return System.currentTimeMillis();
	}

}
