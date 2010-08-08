package SoundEngine;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import SignalGUI.ChannelLights;
import SignalGUI.GUIVisualizer;
import SignalGUI.GraphMapper;
import SignalGUI.ScrollingChannelMapper;
import SignalGUI.ScrollingSpectrumMapper;
import Signals.FFT;
import Signals.LinearFilter;

/**
 * This class is responsible for generating the visualizations.
 * @author steve
 *
 */
public class VisualizationEngine {

	private double[] buffer;
	private int bufferCursor = 0;
	private final int BUFFER_SIZE = 1024;
	
	// The audio format of data being written in
	private final int FRAME_SIZE;
	private final int BYTES_PER_SAMPLE;
	private final long MAX_SAMPLE_VAL;
	private final int SAMPLE_RATE;
	
	
	// For multi-channel audio (ex., stereo), only use this channel for processing
	private final int CHANNEL_TO_PROCESS = 0;
	
	// Visualization stuff
	GUIVisualizer gui;
	GraphMapper graphMapper;
	ScrollingSpectrumMapper spectrumMapper;
	ScrollingChannelMapper channelMapper;
	ChannelLights lights;
	
	// State machines that output channel values
	int numChannels = 2;
	BassFinder bassFinder;
	ClapFinder clapFinder;
	
	// Keep a helper thread so that visualization display when run in parallel with the FFT calculuations,
	// thereby reducing overall latency through parallelization
	VisualizerHelperThread helperThread;
	
	
	public VisualizationEngine(AudioFormat format) {
		
		// Remember stuff about the audio format
		// For simplicity for now, only support 16 bit samples
		if (format.getSampleSizeInBits() != 16) {
			System.out.println("Error: I currently only support 16 bit linear PCM audio data!");
			throw new RuntimeException("I only support 16 bit linear PCM audio data!");	
		} else if (format.isBigEndian()){
			System.out.println("Error: I don't feel like supporting big endian!");
			throw new RuntimeException("I don't feel like supporting big endian!");	
		} else {
			// Okay
			BYTES_PER_SAMPLE = 2;
			FRAME_SIZE = format.getFrameSize();
			MAX_SAMPLE_VAL = (long) Math.pow(2, 8*BYTES_PER_SAMPLE - 1);
			SAMPLE_RATE = (int) format.getSampleRate();
			
			System.out.println("Audio frame size: " + format.getFrameSize());
			
		}
		
		// Set up sample buffer
		buffer = new double[BUFFER_SIZE];
		bufferCursor = 0;
		
		
		initVisualizations();
		
		
	}
	
	private void initVisualizations() {
		// Set up the GUI
		gui = GUIVisualizer.makeGUI();
		graphMapper = new GraphMapper(30, 30, 500, 250, (Graphics2D) gui.getGraphics());
		spectrumMapper = new ScrollingSpectrumMapper(30, 300, 800, 200, (Graphics2D) gui.getGraphics());
		
		// Select channel colors
		Color[] channelColors = new Color[numChannels];
		channelColors[0] = Color.RED;
		channelColors[1] = Color.BLUE;
		channelMapper = new ScrollingChannelMapper(channelColors, 30, 550, 800, 200, (Graphics2D) gui.getGraphics());
		lights = new ChannelLights(channelColors, 100, 550, 30, 300, 101, (Graphics2D) gui.getGraphics());
		
		// Start some state machines
		bassFinder = new BassFinder(SAMPLE_RATE, BUFFER_SIZE);
		clapFinder = new ClapFinder(SAMPLE_RATE, BUFFER_SIZE);
		
		
		// Start up a helper thread
		helperThread = new VisualizerHelperThread(this);
		
		
	}
	
	
	/**
	 * Write data into the buffer, and visualize when appropriate.
	 */
	public void write(byte[] data, int offset, int length) {
		// Data is in the form of frames, which could be multi-channel audio.
		// Read in by samples
		long lValue;
		double dValue;
		
		for(int dataCursor = offset; dataCursor < length + offset; dataCursor += FRAME_SIZE) {
			// Read in one sample
			lValue = 0;
			if (BYTES_PER_SAMPLE == 2) {
				lValue = ((((short) data[dataCursor + 1]) + 128) << 8) | (((short) data[dataCursor]) + 128);
			}
			
			// Convert this to a double value, and store it!
			
			
			dValue = (double) (lValue - MAX_SAMPLE_VAL) / (MAX_SAMPLE_VAL);
			
			// Put in in the buffer!
			buffer[bufferCursor++] = dValue;
			
			// Is it time to visualize?
			if (bufferCursor == BUFFER_SIZE) {
				visualize();
				
				// Reset the ring buffer
				bufferCursor = 0;
			}

		}
	}
	
