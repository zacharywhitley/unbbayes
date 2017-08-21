/**
 * 
 */
package unbbayes.prs.mebn.ssbn.extension.noisyMax;

import unbbayes.prs.mebn.ssbn.SSBN;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeyAlgorithmParameters;
import unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator;
import unbbayes.util.extension.bn.inference.ICIFactorizationJunctionTreeAlgorithm;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.NoisyMaxCPTConverter;

/**
 * This is an SSBN generator which delegates to Junction Tree algorithm with Noisy-MAX optimization instead of classic
 * Junction Tree algorithm.
 * @author Shou Matsumoto
 */
public class NoisyMaxSSBNAlgorithm extends LaskeySSBNGenerator {
	
	private float probErrorMargin = NoisyMaxCPTConverter.DEFAULT_PROBABILITY_ERROR_MARGIN;
	
	private ICIFactorizationJunctionTreeAlgorithm defaultAlgorithm = null;

	/**
	 * @param _parameters
	 */
	public NoisyMaxSSBNAlgorithm(LaskeyAlgorithmParameters _parameters) {
		super(_parameters);
	}
	
	/**
	 * Substitute the compilation algorithm with the one that uses Noisy MAX optimization, then call super.
	 * @see unbbayes.prs.mebn.ssbn.laskeyalgorithm.LaskeySSBNGenerator#compileAndInitializeSSBN(unbbayes.prs.mebn.ssbn.SSBN)
	 * @see #getDefaultAlgorithm()
	 */
	protected void compileAndInitializeSSBN(SSBN ssbn) throws Exception {
		getDefaultAlgorithm().setNetwork(ssbn.getNetwork());
		ssbn.setAlgorithm(getDefaultAlgorithm());
		super.compileAndInitializeSSBN(ssbn);
	}

	/**
	 * @return the defaultAlgorithm : the BN algorithm to be used in {@link #compileAndInitializeSSBN(SSBN)}
	 * for calculating joint probability distribution of SSBN. Default is {@link ICIFactorizationJunctionTreeAlgorithm}
	 */
	public IInferenceAlgorithm getDefaultAlgorithm() {
		if (defaultAlgorithm == null) {
			defaultAlgorithm = new ICIFactorizationJunctionTreeAlgorithm();
			defaultAlgorithm.setProbErrorMargin(getProbErrorMargin());
		}
		return defaultAlgorithm;
	}

	/**
	 * @param defaultAlgorithm : the BN algorithm to be used in {@link #compileAndInitializeSSBN(SSBN)}
	 * for calculating joint probability distribution of SSBN. Default is {@link ICIFactorizationJunctionTreeAlgorithm}
	 */
	public void setDefaultAlgorithm(IInferenceAlgorithm defaultAlgorithm) {
		this.defaultAlgorithm = (ICIFactorizationJunctionTreeAlgorithm) defaultAlgorithm;
	}

	/**
	 * @return the probErrorMargin : error margin to set in {@link ICIFactorizationJunctionTreeAlgorithm} when it's instantiated at
	 * {@link #getDefaultAlgorithm()}. Default is {@link NoisyMaxCPTConverter#DEFAULT_PROBABILITY_ERROR_MARGIN}.
	 */
	public float getProbErrorMargin() {
		return probErrorMargin;
	}

	/**
	 * @param probErrorMargin : error margin to set in {@link ICIFactorizationJunctionTreeAlgorithm} when it's instantiated at
	 * {@link #getDefaultAlgorithm()}. Default is {@link NoisyMaxCPTConverter#DEFAULT_PROBABILITY_ERROR_MARGIN}.
	 */
	public void setProbErrorMargin(float probErrorMargin) {
		this.probErrorMargin = probErrorMargin;
		
		// update error margin if algorithm is already instantiated
		if (defaultAlgorithm != null ) {
			defaultAlgorithm.setProbErrorMargin(probErrorMargin);
		}
	}

}
