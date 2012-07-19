/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

/**
 * This class uses a joint probability table
 * and joint asset table
 * in order to provide the same functionality
 * of {@link AssetAwareInferenceAlgorithm}.
 * That is, instead of using a junction tree for assets, it
 * calculates all possible combination of states.
 * Joint probabilities are calculated
 * as Product(clique_potentials) / Product(separator_potentials)
 * of the junction tree in {@link #getRelatedProbabilisticNetwork()}
 * @author Shou Matsumoto
 *
 */
public class BruteForceAssetAwareInferenceAlgorithm extends
		AssetAwareInferenceAlgorithm {

	/**
	 * 
	 */
	public BruteForceAssetAwareInferenceAlgorithm() {
		// TODO Auto-generated constructor stub
	}

}
