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
public class VUBass extends Visualizer {

	protected ColorGenerator rgbController;
	
	@Override
	public String getName() {
		return "Scott's Awesome VU Meter!!!one!";
	}
	
	public VUBass(int fftSize, double updatesPerSecond) {
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
		double bassLevel = (Double) featureList.getFeature("CLAP_LEVEL");
		double clapLevel = (Double) featureList.getFeature("CLAP_LEVEL");
		
		// Compute a new set of colorings, and store them.
		ColorOutput colorOutput = new ColorOutput();
		
		// Make the second color a randomized hue, with brightness determined by the clap level.
		
		float brightness = (float)bassLevel * 2;
		if (brightness > 1) brightness = 1;
		
		float hue = 0.666f - (float)(bassLevel)*0.666f;
		if (hue > 0.333) hue = 0.333f;
		
		Color c = Color.getHSBColor(hue, (float) 1.0, brightness);
		//System.out.println(c);
		colorOutput.rgbLights[0] = c;
		
		
		
		// Return the result
		return colorOutput;
	}



}
