package LightDJGUI;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import SoundEngine.VisualizationEngineParty;

import Common.UserControl;

/**
 * This class, although implementing UserControl, is actually non-interactive.
 * It displays an indicator light that can either look like it's glowing, or be off.
 * @author steve
 *
 */
public class IndicatorLight implements UserControl {

	protected boolean onoff;
	protected boolean renderNeeded;
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	
	protected static int INDICATOR_LIGHT_WIDTH;
	protected static int INDICATOR_LIGHT_HEIGHT;
	protected static BufferedImage onImage = null;
	protected static BufferedImage offImage = null;
	
	public IndicatorLight() {
		// Make sure static information is loaded!
		loadStaticData();
		
		// Set state variables
		onoff = false;
		renderNeeded = true;
	}
	
	protected static void loadStaticData() {
		// Load images / static information
		if (onImage == null) {
			try {
				// Select appropriate size based on user's DPI settings
				INDICATOR_LIGHT_WIDTH = scale(32);
				INDICATOR_LIGHT_HEIGHT = scale(20);
						
				// Load the large raw images
				BufferedImage onImageRaw = ImageIO.read(new File("Images/led_bright_large.png"));
				BufferedImage offImageRaw = ImageIO.read(new File("Images/led_dark_large.png"));
						
				// Scale them down to the appropriate size (controlled by user's DPI settings)
				onImage = VisualizationEngineParty.scaleImage(onImageRaw, INDICATOR_LIGHT_WIDTH, INDICATOR_LIGHT_HEIGHT);
				offImage = VisualizationEngineParty.scaleImage(offImageRaw, INDICATOR_LIGHT_WIDTH, INDICATOR_LIGHT_HEIGHT);
						
			} catch (IOException e) {
				System.out.println("Warning: Could not load indicator light images!");
				e.printStackTrace();
				return;
			}
		}
	}
	
	protected static int scale(int val) {
		return VisualizationEngineParty.scale(val);
	}
	
	@Override
	public boolean needsToRender() {
		return renderNeeded;
	}

	@Override
	public void render(Graphics2D g2D) {

		if (onoff) {
			g2D.drawImage(onImage, x, y, null);
		} else {
			g2D.drawImage(offImage, x, y, null);
		}
		
		renderNeeded = false;
	}

	@Override
	public void setLocation(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;	
	}

	@Override
	public void setValue(float val) {
		boolean newVal;
		if (val == 0.0f) {
			newVal = false;
		} else {
			newVal = true;
		}
		
		if (newVal != onoff) {
			renderNeeded = true;
		}
		
		onoff = newVal;
	}

	@Override
	public int getHeight() {return height;}

	@Override
	public int getWidth() {return width;}

	@Override
	public int getX() {return x;}

	@Override
	public int getY() {return y;}

	@Override
	public boolean isVisible() {return true;}

	@Override
	public void mouseDown(int x, int y) {}

	@Override
	public void mouseDragged(int x, int y) {}

	@Override
	public void mouseUp(int x, int y) {}

}
