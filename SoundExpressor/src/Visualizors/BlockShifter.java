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
public class BlockShifter extends Visualizer {

	// Keep a cyclic gradient for the backpanels
	protected RGBGradientCompoundLinear colorGradient;
	protected static int peakPosition = 0;
	protected static int COUNT_PER_PEAK_DECREASE = 30;
	protected static int peakTimeCounter = 0;
	
	protected static int SHIFT_LENGTH = 10;
	protected static int shiftCounter = 0;
	
	protected int HALF_SIZE = 12;
	
	protected Color[] colorRegister;
	
	@Override
	public String getName() {
		return "Block Shifter";
	}
	
	public BlockShifter(int fftSize, double updatesPerSecond) {
		super(fftSize, updatesPerSecond);
	}
	
	@Override
	public void init() {
		colorRegister = new Color[HALF_SIZE];
		
	}

	@Override
	public ColorOutput visualize(FeatureList featureList) {
		
		// Retreive any necessary parameters from the FeatureList
		double bassLevel = (Double) featureList.getFeature("BASS_LEVEL");
		double clapLevel = (Double) featureList.getFeature("CLAP_LEVEL");
		double level = (Double) featureList.getFeature("OVERALL_LEVEL");
		
		double j = (Double) featureList.getFeature("KEY_J");
		double k = (Double) featureList.getFeature("KEY_K");
		double l = (Double) featureList.getFeature("KEY_L");
		double s = (Double) featureList.getFeature("KEY_S");
		double d = (Double) featureList.getFeature("KEY_D");
		double f = (Double) featureList.getFeature("KEY_F");
		
		double trigger = j + k + l + s + d + f;
		
		// Compute a new set of colorings, and store them.
		ColorOutput colorOutput = new ColorOutput();
		
		// Time to shift?
		if (shiftCounter >= SHIFT_LENGTH) {
			shiftColors();
			shiftCounter = 0;
		} else {
			shiftCounter++;
		}
		

		
		if (trigger > 0.99) {
			// Add a new color
			int r = (int) (256 * Math.random());
			int g = (int) (256 * Math.random());
			int b = (int) (256 * Math.random());
			
			Color c = new Color(r, g, b);
			
			colorRegister[0] = c;
			
		}
		
		for(int i = 0; i < HALF_SIZE; i++) {
			setMirror(i, colorRegister[i], colorOutput);
		}
		

		
		// Return the result
		return colorOutput;
	}

	// Shift all the colors
	protected void shiftColors() {
		for(int i = HALF_SIZE - 1; i >= 1; i--) {
			colorRegister[i] = colorRegister[i - 1];
		}
		colorRegister[0] = Color.black;
	}
	
	
	protected void setMirror(int i, Color c, ColorOutput colorOutput) {
		colorOutput.setFrontRGBLight(HALF_SIZE - 1 - i, c);
		colorOutput.setFrontRGBLight(HALF_SIZE + i, c);
		colorOutput.setRearRGBLight(HALF_SIZE - 1 - i, c);
		colorOutput.setRearRGBLight(HALF_SIZE + i, c);
	}

	

}
