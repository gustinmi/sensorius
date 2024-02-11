package com.gustinmi.sensorius;

public class JsonGenerators {
	
	public static class SensorMockData {

		private long timeMillis;
		private int elevation;
		private Float temperature;

		public SensorMockData setTimeMillis(long timeMillis) {
			this.timeMillis = timeMillis;
			return this;
		}
		
		public SensorMockData setElevation(int elevation) {
			this.elevation = elevation;
			return this;
		}
		
		public SensorMockData setTemperature(Float temperature) {
			this.temperature = temperature;
			return this;
		}

	}

	public static String getReading(SensorMockData d) {
		return JsonGenerators.getReading(d.timeMillis, d.elevation, d.temperature);
	}

	public static String getReading() {
		return JsonGenerators.getReading(0, 0, null);
	}

	public static String getReading(long timeMillis, int elevation, Float temperature) {
		final StringBuilder reading = new StringBuilder();
		reading.append("{");
		reading.append("\"longitude\": \"46°04'53.6\\\"N\"");
		reading.append(",\"latitude\": \"14°29'43.5\\\"E\"");
		
		if (elevation == 0)
			reading.append(",\"elevation\": " + String.format("%s", Randomizer.getRandomNumber(290, 293)));
		else
			reading.append(",\"elevation\": " + String.format("%s", elevation));
		
		if (timeMillis == 0)
			reading.append(",\"timestamp\": " + String.format("%s", SensorDataTests.currentTimeMillis()));
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
	public static String getSensorReadingMalformed() {
		final StringBuilder reading = new StringBuilder();
		reading.append("{");
		reading.append("\"longitude\": \"46°04'53.6\\\"N\"");
		reading.append("itude\": \"14°29'43.5\\\"E\"");
		reading.append(",\"elevation\": " + String.format("%s", Randomizer.getRandomNumber(290, 293)) );
		reading.append("\"timestamp\": " + String.format("%s", SensorDataTests.currentTimeMillis()));
		reading.append(",\"temperature\": 2.22");
		reading.append("");
		return reading.toString();
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

}
