package SoundEngine;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.sampled.AudioFormat;
import javax.swing.JFrame;

import Common.ColorOutput;
import Common.FeatureList;
import Common.UserControl;
import FeatureDetectors.BassFinder;
import FeatureDetectors.ClapFinder;
import FeatureDetectors.FeatureDetector;
import FeatureDetectors.FrequencyRangeFinder;
import FeatureDetectors.LevelMeter;
import FeatureDetectors.SharpClapFinder;
import LightDJGUI.ColorOutputDisplayer;
import LightDJGUI.ColorOutputDisplayerPanel;
import LightDJGUI.ColorOutputDisplayerParty;
import LightDJGUI.ConfigFileParser;
import LightDJGUI.CrossfaderKnob;
import LightDJGUI.IndicatorLight;
import LightDJGUI.LightDJGUI;
import LightDJGUI.MouseAcceptorPanel;
import LightDJGUI.PulseKeeper;
import LightDJGUI.ScrollingSpectrum;
import LightDJGUI.VisualizerChooser;
import MidiInterface.MidiConnector;
import PartyLightsController.PartyLightsController8;
import PartyLightsController.PartyLightsController16;
import PostProcessors.Blackout;
import PostProcessors.LightVolume;
import PostProcessors.PostProcessor;
import PostProcessors.Strobes;
import PostProcessors.WhiteBurst;
import SignalGUI.ChannelLights;
import SignalGUI.ColoredLight;
import SignalGUI.GraphDisplay;
import SignalGUI.RGBLight;
import SignalGUI.RealtimePlotter;
import SignalGUI.ScrollingChannel;
import SignalGUI.TextLight;
import Signals.FFT;
import Utils.TimerTicToc;
import Visualizors.Ambiance;
import Visualizors.AutoBlockShifter;
import Visualizors.Black;
import Visualizors.BlockShifter;
import Visualizors.ColorEnergyWave;
import Visualizors.ColoredBass;
import Visualizors.DoubleChaser;
import Visualizors.FingerPiano;
import Visualizors.FireSlider;
import Visualizors.HueBass;
import Visualizors.HueBass2;
import Visualizors.JazzColors;
import Visualizors.LowSatAmbiance;
import Visualizors.RGBGradientLinear;
import Visualizors.RainbowSlider;
import Visualizors.RedBassColoredClapVisualizer;
import Visualizors.UVBass;
import Visualizors.VUBass;
import Visualizors.Visualizer;
import Visualizors.WhiteBlackSlider;
import Visualizors.WhitePulseBass;

/**
 * This class is responsible for music visualizations for Steve's LED's.
 * @author Steve Levine
 */
public class VisualizationEngineParty extends VisualizationEngine implements ComponentListener {

	// Visualization stuff
	LightDJGUI gui;
	GraphDisplay graphMapper;
	ScrollingChannel channelMapper;
	ChannelLights lights;
	ColoredLight bassLight;
	RGBLight rgbLight;
	TextLight textLight;
	//RealtimePlotter plotter;

	// The list of feature detectors
	public ArrayList<FeatureDetector> featureDetectors;
	
	// The list of visualizers
	public ArrayList<Visualizer> visualizers;
	
	// The list of post processing effects
	public ArrayList<PostProcessor> postProcessors;
	// A status light indicator for each post processing effect
	public IndicatorLight[] statusLights;
	// Special post processors that we should keep track of
	PostProcessor volumePostProcessor;
	PostProcessor whiteBurst;
	
	
	// The arduino LED visualizer
	//LEDVisualizer ledVisuals;
	PartyLightsController8 ledVisuals;
	//PartyLightsController16 ledVisuals;
	
	// MIDI stuff
	// The following class allows for MIDI communication
	MidiConnector midiConnector;
	// The following map MIDI channels to UserControl indices
	Map<Integer, Integer> midiLeftPluginIndices;
	Map<Integer, Integer> midiRightPluginIndices;
	Map<Integer, Integer> midiGeneralSliderIndices;
	Map<Integer, Integer> midiGeneralButtonIndices;
	Map<Integer, Integer> midiVisualizerButtonIndices;
	Map<Integer, Integer> midiPianoButtonIndices;
	Map<Integer, Integer> midiSpecialButtonIndices;
	int midiCrossfaderChannel;
	
	
	TimerTicToc tictoc;
	

	public VisualizationEngineParty(AudioFormat format, double videoDelaySec) {
		super(format, videoDelaySec);
		tictoc = new TimerTicToc();
	}
	
	@Override
	protected void initVisualizations() {

		// Make sure the HiDPI setting is loaded if necessary!
		DPI_MULT = Float.parseFloat(ConfigFileParser.getSettingOrDefault("DPI_MULTIPLIER", "1.0"));
		
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
		
		// Set up all of the post processing effects
		postProcessors = allPostProcessors();
		for(PostProcessor p : postProcessors) {
			p.init();
			
			// Get the list of controls requested
			
		}
		// Create a status light for each post processor to show if it's active or not
		statusLights = new IndicatorLight[postProcessors.size()];
		for(int i = 0; i < postProcessors.size(); i++) {
			statusLights[i] = new IndicatorLight();
		}
		
		
		try {
			//ledVisuals = new LEDVisualizer();
			ledVisuals = new PartyLightsController8();
			//ledVisuals = new PartyLightsController16();
		} catch (Throwable o) {
			System.out.println("WARNING: Couldn't connect to LEDs!");
		}
		
		// Start up MIDI
		setupMIDIControllers();
		
		// Set up the GUI
		startGUI();
		
	}
	
	/**
	 * Return the comprehensive list of all FeatureDetectors. Must be added here to show up0 the LightDJ GUI.
	 * @return
	 */
	public ArrayList<FeatureDetector> allFeatureDetectors() {
		ArrayList<FeatureDetector> detectors = new ArrayList<FeatureDetector>();
		
		int FFT_SIZE = BUFFER_SIZE;
		double UPDATES_PER_SECOND = 1.0 * SAMPLE_RATE / FFT_SIZE * BUFFER_OVERLAP; 
		
		// Add the detectors here
		detectors.add(new BassFinder(FFT_SIZE, UPDATES_PER_SECOND));
		detectors.add(new ClapFinder(FFT_SIZE, UPDATES_PER_SECOND));
		//detectors.add(new FrequencyRangeFinder(FFT_SIZE, UPDATES_PER_SECOND));
		detectors.add(new LevelMeter(FFT_SIZE, UPDATES_PER_SECOND));
		detectors.add(new SharpClapFinder(FFT_SIZE, UPDATES_PER_SECOND));
		
		
		// Return them all
		return detectors;
		
	}
	
