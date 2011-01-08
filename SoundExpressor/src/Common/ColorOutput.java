package Common;

import java.awt.Color;

/**
 * The output of a Visualizer plugin! Represents a complete coloring of all the lights
 * @author Steve Levine
 *
 */
public class ColorOutput {
	
	// Change these to match the number of lights in the system
	public static final int NUM_RGB_LIGHTS = 2;
	public static final int NUM_STROBE_LIGHTS = 0;
	
	
	public Color[] rgbLights;
	public double[] strobeLights;
	public double uvLight;
	
	// Initialize everything to black
	public ColorOutput() {
		rgbLights = new Color[NUM_RGB_LIGHTS];
		for(int i = 0; i < NUM_RGB_LIGHTS; i++) {
			rgbLights[i] = Color.BLACK;
		}
		
		strobeLights = new double[NUM_STROBE_LIGHTS];
		for(int i = 0; i < NUM_STROBE_LIGHTS; i++) {
			strobeLights[i] = 0.0;
		}
		
		uvLight = 0.0;
		
	}
	
}
