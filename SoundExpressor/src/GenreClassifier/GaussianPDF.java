package GenreClassifier;

/**
 * Represents a Gaussian PDF
 * @author steve
 *
 */
public class GaussianPDF {
	protected double u;
	protected double sigma;
	
	public GaussianPDF(double u, double sigma) {
		this.u = u;
		this.sigma = sigma;
	}
	
	public double gaussianPDF(double x) {
		return 1.0/Math.sqrt(2*Math.PI*sigma*sigma)*Math.exp(-(x - u)*(x - u) / (2*sigma*sigma));
	}
	
}
