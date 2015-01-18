/**
 * 
 */
package unbbayes.prs.bn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JPanel;

import unbbayes.controller.INetworkMediator;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor;
import unbbayes.prs.bn.cpt.impl.NormalizeTableFunction;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.id.DecisionNode;
import unbbayes.util.Debug;
import unbbayes.util.SetToolkit;
import unbbayes.util.dseparation.impl.MSeparationUtility;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener;
import unbbayes.util.extension.bn.inference.IPermanentEvidenceInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.IRandomVariableAwareInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel;

/**
 * Class for junction tree compiling algorithm.
 * It includes basic consistency check for single entity networks.
 * 
 * By now, this is still a wrapper for {@link ProbabilisticNetwork#compile()}.
 * 
 * TODO gradually migrate attributes in {@link SingleEntityNetwork} and {@link ProbabilisticNetwork} related to compilation routine to this class.
 * 
 * @author Shou Matsumoto
 *
 */
public class JunctionTreeAlgorithm implements IRandomVariableAwareInferenceAlgorithm, IPermanentEvidenceInferenceAlgorithm {
	
	private static ResourceBundle generalResource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.controller.resources.ControllerResources.class.getName(),
			Locale.getDefault(),
			JunctionTreeAlgorithm.class.getClassLoader());
	
	private ProbabilisticNetwork net;
	
	private InferenceAlgorithmOptionPanel optionPanel;
	
	/** This is the default instance of a builder for instantiating new {@link JunctionTree} */
	public static final IJunctionTreeBuilder DEFAULT_JUNCTION_TREE_BUILDER = new DefaultJunctionTreeBuilder();
	
	private IJunctionTreeBuilder junctionTreeBuilder = DEFAULT_JUNCTION_TREE_BUILDER;

	/** Load resource file from util */
  	private static ResourceBundle utilResource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.util.resources.UtilResources.class.getName());
	

  	/** Load resource file from this package */
  	protected static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.prs.bn.resources.BnResources.class.getName());

	private List<IJunctionTreeCommand> verifyConsistencyCommandList;
  	
	private List<INode> sortedDecisionNodes = new ArrayList<INode>(0);

//	private List<Edge> markovArc = new ArrayList<Edge>(0);
//
//	private List<Edge> markovArcCpy = new ArrayList<Edge>(0);

	private List<IInferenceAlgorithmListener> inferenceAlgorithmListeners = new ArrayList<IInferenceAlgorithmListener>(0);

	private String virtualNodePrefix = "V_";

	private Map<INode,Collection<IRandomVariable>> virtualNodesToCliquesAndSeparatorsMap = new HashMap<INode,Collection<IRandomVariable>>();

	private float virtualNodePositionRandomness = 400;
  	
	/** Default value of {@link #getLikelihoodExtractor()} */
	public static final ILikelihoodExtractor DEFAULT_LIKELIHOOD_EXTRACTOR = LikelihoodExtractor.newInstance();
	
	private ILikelihoodExtractor likelihoodExtractor = DEFAULT_LIKELIHOOD_EXTRACTOR;

	private boolean isToCalculateJointProbabilityLocally = true;

	private boolean isToUseEstimatedTotalProbability = true;
	
	private boolean isToConnectParentsWhenAbsorbingNode = true;
	
	/** This is a default instance of {@link #getMSeparationUtility()} */
	public static final MSeparationUtility DEFAULT_MSEPARATION_UTILITY = MSeparationUtility.newInstance();
	
	private MSeparationUtility mseparationUtility = DEFAULT_MSEPARATION_UTILITY;
	

	private boolean isToDeleteEmptyCliques = false;
	


