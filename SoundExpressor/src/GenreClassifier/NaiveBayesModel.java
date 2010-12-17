package GenreClassifier;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class NaiveBayesModel {
	
	protected double[] priors;
	protected GaussianPDF[][] conditionalProbs;
	protected int[] classes;
	
	/**
	 * Reads in the parameters for this Gaussian Naive Bayes model from a file.
	 * @param filename
	 */
	public NaiveBayesModel(String filename) {
		loadParametersFromFile(filename);
	}
	
	public int[] classes() {
		return classes;
	}
	
	public double getPrior(int y) {
		return priors[y - 1];
	}

	public double computeConditional(double x, int i, int y) {
		GaussianPDF g = conditionalProbs[y - 1][i];
		return g.gaussianPDF(x);
	}
	
	
	// Read in parameters from the file
	private void loadParametersFromFile(String filename) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String line;
			int lineNum = 0;
			
			int numClasses = 0;
			int featureVectorLen = 0;
			
			while((line = in.readLine()) != null) {
				if (!line.startsWith("#")) { // Ignore comments
					String fields[];
					// A special line that contains parameter information
					if (lineNum == 0) {	// First line contains meta data
						fields = line.split(" ");
						
						// Read in information about the model
						numClasses = Integer.parseInt(fields[0]);
						featureVectorLen = Integer.parseInt(fields[1]);
						
						priors = new double[numClasses];
						conditionalProbs = new GaussianPDF[numClasses][featureVectorLen];
						classes = new int[numClasses];
						for(int i = 0; i < numClasses; i++) {
							classes[i] = i + 1;
						}
						
					} else if (lineNum == 1) { // Second line contains prior values
						fields = line.split(" ");
						for(int i = 0; i < numClasses; i++) {
							priors[i] = Double.parseDouble(fields[i]);
						}
					} else { // Other lines encode conditional information
						fields = line.split(" ");
						int y = Integer.parseInt(fields[0]);
						int i = Integer.parseInt(fields[1]);
						double u = Double.parseDouble(fields[2]);
						double sigma = Double.parseDouble(fields[3]);
						
						conditionalProbs[y - 1][i] = new GaussianPDF(u, sigma);
						
					}
					
					lineNum++;
				}
			}
			
			
			
			
		} catch (Exception e) {
			System.out.println("Could not load Naive Bayes Classification Parameters!");
			e.printStackTrace();
		}
	}
	
}
