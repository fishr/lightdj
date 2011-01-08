package FeatureDetectors;

import Common.FeatureList;

/**
 * A state-machine like object that, when stepped with FFT values, attempts to output the current bass level.
 * Attempts to auto-adapt to changing volume levels.
 * @author Steve Levine
 *
 */
public class FrequencyRangeFinder extends FeatureDetector {
	
	protected double averageHalfLife = 0.125;
	protected double phi;
	protected double averagedLevel = 0;
	protected double lastOutput = 0;
	protected double decayRate = 0.75;
	protected double normalizingVal = 1.0;
	protected double regular = 0;
	protected double minFreq = 1000;
	protected double maxFreq = 1200;
	
	public FrequencyRangeFinder(int fftSize, double updatesPerSecond) {
		super(fftSize, updatesPerSecond);
	}
	
	
	@Override
	public void init() {

		phi = Math.pow(0.5, 1/(averageHalfLife * UPDATES_PER_SECOND));
	}
	
	@Override
	public void computeFeatures(double[] frequencies, double[] magnitudes, FeatureList featureList) {
		// Compute the level of bass
		double freqLevel = getFreqs(frequencies, magnitudes);
		
		// Create a feature of this, and add it to the featureList.
		featureList.addFeature("FREQ_RANGE_CUSTOM", freqLevel);
		
	}

	
//	public FrequencyRangeFinder(int sampleRate, int fftSize, double minFreq, double maxFreq) {
//		this(sampleRate, fftSize);
//		this.minFreq = minFreq;
//		this.maxFreq = maxFreq;
//	}
	
	// Estimate the bass, given an FFT.
	public double getFreqs(double[] frequencies, double[] magnitudes) {

		
		double outputVal;
		
		// Compute an average from everything from minBassFreq to maxBassFreq
		double largetFreq = frequencies[frequencies.length - 1];
		int minIndex = (int) (minFreq / largetFreq * frequencies.length);
		int maxIndex = (int) (maxFreq / largetFreq * frequencies.length);
		
		double sum = 0;
		int n = 0;
		for(int i = minIndex; i <= maxIndex; i++) {
			sum += magnitudes[i];
			n++;
		}
		
		double level = sum / n;
		
		
		return level;
		
	}
	
	double getAveragedLevel() {
		return averagedLevel;
	}
	
	
}
