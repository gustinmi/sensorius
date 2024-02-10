package com.gustinmi.sensorius;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeseriesDb {
	
	public static final Logger logger = LoggerFactory.getLogger(SensoriusApplication.class);
	
	public static final TimeseriesDb INSTANCE = new TimeseriesDb();
		
	private TimeseriesDb() { }
	
	
	/**
	 * We are retrieving copy a list so iterating is not a issue if data changes
	 * Data was also filtered before so we save what we get
	 * Ordering is actually not important as long as we have creation date as a index in database 
	 * @param dataList
	 */
	public void flushToDb(final List<SensorData> dataList) {

		for (final Iterator<SensorData> iterator = dataList.iterator(); iterator.hasNext();) {
			final SensorData sensorData2 = iterator.next();
			logger.info("Saving to timeseries {}", sensorData2.toString());
		}
		
	}
	
}