//	/** Set of nodes detected when {@link #run()} was executed the previous time */
//	private Collection<INode> nodesPreviousRun = new HashSet<INode>();
//	
//	/** Set of arcs (edges) detected when {@link #run()} was executed the previous time */
//	private Collection<Edge> edgesPreviousRun  = new HashSet<Edge>();
	
	
	/** This is the error margin used when comparing probabilities */
	public static final float ERROR_MARGIN = 0.00005f;
	

	/** This is a default instance of a node with only 1 state. Use this instance if you want to have nodes with 1 state not to occupy too much space in memory */
	public static final ProbabilisticNode ONE_STATE_PROBNODE = new ProbabilisticNode() {
		private static final long serialVersionUID = -433609923386089166L;
		
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
	
	/**
	 * This listener can be used if you want the junction tree algorithm not to reset the clique potentials before each propagation.
	 * This listener also clears all virtual nodes automatically at the end of the propagation.
	 * @see #DEFAULT_INFERENCE_ALGORITHM_LISTENER
	 * @see #clearVirtualNodes()
	 */
	public static final IInferenceAlgorithmListener CLEAR_VIRTUAL_NODES_ALGORITHM_LISTENER = new IInferenceAlgorithmListener() {
		

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
										evidenceNodes.addAll(jt.getLikelihoodExtractor().extractLikelihoodParents(algorithm.getNetwork(), node));
										// create the virtual node
										jt.addVirtualNode(algorithm.getNetwork(), evidenceNodes);
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
			} 
		}
		
	};

	/**
	 * This is the default inference algorithm listener.
	 * Note that this listener resets the clique potentials before propagation, so that hard evidences
	 * can overwrite conflicting hard evidences which was added previously, so it is not
	 * suitable for absorbing virtual evidence.
	 * @see #CLEAR_VIRTUAL_NODES_ALGORITHM_LISTENER
	 */
	public static final IInferenceAlgorithmListener DEFAULT_INFERENCE_ALGORITHM_LISTENER = new IInferenceAlgorithmListener() {
//			private Map<String, Float[]> likelihoodMap;

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
			 * Store findings and reset network before propagation, so that we can
			 * overwrite previous findings if we add new findings to nodes
			 * already having findings.
			 */
			public void onBeforePropagate(IInferenceAlgorithm algorithm) {
				// The change bellow is to adhere to feature request #3314855
				// Save the list of evidence entered
				Map<String, Integer> evidenceMap = new HashMap<String, Integer>();
				Set<String> negativeEvidenceNodeNames = new HashSet<String>();	// name of nodes in evidenceMap whose evidences are negative (indicate evidence NOT in a given state)
				Map<String, float[]> likelihoodMap = new HashMap<String, float[]>();	// backup plan for cases when we could not create virtual nodes
				
				if ((algorithm.getNetwork() != null) && ( algorithm.getNetwork() instanceof SingleEntityNetwork)) {
					SingleEntityNetwork net = (SingleEntityNetwork)algorithm.getNetwork();
					for (Node n : new ArrayList<Node>(net.getNodes())) {	// iterate in a new list, because net.getNodes may suffer concurrent changes because of virtual nodes.
						if (n instanceof TreeVariable) {
							TreeVariable node = (TreeVariable)n;
							if (node.hasEvidence()) {
								if (node.hasLikelihood()) {
//									Float[] likelihood = new Float[node.getStatesSize()];
//									for (int i = 0; i < node.getStatesSize(); i++) {
//										likelihood[i] = node.getMarginalAt(i);
//									}
//									likelihoodMap.put(node.getName(), likelihood);

//									Float[] likelihood = new Float[node.getLikelihood().length];
//									for (int i = 0; i < likelihood.length; i++) {
//										likelihood[i] = node.getLikelihood()[i];
//									}
//									likelihoodMap.put(node.getName(), likelihood);
									// the following code was added here because we need current marginal to calculate soft evidence
									if (algorithm instanceof JunctionTreeAlgorithm) {
										JunctionTreeAlgorithm jt = (JunctionTreeAlgorithm) algorithm;
										// Enter the likelihood as virtual nodes
										try {
											// prepare list of nodes to add soft/likelihood evidence
											List<INode> evidenceNodes = new ArrayList<INode>();
											evidenceNodes.add(node);	// the main node is the one carrying the likelihood ratio
											// if conditional soft evidence, add all condition nodes (if non-conditional, then this will add an empty list)
											evidenceNodes.addAll(jt.getLikelihoodExtractor().extractLikelihoodParents(algorithm.getNetwork(), node));
											// create the virtual node
											INode virtual = null;
											try {
												virtual = jt.addVirtualNode(algorithm.getNetwork(), evidenceNodes);
												if (virtual != null) {
													// store the hard evidence of the new virtual node, so that it can be retrieved after reset
													// hard evidence of virtual node is never a "NOT" evidence (evidence is always about a given particular state, and never about values "NOT" in a given state)
													evidenceMap.put(virtual.getName(), ((TreeVariable) virtual).getEvidence());
												}
											} catch (Exception e) {
												Debug.println(getClass(), "Could not create virtual node for " + node, e);
												// backup plan: use old routine (although it is not entirely correct)
												// backup the likelihood values
												likelihoodMap.put(node.getName(), node.getLikelihood());
												// putting in evidenceMap will mark this node as evidence (no matter what kind of evidence it is actually)
												evidenceMap.put(node.getName(), 0);	
											}
										} catch (Exception e) {
											throw new RuntimeException(e);
										}
									}
								} else {
									// store hard evidence, so that it can be retrieved after reset
									int evidenceIndex = node.getEvidence();
									if (node.getMarginalAt(evidenceIndex) == 0) {
										// this is a "NOT" evidence (evidence about "NOT" in a given state)
										negativeEvidenceNodeNames.add(node.getName());
									}
									evidenceMap.put(node.getName(), evidenceIndex);
								}
							}
						}
					}
					// Reset evidence in order to allow changes in node which already had a different evidence set
					algorithm.reset();
					// Enter the list of evidence again
					for (String name : evidenceMap.keySet()) {
						// if name is in negativeEvidenceNodeNames, add as negative finding. Add as normal finding otherwise
						((TreeVariable)net.getNode(name)).addFinding(evidenceMap.get(name), negativeEvidenceNodeNames.contains(name));

						if (likelihoodMap.containsKey(name)) {
							// if name is in likelihoodMap, this was a likelihood/soft evidence with no virtual node (the virtual node has failed)
							// so, use old routine for likelihood evidence
							((TreeVariable)net.getNode(name)).setMarginalProbabilities(likelihoodMap.get(name));
						}
					}
					
					// the following code is not necessary anymore, because likelihood evidences are now virtual nodes and hard evidences 
					// (so, we only need to store hard evidences)
//					// Enter the likelihood as virtual nodes
//					if (algorithm instanceof JunctionTreeAlgorithm) {
//						JunctionTreeAlgorithm jt = (JunctionTreeAlgorithm) algorithm;
//						for (String name : likelihoodMap.keySet()) {
//							float[] likelihood = new float[likelihoodMap.get(name).length];
//							for (int i = 0; i < likelihood.length; i++) {
//								likelihood[i] = likelihoodMap.get(name)[i];
//							}
//							TreeVariable var = ((TreeVariable)net.getNode(name));
//							try {
//								// restore likelihood
//								var.addLikeliHood(likelihood);
//								// prepare list of nodes to add soft/likelihood evidence
//								List<INode> evidenceNodes = new ArrayList<INode>();
//								evidenceNodes.add(var);	// the main node is the one carrying the likelihood ratio
//								// if conditional soft evidence, add all condition nodes (if non-conditional, then this will add an empty list)
//								evidenceNodes.addAll(jt.getLikelihoodExtractor().extractLikelihoodParents(getNetwork(), var));
//								// create the virtual node
//								jt.addVirtualNode(getNetwork(), evidenceNodes);
//							} catch (Exception e) {
//								throw new RuntimeException(e);
//							}
//						}
//					}
				}
					
				// Finally propagate evidence
			}
			public void onAfterRun(IInferenceAlgorithm algorithm) {}
			public void onAfterReset(IInferenceAlgorithm algorithm) {}
			
			/**
			 * Guarantee that each clique is normalized, if the network is disconnected
			 */
			public void onAfterPropagate(IInferenceAlgorithm algorithm) {
				if (algorithm == null) {
					Debug.println(getClass(), "Algorithm == null");
					return;
				}
				
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
			}
		};
	
	/**
	 * Default constructor for plugin support
	 */
	public JunctionTreeAlgorithm() {
		super();
		// initialize commands for checkConsistency
		this.setVerifyConsistencyCommandList(this.initConsistencyCommandList());
		
		// add dynamically changeable behavior (i.e. routines that are not "mandatory", so it is interesting to be able to disable them when needed)
		this.getInferenceAlgorithmListeners().add(DEFAULT_INFERENCE_ALGORITHM_LISTENER);
	}
	
	/**
	 * Initializes the values of {@link IRandomVariable#getInternalIdentificator()}
	 * of the nodes in {@link #getNet()}. 
	 * By default, the internal id will be the index of the node within the network.
	 * @see IInferenceAlgorithmListener#onBeforeRun(IInferenceAlgorithm)
	 * @see #updateCliqueAndSeparatorInternalIdentificators(IJunctionTree)
	 */
	public void initInternalIdentificators() {
		// the network to be considered is the current network
		ProbabilisticNetwork probabilisticNetwork = getNet();
		if (probabilisticNetwork != null) {
			
			// update ids of nodes (mainly prob nodes and utility nodes)
			int nodeCount = probabilisticNetwork.getNodeCount();
			for (int i = 0; i < nodeCount; i++) {
				Node node = probabilisticNetwork.getNodeAt(i);
				if (node instanceof IRandomVariable) {
					// by default, the internal identificator will be the index of the node within the network.
					((IRandomVariable) node).setInternalIdentificator(i);
				}
			}
			
			// the following are updated at JunctionTree#initBelief
//			// update variables in junction tree (i.e. cliques and separators)
//			IJunctionTree junctionTree = probabilisticNetwork.getJunctionTree();
//			if (junctionTree != null) {
//				// update ids of cliques. Note: Clique#getIndex() is not actually enough to identificate a clique, as it claims
//				List<Clique> cliques = junctionTree.getCliques();
//				if (cliques != null) {
//					// cliques have non-negative ids
//					int index = 0;
//					for (Clique clique : cliques) {
//						clique.setInternalIdentificator(index);
//						index++;
//					}
//				}
//				// update ids of separators
//				Collection<Separator> separators = junctionTree.getSeparators();
//				if (separators != null) {
//					// separators have negative ids, in order to distinguish from cliques
//					int index = -1;
//					for (Separator separator : separators) {
//						separator.setInternalIdentificator(index);
//						index--;
//					}
//				}
//			}
			
		}
	}
	
	/**
	 * @return Instantiates and initializes a list which can be used by
	 * {@link #getVerifyConsistencyCommandList()}.
	 */
	protected List<IJunctionTreeCommand> initConsistencyCommandList() {
		List<IJunctionTreeCommand> ret = new ArrayList<JunctionTreeAlgorithm.IJunctionTreeCommand>();
		
		// initialization
		ret.add(new IJunctionTreeCommand() {
			public void undoAction(IInferenceAlgorithm algorithm, Graph graph) throws UndoableJTCommandException {throw new UndoableJTCommandException();}
			public void doAction(IInferenceAlgorithm algorithm, Graph graph) {
				if (graph == null || graph.getNodeCount() == 0) {
					throw new RuntimeException(resource.getString("EmptyNetException"));
				}
				if (graph instanceof ProbabilisticNetwork) {
					ProbabilisticNetwork net = (ProbabilisticNetwork) graph;
					if (net.getNodeIndexes() == null) {
						net.setNodeIndexes(new HashMap<String, Integer>());
					} else {
						net.getNodeIndexes().clear();
					}
					for (int i = net.getNodes().size()-1; i>=0; i--) {
						net.getNodeIndexes().put(net.getNodes().get(i).getName(), new Integer(i));				
					}
				}
			}
		});
		
		// check utility nodes
		ret.add(new IJunctionTreeCommand() {
			public void undoAction(IInferenceAlgorithm algorithm, Graph graph) throws UndoableJTCommandException {throw new UndoableJTCommandException();}
			public void doAction(IInferenceAlgorithm algorithm, Graph graph) {
				if (graph.getNodeCount() > 0) {
					try {
						verifyUtility(graph);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
		
		// check cycles
		ret.add(new IJunctionTreeCommand() {
			public void undoAction(IInferenceAlgorithm algorithm, Graph graph) throws UndoableJTCommandException {throw new UndoableJTCommandException();}
			public void doAction(IInferenceAlgorithm algorithm, Graph graph) {
				if (graph instanceof ProbabilisticNetwork && graph.getNodeCount() > 0) {
					ProbabilisticNetwork net = (ProbabilisticNetwork) graph;
					try {
						// TODO move the content of net.verifyCycles() to this class
						net.verifyCycles();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
		
		// check if there is no disconnected node
		// there is no need for this check anymore, because we can compile disconnected subnets now.
//		ret.add(new IJunctionTreeCommand() {
//			public void undoAction(IInferenceAlgorithm algorithm, Graph graph) throws UndoableJTCommandException {throw new UndoableJTCommandException();}
//			public void doAction(IInferenceAlgorithm algorithm, Graph graph) {
//				if (graph instanceof ProbabilisticNetwork  && graph.getNodeCount() > 0) {
//					ProbabilisticNetwork net = (ProbabilisticNetwork) graph;
//					try {
//						net.verifyConectivity();
//					} catch (Exception e) {
//						throw new RuntimeException(e);
//					}
//				}
//			}
//		});
		
		// verify CPT consistency
		ret.add(new IJunctionTreeCommand() {
			public void undoAction(IInferenceAlgorithm algorithm, Graph graph) throws UndoableJTCommandException {throw new UndoableJTCommandException();}
			public void doAction(IInferenceAlgorithm algorithm, Graph graph) {
				try {
					verifyPotentialTables(graph);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		// check decision node consistency and fill list of decision nodes
		ret.add(new IJunctionTreeCommand() {
			public void undoAction(IInferenceAlgorithm algorithm, Graph graph) throws UndoableJTCommandException {throw new UndoableJTCommandException();}
			public void doAction(IInferenceAlgorithm algorithm, Graph graph) {
				try {
					// this should throw exception if decision nodes were not properly ordered (by arcs)
					setSortedDecisionNodes(sortDecisionNodes(graph));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		return ret;
	}


	/**
	 * This method will initialize {@link #getSortedDecisionNodes()}
	 * @param graph
	 * @throws Exception  : if the ordering of decision nodes could not be established.
	 * @returns decision nodes properly ordered
	 */
	protected List<INode> sortDecisionNodes(Graph graph) throws Exception {

		if (!(graph instanceof ProbabilisticNetwork)) {
			return null;
		}
		
		ProbabilisticNetwork net = (ProbabilisticNetwork) graph;
		List<Node> nodeList = net.getNodes();
		
		// clear adjacency mapping, because we'll use it in this method (and it needs to be clean)
		for (Node node : nodeList) {
			node.clearAdjacents();
		}
		
		// collect decision nodes
		List<INode> decisionNodes = new ArrayList<INode>();
		int numNodes = nodeList.size();
		for (int i = 0; i < numNodes; i++) {
			if (nodeList.get(i).getType() == Node.DECISION_NODE_TYPE) {
				decisionNodes.add(nodeList.get(i));
			}
		}
	
		// start ordering the decision nodes
		int numDecisionNodes = decisionNodes.size();
		if (numDecisionNodes > 0) {
			// a queue to check
			ArrayList<INode> queue = new ArrayList<INode>();
			queue.ensureCapacity(nodeList.size()); 
			
			for (int i = 0; i < numDecisionNodes; i++) {
				boolean isVisited[] = new boolean[nodeList.size()];
				Node firstInQueue = (Node) decisionNodes.get(i);
				queue.clear();
				queue.add(firstInQueue);
				
				while (queue.size() != 0) {
					INode currentInQueue = queue.remove(0);
					isVisited[nodeList.indexOf(currentInQueue)] = true;
					
					int numChildren = currentInQueue.getChildNodes().size();
					for (int k = 0; k < numChildren; k++) {
						Node child = (Node) currentInQueue.getChildNodes().get(k);
						if (!isVisited[nodeList.indexOf(child)]) {
							if (child.getType() == Node.DECISION_NODE_TYPE
									&& !firstInQueue.getAdjacents().contains(child)) {
								firstInQueue.getAdjacents().add(child);
							}
							queue.add(child);
						}
					}
				}
			}
		}
	
		boolean hasSwap = true;
		while (hasSwap) {
			hasSwap = false;
			for (int i = 0; i < decisionNodes.size() - 1; i++) {
				Node node1 = (Node) decisionNodes.get(i);
				Node node2 = (Node) decisionNodes.get(i + 1);
				try {
					node1 = (Node) decisionNodes.get(i);
					node2 = (Node) decisionNodes.get(i + 1);
				} catch (ClassCastException e) {
					try {
						Debug.println(getClass(), e.getMessage(), e);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					continue;
				}
				if (node1 != null
						&& node2 != null
						&& (node1.getAdjacents().size() < node2.getAdjacents().size())) {
					decisionNodes.set(i + 1, node1);
					decisionNodes.set(i, node2);
					hasSwap = true;
				}
			}
		}
	
		for (int i = 0; i < decisionNodes.size(); i++) {
			Node decisionNode = null;
			try {
				decisionNode = (Node) decisionNodes.get(i);
			} catch (ClassCastException e) {
				try {
					Debug.println(getClass(), e.getMessage(), e);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				continue;
			}
			if (decisionNode != null
					&& ( decisionNode.getAdjacents().size() != decisionNodes.size() - i - 1) ) {
				throw new Exception(resource.getString("DecisionOrderException"));
			}
		}
	
		for (Node node : nodeList) {
			node.clearAdjacents();
		}
		
		return decisionNodes;
	}

	/**
	 * This method is used in {@link #verifyConsistency(ProbabilisticNetwork)}
	 * in order to check consistency of conditional probability tables
	 * @param graph
	 * @throws Exception 
	 * @see {@link ProbabilisticTable#verifyConsistency()}
	 */
	protected void verifyPotentialTables(Graph graph) throws Exception {
		if (graph instanceof ProbabilisticNetwork) {
			// just call ProbabilisticTable#verifyConsistency() for CPT of each node
			for (Node node : ((ProbabilisticNetwork)graph).getNodes()) {
				if ( node instanceof ProbabilisticNode ) {
					PotentialTable probabilityFunction = ((ProbabilisticNode) node).getProbabilityFunction();
					if (probabilityFunction instanceof ProbabilisticTable) {
						((ProbabilisticTable) probabilityFunction).verifyConsistency();
					}
				}
			}
		}
	}

	/**
	 * Constructor using fields
	 */
	public JunctionTreeAlgorithm(ProbabilisticNetwork net) {
		this();
		this.setNet(net);
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getDescription()
	 */
	public String getDescription() {
		return this.utilResource.getString("junctionTreeAlgorithmDescription");
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getName()
	 */
	public String getName() {
		return this.utilResource.getString("junctionTreeAlgorithmName");
	}

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#run()
	 */
	public void run() throws IllegalStateException {
		// make sure all internal IDs were initialized
		this.initInternalIdentificators();
		
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
			listener.onBeforeRun(this);
		}
		if (this.getNet() == null
				|| this.getNet().getNodes().size() == 0) {
			throw new IllegalStateException(resource.getString("EmptyNetException"));
		}
		
		try {
			
			// the following code should be equivalent to "getNet().compile()", but without filling getNet() with garbage related to compilation process
			this.verifyConsistency(getNet());
			this.moralize(getNet());
			List<INode> nodeEliminationOrder = this.triangulate(getNet());
			IJunctionTree junctionTree = this.buildJunctionTree(getNet(), nodeEliminationOrder);
			getNet().setJunctionTree(junctionTree);
			setSortedDecisionNodes(null);	// clear this list, just to make sure it is rebuild at next compilation
			
			// TODO migrate these GUI code to the plugin infrastructure, instead of leaving them here
			if (this.getMediator() != null) {
				JPanel component = this.getMediator().getScreen().getNetWindowCompilation();
				if (component == null) {
					throw new NullPointerException("No compilation pane for " + this.getName() + " could be obtained.");
				}
				// avoid duplicate
				this.getMediator().getScreen().getContentPane().remove(component);
				this.getMediator().getScreen().getCardLayout().removeLayoutComponent(component);
				
				this.getMediator().getScreen().getContentPane().add(component, this.getMediator().getScreen().PN_PANE_PN_COMPILATION_PANE);
				this.getMediator().getScreen().getCardLayout().addLayoutComponent(component, this.getMediator().getScreen().PN_PANE_PN_COMPILATION_PANE);
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
			listener.onAfterRun(this);
		}
	}
	
    
    

	/**
     * This method moves the root clique to 1st index, and then updates {@link IRandomVariable#getInternalIdentificator()}
     * accordingly to its order of appearance in {@link IJunctionTree#getCliques()} and {@link IJunctionTree#getSeparators()}.
     * This is necessary because some implementations assumes that {@link IRandomVariable#getInternalIdentificator()} is synchronized with indexes.
     * This is different from {@link #initInternalIdentificators()}, because this one only updates {@link Clique} and {@link Separator}.
     * @param junctionTree : where separators and cliques will be accessed
     * @see JunctionTree#updateCliqueAndSeparatorInternalIdentificators()
     */
    public void updateCliqueAndSeparatorInternalIdentificators( IJunctionTree junctionTree) {
    	if (junctionTree == null) {
    		return;	// nothing to do
    	}
    	
    	// check if we can simply delegate
    	if (junctionTree instanceof JunctionTree) {
    		// just delegate to junction tree
    		((JunctionTree)junctionTree).updateCliqueAndSeparatorInternalIdentificators();
    		return;
    	}
    	
    	// do it manually
    	if (junctionTree.getCliques() != null && !junctionTree.getCliques().isEmpty() ) {
    		// move the new root clique to the 1st entry in the list of cliques in junction tree, because some algorithms assume the 1st element is the root;
    		Clique root = junctionTree.getCliques().get(0);
    		while (root.getParent() != null) { 
    			root = root.getParent(); // go up in hierarchy until we find the root
    		}
    		int indexOfRoot = junctionTree.getCliques().indexOf(root);
    		if (indexOfRoot > 0) {
    			// move root to the beginning (index 0) of the list
    			Collections.swap(junctionTree.getCliques(), 0, indexOfRoot);
    		}
    		
    		// redistribute internal identifications accordingly to indexes
    		for (int i = 0; i < junctionTree.getCliques().size(); i++) {
    			junctionTree.getCliques().get(i).setIndex(i);
    			junctionTree.getCliques().get(i).setInternalIdentificator(i);
    		}
    	}
		// do the same for separators
		int separatorIndex = -1;
		for (Separator sep : junctionTree.getSeparators()) {
			sep.setInternalIdentificator(separatorIndex--);
		}
	}


	/**
     * Performs moralization (for each node, link its parents with arcs) of the network.
     * @param net
     */
    public void moralize(ProbabilisticNetwork net) {
		Node auxNo;
		Node auxPai1;
		Node auxPai2;
		Edge auxArco;
		
		// reset adjacency info
		for (Node node : net.getNodes()) {
			node.clearAdjacents();
		}
	
		if (net.isCreateLog()) {
			net.getLogManager().append(resource.getString("moralizeLabel"));
		}
		
		net.getMarkovArcs().clear();
		
		Collection<Edge> markovArcsToBeForced = net.getMarkovArcsToBeForced();
		if (markovArcsToBeForced != null && !markovArcsToBeForced.isEmpty()) {
			net.getMarkovArcs().addAll(markovArcsToBeForced);
		}
		
		net.setEdgesCopy(SetToolkit.clone(net.getEdges()));
		
		// remove the list of edges for information
		int sizeArcos = net.getMarkovArcs().size() - 1;
		for (int i = sizeArcos; i >= 0; i--) {
			auxArco = net.getMarkovArcs().get(i);
			if (auxArco.getDestinationNode().getType()
				== Node.DECISION_NODE_TYPE) {
				net.getMarkovArcs().remove(i);
			}
		}
	
		int sizeNos = net.getNodes().size();
		for (int n = 0; n < sizeNos; n++) {
			auxNo = net.getNodes().get(n);
			if (!(auxNo.getType() == Node.DECISION_NODE_TYPE)
				&& auxNo.getParents().size() > 1) {
				int sizePais = auxNo.getParents().size();
				for (int j = 0; j < sizePais - 1; j++) {
					auxPai1 = auxNo.getParents().get(j);
					for (int k = j + 1; k < sizePais; k++) {
						auxPai2 = auxNo.getParents().get(k);
						if ((net.hasEdge(auxPai1, auxPai2,  net.getEdgesCopy()) == -1)
							&& (net.hasEdge(auxPai1, auxPai2, net.getMarkovArcs()) == -1)) {
							auxArco = new Edge(auxPai1, auxPai2);
							if (net.isCreateLog()) {
								net.getLogManager().append(
									auxPai1.getName()
										+ " - "
										+ auxPai2.getName()
										+ "\n");
							}
							net.getMarkovArcs().add(auxArco);
						}
					}
				}
			}
		}
		
		makeAdjacents(net);
		
		if (net.isCreateLog()) {
			net.getLogManager().append("\n");
		}
	}

	/**
     * Starts the triangularization process of the junction tree algorithm
     * @param net
     * @return a list specifying the order of elimination of nodes used in this triangulation process.
     * @see #minimumWeightElimination(List, ProbabilisticNetwork, List)
     */
	public List<INode> triangulate(ProbabilisticNetwork net) {

//		Node aux;
		List<Node> auxNodes;
		List<Node> nodeList = net.getNodes();

		if (net.isCreateLog()) {
			net.getLogManager().append(resource.getString("triangulateLabel"));
		}
		auxNodes = SetToolkit.clone(nodeList);
		
		// remove utility nodes from auxNodes
		{
			Set<Node> nodesToRemove = new HashSet<Node>();
			for (Node node : auxNodes) {
				if (node.getType() == Node.UTILITY_NODE_TYPE) {
					nodesToRemove.add(node);
				}
			}
			auxNodes.removeAll(nodesToRemove);
		}
		
		// reset copy of nodes
		net.getNodesCopy().clear();
		net.getNodesCopy().addAll(SetToolkit.clone(auxNodes));
		List<Node> nodesCopy = net.getNodesCopy();
		
		List<INode> decisionNodes = getSortedDecisionNodes();
		if (decisionNodes == null) {
			// double-checking decision nodes.
			try {
				decisionNodes = sortDecisionNodes(net);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		for (INode node : decisionNodes) {
			auxNodes.remove(node);
			auxNodes.removeAll(node.getParentNodes());
		}

		List<INode> nodeEliminationOrder = new ArrayList<INode>(nodesCopy.size());
		
		// fill nodeEliminationOrder until there is no more nodes that can be eliminated in triangulation process
		while (minimumWeightElimination(auxNodes, net, nodeEliminationOrder));

		// consider elimination of decision nodes as well
		for (int i = decisionNodes.size() - 1; i >= 0; i--) {
			Node decisionNode = (Node) decisionNodes.get(i);
			nodeEliminationOrder.add(decisionNode);
			int sizeAdjacentes = decisionNode.getAdjacents().size();
			for (int j = 0; j < sizeAdjacentes; j++) {
				Node v = decisionNode.getAdjacents().get(j);
				v.getAdjacents().remove(decisionNode);
			}
			if (net.isCreateLog()) {
				net.getLogManager().append(
					"\t" + nodeEliminationOrder.size() + " " + decisionNode.getName() + "\n");
			}

			auxNodes = SetToolkit.clone(decisionNode.getParents());
			auxNodes.removeAll(decisionNodes);
			auxNodes.removeAll(nodeEliminationOrder);
			for (int j = 0; j < i; j++) {
				Node decision = (Node) decisionNodes.get(j);
				auxNodes.removeAll(decision.getParents());
			}

			while (minimumWeightElimination(auxNodes, net, nodeEliminationOrder)) ;
		}
		
		makeAdjacents(net);
		
		return nodeEliminationOrder;
	}
	
	
	/**
	 * Calls {@link TreeVariable#resetEvidence()} for all nodes in {@link SingleEntityNetwork#getNodesCopy()}.
	 * @param net
	 */
	public void resetEvidences(SingleEntityNetwork net) {
		for (Node node : net.getNodesCopy()) {
			if (node instanceof TreeVariable) {
				((TreeVariable)node).resetEvidence();
				// OBS utility nodes are not tree variables, so this method will ignore them
			}
		}
	}
	
	/**
	 * Builds a junction tree from a consistent markov net.
	 * It is expected that {@link #moralize(ProbabilisticNetwork)} and {@link #triangulate(ProbabilisticNetwork)} were
	 * executed prior to this method.
	 * @param net : the markov net to compile junction tree from.
	 * @param nodeEliminationOrder : the ordering of elimination of nodes used in {@link #triangulate(ProbabilisticNetwork)}
	 * @return the junction tree generated by {@link #getJunctionTreeBuilder()} and then filled accordingly to the specified probabilistic network.
	 * @throws InstantiationException : if a junction tree could not be instantiated.
	 * @throws IllegalAccessException : if a junction tree could not be instantiated because of access issues. 
	 * @see ProbabilisticNetwork#compileJT(IJunctionTree)
	 * @see #verifyConsistency(Graph)
	 * @see #moralize(ProbabilisticNetwork)
	 * @see #triangulate(ProbabilisticNetwork)
	 */
	protected IJunctionTree buildJunctionTree(final ProbabilisticNetwork net, List<INode> nodeEliminationOrder) throws InstantiationException, IllegalAccessException  {
		
		IJunctionTreeBuilder jtBuilder = getJunctionTreeBuilder();
		if (jtBuilder == null) {
			jtBuilder = new DefaultJunctionTreeBuilder();
			this.setJunctionTreeBuilder(jtBuilder);
		}
		IJunctionTree junctionTree = jtBuilder.buildJunctionTree(getNet());
		
		resetEvidences(net);
		
		this.cliques(net, nodeEliminationOrder, junctionTree);
		this.strongTreeMethod(net, nodeEliminationOrder, junctionTree);
		this.sortCliqueNodes(net, nodeEliminationOrder, junctionTree);
		this.associateCliques(net, nodeEliminationOrder, junctionTree);
		try {
			junctionTree.initBeliefs();
		} catch (Exception e) {
			// TODO exception translation is not recommended
			throw new RuntimeException(e);
		}
	
		updateMarginals(net);
	
		if (net.isCreateLog()) {
			Thread t = new Thread(new Runnable() {
				public void run() {
					net.makeLog();
					System.out.println("**Log ended**");
				}
			});
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		}
		
		return junctionTree;
	}
	
	/**
	 * This method iterates over nodes in {@link ProbabilisticNetwork#getNodesCopy()} and
	 * updates the cache of marginal probabilities of all such nodes.
	 * @param net : marginals will be updated for all nodes in the list {@link ProbabilisticNetwork#getNodesCopy()} of this instance.
	 * @see TreeVariable#marginal()
	 */
	public void updateMarginals(ProbabilisticNetwork net) {
		List<Node> nodesCopy = net.getNodesCopy();
		if (nodesCopy != null) {
			for (Node node : nodesCopy) {
				/* Check if the node represents a numeric attribute */
				if (node.getStatesSize() == 0) {
					/* 
					 * The node represents a numeric attribute which has no
					 * potential table. Just skip it.
					 */
					continue;
				}
				((TreeVariable)node).marginal();
			}
		}
	}

	/**
	 * Adds nodes in {@link Clique#getNodesList()} and {@link Separator#getNodesList()} to 
	 * their respective clique/separator tables by using {@link PotentialTable#addVariable(INode)},
	 * and then updates the links {@link Clique#getAssociatedProbabilisticNodesList()} and
	 * {@link Clique#getAssociatedUtilityNodesList()}, which are inverse links associating
	 * nodes to smallest clique/separator containing such node.
	 * This will also build the {@link TreeVariable#getAssociatedClique()}, which
	 * is the link from node to smallest clique/separator containing the node (this is used in order to make
	 * marginalization faster, because marginalization in smaller clique/separator tables are faster).
	 * @param net : net whose junctionTree was generated from
	 * @param nodeEliminationOrder : ordering used in {@link #triangulate(ProbabilisticNetwork)}.
	 * @param junctionTree : junction tree where cliques and separators belong.
	 */
	protected void associateCliques(ProbabilisticNetwork net,	List<INode> nodeEliminationOrder, IJunctionTree junctionTree) {
		
		// TODO clean the code and stop using auxiliary variables reused along the entire method for different purposes.
		int min;
		Node auxNode;
		IProbabilityFunction auxTable, auxUtilTab;
		Clique auxClique;
		Clique smallestClique = null;
	
		for (int i = junctionTree.getCliques().size() - 1; i >= 0; i--) {
			auxClique = (Clique) junctionTree.getCliques().get(i);
			auxTable = auxClique.getProbabilityFunction();
			auxUtilTab = auxClique.getUtilityTable();
	
			int numNodes = auxClique.getNodesList().size();
			for (int c = 0; c < numNodes; c++) {
				auxTable.addVariable(auxClique.getNodesList().get(c));
				auxUtilTab.addVariable(auxClique.getNodesList().get(c));
			}
		}
	
		for (Separator auxSep : junctionTree.getSeparators()) {
			auxTable = auxSep.getProbabilityFunction();
			auxUtilTab = auxSep.getUtilityTable();
			int numNodes = auxSep.getNodesList().size();
			for (int c = 0; c < numNodes; c++) {
				auxTable.addVariable(auxSep.getNodesList().get(c));
				auxUtilTab.addVariable(auxSep.getNodesList().get(c));
			}
		}
	
		int numNodes = net.getNodeCount();
		for (int n = 0; n < numNodes; n++) {
			if (net.getNodes().get(n).getType() == Node.DECISION_NODE_TYPE) {
				continue;
			}
	
			min = Integer.MAX_VALUE;
			auxNode = net.getNodes().get(n);
	
			int numCliques = junctionTree.getCliques().size();
			for (int c = 0; c < numCliques; c++) {
				auxClique = (Clique) junctionTree.getCliques().get(c);
	
				if (auxClique.getProbabilityFunction().tableSize() < min
					&& SetToolkit.containsAllExact(auxClique.getNodesList(), auxNode.getParents())) {
					if (auxNode.getType() == Node.PROBABILISTIC_NODE_TYPE
						&& !SetToolkit.containsExact(auxClique.getNodesList(), auxNode)) {
						continue;
					}
					smallestClique = auxClique;
					min = smallestClique.getProbabilityFunction().tableSize();
				}
			}
			// insert to proper list
			if (auxNode instanceof ProbabilisticNode) {
				smallestClique.getAssociatedProbabilisticNodesList().add(auxNode);
			} else {
				smallestClique.getAssociatedUtilityNodesList().add(auxNode);
			}
		}
		
		// also build the inverse link (from node to cliques)
		for (Node node : net.getNodesCopy()) {
			// this code will associate nodes with clique/separator with smallest table size.
			int smallestTableSize = Integer.MAX_VALUE;
			
			// find smallest separator containing the node (because separators are likely to have smaller table size than cliques)
			if (node instanceof ProbabilisticNode) {
				for (Separator separator : junctionTree.getSeparators()) {
					if (separator.getNodesList().contains(node) && (separator.getProbabilityFunction().tableSize() < smallestTableSize)) {
						((ProbabilisticNode) node).setAssociatedClique(separator);
						smallestTableSize = separator.getProbabilityFunction().tableSize();
					}
				}
			}
	
			// if node was not present in any separator, then associate it with smallest clique.
			if (smallestTableSize == Integer.MAX_VALUE) {
				for (Clique clique : junctionTree.getCliques()) {
					if (clique.getNodesList().contains(node) && (clique.getProbabilityFunction().tableSize() < smallestTableSize)) {
						if (node instanceof ProbabilisticNode) {
							((ProbabilisticNode) node).setAssociatedClique(clique);
						} else {
							// note: decision nodes should only be associated with cliques, not separators
							((DecisionNode) node).setAssociatedClique(clique);
							break;	// no need to be in the smallest clique
						}
						smallestTableSize = clique.getProbabilityFunction().tableSize();
					}
				}
			}
			
		}	// end of for each node
	
	}

	/**
	 * Sort nodes in cliques and separators accordingly to the provided elimination order.
	 * @param net : net whose junctionTree was generated from
	 * @param nodeEliminationOrder : ordering to be considered to sort nodes in cliques.
	 * @param junctionTree : junction tree where cliques and separators belong.
	 */
	protected void sortCliqueNodes(ProbabilisticNetwork net, List<INode> nodeEliminationOrder, IJunctionTree junctionTree) {
		List cliqueList = junctionTree.getCliques();
		boolean isID = net.isID();
		for (int k = 0; k < cliqueList.size(); k++) {
			Clique clique = (Clique) cliqueList.get(k);
			ArrayList<Node> nodesInClique = clique.getNodes();
			boolean hasSwapped = true;
			// In general, cliques are expected to be small (much less than 100 nodes), so bubble sort should be OK for most of cases.
			while (hasSwapped) {
				hasSwapped = false;
				for (int i = 0; i < nodesInClique.size() - 1; i++) {
					Node node1 = nodesInClique.get(i);
					Node node2 = nodesInClique.get(i + 1);
					if (isID) {
						if (nodeEliminationOrder.indexOf(node1) > nodeEliminationOrder.indexOf(node2)) {
							nodesInClique.set(i + 1, node1);
							nodesInClique.set(i, node2);
							hasSwapped = true;
						}
					} else { 
						if (node1.getName().compareToIgnoreCase(node2.getName()) > 0 ) {
							nodesInClique.set(i + 1, node1);
							nodesInClique.set(i, node2);
							hasSwapped = true;
						}	
					}
				}
			}
		}
	
		for (Separator separator : junctionTree.getSeparators()) {
			ArrayList<Node> nodesInSeparator = separator.getNodes();
			// In general, separators are expected to be small (even smaller than cliques -- less than 100 nodes), so bubble sort should be OK for most of cases.
			boolean hasSwapped = true;
			while (hasSwapped) {
				hasSwapped = false;
				for (int i = 0; i < nodesInSeparator.size() - 1; i++) {
					Node node1 = nodesInSeparator.get(i);
					Node node2 = nodesInSeparator.get(i + 1);
					if (node1.getName().compareToIgnoreCase(node2.getName()) > 0 ) {
						nodesInSeparator.set(i + 1, node1);
						nodesInSeparator.set(i, node2);
						hasSwapped = true;
					}
				}
			}
		}
	}

	/**
	 * Sub-method of strong-tree method which uses an alpha-ordering in order to 
	 * establish some partial ordering of cliques
	 * @param nodesInClique : list of nodes pertaining to current clique
	 * @param ordering : list specifying the alpha ordering of nodes. Nodes in smaller indexes in this list has smaller order.
	 * Alpha-ordering is usually the inverse of elimination order returned by {@link #triangulate(ProbabilisticNetwork)} 
	 */
	protected int getCliqueIndexFromAlphaOrder(List<Node> nodesInClique, List<Node> ordering) {
		
		// get the maximum index node
		int maxIndex = -1;
		Node nodeWithMaxIndex = null;
		for (Node node : nodesInClique) {
			int currentIndex = ordering.indexOf(node);
			if (maxIndex < currentIndex) {
				maxIndex = currentIndex;
				nodeWithMaxIndex = node;
			}
		}
	
		// disconsider the max node
		nodesInClique.remove(nodeWithMaxIndex);
		if (nodesInClique.isEmpty()) {
			return maxIndex;
		}
	
		// Build list of common neighbors of nodes in clique
		List<Node> neighbors = SetToolkit.clone(nodesInClique.get(0).getAdjacents());
		int numNodes = nodesInClique.size();
		for (int i = 1; i < numNodes; i++) {
			List<Node> intersection = SetToolkit.intersection(neighbors, nodesInClique.get(i).getAdjacents());
//			neighbors.clear();
			neighbors = intersection;
		}
		neighbors.remove(nodeWithMaxIndex);
	
		// return 0 if all neighbors out of this clique has higher index
		int neighborIndex = 0;
		for (Node neighbor : neighbors) {
			if (nodesInClique.contains(neighbor) || (ordering.indexOf(neighbor) > maxIndex)) {
				continue;
			}
			neighborIndex = maxIndex;
			break;
		}
	
		return neighborIndex;
	}

	/**
	 * Assembles the junction tree by using Frank Jensen's strong tree method
	 * @param net : net containing the nodes and the arcs. Nodes in {@link SingleEntityNetwork#getNodesCopy()} will be referenced by this method.
	 * @param nodeEliminationOrder : ordering used in {@link #triangulate(ProbabilisticNetwork)}
	 * @param junctionTree : junction tree to be modified. 
	 * {@link IJunctionTree#getCliques()} must be already filled by {@link #cliques(ProbabilisticNetwork, List, IJunctionTree)}.
	 */
	protected void strongTreeMethod(ProbabilisticNetwork net, List<INode> nodeEliminationOrder, IJunctionTree junctionTree) {
		int ndx;
		Clique auxClique;
		Clique auxClique2;
		List<Node> uni;
		List<Node> inter;
		List<Node> auxList;
		List<Node> listaNos;
		Separator sep;
		List<INode> alpha = new ArrayList<INode>();
	
		for (int i = nodeEliminationOrder.size() - 1; i >= 0; i--) {
			alpha.add(nodeEliminationOrder.get(i));
		}
	
		if (net.getNodesCopy().size() > 1) {
			int sizeCliques = junctionTree.getCliques().size();
			for (int i = 0; i < sizeCliques; i++) {
				auxClique = (Clique) junctionTree.getCliques().get(i);
				listaNos = SetToolkit.clone(auxClique.getNodesList());
				//calculate index
				while ((ndx = getCliqueIndexFromAlphaOrder(listaNos, (List)alpha)) <= 0
					&& listaNos.size() > 1);
				if (ndx < 0) {
					ndx = 0;
				}
				auxClique.setIndex(ndx);
				listaNos.clear();
			}
			alpha.clear();
	
			Comparator<Clique> comparador = new Comparator<Clique>() {
				public int compare(Clique o1, Clique o2) {
					Clique c1 = o1;
					Clique c2 = o2;
					if (c1.getIndex() > c2.getIndex()) {
						return 1;
					}
					if (c1.getIndex() < c2.getIndex()) {
						return -1;
					}
					return 0;
				}
			};
	
			Collections.sort(junctionTree.getCliques(), comparador);
	
			auxClique = (Clique) junctionTree.getCliques().get(0);
			uni = SetToolkit.clone(auxClique.getNodesList());
	
			int sizeCliques1 = junctionTree.getCliques().size();
			for (int i = 1; i < sizeCliques1; i++) {
				auxClique = (Clique) junctionTree.getCliques().get(i);
				inter = SetToolkit.intersection(auxClique.getNodesList(), uni);
				for (int j = 0; j < i; j++) {
					auxClique2 = (Clique) junctionTree.getCliques().get(j);
	
					if (!auxClique2.getNodesList().containsAll(inter)) {
						continue;
					}
	
					sep = new Separator(auxClique2, auxClique);
					sep.setNodes(inter);
					junctionTree.addSeparator(sep);
	
					auxList = SetToolkit.union(auxClique.getNodes(), uni);
					uni.clear();
					uni = auxList;
					break;
				}
			}
		}
	}

	/**
	 * This method will generate cliques for the provided junction tree
	 * @param net : markov net to be compiled to a junction tree.
	 * A bayes net can be converted to markov net by calling {@link #moralize(ProbabilisticNetwork)} and {@link #triangulate(ProbabilisticNetwork)}.
	 * Nodes in {@link SingleEntityNetwork#getNodesCopy()} will be referenced by this method.
	 * @param nodeEliminationOrder : ordering used in {@link #triangulate(ProbabilisticNetwork)}
	 * @param junctionTree : junction tree to be modified. This method will fill {@link IJunctionTree#getCliques()}.
	 */
	protected void cliques(ProbabilisticNetwork net, List<INode> nodeEliminationOrder, IJunctionTree junctionTree) {
		
		// this will be filled with cliques instantiated so far
		List<Clique> generatedCliques = new ArrayList<Clique>();
	
		for (Node node : net.getNodesCopy()) {
			int eliminationOrderIndex = nodeEliminationOrder.indexOf(node);
			Clique newClique = new Clique();
			newClique.getNodesList().add(node);
	
			for (Node adjacentNode : node.getAdjacents()) {
				if (nodeEliminationOrder.indexOf(adjacentNode) > eliminationOrderIndex) {
					newClique.getNodesList().add(adjacentNode);
				}
			}
			generatedCliques.add(newClique);
		}
	
		if (!generatedCliques.isEmpty()) {
//			boolean hasSwapped = true; 
//			while (hasSwapped) {
//				hasSwapped = false;
//				for (int i = 0; i < generatedCliques.size() - 1; i++) {
//					Clique clique1 = generatedCliques.get(i);
//					Clique clique2 = generatedCliques.get(i + 1);
//					if (clique1.getNodesList().size() > clique2.getNodesList().size()) {
//						generatedCliques.set(i + 1, clique1);
//						generatedCliques.set(i, clique2);
//						hasSwapped = true;
//					}
//				}
//			}
			// the above code was an sub-optimal bubble sort, so it was substituted by the sorting method below (which is supposedly a modified merge sort)
			Collections.sort(generatedCliques, new Comparator<Clique>() {
				/** This shall sort by clique size (larger cliques at the end) */
				public int compare(Clique clique1, Clique clique2) {
					return clique1.getNodesList().size() - clique2.getNodesList().size();
				}
			});
		}
	
		int numCliques = generatedCliques.size();
	
		for1 : for (int i = 0; i < numCliques; i++) {
			Clique clique1 = generatedCliques.get(i);
			for (int j = i + 1; j < numCliques; j++) {
				Clique clique2 = generatedCliques.get(j);
	
				if (clique2.getNodesList().containsAll(clique1.getNodesList())) {
					continue for1;
				}
			}
			junctionTree.getCliques().add(clique1);
		}
		
		// dispose the list
//		generatedCliques.clear();
	}

	/**
	 * Sets up node adjacency 
	 * @param net
	 */
	public void makeAdjacents(ProbabilisticNetwork net) {
		// resets the adjacency information
    	for (Node node : net.getNodes()) {
			node.clearAdjacents();
		}
    	for (int z = net.getMarkovArcs().size() - 1; z >= 0; z--) {
			Edge arc = net.getMarkovArcs().get(z);
			arc.getOriginNode().getAdjacents().add(arc.getDestinationNode());
			arc.getDestinationNode().getAdjacents().add(arc.getOriginNode());
		}
    	
    	List<Edge> edgesCopy = net.getEdgesCopy();
    	for (int z = edgesCopy.size() - 1; z >= 0; z--) {
			Edge auxArco = edgesCopy.get(z);
			if (auxArco.getDestinationNode().getType() == Node.UTILITY_NODE_TYPE) {
				edgesCopy.remove(z);
			} else {
				auxArco.getOriginNode().getAdjacents().add(auxArco.getDestinationNode());
				auxArco.getDestinationNode().getAdjacents().add(auxArco.getOriginNode());
			}
		}
    }

	/**
	 * Sub-routine for {@link #triangulate(ProbabilisticNetwork)}.
	 * It eliminates the nodes in the graph by using minimum weight heuristics.
	 * First, it eliminates nodes whose adjacent nodes are pairwise connected.
	 * After that, if there are more nodes in the graph, it eliminates them using the
	 * minimum weight heuristic.
	 *
	 * @param  nodes  collection of nodes considered here.
	 * @param nodeEliminationOrder : this is an output argument that will be filled with nodes
	 * ordered by the order of elimination used by this algorithm for triangulation.
	 * This list may be referenced later as a heuristic for ordering cliques.
	 * 
	 */
	protected boolean minimumWeightElimination(List<Node> nodes, ProbabilisticNetwork net, List<INode> nodeEliminationOrder) {
		if (nodeEliminationOrder == null) {
			nodeEliminationOrder = new ArrayList<INode>();
		}
		boolean hasSome 	= true;
		
		while (hasSome) {
			hasSome = false;
	
			for (int i = nodes.size() - 1; i >= 0; i--) {
				Node auxNode = nodes.get(i);
	
				if (isNeedingMoreArc(auxNode, net)) {
					continue;
				}
	
				for (int j = auxNode.getAdjacents().size() - 1; j >= 0; j--) {
					Node v = auxNode.getAdjacents().get(j);
					//boolean removed = v.getAdjacents().remove(auxNo);				
					//assert removed;
					v.getAdjacents().remove(auxNode);
				}
				nodes.remove(auxNode);
				hasSome = true;
				nodeEliminationOrder.add(auxNode);
				if (net.isCreateLog()) {
					net.getLogManager().append(
						"\t" + nodeEliminationOrder.size() + " " + auxNode.getName() + "\n");
				}
			}
		}
	
		if (nodes.size() > 0) {
			Node auxNo = weight(nodes); //auxNo: clique with maximum weight.
			nodeEliminationOrder.add(auxNo);
			if (net.isCreateLog()) {
				net.getLogManager().append(
					"\t" + nodeEliminationOrder.size() + " " + auxNo.getName() + "\n");
			}
			addChordAndEliminateNode(auxNo, nodes, net); //Eliminate node and reduce the scope to be considered.
			return true;
		}
		
		return false;
	}
	
	/**
	 * Method used inside {@link #triangulate(ProbabilisticNetwork)}
	 * in order to use the minimum weight heuristic.
	 *
	 * @param  nodes  available nodes
	 * @return   a node having the set of adjacent nodes
	 * with a minimum weight
	 */
	private Node weight(List<Node> nodes) {
		Node v;
		Node auxNode;
		double p;
	
		Node noMin = null;
		double pmin = Double.MAX_VALUE;

		for (int i = nodes.size()-1; i >= 0; i--) {
			auxNode = nodes.get(i);
			p = Math.log(auxNode.getStatesSize());
	
			for (int j = auxNode.getAdjacents().size()-1; j >= 0; j--) {
				v = auxNode.getAdjacents().get(j);
				p += Math.log(v.getStatesSize());
			}
			if (p < pmin) {
				pmin = p;
				noMin = auxNode;
			}
		}
		
//		assert noMin != null;
		return noMin;
	}
	
	/**
	 * Method used inside {@link #triangulate(ProbabilisticNetwork)}
	 * in order to eliminate nodes and reduce the graph after adding a new arc to result in a new chord.
	 * The new arcs are included in {@link SingleEntityNetwork#getMarkovArcs()} of {@link #getNet()}.
	 * {@link Node#getAdjacents()} is also updated accordingly to the new arcs.
	 *@param  node      node to be eliminated
	 *@param  nodes  available nodes
	 * @see #minimumWeightElimination(List, ProbabilisticNetwork, List)
	 * @see ProbabilisticNetwork#getMarkovArcs()
	 * @see Node#getAdjacents()
	 */
	protected void addChordAndEliminateNode(Node node, List<Node> nodes, ProbabilisticNetwork net) {	
		for (int i = node.getAdjacents().size()-1; i > 0; i--) {
			Node auxNode1 = node.getAdjacents().get(i);
	
			for (int j = i - 1; j >= 0; j--) {
				Node auxNode2 = node.getAdjacents().get(j);
				if (! auxNode2.getAdjacents().contains(auxNode1)) {
					Edge auxArco = new Edge(auxNode1, auxNode2);
					if (net.isCreateLog()) {
						net.getLogManager().append(
							auxNode1.getName()
								+ resource.getString("linkedName")
								+ auxNode2.getName()
								+ "\n");
					}
					net.getMarkovArcs().add(auxArco);
					auxNode1.getAdjacents().add(auxNode2);
					auxNode2.getAdjacents().add(auxNode1);			
				}
			}
		}
	
		for (int i = node.getAdjacents().size() - 1; i >= 0; i--) {
			Node auxNo1 = node.getAdjacents().get(i);
			auxNo1.getAdjacents().remove(node);
		}
		nodes.remove(node);
	}

	/**
	 * This method is used inside {@link #triangulate(ProbabilisticNetwork)}
	 * in order to verify whether we need to insert another arc in order
	 * to eliminate a node.
	 * @param auxNo
	 * @return
	 */
	protected boolean isNeedingMoreArc(Node node, ProbabilisticNetwork net) {
		if (node.getAdjacents().size() < 2) {
			return false;
		}
	
		for (int i = node.getAdjacents().size()-1; i > 0; i--) {
			Node auxNo1 = node.getAdjacents().get(i);
	
			for (int j = i - 1; j >=0; j--) {
				Node auxNo2 = node.getAdjacents().get(j);
				if (! auxNo2.getAdjacents().contains(auxNo1)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks the consistency of junction tree and network.
	 * @param net
	 * @see #run()
	 */
	protected void verifyConsistency(Graph net) {
		if (this.getVerifyConsistencyCommandList() != null) {
			for (IJunctionTreeCommand command : this.getVerifyConsistencyCommandList()) {
				command.doAction(this, net);
			}
		}
	}


	/**
	 * Template method for {@link #verifyConsistency(ProbabilisticNetwork)}
	 * @param net
	 * @throws Exception 
	 */
	protected void verifyUtility(Graph graph) throws Exception {
		if (graph instanceof ProbabilisticNetwork) {
			ProbabilisticNetwork net = (ProbabilisticNetwork) graph;
			// just check that all utility nodes don't have children
			for (Node node : net.getNodes()) {
				if (node.getType() == Node.UTILITY_NODE_TYPE && node.getChildren().size() != 0) {
					throw new Exception( resource.getString("variableName") + node + resource.getString("hasChildName"));
				}
			}
		}
	}


	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#setNetwork(unbbayes.prs.Graph)
	 */
	public void setNetwork(Graph g) throws IllegalArgumentException {
		this.setNet((ProbabilisticNetwork)g);
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getNetwork()
	 */
	public Graph getNetwork() {
		return this.getNet();
	}

	/**
	 * @return the net
	 */
	public ProbabilisticNetwork getNet() {
		return net;
	}

	/**
	 * @param net the net to set
	 */
	public void setNet(ProbabilisticNetwork net) {
		this.net = net;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#reset()
	 */
	public void reset() {
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
			listener.onBeforeReset(this);
		}
		try {
			this.getNet().initialize();
			if (this.getMediator() != null) {
				// if we have access to the controller, update status label
				float totalEstimateProb = this.getNet().PET();
				this.getMediator().getScreen().setStatus(this.getResource().getString("statusReady"));
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
			listener.onAfterReset(this);
		}
	}

	/**
	 * Just delegates to {@link #propagate(Clique, boolean)} passing null and true as its argument
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#propagate()
	 */
	public void propagate() {
		this.propagate(null, true);
	}
	
	/**
	 * Performs a junction tree algorithm propagation, but only for the subtree of the junction tree
	 * which is rooted by the clique passed as the argument.
	 * This is particularly useful if only 1 clique has evidence and such clique is part of a disconnected
	 * subnet (thus, the nodes in the subnet are part of a sub-tree in the junction tree which is connected with some empty separator).
	 * @param rootOfSubtree: only this clique and its descendants will be considered in the propagation.
	 * @param isToUpdateMarginals : if this is false and rootOfSubtree != null, then marginal probabilities of all nodes will not
	 * be updated at the end of execution of this method. Otherwise, the marginal probabilities will be updated with no problem.
	 * Use this feature in order to avoid marginal updating when several propagations are expected to be executed in a sequence, and
	 * the marginals are not required to be updated at each propagation (so that we won't run the marginal updating several times
	 * unnecessarily).
	 */
	public void propagate(Clique rootOfSubtree, boolean isToUpdateMarginals) {
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
			listener.onBeforePropagate(this);
		}
		try {
			if (rootOfSubtree != null) {
				this.getNet().updateEvidences(rootOfSubtree, isToUpdateMarginals);
			} else {
				this.getNet().updateEvidences();
			}
			if (this.getMediator() != null) {
				// if we have access to the controller, update status label
				float totalEstimateProb = this.getNet().PET();
				this.getMediator().getScreen().setStatus(this.getResource()
						.getString("statusEvidenceProbabilistic")
						+ (totalEstimateProb * 100.0) + "%");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
			listener.onAfterPropagate(this);
		}
	}

	/**
	 * Add a virtual node with 2 states.
	 * This is the main mechanism used in likelihood evidence and soft evidence.
	 * The new virtual node is just a child of parentNode.
	 * It assumes parentNodes are all within {@link #getNet()}
	 * and {@link #getLikelihoodExtractor()} can extract the likelihood from parentNodes.<br/>
	 * It will fill the table of the virtual node as follows:<br/>
	 * <var>
	 * 				Parent2State0		Parent2State0		Parent2State0		<br/>
	 * 				ParentState0		ParentState1		ParentState2	... <br/>
	 * virtState1	likelihood[0]		likelihood[1]		likelihood[2]		<br/>
	 * virtState2	1-likelihood[0]		1-likelihood[1]		1-likelihood[2]		<br/>
	 * <var/>
	 * @param parentNodes	: all nodes to become parents of the virtual evidence. Usually, this is a list with 1 element, but if you want to add
	 * conditional soft evidence, then the first node of this list will be the node whose soft evidence will be added, and the other
	 * nodes are the nodes assumed to be true in the conditional evidence. The first node in this list will be the main parent of
	 * the new virtual node.
	 * @param graph : the network containing parentNodes
	 * @return the generated virtual node
	 * @throws Exception
	 * @author Shou Matsumoto
	 * @author Alexandre Martins
	 */
	public INode addVirtualNode(Graph graph, List<INode> parentNodes) throws Exception {
		
		// assertion
		if (parentNodes == null || parentNodes.size() <= 0) {
			throw new IllegalArgumentException("parentNodes == null");
		}
		
		// extract network
		SingleEntityNetwork net = this.getNet();
		if (net == null) {
			throw new IllegalStateException("Network == null");
		}
		
		if (net.isID()) {
			throw new IllegalArgumentException("Virtual nodes for influence diagrams not supported yet.");
		}
		
		// extract likelihood value from extractor
		float[] likelihood = this.getLikelihoodExtractor().extractLikelihoodRatio(graph, parentNodes.get(0));
		// reset any finding of the main parent (the parent with the likelihood evidence)
		((TreeVariable)parentNodes.get(0)).resetLikelihood();
		
		// this is going to be the virtual node for propagation of likelihood evidence
		ProbabilisticNode virtualNode = new ProbabilisticNode();

		// set the position of the virtual node to be close to the node (1st element of the list) that we are adding evidence
		java.awt.geom.Point2D.Double position = ((Node)parentNodes.get(0)).getPosition();
		virtualNode.setPosition(position.getX() + virtualNodePositionRandomness*Math.random(), position.getY() + virtualNodePositionRandomness *Math.random());
		
		// generate name of virtual node
		String newName = getVirtualNodePrefix();
		for (INode node : parentNodes) {
			newName += node.getName();
		}
		// append the likelihood value to the name of the node
		if (likelihood.length <= parentNodes.get(0).getStatesSize()) {
			for (float f : likelihood) {
				newName += "_" + f;
			}
		}
		if (net.getNodeIndex(newName) >= 0) {
			// guarantee a unique name
			for (int i = 1; i < Integer.MAX_VALUE; i++) { // We assume Integer.MAX_VALUE is enough...
				if (net.getNodeIndex(newName + "_" + i) < 0) {
					// Supposedly, there is no node with the same name. Use it
					newName = newName + "_" + i;
					break;
				}
			}
		}
		virtualNode.setName(newName);
		virtualNode.setInternalIdentificator(net.getNodeCount());
		// we need to add only 2 states (one will be set as a finding)
		virtualNode.appendState(this.getResource().getString("likelihoodName"));
		virtualNode.appendState(this.getResource().getString("dummyState"));
		
		net.addNode(virtualNode);
		
		// init potential table for virtual node
		PotentialTable virtualNodeCPT = virtualNode.getProbabilityFunction();
		if (virtualNodeCPT.getVariablesSize() <= 0) {
			// if virtualNode itself is not registered as a variable in virtualNodeCPT, add it  
			virtualNodeCPT.addVariable(virtualNode);
		}
		
		// add edge from parentNodes to the virtual node
		for (INode parentNode : parentNodes) {
			net.addEdge(new Edge((Node) parentNode,virtualNode));
		}
		
		/*
		 * Fill virtualNodeCPT like the following table:
		 * 
		 * 				Parent2State0		Parent2State0		Parent2State0		
		 * 				ParentState0		ParentState1		ParentState2	...
		 * virtState1	likelihood[0]		likelihood[1]		likelihood[2]
		 * virtState2	1-likelihood[0]		1-likelihood[1]		1-likelihood[2]
		 * 
		 * 
		 */
		try {
			for (int i = 0; i < virtualNodeCPT.tableSize()/2; i++) {
				virtualNodeCPT.setValue(2*i, likelihood[i]);
				virtualNodeCPT.setValue(2*i+1, 1-likelihood[i]);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException(this.getResource().getString("sizeOfLikelihoodException") + this.getResource().getString("expectedSize") + virtualNodeCPT.tableSize()/2 , e);
		}
		
		// this is not necessary for ProbabilisticNode, but other types of nodes may need explicit initialization of the marginals
		virtualNode.initMarginalList();

		// update the marginal values (we only updated clique/separator potentials, thus, the marginals still have the old values if we do not update)
//		virtualNode.updateMarginal();
//		virtualNode.copyMarginal();
		
		// set finding always to the first state (because the second state is just a dummy state)
		virtualNode.addFinding(0);
		
		// create clique and separator for the virtual node
		Collection<IRandomVariable> sepAndCliqueSet = createCliqueAndSeparatorForVirtualNode(virtualNode, parentNodes, net);
		
		// register new virtual node (so that it can be removed later)
		this.getVirtualNodesToCliquesAndSeparatorsMap().put(virtualNode, sepAndCliqueSet);
		
		return virtualNode;
	}
	
	/**
	 * This is used in {@link #addVirtualNode(Graph, List)} in order to instantiate cliques and separators related to
	 * the virtual node, add such cliques/separators to the junction tree, and link {@link Clique#getAssociatedProbabilisticNodesList()}
	 * and {@link TreeVariable#getAssociatedClique()}.
	 * <br/>
	 * Subclasses may overwrite this method in order to customize how cliques and separators related to the virtual node (of soft/likelihood evidences)
	 * will be handled by the algorithm. For instance, algorithms not actually using junction trees can simply overwrite this method
	 * so that it does not do anything.
	 * @param virtualNode : the virtual node generated in {@link #addVirtualNode(Graph, List)}
	 * @param parentNodes : the same arguments of {@link #addVirtualNode(Graph, List)}
	 * @param net : the net containing the junction tree where cliques and separators generated by this method will be included.
	 * @return : the cliques and separators generated (and included to junction tree) by this method.
	 */
	protected Collection<IRandomVariable> createCliqueAndSeparatorForVirtualNode( ProbabilisticNode virtualNode, List<INode> parentNodes, SingleEntityNetwork net) {
		// prepare junction tree so that we can manipulate cliques
		IJunctionTree junctionTree = net.getJunctionTree();
		
		// find the smallest clique containing all the parents
		int smallestSize = Integer.MAX_VALUE;
		Clique smallestCliqueContainingAllParents = null;
		for (Clique clique : junctionTree.getCliquesContainingAllNodes(parentNodes, Integer.MAX_VALUE)) {
			if (!clique.getNodesList().contains(virtualNode) && (clique.getProbabilityFunction().tableSize() < smallestSize)) {
				smallestCliqueContainingAllParents = clique;
				smallestSize = clique.getProbabilityFunction().tableSize();
			}
		}
		
		// if could not find smallest clique, the arguments are inconsistent
		if (smallestCliqueContainingAllParents == null) {
			throw new IllegalArgumentException(getResource().getString("noCliqueForNodes") + parentNodes);
		}
		
		// reorder parent nodes, so that the order matches the nodes in clique potential.
		List<INode> orderedParentNodes = new ArrayList<INode>(parentNodes.size());
//		for (INode parent : smallestCliqueContainingAllParents.getNodes()) {
		for (INode parent : smallestCliqueContainingAllParents.getProbabilityFunction().variableList) {	// use the ordering of variables in clique table instead
			if (parentNodes.contains(parent)) {
				orderedParentNodes.add(parent);
			}
		}
		
		// create clique for the virtual node and parents
		Clique cliqueOfVirtualNode = new Clique();
		cliqueOfVirtualNode.getNodesList().add(virtualNode);
		cliqueOfVirtualNode.getNodesList().addAll((List)orderedParentNodes);		// TODO check if clique#getNodes() is sensitive to ordering (clique tables are, but this is usually not so sensitive).
		cliqueOfVirtualNode.getProbabilityFunction().addVariable(virtualNode);
		for (INode parentNode : orderedParentNodes) {	// use same ordering of variables in clique table, because some algorithms may require same ordering.
			cliqueOfVirtualNode.getProbabilityFunction().addVariable(parentNode);
		}
		cliqueOfVirtualNode.setInternalIdentificator(junctionTree.getCliques().size());
		
		// add clique to junction tree, so that the algorithm can handle the clique correctly
		junctionTree.getCliques().add(cliqueOfVirtualNode);
		
		// create separator between the clique of parent nodes and virtual node (the separator should contain all parents)
		Separator separatorOfVirtualCliqueAndParents = new Separator(smallestCliqueContainingAllParents , cliqueOfVirtualNode);
		separatorOfVirtualCliqueAndParents.setNodes(new ArrayList<Node>((List)orderedParentNodes));
		for (INode parentNode : orderedParentNodes) {	// again, use the same ordering of variables in clique table
			separatorOfVirtualCliqueAndParents.getProbabilityFunction().addVariable(parentNode);
		}
		junctionTree.addSeparator(separatorOfVirtualCliqueAndParents);
		separatorOfVirtualCliqueAndParents.setInternalIdentificator(-(junctionTree.getSeparators().size()+1));
		
		// just to guarantee that the network is fresh
		net.resetNodesCopy();
		
		// now, let's link the nodes with the cliques
		cliqueOfVirtualNode.getAssociatedProbabilisticNodesList().add(virtualNode);
		virtualNode.setAssociatedClique(cliqueOfVirtualNode);
		
		// initialize the probabilities of clique and separator
		net.getJunctionTree().initBelief(cliqueOfVirtualNode);
		net.getJunctionTree().initBelief(separatorOfVirtualCliqueAndParents);	// this one sets all separator potentials to 1
		
//		cliqueOfVirtualNode.normalize();
//		((JunctionTree) junctionTree).absorb(cliqueOfVirtualNode, smallestCliqueContainingAllParents);
//		cliqueOfVirtualNode.normalize();
		
		// propagation (make sure the probabilities of the new clique and separator becomes globally consistent)
//		junctionTree.consistency();
		// the above propagation doesn't seem to be necessary
		
		// store the potentials after propagation, so that the "reset" will restore these values
		cliqueOfVirtualNode.getProbabilityFunction().copyData();	
		separatorOfVirtualCliqueAndParents.getProbabilityFunction().copyData();

		// prepare a collection containing the clique of virtual node and respective separator, so that we can store them and delete them from JT when prompted
		Set<IRandomVariable> sepAndCliqueSet = new HashSet<IRandomVariable>();
		sepAndCliqueSet.add(cliqueOfVirtualNode);
		sepAndCliqueSet.add(separatorOfVirtualCliqueAndParents);
		
		return sepAndCliqueSet;
	}

	/**
	 * This method just removes all virtual nodes created in {@link #addVirtualNode(List)}
	 * from {@link #getNetwork()} by accessing #getVirtualNodes().
	 * #getVirtualNodes() will be cleared after execution of this method. 
	 */
	public void clearVirtualNodes() {
		for (INode virtualNode : getVirtualNodesToCliquesAndSeparatorsMap().keySet()) {
			
			// remove the node but does not update cliques
			getNet().removeNode((Node) virtualNode, false);
			
			// remove clique/separator from junction tree
			if (this.getNet().getJunctionTree() != null) {
				// store the separators to be deleted, and delete them later
				List<Separator> separatorsToRemove = new ArrayList<Separator>(getVirtualNodesToCliquesAndSeparatorsMap().size()/2);
				for (IRandomVariable cliqueOrSep : getVirtualNodesToCliquesAndSeparatorsMap().get(virtualNode)) {
					if (cliqueOrSep instanceof Clique) {
						// remove this clique from parent
						Clique clique = (Clique) cliqueOrSep;
						// this.getNet().getJunctionTree().removeSeparator((Separator)cliqueOrSep); may have done this already, so check if there is a reference to parent
						if (clique.getParent() != null) {
							// do not use List#remove() because it uses equals internally
							int indexToRemove = 0;
							for (Clique child :  clique.getParent().getChildren()) {
								// use this comparison, because it's faster than equals()
								if (child.getInternalIdentificator() == clique.getInternalIdentificator()) {
									break;
								}
								indexToRemove++;
							}
							clique.getParent().getChildren().remove(indexToRemove);
						}
						
						// remove the clique containing the virtual node
						List<Clique> cliques = this.getNet().getJunctionTree().getCliques();
						int indexToRemove = 0;
						for (Clique cliqueToCompare : this.getNet().getJunctionTree().getCliques()) {
							// do this comparison instead of equals, which is a name comparison
							if (cliqueToCompare.getInternalIdentificator() == cliqueOrSep.getInternalIdentificator()) {
								break;
							}
							indexToRemove++;
						}
						cliques.remove(indexToRemove);
					} else {
						// keep track of separators, so that we can remove them later.
						// don't remove now, because extensions of JunctionTree#removeSeparator may disconnect cliques by using comparisons with Object#equals, which is not always precise
						separatorsToRemove.add((Separator)cliqueOrSep);
					}
				}
				// remove the separators at once, after the cliques
				for (Separator separator : separatorsToRemove) {
					this.getNet().getJunctionTree().removeSeparator(separator);
				}
			}
			
		}
		getVirtualNodesToCliquesAndSeparatorsMap().clear();
	}
	
	
	/**
	 * Obtains the joint probability of a set of node.
	 * For nodes [A,B,C] and states [a,b,c], the joint probability is basically the chain 
	 * P(C=c)*P(B=b|C=c)*P(A|B=b,C=c).
	 * @param nodesAndStatesToConsider :  the nodes to calculate joint probability.
	 * If negative states are passed, then Math.abs(state + 1) will
	 * be set as 0%.
	 * @return the joint probability
	 */
	public float getJointProbability(Map<ProbabilisticNode,Integer> nodesAndStatesToConsider) {
		if (nodesAndStatesToConsider == null || nodesAndStatesToConsider.isEmpty()) {
			throw new IllegalArgumentException("This method cannot calculate joint probability without specifying the nodes && states");
		}
		
		// at this point, there are some values in nodesAndStatesToConsider.
		if (nodesAndStatesToConsider.size() == 1) {
			// we only need marginal probability
			ProbabilisticNode node = nodesAndStatesToConsider.keySet().iterator().next();
			Integer state = nodesAndStatesToConsider.get(node);
			if (state < 0 ) {
				// what is the probability of "not" state
				return 1 - node.getMarginalAt(Math.abs(state + 1));
			}
			return node.getMarginalAt(state);
		}
		
		// check if there is a clique containing all the nodes. If so, we do not need to propagate findings.
		if (isToCalculateJointProbabilityLocally()) {
			// extract 1 clique containing all nodes
			List<Clique> cliques = this.getNet().getJunctionTree().getCliquesContainingAllNodes((Collection)nodesAndStatesToConsider.keySet(), 1);
			if (cliques != null && !cliques.isEmpty()) {
				// obtain clone of the clique table (need clone, because we'll marginalize-out some nodes)
				PotentialTable cliqueTable = (PotentialTable) cliques.get(0).getProbabilityFunction().clone();
				// Obtain all nodes which are part of clique but not specified in nodesAndStatesToConsider
				ArrayList<Node> nodesToMarginalizeOut = new ArrayList<Node>(cliques.get(0).getNodes());
				nodesToMarginalizeOut.removeAll(nodesAndStatesToConsider.keySet());
				// marginalize-out nodes that are not specified in nodesAndStatesToConsider
				for (Node node : nodesToMarginalizeOut) {
					cliqueTable.removeVariable(node);
				}
				// obtain the index of the cell in cliqueTable related to the states in nodesAndStatesToConsider
				int[] coordinate = new int[cliqueTable.getVariablesSize()];
				boolean hasNegativeState = false;
				for (ProbabilisticNode node : nodesAndStatesToConsider.keySet()) {
					int state = nodesAndStatesToConsider.get(node);
					if (state < 0) {
						hasNegativeState = true;
						break;
					}
					coordinate[cliqueTable.indexOfVariable(node)] = state;
					// TODO check if all values were really filled
				}
				if (!hasNegativeState) {
					return cliqueTable.getValue(coordinate);
				} else {
					// sum clique potentials that matches the nodesAndStatesToConsider
					double sum = 0.0;
					for (int i = 0; i < cliqueTable.tableSize(); i++) {
						// check if states related to the current cell matches the states specified in nodesAndStatesToConsider
						coordinate = cliqueTable.getMultidimensionalCoord(i);
						boolean isExactMatch = true;
						for (ProbabilisticNode node : nodesAndStatesToConsider.keySet()) {
							Integer state = nodesAndStatesToConsider.get(node);
							if (state >= 0 && coordinate[cliqueTable.indexOfVariable(node)] != state) {
								// this cell is not related to the state
								isExactMatch = false;
								break;
							} else if (state < 0 && coordinate[cliqueTable.indexOfVariable(node)] == Math.abs(state + 1)) {
								// negative states indicates that we should not consider that state. So, this cell should not be used.
								isExactMatch = false;
								break;
							}
						}
						if (isExactMatch) {
							sum += cliqueTable.getValue(i);
						}
					}
					return (float) sum;
				}
			}
		}
		
		// if nodes are distributed across cliques, we need to propagate findings.
		
		// backup original network, so that we can revert to it later
		ProbabilisticNetwork originalNetwork = this.getNet();
		
		// clone net, so that we don't change the original
		ProbabilisticNetwork clonedNetwork = this.cloneProbabilisticNetwork(getNet());	
		
		// we will use the cloned network from now
		this.setNet(clonedNetwork);
		
		// the value to return
		double ret = 1;	// 1 is the identity value in multiplication
		try {
			// calculate P(C=c)*P(B=b|C=c)*P(A|B=b,C=c) in the cloned network
			for (ProbabilisticNode origNode : nodesAndStatesToConsider.keySet()) {
				// extract the state
				Integer stateIndex = nodesAndStatesToConsider.get(origNode);
				if (stateIndex == null) {
					continue;	// ignore
				}
				// Extract cloned node. Original and cloned nodes have the same name
				ProbabilisticNode clonedNode = (ProbabilisticNode) clonedNetwork.getNode(origNode.getName());
				
				// add evidences
				if (stateIndex < 0) {
					if (!isToUseEstimatedTotalProbability()) {
						// multiply marginal (or conditional prob) before adding the finding (if we add finding, the marginals will have only 0s and 1s)
						for (int i = 0; i < clonedNode.getStatesSize(); i++) {
							if (i != Math.abs(stateIndex + 1)) {
								ret *= clonedNode.getMarginalAt(i);
							}
						}
					}
					// this is a finding for "not" this state (i.e. a finding setting this state as 0%)
					clonedNode.addFinding(Math.abs(stateIndex + 1), true);
				} else {
					if (!isToUseEstimatedTotalProbability()) {
						// multiply marginal (or conditional prob) before adding the finding (if we add finding, the marginals will have only 0s and 1s)
						ret *= clonedNode.getMarginalAt(stateIndex);
					}
					clonedNode.addFinding(stateIndex);
				}
				
				// if isToUseEstimatedTotalProbability, then we shall propagate all findings at once and then use this.getJunctionTree().getN()
				if (!isToUseEstimatedTotalProbability()) {
					// propagate findings in the cloned network
					this.propagate();
				}
			}
			
			// if isToUseEstimatedTotalProbability, then we shall propagate all findings at once and then use this.getJunctionTree().getN()
			if (isToUseEstimatedTotalProbability()) {
				this.propagate();
				ret = this.getJunctionTree().getN();
			}
		} catch (Throwable t) {
			this.setNet(originalNetwork);
			throw new RuntimeException(t.getMessage(),t);
		}
		
		// revert to original network
		this.setNet(originalNetwork);
		
		return (float) ret;
	}
	

	/**
	 * Indicates whether a given value must be considered as "unspecified" (so can assume either 0% or 100%)
	 * @param value: value to check
	 * @return value == null || Float.isInfinite(value) || Float.isNaN(value) || value < 0 || value > 1 
	 */
	public boolean isUnspecifiedProb(Float value) {
		return value == null || Float.isInfinite(value) || Float.isNaN(value) || value < 0 || value > 1;
	}

	/**
	 * Will attempt to obtain what is the index of hard evidence.
	 * Will return negative or null if this is not a hard evidence
	 * @param prob : list to check content.
	 * @return : positive value if hard evidence (it will be the index of the state set to 100%), 
	 * negative value if negative hard evidence, and null if soft evidence.
	 * In case of negative hard evidence, then (-RETURNED_VALUE-1) will be the index of the 1st state
	 * found to be set to 0%.
	 * @see #getEvidenceType(List)
	 */
	public Integer getResolvedState(List<Float> prob) {
		// initial assertion
		if (prob == null || prob.isEmpty()) {
			throw new IllegalArgumentException("Could not verify whether the specified probability is a hard evidence, soft evidence, or ; \"negative\" hard evidence; " +
					"because nothing was provided.");
		}
		float sum = 0;	// if the sum of specified probs is 1, then it is either a positive hard evidence or a soft evidence. If not, then it is negative hard evidence 
		boolean hasSpecifiedProb = false;	// if all probs were unspecified, then this is false, and neither of the three types of findings matches our case
		boolean hasUnspecifiedProb = false;	// if there is any unspecified value, then this becomes true
		boolean hasSoftEvidence = false;	// if 0 < value < 1, then it is a soft evidence (not a hard evidence)
		boolean hasZeros = false;	// if at least one state is specified as 0%, then this flag will be turned on
		int index1stState0 = -1;	// will hold the first state settled with 0%. This will be used as a return if this is a hard evidence
		int index1stState1 = -1;	// will hold the first state settled with 100%. This will be used as a return if this is a negative hard evidence.
		for (int i = 0; i < prob.size(); i++) {
			Float value = prob.get(i);
			if (isUnspecifiedProb(value)) {
				hasUnspecifiedProb = true;
				continue;	// ignore unspecified values for now
			}
			// at this point, value is supposedly between 0　and 1
			sum += value;
			if (value > ERROR_MARGIN && value < 1-ERROR_MARGIN) {
				// 0 < value < 1 given error margin
				hasSoftEvidence = true;
			} else if (Math.abs(value) < ERROR_MARGIN) {
				// there is a state explicitly at 0%
				hasZeros =  true;
				if (index1stState0 < 0) {
					index1stState0 = i;
				}
			} else {
				// this is a state explicitly at 100%
				if (index1stState1 < 0) {
					index1stState1 = i;
				}
			}
			hasSpecifiedProb = true;	// turn on flag indicating that at least 1 prob was specified
		}
		if (!hasSpecifiedProb) {
			throw new IllegalArgumentException("No probability was explicited in the argument. Please specify the probabability of at least 1 state");
		}
		// at this point, prob contains at least 1 state with valid probability value (i.e. not all states are "unspecified")
		if (hasSoftEvidence) {
			// check if the specified soft evidence is normalized
			if (Math.abs(1f-sum) > ERROR_MARGIN) {
				throw new IllegalArgumentException("The soft evidence " + prob +" does not seem to sum up to 1.");
			}
			if (hasUnspecifiedProb) {
				throw new IllegalArgumentException("In a soft evidence, all values must be explicitly specified.");
			}
			// this is a valid soft evidence
//			return EvidenceType.SOFT_EVIDENCE;
			return null;
		}
		// at this point, can be considered as hard evidence. Check sum to see if this is negative or positive hard evidence
		if (Math.abs(1f-sum) < ERROR_MARGIN) {
			// sum was 1 and was not soft evidence, so it was specifying only 1 state explicitly with 100%
			if (index1stState1 < 0) {
				throw new RuntimeException(prob + " was detected to be a hard evidence, but no state settled to 1 was found.");
			}
//			return EvidenceType.HARD_EVIDENCE;
			return index1stState1;
		} 
		// at this point, sum was not 1 and this is a hard evidence, so it was either only specifying zeros (if sum < 1) or specifying 1 multiple times (if sum > 1)
		if (hasZeros) {
			// at least one state was set at 0%, so this is a negative hard evidence
			if (index1stState0 < 0) {
				throw new RuntimeException(prob + " was detected to be a negative evidence, but no state settled to 0% was found.");
			}
//			return EvidenceType.NEGATIVE_HARD_EVIDENCE;
			return -index1stState0-1;
		}
		// if every specified probability was 1, then it is a negative hard evidence, but we don't know which state to set to 0%
		throw new IllegalArgumentException("The specified list " + prob + ", is likely to be specifying a negative finding, but could not infer from it which state to set to 0%.");
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IPermanentEvidenceInferenceAlgorithm#setAsPermanentEvidence(java.util.Map, boolean)
	 */
	public void setAsPermanentEvidence(Map<INode, List<Float>> evidences, boolean isToDeleteNode){
		// initial assertion
		if (evidences == null || evidences.isEmpty()) {
			return;
		}
		
		// fill the findings
		Map<INode, List<Float>> hardEvidenceEntry = new HashMap<INode, List<Float>>();	// will store elements in evidences which were hard evidences
		Map<INode, List<Float>> entriesToBeDeleted = new HashMap<INode, List<Float>>();	// will store elements in evidences which were not negative hard evidences (because negative evidences will set states to 0%, and shall not be delete node from net)
		List<TreeVariable> nodesToResetEvidenceAtTheEnd = new ArrayList<TreeVariable>(evidences.size());	// TreeVariable#resetEvidence() will be called for nodes in this list will 
		boolean hasProbChange = false;	// this will become true if at least one node had its probability marked for change.
		for (Entry<INode, List<Float>> entry : evidences.entrySet()) {
			INode node = entry.getKey();
			ProbabilisticNode probNode = (ProbabilisticNode) getNet().getNode(node.getName());
			
			// ignore nodes which we did not find
			if (probNode == null) {
				throw new IllegalArgumentException("Probabilistic node " + node + " was not found in probabilistic network.");
			} 
			// extract state of evidence
			List<Float> prob = evidences.get(node);
			if (prob == null) {
				Debug.println(getClass(), "Evidence of node " + node + " was null");
				// ignore this value, but mark it to be deleted posteriorly
				entriesToBeDeleted.put(entry.getKey(), entry.getValue());
				continue;
			}
			
			// special treatment depending on type of evidence
			boolean isNegativeHardEvidence = true;	// this will become false if getEvidenceType(prob) == HARD_EVIDENCE
			// check if this is a soft evidence or hard evidence	
			Integer resolvedState = getResolvedState(prob);
			if (resolvedState == null) {
				// soft evidence
				// this method does not support "permanent" soft evidence. Must use trades instead.
				// check if it is normalized
				float sum = 0f;
				float[] likelihood = new float[prob.size()];	// also, use same loop to convert to an array of float instead of Float
				for (int i = 0; i < prob.size(); i++) {
					Float value = prob.get(i);
					if (isUnspecifiedProb(value)) {
						// unspecified values in soft evidence shall be considered as 0%
						value = 0f;
					}
					sum += value;
					likelihood[i] = value;	// use same loop to fill array of float instead of using list of Float
				}
				// check if it was normalized
				if (Math.abs(sum - 1f) > ERROR_MARGIN) {
					throw new IllegalArgumentException("Soft evidence of question " + node + " doesn't look normalized, because its sum was " + sum);
				}
				
				// at this point, it is normalized.
				
				// TODO we may need to propagate before adding soft evidence, because soft evidence is sensitive to current state of network.
				
				probNode.addLikeliHood(likelihood);
				hasProbChange = true;	// indicate that at least one node was marked for a change
				
				// soft evidences are to be deleted, so put in the mapping
				entriesToBeDeleted.put(entry.getKey(), entry.getValue());

				// by default, reset evidences of soft evidences
				nodesToResetEvidenceAtTheEnd.add(probNode);
				
			} else { 
				// hard evidence (negative or positive)
				if (resolvedState >= 0) {
					// positive (i.e. "normal") hard evidence
					isNegativeHardEvidence = false;
					// also mark this entry as positive (actually, non-negative) hard evidence.
					entriesToBeDeleted.put(entry.getKey(), entry.getValue());	// normal hard evidences will be deleted from net
				} else {
					isNegativeHardEvidence = true;
					resolvedState = -resolvedState - 1;

					// by default, reset evidences of all negative hard evidence
					nodesToResetEvidenceAtTheEnd.add(probNode);
				}
				
				// the following code is common to both types of evidence, because they have similar treatment, differing only by isNegativeHardEvidence
				
				// modify unspecified values to 0 or 1 depending on whether caller was specifying negative hard evidences 
				// (by indicating which states are 0%) or normal hard evidences (by indicating which states are 100%)
				for (int i = 0; i < prob.size(); i++) {
					if (isUnspecifiedProb(prob.get(i))) {
						// change unspecified values to 1 if there were zeros, and 0 if there were no zeros.
						prob.set(i, isNegativeHardEvidence?1f:0f);
					}
				}
				
				// by turning the flag of findings on and providing the desired marginal, we can add any type of hard evidence 
				// (either those which sets specified state to 1 or those which sets specified states to 0)
				probNode.addFinding(resolvedState,isNegativeHardEvidence);	// just to set the flag indicating presence of a finding

				hasProbChange = true;	// indicate that at least one node was marked for a change
				
				// set up the marginal which will set a specified state to 1 or some specified states to 0
				float[] newMarginalToSet = new float[prob.size()];
				for (int i = 0; i < prob.size(); i++) {
					newMarginalToSet[i] = prob.get(i);
				}
				probNode.setMarginalProbabilities(newMarginalToSet);
				
				// mark this entry as a hard evidence
				hardEvidenceEntry.put(entry.getKey(), entry.getValue());
			}
			
		}
		
		// propagate only the probabilities of hard evidences
		if (hasProbChange) {
			try {
				this.propagate();
			} catch (RuntimeException e) {
				// reset evidences from all nodes, so that they are not re-inserted
				for (Node node : getNet().getNodes()) {
					if (node instanceof TreeVariable) {
						((TreeVariable)node).resetEvidence();
					}
				}
				throw e;
			}
		}
		
		
		// delete resolved nodes
		if (isToDeleteNode) {
			for (INode node : entriesToBeDeleted.keySet()) {	// only delete nodes which are not negative hard evidence
				// connect the parents, because they shall be conditionally dependent even after resolution
				if (isToConnectParentsWhenAbsorbingNode()) {
					// iterate over all pairs of parent nodes
					for (int i = 0; i < node.getParentNodes().size()-1; i++) {
						for (int j = i+1; j < node.getParentNodes().size(); j++) {
							// extract the pair of nodes
							Node parent1 = (Node) node.getParentNodes().get(i);
							Node parent2 = (Node) node.getParentNodes().get(j);
							
							// check if there is an arc between these 2 nodes
							if (parent1.getParents().contains(parent2) || parent2.getParents().contains(parent1)){
								// ignore this combination of nodes, because they are already connected
								continue;
							}
							
							if (getMSeparationUtility().getRoutes(parent1, parent2, null, null, 1).isEmpty()) {
								// there is no route from node1 to node2, so we can create parent2->parent1 without generating cycle
								Edge edge = new Edge(parent2,parent1);
								// add edge into the network
								try {
									getNet().addEdge(edge);
								} catch (Exception e) {
									throw new RuntimeException("Could not add edge from " + parent2 + " to " + parent1 + " while absorbing " + node, e);
								}
								// normalize table
								new NormalizeTableFunction().applyFunction((ProbabilisticTable) ((ProbabilisticNode)parent1).getProbabilityFunction());
							} else { // supposedly, we can always add edges in one of the directions (i.e. there is no way we add arc in each direction and both result in cycle)
								// there is a route from node1 to node2, so we cannot create parent2->parent1 (it will create a cycle if we do so), so create parent1->parent2
								Edge edge = new Edge(parent1,parent2);
								// add edge into the network
								try {
									getNet().addEdge(edge);
								} catch (Exception e) {
									throw new RuntimeException("Could not add edge from " + parent1 + " to " + parent2 + " while absorbing " + node, e);
								}
								// normalize table
								new NormalizeTableFunction().applyFunction((ProbabilisticTable) ((ProbabilisticNode)parent2).getProbabilityFunction());
							}
						}
					}
				}
				getNet().removeNode((Node) node); // this will supposedly delete the nodes from cliques as well
			}
			// special treatment if the node to remove makes the clique to become empty
			List<Clique> emptyCliques = new ArrayList<Clique>(); // keep track of which cliques were detected to be empty
			for (Clique clique : getNet().getJunctionTree().getCliques()) {
				// TODO update only the important clique
				if (clique.getProbabilityFunction().tableSize() <= 0) {
					clique.getProbabilityFunction().addVariable(ONE_STATE_PROBNODE);  // this node has only 1 state
					clique.getProbabilityFunction().setValue(0, 1f);	// if there is only 1 possible state, it should have 100% probability4
					// don't forget to keep track of empty cliques, so that we can eventually remove them later if necessary
					emptyCliques.add(clique);
				}
			}
			if (isToDeleteEmptyCliques() && !emptyCliques.isEmpty()) {
				// if we are only using probabilities, then we don't need the empty cliques anymore
				getNet().getJunctionTree().removeCliques(emptyCliques);
//				this.deleteEmptyCliques(emptyCliques, getNet().getJunctionTree());
			}
		}
		if (!isToDeleteNode) {
			// copy all clique/separator potentials
			for (Clique clique : getNet().getJunctionTree().getCliques()) {
				clique.getProbabilityFunction().copyData();
			}
			for (Separator sep : getNet().getJunctionTree().getSeparators()) {
				sep.getProbabilityFunction().copyData();
			}
		}
		
		
		// reset evidences from nodes that were handled already
		for (TreeVariable node : nodesToResetEvidenceAtTheEnd) {
			node.resetEvidence();
		}
		
	}


	/**
	 * This is the option panel related to this algorithm
	 * @return the optionPanel
	 */
	public InferenceAlgorithmOptionPanel getOptionPanel() {
		return optionPanel;
	}


	/**
	 * This is the option panel related to this algorithm
	 * @param optionPanel the optionPanel to set
	 */
	public void setOptionPanel(InferenceAlgorithmOptionPanel optionPanel) {
		this.optionPanel = optionPanel;
	}

	/**
	 * @return the mediator
	 */
	public INetworkMediator getMediator() {
		if (this.getOptionPanel() != null) {
			return this.getOptionPanel().getMediator();
		}
		return null;
	}
	
	/**
	 * @return the resource
	 */
	public static ResourceBundle getResource() {
		return generalResource;
	}

	/**
	 * @param resource the resource to set
	 */
	public static void setResource(ResourceBundle resource) {
		JunctionTreeAlgorithm.generalResource = resource;
	}

	/**
	 * Extract the junction tree from network.
	 * @return
	 * @see SingleEntityNetwork#getJunctionTree()
	 */
	public IJunctionTree getJunctionTree() {
		return getNet().getJunctionTree();
	}

	/**
	 * @return the junctionTreeBuilder 
	 * @see ProbabilisticNetwork#getJunctionTreeBuilder()
	 */
	public IJunctionTreeBuilder getJunctionTreeBuilder() {
		if (getNet() != null) {
			this.junctionTreeBuilder = getNet().getJunctionTreeBuilder();
		}
		return this.junctionTreeBuilder;
	}


	/**
	 * @param junctionTreeBuilder the junctionTreeBuilder to set
	 * @see ProbabilisticNetwork#setJunctionTreeBuilder(IJunctionTreeBuilder)
	 */
	public void setJunctionTreeBuilder(IJunctionTreeBuilder junctionTreeBuilder) {
		this.junctionTreeBuilder = junctionTreeBuilder;
		if (getNet() != null) {
			getNet().setJunctionTreeBuilder(junctionTreeBuilder);
		}
	}
	
	/**
	 * Commands to be executed in {@link #verifyConsistency(ProbabilisticNetwork)}
	 * @return the verifyConsistencyCommandList
	 */
	public List<IJunctionTreeCommand> getVerifyConsistencyCommandList() {
		return verifyConsistencyCommandList;
	}


	/**
	 * Commands to be executed in {@link #verifyConsistency(ProbabilisticNetwork)}
	 * @param verifyConsistencyCommandList the verifyConsistencyCommandList to set
	 */
	public void setVerifyConsistencyCommandList(
			List<IJunctionTreeCommand> verifyConsistencyCommandList) {
		this.verifyConsistencyCommandList = verifyConsistencyCommandList;
	}
	

	/**
	 * This method is used in {@link #sortDecisionNodes(Graph)} and {@link #triangulate(ProbabilisticNetwork)} 
	 * in order to trace decision nodes and its order.
	 * 
	 * @return the sortedDecisionNodes
	 */
	public List<INode> getSortedDecisionNodes() {
		return sortedDecisionNodes;
	}

	/**
	 * This method is used in {@link #sortDecisionNodes(Graph)} and {@link #triangulate(ProbabilisticNetwork)} 
	 * in order to trace decision nodes and its order.
	 * 
	 * @param sortedDecisionNodes the sortedDecisionNodes to set
	 */
	public void setSortedDecisionNodes(List<INode> orderedDecisionNodes) {
		this.sortedDecisionNodes = orderedDecisionNodes;
	}

	
	/**
	 * Objects of this interface represents methods that will be executed by {@link JunctionTreeAlgorithm}
	 * in some methods, like {@link JunctionTreeAlgorithm#verifyConsistency(ProbabilisticNetwork)}
	 * @author Shou Matsumoto
	 *
	 */
	public interface IJunctionTreeCommand {
		/**
		 * Main method of command design pattern
		 * @param algorithm
		 * @param graph
		 */
		public void doAction(IInferenceAlgorithm algorithm, Graph graph);
		
		/**
		 * If possible, this method will revert the {@link #doAction(IJunctionTree, IInferenceAlgorithm, Graph)}
		 * @param algorithm
		 * @param graph
		 * @throws UndoableJTCommandException when {@link #doAction(IJunctionTree, IInferenceAlgorithm, Graph)} is not undoable
		 */
		public void undoAction(IInferenceAlgorithm algorithm, Graph graph) throws UndoableJTCommandException;
	}
	
	/** This exception is thrown by {@link IJunctionTreeCommand#undoAction(IJunctionTree, IInferenceAlgorithm, Graph)} if command is not undoable */
	public class UndoableJTCommandException extends Exception {
		/** @see Exception#Exception() */
		public UndoableJTCommandException() {super();}
		/** @see Exception#Exception(String, Throwable) */
		public UndoableJTCommandException(String message, Throwable cause) {super(message, cause);}
		/** @see Exception#Exception(String) */
		public UndoableJTCommandException(String message) {super(message);}
		/** @see Exception#Exception(Throwable) */
		public UndoableJTCommandException(Throwable cause) {super(cause);}
	}

//	/**
//	 * This is the edges of a moralized bayesian network
//	 * @see JunctionTreeAlgorithm#moralize(ProbabilisticNetwork)
//	 * @return the markovArc
//	 */
//	public List<Edge> getMarkovArc() {
//		return markovArc;
//	}
//
//	/**
//	 * This is the edges of a moralized bayesian network
//	 * @see JunctionTreeAlgorithm#moralize(ProbabilisticNetwork)
//	 * @param markovArc the markovArc to set
//	 */
//	public void setMarkovArc(List<Edge> markovArc) {
//		this.markovArc = markovArc;
//	}

//	/**
//	 * @return the markovArcCpy
//	 */
//	public List<Edge> getMarkovArcCpy() {
//		return markovArcCpy;
//	}
//
//	/**
//	 * @param markovArcCpy the markovArcCpy to set
//	 */
//	public void setMarkovArcCpy(List<Edge> markovArcCpy) {
//		this.markovArcCpy = markovArcCpy;
//	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#addInferencceAlgorithmListener(unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener)
	 */
	public void addInferencceAlgorithmListener(
			IInferenceAlgorithmListener listener) {
		this.getInferenceAlgorithmListeners().add(listener);
	}

	/**
	 * @return the inferenceAlgorithmListeners
	 */
	public List<IInferenceAlgorithmListener> getInferenceAlgorithmListeners() {
		return inferenceAlgorithmListeners;
	}

	/**
	 * @param inferenceAlgorithmListeners the inferenceAlgorithmListeners to set
	 */
	public void setInferenceAlgorithmListeners(
			List<IInferenceAlgorithmListener> inferenceAlgorithmListeners) {
		this.inferenceAlgorithmListeners = inferenceAlgorithmListeners;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#removeInferencceAlgorithmListener(unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener)
	 */
	public void removeInferencceAlgorithmListener(IInferenceAlgorithmListener listener) {
		if (listener == null) {
			if (this.getInferenceAlgorithmListeners() == null) {
				this.setInferenceAlgorithmListeners(new ArrayList<IInferenceAlgorithmListener>());
			} else {
				this.getInferenceAlgorithmListeners().clear();
			}
		} else if (this.getInferenceAlgorithmListeners() != null) {
			this.getInferenceAlgorithmListeners().remove(listener);
		}
	}

	/**
	 * @param mediator the mediator to set
	 */
	public void setMediator(INetworkMediator mediator) {
		if (!mediator.equals(this.getMediator())) {
			throw new IllegalArgumentException("Change the mediator from the option panel instead of from algorithm");
		}
//		if (this.getOptionPanel() != null) {
//			this.getOptionPanel().setMediator(mediator);
//		}
	}

	/**
	 * This is going to be a prefix for names of virtual nodes
	 * created by {@link #addVirtualNode(SingleEntityNetwork, Node, float[])}
	 * @return the virtualNodePrefix
	 */
	public String getVirtualNodePrefix() {
		return virtualNodePrefix;
	}

	/**
	 * This is going to be a prefix for names of virtual nodes
	 * created by {@link #addVirtualNode(SingleEntityNetwork, Node, float[])}
	 * @param virtualNodePrefix the virtualNodePrefix to set
	 */
	public void setVirtualNodePrefix(String virtualNodePrefix) {
		this.virtualNodePrefix = virtualNodePrefix;
	}

	/**
	 * This is a map whose the keys are storing all virtual nodes created in {@link #addVirtualNode(SingleEntityNetwork, Node, float[])},
	 * and the values are storing all cliques and separators created in {@link #addVirtualNode(SingleEntityNetwork, Node, float[])}.
	 * This map is used in {@link #clearVirtualNodes()} in order to revert all changes related to virtual nodes.
	 * @return the virtualNodesToCliquesAndSeparatorsMap
	 */
	public Map<INode,Collection<IRandomVariable>> getVirtualNodesToCliquesAndSeparatorsMap() {
		return virtualNodesToCliquesAndSeparatorsMap;
	}

	/**
	 * This is a map whose the keys are storing all virtual nodes created in {@link #addVirtualNode(SingleEntityNetwork, Node, float[])},
	 * and the values are storing all cliques and separators created in {@link #addVirtualNode(SingleEntityNetwork, Node, float[])}.
	 * This map is used in {@link #clearVirtualNodes()} in order to revert all changes related to virtual nodes.
	 * @param virtualNodesToCliquesAndSeparatorsMap the virtualNodesToCliquesAndSeparatorsMap to set
	 */
	public void setVirtualNodesToCliquesAndSeparatorsMap(Map<INode,Collection<IRandomVariable>> virtualNodes) {
		this.virtualNodesToCliquesAndSeparatorsMap = virtualNodes;
	}

	/**
	 * @return
	 * Given that {@link #addVirtualNode(SingleEntityNetwork, List, float[])} will
	 * create a virtual node close to the node which we are 
	 * adding the soft/likelihood evidence, this method returns the maximum distance
	 * from such node (which we add the evidence) to the virtual node.
	 * @see #addVirtualNode(SingleEntityNetwork, List, float[])
	 */
	public float getVirtualNodePositionRandomness() {
		return virtualNodePositionRandomness;
	}

	/**
	 * 
	 * Given that {@link #addVirtualNode(SingleEntityNetwork, List, float[])} will
	 * create a virtual node close to the node which we are 
	 * adding the soft/likelihood evidence, this method returns the maximum distance
	 * from such node (which we add the evidence) to the virtual node.
	 * @see #addVirtualNode(SingleEntityNetwork, List, float[])
	 * @param virtualNodePositionRandomness the virtualNodePositionRandomness to set
	 */
	public void setVirtualNodePositionRandomness(float virtualNodePositionRandomness) {
		this.virtualNodePositionRandomness = virtualNodePositionRandomness;
	}

	/**
	 * This object is used in {@link #addVirtualNode(List)} in order
	 * to extract the likelihood ratio from a list of nodes.
	 * Change this object if you want it to extract
	 * likelihood ratio in a different manner, or you want the likelihood ratio
	 * to be transformed before it is used in {@link #addVirtualNode(List)}.
	 * @param likelihoodExtractor the likelihoodExtractor to set
	 */
	public void setLikelihoodExtractor(ILikelihoodExtractor likelihoodExtractor) {
		this.likelihoodExtractor = likelihoodExtractor;
	}

	/**
	 * This object is used in {@link #addVirtualNode(List)} in order
	 * to extract the likelihood ratio from a list of nodes.
	 * Change this object if you want it to extract
	 * likelihood ratio in a different manner, or you want the likelihood ratio
	 * to be transformed before it is used in {@link #addVirtualNode(List)}.
	 * @return the likelihoodExtractor
	 */
	public ILikelihoodExtractor getLikelihoodExtractor() {
		return likelihoodExtractor;
	}

	/**
	 * @return if true, this indicates that this junction tree algorithm performs
	 * normalization on clique tables in order to maintain consistency.
	 * If false, this junction tree algorithm does not perform normalization
	 * (e.g. it may be a junction tree algorithm for explanation, instead of probability propagation).
	 * By default, a junction tree algorithm return true here, because it is 
	 * a probability propagation algorithm.
	 */
	public boolean isAlgorithmWithNormalization() {
		return true;
	}
	
	/**
	 * This method clones a probabilistic network .
	 * @param originalProbabilisticNetwork : network to clone
	 * @return the clone of the originalProbabilisticNetwork
	 */
	public ProbabilisticNetwork cloneProbabilisticNetwork(ProbabilisticNetwork originalProbabilisticNetwork) {
		return new ProbabilisticNetworkClone(originalProbabilisticNetwork);
	}
	
	/**
	 * If true, {@link #getJointProbability(Map)} will attempt to 
	 * check whether there is a clique containing all nodes simultaneously,
	 * and then it will attempt to calculate joint probability
	 * without doing propagation.
	 * @param isToCalculateJointProbabilityLocally the isToCalculateJointProbabilityLocally to set
	 */
	public void setToCalculateJointProbabilityLocally(
			boolean isToCalculateJointProbabilityLocally) {
		this.isToCalculateJointProbabilityLocally = isToCalculateJointProbabilityLocally;
	}

	/**
	 * If true, {@link #getJointProbability(Map)} will attempt to 
	 * check whether there is a clique containing all nodes simultaneously,
	 * and then it will attempt to calculate joint probability
	 * without doing propagation.
	 * @return the isToCalculateJointProbabilityLocally
	 */
	public boolean isToCalculateJointProbabilityLocally() {
		return isToCalculateJointProbabilityLocally;
	}


	/**
	 * If true, {@link #getJointProbability(Map)} will rely on {@link JunctionTree#getN()}
	 * to obtain joint probability. This is supposedly faster than calculating
	 * the joint normally.
	 * If you are using an unreliable implementation of {@link JunctionTree},
	 * set this attribute to false.
	 * @param isToUseEstimatedTotalProbability the isToUseEstimatedTotalProbability to set
	 */
	public void setToUseEstimatedTotalProbability(
			boolean isToUseEstimatedTotalProbability) {
		this.isToUseEstimatedTotalProbability = isToUseEstimatedTotalProbability;
	}

	/**
	 * If true, {@link #getJointProbability(Map)} will rely on {@link JunctionTree#getN()}
	 * to obtain joint probability. This is supposedly faster than calculating
	 * the joint normally.
	 * If you are using an unreliable implementation of {@link JunctionTree},
	 * set this attribute to false.
	 * @return the isToUseEstimatedTotalProbability
	 */
	public boolean isToUseEstimatedTotalProbability() {
		return isToUseEstimatedTotalProbability;
	}


	/**
	 * This class represents a clone of a {@link ProbabilisticNetwork},
	 * only in the context of the {@link JunctionTreeAlgorithm} context
	 * (i.e. it only clones attributes necessary for the correct functionality
	 * of {@link JunctionTreeAlgorithm}).
	 * @author Shou Matsumoto
	 */
	public class ProbabilisticNetworkClone extends ProbabilisticNetwork {
		private static final long serialVersionUID = 2863527797831091610L;
		private final ProbabilisticNetwork originalNet;
		/** 
		 * Instantiates a clone of a {@link ProbabilisticNetwork}
		 * @param originalNet : net to be cloned 
		 */
		public ProbabilisticNetworkClone(ProbabilisticNetwork originalNet) {
			super(originalNet.getName());
			this.originalNet = originalNet;
			setCreateLog(originalNet.isCreateLog());
			
			// copy nodes
			for (Node node : originalNet.getNodes()) {
				if (!(node instanceof ProbabilisticNode)) {
					// ignore unknown nodes
					Debug.println(getClass(), node + " is not a ProbabilisticNode and will not be copied.");
					continue;
				}
				// ProbabilisticNode has a clone() method, but it keeps parents and children pointing to old nodes (which may cause future problems)
				ProbabilisticNode newNode = ((ProbabilisticNode)node).basicClone();	// so, use basicClone, which does not clone references
				if (newNode.getProbabilityFunction().getVariablesSize() <= 0) {
					newNode.getProbabilityFunction().addVariable(newNode);
				}
				this.addNode(newNode);
			}
			
			// copy edges
			for (Edge oldEdge : originalNet.getEdges()) {
				Node node1 = this.getNode(oldEdge.getOriginNode().getName());
				Node node2 = this.getNode(oldEdge.getDestinationNode().getName());
				if (node1 == null || node2 == null) {
					Debug.println(getClass(), oldEdge + " has a node which was not copied to the cloned network.");
					continue;
				}
				Edge newEdge = new Edge(node1, node2);
				try {
					this.addEdge(newEdge);
				} catch (InvalidParentException e) {
					throw new RuntimeException("Could not clone edge " + oldEdge +" of network " + originalNet , e);
				}
			}
			
			// copy cpt
			for (Node node : originalNet.getNodes()) {
				if (node instanceof ProbabilisticNode) {
					PotentialTable oldCPT = ((ProbabilisticNode) node).getProbabilityFunction();
					PotentialTable newCPT = ((ProbabilisticNode) this.getNode(node.getName())).getProbabilityFunction();
					// CAUTION: the following code will throw an ArrayIndexOutouBoundException when oldCPT and newCPT have different sizes.
					newCPT.setValues(oldCPT.getValues());	// they supposedly have same size.
				}
			}
			
			// also clone the builder of Junction tree, so that the junction trees generated from the clone are also instances of the same class (useful when JT is customized for some purpose)
			this.setJunctionTreeBuilder(originalNet.getJunctionTreeBuilder());
			
			// instantiate junction tree and copy content
			if (originalNet.getJunctionTree() != null) {
				try {
					// use the builder, so that we instantiate JT from same class when we copy its content.
					this.setJunctionTree(getJunctionTreeBuilder().buildJunctionTree(this));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			
			// mapping between original cliques/separator to copied clique/separator 
			// (cliques are needed posteriorly in order to copy separators, and seps are needed in order to copy relation from node to clique/separators)
			Map<IRandomVariable, IRandomVariable> oldCliqueToNewCliqueMap = new HashMap<IRandomVariable, IRandomVariable>();	// cannot use tree map, because cliques are not comparable
			
			// copy cliques if there are cliques to copy
			if (originalNet.getJunctionTree() != null && originalNet.getJunctionTree().getCliques() != null) {
				for (Clique origClique : originalNet.getJunctionTree().getCliques()) {
					Clique newClique = origClique.clone(this);
					
					// NOTE: this is ignoring utility table and nodes
					
					this.getJunctionTree().getCliques().add(newClique);
					oldCliqueToNewCliqueMap.put(origClique, newClique);
				}
			}
			
			// copy separators if there are separators to copy
			if (originalNet.getJunctionTree() != null && originalNet.getJunctionTree().getSeparators() != null) {
				for (Separator origSeparator : originalNet.getJunctionTree().getSeparators()) {
					boolean hasInvalidNode = false;	// this will be true if a clique contains a node not in Network.
					
					// extract the cliques related to the two cliques that the origSeparator connects
					Clique newClique1 = (Clique) oldCliqueToNewCliqueMap.get(origSeparator.getClique1());
					Clique newClique2 = (Clique) oldCliqueToNewCliqueMap.get(origSeparator.getClique2());
					if (newClique1 == null || newClique2 == null) {
						try {
							Debug.println(getClass(), "Could not clone separator between " + origSeparator.getClique1() + " and " + origSeparator.getClique2());
						} catch (Throwable t) {
							t.printStackTrace();
						}
						continue;
					}
					
					Separator newSeparator = new Separator(newClique1, newClique2);
					newSeparator.setInternalIdentificator(origSeparator.getInternalIdentificator());
					
					// fill the separator's node list
					for (Node origNode : origSeparator.getNodes()) {
						Node newNode = this.getNode(origNode.getName());	
						if (newNode == null) {
							hasInvalidNode = true;
							break;
						}
						newSeparator.getNodes().add(newNode);
					}
					if (hasInvalidNode) {
						// the original clique has a node not present in the net
						continue;
					}
					
					// copy separator potential
					PotentialTable origPotential = origSeparator.getProbabilityFunction();
					PotentialTable newPotential = newSeparator.getProbabilityFunction();
					for (int j = 0; j < origPotential.getVariablesSize(); j++) {
						Node newNode = this.getNode(origPotential.getVariableAt(j).getName());
						if (newNode == null) {
							hasInvalidNode = true;
							break;
						}
						newPotential.addVariable(newNode);
					}
					if (hasInvalidNode) {
						// the original clique has a node not present in the net
						continue;
					}
					
					// copy potential
					newPotential.setValues(origPotential.getValues());	// they supposedly have the same size
					
					// NOTE: this is ignoring utility table and nodes
					this.getJunctionTree().addSeparator(newSeparator);
					oldCliqueToNewCliqueMap.put(origSeparator, newSeparator);
				}
			}
			
			// copy relationship between cliques/separator and nodes
			for (Node origNode : originalNet.getNodes()) {
				ProbabilisticNode newNode = (ProbabilisticNode)this.getNode(origNode.getName());
				if (newNode == null) {
					try {
						Debug.println(getClass(), "Could not find node copied from " + origNode);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					continue;
				}
				newNode.setAssociatedClique(oldCliqueToNewCliqueMap.get(((TreeVariable)origNode).getAssociatedClique()));
				
				// force marginal to have some value
				newNode.initMarginalList();
				if (origNode instanceof TreeVariable) {
					// fill with original marginal
					try {
						// this is faster, but deprecated
						newNode.setMarginalProbabilities(((TreeVariable) origNode).marginalList);
					} catch (Throwable e) {
						// fill one-by-one (slower, but higher compatibility)
						int statesSize = newNode.getStatesSize();
						for (int i = 0; i < statesSize; i++) {
							newNode.setMarginalAt(i, newNode.getMarginalAt(i));
						}
					}
				} 
			}
		}
		/**
		 * @return the network which was used as the basis for this cloned network
		 */
		public ProbabilisticNetwork getOriginalNet() {
			return originalNet;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (super.equals(obj)) {
				return true;
			}
			if (this.getOriginalNet().equals(obj)) {
				return true;
			}
			if (obj instanceof ProbabilisticNetworkClone) {
				ProbabilisticNetworkClone probabilisticNetworkClone = (ProbabilisticNetworkClone) obj;
				return this.getOriginalNet().equals(probabilisticNetworkClone.getOriginalNet());
			}
			return false;
		}
	}
	
	/**
	 * This method updates the CPTs of each nodes based on what the
	 * clique potentials tells about conditional probabilities of nodes
	 * given parents. This is useful to permanently store
	 * cliques which have absorbed virtual/soft evidences.
	 * @return the updated network.
	 */
	public ProbabilisticNetwork updateCPTBasedOnCliques () {
		InCliqueConditionalProbabilityExtractor cptExtractor = (InCliqueConditionalProbabilityExtractor) InCliqueConditionalProbabilityExtractor.newInstance();
		cptExtractor.setToJoinCliquesWhenNoCliqueFound(true);	// force the extractor to join cliques when there is no clique containing all questions simultaneously
		
		NormalizeTableFunction normalizer = new NormalizeTableFunction();	// responsible for normalizing the CPT
		
		for (Node node : getNet().getNodes()) {
			if (node instanceof ProbabilisticNode) {
				
				PotentialTable oldCPT = ((ProbabilisticNode) node).getProbabilityFunction();
				// needs to keep same ordering of variables in newCPT, so I'm passing a sublist of oldCPT.variableList (sublist, because the 1st variable in it is the node itself)
				PotentialTable newCPT = (PotentialTable) cptExtractor.buildCondicionalProbability(node, (List)oldCPT.variableList.subList(1, oldCPT.variableList.size()), getNet(), null);	// null as the algorithm forces the return to be conditional probabilities
				
				// iterate over columns of the table, not over each cell
				for (int indexOf1stCellOfColumn = 0; indexOf1stCellOfColumn < oldCPT.tableSize(); indexOf1stCellOfColumn+=oldCPT.getVariableAt(0).getStatesSize()) {	
					
					// check if parent has hard evidence (in such case, this column will have only 0% prob)
					boolean isImpossibleState = true;
					// iterate over cells in the current column
					for (int stateIndex = 0; stateIndex < oldCPT.getVariableAt(0).getStatesSize(); stateIndex++) {
						if (newCPT.getValue(indexOf1stCellOfColumn + stateIndex) > ERROR_MARGIN) {
							isImpossibleState = false;
							break;
						}
					}
					
					// if it is impossible state, then fill with uniform distribution. If it is a possible state, then update with new distribution
					int statesSize = oldCPT.getVariableAt(0).getStatesSize();	// this is how many cells to fill in this iteration
					if (isImpossibleState) {
						// this is an invalid column in cpt (because parents are impossible), so fill with uniform.
						float value = 1f/statesSize;
						for (int stateIndex = 0; stateIndex < statesSize; stateIndex++) {
							oldCPT.setValue(indexOf1stCellOfColumn+stateIndex,value);
						}
					} else {
						// fill with new CPT
						for (int stateIndex = 0; stateIndex < statesSize; stateIndex++) {
							oldCPT.setValue(indexOf1stCellOfColumn+stateIndex,newCPT.getValue(indexOf1stCellOfColumn+stateIndex));
						}
					}
				}
				// force table to be normalized
				normalizer.applyFunction((ProbabilisticTable) oldCPT);
			}
			
		}
		return this.getNet();
	}
	

	/**
	 * If this is true, then {@link #setAsPermanentEvidence(Map, boolean)} will attempt
	 * to connect parent of resolved nodes when it is configured to remove/absorb resolved nodes.
	 * @return the isToConnectParentsWhenAbsorbingNode
	 */
	public boolean isToConnectParentsWhenAbsorbingNode() {
		return isToConnectParentsWhenAbsorbingNode;
	}

	/**
	 * If this is true, then {@link #setAsPermanentEvidence(Map, boolean)} will attempt
	 * to connect parent of resolved nodes when it is configured to remove/absorb resolved nodes.
	 * @param isToConnectParentsWhenAbsorbingNode the isToConnectParentsWhenAbsorbingNode to set
	 */
	public void setToConnectParentsWhenAbsorbingNode(
			boolean isToConnectParentsWhenAbsorbingNode) {
		this.isToConnectParentsWhenAbsorbingNode = isToConnectParentsWhenAbsorbingNode;
	}
	

	/**
	 * This is used in {@link #removeInferencceAlgorithmListener(IInferenceAlgorithmListener)} while
	 * connecting parents of absorbed nodes, in order to check which direction of arcs will not cause
	 * cycles.
	 * @return the mseparationUtility
	 */
	public MSeparationUtility getMSeparationUtility() {
		return mseparationUtility;
	}

	/**
	 * This is used in {@link #removeInferencceAlgorithmListener(IInferenceAlgorithmListener)} while
	 * connecting parents of absorbed nodes, in order to check which direction of arcs will not cause
	 * cycles.
	 * @param mseparationUtility the mseparationUtility to set
	 */
	public void setMSeparationUtility(MSeparationUtility mseparationUtility) {
		this.mseparationUtility = mseparationUtility;
	}
	

	/**
	 * @return the isToDeleteEmptyCliques : if true, {@link #setAsPermanentEvidence(INode, List, boolean)} will
	 * also delete cliques that became empty due to nodes being permanently removed (i.e. absorbed after setting hard evidences).
	 * If false, empty separators will be kept.
	 */
	public boolean isToDeleteEmptyCliques() {
		return isToDeleteEmptyCliques;
	}

	/**
	 * @param isToDeleteEmptyCliques : if true, {@link #setAsPermanentEvidence(INode, List, boolean)} will
	 * also delete cliques that became empty due to nodes being permanently removed (i.e. absorbed after setting hard evidences).
	 * If false, empty separators will be kept.
	 */
	public void setToDeleteEmptyCliques(boolean isToDeleteEmptyCliques) {
		this.isToDeleteEmptyCliques = isToDeleteEmptyCliques;
	}

	
}
