package com.gustinmi.sensorius.utils;

/**
 * Java conditional compiling: public static variables are evaluated in runtime. As such, if they used with if (var) statment, everything inside if is thrown away by compiler
 * Used to use debug logging only in certain conditions and prevent checking isLogEnabled on each log statement  
 * */
public class CompilationConstants {
	
	/** LOG add procedure of sensor registry */
	public static final boolean INFO_SENSOR_REGISTRY = false;
	
	/** LOG details of time series db */
	public static final boolean INFO_TIMESERIES = true;
	
	

}
