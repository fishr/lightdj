package SoundEngine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.sound.sampled.*;

import SignalGUI.GUIVisualizer;
import SignalGUI.GraphDisplay;
import Utils.TimerTicToc;

/**
 * Maintains the audio and visuals, especially the sync.
 * @author Steve Levine
 *
 */

public class SoundVisualizer {

	// Constants
	private int SOUNDCARD_BUFFER_SIZE = 1024*64; //1024*64	// The size of the audio buffer used by Java
	private int AUDIO_BUFFER_SIZE;					// The size fo the audio buffer used by this software
													// to lock-step synchronize audio with visuals
	
	// Audio stuff
	SourceDataLine outputLine;
	boolean audioWorking = false;
	boolean passThru;
	
	// Raw bytes for the audio buffer - will ultimately be sent to the hardware buffer.
	byte[] audioBuffer;
	int bufferReadPointer;
	int bufferWritePointer;
	
	// A visualization engine
	VisualizationEngine visuals;
	
	public SoundVisualizer(AudioFormat format, boolean passThruToAudio, double initialAudioDelay, double initialVideoDelay, int maxBufferWriteSize) {
		
		// Set up audio and video
		initAudioAndVideo(format, initialAudioDelay, initialVideoDelay, passThruToAudio, maxBufferWriteSize);
		
	}
	
	// Sets up audio
	private void initAudioAndVideo(AudioFormat format, double initialAudioDelaySec, double initialVideoDelaySec, boolean passThruToAudio, int maxBufferWriteSize) {
		
		passThru = passThruToAudio;
		
		if (passThru) {
			int audioFrameSize = format.getFrameSize();
			int audioFramesToDelay = (int) (initialAudioDelaySec * format.getFrameRate());
			
			try {
				
				// Choose the size to use the internal audio buffer used in this software
				AUDIO_BUFFER_SIZE = audioFramesToDelay * audioFrameSize + maxBufferWriteSize;
				audioBuffer = new byte[AUDIO_BUFFER_SIZE];
				bufferReadPointer = 0;	// Start sending data to the hardware buffer at 0
				bufferWritePointer = audioFramesToDelay * audioFrameSize;	// Start writing immediately after the silence
				
				// Open a hardware audio buffer to output sound
				outputLine = AudioSystem.getSourceDataLine(format);
				outputLine.open(format, SOUNDCARD_BUFFER_SIZE);
				
				System.out.println("Hardware buffer size: " + outputLine.getBufferSize());
				System.out.println("Audio buffer size: " + AUDIO_BUFFER_SIZE);
				
			} catch (LineUnavailableException e) {
				System.out.println("SoundVisualizer cannot open source data line!");
				e.printStackTrace();
				return;
			}
			
			// Write empty data into the audio buffer for the prescribed amount of time
	
			// so that we can effectively adjust the initial delay of the audio. This will help
			// to sync the music to the lights.
			//for(int i = bufferReadPointer; i < bufferWritePointer; i++) {
			//	audioBuffer[i] = (byte) (8.0 * Math.random() -128);
			//}
			
		}
		audioWorking = true;
		
		// Set up video
		// Set up a visualization engine
		visuals = new VisualizationEngineParty(format, initialVideoDelaySec);
		//visuals = new VisualizationEngineAC(format, initialVideoDelaySec);

	}
	
	public void start(double startupDelay) {
		if (passThru) {
			outputLine.start();
			//System.out.println("Available: " + outputLine.available() + ", writing " + silence.length);
			//outputLine.write(silence, 0, silence.length);
		}
		visuals.start(startupDelay);
	}
	
	// Write bytes into the buffer
	public void write(byte[] data, int offset, int length) {
		// Pass through to the audio buffer and to the visuals
		
		if (passThru) {
			
			// Add this data to the audio buffer
			for(int i = offset; i < offset + length; i++) {
				audioBuffer[bufferWritePointer] = data[i];
				bufferWritePointer = (bufferWritePointer + 1) % AUDIO_BUFFER_SIZE;
			}

			
			// Write proper data to the hardware buffer
			if (bufferReadPointer + length <= AUDIO_BUFFER_SIZE) {
				outputLine.write(audioBuffer, bufferReadPointer, length);
			} else {
				outputLine.write(audioBuffer, bufferReadPointer, AUDIO_BUFFER_SIZE - bufferReadPointer);
				outputLine.write(audioBuffer, 0, length - (AUDIO_BUFFER_SIZE - bufferReadPointer));
				
			}
			bufferReadPointer = (bufferReadPointer + length) % AUDIO_BUFFER_SIZE;
		}
		
		// Write the audio to the visualizer!
		visuals.write(data, offset, length);
		
		TimerTicToc t = new TimerTicToc();

		
	
	}
	
	
}


