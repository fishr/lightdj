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
public class FingerPiano extends Visualizer {

	protected static double phaseOffsetBase = 0.07;
	protected static double deltaOmega = -0.001;
	protected static double theta;
	protected double b0, b1, b2, b3, b4, b5, b6, b7;
	
	protected static int NUM_FRONT_RGB_LIGHTS = ColorOutput.NUM_FRONT_RGB_PANELS*4;
	
	@Override
	public String getName() {
		return "Finger Piano";
	}
	
	public FingerPiano(int fftSize, double updatesPerSecond) {
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
		double a = (Double) featureList.getFeature("KEY_A");
		double s = (Double) featureList.getFeature("KEY_S");
		double d = (Double) featureList.getFeature("KEY_D");
		double f = (Double) featureList.getFeature("KEY_F");
		double j = (Double) featureList.getFeature("KEY_J");
		double k = (Double) featureList.getFeature("KEY_K");
		double l = (Double) featureList.getFeature("KEY_L");
		double semi = (Double) featureList.getFeature("KEY_;");
		
		// Compute a new set of colorings, and store them.
		ColorOutput colorOutput = new ColorOutput();
		
		double phaseOffset = phaseOffsetBase;// * (1 - bassLevel);
		float brightness = 1.0f; //(float) (1 - bassLevel); //1.0f; //(float) (0.75 + 0.25 * bassLevel);
		float saturation = 1.0f; //(float) (1.0 - 0.5 * bassLevel);
		
		double decayRate = 0.9;
		
		if (a >= 0.99) {
			b0 = 1.0;
		} else {
			b0 = decayRate * b0;
		}
		
		if (s >= 0.99) {
			b1 = 1.0;
		} else {
			b1 = decayRate * b1;
		}
		
		if (d >= 0.99) {
			b2 = 1.0;
		} else {
			b2 = decayRate * b2;
		}
		
		if (f >= 0.99) {
			b3 = 1.0;
		} else {
			b3 = decayRate * b3;
		}
		
		if (j >= 0.99) {
			b4 = 1.0;
		} else {
			b4 = decayRate * b4;
		}
		
		if (k >= 0.99) {
			b5 = 1.0;
		} else {
			b5 = decayRate * b5;
		}
		
		if (l >= 0.99) {
			b6 = 1.0;
		} else {
			b6 = decayRate * b6;
		}
		
		if (semi >= 0.99) {
			b7 = 1.0;
		} else {
			b7 = decayRate * b7;
		}
		
		
		Color c0, c1, c2, c3, c4, c5, c6, c7;
		c0 = Color.getHSBColor((float) (theta + 0*phaseOffset), 1.0f, (float) b0);
		c1 = Color.getHSBColor((float) (theta + 1*phaseOffset), 1.0f, (float) b1);
		c2 = Color.getHSBColor((float) (theta + 2*phaseOffset), 1.0f, (float) b2);
		c3 = Color.getHSBColor((float) (theta + 3*phaseOffset), 1.0f, (float) b3);
		c4 = Color.getHSBColor((float) (theta + 4*phaseOffset), 1.0f, (float) b4);
		c5 = Color.getHSBColor((float) (theta + 5*phaseOffset), 1.0f, (float) b5);
		c6 = Color.getHSBColor((float) (theta + 5*phaseOffset), 1.0f, (float) b6);
		c7 = Color.getHSBColor((float) (theta + 5*phaseOffset), 1.0f, (float) b7);
		
		// Set the front panels
		colorOutput.setFrontPanel(0, c0, c0, c0, c0);
		colorOutput.setFrontPanel(1, c1, c1, c1, c1);
		colorOutput.setFrontPanel(2, c2, c2, c2, c2);
		colorOutput.setFrontPanel(3, c3, c3, c3, c3);
		colorOutput.setFrontPanel(4, c4, c4, c4, c4);
		colorOutput.setFrontPanel(5, c5, c5, c5, c5);
		colorOutput.setFrontPanel(6, c6, c6, c6, c6);
		colorOutput.setFrontPanel(7, c7, c7, c7, c7);
		
		// Set the back panels
		colorOutput.setRearPanel(0, c0, c0, c0, c0);
		colorOutput.setRearPanel(1, c1, c1, c1, c1);
		colorOutput.setRearPanel(2, c2, c2, c2, c2);
		colorOutput.setRearPanel(3, c3, c3, c3, c3);
		colorOutput.setRearPanel(4, c4, c4, c4, c4);
		colorOutput.setRearPanel(5, c5, c5, c5, c5);
		colorOutput.setRearPanel(6, c6, c6, c6, c6);
		colorOutput.setRearPanel(7, c7, c7, c7, c7);
		
		// Increment the color shiftings
		theta += deltaOmega;
		if (theta > 1.0) {theta--;}
		if (theta < 0.0) {theta++;}
		
		// Set the UV's to the bass
		colorOutput.setAllUVWhites(bassLevel, 0.0);
		
		// Return the result
		return colorOutput;
	}



}
