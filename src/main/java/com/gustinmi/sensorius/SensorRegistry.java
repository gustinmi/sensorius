package com.gustinmi.sensorius;

import static com.gustinmi.sensorius.CompilationConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gustinmi.sensorius.SensorData.SensId;

public class SensorRegistry {
	
	public static final Logger logger = LoggerFactory.getLogger(SensorRegistry.class);
	
    public static final SensorRegistry INSTANCE = new SensorRegistry();
    
    public static volatile FlushCondition flushCondition;
    
	private final Map<SensId, SortedSet<SensorData>> sensors = new HashMap<SensorData.SensId, SortedSet<SensorData>>(1000);
	
	private SensorRegistry() {
		flushCondition = new MaxAgeMaxSizeCondition(false);
	}
	
	public void initialize(boolean shouldFlush)	{
		if (shouldFlush) {
			logger.warn("Turning on flush mode");
			flushCondition = new MaxAgeMaxSizeCondition(true);
		}
	}
	
	
	/** Add new sensor reading from kafka consumer
	 * @param rawSensData
	 */
	public void addSensorReading(final String rawSensData) { // TODO change method to return object, its elegant for testing
		
		logger.info("Considering sensor unchecked reading {}", rawSensData);
		
		final SensorData sensData = SensorData.fromRaw(rawSensData);
		if (sensData == null) return; // nothing we can do about json syntax errors, drop it as soon as you can

		// handle adding of sensors	
		if (sensors.containsKey(sensData.getId())) { // existing sensor
			
			if (INFO_SENSOR_REGISTRY) logger.info("Sensor {} already present, adding reading", sensData.getId().toString());
			
			final SortedSet<SensorData> readingsData = sensors.get(sensData.getId());
			readingsData.add(sensData); // will skip readings with same timestamp
			
			if (INFO_SENSOR_REGISTRY) logger.info("Sensor has total of {} readings", readingsData.size());
			
			// Check if we need to flush data
			long currReadingTsMs = sensData.getTimestamp();
			long firstReadingTsMs = readingsData.first().getTimestamp();
			final boolean shouldFlush = flushCondition.shouldFlush(readingsData.size(), firstReadingTsMs, currReadingTsMs);
			if (!shouldFlush) return;

			logger.info("Flushing sensor data for {}", sensData.getId().toString());
			
			// we are going to empty buffer for sensor	
			final SortedSet<SensorData> copyOfDataset; // work with a copy of the list, so we don't deadlock
			synchronized (readingsData) { //TODO check threading
				copyOfDataset = new TreeSet<SensorData>(readingsData);
			}
			readingsData.clear(); //TODO check threading 
			
			final List<SensorData> uniqList = extractUniqReadings(copyOfDataset);
			
			try {
				final int numOfSaved = TimeseriesDb.INSTANCE.flushToDb(uniqList);
				if (numOfSaved != uniqList.size())
					logger.warn("Not all items flushed to database. Diff is {}", uniqList.size() - numOfSaved);
				if (INFO_SENSOR_REGISTRY) logger.info("Flushed {} items", numOfSaved);
				
				
			} catch (RuntimeException e) {
				logger.error("Exception while saving new readings into timeseries db: " + e.toString(), e);
			} 
			
			
		} else { // sensor appeared for the first time
			
			if (INFO_SENSOR_REGISTRY) logger.info("Adding new sensor {} with 1 reading data", sensData.getId().toString());
			
			final SortedSet<SensorData> newDataSet = new TreeSet<SensorData>();
			newDataSet.add(sensData);
			sensors.put(sensData.getId(), newDataSet);
			
			// IMPORTANT:  since this is fresh data, there is no need to save to database anythings
		}
				
		return;
		
	}
	
	public static List<SensorData> extractUniqReadings(final SortedSet<SensorData> copyOfDataset) {
		Float lastTemperature = null;
		final List<SensorData> uniqList = new ArrayList<SensorData>(10);
		final Iterator<SensorData> iterator = copyOfDataset.iterator();
        while (iterator.hasNext()) {
        	
        	final SensorData candidate = iterator.next();
        	if (INFO_SENSOR_REGISTRY) logger.info("Considering data for persistence: {}", candidate.toString());
        	
        	if (lastTemperature == null) { // first element
        		if (INFO_SENSOR_REGISTRY) logger.info("Adding first element");
        		lastTemperature = candidate.getTemperature();
        		uniqList.add(candidate);
        		continue;
        	}
        	
        	if (candidate.getTemperature() == lastTemperature) continue; // same reading
        	else { // we have temperature change
				if (INFO_SENSOR_REGISTRY) logger.info("Adding new persistence candidate");
				uniqList.add(candidate);
				lastTemperature = candidate.getTemperature();
				continue;
			}	
        }
        
        return uniqList;
	}
	
	
	
	public void clear() {
		sensors.clear();
	}
		
	public int getSensorCount() {
		return sensors.keySet().size();
	}
	
	public List<SensorData> getOrderedDataForSensor(final SensId id) {
		synchronized (sensors) {
			final SortedSet<SensorData> sortedSet = sensors.get(id);
			final List<SensorData> orderList = new ArrayList<SensorData>(sortedSet.size());
			for (final Iterator<SensorData> iterator = sortedSet.iterator(); iterator.hasNext();) {
				final SensorData sensorData = iterator.next();
				orderList.add(sensorData);
			}
			return orderList;	
		}
	}	
	
	

}
