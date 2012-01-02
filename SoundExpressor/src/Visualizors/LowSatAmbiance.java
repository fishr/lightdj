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
public class LowSatAmbiance extends Visualizer {

	protected static double phaseOffsetBase = 0.01;
	protected static double deltaOmega = -0.001;
	protected static double theta;
	
	protected static int NUM_FRONT_RGB_LIGHTS = ColorOutput.NUM_FRONT_RGB_PANELS * ColorOutput.NUM_LEDS_PER_RGB_BOARD;
	
	@Override
	public String getName() {
		return "Low-Sat Ambiance";
	}
	
	public LowSatAmbiance(int fftSize, double updatesPerSecond) {
		super(fftSize, updatesPerSecond);
	}
	
	@Override
	public void init() {
		// Initialize some parameters

		
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
		
		double phaseOffset = phaseOffsetBase;// * (1 - bassLevel);
		float brightness = 0.1f; //(float) (1 - bassLevel); //1.0f; //(float) (0.75 + 0.25 * bassLevel);
		float saturation = 0.6f; //(float) (1.0 - 0.5 * bassLevel);
		
		// Make the first light red in proprotion to the bass
		Color c0, c1, c2, c3;
		
		
		double symLight = -(double) NUM_FRONT_RGB_LIGHTS / 2 - 0.5;
		double a = 1.0;
		
		for(int light = 0; light < NUM_FRONT_RGB_LIGHTS; light++) {
			colorOutput.setFrontRGBLight(light, Color.getHSBColor((float) (theta + phaseOffsetBase*symLight*(1 - bassLevel*Math.exp(-0.00005*symLight*symLight*symLight*symLight))), saturation, brightness));
			colorOutput.setRearRGBLight(light, Color.getHSBColor((float) (theta + phaseOffsetBase*symLight*(1 - bassLevel*Math.exp(-0.00005*symLight*symLight*symLight*symLight))), saturation, brightness));
			symLight++;
		}
		
		theta += deltaOmega;
		if (theta > 1.0) {theta--;}
		if (theta < 0.0) {theta++;}
		
		
		// Return the result
		return colorOutput;
	}



}
