/**
 * 
 */
package unbbayes.util.extension.bn.inference;

import unbbayes.prs.bn.IProbabilityFunction;

/**
 * Checks if content of local distribution follows the pattern of a distribution
 * with Independence of Causal Influence (ICI), like noisy-max.
 * @author Shou Matsumoto
 * @see NoisyMaxCPTConverter
 */
public interface IIndependenceCausalInfluenceChecker {

	/**
	 * Checks if content of local distribution follows the pattern of a distribution
	 * with Independence of Causal Influence (ICI), like noisy-max.
	 * @param localDistribution : the distribution to check
	 * @return: true if provided local distribution can be considered to represent a ICI distribution
	 */
	public boolean isICI(IProbabilityFunction localDistribution);
}
