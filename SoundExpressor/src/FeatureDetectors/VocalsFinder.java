package FeatureDetectors;


/**
 * A state-machine like object that, when stepped with FFT values, attempts to output the current vocals level.
 * Attempts to auto-adapt to changing volume.
 * @author Steve Levine
 *
 */
public class VocalsFinder extends FrequencyRangeFinder {
	public VocalsFinder(int sampleRate, int fftSize) {
		super(sampleRate, fftSize);
		
		minFreq = 1000;
		maxFreq = 16000;
		normalizingVal = 15.0;
		decayRate = 0.5;
		phi = 0.9;
	}
	
}
