/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import unbbayes.prs.bn.ILikelihoodExtractor;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.JeffreyRuleLikelihoodExtractor;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNetworkFilter;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.Separator;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener;
import unbbayes.util.extension.bn.inference.IRandomVariableAwareInferenceAlgorithm;

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
	
	private boolean isToNormalizeDisconnectedNets = false;
	
	
	private IInferenceAlgorithm probabilityPropagationDelegator;

	private IAssetNetAlgorithm assetPropagationDelegator;

	private boolean isToUpdateAssets = true;
	
	private boolean isToUseQValues = AssetPropagationInferenceAlgorithm.IS_TO_USE_Q_VALUES;

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

	

	/** Default value to fill {@link JunctionTreeAlgorithm#setLikelihoodExtractor(ILikelihoodExtractor)} */
	public static final JeffreyRuleLikelihoodExtractor DEFAULT_JEFFREYRULE_LIKELIHOOD_EXTRACTOR = (JeffreyRuleLikelihoodExtractor) JeffreyRuleLikelihoodExtractor.newInstance();

	/** This is a default instance of a node with only 1 state. Use this instance if you want to have nodes with 1 state not to occupy too much space in memory */
	public static final ProbabilisticNode ONE_STATE_PROBNODE = new ProbabilisticNode() {
		// TODO do not allow edit

		/* (non-Javadoc)
		 * @see unbbayes.prs.bn.ProbabilisticNode#clone(double)
		 */
		public ProbabilisticNode clone(double radius) {
			return this;
		}

		/* (non-Javadoc)
		 * @see unbbayes.prs.bn.ProbabilisticNode#clone()
		 */
		public Object clone() {
			return this;
		}

		/* (non-Javadoc)
		 * @see unbbayes.prs.bn.ProbabilisticNode#basicClone()
		 */
		public ProbabilisticNode basicClone() {
			return this;
		}
	};
	static{
//		ONE_STATE_PROBNODE = new ProbabilisticNode();
		ONE_STATE_PROBNODE.setName("PROB_NODE_WITH_SINGLE_STATE");
		// copy states
		ONE_STATE_PROBNODE.appendState("VIRTUAL_STATE");
		ONE_STATE_PROBNODE.initMarginalList();	// guarantee that marginal list is initialized
		ONE_STATE_PROBNODE.setMarginalAt(0, 1f);	// set the only state to 100%
	}
	

	private List<ExpectedAssetCellMultiplicationListener> expectedAssetCellListeners = new ArrayList<AssetAwareInferenceAlgorithm.ExpectedAssetCellMultiplicationListener>();

	private boolean isToCalculateLPE = false;

	private float expectedAssetPivot = 0;

	private boolean isToResetEvidenceBeforeRun = true;

	private Map<TreeVariable, Integer> permanentEvidenceMap = null;

	private String name = null;

	private boolean isToChangeGUI = true;
	


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
		// explicitly reset all evidences
		if (isToResetEvidenceBeforeRun()) {
			for (Node node : getNetwork().getNodes()) {
				if (node instanceof TreeVariable) {
					((TreeVariable) node).resetLikelihood();
					((TreeVariable) node).resetEvidence();
				}
			}
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
		if (this.getMediator() != null && isToChangeGUI() ) {
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
		if (this.name  == null) {
			this.name = this.getProbabilityPropagationDelegator().getName() + " + assets";
		}
		return this.name;
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
		
		if (getPermanentEvidenceMap()!= null) {
			for (TreeVariable node : getPermanentEvidenceMap().keySet()) {
				int state = getPermanentEvidenceMap().get(node);
				if (state < 0) {
					// set finding as negative (i.e. finding setting a state to 0%)
					node.addFinding(Math.abs(state+1), true);
				} else {
					node.addFinding(state);
				}
			}
		}
		
		if (isToUpdateAssets()) {
			// store the probability before the propagation, so that we can calculate the ratio
			this.updateProbabilityPriorToPropagation();
		}
		// propagate probability
		this.getProbabilityPropagationDelegator().propagate();
		
		// zeroAssetsException != null if this.getAssetPropagationDelegator().propagate() has thrown such exception
		ZeroAssetsException zeroAssetsException = null;	
		if (isToUpdateAssets()) {
			// calculate ratio and propagate assets
			try {
				this.getAssetPropagationDelegator().propagate();
			} catch (ZeroAssetsException e) {
				// revert all changes in probabilities
				this.revertLastProbabilityUpdate();
				// do not re-throw e immediately, because we still want to execute anything (i.e. execute IInferenceAlgorithmListener) a normal propagation would do
				zeroAssetsException = e;
			}
		}
		
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListener()) {
			listener.onAfterPropagate(this);
		}
		
		if (zeroAssetsException != null) {
			// this.getAssetPropagationDelegator().propagate() did not execute properly
			throw zeroAssetsException;
		}
	}
	
	

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#revertLastProbabilityUpdate()
	 */
	public void revertLastProbabilityUpdate() {
		this.getAssetPropagationDelegator().revertLastProbabilityUpdate();
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
	 * @param delegator : in {@link AssetAwareInferenceAlgorithm}, only instances compatible with {@link JunctionTreeAlgorithm}
	 * are allowed.
	 */
	public void setProbabilityPropagationDelegator(IInferenceAlgorithm delegator) {
		if (!(delegator instanceof JunctionTreeAlgorithm)) {
			throw new IllegalArgumentException("Only instances of " + JunctionTreeAlgorithm.class.getName() + " are allowed in " + this.getClass().getName()+ "#setProbabilityPropagationDelegator(IInferenceAlgorithm)");
		} else {
			this.setProbabilityPropagationDelegator((JunctionTreeAlgorithm) delegator);
		}
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
//					if (algorithm == null) {
//						Debug.println(getClass(), "Algorithm == null");
//						return;
//					}
//					if ((algorithm.getNetwork() != null) && ( algorithm.getNetwork() instanceof SingleEntityNetwork)) {
//						SingleEntityNetwork net = (SingleEntityNetwork)algorithm.getNetwork();
//						
//						if (net.isHybridBN()) {
//							// TODO use resource file instead
//							throw new IllegalArgumentException(
//										algorithm.getName() 
//										+ " cannot handle continuous nodes. \n\n Please, go to the Global Options and choose another inference algorithm."
//									);
//						}
//					}
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
				public void onAfterRun(IInferenceAlgorithm algorithm) {
					// update the internal ids of separators, nodes, etc
					if (algorithm instanceof IRandomVariableAwareInferenceAlgorithm) {
						((IRandomVariableAwareInferenceAlgorithm)algorithm).initInternalIdentificators();
					}
				}
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
//					if ((algorithm.getNetwork() != null) && (algorithm.getNetwork() instanceof SingleEntityNetwork)) {
//						SingleEntityNetwork network = (SingleEntityNetwork) algorithm.getNetwork();
//						if (isToNormalizeDisconnectedNets() && !network.isConnected()) {
//							// network is disconnected.
//							if (network.getJunctionTree() != null) {
//								// extract all cliques and normalize them
//								for (Clique clique : network.getJunctionTree().getCliques()) {
//									try {
//										clique.normalize();
//										for (Node node : clique.getAssociatedProbabilisticNodes()) {
//											if (node instanceof TreeVariable) {
//												((TreeVariable) node).updateMarginal();
//											}
//										}
//									} catch (Exception e) {
//										throw new RuntimeException(e);
//									}
//								}
//							}
//						}
//					}
					
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
	 * <br/><br/>
	 * If {@link #isToUseQValues()} == false (i.e. asset tables are storing assets - logarithm space - instead of q-values), then
	 * this method returns [ P(T=t|A=a)*power(base, -m1/b) ; 1- (1 - P(T=t|A=a))*power(base,-m2/b) ]
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
		Map<INode, Integer> conditions = new HashMap<INode, Integer>();
		for (int i = 0; i < assetNodes.size(); i++) {
			// coord[0] has the state of T (the target node). coord[1] has the state of first node of A (assumption nodes), and so on
			conditions.put(assetNodes.get(i), indexesOfStatesOfNodes[i]);
		}
		
		// obtain m1, which is the min value (q or assets, depending on isToUseQValues()) assuming  T=t and A=a
		this.runMinPropagation(conditions);
		double minValue = this.calculateExplanation(null);	// this is m1
		this.undoMinPropagation();	// revert changes in the asset tables
		
		// assume T!=t and A=a
		// T!=t is represented as negative evidence (e.g. not state 0 == state -1, not state 1 == state -2, not state 2 == state -3, and so on)
		conditions.put(assetNodes.get(0), -(indexesOfStatesOfNodes[0] + 1)); // negative evidence to T = t (which is always the first node in cpt)
		
		// obtain m2, which is the min value (q or assets, depending on isToUseQValues()) assuming  T!=t and A=a
		this.runMinPropagation(conditions);
		double minValueAssumingNotTarget = this.calculateExplanation(null);	// this is m2
		this.undoMinPropagation();	// revert changes in the asset tables
		
		// clear all findings explicitly.
		for (int i = 0; i < assetNodes.size(); i++) {
			assetNodes.get(i).resetEvidence();
		}
		
		// extract value P(T=t|A=a) 
		float prob = cpt.getValue(indexInCPT);
		
		// this will be the value to return
		float[] ret = new float[2];
		
		if (isToUseQValues()) {
			// P(T=t|A=a)/m1  
			ret[0] = (float) (prob/minValue);
			// 1 - (1-P(T=t|A=a))/m2
			ret[1] = (float) (1-((1 - prob)/minValueAssumingNotTarget));
		} else { // minQValue and minQValueAssumingNotTarget are asset values instead of q-values
			// P(T=t|A=a)*power(base, -m1/b)
			ret[0] = (float) (prob*(Math.pow(this.getqToAssetConverter().getCurrentLogBase(), -minValue/this.getqToAssetConverter().getCurrentCurrencyConstant())));
			if (Float.isInfinite(ret[0]) || Float.isNaN(ret[0])) {
				// in this case, calculateExplanation returned min assets. Convert it to Q.
				minValue = this.getqToAssetConverter().getQValuesFromScore((float) minValue);
				// we can still try using the old equation using q-values
				ret[0] = (float) (prob/minValue);
			}
			// 1- (1 - P(T=t|A=a))*power(base,-m2/b)
			ret[1] = (float) (1-((1 - prob)*Math.pow(this.getqToAssetConverter().getCurrentLogBase(), -minValueAssumingNotTarget/this.getqToAssetConverter().getCurrentCurrencyConstant())));
			if (Float.isInfinite(ret[1]) || Float.isNaN(ret[1])) {
				// in this case, calculateExplanation returned min assets. Convert it to Q.
				minValueAssumingNotTarget = this.getqToAssetConverter().getQValuesFromScore((float) minValueAssumingNotTarget);
				// we can still try using the old equation using q-values
				ret[1] = (float) (1-((1 - prob)/minValueAssumingNotTarget));
			}
		}
		
		
		
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
	public float getDefaultInitialAssetTableValue() {
		try {
			return this.getAssetPropagationDelegator().getDefaultInitialAssetTableValue();
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		return 1000.0f;
	}

	/**
	 * This will delegate to {@link #getAssetPropagationDelegator()}
	 * @param initialAssetQuantity
	 */
	public void setDefaultInitialAssetTableValue(float initialAssetQuantity) {
		try {
			this.getAssetPropagationDelegator().setDefaultInitialAssetTableValue(initialAssetQuantity);
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
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#createAssetNetFromProbabilisticNet(unbbayes.prs.bn.ProbabilisticNetwork, boolean)
	 */
	public AssetNetwork createAssetNetFromProbabilisticNet(ProbabilisticNetwork relatedProbabilisticNetwork)
			throws InvalidParentException {
		if (getProbabilityPropagationDelegator() instanceof JunctionTreeAlgorithm) {
			JunctionTreeAlgorithm junctionTreeAlgorithm = (JunctionTreeAlgorithm) getProbabilityPropagationDelegator();
			// create asset net without dummy (virtual) nodes
			return this.getAssetPropagationDelegator().createAssetNetFromProbabilisticNet(new ProbabilisticNetworkFilter(relatedProbabilisticNetwork, junctionTreeAlgorithm.getVirtualNodesToCliquesAndSeparatorsMap().keySet()));
		}
		return this.getAssetPropagationDelegator().createAssetNetFromProbabilisticNet(relatedProbabilisticNetwork);
	}
	
	
	/**
	 * Calculates the expected assets.
	 * If you want this to be conditional, call {@link ProbabilisticNode#addFinding(int)}
	 * and  {@link IInferenceAlgorithm#propagate()} in {@link #getProbabilityPropagationDelegator()}
	 * first.
	 * This method uses {@link #getEmptySeparatorsDefaultContent()}
	 * if an empty separator is found.
	 * @return expected assets SUM(ProbCliques * AssetsClique) - SUM(ProbSeparators * AssetsSeparators).
	 * @see #getqToAssetConverter()
	 * 
	 */
	public double calculateExpectedAssets() {
		
		// this is the value to be returned by this method
		double ret = 0 ;
		
		// this is the probabilistic network to be used. If there is no conditions, then this is the original network
		ProbabilisticNetwork bayesNet = (ProbabilisticNetwork) this.getNetwork();
		SingleEntityNetwork  assetNet = this.getAssetNetwork();
		
		// initial assertion
		if (assetNet.getJunctionTree() == null
				|| assetNet.getJunctionTree().getCliques() == null
				||  bayesNet.getJunctionTree() == null
				||  bayesNet.getJunctionTree().getCliques() == null) {
			throw new IllegalStateException("Probabilistic network or asset network of " + assetNet +  " is not correctly initialized.");
		}
		
		// guarantee that quantity of cliques matches
		if (assetNet.getJunctionTree().getCliques().size() !=  bayesNet.getJunctionTree().getCliques().size()) {
			throw new IllegalStateException("Probabilistic network has " + bayesNet.getJunctionTree().getCliques().size()
					+ " cliques, but asset network has " + assetNet.getJunctionTree().getCliques().size() + " cliques.");
		}
		
		/*
		 * Since ArrayList<Separator> uses Separator#equals() for searching objects within the list, 
		 * and Separator#equals() compares the names of nodes contained in the separator
		 * (and nodes in prob separators and asset separator are the same),
		 * if we convert the set of prob separators to ArrayList, it is possible to
		 * search for prob separators using asset separators as argument.
		 */
		List<Separator> listOfProbSeparator = new ArrayList<Separator>( bayesNet.getJunctionTree().getSeparators() );
		
		// extract iterators of cliques and separators, so that we can add cliques and subtract separators alternately
		// so that we avoid making ret to become too huge
		Iterator<Clique> assetCliqueIterator = assetNet.getJunctionTree().getCliques().iterator();
		Iterator<Clique> probCliqueIterator = bayesNet.getJunctionTree().getCliques().iterator();
		Iterator<Separator> assetSeparatorIterator = assetNet.getJunctionTree().getSeparators().iterator();
		
		// at this point, assetCliqueIterator and probCliqueIterator have same size and supposedly have same ordering
		while (assetCliqueIterator.hasNext() || assetSeparatorIterator.hasNext()) {
			if (assetCliqueIterator.hasNext()) {
				// add product of probabilities and assets of cliques , 
				Clique assetClique = assetCliqueIterator.next();
				Clique probClique  = probCliqueIterator.next();
				for (int j = 0; j < assetClique.getProbabilityFunction().tableSize(); j++) {
					// extract values in the cell
					float assetValue = assetClique.getProbabilityFunction().getValue(j);
					float probValue = 1f; 
					if (probClique.getProbabilityFunction().tableSize() > 1) {
						// note: if probClique has no variable, it is considered as if it has 1 variable with 100% prob
						probValue = probClique.getProbabilityFunction().getValue(j);
					}
					
					if (probValue == 0f) {
						continue; // if probability is zero, no need to consider it
					} 
					if (isToUseQValues()) {
						if (assetValue <= 0f) {
							throw new ZeroAssetsException("Negative infinite asset detected in clique "+ assetClique +". User = " + getAssetNetwork());
						}
					} else if (Float.isInfinite(assetValue)) {
						throw new ZeroAssetsException("Inconsistent asset detected in clique "+ assetClique +": " + assetValue + ", user = " + getAssetNetwork());
					}
					double value;
					if (isToUseQValues()) {
						value = probValue 
							* (getqToAssetConverter().getScoreFromQValues(assetValue) - getExpectedAssetBasis());
					} else {
						value = probValue * (assetValue - getExpectedAssetBasis());
					}
					ret +=  value;
					this.notifyExpectedAssetCellListener(probClique, assetClique, j, j, value);
				}			
			}
			if (assetSeparatorIterator.hasNext()) {
				// subtracts the product of probabilities and assets of separators 
				Separator assetSeparator = assetSeparatorIterator.next();
				Separator probSeparator = listOfProbSeparator.get(listOfProbSeparator.indexOf(assetSeparator));
				if (probSeparator.getNodes() == null || probSeparator.getNodes().isEmpty()
						|| probSeparator.getProbabilityFunction().getVariablesSize() <= 0 
						|| probSeparator.getProbabilityFunction().tableSize() <= 0) {
					// this is an empty separator, so it has 
					double value;
					if (isToUseQValues()) {
						value = getqToAssetConverter().getScoreFromQValues(getEmptySeparatorsDefaultContent()) - getExpectedAssetBasis();
					} else {
						value = getEmptySeparatorsDefaultContent() - getExpectedAssetBasis();
					}
					ret -= value;
					this.notifyExpectedAssetCellListener(probSeparator, assetSeparator, -1, -1, value);
				}
				for (int i = 0; i < assetSeparator.getProbabilityFunction().tableSize(); i++) {	
					if (probSeparator.getProbabilityFunction().getValue(i) == 0f) {
						continue; // if probability is zero, no need to consider it
					} 
					if (isToUseQValues()) {
						if (assetSeparator.getProbabilityFunction().getValue(i) <= 0f) {
							throw new ZeroAssetsException("Negative infinite asset detected in separator "+ assetSeparator +  ". User = " + getAssetNetwork());
						}
					} else if (Float.isInfinite(assetSeparator.getProbabilityFunction().getValue(i))) {
						throw new ZeroAssetsException("Inconsistent asset detected in separator + " + assetSeparator+  " : " + assetSeparator.getProbabilityFunction().getValue(i) + ", user = " + getAssetNetwork());
					}
					double value ;
					if (isToUseQValues()) {
						value = probSeparator.getProbabilityFunction().getValue(i) 
							* (getqToAssetConverter().getScoreFromQValues(assetSeparator.getProbabilityFunction().getValue(i))  - getExpectedAssetBasis());
					} else {
						value = probSeparator.getProbabilityFunction().getValue(i) 
							* (assetSeparator.getProbabilityFunction().getValue(i)  - getExpectedAssetBasis());
					}
					ret -= value;
					this.notifyExpectedAssetCellListener(probSeparator, assetSeparator, i, i, value);
				}
			}
			if (Double.isInfinite(ret)) {
				throw new ZeroAssetsException("Overflow when calculating expected assets of user " + assetNet);
			}
		}
		
		return ret;
	}
	

	/**
	 * Classes implementing this interface represents listeners
	 * invoked when a cell in clique/separator table is read/handled 
	 * by {@link AssetAwareInferenceAlgorithm#calculateExpectedAssets()}
	 * @author Shou Matsumoto
	 *
	 */
	public interface ExpectedAssetCellMultiplicationListener {
		/**
		 * This method is called by {@link AssetAwareInferenceAlgorithm#calculateExpectedAssets()} when a cell of the clique table is 
		 * treated during calculation of expected assets. This is useful if you want to 
		 * extract detailed components of the operation executed by {@link AssetAwareInferenceAlgorithm#calculateExpectedAssets()}.
		 * @param probCliqueOrSep : clique or separator of the probabilistic network
		 * @param assetCliqueOrSep : clique or separator of the asset network
		 * @param indexInProbTable : index of the prob clique/separator table currently being read
		 * @param indexInAssetTable : index of the asset clique/separator table currently being read
		 * @param value : usually, this is the multiplication of the values in probCliqueOrSep
		 * and assetCliqueOrSep respectively at index indexInProbTable and indexInAssetTable.
		 * @see AssetAwareInferenceAlgorithm#getExpectedAssetCellListeners()
		 * @see AssetAwareInferenceAlgorithm#notifyExpectedAssetCellListener(IRandomVariable, IRandomVariable, int, int, double)
		 */
		void onModification(IRandomVariable probCliqueOrSep, IRandomVariable assetCliqueOrSep, int indexInProbTable, int indexInAssetTable, double value);
	}
	
	/**
	 * This method is called by {@link #calculateExpectedAssets()} when a cell of the clique table is 
	 * treated during calculation of expected assets. This is useful if you want to 
	 * extract detailed components of the operation executed by {@link #calculateExpectedAssets()}.
	 * @param probCliqueOrSep : clique or separator of the probabilistic network
	 * @param assetCliqueOrSep : clique or separator of the asset network
	 * @param indexInProbTable : index of the prob clique/separator table currently being read
	 * @param indexInAssetTable : index of the asset clique/separator table currently being read
	 * @param value : usually, this is the multiplication of the values in probCliqueOrSep
	 * and assetCliqueOrSep respectively at index indexInProbTable and indexInAssetTable.
	 * @see #getExpectedAssetCellListeners()
	 */
	protected void notifyExpectedAssetCellListener(IRandomVariable probCliqueOrSep, IRandomVariable assetCliqueOrSep, int indexInProbTable, int indexInAssetTable, double value) {
		if (getExpectedAssetCellListeners() != null) {
			for (ExpectedAssetCellMultiplicationListener listener : getExpectedAssetCellListeners()) {
				listener.onModification(probCliqueOrSep, assetCliqueOrSep, indexInProbTable, indexInAssetTable, value);
			}
		}
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
			throw new IllegalStateException("getAssetPropagationDelegator() == null. You may be using an incompatible algorithm for asset updating.");
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
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#runMinPropagation(Map)
	 */
	public void runMinPropagation(Map<INode, Integer> conditions) {
		this.getAssetPropagationDelegator().runMinPropagation(conditions);
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

	/**
	 * Delegates to {@link #getAssetPropagationDelegator()}
	 */
	public boolean isToUpdateOnlyEditClique() {
		return this.getAssetPropagationDelegator().isToUpdateOnlyEditClique();
	}

	/**
	 * Delegates to {@link #getAssetPropagationDelegator()}
	 */
	public void setToUpdateOnlyEditClique(boolean isToUpdateOnlyEditClique) {
		this.getAssetPropagationDelegator().setToUpdateOnlyEditClique(isToUpdateOnlyEditClique);
	}
	
	/**
	 * @param isToNormalizeDisconnectedNets the isToNormalizeDisconnectedNets to set
	 */
	public void setToNormalizeDisconnectedNets(
			boolean isToNormalizeDisconnectedNets) {
		this.isToNormalizeDisconnectedNets = isToNormalizeDisconnectedNets;
	}
	/**
	 * @return the isToNormalizeDisconnectedNets
	 */
	public boolean isToNormalizeDisconnectedNets() {
		return isToNormalizeDisconnectedNets;
	}

	
	/**
	 * delegates to {@link #clone(boolean)}
	 * @see #clone(boolean)
	 */
	public IInferenceAlgorithm clone()  throws CloneNotSupportedException  {
		return this.clone(true);
	}
	
	/**
	 * This method is used in {@link #clone()} for copying the probabilistic network
	 * related to this algorithm.
	 * You may call this method instead of {@link #clone()} if
	 * your objective is to clone only {@link #getRelatedProbabilisticNetwork()}.
	 * @param originalProbabilisticNetwork : network to clone
	 * @return the clone of the originalProbabilisticNetwork
	 */
	public ProbabilisticNetwork cloneProbabilisticNetwork(ProbabilisticNetwork originalProbabilisticNetwork) {
		return ((JunctionTreeAlgorithm)this.getProbabilityPropagationDelegator()).cloneProbabilisticNetwork(originalProbabilisticNetwork);
	}
	
	/**
	 * This is used in {@link #clone()} to create a new instance of this class.
	 * @param probAlgorithm
	 * @return
	 * @see #getInstance(IInferenceAlgorithm)
	 */
	protected IInferenceAlgorithm newInstance(IInferenceAlgorithm probAlgorithm) {
		return getInstance(probAlgorithm);
	}
	
	/**
	 * This method will create a copy of itself, 
	 * a copy of the probabilistic network,
	 * and a copy of the asset network.
	 * This method may be useful for previewing the 
	 * result of edits without modifying the original
	 * probabilistic network and asset network.
	 * CAUTION: this method only copies attributes relevant to the inference related to 
	 * junction tree algorithm and asset update.
	 * @param isToCloneAssets : if false, {@link #getAssetNetwork()} will not be cloned.
	 * @return a copy of probabilisticNetwork in the context of asset calculation.
	 */
	public IInferenceAlgorithm clone(boolean isToCloneAssets)  throws CloneNotSupportedException  {
		
		// clone Bayes network
		ProbabilisticNetwork newNet = this.cloneProbabilisticNetwork(getRelatedProbabilisticNetwork());
		
		// clone JT algorithm using the cloned BN
		JunctionTreeAlgorithm jtAlgorithm = new JunctionTreeAlgorithm(newNet);
		if (getProbabilityPropagationDelegator() instanceof JunctionTreeAlgorithm) {
			// reuse same likelihood extractor
			JunctionTreeAlgorithm origJTAlgorithm = (JunctionTreeAlgorithm) getProbabilityPropagationDelegator();
			jtAlgorithm.setLikelihoodExtractor(origJTAlgorithm.getLikelihoodExtractor());
		} else {
			// unknown type of BN inference algorithm...
			Debug.println(getClass(), "Unknown type of BN inference algorithm. Cannot extract likelihood extractor. Using default: JeffreyRuleLikelihoodExtractor.");
			jtAlgorithm.setLikelihoodExtractor(DEFAULT_JEFFREYRULE_LIKELIHOOD_EXTRACTOR);
		}
		
		// clone this algorithm
		AssetAwareInferenceAlgorithm ret = (AssetAwareInferenceAlgorithm) getInstance(jtAlgorithm);
		// copy settings
		ret.setDefaultInitialAssetTableValue(getDefaultInitialAssetTableValue());
		ret.setToAllowZeroAssets(this.isToAllowZeroAssets());
		ret.setToCalculateMarginalsOfAssetNodes(this.isToCalculateMarginalsOfAssetNodes());
		ret.setToLogAssets(this.isToLogAssets());
		ret.setToNormalizeDisconnectedNets(this.isToNormalizeDisconnectedNets());
		ret.setToPropagateForGlobalConsistency(this.isToPropagateForGlobalConsistency());
		ret.setToUpdateAssets(this.isToUpdateAssets());
		ret.setToUpdateOnlyEditClique(this.isToUpdateOnlyEditClique());
		ret.setToUpdateSeparators(this.isToUpdateSeparators());
		ret.setToUseQValues(this.isToUseQValues());
		ret.setToCalculateLPE(this.isToCalculateLPE());
		ret.setExpectedAssetPivot(this.getExpectedAssetBasis());
		
		// copy current converter which converts q values to assets and vice-versa
		ret.setqToAssetConverter(this.getqToAssetConverter());
		
		// return now if we do not need to clone assets
		if (!isToCloneAssets) {
			ret.setAssetNetwork(null);
			return ret;
		}
		
		// clone asset net. createAssetNetFromProbabilisticNet is supposed to create correct structure of network and JT
		AssetNetwork newAssetNet = null;
		try {
			newAssetNet = ret.createAssetNetFromProbabilisticNet(newNet);
		} catch (InvalidParentException e) {
			throw new RuntimeException("Could not clone asset network of user " + getAssetNetwork(), e);
		}
		
		// mapping between original cliques to copied cliques.
		Map<Clique, Clique> oldCliqueToNewCliqueMap = new HashMap<Clique, Clique>();
		
		if (getAssetNetwork().getJunctionTree() != null && getAssetNetwork().getJunctionTree().getCliques() != null) {
			// copy asset tables of cliques. Since cliques are stored in a list, the ordering of the copied cliques is supposedly the same
			for (int i = 0; i < getAssetNetwork().getJunctionTree().getCliques().size(); i++) {
				(newAssetNet.getJunctionTree().getCliques().get(i).getProbabilityFunction()).setValues(
						(getAssetNetwork().getJunctionTree().getCliques().get(i).getProbabilityFunction()).getValues()
				);
				// store asset cliques in the map, so that we can use it for the separators later
				oldCliqueToNewCliqueMap.put(getAssetNetwork().getJunctionTree().getCliques().get(i), newAssetNet.getJunctionTree().getCliques().get(i));
			}
		}
		
		if (getAssetNetwork().getJunctionTree() != null && getAssetNetwork().getJunctionTree().getSeparators() != null) {
			// copy asset tables of separators. Use oldCliqueToNewCliqueMap, because separators are not stored in a list
			for (Separator oldSep : getAssetNetwork().getJunctionTree().getSeparators()) {
				Separator newSep = newAssetNet.getJunctionTree().getSeparator(
						oldCliqueToNewCliqueMap.get(oldSep.getClique1()),
						oldCliqueToNewCliqueMap.get(oldSep.getClique2())
				);
				if (newSep == null) {
					throw new RuntimeException("Could not access copy of separator " + oldSep + " correctly while copying asset tables of separators.");
				}
				// tables are supposedly with same size
				(newSep.getProbabilityFunction()).setValues((oldSep.getProbabilityFunction()).getValues());
			}
		}
		
		// link algorithm to the new asset net
		ret.setAssetNetwork(newAssetNet);
		
		return ret;
	}
	
	/**
	 * Delegates to {@link #getProbabilityPropagationDelegator()}
	 * if it is an instance of {@link JunctionTreeAlgorithm}.
	 * @return the joint probability
	 */
	public float getJointProbability(Map<ProbabilisticNode,Integer> nodesAndStatesToConsider) {
		if (getProbabilityPropagationDelegator() instanceof JunctionTreeAlgorithm) {
			return ((JunctionTreeAlgorithm) getProbabilityPropagationDelegator()).getJointProbability(nodesAndStatesToConsider);
		}
		throw new UnsupportedOperationException("#getProbabilityPropagationDelegator() is not a JunctionTreeAlgorithm. This method does not support calculation of joint probability in algorithms which are not based on junction tree.");
	}

	/**
	 * If false, {@link #propagate()} will throw a {@link ZeroAssetsException}
	 * when q-values gets less than or equals to 1 (i.e. when the respective
	 * assets - log values - goes to 0 or negative).
	 * @param isToAllowQValuesSmallerThan1 the isToAllowQValuesSmallerThan1 to set
	 */
	public void setToAllowZeroAssets(boolean isToAllowQValuesSmallerThan1) {
		IAssetNetAlgorithm assetAlgorithm = this.getAssetPropagationDelegator();
		if (assetAlgorithm != null) {
			assetAlgorithm.setToAllowZeroAssets(isToAllowQValuesSmallerThan1);
		}
	}

	/**
	 * If false, {@link #propagate()} will throw a {@link ZeroAssetsException}
	 * when q-values gets less than or equals to 1 (i.e. when the respective
	 * assets - log values - goes to 0 or negative).
	 * @return the isToAllowQValuesSmallerThan1
	 */
	public boolean isToAllowZeroAssets() {
		IAssetNetAlgorithm assetAlgorithm = this.getAssetPropagationDelegator();
		if (assetAlgorithm != null) {
			return assetAlgorithm.isToAllowZeroAssets();
		}
		return true;	// default value
	}
	
	/**
	 * This is equivalent to {@link #setAsPermanentEvidence(Map, boolean)}
	 * with the map being {@link Collections#singletonMap(Object, Object)}
	 * @param node : node to add hard evidence
	 * @param state : state of hard evidence
	 * @param isToDeleteNode : if true, the node will be deleted from the network.
	 */
	public void setAsPermanentEvidence(INode node, Integer state, boolean isToDeleteNode){
		this.setAsPermanentEvidence(Collections.singletonMap(node, state), isToDeleteNode);
	}
	
	/**
	 * This method will add evidences to nodes, propagate evidence in the probabilistic network, and
	 * then delegate to {@link #getAssetPropagationDelegator()}
	 */
	public void setAsPermanentEvidence(Map<INode, Integer> evidences, boolean isToDeleteNode){
		// initial assertion
		if (evidences == null || evidences.isEmpty()) {
			return;
		}
		// fill the findings
		for (INode node : evidences.keySet()) {
			ProbabilisticNode probNode = (ProbabilisticNode) getRelatedProbabilisticNetwork().getNode(node.getName());
			// ignore nodes which we did not find
			if (probNode == null) {
				throw new IllegalArgumentException("Probabilistic node " + node + " was not found in probabilistic network.");
			} 
			// extract state of evidence
			Integer state = evidences.get(node);
			if (state == null) {
				Debug.println(getClass(), "Evidence of node " + node + " was null");
				// ignore this value
				continue;
			}
			// set and propagate evidence in the probabilistic network
			if (state < 0) {
				// set finding as negative (i.e. finding setting a state to 0%)
				probNode.addFinding(Math.abs(state+1), true);
			} else {
				probNode.addFinding(state);
			}
		}
		
		// propagate only the probabilities
		getProbabilityPropagationDelegator().propagate();
		
		// delete resolved nodes
		if (isToDeleteNode) {
			for (INode node : evidences.keySet()) {
				getRelatedProbabilisticNetwork().removeNode((Node) node); // this will supposedly delete the nodes from cliques as well
			}
			// special treatment if the node to remove makes the clique to become empty
			for (Clique clique : getRelatedProbabilisticNetwork().getJunctionTree().getCliques()) {
				// TODO update only the important clique
				if (clique.getProbabilityFunction().tableSize() <= 0) {
					clique.getProbabilityFunction().addVariable(ONE_STATE_PROBNODE);
				}
			}
		}
		if (isToUpdateAssets()) {
			this.getAssetPropagationDelegator().setAsPermanentEvidence(evidences, isToDeleteNode);
		}
		if (!isToDeleteNode) {
			// copy all clique/separator potentials
			for (Clique clique : getRelatedProbabilisticNetwork().getJunctionTree().getCliques()) {
				clique.getProbabilityFunction().copyData();
			}
			for (Separator sep : getRelatedProbabilisticNetwork().getJunctionTree().getSeparators()) {
				sep.getProbabilityFunction().copyData();
			}
			if (getPermanentEvidenceMap() == null) {
				setPermanentEvidenceMap(new HashMap<TreeVariable, Integer>());
			}
			getPermanentEvidenceMap().putAll((Map)evidences);
		}
	}

//	/**
//	 * This method only delegates to
//	 * {@link AssetPropagationInferenceAlgorithm#getAssetTablesBeforeLastPropagation()}
//	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#getAssetTablesBeforeLastPropagation()
//	 */
//	public Map<IRandomVariable, DoublePrecisionProbabilisticTable> getAssetTablesBeforeLastPropagation() {
//		IAssetNetAlgorithm delegator = getAssetPropagationDelegator();
//		if (delegator != null) {
//			return delegator.getAssetTablesBeforeLastPropagation();
//		}
//		return null;
//	}

	/**
	 * Only delegates to {@link #getAssetPropagationDelegator()}
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#setToCalculateMarginalsOfAssetNodes(boolean)
	 */
	public void setToCalculateMarginalsOfAssetNodes(
			boolean isToCalculateMarginalsOfAssetNodes) {
		getAssetPropagationDelegator().setToCalculateMarginalsOfAssetNodes(isToCalculateMarginalsOfAssetNodes);
	}

	/**
	 * This method only delegates to {@link #getAssetPropagationDelegator()}
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#setqToAssetConverter(unbbayes.prs.bn.inference.extension.IQValuesToAssetsConverter)
	 */
	public void setqToAssetConverter(IQValuesToAssetsConverter qToAssetConverter) {
		if (getAssetPropagationDelegator() != null) {
			getAssetPropagationDelegator().setqToAssetConverter(qToAssetConverter);
		}
	}

	/** 
	 * This method only delegates to {@link #getAssetPropagationDelegator()}
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#getqToAssetConverter()
	 */
	public IQValuesToAssetsConverter getqToAssetConverter() {
		if (getAssetPropagationDelegator() == null) {
			return null;
		}
		return getAssetPropagationDelegator().getqToAssetConverter();
	}

	/**
	 * {@link ExpectedAssetCellMultiplicationListener#onModification(IRandomVariable, IRandomVariable, int, double)}
	 * will be invoked on all elements in this list when {@link #notifyExpectedAssetCellListener(IRandomVariable, IRandomVariable, int, double)}
	 * is invoked. 
	 * {@link #notifyExpectedAssetCellListener(IRandomVariable, IRandomVariable, int, double)}
	 * is, by default, invoked for each cell treated in {@link #calculateExpectedAssets()}.
	 * @param expectedAssetCellListeners the expectedAssetCellListeners to set
	 */
	public void setExpectedAssetCellListeners(
			List<ExpectedAssetCellMultiplicationListener> expectedAssetCellListeners) {
		this.expectedAssetCellListeners = expectedAssetCellListeners;
	}

	/**
	 * {@link ExpectedAssetCellMultiplicationListener#onModification(IRandomVariable, IRandomVariable, int, double)}
	 * will be invoked on all elements in this list when {@link #notifyExpectedAssetCellListener(IRandomVariable, IRandomVariable, int, double)}
	 * is invoked. 
	 * {@link #notifyExpectedAssetCellListener(IRandomVariable, IRandomVariable, int, double)}
	 * is, by default, invoked for each cell treated in {@link #calculateExpectedAssets()}.
	 * @return the expectedAssetCellListeners
	 */
	public List<ExpectedAssetCellMultiplicationListener> getExpectedAssetCellListeners() {
		return expectedAssetCellListeners;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#isToCalculateMarginalsOfAssetNodes()
	 */
	public boolean isToCalculateMarginalsOfAssetNodes() {
		return getAssetPropagationDelegator().isToCalculateMarginalsOfAssetNodes();
	}

	/**
	 * Just delegates to {@link #getAssetPropagationDelegator()}.
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#getEmptySeparatorsDefaultContent()
	 * @see #calculateExpectedAssets()
	 */
	public float getEmptySeparatorsDefaultContent() {
		return getAssetPropagationDelegator().getEmptySeparatorsDefaultContent();
	}

	/**
	 * Just delegates to {@link #getAssetPropagationDelegator()}
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#setEmptySeparatorsDefaultContent(double)
	 * @see #calculateExpectedAssets()
	 */
	public void setEmptySeparatorsDefaultContent(float emptySeparatorsContent) {
		getAssetPropagationDelegator().setEmptySeparatorsDefaultContent(emptySeparatorsContent);
	}

	/**
	 * Adds assets to the q tables.
	 * @param delta : assets to add. Caution: this is assets, not q-values.
	 * @see #getqToAssetConverter()
	 * @see #getAssetNetwork()
	 * @see #setEmptySeparatorsDefaultContent(double)
	 */
	public void addAssets(float delta) {
		/*
		 * If we want to add something into a logarithm value, we must multiply some ratio.
		 * Assuming that asset = b logX (q)  (note: X is some base), then X^(asset/b) = q
		 * So, X^((asset+delta)/b) = X^(asset/b + delta/b) = X^(asset/b) * X^(delta/b)  = q * X^(delta/b)
		 * So, we need to multiply X^(delta/b) in order to update q when we add delta into asset.
		 */
		float ratio = (float) Math.pow(getqToAssetConverter().getCurrentLogBase(), delta / getqToAssetConverter().getCurrentCurrencyConstant() );
		// add delta to all cells in asset tables of cliques
		for (Clique clique : this.getAssetNetwork().getJunctionTree().getCliques()) {
			PotentialTable assetTable = clique.getProbabilityFunction();
			for (int i = 0; i < assetTable.tableSize(); i++) {
				if (isToUseQValues()) {
					if (assetTable.getValue(i) != Float.POSITIVE_INFINITY && Float.isInfinite(assetTable.getValue(i) * ratio)) {
						throw new ZeroAssetsException("Overflow detected when adding " + delta + " to min assets of user " + getAssetNetwork() + ". The asset table's cell had value " + assetTable.getValue(i));
					}
					assetTable.setValue(i, (assetTable.getValue(i) * ratio) );
				} else {
					assetTable.setValue(i, (assetTable.getValue(i) + delta) );
				}
			}
		}
		// add delta to all cells in asset tables of separators
		for (Separator separator : this.getAssetNetwork().getJunctionTree().getSeparators()) {
			PotentialTable assetTable = separator.getProbabilityFunction();
			for (int i = 0; i < assetTable.tableSize(); i++) {
				if (isToUseQValues()) {
					if (assetTable.getValue(i) != Float.POSITIVE_INFINITY && Float.isInfinite(assetTable.getValue(i) * ratio)) {
						throw new ZeroAssetsException("Overflow detected when adding " + delta + " to min assets of user " + getAssetNetwork());
					}
					assetTable.setValue(i, (assetTable.getValue(i) * ratio));
				} else {
					assetTable.setValue(i, (assetTable.getValue(i) + delta));
				}
			}
		}
		// add delta to empty separators (but since all empty separators supposedly have the same assets, it is stored in only 1 place)
		if (isToUseQValues()) {
			if (Float.isInfinite(this.getEmptySeparatorsDefaultContent() * ratio)) {
				throw new ZeroAssetsException("Overflow detected when adding " + delta + " to min assets of user " + getAssetNetwork());
			}
			this.setEmptySeparatorsDefaultContent((this.getEmptySeparatorsDefaultContent()*ratio));
		} else {
			this.setEmptySeparatorsDefaultContent((this.getEmptySeparatorsDefaultContent() + delta));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#setToUseQValues(boolean)
	 */
	public void setToUseQValues(boolean isToUseQValues) {
		this.isToUseQValues = isToUseQValues;
		if (this.getAssetPropagationDelegator() != null) {
			this.getAssetPropagationDelegator().setToUseQValues(isToUseQValues);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#isToUseQValues()
	 */
	public boolean isToUseQValues() {
		if (this.getAssetPropagationDelegator() != null) {
			return this.getAssetPropagationDelegator().isToUseQValues();
		}
		return isToUseQValues;
	}

	/**
	 * Only delegates to {@link #getAssetPropagationDelegator()}
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#isToCalculateLPE()
	 */
	public boolean isToCalculateLPE() {
		if (getAssetPropagationDelegator() != null) {
			return this.getAssetPropagationDelegator().isToCalculateLPE();
		}
		return isToCalculateLPE;
	}

	/**
	 * Only delegates to {@link #getAssetPropagationDelegator()}
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#setToCalculateLPE(boolean)
	 */
	public void setToCalculateLPE(boolean isToCalculateLPE) {
		this.isToCalculateLPE  = isToCalculateLPE;
		if (this.getAssetPropagationDelegator() != null) {
			this.getAssetPropagationDelegator().setToCalculateLPE(isToCalculateLPE);
		}
	}

	/**
	 * Just delegates to {@link #getAssetPropagationDelegator()}.
	 * May return null if {@link #getAssetPropagationDelegator()} == null
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#getMemento()
	 */
	public IAssetNetAlgorithmMemento getMemento() {
		if (this.getAssetPropagationDelegator() == null) {
			return null;
		}
		return this.getAssetPropagationDelegator().getMemento();
	}

	/**
	 * Just delegates to {@link #getAssetPropagationDelegator()}
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#setMemento(unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm.IAssetNetAlgorithmMemento)
	 * @throws NullPointerException : if {@link #getAssetPropagationDelegator()} == null
	 */
	public void setMemento(IAssetNetAlgorithmMemento memento) throws NoSuchFieldException {
		if (this.getAssetPropagationDelegator() == null) {
			throw new NoSuchFieldException("this.getAssetPropagationDelegator() == null");
		}
		this.getAssetPropagationDelegator().setMemento(memento);
	}

	/**
	 * @param expectedAssetPivot the expectedAssetPivot to set
	 */
	public void setExpectedAssetPivot(float expectedAssetPivot) {
		this.expectedAssetPivot = expectedAssetPivot;
	}

	/**
	 * @return the expectedAssetPivot
	 */
	public float getExpectedAssetBasis() {
		return expectedAssetPivot;
	}

	/**
	 * If true, evidences will be forced to be removed from system
	 * before {@link #run()}
	 * @param isToResetEvidenceBeforeRun the isToResetEvidenceBeforeRun to set
	 */
	public void setToResetEvidenceBeforeRun(boolean isToResetEvidenceBeforeRun) {
		this.isToResetEvidenceBeforeRun = isToResetEvidenceBeforeRun;
	}

	/** 
	 * If true, evidences will be forced to be removed from system
	 * before {@link #run()}
	 * @return the isToResetEvidenceBeforeRun
	 */
	public boolean isToResetEvidenceBeforeRun() {
		return isToResetEvidenceBeforeRun;
	}

	/**
	 * This map stores nodes which were marked as findings by {@link #setPermanentEvidenceMap(Map)}.
	 * @param permanentEvidenceMap the permanentEvidenceMap to set
	 */
	protected void setPermanentEvidenceMap(Map<TreeVariable, Integer> permanentEvidenceMap) {
		this.permanentEvidenceMap = permanentEvidenceMap;
	}

	/**
	 * This map stores nodes which were marked as findings by {@link #setPermanentEvidenceMap(Map)}.
	 * @return the permanentEvidenceMap
	 */
	protected Map<TreeVariable, Integer> getPermanentEvidenceMap() {
		return permanentEvidenceMap;
	}

	/**
	 * Calculates the marginal probability of a node from a clique table.
	 * This method supposedly performs the same of
	 * {@link ProbabilisticNode#updateMarginal()} and returns the marginals
	 * as a list of float, but it uses an arbitrary clique table instead
	 * of the clique table of {@link ProbabilisticNode#getAssociatedClique()}.
	 * @param mainNode : node to obtain marginal
	 * @param cliqueTable : clique table from which the marginal probability will be calculated
	 * @param evidences : those values will be considered as hard evidences in the clique.
	 * @return : non-null list of the marginal probabilities. The ordering is important, because
	 * element 0 will represent the marginal probability of state 0 of mainNode, the element 1
	 * will represent the state 1 of mainNode, and so on.
	 */
	public List<Float> getMarginalFromPotentialTable( ProbabilisticNode mainNode, PotentialTable cliqueTable, Map<INode, Integer> evidences) {
		List<Float> ret = new ArrayList<Float>();
		
		// use a clone, because we will sum out some variables (i.e. modify content of the table)
		PotentialTable table = (PotentialTable) cliqueTable.clone();
		
		// consider the hard evidences specified in the map "evidences"
		if (evidences != null && !evidences.isEmpty()) {
			// set all impossible states (complement of the states specified in evidences) to zero
			for (int i = 0; i < table.tableSize(); i++) {
				// convert i into a vetor which indicates what node is in which state at cell i.
				int[] multidimensionalCoord = table.getMultidimensionalCoord(i);
				// if states of multidimensionalCoord dont match evidences, the cell shall be filled with zero
				boolean isToSetToZero = false;
				for (INode evidenceNode : evidences.keySet()) {
					int indexOfEvidenceNode = table.indexOfVariable((Node) evidenceNode);
					if (indexOfEvidenceNode < 0) {
						continue;	// ignore evidence nodes which are not in the table
					}
					if (multidimensionalCoord[indexOfEvidenceNode ] != evidences.get(evidenceNode)) {
						isToSetToZero = true;
						break;
					}
				}
				if (isToSetToZero) {
					table.setValue(i, 0f);
				}
			}
			
			// normalize table, because the sum is not 1 anymore (because we set some cells to zero)
			table.normalize();
		}
		
		// the index of main node in the clique table
		int indexOfMainNode = table.indexOfVariable(mainNode);
		if (indexOfMainNode < 0) {
			// cannot calculate marginal if mainNode is not in the table
			Debug.println(getClass(), mainNode + " is not in the provided clique table.");
			return ret;
		}
		
		// sum out all nodes except for mainNode
        int nodeCount = table.variableCount();
		for (int i = 0; i < nodeCount ; i++) {
            if (i != indexOfMainNode) {
            	table.removeVariable(cliqueTable.getVariableAt(i));
            }
        }

		// obtain the size of the table and assert that all nodes were summed out
        int tableSize = table.tableSize();
        if (tableSize != mainNode.getStatesSize()) {
        	// not all nodes were summed out
        	Debug.println(getClass(), "Failed to sum out all nodes except " + mainNode);
        	return ret;
        }
        
        // update ret
        for (int i = 0; i < tableSize; i++) {
            ret.add(table.getValue(i));
        }
		
		return ret;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * If true, {@link #run()} will attempt to change the GUI (e.g. add extra panel).
	 * @param isToChangeGUI the isToChangeGUI to set
	 */
	public void setToChangeGUI(boolean isToChangeGUI) {
		this.isToChangeGUI = isToChangeGUI;
	}

	/**
	 * If true, {@link #run()} will attempt to change the GUI (e.g. add extra panel).
	 * @return the isToChangeGUI
	 */
	public boolean isToChangeGUI() {
		return isToChangeGUI;
	}

	/**
	 * Only delegates to {@link #getProbabilityPropagationDelegator()}
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#initInternalIdentificators()
	 */
	public void initInternalIdentificators() {
		if (getProbabilityPropagationDelegator() instanceof IRandomVariableAwareInferenceAlgorithm) {
			((IRandomVariableAwareInferenceAlgorithm) getProbabilityPropagationDelegator()).initInternalIdentificators();
		}
	}


	
	
}
