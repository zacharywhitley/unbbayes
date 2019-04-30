package unbbayes.util;

import java.util.Arrays;

import org.apache.commons.math3.distribution.GammaDistribution;

import cc.mallet.types.Dirichlet;


/**
 * Sampler of <a href="http://en.wikipedia.org/wiki/Dirichlet_distribution#Random_number_generation">Dirichlet distribution</a>
 * based on <a href="http://en.wikipedia.org/wiki/Gamma_distribution">Gamma distribution</a>
 * @author Shou Matsumoto
 * @see org.apache.commons.math3.distribution.GammaDistribution
 * @see cc.mallet.types.Dirichlet
 */
public class DirichletSampler {

	private double[] alphas;
	
	private Dirichlet dirichlet = null;

	/**
	 * Delegates to {@link #DirichletSampler(double[], boolean)}
	 * with false as its second argument.
	 */
	public DirichletSampler(double[] alphas) {
		this(alphas, false);
	}
	/**
	 * Instantiates a sampler of Dirichlet distribution
	 * with its alpha parameters.
	 * @param alphas : the alpha parameters of dirichlet distribution
	 * @param isToUseGamma : if false, {@link #sample()} will
	 *  use {@link Dirichlet} for sampling. If true,
	 *  then {@link org.apache.commons.math3.distribution.GammaDistribution}
	 *  will be used instead.
	 *  @see #setDirichlet(Dirichlet)
	 */
	public DirichletSampler(double[] alphas, boolean isToUseGamma) {
		this.alphas = alphas;
		if (!isToUseGamma) {
			dirichlet = new Dirichlet(alphas);
		}
	}
	
//	/**
//	 * Default constructor initializing fields
//	 */
//	public DirichletSampler(double[] alphas, Dirichlet dirichlet) {
//		this(alphas, false);
//		this.setDirichlet(dirichlet);
//	}
	
	/**
	 * @return sample of Dirichlet distribution based on 
	 * {@link #getAlphas()}.
	 * For {@link #getAlphas()} of size K,
	 * we sample K samples y1,...,yk from Gamma(alphas[i], 1)
	 * and then normalize them in order to obtain a
	 * sample of Dirichlet distribution with size K.
	 */
	public double[] sample() {
		
		// use mallet if specified
		if (this.dirichlet != null) {
			return this.dirichlet.nextDistribution();
		}
		
		// use apache commons gamma dist to simulate dirichlet
		
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
		if (dirichlet != null) {
			// substitute with dirichlet with new alpha parameters
			dirichlet = new Dirichlet(alphas);
		}
	}


	/**
	 * @return 
	 * the dirichlet instance to use in {@link #sample()}.
	 * if non null, then {@link #sample()}
	 * will use this instance for sampling
	 *  Dirichlet distribution.
	 * If null, then it will use {@link org.apache.commons.math3.distribution.GammaDistribution}
	 * instead.
	 * @see #setAlphas(double[])
	 */
	public Dirichlet getDirichlet() {
		return dirichlet;
	}

	/**
	 * @param dirichlet : 
	 * the dirichlet instance to use in {@link #sample()}.
	 * if non null, then {@link #sample()}
	 * will use this instance for sampling
	 *  Dirichlet distribution.
	 * If null, then it will use {@link org.apache.commons.math3.distribution.GammaDistribution}
	 * instead.
	 * @see #setAlphas(double[])
	 */
	public void setDirichlet(Dirichlet dirichlet) {
		this.dirichlet = dirichlet;
	}

}
