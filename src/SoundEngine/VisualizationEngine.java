package SoundEngine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.concurrent.Semaphore;
import javax.sound.sampled.AudioFormat;


import Arduino.LEDVisualizer;
import SignalGUI.ChannelLights;
import SignalGUI.ColoredLight;
import SignalGUI.GUIVisualizer;
import SignalGUI.GraphDisplay;
import SignalGUI.RGBLight;
import SignalGUI.RealtimePlotter;
import SignalGUI.ScrollingChannel;
import SignalGUI.ScrollingSpectrum;
import Signals.FFT;
import Signals.FFTEngine;
import Utils.TimerTicToc;

/**
 * This class is responsible for music visualizations.
 *
 */
public class VisualizationEngine {

	private double[][] buffers;
	private int[] bufferCursors;
	private final int BUFFER_SIZE = 1024;
	private final int BUFFER_OVERLAP = 1;	// Must be a power of 2
	
	// The audio format of data being written in
	private final int FRAME_SIZE;
	private final int BYTES_PER_SAMPLE;
	private final long MAX_SAMPLE_VAL;
	private final int SAMPLE_RATE;
	
	
	// For multi-channel audio (ex., stereo), only use this channel for processing
	private final int CHANNEL_TO_PROCESS = 0;
	
	// Visualization stuff
	GUIVisualizer gui;
	GraphDisplay graphMapper;
	ScrollingSpectrum spectrumMapper;
	ScrollingChannel channelMapper;
	ChannelLights lights;
	ColoredLight bassLight;
	RGBLight rgbLight;
	RealtimePlotter plotter;
	
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
		
		// Divide up the GUI into different useful stuff.
		graphMapper = new GraphDisplay(30, 30, 700, 350, (Graphics2D) gui.getGraphics());
		spectrumMapper = new ScrollingSpectrum(30, 400, 500, 300, (Graphics2D) gui.getGraphics());
		channelMapper = new ScrollingChannel(30, 750, 500, 200, (Graphics2D) gui.getGraphics());
		bassLight = new ColoredLight(Color.RED, 75, 750, 30, 75, 100, (Graphics2D) gui.getGraphics());
		rgbLight = new RGBLight(75, 840, 30, 75, 100, (Graphics2D) gui.getGraphics());
		plotter = new RealtimePlotter(new Color[]{Color.RED, Color.YELLOW}, 560, 400, 450, 300, 100.0, (Graphics2D) gui.getGraphics());
		
		// Start some state machines
		bassFinder = new BassFinder(SAMPLE_RATE, BUFFER_SIZE);
		clapFinder = new ClapFinder(SAMPLE_RATE, BUFFER_SIZE);
		vocalsFinder = new VocalsFinder(SAMPLE_RATE, BUFFER_SIZE);
		
		// Start an RGB controller with some colors
		//rgbController = new RGBGradientController(Color.BLUE, Color.RED);
		rgbController = new HueRotator(0.0, 0.373);
		
		ledVisuals = new LEDVisualizer();
		
		
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
		
		// Obtain the frequencies and corresponding magnitudes of the FFT
		double[] frequencies = fft.getFrequencies();
		double[] magnitudes = fft.getMagnitudes();
		
		// Compute useful values from the FFT data
		double bassLevel = bassFinder.getFreqs(frequencies, magnitudes);
		double vocalsLevel = vocalsFinder.getFreqs(frequencies, magnitudes);
		double clapLevel = clapFinder.getFreqs(frequencies, magnitudes);
		
		// Compute the color to use for the RGB lights
		rgbController.step(clapLevel);
		
		// Compute some channel values
		double[] channels = new double[numChannels];
		channels[0] = bassLevel;
		channels[1] = rgbController.getRed();
		channels[2] = rgbController.getGreen();
		channels[3] = rgbController.getBlue();
		
		// Update LED lights
		//ledVisuals.visualize(channels);					// Send SERIAL to the RGB's
		//lights.updateWithNewChannelVals(channels);			// Update the lights
		bassLight.update(bassLevel);
		rgbLight.update(rgbController.getColor());
		channelMapper.updateWithNewChannelColors(new Color[]{bassLight.getCurrentColor(), rgbLight.getCurrentColor()});	// Update the scrolling "rock band" display
		plotter.update(new double[] {bassFinder.getCurrentLevel(), bassFinder.getAveragedLevel()});
		
		// Draw a live spectrum, and a time-history version of the spectrum
		graphMapper.drawPositiveLogHalfX(frequencies, magnitudes, null, 30, 20000, 200);
		spectrumMapper.updateWithNewSpectrum(frequencies, magnitudes, 30, 20000, 100);
		

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
