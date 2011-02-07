package LightDJGUI;

/**
 * Responsible for keeping the pulse of the music, as played by the LightDJ.
 * @author Steve Levine
 *
 */
public class PulseKeeper {

	// Constants
	protected static double BEATS_PER_MEASURE = 4.0;
	
	// Timing information for the current pulse
	protected int beatsEntered;
	protected long startTime;
	protected long millisecondsBetweenPulses;
	
	public PulseKeeper() {
		beatsEntered = 0;
	}
	
	
	public void startEnteringPulses() {
		beatsEntered = 0;
		
	}
	
	public void enterPulse() {
		long now = System.currentTimeMillis();
		if (beatsEntered == 0) {
			// Sets the phase
			startTime = now;
		} else {
			// Compute the number of millisecond between each beat
			millisecondsBetweenPulses = (now - startTime) / beatsEntered;
		}
		
		
		
		
		beatsEntered++;
	}
	
	
	public void stopEnteringPulses() {
		
	}
	
	
	
	/**
	 * Return the current beat pulse as a double
	 * @return
	 */
	public double getPulse() {
		
		long now = System.currentTimeMillis();
		if (millisecondsBetweenPulses == 0) {
			return 0.0;
		}
		
		
		return 1.0 + (1.0 * (now - startTime) / millisecondsBetweenPulses) % BEATS_PER_MEASURE;
	}
	
	/**
	 * Return a triangle wave-like form from the pulse to simulate the bass. Varies from 0.0 to 1.0.
	 */
	public double getWavePulse() {
		long now = System.currentTimeMillis();
		if (millisecondsBetweenPulses == 0) {
			return 0.0;
		}
		
		long nearestBeat = ((int) ((now - startTime) / millisecondsBetweenPulses)) * millisecondsBetweenPulses + startTime;
		long offset = now - nearestBeat;
		
		//System.out.println(offset);
		
		return Math.max(1.0 - 1 / (0.3333) * offset / 1000.0, 0.0);
		
	}
	
	public double getBPM() {
		return 60000.0 / millisecondsBetweenPulses;
	}
	
	
}
