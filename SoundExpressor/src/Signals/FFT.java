package Signals;

/**
 * This class contains a computed Fast Fourier Transforms (FFT's)
 * @author Steve Levine
 *
 */
public class FFT {

	private double[][] X;
	private double fs;
	
	public FFT(double[][] fftValues, double fs) {
		this.X = fftValues;
		this.fs = fs;
	}
	
	public double getNyquistFrequency() {
		return fs/2;
	}
	
	public double[] getMagnitudes() {
		double[] mags = new double[X.length];
		
		for(int k = 0; k < X.length; k++) {
			mags[k] = Math.sqrt(X[k][0]*X[k][0] + X[k][1]*X[k][1]);
		}
		
		return mags;
		
	}
	
	public double[] getFrequencies() {
		double[] freqs = new double[X.length];
		
		double scale = fs / X.length;
		
		for(int i = 0; i < X.length; i++) {
			freqs[i] = scale * i;
		}
		
		return freqs;
	}
	
	public double[] getLogMagnitudes() {
		double[] mags = new double[X.length];
		
		for(int k = 0; k < X.length; k++) {
			mags[k] = Math.log10(Math.sqrt(X[k][0]*X[k][0] + X[k][1]*X[k][1]));
		}
		
		return mags;
	}
	
	
	
	/**
	 * Perform an FFT on the given signal.
	 * 
	 * Although this recursive, divide-and-conquer algorithm works fine,
	 * I wrote a faster implementation in the FFT engine - which is why
	 * this one is deprecated.
	 * 
	 * @param signal - a double array of time-domain values
	 * @return - Returns a complex array corresponding to the FFT
	 * 
	 * PRECONDITION: ASSUMES THAT SIGNAL IS A POWER OF 2!
	 */
	public Complex[] computeFFT_deprecated(double[] signal) {
		
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
		Complex[] fft_evens = computeFFT_deprecated(signal_even);
		Complex[] fft_odds = computeFFT_deprecated(signal_odd);
		
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
