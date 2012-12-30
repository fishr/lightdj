package LightDJGUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import SoundEngine.VisualizationEngineParty;

import Common.UserControl;

/**
 * Represents a generic, circular knob. Adjusts a value between 0.0f and 1.0f.
 * Can also receive float values to change!
 * @author steve
 *
 */
public class GenericKnob implements UserControl {

	// Positioning information
	protected boolean renderNeeded;
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected int diameter;
	
	// State information
	protected float value;
	protected String label;
	
	// Interaction stuff
	protected int xStartDrag;
	protected int yStartDrag;
	protected float startVal;
	protected boolean isDragging;
	
	protected static int GAP;
	protected static int THIN_GAP;
	protected static int MOUSE_NORMALIZER;
	
	public GenericKnob(float defaultVal, int diameter, String label) {
		super();
		this.renderNeeded = false;
		this.diameter = diameter;
		this.isDragging = false;
		
		// Set the default value
		this.value = defaultVal;
		this.label = label;
		
		
	}
	
	// Return whether this component is requesting to be re-rendered. If true,
	// the render() method will eventually be called at the right time.
	@Override
	public boolean needsToRender() {
		return renderNeeded;
	}

	// Render on a surface
	@Override
	public void render(Graphics2D g2D) {
		// Render
		GAP = VisualizationEngineParty.scale(6);
		THIN_GAP = VisualizationEngineParty.scale(2);
		MOUSE_NORMALIZER = VisualizationEngineParty.scale(200);
		
		// Clear what was there
		g2D.setColor(VisualizationEngineParty.PANEL_BACKGROUND_COLOR);
		g2D.fillRect(x, y, width, height);
		
		// Draw a partially filled arc
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//g2D.setColor(Color.DARK_GRAY);
		//g2D.fillOval(x, y, diameter, diameter);
		
		g2D.setColor(VisualizationEngineParty.TEXT_COLOR);
		g2D.fillArc(x + GAP, y + GAP, diameter - (2*GAP-1), diameter - (2*GAP-1), 270, (int)(-this.value * 350));
		
		// Draw a black outline
		g2D.setColor(Color.DARK_GRAY);
		g2D.setStroke(new BasicStroke(2.0f * VisualizationEngineParty.DPI_MULT));
		g2D.drawOval(x + THIN_GAP, y + THIN_GAP, diameter - 2*THIN_GAP, diameter - 2*THIN_GAP);
		
		// Write the text next to it
		g2D.setFont(VisualizationEngineParty.PANEL_FONT_SMALL);
		g2D.setColor(VisualizationEngineParty.TEXT_COLOR);
		g2D.drawString(label, x + diameter + VisualizationEngineParty.scale(10), y + VisualizationEngineParty.scale(30));
		
		// No longer requesting to be rendered
		renderNeeded = false;
	}

	// Store location
	@Override
	public void setLocation(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public void setValue(float val) {

		// Clip the value to between 0 and 1, inclusive.
		if (val < 0.0) {
			this.value = 0.0f;
		} else if (val > 1.0) {
			this.value = 1.0f;
		} else {
			this.value = val;
		}

		// Request to be rendered
		renderNeeded = true;
	}

	// Return the value of this knob (from 0.0 to 1.0)
	public float getValue() {
		return this.value;
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
	public void mouseDown(int x, int y) {
		if (x <= diameter && y <= diameter) {
			isDragging = true;
			xStartDrag = x;
			yStartDrag = y;
			startVal = value;
		}
	}

	@Override
	public void mouseDragged(int x, int y) {
		if (isDragging) {
			float diff = y - yStartDrag;
			setValue(startVal - (1.0f / MOUSE_NORMALIZER) * diff);
		}
	}

	@Override
	public void mouseUp(int x, int y) {
		isDragging = false;
		
	}
	
}
