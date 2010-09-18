package SignalGUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * A class that maps mathematical coordinates to screen coordinates for use with graphing a spectrum.
 * @author steve
 *
 */
public class GraphDisplay {

	private int screenX;
	private int screenY;
	private int width;
	private int height;
	
	private BufferedImage buffer;
	private Graphics2D outputG2D;
	
	
	public GraphDisplay(int screenX, int screenY, int width, int height, Graphics2D g2D) {
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
	
	public void drawPositiveLogHalfX(double[] x_vals, double[] y_vals, double[] v_vals, double minX, double maxX, double maxY) {
		Graphics2D g2D = (Graphics2D) buffer.getGraphics();
		
		//g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setColor(Color.BLACK);
		g2D.clearRect(0, 0, width, height);
		
		
		g2D.setColor(Color.WHITE);
		g2D.setBackground(Color.BLACK);
		
		double scaleX = (double) width / (Math.log10(maxX) - Math.log10(minX));
		double scaleY = (double) height / maxY;
	
		double logMinX = Math.log10(minX);
		
		// Draw the X and Y axes
		g2D.drawLine(transformX(1), transformY(1), transformX(width-1), transformY(1));
		g2D.drawLine(transformX(1), transformY(1), transformX(1), transformY(height-1));
		
		// Draw a few reference lines
		Stroke oldStroke = g2D.getStroke();
		g2D.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0));
		Color normalLine = new Color(90, 90, 90);
		Color specialLine = new Color(120, 120, 120);
		//double[] f = {10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0, 1000.0, 10000.0};
		for(int power = 1; power <= 4; power++) {
			for(int i = 1; i < 10; i++) {
				
				if (i == 1) {
					g2D.setColor(specialLine);
				} else {
					g2D.setColor(normalLine);
				}
				
				double f = (double) i * Math.pow(10.0, power);
				
				double x = scaleX * (Math.log10(f) - logMinX);
				if (x > 0) {
					g2D.drawLine(transformX(x), transformY(1), transformX(x), transformY(height-1));
				}
			}
			
		}
		
		g2D.setStroke(oldStroke);
		
		

		
		// Draw the values!
		double lastX = scaleX * (Math.log10(x_vals[0]) - logMinX);
		double lastY = scaleY * y_vals[0];
		double lastV;
		if (v_vals != null) {
			lastV = scaleY * v_vals[0];
		} else {
			lastV = 0;
		}
		for(int i = 1; i < x_vals.length / 2; i++) {
			double x = scaleX * (Math.log10(x_vals[i]) - logMinX);
			double y = scaleY * y_vals[i];
			double v = 0;
			
			g2D.setColor(Color.GREEN);
			g2D.drawLine(transformX(x), transformY(y), transformX(lastX), transformY(lastY));
			
			if (v_vals != null) {
				g2D.setColor(Color.BLUE);
				v = scaleY * v_vals[i];
				g2D.drawLine(transformX(x), transformY(v), transformX(lastX), transformY(lastV));
			}
			
			
			lastX = x;
			lastY = y;
			lastV = v;
		
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
	private int transformX(double x) {
		return (int) (x);
	}
	
	private int transformY(double y) {
		return (int) (height - y);
	}
	
	
	
	
}
