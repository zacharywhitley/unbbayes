/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import unbbayes.controller.INetworkMediator;
import unbbayes.prs.Graph;
import unbbayes.prs.Node;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener;

/**
 * The pseudocode implemented by this algorithm is:<br/>
 * <br/> Assume {A}
 * <br/> Choose var V and value v
 * <br/> 			P(V|A)
 * <br/> Calc w = {E(S|A,V=vj); E(S|A,V!=vj)} (calculate if your A is consistent to what you bet previously - you may be accidentally assuming things that you believe that will not happen, given previous bets)
 * <br/> Calc limits for v+|A -> L+; v-|A -> L-
 * <br/> User selects P(vi) in [Li-, Li+]
 * <br/> Update P
 * <br/> Update Q
 * <br/> Update E(assets) (networth)
 * <br/> 			Person i, value j
 * @author Shou Matsumoto
 *
 */
public class AssetAwareInferenceAlgorithm implements IInferenceAlgorithm {

	private Comparator<Clique> cliqueComparator = new Comparator<Clique>() {
		public int compare(Clique c1, Clique c2) {
			return c1.toString().compareTo(c2.toString());
		}
	};
	
	private IInferenceAlgorithm delegator;
	
	
	/** 
	 * Name of the property in {@link Graph#getProperty(String)} which manages current assets. 
	 * The content is a Map<Clique, PotentialTable>, which maps a node to its clique-wise asset table (an asset table for all nodes in the same clique).
	 */
	public static final String CURRENT_ASSETS_PROPERTY = AssetAwareInferenceAlgorithm.class.getName() + ".currentAssetMap";

//	/** 
//	 * Name of the property in {@link Graph#getProperty(String)} which manages assets prior to {@link #propagate()}. 
//	 * The content is a Map<Clique, PotentialTable>, which maps a node to its clique-wise asset table (an asset table for all nodes in the same clique).
//	 */
//	public static final String LAST_ASSETS_PROPERTY = AssetAwareInferenceAlgorithm.class + ".lastAssetMap";
	

	/** 
	 * Name of the property in {@link Graph#getProperty(String)} which manages assets prior to {@link #propagate()}. 
	 * The content is a Map<Clique, PotentialTable>, which maps a node to its clique-wise probability table (probabilities for all nodes in the same clique).
	 */
	public static final String LAST_PROBABILITY_PROPERTY = AssetAwareInferenceAlgorithm.class.getName() + ".lastProbabilityMap";
	
	/** 
	 * Name of the property in {@link Graph#getProperty(String)} which manages assets during first call of {@link #run()}. 
	 * The content is a Map<Clique, PotentialTable>, which maps a node to its clique-wise asset table (an asset table for all nodes in the same clique). 
	 */
	public static final String INITIAL_ASSETS_PROPERTY = AssetAwareInferenceAlgorithm.class.getName() + ".initialAssetMap";
	
	private float defaultInitialAssetQuantity = 1000.0f;
	
	/**
	 * Default constructor is at least protected, in order to allow inheritance.
	 */
	protected AssetAwareInferenceAlgorithm() {}
	
	/**
	 * Default instantiation method.
	 */
	public static IInferenceAlgorithm getInstance(IInferenceAlgorithm delegator) {
		AssetAwareInferenceAlgorithm ret = new AssetAwareInferenceAlgorithm();
		ret.setDelegator(delegator);
		return ret;
	}
	
	
	/**
	 *  Resets the assets given information in {@link #getNetwork()} and {@link Graph#getProperty(String)}.
	 * See {@link #CURRENT_ASSETS_PROPERTY} for the name of the graph property which manages the information in {@link #getNetwork()}
	 * related to assets.
	 * This is basically the opposite of {@link #initializeAssets()} (while {@link #initializeAssets()} copies the property {@link #CURRENT_ASSETS_PROPERTY}
	 * to {@link #INITIAL_ASSETS_PROPERTY}, this method copies the {@link #INITIAL_ASSETS_PROPERTY} to {@link #CURRENT_ASSETS_PROPERTY}).
	 */
	public void resetAssets() {
		// assertion
		if (this.getNetwork() == null) {
			throw new NullPointerException(this.getClass() + "#resetAssets() was called without setting the network to a non-null value." );
		}
		// overwrite current assets with initial assets
		this.getNetwork().addProperty(CURRENT_ASSETS_PROPERTY, this.getNetwork().getProperty(INITIAL_ASSETS_PROPERTY));
		
		// overwrite initial assets with a copy, so that changes in current assets will not change initial assets
		this.initializeAssets();
	}
	
