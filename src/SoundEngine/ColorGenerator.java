package SoundEngine;

import java.awt.Color;

public interface ColorGenerator {
	// Get the current color
	public double getRed();
	public double getGreen();
	public double getBlue();
	public Color getColor();
	
	// State machine-like step functions
	void step(double val);
	void step(double val1, double val2);
}
