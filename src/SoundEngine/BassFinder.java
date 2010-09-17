package SoundEngine;

/**
 * A state-machine like object that, when stepped with FFT values, attempts to output the current bass level.
 * Attempts to auto-adapt to changing volume.
 * @author steve
 *
 */
public class BassFinder extends FrequencyRangeFinder {
	
	public BassFinder(int sampleRate, int fftSize) {
		super(sampleRate, fftSize);
		
		minFreq = 10;
		maxFreq = 70;
		normalizingVal = 30.0;
		averageHalfLife = 1.5;
		decayRate = 1.0 / (50);
	}
	
}
