package SoundEngine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.concurrent.Semaphore;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import Arduino.LEDVisualizer;
import SignalGUI.ChannelLights;
import SignalGUI.GUIVisualizer;
import SignalGUI.GraphMapper;
import SignalGUI.ScrollingChannelMapper;
import SignalGUI.ScrollingSpectrumMapper;
import Signals.Complex;
import Signals.FFT;
import Signals.FFTEngine;
import Signals.LinearFilter;
import Utils.TimerTicToc;

/**
 * This class is responsible for generating the visualizations.
 * @author steve
 *
 */
public class VisualizationEngine {

	private double[][] buffers;
	private int[] bufferCursors;
	private final int BUFFER_SIZE = 8192;
	private final int BUFFER_OVERLAP = 16;	// Must be a power of 2
	
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
	int numChannels = 4;
	BassFinder bassFinder;
	VocalsFinder vocalsFinder;
	ClapFinder clapFinder;
	
	// Color controllers
	ColorGenerator rgbController;
	
	
	// The arduino LED visualizer
	LEDVisualizer ledVisuals;
	
	// Keep a helper thread so that visualization display when run in parallel with the FFT calculuations,
	// thereby reducing overall latency through parallelization
	VisualizerHelperThread helperThread;
	
	// Used for profiling and debugging
	TimerTicToc timer;
	
	// The FFT engine
	FFTEngine fftEngine;
	
	
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
		
		// Set up sample buffers
		buffers = new double[BUFFER_OVERLAP][BUFFER_SIZE];
		bufferCursors = new int[BUFFER_OVERLAP];
		for(int i = 0; i < BUFFER_OVERLAP; i++) {
			bufferCursors[i] = i*(BUFFER_SIZE/BUFFER_OVERLAP);
		}
		
		
		
		// Set up a profiler, for debugging use
		timer = new TimerTicToc();
		
		// Start the FFT engine
		fftEngine = new FFTEngine(BUFFER_SIZE, SAMPLE_RATE);
		
