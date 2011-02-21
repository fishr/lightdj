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
public class WhitePulseBass extends Visualizer {

	
	@Override
	public String getName() {
		return "White Bass Pulse";
	}
	
	public WhitePulseBass(int fftSize, double updatesPerSecond) {
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
		//double bassLevel = (Double) featureList.getFeature("PULSE_BASS");
		double bassLevel = (Double) featureList.getFeature("BASS_LEVEL");
		double clapLevel = (Double) featureList.getFeature("CLAP_LEVEL");
		
		// Compute a new set of colorings, and store them.
		ColorOutput colorOutput = new ColorOutput();
		
		//bassLevel = 1.0;
		Color shadeOfWhite = new Color((float) bassLevel, (float) bassLevel, (float) bassLevel);
		
		Color shadeOfRed = new Color((float) (1 - bassLevel), 0.0f, 0.0f);
		
		// Make the first light red in proportion to the bass
//		colorOutput.rgbLights[0] = shadeOfWhite;
//		colorOutput.rgbLights[1] = shadeOfWhite;
//		colorOutput.rgbLights[2] = shadeOfWhite;
//		colorOutput.rgbLights[3] = shadeOfWhite;
		colorOutput.setAllFrontRGBLEDs(shadeOfWhite);
		colorOutput.setAllRearRGBLEDs(shadeOfRed);
		
		// Set the UV's to the bass
		colorOutput.setAllUVWhites(bassLevel, 0.0);
		
		// Return the result
		return colorOutput;
	}



}
