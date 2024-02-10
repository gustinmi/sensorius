package com.gustinmi.sensorius;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyFloat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SensorRegistryTest {

	public static final Logger logger = LoggerFactory.getLogger(App.class);
	
	@BeforeEach 
	public void setupTest() {
		SensorRegistry.INSTANCE.clear();
		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 0);
	}
	
	@Test
	public void testClear() {
		
		SensorRegistry.INSTANCE.addSensorReading(JsonGenerators.getSensorReading());
		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 1);
		SensorRegistry.INSTANCE.clear();
		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 0);
	}
	
	@Test
	public void testSensorDataSameSensor() {
		
		long now = SensorDataTests.currentTimeMillis();
		
		final String first = JsonGenerators.getSensorReading(now, 200, anyFloat());
		final SensorData fromRawFirst = SensorData.fromRaw(first);
		SensorRegistry.INSTANCE.addSensorReading(first);
		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 1);
		
		final String second = JsonGenerators.getSensorReading(now+1000, 200, anyFloat());
		SensorRegistry.INSTANCE.addSensorReading(second);
		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 1);
		
		final List<SensorData> orderedDataForSensor = SensorRegistry.INSTANCE.getOrderedDataForSensor(fromRawFirst.getId());
		assertTrue(orderedDataForSensor.size() == 2);
		
	}
	
	@Test
	public void testSensorDataInvalid() {
				
		final String second = JsonGenerators.getSensorReadingMalformed();
		SensorRegistry.INSTANCE.addSensorReading(second);
		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 0);
		
		
	}
	
	
	@Test
	public void testSensorCount() {
		long now = SensorDataTests.currentTimeMillis();
		
		final String first = JsonGenerators.getSensorReading(now, 200, anyFloat());
		SensorRegistry.INSTANCE.addSensorReading(first);
		
		final String beforeFirst = JsonGenerators.getSensorReading(now-1000, 200, anyFloat());
		SensorRegistry.INSTANCE.addSensorReading(beforeFirst);
		
		final String beforeBeforeFirst = JsonGenerators.getSensorReading(now-1200, 200, anyFloat());
		SensorRegistry.INSTANCE.addSensorReading(beforeBeforeFirst);

		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 1);
		
		// adding different sensor
		final String last = JsonGenerators.getSensorReading();
		SensorRegistry.INSTANCE.addSensorReading(last);
		
		assertFalse(SensorRegistry.INSTANCE.getSensorCount() == 1);
		assertTrue(SensorRegistry.INSTANCE.getSensorCount() == 2);
		
	}
	
	@Test
	public void testOrderingSensorData() {
		long now = SensorDataTests.currentTimeMillis();
		
		final String first = JsonGenerators.getSensorReading(now, 200, anyFloat());
		SensorRegistry.INSTANCE.addSensorReading(first);
		final SensorData fromRawFirst = SensorData.fromRaw(first);
			
		
		final String beforeFirst = JsonGenerators.getSensorReading(now-1000, 200, anyFloat());
		SensorRegistry.INSTANCE.addSensorReading(beforeFirst);
		final SensorData fromRawBeforeFirst = SensorData.fromRaw(beforeFirst);
		
		final String beforeBeforeFirst = JsonGenerators.getSensorReading(now-1200, 200, anyFloat());
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
	
}



































