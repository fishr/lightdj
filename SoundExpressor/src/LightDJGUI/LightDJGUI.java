package LightDJGUI;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import Signals.FFT;
import SoundEngine.VisualizationEngineParty;

/**
 * A form for displaying visualizations.
 * @author Steve Levine
 *
 */
public class LightDJGUI extends JPanel implements MouseListener, MouseMotionListener, KeyListener  {
	
	
	
	protected VisualizationEngineParty engine;
	
	/**
	 * Call the super's constructor.
	 */
	public LightDJGUI(VisualizationEngineParty engine) {
		super();
		
		this.engine = engine;
		
		// Detect mouse events
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		this.setFocusable(true);
	}
	
	
	@Override
	public void paint(Graphics g) {
		engine.paintDJ(g);
		
	}

	
	/** 
	 * Mouse stuff
	 */
	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		engine.mouseDown(e.getX(), e.getY());
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		engine.mouseUp(e.getX(), e.getY());
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		engine.mouseDragged(e.getX(), e.getY());
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {}




	/**
	 * Keyboard stuff
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		engine.keyDown(e.getKeyCode());
		
	}


	@Override
	public void keyReleased(KeyEvent e) {
		engine.keyUp(e.getKeyCode());
		
	}


	@Override
	public void keyTyped(KeyEvent e) {}
	
}



