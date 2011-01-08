package GenreClassifier;

/**
 * Represents information about a song file
 * @author Steve Levine
 *
 */
public class SongFileInfo {
	public String title;
	public String filename;
	public String genre;
	
	public SongFileInfo(String title, String filename, String genre) {
		this.title = title;
		this.filename = filename;
		this.genre = genre;
	}
	
}
