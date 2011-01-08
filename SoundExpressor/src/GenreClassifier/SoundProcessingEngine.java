package GenreClassifier;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.sound.sampled.AudioFormat;

import FeatureDetectors.BassFinder;
import FeatureDetectors.ClapFinder;
import FeatureDetectors.FrequencyRangeFinder;
import FeatureDetectors.LevelMeter;
import FeatureDetectors.VocalsFinder;
import SignalGUI.ChannelLights;
import SignalGUI.ColoredLight;
import SignalGUI.DistributionPlotter;
import SignalGUI.GUIVisualizer;
import SignalGUI.GraphDisplay;
import SignalGUI.RGBLight;
import SignalGUI.RealtimePlotter;
import SignalGUI.ScrollingChannel;
import SignalGUI.ScrollingSpectrum;
import Signals.FFT;
import Signals.FFTEngine;
import SoundEngine.RhythmMeter;
import Utils.TimerTicToc;

/**
 * This class is responsible for music visualizations.
 * @author Steve Levine
 */
public class SoundProcessingEngine {

	private double[][] buffers;
	private int[] bufferCursors;
	private final int BUFFER_SIZE = 2048;
	private final int BUFFER_OVERLAP = 8;	// Must be a power of 2
	
	// The audio format of data being written in
	private final int FRAME_SIZE;
	private final int BYTES_PER_SAMPLE;
	private final long MAX_SAMPLE_VAL;
	private final int SAMPLE_RATE;
	
	
	// For multi-channel audio (ex., stereo), only use this channel for processing
	private final int CHANNEL_TO_PROCESS = 0;
	
	// Visualization stuff
	GUIVisualizer gui;
	DistributionPlotter bassDist;
	DistributionPlotter midsDist;
	DistributionPlotter highsDist;
	DistributionPlotter levelDist;
	DistributionPlotter rhythmDist;
	
	// State machines that output channel values
	int numChannels = 4;
	BassFinder bassFinder;
	FrequencyRangeFinder midsFinder;
	FrequencyRangeFinder highsFinder;
	VocalsFinder vocalsFinder;
	ClapFinder clapFinder;
	LevelMeter levelMeter;
	RhythmMeter rhythmMeter;
	
	// Used for profiling and debugging
	TimerTicToc timer;
	
	// The FFT engine
	FFTEngine fftEngine;
	
	
	public SoundProcessingEngine(AudioFormat format) {
		
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
		//graphMapper = new GraphDisplay(30, 30, 700, 350, (Graphics2D) gui.getGraphics());
		//spectrumMapper = new ScrollingSpectrum(30, 400, 500, 300, (Graphics2D) gui.getGraphics());
		//channelMapper = new ScrollingChannel(30, 750, 500, 200, (Graphics2D) gui.getGraphics());
		//bassLight = new ColoredLight(Color.RED, 150, 750, 30, 150, 150, (Graphics2D) gui.getGraphics());
		//rgbLight = new RGBLight(150, 920, 30, 150, 150, (Graphics2D) gui.getGraphics());
		//plotter = new RealtimePlotter(new Color[]{Color.RED, Color.GREEN}, 605, 400, 450, 300, 200.0, (Graphics2D) gui.getGraphics());
		Graphics2D g2D = (Graphics2D) gui.getGraphics(); //null;
		
		bassDist = new DistributionPlotter(Color.GREEN, "Bass Distribution", 5, 5, 500, 300, 75.0, 300.0, g2D);
		midsDist = new DistributionPlotter(Color.GREEN, "Mids Distribution", 510, 5, 500, 300, 10.0, 40.0, g2D);
		highsDist = new DistributionPlotter(Color.BLUE, "Highs Distribution", 5, 315, 500, 300, 3.0, 12.0, g2D);
		levelDist = new DistributionPlotter(Color.GREEN, "Level Distribution", 510, 315, 500, 300, 10.0, 40.0, g2D);
		rhythmDist = new DistributionPlotter(Color.YELLOW, "Rhythm", 5, 625, 500, 300, 4.0, 400.0, g2D);
		
		// Start some state machines
		bassFinder = new BassFinder(SAMPLE_RATE, BUFFER_SIZE);
		//midsFinder = new FrequencyRangeFinder(SAMPLE_RATE, BUFFER_SIZE, 200.0, 2000.0);
		//highsFinder = new FrequencyRangeFinder(SAMPLE_RATE, BUFFER_SIZE, 6000.0, 20000.0);
		levelMeter = new LevelMeter(SAMPLE_RATE, BUFFER_SIZE);
		rhythmMeter = new RhythmMeter(SAMPLE_RATE, BUFFER_SIZE);
		//clapFinder = new ClapFinder(SAMPLE_RATE, BUFFER_SIZE);
		//vocalsFinder = new VocalsFinder(SAMPLE_RATE, BUFFER_SIZE);
		
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
		updateVisuals(fft);
	
	}
	
	
	public void updateVisuals(FFT fft) {
		
		// Obtain the frequencies and corresponding magnitudes of the FFT
		double[] frequencies = fft.getFrequencies();
		double[] magnitudes = fft.getMagnitudes();
		
		// Compute useful values from the FFT data
		double bassLevel = bassFinder.getFreqs(frequencies, magnitudes);
		double midsLevel = midsFinder.getFreqs(frequencies, magnitudes);
		double highsLevel = highsFinder.getFreqs(frequencies, magnitudes);
		double level = levelMeter.getLevel(frequencies, magnitudes);
		boolean rhythmBeat = rhythmMeter.update(bassLevel);
		//double vocalsLevel = vocalsFinder.getFreqs(frequencies, magnitudes);
		//double clapLevel = clapFinder.getFreqs(frequencies, magnitudes);
		
		
		
		// Update LED lights
		//ledVisuals.visualize(channels);					// Send SERIAL to the RGB's
		//bassLight.update(bassLevel);
		//channelMapper.updateWithNewChannelColors(new Color[]{bassLight.getCurrentColor(), rgbLight.getCurrentColor()});	// Update the scrolling "rock band" display
		//plotter.update(new double[] {bassFinder.getCurrentLevel(), bassFinder.getAveragedLevel(), bassFinder.getAveragedLevel() + bassFinder.getAveragedSpread(), bassFinder.getAveragedLevel() - bassFinder.getAveragedSpread(), bassFinder.getBassDelta()});
		
		//plotter.update(new double[] {bassFinder.getBassDelta(), 100.0 * bassLevel});
		bassDist.update(bassFinder.getCurrentLevel(), bassFinder.getCurrentLevel());
		midsDist.update(midsLevel, 1.0);
		highsDist.update(highsLevel, 1.0);
		levelDist.update(level, 1.0);
		if (rhythmBeat) {
			rhythmDist.update(rhythmMeter.getDeltaTime(), 1.0);
		}
		
		// Draw a live spectrum, and a time-history version of the spectrum
		//graphMapper.drawPositiveLogHalfX(frequencies, magnitudes, null, 30, 20000, 300);
		//spectrumMapper.updateWithNewSpectrum(frequencies, magnitudes, 30, 20000, 100);
		

	}

