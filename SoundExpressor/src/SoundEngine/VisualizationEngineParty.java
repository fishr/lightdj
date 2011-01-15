package SoundEngine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import javax.sound.sampled.AudioFormat;


import Arduino.LEDVisualizer;
import Arduino.RelayVisuals;
import Common.ColorOutput;
import Common.FeatureList;
import FeatureDetectors.BassFinder;
import FeatureDetectors.ClapFinder;
import FeatureDetectors.FeatureDetector;
import FeatureDetectors.FrequencyRangeFinder;
import FeatureDetectors.LevelMeter;
import FeatureDetectors.SharpClapFinder;
import FeatureDetectors.SilenceFinder;
import FeatureDetectors.VocalsFinder;
import GenreClassifier.NaiveBayesClassifier;
import GenreClassifier.SongFeatureVector;
import LightDJGUI.LightDJGUI;
import LightDJGUI.ScrollingSpectrum;
import PartyLightsController.PartyLightsController;
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
import Visualizors.ColorGenerator;
import Visualizors.CrazyStrobe;
import Visualizors.HueRotator;
import Visualizors.RedBassColoredClapVisualizer;
import Visualizors.VUBass;
import Visualizors.Visualizer;

/**
 * This class is responsible for music visualizations for Steve's LED's.
 * @author Steve Levine
 */
public class VisualizationEngineParty extends VisualizationEngine {

	// Visualization stuff
	LightDJGUI gui;
	GraphDisplay graphMapper;
	ScrollingChannel channelMapper;
	ChannelLights lights;
	ColoredLight bassLight;
	RGBLight rgbLight;
	TextLight textLight;
	RealtimePlotter plotter;

	// The list of feature detectors
	protected ArrayList<FeatureDetector> featureDetectors;
	
	// The list of visualizers
	protected ArrayList<Visualizer> visualizers;
	
	
	// The arduino LED visualizer
	//LEDVisualizer ledVisuals;
	PartyLightsController ledVisuals;
	
	TimerTicToc tictoc;
	

	public VisualizationEngineParty(AudioFormat format, double videoDelaySec) {
		super(format, videoDelaySec);
		tictoc = new TimerTicToc();
	}
	
	@Override
	protected void initVisualizations() {

		// Set up the feature detector plugins
		featureDetectors = allFeatureDetectors();
		for(FeatureDetector f : featureDetectors) {
			f.init();
			
			// Get the list of controls requested
			
		}
		
		
		// Set up all of the visualizer plugins
		visualizers = allVisualizers();
		for(Visualizer v : visualizers) {
			v.init();
			
			// Get the list of controls requested
			
		}
		
		
		try {
			//ledVisuals = new LEDVisualizer();
			ledVisuals = new PartyLightsController();
		} catch (Throwable o) {
			System.out.println("WARNING: Couldn't connect to LED's via USB!");
		}
		
		
		
		// Set up the GUI
		gui = LightDJGUI.startGUI(BUFFER_SIZE, SAMPLE_RATE);
		Graphics2D g2D = (Graphics2D) gui.getGraphics(); 
		
		// Divide up the GUI into different useful stuff.
		graphMapper = new GraphDisplay(30, 30, 700, 350, (Graphics2D) g2D);
		channelMapper = new ScrollingChannel(30, 750, 500, 200, (Graphics2D) g2D);
		bassLight = new ColoredLight(Color.RED, 150, 750, 30, 150, 150, (Graphics2D) g2D);
		rgbLight = new RGBLight(150, 920, 30, 150, 150, (Graphics2D) g2D);
		plotter = new RealtimePlotter(new Color[]{Color.RED, Color.GREEN}, 605, 400, 450, 300, 200.0, (Graphics2D) g2D);
		textLight = new TextLight(100, 100, 300, 100, g2D);
		//plotter = new RealtimePlotter(new Color[]{Color.RED}, 605, 400, 450, 300, 200.0, (Graphics2D) gui.getGraphics());
		
		
		
		
	}
	
