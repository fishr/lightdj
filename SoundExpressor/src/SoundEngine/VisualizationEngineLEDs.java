package SoundEngine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import javax.sound.sampled.AudioFormat;


import Arduino.LEDVisualizer;
import Arduino.RelayVisuals;
import FeatureDetectors.BassFinder;
import FeatureDetectors.ClapFinder;
import FeatureDetectors.FrequencyRangeFinder;
import FeatureDetectors.LevelMeter;
import FeatureDetectors.SharpClapFinder;
import FeatureDetectors.SilenceFinder;
import FeatureDetectors.VocalsFinder;
import GenreClassifier.NaiveBayesClassifier;
import GenreClassifier.SongFeatureVector;
import SignalGUI.ChannelLights;
import SignalGUI.ColoredLight;
import SignalGUI.DistributionPlotter;
import SignalGUI.GUIVisualizer;
import SignalGUI.GraphDisplay;
import SignalGUI.RGBLight;
import SignalGUI.RealtimePlotter;
import SignalGUI.ScrollingChannel;
import SignalGUI.ScrollingSpectrum;
import SignalGUI.TextLight;
import Signals.FFT;
import Signals.FFTEngine;
import Utils.TimerTicToc;

/**
 * This class is responsible for music visualizations.
 *
 */
public class VisualizationEngineLEDs extends VisualizationEngine {

	// Visualization stuff
	GUIVisualizer gui;
	GraphDisplay graphMapper;
	ScrollingSpectrum spectrumMapper;
	ScrollingChannel channelMapper;
	ChannelLights lights;
	ColoredLight bassLight;
	RGBLight rgbLight;
	TextLight textLight;
	RealtimePlotter plotter;

	
	// State machines that output channel values
	int numChannels = 4;
	BassFinder bassFinder;
	VocalsFinder vocalsFinder;
	ClapFinder clapFinder;
	LevelMeter levelMeter;
	FrequencyRangeFinder midsFinder;
	FrequencyRangeFinder highsFinder;
	RhythmMeter rhythmMeter;
	SilenceFinder silenceFinder;
	SharpClapFinder sharpClapFinder;
	
	DistributionPlotter bassDist;
	DistributionPlotter midsDist;
	DistributionPlotter highsDist;
	DistributionPlotter levelDist;
	DistributionPlotter rhythmDist;

	NaiveBayesClassifier genreClassifier;
	Map<Integer, String> genreNames;
	
	// Color controllers
	ColorGenerator rgbController;
	
	// The arduino LED visualizer
	LEDVisualizer ledVisuals;
	
	TimerTicToc tictoc;
	

	public VisualizationEngineLEDs(AudioFormat format, double videoDelaySec) {
		super(format, videoDelaySec);
		tictoc = new TimerTicToc();
	}
	
	@Override
	protected void initVisualizations() {
		
		// Set up the GUI
		gui = GUIVisualizer.makeGUI();
		Graphics2D g2D = (Graphics2D) gui.getGraphics(); //null
		
		// Divide up the GUI into different useful stuff.
		graphMapper = new GraphDisplay(30, 30, 700, 350, (Graphics2D) g2D);
		spectrumMapper = new ScrollingSpectrum(30, 400, 500, 300, g2D);
		channelMapper = new ScrollingChannel(30, 750, 500, 200, (Graphics2D) g2D);
		bassLight = new ColoredLight(Color.RED, 150, 750, 30, 150, 150, (Graphics2D) g2D);
		rgbLight = new RGBLight(150, 920, 30, 150, 150, (Graphics2D) g2D);
		plotter = new RealtimePlotter(new Color[]{Color.RED, Color.GREEN}, 605, 400, 450, 300, 200.0, (Graphics2D) g2D);
		textLight = new TextLight(100, 100, 300, 100, g2D);
		//plotter = new RealtimePlotter(new Color[]{Color.RED}, 605, 400, 450, 300, 200.0, (Graphics2D) gui.getGraphics());
		
		
		// Start some state machines
		bassFinder = new BassFinder(SAMPLE_RATE, BUFFER_SIZE);
		clapFinder = new ClapFinder(SAMPLE_RATE, BUFFER_SIZE);
		//vocalsFinder = new VocalsFinder(SAMPLE_RATE, BUFFER_SIZE);
		midsFinder = new FrequencyRangeFinder(SAMPLE_RATE, BUFFER_SIZE, 200.0, 2000.0);
		highsFinder = new FrequencyRangeFinder(SAMPLE_RATE, BUFFER_SIZE, 6000.0, 20000.0);
		levelMeter = new LevelMeter(SAMPLE_RATE, BUFFER_SIZE);
		rhythmMeter = new RhythmMeter(SAMPLE_RATE, BUFFER_SIZE);
		silenceFinder = new SilenceFinder(Math.round(0.5 * SAMPLE_RATE * BUFFER_OVERLAP / BUFFER_SIZE));
		sharpClapFinder = new SharpClapFinder(SAMPLE_RATE, BUFFER_SIZE);
		
		bassDist = new DistributionPlotter(Color.GREEN, "Bass Distribution", 5, 5, 500, 300, 75.0, 300.0, g2D);
		midsDist = new DistributionPlotter(Color.GREEN, "Mids Distribution", 510, 5, 500, 300, 10.0, 40.0, g2D);
		highsDist = new DistributionPlotter(Color.BLUE, "Highs Distribution", 5, 315, 500, 300, 3.0, 12.0, g2D);
		levelDist = new DistributionPlotter(Color.GREEN, "Level Distribution", 510, 315, 500, 300, 10.0, 40.0, g2D);
		rhythmDist = new DistributionPlotter(Color.YELLOW, "Rhythm", 5, 625, 500, 300, 4.0, 400.0, g2D);
		
		
		genreClassifier = new NaiveBayesClassifier("/home/steve/Desktop/Music/naive_bayes_model.dat");
		genreNames = new HashMap<Integer, String>();
		genreNames.put(1, "Electronic");
		genreNames.put(2, "Rock");
		genreNames.put(3, "Classical");
		genreNames.put(4, "Jazz");
		genreNames.put(5, "Hip Hop");
		
		
		
		// Start an RGB controller with some colors
		//rgbController = new RGBGradientController(Color.BLUE, Color.RED);
		rgbController = new HueRotator(0.0, 0.373);
		
		try {
			ledVisuals = new LEDVisualizer();
		} catch (Throwable o) {
			System.out.println("WARNING: Couldn't connect to LED's via USB!");
		}
	}
	
	


