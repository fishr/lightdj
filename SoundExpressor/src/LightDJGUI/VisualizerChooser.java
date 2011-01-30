package LightDJGUI;

import java.util.ArrayList;

import SoundEngine.VisualizationEngineParty;
import Visualizors.Visualizer;

public class VisualizerChooser implements MouseAcceptorPanel {
	protected VisualizationEngineParty engine;
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected ArrayList<Visualizer> visualizers;
	
	
	public VisualizerChooser(VisualizationEngineParty eng, ArrayList<Visualizer> visualizerList) {
		this.engine = eng;
		this.visualizers = visualizerList;
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
	public boolean isVisible() {return engine.activeLayer;}
	
	public void setPosition(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void mouseDown(int x, int y) {
		// Choose a plugin
		
		engine.activeLayer = false;
		
		
	}

	@Override
	public void mouseDragged(int x, int y) {
		// Don't care
		
	}

	@Override
	public void mouseUp(int x, int y) {
		// Don't care
		
	}
	

}
