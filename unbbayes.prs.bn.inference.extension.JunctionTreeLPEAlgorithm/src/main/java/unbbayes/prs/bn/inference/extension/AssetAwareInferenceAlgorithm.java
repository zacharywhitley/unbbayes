/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import unbbayes.controller.INetworkMediator;
import unbbayes.gui.AssetCompilationPanelBuilder;
import unbbayes.prs.Graph;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNetworkFilter;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.Debug;
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
public class AssetAwareInferenceAlgorithm implements IAssetNetAlgorithm {
	
	private List<IInferenceAlgorithmListener> inferenceAlgorithmListener = new ArrayList<IInferenceAlgorithmListener>();
	

	
	
	private IInferenceAlgorithm probabilityPropagationDelegator;

	private AssetPropagationInferenceAlgorithm assetPropagationDelegator;
	
	
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
	 * Name of the property in {@link Graph#getProperty(String)} which manages assets during first call of {@link #run()}. 
	 * The content is a Map<Clique, PotentialTable>, which maps a node to its clique-wise asset table (an asset table for all nodes in the same clique). 
	 */
	public static final String INITIAL_ASSETS_PROPERTY = AssetAwareInferenceAlgorithm.class.getName() + ".initialAssetMap";
	
	
	
	/**
	 * Default constructor is at least protected, in order to allow inheritance.
	 */
	protected AssetAwareInferenceAlgorithm() {}
	
	/**
	 * Default instantiation method.
	 */
	public static IInferenceAlgorithm getInstance(IInferenceAlgorithm probabilityDelegator) {
		AssetAwareInferenceAlgorithm ret = new AssetAwareInferenceAlgorithm();
		ret.setProbabilityPropagationDelegator(probabilityDelegator);
		try {
			ret.setAssetPropagationDelegator(AssetPropagationInferenceAlgorithm.getInstance((ProbabilisticNetwork) probabilityDelegator.getNetwork()));
		} catch (Exception e) {
			throw new IllegalArgumentException("The network managed by " + probabilityDelegator + " must be an instance of " + ProbabilisticNetwork.class.getName(), e);
		}
		return ret;
	}
	
	
	
	/**
	 * This method creates a copy of {@link Graph#getProperty(String)}, property name {@link #CURRENT_ASSETS_PROPERTY}
	 * in {@link Graph#addProperty(String, Object)}, property name {@link #INITIAL_ASSETS_PROPERTY}.
	 * It only initializes the assets if {@link #INITIAL_ASSETS_PROPERTY} is not already set in {@link Graph#getProperty(String)},
	 * so it is possible to initialize the {@link #INITIAL_ASSETS_PROPERTY} with any value if you wish.
	 * @throws InvalidParentException 
	 * @throws IllegalArgumentException 
	 */
	public void initializeAssets() throws IllegalArgumentException, InvalidParentException {
		this.getAssetPropagationDelegator().setRelatedProbabilisticNetwork((ProbabilisticNetwork) this.getNetwork());
//		this.getAssetPropagationDelegator().setNetwork(this.getAssetPropagationDelegator().createAssetNetFromProbabilisticNet(this.getAssetPropagationDelegator().getRelatedProbabilisticNetwork()));
	}
	
	


	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#setNetwork(unbbayes.prs.Graph)
	 */
	public void setNetwork(Graph g) throws IllegalArgumentException {
		this.getProbabilityPropagationDelegator().setNetwork(g);
		// it should update the this.getAssetPropagationDelegator only on propagate
//		try {
//			this.getAssetPropagationDelegator().setRelatedProbabilisticNetwork((ProbabilisticNetwork) g);
//		} catch (InvalidParentException e) {
//			throw new IllegalArgumentException(e);
//		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getNetwork()
	 */
	public Graph getNetwork() {
		return this.getProbabilityPropagationDelegator().getNetwork();
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#run()
	 */
	public void run() throws IllegalStateException {
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListener()) {
			listener.onBeforeRun(this);
		}
		
		this.getProbabilityPropagationDelegator().run();
//		try {
//			this.getAssetPropagationDelegator().setRelatedProbabilisticNetwork((ProbabilisticNetwork) this.getProbabilityPropagationDelegator().getNetwork());
//		} catch (Exception e) {
//			throw new IllegalArgumentException("Could not initialize asset network for " + this.getProbabilityPropagationDelegator().getNetwork(),e);
//		}
		try {
			this.initializeAssets();
		} catch (Exception e) {
			throw new IllegalStateException(this + " was called with an illegal network.",e);
		}
		
		// TODO migrate these GUI code to the plugin infrastructure
		if (this.getMediator() != null) {
			AssetCompilationPanelBuilder builder = new AssetCompilationPanelBuilder();
			JComponent component = builder.buildCompilationPanel(this, this.getMediator());
			this.getMediator().getScreen().getContentPane().add(component, this.getMediator().getScreen().PN_PANE_PN_COMPILATION_PANE);
			this.getMediator().getScreen().getCardLayout().addLayoutComponent(component, this.getMediator().getScreen().PN_PANE_PN_COMPILATION_PANE);
		}
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListener()) {
			listener.onAfterRun(this);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getName()
	 */
	public String getName() {
		return this.getProbabilityPropagationDelegator().getName() + " + assets";
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getDescription()
	 */
	public String getDescription() {
		return "[Adaptation to handle assets]" + this.getProbabilityPropagationDelegator().getDescription();
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#reset()
	 */
	public void reset() {
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListener()) {
			listener.onBeforeReset(this);
		}
		
		// reset probability
		this.getProbabilityPropagationDelegator().reset();
		// reset assets
		this.getAssetPropagationDelegator().reset();
		
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListener()) {
			listener.onAfterReset(this);
		}
	}


	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#propagate()
	 */
	public void propagate() {
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListener()) {
			listener.onBeforePropagate(this);
		}
		
		// store the probability before the propagation, so that we can calculate the ratio
		this.getAssetPropagationDelegator().updateProbabilityPriorToPropagation();
		// propagate probability
		this.getProbabilityPropagationDelegator().propagate();
		// calculate ratio and propagate assets
		this.getAssetPropagationDelegator().propagate();
		
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListener()) {
			listener.onAfterPropagate(this);
		}
	}
	
	

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#addInferencceAlgorithmListener(unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener)
	 */
	public void addInferencceAlgorithmListener(
			IInferenceAlgorithmListener listener) {
//		this.getProbabilityPropagationDelegator().addInferencceAlgorithmListener(listener);
		this.getInferenceAlgorithmListener().add(listener);
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#removeInferencceAlgorithmListener(unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener)
	 */
	public void removeInferencceAlgorithmListener(
			IInferenceAlgorithmListener listener) {
//		this.getProbabilityPropagationDelegator().removeInferencceAlgorithmListener(listener);
		this.getInferenceAlgorithmListener().remove(listener);
	}

	/**
	 * Requests to this class not related to assets will be delegated
	 * to this object.
	 * @return the probabilityPropagationDelegator
	 */
	public IInferenceAlgorithm getProbabilityPropagationDelegator() {
		return probabilityPropagationDelegator;
	}

	/**
	 * Requests to this class not related to assets will be delegated
	 * to this object.
	 * @param probabilityPropagationDelegator the probabilityPropagationDelegator to set
	 */
	public void setProbabilityPropagationDelegator(IInferenceAlgorithm delegator) {
		this.probabilityPropagationDelegator = delegator;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#setMediator(unbbayes.controller.INetworkMediator)
	 */
	public void setMediator(INetworkMediator mediator) {
		this.getProbabilityPropagationDelegator().setMediator(mediator);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getMediator()
	 */
	public INetworkMediator getMediator() {
		return this.getProbabilityPropagationDelegator().getMediator();
	}


	

	/**
	 * @return the assetPropagationDelegator
	 */
	public AssetPropagationInferenceAlgorithm getAssetPropagationDelegator() {
		return assetPropagationDelegator;
	}

	/**
	 * @param assetPropagationDelegator the assetPropagationDelegator to set
	 */
	public void setAssetPropagationDelegator(
			AssetPropagationInferenceAlgorithm assetPropagationDelegator) {
		this.assetPropagationDelegator = assetPropagationDelegator;
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
	 * This will delegate to {@link #getAssetPropagationDelegator()}
	 * @return
	 */
	public float getDefaultInitialAssetQuantity() {
		try {
			return this.getAssetPropagationDelegator().getDefaultInitialAssetQuantity();
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		return 1000.0f;
	}

	/**
	 * This will delegate to {@link #getAssetPropagationDelegator()}
	 * @param initialAssetQuantity
	 */
	public void setDefaultInitialAssetQuantity(float initialAssetQuantity) {
		try {
			this.getAssetPropagationDelegator().setDefaultInitialAssetQuantity(initialAssetQuantity);
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#getRelatedProbabilisticNetwork()
	 */
	public ProbabilisticNetwork getRelatedProbabilisticNetwork() {
		return this.getAssetPropagationDelegator().getRelatedProbabilisticNetwork();
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#createAssetNetFromProbabilisticNet(unbbayes.prs.bn.ProbabilisticNetwork)
	 */
	public Graph createAssetNetFromProbabilisticNet(
			ProbabilisticNetwork relatedProbabilisticNetwork)
			throws InvalidParentException {
		if (getProbabilityPropagationDelegator() instanceof JunctionTreeAlgorithm) {
			JunctionTreeAlgorithm junctionTreeAlgorithm = (JunctionTreeAlgorithm) getProbabilityPropagationDelegator();
			// create asset net without dummy (virtual) nodes
			return this.getAssetPropagationDelegator().createAssetNetFromProbabilisticNet(new ProbabilisticNetworkFilter(relatedProbabilisticNetwork, junctionTreeAlgorithm.getVirtualNodesToCliquesAndSeparatorsMap().keySet()));
		}
		return this.getAssetPropagationDelegator().createAssetNetFromProbabilisticNet(relatedProbabilisticNetwork);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#setRelatedProbabilisticNetwork(unbbayes.prs.bn.ProbabilisticNetwork)
	 */
	public void setRelatedProbabilisticNetwork(
			ProbabilisticNetwork relatedProbabilisticNetwork)
			throws IllegalArgumentException, InvalidParentException {
		this.setNetwork(relatedProbabilisticNetwork);
		
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#setAssetNetwork(unbbayes.prs.bn.AssetNetwork)
	 */
	public void setAssetNetwork(AssetNetwork network)
			throws IllegalArgumentException {
		this.getAssetPropagationDelegator().setAssetNetwork(network);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#getAssetNetwork()
	 */
	public AssetNetwork getAssetNetwork() {
		return this.getAssetPropagationDelegator().getAssetNetwork();
	}


	

	
	
}
