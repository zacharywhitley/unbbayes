/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import unbbayes.controller.INetworkMediator;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.AssetNode;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.DefaultJunctionTreeBuilder;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.Separator;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener;

/**
 * This is a combination of algorithm for updating q values and propagating min-q values.
 * @author Shou Matsumoto
 *
 */
public class AssetPropagationInferenceAlgorithm extends JunctionTreeLPEAlgorithm implements IAssetNetAlgorithm  {
	
	/** 
	 * Name of the property in {@link Graph#getProperty(String)} which manages assets prior to {@link #propagate()}. 
	 * The content is a Map<IRandomVariable, PotentialTable>, 
	 * which maps a node to its clique-wise probability table (probabilities for all nodes in the same clique/separator).
	 */
	public static final String LAST_PROBABILITY_PROPERTY = AssetPropagationInferenceAlgorithm.class.getName() + ".lastProbabilityMap";
	
	/** 
	 * Name of the property in {@link Graph#getProperty(String)} which manages the links from cliques/separators in the probabilistic network
	 * (i.e. {@link #getRelatedProbabilisticNetwork()})
	 * to cliques/separators in the asset network (i.e. {@link #getNetwork()}). 
	 * Cliques/separators in the asset networks were all created from cliques/separators
	 * in the probabilistic network in {@link #setRelatedProbabilisticNetwork(ProbabilisticNetwork)}.
	 */
	public static final String ORIGINALCLIQUE_TO_ASSETCLIQUE_MAP_PROPERTY = AssetPropagationInferenceAlgorithm.class.getName() + ".originalCliqueToAssetCliqueMap";

	private ProbabilisticNetwork relatedProbabilisticNetwork;
	
	private float defaultInitialAssetQuantity = 1000.0f;
	
	private INetworkMediator mediator;
	

	private boolean isToPropagateForGlobalConsistency = true;

	private boolean isToUpdateSeparators = true;
	
	private boolean isToUpdateOnlyEditClique = false;
	
	private boolean isToAllowQValuesSmallerThan1 = true;

//	private Map<IRandomVariable, IRandomVariable> originalCliqueToAssetCliqueMap;
	
	private Comparator<IRandomVariable> randomVariableComparator = new Comparator<IRandomVariable>() {
		public int compare(IRandomVariable v1, IRandomVariable v2) {
			return v1.toString().compareTo(v2.toString());
		}
	};

	/** @see AssetPropagationInferenceAlgorithm#getCellValuesComparator() */
	private Comparator cellValuesComparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			// ignore zeros
			if (Float.compare((Float)o1, 0.0f) == 0) {
				return (Float.compare((Float)o2, 0.0f) == 0)?0:1;
			}
			if (Float.compare((Float)o2, 0.0f) == 0) {
				return -1;
			}
			return Float.compare((Float)o1, (Float)o2);
		}
	};

	private boolean isToLogAssets = false;

	private IRandomVariable editCliqueOrSeparator;


//	private AssetAwareInferenceAlgorithm assetAwareInferenceAlgorithm;
	
//	private IInferenceAlgorithmListener listenerToStorePriorProbability = new IInferenceAlgorithmListener() {
//		public void onBeforeRun(IInferenceAlgorithm algorithm) {}
//		public void onBeforeReset(IInferenceAlgorithm algorithm) {}
//		public void onBeforePropagate(IInferenceAlgorithm algorithm) {
//			// TODO Auto-generated method stub
//			
//		}
//		public void onAfterRun(IInferenceAlgorithm algorithm) {}
//		public void onAfterReset(IInferenceAlgorithm algorithm) {}
//		public void onAfterPropagate(IInferenceAlgorithm algorithm) {
//			// propagate assets after the probabilities were propagated
//			AssetPropagationInferenceAlgorithm.this.propagate();
//		}
//	};
	
	/**
	 * Default constructor is protected in order to allow inheritance.
	 * Use {@link #getInstance(ProbabilisticNetwork)} to instantiate objects.
	 * @see #getInstance(ProbabilisticNetwork)
	 */
	protected AssetPropagationInferenceAlgorithm() {}
	
	/**
	 * Default constructor method initializing fields.
	 * @param relatedProbabilisticNetwork : network containing the probability. This network will be used as a basis for creating an asset network 
	 * (the one retrievable from {@link #getNetwork()}).
	 * @return
	 * @throws IllegalArgumentException if the relatedProbabilisticNetwork is in an unexpected format.
	 * @throws InvalidParentException if the relatedProbabilisticNetwork contains invalid edges.
	 * @see #getRelatedProbabilisticNetwork()
	 * @see #getNetwork()
	 */
	public static IAssetNetAlgorithm getInstance(ProbabilisticNetwork relatedProbabilisticNetwork) throws IllegalArgumentException, InvalidParentException {
		AssetPropagationInferenceAlgorithm ret = new AssetPropagationInferenceAlgorithm();
		ret.setRelatedProbabilisticNetwork(relatedProbabilisticNetwork);
		// this is a builder to create instances of MinProductJunctionTree. 
		ret.setDefaultJunctionTreeBuilder(new DefaultJunctionTreeBuilder(MinProductJunctionTree.class));
		
		// initialize listener to be called after propagation, to log asset net
		ret.getInferenceAlgorithmListeners().clear();
		ret.addInferencceAlgorithmListener(new IInferenceAlgorithmListener() {
			public void onBeforeRun(IInferenceAlgorithm algorithm) {}
			public void onBeforeReset(IInferenceAlgorithm algorithm) {}
			public void onBeforePropagate(IInferenceAlgorithm algorithm) {}
			public void onAfterRun(IInferenceAlgorithm algorithm) {}
			public void onAfterReset(IInferenceAlgorithm algorithm) {}
			public void onAfterPropagate(IInferenceAlgorithm algorithm) {
				// I'm adding the log feature as a dynamic command, because it is not mandatory
				// so, anyone can remove this feature dynamically
				try {
					((AssetPropagationInferenceAlgorithm)algorithm).logAssets();
				} catch (Exception e) {
					Debug.println(getClass(), "Failed to log assets: " + e.getMessage(), e);
				}
			}
		});
		return ret;
	}



	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#run()
	 */
	public void run() throws IllegalStateException {
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
			listener.onBeforeRun(this);
		}

		
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
			listener.onAfterRun(this);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getName()
	 */
	public String getName() {
		return "Asset Propagation Algorithm";
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getDescription()
	 */
	public String getDescription() {
		return "Algorithm to propagate assets given another probabilistic network with same topology.";
	}

	/**
	 * This method just restores all cells of the q-tables to be {@link #getDefaultInitialAssetQuantity()}.
	 * It uses {@link #initAssetPotential(PotentialTable)} internally.
	 */
	public void reset() {
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
			listener.onBeforeReset(this);
		}
		
		for (Clique clique : this.getAssetNetwork().getJunctionTree().getCliques()) {
			this.initAssetPotential(clique.getProbabilityFunction());
		}
		
		for (Separator sep : this.getAssetNetwork().getJunctionTree().getSeparators()) {
			this.initAssetPotential(sep.getProbabilityFunction());
		}
		
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
			listener.onAfterReset(this);
		}
	}

	/**
	 *  This method uses the property {@link #LAST_PROBABILITY_PROPERTY} of {@link #getNetwork()} and {@link Graph#getProperty(String)}
	 * in order to calculate the ratio of change in probability compared to the previous values.
	 * It assumes {@link #updateProbabilityPriorToPropagation()} was called prior to this method.
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#propagate()
	 */
	public void propagate() {
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
			listener.onBeforePropagate(this);
		}

		// assertions
		if (this.getNetwork() == null) {
			throw new NullPointerException(this.getClass() + "#propagate() was called without setting the asset network to a non-null value." );
		}
		if (this.getRelatedProbabilisticNetwork() == null) {
			throw new NullPointerException(this.getClass() + "#propagate() was called without setting the probabilistic network to a non-null value." );
		}
		
