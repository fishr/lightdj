package Signals;

/**
 * Represents a faster Fast Fourier Transform (FFT) engine. Specific to a given FFT size.
 * The size must range from 2 to 2^31, and be a power of 2.
 * 
 * Trades away memory and pre-computation time, in favor of fast FFT computation time.
 * 
 * Upon being initialized, the FFT does a bunch of pre-computations to prepare itself.
 * This allows each FFT call to be run faster and more efficiently.
 * 
 * @author Steve Levine
 *
 */
public class FFTEngine {
	
	// Some useful fields for this sized FFT
	private int N;		// The size of the FFT, i.e., 1024
	private int v;		// log2(N)
	private double fs;	// The sample rate
	
	private int[] bitReversedIndices;		// Map regular index to bit-reversed index
	private double[][] WnPowers;			// Wn = exp(-j*2 PI / N), to several powers
	private int[][][] butterflyMappings;	// Each of the v entries contains N/2 entries which are triples (p, q, k), such that the
											// butterfly computation is executed on values with indices p and q,
											// and the complex scale WnPowers[k] is used.
	
	
	public FFTEngine(int fft_size, double fs) {
		N = fft_size;
		v = (int) Math.round(Math.log(N)/Math.log(2));
		this.fs = fs;
		
		// Precompute lots of stuff, so we can fly on each FFT
		prepareEngine();
	}
	
	// Prepare the engine, by performing useful precomputations to save time.
	// This doesn't need to be fast, since it is only run once at the beginning.
	private void prepareEngine() {
		
		// Set up the bit-reversed indices
		bitReversedIndices = new int[N];
		for(int i = 0; i < N; i++) {
			int bitReverse = 0;
			for(int j = 0; j < v; j++) {
				bitReverse |= ( (i >> (v - j - 1)) & 0x01  ) << j;
			}
			bitReversedIndices[i] = bitReverse;
		}
		
		
		// Pre-compute Wn raised to the relevant powers
		WnPowers = new double[N/2][2];
		for(int k = 0; k < N/2; k++) {
			double theta = - 2*Math.PI / N * k;
			WnPowers[k][0] = Math.cos(theta);
			WnPowers[k][1] = Math.sin(theta);
		}
		
		
		// Precompute the ordering and Wn usage for each butterfly computation to be performed,
		// for each v levels of the FFT.
		butterflyMappings = new int[v][N/2][3];
		// Iterate over each of the v levels of FFT butterfly computations
		for(int L = 1; L <= v; L++) {
			
			int pairIndex = 0;
			int WnIndex = 0;
			int B = (1 << L); // The "block size" associated with this FFT level
			
			int i = 0;
			while(i < N) {
				for(int j = 0; j < B/2; j++) {
					butterflyMappings[L - 1][pairIndex][0] = i + j;
					butterflyMappings[L - 1][pairIndex][1] = i + j + B/2;
					butterflyMappings[L - 1][pairIndex][2] = WnIndex;
				
					WnIndex = (WnIndex + (1 << (v - L))) % (N/2);
					pairIndex++;
				}
				i += B;
			}
			
			
		}
		
		// All done!
		
	}
	
	
	// Compute the FFT! This algorithm is optimized to use butterfly computations.
	// Additionally, the main calculation consists solely of additions, multiplications,
	// and array reads/writes. Computes in place. No Java class instantiations, memory allocations, 
	// function calls, etc. - this is to keep things as fast as possible.
	//
	public FFT computeFFT(double[] x) {
		
		// Basic error checking
		if (x.length != N) {
			throw new RuntimeException("Error: Invalid length signal for the given sized FFT!");
		}
		
		
		// Start the list of complex registers
		// Initialize with bit-reversed order input
		double[][] X = new double[N][2];
		for(int i = 0; i < N; i++) {
			X[i][0] = x[bitReversedIndices[i]];
			X[i][1] = 0;
		}
		
		// Start the v levels of FFT butterfly computations!
		for(int m = 0; m < v; m++) {
			// Execute the butterfly computations, using the pre-computed ordering
			for(int butterflyIndex = 0; butterflyIndex < N / 2; butterflyIndex++) {
				// Retrieve the two indices
				int p = butterflyMappings[m][butterflyIndex][0];
				int q = butterflyMappings[m][butterflyIndex][1];
				int WnIndex = butterflyMappings[m][butterflyIndex][2];
				
				double deltaR = WnPowers[WnIndex][0] * X[q][0] - WnPowers[WnIndex][1] * X[q][1];
				double deltaI = WnPowers[WnIndex][0] * X[q][1] + WnPowers[WnIndex][1] * X[q][0]; 
				
				X[q][0] = X[p][0] - deltaR;
				X[q][1] = X[p][1] - deltaI;
				
				X[p][0] = X[p][0] + deltaR;
				X[p][1] = X[p][1] + deltaI;
				
				
			}
		}
		
		
		// Done! Return a wrapper class holding the computed values with the sample rate.
		return new FFT(X, fs);
		
	}
	
	
}
