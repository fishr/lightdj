package SignalGUI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


/**
 * Represents a scrolling display used for displaying different channel values
 * @author steve
 *
 */
public class ScrollingChannelMapper {
	private int screenX;
	private int screenY;
	private int width;
	private int height;
	
	private BufferedImage buffer;
	private Graphics2D outputG2D;
	
	private int currentX = 0;
	
	Color[] colors;
	
	public ScrollingChannelMapper(Color[] colors, int screenX, int screenY, int width, int height, Graphics2D g2D) {
		this.screenX = screenX;
		this.screenY = screenY;
		this.width = width;
		this.height = height;
		outputG2D = g2D;
		
		this.colors = colors;
		
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
	}
	
	
	// PRECONDITION: The number of channels is equal to the number of colors passed in earlier.
	
	public void updateWithNewChannelVals(double[] channelVals) {
		Graphics2D g2D = (Graphics2D) buffer.getGraphics();
		
				
		int size = height / colors.length;
		for(int i = 0; i < colors.length; i++) {
			// Set the current color
			g2D.setColor(scaleColor(colors[i], channelVals[i]));
			g2D.drawRect(currentX, size*i, 1, size);
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
	
	
	
}