	/**
	 * This method creates a copy of {@link Graph#getProperty(String)}, property name {@link #CURRENT_ASSETS_PROPERTY}
	 * in {@link Graph#addProperty(String, Object)}, property name {@link #INITIAL_ASSETS_PROPERTY}.
	 * It only initializes the assets if {@link #INITIAL_ASSETS_PROPERTY} is not already set in {@link Graph#getProperty(String)},
	 * so it is possible to initialize the {@link #INITIAL_ASSETS_PROPERTY} with any value if you wish.
	 */
	public void initializeAssets() {
		// assertion
		if (this.getNetwork() == null) {
			throw new NullPointerException(this.getClass() + "#initializeAssets() was called without setting the network to a non-null value." );
		}
		Object property = this.getNetwork().getProperty(CURRENT_ASSETS_PROPERTY);
		// initialize current assets if it was not created yet
		if (property == null
				|| ( (property instanceof Map) && ((Map)property).isEmpty() ) ) {
			// init property
			property = new TreeMap<Clique, PotentialTable>(this.getCliqueComparator());
			
			// we only have access to cliques from nodes
			for (Node node : this.getNetwork().getNodes()) {
				if (node != null 
						&& (node instanceof TreeVariable)) {
					// extract clique, so that we can extract clique table
					IRandomVariable rv = ((TreeVariable)node).getAssociatedClique();
					if (rv != null && (rv instanceof Clique)) {
						Clique clique = (Clique) rv;
						if (!((Map)property).containsKey(clique)) {
							// this is the first time we saw this clique. Create a copy and fill with default asset quantity
							PotentialTable assetTable = (PotentialTable) clique.getProbabilityFunction().clone();
							for (int i = 0; i < assetTable.tableSize(); i++) {
								assetTable.setValue(i, this.getDefaultInitialAssetQuantity());
							}
							// update property
							((Map)property).put(clique, assetTable);
						}
					}
				}
			}
			// overwrite property (i.e. current assets) with new values
			if (!((Map)property).isEmpty()) {
				this.getNetwork().addProperty(CURRENT_ASSETS_PROPERTY, property);
			}
		}
		// assertion
		if (property == null) {
			throw new IllegalStateException("The network property \"" + CURRENT_ASSETS_PROPERTY + "\" of " + this.getNetwork() + " should not be null at this moment.");
		}
		// copy content of current assets to initial assets
		if (property instanceof Map) {
			Map<Clique, PotentialTable> currentAssets = (Map<Clique, PotentialTable>)property;
			Map<Clique, PotentialTable> initialAssets = new TreeMap<Clique, PotentialTable>(this.getCliqueComparator());
			
			// fill initial assets with the content of current assets. Use different objects, so that changes on the current assets will not change the initial assets
			for (Clique key : currentAssets.keySet()) {
				initialAssets.put(key,(PotentialTable) currentAssets.get(key).clone());
			}
			
			// overwrite initial asset property with new value
			this.getNetwork().addProperty(INITIAL_ASSETS_PROPERTY, initialAssets);
		}
	}
	
