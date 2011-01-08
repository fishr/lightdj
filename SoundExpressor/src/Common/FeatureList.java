package Common;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a list of Features computed from a particular frame of audio.
 * @author Steve Levine
 *
 */
public class FeatureList {

	private Map<String, Object> featureMap;
	
	public FeatureList() {
		featureMap = new HashMap<String,Object>();
	}
	
	/**
	 * Adds a feature to the FeatureList, and throws an error if it already exists.
	 */
	public void addFeature(String key, Object value) {
		// Add this feature to the list if it does not already exist there!
		if (featureMap.containsKey(key)) {
			// This key already exists! Throw an error.
			throw new RuntimeException("Error: Attempting to add a Feature \"" + key + "\", but this key already exists in the FeatureList!");
		}
		
		// Add it
		featureMap.put(key, value);
	}
	
	/**
	 * Retrieves a feature from the FeatureList, and throws an error if it doesn't exist
	 */
	public Object getFeature(String key) {
		if (!featureMap.containsKey(key)) {
			throw new RuntimeException("Error: There is no feature \"" + key + "\" in this FeatureList!");
		}
		
		return featureMap.get(key);
		
	}
	
	/**
	 * Returns true if this FeatureList contains the feature, and false otherwise
	 */
	public boolean containsFeature(String key) {
		return featureMap.containsKey(key);
	}
	
}
