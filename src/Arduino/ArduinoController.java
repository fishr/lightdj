package Arduino;

import java.io.IOException;
import java.io.OutputStream;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

// A class that communicates over serial (in our case, USB)
// to an Arduino running appropriate code.
public class ArduinoController {

	private String serialPortName;
	private int speed;
	private boolean isConnected;
	private OutputStream outStream;
	
	public ArduinoController(String serialPortName, int speed) {
		this.serialPortName = serialPortName;
		this.speed = speed;
		isConnected = false;
		outStream = null;
	}
	
	// Attempts to connect the port, returning true on success.
	// Throws an error if we can't connect to it.
	public void connect() throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(serialPortName);
		if (portIdentifier.isCurrentlyOwned()) {
			throw new RuntimeException("Error: The serial port " + serialPortName + " is already owned!");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				
				outStream = serialPort.getOutputStream();
				isConnected = true;
			} else {
				throw new RuntimeException("Got a non-serial port, but only serial ports supported!");
			}
		}
		
		
		
		
	}
	
	// Writes data to the port
	public void write(byte[] data) throws IOException {
		outStream.write(data);
	}
	
	
	
}
