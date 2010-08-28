/**
 * 
 */
package unbbayes.prs.prm.compiler;

import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * This is a common interface for those holding a {@link IInferenceAlgorithm}
 * for Bayesian Network compilation, in a PRM project
 * @author Shou Matsumoto
 *
 */
public interface IBNInferenceAlgorithmHolder {
	
	/**
	 * Obtains an inference algorithm to compile BN in a PRM context
	 * @return
	 */
	public IInferenceAlgorithm getBNInferenceAlgorithm();
	

	/**
	 * This is an inference algorithm to compile BN in a PRM context
	 * @param inferenceAlgorithm
	 */
	public void setBNInferenceAlgorithm(IInferenceAlgorithm inferenceAlgorithm);
}
