package PartyLightsController;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;

import Common.ColorOutput;
import Common.ColorOutput.OverallOutputCompression;
import LightDJGUI.ConfigFileParser;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

/**
 * This class is responsible for outputting commands over serial to the Next Make Party Lighting System.
 * 
 * This is an updated version of the protocol - SYNC Byte is set to 255, and all color channels (R, G, and B) are 
 * 16-bit instead of 8-bit.
 * 
 * @author Steve Levine
 *
 */
public class PartyLightsController16 {

	// Serial port fields
	protected String serialPortName;
	protected int speed;
	protected int serialDataBits = 8;
	protected int serialStopBits = 0;
	protected int serialParityBits = 0;
	private boolean isConnected;
	private OutputStream outStream;
	
	// Protocol information
	protected static final int MAX_COLOR_CHANNEL_VALUE = 4095;
	protected static final int BYTES_PER_COLOR_CHANNEL = 2;
	
	protected static final int SPECIAL_SYNC_BYTE = 255;
	protected static final int ACTION_EMERGENCY_LIGHTING = 254;
	protected static final int ACTION_EVERYTHING_OFF = 253;
	protected static final int ACTION_FRONT_LEDS_SAME = 252;
	protected static final int ACTION_REAR_LEDS_SAME = 251;
	protected static final int ACTION_SET_ALL_UVS = 250;
	protected static final int ACTION_SET_ALL_WHITES = 249;
	protected static final int ACTION_STROBE_WHITE = 248;
	protected static final int ACTION_FRONT_PANELS_SAME = 247;
	protected static final int ACTION_REAR_PANELS_SAME = 246;	
	protected static final int ACTION_STROBE_UV = 245;	

	
	protected static int NUM_RGB_FRONT_PANELS;
	protected static int NUM_RGB_REAR_PANELS;
	protected static int NUM_UV_STROBE_BOARDS;
	
	protected static final int LENGTH_HEADER = 2;
	protected static final int LIGHTS_PER_BOARD = 4;
	protected static final int CHANNELS_PER_LIGHT = 3;
	protected static final int UVCHANNELS_PER_BOARD = 2;
	
	protected static final int LENGTH_FRONT_RGB_INDIV_PACKET = LENGTH_HEADER + LIGHTS_PER_BOARD*CHANNELS_PER_LIGHT*BYTES_PER_COLOR_CHANNEL;
	protected static final int LENGTH_REAR_RGB_INDIV_PACKET = LENGTH_HEADER + LIGHTS_PER_BOARD*CHANNELS_PER_LIGHT*BYTES_PER_COLOR_CHANNEL;
	protected static final int LENGTH_FRONT_LEDS_SAME_PACKET = LENGTH_HEADER + CHANNELS_PER_LIGHT*BYTES_PER_COLOR_CHANNEL;
	protected static final int LENGTH_REAR_LEDS_SAME_PACKET = LENGTH_HEADER + CHANNELS_PER_LIGHT*BYTES_PER_COLOR_CHANNEL;
	protected static final int LENGTH_FRONT_PANELS_SAME_PACKET = LENGTH_HEADER + LIGHTS_PER_BOARD*CHANNELS_PER_LIGHT*BYTES_PER_COLOR_CHANNEL;
	protected static final int LENGTH_REAR_PANELS_SAME_PACKET = LENGTH_HEADER + LIGHTS_PER_BOARD*CHANNELS_PER_LIGHT*BYTES_PER_COLOR_CHANNEL;
	
	protected static final int LENGTH_UVWHITE_INDIV_PACKET = LENGTH_HEADER + UVCHANNELS_PER_BOARD*BYTES_PER_COLOR_CHANNEL;
	protected static final int LENGTH_WHITE_STROBE_PACKET = LENGTH_HEADER;
	protected static final int LENGTH_UV_STROBE_PACKET = LENGTH_HEADER;
	protected static final int LENGTH_ALL_OFF_PACKET = LENGTH_HEADER;
	protected static final int LENGTH_EMERGENCY_LIGHTING_PACKET = LENGTH_HEADER;
	protected static final int LENGTH_WHITE_SET_ALL_PACKET = LENGTH_HEADER + BYTES_PER_COLOR_CHANNEL;
	protected static final int LENGTH_UV_SET_ALL_PACKET = LENGTH_HEADER + BYTES_PER_COLOR_CHANNEL;
	
