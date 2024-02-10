package com.gustinmi.sensorius;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SensoriusJsonTests {
	
	public static final Logger logger = LoggerFactory.getLogger(SensoriusApplication.class);

	@Test
	void createSensorData() {
		logger.info("Creating demo sensor data");
		
		final String raw = getSensorReading();
		logger.info("Raw sensor data is {}", raw);
		final SensorData fromRaw = SensorData.fromRaw(raw);
		if (fromRaw == null) fail("Could not parse sensor data json");
		logger.info("Raw sensor data is {}", fromRaw.toString());
		
		final SensorData otherFromRaw = SensorData.fromRaw(raw);
		logger.info("Raw sensor data is {}", otherFromRaw.toString());
		
		assertTrue(fromRaw.equals(otherFromRaw));
		assertTrue(fromRaw.hashCode() == otherFromRaw.hashCode());
		
		final String rawAnother = getSensorReading();
		logger.info("Another raw sensor data is {}", rawAnother);
		final SensorData fromRawAnother = SensorData.fromRaw(rawAnother);
		
		assertTrue(!fromRaw.equals(fromRawAnother));
		 
		final String malformedRaw = getSensorReadingMalformed();
		final SensorData malformed = SensorData.fromRaw(malformedRaw);
		assertTrue(malformed == null);
		
	}
	
	public static String getSensorReading() {
		return getSensorReading(0, 0, null);
	}


	public static String getSensorReading(long timeMillis, int elevation, Float temperature) {
		final StringBuilder reading = new StringBuilder();
		reading.append("{");
		reading.append("\"longitude\": \"46°04'53.6\\\"N\"");
		reading.append(",\"latitude\": \"14°29'43.5\\\"E\"");
		
		if (elevation == 0)
			reading.append(",\"elevation\": " + String.format("%s", Randomizer.getRandomNumber(290, 293)));
		else
			reading.append(",\"elevation\": " + String.format("%s", elevation));
		
		if (timeMillis == 0)
			reading.append(",\"timestamp\": " + String.format("%s", currentTimeMillis()));
		else
			reading.append(",\"timestamp\": " + String.format("%s", timeMillis));
		
		if (temperature!=null && temperature > 0)
			reading.append(",\"temperature\": " + temperature.toString());
		else
			reading.append(",\"temperature\": 2.22");
		
		reading.append("}");
		return reading.toString();
	}

	@Deprecated(since = "Just for testing")
	private String getSensorReadingMalformed() {
		final StringBuilder reading = new StringBuilder();
		reading.append("{");
		reading.append("\"longitude\": \"46°04'53.6\\\"N\"");
		reading.append("itude\": \"14°29'43.5\\\"E\"");
		reading.append(",\"elevation\": " + String.format("%s", Randomizer.getRandomNumber(290, 293)) );
		reading.append("\"timestamp\": " + String.format("%s", currentTimeMillis()));
		reading.append(",\"temperature\": 2.22");
		reading.append("");
		return reading.toString();
	}
	
		

	public static long currentTimeMillis() {
		return System.currentTimeMillis();
	}

}
