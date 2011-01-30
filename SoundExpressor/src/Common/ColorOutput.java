package Common;

import java.awt.Color;

import Visualizors.RGBGradientLinear;

/**
 * The output of a Visualizer plugin! Represents a complete coloring of all the lights
 * @author Steve Levine
 *
 */
public class ColorOutput {
	
	// Change these to match the number of lights in the system
	public static final int NUM_RGB_LIGHTS = 64;
	public static final int NUM_STROBE_LIGHTS = 8;
	public static final int NUM_UV_LIGHTS = 8;
	
	public static final int NUM_FRONT_RGB_PANELS = 8;
	public static final int NUM_REAR_RGB_PANELS = 8;
	public static final int NUM_UVWHITE_PANELS = 8;
	
	
	public Color[] rgbLights;
	public double[] whiteLights;
	public double[] uvLights;
	
	// Compression information
	public OverallOutputCompression overallOutputCompression;
	public RGBFrontColorOutputCompression rgbFrontColorOutputCompression;
	public RGBRearColorOutputCompression rgbRearColorOutputCompression;
	public UVWhiteColorOutputCompression uvWhiteColorOutputCompression;
	
	// Denotes the most efficient way to encode a given ColorOutput when sent via the serial protocol.
	public enum OverallOutputCompression {
		OVERALL_COMPRESSION_NONE,
		OVERALL_COMPRESSION_EMERGENCY_LIGHTING,
		OVERALL_COMPRESSION_ALL_OFF, 
		OVERALL_COMPRESSION_WHITE_STROBE,
		OVERALL_COMPRESSION_UV_STROBE
	}
	public enum RGBFrontColorOutputCompression {
		RGB_FRONT_COMPRESSION_DIFF,
		RGB_FRONT_COMPRESSION_LEDS_SAME,
		RGB_FRONT_COMPRESSION_PANELS_SAME
		
	}
	public enum RGBRearColorOutputCompression {
		RGB_REAR_COMPRESSION_DIFF,
		RGB_REAR_COMPRESSION_LEDS_SAME,
		RGB_REAR_COMPRESSION_PANELS_SAME
		
	}
	public enum UVWhiteColorOutputCompression {
		UVWHITE_COMPRESSION_WHITE_AND_UV_SAME,
		UVWHITE_COMPRESSION_WHITE_AND_UV_DIFF
	}
	
	// Initialize everything to black
	public ColorOutput() {
		rgbLights = new Color[NUM_RGB_LIGHTS];
		for(int i = 0; i < NUM_RGB_LIGHTS; i++) {
			rgbLights[i] = Color.BLACK;
		}
		
		whiteLights = new double[NUM_STROBE_LIGHTS];
		for(int i = 0; i < NUM_STROBE_LIGHTS; i++) {
			whiteLights[i] = 0.0;
		}
		
		uvLights = new double[NUM_UV_LIGHTS];
		for(int i = 0; i < NUM_UV_LIGHTS; i++) {
			uvLights[i] = 0.0;
		}
		
		// Set the current compression
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		rgbFrontColorOutputCompression = RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_LEDS_SAME;
		rgbRearColorOutputCompression = RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_LEDS_SAME;
		uvWhiteColorOutputCompression = UVWhiteColorOutputCompression.UVWHITE_COMPRESSION_WHITE_AND_UV_SAME;
		
	}
	
	/**
	 * API functions to easily set specific stuff. This will also trigger efficient compression.
	 */
	public void emergencyLighting() {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_EMERGENCY_LIGHTING;
		
		for(int i = 0; i < NUM_RGB_LIGHTS; i++) {
			rgbLights[i] = Color.WHITE;
		}
		
		whiteLights = new double[NUM_STROBE_LIGHTS];
		for(int i = 0; i < NUM_STROBE_LIGHTS; i++) {
			whiteLights[i] = 1.0;
		}
		
		uvLights = new double[NUM_UV_LIGHTS];
		for(int i = 0; i < NUM_UV_LIGHTS; i++) {
			uvLights[i] = 0.0;
		}
		
		
	}
	
	public void allOff() {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_ALL_OFF;
		
		// Turn them all off
		for(int i = 0; i < NUM_RGB_LIGHTS; i++) {
			rgbLights[i] = Color.BLACK;
		}
		
		for(int i = 0; i < NUM_STROBE_LIGHTS; i++) {
			whiteLights[i] = 0.0;
		}
		
		for(int i = 0; i < NUM_UV_LIGHTS; i++) {
			uvLights[i] = 0.0;
		}
		
	}
	
	public void setAllFrontPanels(Color c0, Color c1, Color c2, Color c3) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		rgbFrontColorOutputCompression = RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_PANELS_SAME;
		
