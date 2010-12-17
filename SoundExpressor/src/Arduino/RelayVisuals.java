package Arduino;

import java.io.UnsupportedEncodingException;

/**
 * A visualizer for LED's.
 * @author steve
 *
 */
public class RelayVisuals {

	// Some constants
	private String serialPort = "/dev/ttyUSB0";
	private int portSpeed = 115200;
	
	// Fields
	private ArduinoController arduino;
	
	
	public RelayVisuals()  {
		arduino = new ArduinoController(serialPort, portSpeed);
		try {
			arduino.connect();
		} catch (Exception e) {
			System.out.println("Error: Couldn't connect to Arduino!");
			//e.printStackTrace();
		}
	}
	
	public void visualize(double[] vals)  {
		StringBuilder sb = new StringBuilder();
		//sb.append("0,");
		
		byte outputByte = 0;
		
		for(int i = 0; i < vals.length; i++) {
			if (vals[i] > 0.5) {
				outputByte |= (1 << i);
			}
			
		}
		
	
		try {
			arduino.write(new byte[]{outputByte});
		} catch (Exception e) {
			// Error
			System.out.println("Error! Couldn't write to Arduino...");
		}
	}
	
	
}
