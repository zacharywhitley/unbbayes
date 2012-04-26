/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import unbbayes.controller.INetworkMediator;
import unbbayes.gui.AssetCompilationPanelBuilder;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.AssetNode;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNetworkFilter;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.bn.TreeVariable;
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

	private IAssetNetAlgorithm assetPropagationDelegator;




	private boolean isToUpdateAssets = true;
	
	
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
		// for some reason, polymorphism is not working so properly...
		if (probabilityDelegator instanceof JunctionTreeAlgorithm) {
			// call explicitly
			ret.setProbabilityPropagationDelegator((JunctionTreeAlgorithm)probabilityDelegator);
		} else {
			ret.setProbabilityPropagationDelegator(probabilityDelegator);
		}
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
	
	


	/**
	 * This will set the probabilistic network (the one used in {@link #getProbabilityPropagationDelegator()}).
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

	/**
	 * this will return the probabilitstic network obtainable {@link #getProbabilityPropagationDelegator()}
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
		if (getNetwork() == null) {
			throw new NullPointerException("No Bayes Net to compile.");
		}
		if (getNetwork() instanceof SingleEntityNetwork && ((SingleEntityNetwork) getNetwork()).isID()) {
			throw new IllegalStateException(this.getName() + " does not support Influence Diagrams.");
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
		
		if (isToUpdateAssets()) {
			// store the probability before the propagation, so that we can calculate the ratio
			this.updateProbabilityPriorToPropagation();
		}
		// propagate probability
		this.getProbabilityPropagationDelegator().propagate();
		
		if (isToUpdateAssets()) {
			// calculate ratio and propagate assets
			this.getAssetPropagationDelegator().propagate();
		}
		
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
	 * 
	 * @param delegator
	 */
	public void setProbabilityPropagationDelegator(IInferenceAlgorithm delegator) {
		// TODO check why polymorphism is not working
		this.probabilityPropagationDelegator = delegator;
	}
	
	/**
	 * Requests to this class not related to assets will be delegated
	 * to this object.
	 * This method also uses {@link IInferenceAlgorithm#removeInferencceAlgorithmListener(IInferenceAlgorithmListener)}
	 * and {@link IInferenceAlgorithm#addInferencceAlgorithmListener(IInferenceAlgorithmListener)} in order
	 * to adjust the behavior of the algorithm.
	 * Particularly, this method will remove the old listeners and add a new one which is similar to the one added by
	 * {@link JunctionTreeAlgorithm#JunctionTreeAlgorithm(ProbabilisticNetwork)}, but does not
	 * reset the network each time a {@link IInferenceAlgorithm#propagate()} is called.
	 * By doing this, we cannot overwrite hard evidences anymore (i.e. we cannot add a hard evidence to one state, and then
	 * add another hard evidence to another state - which is 0% now because of the previous hard evidence), 
	 * but in the other hand, deleting a virtual node should behave like incorporating the soft/likelihood evidence
	 * into the clique. This is done by implementing {@link IInferenceAlgorithmListener#onBeforePropagate(IInferenceAlgorithm)} properly.
	 * It also guarantees that disconnected cliques are normalized after propagation (by using {@link IInferenceAlgorithmListener#onAfterPropagate(IInferenceAlgorithm)})
	 * It also checks whether the network is hybrid (which cannot be handled by this algorithm). 
	 * If so, it will throw an {@link IllegalArgumentException} on {@link IInferenceAlgorithmListener#onBeforeRun(IInferenceAlgorithm)}.
	 * @param probabilityPropagationDelegator the probabilityPropagationDelegator to set
	 */
	public void setProbabilityPropagationDelegator(JunctionTreeAlgorithm delegator) {
		this.probabilityPropagationDelegator = delegator;
		if (this.probabilityPropagationDelegator != null) {
			// calling removeInferencceAlgorithmListener with null is supposed to remove all listeners...
			this.probabilityPropagationDelegator.removeInferencceAlgorithmListener(null);
			
			// add dynamically changeable behavior (i.e. routines that are not "mandatory", so it is interesting to be able to disable them when needed)
			this.probabilityPropagationDelegator.addInferencceAlgorithmListener(new IInferenceAlgorithmListener() {
				public void onBeforeRun(IInferenceAlgorithm algorithm) {
					if (algorithm == null) {
						Debug.println(getClass(), "Algorithm == null");
						return;
					}
					if ((algorithm.getNetwork() != null) && ( algorithm.getNetwork() instanceof SingleEntityNetwork)) {
						SingleEntityNetwork net = (SingleEntityNetwork)algorithm.getNetwork();
						
						if (net.isHybridBN()) {
							// TODO use resource file instead
							throw new IllegalArgumentException(
										algorithm.getName() 
										+ " cannot handle continuous nodes. \n\n Please, go to the Global Options and choose another inference algorithm."
									);
						}
					}
				}
				public void onBeforeReset(IInferenceAlgorithm algorithm) {}
				
				/**
				 * Add virtual nodes.
				 * This code was added here (before propagation) because we need current marginal (i.e. prior probabilities) to calculate likelihood ratio of soft evidence by using
				 * Jeffrey's rule.
				 */
				public void onBeforePropagate(IInferenceAlgorithm algorithm) {
					// we will iterate on all nodes and check whether they have soft/likelihood evidences. If so, create virtual nodes
					if ((algorithm.getNetwork() != null) && ( algorithm.getNetwork() instanceof SingleEntityNetwork)) {
						SingleEntityNetwork net = (SingleEntityNetwork)algorithm.getNetwork();
						// iterate in a new list, because net.getNodes may suffer concurrent changes because of virtual nodes.
						for (Node n : new ArrayList<Node>(net.getNodes())) {	
							if (n instanceof TreeVariable) {
								TreeVariable node = (TreeVariable)n;
								if (node.hasEvidence()) {
									if (node.hasLikelihood()) {
										if (algorithm instanceof JunctionTreeAlgorithm) {
											JunctionTreeAlgorithm jt = (JunctionTreeAlgorithm) algorithm;
											// Enter the likelihood as virtual nodes
											try {
												// prepare list of nodes to add soft/likelihood evidence
												List<INode> evidenceNodes = new ArrayList<INode>();
												evidenceNodes.add(node);	// the main node is the one carrying the likelihood ratio
												// if conditional soft evidence, add all condition nodes (if non-conditional, then this will add an empty list)
												evidenceNodes.addAll(jt.getLikelihoodExtractor().extractLikelihoodParents(getNetwork(), node));
												// create the virtual node
												jt.addVirtualNode(getNetwork(), evidenceNodes);
											} catch (Exception e) {
												throw new RuntimeException(e);
											}
										}
									} 
								}
							}
						}
					}
						
					// Finally propagate evidence
				}
				public void onAfterRun(IInferenceAlgorithm algorithm) {}
				public void onAfterReset(IInferenceAlgorithm algorithm) {}
				
				/**
				 * Guarantee that each clique is normalized, if the network is disconnected
				 * and clear virtual nodes
				 */
				public void onAfterPropagate(IInferenceAlgorithm algorithm) {
					if (algorithm == null) {
						Debug.println(getClass(), "Algorithm == null");
						return;
					}
					
					// Guarantee that each clique is normalized, if the network is disconnected
					if ((algorithm.getNetwork() != null) && (algorithm.getNetwork() instanceof SingleEntityNetwork)) {
						SingleEntityNetwork network = (SingleEntityNetwork) algorithm.getNetwork();
						if (!network.isConnected()) {
							// network is disconnected.
							if (network.getJunctionTree() != null) {
								// extract all cliques and normalize them
								for (Clique clique : network.getJunctionTree().getCliques()) {
									try {
										clique.normalize();
										for (Node node : clique.getAssociatedProbabilisticNodes()) {
											if (node instanceof TreeVariable) {
												((TreeVariable) node).updateMarginal();
											}
										}
									} catch (Exception e) {
										throw new RuntimeException(e);
									}
								}
							}
						}
					}
					
					// clear virtual nodes if this is using junction tree algorithm or algorithms related to it
					if (algorithm instanceof JunctionTreeAlgorithm) {
						((JunctionTreeAlgorithm) algorithm).clearVirtualNodes();						
					} else if (algorithm instanceof AssetAwareInferenceAlgorithm) {
						// extract algorithm responsible for updating probabilities
						IInferenceAlgorithm probAlgorithm = ((AssetAwareInferenceAlgorithm)algorithm).getProbabilityPropagationDelegator();
						if (probAlgorithm instanceof JunctionTreeAlgorithm) {
							((JunctionTreeAlgorithm) probAlgorithm).clearVirtualNodes();		
						}
					}
				}
			});
		}
	}

	/**
	 * Caution: this method assumes that the nodes in the asset net {@link #getAssetNetwork()} are synchronized with the nodes in the cpt
	 * by names (i.e. for each node in cpt, there is a node in {@link #getAssetNetwork()} with the same name).
	 * 
	 * @param cpt : a CPT representing the conditional probability P(Target | Assumptions), or simply P(T|A) (A is a set of nodes).
	 * {@link unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor#buildCondicionalProbability(INode, List, Graph, IInferenceAlgorithm)}
	 * can dynamically create such CPT. {@link IProbabilityFunction#getVariableAt(0)} is assumed to be the target variable.
	 * 
	 * @param indexInCPT : index of the cell in the CPT (condProb) to calculate allowed interval of edit. For example, suppose
	 * target boolean random variable T and assumption boolean random variable A. Also suppose 
	 * P(T | A) = [P(T=false|A=false), P(T=false|A=true), P(T=true|A=false), P(T=true|A=true)] = [.2, .8, .4, .6].
	 * Then, indexInCondProb == 0 is a reference to P(T=false|A=false) = .2, hence this method will calculate the allowed interval of 
	 * edit for P(T=false|A=false). Similarly, indexInCondProb == 3 is a reference to P(T=true|A=true) = .6, 
	 * and this method will calculate the allowed interval of edit for P(T=true|A=true).
	 * 
	 * @return the array [ P(T=t|A=a)/m1 ; 1- (1 - P(T=t|A=a))/m2 ], whose index 0 is the lower bound of allowed edit (probability that guarantees that
	 * the min-q will never become below 1), and index 1 is the respective upper bound. 
	 * In this formula, the value m1 is the min-q assuming T=t and A=a, and m2 is the min-q
	 * assuming T!=t and A=a.
	 * 
	 * @see #runMinPropagation()
	 * @see #calculateExplanation(List)
	 * @see #undoMinPropagation()
	 */
	public float[] calculateIntervalOfAllowedEdit(PotentialTable cpt, int indexInCPT) {
		// initial assertions
		if (cpt == null) {
			throw new NullPointerException(getClass().getName()+"#calculateIntervalOfAllowedEdit(null," + indexInCPT + ")");
		}
		if (indexInCPT < 0 || indexInCPT >= cpt.tableSize()) {
			// cpt.getMultidimensionalCoord may enter in infinite loop if we do not throw exception here.
			throw new ArrayIndexOutOfBoundsException(indexInCPT);
		}
		
		// extract asset nodes related to the nodes in cpt. Supposedly, they have same names.
		List<AssetNode> assetNodes = new ArrayList<AssetNode>();	// use list, because the order is important (e.g. 1st node is always the target)
		for (int i = 0; i < cpt.getVariablesSize(); i++) {
			INode probNode = cpt.getVariableAt(i);
			INode assetNode = this.getAssetNetwork().getNode(probNode.getName());
			if (assetNode == null || !(assetNode instanceof AssetNode)) {
				throw new IllegalArgumentException(probNode + " has no respective asset node: " + assetNode);
			}
			// at this point, assetNode is an instance of AssetNode
			assetNodes.add((AssetNode)assetNode);
		}
		
		// indexInCondProb is the index of the CPT represented as a 1 dimensional array. Translate it to indexes of states of nodes.
		int[] indexesOfStatesOfNodes = cpt.getMultidimensionalCoord(indexInCPT);
		
		// assume T=t and A=a (A is actually a set of all nodes in cpt which are not T)
		for (int i = 0; i < assetNodes.size(); i++) {
			// coord[0] has the state of T (the target node). coord[1] has the state of first node of A (assumption nodes), and so on
			assetNodes.get(i).addFinding(indexesOfStatesOfNodes[i]);
		}
		
		// obtain m1, which is the min-q value assuming  T=t and A=a
		this.runMinPropagation();
		float minQValue = this.calculateExplanation(new ArrayList<Map<INode,Integer>>());	// this is m1
		this.undoMinPropagation();	// revert changes in the asset tables
		
		// assume T!=t and A=a
		assetNodes.get(0).addFinding(indexesOfStatesOfNodes[0], true);	// negative evidence to T = t (which is always the first node in cpt)
		for (int i = 1; i < assetNodes.size(); i++) { // all other evidences are ordinal evidences
			// coord[0] has the state of T (the target node). coord[1] has the state of first node of A (assumption nodes), and so on
			assetNodes.get(i).addFinding(indexesOfStatesOfNodes[i]);
		}
		
		// obtain m2, which is the min-q value assuming  T!=t and A=a
		this.runMinPropagation();
		float minQValueAssumingNotTarget = this.calculateExplanation(new ArrayList<Map<INode,Integer>>());	// this is m2
		this.undoMinPropagation();	// revert changes in the asset tables
		
		// clear all findings explicitly.
		for (int i = 0; i < assetNodes.size(); i++) {
			assetNodes.get(i).resetEvidence();
		}
		
		// extract value P(T=t|A=a) 
		float prob = cpt.getValue(indexInCPT);
		
		// this will be the value to return
		float[] ret = new float[2];
		
		// P(T=t|A=a)/m1  
		ret[0] = prob/minQValue;
		
		// 1 - (1-P(T=t|A=a))/m2
		ret[1] = 1-((1 - prob)/minQValueAssumingNotTarget);
		
		return ret;
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
	public IAssetNetAlgorithm getAssetPropagationDelegator() {
		return assetPropagationDelegator;
	}

	/**
	 * @param assetPropagationDelegator the assetPropagationDelegator to set
	 */
	public void setAssetPropagationDelegator(
			IAssetNetAlgorithm assetPropagationDelegator) {
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
	public AssetNetwork createAssetNetFromProbabilisticNet(
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

	/**
	 * Delegates to {@link #getAssetPropagationDelegator()}
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#isToPropagateForGlobalConsistency()
	 */
	public boolean isToPropagateForGlobalConsistency() {
		try {
			return this.getAssetPropagationDelegator().isToPropagateForGlobalConsistency();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Delegates to {@link #getAssetPropagationDelegator()}
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#setToPropagateForGlobalConsistency(boolean)
	 */
	public void setToPropagateForGlobalConsistency(
			boolean isToPropagateForGlobalConsistency) {
		try {
			this.getAssetPropagationDelegator().setToPropagateForGlobalConsistency(isToPropagateForGlobalConsistency);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}


	
	/**
	 * This method only delegates to {@link #getAssetPropagationDelegator()}
	 * @see IAssetNetAlgorithm#calculateExplanation(List)
	 * @see AssetPropagationInferenceAlgorithm#calculateExplanation(List)
	 * @see IExplanationJunctionTree#calculateExplanation(Graph, IInferenceAlgorithm)
	 */
	public float calculateExplanation( List<Map<INode, Integer>> inputOutpuArgumentForExplanation){
		if (this.getAssetPropagationDelegator() == null) {
			return Float.NaN;
		}
		if (inputOutpuArgumentForExplanation == null) {
			inputOutpuArgumentForExplanation = new ArrayList<Map<INode,Integer>>();
		}
		return this.getAssetPropagationDelegator().calculateExplanation(inputOutpuArgumentForExplanation);
	}

	/**
	 * Delegates to {@link #getAssetPropagationDelegator()}
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#isToUpdateSeparators()
	 */
	public boolean isToUpdateSeparators() {
		try {
			return this.getAssetPropagationDelegator().isToUpdateSeparators();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Delegates to {@link #getAssetPropagationDelegator()}
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#setToUpdateSeparators(boolean)
	 */
	public void setToUpdateSeparators(boolean isToPropagateForGlobalConsistency) {
		try {
			this.getAssetPropagationDelegator().setToUpdateSeparators(isToPropagateForGlobalConsistency);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Delegates to {@link #getAssetPropagationDelegator()}.
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#runMinPropagation()
	 */
	public void runMinPropagation() {
		this.getAssetPropagationDelegator().runMinPropagation();
	}

	/**
	 * Delegates to {@link #getAssetPropagationDelegator()}.
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#undoMinPropagation()
	 */
	public void undoMinPropagation() {
		this.getAssetPropagationDelegator().undoMinPropagation();
	}

	/**
	 * Delegates to {@link #getAssetPropagationDelegator()}.
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#updateProbabilityPriorToPropagation()
	 */
	public void updateProbabilityPriorToPropagation() {
		this.getAssetPropagationDelegator().updateProbabilityPriorToPropagation();
	}
	
	/**
	 * deletages to {@link #getAssetPropagationDelegator()}
	 * @param isToLogAssets the isToLogAssets to set
	 */
	public void setToLogAssets(boolean isToLogAssets) {
		getAssetPropagationDelegator().setToLogAssets(isToLogAssets);
	}

	/**
	 * 
	 * deletages to {@link #getAssetPropagationDelegator()}
	 * @return the isToLogAssets
	 */
	public boolean isToLogAssets() {
		return getAssetPropagationDelegator().isToLogAssets();
	}

	/**
	 * @param isToUpdateAssets the isToUpdateAssets to set
	 */
	public void setToUpdateAssets(boolean isToUpdateAssets) {
		this.isToUpdateAssets = isToUpdateAssets;
	}

	/**
	 * @return the isToUpdateAssets
	 */
	public boolean isToUpdateAssets() {
		return isToUpdateAssets;
	}
}
