/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

/**
 * This class is equivalent to {@link BruteForceAssetAwareInferenceAlgorithm},
 * but the joint probability is calculated by using the cpts.
 * <br/>
 * E.g. <br/><br/>
 * P(A,B,C) = P(C|Pa(C))*P(B|Pa(B))*P(A|Pa(A)).
 * @author Shou Matsumoto
 *
 */
public class CPTBruteForceAssetAwareInferenceAlgorithm extends
		BruteForceAssetAwareInferenceAlgorithm {

	/**
	 * 
	 */
	public CPTBruteForceAssetAwareInferenceAlgorithm() {
		// TODO Auto-generated constructor stub
	}
	
	

}
