package SoundEngine;

/**
 * Measures the overall level of the music, like a VU meter. Gently lowpasses the output.
 * @author Steve
 *
 */
public class LevelMeter  {
	
	protected double averageHalfLife;
	
	protected double updatesPerSecond;
	protected double phi;
	protected double decayRate;
	protected double normalizingVal;
	
	protected double averagedLevel;
	
	public LevelMeter(int sampleRate, int fftSize) {

		// Initiate other parameters
		normalizingVal = 0.01;
		averageHalfLife = 0.0025;
		decayRate = 1.0 / (20);
		
		// Calculate some parameters
		updatesPerSecond = 1.0 * sampleRate / fftSize; 
		phi = Math.pow(0.5, 1/(averageHalfLife * updatesPerSecond));
		
	}
	
	public double getLevel(double[] frequencies, double[] magnitudes) {
		
		double sum = 0;
		int n = 0;
		for(int i = 1; i < frequencies.length; i++) {
			sum += Math.log(1 + magnitudes[i]);
			n++;
		}
		
		double level = sum / n;
		averagedLevel = averagedLevel * phi + level*(1 - phi);
		return averagedLevel / normalizingVal;
		
		
		
	}
	
}