	public SongFeatureVector finish() {
	
		bassDist.render();
		midsDist.render();
		highsDist.render();
		levelDist.render();
		rhythmDist.render();
		
		// Assemble a feature vector and return it!
		SongFeatureVector vector = new SongFeatureVector();
		
		
		vector.bassMax = bassDist.getMax();
		vector.bassAve = bassDist.getAverageVal();
		vector.bassSpread = bassDist.getAverageSpread();
		vector.bassPeak = bassDist.getPeak();
		vector.bass0 = bassDist.getNormalizedBin(0);
		vector.bass1 = bassDist.getNormalizedBin(1);
		vector.bass2 = bassDist.getNormalizedBin(2);
		vector.bass3 = bassDist.getNormalizedBin(3);
		
		vector.midsMax = midsDist.getMax();
		vector.midsAve = midsDist.getAverageVal();
		vector.midsSpread = midsDist.getAverageSpread();
		vector.midsPeak = midsDist.getPeak();
		vector.mids0 = midsDist.getNormalizedBin(0);
		vector.mids1 = midsDist.getNormalizedBin(1);
		vector.mids2 = midsDist.getNormalizedBin(2);
		vector.mids3 = midsDist.getNormalizedBin(3);
		
		vector.highsMax = highsDist.getMax();
		vector.highsAve = highsDist.getAverageVal();
		vector.highsSpread = highsDist.getAverageSpread();
		vector.highsPeak = highsDist.getPeak();
		vector.highs0 = highsDist.getNormalizedBin(0);
		vector.highs1 = highsDist.getNormalizedBin(1);
		vector.highs2 = highsDist.getNormalizedBin(2);
		vector.highs3 = highsDist.getNormalizedBin(3);
		
		vector.levelMax = levelDist.getMax();
		vector.levelAve = levelDist.getAverageVal();
		vector.levelSpread = levelDist.getAverageSpread();
		vector.levelPeak = levelDist.getPeak();
		vector.level0 = levelDist.getNormalizedBin(0);
		vector.level1 = levelDist.getNormalizedBin(1);
		vector.level2 = levelDist.getNormalizedBin(2);
		vector.level3 = levelDist.getNormalizedBin(3);

		vector.rhythmAve = rhythmDist.getAverageVal();
		vector.rhythmPeak = rhythmDist.getPeak();
		
		
		
		return vector;
		
	}
	
	
}


