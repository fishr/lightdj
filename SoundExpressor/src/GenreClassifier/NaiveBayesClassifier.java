package GenreClassifier;

/**
 * @author Steve Levine
 */
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a Naive Bayes classifier (uses 1D Gaussians to model conditionally independent features of the feature vectors.
 * 
 * Please note that this implementation does not do training - that's done in the Python code I already wrote! This code
 * does classification only.
 * @author steve
 *
 */
public class NaiveBayesClassifier {

	protected NaiveBayesModel model;
	
	public NaiveBayesClassifier(String filename) {
		model = new NaiveBayesModel(filename);
	}
	
	
	public int classify(SongFeatureVector feature_vector) {
		double[] vector = feature_vector.vector();
		return classify(vector);
	}
	
	public int classify(double[] vector) {
		int d = vector.length;
		int[] classes = model.classes();
		
		// Compute the posteriors for each class
		Map<Integer, Double> posteriors = new HashMap<Integer, Double>();
		for(int y : classes) {
			double posterior = model.getPrior(y);
			for(int i = 0; i < d; i++) {
				posterior *= model.computeConditional(vector[i], i, y);
			}
			
			posteriors.put(y, posterior);
			
		}
		
		// Select the argmax class, and return it
		int y_hat = 0;
		double max_posterior = 0.0;
		for(int y : model.classes()) {
			if (posteriors.get(y) > max_posterior) {
				max_posterior = posteriors.get(y);
				y_hat = y;
			}
		}
		
		return y_hat;
		
	}
	
}
