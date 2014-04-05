/**
 * 
 */
package unbbayes.prs.bn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.inference.extension.AssetPropagationInferenceAlgorithm;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.Debug;


/**
 * This class represents a network of assets instead of probabilities.
 * It is not conceptually a probabilistic network, but it is extending 
 * {@link ProbabilisticNetwork} just for class compatibility (i.e.
 * just to make sure algorithms like {@link unbbayes.prs.bn.inference.extension.JunctionTreeLPEAlgorithm}
 * works for assets as well).
 * 
 * @author Shou Matsumoto
 *
 */
public class AssetNetwork extends ProbabilisticNetwork {
	
	private ProbabilisticNetwork relatedNetwork;
	private boolean isToCalculateMarginalsOfAssetNodes = false;
	private float emptySeparatorsContent = Float.NaN;
	private Map<Integer, IRandomVariable> originalCliqueToAssetCliqueMap = null;
	private Map<Integer, IRandomVariable> assetCliqueToOriginalCliqueMap = null;
	private boolean isPropagationExecuted = false;
	
	
	/**
	 * Default constructor is protected to allow inheritance.
	 * Use {@link #getInstance()} to instantiate.
	 */
	protected AssetNetwork() {
		super("Asset Network");
		setHierarchicTree(null);
		setLogManager(null);
//		getLogManager().setEnabled(false);
		
		// reduce memory usage at maximum extent by default, because this is instantiated a lot
		this.setProperties(Collections.EMPTY_MAP);
		copiaNos = nodeList;
		arcosMarkov = null;	// this is not used anyway
		edgeList = Collections.EMPTY_LIST;	// this is not used anyway
	}
	
	
	
	/**
	 * Default constructor method initializing network.
	 * This method calls {@link #setRelatedNetwork(ProbabilisticNetwork)}, which instantiates asset nodes and edges.
	 * @param relatedNetwork : probabilistic network containing the probability distribution of this asset network
	 * @return a network of assets having the same structure of relatedNetwork
	 * @throws InvalidParentException : if relatedNetwork contains invalid edges
	 */
	public static AssetNetwork getInstance(ProbabilisticNetwork relatedNetwork) throws InvalidParentException {
		return AssetNetwork.getInstance(relatedNetwork, false);
	}
	
	/**
	 * Default constructor method initializing network.
	 * This method calls {@link #setRelatedNetwork(ProbabilisticNetwork)}, which instantiates asset nodes and edges.
	 * @param relatedNetwork : probabilistic network containing the probability distribution of this asset network
	 * 
	 * @return a network of assets having the same structure of relatedNetwork
	 * @throws InvalidParentException : if relatedNetwork contains invalid edges
	 */
	public static AssetNetwork getInstance(ProbabilisticNetwork relatedNetwork, boolean isToAddEdge) throws InvalidParentException {
		AssetNetwork ret = new AssetNetwork();
		ret.setRelatedNetwork(relatedNetwork, isToAddEdge);
		if (relatedNetwork != null) {
			ret.setName("Asssets of " + relatedNetwork.getName());
		}
		return ret;
	}

	

	/**
	 * @return the relatedNetwork
	 */
	public ProbabilisticNetwork getRelatedNetwork() {
		return relatedNetwork;
	}
	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.Network#addNode(unbbayes.prs.Node)
	 */
	@Override
	public void addNode(Node node) {
		 nodeList.add(node);
	    // Set its index and add the listener to make sure it is always updated.
	    nodeIndexes.put(node.getName(), new Integer(nodeList.size()-1));
	    // do not add listener
//	    node.addNodeNameChangedListener(nodeNameChangedListener);
	}
	
	/**
	 * Only updates parent and child nodes, but does not update {@link #getEdges()}
	 * @see unbbayes.prs.Network#addEdge(unbbayes.prs.Edge)
	 */
	public void addEdge(Edge edge) throws InvalidParentException {
		edge.getOriginNode().addChild(edge.getDestinationNode());
		edge.getDestinationNode().addParent(edge.getOriginNode());
	    if (edge.getDestinationNode() instanceof IRandomVariable) {
			IRandomVariable v2 = (IRandomVariable) edge.getDestinationNode();
			IProbabilityFunction auxTab = v2.getProbabilityFunction();
			auxTab.addVariable(edge.getOriginNode());
		}
	}

	public void setRelatedNetwork(ProbabilisticNetwork relatedNetwork)  throws InvalidParentException  {
		this.setRelatedNetwork(relatedNetwork, false);
	}
	
