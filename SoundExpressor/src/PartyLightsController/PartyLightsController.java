package PartyLightsController;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;

import Common.ColorOutput;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

/**
 * This class is responsible for outputting commands over serial to the Next Make Party Lighting System.
 * 
 * @author Steve Levine
 *
 */
public class PartyLightsController {

	// Serial port fields
	private String serialPortName;
	private int speed;
	private boolean isConnected;
	private OutputStream outStream;
	
	// Protocol information
	protected static final int ACTION_EMERGENCY_LIGHTING = 254;
	protected static final int ACTION_EVERYTHING_OFF = 253;
	protected static final int ACTION_ALL_FRONT_RGB = 252;
	protected static final int ACTION_ALL_REAR_RGB = 251;

	
	protected static final int NUM_RGB_BOARDS = 1;
	protected static final int NUM_UV_STROBE_BOARDS = 0;
	
	protected static final int LENGTH_RGB_PACKET = 2 + 12;
	
	
	public PartyLightsController() {
		// Set some defaults
		this.serialPortName = "/dev/ttyUSB0";
		this.speed = 38400;
		isConnected = false;
		outStream = null;
		
		// Attempt to connect
		try {
			connect();
		} catch (Exception e) {
			System.out.println("Error: Couldn't connect to Party Lighting System!");
			//e.printStackTrace();
		}
	}
	
	/**
	 * Attempts to connect to the serial port, returning true on success.
	 * If there's an error, it is thrown.
	 */
	private void connect() throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(serialPortName);
		if (portIdentifier.isCurrentlyOwned()) {
			throw new RuntimeException("Error: The serial port " + serialPortName + " is already owned!");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
				
				
				//serialPort.setLowLatency();
				
				
				outStream = serialPort.getOutputStream();
				isConnected = true;
				System.out.println("Serial successfully connected...");
			} else {
				throw new RuntimeException("Got a non-serial port, but only serial ports supported!");
			}
		}
		
		
		
		
	}
	
	/**
	 * Writes data to the port
	 */
	private void write(byte[] data) throws IOException {
		if (isConnected) {
			outStream.write(data);
			outStream.flush();
		}
	}

	/**
	 * Write color output data!
	 */
	public void visualize(ColorOutput colorOutput) {
			
		byte[] output = new byte[NUM_RGB_BOARDS * LENGTH_RGB_PACKET];
		
		
		// For now don't implement any compression to send the smallest command.
		int cursor = 0;
		for(int board = 0; board < NUM_RGB_BOARDS; board++) {
			
			generateRGBPanelPacket(output, cursor, colorOutput, board);
			cursor += LENGTH_RGB_PACKET;
			
		}
		
		
		
		
		try {
			Thread.sleep(30);
		} catch (Exception e) {
			
		}
		
		
		
		
		
		
		debugPrint(output);
		try {
			write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}
	
	private void debugPrint(byte[] data) {
		System.out.println("*****");
		for(int i = 0; i < data.length; i++) {
			System.out.println(convertSignedByteToUnsignedInt(data[i]));
		}
	}
	
	private int convertSignedByteToUnsignedInt(byte b) {
		if (b >= 0) {
			return (int) b;
		} else {
			return ((int) b) + 256;
		}
	}
	
	private void generateRGBPanelPacket(byte[] data, int cursor, ColorOutput colorOutput, int board) {
		Color c;
		
		data[cursor] = (byte) 255;
		data[cursor + 1] = (byte)board;
		
		c = colorOutput.rgbLights[getLightIndexFromBoard(board, 0)];
		data[cursor + 2] = limit(c.getRed());
		data[cursor + 3] = limit(c.getGreen());
		data[cursor + 4] = limit(c.getBlue());
		
		c = colorOutput.rgbLights[getLightIndexFromBoard(board, 1)];
		data[cursor + 5] = limit(c.getRed());
		data[cursor + 6] = limit(c.getGreen());
		data[cursor + 7] = limit(c.getBlue());
		
		c = colorOutput.rgbLights[getLightIndexFromBoard(board, 2)];
		data[cursor + 8] = limit(c.getRed());
		data[cursor + 9] = limit(c.getGreen());
		data[cursor + 10] = limit(c.getBlue());
		
		c = colorOutput.rgbLights[getLightIndexFromBoard(board, 3)];
		data[cursor + 11] = limit(c.getRed());
		data[cursor + 12] = limit(c.getGreen());
		data[cursor + 13] = limit(c.getBlue());
		
	}
	
	private byte limit(int val) {
		if (val > 254) {
			return (byte) 254;
		} else {
			return (byte) val;
		}
	}
	
	private int getLightIndexFromBoard(int board, int light) {
		return 4*board + light;
	}
	
	
}
