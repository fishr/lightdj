package SignalGUI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * A class that maps mathematical coordinates to screen coordinates
 * @author steve
 *
 */
public class GraphMapper {

	private int screenX;
	private int screenY;
	private int width;
	private int height;
	
	private BufferedImage buffer;
	private Graphics2D outputG2D;
	
	
	public GraphMapper(int screenX, int screenY, int width, int height, Graphics2D g2D) {
		this.screenX = screenX;
		this.screenY = screenY;
		this.width = width;
		this.height = height;
		outputG2D = g2D;
		
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
	}
	
	public int getScreenX() {return screenX;}
	public int getScreenY() {return screenY;}
	public int getWidth() {return width;}
	public int getHeight() {return height;}
	
	
	/**
	 * 
	 * @param y_vals
	 * @param maxY
	 */
	public void drawPositiveGraph(double[] y_vals, double maxY) {
		
		Graphics2D g2D = (Graphics2D) buffer.getGraphics();
		
		//g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setColor(Color.BLACK);
		g2D.clearRect(0, 0, width, height);
		
		
		g2D.setColor(Color.WHITE);
		g2D.setBackground(Color.BLACK);
		
		// Draw the X and Y axes4096
		g2D.drawLine(transformX(1), transformY(1), transformX(width-1), transformY(1));
		g2D.drawLine(transformX(1), transformY(1), transformX(1), transformY(height-1));
		

		g2D.setColor(Color.GREEN);
		
		double scaleX = (double) width / (y_vals.length - 1);
		double scaleY = (double) height / maxY;
		
		// Draw the values!
		double lastX = 0;
		double lastY = scaleY * y_vals[0];
		for(int i = 1; i < y_vals.length; i++) {
			double x = scaleX * i;
			double y = scaleY * y_vals[i];
			
			g2D.drawLine(transformX(x), transformY(y), transformX(lastX), transformY(lastY));
			lastX = x;
			lastY = y;
		
		}
		
		outputGraph();
	}
	
	public void drawPositiveLogHalfX(double[] x_vals, double[] y_vals, double minX, double maxX, double maxY) {
		Graphics2D g2D = (Graphics2D) buffer.getGraphics();
		
		//g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setColor(Color.BLACK);
		g2D.clearRect(0, 0, width, height);
		
		
		g2D.setColor(Color.WHITE);
		g2D.setBackground(Color.BLACK);
		
		// Draw the X and Y axes
		g2D.drawLine(transformX(1), transformY(1), transformX(width-1), transformY(1));
		g2D.drawLine(transformX(1), transformY(1), transformX(1), transformY(height-1));
		

		g2D.setColor(Color.GREEN);
		
		double scaleX = (double) width / (Math.log10(maxX) - Math.log10(minX));
		double scaleY = (double) height / maxY;
	
		double logMinX = Math.log10(minX);
		
		// Draw the values!
		double lastX = scaleX * (Math.log10(x_vals[0]) - logMinX);
		double lastY = scaleY * y_vals[0];
		for(int i = 1; i < x_vals.length / 2; i++) {
			double x = scaleX * (Math.log10(x_vals[i]) - logMinX);
			double y = scaleY * y_vals[i];
			
			g2D.drawLine(transformX(x), transformY(y), transformX(lastX), transformY(lastY));
			lastX = x;
			lastY = y;
		
		}
		
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
	public int transformX(double x) {
		return (int) (x);
	}
	
	public int transformY(double y) {
		return (int) (height - y);
	}
	
	
	
	
}