	/**
	 * Return the comprehensive list of all FeatureDetectors. Must be added here to show up in the LightDJ GUI.
	 * @return
	 */
	public ArrayList<FeatureDetector> allFeatureDetectors() {
		ArrayList<FeatureDetector> detectors = new ArrayList<FeatureDetector>();
		
		int FFT_SIZE = BUFFER_SIZE;
		double UPDATES_PER_SECOND = 1.0 * SAMPLE_RATE / FFT_SIZE * BUFFER_OVERLAP; 
		
		// Add the detectors here
		detectors.add(new BassFinder(FFT_SIZE, UPDATES_PER_SECOND));
		detectors.add(new ClapFinder(FFT_SIZE, UPDATES_PER_SECOND));
		detectors.add(new FrequencyRangeFinder(FFT_SIZE, UPDATES_PER_SECOND));
		detectors.add(new LevelMeter(FFT_SIZE, UPDATES_PER_SECOND));
		
		
		// Return them all
		return detectors;
		
	}
	
	/**
	 * Return the comprehensive list of all FeatureDetectors. Must be added here to show up in the LightDJ GUI.
	 * @return
	 */
	public ArrayList<Visualizer> allVisualizers() {
		ArrayList<Visualizer> visualizers = new ArrayList<Visualizer>();
		
		int FFT_SIZE = BUFFER_SIZE;
		double UPDATES_PER_SECOND = 1.0 * SAMPLE_RATE / FFT_SIZE * BUFFER_OVERLAP; 
		
		// Add the detectors here
		//visualizers.add(new VUBass(FFT_SIZE, UPDATES_PER_SECOND));
		//visualizers.add(new RedBassColoredClapVisualizer(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new CrazyStrobe(FFT_SIZE, UPDATES_PER_SECOND));
		
		return visualizers;
		
	}

	
	
	@Override
	protected RenderFrame computeVisualsRendering(FFT fft) {
		
		// Create a featurelist, and pass it along with the FFT to each FeatureDetector
		FeatureList featureList = new FeatureList();
		double[] frequencies = fft.getFrequencies();
		double[] magnitudes = fft.getMagnitudes();
		
		// Compute all of the features
		for(FeatureDetector f : featureDetectors) {
			f.computeFeatures(frequencies, magnitudes, featureList);
		}
		
		// Now that we have a full-fledged FeatureList, pass it to the Visualizers
		ColorOutput[] colorOutputs = new ColorOutput[visualizers.size()];
		for(int i = 0; i < visualizers.size(); i++) {
			Visualizer v = visualizers.get(i);
			ColorOutput c = v.visualize(featureList);
			colorOutputs[i] = c;
		}
		
		
		RenderFrameParty renderFrame = new RenderFrameParty();
		renderFrame.colorOutputs = colorOutputs;
		
		
		
		//plotter.update(new double[] {100.0*((Double) featureList.getFeature("BASS_RAW")), 0.0});
		
		
		//plotter.render();
		
		return renderFrame;
	}

	@Override
	protected void renderVisuals(RenderFrame rf) {
		
		RenderFrameParty renderFrame = (RenderFrameParty) rf;

		
		
		// Update LED lights
		//System.out.println(renderFrame.colorOutputs[0].rgbLights[0]);
		
		//ledVisuals.visualize(renderFrame.colorOutputs[0]);	// Send SERIAL to the RGB's
		bassLight.update(renderFrame.colorOutputs[0].rgbLights[0].getRed() / 255.0);
		rgbLight.update(renderFrame.colorOutputs[0].rgbLights[1]);
		//channelMapper.updateWithNewChannelColors(new Color[]{bassLight.getCurrentColor(), rgbLight.getCurrentColor()});	// Update the scrolling "rock band" display
		plotter.render();
		
	
	}
	
	/**
	 * Takes in the current color frame to be rendered. Retrieves the current cross-fade mixing
	 * parameter from the LightingDJ GUI, and mixes the appropriate ColorOutputs together to yield
	 * one color output.
	 */
	protected ColorOutput mixColors(RenderFrameParty rf) {
		
		// For now, just return the first.
		return rf.colorOutputs[0];
		
		
	}
	

}


class RenderFrameParty extends RenderFrame {

	ColorOutput[] colorOutputs;
	
}
