package Common;

import java.awt.Graphics2D;

import LightDJGUI.MouseAcceptorPanel;

/**
 * Represents a user control that a FeatureDetector can request and use.
 * @author Steve Levine
 *
 */
public interface UserControl extends MouseAcceptorPanel {
	
	public void setLocation(int x, int y, int width, int height);
	
	public void render(Graphics2D g2D);
	
	public boolean needsToRender();
	
	public void setValue(float val);
	
	
}
