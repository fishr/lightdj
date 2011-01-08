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

	
}
