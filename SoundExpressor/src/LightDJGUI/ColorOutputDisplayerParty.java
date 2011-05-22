package LightDJGUI;

import java.awt.Color;
import java.awt.Graphics2D;

import SoundEngine.VisualizationEngineParty;
import SoundEngine.VisualizationEngineParty.LightDJPostProcessing;
import Visualizors.RGBGradientLinear;

import Common.ColorOutput;

/**
 * Displays the LED's for the party system
 * @author Steve Levine
 *
 */
public class ColorOutputDisplayerParty implements ColorOutputDisplayer {

	// Constants
	protected static int LIGHTS_PER_RGB_PANEL = 4;
	protected static int LIGHTS_PER_UVWHITE_BOARD = 2;
	
	protected static int NUM_FRONT_RGB_PANELS = 6;
	protected static double SPACING_FRONT_PANELS = 1;
	
	
	protected static int NUM_REAR_RGB_PANELS = 6;
	protected static double SPACING_REAR_PANELS = 1;
	
	protected static int NUM_UVWHITE_PANELS = 7;
	protected static int SPACING_UVWHITE_PANELS = 2;
	
	protected static int NUM_STROBE_LIGHTS = 7;
	protected static int NUM_UV_LIGHTS = 7;
	protected static int NUM_RGB_LIGHTS = ColorOutput.NUM_RGB_LIGHTS;
	
	protected static int REAR_START_RGB_INDEX = 32;
	
	
	VisualizationEngineParty engine;
	protected int strobeState = 0;
	
	public ColorOutputDisplayerParty(VisualizationEngineParty engine) {
		this.engine = engine;
	}
	
	
	@Override
	public void render(ColorOutput c, Graphics2D g2D, int x, int y, int width, int height) {
		// Do any preprocessing (i.e., apply any overarching compressions to the computed colors)
		LightDJPostProcessing postProcessing = engine.getPostProcessingMethod();
		if (postProcessing == LightDJPostProcessing.POST_PROCESSING_WHITE_STROBE) {
			if (strobeState == 0) {
				
				for(int i = 0; i < NUM_RGB_LIGHTS; i++) {
					c.rgbLights[i] = Color.WHITE;
				}
				
				for(int i = 0; i < NUM_STROBE_LIGHTS; i++) {
					c.whiteLights[i] = 1.0;
				}
				
				for(int i = 0; i < NUM_UV_LIGHTS; i++) {
					c.uvLights[i] = 0.0;
				}
				
				
			} else {

				for(int i = 0; i < NUM_RGB_LIGHTS; i++) {
					c.rgbLights[i] = Color.BLACK;
				}
				
				for(int i = 0; i < NUM_STROBE_LIGHTS; i++) {
					c.whiteLights[i] = 0.0;
				}
				
				for(int i = 0; i < NUM_UV_LIGHTS; i++) {
					c.uvLights[i] = 0.0;
				}
			}
			strobeState = (strobeState + 1) % 2;
			
		} else if (postProcessing == LightDJPostProcessing.POST_PROCESSING_UV_STROBE) {
			if (strobeState == 0) {
				
				for(int i = 0; i < NUM_RGB_LIGHTS; i++) {
					c.rgbLights[i] = Color.BLACK;
				}
				
				for(int i = 0; i < NUM_STROBE_LIGHTS; i++) {
					c.whiteLights[i] = 0.0;
				}
				
				for(int i = 0; i < NUM_UV_LIGHTS; i++) {
					c.uvLights[i] = 1.0;
				}
				
				
			} else {

				for(int i = 0; i < NUM_RGB_LIGHTS; i++) {
					c.rgbLights[i] = Color.BLACK;
				}
				
				for(int i = 0; i < NUM_STROBE_LIGHTS; i++) {
					c.whiteLights[i] = 0.0;
				}
				
				for(int i = 0; i < NUM_UV_LIGHTS; i++) {
					c.uvLights[i] = 0.0;
				}
			}
			strobeState = (strobeState + 1) % 2;
			
		}
		
		// Render it
		drawParty(c, g2D, x, y, width, height);
		
		
		
	}
	
	public void drawParty(ColorOutput c, Graphics2D g2D, int x, int y, int width, int height) {
		double WIDTH_FRONT = NUM_FRONT_RGB_PANELS * LIGHTS_PER_RGB_PANEL + (NUM_FRONT_RGB_PANELS - 1) * SPACING_FRONT_PANELS;
		double WIDTH_HEIGHT = 5.0;
		
		int size = Math.min((int) (width / WIDTH_FRONT), (int) (height / WIDTH_HEIGHT));
		int yOffset = (int) ((height - WIDTH_HEIGHT * size) / 2.0);
		int xOffset =(int) ((width - WIDTH_FRONT * size) /2.0);
		
		// Draw a black rectangle
		//g2D.setColor(Color.BLACK);
		//g2D.fillRect(x, y, width, height);
		
		// Draw color 1, and then color 2
		
		// Draw the top RGB panels
		for(int panelIndex = 0; panelIndex < NUM_FRONT_RGB_PANELS; panelIndex++) {
			int currentX = x + xOffset + (int) (panelIndex * (LIGHTS_PER_RGB_PANEL + SPACING_FRONT_PANELS)*size);
			int currentY = y + yOffset;
			
			// Draw this panel
			for(int light = 0; light < LIGHTS_PER_RGB_PANEL; light++) {
				g2D.setColor(c.rgbLights[LIGHTS_PER_RGB_PANEL*panelIndex + light]);
				g2D.fillRect(currentX + light*size, currentY, size, size);
			}
		}
			
		// Draw the bottom RGB
		for(int panelIndex = 0; panelIndex < NUM_REAR_RGB_PANELS; panelIndex++) {
			int currentX = x + xOffset + (int) (panelIndex * (LIGHTS_PER_RGB_PANEL + SPACING_REAR_PANELS)*size);
			int currentY = y + yOffset + 4*size ;
			
			// Draw this panel
			for(int light = 0; light < LIGHTS_PER_RGB_PANEL; light++) {
				g2D.setColor(c.rgbLights[LIGHTS_PER_RGB_PANEL*panelIndex + light + REAR_START_RGB_INDEX]);
				g2D.fillRect(currentX + light*size, currentY, size, size);
			}
		}
		
		// Draw the UV white boards
		for(int panelIndex = 0; panelIndex < NUM_UVWHITE_PANELS; panelIndex++) {
			int currentX = x + xOffset + (int) (panelIndex * (LIGHTS_PER_UVWHITE_BOARD + SPACING_UVWHITE_PANELS)*size);
			int currentY = y + yOffset + 2*size ;
			
			// Draw this panel
			g2D.setColor(RGBGradientLinear.linearGradient(Color.BLACK, Color.WHITE, c.whiteLights[panelIndex]));
			g2D.fillRect(currentX, currentY, size, size);
			g2D.setColor(RGBGradientLinear.linearGradient(Color.BLACK, Color.MAGENTA, c.uvLights[panelIndex]));
			g2D.fillRect(currentX + size, currentY, size, size);
			
			
		}
		
		
	}
	

	
}