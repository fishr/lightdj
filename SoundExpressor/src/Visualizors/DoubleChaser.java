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
public class DoubleChaser extends Visualizer {

	protected static int FRAMES_PER_MOVE = 12;
	
	
	// Keep a cyclic gradient for the backpanels
	protected double phase = 0.0;
	protected double deltaPhase = 0.001;
	
	
	protected int counter = 0;
	protected int position = 0;
	
	protected static int NUM_RGB_LEDS = 48;
	
	@Override
	public String getName() {
		return "Double Chaser";
	}
	
	public DoubleChaser(int fftSize, double updatesPerSecond) {
		super(fftSize, updatesPerSecond);
	}
	
	@Override
	public void init() {
		// Initialize some parameters
		// Set up a color gradient
	
		// We don't need to request any user controls for this visualization plugin
		
	}

	@Override
	public ColorOutput visualize(FeatureList featureList) {
		
		// Retreive any necessary parameters from the FeatureList
		double bassLevel = (Double) featureList.getFeature("BASS_LEVEL");
		double clapLevel = (Double) featureList.getFeature("CLAP_LEVEL");
		
		// Compute a new set of colorings, and store them.
		ColorOutput colorOutput = new ColorOutput();
		

		if (counter == FRAMES_PER_MOVE) {
			// It's time to move one!
			position = (position + 1) % NUM_RGB_LEDS;
			
			counter = 0;
		} else {
			counter++;
		}
		
		// Set the appropriate lights
		double brightness = 1.0; //0.75 * bassLevel + 0.25;
		
		colorOutput.setRGBLightFrontOrBack(getPosition(position) % NUM_RGB_LEDS, Color.getHSBColor((float) phase, 1.0f, (float) (brightness)));
		colorOutput.setRGBLightFrontOrBack(getPosition(position + NUM_RGB_LEDS / 2) % NUM_RGB_LEDS, Color.getHSBColor((float) phase + 0.5f, 1.0f, (float) (brightness)));
		
		// Update the color for next time
		phase += deltaPhase;
		if (phase > 1) {
			phase--;
		}
		
		// Return the result
		return colorOutput;
	}

	
	protected int getPosition(int j) {
		int i = j % NUM_RGB_LEDS;
		
		if (i < NUM_RGB_LEDS / 2) {
			return i;
		} else {
			return 3*NUM_RGB_LEDS/2 - 1 - i;
		}
		
	}


}
