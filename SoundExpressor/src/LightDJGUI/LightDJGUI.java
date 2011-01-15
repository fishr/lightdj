package LightDJGUI;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.*;

import Signals.FFT;

/**
 * A form for displaying visualizations.
 * @author Steve Levine
 *
 */
public class LightDJGUI extends JPanel implements ComponentListener {
	
	// Graphics and GUI-related variables
	protected final static int SIDEBAR_WIDTH = 350;
	BufferedImage background = null;
	BufferedImage turntableLogo = null;
	ScrollingSpectrum spectrumMapper;
	
	
	// Display the color output on the GUI correctly
	ColorOutputDisplayer colorOutputDisplayer;
	
	// The most recently computed FFT
	FFT latestFFT;
	
	// Useful constants
	protected int BUFFER_SIZE;
	protected double SAMPLE_RATE;
	
	/**
	 * Call the super's constructor.
	 */
	public LightDJGUI(int bufferSize, int sampleRate) {
		super();
		BUFFER_SIZE = bufferSize;
		SAMPLE_RATE = sampleRate;
		
		// Choose a color output displayer
		colorOutputDisplayer = new ColorOutputDisplayerTwoLEDs();
		
	}
	
	public void updateWithFFT(FFT fft) {
		// Be sure to synchronize
		synchronized (this) {
			latestFFT = fft;
		}
		
		// Update anything that needs the new FFT
		spectrumMapper.updateWithNewSpectrum(fft.getFrequencies(), fft.getMagnitudes());
		
	}
	
	/**
	 * Renders the GUI.
	 * Called at a specified rate by a separate thread, so as to not refresh unncessarily quickly.
	 */
	public void render() {
		// Use the most recently-updated FFT
		FFT fft;
		synchronized(this) {
			fft = latestFFT;
		}
		
		spectrumMapper.render();
		
	}
	
	
	
	/**
	 * Immediately create the form, and display it to the user.
	 */
	private void createAndShowGUI() {
		JFrame frame = new JFrame("Light DJ");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);
		frame.pack();
		setBackground(Color.BLACK);
        frame.setSize(1500, 1000);
		frame.setVisible(true);
		frame.addComponentListener(this);
		
		// Generate the background
		generateBackground();

		// Set up some other GUI elements
		spectrumMapper = new ScrollingSpectrum(25, 25, 300, 300, (Graphics2D) frame.getGraphics(), 30, 20000, 100.0, BUFFER_SIZE, SAMPLE_RATE);
		
		
		System.out.println("Light DJ started.");
	}
	// Try and load it


	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}

	public void componentResized(ComponentEvent e) {
		generateBackground();
	}
	
	@Override
	public void paint(Graphics g) {
		// Repaint the background
		Graphics2D g2D = (Graphics2D) g;
		g2D.drawImage(background, 0, 0, null);
		
	}

	/**
	 * Generate a pretty looking background image
	 */
	private void generateBackground() {
		// Allocate an image of the proper size
		int width = this.getWidth();
		int height = this.getHeight();
		background = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2D = (Graphics2D) background.getGraphics();
		
		// Paint a black background
		g2D.setBackground(Color.BLACK);
		g2D.clearRect(0, 0, width, height);
		
		// Draw the sidebar
		g2D.setColor(new Color(10,10,10));
		g2D.fillRect(0, 0, SIDEBAR_WIDTH, height);
		
		// Display the turntable image, loading it if necessary.
		if (turntableLogo == null) {
			// Try and load it
			try {
				turntableLogo = ImageIO.read(new File("Images/background.png"));
				g2D.drawImage(turntableLogo, SIDEBAR_WIDTH, 0, null);
			} catch (IOException e) {
				System.out.println("Warning: Could not load LightDJ background image!");
				e.printStackTrace();
			}
		} else {
			g2D.drawImage(turntableLogo, SIDEBAR_WIDTH, 0, null);
		}
		
		// Resize where the scrolling spectrum goes
		
		
	}

	

	public static LightDJGUI startGUI(int bufferSize, int sampleRate) {
		LightDJGUI gui = new LightDJGUI(bufferSize, sampleRate);
		gui.createAndShowGUI();
		return gui;
	}
	
}

/**
 * This will allow rendering the Light DJ in a separate thread.
 */
class RenderTask extends TimerTask {
	private LightDJGUI dj;
	public RenderTask(LightDJGUI dj) {this.dj = dj;}
	public void run() {dj.render();}
}
