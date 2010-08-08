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
		
		minFreq = 300;
		maxFreq = 2400;
		normalizingVal = 2.0;
		phi = 0;
	}
	
}
