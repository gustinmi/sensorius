package com.gustinmi.sensorius;

import static com.gustinmi.sensorius.utils.CompilationConstants.*;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;

import com.gustinmi.sensorius.utils.LoggingFactory;

/**
 * Mock implementation of timeseries db adapter (like influx)
 * @see https://medium.berkayozcan.com/connecting-an-influxdb-database-to-a-spring-boot-application-81f53ead3c7
 * @see https://medium.com/trabe/monitoring-humidity-and-temperature-with-grafana-influxdb-and-orange-pi-9680046c70c
 */
public class TimeseriesDb {
	
	public static final Logger logger = LoggingFactory.loggerForThisClass();
	
	public static final TimeseriesDb INSTANCE = new TimeseriesDb();
		
	private TimeseriesDb() { }
	
	/**
	 * We are retrieving copy a list so iterating is not a issue if data changes
	 * Data was also filtered before so we save what we get here
	 * Ordering is actually not important as long as we have creationDate as a index in database 
	 * @param dataList
	 * @return 
	 */
	public int flushToDb(final List<SensorData> dataList) {
		int numOfSaved = 0;
		logger.info("Trying to saving to timeseries {} number of items", dataList.size());
		for (final Iterator<SensorData> iterator = dataList.iterator(); iterator.hasNext();) {
			final SensorData sd = iterator.next();
			if (INFO_TIMESERIES) logger.info("Saving timeserie {}", sd.toString());
			numOfSaved++;
		}
		return numOfSaved;
	}
	
}
