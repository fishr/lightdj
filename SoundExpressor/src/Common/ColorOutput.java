package Common;

import java.awt.Color;

import org.w3c.dom.css.RGBColor;

import Visualizors.RGBGradientLinear;

/**
 * The output of a Visualizer plugin! Represents a complete coloring of all the lights
 * @author Steve Levine
 *
 */
public class ColorOutput {
	
	// Change these to match the number of lights in the system
	public static int NUM_RGB_LIGHTS_FRONT = 32;
	public static int NUM_RGB_LIGHTS_REAR = 32;
	public static int NUM_STROBE_LIGHTS = 8;
	public static int NUM_UV_LIGHTS = 8;
	public static int NUM_LEDS_PER_RGB_BOARD = 4;
	
	public static int NUM_FRONT_RGB_PANELS = 8;
	public static int NUM_REAR_RGB_PANELS = 8;
	public static int NUM_UVWHITE_PANELS = 8;
	
	public static int START_REAR_LIGHT_ADDRESSES = 32;	// Computed elsewhere and set here
	public static int START_REAR_PANEL_ADDRESSES = 8;
	public static int START_UVWHITE_PANEL_ADDRESSES = 16;
	
	public Color[] rgbLightsFront;
	public Color[] rgbLightsRear;
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
	
	// Determine which type of compression will make the output the smallest!
	public void determineCompression() {
		// Overall output compression will have already be determined if a special command, like strobe() was called!
		
		// Determine any applicable front, rear, and uvwhite compression.
		this.rgbFrontColorOutputCompression = getFrontRGBCompression();
		this.rgbRearColorOutputCompression = getRearRGBCompression();
		this.uvWhiteColorOutputCompression = getUVWhiteCompression();
		
		if (this.overallOutputCompression == OverallOutputCompression.OVERALL_COMPRESSION_NONE && this.rgbFrontColorOutputCompression == RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_LEDS_SAME && this.rgbRearColorOutputCompression == RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_LEDS_SAME  && this.uvWhiteColorOutputCompression == UVWhiteColorOutputCompression.UVWHITE_COMPRESSION_WHITE_AND_UV_SAME && rgbLightsFront[0].equals(Color.BLACK) && whiteLights[0] == 0.0 && uvLights[0] == 0.0) {
			this.overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_ALL_OFF;
		}
		
		//System.out.println(overallOutputCompression + "-" + rgbFrontColorOutputCompression + "-" + rgbRearColorOutputCompression + "-" + uvWhiteColorOutputCompression);
		
	}
	
	protected RGBFrontColorOutputCompression getFrontRGBCompression() {
		if (allPanelsSame(rgbLightsFront, NUM_FRONT_RGB_PANELS, NUM_LEDS_PER_RGB_BOARD)) {
			// All the fronts are the same - this means either all the LED's are the same color, or just all the boards.
			if (isPanelSame(rgbLightsFront, NUM_LEDS_PER_RGB_BOARD)) {
				// All panels are the same, and all lights within the first panel are the same. Therefore all LED's are the same.
				return RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_LEDS_SAME;
			} else {
				return RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_PANELS_SAME;
			}
		} else {
			// All the panels are not the same. No compression for the front.
			return RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_DIFF;
		}
	}
	
	protected RGBRearColorOutputCompression getRearRGBCompression() {
		if (allPanelsSame(rgbLightsRear, NUM_REAR_RGB_PANELS, NUM_LEDS_PER_RGB_BOARD)) {
			// All the fronts are the same - this means either all the LED's are the same color, or just all the boards.
			if (isPanelSame(rgbLightsRear, NUM_LEDS_PER_RGB_BOARD)) {
				// All panels are the same, and all lights within the first panel are the same. Therefore all LED's are the same.
				return RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_LEDS_SAME;
			} else {
				return RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_PANELS_SAME;
			}
		} else {
			// All the panels are not the same. No compression for the front.
			return RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_DIFF;
		}
	}
	
	protected UVWhiteColorOutputCompression getUVWhiteCompression() {
		double val = whiteLights[0];
		for(int light = 1; light < NUM_UVWHITE_PANELS; light++) {
			if (whiteLights[light] != val) {
				return UVWhiteColorOutputCompression.UVWHITE_COMPRESSION_WHITE_AND_UV_DIFF;
			}
		}
		
		val = uvLights[0];
		for(int light = 1; light < NUM_UVWHITE_PANELS; light++) {
			if (uvLights[light] != val) {
				return UVWhiteColorOutputCompression.UVWHITE_COMPRESSION_WHITE_AND_UV_DIFF;
			}
		}
		
		// If we got here, all the same!
		return UVWhiteColorOutputCompression.UVWHITE_COMPRESSION_WHITE_AND_UV_SAME;
	}
	