	/**
	 * This method sets the relatedNetwork (probabilistic network w/ the probabilities of the assets of this network)
	 * and instantiates asset nodes and edges according to nodes/edges in relatedNetwork. It only considers instances of {@link ProbabilisticNode}
	 * in the relatedNetwork. It does not copy cliques/separators, because some algorithms may not use cliques/separators.
	 * @param relatedNetwork the relatedNetwork to set
	 * @throws InvalidParentException if relatedNetwork contains invalid edges
	 */
	public void setRelatedNetwork(ProbabilisticNetwork relatedNetwork, boolean isToAddEdge)  throws InvalidParentException  {
		if (this.relatedNetwork == null
				|| !this.relatedNetwork.equals(relatedNetwork)) {
//			// stores which node has originated the corresponding asset node
//			Map<Node, AssetNode> nodeToAssetMap = new HashMap<Node, AssetNode>();
			
			// clear old nodes and edges
			this.clearNodes();
			this.getNodeIndexes().clear();
			this.clearEdges();
			
			// copy nodes
			for (Node aux : relatedNetwork.getNodes()) {
				if (aux instanceof ProbabilisticNode) {
					ProbabilisticNode node = (ProbabilisticNode) aux;
					// only consider probabilistic nodes
					AssetNode assetNode = AssetNode.getInstance();
					assetNode.setToCalculateMarginal(isToCalculateMarginalsOfAssetNodes());
					assetNode.setName(node.getName());
					assetNode.setInternalIdentificator(node.getInternalIdentificator());
					// copy states
					for (int i = 0; i < node.getStatesSize(); i++) {
						assetNode.appendState(node.getStateAt(i));
					}
					assetNode.initMarginalList();	// guarantee that marginal list is initialized
					this.addNode(assetNode);
//					nodeToAssetMap.put(node, assetNode);
				}
			}
			
			if (isToAddEdge) {
				// copy edges
				for (Edge edge : relatedNetwork.getEdges()) {
//				if (nodeToAssetMap.containsKey(edge.getOriginNode()) && nodeToAssetMap.containsKey(edge.getDestinationNode())) {
//					// both nodes in the edge are mapped
//					Edge newEdge = new Edge(nodeToAssetMap.get(edge.getOriginNode()), nodeToAssetMap.get(edge.getDestinationNode()));
//					this.addEdge(newEdge);
//				}
					// the asset node and original node has the same name, so we can obtain the asset node by searching by name
					Node assetNodeOrig = this.getNode(edge.getOriginNode().getName());	
					Node assetNodeDest = this.getNode(edge.getDestinationNode().getName());
					if (assetNodeOrig != null && assetNodeDest != null) {
						Edge newEdge = new Edge(assetNodeOrig, assetNodeDest);
						this.addEdge(newEdge);
					} else {
						try {
							Debug.println(getClass(), "Could not create edge: " + assetNodeOrig + " -> " + assetNodeDest);
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
				}
			}
		}
		this.relatedNetwork = relatedNetwork;
		
	}

	/**
	 * This method will always return false, because AssetNetwork will never contain
	 * utility nodes or decision nodes.
	 */
	public boolean isID() {
		return false;
	}

	/**
	 * This method does the same as {@link ProbabilisticNetwork#removeNode(Node)}, but it does not call
	 * {@link PotentialTable#normalize()}.
	 */
	public void removeNode(Node nodeToRemove) {
		/*
		 * NOTE: this method assumes that the junction tree algorithm is implemented in a way
		 * which it ignores separators containing 0 nodes (i.e. the empty separator still represents
		 * a link between cliques, but such link is used only for accessing cliques in a 
		 * hierarchic ordering, and it is not supposed to propagate evidences - e.g. absorb will do nothing).
		 */
		if (!this.isID() && !this.isHybridBN()) {
			// attempt to remove probabilistic nodes from junction tree as well
			// this only makes sense if we are attempting to remove nodes from a compiled BN
			// (nonsense if you are deleting nodes in edit mode)
			if (getJunctionTree() != null) {
				// remove variable from separators
				if (getJunctionTree().getSeparators() != null) {
					for (Separator separator : getJunctionTree().getSeparators()) {
						// testing presence of variable in a list has almost same comput. cost of just deleting
						PotentialTable sepTable = separator.getProbabilityFunction();
						sepTable.purgeVariable(nodeToRemove, false);
						separator.getNodes().remove(nodeToRemove);
					}
				}
				// remove variable from cliques
				if (getJunctionTree().getCliques() != null) {
					for (Clique clique : getJunctionTree().getCliquesContainingAllNodes((Collection)Collections.singletonList(nodeToRemove), Integer.MAX_VALUE)) {
						// testing presence of variable in a list has almost same comput. cost of just deleting
						PotentialTable cliqueTable = clique.getProbabilityFunction();
						cliqueTable.purgeVariable(nodeToRemove, false);
						clique.getAssociatedProbabilisticNodes().remove(nodeToRemove);
						clique.getNodes().remove(nodeToRemove);
					}
				}
			}
		}

	    // delete node from the list of all available nodes in the network
		int indexToRemove = this.getNodeIndex(nodeToRemove.getName());
	    if (indexToRemove >= 0) {
	    	getNodes().remove(indexToRemove);
	    	
	    	// rebuild index of names
	    	getNodeIndexes().clear();
	    	for (int indexOfNode = 0; indexOfNode < getNodes().size(); indexOfNode++) {
	    		Node node = getNodes().get(indexOfNode);
	    		getNodeIndexes().put(node.getName(), new Integer(indexOfNode));
	    	}
	    	
	    	// remove edges containing nodeToRemove
	    	List<Edge> edges = getEdges();
	    	for (int i = 0; i < edges.size(); i++) {
	    		Edge edge = edges.get(i);
	    		// compare the internal identificator instead of equals (name comparison) for performance
	    		if (((IRandomVariable)edge.getOriginNode()).getInternalIdentificator() ==  ((IRandomVariable)nodeToRemove).getInternalIdentificator()) {
	    			// we are not removing the parents/children for performance
	    			// TODO remove edge.getOriginNode() from parents of edge.getDestinationNode()
	    			edges.remove(i);
	    			i--;
	    		} else if	(((IRandomVariable)edge.getDestinationNode()).getInternalIdentificator() ==  ((IRandomVariable)nodeToRemove).getInternalIdentificator()) {
	    			// we are not removing the parents/children for performance
    				// TODO remove edge.getDestinationNode() from parents of edge.getOriginNode()
    				edges.remove(i);
    				i--;
	    			
	    		}
			}
	    }
	    
	    // delete copy of node (which is basically used by GUI to change marginals of nodes without changing the original node, or as a list of nodes without utility nodes)
		if (getNodesCopy() != getNodes() && getNodesCopy() != null) {
			getNodesCopy().remove(nodeToRemove);
		}
	}
	
	/**
	 * If false, {@link AssetNode#updateMarginal()} will set the
	 * marginal of the asset nodes to default values
	 * (usually, zeros). If true, {@link AssetNode#updateMarginal()} will
	 * attempt to calculate the marginal assets using 
	 * the clique tables and some specific operation for marginalization
	 * (e.g. sum-out, min-out, max-out).
	 * @param isToCalculateMarginalsOfAssetNodes the isToCalculateMarginalsOfAssetNodes to set
	 * @see AssetNode#setToCalculateMarginal(boolean)
	 */
	public void setToCalculateMarginalsOfAssetNodes(boolean isToCalculateMarginalsOfAssetNodes) {
		this.isToCalculateMarginalsOfAssetNodes   = isToCalculateMarginalsOfAssetNodes;
		if (getNodes() != null) {
			for (Node node : getNodes()) {
				if (node instanceof AssetNode) {
					AssetNode assetNode = (AssetNode) node;
					assetNode.setToCalculateMarginal(isToCalculateMarginalsOfAssetNodes);
				}
			}
		}
	}
	
	

//	/* (non-Javadoc)
//	 * @see unbbayes.prs.bn.SingleEntityNetwork#updateEvidences()
//	 */
//	@Override
//	public void updateEvidences() throws Exception {
//		int sizeNos = this.getNodesCopy().size();
//		for (int c = 0; c < sizeNos; c++) {
//			TreeVariable node = (TreeVariable) copiaNos.get(c);
//			node.updateEvidences();
//			if (this.getJunctionTree() instanceof LogarithmicMinProductJunctionTree) {
//				// impossible values in logarithm space are represented as positive infinity. However, if asset were negative, 
//				// multiplication with positive infinity may result in negative infinity. Hence, we correct it
//				PotentialTable table = (PotentialTable) node.getAssociatedClique().getProbabilityFunction();
//				for (int i = 0; i < table.tableSize(); i++) {
//					if (table.getValue(i) == Float.NEGATIVE_INFINITY) {
//						table.setValue(i, Float.POSITIVE_INFINITY);
//					}
//				}
//			}
//		}
//
//		try {
//			junctionTree.consistency();
//		} catch (Exception e) {
//			try {
//				initialize();
//			} catch (Exception e2) {
//				// added this catch, because if an exception is thrown at initialize(), 
//				// the exception e will be lost.
//				e2.printStackTrace();
//			}
//			throw e;
//		}
//		updateMarginais();
//		resetLikelihoods();
//	}

	/**
	 * If false, {@link AssetNode#updateMarginal()} will set the
	 * marginal of the asset nodes to default values
	 * (usually, zeros). If true, {@link AssetNode#updateMarginal()} will
	 * attempt to calculate the marginal assets using 
	 * the clique tables and some specific operation for marginalization
	 * (e.g. sum-out, min-out, max-out).
	 * @return the isToCalculateMarginalsOfAssetNodes
	 * @see AssetNode#setToCalculateMarginal(boolean)
	 */
	public boolean isToCalculateMarginalsOfAssetNodes() {
		return isToCalculateMarginalsOfAssetNodes;
	}

	/**
	 * Asset nets can never be hybrid bn.
	 * @see unbbayes.prs.bn.SingleEntityNetwork#isHybridBN()
	 */
	public boolean isHybridBN() {
		return false;
	}



	/* (non-Javadoc)
	 * @see unbbayes.prs.Network#addProperty(java.lang.String, java.lang.Object)
	 */
	public void addProperty(String name, Object value) {
		if (this.getProperties() == Collections.EMPTY_MAP) {
			// make sure we won't throw exception because of immutable map
			this.setProperties(new HashMap<String, Object>());
		}
		super.addProperty(name, value);
	}
	
	/**
	 * set the default value in asset tables of empty separators.
	 * @param emptySeparatorsContent
	 */
	public void setEmptySeparatorsDefaultContent(float emptySeparatorsContent) {
		this.emptySeparatorsContent = emptySeparatorsContent;
	}


	/**
	 * @return the default value in asset tables of empty separators.
	 */
	public float getEmptySeparatorsDefaultContent() {
		return this.emptySeparatorsContent;
	}


	/**
	 * @return a mapping from {@link IRandomVariable#getInternalIdentificator()} of cliques/separators of probabilities 
	 * to cliques/separators of assets.
	 */
	public Map<Integer, IRandomVariable> getOriginalCliqueToAssetCliqueMap() {
		return originalCliqueToAssetCliqueMap;
	}


	/**
	 * sets the mapping from {@link IRandomVariable#getInternalIdentificator()} of cliques/separators of probabilities 
	 * to cliques/separators of assets.
	 * @param originalCliqueToAssetCliqueMap
	 */
	public void setOriginalCliqueToAssetCliqueMap(
			Map<Integer, IRandomVariable> originalCliqueToAssetCliqueMap) {
		this.originalCliqueToAssetCliqueMap = originalCliqueToAssetCliqueMap;
	}


	/**
	 * sets the map linking cliques from probabilistic network to
	 * cliques in asset network.
	 * @param map
	 */
	public void setAssetCliqueToOriginalCliqueMap(
			Map<Integer, IRandomVariable> map) {
		this.assetCliqueToOriginalCliqueMap = map;
	}


	/**
	 * @return the map linking cliques from probabilistic network to
	 * cliques in asset network.
	 */
	public Map<Integer, IRandomVariable> getAssetCliqueToOriginalCliqueMap() {
		return assetCliqueToOriginalCliqueMap;
	}



	/**
	 * this method does nothing, because in asset netwoks, the {@link #getNodes()}
	 * and {@link #getNodesCopy()} are the same instance.
	 * @see unbbayes.prs.bn.SingleEntityNetwork#resetNodesCopy()
	 */
	public void resetNodesCopy() {
		copiaNos = getNodes();
	}



	/**
	 * In {@link AssetNetwork}, this method
	 * simply returns {@link #getNodes()}, because
	 * there are no utility nodes
	 * @see unbbayes.prs.bn.SingleEntityNetwork#getNodesCopy()
	 */
	public ArrayList<Node> getNodesCopy() {
		return getNodes();
	}



	/**
	 * This is a flag indicating that a  propagation (min propagation) was executed previously, so that
	 * methods related to this network knows that the asset values in this junction tree
	 * are not in its original form.
	 * @return the isPropagationExecuted
	 * @see AssetPropagationInferenceAlgorithm#runMinPropagation(Map, boolean)
	 * @see AssetPropagationInferenceAlgorithm#undoMinPropagation()
	 */
	public boolean isPropagationExecuted() {
		return this.isPropagationExecuted;
	}



	/**
	 * This is a flag indicating that a  propagation (min propagation) was executed previously, so that
	 * methods related to this network knows that the asset values in this junction tree
	 * are not in its original form.
	 * @param isPropagationExecuted the isPropagationExecuted to set
	 * @see AssetPropagationInferenceAlgorithm#runMinPropagation(Map, boolean)
	 * @see AssetPropagationInferenceAlgorithm#undoMinPropagation()
	 */
	public void setPropagationExecuted(boolean isPropagationExecuted) {
		this.isPropagationExecuted = isPropagationExecuted;
	}
	
	
}