	protected static int START_REAR_PANEL_INDEX;
	protected static int START_UVWHITE_PANEL_INDEX;
	
	// Voloume controls
	public float overallVolume = 1.0f;
	public float frontVolume = 1.0f;
	public float rearVolume = 1.0f;
	public float strobeVolume = 1.0f;
	
	public enum LightPlacement {
		PLACEMENT_FRONT,
		PLACEMENT_REAR,
		PLACEMENT_STROBES
	}
	
	public PartyLightsController16() {
		
		// Set some parameters
		NUM_RGB_FRONT_PANELS = ColorOutput.NUM_FRONT_RGB_PANELS;
		NUM_RGB_REAR_PANELS = ColorOutput.NUM_REAR_RGB_PANELS;
		NUM_UV_STROBE_BOARDS = ColorOutput.NUM_UVWHITE_PANELS;
		START_REAR_PANEL_INDEX = ColorOutput.START_REAR_PANEL_ADDRESSES;
		START_UVWHITE_PANEL_INDEX = ColorOutput.START_UVWHITE_PANEL_ADDRESSES;
		
		// Set some defaults
		
		final String serialPortNameDefault = "/dev/ttyUSB0";	// Xbee
		final int serialPortSpeedDefault = 115200;				// Friggin XBee
		
		
		this.serialPortName = ConfigFileParser.getSettingOrDefault("SERIAL_PORT_NAME", "/dev/ttyUSB0");
		this.speed = ConfigFileParser.getSettingOrDefault("SERIAL_BAUDRATE", 115200);
		this.serialDataBits = ConfigFileParser.getSettingOrDefault("SERIAL_DATABITS", 8);
		this.serialParityBits = ConfigFileParser.getSettingOrDefault("SERIAL_PARITY", 0);
		this.serialStopBits = ConfigFileParser.getSettingOrDefault("SERIAL_STOPBITS", 2);
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
	protected void connect() throws Exception {
		System.out.println("Serial: " + this.serialPortName + " " + this.serialDataBits + "-" + this.serialParityBits + "-" + this.serialStopBits);
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(this.serialPortName);
		if (portIdentifier.isCurrentlyOwned()) {
			throw new RuntimeException("Error: The serial port " + serialPortName + " is already owned!");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(this.speed, this.serialDataBits, this.serialStopBits, this.serialParityBits);
				
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
	protected void write(byte[] data) throws IOException {
		
		//System.out.println("*********");
		//for(int i = 0; i < data.length; i++) {
		//	int d = data[i];
		//	if (d < 0) {
		//		System.out.println(d + 256);
		//	} else {
		//		System.out.println(d);
		//	}
		//}
		
		if (isConnected) {
			outStream.write(data);
			outStream.flush();
		}
	}

	/**
	 * Write color output data!
	 */
	public void visualize(ColorOutput colorOutput) {
		
		// Determine the optimal compression
		colorOutput.determineCompression();
		//System.out.println(colorOutput.uvWhiteColorOutputCompression);
		
		// Apply any overall compression!
		switch(colorOutput.overallOutputCompression) {
		case OVERALL_COMPRESSION_EMERGENCY_LIGHTING:
			sendEmergencyLighting();
			break;
			
		case OVERALL_COMPRESSION_ALL_OFF:
			sendAllOff();
			break;
			
		case OVERALL_COMPRESSION_WHITE_STROBE:
			sendWhiteStrobe();
			break;
			
		case OVERALL_COMPRESSION_UV_STROBE:
			sendUVStrobe();
			break;
			
		case OVERALL_COMPRESSION_NONE:
			// There's no overall compression; so send commands to the RGB fronts, RGB rears, and the strobes.
			// Fronts
			switch(colorOutput.rgbFrontColorOutputCompression) {
			case RGB_FRONT_COMPRESSION_LEDS_SAME:
				sendFrontLEDSame(colorOutput);
				break;
				
			case RGB_FRONT_COMPRESSION_PANELS_SAME:
				sendFrontPanelSame(colorOutput);
				break;
				
			case RGB_FRONT_COMPRESSION_DIFF:
				sendFrontRGBPanelDiff(colorOutput);
				break;
			}
			
			/*
			//Rears
			switch(colorOutput.rgbRearColorOutputCompression) {
			case RGB_REAR_COMPRESSION_LEDS_SAME:
				sendRearLEDSame(colorOutput);
				break;
				
			case RGB_REAR_COMPRESSION_PANELS_SAME:
				sendRearPanelSame(colorOutput);
				break;
				
			case RGB_REAR_COMPRESSION_DIFF:
				sendRearRGBPanelDiff(colorOutput);
				break;
			}
			*/
			
			
			// Send UV/Whites
			switch(colorOutput.uvWhiteColorOutputCompression) {
			case UVWHITE_COMPRESSION_WHITE_AND_UV_SAME:
				sendAllWhitesSame(colorOutput);
				sendAllUVSame(colorOutput);
				break;
				
			case UVWHITE_COMPRESSION_WHITE_AND_UV_DIFF:
				sendWhiteUVPanelsDiff(colorOutput);
				break;
				
			}
			
			break;
		
		}		
	}
	
	// Gamma correct to approximate the sRGB colorspace
	protected float gammaCorrect(float val) {
		double gamma = 3.4;	// 2.2
		return (float) Math.pow(val, gamma);
	}
	
	protected int fillValueData(byte[] data, int startIndex, float val, LightPlacement placement) {
		
		// Apply gamma correction
		val = gammaCorrect(val);
		
		// Apply any volume attenuations
		val = limit(val, placement);
		
		// Convert to the PWM range
		int out = (int) (val * MAX_COLOR_CHANNEL_VALUE + 0.5f);

		// Hard limit saturate at max value
		if (out > MAX_COLOR_CHANNEL_VALUE) {
			out = MAX_COLOR_CHANNEL_VALUE;
		}
		
		// Split it into 2 bytes
		byte lower = (byte) (out & 0xFF);
		byte upper = (byte) ((out >> 8) & 0xFF);
		
		// Make sure we're not sending the special sync byte.
		if (upper == (byte) SPECIAL_SYNC_BYTE) {
			upper = (byte) (SPECIAL_SYNC_BYTE - 1);
		}
		
		if (lower == (byte) SPECIAL_SYNC_BYTE) {
			lower = (byte) (SPECIAL_SYNC_BYTE - 1);
		}
		
		int index = startIndex;
		
		// Fill in the data.
		data[index++] = upper;
		data[index++] = lower;
		
		return index - startIndex;
	}
	
	// Utility function to fill in a field of data. Returns the number of bytes used.
	protected int fillColorData(byte[] data, int startIndex, Color c, LightPlacement placement) {
		// Extract floating point precision values of the colors
		float[] colors = new float[3];
		c.getRGBColorComponents(colors);
		
		int index = startIndex;
		
		for(int channel = 0; channel < 3; channel++) {
			// Write this val (checks limits, does gamma correction, conversino to max PWM value, etc.)
			index += fillValueData(data, index, colors[channel], placement);
		}
		
		return (index - startIndex);
	}
	
	protected void sendFrontRGBPanelDiff(ColorOutput colorOutput) {
		byte[] data = new byte[NUM_RGB_FRONT_PANELS * LENGTH_FRONT_RGB_INDIV_PACKET];
		// For now don't implement any compression to send the smallest command.
		int cursor = 0;
		for(int board = 0; board < NUM_RGB_FRONT_PANELS; board++) {
			
			Color c;
			
			data[cursor++] = (byte) SPECIAL_SYNC_BYTE;
			data[cursor++] = (byte) board;
			
			c = colorOutput.rgbLightsFront[getFrontLightIndexFromBoard(board, 0)];
			cursor += fillColorData(data, cursor, c, LightPlacement.PLACEMENT_FRONT);
			
			c = colorOutput.rgbLightsFront[getFrontLightIndexFromBoard(board, 1)];
			cursor += fillColorData(data, cursor, c, LightPlacement.PLACEMENT_FRONT);
			
			c = colorOutput.rgbLightsFront[getFrontLightIndexFromBoard(board, 2)];
			cursor += fillColorData(data, cursor, c, LightPlacement.PLACEMENT_FRONT);
			
			c = colorOutput.rgbLightsFront[getFrontLightIndexFromBoard(board, 3)];
			cursor += fillColorData(data, cursor, c, LightPlacement.PLACEMENT_FRONT);
			
		}
		
		debugPrint(data);
		try {
			write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	protected void sendFrontLEDSame(ColorOutput colorOutput) {
		byte[] output = new byte[LENGTH_FRONT_LEDS_SAME_PACKET];
		output[0] = (byte) SPECIAL_SYNC_BYTE;
		output[1] = (byte) ACTION_FRONT_LEDS_SAME;
		fillColorData(output, 2, colorOutput.rgbLightsFront[0], LightPlacement.PLACEMENT_FRONT);
		
		debugPrint(output);
		try {
			write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void sendFrontPanelSame(ColorOutput colorOutput) {
		byte[] output = new byte[LENGTH_FRONT_PANELS_SAME_PACKET];
		int cursor = 0;
		output[cursor++] = (byte) SPECIAL_SYNC_BYTE;
		output[cursor++] = (byte) ACTION_FRONT_PANELS_SAME;
		cursor += fillColorData(output, cursor, colorOutput.rgbLightsFront[0], LightPlacement.PLACEMENT_FRONT);
		cursor += fillColorData(output, cursor, colorOutput.rgbLightsFront[1], LightPlacement.PLACEMENT_FRONT);
		cursor += fillColorData(output, cursor, colorOutput.rgbLightsFront[2], LightPlacement.PLACEMENT_FRONT);
		cursor += fillColorData(output, cursor, colorOutput.rgbLightsFront[3], LightPlacement.PLACEMENT_FRONT);
		
		
		debugPrint(output);
		try {
			write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void sendRearRGBPanelDiff(ColorOutput colorOutput) {
		byte[] data = new byte[NUM_RGB_REAR_PANELS * LENGTH_REAR_RGB_INDIV_PACKET];
		// For now don't implement any compression to send the smallest command.
		int cursor = 0;
		for(int board = 0; board < NUM_RGB_REAR_PANELS; board++) {
			
			Color c;
			
			data[cursor++] = (byte) SPECIAL_SYNC_BYTE;
			data[cursor++] = (byte) (board + START_REAR_PANEL_INDEX);
			
			c = colorOutput.rgbLightsRear[getRearLightIndexFromBoard(board, 0)];
			cursor += fillColorData(data, cursor, c, LightPlacement.PLACEMENT_REAR);
			
			c = colorOutput.rgbLightsRear[getRearLightIndexFromBoard(board, 1)];
			cursor += fillColorData(data, cursor, c, LightPlacement.PLACEMENT_REAR);
			
			c = colorOutput.rgbLightsRear[getRearLightIndexFromBoard(board, 2)];
			cursor += fillColorData(data, cursor, c, LightPlacement.PLACEMENT_REAR);
			
			c = colorOutput.rgbLightsRear[getRearLightIndexFromBoard(board, 3)];
			cursor += fillColorData(data, cursor, c, LightPlacement.PLACEMENT_REAR);
			
		}
		
		debugPrint(data);
		try {
			write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	protected void sendRearLEDSame(ColorOutput colorOutput) {
		byte[] output = new byte[LENGTH_REAR_LEDS_SAME_PACKET];
		output[0] = (byte) SPECIAL_SYNC_BYTE;
		output[1] = (byte) ACTION_REAR_LEDS_SAME;
		fillColorData(output, 2, colorOutput.rgbLightsRear[0], LightPlacement.PLACEMENT_REAR);
		
		
		debugPrint(output);
		try {
			write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	protected void sendRearPanelSame(ColorOutput colorOutput) {
		byte[] output = new byte[LENGTH_REAR_PANELS_SAME_PACKET];
		int cursor = 0;
		output[cursor++] = (byte) SPECIAL_SYNC_BYTE;
		output[cursor++] = (byte) ACTION_REAR_PANELS_SAME;
		cursor += fillColorData(output, cursor, colorOutput.rgbLightsRear[0], LightPlacement.PLACEMENT_REAR);
		cursor += fillColorData(output, cursor, colorOutput.rgbLightsRear[1], LightPlacement.PLACEMENT_REAR);
		cursor += fillColorData(output, cursor, colorOutput.rgbLightsRear[2], LightPlacement.PLACEMENT_REAR);
		cursor += fillColorData(output, cursor, colorOutput.rgbLightsRear[3], LightPlacement.PLACEMENT_REAR);
		
		debugPrint(output);
		try {
			write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void sendAllWhitesSame(ColorOutput colorOutput) {
		byte[] output = new byte[LENGTH_WHITE_SET_ALL_PACKET];
		output[0] = (byte) SPECIAL_SYNC_BYTE;
		output[1] = (byte) ACTION_SET_ALL_WHITES;
		fillValueData(output, 2, (float) colorOutput.whiteLights[0], LightPlacement.PLACEMENT_STROBES);
		
		debugPrint(output);
		try {
			write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	protected void sendAllUVSame(ColorOutput colorOutput) {
		byte[] output = new byte[LENGTH_UV_SET_ALL_PACKET];
		output[0] = (byte) SPECIAL_SYNC_BYTE;
		output[1] = (byte) ACTION_SET_ALL_UVS;
		fillValueData(output, 2, (float) colorOutput.uvLights[0], LightPlacement.PLACEMENT_STROBES);
		
		debugPrint(output);
		try {
			write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	protected void sendWhiteUVPanelsDiff(ColorOutput colorOutput) {
		byte[] data = new byte[NUM_UV_STROBE_BOARDS * LENGTH_UVWHITE_INDIV_PACKET];
		// For now don't implement any compression to send the smallest command.
		int cursor = 0;
		for(int board = 0; board < NUM_UV_STROBE_BOARDS; board++) {
			
			data[cursor++] = (byte) SPECIAL_SYNC_BYTE;
			data[cursor++] = (byte) (board + START_UVWHITE_PANEL_INDEX);
			cursor += fillValueData(data, cursor, (float) colorOutput.uvLights[board], LightPlacement.PLACEMENT_STROBES);
			cursor += fillValueData(data, cursor, (float) colorOutput.whiteLights[board], LightPlacement.PLACEMENT_STROBES);
			//System.out.println(colorOutput.whiteLights[board]);
		}
		
		debugPrint(data);
		try {
			write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	protected void sendWhiteStrobe() {
		byte[] output = new byte[LENGTH_WHITE_STROBE_PACKET];
		output[0] = (byte) SPECIAL_SYNC_BYTE;
		output[1] = (byte) ACTION_STROBE_WHITE;
		
		// Try to write to output
		debugPrint(output);
		try {
			write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void sendUVStrobe() {
		byte[] output = new byte[LENGTH_UV_STROBE_PACKET];
		output[0] = (byte) SPECIAL_SYNC_BYTE;
		output[1] = (byte) ACTION_STROBE_UV;
		
		// Try to write to output
		try {
			write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void sendEmergencyLighting() {
		byte[] output = new byte[LENGTH_EMERGENCY_LIGHTING_PACKET];
		output[0] = (byte) SPECIAL_SYNC_BYTE;
		output[1] = (byte) ACTION_EMERGENCY_LIGHTING;
		
		// Try to write to output
		debugPrint(output);
		try {
			write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void sendAllOff() {
		byte[] output = new byte[LENGTH_ALL_OFF_PACKET];
		output[0] = (byte) SPECIAL_SYNC_BYTE;
		output[1] = (byte) ACTION_EVERYTHING_OFF;
		
		// Try to write to output
		debugPrint(output);
		try {
			write(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	protected void debugPrint(byte[] data) {
//		System.out.println("*****");
//		for(int i = 0; i < data.length; i++) {
//			System.out.println(convertSignedByteToUnsignedInt(data[i]));
//		}
	}
	
	protected int convertSignedByteToUnsignedInt(byte b) {
		if (b >= 0) {
			return (int) b;
		} else {
			return ((int) b) + 256;
		}
	}
	
	
	protected float limit(float a, LightPlacement placement) {
		// Placement: 0 => front
		//            1 => rear
		//            2 => white, uv strobes
		
//		double overallVolume = 0.3;
//		double frontVolume = 0.5;
//		double rearVolume = 0.10;
//		double strobeVolume = 1.0;
		
		float val = 0;
		
		switch(placement) {
		case PLACEMENT_FRONT:
			val = (overallVolume * frontVolume * a);
			break;
			
		case PLACEMENT_REAR:
			val = (overallVolume * rearVolume * a);
			break;
			
		case PLACEMENT_STROBES:
			val = (overallVolume * strobeVolume * a);
			break;
		}
		

		return val;
	}
	
	protected int getFrontLightIndexFromBoard(int board, int light) { 
		return  ColorOutput.NUM_LEDS_PER_RGB_BOARD*board + light;
	}
	
	protected int getRearLightIndexFromBoard(int board, int light) {
		return ColorOutput.NUM_LEDS_PER_RGB_BOARD*board + light;
	}
	
	
}
