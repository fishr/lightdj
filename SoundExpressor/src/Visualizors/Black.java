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
public class Black extends Visualizer {

	
	@Override
	public String getName() {
		return "Black";
	}
	
	public Black(int fftSize, double updatesPerSecond) {
		super(fftSize, updatesPerSecond);
	}
	
	@Override
	public void init() {
		// Initialize some parameters
		
		// We don't need to request any user controls for this visualization plugin
		
	}

	@Override
	public ColorOutput visualize(FeatureList featureList) {
		
		// Compute a new set of colorings, and store them.
		ColorOutput colorOutput = new ColorOutput();
		
		colorOutput.setAllFrontRGBLEDs(Color.BLACK);
		colorOutput.setAllRearRGBLEDs(Color.BLACK);
		
		
		// Return the result
		return colorOutput;
	}



}
