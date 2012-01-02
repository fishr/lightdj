package Arduino;

import java.io.UnsupportedEncodingException;

import Common.ColorOutput;

/**
 * A visualizer for LED's.
 * @author steve
 *
 */
public class LEDVisualizer {

	// Some constants
	private int MAX_PWM = 4095;
	private int MIN_PWM = 0;
	private String serialPort = "/dev/ttyACM0";
	private int portSpeed = 115200;
	
	// Fields
	private ArduinoController arduino;
	
	
	public LEDVisualizer()  {
		arduino = new ArduinoController(serialPort, portSpeed);
		try {
			arduino.connect();
		} catch (Exception e) {
			System.out.println("Error: Couldn't connect to Arduino!");
			//e.printStackTrace();
		}
	}
	
	public void visualize(ColorOutput colors) {
		// Just look at the first two colors
//		double[] channels = new double[4];
//		
//		channels[0] = colors.rgbLightsFront[0].getRed() / ((double) MAX_PWM);
//		channels[1] = colors.rgbLightsFront[1].getRed() / ((double) MAX_PWM);
//		channels[2] = colors.rgbLightsFront[1].getGreen() / ((double) MAX_PWM);
//		channels[3] = colors.rgbLightsFront[1].getBlue() / ((double) MAX_PWM);
		
		double[] channels = new double[3];
		
		channels[0] = colors.rgbLightsFront[0].getRed() / ((double) MAX_PWM);
		channels[1] = colors.rgbLightsFront[0].getGreen() / ((double) MAX_PWM);
		channels[2] = colors.rgbLightsFront[0].getBlue() / ((double) MAX_PWM);
		
		// Call the original visualizer
		visualize(channels);
		
	}
	
	public void visualize(double[] vals)  {
		StringBuilder sb = new StringBuilder();
		//sb.append("0,");
		
		for(int i = 0; i < vals.length; i++) {
			// Convert to an integer
			int intVal = (int) Math.round(MAX_PWM * vals[i]);
			if (intVal > MAX_PWM) {intVal = MAX_PWM;} else if (intVal < MIN_PWM) {intVal = MIN_PWM;}
			sb.append(String.valueOf(intVal));
			
			// Also add a comma if we're not at the end
			if (i == vals.length - 1) {
				sb.append('R');
			} else {
				sb.append(',');
			}
			
		}
		
		// Convert to ASCII bytes and send over
		byte[] data = new byte[sb.length()];
		for(int i = 0; i < data.length; i++) {
			data[i] = (byte) sb.charAt(i);
		}
	
		try {
			arduino.write(data);
		} catch (Exception e) {
			// Error
			System.out.println("Error! Couldn't write to Arduino...");
		}
	}
	
	public void lcdBacklightOn() {
		byte[] data = new byte[]{'['};
		try {
			arduino.write(data);
		} catch (Exception e) {
			// Error
			System.out.println("Error! Couldn't write to Arduino...");
		}
	}
	
	public void lcdBacklightOff() {
		byte[] data = new byte[]{']'};
		try {
			arduino.write(data);
		} catch (Exception e) {
			// Error
			System.out.println("Error! Couldn't write to Arduino...");
		}
	}
	
	public void lcdSetText(String topLine, String bottomLine) {
		String serialOut = "<" + topLine + ">" + bottomLine + "#";
		try {
			byte[] data = serialOut.getBytes();
			arduino.write(data);
		} catch (Exception e) {
			System.out.println("Error! Couldn't write to Arduino...");
		}
	}
	
	
}