		// Load up the visualizations
		initVisualizations();
		
		
	}
	
	private void initVisualizations() {
		// Set up the GUI
		gui = GUIVisualizer.makeGUI();
		graphMapper = new GraphMapper(30, 30, 700, 350, (Graphics2D) gui.getGraphics());
		spectrumMapper = new ScrollingSpectrumMapper(30, 400, 800, 300, (Graphics2D) gui.getGraphics());
		
		// Select channel colors
		Color[] channelColors = new Color[numChannels];
		channelColors[0] = Color.RED;
		channelColors[1] = Color.RED;
		channelColors[2] = Color.GREEN;
		channelColors[3] = Color.BLUE;
		channelMapper = new ScrollingChannelMapper(channelColors, 30, 750, 800, 200, (Graphics2D) gui.getGraphics());
		lights = new ChannelLights(channelColors, 75, 750, 30, 360, 76, (Graphics2D) gui.getGraphics());
		
		// Start some state machines
		bassFinder = new BassFinder(SAMPLE_RATE, BUFFER_SIZE);
		clapFinder = new ClapFinder(SAMPLE_RATE, BUFFER_SIZE);
		vocalsFinder = new VocalsFinder(SAMPLE_RATE, BUFFER_SIZE);
		
		// Start an RGB controller with some colors
		//rgbController = new RGBGradientController(Color.BLUE, Color.RED);
		rgbController = new HueRotator(0.0, 0.373);
		
		// Try to connect to the Arduino controlling the lights
		ledVisuals = new LEDVisualizer();
		//ledVisuals.lcdSetText("1", "2");
		//ledVisuals.lcdBacklightOn();
		
		
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
			
			// Put in in the buffers!
			for(int i = 0; i < BUFFER_OVERLAP; i++) {
			
				buffers[i][bufferCursors[i]++] = dValue;
				
				// Is it time to visualize?
				if (bufferCursors[i] == BUFFER_SIZE) {
					visualize(buffers[i]);
					
					// Reset the ring buffer
					bufferCursors[i] = 0;
				}
				
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
	
	
	private void visualize(double[] buffer) {
		
		// Compute an FFT
		
		//timer.tic();
		FFT fft = fftEngine.computeFFT(buffer);  //new FFT(buffer, SAMPLE_RATE);
		//timer.toc();
		//System.out.println(timer.getAverageTime());
		
		// Update the render helper thread
		helperThread.updateFFT(fft);
	
	}
	
	
	public void updateVisuals(FFT fft) {
		
		//timer.tic();
		
		double[] frequencies = fft.getFrequencies();
		double[] magnitudes = fft.getMagnitudes();
		
		// Update the RGB controller
		double bassLevel = bassFinder.getFreqs(frequencies, magnitudes);
		//System.out.println(bassLevel);
		double vocalsLevel = vocalsFinder.getFreqs(frequencies, magnitudes);
		double clapLevel = clapFinder.getFreqs(frequencies, magnitudes);
		//rgbController.computeGradient(clapLevel, vocalsLevel);
		rgbController.step(clapLevel);
		
		// Compute some channel values
		double[] channels = new double[numChannels];
		channels[0] = bassLevel;
		channels[1] = rgbController.getRed();
		channels[2] = rgbController.getGreen();
		channels[3] = rgbController.getBlue();
		ledVisuals.visualize(channels);
		

		
		//lights.updateWithNewChannelVals(channels);
		//channelMapper.updateWithNewChannelVals(channels);
		
		
		
		//graphMapper.drawPositiveGraph(fft.getLogMagnitudes(), 4);
		//double[] logMags = fft.getLogMagnitudes();
		
		// Low pass filter it, just for kicks
		//LinearFilter filter = LinearFilter.createAveragingFilter(1);
		//LinearFilter filter = LinearFilter.createAveragingFilter(1);//new LinearFilter(new double[]{0.25,0.5,0.25}, new double[]{0});

		
		//graphMapper.drawPositiveLogHalfX(fft.getFrequencies(), filteredData, 15, 20000, 4);
		//if (lastData == null) {lastData = filteredData;}
		//graphMapper.drawPositiveLogHalfX(fft.getFrequencies(), differenceSquared(magnitudes, lastData), 15, 20000, 8);
		
		LinearFilter filter = new LinearFilter(new double[]{0.2}, new double[]{0.8});
		//double[] lowpassedMags = filter.filterSignal(magnitudes);
		
		graphMapper.drawPositiveLogHalfX(frequencies, magnitudes, null, 30, 20000, 200);
		//graphMapper.drawPositiveGraph(buffer, 2);
		spectrumMapper.updateWithNewSpectrum(frequencies, magnitudes, 30, 20000, 100);
		
		//graphMapper.drawPositiveLogHalfX(frequencies, clapFinder.getAveragedFreqs(), null, 30, 20000, 40);
		//spectrumMapper.updateWithNewSpectrum(frequencies, clapFinder.getAveragedFreqs(), 30, 20000, 100);
		
		//timer.toc();
		//System.out.println(timer.getAverageTime());
		
		//lastData = logMags;
	}

	
}


// A separate thread that does rendering, so as to not slow down the FFT-calculations too much while 
// we render the visuals
class VisualizerHelperThread implements Runnable {
	
	private FFT recentFFT;
	private VisualizationEngine engine;
	private boolean isRunning;
	private Semaphore semaphore;
	
	public VisualizerHelperThread(VisualizationEngine engine) {
		isRunning = false;
		this.engine = engine;
		
		// Set up a semaphore so that we only renderer (consumer) only works
		// when we have an FFT (from the producer)
		semaphore = new Semaphore(1);
		
	}
	
	// Update the FFT being used
	public void updateFFT(FFT fft) {
		
		// Cache a reference to this FFT
		if (recentFFT != null) {
			synchronized(recentFFT) {
				recentFFT = fft;
				// Up the semaphore if necessary
				if (semaphore.availablePermits() == 0) {
					semaphore.release();
				}
			}
		} else {
			recentFFT = fft;
			// Up the semaphore if necessary
			if (semaphore.availablePermits() == 0) {
				semaphore.release();
			}
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
			
			// Wait until we have data!
			semaphore.acquireUninterruptibly();
			
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
