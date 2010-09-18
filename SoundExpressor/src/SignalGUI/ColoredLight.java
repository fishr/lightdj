package SignalGUI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


/**
 * Represents a single, colored light of a specific color.
 * @author Steve
 *
 */
public class ColoredLight {
	private int screenX;
	private int screenY;
	
	private BufferedImage buffer;
	private Graphics2D outputG2D;

	int lightSize;
	Color color;
	Color currentLightColor;
	
	static final int spacing = 10;
	
	public ColoredLight(Color color, int lightSize, int screenX, int screenY, int width, int height, Graphics2D g2D) {
		this.screenX = screenX;
		this.screenY = screenY;
		outputG2D = g2D;
		
		this.color = color;
		this.lightSize = lightSize;
		
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
	}
	
	
	// PRECONDITION: The number of channels is equal to the number of colors passed in earlier.
	
	public void update(double channelVal) {
		Graphics2D g2D = (Graphics2D) buffer.getGraphics();
		
				
		// Draw a light of the appropriate color!
		currentLightColor = scaleColor(color, channelVal);
		outputG2D.setColor(currentLightColor);
		outputG2D.fillRect(screenX, screenY, lightSize, lightSize);
		
		
		// Output!
		//outputGraph();
		
	}
	
	
	// Select a color based on a value between 0 and 1
	private Color scaleColor(Color c, double normalizedVal) {
		if (normalizedVal > 1.0) {
			normalizedVal = 1.0;
		} else if (normalizedVal < 0.0) {
			normalizedVal = 0.0;
		}
		
		return new Color((int) Math.round(normalizedVal * c.getRed()), (int) Math.round(normalizedVal * c.getGreen()), (int) Math.round(normalizedVal * c.getBlue()));		
	}
	
	
	
	private void outputGraph() {
		outputG2D.drawImage(buffer, screenX, screenY, null);
	}
	
	public Color getCurrentColor() {
		return currentLightColor;
	}
	
}
