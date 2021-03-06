package Visualizors;

import java.awt.Color;


/**
 * Varies between two colors C0 and C1, based on a double value between 0 and 1 inclusive.
 * @author Steve Levine
 *
 */
public class RGBGradientLinear implements ColorGenerator{

	public Color c0;
	public Color c1;
	
	public double r = 0;
	public double g = 0;
	public double b = 0;
	
	public RGBGradientLinear(Color c0, Color c1) {
		this.c0 = c0;
		this.c1 = c1;
	}
	
	public Color computeGradient(double val, double brightness) {
		// Constrain the value to be in [0, 1]
		if (val > 1.0) {
			val = 1.0;
		} else if (val < 0.0) {
			val = 0.0;
		}
		
		if (brightness > 1.0) {
			brightness = 1.0;
		} else if (brightness < 0.0) {
			brightness = 0.0;
		}
		
		r = brightness * (val * c1.getRed() + (1 - val) * c0.getRed()) / 255.0;
		g = brightness * (val * c1.getGreen() + (1 - val) * c0.getGreen()) / 255.0;
		b = brightness * (val * c1.getBlue() + (1 - val) * c0.getBlue()) / 255.0;
		
		return getColor();
	}
	
	public double getRed() {return r;}
	public double getGreen() {return g;}
	public double getBlue() {return b;}
	public Color getColor() {return new Color((float) r, (float) g, (float) b);}
	
	public void step(double val) {
		computeGradient(val, 1.0);
	}
	
	public void step(double val1, double val2) {
		computeGradient(val1, val2);
	}
	
	public static Color linearGradient(Color c1, Color c2, double alpha) {
		// Constrain the value to be in [0, 1]
		if (alpha > 1.0) {
			alpha = 1.0;
		} else if (alpha < 0.0) {
			alpha = 0.0;
		}
		
		float[] rgb_c1 = new float[3];
		float[] rgb_c2 = new float[3];
		c1.getRGBColorComponents(rgb_c1);
		c2.getRGBColorComponents(rgb_c2);
		float r = (float) (alpha * rgb_c2[0] + (1 - alpha) * rgb_c1[0]);
		float g = (float) (alpha * rgb_c2[1] + (1 - alpha) * rgb_c1[1]);
		float b = (float) (alpha * rgb_c2[2] + (1 - alpha) * rgb_c1[2]);
		
		return new Color(r, g, b);
	}
	
}