	public double[] differenceSquared(double[] current, double[] last) {
		double[] output = new double[current.length];
		
		for(int i = 0; i < current.length; i++) {
			output[i] = (current[i] - last[i]) * (current[i] - last[i]);
		}
		
		return output;
	}
	
	
	private void visualize() {
		
		// Compute an FFT
		FFT fft = new FFT(buffer, SAMPLE_RATE);
		
		// Update the render helper thread
		helperThread.updateFFT(fft);
	
	}
	
	
	public void updateVisuals(FFT fft) {
		//graphMapper.drawPositiveGraph(fft.getLogMagnitudes(), 4);
		//double[] logMags = fft.getLogMagnitudes();
		
		// Low pass filter it, just for kicks
		//LinearFilter filter = LinearFilter.createAveragingFilter(1);
		//LinearFilter filter = LinearFilter.createAveragingFilter(1);//new LinearFilter(new double[]{0.25,0.5,0.25}, new double[]{0});
		double[] frequencies = fft.getFrequencies();
		double[] magnitudes = fft.getMagnitudes(); //filter.filterSignal(logMags);
		
		//graphMapper.drawPositiveLogHalfX(fft.getFrequencies(), filteredData, 15, 20000, 4);
		//if (lastData == null) {lastData = filteredData;}
		//graphMapper.drawPositiveLogHalfX(fft.getFrequencies(), differenceSquared(magnitudes, lastData), 15, 20000, 8);
		graphMapper.drawPositiveLogHalfX(frequencies, magnitudes, 30, 20000, 240);
		//graphMapper.drawPositiveGraph(buffer, 2);
		spectrumMapper.updateWithNewSpectrum(frequencies, magnitudes, 30, 20000, 80);
		
		// Compute some channel values
		double[] channels = new double[numChannels];
		channels[0] = bassFinder.getFreqs(frequencies, magnitudes);
		channels[1] = clapFinder.getFreqs(frequencies, magnitudes);
		channelMapper.updateWithNewChannelVals(channels);
		lights.updateWithNewChannelVals(channels);
		
		//lastData = logMags;
	}

	
}


// A separate thread that does rendering, so as to not slow down the FFT-calculations too much while 
// we render the visuals
class VisualizerHelperThread implements Runnable {
	
	private FFT recentFFT;
	private VisualizationEngine engine;
	boolean isRunning;
	
	public VisualizerHelperThread(VisualizationEngine engine) {
		isRunning = false;
		this.engine = engine;
	}
	
	// Update the FFT being used
	public void updateFFT(FFT fft) {
		
		// Cache a reference to this FFT
		if (recentFFT != null) {
			synchronized(recentFFT) {
				recentFFT = fft;
			}
		} else {
			recentFFT = fft;
		}
	
		// Spawn a new thread if we haven't already!
		if (!isRunning) {
			isRunning = true;
			Thread t = new Thread(this);
			t.start();
		}
		
	}
	
	// Run stuff!
	public void run() {
		
		FFT fft;
		
		while (true) {
			
			// Store a local reference to the FFT, so that way if it
			// gets updated later, we can still operate on the old one.
			synchronized(recentFFT) {
				fft = recentFFT;
			}
			
			// Process this FFT
			engine.updateVisuals(fft);
			
			
		}
	}
	

	
}
