package Common;

import java.awt.Color;

/**
 * The output of a Visualizer plugin! Represents a complete coloring of all the lights
 * @author Steve Levine
 *
 */
public class ColorOutput {
	
	// Change these to match the number of lights in the system
	public static final int NUM_RGB_LIGHTS = 4;
	public static final int NUM_STROBE_LIGHTS = 0;
	public static final int NUM_UV_LIGHTS = 0;
	
	
	public Color[] rgbLights;
	public double[] strobeLights;
	public double[] uvLights;
	
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
		
		uvLights = new double[NUM_UV_LIGHTS];
		for(int i = 0; i < NUM_UV_LIGHTS; i++) {
			uvLights[i] = 0.0;
		}
		
	}
	
}
