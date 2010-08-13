package SignalGUI;

import java.awt.*;
import javax.swing.*;

public class GUIVisualizer extends JPanel {
	
	
	public GUIVisualizer() {
		super();
	}
	
	
	private void createAndShowGUI() {
		JFrame frame = new JFrame("Sound Visualizer!");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.add(this);
		frame.pack();
		setBackground(Color.BLACK);
        frame.setSize(1100, 1000);
		frame.setVisible(true);
		System.out.println("GUI ready.");
	}
	
	public static GUIVisualizer makeGUI() {
		final GUIVisualizer panel = new GUIVisualizer();
		
		//javax.swing.SwingUtilities.invokeLater(new Runnable() {
		//	public void run() {
				panel.createAndShowGUI();
		//	}
		//});
		
		return panel;
	}

	
	private void outputGraph() {
		
	}
	
}