	// Check if all of the panels are the same color.
	protected boolean allPanelsSame(Color[] colors, int numPanels, int numLEDsPerPanel) {
		for(int light = 0; light < numLEDsPerPanel; light++) {
			// All colors should be the same as this color.
			Color c = colors[light];
			for(int panel = 0; panel < numPanels; panel++) {
				int index = panel*numLEDsPerPanel + light;
				if (!c.equals(colors[index])) {
					// Not all the same!
					return false;
				}
			}
		}
		return true;
	}
	
	// Check if all of the LED's within some chosen panel are the same color.
	protected boolean isPanelSame(Color[] colors, int numLEDsPerPanel) {
		// Simply check the first panel
		Color c = colors[0];
		for(int light = 0; light < numLEDsPerPanel; light++) {
			if (!c.equals(colors[light])) {
				// Not all true!
				return false;
			}
		}
		return true;
	}
	
	// Initialize everything to black
	public ColorOutput() {
		rgbLightsFront = new Color[NUM_RGB_LIGHTS_FRONT];
		for(int i = 0; i < NUM_RGB_LIGHTS_FRONT; i++) {
			rgbLightsFront[i] = Color.BLACK;
		}
		
		rgbLightsRear = new Color[NUM_RGB_LIGHTS_REAR];
		for(int i = 0; i < NUM_RGB_LIGHTS_REAR; i++) {
			rgbLightsRear[i] = Color.BLACK;
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
	
	// Constructor that makes a copy
	public ColorOutput(ColorOutput c) {
		rgbLightsFront = new Color[NUM_RGB_LIGHTS_FRONT];
		for(int i = 0; i < NUM_RGB_LIGHTS_FRONT; i++) {
			rgbLightsFront[i] = c.rgbLightsFront[i];
		}
		
		rgbLightsRear = new Color[NUM_RGB_LIGHTS_REAR];
		for(int i = 0; i < NUM_RGB_LIGHTS_REAR; i++) {
			rgbLightsRear[i] = c.rgbLightsRear[i];
		}
		
		whiteLights = new double[NUM_STROBE_LIGHTS];
		for(int i = 0; i < NUM_STROBE_LIGHTS; i++) {
			whiteLights[i] = c.whiteLights[i];
		}
		
		uvLights = new double[NUM_UV_LIGHTS];
		for(int i = 0; i < NUM_UV_LIGHTS; i++) {
			uvLights[i] = c.uvLights[i];
		}
		
		// Set the current compression
		overallOutputCompression = c.overallOutputCompression;
		rgbFrontColorOutputCompression = c.rgbFrontColorOutputCompression;
		rgbRearColorOutputCompression = c.rgbRearColorOutputCompression;
		uvWhiteColorOutputCompression = c.uvWhiteColorOutputCompression;
	}
	
	/**
	 * API functions to easily set specific stuff. This will also trigger efficient compression.
	 */
	public void emergencyLighting() {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_EMERGENCY_LIGHTING;
		
		for(int i = 0; i < NUM_RGB_LIGHTS_FRONT; i++) {
			rgbLightsFront[i] = Color.WHITE;
		}
		
		for(int i = 0; i < NUM_RGB_LIGHTS_REAR; i++) {
			rgbLightsRear[i] = Color.WHITE;
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
		//overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_ALL_OFF;
		
		// Turn them all off
		for(int i = 0; i < NUM_RGB_LIGHTS_FRONT; i++) {
			rgbLightsFront[i] = Color.BLACK;
		}
		
		for(int i = 0; i < NUM_RGB_LIGHTS_REAR; i++) {
			rgbLightsRear[i] = Color.BLACK;
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
		//rgbFrontColorOutputCompression = RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_PANELS_SAME;
		
		for(int panelIndex = 0; panelIndex < NUM_FRONT_RGB_PANELS; panelIndex++) {
			int i = 4 * panelIndex;
			rgbLightsFront[i] = c0;
			rgbLightsFront[i + 1] = c1;
			rgbLightsFront[i + 2] = c2;
			rgbLightsFront[i + 3] = c3;
		}
		
	}
	
	public void setAllRearPanels(Color c0, Color c1, Color c2, Color c3) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		//rgbRearColorOutputCompression = RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_PANELS_SAME;
		
		for(int panelIndex = 0; panelIndex < NUM_REAR_RGB_PANELS; panelIndex++) {
			int i = 4 * panelIndex;
			rgbLightsRear[i] = c0;
			rgbLightsRear[i + 1] = c1;
			rgbLightsRear[i + 2] = c2;
			rgbLightsRear[i + 3] = c3;
		}
	}
	
	public void setAllFrontRGBLEDs(Color c) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		//rgbFrontColorOutputCompression = RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_LEDS_SAME;
		
		for(int i = 0; i < NUM_RGB_LIGHTS_FRONT; i++) {
			rgbLightsFront[i] = c;
		}
		
	}
	
	public void setAllRearRGBLEDs(Color c) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		//rgbRearColorOutputCompression = RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_LEDS_SAME;
		
		for(int i = 0; i < NUM_RGB_LIGHTS_REAR; i++) {
			rgbLightsRear[i] = c;
		}
		
		
	}
	
	
	public void setAllUVWhites(double uv, double white) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		//uvWhiteColorOutputCompression = UVWhiteColorOutputCompression.UVWHITE_COMPRESSION_WHITE_AND_UV_SAME;
		
		for(int panelIndex = 0; panelIndex < NUM_UVWHITE_PANELS; panelIndex++) {
			uvLights[panelIndex] = uv;
			whiteLights[panelIndex] = white;
		}
		
	}
	
