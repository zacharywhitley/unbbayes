/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * Builder of {@link AssetAwareInferenceAlgorithm}
 * @author Shou Matsumoto
 */
public interface IAssetAwareInferenceAlgorithmBuilder {
	/**
	 * 
	 * @param probDelegator : requests for updating probabilities
	 * will be delegated to this object.
	 * @param initialQValues : q values to be used to fill the asset tables
	 * @return instance of AssetAwareInferenceAlgorithm
	 * @see AssetAwareInferenceAlgorithm#getInstance(IInferenceAlgorithm)
	 */
	public AssetAwareInferenceAlgorithm build(IInferenceAlgorithm probDelegator, float initialQValues);
}
