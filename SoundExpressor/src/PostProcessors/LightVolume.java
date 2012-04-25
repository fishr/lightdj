package PostProcessors;

import java.awt.Color;

import Common.ColorOutput;
import Common.FeatureList;
import LightDJGUI.GenericKnob;
import PartyLightsController.PartyLightsController16.LightPlacement;
import Visualizors.RGBGradientLinear;

/**
 * This post processor adjusts the overall "light volume" of our lights.
 * @author steve
 *
 */
public class LightVolume extends PostProcessor {

	protected GenericKnob volumeKnob;
	
	
	public LightVolume(double updatesPerSecond) {
		super(updatesPerSecond);
		
	}

	@Override
	public String getName() {
		return "Light Volume";
	}

	@Override
	public void init() {
		// Make a user control
		volumeKnob = new GenericKnob(1.0f, 40, "Volume");
		requestUserControl(volumeKnob);
		
	}

	@Override
	public void postProcess(ColorOutput colorOutput, FeatureList featureList) {
		float volume = volumeKnob.getValue();
		
		// Scale all of the front panels
		for(int light = 0; light < ColorOutput.NUM_FRONT_RGB_PANELS*ColorOutput.NUM_LEDS_PER_RGB_BOARD; light++) {
			colorOutput.rgbLightsFront[light] = RGBGradientLinear.linearGradient(Color.BLACK, colorOutput.rgbLightsFront[light], volume);
		}
		
		// Scale all of the front panels
		for(int light = 0; light < ColorOutput.NUM_REAR_RGB_PANELS*ColorOutput.NUM_LEDS_PER_RGB_BOARD; light++) {
			colorOutput.rgbLightsRear[light] = RGBGradientLinear.linearGradient(Color.BLACK, colorOutput.rgbLightsRear[light], volume);
		}
		
		// Scale all of the front panels
		for(int light = 0; light < ColorOutput.NUM_UVWHITE_PANELS; light++) {
			colorOutput.uvLights[light] = volume * colorOutput.uvLights[light];
			colorOutput.whiteLights[light] = volume * colorOutput.whiteLights[light];
		}
		
	}

	@Override
	public boolean isActive() {
		return true;
	}


}
