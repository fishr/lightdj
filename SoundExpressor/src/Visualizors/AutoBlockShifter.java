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
public class AutoBlockShifter extends Visualizer {

	// Keep a cyclic gradient for the backpanels
	protected RGBGradientCompoundLinear colorGradient;
	protected static int peakPosition = 0;
	protected static int COUNT_PER_PEAK_DECREASE = 30;
	protected static int peakTimeCounter = 0;
	
	protected static int SHIFT_LENGTH = 12;
	protected static int shiftCounter = 0;
	
	protected int HALF_SIZE;
	
	protected static boolean triggerState;
	protected static final double TRIGGER_HIGH = 0.7;
	protected static final double TRIGGER_LOW = 0.5;
	
	protected Color[] colorRegister;
	
	@Override
	public String getName() {
		return "Auto Block Shifter";
	}
	
	public AutoBlockShifter(int fftSize, double updatesPerSecond) {
		super(fftSize, updatesPerSecond);
	}
	
	@Override
	public void init() {
		HALF_SIZE = ColorOutput.NUM_FRONT_RGB_PANELS * ColorOutput.NUM_LEDS_PER_RGB_BOARD / 2;
		colorRegister = new Color[HALF_SIZE];
		triggerState = false;
		
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
		
		double trigger = bassLevel;
		
		// Compute a new set of colorings, and store them.
		ColorOutput colorOutput = new ColorOutput();
		
		// Time to shift?
		if (shiftCounter >= SHIFT_LENGTH) {
			shiftColors();
			shiftCounter = 0;
		} else {
			shiftCounter++;
		}
		

		
		if (triggerState) {
			if (trigger < TRIGGER_LOW) {
				triggerState = false;
			}
		} else {
			if (trigger >= TRIGGER_HIGH) {
				// Add a new color
				int r = (int) (256 * Math.random());
				int g = (int) (256 * Math.random());
				int b = (int) (256 * Math.random());
				
				Color c = new Color(r, g, b);
				
				colorRegister[0] = c;
				
				triggerState = true;
			}
		}
		
		
		// Set those colors
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
