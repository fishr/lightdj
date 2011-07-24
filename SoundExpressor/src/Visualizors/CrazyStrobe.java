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
public class CrazyStrobe extends Visualizer {

	protected int state = 0;
	
	@Override
	public String getName() {
		return "Scott's Awesome VU Meter!!!one!";
	}
	
	public CrazyStrobe(int fftSize, double updatesPerSecond) {
		super(fftSize, updatesPerSecond);
	}
	
	@Override
	public void init() {
		// Initialize some parameters
		
		// We don't need to request any user controls for this visualization plugin
		
	}

	@Override
	public ColorOutput visualize(FeatureList featureList) {
		
		ColorOutput colorOutput = new ColorOutput();
		
		if (state == 0) {
			colorOutput.rgbLightsFront[0] = Color.WHITE;
		} else {
			colorOutput.rgbLightsFront[0] = Color.BLACK;
		}
		
		state = (state + 1) % (172 / 7);
		
		
		// Return the result
		return colorOutput;
	}



}
