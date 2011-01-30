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

	protected RGBGradientLinear colorGradient;
	
	@Override
	public String getName() {
		return "VU Meter - 1 Panel";
	}
	
	public VUBass(int fftSize, double updatesPerSecond) {
		super(fftSize, updatesPerSecond);
	}
	
	@Override
	public void init() {
		// Initialize some parameters
		// Set up a color gradient
		colorGradient = new RGBGradientLinear(Color.GREEN, Color.RED);
		
		// We don't need to request any user controls for this visualization plugin
		
	}

	@Override
	public ColorOutput visualize(FeatureList featureList) {
		
		// Retreive any necessary parameters from the FeatureList
		double bassLevel = (Double) featureList.getFeature("BASS_LEVEL");
		double clapLevel = (Double) featureList.getFeature("CLAP_LEVEL");
		
		// Compute a new set of colorings, and store them.
		ColorOutput colorOutput = new ColorOutput();
		
		// Make the second color a randomized hue, with brightness determined by the clap level.
		double bright = clapLevel; // 0.7 * bassLevel + 0.7 * clapLevel; //(System.currentTimeMillis() % 1000) / 1000.0;
		double b0 = 0;
		double b1 = 0;
		double b2 = 0;
		double b3 = 0;
		
//		b0 = Math.min(4*bright, 1.0);
//		b1 = Math.min(3*bright, 1.0);
//		b2 = Math.min(2*bright, 1.0);
//		b3 = Math.min(1*bright, 1.0);
		
		if (bright < 0.25) {
			b0 = bright / 0.25;
		} else if (bright < 0.5) {
			b0 = 1.0;
			b1 = (bright - 0.25) / 0.25;
		} else if (bright < 0.75) {
			b0 = 1.0;
			b1 = 1.0;
			b2 = (bright - 0.5) / 0.25;
		} else {
			b0 = 1.0;
			b1 = 1.0;
			b2 = 1.0;
			b3 = (bright - 0.75) / 0.25;
		}
		
		
		
		colorOutput.rgbLights[0] = colorGradient.computeGradient(0.0, b0);
		colorOutput.rgbLights[1] = colorGradient.computeGradient(0.3333*0.33333, b1);
		colorOutput.rgbLights[2] = colorGradient.computeGradient(0.6667*0.66667, b2);
		colorOutput.rgbLights[3] = colorGradient.computeGradient(1.0, b3);
		
		
		
		// Return the result
		return colorOutput;
	}



}
