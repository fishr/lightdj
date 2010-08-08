package SoundEngine;

/**
 * A state-machine like object that, when stepped with FFT values, attempts to output the current bass level.
 * Attempts to auto-adapt to changing volume.
 * @author steve
 *
 */
public class ClapFinder extends FrequencyRangeFinder {
	
	public ClapFinder(int sampleRate, int fftSize) {
		super(sampleRate, fftSize);
		
		minFreq = 2000;
		maxFreq = 15000;
		normalizingVal = 0.1;
		
	}
	
	
	@Override
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
			sum += Math.pow(magnitudes[i], 0.1);
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
		
		double c = 0.25;
		if (outputVal < c * lastOutput) {
			actualOutput = c * lastOutput;
		} else {
			actualOutput = outputVal;
		}
		
		
		regular = outputVal;
		lastOutput = actualOutput;
		return actualOutput;
		
	}
	
	
}
