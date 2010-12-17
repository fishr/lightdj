package Utils;

public class TimerAccuracyTest implements Runnable {

	public static void main(String[] args) {
		
		TimerAccuracyTest test = new TimerAccuracyTest();
		Thread t = new Thread(test);
		t.start();
		
	}
	
	public void run() {
		System.out.println("Testing clock accuracy:");
		
		int N = 100;
		long times[] = new long[N];
		
		
		for(int i = 0; i < N; i++) {
			times[i] = System.nanoTime();

				try {
					Thread.sleep(0, 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}
		
		
		for(int i = 1; i < N; i++) {
			System.out.println(times[i] - times[i - 1]);
		}
		
		while(true) {
			try {
				Thread.sleep(0,1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
