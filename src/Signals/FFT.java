package Signals;

/**
 * This class performs Fast Fourier Transforms (FFT's)
 * @author steve
 *
 */
public class FFT {

	private Complex[] fftValues;
	private double fs;
	
	public FFT(double[] signal, double fs) {
		this.fs = fs;
		fftValues = computeFFT(signal);
	}
	
	public double getNyquistFrequency() {
		return fs/2;
	}
	
	public double[] getMagnitudes() {
		double[] mags = new double[fftValues.length];
		
		for(int k = 0; k < fftValues.length; k++) {
			mags[k] = fftValues[k].radius();
		}
		
		return mags;
		
	}
	
	public double[] getFrequencies() {
		double[] freqs = new double[fftValues.length];
		
		double scale = fs / fftValues.length;
		
		for(int i = 0; i < fftValues.length; i++) {
			freqs[i] = scale * i;
		}
		
		return freqs;
	}
	
	public double[] getLogMagnitudes() {
		double[] mags = new double[fftValues.length];
		
		for(int k = 0; k < fftValues.length; k++) {
			mags[k] = Math.log10(fftValues[k].radius());
		}
		
		return mags;
	}
	
	/**
	 * Perform an FFT on the given signal
	 * @param signal - a double array of time-domain values
	 * @return - Returns a complex array corresponding to the FFT
	 * 
	 * PRECONDITION: ASSUMES THAT SIGNAL IS A POWER OF 2!
	 */
	
	
	private Complex[] computeFFT(double[] signal) {
		
		int N = signal.length;
		
		
		if (N == 1) {
			// Base case
			return new Complex[]{new Complex(signal[0])};
			
		} else if (N % 2 != 0) {
			// We weren't given a power of 2! Throw an exception.
			throw new RuntimeException("FFT Signal length is not a power of 2!");
		}
		
		// Make an even list, and an odd list
		double[] signal_even = new double[N/2];
		double[] signal_odd = new double[N/2];
		
		for(int m = 0; m < N/2; m++) {
			signal_even[m] = signal[2*m];
			signal_odd[m] = signal[2*m + 1];
		}
		
		// Recursively compute the FFT's of these half lists
		Complex[] fft_evens = computeFFT(signal_even);
		Complex[] fft_odds = computeFFT(signal_odd);
		
		// Assemble them into one FFT
		Complex[] fft_output = new Complex[N];
		for(int k = 0; k < N/2; k++) {
			Complex t = fft_evens[k];
			Complex delta = Complex.exp(-2*Math.PI*k/N).multiply(fft_odds[k]);
			fft_output[k] = t.add(delta);
			fft_output[k + N/2] = t.subtract(delta);
		}
		
		// Return the output!
		return fft_output;
		
	}
	
	
}
