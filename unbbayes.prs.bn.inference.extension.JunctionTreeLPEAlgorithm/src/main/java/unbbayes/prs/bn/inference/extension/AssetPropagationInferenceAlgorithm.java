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
	 * This property is used in {@link #getAssetNetwork()}, {@link AssetNetwork#getProperty(String)} in order 
	 * to store the default q-value of empty separators. Default q-values of empty separators
	 * are used when joint q-values are calculated (e.g.{@link #calculateExplanation(List)}). 
	 */
	public static final String EMPTY_SEPARATOR_DEFAULT_QVALUE_PROPERTY = "EMPTY_SEPARATOR_DEFAULT_QVALUE_PROPERTY";
	
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
	

	private boolean isToPropagateForGlobalConsistency = false;

	private boolean isToUpdateSeparators = true;
	
	private boolean isToUpdateOnlyEditClique = false;
	
	private boolean isToAllowQValuesSmallerThan1 = true;
	
	

//	private Map<IRandomVariable, IRandomVariable> originalCliqueToAssetCliqueMap;
	
	private Comparator<IRandomVariable> randomVariableComparator = new Comparator<IRandomVariable>() {
		public int compare(IRandomVariable v1, IRandomVariable v2) {
			int nameComp = v1.toString().compareTo(v2.toString());
			if (nameComp == 0 && (v1 instanceof Clique) && (v2 instanceof Clique)) {
				// do special treatment on cliques with same variables
				return ((Clique)v1).getIndex() - ((Clique)v2).getIndex();
			}
			return nameComp;
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

	private Map<IRandomVariable, PotentialTable> assetTablesBeforeLastPropagation;

	private boolean isToCalculateMarginalsOfAssetNodes = false;

	/** Initialize with default implementation */
	private IMinQCalculator globalQValueCalculator = new IMinQCalculator() {
		
		/** 
		 * Returns Product(Cliques)/Product(Separators). It assumes assetNet was compiled (i.e. has {@link AssetNetwork#getJunctionTree()}).
		 * This method uses {@link AssetPropagationInferenceAlgorithm#getEmptySeparatorsQValue()}
		 * if an empty separator  is found.
		 */
		public float getGlobalQ(AssetNetwork assetNet, Map<INode, Integer> filter) {
			
			// initial assertion
			if (assetNet == null) {
				return 0f;
			}
			
			double ret = 1;	// var to be returned
			
			// calculate Product(Cliques)
			for (Clique clique : assetNet.getJunctionTree().getCliques()) {
				// extract clique table
				PotentialTable cliqueTable = clique.getProbabilityFunction();
				// iterate on each cell of clique table
				for (int i = 0; i < cliqueTable.tableSize(); i++) {
					float value = cliqueTable.getValue(i);	// value of cell in clique table
					// filter by states
					if (filter != null) {
						// isNotMatching == true if the variables&states related to current cell in table is incompatible with filter
						boolean isNotMatching = false;	
						// what combination of states the index i represents in cliqueTable
						int[] combinationOfStates = cliqueTable.getMultidimensionalCoord(i); 
						// look for node&state in filter which does not match with content of combinationOfStates
						for (INode node : filter.keySet()) {
							if ( clique.getNodes().contains(node) && combinationOfStates[cliqueTable.getVariableIndex((Node)node)] != filter.get(node)) {
								// current clique contain node in filter, and the state in filter does not match state of combinationOfStates
								isNotMatching = true;
								break;
							}
						}
						if (isNotMatching) {
							continue;	// do not multiply if current cell in table does not correspond with filter
						}
					}
					// note: at this point, this cell in cliqueTable matches filter
					if (Float.compare(value , 0.0f) == 0) {
						// once zero, it will always be zero.
						return 0;
					}
					// Product(Cliques)
					ret *= value;
				}
			}
			// calculate Product(Separators)
			for (Separator separator : assetNet.getJunctionTree().getSeparators()) {
				// extract the separator table from separator
				PotentialTable sepTable = separator.getProbabilityFunction();
				if (separator.getNodes() == null || separator.getNodes().isEmpty()
						|| sepTable.getVariablesSize() <= 0 || sepTable.tableSize() <= 0) {
					// this is an empty separator (i.e. network is disconnected). Use a default value when empty separator is found
					ret /= getEmptySeparatorsQValue();
				} else {	// separator is not empty
					// iterate on each cell of separator table
					for (int i = 0; i < sepTable.tableSize(); i++) {
						float value = sepTable.getValue(i);	// value of cell in clique table
						// filter by states
						if (filter != null) {
							// isNotMatching == true if the variables&states related to current cell in table is incompatible with filter
							boolean isNotMatching = false;	
							// what combination of states the index i represents in cliqueTable
							int[] combinationOfStates = sepTable.getMultidimensionalCoord(i); 
							// look for node&state in filter which does not match with content of combinationOfStates
							for (INode node : filter.keySet()) {
								if ( separator.getNodes().contains(node) && combinationOfStates[sepTable.getVariableIndex((Node)node)] != filter.get(node)) {
									// current clique contain node in filter, and the state in filter does not match state of combinationOfStates
									isNotMatching = true;
									break;
								}
							}
							if (isNotMatching) {
								continue;	// do not multiply if current cell in table does not correspond with filter
							}
						}
						// note: at this point, this cell in sepTable matches filter
						if (Float.compare(value, 0.0f) == 0) {
							// once divided by zero, it will be always undefined.
							return Float.NaN;
						}
						// Product(Cliques)/Product(Separators)
						ret /= value;
					}	// end of for i < sepTable.tableSize()
				}	// end of else (separator is not empty)
			} // end of foreach separator
			
			return (float) ret;
		}
	};

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
	 * This interface contain operation for calculating global q values
	 * given set of nodes and states.
	 */
	public interface IMinQCalculator {
		/**
		 * Calculates the global q value given set of states.
		 * @param assetNet : network containing nodes and states
		 * @param filter : mapping from node to respective state.
		 * @return global q value
		 */
		float getGlobalQ(AssetNetwork assetNet , Map<INode, Integer> filter);
	}
	
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
	public synchronized void propagate() {
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
		
		// reset the mapping which stores the old q-values. This map can also be used in order to revert changes when q-values goes below 1
		setAssetTablesBeforeLastPropagation(new HashMap<IRandomVariable, PotentialTable>());
		
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
			getAssetTablesBeforeLastPropagation().put(assetCliqueOrSeparator, (PotentialTable) assetTable.clone());
			
			// perform clique-wise update of asset values using a ratio
			for (int i = 0; i < assetTable.tableSize(); i++) {
				float previousProbability = previousProbabilities.getValue(i);
				if (previousProbability <= 0) {
					// This is an impossible state, because some complementary state is set as a finding.
					// Impossible supposedly cannot be changed anymore, so do not update this state.
					if (currentProbabilities.getValue(i) > 0) {
						throw new RuntimeException("Invalid attempt to change probability of clique/separator " 
								+ assetTable
								+ ", coordinate " + assetTable.getMultidimensionalCoord(i)
								+ ", from " + previousProbability
								+ " to " + currentProbabilities.getValue(i)
								+ " was found.");
					}
					continue;
				}
				// multiply assets by the ratio (current probability values / previous probability values)
				float newValue = assetTable.getValue(i) * ( currentProbabilities.getValue(i) / previousProbability );
				// check if assets is <= 1
				if (!isToAllowQValuesSmallerThan1() &&  (newValue <= 1f) && (newValue > 0f) ) {
					// revert all previous cliques/separators, including current
					for (IRandomVariable modifiedCliqueOrSep : getAssetTablesBeforeLastPropagation().keySet()) {
						assetTable = (PotentialTable) modifiedCliqueOrSep.getProbabilityFunction();
						// CAUTION: the following code only works because the vector of old and new values have the same size
						assetTable.setValues(getAssetTablesBeforeLastPropagation().get(modifiedCliqueOrSep).getValues());
						// update marginal of asset nodes
						for (int j = 0; j < assetTable.getVariablesSize(); j++) {
							((TreeVariable)assetTable.getVariableAt(j)).updateMarginal();
						}
					}
					// do everything a normal propagation algorithm would do if it have had run properly
					if (isToPropagateForGlobalConsistency()) {
						this.runMinPropagation();
					}
					for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
						listener.onAfterPropagate(this);
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
			// also, use the copied potential table to store old values, so that we can use restoreData to undo changes.
			((PotentialTable) getEditCliqueOrSeparator().getProbabilityFunction()).copyData();
			return;
		}
		
		// iterate on cliques
		for (Clique clique : this.getRelatedProbabilisticNetwork().getJunctionTree().getCliques()) {
			// fill property with clones, so that changes on the original tables won't affect the property
			PotentialTable table = (PotentialTable) ((PotentialTable) clique.getProbabilityFunction()).clone();
			property.put(clique, table); // update property
			// also, use the copied potential table to store old values, so that we can use restoreData to undo changes.
			((PotentialTable) clique.getProbabilityFunction()).copyData();
		}
		
		// iterate on separators
		for (Separator separator : this.getRelatedProbabilisticNetwork().getJunctionTree().getSeparators()) {
			// fill property with clones, so that changes on the original tables won't affect the property
			PotentialTable table = (PotentialTable) ((PotentialTable) separator.getProbabilityFunction()).clone();
			property.put(separator, table); // update property
			// also, use the copied potential table to store old values, so that we can use restoreData to undo changes.
			((PotentialTable) separator.getProbabilityFunction()).copyData();
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
		ret.setToCalculateMarginalsOfAssetNodes(isToCalculateMarginalsOfAssetNodes());
		
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
				// use min-out as default operation to be applied when removing a variable or when doing marginalization
				assetPotential.setSumOperation(new MinProductJunctionTree().new MinOperation());
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
		for (Clique clique : this.getRelatedProbabilisticNetwork().getJunctionTree().getCliques()) {
			try {
				explMessage += clique + ":\n \t \t";
				PotentialTable assetTable = (PotentialTable) this.getOriginalCliqueToAssetCliqueMap().get(clique).getProbabilityFunction();
				PotentialTable probTable = clique.getProbabilityFunction();
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
		for (Separator separator : this.getRelatedProbabilisticNetwork().getJunctionTree().getSeparators()) {
			try {
				explMessage += separator + ":\n \t \t";
				PotentialTable assetTable = (PotentialTable) this.getOriginalCliqueToAssetCliqueMap().get(separator).getProbabilityFunction();
				PotentialTable probTable = separator.getProbabilityFunction();
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
	 * We are also assuming that the variables in {@link Clique#getNodes()} are equals to
	 * the variables related to {@link Clique#getProbabilityFunction()} (see {@link PotentialTable#getVariableAt(int)}),
	 * no matter the ordering.
	 * @param inputOutpuArgumentForExplanation : an input/output parameter. However, current implementation
	 * assumes this is an empty collection (hence, this is only used as output argument).
	 * Currently, this list will be filled with only 1 LPE.
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#calculateExplanation(List)
	 * @see IExplanationJunctionTree#calculateExplanation(Graph, IInferenceAlgorithm)
	 * TODO allow multiple combinations of states.
	 * @see #getGlobalQValueCalculator()
	 */
	public float calculateExplanation( List<Map<INode, Integer>> inputOutpuArgumentForExplanation){
		
		// TODO return more than 1 LPE
		Debug.println(getClass(), "Current version returns only 1 explanation");
		
		// this map will contain least probable state for each node
		Map<INode, Integer> nodeToLPEMap = new HashMap<INode, Integer>();
		
		// look for min in cliques
		Set<Clique> rootCliques = new HashSet<Clique>();	// for debug
		for (Clique clique : getAssetNetwork().getJunctionTree().getCliques()) {
			// for debug
			if (clique.getParent() == null
					|| getAssetNetwork().getJunctionTree().getSeparator(clique, clique.getParent()) == null
					|| getAssetNetwork().getJunctionTree().getSeparator(clique, clique.getParent()).getNodes().isEmpty()) {
				rootCliques.add(clique);
			}
			PotentialTable cliqueTable = clique.getProbabilityFunction();
			Set<Integer> indexesOfMin = new HashSet<Integer>();
			double minValue = Double.MAX_VALUE;
			for (int i = 0; i < cliqueTable.tableSize(); i++) {
				float value = cliqueTable.getValue(i);
				// this.getCellValuesComparator() can personalize comparison, like: ignore zero
				if (this.getCellValuesComparator().compare(value, (float) minValue) <= 0) {
					if (this.getCellValuesComparator().compare(value, (float) minValue) < 0) {
						// value is smaller, so old values in indexesOfMin are useless
						indexesOfMin.clear();
					}
					minValue = value;
					indexesOfMin.add(i);
				}
			}
			// extract combination of states related to the indexOfMin in cliqueTable
			for (Integer indexOfMin : indexesOfMin) {
				// extract combination of states of this local LPE
				int[] states = cliqueTable.getMultidimensionalCoord(indexOfMin);
				
				// must only consider LPEs compatible with previously considered local LPEs
				if (!nodeToLPEMap.isEmpty()) {
					boolean isCompatible = true;
					// for each node, check that both indexOfMin and nodeToLPEMap points to same states
					for (INode node : nodeToLPEMap.keySet()) {
						// check whether current node is present in current table 
						// and some nodes related to the cell of the table points to states other than the ones in nodeToLPEMap
						int indexOfVariable = cliqueTable.indexOfVariable((Node) node);
						if (indexOfVariable >= 0										// node exist in cliqueTable
								&& states[indexOfVariable] != nodeToLPEMap.get(node)) {	// they are pointing to different states
							isCompatible = false;
							break;
						}
					}
					if (!isCompatible) {
						continue; // try next indexOfMin
					} 
				}
				// at this point, either this is the first clique or indexOfMin is compatible with states already in nodeToLPEMap
				
				// NOTE: we assume the nodes in cliques == nodes in cliqueTable
				for (Node node : clique.getNodes()) {
					// extract LPE state of currently evaluated node
					Integer state = states[cliqueTable.getVariableIndex(node)];
					// put to nodeToLPEMap
					Integer oldState = nodeToLPEMap.put(node, state);
					if (oldState != null && oldState != state) {
						throw new IllegalStateException(clique + " contains duplicate nodes or inconsistent assets. Please, run runMinPropagation() before this method.");
					} 
				}
				
				// use only the 1st LPE. TODO allow multiple LPEs
				break;
			}
		}
		// TODO for consistency, look for min in separators as well
		
		// TODO allow multiple LPE
		if (inputOutpuArgumentForExplanation != null) {
			inputOutpuArgumentForExplanation.add(nodeToLPEMap);
		}
		
		// calculate global Q value
		float ret = this.getGlobalQValueCalculator().getGlobalQ(getAssetNetwork(), nodeToLPEMap);
		
		// this is just for debugging
		float minValueOfRootCliques = 0.0f;
		for (Clique root : rootCliques) {
			PotentialTable table = root.getProbabilityFunction();
			for (int i = 0; i < table.tableSize(); i++) {
				if (this.getCellValuesComparator().compare(table.getValue(i) , minValueOfRootCliques) < 0) {
					minValueOfRootCliques = table.getValue(i);
				}
			}
		}
		// this is also for debugging
		if (ret - 0.0001 >= minValueOfRootCliques || minValueOfRootCliques >= ret + 0.0001) {
			if (ret != 0.0f && this.getCellValuesComparator().compare(ret , minValueOfRootCliques) > 0) {
//				System.err.print("[WARN]" + this.getClass().getName() + " Global min q = " + ret + ", root clique's min q = " + minValueOfRootCliques);
//				System.err.println(". This discrepancy may happen in disconnected networks/cliques : " + rootCliques);
//				System.err.println("[WARN]" + this.getClass().getName() + " Using min q = " + minValueOfRootCliques + " due to the discrepancy.");
//				ret = minValueOfRootCliques;
			}
		}
		return ret;
	}
	

//	/** 
//	 * This method obtains the min-q states and returns the min-q value.
//	 * Currently, it assumes inputOutpuArgumentForExplanation is an empty collection.
//	 * It is assumed that {@link #runMinPropagation()} was called prior to this method.
//	 * This method will return the minimum local q value if global consistency does not hold
//	 * (i.e. if the min-q value differs between different cliques, it will return the smaller one).
//	 * @param inputOutpuArgumentForExplanation : an input/output parameter. However, current implementation
//	 * assumes this is an empty collection (hence, this is only used as output argument).
//	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#calculateExplanation(List)
//	 * @see IExplanationJunctionTree#calculateExplanation(Graph, IInferenceAlgorithm)
//	 * TODO allow multiple combinations of states.
//	 */
//	public float calculateExplanation( List<Map<INode, Integer>> inputOutpuArgumentForExplanation){
//		
//		// this will hold min-q value
////		float ret = Float.POSITIVE_INFINITY;
//
//		// TODO return more than 1 LPE
//		Debug.println(getClass(), "Current version returns only 1 explanation");
//		
//		// this map will contain one combination of min-q state
//		Map<INode, Integer> stateMap = new HashMap<INode, Integer>();
//		
//		// this map contains the min-q value obtained when stateMap was updated (hence, this is synchronized with stateMap). 
//		// this map is only needed if this method must return something even when global consistency does not hold 
//		// (in order to compare min-q vales between different cliques)
//		Map<INode, Float> valueMap = new HashMap<INode, Float>();
//		
//		for (Clique clique : this.getAssetNetwork().getJunctionTree().getCliques()) {
//			PotentialTable table = clique.getProbabilityFunction();
//			if (table.tableSize() <= 0) {
//				throw new IllegalArgumentException(clique + "- table size: " + table.tableSize());
//			}
//			// find index of the min value in clique
//			int indexOfMinInClique = 0;
//			float valueOfMinInClique = table.getValue(indexOfMinInClique);
//			for (int i = 1; i < table.tableSize(); i++) {
//					indexOfMinInClique = i;
//					valueOfMinInClique = table.getValue(i);
//				}
//			}
//			if (inputOutpuArgumentForExplanation == null) {
//				// we do not need to obtain explanation. So only calculate min value
//				if (valueOfMinInClique < ret) { // Note: if global consistency holds, this check is unnecessary, but in disconnected nets the global consistency does not hold
//					ret = valueOfMinInClique;	
//				}
//				continue;
//			}
//			
//			// the indexes of the states can be obtained from the index of the linearized table by doing the following operation:
//			// indexOfLPEOfNthNode = mod(indexOfMinInClique / prodNumberOfStatesPrevNodes, numberOfStates). 
//			// prodNumberOfStatesPrevNodes is the product of the number of states for all previous nodes. If this is the first node, then prodNumberOfStatesPrevNodes = 1.
//			// e.g. suppose there are 2 nodes A(w/ 4 states), B(w/ 3 states) and C (w/ 2 states). If the maximum probability occurs at index 5, then
//			// the state of A is mod(5/1 , 4) = 1, and the state of B is mod(5/4, 3) = 1, and state of C is mod(5/(4*3),2) = 0.
//			// I.e.
//			//      |                             c0                            |                             c1                            |
//			//      |         b0        |         b1        |         b2        |         b0        |         b1        |         b2        |
//			//      | a0 | a1 | a2 | a3 | a0 | a1 | a2 | a3 | a0 | a1 | a2 | a3 | a0 | a1 | a2 | a3 | a0 | a1 | a2 | a3 | a0 | a1 | a2 | a3 |
//			//index:| 0  |  1 |  2 |  3 | 4  | 5  | 6  | 7  |  8 | 9  | 10 | 11 | 12 | 13 | 14 | 15 | 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 |
//			int prodNumberOfStatesPrevNodes = 1;
//			for (int i = 0; i < table.getVariablesSize(); i++) {
//				INode node = table.getVariableAt(i);
//				int numberOfStates = node.getStatesSize();
//				// number of states must be strictly positive
//				if (numberOfStates <= 0) {
//					try {
//						Debug.println(getClass(), "[Warning] Size of " + table.getVariableAt(i) + " is " + numberOfStates);
//					} catch (Throwable t) {
//						t.printStackTrace();
//					}
//					continue;
//				}
//				// calculate most probable state
//				int indexOfLPEOfNthNode = (indexOfMinInClique / prodNumberOfStatesPrevNodes) % numberOfStates;
//				// add to states if it was not already added
//				if (!stateMap.containsKey(node)) {
//					// this is the first time we add this entry. Add it
//					stateMap.put(node, indexOfLPEOfNthNode);
//					valueMap.put(node, valueOfMinInClique);
////					if (valueOfMinInClique < ret) { // Note: if global consistency holds, this check is unnecessary, but in disconnected nets the global consistency does not hold
////						ret = valueOfMinInClique;	
////					}
//				} else {
//					// check consistency (min state should be unique between cliques)
//					if (!stateMap.get(node).equals(indexOfLPEOfNthNode)) {
//						if (this.getCellValuesComparator().compare(valueMap.get(node), valueOfMinInClique) < 0) {
//							// new value is greater. Update
//							stateMap.put(node, indexOfLPEOfNthNode);
//							valueMap.put(node, valueOfMinInClique);
//							// this clique has a smaller local min-q value...
////							if (valueOfMinInClique < ret) { 
////								ret = valueOfMinInClique;	
////							}
//						}
//						throw new IllegalStateException("Obtained states differ between cliques (clique inconsistency)... The current clique is: " 
//									+ clique + "; node is " + node + "; previous state: " + stateMap.get(node) + "; index of state found in current clique: " + indexOfLPEOfNthNode);
////						try {
////							Debug.println(getClass(), "Obtained states differ between cliques (clique inconsistency)... The current clique is: " 
////									+ clique + "; node is " + node + "; previous state: " + stateMap.get(node) + "; index of state found in current clique: " + indexOfLPEOfNthNode);
////						} catch (Throwable t) {
////							t.printStackTrace();
////						}
//					}
//				}
//				prodNumberOfStatesPrevNodes *= numberOfStates;
//			}
//		}
//		
//		// handle the input/output argument
//		if (inputOutpuArgumentForExplanation != null && !inputOutpuArgumentForExplanation.contains(stateMap)) {
//			inputOutpuArgumentForExplanation.add(stateMap);
//		}
////		return ret;
//		return this.getGlobalQValueCalculator().getGlobalQ(getAssetNetwork(), stateMap);
//	}

	/**
	 * Just executes {@link JunctionTreeLPEAlgorithm#propagate()}.
	 * It uses {@link PotentialTable#copyData()} for all cliques and separator's tables,
	 * so that {@link #undoMinPropagation()} can call {@link PotentialTable#restoreData()}
	 * in order to revert any changes.
	 * This will run min propagation even when {@link #isToPropagateForGlobalConsistency()} == false.
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#runMinPropagation()
	 */
	public void runMinPropagation() {
		// do not run min propagation if network was not "compiled" (i.e. junction tree was not properly initialized)
		if (this.getAssetNetwork() == null
				|| this.getAssetNetwork().getJunctionTree() == null
				|| this.getAssetNetwork().getJunctionTree().getCliques() == null
				|| this.getAssetNetwork().getJunctionTree().getSeparators() == null) {
			throw new IllegalStateException("Method \"runMinPropagation\" was invoked before compilation of junction tree of the network " + getAssetNetwork());
		}
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
	public Comparator<Float> getCellValuesComparator() {
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

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#revertLastProbabilityUpdate()
	 */
	public void revertLastProbabilityUpdate() {
		for (IRandomVariable origCliqueOrSeparator : getOriginalCliqueToAssetCliqueMap().keySet()) {
			// extract previous probability (i.e. prior to propagation) values from network property
			PotentialTable previousProbabilities = ((Map<IRandomVariable, PotentialTable>) this.getNetwork().getProperty(LAST_PROBABILITY_PROPERTY)).get(origCliqueOrSeparator);
			// extract current probability
			PotentialTable currentProbabilities = (PotentialTable) origCliqueOrSeparator.getProbabilityFunction();
			// set current to the previous (i.e. revert prob)
			currentProbabilities.setValues(previousProbabilities.getValues());
			for (int i = 0; i < currentProbabilities.variableCount(); i++) {
				((TreeVariable)currentProbabilities.getVariableAt(i)).updateMarginal();
			}
		}
	}

	/**
	 * This method will set cells in the asset tables of {@link #getAssetNetwork()} to zero.
	 * Zero is an identity value in marginalization (sum-out - {@link unbbayes.prs.bn.PotentialTable.SumOperation}) 
	 * (i.e. SUM(X1,X2,...,Xn,0) = SUM(X1,X2,...,Xn) ), 
	 * and it is also a value ignored in current implementations of 
	 * least probable explanation (min-out - {@link unbbayes.prs.bn.inference.extension.MinProductJunctionTree.MinOperation}) 
	 * and most probable explanation (max-out - {@link unbbayes.prs.bn.PotentialTable.MaxOperation}) operations.
	 * Hence, by setting cells to 0, the cell will be ignored in all 3 types of currently implemented 
	 * algorithms based on junction tree (i.e. {@link unbbayes.prs.bn.JunctionTreeAlgorithm},
	 * {@link unbbayes.prs.bn.inference.extension.JunctionTreeLPEAlgorithm}, {@link unbbayes.prs.bn.inference.extension.JunctionTreeMPEAlgorithm}).
	 * This method will also attempt to delete the node from the network.
	 * @param node : only assets related to states of this node will be changed. 
	 * {@link AssetNetwork#removeNode(Node)} will be used in order to delete this node from the {@link #getAssetNetwork()}.
	 * @param state : q-values of states complementary to this state (i.e. states of "node" which are different
	 * to "state") will be set to 0.
	 * @param isToDeleteNode : if true, node will be deleted after propagation.
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#setAsPermanentEvidence(unbbayes.prs.INode, int)
	 */
	public void setAsPermanentEvidence(INode node, int state , boolean isToDeleteNode) {
		// initial assertions
		if (node == null) {
			throw new NullPointerException("Cannot add evidences to a null node.");
		}
		if (! (node instanceof Node)) {
			throw new UnsupportedOperationException(node + " is a node of type " + node.getClass() + ", which is not supported by the current version of " + this.getClass());
		}
		if (state < 0 || state >= node.getStatesSize()) {
			throw new ArrayIndexOutOfBoundsException(state + " is not a valid index for a state of node " + node);
		}
		if (getAssetNetwork() == null) {
			throw new IllegalStateException("This algorithm should be related to some AssetNetwork before calling this method. Please, call #setAssetNetwork(AssetNetwork).");
		}
		if (getAssetNetwork().getJunctionTree() == null 
				|| getAssetNetwork().getJunctionTree().getCliques() == null 
				|| getAssetNetwork().getJunctionTree().getSeparators() == null ) {
			throw new IllegalStateException("This algorithm cannot be used without a Junction Tree. Make sure getAssetNetwork().getJunctionTree() was correctly initialized.");
		}
		
		// Set cells of clique tables to zero. 
		for (Clique clique : getAssetNetwork().getJunctionTree().getCliques()) {
			// extract clique table
			PotentialTable cliqueTable = clique.getProbabilityFunction();
			// consider only clique tables containing node
			// clique.getNodes() may be different from the variables in cliqueTable. 
			// We'd like to prioritize vars in the tables rather than in clique.getNodes()
			if (cliqueTable.getVariableIndex((Node)node) >= 0) {
				// iterate over cells in the clique table
				for (int i = 0; i < cliqueTable.tableSize(); i++) {
					// using "multidimensionalCoord" is easier than "i" if our objective is to compare with "state"
					int[] multidimensionalCoord = cliqueTable.getMultidimensionalCoord(i);
					// set all cells unrelated to "state" to 0
					if (multidimensionalCoord[cliqueTable.getVariableIndex((Node) node)] != state) {
						cliqueTable.setValue(i,0);
					}
				}
			}
		}
		// Set cells of separator tables to zero as well.
		for (Separator separator : getAssetNetwork().getJunctionTree().getSeparators()) {
			// extract separator table
			PotentialTable separatorTable = separator.getProbabilityFunction();
			// consider only separator tables containing node
			// separator.getNodes() may be different from the variables in separatorTable. 
			// We'd like to prioritize vars in the tables rather than in separator.getNodes()
			if (separatorTable.getVariableIndex((Node)node) >= 0) {
				// iterate over cells in the separator table
				for (int i = 0; i < separatorTable.tableSize(); i++) {
					// using "multidimensionalCoord" is easier than "i" if our objective is to compare with "state"
					int[] multidimensionalCoord = separatorTable.getMultidimensionalCoord(i);
					// set all cells unrelated to "state" to 0
					if (multidimensionalCoord[separatorTable.getVariableIndex((Node) node)] != state) {
						separatorTable.setValue(i,0);
					}
				}
			}
		}
		// delete node. This will supposedly delete columns in the asset tables (cliques + separators) as well
		if (isToDeleteNode) {
			getAssetNetwork().removeNode((Node) node);
		}
	}

	/**
	 * This map stores what were the asset tables before the last call of
	 * {@link #propagate()}
	 * @param assetTablesBeforeLastPropagation the assetTablesBeforeLastPropagation to set
	 */
	public void setAssetTablesBeforeLastPropagation(
			Map<IRandomVariable, PotentialTable> assetTablesBeforeLastPropagation) {
		this.assetTablesBeforeLastPropagation = assetTablesBeforeLastPropagation;
	}

	/**
	 * @see #setAssetTablesBeforeLastPropagation(Map)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#getAssetTablesBeforeLastPropagation()
	 */
	public Map<IRandomVariable, PotentialTable> getAssetTablesBeforeLastPropagation() {
		return assetTablesBeforeLastPropagation;
	}


	/**
	 * Delegates to  {@link #getAssetNetwork()} and {@link AssetNetwork#setToCalculateMarginalsOfAssetNodes(boolean)}
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#setToCalculateMarginalsOfAssetNodes(boolean)
	 */
	public void setToCalculateMarginalsOfAssetNodes(boolean isToCalculateMarginalsOfAssetNodes) {
		this.isToCalculateMarginalsOfAssetNodes = isToCalculateMarginalsOfAssetNodes;
		if (this.getAssetNetwork() != null) {
			this.getAssetNetwork().setToCalculateMarginalsOfAssetNodes(isToCalculateMarginalsOfAssetNodes);
		}
	}

	/**
	 * @return delegates to {@link #getAssetNetwork()} and {@link AssetNetwork#isToCalculateMarginalsOfAssetNodes()}
	 */
	public boolean isToCalculateMarginalsOfAssetNodes() {
		if (this.getAssetNetwork() != null) {
			return this.getAssetNetwork().isToCalculateMarginalsOfAssetNodes();
		}
		return isToCalculateMarginalsOfAssetNodes;
	}

	/**
	 * This object is used in {@link #calculateExplanation(List)} in order
	 * to calculate the min-q value given LPE.
	 * @return the globalQValueCalculator
	 */
	public IMinQCalculator getGlobalQValueCalculator() {
		return globalQValueCalculator;
	}

	/**
	 * This object is used in {@link #calculateExplanation(List)} in order
	 * to calculate the min-q value given LPE.
	 * @param globalQValueCalculator the globalQValueCalculator to set
	 */
	public void setGlobalQValueCalculator(IMinQCalculator globalQValueCalculator) {
		this.globalQValueCalculator = globalQValueCalculator;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#getEmptySeparatorsQValue()
	 */
	public float getEmptySeparatorsQValue() {
		Float ret = (Float) getAssetNetwork().getProperty(EMPTY_SEPARATOR_DEFAULT_QVALUE_PROPERTY);
		if (ret == null) {
			ret = getDefaultInitialAssetQuantity();
			setEmptySeparatorsQValue(ret);
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#setEmptySeparatorsQValue(float)
	 */
	public void setEmptySeparatorsQValue(float emptySeparatorsQValue) {
		getAssetNetwork().getProperties().put(EMPTY_SEPARATOR_DEFAULT_QVALUE_PROPERTY, emptySeparatorsQValue);
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
