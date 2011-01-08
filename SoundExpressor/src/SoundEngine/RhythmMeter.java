package SoundEngine;

/** Measures the time difference between bass beats.
 * By examining the distribution of this, it is possible to detect the tempo, and perhaps even the
 * "feel" of the music in the bass.
 * @author Steve Levine
 *
 */
public class RhythmMeter  {

	
	protected double updatesPerSecond;
	
	private enum BASS_RHTYHM_STATE {
		STATE_LOW_BASS,
		STATE_HIGH_BASS,
	};
	
	protected int T;
	private boolean updateReady;
	protected BASS_RHTYHM_STATE state; 
	
	public RhythmMeter(int sampleRate, int fftSize) {
		
		// Calculate some parameters
		updatesPerSecond = 1.0 * sampleRate / fftSize; 

		// Initialize
		T = 0;
		updateReady = false;
		state = BASS_RHTYHM_STATE.STATE_LOW_BASS;
		
		
	}
	
	public boolean update(double bassLevel) {
		// What to do depends on our current state.
		switch(state) {
		case STATE_LOW_BASS:
			if (bassLevel <= 0.0) {
				// Still low; increment T
				T++;
			} else {
				// Bass got high;
				T++;
				state = BASS_RHTYHM_STATE.STATE_HIGH_BASS;
				updateReady = true; // Ready to output this value
			}
			
			break;
			
		case STATE_HIGH_BASS:
			if (bassLevel <= 0.0) {
				// Bass got low
				// Remove the past output - if the consumer is doing it's job, it has been received.
				T = 0;
				updateReady = false;
				state = BASS_RHTYHM_STATE.STATE_LOW_BASS;
			} else {
				// Bass still high
				// Don't do anything - keep T frozen.
			}
			
			break;
		}
		
		return updateReady;
	}
	
	public int getDeltaTime() {
		updateReady = false; // Don't give the same output multiple times.
		return T;
	}
	
}
