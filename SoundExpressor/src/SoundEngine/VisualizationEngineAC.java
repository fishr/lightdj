package SoundEngine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import javax.sound.sampled.AudioFormat;


import Arduino.LEDVisualizer;
import Arduino.RelayVisuals;
import FeatureDetectors.BassFinder;
import FeatureDetectors.SharpClapFinder;
import FeatureDetectors.SilenceFinder;
import GenreClassifier.NaiveBayesClassifier;
import GenreClassifier.SongFeatureVector;
import LightDJGUI.ScrollingSpectrum;
import SignalGUI.ChannelLights;
import SignalGUI.ColoredLight;
import SignalGUI.DistributionPlotter;
import SignalGUI.GUIVisualizer;
import SignalGUI.GraphDisplay;
import SignalGUI.RGBLight;
import SignalGUI.RealtimePlotter;
import SignalGUI.ScrollingChannel;
import SignalGUI.TextLight;
import Signals.FFT;
import Signals.FFTEngine;
import Utils.TimerTicToc;

/**
 * This class is responsible for music visualizations for AC relays.
 * @author Steve Levine
 */
public class VisualizationEngineAC extends VisualizationEngine {
	
	// Visualization stuff
	GUIVisualizer gui;
	GraphDisplay graphMapper;
	ScrollingSpectrum spectrumMapper;
	ScrollingChannel channelMapper;
	ChannelLights lights;
	ColoredLight bassLight;
	ColoredLight highsLight;
	TextLight textLight;
	RealtimePlotter plotter;

	
	// State machines that output channel values
	int numChannels = 6;
	BassFinder bassFinder;
	SilenceFinder silenceFinder;
	SharpClapFinder sharpClapFinder;
	
	// Alternator state machine
	SignalMultiplexer signalMux;
	
	// The arduino LED visualizer
	//LEDVisualizer ledVisuals;
	RelayVisuals ledVisuals;
	
	// Add hysteresis
	boolean bassHigh = false;
	
	public VisualizationEngineAC(AudioFormat format, double videoDelaySec) {
		super(format, videoDelaySec);
	}
	
	@Override
	protected void initVisualizations() {
		
		// Set up the GUI
		gui = GUIVisualizer.makeGUI();
		Graphics2D g2D = (Graphics2D) gui.getGraphics(); //null
		
		// Divide up the GUI into different useful stuff.
		graphMapper = new GraphDisplay(30, 30, 700, 350, (Graphics2D) g2D);
		spectrumMapper = new ScrollingSpectrum(30, 400, 500, 300, g2D, 30, 20000, 100, BUFFER_SIZE,  SAMPLE_RATE);
		channelMapper = new ScrollingChannel(30, 750, 500, 200, (Graphics2D) g2D);
		bassLight = new ColoredLight(Color.RED, 150, 750, 30, 150, 150, (Graphics2D) g2D);
		highsLight = new ColoredLight(Color.GREEN, 150, 920, 30, 150, 150, (Graphics2D) g2D);
		plotter = new RealtimePlotter(new Color[]{Color.RED, Color.GREEN}, 605, 400, 450, 300, 200.0, (Graphics2D) g2D);
		textLight = new TextLight(100, 100, 300, 100, g2D);
		//plotter = new RealtimePlotter(new Color[]{Color.RED}, 605, 400, 450, 300, 200.0, (Graphics2D) gui.getGraphics());
		
		
		// Start some state machines
		bassFinder = new BassFinder(SAMPLE_RATE, BUFFER_SIZE);
		silenceFinder = new SilenceFinder(Math.round(0.5 * SAMPLE_RATE * BUFFER_OVERLAP / BUFFER_SIZE));
		sharpClapFinder = new SharpClapFinder(SAMPLE_RATE, BUFFER_SIZE);
		
		// INitialize them
		bassFinder.init();
		silenceFinder.init();
		sharpClapFinder.init();
		
		// Signal multiplexer
		signalMux = new SignalMultiplexer(3);
		
		try {
			ledVisuals = new RelayVisuals();
		} catch (Throwable o) {
			System.out.println("WARNING: Couldn't connect to LED's via USB!");
		}
		
	}
	
	/**
	 * Compute a frame to be rendered at the appropriate time
	 */
	@Override
	protected RenderFrame computeVisualsRendering(FFT fft) {
		
		// Obtain the frequencies and corresponding magnitudes of the FFT
		double[] frequencies = fft.getFrequencies();
		double[] magnitudes = fft.getMagnitudes();
		
		// Compute useful values from the FFT data
		double bassLevel = bassFinder.getFreqs(frequencies, magnitudes);
		double sharpClapLevel = sharpClapFinder.getFreqs(frequencies, magnitudes);
		
		// Add some hysteresis to the bass to redice flicker!
		if (bassHigh == true) {
			if (bassLevel < 0.4) {
				bassHigh = false;
				bassLevel = 0.0;
			} else {
				bassLevel = 1.0;
			}
		} else {
			if (bassLevel > 0.6) {
				bassHigh = true;
				bassLevel = 1.0;
			} else {
				bassLevel = 0.0;
			}
		}
		
		signalMux.update(bassLevel);
		
		// Compute some channel values
		double[] channels = new double[numChannels];
		channels[0] = bassLevel;
		channels[1] = bassLevel; //sharpClapLevel;
		channels[2] = bassLevel;//sharpClapLevel;
		channels[3] = signalMux.getChannelValue(0);
		channels[4] = signalMux.getChannelValue(1);
		channels[5] = signalMux.getChannelValue(2);
		
		// Create and return a render frame. Assume that the timestamp and framewidth will be set elsewhere.
		RenderFrameAC renderFrame = new RenderFrameAC();
		renderFrame.channels = channels;
		return renderFrame;

	}
	
	
	/**
	 * Actually render the previously computed frame
	 */
	@Override
	protected void renderVisuals(RenderFrame rf) {
		RenderFrameAC renderFrame = (RenderFrameAC) rf;
		
		// Update LED lights	
		ledVisuals.visualize(renderFrame.channels);					// Send SERIAL to the RGB's
		bassLight.update(renderFrame.channels[0]);
		highsLight.update(renderFrame.channels[1]);
	}
	
}

class RenderFrameAC extends RenderFrame {
	// Rendering details
	protected double[] channels;
}