	public void setFrontPanel(int panelIndex, Color c0, Color c1, Color c2, Color c3) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		//rgbFrontColorOutputCompression = RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_DIFF;
		
		if (panelIndex < 0 || panelIndex >= NUM_FRONT_RGB_PANELS) {
			//throw new RuntimeException("Error: Invalid Front PanelIndex: " + panelIndex);
			System.out.println("Warning: Invalid Front PanelIndex: " + panelIndex);
			return;
		}
		
		int i = 4*panelIndex;
		rgbLightsFront[i] = c0;
		rgbLightsFront[i + 1] = c1;
		rgbLightsFront[i + 2] = c2;
		rgbLightsFront[i + 3] = c3;
		
		
	}
	
	public void setRearPanel(int panelIndex, Color c0, Color c1, Color c2, Color c3) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		//rgbRearColorOutputCompression = RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_DIFF;
		
		if (panelIndex < 0 || panelIndex >= NUM_REAR_RGB_PANELS) {
			//throw new RuntimeException("Error: Invalid Rear PanelIndex: " + panelIndex);
			System.out.println("Warning: Invalid Rear PanelIndex: " + panelIndex);
			return;
		}
		
		int i = 4*panelIndex;
		rgbLightsRear[i] = c0;
		rgbLightsRear[i + 1] = c1;
		rgbLightsRear[i + 2] = c2;
		rgbLightsRear[i + 3] = c3;
		
	}
	
	public void setUVWhitePanel(int panelIndex, double uv, double white) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		//uvWhiteColorOutputCompression = UVWhiteColorOutputCompression.UVWHITE_COMPRESSION_WHITE_AND_UV_DIFF;
		
		if (panelIndex < 0 || panelIndex >= NUM_UVWHITE_PANELS) {
			//throw new RuntimeException("Error: Invalid UV/White PanelIndex: " + panelIndex);
			System.out.println("Warning: Invalid UV/White PanelIndex: " + panelIndex);
			return;
		}
		
		uvLights[panelIndex] = uv;
		whiteLights[panelIndex] = white;
		
	}
	
	
	public void setRGBLightFrontOrBack(int i, Color c) {
		if (i < NUM_RGB_LIGHTS_FRONT) {
			setFrontRGBLight(i, c);
		} else {
			setRearRGBLight(i - NUM_RGB_LIGHTS_FRONT, c);
		}
	}
	
	public void setFrontRGBLight(int i, Color c) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		//rgbFrontColorOutputCompression = RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_DIFF;
		
		if (i >= 0 && i < rgbLightsFront.length) { 
			rgbLightsFront[i] = c; 
		} else {
			System.out.println("Error: Invalid Front RGB Light Index: " + i);
		}
		
	}
	
	public void setRearRGBLight(int i, Color c) {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		//rgbRearColorOutputCompression = RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_DIFF;

		if (i >= 0 && i < rgbLightsRear.length) { 
			rgbLightsRear[i] = c; 
		} else {
			System.out.println("Error: Invalid Rear RGB Light Index: " + i);
		}
		
	}
	
	public void setWhiteStrobe() {
		overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_WHITE_STROBE;
		
		for(int i = 0; i < NUM_RGB_LIGHTS_FRONT; i++) {
			rgbLightsFront[i] = Color.BLACK;
		}
		
		for(int i = 0; i < NUM_RGB_LIGHTS_REAR; i++) {
			rgbLightsRear[i] = Color.BLACK;
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
		
		for(int i = 0; i < NUM_RGB_LIGHTS_FRONT; i++) {
			rgbLightsFront[i] = Color.BLACK;
		}
		
		for(int i = 0; i < NUM_RGB_LIGHTS_REAR; i++) {
			rgbLightsRear[i] = Color.BLACK;
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
		
		if (alpha < 0.01) {
			alpha = 0.0;
			return new ColorOutput(c1);
		} else if (alpha > 0.99) {
			alpha = 1.0;
			return new ColorOutput(c2);
		}
		
		// Mix RGB lights
		for(int i = 0; i < NUM_RGB_LIGHTS_FRONT; i++) {
			out.rgbLightsFront[i] = RGBGradientLinear.linearGradient(c1.rgbLightsFront[i], c2.rgbLightsFront[i], alpha);
		}
		for(int i = 0; i < NUM_RGB_LIGHTS_REAR; i++) {
			out.rgbLightsRear[i] = RGBGradientLinear.linearGradient(c1.rgbLightsRear[i], c2.rgbLightsRear[i], alpha);
		}
		
		// Mix white lights
		for(int i = 0; i < NUM_STROBE_LIGHTS; i++) {
			out.whiteLights[i] = alpha * c2.whiteLights[i] + (1 - alpha) * c1.whiteLights[i];
		}
		
		// Mix UV lights
		for(int i = 0; i < NUM_UV_LIGHTS; i++) {
			out.uvLights[i] = alpha * c2.uvLights[i] + (1 - alpha) * c1.uvLights[i];
		}
		
		out.overallOutputCompression = OverallOutputCompression.OVERALL_COMPRESSION_NONE;
		
		// Compress the data maximally
		//if (c1.rgbFrontColorOutputCompression == RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_LEDS_SAME && c2.rgbFrontColorOutputCompression == RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_LEDS_SAME) {
		//	out.rgbFrontColorOutputCompression = RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_LEDS_SAME;
		//} else if (c1.rgbFrontColorOutputCompression == RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_PANELS_SAME && c2.rgbFrontColorOutputCompression == RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_PANELS_SAME) {
		//	out.rgbFrontColorOutputCompression = RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_PANELS_SAME;
		//} else if (c1.rgbFrontColorOutputCompression == RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_LEDS_SAME && c2.rgbFrontColorOutputCompression == RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_PANELS_SAME  || c1.rgbFrontColorOutputCompression == RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_PANELS_SAME && c2.rgbFrontColorOutputCompression == RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_LEDS_SAME) {
		//	out.rgbFrontColorOutputCompression = RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_PANELS_SAME;
		//} else {
		//	out.rgbFrontColorOutputCompression = RGBFrontColorOutputCompression.RGB_FRONT_COMPRESSION_DIFF;
		//}
		
		//if (c1.rgbRearColorOutputCompression == RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_LEDS_SAME && c2.rgbRearColorOutputCompression == RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_LEDS_SAME) {
		//	out.rgbRearColorOutputCompression = RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_LEDS_SAME;
		//} else if (c1.rgbRearColorOutputCompression == RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_PANELS_SAME && c2.rgbRearColorOutputCompression == RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_PANELS_SAME) {
		//	out.rgbRearColorOutputCompression = RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_PANELS_SAME;
		//} else if (c1.rgbRearColorOutputCompression == RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_LEDS_SAME && c2.rgbRearColorOutputCompression == RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_PANELS_SAME  || c1.rgbRearColorOutputCompression == RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_PANELS_SAME && c2.rgbRearColorOutputCompression == RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_LEDS_SAME) {
		//	out.rgbRearColorOutputCompression = RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_PANELS_SAME;
		//} else {
		//	out.rgbRearColorOutputCompression = RGBRearColorOutputCompression.RGB_REAR_COMPRESSION_DIFF;
		//}
		
		//if (c1.uvWhiteColorOutputCompression == UVWhiteColorOutputCompression.UVWHITE_COMPRESSION_WHITE_AND_UV_SAME && c2.uvWhiteColorOutputCompression == UVWhiteColorOutputCompression.UVWHITE_COMPRESSION_WHITE_AND_UV_SAME) {
		//	out.uvWhiteColorOutputCompression = UVWhiteColorOutputCompression.UVWHITE_COMPRESSION_WHITE_AND_UV_SAME;
		//} else {
		//	out.uvWhiteColorOutputCompression = UVWhiteColorOutputCompression.UVWHITE_COMPRESSION_WHITE_AND_UV_DIFF;
		//}
		
		// Done mixing! Return the output.
		return out;
	}
	
}
