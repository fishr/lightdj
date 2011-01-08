package SignalGUI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;


/**
 * Represents a single, colored light of a specific color.
 * @author Steve Levine
 *
 */
public class TextLight {
	private int screenX;
	private int screenY;
	private int width;
	private int height;
	
	private BufferedImage buffer;
	private Graphics2D outputG2D;

	int lightSize;
	String currentText = "";
	
	static final int spacing = 10;
	
	public TextLight(int screenX, int screenY, int width, int height, Graphics2D g2D) {
		this.screenX = screenX;
		this.screenY = screenY;
		this.width = width;
		this.height = height;
		outputG2D = g2D;
		
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}
	
	
	// PRECONDITION: The number of channels is equal to the number of colors passed in earlier.
	
	public void update(String text) {
		Graphics2D g2D = (Graphics2D) buffer.getGraphics();
				
		// Draw a light of the appropriate color!
		g2D.setColor(Color.BLACK);
		g2D.fillRect(0, 0, width, height);
		g2D.setColor(Color.WHITE);
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
		        RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setFont(new Font("Purisa", 0, 48));
		g2D.drawString(text, 10, 40);

		// Output!
		outputGraph();
		
	}
	
	
	
	
	
	private void outputGraph() {
		outputG2D.drawImage(buffer, screenX, screenY, null);
	}
	
	
}
