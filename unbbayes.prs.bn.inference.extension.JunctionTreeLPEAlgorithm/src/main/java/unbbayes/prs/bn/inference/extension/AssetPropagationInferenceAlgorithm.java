/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import unbbayes.controller.INetworkMediator;
import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.AssetNode;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.JunctionTree;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.Separator;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener;

/**
 * @author Shou Matsumoto
 *
 */
public class AssetPropagationInferenceAlgorithm implements IAssetNetAlgorithm {
	
	/** 
	 * Name of the property in {@link Graph#getProperty(String)} which manages assets prior to {@link #propagate()}. 
	 * The content is a Map<IRandomVariable, PotentialTable>, 
	 * which maps a node to its clique-wise probability table (probabilities for all nodes in the same clique/separator).
	 */
	public static final String LAST_PROBABILITY_PROPERTY = AssetPropagationInferenceAlgorithm.class.getName() + ".lastProbabilityMap";
	

	private List<IInferenceAlgorithmListener> inferenceAlgorithmListener = new ArrayList<IInferenceAlgorithmListener>();
	
	private AssetNetwork network;
	
	private ProbabilisticNetwork relatedProbabilisticNetwork;
	
	private float defaultInitialAssetQuantity = 1000.0f;
	
	private INetworkMediator mediator;

	private Map<IRandomVariable, IRandomVariable> originalCliqueToAssetCliqueMap;
	