	/**
	 * Return the comprehensive list of all visualizers. Must be added here to show up in the LightDJ GUI.
	 * @return
	 */
	public ArrayList<Visualizer> allVisualizers() {
		ArrayList<Visualizer> visualizers = new ArrayList<Visualizer>();
		
		int FFT_SIZE = BUFFER_SIZE;
		double UPDATES_PER_SECOND = (double) SAMPLE_RATE / FFT_SIZE * BUFFER_OVERLAP; 
		
		// Add the visualizers here
		
		visualizers.add(new RedBassColoredClapVisualizer(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new VUBass(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new WhitePulseBass(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new RainbowSlider(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new WhiteBlackSlider(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new FireSlider(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new DoubleChaser(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new Ambiance(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new UVBass(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new FingerPiano(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new LowSatAmbiance(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new Black(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new BlockShifter(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new HueBass(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new AutoBlockShifter(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new ColorEnergyWave(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new HueBass2(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new ColoredBass(FFT_SIZE, UPDATES_PER_SECOND));
		visualizers.add(new JazzColors(FFT_SIZE, UPDATES_PER_SECOND));
		//visualizers.add(new CrazyStrobe(FFT_SIZE, UPDATES_PER_SECOND));
		
		return visualizers;
		 
	}

	
	/**
	 * Return the comprehensive list of all post processors. Must be added here to show up in the LightDJ GUI.
	 * @return
	 */
	public ArrayList<PostProcessor> allPostProcessors() {
		ArrayList<PostProcessor> postProcessors = new ArrayList<PostProcessor>();
		int FFT_SIZE = BUFFER_SIZE;
		double UPDATES_PER_SECOND = (double) SAMPLE_RATE / FFT_SIZE * BUFFER_OVERLAP; 
		
		// Add the post processors here
		volumePostProcessor = new LightVolume(UPDATES_PER_SECOND);
		postProcessors.add(volumePostProcessor);
		postProcessors.add(new Blackout(UPDATES_PER_SECOND));
		postProcessors.add(new Strobes(UPDATES_PER_SECOND));
		
		// WhiteBurst should always be last for emergency purposes, as it controls
		// the Emergency Lighting and should have the "final say" of all post processors.
		whiteBurst = new WhiteBurst(UPDATES_PER_SECOND);
		postProcessors.add(whiteBurst);
		
		return postProcessors;
		 
	}
	
	
	@Override
	protected RenderFrame computeVisualsRendering(FFT fft) {
		
		// Create a featurelist, and pass it al ong with the FFT to each FeatureDetector
		FeatureList featureList = new FeatureList();
		double[] frequencies = fft.getFrequencies();
		double[] magnitudes = fft.getMagnitudes();
		
		// Compute all of the features
		for(FeatureDetector f : featureDetectors) {
			try {
				f.computeFeatures(frequencies, magnitudes, featureList);
			} catch (Exception e) {
				System.out.println("Error with FeatureDetector!");
				e.printStackTrace();
			}
		}
		
		
		// Add in specially-computed features.
		featureList.addFeature("PULSE", pulseKeeper.getPulse());	// The pulse wave (simulated bass), as controlled by the LightDJ
		featureList.addFeature("PULSE_BASS", pulseKeeper.getWavePulse());	// The pulse wave (simulated bass), as controlled by the LightDJ
		
		featureList.addFeature("KEY_ENTER", enterKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_BACKSPACE", backspaceKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_CONTROL", controlKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_ALT", altKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_SHIFT", shiftKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_TAB", tabKeyPressed ? 1.0 : 0.0);
		
		featureList.addFeature("KEY_[", leftBracketKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_]", rightBracketKeyPressed ? 1.0 : 0.0);
		
		
		
		featureList.addFeature("KEY_Q", qKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_W", wKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_E", eKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_R", rKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_T", tKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_Y", yKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_U", uKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_I", iKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_O", oKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_P", pKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_A", aKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_S", sKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_D", dKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_F", fKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_G", gKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_H", hKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_J", jKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_K", kKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_L", lKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_;", semiColonKeyPressed ? 1.0 : 0.0);

		featureList.addFeature("KEY_0", zeroKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_1", oneKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_2", twoKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_3", threeKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_4", fourKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_5", fiveKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_6", sixKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_7", sevenKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_8", eightKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_9", nineKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_MINUS", minusKeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_PLUS", plusKeyPressed ? 1.0 : 0.0);
		
		featureList.addFeature("KEY_F1", f1KeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_F2", f2KeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_F3", f3KeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_F4", f4KeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_F5", f5KeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_F6", f6KeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_F7", f7KeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_F8", f8KeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_F9", f9KeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_F10", f10KeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_F11", f11KeyPressed ? 1.0 : 0.0);
		featureList.addFeature("KEY_F12", f12KeyPressed ? 1.0 : 0.0);
		
		// Now that we have a full-fledged FeatureList, pass it to the Visualizers
		ColorOutput[] colorOutputs = new ColorOutput[visualizers.size()];
		for(int i = 0; i < visualizers.size(); i++) {
			Visualizer v = visualizers.get(i);
			ColorOutput c = null;
			try {
				c = v.visualize(featureList);
			} catch (Exception e) {
				System.out.println("Error with Visualizer!");
				e.printStackTrace();
			}
			colorOutputs[i] = c;
		}
		
		
		RenderFrameParty renderFrame = new RenderFrameParty();
		renderFrame.colorOutputs = colorOutputs;
		renderFrame.featureList = featureList;
		
		
		
		// Update (but do not render) relevant visual GUI elements
		// plotter.update(new double[] {(100.0 * (Double) featureList.getFeature("BASS_LEVEL")), 0.0, 0.0});
		spectrumMapper.updateWithNewSpectrum(frequencies, magnitudes);
		
		
		return renderFrame;
	}

	@Override
	protected void renderVisuals(RenderFrame rf) {
		
		RenderFrameParty renderFrame = (RenderFrameParty) rf;

		// Mix the colors as requested by the LightDJ
		ColorOutput colorOutput = mixColors(renderFrame);
		
		// Apply any necessary post-processing
		applyPostProcessing(colorOutput, renderFrame.featureList);
		
		// Send the command to the LED's
		ledVisuals.visualize(colorOutput);	// Send SERIAL to the RGB's
		
		renderFrame.finalOutput = colorOutput;
		
		// Store the last frame so that it can be rendered appropriately!
		synchronized(this) {
			lastFrame = renderFrame;
		}
	
	}
	
	// Apply post processing to these computed colors using the feature list.
	protected void applyPostProcessing(ColorOutput colorOutput, FeatureList featureList) {
		
		// Run through each of the post processors and execute.
		for(PostProcessor postProcessor : postProcessors) {
			postProcessor.postProcess(colorOutput, featureList);
		}
		
		
	}
	

	
	/**
	 * Takes in the current color frame to be rendered. Retrieves the current cross-fade mixing
	 * parameter from the LightingDJ GUI, and mixes the appropriate ColorOutputs together to yield
	 * one color output.
	 */
	protected ColorOutput mixColors(RenderFrameParty rf) {
		
		return ColorOutput.mix(rf.colorOutputs[visualizerLeftIndex], rf.colorOutputs[visualizerRightIndex], alpha);
		
		
	}
	

	/**
	 * TODO
	 * TODO
	 * TODO
	 * 
	 * The following methods and fields are all for the LightDJ GUI!
	 * 
	 * TODO
	 * TODO
	 * TODO
	 */
	// Graphics and GUI-related variables 
	public static float DPI_MULT;
	protected static int SIDEBAR_WIDTH;
	protected static int SPECTRUM_WIDTH;
	protected static int SPECTRUM_HEIGHT;
	protected static int SPECTRUM_HEIGHT_MIN;
	protected static int PLOTTER_WIDTH;
	protected static int PLOTTER_HEIGHT;
	protected static int BORDER_SIZE;
	protected static int LIGHTBAR_HEIGHT;
	protected static int RECORD_CONTROLS_WIDTH;
	protected static int RECORD_CONTROLS_HEIGHT;
	protected static int RECORD_WIDTH;
	protected static int RECORD_HEIGHT;
	protected static int RECORDS_WIDTH;
	protected static int ACTIVE_LAYER_X;
	protected static int ACTIVE_LAYER_Y;
	protected static int ACTIVE_LAYER_WIDTH;
	protected static int ACTIVE_LAYER_HEIGHT;
	protected static int RECORD_BOX_LEFT_X;
	protected static int RECORD_BOX_LEFT_Y;
	protected static int RECORD_BOX_RIGHT_X;
	protected static int RECORD_BOX_RIGHT_Y;
	protected static int RECORD_BOX_COLOR_DISPLAY_HEIGHT;
	protected static int CROSSFADER_X;
	protected static int CROSSFADER_Y;
	protected static int CROSSFADER_WIDTH;
	protected static int CROSSFADER_HEIGHT;
	protected static int CROSSFADER_INDENT;
	protected static int PLUGIN_THUMBNAIL_WIDTH; // 550
	protected static int PLUGIN_THUMBNAIL_HEIGHT; // 100
	protected static int PULSE_KEEPER_X;
	protected static int PULSE_KEEPER_Y;
	protected static int PULSE_KEEPER_WIDTH;
	protected static int PULSE_KEEPER_HEIGHT;
	protected static int POSTPROCESSOR_X;
	protected static int POSTPROCESSOR_Y;
	protected static int POSTPROCESSOR_WIDTH;
	protected static int POSTPROCESSOR_TITLE_HEIGHT;
	protected static int POSTPROCESSOR_BOTTOM_HEIGHT;
	protected static int POST_PROCESSOR_USER_CONTROL_SLOT_HEIGHT;
	protected static int USER_CONTROL_SLOT_HEIGHT;
	public static int PANEL_BORDER_RADIUS;
	public static int LAYER_BORDER_RADIUS;
	
	// Some color information
	public static Color PANEL_BACKGROUND_COLOR;
	public static Color PANEL_BORDER_COLOR;
	public static Color TEXT_COLOR;
	public static Color HOT_COLOR;
	public static AlphaComposite COMPOSITE_TRANSLUCENT;
	public static AlphaComposite COMPOSITE_OPAQUE;
	public static Font PANEL_FONT;
	public static Font PANEL_FONT_SMALL;
	public static Font PANEL_FONT_LARGE;
	public static Font PULSE_KEEPER_FONT;
	public static Stroke REGULAR_STROKE;
	public static Stroke THICK_STROKE;
	
	// Render buffers
	protected BufferedImage background;
	protected BufferedImage buffer;
	
	// Preloaded images
	protected BufferedImage turntableLogo;
	protected BufferedImage recordLeft;
	protected BufferedImage recordRight;
	
	// Controls and displays
	protected ScrollingSpectrum spectrumMapper;
	protected RealtimePlotter plotter;
	protected RenderFrameParty lastFrame;
	protected ColorOutputDisplayer colorOutputDisplayer;
	protected CrossfaderKnob crossfaderKnob;
	protected VisualizerChooser visualizerChooser;
	protected PulseKeeper pulseKeeper;
	
	// Keep track of all the elements that can receive mouse events.
	protected List<MouseAcceptorPanel> mouseAcceptors;
	protected MouseAcceptorPanel activePanel;
	
	// Whether or not to draw a layer above everything else, for fine-tuned control
	public boolean activeLayer = false;
	
	// Which visualizers are currently being used
	protected static int visualizerLeftIndex = 0;
	protected static int visualizerRightIndex = 1;
	protected static int activePlugin = 1;	// 0 => Left is selected, 1 => Right is selected
	protected static double alpha;	// The mixing between the two.
	
	// Keep track of which keys are pressed
	protected static boolean controlKeyPressed = false;
	protected static boolean shiftKeyPressed = false;
	protected static boolean altKeyPressed = false;
	protected static boolean enterKeyPressed = false;
	protected static boolean escapeKeyPressed = false;
	protected static boolean backspaceKeyPressed = false;
	protected static boolean tabKeyPressed = false;
	protected static boolean f1KeyPressed = false;
	protected static boolean f2KeyPressed = false;
	protected static boolean f3KeyPressed = false;
	protected static boolean f4KeyPressed = false;
	protected static boolean f5KeyPressed = false;
	protected static boolean f6KeyPressed = false;
	protected static boolean f7KeyPressed = false;
	protected static boolean f8KeyPressed = false;
	protected static boolean f9KeyPressed = false;
	protected static boolean f10KeyPressed = false;
	protected static boolean f11KeyPressed = false;
	protected static boolean f12KeyPressed = false;
	protected static boolean leftBracketKeyPressed = false;
	protected static boolean rightBracketKeyPressed = false;
	protected static boolean qKeyPressed = false;
	protected static boolean wKeyPressed = false;
	protected static boolean eKeyPressed = false;
	protected static boolean rKeyPressed = false;
	protected static boolean tKeyPressed = false;
	protected static boolean yKeyPressed = false;
	protected static boolean uKeyPressed = false;
	protected static boolean iKeyPressed = false;
	protected static boolean oKeyPressed = false;
	protected static boolean pKeyPressed = false;
	protected static boolean aKeyPressed = false;
	protected static boolean sKeyPressed = false;
	protected static boolean dKeyPressed = false;
	protected static boolean fKeyPressed = false;
	protected static boolean gKeyPressed = false;
	protected static boolean hKeyPressed = false;
	protected static boolean jKeyPressed = false;
	protected static boolean kKeyPressed = false;
	protected static boolean lKeyPressed = false;
	protected static boolean semiColonKeyPressed = false;
	protected static boolean oneKeyPressed = false;
	protected static boolean twoKeyPressed = false;
	protected static boolean threeKeyPressed = false;
	protected static boolean fourKeyPressed = false;
	protected static boolean fiveKeyPressed = false;
	protected static boolean sixKeyPressed = false;
	protected static boolean sevenKeyPressed = false;
	protected static boolean eightKeyPressed = false;
	protected static boolean nineKeyPressed = false;
	protected static boolean zeroKeyPressed = false;
	protected static boolean minusKeyPressed = false;
	protected static boolean plusKeyPressed = false;
	protected static boolean spaceKeyPressed = false;

	// Store the state of the LightDJ
	public enum LightDJState {
		LIGHTDJ_STATE_NORMAL,
		LIGHTDJ_STATE_CHOOSING_VISUALIZER
	}
	protected LightDJState lightDJState;
	
	// Allow for automated cross-fade effects
	public enum CrossfadeAutomator {
		CROSSFADE_MANUAL,
		CROSSFADE_AUTO
	}
	protected CrossfadeAutomator crossfadeAutomator;
	protected double crossfadeSpeed = 0.0;
	protected static double CROSSFADE_SPEED_SLOW = 0.015;
	protected static double CROSSFADE_SPEED_FAST = 0.04;
	
	
	
	// Shifting/
	protected int SHIFT_SPEED = 1;
	protected int SHIFT_BLOCK_SIZE = 24;
	protected int shiftCounter;
	protected int shiftPosition;
	
	
	// Scatter strobing
	protected long[] scatterStrobes = new long[ColorOutput.NUM_UVWHITE_PANELS];
	protected long scatterTimeLength = 100;	// in milliseconds
	protected int lastScatterStrobe = -1;
	
	
	// Hue shifting
	protected long HUE_SHIFT_TIME = 750;	// milliseconds
	
	
	// TODO
	// TODO
	// TODO
	
	
	public void startGUI() {
		gui = new LightDJGUI(this);
		JFrame frame = new JFrame("Light DJ");
		frame.setUndecorated(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(gui);
		frame.pack();
		gui.setBackground(Color.BLACK);
		frame.setSize(1500, 1100);
		frame.setLocation(0, 0);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		frame.addComponentListener(this);
		
		// Set some geometric parameters!
		SIDEBAR_WIDTH = scale(350);
		SPECTRUM_WIDTH = scale(900);
		SPECTRUM_HEIGHT = scale(400);
		SPECTRUM_HEIGHT_MIN = 10;
		PLOTTER_WIDTH = scale(900);
		PLOTTER_HEIGHT = scale(300);
		BORDER_SIZE = scale(10);
		LIGHTBAR_HEIGHT = scale(200);
		RECORD_CONTROLS_WIDTH = scale(500);
		RECORD_CONTROLS_HEIGHT = scale(250);
		RECORD_WIDTH = scale(400);
		RECORD_HEIGHT = scale(200);
		RECORDS_WIDTH = 2*RECORD_CONTROLS_WIDTH + BORDER_SIZE;
		ACTIVE_LAYER_X = 9*BORDER_SIZE;
		ACTIVE_LAYER_Y = 9*BORDER_SIZE;
		RECORD_BOX_COLOR_DISPLAY_HEIGHT = scale(100);
		CROSSFADER_INDENT = scale(10);
		PLUGIN_THUMBNAIL_WIDTH = scale(430); // 550
		PLUGIN_THUMBNAIL_HEIGHT = scale(80); // 100
		PULSE_KEEPER_WIDTH = SIDEBAR_WIDTH - 2*BORDER_SIZE;
		PULSE_KEEPER_HEIGHT = scale(100);
		POSTPROCESSOR_WIDTH = SIDEBAR_WIDTH - 2*BORDER_SIZE;
		POSTPROCESSOR_TITLE_HEIGHT = scale(40);
		POSTPROCESSOR_BOTTOM_HEIGHT = scale(0);
		POST_PROCESSOR_USER_CONTROL_SLOT_HEIGHT = scale(40);
		USER_CONTROL_SLOT_HEIGHT = scale(60);
		PANEL_BORDER_RADIUS = scale(40);
		LAYER_BORDER_RADIUS = scale(60);
		
		
		// Set some colors
		PANEL_BACKGROUND_COLOR = new Color(20, 20, 20);
		PANEL_BORDER_COLOR = new Color(40, 40, 40);
		TEXT_COLOR = new Color(160, 160, 160);
		HOT_COLOR = new Color(200, 114, 0);
		COMPOSITE_OPAQUE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
		COMPOSITE_TRANSLUCENT = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f);
		
		// Attempt to load custom fancy-looking fonts from the Fonts/ directory. If that doesn't work,
		// try to use ones that may be installed (which may result in completely different fonts)
		try {
			Font eraser = Font.createFont(Font.TRUETYPE_FONT, new File("Fonts/Eraser.ttf"));
			PANEL_FONT = eraser.deriveFont(24.0f * DPI_MULT);
			PANEL_FONT_SMALL = eraser.deriveFont(16.0f * DPI_MULT);
			PANEL_FONT_LARGE = eraser.deriveFont(48.0f * DPI_MULT);
			
			Font nimbus = Font.createFont(Font.TRUETYPE_FONT, new File("Fonts/LiberationMono-Bold.ttf"));
			PULSE_KEEPER_FONT = nimbus.deriveFont(72.0f * DPI_MULT);
			
		} catch (Exception e) {
			System.out.println("Error: Could not load custom fonts from the Fonts/ directory!");
			PANEL_FONT = new Font("Eraser", Font.PLAIN, scale(24));
			PANEL_FONT_SMALL= new Font("Eraser", Font.PLAIN, scale(16));
			PANEL_FONT_LARGE = new Font("Eraser", Font.PLAIN, scale(48));
			PULSE_KEEPER_FONT = new Font("Nimbus Mono L", Font.BOLD, scale(72));
			e.printStackTrace();
		}

		REGULAR_STROKE = new BasicStroke(1.0f * DPI_MULT);
		THICK_STROKE = new BasicStroke(3.0f * DPI_MULT);
		alpha = 0.0;
		
		// Set the default states
		lightDJState = LightDJState.LIGHTDJ_STATE_NORMAL;
		crossfadeAutomator = CrossfadeAutomator.CROSSFADE_MANUAL;
			
		// Choose which color output displayer to use!
		colorOutputDisplayer = new ColorOutputDisplayerParty(this);
		
		// Start the crossfader knob
		crossfaderKnob = new CrossfaderKnob(this);
		
		// Start the visualizer chooser		startAutoCrossfade(-CROSSFADE_SPEED_SLOW);
		visualizerChooser = new VisualizerChooser(this, visualizers);
		
		// Start the pulse keeper
		pulseKeeper = new PulseKeeper();
		
		// Set up some other GUI elements
		spectrumMapper = new ScrollingSpectrum(0, 0, SPECTRUM_WIDTH, SPECTRUM_HEIGHT, null, 40, 20000, 4.0, BUFFER_SIZE, SAMPLE_RATE);
		// plotter = new RealtimePlotter(new Color[]{Color.RED, Color.YELLOW, Color.GREEN}, 0, 0, PLOTTER_WIDTH, PLOTTER_HEIGHT, 100.0, null);
	
		
		// Set up the mouse acceptors
		mouseAcceptors = new LinkedList<MouseAcceptorPanel>();
		activePanel = null;
		mouseAcceptors.add(visualizerChooser);
		mouseAcceptors.add(crossfaderKnob);
		// Also add mouse acceptors for the post processing effects
		for(PostProcessor p : postProcessors) {
			for(UserControl control : p.getRequestedUserControls()) {
				mouseAcceptors.add(control);
			}
		}
		
		
		// Generate the background
		// generateBackground();
	
		// Start a tast to render regularly!
		Timer t = new Timer();
		t.scheduleAtFixedRate(new RenderTask(this), 0, 17);
		
		System.out.println("Light DJ started.");
	}
	
	
	public static int scale(int val) {
		return (int) Math.round(VisualizationEngineParty.DPI_MULT * val);
	}
	
	/**
	 * Generate a pretty looking background image
	 */
	protected void generateBackground() {
		// Allocate an image of the proper size
		int width = gui.getWidth();
		int height = gui.getHeight();
		background = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2D = (Graphics2D) background.getGraphics();
		
		// Redefine some geometric parameters
		RECORD_BOX_LEFT_X = SIDEBAR_WIDTH + (width - RECORDS_WIDTH - SIDEBAR_WIDTH) / 2 + BORDER_SIZE;
		RECORD_BOX_LEFT_Y = BORDER_SIZE*2 + LIGHTBAR_HEIGHT;
		RECORD_BOX_RIGHT_X = SIDEBAR_WIDTH + (width - RECORDS_WIDTH - SIDEBAR_WIDTH) / 2 + BORDER_SIZE*2 + RECORD_CONTROLS_WIDTH;
		RECORD_BOX_RIGHT_Y =  BORDER_SIZE*2 + LIGHTBAR_HEIGHT;
		
		ACTIVE_LAYER_WIDTH = width - 18*BORDER_SIZE;
		ACTIVE_LAYER_HEIGHT = height - 18*BORDER_SIZE;
		
		CROSSFADER_X = RECORD_BOX_LEFT_X + RECORD_CONTROLS_WIDTH / 2;
		CROSSFADER_Y = RECORD_BOX_LEFT_Y + RECORD_CONTROLS_HEIGHT + scale(175);
		CROSSFADER_WIDTH = RECORD_CONTROLS_WIDTH + BORDER_SIZE;
		CROSSFADER_HEIGHT = scale(150);
		if (crossfaderKnob != null) {
			crossfaderKnob.setLocation(CROSSFADER_X, CROSSFADER_Y, CROSSFADER_WIDTH, CROSSFADER_HEIGHT);
			crossfaderKnob.needsToRender();
		}
		
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		
		// Paint a black background
		g2D.setBackground(Color.BLACK);
		g2D.clearRect(0, 0, width, height);
		
		
		// Display the turntable image, loading it if necessary.
		if (turntableLogo == null) {
			// Try and load it
			try {
				
				turntableLogo = ImageIO.read(new File("Images/background.png"));
				
				BufferedImage recordLeftRaw = ImageIO.read(new File("Images/record_left_large.png"));
				BufferedImage recordRightRaw = ImageIO.read(new File("Images/record_right_large.png"));
				recordLeft = scaleImage(recordLeftRaw, RECORD_WIDTH, RECORD_HEIGHT);
				recordRight = scaleImage(recordRightRaw, RECORD_WIDTH, RECORD_HEIGHT);
				
				//recordLeft = ImageIO.read(new File("Images/record_left.png"));
				//recordRight = ImageIO.read(new File("Images/record_right.png"));
				
			} catch (IOException e) {
				System.out.println("Warning: Could not load LightDJ background image!");
				e.printStackTrace();
				return;
			}
		}
		g2D.drawImage(turntableLogo, SIDEBAR_WIDTH, 0, null);
		
		// Draw the sidebar
		//g2D.setColor(new Color(10,10,10));
		//g2D.fillRect(0, 0, SIDEBAR_WIDTH, height);
		g2D.setColor(PANEL_BORDER_COLOR);
		g2D.drawLine(SIDEBAR_WIDTH, 0, SIDEBAR_WIDTH, height);
		

		g2D.drawImage(recordLeft, RECORD_BOX_LEFT_X + (RECORD_CONTROLS_WIDTH - RECORD_WIDTH) /2, RECORD_BOX_LEFT_Y + RECORD_CONTROLS_HEIGHT, null);
		g2D.drawImage(recordRight, RECORD_BOX_RIGHT_X + (RECORD_CONTROLS_WIDTH - RECORD_WIDTH) /2, RECORD_BOX_RIGHT_Y + RECORD_CONTROLS_HEIGHT, null);
		

		
		// Render the visualizer plugins
		loadVisualizerPlugin(true, visualizerLeftIndex);
		loadVisualizerPlugin(false, visualizerRightIndex);
		
		// Draw the backgrounds for the post processors!
		POSTPROCESSOR_X = BORDER_SIZE;
		POSTPROCESSOR_Y = PULSE_KEEPER_Y + PULSE_KEEPER_HEIGHT + BORDER_SIZE;
		int y = POSTPROCESSOR_Y;
		int w = (POSTPROCESSOR_WIDTH - 5*BORDER_SIZE)/2;
		int postProcessorIndex = 0;
		for(PostProcessor postProcessor : postProcessors) {
			// Determine the height of this box
			int h = POSTPROCESSOR_TITLE_HEIGHT;
			int xi = 1;
			int yi = -1;
			for(UserControl control : postProcessor.getRequestedUserControls()) {
				if (xi == 1) {
					xi = 0;
					yi += 1;
				} else {
					xi = 1;
				}
				control.setLocation(2 * BORDER_SIZE + xi*(w + BORDER_SIZE), y + h + yi * (POST_PROCESSOR_USER_CONTROL_SLOT_HEIGHT + BORDER_SIZE), w, POST_PROCESSOR_USER_CONTROL_SLOT_HEIGHT);
			}
			h += (yi + 1) * (POST_PROCESSOR_USER_CONTROL_SLOT_HEIGHT + BORDER_SIZE);
			h += POSTPROCESSOR_BOTTOM_HEIGHT;
			
			// Set the location of this post processor's status light
			statusLights[postProcessorIndex].setLocation(SIDEBAR_WIDTH - BORDER_SIZE - scale(50), y + scale(7), 1, 1);
			
			// Draw the box
			g2D.setColor(PANEL_BACKGROUND_COLOR);
			g2D.fillRoundRect(POSTPROCESSOR_X, y, POSTPROCESSOR_WIDTH, h, PANEL_BORDER_RADIUS, PANEL_BORDER_RADIUS);
			g2D.setColor(PANEL_BORDER_COLOR);
			g2D.setStroke(REGULAR_STROKE);
			g2D.drawRoundRect(POSTPROCESSOR_X, y, POSTPROCESSOR_WIDTH, h, PANEL_BORDER_RADIUS, PANEL_BORDER_RADIUS);
			
			// Draw the title and the check box (off right now by default)
			g2D.setColor(TEXT_COLOR);
			g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2D.setFont(PANEL_FONT);
			g2D.drawString(postProcessor.getName(), POSTPROCESSOR_X + BORDER_SIZE, y + scale(16) + BORDER_SIZE);
			g2D.setColor(PANEL_BORDER_COLOR);
			g2D.drawLine(POSTPROCESSOR_X + BORDER_SIZE, y + scale(25) + BORDER_SIZE, POSTPROCESSOR_X + POSTPROCESSOR_WIDTH - BORDER_SIZE, y + scale(25) + BORDER_SIZE);
		
			// Render all of the controls
			for(UserControl control : postProcessor.getRequestedUserControls()) {
				control.render(g2D);
			}
			statusLights[postProcessorIndex].render(g2D);
			
			y += h + BORDER_SIZE;
			
			postProcessorIndex++;
		}
		
		
		// Resize where the scrolling spectrum goes
		int specHeight = Math.min(SPECTRUM_HEIGHT, height - y - BORDER_SIZE);
		if (specHeight < SPECTRUM_HEIGHT_MIN) {
			specHeight = SPECTRUM_HEIGHT_MIN;
		}
		spectrumMapper.move(BORDER_SIZE, height - specHeight - BORDER_SIZE, SIDEBAR_WIDTH - 2*BORDER_SIZE, specHeight);
		spectrumMapper.setGraphics((Graphics2D) background.getGraphics());
		
		// Resize where the graphs go
		// plotter.move(BORDER_SIZE, gui.getHeight() - SPECTRUM_HEIGHT - BORDER_SIZE * 2 - PLOTTER_HEIGHT, SIDEBAR_WIDTH - 2*BORDER_SIZE, PLOTTER_HEIGHT);
		// plotter.setGraphics((Graphics2D) background.getGraphics());
		
		// Make sure the visualizer chooser is set up correctly
		visualizerChooser.setPosition(ACTIVE_LAYER_X, ACTIVE_LAYER_Y, ACTIVE_LAYER_WIDTH, ACTIVE_LAYER_HEIGHT);		
		
	}
	
	/**
	 * Helper function to do some image scaling.
	 */
	public static BufferedImage scaleImage(BufferedImage imageOrig, int w, int h) {
		/**
		 * Alternate method - higher quality, but a lot slower.
		 */
		//BufferedImage imageNew  = new BufferedImage(w, h, imageOrig.getType());
		//Graphics2D g2D = (Graphics2D) imageNew.getGraphics();
		//g2D.drawImage(imageOrig.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
		
		// Assume the two images have the same aspect ratio.
		BufferedImage img_t = imageOrig;
		int w_t = imageOrig.getWidth();
		int w_h = imageOrig.getHeight();
		while((float) w_t / w >= 2.0f) {
			// Downsize by an exact factor of 2.0
			w_t /= 2;
			w_h /= 2;
			BufferedImage img_tt = new BufferedImage(w_t, w_h, img_t.getType());
			Graphics2D g2D = (Graphics2D) img_tt.getGraphics();
			g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2D.drawImage(img_t, 0, 0, w_t, w_h, null);
			img_t = img_tt;
			
		}
		
		
		// Perform the final resize
		if (w != w_t) {
			BufferedImage imageNew  = new BufferedImage(w, h, imageOrig.getType());
			Graphics2D g2D = (Graphics2D) imageNew.getGraphics();
			g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2D.drawImage(img_t, 0, 0, w, h, null);
			return imageNew;
		} else {
			return img_t;
		}
		
		
//		BufferedImage imageNew  = new BufferedImage(w, h, imageOrig.getType());
//		Graphics2D g2D = (Graphics2D) imageNew.getGraphics();
//		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//		g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g2D.drawImage(imageOrig, 0, 0, w, h, null);
		
		// Return it
		//return imageNew;

	}
	
	
	/**
	 * Draws everything for the visualizer plugin
	 */
	protected void loadVisualizerPlugin(boolean left, int pluginIndex) {
		
		int x; int y; int width; int height;
		
		Visualizer visualizer = visualizers.get(pluginIndex);
		Graphics2D g2D = (Graphics2D) background.getGraphics();
		
		if (left) {
			g2D.setColor(PANEL_BACKGROUND_COLOR);
			g2D.fillRoundRect(RECORD_BOX_LEFT_X, RECORD_BOX_LEFT_Y, RECORD_CONTROLS_WIDTH, RECORD_CONTROLS_HEIGHT, PANEL_BORDER_RADIUS, PANEL_BORDER_RADIUS);
			g2D.setColor(PANEL_BORDER_COLOR);
			g2D.drawRoundRect(RECORD_BOX_LEFT_X, RECORD_BOX_LEFT_Y, RECORD_CONTROLS_WIDTH, RECORD_CONTROLS_HEIGHT, PANEL_BORDER_RADIUS, PANEL_BORDER_RADIUS);
			x = RECORD_BOX_LEFT_X + BORDER_SIZE;
			y = RECORD_BOX_LEFT_Y + BORDER_SIZE;
			width = RECORD_CONTROLS_WIDTH - 2*BORDER_SIZE;
			height = RECORD_CONTROLS_HEIGHT - 2*BORDER_SIZE;
			
		} else {
			g2D.setColor(PANEL_BACKGROUND_COLOR);
			g2D.fillRoundRect(RECORD_BOX_RIGHT_X, RECORD_BOX_RIGHT_Y, RECORD_CONTROLS_WIDTH, RECORD_CONTROLS_HEIGHT, PANEL_BORDER_RADIUS, PANEL_BORDER_RADIUS);
			g2D.setColor(PANEL_BORDER_COLOR);
			g2D.drawRoundRect(RECORD_BOX_RIGHT_X, RECORD_BOX_RIGHT_Y, RECORD_CONTROLS_WIDTH, RECORD_CONTROLS_HEIGHT, PANEL_BORDER_RADIUS, PANEL_BORDER_RADIUS);
			x = RECORD_BOX_RIGHT_X + BORDER_SIZE;
			y = RECORD_BOX_RIGHT_Y + BORDER_SIZE;
			width = RECORD_CONTROLS_WIDTH - 2*BORDER_SIZE;
			height = RECORD_CONTROLS_HEIGHT - 2*BORDER_SIZE;
		}
		

		g2D.setColor(TEXT_COLOR);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2D.setFont(PANEL_FONT);
		g2D.drawString(visualizer.getName(), x, y + scale(16));
		g2D.setColor(PANEL_BORDER_COLOR);
		g2D.drawLine(x, y + scale(25), x + width, y + scale(25));
	
		y += scale(30);
		
		// Set the location of any controls thacontrol.render(g2D);t needed to be rendered, and render them
		int slotX = 0; int slotY = 0;
		for(UserControl control : visualizer.getRequestedUserControls()) {
			control.setLocation(x + slotX * width / 2, y + slotY * USER_CONTROL_SLOT_HEIGHT, width / 2, USER_CONTROL_SLOT_HEIGHT - BORDER_SIZE);
			control.render(g2D);
			
			slotX++;
			if (slotX == 2) {
				slotX = 0;
				slotY++;
			}
		}
		
		
	}
	
	public void setMixerAlpha(double a) {
		
		// Set the mixer!
		if (a < 0.0) {
			alpha = 0.0;
		} else if (a > 1.0) {
			alpha = 1.0;
		} else {
			alpha = a;
		}
		
	}
	
	
	
	
	/**
	 * Paint the visualizer chooser
	 */
	public void paintVisualizerChooser(RenderFrameParty renderFrame) {
		Graphics2D g2D = (Graphics2D) buffer.getGraphics();
	
		g2D.setFont(PANEL_FONT_LARGE);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING , RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2D.setColor(TEXT_COLOR);
		g2D.drawString("Visualizorz", ACTIVE_LAYER_X, ACTIVE_LAYER_Y - scale(10));
		
		// Now, draw each plugin.
		int col = 0;
		int row = 0;
		int i = 0;
		for(int pluginIndex = 0; pluginIndex < visualizers.size(); pluginIndex++) {
			Visualizer v = visualizers.get(pluginIndex);
			int x = ACTIVE_LAYER_X + 2*BORDER_SIZE + (PLUGIN_THUMBNAIL_WIDTH + BORDER_SIZE) * col;
			int y = ACTIVE_LAYER_Y + 2*BORDER_SIZE + (PLUGIN_THUMBNAIL_HEIGHT + BORDER_SIZE + scale(30)) * row;
			
			g2D.setColor(PANEL_BORDER_COLOR);
			g2D.drawRoundRect(x, y, PLUGIN_THUMBNAIL_WIDTH, PLUGIN_THUMBNAIL_HEIGHT, scale(20), scale(20));
			
			g2D.setFont(PANEL_FONT);
			g2D.setColor(TEXT_COLOR);
			g2D.drawString(v.getName(), x + scale(30), y);
			
			g2D.setFont(PANEL_FONT);
			g2D.setColor(HOT_COLOR);
			g2D.drawString("abcdefghijklmnopqrstuvwxyz".substring(i, i+1), x, y);
			
			colorOutputDisplayer.render(renderFrame.colorOutputs[pluginIndex], g2D, x, y + scale(40), PLUGIN_THUMBNAIL_WIDTH, PLUGIN_THUMBNAIL_HEIGHT - scale(40));
			
			
			// Now increment
			col++;
			i++;
			x = ACTIVE_LAYER_X + 2*BORDER_SIZE + (PLUGIN_THUMBNAIL_WIDTH + BORDER_SIZE) * col;
			if (x + PLUGIN_THUMBNAIL_WIDTH > ACTIVE_LAYER_X + ACTIVE_LAYER_WIDTH - 4*BORDER_SIZE) {
				col = 0;
				row++;
			}
			
			if (row * PLUGIN_THUMBNAIL_HEIGHT > ACTIVE_LAYER_HEIGHT - 4*BORDER_SIZE) {
				return;
			}
			
			
		}
	
	}
	
	public void paintPulseKeeper() {
		Graphics2D g2D = (Graphics2D) background.getGraphics();
		
		g2D.setColor(Color.BLACK);
		g2D.fillRect(PULSE_KEEPER_X, PULSE_KEEPER_Y, PULSE_KEEPER_WIDTH, PULSE_KEEPER_HEIGHT);
		
		//g2D.setColor(RGBGradientLinear.linearGradient(Color.BLACK, Color.WHITE, ))
		g2D.setColor(TEXT_COLOR);
		g2D.setFont(PULSE_KEEPER_FONT);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		double pulse = pulseKeeper.getPulse();
		int beat = (int) pulse;
		int subBeat = (int) (100.0 * (pulse - beat));
		String pulseString = String.format("%d:%d", beat, subBeat);
		String bpmString = String.format("BPM: %.3f", pulseKeeper.getBPM());
		
		g2D.drawString(pulseString, PULSE_KEEPER_X, PULSE_KEEPER_Y + scale(52));
		g2D.setFont(PANEL_FONT);
		g2D.drawString(bpmString, PULSE_KEEPER_X + scale(10), PULSE_KEEPER_Y + scale(90));
		
	}
	
	
	public void chooseVisualizer(int pluginIndex, int activePlugin) {
		
		// Don't change it the left and right plugins are being set to the same thing!
		if (activePlugin == 0 && pluginIndex == visualizerRightIndex) {
			 // The same! Do nothingPAINT
		} else if (activePlugin == 1 && pluginIndex == visualizerLeftIndex) {
			// The same! Do nothing
		} else if (activePlugin == 0) {
			visualizerLeftIndex = pluginIndex;
			loadVisualizerPlugin(true, pluginIndex);
		} else {
			visualizerRightIndex = pluginIndex;
			loadVisualizerPlugin(false, pluginIndex);
		}
		
		
		// Set the state 
		lightDJState = LightDJState.LIGHTDJ_STATE_NORMAL;
		
		
	}
	
	
	/**
	 * Render the Light DJ GUI 
	 * (Called automagically by a timer)
	 */
	public void renderDJ() {
		RenderFrameParty renderFrame;
		synchronized(this) {
			renderFrame = lastFrame;
		}
		
		if (renderFrame == null) {
			// Don't render!
			return;
		}
		
		Graphics2D g2D = (Graphics2D) background.getGraphics();
		int width = gui.getWidth();
		int height = gui.getHeight();
		
		// Compute the mixed colors
		ColorOutput outputColors = renderFrame.finalOutput;
		ColorOutput leftColors = renderFrame.colorOutputs[visualizerLeftIndex];
		ColorOutput rightColors = renderFrame.colorOutputs[visualizerRightIndex];
		
		// Display the mixed colors, as well as the left and ride individual colors in the appropriate locations
		colorOutputDisplayer.render(outputColors, g2D, SIDEBAR_WIDTH + BORDER_SIZE, BORDER_SIZE, width - SIDEBAR_WIDTH - 2*BORDER_SIZE, LIGHTBAR_HEIGHT);
		colorOutputDisplayer.render(leftColors, g2D, RECORD_BOX_LEFT_X + BORDER_SIZE, RECORD_BOX_LEFT_Y + RECORD_CONTROLS_HEIGHT - BORDER_SIZE - RECORD_BOX_COLOR_DISPLAY_HEIGHT, RECORD_CONTROLS_WIDTH - 2*BORDER_SIZE, RECORD_BOX_COLOR_DISPLAY_HEIGHT); 
		colorOutputDisplayer.render(rightColors, g2D, RECORD_BOX_RIGHT_X + BORDER_SIZE, RECORD_BOX_RIGHT_Y + RECORD_CONTROLS_HEIGHT - BORDER_SIZE - RECORD_BOX_COLOR_DISPLAY_HEIGHT, RECORD_CONTROLS_WIDTH - 2*BORDER_SIZE, RECORD_BOX_COLOR_DISPLAY_HEIGHT); 
		
		// Render any user controls that are requesting to be updated
		for(UserControl control : visualizers.get(visualizerLeftIndex).getRequestedUserControls()) {
			if (control.needsToRender()) {
				control.render(g2D);
			}
		}
		for(UserControl control : visualizers.get(visualizerRightIndex).getRequestedUserControls()) {
			if (control.needsToRender()) {
				control.render(g2D);
			}
		}
			
		
		// Draw the cross-fader knob
		//paintCrossfader(false);
		if (crossfaderKnob.needsToRender()) {
			crossfaderKnob.render(g2D);
		}
		
		// Render the side panel
		renderSidePanel();
		
		Graphics2D g2DGui = (Graphics2D) gui.getGraphics();
		Graphics2D g2DBuf = (Graphics2D) buffer.getGraphics();
		
		
		if (activeLayer) {
			g2DBuf.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
			g2DBuf.drawImage(background, 0, 0, null);
			
			g2DBuf.setColor(PANEL_BACKGROUND_COLOR);
			//g2DBuf.setComposite(COMPOSITE_TRANSLUCENT);
			g2DBuf.fillRoundRect(ACTIVE_LAYER_X, ACTIVE_LAYER_Y - BORDER_SIZE, ACTIVE_LAYER_WIDTH, ACTIVE_LAYER_HEIGHT, LAYER_BORDER_RADIUS, LAYER_BORDER_RADIUS);
			//g2DBuf.setComposite(COMPOSITE_OPAQUE);
			 
			g2DBuf.setColor(PANEL_BORDER_COLOR);
			g2DBuf.drawRoundRect(ACTIVE_LAYER_X, ACTIVE_LAYER_Y - BORDER_SIZE, ACTIVE_LAYER_WIDTH, ACTIVE_LAYER_HEIGHT, LAYER_BORDER_RADIUS, LAYER_BORDER_RADIUS);
			
			// Now, depending on the state
			if (lightDJState == LightDJState.LIGHTDJ_STATE_CHOOSING_VISUALIZER) {
				// Draw more stuff
				paintVisualizerChooser(renderFrame);
			}
			
			
			g2DGui.drawImage(buffer, 0, 0, null);
		
		} else {
			
			g2DGui.drawImage(background, 0, 0, null);
		}
		
		
		
		// Step the AutoDJ
		autoDJStep();
		
	}
	
	protected void renderSidePanel() {
		Graphics2D g2D = (Graphics2D) background.getGraphics();
		int width = gui.getWidth();
		int height = gui.getHeight();
		
		// Render the spectrum in the lower left corner
		spectrumMapper.render();
		
		// Render graphs
		//plotter.render();
		
		// Render the pulse
		paintPulseKeeper();
		
		// Render any post-processing user controls
		for(int postProcessorIndex = 0; postProcessorIndex < postProcessors.size(); postProcessorIndex++) {
			PostProcessor p = postProcessors.get(postProcessorIndex);
			for(UserControl control : p.getRequestedUserControls()) {
				if (control.needsToRender()) {
					control.render(g2D);
				}
			}
			// Also, don't forget about the status light!
			statusLights[postProcessorIndex].setValue((p.isActive() ? 1.0f : 0.0f));
			if (statusLights[postProcessorIndex].needsToRender()) {
				statusLights[postProcessorIndex].render(g2D);
			}
		}
		
	}
	
	public void paintDJ(Graphics g) {
		// Repaint the background
		Graphics2D g2D = (Graphics2D) g;
		g2D.drawImage(background, 0, 0, null);
		
	}
	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}
	public void componentResized(ComponentEvent e) {generateBackground();}

	/**
	 * Mouse stuff
	 */
	public void mouseDown(int xM, int yM) {
		boolean result;
		
		result = processMouseDown(xM, yM, mouseAcceptors);
		
		// If not a general one, try the left plugin
		if (!result) {
			result = processMouseDown(xM, yM, visualizers.get(visualizerLeftIndex).getRequestedUserControls());
		}
		
		// If not a left one, try the right plugin
		if (!result) {
			result = processMouseDown(xM, yM, visualizers.get(visualizerRightIndex).getRequestedUserControls());
		}
		
	}
	
	// Search through the mouseAcceptors, and process mousedowns. Returns true if we find
	// a mouseacceptor where we clicked, false otherwise.
	protected boolean processMouseDown(int xM, int yM, List list) {
		List<MouseAcceptorPanel> acceptors = (List<MouseAcceptorPanel>) list;
		for(MouseAcceptorPanel p : acceptors) {
			// Does the mouse event fall into this panel? If it does, cast it!
			int x = p.getX();
			int y = p.getY();
			int w = p.getWidth();
			int h = p.getHeight();
			
			if (p.isVisible() && xM >= x && xM < x + w && yM >= y && yM < y + h) {
				// Found it! Convert to relative coordinates and go.
				activePanel = p;
				p.mouseDown(xM - x, yM - y);
				return true;
			}
			
		}
		return false;
	}
	
	public void mouseUp(int xM, int yM) {
		if (activePanel == null) {
			return;
		}
		activePanel.mouseUp(xM - activePanel.getX(), yM - activePanel.getY());
		activePanel = null;
	}
	
	public void mouseDragged(int xM, int yM) {
		if (activePanel == null) {
			return;
		}
		activePanel.mouseDragged(xM - activePanel.getX(), yM - activePanel.getY());
	}
	
	/**
	 * Keyboard stuff
	 */
	public void keyDown(int keyCode) {
		
		// Record which keys are pressed
		if (keyCode == KeyEvent.VK_CONTROL) {
			controlKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_ALT) {
			altKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_ENTER) {
			enterKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_SHIFT) {
			shiftKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_BACK_SPACE) {
			backspaceKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_TAB) {
			tabKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_A) {
			aKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_S) {
			sKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_D) {
			dKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_F) {
			fKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_G) {
			gKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_H) {
			hKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_J) {
			jKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_K) {
			kKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_L) {
			lKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_SEMICOLON) {
			semiColonKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_Q) {
			qKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_W) {
			wKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_E) {
			eKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_R) {
			rKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_T) {
			tKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_Y) {
			yKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_U) {
			uKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_I) {
			iKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_O) {
			oKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_P) {
			pKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_OPEN_BRACKET) {
			leftBracketKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_CLOSE_BRACKET) {
			rightBracketKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_F1) {
			f1KeyPressed = true;
		} else if (keyCode == KeyEvent.VK_F2) {
			f2KeyPressed = true;
		} else if (keyCode == KeyEvent.VK_F3) {
			f3KeyPressed = true;
		} else if (keyCode == KeyEvent.VK_F4) {
			f4KeyPressed = true;
		} else if (keyCode == KeyEvent.VK_F5) {
			f5KeyPressed = true;
		} else if (keyCode == KeyEvent.VK_F6) {
			f6KeyPressed = true;
		} else if (keyCode == KeyEvent.VK_F7) {
			f7KeyPressed = true;
		} else if (keyCode == KeyEvent.VK_F8) {
			f8KeyPressed = true;
		} else if (keyCode == KeyEvent.VK_F9) {
			f9KeyPressed = true;
		} else if (keyCode == KeyEvent.VK_F10) {
			f10KeyPressed = true;
		} else if (keyCode == KeyEvent.VK_F11) {
			f11KeyPressed = true;
		} else if (keyCode == KeyEvent.VK_F12) {
			f12KeyPressed = true;
		} else if (keyCode == KeyEvent.VK_0) {
			zeroKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_1) {
			oneKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_2) {
			twoKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_3) {
			threeKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_4) {
			fourKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_5) {
			fiveKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_6) {
			sixKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_7) {
			sevenKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_8) {
			eightKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_9) {
			nineKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_MINUS) {
			minusKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_PLUS) {
			plusKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_SPACE){
			spaceKeyPressed = !spaceKeyPressed;
		}
		
		
		// Switch based on the current LightDJ state
		switch(lightDJState) {

		case LIGHTDJ_STATE_NORMAL:
			if (spaceKeyPressed) {
				activeLayer = true;
				lightDJState = LightDJState.LIGHTDJ_STATE_CHOOSING_VISUALIZER;
				
			} else if (keyCode == KeyEvent.VK_LEFT) {
				if (controlKeyPressed) {
					startAutoCrossfade(-CROSSFADE_SPEED_SLOW);
				} else if (altKeyPressed) {
					startAutoCrossfade(-CROSSFADE_SPEED_FAST);
				} else {
					// Left arrow - cross-fade all the way to the left if not automated
					alpha = 0.0;
					crossfaderKnob.setValue((float) alpha);
				}
				
			} else if (keyCode == KeyEvent.VK_RIGHT) {
				if (controlKeyPressed) {
					startAutoCrossfade(CROSSFADE_SPEED_SLOW);
				} else if (altKeyPressed) {
					startAutoCrossfade(CROSSFADE_SPEED_FAST);
				} else {
					// Right arrow - cross-fade all the way to the right if not automated
					alpha = 1.0;
					crossfaderKnob.setValue((float) alpha);
				}
				
			} else if (keyCode == KeyEvent.VK_DOWN) {
				// Auto cross-fade to the opposite direction
				if (alpha > 0.5) {
					// Cross-fade left
					if (controlKeyPressed) {
						startAutoCrossfade(-CROSSFADE_SPEED_SLOW);
					} else if (altKeyPressed) {
						startAutoCrossfade(-CROSSFADE_SPEED_FAST);
					} else {
						alpha = 0.0;
						crossfaderKnob.setValue((float) alpha);
					}
					
				} else {
					// Cross-fade right
					if (controlKeyPressed) {
						startAutoCrossfade(CROSSFADE_SPEED_SLOW);
					} else if (altKeyPressed) {
						startAutoCrossfade(CROSSFADE_SPEED_FAST);
					} else {
						alpha = 1.0;
						crossfaderKnob.setValue((float) alpha);
					}
				}
								
			} else if (keyCode == KeyEvent.VK_SHIFT) {
				// The shift key is a trigger to start entering the pulse
				pulseKeeper.startEnteringPulses();
				shiftKeyPressed = true;
			} else if (keyCode == KeyEvent.VK_Q) {
				// If the shift key is being held down, then this is a trigger to enter a pulse.
				if (shiftKeyPressed) {
					pulseKeeper.enterPulse();
				}
			} else if (keyCode == KeyEvent.VK_Z) {// Auto cross-fade to the opposite direction
				if (alpha > 0.5) {
					// Cross-fade left
					if (controlKeyPressed) {
						startAutoCrossfade(-CROSSFADE_SPEED_SLOW);
					} else if (altKeyPressed) {
						startAutoCrossfade(-CROSSFADE_SPEED_FAST);
					} else {
						alpha = 0.0;
						crossfaderKnob.setValue((float) alpha);
					}
					
				} else {
					// Cross-fade right
					if (controlKeyPressed) {
						startAutoCrossfade(CROSSFADE_SPEED_SLOW);
					} else if (altKeyPressed) {
						startAutoCrossfade(CROSSFADE_SPEED_FAST);
					} else {
						alpha = 1.0;
						crossfaderKnob.setValue((float) alpha);
					}
				}
			}
			
			break;
			
		case LIGHTDJ_STATE_CHOOSING_VISUALIZER:
			if (!spaceKeyPressed) {
				activeLayer = false;
				lightDJState = LightDJState.LIGHTDJ_STATE_NORMAL;
			} else if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
				int rawIndex = keyCode - KeyEvent.VK_A;
				if (rawIndex >= visualizers.size()) {
					rawIndex = visualizers.size() - 1;
				}
				
				// Choose which to modify - the left or the right plugin
				int activePlugin = (int) (1.0 - Math.round(alpha));
				
				// Set that effect
				chooseVisualizer(rawIndex, activePlugin);
				activeLayer = false;
				spaceKeyPressed = !spaceKeyPressed;
				lightDJState = LightDJState.LIGHTDJ_STATE_NORMAL;
			}
		
			break;
		
		}
	}
	
	public void keyUp(int keyCode) {
		// Record which key was just released
		if (keyCode == KeyEvent.VK_CONTROL) {
			controlKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_ALT) {
			altKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_ENTER) {
			enterKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_SHIFT) {
			// The shift key is a trigger to enter the pulse!
			pulseKeeper.stopEnteringPulses();
			shiftKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_BACK_SPACE) {
			backspaceKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_TAB) {
			tabKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_A) {
			aKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_S) {
			sKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_D) {
			dKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_F) {
			fKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_G) {
			gKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_H) {
			hKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_J) {
			jKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_K) {
			kKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_L) {
			lKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_SEMICOLON) {
			semiColonKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_Q) {
			qKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_W) {
			wKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_E) {
			eKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_R) {
			rKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_T) {
			tKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_Y) {
			yKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_U) {
			uKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_I) {
			iKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_O) {
			oKeyPressed = true;
		} else if (keyCode == KeyEvent.VK_P) {
			pKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_OPEN_BRACKET) {
			leftBracketKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_CLOSE_BRACKET) {
			rightBracketKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_F1) {
			f1KeyPressed = false;
		} else if (keyCode == KeyEvent.VK_F2) {
			f2KeyPressed = false;
		} else if (keyCode == KeyEvent.VK_F3) {
			f3KeyPressed = false;
		} else if (keyCode == KeyEvent.VK_F4) {
			f4KeyPressed = false;
		} else if (keyCode == KeyEvent.VK_F5) {
			f5KeyPressed = false;
		} else if (keyCode == KeyEvent.VK_F6) {
			f6KeyPressed = false;
		} else if (keyCode == KeyEvent.VK_F7) {
			f7KeyPressed = false;
		} else if (keyCode == KeyEvent.VK_F8) {
			f8KeyPressed = false;
		} else if (keyCode == KeyEvent.VK_F9) {
			f9KeyPressed = false;
		} else if (keyCode == KeyEvent.VK_F10) {
			f10KeyPressed = false;
		} else if (keyCode == KeyEvent.VK_F11) {
			f11KeyPressed = false;
		} else if (keyCode == KeyEvent.VK_F12) {
			f12KeyPressed = false;
		} else if (keyCode == KeyEvent.VK_0) {
			zeroKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_1) {
			oneKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_2) {
			twoKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_3) {
			threeKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_4) {
			fourKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_5) {
			fiveKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_6) {
			sixKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_7) {
			sevenKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_8) {
			eightKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_9) {
			nineKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_MINUS) {
			minusKeyPressed = false;
		} else if (keyCode == KeyEvent.VK_PLUS) {
			plusKeyPressed = false;
		} 
		
		
	}

	
	/**
	 * Automated cross-fade effects
	 */
	public void startAutoCrossfade(final double speed) {
		crossfadeSpeed = speed;
		if (crossfadeAutomator == CrossfadeAutomator.CROSSFADE_AUTO) {
			// Already cross-fading don't start a new thread!
			return;
		}
		
		crossfadeAutomator = CrossfadeAutomator.CROSSFADE_AUTO;
		
		(new Thread(new Runnable() {
			public void run() {
				while(crossfadeAutomator == CrossfadeAutomator.CROSSFADE_AUTO) {
					automateCrossfades();
			
					// Wait a little bit
					try {
						Thread.sleep(30);
					} catch (Exception e) {
						// Couldn't sleep
						e.printStackTrace();
					}
				}
				// System.out.println("Done with automatic crossfade.");
			}
		})).start();
	}
	
	
	public void automateCrossfades() {
		switch (crossfadeAutomator) {
		case CROSSFADE_MANUAL:
			// Don't do anything!
			break;
			
		case CROSSFADE_AUTO:
			// Move over to the left.
			alpha += crossfadeSpeed;
			if (alpha < 0.0) {
				alpha = 0.0;
				crossfadeAutomator = CrossfadeAutomator.CROSSFADE_MANUAL;
				crossfaderKnob.setIsHot(false);
				crossfaderKnob.setValue((float) alpha);
			} else if (alpha > 1.0) {
				alpha = 1.0;
				crossfadeAutomator = CrossfadeAutomator.CROSSFADE_MANUAL;
				crossfaderKnob.setIsHot(false);
				crossfaderKnob.setValue((float) alpha);;
			} else {
				crossfaderKnob.setIsHot(true);
				crossfaderKnob.setValue((float) alpha);
			}
			break;
			
			
		}
	}
	
	public void endAutomaticCrossfade() {
		// Disable any automated cross-fading
		crossfadeAutomator = CrossfadeAutomator.CROSSFADE_MANUAL;
	}
	
	
	/**
	 * MIDI Processing events
	 */
	
	public void setupMIDIControllers() {
		
		// Initialize some data structures
		midiLeftPluginIndices = new HashMap<Integer, Integer>();
		midiRightPluginIndices = new HashMap<Integer, Integer>();
		midiGeneralSliderIndices = new HashMap<Integer, Integer>();
		midiGeneralButtonIndices = new HashMap<Integer, Integer>();
		midiVisualizerButtonIndices = new HashMap<Integer, Integer>();
		midiPianoButtonIndices = new HashMap<Integer, Integer>();
		midiSpecialButtonIndices = new HashMap<Integer, Integer>();
		midiCrossfaderChannel = 0;
		
		// Parse the configuration file		
		// Left plugins
		String pluginChannels = ConfigFileParser.getSettingOrDefault("MIDI_LEFT_PLUGIN_CHANNELS", "");
		String[] channels = pluginChannels.split(",");
		for(int i = 0; i < channels.length; i++) {
			try {
				midiLeftPluginIndices.put(Integer.parseInt(channels[i].trim()), i);
			} catch (Exception e) {
				System.out.println("Error in configuration file, MIDI section!");
				e.printStackTrace();
			}
		}
		
		// Right plugins
		pluginChannels = ConfigFileParser.getSettingOrDefault("MIDI_RIGHT_PLUGIN_CHANNELS", "");
		channels = pluginChannels.split(",");
		for(int i = 0; i < channels.length; i++) {
			try {
				midiRightPluginIndices.put(Integer.parseInt(channels[i].trim()), i);
			} catch (Exception e) {
				System.out.println("Error in configuration file, MIDI section!");
				e.printStackTrace();
			}
		}
		
		/*// General buttons plugins
		pluginChannels = ConfigFileParser.getSettingOrDefault("MIDI_BUTTONS", "");
		channels = pluginChannels.split(",");
		for(int i = 0; i < channels.length; i++) {
			try {
				midiGeneralButtonIndices.put(Integer.parseInt(channels[i].trim()), i);
			} catch (Exception e) {
				System.out.println("Error in configuration file, MIDI section!");
				e.printStackTrace();
			}
		}*/
		
		//Piano plugin buttons
		pluginChannels = ConfigFileParser.getSettingOrDefault("MIDI_PIANO_BUTTONS", "");
		channels = pluginChannels.split(",");
		for(int i = 0; i < channels.length; i++) {
			try {
				midiPianoButtonIndices.put(Integer.parseInt(channels[i].trim()), i);
			} catch (Exception e) {
				System.out.println("Error in configuration file, MIDI section!");
				e.printStackTrace();
			}
		}
		
		//Piano plugin buttons
		pluginChannels = ConfigFileParser.getSettingOrDefault("MIDI_SPECIAL_BUTTONS", "");
		channels = pluginChannels.split(",");
		for(int i = 0; i < channels.length; i++) {
			try {
				midiSpecialButtonIndices.put(Integer.parseInt(channels[i].trim()), i);
			} catch (Exception e) {
				System.out.println("Error in configuration file, MIDI section!");
				e.printStackTrace();
			}
		}
		
		// Plugin controlling buttons
		// General buttons plugins
		pluginChannels = ConfigFileParser.getSettingOrDefault("MIDI_VISUALIZER_BUTTONS", "");
		channels = pluginChannels.split(",");
		for(int i = 0; i < channels.length; i++) {
			try {
				midiVisualizerButtonIndices.put(Integer.parseInt(channels[i].trim()), i);
			} catch (Exception e) {
				System.out.println("Error in configuration file, MIDI section!");
				e.printStackTrace();
			}
		}
		
		// General sliders
		pluginChannels = ConfigFileParser.getSettingOrDefault("MIDI_GENERAL_SLIDER_CHANNELS", "");
		channels = pluginChannels.split(",");
		for(int i = 0; i < channels.length; i++) {
			try {
				midiGeneralSliderIndices.put(Integer.parseInt(channels[i].trim()), i);
			} catch (Exception e) {
				System.out.println("Error in configuration file, MIDI section!");
				e.printStackTrace();
			}
		}
		
		// MIDI crossfader
		pluginChannels = ConfigFileParser.getSettingOrDefault("MIDI_CROSSFADER_CHANNEL", "");
		channels = pluginChannels.split(",");
		if (channels.length > 1) {
			System.out.println("Error in configuration file: MIDI_CROSSFADER_CHANNEL may only contain one value!");
		}
		if (channels.length == 1) {	// Ignore if 0 specified
			try {
				midiCrossfaderChannel = Integer.parseInt(channels[0].trim());
			} catch (Exception e) {
				System.out.println("Error in configuration file, MIDI_CROSSFADER_CHANNEL line!");
				e.printStackTrace();
			}
		}
		
		// Set up MIDI communication
		midiConnector = new MidiConnector();
		midiConnector.connectToMIDIControllers(this);
	}
	
	public void processMIDIEvent(MidiMessage message) {
		// For now, only deal with short messages
		if (message instanceof ShortMessage) {
			ShortMessage event = (ShortMessage) message;

			// See if the channel corresponding to this event matches one of the ones specified by the
			// configuration file
			int command = event.getCommand();
			int channel = event.getData1();
			int index;

			if (command == 176) { // Slider event
				if (channel == midiCrossfaderChannel) {
					// Crossfade it!
					updateControl(crossfaderKnob, event);

				} else if (midiLeftPluginIndices.containsKey(channel)) {
					index = midiLeftPluginIndices.get(channel);
					List<UserControl> userControls = visualizers.get(visualizerLeftIndex).getRequestedUserControls();
					if (index < userControls.size()) {
						// We have a valid mapping!
						updateControl(userControls.get(index), event);
					}
				} else if (midiRightPluginIndices.containsKey(channel)) {
					index = midiRightPluginIndices.get(channel);
					List<UserControl> userControls = visualizers.get(visualizerRightIndex).getRequestedUserControls();
					if (index < userControls.size()) {
						// We have a valid mapping!
						updateControl(userControls.get(index), event);
					}
				} else if (midiGeneralSliderIndices.containsKey(channel)) {

					// One of the special sliders was changed! Handle these in a custom way.
					index = midiGeneralSliderIndices.get(channel);
					if (index == 0) {
						// Process the volume knob!
						List<UserControl> userControls = volumePostProcessor.getRequestedUserControls();
						UserControl volumeKnob = userControls.get(0);
						updateControl(volumeKnob, event);
					} else if (index == 1) {
						// Process the volume knob!
						List<UserControl> userControls = whiteBurst.getRequestedUserControls();
						UserControl hueKnob = userControls.get(2);
						updateControl(hueKnob, event);
					}
				}

			} else if (command == 144) {// Button press event
				boolean pushed = (event.getData2() != 0 );  //== 127);    <-- altered this because I use piano keys, which are touch sensitive

				if (midiVisualizerButtonIndices.containsKey(channel)) {
					// Change one of the visualizers!
					if (pushed) {
						index = midiVisualizerButtonIndices.get(channel);

						switch(index) {
						case 0:  // Left left
							visualizerLeftIndex--;
							if (visualizerLeftIndex==visualizerRightIndex || visualizerLeftIndex + visualizers.size() == visualizerRightIndex){
								visualizerLeftIndex--;
							}
							if (visualizerLeftIndex < 0) {
								visualizerLeftIndex += visualizers.size();
							}
							loadVisualizerPlugin(true, visualizerLeftIndex);
							break;

						case 1:  // Left right	
							visualizerLeftIndex++;
							if (visualizerLeftIndex==visualizerRightIndex || visualizerLeftIndex - visualizers.size() == visualizerRightIndex){
								visualizerLeftIndex++;
							}
							if (visualizerLeftIndex >= visualizers.size()) {
								visualizerLeftIndex -= visualizers.size();
							}
							loadVisualizerPlugin(true, visualizerLeftIndex);
							break;
							
						case 2:
							spaceKeyPressed = !spaceKeyPressed;
							if (spaceKeyPressed) {
								activeLayer = true;
								lightDJState = LightDJState.LIGHTDJ_STATE_CHOOSING_VISUALIZER;
							}else if (!spaceKeyPressed) {
								activeLayer = false;
								lightDJState = LightDJState.LIGHTDJ_STATE_NORMAL;
							}
							break;

						case 3:  // Right left
							visualizerRightIndex--;
							if (visualizerRightIndex == visualizerLeftIndex || visualizerRightIndex + visualizers.size() == visualizerLeftIndex) {
								visualizerRightIndex--;
							}
							if (visualizerRightIndex < 0) {
								visualizerRightIndex += visualizers.size();
							}
							loadVisualizerPlugin(false, visualizerRightIndex);
							break;

						case 4:  // Right right
							visualizerRightIndex++;
							if (visualizerRightIndex == visualizerLeftIndex || visualizerRightIndex - visualizers.size() == visualizerLeftIndex) {
								visualizerRightIndex++;
							}
							if (visualizerRightIndex >= visualizers.size()) {
								visualizerRightIndex -= visualizers.size();
							}
							loadVisualizerPlugin(false, visualizerRightIndex);
							break;

						default:
							// Error! Don't do anything

						}
					}
				}
				else if (midiPianoButtonIndices.containsKey(channel)) {  //processes the piano keys for finger piano
					if (pushed) {
						index = midiPianoButtonIndices.get(channel);

						switch(index) {
						case 0:  // Left left
							aKeyPressed = true;
							break;

						case 1:  // Left right
							sKeyPressed = true;
							break;

						case 2:  // Right left
							dKeyPressed = true;
							break;

						case 3:  // Right right
							fKeyPressed = true;
							break;
						case 4:  // Left left
							jKeyPressed = true;
							break;

						case 5:  // Left right
							kKeyPressed = true;
							break;

						case 6:  // Right left
							lKeyPressed = true;
							break;

						case 7:  // Right right
							semiColonKeyPressed = true;
							break;	

						default:
							// Error! Don't do anything

						}
					}

				}
				else if (midiSpecialButtonIndices.containsKey(channel)) {  //processes the buttons that will trigger strobe
					// Change one of the visualizers!
					if (pushed) {
						index = midiSpecialButtonIndices.get(channel);

						switch(index) {
						case 0:  // Left left
							f12KeyPressed = true;
							break;

						case 1:  // Left right
							f8KeyPressed = true;
							break;

						case 2:  // Right left
							f5KeyPressed = true;
							break;

						case 3:  // Right right
							backspaceKeyPressed = true;
							break;

						default:
							// Error! Don't do anything

						}
					}
				}

			}

			else if (command == 128) {// Button release event

				if (midiPianoButtonIndices.containsKey(channel)) { 

					index = midiPianoButtonIndices.get(channel);

					switch(index) {
					case 0:
						aKeyPressed = false;
						break;

					case 1: 
						sKeyPressed = false;
						break;

					case 2:  
						dKeyPressed = false;
						break;

					case 3: 
						fKeyPressed = false;
						break;
					case 4:  
						jKeyPressed = false;
						break;

					case 5: 
						kKeyPressed = false;
						break;

					case 6:  
						lKeyPressed = false;
						break;

					case 7:  
						semiColonKeyPressed = false;
						break;	

					default:
						// Error! Don't do anything


					}

				}
				else if (midiSpecialButtonIndices.containsKey(channel)) {
					// Change one of the visualizers!

					index = midiSpecialButtonIndices.get(channel);

					switch(index) {
					case 0:  
						f12KeyPressed = false;
						break;

					case 1: 
						f8KeyPressed = false;
						break;

					case 2:  
						f5KeyPressed = false;
						break;

					case 3:  
						backspaceKeyPressed = false;
						break;

					default:
						// Error! Don't do anything


					}
				}

			}
		}
	}
	
	// Update a user control by setting it to a new value from a MIDI message
	private void updateControl(UserControl control, ShortMessage event) {
		float value = event.getData2() / 127.0f;
		control.setValue(value);
	}
	
	
	
	/**
	 * Auto-DJ functionality!!!
	 */
	protected int STEPS_PER_TRANSITION = 500;
	protected int autoDJStepCounter = 0;
	protected boolean useAutoDJ = false;
	
	public void autoDJStep() {

		if (useAutoDJ) {
			autoDJStepCounter++;
			if (autoDJStepCounter == STEPS_PER_TRANSITION) {
				// Transition
				System.out.println("AutoDJ - transitioning!");
				
				// Determine which is the active plugin
				int activePlugin = (int) (1.0 - Math.round(alpha));
				
				// Select a random plugin
				int randPluginIndex = selectRandomAutoPlugin();
				
				// Load that plugin!
				chooseVisualizer(randPluginIndex, activePlugin);
				
				// Start a fast auto transition
				if (alpha > 0.5) {
					startAutoCrossfade(-CROSSFADE_SPEED_FAST);
				} else {
					startAutoCrossfade(CROSSFADE_SPEED_FAST);
				}
				
				
				// Reset the hacky step counter.... do this better when you have more time and this isn't right before the party!!! ;-)
				autoDJStepCounter = 0;
			}
		
		
		}
		
		
		
	}
	
	// Select a random plugin that can autoDJ!
	public int selectRandomAutoPlugin() {
		while(true) {
			int randomIndex = (int) (visualizers.size() * Math.random());
			Visualizer viz = visualizers.get(randomIndex);
			if (viz.canAutoDJ()) {
				return randomIndex;
			}
			
		}
	}
	
	
	
}


class RenderFrameParty extends RenderFrame {
	public ColorOutput[] colorOutputs;
	public ColorOutput finalOutput;
	public FeatureList featureList;
}

class RenderTask extends TimerTask {
	private VisualizationEngineParty engine;
	public RenderTask(VisualizationEngineParty eng) {engine = eng;}
	public void run() {
		try {
			engine.renderDJ();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
