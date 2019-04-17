package unbbayes.util;

import java.util.Arrays;

import org.apache.commons.math3.distribution.GammaDistribution;


/**
 * Sampler of <a href="http://en.wikipedia.org/wiki/Dirichlet_distribution#Random_number_generation">Dirichlet distribution</a>
 * based on <a href="http://en.wikipedia.org/wiki/Gamma_distribution">Gamma distribution</a>
 * @author Shou Matsumoto
 * @see org.apache.commons.math3.distribution.GammaDistribution
 */
public class DirichletSampler {

	private double[] alphas;
	

	/**
	 * Instantiates a sampler of Dirichlet distribution
	 * with its alpha parameters.
	 */
	public DirichletSampler(double[] alphas) {
		this.alphas = alphas;
	}
	
	/**
	 * @return sample of Dirichlet distribution based on 
	 * {@link #getAlphas()}.
	 * For {@link #getAlphas()} of size K,
	 * we sample K samples y1,...,yk from Gamma(alphas[i], 1)
	 * and then normalize them in order to obtain a
	 * sample of Dirichlet distribution with size K.
	 */
	public double[] sample() {
		
		// extract the dirichlet parameters
		double[] alphas = getAlphas();
		
		// initial assertions
//		if (alphas == null) {
//			throw new NullPointerException("Alpha parameters must be specified.");
//		}
//		if (alphas.length <= 0) {
//			throw new IllegalArgumentException("Alpha parameters must be specified.");
//		}
		
		// sample from gamma distributions
		double[] gammaSamples = new double[alphas.length];
		double sum = 0;	// for normalization
		for (int i = 0; i < alphas.length; i++) {
			// Note: GammaDistribution will throw exception if alphas[i] <= 0, 
			double sample = new GammaDistribution(alphas[i], 1).sample();
			gammaSamples[i] = sample;
			sum += sample;
		}
		
		if (sum == 0) {
			// all gamma samples were zero. 
			// Consider this special case as uniform distribution
			Arrays.fill(gammaSamples, 1d/((double)gammaSamples.length));
		} else {
			// normalize gamma samples
			for (int i = 0; i < gammaSamples.length; i++) {
				gammaSamples[i] /= sum;
			}
		}
		
		return gammaSamples;
	}

	/**
	 * @return the alpha parameteres of Dirichlet distribution
	 */
	public double[] getAlphas() {
		return alphas;
	}

	/**
	 * @param alphas :
	 * the alpha parameteres of Dirichlet distribution
	 */
	public void setAlphas(double[] alphas) {
		this.alphas = alphas;
	}

}