	/**
	 * Updates the assets given information in {@link #getNetwork()} and {@link Graph#getProperty(String)}.
	 * See {@link #CURRENT_ASSETS_PROPERTY} for the name of the graph property which manages the information in {@link #getNetwork()}
	 * related to assets.
	 * This method uses the property {@link #LAST_PROBABILITY_PROPERTY} of {@link #getNetwork()} and {@link Graph#getProperty(String)}
	 * in order to calculate the ratio of change in probability compared to the previous values.
	 */
	public void updateAssets() {
		// assertion
		if (this.getNetwork() == null) {
			throw new NullPointerException(this.getClass() + "#updateAssets() was called without setting the network to a non-null value." );
		}
		
		// the map storing probabilities prior to propagation
		Map<Clique, PotentialTable> lastProbabilityMap = (Map<Clique, PotentialTable>) this.getNetwork().getProperty(LAST_PROBABILITY_PROPERTY);
		
		// the assets to be updated
		Map<Clique, PotentialTable> currentAssetsMap = (Map<Clique, PotentialTable>) this.getNetwork().getProperty(CURRENT_ASSETS_PROPERTY);
		
		// the maps can be synchronized by the keys.
		for (Clique clique : currentAssetsMap.keySet()) {
			if (clique == null) {
				continue;	//ignore null entry
			}
			// extract current and previous probability values
			PotentialTable currentProbabilities = clique.getProbabilityFunction();
			PotentialTable previousProbabilities = lastProbabilityMap.get(clique);
			
			// extract asset table of this clique
			PotentialTable assetTable = currentAssetsMap.get(clique);
			
			// assertion: tables must be non-null and have same size
			if (currentProbabilities == null || previousProbabilities == null || assetTable == null
					|| ( assetTable.tableSize() != currentProbabilities.tableSize() )
					|| ( assetTable.tableSize() != previousProbabilities.tableSize() )) {
				throw new IllegalStateException("The contents of properties \n\"" + LAST_PROBABILITY_PROPERTY + "\" (which is a potential table) and \n\"" 
						+ CURRENT_ASSETS_PROPERTY + "\" (which is an asset table) in " + this.getNetwork() + " are not synchronized by the clique " + clique);
			}
			
			// perform clique-wise update of asset values using a ratio
			for (int i = 0; i < assetTable.tableSize(); i++) {
				// multiply assets by the ratio (current probability values / previous probability values)
				assetTable.setValue(i, assetTable.getValue(i) * ( currentProbabilities.getValue(i) / previousProbabilities.getValue(i) ) );
			}
		}
	}
	