	@Override
	protected RenderFrame computeVisualsRendering(FFT fft) {
		
		double[] frequencies = fft.getFrequencies();
		double[] magnitudes = fft.getMagnitudes();
		
		// Compute useful values from the FFT data
		double bassLevel = bassFinder.getFreqs(frequencies, magnitudes);
		double midsLevel = midsFinder.getFreqs(frequencies, magnitudes);
		double highsLevel = highsFinder.getFreqs(frequencies, magnitudes);
		//double vocalsLevel = vocalsFinder.getFreqs(frequencies, magnitudes);
		double clapLevel = clapFinder.getFreqs(frequencies, magnitudes);
		double sharpClapLevel = sharpClapFinder.getFreqs(frequencies, magnitudes);
		double level = levelMeter.getLevel(frequencies, magnitudes);
		boolean bassTimeDeltaReady = rhythmMeter.update(bassLevel);
		boolean silent = silenceFinder.update(level);
		
		// If necessary, reset the distributions
		if (silent) {
			bassDist.reset();
			midsDist.reset();
			highsDist.reset();
			levelDist.reset();
			rhythmDist.reset();
		}

		bassDist.update(bassFinder.getCurrentLevel(), bassFinder.getCurrentLevel());
		midsDist.update(midsLevel, 1.0);
		highsDist.update(highsLevel, 1.0);
		levelDist.update(level, 1.0);
		if (bassTimeDeltaReady) {
			rhythmDist.update(rhythmMeter.getDeltaTime(), 1.0);
		}
		
		// Compute the color to use for the RGB lights
		rgbController.step(clapLevel);
		//rgbController.step(sharpClapLevel);
		
		// Compute some channel values
		double[] channels = new double[numChannels];
		channels[0] = bassLevel;
		channels[1] = rgbController.getRed();
		channels[2] = rgbController.getGreen();
		channels[3] = rgbController.getBlue();
		
		//plotter.update(new double[] {bassFinder.getCurrentLevel(), bassFinder.getAveragedLevel(), bassFinder.getAveragedLevel() + bassFinder.getAveragedSpread(), bassFinder.getAveragedLevel() - bassFinder.getAveragedSpread(), bassFinder.getBassDelta()});
		plotter.update(new double[] {bassFinder.getCurrentLevel(), 30.0 * sharpClapFinder.getCurrentLevel()});
		
		
		RenderFrameLEDs renderFrame = new RenderFrameLEDs();
		renderFrame.channels = channels;
		renderFrame.rgb = rgbController.getColor();
		renderFrame.silent = silent;
		
		return renderFrame;
	}

	@Override
	protected void renderVisuals(RenderFrame rf) {
		//tictoc.tic();
		
		
		RenderFrameLEDs renderFrame = (RenderFrameLEDs) rf;

		
		// Obtain the frequencies and corresponding magnitudes of the FFT

		
		// Update LED lights	
		//ledVisuals.visualize(renderFrame.channels);	// Send SERIAL to the RGB's
		bassLight.update(renderFrame.channels[0]);
		rgbLight.update(renderFrame.rgb);
		//channelMapper.updateWithNewChannelColors(new Color[]{bassLight.getCurrentColor(), rgbLight.getCurrentColor()});	// Update the scrolling "rock band" display
		plotter.render();
		//plotter.update(new double[] {bassFinder.getCurrentLevel(), 100.0 * bassLevel});
		//plotter.update(new double[] {100.0*bassLevel, 100.0 * clapLevel});
		//plotter.update(new double[]{level});
		//bassDist.update(bassFinder.getCurrentLevel());
		

		
//		
//		if (cycle_clock % 100 == 0) {
//			if (!silent) {
//				SongFeatureVector vector = assembleFeatureVector();
//				int y_hat = genreClassifier.classify(vector);
//				textLight.update(genreNames.get(y_hat));
//			} else {
//				textLight.update("");
//			}
//			
//			bassDist.render();
//			midsDist.render();
//			highsDist.render();
//			levelDist.render();
//			rhythmDist.render();
//		}
//		cycle_clock++;
		
		
		// Draw a live spectrum, and a time-history version of the spectrum
		//graphMapper.drawPositiveLogHalfX(frequencies, magnitudes, null, 30, 20000, 300);
		//spectrumMapper.updateWithNewSpectrum(frequencies, magnitudes, 30, 20000, 100);
		
		//tictoc.toc();
		//System.out.println("Render rate: " + tictoc.getNumCallsPerSecond() + ", time: " + tictoc.getAverageTime());
	}
	
	
	private SongFeatureVector assembleFeatureVector() {
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


class RenderFrameLEDs extends RenderFrame {

	double[] channels;
	Color rgb;
	boolean silent;
	
}
