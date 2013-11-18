package unbbayes.prs.bn.inference.extension;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.Clique;
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
	 * values obtained from {@link #getDefaultInitialAssetTableValue()}.
	 * @param relatedProbabilisticNetwork : the bayesian network to be based on
	 * @return an asset network
	 * @throws InvalidParentException 
	 */
	public AssetNetwork createAssetNetFromProbabilisticNet(ProbabilisticNetwork relatedProbabilisticNetwork)
			throws InvalidParentException;
	
	/**
	 * Run only the min propagation algorithm, which will propagate the minimum q values between the q-tables.
	 * @param conditions : mapping from node to its state. This map indicates what conditions
	 * should be considered (e.g. considered as findings) in a min-propagation.
	 */
	public void runMinPropagation(Map<INode, Integer> conditions);
	
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
	 * @return the defaultInitialAssetTableValue : values assumed by the cells of q-tables when algorithm starts.
	 */
	public float getDefaultInitialAssetTableValue();
	
	/**
	 * @param defaultInitialAssetTableValue : values assumed by the cells of q-tables when algorithm starts.
	 */
	public void setDefaultInitialAssetTableValue(float defaultInitialAssetQuantity);
	
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
	 * @param isToAllowZeroAssets the isToAllowQValuesSmallerThan1 to set
	 */
	public void setToAllowZeroAssets(boolean isToAllowZeroAssets);

	/**
	 * If false, {@link #propagate()} will throw a {@link ZeroAssetsException}
	 * when q-values gets less than or equals to 1 (i.e. when the respective
	 * assets - log values - goes to 0 or negative).
	 * @return the isToAllowQValuesSmallerThan1
	 */
	public boolean isToAllowZeroAssets();
	
	

	/**
	 * This method reverts the last probability update performed by this class.
	 * It basically uses the information stored in {@link #updateProbabilityPriorToPropagation()}
	 * in order to restore the values.
	 */
	public void revertLastProbabilityUpdate();
	
	/**
	 * This method shall make best effort in order to mark
	 * the probability of a given node to a value permanently.
	 * For example, implementations may remove the node from
	 * the network after inserting an evidence.
	 * @param evidences : node to add permanent hard evidence and their probability distribution.
	 * For example, if a list [.7,.1,.2] is provided, then the 1st state will be set
	 * to 70%, the second to 10% and the third to 20% (and the node may eventually be absorbed).
	 * If set to [1,0,0,...,0], then a hard evidence will be inserted.
	 * If invalid values (e.g. values above 1, below 0, Infinite, NaN or null) are present,
	 * and the other values in the remaining indexes shall be either 0 or 1, and it will
	 * be considered as a hard evidence. For example, providing [0,null,null] will be
	 * a "negative" hard evidence  setting the 1st state to 0% and adjusting the probabilities
	 * of the other states consistently. If [1,null,null] is provided, then it shall be equivalent
	 * to [1,0,0] 
	 * @param isToDeleteNode : if true, node will be deleted after setting as permanent node.
	 */
	public void setAsPermanentEvidence(Map<INode, List<Float>> evidences, boolean isToDeleteNode);
	
//	/**
//	 * This map stores what were the asset tables before the last call of
//	 * {@link #propagate()}
//	 * @return the assetTablesBeforeLastPropagation. This is a mapping from a Clique or Separator
//	 * to the PotentialTable (the assset table).
//	 */
//	public Map<IRandomVariable, DoublePrecisionProbabilisticTable> getAssetTablesBeforeLastPropagation();
	
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
	
	/**
	 * Separators with no variables may exist if network is disconnected.
	 * This value will be used as a default content (q-value or asset)
	 * of empty separators if such separators are present.
	 * This value is used in methods like {@link #calculateExplanation(List)},
	 * which uses joint q-values.
	 * @return the emptySeparatorsQValue value,
	 */
	public float getEmptySeparatorsDefaultContent();
	
	/**
	 * Separators with no variables may exist if network is disconnected.
	 * This value will be used as a default content (assets or q-value)
	 * of empty separators if such separators are present.
	 * This value is used in methods like {@link #calculateExplanation(List)},
	 * which uses joint q-values.
	 * @param emptySeparatorsContent
	 */
	public void setEmptySeparatorsDefaultContent(float emptySeparatorsContent);
	
	/**
	 * If true, then exponential q values will be stored instead of
	 * logarithmic assets. If false, asset tables will store
	 * assets instead of q-values.
	 * @param isToUseQValues the isToUseQValues to set
	 */
	public void setToUseQValues(boolean isToUseQValues);

	/**
	 * If true, then exponential q values will be stored instead of
	 * logarithmic assets. If false, asset tables will store
	 * assets instead of q-values.
	 * @return the isToUseQValues
	 */
	public boolean isToUseQValues();
	
	/**
	 * This object will be used to convert q-values to assets,
	 * and assets to q-values.
	 * <br/><br/>
	 * Usually, assets and q-values are related with logarithm function:
	 * asset = b log (q). 
	 * <br/><br/>
	 * In which b is some constant.
	 * @param qToAssetConverter the qToAssetConverter to set
	 */
	public void setqToAssetConverter(IQValuesToAssetsConverter qToAssetConverter);

	/**
	 * This object will be used to convert q-values to assets,
	 * and assets to q-values.
	 * <br/><br/>
	 * Usually, assets and q-values are related with logarithm function:
	 * asset = b log (q). 
	 * <br/><br/>
	 * In which b is some constant.
	 * @return the qToAssetConverter
	 */
	public IQValuesToAssetsConverter getqToAssetConverter();
	
	/**
	 * If true, {@link #calculateExplanation(List)} will 
	 * fill the input/output argument (the list) with the
	 * least probable explanation (i.e. the min-states).
	 * If false, the method will only return the min-values.
	 * This can be used to improve performance when
	 * the min-state is not necessary.
	 * @return the isToCalculateLPE
	 */
	public boolean isToCalculateLPE() ;
	
	/**
	 * @return if true, this algorithm considers the {@link Float#POSITIVE_INFINITY} or {@link Float#NEGATIVE_INFINITY}
	 * as valid values.
	 */
	public boolean isToAllowInfinite();

	/**
	 * If true, {@link #calculateExplanation(List)} will 
	 * fill the input/output argument (the list) with the
	 * least probable explanation (i.e. the min-states).
	 * If false, the method will only return the min-values.
	 * This can be used to improve performance when
	 * the min-state is not necessary.
	 * @param isToCalculateLPE the isToCalculateLPE to set
	 */
	public void setToCalculateLPE(boolean isToCalculateLPE) ;
	
	/**
	 * Obtains a "memento" object, which is
	 * an object representing the current state
	 * of this algorithm. Usually, the asset network
	 * and probabilistic network are enough for
	 * representing the algorithm.
	 * @return a memento object
	 */
	public IAssetNetAlgorithmMemento getMemento();
	
	/**
	 * Restore the state of this algorithm by using
	 * a memento object, which is
	 * an object representing the current state
	 * of this algorithm. Usually, the asset network
	 * and probabilistic network are enough for
	 * representing the algorithm.
	 * @param memento : object returned by {@link #getMemento()}
	 * @throws NoSuchFieldException : if memento does not
	 * contain required fields (i.e. it is not in a 
	 * required format). This may happen if the memento
	 * object was not obtained from {@link #getMemento()}.
	 */
	public void setMemento(IAssetNetAlgorithmMemento memento) throws NoSuchFieldException;
	
	/**
	 * Memento objects (object representing the current state of another object)
	 * of {@link IAssetNetAlgorithm} shall implement this empty interface.
	 * @author Shou Matsumoto
	 */
	public interface IAssetNetAlgorithmMemento{};
	
	/**
	 * @return the cliques containing the nodes with soft evidences in the next call of {@link #propagate()}.
	 * If set to null or empty, then {@link #updateProbabilityPriorToPropagation()} will
	 * attempt to infer what cliques contains such evidences.
	 * This list will be reset after {@link #propagate()}.
	 * CAUTION: these cliques are expected to be in {@link #getRelatedProbabilisticNetwork()}, so
	 * they are cliques with probabilities. This is because we need to obtain the probabilities' ratio
	 * in order to calculate the value to update.
	 */
	public List<Clique> getEditCliques();
	
	/**
	 * Creates a new node using the object passed as the argument, and adds it into {@link #getAssetNetwork()}.
	 * @param nodeInProbNet : node in  probNet to be cloned into assetNet
	 * @param probNet : network where nodeInProbNet resides.
	 * @param assetNet : network where the new node will be created.
	 * @return the created node
	 */
	public INode addDisconnectedNodeIntoAssetNet(INode nodeInProbNet, Graph probNet, AssetNetwork assetNet);
	
//	/**
//	 * This method will attempt to connect nodes without re-compiling the network (re-generating junction tree).
//	 * By default, the network obtainable from {@link #getNetwork()} will be updated.
//	 * @param child : the child node (whose new arcs/edges will be pointing to)
//	 * @param parents : the parent nodes (whose new arcs/edges will be pointing from)
//	 * @param isToOptimizeForProbNetwork: if true, this method will assume that only probabilistic networks are being
//	 * used, and optimizations will be performed assuming that cliques are globally consistent and normalized.
//	 * <br/>
//	 * <br/>
//	 * CAUTION: ALWAYS USE FALSE IF YOU NEED TO USE THIS ALGORITHM TOGETHER WITH ANY KIND OF ASSET NETWORKS, BECAUSE ASSET NETWORKS
//	 * ARE NEITHER GLOBALLY CONSISTENT NOR NORMALIZED. NOTE THAT PASSING TRUE IN PROBABILISTIC NETWORKS AND FALSE AT 
//	 * ASSET NETWORKS MAY CAUSE THE JUNCTION TREES OF PROBABILITIES AND ASSETS TO BECOME DIFFERENT, CAUSING ERRORS,
//	 * SO USE FALSE AT BOTH IF YOU ARE USING ASSETS ANYWHERE.
//	 * <br/>
//	 * @param isToUpdateJunctionTree: if false, the junction tree won't be updated. This is only useful when
//	 * you expect more edges to be created, and only the last call must update the junction tree. Use true by default.
//	 * @return : the list of new edges created as a result of the execution of this method. 
//	 * Implementations may return null or empty if this method only changes the junction tree, without updating the Bayes net.
//	 * @throws UnsupportedOperationException if the implementation is not able to connect the nodes
//	 * specified in its arguments, mainly due to complexity.
//	 * @throws IllegalArgumentException : if the nodes specified in the arguments are not managed by this algorithm.
//	 * @throws InvalidParentException : if the provided parent is not consistent. See {@link unbbayes.prs.Network#addEdge(Edge)}
//	 */
//	public List<Edge> addEdgesToNet(INode child, List<INode> parents, boolean isToOptimizeForProbNetwork) throws UnsupportedOperationException, IllegalArgumentException,InvalidParentException;
	
	/**
	 * This method will attempt to connect nodes without re-compiling the network (re-generating junction tree).
	 * By default, the network obtainable from {@link #getNetwork()} will be updated.
	 * @param nodeAndParents : a mapping from the child node (whose new arcs/edges will be pointing to)
	 * to its parent nodes (whose new arcs/edges will be pointing from).
	 * @param virtualArcs : these are arcs may not be actually present in the managed network, but this algorithm must behave
	 * like if they were present. Some implementations may require that these arcs do exist in the system already,
	 * and this method may change the direction of these arcs if the new arcs to be included causes cycles
	 * because of these virtual arcs. It depends on the implementation, but the second approach is suggested.
	 * In normal cases, just set this argument to null.
	 * @param isToOptimizeForProbNetwork: if true, this method will assume that only probabilistic networks are being
	 * used, and optimizations will be performed assuming that cliques are globally consistent and normalized.
	 * <br/>
	 * <br/>
	 * CAUTION: ALWAYS USE FALSE IF YOU NEED TO USE THIS ALGORITHM TOGETHER WITH ANY KIND OF ASSET NETWORKS, BECAUSE ASSET NETWORKS
	 * ARE NEITHER GLOBALLY CONSISTENT NOR NORMALIZED. NOTE THAT PASSING TRUE IN PROBABILISTIC NETWORKS AND FALSE AT 
	 * ASSET NETWORKS MAY CAUSE THE JUNCTION TREES OF PROBABILITIES AND ASSETS TO BECOME DIFFERENT, CAUSING ERRORS,
	 * SO USE FALSE AT BOTH IF YOU ARE USING ASSETS ANYWHERE.
	 * <br/>
	 * @param isToUpdateJunctionTree: if false, the junction tree won't be updated. This is only useful when
	 * you expect more edges to be created, and only the last call must update the junction tree. Use true by default.
	 * @return : the list of new edges created as a result of the execution of this method. 
	 * Implementations may return null or empty if this method only changes the junction tree, without updating the Bayes net.
	 * @throws UnsupportedOperationException if the implementation is not able to connect the nodes
	 * specified in its arguments, mainly due to complexity.
	 * @throws IllegalArgumentException : if the nodes specified in the arguments are not managed by this algorithm.
	 * @throws InvalidParentException : if the provided parent is not consistent. See {@link unbbayes.prs.Network#addEdge(Edge)}
	 */
	public List<Edge> addEdgesToNet(Map<INode, List<INode>> nodeAndParents, boolean isToOptimizeForProbNetwork, List<Edge> virtualArcs) throws UnsupportedOperationException, IllegalArgumentException,InvalidParentException;
	
	/**
	 * Finds the shortest path from a clique to another clique (regardless of direction of the connections
	 * - parent or children) in a junction tree.
	 * By default, the junction tree of {@link #getNetwork()} will be used for the search.
	 * @param from : set of nodes to be contained in the clique to start search from
	 * @param to : set of nodes to be contained in the clique to end search
	 * @return - empty: if there was no path (this is supposedly impossible in a consistent junction tree, but may happen if
	 * tree is disconnected - with no empty separator connecting cliques - or the junction tree did not exist at all),
	 * or when "from" is equal to "to".
	 * <br/>
	 * - Non-empty list: the list of cliques pertaining to the path between the cliques provided in the argument.
	 * If the list contains only 1 element, this means that all nodes were in the same clique.
	 * <br/>
	 * <br/>
	 * This method shall not return null.  
	 */
	public List<Clique> findShortestJunctionTreePath(Collection<INode> from, Collection<INode> to);
	
}