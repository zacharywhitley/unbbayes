package unbbayes.prs.bn.inference.extension;

import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

public interface IAssetNetAlgorithm extends IInferenceAlgorithm{

	/**
	 * Delegates to {@link IInferenceAlgorithm#getNetwork()} from the algorithm
	 * obtained from {@link #getAssetAwareInferenceAlgorithm()}
	 * @return the relatedProbabilisticNetwork
	 */
	public ProbabilisticNetwork getRelatedProbabilisticNetwork();

	/**
	 * Initializes the {@link #getNetwork()} (asset network) and 
	 * delegates to {@link IInferenceAlgorithm#getNetwork()} from the algorithm
	 * obtained from {@link #getAssetAwareInferenceAlgorithm()}
	 * @param relatedProbabilisticNetwork the relatedProbabilisticNetwork to set
	 * @throws InvalidParentException 
	 * @throws IllegalArgumentException 
	 */
	public void setRelatedProbabilisticNetwork(
			ProbabilisticNetwork relatedProbabilisticNetwork) throws IllegalArgumentException, InvalidParentException;
	
	/**
	 * This is the asset network. It must be an instance of {@link AssetNetwork}
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#setNetwork(unbbayes.prs.Graph)
	 */
	public void setAssetNetwork(AssetNetwork network) throws IllegalArgumentException;

	/**
	 * This is the asset network
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getNetwork()
	 */
	public AssetNetwork getAssetNetwork() ;
	
	/**
	 * This method generates a network of assets with the same nodes and network topology of
	 * relatedProbabilisticNetwork, and initializes the assets using the
	 * values obtained from {@link #getDefaultInitialAssetQuantity()}.
	 * @param relatedProbabilisticNetwork
	 * @return an asset network
	 * @throws InvalidParentException 
	 */
	public AssetNetwork createAssetNetFromProbabilisticNet(ProbabilisticNetwork relatedProbabilisticNetwork)
			throws InvalidParentException;

}