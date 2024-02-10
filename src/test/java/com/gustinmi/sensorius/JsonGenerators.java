package com.gustinmi.sensorius;

public class JsonGenerators {

	public static String getSensorReading() {
		return JsonGenerators.getSensorReading(0, 0, null);
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

}
