package LightDJGUI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses a LightDJ configuration file.
 * @author Steve Levine
 *
 */
public class ConfigFileParser {
	private static Map<String,String> configSettings;
	
	// Parse the file
	public static void parseFile(String filename) {
		
		// Create a new map to store the key and value pairs
		configSettings = new HashMap<String, String>();
		
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(filename));
		} catch (Exception e) {
			System.out.println("Error: Could not open configuration file!");
			e.printStackTrace();
			return;
		}
		
		String input;
		int lineNum = 0;
		
		try {
			while ((input = in.readLine()) != null) {
				lineNum++;
				// Ignore this line if it is empty or starts with a # sign
				input = input.trim();	// Remove beginning and trailing whitespace
				if (input.equals("") || input.startsWith("#")) {
					continue;	// Skip this line
				}
				
				// Parse this line, which should be of the form KEY = VALUE (also remove whitespace)
				String[] exp = input.split("=");
				if (exp.length != 2) {
					System.out.println("Warning: error in config file on line " + lineNum);
					continue;
				}
				
				// Extract the key / value pair, remove whitespace, and make the key upper case
				String key = exp[0].trim().toUpperCase();
				String val = exp[1].trim();
				
				// Store this key / value pair
				configSettings.put(key, val);
				
			}
		} catch (IOException e) {
			System.out.println("Error: Could not finish reading configurationfile!");
			e.printStackTrace();
		}
		
	}
	
	// Return the value corresponding to some key in the configuration file
	public static String getSettingOrDefault(String key, String defaultVal) {
		if (containsSetting(key)) {
			return configSettings.get(key);
		} else {
			return defaultVal;
		}
	}
	
	// Checks to see if the configuration file contains the given setting
	public static boolean containsSetting(String key) {
		return configSettings.containsKey(key);
	}
	
	// Print out a configuration file to standard out
	public static void printConfigFile() {
		System.out.println("Configuration File:");
		for(String key : configSettings.keySet()) {
			String val = configSettings.get(key);
			System.out.println("   " + key + " = " + val);
		}
	}
	
}
