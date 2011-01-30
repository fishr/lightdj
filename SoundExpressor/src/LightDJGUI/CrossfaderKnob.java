package LightDJGUI;

import SoundEngine.VisualizationEngineParty;

/**
 * Implements the GUI for the Crossfader Knob
 * @author steve
 *
 */
public class CrossfaderKnob implements MouseAcceptorPanel {
	
	protected VisualizationEngineParty engine;
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	
	
	public CrossfaderKnob(VisualizationEngineParty eng) {
		this.engine = eng;
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
	
	public void setPosition(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void mouseDown(int x, int y) {
		engine.paintCrossfader(true);
		
		
	}

	@Override
	public void mouseDragged(int x, int y) {
		double alpha = 1.0 * x / width;
		if (alpha < 0.0) {
			alpha = 0.0;
		} else if (alpha > 1.0) {
			alpha = 1.0;
		} 
		
		engine.setMixerAlpha(alpha);
		engine.paintCrossfader(true);
		
	}

	@Override
	public void mouseUp(int x, int y) {
		engine.paintCrossfader(false);
		
	}
	
	
	
}
