package com.gustinmi.sensorius;

interface FlushCondition {
	boolean shouldFlush(int size, long first, long last);
}

/** Currently this is only condition. It flushes bufer based on:
 * 1. time frame or 
 * 2. (if density increases) based on number of items in buffer */
public class MaxAgeMaxSizeCondition implements FlushCondition {
	
	//TODO move to spring config
	public static final int SENS_BUFF_MAXSIZE = 10;
	
	//TODO move to spring config
	public static final int SENS_BUFF_MAXAGE_MS = 5_000;
	
	private final boolean isEnabled; 
	
	public MaxAgeMaxSizeCondition(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/**
	 * 	SIMPLIFICATION - ocasionally data will be the same through multiple frames.
	 *  We do not care if we have 1 entry of sam e reading per frame 
	 */
	@Override
	public boolean shouldFlush(int size, long first, long last) {
		
		if (!isEnabled) {
			if (size >= 1000) { // we do not want to proceed to infinity
				return true;
			}
			return false;
		} 
				
		if (size > SENS_BUFF_MAXSIZE) {
			// if max items in bufer, we flush it in any case
			return true;
		}
		else { // check age of items in buffer
			if (last - first >= SENS_BUFF_MAXAGE_MS) { // not old enough
				return true;
			}else {
				return false;	
			}
		}
		
	}

	
	
}
