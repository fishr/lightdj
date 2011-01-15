package LightDJGUI;

import java.awt.Color;
import java.awt.Graphics2D;

import Common.ColorOutput;

/**
 * Displays the LED's for the party system
 * @author Steve Levine
 *
 */
public class ColorOutputDisplayerTwoLEDs implements ColorOutputDisplayer {

	@Override
	public void render(ColorOutput c, Graphics2D g2D, int x, int y, int width, int height) {
		
		int size = Math.min((int) (4.0/9.0*width), height);
		int yOffset = (height - size) / 2;
		int xOffset =(int) ((width - 9.0/4.0 * size) / 2.0);
		
		// Draw a black rectangle
		g2D.setColor(Color.BLACK);
		g2D.fillRect(x, y, width, height);
		
		// Draw color 1, and then color 2
		g2D.setColor(c.rgbLights[0]);
		g2D.drawRect(x + xOffset, y + yOffset, size, size);
		
		g2D.setColor(c.rgbLights[1]);
		g2D.drawRect(x + xOffset, y + yOffset, size, size);
		
	}
	

	
}
