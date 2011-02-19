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

	// Keep a cyclic gradient for the backpanels
	protected RGBGradientCompoundLinear colorGradient;
	protected double phase = 0.0;
	protected double deltaPhase = 0.001;
	protected static boolean DISPLAY_PEAK = true;
	protected static int peakPosition = 0;
	protected static int COUNT_PER_PEAK_DECREASE = 30;
	protected static int peakTimeCounter = 0;
	
	protected static int FADEOUT_LENGTH = 300;
	protected static int fadeoutCounter = 0;
	
	protected int HALF_SIZE = 12;
	
	@Override
	public String getName() {
		return "VU Meter - Giant";
	}
	
	public VUBass(int fftSize, double updatesPerSecond) {
		super(fftSize, updatesPerSecond);
	}
	
	@Override
	public void init() {
		// Initialize some parameters
		// Set up a color gradient
		colorGradient = new RGBGradientCompoundLinear(new Color[] {Color.GREEN, Color.YELLOW, Color.RED}, new Color[] {Color.YELLOW, Color.RED, Color.GREEN}, new double[] {0.0, 0.333, 0.667}, new double[] {0.333, 0.667, 1.0});
		
		// We don't need to request any user controls for this visualization plugin
		
	}

	@Override
	public ColorOutput visualize(FeatureList featureList) {
		
		// Retreive any necessary parameters from the FeatureList
		double bassLevel = (Double) featureList.getFeature("BASS_LEVEL");
		double clapLevel = (Double) featureList.getFeature("CLAP_LEVEL");
		double level = (Double) featureList.getFeature("OVERALL_LEVEL");
		
		// Compute a new set of colorings, and store them.
		ColorOutput colorOutput = new ColorOutput();
		
		// Make the second color a randomized hue, with brightness determined by the clap level.
		double bright = level; //0.7 * bassLevel + 0.3 * clapLevel;  //(System.currentTimeMillis() % 1000) / 1000.0;
		double b0 = 0;
		double b1 = 0;
		double b2 = 0;
		double b3 = 0;
		
//		b0 = Math.min(4*bright, 1.0);
//		b1 = Math.min(3*bright, 1.0);
//		b2 = Math.min(2*bright, 1.0);
//		b3 = Math.min(1*bright, 1.0);
		
//		if (bright < 0.25) {
//			b0 = bright / 0.25;
//		} else if (bright < 0.5) {
//			b0 = 1.0;
//			b1 = (bright - 0.25) / 0.25;
//		} else if (bright < 0.75) {
//			b0 = 1.0;
//			b1 = 1.0;
//			b2 = (bright - 0.5) / 0.25;
//		} else {
//			b0 = 1.0;
//			b1 = 1.0;
//			b2 = 1.0;
//			b3 = (bright - 0.75) / 0.25;
//		}
//		
//		
//		
//		colorOutput.rgbLights[0] = colorGradient.computeGradient(0.0, b0);
//		colorOutput.rgbLights[1] = colorGradient.computeGradient(0.3333*0.33333, b1);
//		colorOutput.rgbLights[2] = colorGradient.computeGradient(0.6667*0.66667, b2);
//		colorOutput.rgbLights[3] = colorGradient.computeGradient(1.0, b3);
		
		for(int i = 0; i < HALF_SIZE; i++) {
			if (i / (double) HALF_SIZE <= bright) {
				//setMirror(i, colorGradient.computeGradient((i / 16.0), 1.0), colorOutput);
				Color c = getVUColor(i);

				if ((i + 1) / (double) HALF_SIZE > bright) {
					// This is the "transition" one - make it a brightness gradient!
					c = RGBGradientLinear.linearGradient(Color.BLACK,c, (double) HALF_SIZE * (bright - i / (double) HALF_SIZE));
					
					// Compute the lowpass peak 
					if (i > peakPosition) {
						peakPosition = i;
						peakTimeCounter = 0;
						fadeoutCounter = 0;
					} else {
						peakTimeCounter++;
					}
					if (peakTimeCounter > COUNT_PER_PEAK_DECREASE) {
						peakTimeCounter = 0;
						peakPosition--;
					}
					
				}
				setMirror(i, c, colorOutput);
				
			} else {
				setMirror(i, Color.BLACK, colorOutput);
			}
		}
		
		// If necessary, draw the peak
		fadeoutCounter++;
		//setMirror(peakPosition, RGBGradientLinear.linearGradient(getVUColor(peakPosition), Color.BLACK, (double) fadeoutCounter / FADEOUT_LENGTH), colorOutput);
		
		// Set the back colors
		colorOutput.setAllRearRGBLEDs(colorGradient.computeGradient(phase));
		
		phase += deltaPhase;
		if (phase > 1) {
			phase--;
		}
		
		
		// Return the result
		return colorOutput;
	}

	
	protected void setMirror(int i, Color c, ColorOutput colorOutput) {
		colorOutput.setFrontRGBLight(HALF_SIZE - 1 - i, c);
		colorOutput.setFrontRGBLight(HALF_SIZE + i, c);
	}

	
	protected Color getVUColor(int i) {
		Color c;
//		if (i < 7) {
//			c = Color.GREEN;
//		} else if (i < 11) {
//			c = Color.YELLOW;
//		} else {
//			c = Color.RED;
//		}
//		
		if (i < 3) {
			c = Color.GREEN;
		} else if (i < 7) {
			c = Color.YELLOW;
		} else {
			c = Color.RED;
		}
		
		return c;
	}

}
