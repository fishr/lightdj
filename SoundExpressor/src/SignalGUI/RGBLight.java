package SignalGUI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


/**
 * Represents a single, colored light of a specific color.
 * @author Steve Levine
 *
 */
public class RGBLight {
	private int screenX;
	private int screenY;
	
	private BufferedImage buffer;
	private Graphics2D outputG2D;

	int lightSize;
	Color currentLightColor;
	
	static final int spacing = 10;
	
	public RGBLight(int lightSize, int screenX, int screenY, int width, int height, Graphics2D g2D) {
		this.screenX = screenX;
		this.screenY = screenY;
		outputG2D = g2D;
		
		this.lightSize = lightSize;
		
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
	}
	
	
	// PRECONDITION: The number of channels is equal to the number of colors passed in earlier.
	
	public void update(Color color) {
		Graphics2D g2D = (Graphics2D) buffer.getGraphics();
		
				
		// Draw a light of the appropriate color!
		currentLightColor = color;
		outputG2D.setColor(currentLightColor);
		outputG2D.fillRect(screenX, screenY, lightSize, lightSize);
		
		
		// Output!
		//outputGraph();
		
	}
	
	
	
	
	
	private void outputGraph() {
		outputG2D.drawImage(buffer, screenX, screenY, null);
	}
	
	public Color getCurrentColor() {
		return currentLightColor;
	}
	
}
