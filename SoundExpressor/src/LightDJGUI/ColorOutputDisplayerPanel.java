package LightDJGUI;

import java.awt.Color;
import java.awt.Graphics2D;

import Common.ColorOutput;

/**
 * Displays the LED's for the party system
 * @author Steve Levine
 *
 */
public class ColorOutputDisplayerPanel implements ColorOutputDisplayer {

	@Override
	public void render(ColorOutput c, Graphics2D g2D, int x, int y, int width, int height) {
		
		int size = Math.min((int) (0.25 * width), height);
		int yOffset = (height - size) / 2;
		int xOffset =(int) ((width - 4.0*size)/2.0);
		
		// Draw a black rectangle
		//g2D.setColor(Color.BLACK);
		//g2D.fillRect(x, y, width, height);
		
		// Draw color 1, and then color 2
		g2D.setColor(c.rgbLightsFront[0]);
		g2D.fillRect(x + xOffset, y + yOffset, size, size);
		
		g2D.setColor(c.rgbLightsFront[1]);
		g2D.fillRect(x + xOffset + size, y + yOffset, size, size);
		
		g2D.setColor(c.rgbLightsFront[2]);
		g2D.fillRect(x + xOffset + 2*size, y + yOffset, size, size);
		
		g2D.setColor(c.rgbLightsFront[3]);
		g2D.fillRect(x + xOffset + 3*size, y + yOffset, size, size);
		
		
	}
	

	
}
