package FeatureDetectors;

import Common.FeatureList;


/**
 * A state-machine like object that, when stepped with FFT values, attempts to output the current bass level.
 * Attempts to auto-adapt to changing volume.
 * @author Steve Levine
 *
 */
public class ClapFinder extends FeatureDetector  {
	
	
	protected double averageHalfLife = 0.25;
	protected double phi;
	protected double averagedLevel = 0;
	protected double decayRate = 0.75;
	protected double normalizingVal = 1.0;
	protected double regular = 0;
	protected double minFreq;
	protected double maxFreq;
	
	// Low-pass smoothing
	protected double timeLowPass = 0.1;
	protected double percentLowPass = 0.25;
	protected double alpha;
	protected double clapLevelSmoothed = 0.0;
	
	private double[] averagedFrequencyLevels;
	
	double lastOutput = 0;
	
	public ClapFinder(int fftSize, double updatesPerSecond) {
		super(fftSize, updatesPerSecond);
	}
	
	@Override
	public void init() {
		
		minFreq = 8000;
		maxFreq = 16000;
		normalizingVal = 0.1;
		averageHalfLife = 0.25;
		phi = Math.pow(0.5, 1/(averageHalfLife * UPDATES_PER_SECOND));
		
		alpha = 1 - Math.exp(Math.log(percentLowPass) / (UPDATES_PER_SECOND * timeLowPass));
		
		// This will store a low pass on every frequency.
		averagedFrequencyLevels = new double[FFT_SIZE];
		for(int i = 0; i < FFT_SIZE; i++) {
			averagedFrequencyLevels[i] = 0;
		}
	}
	
	
	@Override
	public void computeFeatures(double[] frequencies, double[] magnitudes, FeatureList featureList) {
		// Compute the level of bass
		double clapLevel = getFreqs(frequencies, magnitudes);
		
		clapLevelSmoothed = alpha * clapLevel + (1 - alpha) * clapLevelSmoothed;
		
		// Create a feature of this, and add it to the featureList.
		featureList.addFeature("CLAP_LEVEL", clapLevelSmoothed);
	}
	
	
	// Estimate the bass, given an FFT.
	public double getFreqs(double[] frequencies, double[] magnitudes) {
		
		// Compute an average from everything from minBassFreq to maxBassFreq
		double largetFreq = frequencies[frequencies.length - 1];
		int minIndex = (int) (minFreq / largetFreq * frequencies.length);
		int maxIndex = (int) (maxFreq / largetFreq * frequencies.length);
		
		double sum = 0;
		int n = 0;
		for(int i = minIndex; i <= maxIndex; i++) {
			//sum += Math.pow(magnitudes[i], 0.5);
			averagedFrequencyLevels[i] = phi*averagedFrequencyLevels[i] + (1 - phi) *  magnitudes[i];
			
			if (magnitudes[i] / averagedFrequencyLevels[i] > 1.414) {
				// Compute the percentage of points that are higher than their averaged levels
				sum += 1.0;
			}
			
			n++;
		}
		
		double fractionInExcess = sum / n;
		double preLowPassRetVal;
		
		if (fractionInExcess > 0.0) {
			preLowPassRetVal = fractionInExcess / 0.75;
		} else {
			preLowPassRetVal = 0.0;
		}
		
		// Implement a half lowpass filter, to limit the decay rate
//		double c = 0.98;
//		double output;
//		if (preLowPassRetVal < c * lastOutput) {
//			output = c * lastOutput;
//		} else {
//			output = preLowPassRetVal;
//		}
//		lastOutput = output;
//		
		double output = preLowPassRetVal;
		
		return output;
		
	}
	
	
	public double[] getAveragedFreqs() {
		return averagedFrequencyLevels;
	}

	
}
