package FeatureDetectors;

import Common.FeatureList;

/**
 * Measures the overall level of the music, like a VU meter. Gently lowpasses the output.
 * @author Steve Levine
 *
 */
public class LevelMeter extends FeatureDetector {
	
	protected double averageHalfLife;
	
	protected double phi;
	protected double decayRate;
	protected double normalizingVal;
	protected double averagedLevel;
	
	public LevelMeter(int fftSize, double updatesPerSecond) {
		super(fftSize, updatesPerSecond);
	}
	
	@Override
	public void init() {
		// Initiate other parameters
		normalizingVal = 0.01;
		averageHalfLife = 0.0025;
		decayRate = 1.0 / (20);
		
		// Calculate some parameters
		phi = Math.pow(0.5, 1/(averageHalfLife * UPDATES_PER_SECOND));
	}
	
	@Override
	public void computeFeatures(double[] frequencies, double[] magnitudes, FeatureList featureList) {
		// Compute the level of bass
		double level = getLevel(frequencies, magnitudes);
		
		// Create a feature of this, and add it to the featureList.
		featureList.addFeature("OVERALL_LEVEL", level);
	}
	

	
	public double getLevel(double[] frequencies, double[] magnitudes) {
		double sum = 0;
		int n = 0;
		for(int i = 1; i < frequencies.length; i++) {
			sum += Math.log(1 + magnitudes[i]);
			n++;
		}
		
		double level = sum / n;
		averagedLevel = averagedLevel * phi + level*(1 - phi);
		return averagedLevel / normalizingVal;
		
	}
	
}
