package FeatureDetectors;

import java.util.LinkedList;
import java.util.List;

import Common.UserControl;
import Common.FeatureList;

/**
 * All FeatureDetectors must implement this interface!
 * @author Steve Levine
 */
public abstract class FeatureDetector {
	
	private List<UserControl> controls;
	
	protected int FFT_SIZE;
	protected double UPDATES_PER_SECOND;
	
	/**
	 * Do any necessary initialization. Called at startup.
	 * Request any user controls here!
	 */
	public abstract void init();
	
	/**
	 * Actually compute the features, using the given FFT.
	 * Adds the features to featureList.
	 */
	public abstract void computeFeatures(double[] frequencies, double[] magnitudes, FeatureList featureList);
	
	
	
	public FeatureDetector() {}
	
	public FeatureDetector(int fftSize, double updatesPerSecond) {
		controls = new LinkedList<UserControl>();
		FFT_SIZE = fftSize;
		UPDATES_PER_SECOND = updatesPerSecond;
	}

	/**
	 * FeatureDetectors may request user controls for user input using this function.
	 * Please note that this function only works when called inside init().
	 */
	protected void requestUserControl(UserControl control) {
		controls.add(control);
	}
	
	public List<UserControl> getRequestedUserControls() {
		return controls;
	}
		
}
