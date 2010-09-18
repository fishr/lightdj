package SoundEngine;

import java.awt.Color;

/**
 * Varies between two colors C0 and C1, based on a double value between 0 and 1 inclusive.
 * @author steve
 *
 */
public class HueRotator implements ColorGenerator {

	public double hue = 0.0;
	public double step = 0.0;
	public boolean colorUp = false;
	
	public double r = 0;
	public double g = 0;
	public double b = 0;
	
	public HueRotator(double initHue, double step) {
		this.hue = initHue;
		this.step = step;
	}
	
	public void computeHue(double val) {
		// Constrain the value to be in [0, 1]
		if (val > 1.0) {
			val = 1.0;
		} else if (val < 0.0) {
			val = 0.0;
		}
		
		if (val > 0.85 && !colorUp) {
			colorUp = true;
			step = Math.random();
			hue = (hue + step) % 1.0f;
		} else if (val < 0.5 && colorUp) {
			colorUp = false;
		}
		
		Color color = Color.getHSBColor((float) hue, 1.0f, 1.0f);
		r = val * color.getRed() / 255.0;
		g = val * color.getGreen() / 255.0;
		b = val * color.getBlue() / 255.0;
		
		
	}
	
	public double getRed() {return r;}
	public double getGreen() {return g;}
	public double getBlue() {return b;}
	public Color getColor() {return new Color((float) r, (float) g, (float) b);}

	@Override
	public void step(double val) {
		computeHue(val);
	}

	@Override
	public void step(double val1, double val2) {
		computeHue(val1);
	}
	
}
