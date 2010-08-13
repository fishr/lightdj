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
		
		minFreq = 15;
		maxFreq = 70;
		normalizingVal = 40.0;
		decayRate = 0;
	}
	
}
