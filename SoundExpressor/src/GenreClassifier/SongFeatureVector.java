package GenreClassifier;

/**
 * Represents a feature vector that will be used for machine learning of song genre!
 * @author Steve Levine
 *
 */
public class SongFeatureVector {
	
	// Meta data
	public String title = "";
	public String filename = "";
	public String actualGenre = "";
	public String predictedGenre = "";
	
	
	// Bass stuff
	public double bass0;
	public double bass1;
	public double bass2; 
	public double bass3;
	
	public double bassMax;
	public double bassAve;
	public double bassSpread;
	public double bassPeak;
	
	
	// Mids stuff
	public double mids0;
	public double mids1;
	public double mids2;
	public double mids3;
	
	public double midsMax;
	public double midsAve;
	public double midsSpread;
	public double midsPeak;
	
	// Highs stuff
	public double highs0;
	public double highs1;
	public double highs2;
	public double highs3;
	
	public double highsMax;
	public double highsAve;
	public double highsSpread;
	public double highsPeak;
	
	
	// Overall level stuff
	public double level0;
	public double level1;
	public double level2;
	public double level3;
	
	public double levelMax;
	public double levelAve;
	public double levelSpread;
	public double levelPeak;
	
	
	// Rhythm stuff
	public double rhythmPeak;
	public double rhythmAve;
	
	public double[] vector() {
		
		double[] features = new double[34];
		features[0] = bassMax;
		features[1] = bassAve;
		features[2] = bassSpread;
		features[3] = bassPeak;
		features[4] = midsMax;
		features[5] = midsAve;
		features[6] = midsSpread;
		features[7] = midsPeak;
		features[8] = highsMax;
		features[9] = highsAve;
		features[10] = highsSpread;
		features[11] = highsPeak;
		features[12] = levelMax;
		features[13] = levelAve;
		features[14] = levelSpread;
		features[15] = levelPeak;
		features[16] = rhythmPeak;
		features[17] = rhythmAve;
		features[18] = bass0;
		features[19] = bass1;
		features[20] = bass2;
		features[21] = bass3;
		features[22] = mids0;
		features[23] = mids1;
		features[24] = mids2;
		features[25] = mids3;
		features[26] = highs0;
		features[27] = highs1;
		features[28] = highs2;
		features[29] = highs3;
		features[30] = level0;
		features[31] = level1;
		features[32] = level2;
		features[33] = level3;
		
		return features;
		
	}
	
	public String toString() {
		return title + "," + 
				filename + "," + 
				actualGenre + "," + 
				predictedGenre + "," + 
				bassMax  + "," + 
				bassAve  + "," + 
				bassSpread  + "," + 
				bassPeak + "," + 
				midsMax  + "," + 
				midsAve  + "," + 
				midsSpread  + "," + 
				midsPeak + "," + 
				highsMax  + "," + 
				highsAve  + "," + 
				highsSpread  + "," + 
				highsPeak + "," + 
				levelMax  + "," + 
				levelAve  + "," + 
				levelSpread  + "," + 
				levelPeak + "," +
				rhythmPeak  + "," + 
				rhythmAve  + "," + 
				bass0  + "," + 
				bass1  + "," + 
				bass2  + "," + 
				bass3  + "," + 
				mids0  + "," + 
				mids1  + "," + 
				mids2  + "," + 
				mids3  + "," + 
				highs0  + "," + 
				highs1  + "," + 
				highs2  + "," + 
				highs3  + "," + 
				level0  + "," + 
				level1  + "," + 
				level2  + "," + 
				level3  + ",";
				
	}
	
	public static String getHeaderRow() {
		return "Title,Filename,Actual Genre,Predicted Genre,Bass Max,Bass Ave,Bass Spread,Bass Peak,Mids Max,Mids Ave,Mids Spread,Mids Peak,"+
		"Highs Max,Highs Ave, Highs Spread, Highs Peak,Level Max,Level Ave,Level Spread,Level Peak, Rhythm Peak,Rhythm Ave,Bass 0,Bass 1,Bass 2,Bass 3," +
		"Mids 0,Mids 1, Mids 2,Mids 3,Highs 0,Highs 1,Highs 2,Highs 3,Level 0,Level 1,Level 2,Level 3";
	}
	
}
