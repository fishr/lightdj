package SignalGUI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


/**
 * Represents a scrolling display used for displaying different channel values
 * @author Steve Levine
 *
 */
public class ScrollingChannel {
	private int screenX;
	private int screenY;
	private int width;
	private int height;
	
	private BufferedImage buffer;
	private Graphics2D outputG2D;
	
	
	private int currentX = 0;
	
	public ScrollingChannel(int screenX, int screenY, int width, int height, Graphics2D g2D) {
		this.screenX = screenX;
		this.screenY = screenY;
		this.width = width;
		this.height = height;
		outputG2D = g2D;
		
		
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
	}
	
	
	// PRECONDITION: The number of channels is equal to the number of colors passed in earlier.
	public void updateWithNewChannelColors(Color[] channelColors) {
		Graphics2D g2D = (Graphics2D) buffer.getGraphics();
		
				
		int size = height / channelColors.length;
		for(int i = 0; i < channelColors.length; i++) {
			// Set the current color
			g2D.setColor(channelColors[i]);
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
	
	private void outputGraph() {
		outputG2D.drawImage(buffer, screenX, screenY, null);
	}
	
	
}
