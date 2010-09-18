package Utils;

/** 
 * Helps profile code and tells you how fast it's running
 * @author steve
 *
 */
public class TimerTicToc {

	final int N = 50;
	long[] times;
	int n;
	int index;
	
	long startTime;
	long endTime;
	
	public TimerTicToc() {
		times = new long[N];
		n = 0;
		index = 0;
	}
	
	public void tic() {
		startTime = System.nanoTime();
	}
	
	public void toc() {
		endTime = System.nanoTime();
		
		// Compute the difference!
		times[index] = (endTime - startTime);
		index = (index + 1) % N;
		if (n < N) {
			n++;
		}
	}
	
	
	public double getAverageTime() {
		// Return the average time in milliseconds
		long sum = 0;
		for(int i = 0; i < n; i++) {
			sum += times[i];
		}
		
		return ((double) sum) / n / 1000000.0;
		
	}
	
}
