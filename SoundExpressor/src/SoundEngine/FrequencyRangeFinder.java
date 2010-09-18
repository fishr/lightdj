package SoundEngine;

/**
 * A state-machine like object that, when stepped with FFT values, attempts to output the current bass level.
 * Attempts to auto-adapt to changing volume levels.
 * @author steve
 *
 */
public class FrequencyRangeFinder {
	
	protected double averageHalfLife = 0.5;
	
	protected double updatesPerSecond;
	protected double phi;
	protected double averagedLevel = 0;
	protected double lastOutput = 0;
	protected double decayRate = 0.75;
	
	protected double normalizingVal = 1.0;
	
	
	protected double regular = 0;
	
	protected double minFreq = 0;
	protected double maxFreq = 20000;
	
	public FrequencyRangeFinder(int sampleRate, int fftSize) {
		updatesPerSecond = 1.0 * sampleRate / fftSize; 
		
		phi = Math.pow(0.5, 1/(averageHalfLife * updatesPerSecond));
		
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
		
		
		// Compute a very low-passed version of the signal to use as an estimate of the overall
		// level of this frequency range. This is the "adaptive" part that allows the frequency
		// range finder to adjust to different volume levels
		double threshold = averagedLevel * 1.25 + 5.0;
		if (level > threshold) { 
			outputVal =  (level - threshold) / normalizingVal;
		} else {
			outputVal = 0.0;
		}
		if (outputVal > 1.0) {
			outputVal = 1.0;
		}
		
		
		averagedLevel = averagedLevel * phi + level*(1 - phi);
		
		double actualOutput;
		
//		// Limit how fast the output can fal, in an attempt to minimize flicker
//		if (outputVal < decayRate * lastOutput) {
//			actualOutput = decayRate * lastOutput;
//		} else {
//			actualOutput = outputVal;
//		}
		
		// Limit how fast the output can fal, in an attempt to minimize flicker
		if (outputVal < lastOutput - decayRate) {
			actualOutput = lastOutput - decayRate;
		} else {
			actualOutput = outputVal;
		}
		
		//actualOutput = Math.log(actualOutput + 1.0) / Math.log(2);
		
		
		
//		if (actualOutput > 0.5) {
//			actualOutput = actualOutput;
//		} else {
//			actualOutput = 0;
//		}
		
		
		
		
		lastOutput = actualOutput;
		return actualOutput;
		
	}
	
	
	double getAveragedLevel() {
		return averagedLevel;
	}
	
	
}