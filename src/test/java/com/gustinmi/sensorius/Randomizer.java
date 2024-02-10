package com.gustinmi.sensorius;

import java.text.DecimalFormat;
import java.util.Random;

public class Randomizer {

	public static float generateRandomFloat() {
	    final Random random = new Random();
	    final float randomFloat = random.nextFloat();
	    final DecimalFormat decimalFormat = new DecimalFormat("#.##"); // FIX culture invariant
	    String formattedFloat = decimalFormat.format(randomFloat);
	    formattedFloat = formattedFloat.replace(',', '.');
	    return Float.parseFloat(formattedFloat);
	}

	public static int getRandomNumber(int min, int max) {
	    return (int) ((Math.random() * (max - min)) + min);
	}

}
