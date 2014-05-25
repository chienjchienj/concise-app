package org.sustudio.concise.app.gear.wordClouder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cloud Angle
 * 
 * @author Kuan-ming Su
 *
 */
public enum CloudAngle {

	HorizontalOnly("Horizontal Only"),
	VerticalOnly("Vertical Only"),
	HorizontalAndVertical("Horizontal & Vertical"),
	FortyFiveDegreesMostlyHorizontal("45 Degrees, Mostly Horizontal"),
	FortyFiveDegrees("45 Degrees"),
	Random("Random"),
	;
	
	private final String label;
	
	CloudAngle(final String label) {
		this.label = label;
	}
	
	public String label() {
		return label;
	}
	
	private static List<Float> randomAngles = new ArrayList<Float>();
	public List<Float> getAngles() {
		switch (this) {
		case Random:
			if (randomAngles.size() != 181) {
				randomAngles.clear();
				for (int i = -90; i <= 90; i++) {
					randomAngles.add((float) i);
				}
			}
			return randomAngles;
			
		case VerticalOnly:						return Arrays.asList(-90F,90F);
		case HorizontalAndVertical:				return Arrays.asList(0F,-90F,0F,90F);
		case FortyFiveDegreesMostlyHorizontal:	return Arrays.asList(0F,-90F,-45F, 0F,45F, 90F,0F,0F,0F,0F);
		case FortyFiveDegrees:					return Arrays.asList(-90F,-45F, 0F,45F, 90F);
		case HorizontalOnly: 					
		default:								return Arrays.asList(0F);
		}
	}
	
	public static String[] stringValues() {
		String[] angles = new String[values().length];
		for (int i=0; i<angles.length; i++) {
			angles[i] = values()[i].label();
		}
		return angles;
	}
}
