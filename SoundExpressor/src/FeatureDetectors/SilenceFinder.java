package FeatureDetectors;

import Common.FeatureList;

/**
 * A simple state machine that takes in the current level data, and looks for silents. Outputs
 * true if there has been silence for an appreciable amount of time.
 * @author Steve Levine
 *
 */
public class SilenceFinder extends FeatureDetector {

	protected long numSilents;
	protected long WAIT_PERIOD;
	protected double EPSILON = 5.0;
	
	
	@Override
	public void init() {
		EPSILON = 5.0;
		WAIT_PERIOD = (long) (0.5 * UPDATES_PER_SECOND);
		numSilents = 0;
	}
	
	@Override
	public void computeFeatures(double[] frequencies, double[] magnitudes, FeatureList featureList) {
		// Just look out the other features!
		if (featureList.containsFeature("OVERALL_LEVEL")) {
			double level = (Double) featureList.getFeature("OVERALL_LEVEL");
			boolean silent = update(level);
			featureList.addFeature("SILENT", silent);
		}
		
	}


	// For old stuff
	public SilenceFinder(long WAIT_PERIOD) {
		this.WAIT_PERIOD = WAIT_PERIOD;
		numSilents = 0;
	}
	
	private boolean update(double level) {
		if (level < EPSILON) {
			numSilents++;
		} else {
			numSilents = 0;
		}
		
		return (numSilents >= WAIT_PERIOD);
	}


	
}
