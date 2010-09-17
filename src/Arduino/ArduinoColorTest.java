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
			double red = color.getRed() / 255.0;
			double green = color.getGreen() / 255.0;
			double blue = color.getBlue() / 255.0;
			
			System.out.println("(" + red + ", " + green + ", " + blue + ")");
			
			rgbLED.visualize(new double[] {0, red, green, blue});
			
			hue = (hue + 0.001f) % 1.0f;
			
			// Wait a little bit
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				
			}
		}
		
	}
}
