package LightDJGUI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import Visualizors.RGBGradientCompoundLinear;

/**
 * Represents a scrolling spectrum, showing the past spectrums as a function of time!
 * @author Steve Levine
 *
 */
public class ScrollingSpectrum {
	protected int screenX;
	protected int screenY;
	protected int width;
	protected int height;
	
	protected BufferedImage buffer;
	protected Graphics2D outputG2D;
	
	protected int currentX = 0;
	
	protected double minFreq;
	protected double maxFreq;
	protected double max_val;
	protected double fftMaxFreq;
	protected int fftSize;
	
	protected int[] interpolationIndices;
	protected double[] interpolationBlends;
	
	protected RGBGradientCompoundLinear gradient;
	
	
	public ScrollingSpectrum(int screenX, int screenY, int width, int height, Graphics2D g2D, double minFreq, double maxFreq, double max_val, int fftSize, double fftMaxFreq) {
		this.screenX = screenX;
		this.screenY = screenY;
		this.width = width;
		this.height = height;
		outputG2D = g2D;
		this.minFreq = minFreq;
		this.maxFreq = maxFreq;
		this.max_val = max_val;
		this.fftSize = fftSize;
		this.fftMaxFreq = fftMaxFreq;

		setSize(width, height);
		
		Color darkBlue = new Color(0, 0, 70);
		Color darkReddish = new Color(200, 0, 70);
		Color yellow = new Color(255, 255, 0);
		gradient = new RGBGradientCompoundLinear(new Color[]{Color.BLACK, darkBlue, darkReddish, yellow, Color.WHITE}, new double[]{0.0, 0.2, 0.55, 0.9, 1.0});
		//gradient = new RGBGradientCompoundLinear(new Color[]{Color.BLACK, Color.WHITE}, new double[]{0.0, 1.0});

		
	}
	
	
	private void setSize(int w, int h) {
		
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		// Precompute the indices and blending for interpolations, so that this won't have to happen later.
		interpolationIndices = new int[h];
		interpolationBlends = new double[h];
		
		double logMinFreq = Math.log10(minFreq);
		double logMaxFreq = Math.log10(maxFreq);
		for(int yPixelIndex = 0; yPixelIndex < height; yPixelIndex++) {
			double frequency = Math.pow(10.0,  ((logMaxFreq - logMinFreq) / height * yPixelIndex + logMinFreq));
			
			int index = (int) Math.floor(frequency * fftSize / fftMaxFreq); 
			double xPrevious = fftMaxFreq / fftSize * index;
			double xNext = fftMaxFreq / fftSize * (index + 1);
			double alpha = (frequency - xPrevious) / (xNext - xPrevious);
			
			interpolationIndices[yPixelIndex] = index;
			interpolationBlends[yPixelIndex] = alpha;
		}	
	}
	
	public void updateWithNewSpectrum(double[] frequencies, double magnitudes[]) {
		Graphics2D g2D = (Graphics2D) buffer.getGraphics();
		
		// Draw this spectrum, linearly interpolating between frequencies
		double largestFreq = frequencies[frequencies.length - 1];
		
		double logMinFreq = Math.log10(minFreq);
		double logMaxFreq = Math.log10(maxFreq);
		
		
		for(int yPixelIndex = 0; yPixelIndex < height; yPixelIndex++) {
			double alpha = interpolationBlends[yPixelIndex];
			int index = interpolationIndices[yPixelIndex];
			
			//double magnitude = (1 - alpha) * magnitudes[index] + alpha * magnitudes[index + 1];
			double magnitude = Math.log10((1 - alpha) * magnitudes[index]* magnitudes[index] + alpha * magnitudes[index + 1]* magnitudes[index + 1]);
			
			g2D.setColor(getColor(magnitude / max_val));
			g2D.drawRect(currentX, height - yPixelIndex - 1, 1, 1);
		}
		
		
		// Increment x!
		if (currentX++ > width) {
			currentX = 0;
		}
		
		
		// Draw a green cursor line 
		g2D.setColor(Color.GREEN);
		g2D.drawLine(currentX, 0, currentX, height);
		
	}
	
	// Assume that values is a list of y-values, where the index of each element
	// defines an x-value as follows: x-value = maxX / yvals.length * i. Assume minX = 0.
	double interpolateVal(double[] yvals, double maxX, double xDesired) {
		
		int index = (int) Math.floor(yvals.length / maxX * xDesired);
		double xPrevious = maxX / yvals.length * index;
		double xNext = maxX / yvals.length * (index + 1);
		
		double yPrevious = yvals[index];
		double yNext = yvals[index + 1];
		
		// Linearly interpolate
		return (yNext - yPrevious) * (xDesired - xPrevious) / (xNext - xPrevious) + yPrevious;
		
	}
	
	// Select a color based on a value between 0 and 1
	private Color getColor(double normalizedVal) {
		if (normalizedVal > 1.0) {
			normalizedVal = 1.0;
		} else if (normalizedVal < 0.0) {
			normalizedVal = 0.0;
		}
		
		//return new Color((float) normalizedVal, (float) normalizedVal, (float) normalizedVal);
		return gradient.computeGradient(normalizedVal);
		
	}
	
	public void render() {
		// Output!
		outputGraph();
	}
	
	private void outputGraph() {
		if (outputG2D != null) {
			outputG2D.drawImage(buffer, screenX, screenY, null);
		}
	}
	
	/**
	 * Reset the location and size parameters to move the spectrum.
	 */
	public void move(int x, int y, int width, int height) {
		screenX = x;
		screenY = y;
		if (width > 0) {
			this.width = width;
		} else {
			this.width = 1;
		}
		if (height > 0) {
			this.height = height;
		} else {
			this.height = 1;
		}
		currentX = 0;
		
		setSize(width, height);
	}
	
	public void setGraphics(Graphics2D g2D) {
		this.outputG2D = g2D;
	}
	
}
