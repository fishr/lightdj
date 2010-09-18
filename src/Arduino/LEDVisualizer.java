package Arduino;

import java.io.UnsupportedEncodingException;

/**
 * A visualizer for LED's.
 * @author steve
 *
 */
public class LEDVisualizer {

	// Some constants
	private int MAX_PWM = 255;
	private int MIN_PWM = 0;
	private String serialPort = "/dev/ttyUSB0";
	private int portSpeed = 115200;
	
	// Fields
	private ArduinoController arduino;
	
	
	public LEDVisualizer()  {
		arduino = new ArduinoController(serialPort, portSpeed);
		try {
			arduino.connect();
		} catch (Exception e) {
			System.out.println("Error: Couldn't connect to Arduino!");
			e.printStackTrace();
		}
	}
	
	public void visualize(double[] vals)  {
		StringBuilder sb = new StringBuilder();
		//sb.append("0,");
		
		for(int i = 0; i < vals.length; i++) {
			// Convert to an integer
			int intVal = (int) Math.round(255.0 * vals[i]);
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