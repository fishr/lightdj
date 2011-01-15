package LightDJGUI;

import java.awt.Graphics2D;

import Common.ColorOutput;

/**
 * This class is responsible for rendering ColorOutput classes in a reasonable manner.
 * @author Steve Levine
 *
 */
public interface ColorOutputDisplayer {
	public void render(ColorOutput c, Graphics2D g2D, int x, int y, int width, int height);
}
