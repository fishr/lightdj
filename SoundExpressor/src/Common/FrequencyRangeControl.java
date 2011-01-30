package Common;

/**
 * Represents a user control that allows the user to select a frequency range.
 * @author Steve Levine
 *
 */
public class FrequencyRangeControl implements UserControl {

	protected double minFreq;
	protected double maxFreq;
	
	public FrequencyRangeControl(double initialMinFreq, double initialMaxFreq) {
		minFreq = initialMinFreq;
		maxFreq = initialMaxFreq;
	}

	@Override
	public void render() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLocation(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	
}
