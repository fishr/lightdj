package SoundEngine;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.*;

import Utils.TimerTicToc;

/**
 * Starts everything
 * @author Steve Levine
 *
 */

public class MainClass {

	//private static final String soundFilename = "/home/steve/Desktop/01 Replay.wav";
	private static final String soundFilename = "/home/steve/Desktop/04 Troublemaker.wav";
	//private static final String soundFilename = "/home/steve/Desktop/sweep.wav";
	//private static final String soundFilename = "/home/steve/Desktop/whitenoise.wav";
	private static final int AUDIO_READ_BUFFER_SIZE = 1024;
	private static final int SAMPLE_RATE = 44100;
	private static final boolean USE_CAPTURED_AUDIO = true;
	private static final boolean AUDIO_PASS_THRU = false;
	private static final double INITIAL_AUDIO_DELAY = 0.000;
	private static final double INITIAL_VIDEO_DELAY = 0.005;

	
	public static void main(String[] args) {
		// Do stuff here
		System.out.println("Initializing Sound Expressor...");
		
		if (USE_CAPTURED_AUDIO) {
			System.out.println("Using captured audio...");
			runWithCapturedAudio();
			
		} else {
			System.out.println("Using audio file: " + soundFilename);
			runFromSoundFile();
		}
		
		
	}
	
	// Don't operate from live captured audio, but rather from a pre-recorded sound file.
	public static void runFromSoundFile() {
		// For now, attempt to play back from a clever music file.
		File songFile = new File(soundFilename);
		AudioInputStream audioInputStream;
		
		System.out.println("Opening audio file...");
		try {
			audioInputStream = AudioSystem.getAudioInputStream(songFile);
		} catch (UnsupportedAudioFileException e) {
			System.out.println("Invalid audio file!");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.out.println("Could not open audio file!");
			e.printStackTrace();
			return;
		}
		
		AudioFormat format = audioInputStream.getFormat();
		
		SoundVisualizer engine = new SoundVisualizer(format, true, INITIAL_AUDIO_DELAY, INITIAL_VIDEO_DELAY, AUDIO_READ_BUFFER_SIZE);
		// Start sending it data!
		int bytesToRead = AUDIO_READ_BUFFER_SIZE;
		
		System.out.println("Starting playback...");
		try {
			int numBytesRead = 0;
			byte[] audioData = new byte[bytesToRead];
			
			
			//TimerTicToc timer = new TimerTicToc();
			engine.start(0.5 * AUDIO_READ_BUFFER_SIZE / format.getSampleRate());
			
			while((numBytesRead = audioInputStream.read(audioData)) != -1) {
				
				// Send data!
				engine.write(audioData, 0, numBytesRead);
			}
			
			
		} catch (Exception e) {
			System.out.println("Error during audio playback!");
			e.printStackTrace();
			return;
		}
		System.out.println("Playback finished!");
	}

	
	
	
	// Capture live audio from the computer system, and run on this.
	public static void runWithCapturedAudio() {
		
		// Set up the desired input audio format,  using a default reasonable value
		AudioFormat format;
		format = new AudioFormat((float) SAMPLE_RATE, 16, 2, true, false);
		
		
		TargetDataLine line;
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		if (AudioSystem.isLineSupported(info)) {
			try {
				line = (TargetDataLine) AudioSystem.getLine(info);
				//line.open(format);
				line.open(format, 2*1024);	// Change the line-in audio buffer size here
				System.out.println("Line-in buffer size: " + line.getBufferSize());
			} catch (Exception e) {
				System.out.println("Error: Could not open input audio line!");
				return;
			}
		} else {
			System.out.println("Unsupported audio input line!");
			return;
		}
		System.out.println("Successfully opened up audio port...");
	
		// Sound visualization engine
		SoundVisualizer engine = new SoundVisualizer(format, AUDIO_PASS_THRU, INITIAL_AUDIO_DELAY, INITIAL_VIDEO_DELAY, AUDIO_READ_BUFFER_SIZE);
		
		//Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		
//		try {
//			Thread.sleep(1000);
//		} catch (Exception e) {
//			
//		}
		
		
		
		
		
		// Start reading data from it!
		int bytesToRead = AUDIO_READ_BUFFER_SIZE;
		System.out.println("Starting audio capture...");
		try {
			
			
			int numBytesRead = 0;
			byte[] audioData = new byte[bytesToRead];
			
			line.start();
			line.read(audioData, 0, bytesToRead);	// Start reading now, just to make sure everything is set up

			
			//TimerTicToc t = new TimerTicToc();
			
			engine.start(bytesToRead / (SAMPLE_RATE * format.getFrameSize()));
			while((numBytesRead = line.read(audioData, 0, bytesToRead)) != -1) {
				
				// Send data!
				//t.tic();
				engine.write(audioData, 0, numBytesRead);
				//t.toc();
				
				//System.out.println("Engine: " + t.getAverageTime());
				
			}
			
			
		} catch (Exception e) {
			System.out.println("Error during audio capture!");
			e.printStackTrace();
			return;
		}
		System.out.println("Audio capture ended!");
		
	}
	
	
	
}
