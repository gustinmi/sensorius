package com.gustinmi.sensorius;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyFloat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.gustinmi.sensorius.SensorData.SensId;
import com.gustinmi.sensorius.kafka.KafkaConsumer;
import com.gustinmi.sensorius.utils.JsonGenerators;
import com.gustinmi.sensorius.utils.LoggingFactory;
import com.gustinmi.sensorius.utils.JsonGenerators.SensorMockData;

public class SensorRegistryTest {

	public static final Logger logger = LoggingFactory.loggerForThisClass();
		
	@BeforeEach 
	public void setupTest() {
		SensorRegistry.INSTANCE.clear();
		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 0);
	}
	
	@Test
	public void testClear() {
		SensorRegistry.INSTANCE.addSensorReading(JsonGenerators.getReading());
		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 1);
		SensorRegistry.INSTANCE.clear();
		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 0);
	}
	
	@Test
	public void testSensorDataSameSensor() {
		
		long now = SensorDataTests.currentTimeMillis();
		
		final String first = JsonGenerators.getReading(now, 200, anyFloat());
		final SensorData fromRawFirst = SensorData.fromRaw(first);
		
		SensorRegistry.INSTANCE.addSensorReading(first);
		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 1);
		
		final String second = JsonGenerators.getReading(now+1000, 200, anyFloat());
		SensorRegistry.INSTANCE.addSensorReading(second);
		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 1);
		
		final List<SensorData> orderedDataForSensor = SensorRegistry.INSTANCE.getOrderedDataForSensor(fromRawFirst.getId());
		assertTrue(orderedDataForSensor.size() == 2);
		
	}
	
	@Test
	public void testSensorDataInvalid() {
		@SuppressWarnings("deprecation")
		final String second = JsonGenerators.getSensorReadingMalformed();
		SensorRegistry.INSTANCE.addSensorReading(second);
		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 0);
	}
		
	@Test
	public void testSensorCount() {
		long now = SensorDataTests.currentTimeMillis();
		
		final String first = JsonGenerators.getReading(now, 200, anyFloat());
		SensorRegistry.INSTANCE.addSensorReading(first);
		
		final String beforeFirst = JsonGenerators.getReading(now-1000, 200, anyFloat());
		SensorRegistry.INSTANCE.addSensorReading(beforeFirst);
		
		final String beforeBeforeFirst = JsonGenerators.getReading(now-1200, 200, anyFloat());
		SensorRegistry.INSTANCE.addSensorReading(beforeBeforeFirst);

		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 1);
		
		// adding different sensor
		final String last = JsonGenerators.getReading();
		SensorRegistry.INSTANCE.addSensorReading(last);
		
		assertFalse(SensorRegistry.INSTANCE.getSensorCount() == 1);
		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 2);
		
	}
	
	@Test
	public void testOrderingSensorData() {
		long now = SensorDataTests.currentTimeMillis();
		
		final String first = JsonGenerators.getReading(now, 200, anyFloat());
		SensorRegistry.INSTANCE.addSensorReading(first);
		final SensorData fromRawFirst = SensorData.fromRaw(first);
		
		final String beforeFirst = JsonGenerators.getReading(now-1000, 200, anyFloat());
		SensorRegistry.INSTANCE.addSensorReading(beforeFirst);
		final SensorData fromRawBeforeFirst = SensorData.fromRaw(beforeFirst);
		
		final String beforeBeforeFirst = JsonGenerators.getReading(now-1200, 200, anyFloat());
		SensorRegistry.INSTANCE.addSensorReading(beforeBeforeFirst);
		final SensorData fromRawBeforeBeforeFirst = SensorData.fromRaw(beforeBeforeFirst);
		
		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 1);
		
		final List<SensorData> orderedDataForSensor = SensorRegistry.INSTANCE.getOrderedDataForSensor(fromRawFirst.getId());
		SensorData sensorData1 = orderedDataForSensor.get(0);
		assertFalse(sensorData1.equals(fromRawFirst));
		
		assertTrue(sensorData1.equals(fromRawBeforeBeforeFirst));
		SensorData sensorData2 = orderedDataForSensor.get(1);
		assertTrue(sensorData2.equals(fromRawBeforeFirst));
		SensorData sensorData3 = orderedDataForSensor.get(2);
		assertTrue(sensorData3.equals(fromRawFirst));
		
	}
	

	@Test
	public void testSameTimestampReadingsAreIgnored() {
		
		long now = SensorDataTests.currentTimeMillis();

		final String firstRaw = JsonGenerators.getReading(
				new SensorMockData()
					.setElevation(200)
					.setTemperature((float) 1.2)
					.setTimeMillis(now));
		
		final SensorData first = SensorData.fromRaw(firstRaw);
		final SensId sensId = first.getId();
		
		SensorRegistry.INSTANCE.addSensorReading(firstRaw);
		
		SensorRegistry.INSTANCE.addSensorReading(JsonGenerators.getReading(
				new SensorMockData()
					.setElevation(200)
					.setTemperature((float) 1.2)
					.setTimeMillis(now + 5)));
		
		SensorRegistry.INSTANCE.addSensorReading(JsonGenerators.getReading(
				new SensorMockData()
					.setElevation(200)
					.setTemperature((float) 1.2)
					.setTimeMillis(now + 5)));
		
		final List<SensorData> currReadings = SensorRegistry.INSTANCE.getOrderedDataForSensor(sensId);
		assertTrue(currReadings.size() == 2, String.format("There should be 4 sensor reading but we found %s", currReadings.size()));
		
	}
	
	
	
	
	@Test
	public void testExtractUniqReadings() {
		
		long now = SensorDataTests.currentTimeMillis();

		final String firstRaw = JsonGenerators.getReading(
				new SensorMockData()
					.setElevation(200)
					.setTemperature((float) 1.2)
					.setTimeMillis(now));
		
		final SensorData first = SensorData.fromRaw(firstRaw);
		SensId sensId = first.getId();
		
		int uniqCount = 0;
		SensorRegistry.INSTANCE.addSensorReading(firstRaw);
		uniqCount++;
		
		SensorRegistry.INSTANCE.addSensorReading(JsonGenerators.getReading(
				new SensorMockData()
					.setElevation(200)
					.setTemperature((float) 1.2)
					.setTimeMillis(now + 5)));
		uniqCount++;
		
		SensorRegistry.INSTANCE.addSensorReading(JsonGenerators.getReading(
				new SensorMockData()
					.setElevation(200)
					.setTemperature((float) 1.2)
					.setTimeMillis(now + 7)));
		uniqCount++;
		
		SensorRegistry.INSTANCE.addSensorReading(JsonGenerators.getReading(
				new SensorMockData()
					.setElevation(200)
					.setTemperature((float) 1.5)
					.setTimeMillis(now + 12)));
		uniqCount++;
		
		SensorRegistry.INSTANCE.addSensorReading(JsonGenerators.getReading(
				new SensorMockData()
					.setElevation(200)
					.setTemperature((float) 1.7)
					.setTimeMillis(now + 13)));
		uniqCount++;
		
		SensorRegistry.INSTANCE.addSensorReading(JsonGenerators.getReading(
				new SensorMockData()
					.setElevation(200)
					.setTemperature((float) 1.7)
					.setTimeMillis(now + 14)));
		uniqCount++;
		
		SensorRegistry.INSTANCE.addSensorReading(JsonGenerators.getReading(
				new SensorMockData()
					.setElevation(200)
					.setTemperature((float) 1.7)
					.setTimeMillis(now + 15)));
		uniqCount++;
		
		SensorRegistry.INSTANCE.addSensorReading(JsonGenerators.getReading(
				new SensorMockData()
					.setElevation(200)
					.setTemperature((float) 1.7)
					.setTimeMillis(now + 16)));
		uniqCount++;
		
		SensorRegistry.INSTANCE.addSensorReading(JsonGenerators.getReading(
				new SensorMockData()
					.setElevation(200)
					.setTemperature((float) 1.7)
					.setTimeMillis(now + 17)));
		uniqCount++;
		
		SensorRegistry.INSTANCE.addSensorReading(JsonGenerators.getReading(
				new SensorMockData()
					.setElevation(200)
					.setTemperature((float) 1.7)
					.setTimeMillis(now + 18)));
		uniqCount++;
		
		SensorRegistry.INSTANCE.addSensorReading(JsonGenerators.getReading(
				new SensorMockData()
					.setElevation(200)
					.setTemperature((float) 1.7)
					.setTimeMillis(now + 19)));
		uniqCount++;
		
		final List<SensorData> orderedDataForSensor = SensorRegistry.INSTANCE.getOrderedDataForSensor(sensId);
		assertTrue(orderedDataForSensor.size() == uniqCount, String.format("There should be %s sensor reading but we found only %s", uniqCount, orderedDataForSensor.size()));
		
		
	}
	
}



































