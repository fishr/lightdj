package LightDJGUI;

import java.util.List;

public interface MouseAcceptorPanel {

	public int getX();
	public int getY();
	public int getWidth();
	public int getHeight();
	public boolean isVisible();
	
	public void mouseDown(int x, int y);
	public void mouseUp(int x, int y);
	public void mouseDragged(int x, int y);
	
	
}
