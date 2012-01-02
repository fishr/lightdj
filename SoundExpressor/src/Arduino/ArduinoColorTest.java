package Arduino;

import java.awt.Color;

/**
 * Runs through some colors for an RGB LED using the Arduino
 * @author steve
 *
 */
public class ArduinoColorTest {

	public static void main(String[] args) {
		
		// Continuously rotate through colors
		Color color;
		LEDVisualizer rgbLED = new LEDVisualizer();
		float hue = 0.0f;
		float saturation = 1.0f;
		float brightness = 1.0f;
		
		
		while (true) {
			color = Color.getHSBColor(hue, saturation, brightness);
			float colors[] = new float[3];
			color.getRGBColorComponents(colors);
			double red = colors[0];
			double green = colors[1];
			double blue = colors[2];
			
			//System.out.println("(" + red + ", " + green + ", " + blue + ")");
			
			rgbLED.visualize(new double[] {0, red, green, blue});
			
			hue = (hue + 0.00005f) % 1.0f;
			
			// Wait a little bit
			try {
				//Thread.sleep(1);
			} catch (Exception e) {
				
			}
		}
		
	}
}