		for(int panelIndex = 0; panelIndex < NUM_FRONT_RGB_PANELS; panelIndex++) {
			int i = 4 * panelIndex;
			rgbLights[i] = c0;
			rgbLights[i + 1] = c1;
			rgbLights[i + 2] = c2;
			rgbLights[i + 3] = c3;
		}
		
	}
	
	public void setAllRearPanels(Color c0, Color c1, Color c2, Color c3) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		rgbRearColorOutputCompression = RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_PANELS_SAME;
		
		for(int panelIndex = 0; panelIndex < NUM_REAR_RGB_PANELS; panelIndex++) {
			int i = 4*NUM_FRONT_RGB_PANELS + 4 * panelIndex;
			rgbLights[i] = c0;
			rgbLights[i + 1] = c1;
			rgbLights[i + 2] = c2;
			rgbLights[i + 3] = c3;
		}
	}
	
	public void setAllFrontRGBLEDs(Color c) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		rgbFrontColorOutputCompression = RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_LEDS_SAME;
		
		for(int i = 0; i < NUM_FRONT_RGB_PANELS * 4; i++) {
			rgbLights[i] = c;
		}
		
	}
	
	public void setAllRearRGBLEDs(Color c) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		rgbRearColorOutputCompression = RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_LEDS_SAME;
		
		for(int i = 0; i < NUM_REAR_RGB_PANELS * 4; i++) {
			rgbLights[i + NUM_FRONT_RGB_PANELS * 4] = c;
		}
		
		
	}
	
	
	public void setAllUVWhites(double uv, double white) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		uvWhiteColorOutputCompression = UVWhiteColorOutputCompression.UVWHITE_COMPRESSION_WHITE_AND_UV_SAME;
		
		for(int panelIndex = 0; panelIndex < NUM_UVWHITE_PANELS; panelIndex++) {
			uvLights[panelIndex] = uv;
			whiteLights[panelIndex] = white;
		}
		
	}
	
	public void setFrontPanel(int panelIndex, Color c0, Color c1, Color c2, Color c3) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		rgbFrontColorOutputCompression = RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_DIFF;
		
		if (panelIndex < 0 || panelIndex >= NUM_FRONT_RGB_PANELS) {
			throw new RuntimeException("Error: Invalid Front PanelIndex: " + panelIndex);
		}
		
		int i = 4*panelIndex;
		rgbLights[i] = c0;
		rgbLights[i + 1] = c1;
		rgbLights[i + 2] = c2;
		rgbLights[i + 3] = c3;
		
		
	}
	
	public void setRearPanel(int panelIndex, Color c0, Color c1, Color c2, Color c3) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		rgbRearColorOutputCompression = RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_DIFF;
		
		if (panelIndex < 0 || panelIndex >= NUM_REAR_RGB_PANELS) {
			throw new RuntimeException("Error: Invalid Rear PanelIndex: " + panelIndex);
		}
		
		int i = 4*NUM_FRONT_RGB_PANELS + 4*panelIndex;
		rgbLights[i] = c0;
		rgbLights[i + 1] = c1;
		rgbLights[i + 2] = c2;
		rgbLights[i + 3] = c3;
		
	}
	
	public void setUVWhitePanel(int panelIndex, double uv, double white) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		uvWhiteColorOutputCompression = UVWhiteColorOutputCompression.UVWHITE_COMPRESSION_WHITE_AND_UV_DIFF;
		
		if (panelIndex < 0 || panelIndex >= NUM_UVWHITE_PANELS) {
			throw new RuntimeException("Error: Invalid UV/White PanelIndex: " + panelIndex);
		}
		
		uvLights[panelIndex] = uv;
		whiteLights[panelIndex] = white;
		
	}
	
	
	public void setFrontRGBLight(int i, Color c) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		rgbFrontColorOutputCompression = RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_DIFF;
		
		rgbLights[i] = c; 
		
	}
	
	public void setRearRGBLight(int i, Color c) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		rgbRearColorOutputCompression = RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_DIFF;
		
		rgbLights[4*NUM_FRONT_RGB_PANELS + i] = c; 
		
	}
	
	public void setWhiteStrobe() {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_WHITE_STROBE;
		
		for(int i = 0; i < NUM_RGB_LIGHTS; i++) {
			rgbLights[i] = Color.BLACK;
		}
		
		whiteLights = new double[NUM_STROBE_LIGHTS];
		for(int i = 0; i < NUM_STROBE_LIGHTS; i++) {
			whiteLights[i] = 0.0;
		}
		
		uvLights = new double[NUM_UV_LIGHTS];
		for(int i = 0; i < NUM_UV_LIGHTS; i++) {
			uvLights[i] = 0.0;
		}
	}
	
	public void setUVStrobe() {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_UV_STROBE;
		
		for(int i = 0; i < NUM_RGB_LIGHTS; i++) {
			rgbLights[i] = Color.BLACK;
		}
		
		whiteLights = new double[NUM_STROBE_LIGHTS];
		for(int i = 0; i < NUM_STROBE_LIGHTS; i++) {
			whiteLights[i] = 0.0;
		}
		
		uvLights = new double[NUM_UV_LIGHTS];
		for(int i = 0; i < NUM_UV_LIGHTS; i++) {
			uvLights[i] = 0.0;
		}
	}
	
	
	/**
	 * Mix the two colors linearly together!
	 * @param c1
	 * @param c2
	 * @param alpha
	 * @return
	 */
	public static ColorOutput mix(ColorOutput c1, ColorOutput c2, double alpha) {
		ColorOutput out = new ColorOutput();
		
		if (alpha < 0.0) {
			alpha = 0.0;
		} else if (alpha > 1.0) {
			alpha = 1.0;
		}
		
		// Mix RGB lights
		for(int i = 0; i < NUM_RGB_LIGHTS; i++) {
			out.rgbLights[i] = RGBGradientLinear.linearGradient(c1.rgbLights[i], c2.rgbLights[i], alpha);
		}
		
		// Mix white lights
		for(int i = 0; i < NUM_STROBE_LIGHTS; i++) {
			out.whiteLights[i] = alpha * c2.whiteLights[i] + (1 - alpha) * c1.whiteLights[i];
		}
		
		// Mix UV lights
		for(int i = 0; i < NUM_UV_LIGHTS; i++) {
			out.uvLights[i] = alpha * c2.uvLights[i] + (1 - alpha) * c1.uvLights[i];
		}
		
		// Done mixing! Return the output.
		return out;
	}
	
	
}
