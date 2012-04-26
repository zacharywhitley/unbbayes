package unbbayes.prs.bn.inference.extension;

import java.util.List;
import java.util.Map;

import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * Common interface for the algorithms related to assets
 * @author Shou Matsumoto
 *
 */
public interface IAssetNetAlgorithm extends IInferenceAlgorithm {

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
	
	/**
	 * Run only the min propagation algorithm, which will propagate the minimum q values between the q-tables.
	 */
	public void runMinPropagation();
	
	/**
	 * Reverts the change of {@link #runMinPropagation()}
	 */
	public void undoMinPropagation();
	
	/**
	 * If set to true, min-propagation junction tree algorithm will be called. False otherwise.
	 * @return the isToPropagateForGlobalConsistency
	 */
	public boolean isToPropagateForGlobalConsistency();

	/**
	 * If set to true, min-propagation junction tree algorithm will be called. False otherwise.
	 * @param isToPropagateForGlobalConsistency the isToPropagateForGlobalConsistency to set
	 */
	public void setToPropagateForGlobalConsistency(boolean isToPropagateForGlobalConsistency);
	

	/**
	 * If set to false, the q-values of separators will not be updated.
	 * @return the isToPropagateForGlobalConsistency
	 */
	public boolean isToUpdateSeparators();

	/**
	 * 
	 * If set to false, the q-values of separators will not be updated.
	 * @param isToPropagateForGlobalConsistency the isToPropagateForGlobalConsistency to set
	 */
	public void setToUpdateSeparators(boolean isToPropagateForGlobalConsistency);
	
	/**
	 * This method forces the algorithm to store the current probabilities of the {@link #getRelatedProbabilisticNetwork()},
	 * so that it can be used posteriorly by {@link #propagate()} in order to to calculate the ratio of the change of probability 
	 * between the current (probability when {@link #propagate()} was called) one and the last (probability when this method was called) one.
	 * Those values are stored in the network property {@link #LAST_PROBABILITY_PROPERTY} of {@link #getNetwork()}, which is retrievable from {@link Graph#getProperty(String)}.
	 */
	public void updateProbabilityPriorToPropagation();
	
	/**
	 * @return the defaultInitialAssetQuantity : values assumed by the cells of q-tables when algorithm starts.
	 */
	public float getDefaultInitialAssetQuantity();
	
	/**
	 * @param defaultInitialAssetQuantity : values assumed by the cells of q-tables when algorithm starts.
	 */
	public void setDefaultInitialAssetQuantity(float defaultInitialAssetQuantity);
	
	/**
	 * @return the value of explanation (i.e. min-q value).
	 * @param inputOutpuArgumentForExplanation : this is an input and output argument which will be filled with the states corresponding to the returned value (i.e. this is the min-q states).
	 * This collection of maps indicates nodes and indexes of most probable states. The state can be retrieved by calling {@link INode#getStateAt(int)}
	 * Since the explanation may not be unique (there may be several equivalent explanations), the returned value
	 * is a set.
	 * @see IExplanationJunctionTree#calculateExplanation(Graph, IInferenceAlgorithm)
	 */
	public float calculateExplanation( List<Map<INode, Integer>> inputOutpuArgumentForExplanation);

	
	/**
	 * If this is false, log is disabled.
	 * @param isToLogAssets the isToLogAssets to set
	 */
	public void setToLogAssets(boolean isToLogAssets);

	/**
	 * 
	 * If this is false, log is disabled.
	 * @return the isToLogAssets
	 */
	public boolean isToLogAssets();
}