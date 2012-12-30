package LightDJGUI;

import java.awt.Color;
import java.awt.Graphics2D;

import Common.UserControl;
import SoundEngine.VisualizationEngineParty;

/**
 * Implements the GUI for the Crossfader Knob
 * @author steve
 *
 */
public class CrossfaderKnob implements UserControl {
	
	protected static int CROSSFADER_INDENT;
	protected static int TICK_RADIUS;
	protected static int BOX_SIZE_X;
	protected static int BOX_SIZE_Y;
	
	protected VisualizationEngineParty engine;
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected boolean needsRender;
	protected boolean isHot;
	
	protected double alpha;
	
	public CrossfaderKnob(VisualizationEngineParty eng) {
		this.engine = eng;
		
		CROSSFADER_INDENT = scale(10);
		TICK_RADIUS = scale(10);
		BOX_SIZE_X = scale(10);
		BOX_SIZE_Y = scale(40);
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
	
	public void setLocation(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		needsRender = true;
	}
	
	@Override
	public void mouseDown(int x, int y) {
		isHot = true;
		needsRender = true;
		//engine.endAutomaticCrossfade();
		// engine.paintCrossfader(true);
		
		
	}

	@Override
	public void mouseDragged(int xm, int ym) {
		alpha = 1.0 * xm / width;
		if (alpha < 0.0) {
			alpha = 0.0;
		} else if (alpha > 1.0) {
			alpha = 1.0;
		} 
		
		//engine.endAutomaticCrossfade();
		engine.setMixerAlpha(alpha);
		// engine.paintCrossfader(true);
		isHot = true;
		needsRender = true;

	}

	@Override
	public void mouseUp(int x, int y) {
		//engine.paintCrossfader(false);
		isHot = false;
		needsRender = true;
		
	}

	@Override
	public boolean needsToRender() {
		return needsRender;
	}

	@Override
	public void render(Graphics2D g2D) {
		
		// Erase what was there before
		g2D.setColor(VisualizationEngineParty.PANEL_BACKGROUND_COLOR);
		g2D.fillRoundRect(x, y, width, height, VisualizationEngineParty.PANEL_BORDER_RADIUS, VisualizationEngineParty.PANEL_BORDER_RADIUS);
		g2D.setColor(VisualizationEngineParty.PANEL_BORDER_COLOR);
		g2D.setStroke(VisualizationEngineParty.REGULAR_STROKE);
		g2D.drawRoundRect(x, y, width, height, VisualizationEngineParty.PANEL_BORDER_RADIUS, VisualizationEngineParty.PANEL_BORDER_RADIUS);
		//g2D.fillRect(x, y, width, height);
		
		
		g2D.setColor(VisualizationEngineParty.PANEL_BORDER_COLOR);
		//g2D.drawRect(CROSSFADER_X, CROSSFADER_Y, CROSSFADER_WIDTH, CROSSFADER_HEIGHT);
		g2D.setStroke(VisualizationEngineParty.THICK_STROKE);
		int yCenter = y + height / 2;
		g2D.drawLine(x + CROSSFADER_INDENT, yCenter, x + width - CROSSFADER_INDENT, yCenter);
	
		// Draw hair lines
		for (int i = 0; i  <= 8; i++) {
			int xh = x + CROSSFADER_INDENT + i * (width - 2*CROSSFADER_INDENT) / 8;
			g2D.drawLine(xh, yCenter - TICK_RADIUS, xh, yCenter + TICK_RADIUS);
		}
	
		// Now draw the main box
		g2D.setStroke(VisualizationEngineParty.REGULAR_STROKE);
		int xb = (int) (x + CROSSFADER_INDENT + alpha * (width - 2*CROSSFADER_INDENT));
		if (isHot) {
			g2D.setColor(VisualizationEngineParty.HOT_COLOR);
		} else {
			g2D.setColor(VisualizationEngineParty.TEXT_COLOR);
		}
		g2D.fillRect(xb - BOX_SIZE_X, yCenter - BOX_SIZE_Y, 2*BOX_SIZE_X-1, 2*BOX_SIZE_Y);
		if (isHot) {
			g2D.setColor(Color.WHITE);
		} else {
			g2D.setColor(Color.WHITE);
		}
		
		g2D.drawRect(xb - BOX_SIZE_X, yCenter - BOX_SIZE_Y, 2*BOX_SIZE_X-1, 2*BOX_SIZE_Y);

		// No longer need to render
		needsRender = false;
	}

	@Override
	public void setValue(float val) {
		alpha = val;
		engine.setMixerAlpha(alpha);
		needsRender = true;
	}
	
	public double getAlpha() {
		return alpha;
	}
	
	public void setNeedsToRender() {
		needsRender = true;
	}
	
	public void setIsHot(boolean hot) {
		isHot = hot;
	}
	
	protected int scale(int val) {
		return VisualizationEngineParty.scale(val);
	}
	
}