	/**
	 * Updates the property {@link #LAST_PROBABILITY_PROPERTY} of {@link #getNetwork()} using {@link Graph#getProperty(String)}.
	 * This method basically backs up the clique table (of probabilities) for each clique related to each node.
	 * These values are going to be used by {@link #updateAssets()} to calculate the ratio of the change of probability
	 * caused by {@link #propagate()} (i.e. how much the probabilities were changed compared to the values prior to the propagation).
	 */
	public void updateLastProbabilityPropery() {
		// assertion
		if (this.getNetwork() == null) {
			throw new NullPointerException(this.getClass() + "#updateLastProbabilityPropery() was called without setting the network to a non-null value." );
		}
		// property value to set
		Map<Clique, PotentialTable> property = new TreeMap<Clique, PotentialTable>(this.getCliqueComparator());
		
		// we only have access to clique from nodes
		for (Node node : this.getNetwork().getNodes()) {
			if (node == null) {
				// ignore null values
				continue;
			}
			if (node instanceof TreeVariable) {
				TreeVariable treeVariable = (TreeVariable) node;
				IRandomVariable rv = treeVariable.getAssociatedClique();
				if (rv != null && (rv instanceof Clique)) {
					Clique clique = (Clique) rv;
					// extract clique table from cache or get a clone from clique
					if (!property.containsKey(clique)) {
						// fill property with clones, so that changes on the original tables won't affect the property
						PotentialTable table = (PotentialTable) clique.getProbabilityFunction().clone();
						property.put(clique, table); // update property
					}
				}
			}
		}
		
		// overwrite network property
		this.getNetwork().addProperty(LAST_PROBABILITY_PROPERTY, property);
	}


	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#setNetwork(unbbayes.prs.Graph)
	 */
	public void setNetwork(Graph g) throws IllegalArgumentException {
		this.getDelegator().setNetwork(g);
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getNetwork()
	 */
	public Graph getNetwork() {
		return this.getDelegator().getNetwork();
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#run()
	 */
	public void run() throws IllegalStateException {
		this.getDelegator().run();
		this.initializeAssets();
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getName()
	 */
	public String getName() {
		return this.getDelegator().getName() + " + assets";
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getDescription()
	 */
	public String getDescription() {
		return "[Adaptation to handle assets]" + this.getDelegator().getDescription();
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#reset()
	 */
	public void reset() {
		this.getDelegator().reset();
		this.resetAssets();
	}


	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#propagate()
	 */
	public void propagate() {
		this.updateLastProbabilityPropery();
		this.getDelegator().propagate();
		this.updateAssets();
		this.printAssets();
	}
	
	
	public void printAssets() {
		
		Graph g = this.getNetwork();
		if (g != null && (g instanceof SingleEntityNetwork)) {
			SingleEntityNetwork singleEntityNetwork = (SingleEntityNetwork) g;
			
			
			// extract assets
			Object prop = this.getNetwork().getProperty(CURRENT_ASSETS_PROPERTY);
			if (prop == null || !(prop instanceof Map)) {
				throw new IllegalStateException("The property " + CURRENT_ASSETS_PROPERTY + " of " 
						+ singleEntityNetwork 
						+ " is neither set nor it is not a Map<Clique, PotentialTable>).");
			}
			Map<Clique, PotentialTable> currentAssetsMap = (Map<Clique, PotentialTable>) prop;

			// initialize message to print
			String explMessage = "\n \n Assets: \n \n";
			
			// print assets for each node in each clique
			for (Clique clique : currentAssetsMap.keySet()) {
				explMessage += "Clique {" + clique + "}:\n \t \t";
				PotentialTable assetTable = currentAssetsMap.get(clique);
				PotentialTable probTable = clique.getProbabilityFunction();
				for (int i = 0; i < assetTable.tableSize(); i++) {
					explMessage +=  
//						assetTable.getVariableAt(varIndex) 
//						+ " = " + assetTable.getVariableAt(varIndex).getStateAt(stateIndex++) + " : "
//						+ 
						assetTable.getValue(i) + " (" + probTable.getValue(i)*100 + "%)" + "\n \t \t";
				}
				explMessage += "\n\n";
			}
			
			singleEntityNetwork.getLogManager().append(explMessage);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#addInferencceAlgorithmListener(unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener)
	 */
	public void addInferencceAlgorithmListener(
			IInferenceAlgorithmListener listener) {
		this.getDelegator().addInferencceAlgorithmListener(listener);
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#removeInferencceAlgorithmListener(unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener)
	 */
	public void removeInferencceAlgorithmListener(
			IInferenceAlgorithmListener listener) {
		this.getDelegator().removeInferencceAlgorithmListener(listener);
	}

	/**
	 * Requests to this class not related to assets will be delegated
	 * to this object.
	 * @return the delegator
	 */
	public IInferenceAlgorithm getDelegator() {
		return delegator;
	}

	/**
	 * Requests to this class not related to assets will be delegated
	 * to this object.
	 * @param delegator the delegator to set
	 */
	public void setDelegator(IInferenceAlgorithm delegator) {
		this.delegator = delegator;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#setMediator(unbbayes.controller.INetworkMediator)
	 */
	public void setMediator(INetworkMediator mediator) {
		this.getDelegator().setMediator(mediator);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getMediator()
	 */
	public INetworkMediator getMediator() {
		return this.getDelegator().getMediator();
	}

	/**
	 * This is the quantity of assets initially set for all states
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
	 * This {@link Comparator} comparates cliques within the properties {@link #CURRENT_ASSETS_PROPERTY},
	 * {@link #LAST_PROBABILITY_PROPERTY}, and {@link #INITIAL_ASSETS_PROPERTY}
	 * of {@link #getNetwork()} {@link Graph#getProperty(String)}.
	 * Because those properties are Map<Clique, PotentialTable> (particularly, a {@link TreeMap}),
	 * a {@link Comparator} is used to determine whether two objects are representing the same
	 * clique.
	 * @return the cliqueComparator
	 */
	public Comparator<Clique> getCliqueComparator() {
		return cliqueComparator;
	}

	/**
	 * This {@link Comparator} comparates cliques within the properties {@link #CURRENT_ASSETS_PROPERTY},
	 * {@link #LAST_PROBABILITY_PROPERTY}, and {@link #INITIAL_ASSETS_PROPERTY}
	 * of {@link #getNetwork()} {@link Graph#getProperty(String)}.
	 * Because those properties are Map<Clique, PotentialTable> (particularly, a {@link TreeMap}),
	 * a {@link Comparator} is used to determine whether two objects are representing the same
	 * clique.
	 * @param cliqueComparator the cliqueComparator to set
	 */
	public void setCliqueComparator(Comparator<Clique> cliqueComparator) {
		this.cliqueComparator = cliqueComparator;
	}

}
