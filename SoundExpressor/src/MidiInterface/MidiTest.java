package MidiInterface;

import javax.sound.midi.*;

/**
 * This is a test script that lets connects to MIDI devices, and prints out any SHORT events it receives.
 * @author steve
 *
 */
public class MidiTest {

	public static void main(String[] args) {
		System.out.println("MIDI Test 1.0");
		
		System.out.println("The following MIDI devices exist:");
		MidiDevice.Info[] infos =  MidiSystem.getMidiDeviceInfo();
		
		ReceiverCatcherTest rec = new ReceiverCatcherTest();
		
		for(MidiDevice.Info info : infos) {
			System.out.println(info.getName());
			System.out.println("   " + info.getDescription());
		
			try {
				MidiDevice dev = MidiSystem.getMidiDevice(info);
				dev.open();
				Transmitter trans = dev.getTransmitter();
				trans.setReceiver(rec);
				
			} catch (Exception e) {
				// Nothing!
			}
			
		}
		
		System.out.println();
		System.out.println("Waiting for MIDI events ...");
		System.out.println("Will only print 2-byte (MIDI Short) messages.");
		System.out.println("Output will be in form:\n   <Command> : <Data1> , <Data2>");
		while(true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
}

class ReceiverCatcherTest implements Receiver {

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send(MidiMessage message, long timeStamp) {
		if (message instanceof ShortMessage) {
			ShortMessage shortMsg = (ShortMessage) message;
			System.out.println(shortMsg.getCommand() + " : " + shortMsg.getData1() + ", " + shortMsg.getData2());
		}	
	}
}