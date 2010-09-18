package SoundEngine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.sound.sampled.*;

import SignalGUI.GUIVisualizer;
import SignalGUI.GraphDisplay;

public class SoundVisualizer {

	// Constants
	private final int AUDIO_BUFFER_SIZE = 1024*128;
	
	// Audio stuff
	SourceDataLine outputLine;
	boolean audioWorking = false;
	boolean passThru;
	
	// A visualization engine
	VisualizationEngine visuals;
	
	public SoundVisualizer(AudioFormat format, boolean passThruToAudio) {
		// Set up audio
		initAudio(format);

		// Set up a visualization engine
		visuals = new VisualizationEngine(format);
		passThru = passThruToAudio;
		
	}
	
	// Sets up audio
	private void initAudio(AudioFormat format) {
		try {
			outputLine = AudioSystem.getSourceDataLine(format);
			outputLine.open(format, AUDIO_BUFFER_SIZE);
		} catch (LineUnavailableException e) {
			System.out.println("SoundVisualizer cannot open source data line!");
			e.printStackTrace();
			return;
		}
		outputLine.start();
		
		audioWorking = true;
	}
	
	// Write bytes into the buffer
	public void write(byte[] data, int offset, int length) {
		// Pass through to the audio buffer!
		if (passThru) {
			outputLine.write(data, offset, length);
		}
		
		// Also write the audio to the visualizer!
		visuals.write(data, offset, length);
		
		
	}
	
	
}


