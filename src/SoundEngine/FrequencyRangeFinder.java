package SoundEngine;

/**
 * A state-machine like object that, when stepped with FFT values, attempts to output the current bass level.
 * Attempts to auto-adapt to changing volume.
 * @author steve
 *
 */
public class FrequencyRangeFinder {
	
	protected double decayPerSecond = 0.5;
	
	protected double updatesPerSecond;
	protected double phi;
	protected double averagedLevel = 0;
	protected double lastOutput = 0;
	
	protected double normalizingVal = 1.0;
	
	
	protected double regular = 0;
	
	protected double minFreq = 0;
	protected double maxFreq = 20000;
	
	public FrequencyRangeFinder(int sampleRate, int fftSize) {
		updatesPerSecond = 1.0 * sampleRate / fftSize; 
		
		phi = Math.exp(Math.log(decayPerSecond) / updatesPerSecond);
		
	}
	
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
		
		

		if (level > averagedLevel) { 
			outputVal =  (level - averagedLevel) / normalizingVal;
		} else {
			outputVal = 0.0;
		}
		
		averagedLevel = averagedLevel * phi + level*(1 - phi);
		
		double actualOutput;
		
		double c = 0.75;
		if (outputVal < c * lastOutput) {
			actualOutput = c * lastOutput;
		} else {
			actualOutput = outputVal;
		}
		
		
		regular = outputVal;
		lastOutput = actualOutput;
		return actualOutput;
		
	}
	
	
	double getAveragedLevel() {
		return averagedLevel;
	}
	
	double getRegular() {
		return regular;
	}
	
}
