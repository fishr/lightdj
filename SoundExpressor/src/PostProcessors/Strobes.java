package PostProcessors;

import Common.ColorOutput;
import Common.FeatureList;
import LightDJGUI.GenericKnob;

/**
 * A post processor that takes care of white strobes, uv strobes, and strobe scattering effects.
 * @author steve
 *
 */
public class Strobes extends PostProcessor {

	GenericKnob lengthKnob;
	
	public Strobes(double updatesPerSecond) {
		super(updatesPerSecond);
		
	}

	@Override
	public String getName() {
		return "Strobes";
	}

	@Override
	public void init() {
		
		
	}

	@Override
	public void postProcess(ColorOutput colors, FeatureList featureList) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}


}
