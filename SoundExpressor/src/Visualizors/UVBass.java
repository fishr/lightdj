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
public class UVBass extends Visualizer {

	
	@Override
	public String getName() {
		return "UV Bass";
	}
	
	public UVBass(int fftSize, double updatesPerSecond) {
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
		
		// Compute a new set of colorings, and store them.
		ColorOutput colorOutput = new ColorOutput();
		
		Color shadeOfWhite = new Color((float) bassLevel, (float) bassLevel, (float) bassLevel);
		
		// Make the first light red in proportion to the bass
//		colorOutput.rgbLights[0] = shadeOfWhite;
//		colorOutput.rgbLights[1] = shadeOfWhite;
//		colorOutput.rgbLights[2] = shadeOfWhite;
//		colorOutput.rgbLights[3] = shadeOfWhite;
		colorOutput.setAllUVWhites(bassLevel, 0.0);
		
		// Return the result
		return colorOutput;
	}

	@Override
	public boolean canAutoDJ() {
		return false;
	}

}
