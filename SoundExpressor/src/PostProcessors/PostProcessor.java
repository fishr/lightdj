package PostProcessors;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import SoundEngine.VisualizationEngineParty;

import Common.ColorOutput;
import Common.FeatureList;
import Common.UserControl;


/**
 * Any Visualizer must implement this interface!
 * @author Steve Levine
 *
 */
public abstract class PostProcessor {

	private List<UserControl> controls;
	protected double UPDATES_PER_SECOND;
	
	/**
	 * Do any necessary initialization. Request any user controls here.
	 */
	public abstract void init();
	
	/**
	 * The core method that visualizes a featureList
	 */
	public abstract void postProcess(ColorOutput colors, FeatureList featureList);

	/**
	 * Need to know the name!
	 */
	public abstract String getName();
	
	
	public PostProcessor(double updatesPerSecond) {
		UPDATES_PER_SECOND = updatesPerSecond;
		controls = new ArrayList<UserControl>();
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
	
	/**
	 * Returns true iif this post processor is currently doing something.
	 */
	public abstract boolean isActive();
	
	
	/**
	 * Scale helper function
	 */
	public int scale(int val) {
		return VisualizationEngineParty.scale(val);
	}
}
