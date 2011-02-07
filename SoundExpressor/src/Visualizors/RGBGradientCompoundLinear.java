package Visualizors;

import java.awt.Color;

/**
 * Represents a cyclic linear gradient between n different colors! The last color is the first color. Accepts an argument in the rage 0.0 - 1.0.
 * @author steve
 *
 */
public class RGBGradientCompoundLinear {
	
	
	protected Color[] c1;
	protected Color[] c2;
	protected double[] startPoints;
	protected double[] endPoints;
	
	public RGBGradientCompoundLinear(Color[] c1, Color[] c2, double[] startPoints, double[] endPoints) {
		this.c1 = c1;
		this.c2 = c2;
		this.startPoints = startPoints;
		this.endPoints = endPoints;
	}
	
	/**
	 * Compute the gradient.
	 */
	public Color computeGradient(double x) {
		
		for(int i = 0; i < c1.length; i++) {
			if (x >= startPoints[i] && x <= endPoints[i]) {
				return RGBGradientLinear.linearGradient(c1[i], c2[i], (x - startPoints[i]) / (endPoints[i] - startPoints[i]));
			}
		}
		
		// If we get here, it didn't work!
		return Color.BLACK;
		
	}
	
}
