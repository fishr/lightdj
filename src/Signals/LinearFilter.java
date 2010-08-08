package Signals;

/**
 * Implements a linear filter:
 * 
 * y[n] = a0*x[n] + a1*x[n - 1] + ... + b0*y[n - 1] + b1*y[n - 2]
 *
 */
public class LinearFilter {
	private double[] b_coeffs;
	private double[] a_coeffs;
	
	public LinearFilter(double[] a, double[] b) {
		b_coeffs = b;
		a_coeffs = a;
	}
	
	public int order() {return b_coeffs.length;}
	
	
	/**
	 * Filter the signal x!
	 * @param x
	 * @return
	 */
	public double[] filterSignal(double[] x) {
		
		double[] y_past = new double[b_coeffs.length];
		double[] x_past = new double[a_coeffs.length];
		
		double[] y = new double[x.length];
		
		for(int i = 0; i < x.length; i++) {
			// Compute!
			double sum = 0;
			for(int j = 0; j < x_past.length; j++) {
				sum += (a_coeffs[j] * x_past[j]);
			}
			
			for(int j = 0; j < y_past.length; j++) {
				sum += (b_coeffs[j] * y_past[j]);
			}
			
			y[i] = sum;
			
			// Now, shift everything!
			for(int j = x_past.length - 2; j >= 0; j--) {
				x_past[j + 1] = x_past[j];
			}
			x_past[0] = x[i];
			
			for(int j = y_past.length - 2; j >= 0; j--) {
				y_past[j + 1] = y_past[j];
			}
			y_past[0] = y[i];
			
		}
		
		return y;
		
	}
	
	/**
	 * Create an averaging filter
	 * @param order
	 * @return
	 */
	public static LinearFilter createAveragingFilter(int order) {
		double[] a = new double[order];
		double[] b = new double[1];
		
		for(int i = 0; i < order; i++) {
			a[i] = (double) 1 / order;
		}
		
		b[0] = 0;
		
		return new LinearFilter(a, b);
	}
	

	
	
}
	
	

