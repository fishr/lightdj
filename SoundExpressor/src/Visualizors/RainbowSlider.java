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
public class RainbowSlider extends Visualizer {

	protected static double phaseOffsetBase = 0.06;
	protected static double deltaOmega = -0.002;
	protected static double theta;
	
	@Override
	public String getName() {
		return "Rainbow Slider";
	}
	
	public RainbowSlider(int fftSize, double updatesPerSecond) {
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
		
		// Compute a new set of colorings, and store them.
		
		ColorOutput colorOutput = new ColorOutput();
		
		double phaseOffset = phaseOffsetBase;// * (1 - bassLevel);
		float brightness = (float) (1 - bassLevel); //1.0f; //(float) (0.75 + 0.25 * bassLevel);
		float saturation = 1.0f; //(float) (1.0 - 0.5 * bassLevel);
		
		// Make the first light red in proprotion to the bass
		colorOutput.rgbLights[0] = Color.getHSBColor((float) (theta - 1.5 * phaseOffset), saturation, brightness);
		colorOutput.rgbLights[1] = Color.getHSBColor((float) (theta - 0.5 * phaseOffset), saturation, brightness);
		colorOutput.rgbLights[2] = Color.getHSBColor((float) (theta + 0.5 * phaseOffset), saturation, brightness);
		colorOutput.rgbLights[3] = Color.getHSBColor((float) (theta + 1.5 * phaseOffset), saturation, brightness);
		
		theta += deltaOmega;
		if (theta > 1.0) {theta--;}
		
		
		// Return the result
		return colorOutput;
	}



}