//		 the assets to be updated
//		Map<Clique, PotentialTable> currentAssetsMap = (Map<Clique, PotentialTable>) this.getNetwork().getProperty(CURRENT_ASSETS_PROPERTY);
		
		// cliques/separators to update
		Set<IRandomVariable> cliquesOrSepsToUpdate = new HashSet<IRandomVariable>();
		if (isToUpdateOnlyEditClique()) {
			// update only the edited clique
			cliquesOrSepsToUpdate.add(getEditCliqueOrSeparator());
		} else {
			// update all cliques
			cliquesOrSepsToUpdate.addAll(getOriginalCliqueToAssetCliqueMap().keySet());
		}
		
		// a mapping to be used to store old values, so that we can revert changes when q-values goes below 1
		Map<IRandomVariable, PotentialTable> oldAssetTables = new HashMap<IRandomVariable, PotentialTable>();	// this shall be used only when isToAllowQValuesSmallerThan1() == false
		
		for (IRandomVariable origCliqueOrSeparator : cliquesOrSepsToUpdate) {
			// extract clique related to the asset 
			IRandomVariable assetCliqueOrSeparator = getOriginalCliqueToAssetCliqueMap().get(origCliqueOrSeparator);
			if (assetCliqueOrSeparator == null) {
				continue;	//ignore null entry
			}
			if ((assetCliqueOrSeparator instanceof Separator) && !isToUpdateSeparators()) {
				// this is a separator, but algorithm is configured not to update separators.
				continue;
			}
			
			// extract asset table. We assume we are using table-based representation (PotentialTable)
			PotentialTable assetTable = (PotentialTable) assetCliqueOrSeparator.getProbabilityFunction();
			
//			// extract probabilistic node related to asset node (they have the same name)
//			// Note: if it is not a probabilistic node, then it means that there is an asset node created for non-probabilistic node (this is unexpected)
//			ProbabilisticNode probNode = (ProbabilisticNode) getRelatedProbabilisticNetwork().getNode(assetNode.getName());
			
			// extract current probability values from prob node's clique. We assume we are using table-based representation (PotentialTable)
			PotentialTable currentProbabilities = (PotentialTable) origCliqueOrSeparator.getProbabilityFunction();
			
			// extract previous probability (i.e. prior to propagation) values from network property
			PotentialTable previousProbabilities = ((Map<IRandomVariable, PotentialTable>) this.getNetwork().getProperty(LAST_PROBABILITY_PROPERTY)).get(origCliqueOrSeparator);
			
			// assertion: tables must be non-null and have same size
			if (currentProbabilities == null || previousProbabilities == null || assetTable == null
					|| ( assetTable.tableSize() != currentProbabilities.tableSize() )
					|| ( assetTable.tableSize() != previousProbabilities.tableSize() )) {
				throw new IllegalStateException("The assets and probabilities of asset clique/separator " + assetCliqueOrSeparator 
						+ " are not synchronized with probability clique/separator " + origCliqueOrSeparator);
			}
			
			// backup old asset table (so that we can revert asset tables when necessary)
			if (!isToAllowQValuesSmallerThan1()) {
				oldAssetTables.put(assetCliqueOrSeparator, (PotentialTable) assetTable.clone());
			}
			
			// perform clique-wise update of asset values using a ratio
			for (int i = 0; i < assetTable.tableSize(); i++) {
				// multiply assets by the ratio (current probability values / previous probability values)
				float newValue = assetTable.getValue(i) * ( currentProbabilities.getValue(i) / previousProbabilities.getValue(i) );
				// check if assets is <= 1
				if (!isToAllowQValuesSmallerThan1() && (newValue <= 1f)) {
					// revert all previous cliques/separators, including current
					for (IRandomVariable modifiedCliqueOrSep : oldAssetTables.keySet()) {
						assetTable = (PotentialTable) modifiedCliqueOrSep.getProbabilityFunction();
						// CAUTION: the following code only works because the vector of old and new values have the same size
						assetTable.setValues(oldAssetTables.get(modifiedCliqueOrSep).getValues());
						// update marginal of asset nodes
						for (int j = 0; j < assetTable.getVariablesSize(); j++) {
							((TreeVariable)assetTable.getVariableAt(i)).updateMarginal();
						}
					}
					throw new ZeroAssetsException("Asset's q-values of clique/separator " + assetCliqueOrSeparator + " went to " + newValue);
				}
				assetTable.setValue(i,  newValue);
			}
			
			// update marginal of asset nodes
			for (int i = 0; i < assetTable.getVariablesSize(); i++) {
				((TreeVariable)assetTable.getVariableAt(i)).updateMarginal();
			}
			
		}
		
		// do min calibration (propagate minimum q-values)
		// it assumes that the junction tree used by this algorithm is a MinProductJunctionTree, and normalization is disabled
		// createAssetNetFromProbabilisticNet is supposed to instantiate MinProductJunctionTree and disable its normalization
		if (isToPropagateForGlobalConsistency()) {
			this.runMinPropagation();
		}
		
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
			listener.onAfterPropagate(this);
		}
	}
	
	
	
	/**
	 * This method forces the algorithm to store the current probabilities of the {@link #getRelatedProbabilisticNetwork()},
	 * so that it can be used posteriorly by {@link #propagate()} in order to to calculate the ratio of the change of probability 
	 * between the current (probability when {@link #propagate()} was called) one and the last (probability when this method was called) one.
	 * Those values are stored in the network property {@link #LAST_PROBABILITY_PROPERTY} of {@link #getNetwork()}, which is retrievable from {@link Graph#getProperty(String)}.
	 */
	public void updateProbabilityPriorToPropagation() {
		// assertion
		if (this.getNetwork() == null) {
			throw new NullPointerException(this.getClass() + "#updateProbabilityPriorToPropagation() was called without setting the asset network to a non-null value." );
		}
		if (this.getRelatedProbabilisticNetwork() == null) {
			throw new NullPointerException(this.getClass() + "#updateProbabilityPriorToPropagation() was called without setting the probabilistic network to a non-null value." );
		}
		// property value to set
		Map<IRandomVariable, PotentialTable> property = new TreeMap<IRandomVariable, PotentialTable>(this.getRandomVariableComparator());
		
		if (isToUpdateOnlyEditClique()) {
			for (Node node : getRelatedProbabilisticNetwork().getNodes()) {
				if (node instanceof TreeVariable) {
					TreeVariable treeVar = (TreeVariable) node;
					if (treeVar.hasLikelihood()) {
						setEditCliqueOrSeparator(treeVar.getAssociatedClique());
						// TODO do not assume only 1 edit per propagation
						break;
					}
				}
			}
			// fill property with clones, so that changes on the original tables won't affect the property
			PotentialTable table = (PotentialTable) ((PotentialTable) getEditCliqueOrSeparator().getProbabilityFunction()).clone();
			property.put(getEditCliqueOrSeparator(), table); // update property
			// overwrite asset network property
			this.getNetwork().addProperty(LAST_PROBABILITY_PROPERTY, property);
			return;
		}
		
		// iterate on cliques
		for (Clique clique : this.getRelatedProbabilisticNetwork().getJunctionTree().getCliques()) {
			// fill property with clones, so that changes on the original tables won't affect the property
			PotentialTable table = (PotentialTable) ((PotentialTable) clique.getProbabilityFunction()).clone();
			property.put(clique, table); // update property
		}
		
		// iterate on separators
		for (Separator separator : this.getRelatedProbabilisticNetwork().getJunctionTree().getSeparators()) {
			// fill property with clones, so that changes on the original tables won't affect the property
			PotentialTable table = (PotentialTable) ((PotentialTable) separator.getProbabilityFunction()).clone();
			property.put(separator, table); // update property
		}
		
		// overwrite asset network property
		this.getNetwork().addProperty(LAST_PROBABILITY_PROPERTY, property);
	}


	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#setMediator(unbbayes.controller.INetworkMediator)
	 */
	public void setMediator(INetworkMediator mediator) {
		this.mediator = mediator;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getMediator()
	 */
	public INetworkMediator getMediator() {
		return this.mediator;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#getRelatedProbabilisticNetwork()
	 */
	public ProbabilisticNetwork getRelatedProbabilisticNetwork() {
		return this.relatedProbabilisticNetwork;
	}

	/**
	 * Initializes the {@link #getNetwork()} (asset network) and 
	 * delegates to {@link IInferenceAlgorithm#getNetwork()} from the algorithm
	 * obtained from {@link #getAssetAwareInferenceAlgorithm()}
	 * @param relatedProbabilisticNetwork the relatedProbabilisticNetwork to set
	 * @throws InvalidParentException 
	 * @throws IllegalArgumentException 
	 */
	public void setRelatedProbabilisticNetwork(
			ProbabilisticNetwork relatedProbabilisticNetwork) throws IllegalArgumentException, InvalidParentException {
		
		if (this.getRelatedProbabilisticNetwork() == null 
				|| !this.getRelatedProbabilisticNetwork().equals(relatedProbabilisticNetwork)) {
			this.setNetwork(this.createAssetNetFromProbabilisticNet(relatedProbabilisticNetwork));
		}
		this.relatedProbabilisticNetwork = relatedProbabilisticNetwork;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#createAssetNetFromProbabilisticNet(unbbayes.prs.bn.ProbabilisticNetwork)
	 */
	public AssetNetwork createAssetNetFromProbabilisticNet(ProbabilisticNetwork relatedProbabilisticNetwork)throws InvalidParentException{
		// assertion
		if (relatedProbabilisticNetwork == null) {
//			throw new NullPointerException("relatedProbabilisticNetwork == null" );
			return null;
		}
		
		// object to return
		AssetNetwork ret = AssetNetwork.getInstance(relatedProbabilisticNetwork);
		
		// copy/fill clique
		if (relatedProbabilisticNetwork.getJunctionTree() != null) {
			// prepare JT, which is a data format to store cliques and separators
			// use min-product junction tree, so that we can use Least Probable explanation algorithm for obtaining min-q values when we call propagate()
			MinProductJunctionTree jt = null;
			try {
				jt = (MinProductJunctionTree) getDefaultJunctionTreeBuilder().buildJunctionTree(ret);
				// do not normalize q-table (q-tables are not bound to 1, like probabilities)
				jt.setToNormalize(false);	
			} catch (Exception e) {
				throw new RuntimeException("Could not instantiate junction tree for Least Probable Explanation algorithm", e);
			}	
			
			// the junction tree of this asset net
			ret.setJunctionTree(jt);
			
			setOriginalCliqueToAssetCliqueMap(ret, new TreeMap<IRandomVariable, IRandomVariable>(this.getRandomVariableComparator()));
//			setOriginalCliqueToAssetCliqueMap(ret, new HashMap<IRandomVariable, IRandomVariable>());

			// copy cliques
			for (Clique origClique : relatedProbabilisticNetwork.getJunctionTree().getCliques()) {
				
				Clique newClique = new Clique();
				
				boolean hasInvalidNode = false;	// this will be true if a clique contains a node not in AssetNetwork.
				for (Node node : origClique.getNodes()) {
					Node assetNode = ret.getNode(node.getName());	// extract associated node, because they are related by name
					if (assetNode == null) {
						hasInvalidNode = true;
						break;
					}
					newClique.getNodes().add(assetNode);
				}
				if (hasInvalidNode) {
					// the original clique has a node not present in the asset net
					continue;
				}
				
				// origClique.getNodes() and origClique.getAssociatedProbabilisticNodes() may be different... Copy both separately. 
				for (Node node : origClique.getAssociatedProbabilisticNodes()) {
					Node assetNode = ret.getNode(node.getName());	// extract associated node, because they are related by name
					if (assetNode == null) {
						hasInvalidNode = true;
						break;
					}
					newClique.getAssociatedProbabilisticNodes().add(assetNode);
				}
				if (hasInvalidNode) {
					// the original clique has a node not present in the asset net
					continue;
				}
				
				newClique.setIndex(origClique.getIndex());
				
				// copy clique potential variables
				PotentialTable origPotential = origClique.getProbabilityFunction();
				PotentialTable assetPotential = newClique.getProbabilityFunction();
				for (int i = 0; i < origPotential.getVariablesSize(); i++) {
					Node assetNode = ret.getNode(origPotential.getVariableAt(i).getName());
					if (assetNode == null) {
						hasInvalidNode = true;
						break;
					}
					assetPotential.addVariable(assetNode);
				}
				if (hasInvalidNode) {
					// the original clique has a node not present in the asset net
					continue;
				}
				
				// fills the values of assets with default values
				this.initAssetPotential(assetPotential);
				
				
				// NOTE: this is ignoring utility table and nodes
				
				jt.getCliques().add(newClique);
				getOriginalCliqueToAssetCliqueMap(ret).put(origClique, newClique);
				
			}
			
			// copy relationship of cliques (separators)
			for (Separator origSeparator : relatedProbabilisticNetwork.getJunctionTree().getSeparators()) {
				boolean hasInvalidNode = false;	// this will be true if a clique contains a node not in AssetNetwork.
				
				// extract the cliques related to the two cliques that the origSeparator connects
				Clique assetClique1 = (Clique) getOriginalCliqueToAssetCliqueMap(ret).get(origSeparator.getClique1());
				Clique assetClique2 = (Clique) getOriginalCliqueToAssetCliqueMap(ret).get(origSeparator.getClique2());
				if (assetClique1 == null || assetClique2 == null) {
					try {
						Debug.println(getClass(), "Could not create separator between " + assetClique1 + " and " + assetClique2);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					continue;
				}
				
				Separator newSeparator = new Separator(assetClique1, assetClique2);
				
				// fill the separator's node list
				for (Node origNode : origSeparator.getNodes()) {
					Node assetNode = ret.getNode(origNode.getName());	// assets and prob nodes have same node names.
					if (assetNode == null) {
						hasInvalidNode = true;
						break;
					}
					newSeparator.getNodes().add(assetNode);
				}
				if (hasInvalidNode) {
					// the original clique has a node not present in the asset net
					continue;
				}
				
				// copy separator potential variables
				PotentialTable origPotential = origSeparator.getProbabilityFunction();
				PotentialTable assetPotential = newSeparator.getProbabilityFunction();
				for (int j = 0; j < origPotential.getVariablesSize(); j++) {
					Node assetNode = ret.getNode(origPotential.getVariableAt(j).getName());
					if (assetNode == null) {
						hasInvalidNode = true;
						break;
					}
					assetPotential.addVariable(assetNode);
				}
				if (hasInvalidNode) {
					// the original clique has a node not present in the asset net
					continue;
				}
				
				// fills the values of assets with default values
				this.initAssetPotential(assetPotential);
				
				
				// NOTE: this is ignoring utility table and nodes
				jt.addSeparator(newSeparator);
				getOriginalCliqueToAssetCliqueMap(ret).put(origSeparator, newSeparator);
			}
			
			// copy relationship between cliques/separator and nodes
			for (Node origNode : relatedProbabilisticNetwork.getNodes()) {
				AssetNode assetNode = (AssetNode)ret.getNode(origNode.getName());
				if (assetNode == null) {
					try {
						Debug.println(getClass(), "Could not find asset node for " + origNode);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					continue;
				}
				assetNode.setAssociatedClique(getOriginalCliqueToAssetCliqueMap(ret).get(((TreeVariable)origNode).getAssociatedClique()));
				
				// force marginal to have some value
				assetNode.updateMarginal();
			}
			
		}
		
		return ret;
	}

	/**
	 * Initializes the assets potential of a asset clique potential.
	 * This method also calls {@link PotentialTable#copyData()}, so that these initial potentials can be restored posteriorly.
	 * @param assetCliquePotential
	 * @see #createAssetNetFromProbabilisticNet(ProbabilisticNetwork)
	 * @see #reset()
	 */
	protected void initAssetPotential(PotentialTable assetCliquePotential) {
		for (int i = 0; i < assetCliquePotential.tableSize(); i++) {
			assetCliquePotential.setValue(i, this.getDefaultInitialAssetQuantity());
		}
		assetCliquePotential.copyData();
	}

//	/**
//	 * @return the inferenceAlgorithmListener
//	 */
//	public List<IInferenceAlgorithmListener> getInferenceAlgorithmListener() {
//		return inferenceAlgorithmListener;
//	}
//
//	/**
//	 * @param inferenceAlgorithmListener the inferenceAlgorithmListener to set
//	 */
//	public void setInferenceAlgorithmListener(
//			List<IInferenceAlgorithmListener> inferenceAlgorithmListener) {
//		this.inferenceAlgorithmListener = inferenceAlgorithmListener;
//	}

	/**
	 * @return the defaultInitialAssetQuantity
	 */
	public float getDefaultInitialAssetQuantity() {
		return defaultInitialAssetQuantity;
	}

	/**
	 * @param defaultInitialAssetQuantity the defaultInitialAssetQuantity to set
	 */
	public void setDefaultInitialAssetQuantity(float defaultInitialAssetQuantity) {
		this.defaultInitialAssetQuantity = defaultInitialAssetQuantity;
	}

	/**
	 * Prints the assets to the log panel
	 */
	public void logAssets() {
		if (!isToLogAssets()) {
			return;
		}
		// initialize message to print
		String explMessage = "\n \n "+ getNetwork() + ": \n \n";
		
		// print probabilities and assets for each node in each clique
		for (Clique probClique : this.getRelatedProbabilisticNetwork().getJunctionTree().getCliques()) {
			try {
				explMessage += "Clique {" + probClique + "}:\n \t \t";
				PotentialTable assetTable = (PotentialTable) this.getOriginalCliqueToAssetCliqueMap().get(probClique).getProbabilityFunction();
				PotentialTable probTable = probClique.getProbabilityFunction();
				for (int i = 0; i < assetTable.tableSize(); i++) {
					explMessage +=  
//						assetTable.getVariableAt(varIndex) 
//						+ " = " + assetTable.getVariableAt(varIndex).getStateAt(stateIndex++) + " : "
//						+ 
						assetTable.getValue(i) + " (" + probTable.getValue(i)*100 + "%)" + "\n \t \t";
				}
				
			} catch (Exception e) {
				Debug.println(getClass(), e.getMessage(), e);
				explMessage += " failed to obtain values: " + e.getMessage();
			}
			explMessage += "\n\n";
		}
		
		// print probabilities and assets for each node in each separator
		for (Separator probSeparator : this.getRelatedProbabilisticNetwork().getJunctionTree().getSeparators()) {
			try {
				explMessage += "Separator {" + probSeparator + "}:\n \t \t";
				PotentialTable assetTable = (PotentialTable) this.getOriginalCliqueToAssetCliqueMap().get(probSeparator).getProbabilityFunction();
				PotentialTable probTable = probSeparator.getProbabilityFunction();
				for (int i = 0; i < assetTable.tableSize(); i++) {
					explMessage +=  
						assetTable.getValue(i) + " (" + probTable.getValue(i)*100 + "%)" + "\n \t \t";
				}
				
			} catch (Exception e) {
				Debug.println(getClass(), e.getMessage(), e);
				explMessage += " failed to obtain values: " + e.getMessage();
			}
			explMessage += "\n\n";
		}
		
		this.getRelatedProbabilisticNetwork().getLogManager().append(explMessage);
		this.getAssetNetwork().getLogManager().append(explMessage);
	}

	/**
	 * Calls {@link #getOriginalCliqueToAssetCliqueMap(Network)}.
	 * @return a mapping between cliques/separators in {@link #getRelatedProbabilisticNetwork()} to cliques/separators in {@link #getAssetNetwork()}.
	 */
	public Map<IRandomVariable, IRandomVariable> getOriginalCliqueToAssetCliqueMap() {
		return this.getOriginalCliqueToAssetCliqueMap(getAssetNetwork());
	}
	
	/**
	 * It loads the property {@link #ORIGINALCLIQUE_TO_ASSETCLIQUE_MAP_PROPERTY} from
	 * the network assetNet, which is supposedly a mapping from cliques/separators of probabilities to cliques/separators of assets.
	 * @param assetNet : the network where the mapping (property {@link #ORIGINALCLIQUE_TO_ASSETCLIQUE_MAP_PROPERTY}) is stored.
	 * @return the map linking cliques from {@link #getRelatedProbabilisticNetwork()} to
	 * cliques in assetNet.
	 */
	protected Map<IRandomVariable, IRandomVariable> getOriginalCliqueToAssetCliqueMap(Network assetNet) {
		try {
			return (Map<IRandomVariable, IRandomVariable>) assetNet.getProperty(ORIGINALCLIQUE_TO_ASSETCLIQUE_MAP_PROPERTY);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * Calls {@link #setOriginalCliqueToAssetCliqueMap(Network, Map)} using {@link #getAssetNetwork()} as a parameter
	 * @param originalCliqueToAssetCliqueMap
	 */
	public void setOriginalCliqueToAssetCliqueMap(Map<IRandomVariable, IRandomVariable> originalCliqueToAssetCliqueMap) {
		this.setOriginalCliqueToAssetCliqueMap(getAssetNetwork(), originalCliqueToAssetCliqueMap);
	}

	/**
	 * It stores originalCliqueToAssetCliqueMap into the property {@link #ORIGINALCLIQUE_TO_ASSETCLIQUE_MAP_PROPERTY} of
	 * the network assetNet, which is supposedly .
	 * @param assetNet :  the network where the mapping is going to be stored.
	 * @param originalCliqueToAssetCliqueMap : a mapping from cliques/separators of probabilities to cliques/separators of assetNet.
	 */
	protected void setOriginalCliqueToAssetCliqueMap(Network assetNet, Map<IRandomVariable, IRandomVariable> originalCliqueToAssetCliqueMap) {
		if (assetNet != null) {
			assetNet.addProperty(ORIGINALCLIQUE_TO_ASSETCLIQUE_MAP_PROPERTY, originalCliqueToAssetCliqueMap);
		}
	}
	
	/**
	 * This {@link Comparator} compares cliques/separators within the {@link #getOriginalCliqueToAssetCliqueMap()},
	 * which is instantiated in {@link #createAssetNetFromProbabilisticNet(ProbabilisticNetwork)}.
	 * 
	 * The {@link Comparator} is used to determine whether two objects are representing the same
	 * clique, even though they may be different objects.
	 * @return the randomVariableComparator
	 */
	public Comparator<IRandomVariable> getRandomVariableComparator() {
		return randomVariableComparator;
	}

	/**
	 * This {@link Comparator} compares cliques/separators within the {@link #getOriginalCliqueToAssetCliqueMap()},
	 * which is instantiated in {@link #createAssetNetFromProbabilisticNet(ProbabilisticNetwork)}.
	 * 
	 * The {@link Comparator} is used to determine whether two objects are representing the same
	 * clique, even though they may be different objects.
	 * @param randomVariableComparator the randomVariableComparator to set
	 */
	public void setRandomVariableComparator(Comparator<IRandomVariable> cliqueComparator) {
		this.randomVariableComparator = cliqueComparator;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#setAssetNetwork(unbbayes.prs.bn.AssetNetwork)
	 */
	public void setAssetNetwork(AssetNetwork network)
			throws IllegalArgumentException {
		this.setNetwork(network);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#getAssetNetwork()
	 */
	public AssetNetwork getAssetNetwork() {
		try {
			return (AssetNetwork) this.getNetwork();
		} catch (ClassCastException e) {
			Debug.println(getClass(), this.getNetwork() + ": " + e.getMessage(), e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.JunctionTreeLPEAlgorithm#markMPEAs100Percent(unbbayes.prs.Graph, boolean)
	 */
	protected void markMPEAs100Percent(Graph network, boolean isToCalculateRelativeProb) {
		// do nothing
	}

	
	
	/**
	 * If set to true, min-propagation junction tree algorithm will be called. False otherwise.
	 * @return the isToPropagateForGlobalConsistency
	 */
	public boolean isToPropagateForGlobalConsistency() {
		return isToPropagateForGlobalConsistency;
	}

	/**
	 * If set to true, min-propagation junction tree algorithm will be called. False otherwise.
	 * @param isToPropagateForGlobalConsistency the isToPropagateForGlobalConsistency to set
	 */
	public void setToPropagateForGlobalConsistency(
			boolean isToPropagateForGlobalConsistency) {
		this.isToPropagateForGlobalConsistency = isToPropagateForGlobalConsistency;
	}

	/**
	 * @return the isToUpdateSeparators
	 */
	public boolean isToUpdateSeparators() {
		return isToUpdateSeparators;
	}

	/**
	 * @param isToUpdateSeparators the isToUpdateSeparators to set
	 */
	public void setToUpdateSeparators(boolean isToUpdateSeparators) {
		this.isToUpdateSeparators = isToUpdateSeparators;
	}

	/** 
	 * This method obtains the min-q states and returns the min-q value.
	 * Currently, it assumes inputOutpuArgumentForExplanation is an empty collection.
	 * It is assumed that {@link #runMinPropagation()} was called prior to this method.
	 * This method will return the minimum local q value if global consistency does not hold
	 * (i.e. if the min-q value differs between different cliques, it will return the smaller one).
	 * @param inputOutpuArgumentForExplanation : an input/output parameter. However, current implementation
	 * assumes this is an empty collection (hence, this is only used as output argument).
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#calculateExplanation(List)
	 * @see IExplanationJunctionTree#calculateExplanation(Graph, IInferenceAlgorithm)
	 * TODO allow multiple combinations of states.
	 */
	public float calculateExplanation( List<Map<INode, Integer>> inputOutpuArgumentForExplanation){
//		IJunctionTree jt = this.getAssetNetwork().getJunctionTree();
//		if (jt instanceof IExplanationJunctionTree) {
//			IExplanationJunctionTree explJT = (IExplanationJunctionTree) jt;
//			return explJT.calculateExplanation(getAssetNetwork(), this);
//			
//		}
//		// avoid returning null
//		return new ArrayList<Map<INode,Integer>>();
		
		// this will hold min-q value
		float ret = Float.NaN;

		// TODO return more than 1 LPE
		Debug.println(getClass(), "Current version returns only 1 explanation");
		
		// this map will contain one combination of min-q state
		Map<INode, Integer> stateMap = new HashMap<INode, Integer>();
		
		// this map contains the min-q value obtained when stateMap was updated (hence, this is synchronized with stateMap). 
		// this map is only needed if this method must return something even when global consistency does not hold 
		// (in order to compare min-q vales between different cliques)
		Map<INode, Float> valueMap = new HashMap<INode, Float>();
		
		for (Clique clique : this.getAssetNetwork().getJunctionTree().getCliques()) {
			PotentialTable table = clique.getProbabilityFunction();
			if (table.tableSize() <= 0) {
				throw new IllegalArgumentException(clique + "- table size: " + table.tableSize());
			}
			// find index of the min value in clique
			int indexOfMinInClique = 0;
			float valueOfMinInClique = table.getValue(indexOfMinInClique);
			for (int i = 1; i < table.tableSize(); i++) {
				if (this.getCellValuesComparator().compare(table.getValue(i), table.getValue(indexOfMinInClique)) < 0) {
					indexOfMinInClique = i;
					valueOfMinInClique = table.getValue(i);
				}
			}
			// the indexes of the states can be obtained from the index of the linearized table by doing the following operation:
			// indexOfLPEOfNthNode = mod(indexOfMinInClique / prodNumberOfStatesPrevNodes, numberOfStates). 
			// prodNumberOfStatesPrevNodes is the product of the number of states for all previous nodes. If this is the first node, then prodNumberOfStatesPrevNodes = 1.
			// e.g. suppose there are 2 nodes A(w/ 4 states), B(w/ 3 states) and C (w/ 2 states). If the maximum probability occurs at index 5, then
			// the state of A is mod(5/1 , 4) = 1, and the state of B is mod(5/4, 3) = 1, and state of C is mod(5/(4*3),2) = 0.
			// I.e.
			//      |                             c0                            |                             c1                            |
			//      |         b0        |         b1        |         b2        |         b0        |         b1        |         b2        |
			//      | a0 | a1 | a2 | a3 | a0 | a1 | a2 | a3 | a0 | a1 | a2 | a3 | a0 | a1 | a2 | a3 | a0 | a1 | a2 | a3 | a0 | a1 | a2 | a3 |
			//index:| 0  |  1 |  2 |  3 | 4  | 5  | 6  | 7  |  8 | 9  | 10 | 11 | 12 | 13 | 14 | 15 | 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 |
			int prodNumberOfStatesPrevNodes = 1;
			for (int i = 0; i < table.getVariablesSize(); i++) {
				INode node = table.getVariableAt(i);
				int numberOfStates = node.getStatesSize();
				// number of states must be strictly positive
				if (numberOfStates <= 0) {
					try {
						Debug.println(getClass(), "[Warning] Size of " + table.getVariableAt(i) + " is " + numberOfStates);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					continue;
				}
				// calculate most probable state
				int indexOfLPEOfNthNode = (indexOfMinInClique / prodNumberOfStatesPrevNodes) % numberOfStates;
				// add to states if it was not already added
				if (!stateMap.containsKey(node)) {
					// this is the first time we add this entry. Add it
					stateMap.put(node, indexOfLPEOfNthNode);
					valueMap.put(node, valueOfMinInClique);
					
					ret = valueOfMinInClique;	// assuming that global consistency holds...
				} else {
					// check consistency (min state should be unique between cliques)
					if (!stateMap.get(node).equals(indexOfLPEOfNthNode)) {
						if (this.getCellValuesComparator().compare(valueMap.get(node), valueOfMinInClique) < 0) {
							// new value is greater. Update
							stateMap.put(node, indexOfLPEOfNthNode);
							valueMap.put(node, valueOfMinInClique);
							ret = valueOfMinInClique;	// this clique has a smaller local min-q value...
						}
						try {
							Debug.println(getClass(), "Obtained states differ between cliques (clique inconsistency)... The current clique is: " 
									+ clique + "; node is " + node + "; previous state: " + stateMap.get(node) + "; index of state found in current clique: " + indexOfLPEOfNthNode);
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
				}
				prodNumberOfStatesPrevNodes *= numberOfStates;
			}
		}
		
		// handle the input/output argument
		if (inputOutpuArgumentForExplanation != null && !inputOutpuArgumentForExplanation.contains(stateMap)) {
			inputOutpuArgumentForExplanation.add(stateMap);
		}
		return ret;
	}

	/**
	 * Just executes {@link JunctionTreeLPEAlgorithm#propagate()}.
	 * It uses {@link PotentialTable#copyData()} for all cliques and separator's tables,
	 * so that {@link #undoMinPropagation()} can call {@link PotentialTable#restoreData()}
	 * in order to revert any changes.
	 * This will run min propagation even when {@link #isToPropagateForGlobalConsistency()} == false.
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#runMinPropagation()
	 */
	public void runMinPropagation() {
		// "store" all clique potentials, so that it can be restored later
		for (Clique clique : this.getAssetNetwork().getJunctionTree().getCliques()) {
			clique.getProbabilityFunction().copyData();
		}
		// "store" all separator potential, so that it can be restored later
		for (Separator sep : this.getAssetNetwork().getJunctionTree().getSeparators()) {
			sep.getProbabilityFunction().copyData();
		}
		
		// disable listeners of propagate() temporary, because this is not the official "propagation" service offered by this class
		// if we do not do so, these listeners will be invokes as if we are executing the official propagate().
		List<IInferenceAlgorithmListener> backup = this.getInferenceAlgorithmListeners();
		this.setInferenceAlgorithmListeners(new ArrayList<IInferenceAlgorithmListener>(0));
		super.propagate();
		// restore listeners
		this.setInferenceAlgorithmListeners(backup);
	}

	/**
	 * This method assumes that {@link #runMinPropagation()} was executed prior to this method.
	 * Basically, this method will call {@link PotentialTable#restoreData()} for all
	 * clique and separator tables. Hence, it is assumed that {@link #runMinPropagation()} has
	 * stored the values of such tables prior to the propagation using {@link PotentialTable#copyData()}.
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#undoMinPropagation()
	 */
	public void undoMinPropagation() {
		// "reset" all clique potentials
		for (Clique clique : this.getAssetNetwork().getJunctionTree().getCliques()) {
			clique.getProbabilityFunction().restoreData();
		}
		// "reset" all separator potential
		for (Separator sep : this.getAssetNetwork().getJunctionTree().getSeparators()) {
			sep.getProbabilityFunction().restoreData();
		}
		// reset any findings of asset nodes
		for (Node node : getAssetNetwork().getNodes()) {
			if (node instanceof AssetNode) {
				((AssetNode) node).resetEvidence();
			}
		}
	}
	
	/**
	 * This comparator is used by {@link #calculateExplanation()} in order
	 * to obtain the minimum value in a clique table. By changing this comparator,
	 * it is possible for {@link #calculateExplanation()} to obtain, for instance, the maximum instead.
	 * @return the cellValuesComparator
	 */
	public Comparator getCellValuesComparator() {
		return cellValuesComparator;
	}

	/**
	 * This comparator is used by {@link #calculateExplanation()} in order
	 * to obtain the minimum value in a clique table. By changing this comparator,
	 * it is possible for {@link #calculateExplanation()} to obtain, for instance, the maximum instead.
	 * @param cellValuesComparator the cellValuesComparator to set
	 */
	public void setCellValuesComparator(Comparator cellValuesComparator) {
		this.cellValuesComparator = cellValuesComparator;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.JunctionTreeMPEAlgorithm#updateMarginals(unbbayes.prs.Graph)
	 */
	protected void updateMarginals(Graph graph) {
		//  do nothing, because marginals are not important for asset nodes
	}

	/**
	 * If this is false, {@link #logAssets()} will
	 * not generate logs.
	 * @param isToLogAssets the isToLogAssets to set
	 */
	public void setToLogAssets(boolean isToLogAssets) {
		this.isToLogAssets = isToLogAssets;
	}

	/**
	 * If this is false, {@link #logAssets()} will
	 * not generate logs.
	 * @return the isToLogAssets
	 */
	public boolean isToLogAssets() {
		return isToLogAssets;
	}

	/**
	 * @return the isToUpdateOnlyEditClique
	 */
	public boolean isToUpdateOnlyEditClique() {
		return isToUpdateOnlyEditClique;
	}

	/**
	 * @param isToUpdateOnlyEditClique the isToUpdateOnlyEditClique to set
	 */
	public void setToUpdateOnlyEditClique(boolean isToUpdateOnlyEditClique) {
		this.isToUpdateOnlyEditClique = isToUpdateOnlyEditClique;
	}

	/**
	 * @param editCliquesOrSeparators the editCliquesOrSeparators to set
	 */
	protected void setEditCliqueOrSeparator(IRandomVariable editCliqueOrSeparator) {
		this.editCliqueOrSeparator = editCliqueOrSeparator;
	}

	/**
	 * @return the editCliquesOrSeparators
	 */
	protected IRandomVariable getEditCliqueOrSeparator() {
		return editCliqueOrSeparator;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#isToAllowQValuesSmallerThan1()
	 */
	public boolean isToAllowQValuesSmallerThan1() {
		return isToAllowQValuesSmallerThan1;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#setToAllowQValuesSmallerThan1(boolean)
	 */
	public void setToAllowQValuesSmallerThan1(boolean isToAllowQValuesSmallerThan1) {
		this.isToAllowQValuesSmallerThan1 = isToAllowQValuesSmallerThan1;
	}

	

//	/**
//	 * @return the assetAwareInferenceAlgorithm
//	 */
//	public AssetAwareInferenceAlgorithm getAssetAwareInferenceAlgorithm() {
//		return assetAwareInferenceAlgorithm;
//	}
//
//	/**
//	 * This method also fills the assetAwareInferenceAlgorithm with a
//	 * {@link IInferenceAlgorithmListener} which stores the probabilities
//	 * of {@link #getRelatedProbabilisticNetwork()} before the propagation.
//	 * @param assetAwareInferenceAlgorithm the assetAwareInferenceAlgorithm to set
//	 */
//	public void setAssetAwareInferenceAlgorithm(
//			AssetAwareInferenceAlgorithm assetAwareInferenceAlgorithm) {
//		this.assetAwareInferenceAlgorithm = assetAwareInferenceAlgorithm;
//		asdf
//	}
}
