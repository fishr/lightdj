package SignalGUI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Represents a scrolling spectrum, showing the past spectrums as a function of time!
 * @author Steve
 *
 */
public class ScrollingSpectrum {
	private int screenX;
	private int screenY;
	private int width;
	private int height;
	
	private BufferedImage buffer;
	private Graphics2D outputG2D;
	
	private int currentX = 0;
	
	public ScrollingSpectrum(int screenX, int screenY, int width, int height, Graphics2D g2D) {
		this.screenX = screenX;
		this.screenY = screenY;
		this.width = width;
		this.height = height;
		outputG2D = g2D;
		
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
	}
	
	
	public void updateWithNewSpectrum(double[] frequencies, double magnitudes[], double minFreq, double maxFreq, double max_val) {
		Graphics2D g2D = (Graphics2D) buffer.getGraphics();
		
		// Draw this spectrum, linearly interpolating between frequencies
		double largestFreq = frequencies[frequencies.length - 1];
		
		double logMinFreq = Math.log10(minFreq);
		double logMaxFreq = Math.log10(maxFreq);
		double logFreqScale = (maxFreq - minFreq) / (logMaxFreq - logMinFreq);
		
		int freqIndex = 0;
		double lastFreq = frequencies[freqIndex];
		double lastMag = magnitudes[freqIndex];
		for(int yPixelIndex = 0; yPixelIndex < height; yPixelIndex++) {
			
			// Compute the desired frequency corresponding to this pixel
			//double frequency = (maxFreq - minFreq) / height * yPixelIndex + minFreq;
			double frequency = Math.pow(10.0,  ((logMaxFreq - logMinFreq) / height * yPixelIndex + logMinFreq));
			
			// Interpolate to compute the approximate magnitude at this frequency
			double magnitude = interpolateVal(magnitudes, largestFreq, frequency);
			
			
			double freqVal = magnitude / max_val;
			g2D.setColor(getColor(freqVal));
			g2D.drawRect(currentX, height - yPixelIndex - 1, 1, 1);
		}
		
		// Increment x!
		if (currentX++ > width) {
			currentX = 0;
		}
		
		
		// Draw a green cursor line 
		g2D.setColor(Color.GREEN);
		g2D.drawLine(currentX, 0, currentX, height);
		
		// Output!
		outputGraph();
		
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
		
		return new Color((float) normalizedVal, (float) normalizedVal, (float) normalizedVal);
		
	}
	
	
	
	private void outputGraph() {
		outputG2D.drawImage(buffer, screenX, screenY, null);
	}
	
	
	
}