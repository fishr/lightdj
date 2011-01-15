package Utils;

public class JavaSignedByteTest {

	public static void main(String[] args) {
		for(int i = 0; i < 256; i++) {
			byte b = (byte) i;
			System.out.println(i + " => " + b);
		}
	}
	
}
