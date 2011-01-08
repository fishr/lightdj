package SoundEngine;

/**
 * Multiplexes signals
 * @author Steve Levine
 *
 */

public class SignalMultiplexer {

	protected int numChannels;
	
	protected double THRESHOLD = 0.5;
	protected int channelPointer;
	protected boolean state;
	protected double currentValue;
	
	public SignalMultiplexer(int numChannels) {
		this.numChannels = numChannels;
		channelPointer = 0;
		state = false;
		currentValue = 0;
	}
	
	public void update(double y) {
		if (state == false) {
			if (y >= THRESHOLD) {
				state = true;
				currentValue = 1.0;
			} else {
				// Do nothing
				currentValue = 0.0;
				
			}
		} else { // Already high
			if (y >= THRESHOLD) {
				currentValue = 1.0;
			} else {
				state = false;
				currentValue = 0.0;
				// Also increment to the next lights!
				channelPointer = (channelPointer + 1) % numChannels;
			}
			
		}
	}
	
	public double getChannelValue(int index) {
		if (index == channelPointer) {
			return currentValue;
		} else {
			return 0.0;
		}
	}
	
	
}
