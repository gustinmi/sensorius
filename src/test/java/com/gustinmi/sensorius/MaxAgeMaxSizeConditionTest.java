package com.gustinmi.sensorius;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MaxAgeMaxSizeConditionTest {
	
	
	public static final FlushCondition flushCondition = new MaxAgeMaxSizeCondition(true);
	
	
	@Test
	void testEnabled() {
		
		assertTrue(new MaxAgeMaxSizeCondition(false).shouldFlush(9, 1000, 1500) == false);
		assertTrue(new MaxAgeMaxSizeCondition(true).shouldFlush(9, 1000, 7500) == true);
		
	}
	

	@Test
	void testConditions() {
		
		//                              COUNT   TIME_FIRST  TIME_CURRENT
		
		assertTrue(flushCondition.shouldFlush(9, 1000, 1500) == false); // not enough time passed
		
		assertTrue(flushCondition.shouldFlush(9, 1000, 900) == false); // next is before
		
		assertTrue(flushCondition.shouldFlush(9, 1000, 10_000) == true); // enough old
		
		assertTrue(flushCondition.shouldFlush(11, 1000, 1000) == true); // too big
		
	}

}
