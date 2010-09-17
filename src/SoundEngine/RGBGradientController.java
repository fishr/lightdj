package SoundEngine;

import java.awt.Color;

/**
 * Varies between two colors C0 and C1, based on a double value between 0 and 1 inclusive.
 * @author steve
 *
 */
public class RGBGradientController implements ColorGenerator{

	public Color c0;
	public Color c1;
	
	public double r = 0;
	public double g = 0;
	public double b = 0;
	
	public RGBGradientController(Color c0, Color c1) {
		this.c0 = c0;
		this.c1 = c1;
	}
	
	public void computeGradient(double val, double brightness) {
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
	}
	
	public double getRed() {return r;}
	public double getGreen() {return g;}
	public double getBlue() {return b;}
	
	public void step(double val) {
		computeGradient(val, 1.0);
	}
	
	public void step(double val1, double val2) {
		computeGradient(val1, val2);
	}
	
}
