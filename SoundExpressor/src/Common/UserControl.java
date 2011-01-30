package Common;

/**
 * Represents a user control that a FeatureDetector can request and use.
 * @author Steve Levine
 *
 */
public interface UserControl {
	
	public void setLocation(int x, int y, int width, int height);
	
	public void render();
	
	
}
