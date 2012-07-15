package unbbayes.prs.bn.inference.extension;

import java.util.List;
import java.util.Map;

import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
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
	 * @param relatedProbabilisticNetwork : the bayesian network to be based on
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
	
	/**
	 * @return true if {@link #propagate()} should only update altered clique.
	 */
	public boolean isToUpdateOnlyEditClique() ;

	/**
	 * @param isToUpdateOnlyEditClique : true if {@link #propagate()} should only update altered clique.
	 */
	public void setToUpdateOnlyEditClique(boolean isToUpdateOnlyEditClique);
	
	/**
	 * If false, {@link #propagate()} will throw a {@link ZeroAssetsException}
	 * when q-values gets less than or equals to 1 (i.e. when the respective
	 * assets - log values - goes to 0 or negative).
	 * @param isToAllowQValuesSmallerThan1 the isToAllowQValuesSmallerThan1 to set
	 */
	public void setToAllowQValuesSmallerThan1(boolean isToAllowQValuesSmallerThan1);

	/**
	 * If false, {@link #propagate()} will throw a {@link ZeroAssetsException}
	 * when q-values gets less than or equals to 1 (i.e. when the respective
	 * assets - log values - goes to 0 or negative).
	 * @return the isToAllowQValuesSmallerThan1
	 */
	public boolean isToAllowQValuesSmallerThan1();

	/**
	 * This method reverts the last probability update performed by this class.
	 * It basically uses the information stored in {@link #updateProbabilityPriorToPropagation()}
	 * in order to restore the values.
	 */
	public void revertLastProbabilityUpdate();
	
	/**
	 * This method shall make best effort in order to mark
	 * a state of a given node as a hard evidence permanently.
	 * In other words, the network shall behave like if
	 * a state of a given node is set to 100%
	 * after this method returns.
	 * For example, implementations may remove the node from
	 * the network after the a hard evidence.
	 * @param node : node to add permanent hard evidence.
	 * @param state : state to set as hard evidence
	 * @param isToDeleteNode : if true, node will be deleted after setting as permanent node.
	 */
	public void setAsPermanentEvidence(INode node, int state, boolean isToDeleteNode);
	
	/**
	 * This map stores what were the asset tables before the last call of
	 * {@link #propagate()}
	 * @return the assetTablesBeforeLastPropagation. This is a mapping from a Clique or Separator
	 * to the PotentialTable (the assset table).
	 */
	public Map<IRandomVariable, PotentialTable> getAssetTablesBeforeLastPropagation();
	
	/**
	 * If false, {@link unbbayes.prs.bn.AssetNode#updateMarginal()} will set the
	 * marginal of the asset nodes to default values
	 * (usually, zeros). If true, {@link unbbayes.prs.bn.AssetNode#updateMarginal()} will
	 * attempt to calculate the marginal assets using 
	 * the clique tables and some specific operation for marginalization
	 * (e.g. sum-out, min-out, max-out).
	 * @param isToCalculateMarginalsOfAssetNodes the isToCalculateMarginalsOfAssetNodes to set
	 * @see unbbayes.prs.bn.AssetNode#setToCalculateMarginal(boolean)
	 */
	public void setToCalculateMarginalsOfAssetNodes(boolean isToCalculateMarginalsOfAssetNodes);
	

	
	/**
	 * If false, {@link unbbayes.prs.bn.AssetNode#updateMarginal()} will set the
	 * marginal of the asset nodes to default values
	 * (usually, zeros). If true, {@link unbbayes.prs.bn.AssetNode#updateMarginal()} will
	 * attempt to calculate the marginal assets using 
	 * the clique tables and some specific operation for marginalization
	 * (e.g. sum-out, min-out, max-out).
	 * @return isToCalculateMarginalsOfAssetNodes the isToCalculateMarginalsOfAssetNodes to set
	 * @see unbbayes.prs.bn.AssetNode#setToCalculateMarginal(boolean)
	 */
	public boolean isToCalculateMarginalsOfAssetNodes();
}