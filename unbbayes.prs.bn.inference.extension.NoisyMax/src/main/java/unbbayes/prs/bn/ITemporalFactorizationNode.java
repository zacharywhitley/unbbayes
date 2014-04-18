/**
 * 
 */
package unbbayes.prs.bn;

import unbbayes.util.extension.bn.inference.TemporalFactorizationInferenceAlgorithmOptionPanel;

/**
 * Nodes implementing this interface will be considered to use
 * Inter-Causal Independence, and will be factorized
 * by {@link TemporalFactorizationInferenceAlgorithmOptionPanel} in order to optimize for {@link unbbayes.prs.bn.JunctionTreeAlgorithm}
 * @author Shou Matsumoto
 *
 */
public interface ITemporalFactorizationNode {

}
