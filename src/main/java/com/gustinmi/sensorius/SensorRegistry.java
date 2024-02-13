package com.gustinmi.sensorius;

import static com.gustinmi.sensorius.utils.CompilationConstants.*;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import com.gustinmi.sensorius.SensorData.SensId;
import com.gustinmi.sensorius.utils.LoggingFactory;

public class SensorRegistry {
	
	public static final Logger logger = LoggingFactory.loggerForThisClass();
	
    public static final SensorRegistry INSTANCE = new SensorRegistry();
    
    public static volatile FlushCondition flushCondition;

    private final AtomicBoolean initialized = new AtomicBoolean(false);
    
    /** A hash table supporting full concurrency of retrievals and high expected concurrency for updates. */
	private final Map<SensId, SortedSet<SensorData>> sensors = new ConcurrentHashMap<SensorData.SensId, SortedSet<SensorData>>(1000);
	
	private SensorRegistry() {
		flushCondition = new MaxAgeMaxSizeCondition(false); // default value  
	}
	
	public void initialize(boolean shouldFlush)	{

 		if (initialized.getAndSet(true)) {
 			logger.error("Already initialized");
            return;
        }

		if (shouldFlush) {
			logger.warn("Turning on flush mode");
			flushCondition = new MaxAgeMaxSizeCondition(true);
		}
	}
	
	
	/** Add new sensor reading from kafka consumer
	 * @param rawSensData
	 */
	public void addSensorReading(final String rawSensData) { 
		
		logger.info("Considering sensor unchecked reading {}", rawSensData);
		
		final SensorData sensData = SensorData.fromRaw(rawSensData);
		if (sensData == null) return; // nothing we can do about json syntax errors, drop it as soon as you can

		long tsStart = RealtimeRetriever.currentTimeMillis();
		
		// handle adding of sensors	
		if (sensors.containsKey(sensData.getId())) { // existing sensor
			
			if (INFO_SENSOR_REGISTRY) logger.info("Sensor {} already present, adding reading", sensData.getId().toString());
			
			final int readingSize;
			final SortedSet<SensorData> readingsData = sensors.get(sensData.getId());
			synchronized (readingsData) { 
				readingsData.add(sensData); // will skip readings with same timestamp
				readingSize = readingsData.size();
			}
			
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
				readingsData.clear();
			}
			
			// NO NEED TO LOCK, we are working with copy of data
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
			sensors.putIfAbsent(sensData.getId(), newDataSet);
			
			// IMPORTANT:  since this is fresh data, there is no need to save to database anythings
		}
		
		if (INFO_SENSOR_REGISTRY) logger.info("Total time processing {} ms", RealtimeRetriever.currentTimeMillis() - tsStart);
		
		return; // TODO change method to return object, its elegant for testing
		
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
		synchronized(sensors){
			sensors.clear();
		}
	}
		
	public int getSensorCount() {
		int size;
		synchronized(sensors){
			size = sensors.keySet().size();
		}
		return size;
	}
	
	public List<SensorData> getOrderedDataForSensor(final SensId id) {
		final SortedSet<SensorData> sortedSet = sensors.get(id);
		final List<SensorData> orderList = new ArrayList<SensorData>(sortedSet.size());
		synchronized (sortedSet) { // lock so we do not get concurrent modification exception if other thread is modifying collection
			for (final Iterator<SensorData> iterator = sortedSet.iterator(); iterator.hasNext();) {
				final SensorData sensorData = iterator.next();
				orderList.add(sensorData);
			}
			return orderList;	
		}
	}	

}
