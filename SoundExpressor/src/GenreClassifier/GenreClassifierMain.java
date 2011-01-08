package GenreClassifier;

/**
 * @author Steve Levine
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.*;

import Utils.TimerTicToc;


public class GenreClassifierMain {

	//private static final String soundFilename = "/home/steve/Desktop/Music/HipHop/Replay.wav";
	//private static final String soundFilename = "/home/steve/Desktop/Music/HipHop/Deuces.wav";
	//private static final String soundFilename = "/home/steve/Desktop/Music/HipHop/Lollipop.wav";
	//private static final String soundFilename = "/home/steve/Desktop/Music/Music/HipHop/Replay.wav";
	//private static final String soundFilename = "/home/steve/Desktop/Music/Electronic/StereoDynamite.wav";
	//private static final String soundFilename = "/home/steve/Desktop/Music/Music/Electronic/Alejandro Remix.wav";
	//private static final String soundFilename = "/home/steve/Desktop/Music/Electronic/DJ_Got_Us_(Remix).wav";
	//private static final String soundFilename = "/home/steve/Desktop/Music/HipHop/Replay.wav";
	//private static final String soundFilename = "/home/steve/Desktop/Music/Music/Rock/Love_Bites.wav";
	private static final String soundFilename = "/home/steve/Desktop/Music/Music/Classical/classical_1.wav";

	private static final String songListFile = "/home/steve/Desktop/Music/SongList.csv";
	private static final String outputFeatureVectorFile = "/home/steve/Desktop/Music/SongFeatureVectors.csv";
	
	
	private static final int AUDIO_READ_BUFFER_SIZE = 256;
	
	public static void main(String[] args) {
		// Do stuff here
//		System.out.println("*** Music Statisics Generator ***");
//		System.out.println("   Reading song list file...");
//		
//		// Parse the song list file
//		ArrayList<SongFileInfo> songList = parseSongListFile(songListFile);
//		
//		System.out.println("   --> " + songList.size() + " songs");
//		System.out.println();
//		
//		// Open the output file
//		BufferedWriter out = null;
//		try {
//			out = new BufferedWriter(new FileWriter(outputFeatureVectorFile));
//			out.write(SongFeatureVector.getHeaderRow() + "\n");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		System.out.println("Processing songs:");
//		for(int i = 0; i < songList.size(); i++) {
//			// Print out some information
//			SongFileInfo info = songList.get(i);
//			System.out.println("   " + (i + 1) + "/" + songList.size() + " - " + info.title + " (" + info.genre + ")");
//			
//			// Compute the feature vector for this song
//			SongFeatureVector featureVector = runFromSoundFile(info.filename);
//			if (featureVector == null) {
//				// An error occured! Write a blank line to signify this.
//				System.out.println("        ** Got a null feature vector!");
//				try {
//					out.write(info.title + "," + info.filename + ", *,*,*\n");
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//			} else {
//				// Write out this feature vector to a file.
//				featureVector.title = info.title;
//				featureVector.filename = info.filename;
//				featureVector.actualGenre = info.genre;
//				
//				System.out.println("          " + featureVector.toString());
//				try {
//					out.write(featureVector.toString() + "\n");
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//			}
//			
//			
//		}
//		
//		
//		try {
//			out.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		System.out.println("Done!");
		
		runFromSoundFile(soundFilename);
		
	}
	
	
	public static ArrayList<SongFileInfo> parseSongListFile(String filename) {
		ArrayList<SongFileInfo> songList = new ArrayList<SongFileInfo>();
		
		// Open the file and read in lines
		BufferedReader inputStream;
		try {
			inputStream = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		String line;
		try {
			while ((line = inputStream.readLine()) != null) {
				if (!line.trim().equals("")) {
				
					// Split the line
					String[] fields = line.split(",");
					
					SongFileInfo info = new SongFileInfo(fields[0].trim(), fields[1].trim(), fields[2].trim());
					
					songList.add(info);
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return songList;
	}
	
	
	// Don't operate from live captured audio, but rather from a pre-recorded sound file.
	public static SongFeatureVector runFromSoundFile(String file) {
		// For now, attempt to play back from a clever music file.
		File songFile = new File(file);
		AudioInputStream audioInputStream;
		
		try {
			audioInputStream = AudioSystem.getAudioInputStream(songFile);
		} catch (UnsupportedAudioFileException e) {
			System.out.println("   --> ** Invalid audio file!");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			System.out.println("  --> ** Could not open audio file!");
			e.printStackTrace();
			return null;
		}
		
		try {
			AudioFormat format = audioInputStream.getFormat();
			SoundProcessingEngine engine = new SoundProcessingEngine(format);
			// Start sending it data!
			int bytesPerFrame = format.getFrameSize();
			int bytesToRead = AUDIO_READ_BUFFER_SIZE * bytesPerFrame;
			
			try {
				int numBytesRead = 0;
				byte[] audioData = new byte[bytesToRead];
				
				
				while((numBytesRead = audioInputStream.read(audioData)) != -1) {
					
					// Send data!
					engine.write(audioData, 0, numBytesRead);
					
				}
				
				
			} catch (Exception e) {
				System.out.println("   --> ** Error during audio processing!");
				e.printStackTrace();
				return null;
			}
		
			return engine.finish();
		} catch (Exception e) {
			// Something went wrong! Just return null.
			return null;
		}
	}

}

