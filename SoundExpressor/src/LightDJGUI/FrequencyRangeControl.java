package LightDJGUI;

import java.awt.Graphics2D;

import Common.UserControl;

/**
 * Represents a user control that allows the user to select a frequency range.
 * @author Steve Levine
 *
 */
public class FrequencyRangeControl implements UserControl {

	protected double minFreq;
	protected double maxFreq;
	
	public FrequencyRangeControl(double initialMinFreq, double initialMaxFreq) {
		minFreq = initialMinFreq;
		maxFreq = initialMaxFreq;
	}

	@Override
	public void render(Graphics2D g2D) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLocation(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setValue(float val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean needsToRender() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void mouseDown(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseUp(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	
}
