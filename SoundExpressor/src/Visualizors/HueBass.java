package Visualizors;

import java.awt.Color;

import Common.ColorOutput;
import Common.FeatureList;

/**
 * A basic visualizer that sets the first light to red corresponding to how much bass there is,
 * and the second light to a color rotator based on the clap level.
 * @author Steve Levine0
 *
 */
public class HueBass extends Visualizer {

	protected static double phaseOffsetBase = 0.01;
	protected static double deltaOmega = -0.0003;
	protected static double theta;
	
	protected static int NUM_FRONT_RGB_LIGHTS = 24;
	protected boolean trigger;
	protected static final double THRESHOLD_HIGH = 0.98;
	protected static final double THRESHOLD_LOW = 0.2;
	
	@Override
	public String getName() {
		return "Hue Bass";
	}
	
	public HueBass(int fftSize, double updatesPerSecond) {
		super(fftSize, updatesPerSecond);
	}
	
	@Override
	public void init() {
		// Initialize some parameters
		trigger = false;
		
		// We don't need to request any user controls for this visualization plugin
		
	}

	@Override
	public ColorOutput visualize(FeatureList featureList) {
		
		// Retreive any necessary parameters from the FeatureList
		double bassLevel = (Double) featureList.getFeature("BASS_LEVEL");
		double clapLevel = (Double) featureList.getFeature("CLAP_LEVEL");
		//System.out.println(bassLevel);
		
		// Compute a new set of colorings, and store them.
		
		ColorOutput colorOutput = new ColorOutput();
		
		// Jump the hue?
		if (trigger) {
			if (bassLevel < THRESHOLD_LOW) {
				trigger = false;
			}
		} else {
			if (bassLevel > THRESHOLD_HIGH) {
				trigger = true;
				theta += 0.373041;
			}
		}
		
		double phaseOffset = phaseOffsetBase;// * (1 - bassLevel);
		float brightness = (float) 1.0; //(0.03 + 0.97 * bassLevel);
		float saturation = 1.0f; 
		
		// Make the first light red in proprotion to the bass
		Color c0;
		c0 = Color.getHSBColor((float) theta, (float) saturation, (float) brightness);
		
		colorOutput.setAllFrontRGBLEDs(c0);
		colorOutput.setAllRearRGBLEDs(c0);
		
		theta += deltaOmega;
		if (theta > 1.0) {theta--;}
		if (theta < 0.0) {theta++;}
		
		
		// Return the result
		return colorOutput;
	}

	@Override
	public boolean canAutoDJ() {
		return false;
	}
	
	

}
