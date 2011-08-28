package SignalGUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;


/**
 * Plots data as a function of time, and scrolls. Meant for debugging purposes, and to be
 * helpful with designing new algorithms!
 * @author Steve Levine
 *
 */
public class DistributionPlotter {

	private int screenX;
	private int screenY;
	private int width;
	private int height;
	
	private BufferedImage buffer;
	private Graphics2D outputG2D;
	
	private double binSize;
	private int numPoints;
	private ArrayList<Double> bins;
	private double binsTotal;
	private Color color;
	private String label;
	
	private double sum;
	private double spreadSum;
	private double maxVal = 0;
	private long N;
	
	private boolean AUTO_Y_SCALE = true;

	
	public DistributionPlotter(Color color, String label, int screenX, int screenY, int width, int height, double binSize, double initialMax, Graphics2D g2D) {
		this.screenX = screenX;
		this.screenY = screenY;
		this.width = width;
		this.height = height;
		this.outputG2D = g2D;
		this.label = label;
		
		this.binSize = binSize;
		numPoints = 0;
		
		// Initialize the bins
		int size = (int) (initialMax/binSize);
		bins = new ArrayList<Double>(size);
		for(int i = 0; i < size; i++) {
			bins.add(0.0);
		}
		
		
		binsTotal = 0;
		this.color = color;
		
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
	}
	
	public int getScreenX() {return screenX;}
	public int getScreenY() {return screenY;}
	public int getWidth() {return width;}
	public int getHeight() {return height;}
	
	
	public void update(double y, double weight) {
		
		// Compute which bin to place this value in.
		int binIndex = (int) (y / binSize);
		
		// Increase the number of bins, if necessary.
//		if (binIndex > bins.size()) {
//			int oldSize = bins.size();
//			bins.ensureCapacity(binIndex);
//			for(int i = oldSize; i < binIndex; i++) {
//				bins.add(0);
//			}
//		}
		
		if (binIndex >= bins.size()) {
			
			//binIndex = bins.size() - 1;
			if (y > maxVal) {
				maxVal = y;
			}
			return;
		}
		
		
		// Increment that bin, and also keep track of the largest value (for normalizing purposes)
		double val = bins.get(binIndex) + weight;
		binsTotal += weight;
		if (y > maxVal) {
			maxVal = y;
		}
		sum += y;
		N++;
		spreadSum += Math.abs(y - sum / N);
		
		bins.set(binIndex, val);
		
		// Draw the graph!
		//drawGraph();
		
	}
	
	/**
	 * Reset this distribution (i.e., between songs)
	 */
	public void reset() {
		maxVal = 0;
		sum = 0;
		N = 0;
		spreadSum = 0;
		binsTotal = 0;
		for(int i = 0; i < bins.size(); i++) {
			bins.set(i, 0.0);
		}
	}
	
	public void render() {
		drawGraph();
	}
	
	private void drawGraph() {
		
		Graphics2D g2D = (Graphics2D) buffer.getGraphics();
		
		g2D.setColor(Color.BLACK);
		g2D.clearRect(0, 0, width, height);
		
		
		g2D.setColor(Color.WHITE);
		g2D.setBackground(Color.BLACK);
		
		double scaleX = (double) width / (bins.size() - 1);
		double scaleY = (double) height / binsTotal;
	
		
		// Draw the X and Y axes
		g2D.drawLine(transformX(1), transformY(1), transformX(width-1), transformY(1));
		g2D.drawLine(transformX(1), transformY(1), transformX(1), transformY(height-1));

		g2D.setColor(color);
		double lastX = 1;
		double lastY = scaleY * bins.get(0);
		for(int p = 1; p < bins.size(); p++) {
			double x = scaleX * p;
			double y = scaleY * bins.get(p);
			g2D.drawLine(transformX(lastX), transformY(lastY), transformX(x), transformY(y));
			
			lastX = x;
			lastY = y;
		}
		
		
//		g2D.setColor(Color.WHITE);
//		g2D.drawString(label, 10, 10);
//		g2D.drawString("Ave: " + getAverageVal(), 20, 30);
//		g2D.drawString("Spr: " + getAverageSpread(), 20, 50);
//		g2D.drawString("Max: " + getMax(), 20, 70);
//		g2D.drawString("Peak: " + getPeak(), 20, 90);
		
		// Output this buffered graph image!
		outputGraph();
	}
	
	private void outputGraph() {
		outputG2D.drawImage(buffer, screenX, screenY, null);
	}
	
	
	/**
	 * Transform from graph to screen coordinates
	 * @param x
	 * @return
	 */
	private int transformX(double x) {
		return (int) (x);
	}
	
	private int transformY(double y) {
		return (int) (height - y);
	}
	
	public double getAverageVal() {
		if (N != 0) {
			return (sum / N);
		} else {
			return 0;
		}
	}
	
	public double getAverageSpread() {
		if (N != 0) {
			return (spreadSum / N);
		} else {
			return 0;
		}
	}
	
	public long getCount() {
		return N;
	}
	
	public double getMax() {
		return maxVal;
	}
	
	public double getPeak() {
		double peak = 0.0;
		for(int i = 0; i < bins.size(); i++) {
			if (bins.get(i) > peak) {
				peak = bins.get(i);
			}
		}
		
		if (binsTotal != 0) {
			return peak / binsTotal;
		} else {
			return 0;
		}
	}
	
	public double getNormalizedBin(int i) {
		if (binsTotal != 0) {
			return bins.get(i) / binsTotal;
		} else {
			return 0;
		}
	}
	
	public void setGraphics(Graphics2D g2D) {
		this.outputG2D = g2D;
	}
	
	
}

