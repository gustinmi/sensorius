package com.gustinmi.sensorius;

import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Immutable object holding sensor data
 */
public final class SensorData  implements Comparable<SensorData> {
	
	private static final Gson gson = new Gson();
	
	private transient final SensId id;
	
	private final String longitude;
	private final String latitude;
	private final int elevation;
	
	private final Float temperature;
	private final long timestamp;
	
	private SensorData(final String longitude, final String latitude, final int elevation, final float temperature, final long timestamp) {
		this.temperature = temperature;
		this.timestamp = timestamp;
		this.longitude = longitude;
		this.latitude = latitude;
		this.elevation = elevation;
		
		this.id = SensId.fromIdData(longitude, latitude, elevation);
		
	}
	
	/** factory method for producing sensor data object from raw json string */
	public static final SensorData fromRaw(final String rawSensData) {
		// we use gson object deserializer for now. Could choose something more fundamental
		
		try {
			final SensorData sensDataUnbaked = gson.fromJson(rawSensData, SensorData.class); // this will only call default parameterless constructor
			return new SensorData(sensDataUnbaked.longitude, sensDataUnbaked.latitude, sensDataUnbaked.elevation, sensDataUnbaked.temperature, sensDataUnbaked.timestamp);
		} catch (JsonSyntaxException e) { // we can have anything in kafka data
			return null; 
		}
	}
		
	@Override
	public String toString() {
		return "SensorData [longitude=" + id.getLongitude() + ", latitude=" + id.getLatitude() + ", elevation=" + id.getElevation() + ", temperature=" + temperature + ", timestamp=" + timestamp + "]";
	}

	public SensId getId() {
		return id;
	}

	public float getTemperature() {
		return temperature;
	}
	
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, temperature, timestamp);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final SensorData other = (SensorData) obj;
		return this.id.equals(other.id)
				&& Float.floatToIntBits(temperature) == Float.floatToIntBits(other.temperature)
				&& timestamp == other.timestamp;
	}

	/**
	 * This will order data in realtime (based on timestamp)
	 * In this way we'll be able to eliminate same readings in given frame
	 */
	@Override
	public int compareTo(final SensorData that) {
		
		if (this.timestamp < that.timestamp) return -1;
        if (this.timestamp > that.timestamp) return 1;
        return 0;
	}
	
	
	/**
	 * immutable object for identifyind sensors
	 */
	public static final class SensId  {
		
		private final String longitude;
		private final String latitude;
		private final int elevation;
		
		public SensId(String longitude, String latitude, int elevation) {
			this.longitude = longitude;
			this.latitude = latitude;
			this.elevation = elevation;
		}
		
		public String getLongitude() {
			return longitude;
		}

		public String getLatitude() {
			return latitude;
		}

		public int getElevation() {
			return elevation;
		}

		public static final SensId fromIdData(String longitude, String latitude, int elevation) {
			return new SensId(longitude, latitude, elevation);
		}

		@Override
		public int hashCode() {
			return Objects.hash(elevation, latitude, longitude);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			
			final SensId other = (SensId) obj;
			return (elevation == other.elevation && Objects.equals(latitude, other.latitude) && Objects.equals(longitude, other.longitude));
		}

		@Override
		public String toString() {
			return "SensId [longitude=" + longitude + ", latitude=" + latitude + ", elevation=" + elevation + "]";
		}
		
	}
	
}
