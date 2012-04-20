package PostProcessors;

import java.awt.Color;

import Common.ColorOutput;
import Common.FeatureList;
import LightDJGUI.GenericKnob;
import Visualizors.RGBGradientLinear;

/**
 * This post processor takes care of white bursts and the "emergency lighting" commands.
 * @author steve
 *
 */
public class Blackout extends PostProcessor {

	protected GenericKnob lengthKnob;
	protected boolean active;
	protected long startTime;
	
	
	public Blackout(double updatesPerSecond) {
		super(updatesPerSecond);
		
	}

	@Override
	public String getName() {
		return "Blackout";
	}

	@Override
	public void init() {
		
		active = false;
		
		// Make a user control
		//lengthKnob = new GenericKnob(0.25f, 40, "Length");
		//requestUserControl(lengthKnob);
		
	}

	@Override
	public void postProcess(ColorOutput colorOutput, FeatureList featureList) {
		
		
		// Currently not active. Should it became active?
		if (((Double) featureList.getFeature("KEY_BACKSPACE")) == 1.0) {
			// ACTIVATE!
			active = true;
			startTime = System.currentTimeMillis();
		} else {
			active = false;
		}
		
		
		// Continue a burst that is currently running
		if (active) {
			colorOutput.allOff();
		}
		
	}

	@Override
	public boolean isActive() {
		return active;
	}


}
