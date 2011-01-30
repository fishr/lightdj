package FeatureDetectors;

import Common.FeatureList;
import Common.FrequencyRangeControl;

/**
 * A state-machine like object that, when stepped with FFT values, attempts to output the current bass level.
 * Attempts to auto-adapt to changing volume.
 * @author Steve Levine
 *
 */
public class BassFinder extends FeatureDetector {

	protected double averageHalfLife;
	
	protected double updatesPerSecond;
	protected double phi;
	protected double decayRate;
	protected double normalizingVal;
	protected double minFreq;
	protected double maxFreq;
	protected double lastOutput;
	
	protected double averagedLevel;
	protected double currentBassLevel;
	protected double threshold;
	protected double averagedSpread;
	
	protected int NUM_RECENT_BASS_VALS = 8;
	protected double[] recentBassLevels;
	protected int recentBassIndex;
	
	public BassFinder(int fftSize, double updatesPerSecond) {
		super(fftSize, updatesPerSecond);
	}
	
	@Override
	public void init() {
		// Initialize some parameters
		minFreq = 20;
		maxFreq = 80;
		normalizingVal = 30.0;
		averageHalfLife = 0.1;
		decayRate = 1.0 / (50);
		phi = Math.pow(0.5, 1/(averageHalfLife * UPDATES_PER_SECOND));
		recentBassIndex = 0;
		recentBassLevels = new double[NUM_RECENT_BASS_VALS];
		
		// Request some controls
		FrequencyRangeControl freqRangeControl = new FrequencyRangeControl(minFreq, maxFreq);
		requestUserControl(freqRangeControl);
		
	}
	
	@Override
	public void computeFeatures(double[] frequencies, double[] magnitudes, FeatureList featureList) {
		// Compute the level of bass
		double bassLevel = getFreqs(frequencies, magnitudes);
		
		// Create a feature of this, and add it to the featureList.
		featureList.addFeature("BASS_LEVEL", bassLevel);
		featureList.addFeature("BASS_RAW", currentBassLevel);
	}

	
	
	/**
	 * Does the computation behind actually measuring the bass.
	 * @return The nice bass level, smoothed out etc.
	 */
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
		currentBassLevel = level;
		recentBassLevels[recentBassIndex] = level;
		recentBassIndex = (recentBassIndex + 1) % NUM_RECENT_BASS_VALS;
		
		
		// Compute a very low-passed version of the signal to use as an estimate of the overall
		// level of this frequency range. This is the "adaptive" part that allows the frequency
		// range finder to adjust to different volume levels
		double threshold = averagedLevel + averagedSpread + 10.0; //* 1.25 + 5.0;
		this.threshold = threshold;
		double spread = Math.abs(level - averagedLevel);
		
		if (level > threshold) { 
			outputVal =  (level - threshold) / averagedSpread; //normalizingVal;
		} else {
			outputVal = 0.0;
		}
		if (outputVal > 1.0) {
			outputVal = 1.0;
		}
		
		
		averagedLevel = averagedLevel * phi + level*(1 - phi);
		averagedSpread = averagedSpread * phi + spread*(1 - phi);
		
		double actualOutput;
		
		// Limit how fast the output can fal, in an attempt to minimize flicker
		if (outputVal < lastOutput - decayRate) {
			actualOutput = lastOutput - decayRate;
		} else {
			actualOutput = outputVal;
		}
		
		// Output
		lastOutput = actualOutput;
		return actualOutput;
		
	}

	
	public double getCurrentLevel() {
		return currentBassLevel;
	}
	
	public double getAveragedLevel() {
		return averagedLevel;
	}
	
	public double getThreshold() {
		return threshold;
	}
	
	public double getAveragedSpread() {
		return averagedSpread;
	}
	
	public double getBassDelta() {
		double deltaSum = 0;
		for(int i = 0; i < NUM_RECENT_BASS_VALS; i++) {
			deltaSum += positivify(recentBassLevels[(recentBassIndex + 1) % NUM_RECENT_BASS_VALS] - recentBassLevels[recentBassIndex % NUM_RECENT_BASS_VALS]);
		}
		
		return deltaSum;
	}
	
	private double positivify(double x) {
		if (x > 0) {
			return x;
		} else {
			return 0;
		}
	}

	
}
