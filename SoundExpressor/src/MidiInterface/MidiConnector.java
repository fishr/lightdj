package MidiInterface;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import SoundEngine.VisualizationEngineParty;

/**
 * This class connects the LightDJ software with any external MIDI devices.
 * The idea is that you can use an external MIDI controller to operate the
 * LightDJ software!
 * @author steve
 *
 */
public class MidiConnector {
	
	protected ReceiverCatcher rec;
	
	public void connectToMIDIControllers(VisualizationEngineParty engine) {
		System.out.println("Connecting to MIDI controllers...");
		
		rec = new ReceiverCatcher(engine);
		
		// Iterate through available MIDI devices, and try to open them and get
		// at their events.
		MidiDevice.Info[] infos =  MidiSystem.getMidiDeviceInfo();
		for(MidiDevice.Info info : infos) {
			try {
				MidiDevice dev = MidiSystem.getMidiDevice(info);
				dev.open();
				Transmitter trans = dev.getTransmitter();
				trans.setReceiver(rec);
				
			} catch (Exception e) {
				// Couldn't open this one. Perhaps it doesn't sent events, or is already
				// open by another program.
			}
			
		}
	}
}

class ReceiverCatcher implements Receiver {

	protected VisualizationEngineParty engine;
	
	public ReceiverCatcher(VisualizationEngineParty engine) {
		this.engine = engine;
	}

	@Override
	public void send(MidiMessage message, long timeStamp) {
		// We received a message! Call the visualziation engine.
		engine.processMIDIEvent(message);
	}
	
	@Override
	public void close() {
		
	}
	
}