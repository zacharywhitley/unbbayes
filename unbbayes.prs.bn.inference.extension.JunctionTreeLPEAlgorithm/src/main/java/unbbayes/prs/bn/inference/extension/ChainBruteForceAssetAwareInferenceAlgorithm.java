/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

/**
 * This class is equivalent to {@link BruteForceAssetAwareInferenceAlgorithm},
 * but the joint probability is calculated as a chain. 
 * <br/>
 * E.g.<br/><br/>
 * P(A,B,C) = P(C)*P(B|C)*P(A|B,C).
 * @author Shou Matsumoto
 *
 */
public class ChainBruteForceAssetAwareInferenceAlgorithm extends
		BruteForceAssetAwareInferenceAlgorithm {

	/**
	 * 
	 */
	public ChainBruteForceAssetAwareInferenceAlgorithm() {
		// TODO Auto-generated constructor stub
	}
	
	

}