	private Comparator<IRandomVariable> randomVariableComparator = new Comparator<IRandomVariable>() {
		public int compare(IRandomVariable v1, IRandomVariable v2) {
			return v1.toString().compareTo(v2.toString());
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
	public static AssetPropagationInferenceAlgorithm getInstance(ProbabilisticNetwork relatedProbabilisticNetwork) throws IllegalArgumentException, InvalidParentException {
		AssetPropagationInferenceAlgorithm ret = new AssetPropagationInferenceAlgorithm();
		ret.setRelatedProbabilisticNetwork(relatedProbabilisticNetwork);
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

	/**
	 * This is the asset network. It must be an instance of {@link AssetNetwork}
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#setNetwork(unbbayes.prs.Graph)
	 */
	public void setNetwork(Graph network) throws IllegalArgumentException {
		this.network = (AssetNetwork) network;
	}

	/**
	 * This is the asset network
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getNetwork()
	 */
	public AssetNetwork getNetwork() {
		return network;
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#run()
	 */
	public void run() throws IllegalStateException {
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListener()) {
			listener.onBeforeRun(this);
		}

		
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListener()) {
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

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#reset()
	 */
	public void reset() {
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListener()) {
			listener.onBeforeReset(this);
		}
		
		// reset all clique potentials
		for (Clique clique : this.getNetwork().getJunctionTree().getCliques()) {
			clique.getProbabilityFunction().restoreData();
		}
		// reset all separator potential
		// TODO check whether this is really necessary, because looks like we are not using the separator distributions in the #propagate()
		for (int i = 0; i < this.getNetwork().getJunctionTree().getSeparatorsSize(); i++) {
			this.getNetwork().getJunctionTree().getSeparatorAt(i).getProbabilityFunction().restoreData();
		}
		
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListener()) {
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
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListener()) {
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
		
		for (Node assetNode : getNetwork().getNodes()) {
			
			// extract clique related to the asset node
			IRandomVariable assetCliqueOrSeparator = ((TreeVariable)assetNode).getAssociatedClique();
			if (assetCliqueOrSeparator == null) {
				continue;	//ignore null entry
			}
			
			// extract asset table. We assume we are using table-based representation (PotentialTable)
			PotentialTable assetTable = (PotentialTable) assetCliqueOrSeparator.getProbabilityFunction();
			
			
			// extract previous probability (i.e. prior to propagation) values from network property
			PotentialTable previousProbabilities = ((Map<IRandomVariable, PotentialTable>) this.getNetwork().getProperty(LAST_PROBABILITY_PROPERTY)).get(assetCliqueOrSeparator);
			
			// extract probabilistic node related to asset node (they have the same name)
			// Note: if it is not a probabilistic node, then it means that there is an asset node created for non-probabilistic node (this is unexpected)
			ProbabilisticNode probNode = (ProbabilisticNode) getRelatedProbabilisticNetwork().getNode(assetNode.getName());
			
			// extract current probability values from prob node's clique. We assume we are using table-based representation (PotentialTable)
			PotentialTable currentProbabilities = (PotentialTable) probNode.getAssociatedClique().getProbabilityFunction();
			
			// assertion: tables must be non-null and have same size
			if (currentProbabilities == null || previousProbabilities == null || assetTable == null
					|| ( assetTable.tableSize() != currentProbabilities.tableSize() )
					|| ( assetTable.tableSize() != previousProbabilities.tableSize() )) {
				throw new IllegalStateException("The assets and probabilities of " + assetNode 
						+ " are not synchronized. The asset is related to clique/separator " + assetCliqueOrSeparator
						+ " and the probability is related to clique/separator " + probNode.getAssociatedClique());
			}
			
			// perform clique-wise update of asset values using a ratio
			for (int i = 0; i < assetTable.tableSize(); i++) {
				// multiply assets by the ratio (current probability values / previous probability values)
				assetTable.setValue(i, assetTable.getValue(i) * ( currentProbabilities.getValue(i) / previousProbabilities.getValue(i) ) );
			}
			
			// update marginal of asset node
			((TreeVariable)assetNode).updateMarginal();
		}
		
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListener()) {
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
		
		// iterate on cliques/separators associated with some node
		for (Node node : this.getRelatedProbabilisticNetwork().getNodes()) {
			if (node instanceof TreeVariable) {
				IRandomVariable cliqueOrSeparator = ((TreeVariable)node).getAssociatedClique();
				// extract clique table from cache or get a clone from clique
				if (!property.containsKey(cliqueOrSeparator)) {
					// fill property with clones, so that changes on the original tables won't affect the property
					PotentialTable table = (PotentialTable) ((PotentialTable) cliqueOrSeparator.getProbabilityFunction()).clone();
					property.put(cliqueOrSeparator, table); // update property
				}
			}
		}
		
		// overwrite asset network property
		this.getNetwork().addProperty(LAST_PROBABILITY_PROPERTY, property);
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#addInferencceAlgorithmListener(unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener)
	 */
	public void addInferencceAlgorithmListener(
			IInferenceAlgorithmListener listener) {
		this.getInferenceAlgorithmListener().add(listener);
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#removeInferencceAlgorithmListener(unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener)
	 */
	public void removeInferencceAlgorithmListener(
			IInferenceAlgorithmListener listener) {
		this.getInferenceAlgorithmListener().remove(listener);
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

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#createAssetNetFromProbabilisticNet(unbbayes.prs.bn.ProbabilisticNetwork)
	 */
	public Graph createAssetNetFromProbabilisticNet(ProbabilisticNetwork relatedProbabilisticNetwork) throws InvalidParentException {
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
			JunctionTree jt = new JunctionTree();
			ret.setJunctionTree(jt);
			
			originalCliqueToAssetCliqueMap = new TreeMap<IRandomVariable, IRandomVariable>(this.getRandomVariableComparator());

			// copy cliques
			for (Clique origClique : relatedProbabilisticNetwork.getJunctionTree().getCliques()) {
				Clique newClique = new Clique();
				for (Node node : origClique.getAssociatedProbabilisticNodes()) {
					Node assetNode = ret.getNode(node.getName());	// extract associated node, because they are related by name
					newClique.getAssociatedProbabilisticNodes().add(assetNode);
				}
				for (Node node : origClique.getNodes()) {
					Node assetNode = ret.getNode(node.getName());	// extract associated node, because they are related by name
					newClique.getNodes().add(assetNode);
				}
				newClique.setIndex(origClique.getIndex());
				
				// copy clique potential variables
				PotentialTable origPotential = origClique.getProbabilityFunction();
				PotentialTable assetPotential = newClique.getProbabilityFunction();
				for (int i = 0; i < origPotential.getVariablesSize(); i++) {
					assetPotential.addVariable(ret.getNode(origPotential.getVariableAt(i).getName()));
				}
				
				// fills the values of assets with default values
				this.initAssetPotential(assetPotential);
				
				
				// NOTE: this is ignoring utility table and nodes
				
				jt.getCliques().add(newClique);
				originalCliqueToAssetCliqueMap.put(origClique, newClique);
				
			}
			
			// copy relationship of cliques (separators)
			for (int i = 0; i < relatedProbabilisticNetwork.getJunctionTree().getSeparatorsSize(); i++) {
				Separator origSeparator = relatedProbabilisticNetwork.getJunctionTree().getSeparatorAt(i);
				
				// extract the cliques related to the two cliques that the origSeparator connects
				Clique assetClique1 = (Clique) originalCliqueToAssetCliqueMap.get(origSeparator.getClique1());
				Clique assetClique2 = (Clique) originalCliqueToAssetCliqueMap.get(origSeparator.getClique2());
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
					newSeparator.getNodes().add(assetNode);
				}
				
				// copy separator potential variables
				PotentialTable origPotential = origSeparator.getProbabilityFunction();
				PotentialTable assetPotential = newSeparator.getProbabilityFunction();
				for (int j = 0; j < origPotential.getVariablesSize(); j++) {
					assetPotential.addVariable(ret.getNode(origPotential.getVariableAt(i).getName()));
				}
				
				// fills the values of assets with default values
				this.initAssetPotential(assetPotential);
				
				
				// NOTE: this is ignoring utility table and nodes
				jt.addSeparator(newSeparator);
				originalCliqueToAssetCliqueMap.put(origSeparator, newSeparator);
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
				assetNode.setAssociatedClique(originalCliqueToAssetCliqueMap.get(((TreeVariable)origNode).getAssociatedClique()));
				
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
	 */
	protected void initAssetPotential(PotentialTable assetCliquePotential) {
		// TODO distribute correctly so that the marginal asset gets to the default initial asset quantity
		for (int i = 0; i < assetCliquePotential.tableSize(); i++) {
			assetCliquePotential.setValue(i, this.getDefaultInitialAssetQuantity());
		}
		assetCliquePotential.copyData();
	}

	/**
	 * @return the inferenceAlgorithmListener
	 */
	public List<IInferenceAlgorithmListener> getInferenceAlgorithmListener() {
		return inferenceAlgorithmListener;
	}

	/**
	 * @param inferenceAlgorithmListener the inferenceAlgorithmListener to set
	 */
	public void setInferenceAlgorithmListener(
			List<IInferenceAlgorithmListener> inferenceAlgorithmListener) {
		this.inferenceAlgorithmListener = inferenceAlgorithmListener;
	}

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
		
		// initialize message to print
		String explMessage = "\n \n Assets ( "+ getNetwork().getName() + "): \n \n";
		
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
				e.printStackTrace();
				explMessage += " failed to obtain values: " + e.getMessage();
			}
			explMessage += "\n\n";
		}
		
		this.getRelatedProbabilisticNetwork().getLogManager().append(explMessage);
		this.getNetwork().getLogManager().append(explMessage);
	}

	/**
	 * @return the originalCliqueToAssetCliqueMap
	 */
	public Map<IRandomVariable, IRandomVariable> getOriginalCliqueToAssetCliqueMap() {
		return originalCliqueToAssetCliqueMap;
	}

	/**
	 * @param originalCliqueToAssetCliqueMap the originalCliqueToAssetCliqueMap to set
	 */
	public void setOriginalCliqueToAssetCliqueMap(
			Map<IRandomVariable, IRandomVariable> originalCliqueToAssetCliqueMap) {
		this.originalCliqueToAssetCliqueMap = originalCliqueToAssetCliqueMap;
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
		return this.getNetwork();
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
