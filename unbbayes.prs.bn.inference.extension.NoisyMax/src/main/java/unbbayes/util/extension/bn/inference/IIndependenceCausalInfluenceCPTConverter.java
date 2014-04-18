/**
 * 
 */
package unbbayes.util.extension.bn.inference;

import unbbayes.prs.bn.IProbabilityFunction;

/**
 * Clases implementing this interface are able to read some cells of the CPT
 * and automatically fill other cells of the same CPT and result in
 * Independence of causal influence (e.g. noisy-max/or, noisy-add, etc.).
 * @author Shou Matsumoto
 */
public interface IIndependenceCausalInfluenceCPTConverter {
	
	/**
	 * Read some cells of the CPT and automatically fill other cells of the same CPT, in order to produce 
	 * Independence of causal influence (e.g. noisy-max/or, noisy-add, etc.).
	 * @param localDistribution: the CPT to be changed
	 */
	public void forceCPTToIndependenceCausalInfluence(IProbabilityFunction localDistribution);
}
