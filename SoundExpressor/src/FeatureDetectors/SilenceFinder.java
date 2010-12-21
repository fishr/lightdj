package FeatureDetectors;

/**
 * A simple state machine that takes in the current level data, and looks for silents. Outputs
 * true if there has been silence for an appreciable amount of time.
 * @author steve
 *
 */
public class SilenceFinder {

	protected long WAIT_PERIOD;
	protected long numSilents;
	
	protected double EPSILON = 5.0;
	
	public SilenceFinder(long WAIT_PERIOD) {
		this.WAIT_PERIOD = WAIT_PERIOD;
		numSilents = 0;
	}
	
	public boolean update(double level) {
		if (level < EPSILON) {
			numSilents++;
		} else {
			numSilents = 0;
		}
		
		return (numSilents >= WAIT_PERIOD);
	}
	
}
