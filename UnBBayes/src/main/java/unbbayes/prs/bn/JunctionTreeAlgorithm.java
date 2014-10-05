/**
 * 
 */
package unbbayes.prs.bn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import unbbayes.prs.id.UtilityTable;
import unbbayes.util.Debug;
import unbbayes.util.SetToolkit;
import unbbayes.util.dseparation.impl.MSeparationUtility;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener;
import unbbayes.util.extension.bn.inference.IRandomVariableAwareInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.InferenceAlgorithmOptionPanel;

/**
 * Class for junction tree compiling algorithm.
 * It includes basic consistency check for single entity networks.
 * 
 * By now, this is still a wrapper for {@link ProbabilisticNetwork#compile()}.
 * 
 * TODO gradually migrate every compilation routine from ProbabilisticNetwork to here.
 * 
 * @author Shou Matsumoto
 *
 */
public class JunctionTreeAlgorithm implements IRandomVariableAwareInferenceAlgorithm {
	
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

	private List<Edge> markovArc = new ArrayList<Edge>(0);

	private List<Edge> markovArcCpy = new ArrayList<Edge>(0);

	private List<IInferenceAlgorithmListener> inferenceAlgorithmListeners = new ArrayList<IInferenceAlgorithmListener>(0);

	private String virtualNodePrefix = "V_";

	private Map<INode,Collection<IRandomVariable>> virtualNodesToCliquesAndSeparatorsMap = new HashMap<INode,Collection<IRandomVariable>>();

	private float virtualNodePositionRandomness = 400;
  	
	/** Default value of {@link #getLikelihoodExtractor()} */
	public static final ILikelihoodExtractor DEFAULT_LIKELIHOOD_EXTRACTOR = LikelihoodExtractor.newInstance();
	
	private ILikelihoodExtractor likelihoodExtractor = DEFAULT_LIKELIHOOD_EXTRACTOR;

	private boolean isToCalculateJointProbabilityLocally = true;

	private boolean isToUseEstimatedTotalProbability = true;

	/** {@link #run()} will use dynamic junction tree compilation if number of nodes is above this value */
	private int dynamicJunctionTreeNetSizeThreshold = Integer.MAX_VALUE;	// setting to large values will disable dynamic junction tree compilation
	
	/** Set this to true if you need {@link #run()} to throw exception when {@link #runDynamicJunctionTreeCompilation()} fails. */
	private boolean isToHaltOnDynamicJunctionTreeFailure = false;

//	/** Set of nodes detected when {@link #run()} was executed the previous time */
//	private Collection<INode> nodesPreviousRun = new HashSet<INode>();
//	
//	/** Set of arcs (edges) detected when {@link #run()} was executed the previous time */
//	private Collection<Edge> edgesPreviousRun  = new HashSet<Edge>();
	
	/** Copy of the network used when {@link #run()} was executed the previous time */
	private ProbabilisticNetwork netPreviousRun = null;
	
	/** This is the error margin used when comparing probabilities */
	public static final float ERROR_MARGIN = 0.00005f;
	
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
												// store the hard evidence of the new virtual node, so that it can be retrieved after reset
												// hard evidence of virtual node is never a "NOT" evidence (evidence is always about a given particular state, and never about values "NOT" in a given state)
												evidenceMap.put(virtual.getName(), ((TreeVariable) virtual).getEvidence());
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
				try {
					verifyUtility(graph);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		// check cycles
		ret.add(new IJunctionTreeCommand() {
			public void undoAction(IInferenceAlgorithm algorithm, Graph graph) throws UndoableJTCommandException {throw new UndoableJTCommandException();}
			public void doAction(IInferenceAlgorithm algorithm, Graph graph) {
				if (graph instanceof ProbabilisticNetwork) {
					ProbabilisticNetwork net = (ProbabilisticNetwork) graph;
					try {
						net.verifyCycles();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
		
		// check connective
		ret.add(new IJunctionTreeCommand() {
			public void undoAction(IInferenceAlgorithm algorithm, Graph graph) throws UndoableJTCommandException {throw new UndoableJTCommandException();}
			public void doAction(IInferenceAlgorithm algorithm, Graph graph) {
				if (graph instanceof ProbabilisticNetwork) {
					ProbabilisticNetwork net = (ProbabilisticNetwork) graph;
					try {
						net.verifyConectivity();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		});
		
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
					sortDecisionNodes(graph);
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
	 * @throws Exception 
	 */
	protected void sortDecisionNodes(Graph graph) throws Exception {

		if (!(graph instanceof ProbabilisticNetwork)) {
			return;
		}
		
		ProbabilisticNetwork net = (ProbabilisticNetwork) graph;
		
		List<Node> nodeList = net.getNodes();
		
		// reset decision nodes
		this.setSortedDecisionNodes(new ArrayList<INode>());
		List<INode> decisionNodes = this.getSortedDecisionNodes();
		
		int sizeNos = nodeList.size();
		for (int i = 0; i < sizeNos; i++) {
			if (nodeList.get(i).getType() == Node.DECISION_NODE_TYPE) {
				decisionNodes.add(nodeList.get(i));
			}
		}
	
		ArrayList<Node> fila = new ArrayList<Node>();
		fila.ensureCapacity(nodeList.size()); 
		Node aux, aux2, aux3;
	
		int sizeDecisao = decisionNodes.size();
		for (int i = 0; i < sizeDecisao; i++) {
			boolean visitados[] = new boolean[nodeList.size()];
			aux = (Node) decisionNodes.get(i);
			fila.clear();
			fila.add(aux);
	
			while (fila.size() != 0) {
				aux2 = fila.remove(0);
				visitados[nodeList.indexOf(aux2)] = true;
	
				int sizeFilhos = aux2.getChildren().size();
				for (int k = 0; k < sizeFilhos; k++) {
					aux3 = (Node) aux2.getChildren().get(k);
					if (!visitados[nodeList.indexOf(aux3)]) {
						if (aux3.getType() == Node.DECISION_NODE_TYPE
							&& !aux.getAdjacents().contains(aux3)) {
							aux.getAdjacents().add(aux3);
						}
						fila.add(aux3);
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
			try {
				aux = (Node) decisionNodes.get(i);
			} catch (ClassCastException e) {
				try {
					Debug.println(getClass(), e.getMessage(), e);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				continue;
			}
			if (aux != null
					&& ( aux.getAdjacents().size() != decisionNodes.size() - i - 1) ) {
				throw new Exception(resource.getString("DecisionOrderException"));
			}
		}
	
		for (Node node : nodeList) {
			node.clearAdjacents();
		}
	}

	/**
	 * This method is used in {@link #verifyConsistency(ProbabilisticNetwork)}
	 * in order to check consistency of conditional probability tables
	 * @param graph
	 * @throws Exception 
	 */
	protected void verifyPotentialTables(Graph graph) throws Exception {
		if (graph instanceof ProbabilisticNetwork) {
			ProbabilisticNetwork net = (ProbabilisticNetwork) graph;
			ProbabilisticTable auxTabPot;
			int c;
			Node auxNo;
			ProbabilisticNode auxVP;
			
			int sizeNos = net.getNodeCount();
			for (c = 0; c < sizeNos; c++) {
				auxNo = net.getNodes().get(c);
				if (auxNo.getType() == Node.PROBABILISTIC_NODE_TYPE) {
					auxVP = (ProbabilisticNode) auxNo;
					auxTabPot = (ProbabilisticTable) auxVP.getProbabilityFunction();
					auxTabPot.verifyConsistency();
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
		
		// if this is true, we will not attempt to use dynamic junction tree compilation.
		// I'm using a boolean var here instead of putting it directly in the if-clause, 
		// because if an exception if thrown, I want to set this to true and compile junction tree normally
		boolean isToCompileNormally = this.getNet().getNodes().size() <= getDynamicJunctionTreeNetSizeThreshold()
									|| getNetPreviousRun() == null	// if there is no way to retrieve what were the changes from previous net, then just compile normally
									// if there is no junction tree to reuse, then do not use dynamic junction tree compilation
									|| getJunctionTree() == null
									|| getJunctionTree().getCliques() == null
									|| getJunctionTree().getCliques().isEmpty();
		// check if we should use dynamic junction tree compilation
		if ( !isToCompileNormally ) {
			try {
				this.runDynamicJunctionTreeCompilation();
			} catch (Throwable e) {
				Debug.println(getClass(), "Unable to dynamically compile junction tree. Compiling normally...", e);
				if (isToHaltOnDynamicJunctionTreeFailure()) {
					// throw exception if this algorithm is configured to do so
					throw new RuntimeException("Unable to dynamically compile junction tree.", e);
				}
				isToCompileNormally = true;	// set the flag, so that we can run the normal junction tree compilation in the next if-clause
			}
		} 
		
		// ordinal junction tree compilation
		if (isToCompileNormally) {
			try {
				// TODO gradually migrate all compile routines to here
				this.getNet().compile();
//				if (this.getNet().getNodeCount() == 0) {
//					throw new Exception(resource.getString("EmptyNetException"));
//				}
//				if (this.getNet().isCreateLog()) {
//					this.getNet().getLogManager().reset();
//				}
//				this.verifyConsistency(this.getNet());
//				this.moralize(this.getNet());
//				this.triangularize(this.getNet());		
//				
//				this.getNet().compileJT(this.getJunctionTreeBuilder().buildJunctionTree(this.getNet()));
				
				// TODO migrate these GUI code to the plugin infrastructure
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
		}
		
		// update backup of network if necessary
		if (this.getNet().getNodes().size() > getDynamicJunctionTreeNetSizeThreshold()) {
			// disconnect junction tree from network to clone, because there's no need for a clone of the junction tree.
			IJunctionTree junctionTreeNotToIncludeInClone = getJunctionTree();
			getNet().setJunctionTree(null);	// disconnect net with junction tree, so that junction tree is not cloned in next method
			// clone the network, but do not clone the junction tree
			try {
				ProbabilisticNetwork cloneProbabilisticNetwork = this.cloneProbabilisticNetwork(getNet());
//				cloneProbabilisticNetwork.setJunctionTree(null); // we don't need to keep clone of new junction tree
				this.setNetPreviousRun(cloneProbabilisticNetwork);
			} catch (Exception e) {
				// Failed to backup, but this is not fatal
				Debug.println(getClass(), "Failed to backup network for the next dynamic junction tree compilation", e);
			}
			getNet().setJunctionTree(junctionTreeNotToIncludeInClone);	// restore junction tree
		}
		
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
			listener.onAfterRun(this);
		}
	}
	
	/**
	 * Runs the algorithm of Julia Florez in order to reuse the junction tree that was previously compiled.
	 * Please, notice that if there is no previous junction tree, this method will throw a {@link NullPointerException}.
	 * This method relies on {@link #getNetPreviousRun()} in order to retrieve changes in the net structure.
	 * @throws InvalidParentException  when failed to clone arcs in prime subgraph.
	 * @see #getNet()
	 * @see #getJunctionTree()
	 * @see {@link #run()}
	 * @see #setNetPreviousRun(ProbabilisticNetwork)
	 * @see #getDeletedMoralArcs(ProbabilisticNetwork, ProbabilisticNetwork, Collection)
	 * @see #getIncludedMoralArcs(ProbabilisticNetwork, ProbabilisticNetwork, Collection)
	 * @see #getMaximumPrimeSubgraphDecompositionTree(IJunctionTree, Map)
	 * @see #treatAddNode(INode, IJunctionTree)
	 * @see #treatAddEdge(Edge, IJunctionTree)
	 * @see #treatRemoveEdge(Edge, IJunctionTree)
	 * @see #treatRemoveNode(INode, IJunctionTree)
	 * @see #getCompiledPrimeDecompositionSubnets(Collection)
	 * @see #aggregateJunctionTree(IJunctionTree, IJunctionTree, Collection, IJunctionTree, Map)
	 */
    protected void runDynamicJunctionTreeCompilation() throws InvalidParentException {
		// do dynamic junction tree compilation if number of nodes is above a threshold and the previous junction tree is there
		
		
		// extract the network structure used in previous run
		ProbabilisticNetwork oldNet = getNetPreviousRun();
		// extract the network structure to be used in current run
		ProbabilisticNetwork newNet = getNet();
		
		// extract the junction tree to modify
		IJunctionTree junctionTree = getJunctionTree();
		if (junctionTree == null) {
			throw new NullPointerException("Unable to obtain junction tree to dynamically compile.");
		}
		
		// store what nodes were added/deleted to/from the network
		Collection<INode> nodesToDelete = new HashSet<INode>();
		Collection<INode> nodesToAdd = new HashSet<INode>();
		// store what arcs were added/deleted
		Collection<Edge> edgesToDelete = new HashSet<Edge>();
		Collection<Edge> edgesToAdd = new HashSet<Edge>();			// this will not contain edges that overwrites a moralization arc previously present
		Collection<Edge> allConsideredEdges = new HashSet<Edge>();	// this will also contain edges that overwrites a moralization arc previously present
				
		// check modifications in nodes and arcs
		if (oldNet != null) {
			// keep record of what nodes are actually present in current junction tree, 
			// so that we can detect what nodes were deleted/inserted without using this object of JunctionTreeAlgorithm
			// keep record of Node#getName(), because different instances (of nodes) with same names represents same random variable
			Set<String> nodeNamesInJunctionTree = new HashSet<String>();	// use a set in order to avoid duplicates	
			for (Clique clique : newNet.junctionTree.getCliques()) {   // fill names
				for (Node nodeInClique : clique.getNodesList()) {
					nodeNamesInJunctionTree.add(nodeInClique.getName());
				}
			}
			
			// search for nodes that were deleted
			for (INode oldNode : oldNet.getNodes()) {
				// check if node is in new net. If not, it was deleted
				if (newNet.getNodeIndex(oldNode.getName()) < 0) {
					// if node is absent from junction tree already, then we don't need to consider it
					if (nodeNamesInJunctionTree.contains(oldNode.getName())) {
						// node is still in junction tree, so we need to remove it from junction tree
						nodesToDelete.add(oldNode);
					}
				}
			}
			
			// search for nodes that were included
			for (Node newNode : newNet.getNodes()) {
				if (!(newNode instanceof ProbabilisticNode)) {
					throw new ClassCastException(newNode + " is not a probabilistic node, thus it cannot be handled by this algorithm.");
				}
				// if node is in new net, but not in old net, then it is a new node
				if (oldNet.getNodeIndex(newNode.getName()) < 0) {
					// if node is present junction tree already, then we don't need to consider it
					if (!nodeNamesInJunctionTree.contains(newNode.getName())) {
						// node was not in junction tree, so we need to add it to junction tree
						nodesToAdd.add(newNode);
					}
				}
			}
			
			// search for arcs that were deleted
			for (Edge oldEdge : oldNet.getEdges()) {
				// we know that getEdges() is an ArrayList, and that it uses Object#equals(Object).
				// the Edge#equals(Edge) uses name comparison of nodes it connects.
				// This makes this if-clause to work even when the Edge objects are not exactly the same instances (i.e. we can compare node with its clone).
				if (!newNet.getEdges().contains(oldEdge)) {
					edgesToDelete.add(oldEdge);
				}
			}
			// search for arcs that were included
			for (Edge newEdge : newNet.getEdges()) {
				// we know that getEdges() is an ArrayList, and that it uses Object#equals(Object).
				// the Edge#equals(Edge) uses name comparison of nodes it connects.
				// This makes this if-clause to work even when the Edge objects are not exactly the same instances (i.e. we can compare node with its clone).
				if (!oldNet.getEdges().contains(newEdge)) {
					allConsideredEdges.add(newEdge);	// this is a list of all new edges, regardless of whether it overwrites a pre-existing moralization arc or not
					
					// we should not consider an edge as "new" if it is just connecting parents that was already "connected" by a "moralization arc" in the old net
					// extract the nodes in the old net
					Node oldOriginNode = oldNet.getNode(newEdge.getOriginNode().getName());
					Node oldDestinationNode = oldNet.getNode(newEdge.getDestinationNode().getName());
					if (oldOriginNode == null || oldDestinationNode == null) {
						// the nodes were not present previously, so there is no sense in checking whether the nodes were moralized in previous net
						edgesToAdd.add(newEdge);
					} else {
						// check common children
						List<INode> commonChildren = new ArrayList<INode>(oldOriginNode.getChildNodes());
						commonChildren.retainAll(oldDestinationNode.getChildNodes());	// this gives an intersection
						if (commonChildren.isEmpty()) {
							// they did not have common children, so they did not have any "moralziation arc" previously, so the new edge is actually "new".
							edgesToAdd.add(newEdge);
						}
					}
				}
			}
			// TODO handle cases when edges were included/excluded and junction tree handled by an instance other than this algorithm
		}
		
		
		// This algorithm also needs to track which moral connections (i.e. implicit connections between parents with common child) were deleted because of arc deletion.
		if (!edgesToDelete.isEmpty()) { // I'm assuming that if we never deleted any edge, there is no old moral arc deleted too
			edgesToDelete.addAll(getDeletedMoralArcs(oldNet, newNet, edgesToDelete));
		}
		
		// This algorithm also needs to track which moral connections (i.e. implicit connections between parents with common child) were created because of new arcs.
		if (!edgesToAdd.isEmpty()) { // I'm assuming that if we never included any edge, there is no new moral arc too
			edgesToAdd.addAll(getIncludedMoralArcs(oldNet, newNet, allConsideredEdges));
		}
		
		// if there was no modification, we should not do anything
		if (edgesToAdd.isEmpty()
				&& nodesToAdd.isEmpty()
				&& edgesToDelete.isEmpty()
				&& nodesToDelete.isEmpty()) {
			return;
		}
		
		// obtain the maximum prime subgraph decomposition tree, which will be used to isolate modifications 
		// (i.e. check which portion of JT can be reused, and which portions shall be recompiled)
		IJunctionTree decompositionTree = null;
		// also, this will be a map relating a cluster included in the max prime subgraph decomposition to the clique in the original junction tree
		Map<Clique, Collection<Clique>> clusterToOriginalCliqueMap = new HashMap<Clique, Collection<Clique>>();
		try {
			// the junction tree here needs to be the old junction tree, prior to modifications.
			decompositionTree = getMaximumPrimeSubgraphDecompositionTree(junctionTree, clusterToOriginalCliqueMap); // also fill clusterToOriginalCliqueMap
		} catch (Exception e) {
			throw new RuntimeException("Unable to obtain maximum prime subgraph decomposition tree from the current junction tree.",e);
		}
		
		// this set will be filled with clusters in the maximum prime subgraph decomposition tree which needs to be modified.
		Set<Clique> clustersToModify = new HashSet<Clique>();
		
		// identify which clusters needs to be modified, accordingly to the type of modification (e.g. new nodes, new arcs, or deletions)
		for (INode includedNode : nodesToAdd) {
			clustersToModify.addAll(this.treatAddNode(includedNode, junctionTree, decompositionTree, clusterToOriginalCliqueMap));
		}
		for (INode deletedNode : nodesToDelete) {
			clustersToModify.addAll(this.treatRemoveNode(deletedNode, junctionTree, decompositionTree, clusterToOriginalCliqueMap));
		}
		for (Edge includedEdge : edgesToAdd) {
			clustersToModify.addAll(this.treatAddEdge(includedEdge, junctionTree, decompositionTree, clusterToOriginalCliqueMap));
		}
		for (Edge deletedEdge : edgesToDelete) {
			clustersToModify.addAll(this.treatRemoveEdge(deletedEdge, junctionTree, decompositionTree, clusterToOriginalCliqueMap));
		}
		
		// We don't need the list of modification in net structure anymore. 
		// Release them, because we'll have a memory-intense operation (compilation of junction trees of max prime subgraphs) now
		nodesToAdd.clear(); nodesToAdd = null;
		nodesToDelete.clear() ; nodesToDelete  = null;
		edgesToAdd.clear(); edgesToAdd = null;
		edgesToDelete.clear() ; edgesToDelete  = null;
		
		// for each connected marked clusters, retrieve the nodes in prime subnet decomposition and compile junction tree for each of subnets
		Map<IJunctionTree, Collection<Clique>> compiledSubnetToClusterMap = this.getCompiledPrimeDecompositionSubnets(clustersToModify);

		// aggregate the junction tree of the subnets to the original junction tree (removing unnecessary cliques)
		for (Entry<IJunctionTree, Collection<Clique>> entry : compiledSubnetToClusterMap.entrySet()) {
			this.aggregateJunctionTree(junctionTree, entry.getKey(), entry.getValue(), 
//					decompositionTree, 
					clusterToOriginalCliqueMap);
		}
		
		
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
		// do the same for separators
		int separatorIndex = -1;
		for (Separator sep : junctionTree.getSeparators()) {
			sep.setInternalIdentificator(separatorIndex--);
		}
		
		// make sure the junction tree is still globally consistent
//		try {
//			if (junctionTree instanceof JunctionTree) {
//				// a special type of propagation that is optimized for assuring global consistency at the 1st-time we run compilatin
//				((JunctionTree) junctionTree).initConsistency();
//			} else {
//				// ordinal junction tree propagation to assure global consistency
//				junctionTree.consistency();
//			}
//		} catch (Exception e) {
//			throw new RuntimeException("Failed to assure global consistency of dynamically compiled junction tree.", e);
//		}
		
		// make sure marginals are up to date
//		getNet().resetNodesCopy();
//		getNet().updateMarginals();
		
		// We don't need the junction tree of max prime subgraphs anymore. 
		// Release them, because we have a memory-intense operation (i.e. backup) now.
//		compiledSubnetToClusterMap.clear(); 
//		compiledSubnetToClusterMap = null; 
//		// we don't need the max prime decomposition tree and its clusters too
//		decompositionTree = null;
//		clusterToOriginalCliqueMap.clear(); 
//		clusterToOriginalCliqueMap = null;
//		clustersToModify.clear(); 
//		clustersToModify = null;
		
		// the caller shall make the backup of the network
	}

    /**
     * Will connect the original junction tree to the new junction tree compiled from max prime subgraph decomposition.
     * @param originalJunctionTree : junction tree whose new junction trees obtained from max prime subgraphs will be aggregated to.
     * @param primeSubgraphJunctionTree : this is a fragment of junction tree created by compiling nodes in the connected maximum prime subgraph
     * marked for modification. Cliques in this junction tree will be aggregated to original junction tree.
     * @param modifiedClusters : clusters that belongs to the prime subgraph junction tree.
     * This parameter is necessary because we need to find out which separators connects these clusters to clusters not in this collection.
     * @param clusterToOriginalCliqueMap : this map associates clusters in max prime subgraph decomposition tree to cliques in original junction tree. 
     * It it used to retrieve cliques of original junction tree related to the cluster currently being evaluated.
     * @see #runDynamicJunctionTreeCompilation()
     */
//    * @param maxPrimeSubgraphDecompositionTree : this is the entire maximum prime subgraph decomposition tree
//    * obtained from {@link #getMaximumPrimeSubgraphDecompositionTree(IJunctionTree, Map)}.
    protected void aggregateJunctionTree(IJunctionTree originalJunctionTree, IJunctionTree primeSubgraphJunctionTree, Collection<Clique> modifiedClusters,	// main parameters
//			IJunctionTree maxPrimeSubgraphDecompositionTree,
			Map<Clique, Collection<Clique>> clusterToOriginalCliqueMap) {									// auxiliary parameters for reference

    	// basic assertions
    	if (originalJunctionTree == null || primeSubgraphJunctionTree == null || modifiedClusters == null || modifiedClusters.isEmpty()) {
    		return;	// can't do anything without the main arguments
    	}
    	
		// also build an inverse mapping from original clique to generated clusters in max prime subgraph decomposition tree, 
    	// because we'll use it later to check if a clique was marked for modification
		Map<Clique, Clique> originalCliqueToClusterMap = new HashMap<Clique, Clique>();
		// invert the clusterToOriginalCliqueMap and write it to originalCliqueToClusterMap
		for (Entry<Clique, Collection<Clique>> clusterToOriginalCliques : clusterToOriginalCliqueMap.entrySet()) {
			for (Clique originalClique : clusterToOriginalCliques.getValue()) {
				originalCliqueToClusterMap.put(originalClique, clusterToOriginalCliques.getKey());
			}
		}
    	
    	// get separators pointing to/from the clusters (not between the clusters), so that we can iterate on them
    	// but first, get the cliques related to the clusters
		Set<Clique> modifiedCliques = new HashSet<Clique>();
    	for (Clique cluster : modifiedClusters) {
			modifiedCliques.addAll(clusterToOriginalCliqueMap.get(cluster));
		}
    	// for each of the cliques, get separators that connects modified and not modified cliques
    	Set<Separator> borderSeparators = new HashSet<Separator>();	// this will be filled with separators that are in the border between modified and not modified cliques
    	for (Clique modifiedClique : modifiedCliques) { // only needs to check separators connected to these cliques
			// check parent
    		if (modifiedClique.getParent() != null
    				&& !modifiedClusters.contains(originalCliqueToClusterMap.get(modifiedClique.getParent()))) {
    			// cluster related to this parent clique was not modified, so this is a separator between modified and not modified cliques
    			borderSeparators.add(originalJunctionTree.getSeparator(modifiedClique.getParent(), modifiedClique));
    		}
    		// check children
    		if (modifiedClique.getChildren() != null) {
    			for (Clique child : modifiedClique.getChildren()) {
    				if (!modifiedClusters.contains(originalCliqueToClusterMap.get(child))) {
    	    			// cluster related to this clique was not modified, so this is a separator between modified and not modified cliques
    	    			borderSeparators.add(originalJunctionTree.getSeparator(modifiedClique, child));
    	    		}
    			}
    		}
		}
    	
    	
    	// now, iterate on separators in order to substitute them with connections between new subtrees and the original junction tree
    	for (Separator borderSeparator : borderSeparators) {
    		// border separator connects modified clique with unmodified clique. 
    		Clique unchangedOriginalClique = borderSeparator.getClique1(); 	// Extract the unchanged clique.
    		Clique modifiedOriginalClique = borderSeparator.getClique2();	// Extract the modified clique (to be deleted after this process)
    		if (modifiedCliques.contains(unchangedOriginalClique)) {
    			// clique 1 was the modified one
    			modifiedOriginalClique 	 = borderSeparator.getClique1();
    			unchangedOriginalClique = borderSeparator.getClique2();
    		}
    		
			// find clique in new prime subgraph junction tree whose intersection with the unmodified clique is maximal
    		Clique newCliqueInMaxPrimeJunctionTree = null;
			try {
				newCliqueInMaxPrimeJunctionTree = primeSubgraphJunctionTree.getCliquesContainingMostOfNodes((Collection)unchangedOriginalClique.getNodesList()).get(0);
			} catch (Exception e) {
				throw new RuntimeException("Unable to find clique in max prime subgraph decomposition junction tree containing at least one node in " + unchangedOriginalClique);
			}
			
			// at this point, the new clique (with maximum intersection) shall not be null
			
			/*
			 * In terms of whether it was modified or not, the original junction tree looks like the following:
			 * 
			 * UnchangedClique --BorderSeparator--> {subtree of modified cliques} --{set of border separators}--> {set of other unchanged cliques}.
			 * 
			 * In this model, UnchangedClique is an ancestor clique (parent, or parent of parent, or so on) of all cliques in {subtree of modified cliques} 
			 * and {set of other unchanged cliques}.
			 * The cliques in {subtree of modified cliques} will be substituted with the new junction tree compiled from the max prime subgraph.
			 * 
			 * Therefore, given a single border separator, there are only 2 possible scenarios.
			 * 
			 * 1 - The unchanged clique is a parent of the modified subtree. This can happen only with 1 border separator, due to properties of tree structures.
			 * 	   In this case, a new clique (of the junction tree compiled from max prime subgraph) will become a child of the unchanged clique.
			 *     This is the case when we need to move the new clique to the root of the junction tree of the max prime subgraph,
			 *     in order to keep the hierarchy consistent (e.g. keep unique root).
			 * 
			 * 2 - The unchanged clique is a child of the modified subtree. This happens to all the other border separators.
			 *     In this case, a new clique (of the junction tree compiled from max prime subgraph) will become a parent of the unchanged clique.
			 */
			
			// Connect the new clique to the original unmodified clique, by the border separator; 
			if (modifiedOriginalClique.getParent() != null && modifiedOriginalClique.getParent().equals(unchangedOriginalClique)) {	
				// case 1 - new clique becomes a child of original unchanged clique
				
				// move the new clique to root of its junction tree, so that it gets easier to connect to original junction tree just by adding it to list of children
				((JunctionTree)primeSubgraphJunctionTree).moveCliqueToRoot(newCliqueInMaxPrimeJunctionTree);
				if (newCliqueInMaxPrimeJunctionTree.getParent() != null) {
					throw new RuntimeException("Unable to move clique " + newCliqueInMaxPrimeJunctionTree 
							+ " to root of junction tree fragment generated from max prime subgraph decomposition.");
				}
				
				// if new clique and border separator are the same (i.e. represents same joint space), then join original (unchanged) clique and new clique;
				if (borderSeparator.getNodes().size() == newCliqueInMaxPrimeJunctionTree.getNodesList().size()
						&& borderSeparator.getNodes().contains(newCliqueInMaxPrimeJunctionTree.getNodesList())) {
					// if they have the same size, and one is included in other, then they are the same
					
					// join cliques. The unchanged original clique will remain, and the new clique (the one obtained from max prime subtree) will be deleted
					unchangedOriginalClique.join(newCliqueInMaxPrimeJunctionTree);
					
					// set the joined clique as parent of all children of the clique to delete
					for (Clique childClique : newCliqueInMaxPrimeJunctionTree.getChildren()) {
						
						// inherit children of the clique that is going to be "deleted" (actually, it simply won't be included to original junction tree)
						unchangedOriginalClique.addChild(childClique);
						childClique.setParent(unchangedOriginalClique);
						
						// also create separator with this child clique, 
						
						// but before that, we need to delete separator from the max prime subtree, so that it won't be referenced later
						Separator separatorToDelete = primeSubgraphJunctionTree.getSeparator(newCliqueInMaxPrimeJunctionTree, childClique);
						primeSubgraphJunctionTree.removeSeparator(separatorToDelete);
						
						// now, create the separator in the original net
						Separator newSeparator = new Separator(unchangedOriginalClique, childClique, false);
						
						// copy content of deleted separator
						newSeparator.setInternalIdentificator(separatorToDelete.getInternalIdentificator());
						newSeparator.setNodes(separatorToDelete.getNodes());
						
						// Just reuse the same instance of table of old separator, because discarded the old separator anyway.
						newSeparator.setProbabilityFunction(separatorToDelete.getProbabilityFunction());
						
						// separators between new cliques (of junction trees built from max prime subgraph) and original cliques 
						// (of the junction tree we had before running dynamic compilation) needs to be filled with 1 (the null value in multiplication and division), 
						// so that when we run initial propagation (for global consistency) the local probabilities of original cliques and new cliques gets consistently 
						// merged without being biased by content of old separator table.
//						newSeparator.getProbabilityFunction().fillTable(1f);
						// TODO check if the above is necessary
						
						// finally, include separator to original junction tree
						originalJunctionTree.addSeparator(newSeparator);
					}
					
					// remove new clique from max prime subtree. This will make sure it won't be referenced later
					primeSubgraphJunctionTree.getCliques().remove(newCliqueInMaxPrimeJunctionTree);
					
				} else {
					// set new clique as a child of old unchanged clique
					newCliqueInMaxPrimeJunctionTree.setParent(unchangedOriginalClique);
					unchangedOriginalClique.addChild(newCliqueInMaxPrimeJunctionTree);
					
					// The border separator needs to be replaced, because we cannot change the cliques it points to/from;
					Separator newSeparator = new Separator(unchangedOriginalClique, newCliqueInMaxPrimeJunctionTree, false);
					// copy content of border separator
					newSeparator.setInternalIdentificator(borderSeparator.getInternalIdentificator());
					newSeparator.setNodes(borderSeparator.getNodes());
					
					// Again, just reuse the same instance of table of border separator, because we'll discard the border separator anyway.
					newSeparator.setProbabilityFunction(borderSeparator.getProbabilityFunction());
					
					// Again, separators between new cliques (of junction trees built from max prime subgraph) and original cliques 
					// (of the junction tree we had before running dynamic compilation) needs to be filled with 1 (the null value in multiplication and division), 
					// so that when we run initial propagation (for global consistency) the local probabilities of original cliques and new cliques gets consistently 
					// merged without being biased by content of old separator table.
//					newSeparator.getProbabilityFunction().fillTable(1f);
					// TODO check if the above is necessary
					
					// this will substitute the border separator, once the previous border separator is deleted
					originalJunctionTree.addSeparator(newSeparator);
					
				}
				
				// the border separator needs to be deleted regardless of whether the new clique was merged/joined or not
				originalJunctionTree.removeSeparator(borderSeparator);
				
				// disconnect the old child from parent
				unchangedOriginalClique.removeChild(modifiedOriginalClique);
				modifiedOriginalClique.setParent(null);
				
				
			} else {
				// case 2 - new clique becomes a parent of original unchanged clique
				
				// disconnect unchanged clique (child) from its current parent
				if (unchangedOriginalClique.getParent() != null) {
					unchangedOriginalClique.getParent().removeChild(unchangedOriginalClique);
				}
				
				// if new clique and border separator are the same (i.e. represents same joint space), then join original (unchanged) clique and new clique;
				if (borderSeparator.getNodes().size() == newCliqueInMaxPrimeJunctionTree.getNodesList().size()
						&& borderSeparator.getNodes().contains(newCliqueInMaxPrimeJunctionTree.getNodesList())) {
					// if they have the same size, and one is included in other, then they are the same
				
					// join cliques. The new clique will remain, and the unchanged original clique will be deleted
					newCliqueInMaxPrimeJunctionTree.join(unchangedOriginalClique);
					
					// needs to delete the unchanged clique (the one absorbed by the new clique) from original join tree, because newCliqueInMaxPrimeJunctionTree will take its role
					originalJunctionTree.getCliques().remove(unchangedOriginalClique);
					
					// set the resulting (joined) clique as a parent of all children of the clique being deleted
					for (Clique childClique : unchangedOriginalClique.getChildren()) {
						
						// inherit children of the clique that is going to be "deleted" (actually, it simply won't be included to original junction tree)
						newCliqueInMaxPrimeJunctionTree.addChild(childClique);
						childClique.setParent(newCliqueInMaxPrimeJunctionTree);
						
						// also create separator with this child clique, 
						
						// but before that, we need to delete separator from the original junction tree, so that it won't be referenced later
						Separator separatorToDelete = originalJunctionTree.getSeparator(unchangedOriginalClique, childClique);
						originalJunctionTree.removeSeparator(separatorToDelete);
						
						// now, create the separator in the original net
						Separator newSeparator = new Separator(newCliqueInMaxPrimeJunctionTree, childClique, false);
						
						// copy content of deleted separator
						newSeparator.setInternalIdentificator(separatorToDelete.getInternalIdentificator());
						newSeparator.setNodes(separatorToDelete.getNodes());
						
						// just reusing the same instance of table, because the old separator was deleted anyway
						newSeparator.setProbabilityFunction(separatorToDelete.getProbabilityFunction());
						
						// Again and again... Separators between new cliques (of junction trees built from max prime subgraph) and original cliques 
						// (of the junction tree we had before running dynamic compilation) needs to be filled with 1 (the null value in multiplication and division), 
						// so that when we run initial propagation (for global consistency) the local probabilities of original cliques and new cliques gets consistently 
						// merged without being biased by content of old separator table.
//						newSeparator.getProbabilityFunction().fillTable(1f);
						// TODO check if the above is necessary
						
						// finally, include separator to original junction tree
						originalJunctionTree.addSeparator(newSeparator);
					}
					
				} else {
					// set new clique as a parent of old unchanged clique
					unchangedOriginalClique.setParent(newCliqueInMaxPrimeJunctionTree);
					newCliqueInMaxPrimeJunctionTree.addChild(unchangedOriginalClique);
					
					// Again, the border separator needs to be replaced, because we cannot change the cliques it points to/from;
					Separator newSeparator = new Separator(newCliqueInMaxPrimeJunctionTree, unchangedOriginalClique, false);
					// copy content of border separator
					newSeparator.setInternalIdentificator(borderSeparator.getInternalIdentificator());
					newSeparator.setNodes(borderSeparator.getNodes());
					
					// again, reuse the same instance of border separator's table, because the border separator will be deleted anyway
					newSeparator.setProbabilityFunction(borderSeparator.getProbabilityFunction());
					
					// Again and again and again... Separators between new cliques (of junction trees built from max prime subgraph) and original cliques 
					// (of the junction tree we had before running dynamic compilation) needs to be filled with 1 (the null value in multiplication and division), 
					// so that when we run initial propagation (for global consistency) the local probabilities of original cliques and new cliques gets consistently 
					// merged without being biased by content of old separator table.
//					newSeparator.getProbabilityFunction().fillTable(1f);
					
					// replace the border separator
					originalJunctionTree.addSeparator(newSeparator);
				}
				
				// the border separator needs to be deleted regardless of whether the new clique was merged/joined or not
				originalJunctionTree.removeSeparator(borderSeparator);
				
			}
		}	// end of iteration on border separators
    	
    	// Do not forget to include all the new cliques into the original junction tree;
    	for (Clique newClique : primeSubgraphJunctionTree.getCliques()) {
    		// TODO should we check for duplicates?
			originalJunctionTree.getCliques().add(newClique);
		}
    	// do the same for separators in max prime subtree;
    	for (Separator separator : primeSubgraphJunctionTree.getSeparators()) {
			originalJunctionTree.addSeparator(separator);
		}
    	
    	// Now, we need to delete all the modified (old) cliques and separators from original junction tree;
    	
    	// first, delete the separators that pairwise connects modified cliques (because we only deleted the border separators)
    	Set<Separator> separatorsToDelete = new HashSet<Separator>(); // this is to avoid deleting and reading concurrently (which may cause exceptions)
    	for (Separator separator : originalJunctionTree.getSeparators()) {
			if (modifiedCliques.contains(separator.getClique1())
					|| modifiedCliques.contains(separator.getClique2())) {
				// this is a separator connecting cliques to be deleted (this is a sufficient condition), so delete it
				separatorsToDelete.add(separator);
			}
		} // TODO assuming that number of modified cliques is small, isn't it faster to iterate on pairs of modified cliques?
    	
    	// use the IJunctionTree#removeSeparator(Separator), so that internal indexes (for faster access) are also removed
    	for (Separator separator : separatorsToDelete) {
			originalJunctionTree.removeSeparator(separator);
		}
    	
    	// then, delete all old (modified) cliques
    	originalJunctionTree.getCliques().removeAll(modifiedCliques);
    	
	}


	/**
	 * For nodes in each connected clusters marked for modification, compile a junction tree.
     * @param clustersToCompile : these are the clusters in prime subgraph decomposition tree to be used
     * @return map from the generated junction trees to clusters that has generated the respective junction tree. 
     * By extracting the keys, you can obtain the set of junction trees generated from the argument.
     * By obtaining the values, you can obtain which cluster originated the junction tree
     * @throws InvalidParentException  when failing to clone arcs while generating a clone of the subnetwork.
     * @see #runDynamicJunctionTreeCompilation()
     * @see IJunctionTree#initBeliefs()
     * @see #getNet()
     */
    protected Map<IJunctionTree,Collection<Clique>> getCompiledPrimeDecompositionSubnets( Collection<Clique> clustersToCompile) throws InvalidParentException {
    	// basic assertion
    	if (clustersToCompile == null || clustersToCompile.isEmpty()) {
    		return Collections.EMPTY_MAP;
    	}
		
    	// the map to return
    	Map<IJunctionTree,Collection<Clique>> ret = new HashMap<IJunctionTree,Collection<Clique>>();
    	
    	// if clusters are in this set, then they were processed already
		Set<Clique> processedClusters = new HashSet<Clique>();	
		
		// collect nodes in connected clusters, so that we can build a bayes net from it and compile
		for (Clique cluster : clustersToCompile) {
			// this loop is just to guarantee that all clusters will be processed, regardless of being connected or not each other
			if (!processedClusters.contains(cluster)) { // if current cluster was connected to some previous cluster, it was processed already
				
				// this var will be used to store which clusters were processed before this iteration, 
				// so that we can check which clusters were processed in this iteration
				Set<Clique> clustersProcessedBeforeThisIteration = new HashSet<Clique>(processedClusters);
				
				// obtain nodes in current and connected marked clusters (a cluster is marked if it is in clustersToCompile)
				Set<INode> originalNodes = getNodesInConnectedClustersRecursively(cluster, clustersToCompile, processedClusters);	// this will also update processedClusters
				// Note: above method must insert current cluster to processedClusters
				
				Set<Clique> clustersProcessedInThisIteration = new HashSet<Clique>(processedClusters);
				clustersProcessedInThisIteration.removeAll(clustersProcessedBeforeThisIteration);
				
				
				// we don't need these clusters anymore, so purge them
				clustersProcessedBeforeThisIteration.clear();
				clustersProcessedBeforeThisIteration = null;
				
				// generate a bayes net to add node
				ProbabilisticNetwork subnet = new ProbabilisticNetwork("subnet" + cluster.getInternalIdentificator());
				
				// add nodes that belong to current cluster and connected clusters
				for (INode nodeToAdd : originalNodes) {
					// use a clone instead of the original node, so that we don't change original
					ProbabilisticNode clone = ((ProbabilisticNode)nodeToAdd).basicClone();
					// check if the basic clone did clone the 1st element in cpt (i.e. the node itself)
					if (clone.getProbabilityFunction().getVariablesSize() < 1) {
						// make sure the node in index 0 of cpt is the owner of the cpt
						clone.getProbabilityFunction().addVariable(clone);
					}
					subnet.addNode(clone);
				}
				
				// Now that all necessary nodes were included to the subnet, add edges related to nodes we just added;
				for (INode originalChild : originalNodes) {
					// extract the object in the subnet. They have the same name, but are different instances (because nodes in subnet are clones)
					Node childInSubnet = subnet.getNode(originalChild.getName());
					// add edges that will complete the parents.
					for (INode originalParent : originalChild.getParentNodes()) {
						// check if parent is in subnet
						Node parentInSubnet = subnet.getNode(originalParent.getName());
						if (parentInSubnet != null) {
							// both parent and child exist in subnet, so add arc
							Edge edge = new Edge(parentInSubnet, childInSubnet);
							subnet.addEdge(edge);	// this will also automatically update cpts, so we need to overwrite cpts later
						}
					}
				}
				
				// cpt of nodes must be kept the same of original net, so that potentials can be filled correctly by JunctionTree#initBeliefs();
				for (INode originalNode : originalNodes) {
					if (originalNode instanceof ProbabilisticNode) {
						// extract the CPTs
						PotentialTable originalTable = ((ProbabilisticNode) originalNode).getProbabilityFunction();
						
						// clear the cpt of the node in subnet
						PotentialTable tableToOverwrite = ((ProbabilisticNode) subnet.getNode(originalNode.getName())).getProbabilityFunction();
						while (tableToOverwrite.getVariablesSize() > 1) {	
							// keep the 1st element (at index 0), because it is the owner of the cpt. 
							tableToOverwrite.removeVariable(tableToOverwrite.getVariableAt(1)); // Remove others
						}
						
						// Copy the content of original cpt to current cpt.
						// By doing this, I'm actually including dependences in CPTs without adding arcs.
						// This allows JunctionTree#initBeliefs() to initialize clique potentials that are consistent with original net.
						for (int i = 1; i < originalTable.getVariablesSize(); i++) { // make sure we add variables without braking ordering
							// check if we can find this variable in subnet
							INode nodeToAdd = subnet.getNode(originalTable.getVariableAt(i).getName()); 
							if (nodeToAdd == null) { // No equivalent node in subnet.
								// I'm adding nodes from original net, but this should be fine given the way JunctionTree#initBeliefs() works.
								nodeToAdd  = originalTable.getVariableAt(i);
							}
							tableToOverwrite.addVariable(nodeToAdd);
						}
						
						// Now, the size of table and nodes referenced by it (and their order) are consistent. 
						tableToOverwrite.setValues(originalTable.getValues()); // Overwrite probabilities at once, since the ordering of variables supposedly matches.
					}
				}
				
				// This will compile a junction tree for the subnet. 
				JunctionTreeAlgorithm algorithm = new JunctionTreeAlgorithm(subnet);
				// Make sure ordinal junction tree compilation is used (i.e. we don't call dynamic compilation again)
				algorithm.setDynamicJunctionTreeNetSizeThreshold(Integer.MAX_VALUE);	// this shall guarantee that dynamic compilation is disabled
				subnet.setJunctionTree(null);											// this will also guarantee dynamic compilation to be disabled
				// finally, compile the subnet
				algorithm.run();
				// assert that junction tree was compiled
				if (algorithm.getJunctionTree() == null || algorithm.getJunctionTree() != subnet.getJunctionTree()) {
					throw new RuntimeException("Unable to compile max prime subnet of cluster " + cluster);
				}
				
				// substitute nodes in decomposition junction tree with original nodes, 
				// for backward compatibility and in order to avoid other problems potentially caused by multiple java objects representing same node;
				for (Clique clique : algorithm.getJunctionTree().getCliques()) {
					
					// substitute the nodes in clique
					List<Node> cliqueNodes = clique.getNodesList();
					for (int i = 0; i < cliqueNodes.size(); i++) {
						// just replace the current variable with instance in original net
						cliqueNodes.set(i, net.getNode(cliqueNodes.get(i).getName()));
					}
					
					// don't forget to update the associated nodes of each clique
					cliqueNodes = clique.getAssociatedProbabilisticNodesList();
					for (int i = 0; i < cliqueNodes.size(); i++) {
						// just replace the current variable with instance in original net
						cliqueNodes.set(i, net.getNode(cliqueNodes.get(i).getName()));
					}
					// the opposite direction (node to associated clique) is processed later
					
					// substitute the nodes in potential table too
					PotentialTable table = clique.getProbabilityFunction();
					for (int i = 0; i < table.getVariablesSize(); i++) {
						// just replace the current variable with instance in original net
						table.setVariableAt(i, getNet().getNode(table.getVariableAt(i).getName()));
					}
				}
				
				// do the same for separators
				for (Separator separator : algorithm.getJunctionTree().getSeparators()) {
					
					// substitute the nodes in separator
					List<Node> separatorNodes = separator.getNodes();
					for (int i = 0; i < separatorNodes.size(); i++) {
						// just replace the current variable with instance in original net
						separatorNodes.set(i, net.getNode(separatorNodes.get(i).getName()));
					}
					
					// substitute the nodes in potential table too
					PotentialTable table = separator.getProbabilityFunction();
					for (int i = 0; i < table.getVariablesSize(); i++) {
						// just replace the current variable with instance in original net
						table.setVariableAt(i, getNet().getNode(table.getVariableAt(i).getName()));
					}
				}
				
				// make original nodes to point to new cliques/separators, instead of pointing to old cliques/separators that are likely to be removed later
				for (INode originalNode : originalNodes) {
					if (originalNode instanceof TreeVariable) {
						// I did not remove new nodes from subnet, so I can query subnet in order to get the respective new node
						TreeVariable newNode = (TreeVariable) subnet.getNode(originalNode.getName());
						// subnet was compiled, so nodes in it are supposedly associated with some clique in new junction subtree.
						// use new node (which is associated to new cliques) to associate original node to new clique.
						((TreeVariable) originalNode).setAssociatedClique(newNode.getAssociatedClique());
					}
				}
				
				// add junction tree to the map to be returned.
				// Current cluster and all related clusters are supposedly in clustersProcessedInThisIteration
				ret.put(algorithm.getJunctionTree(), clustersProcessedInThisIteration);
			}
		}
    			
		return ret;
	}

    /**
     * Recursively collects nodes of current cluster and connected clusters marked in clustersToCompile.
     * Connected clusters are clusters related with {@link Clique#getParent()} and {@link Clique#getChildren()}.
     * @param cluster : current cluster in recursive call
     * @param clustersToCompile : if cluster is not in this set, stop recursive calls.
     * @param processedClusters : stores which clusters were processed already, so that we don't process the same cluster twice.
     * @return : set of all nodes in current cluster and all connected clusters;
     * @see #getCompiledPrimeDecompositionSubnets(Collection)
     * @see #runDynamicJunctionTreeCompilation()
     */
	private Set<INode> getNodesInConnectedClustersRecursively(Clique cluster, Collection<Clique> clustersToCompile, Set<Clique> processedClusters) {
		// if current cluster is not marked, then return nothing
		if (clustersToCompile == null || !clustersToCompile.contains(cluster)) {
			return Collections.EMPTY_SET;
		}
		// make sure the processed clusters is not null
		if (processedClusters == null) {
			// instantiate new set, because we'll use it in recursive call anyway
			processedClusters = new HashSet<Clique>();
		}
		// check if this cluster was already processed
		if (processedClusters.contains(cluster)) {
			return Collections.EMPTY_SET; // if it was processed already, return nothing
		}
		// prepare the set to return
		Set<INode> ret = new HashSet<INode>();
		ret.addAll(cluster.getNodesList()); // process current cluster
		processedClusters.add(cluster); 	// mark this cluster as processed before making recursive calls
		
		// call recursive for parent cluster
		ret.addAll(this.getNodesInConnectedClustersRecursively(cluster.getParent(), clustersToCompile, processedClusters));
		
		// call recursive for child clusters
		for (Clique childCluster : cluster.getChildren()) {
			ret.addAll(this.getNodesInConnectedClustersRecursively(childCluster, clustersToCompile, processedClusters));
		}
		
		return ret;
	}

	/**
     * This method identifies which clusters in a maximum prime subgraph decomposition tree needs to be modified
     * by a new edge/arc.
     * If the path between the clusters (of the max prime subgraph decomposition tree) marked for modification
     * has an empty separator, the max prime subgraph decomposition tree (and the respective original junction tree)
     * will be modified so that the clusters (and respective cliques) marked for modifications will
     * become adjacent each other (in order restrict the number of clusters/cliques to be modified).
     * @param includedEdge : the new edge/arc to be included
     * @param originalJunctionTree : the junction tree of probabilities
     * that is being reused in the dynamic junction tree compilation algorithm.
     * This may be modified if it contains empty separators and new edges connects nodes that crosses such empty separators.
     * @param decompositionTree : this is the maximum subgraph decomposition tree
     * obtained from {@link #getMaximumPrimeSubgraphDecompositionTree(IJunctionTree)}.
     * This object can have its content modified after calling this method.
	 * @param clusterToOriginalCliqueMap : this map associates clusters in max prime subgraph decomposition tree to cliques in original junction tree. 
     * It it used to retrieve cliques of original junction tree related to the cluster currently being evaluated.
     * @return clusters in the decomposition tree that needs to be modified
     * @see #runDynamicJunctionTreeCompilation()
     * @see #getClustersToModifyAddNode(INode, IJunctionTree)
     * @see #getClustersToModifyRemoveEdge(Edge, IJunctionTree)
     * @see #getClustersToModifyRemoveNode(INode, IJunctionTree)
     */
    protected Collection<Clique> treatAddEdge( Edge includedEdge, IJunctionTree originalJunctionTree, IJunctionTree decompositionTree, Map<Clique, Collection<Clique>> clusterToOriginalCliqueMap) {
    	// basic assertion
    	if (includedEdge == null || decompositionTree == null) {
    		return Collections.EMPTY_LIST;	// do nothing, and just return empty
    	}
    	
		// get the parent and child nodes
    	Node child = includedEdge.getDestinationNode();
    	Node parent = includedEdge.getOriginNode();
    	
    	// find clusters containing child and clusters containing parent
    	List<Clique> childClusters = new ArrayList<Clique>();
    	List<Clique> parentClusters = new ArrayList<Clique>();
    	for (Clique cluster : decompositionTree.getCliques()) {
			if (cluster.getNodesList().contains(child)) {
				childClusters.add(cluster);
			}
			if (cluster.getNodesList().contains(parent)) {
				parentClusters.add(cluster);
			}
		}
    	if (childClusters.isEmpty()) {
    		throw new IllegalArgumentException("There is no maximum prime subgraph decomposition tree cluster containing child node " + child);
    	}
    	if (parentClusters.isEmpty()) {
    		throw new IllegalArgumentException("There is no maximum prime subgraph decomposition tree cluster containing parent node " + parent);
    	}
    	
    	// find shortest path between clusters of child node and clusters of parent node
    	List<Clique> shortestPathFromChild = null;  			// shortest path from the cluster of child node to the cluster of parent node
    	for (Clique clusterWithChildNode : childClusters) {
    		for (Clique clusterWithParentNode : parentClusters) {
    			List<Clique> currentPath = ((JunctionTree)decompositionTree).getPath(clusterWithChildNode, clusterWithParentNode);
    			// check if current path is shorter than the shortest path we know so far
    			if (shortestPathFromChild == null || currentPath.size() < shortestPathFromChild.size() ) {
    				shortestPathFromChild = currentPath;
    			}
    		}
		}
    	
    	// the decomposition tree is supposed to have 1 root, so any clusters should always have a path in between
    	if (shortestPathFromChild == null || shortestPathFromChild.isEmpty()) {
    		throw new IllegalArgumentException("The maximum prime subgraph decomposition tree is expected to have a single root (so there should be a path between any pair of clusters in the tree), but no path between clusters containing " 
    					+ child + " and " + parent + " was found");
    	}
    	
    	// if path has only 1 cluster, then child node and parent node are in same cluster already
    	if (shortestPathFromChild.size() == 1) {
    		return shortestPathFromChild;	// by returning it, we indicate that this cluster is marked for modification
    	}
    	
    	// find an empty separator between path, starting from child node's cluster
    	Separator emptySeparatorInShortestPath = null;	// if there is an empty separator in path, this will be the closest to child
    	int stepsToEmptySepFromChildCluster = 0;					// this stores how long (in steps) it took to reach an empty separator from the cluster containing child node
    	for (;stepsToEmptySepFromChildCluster < shortestPathFromChild.size() - 1; stepsToEmptySepFromChildCluster++) {
			Separator separator = decompositionTree.getSeparator(shortestPathFromChild.get(stepsToEmptySepFromChildCluster), shortestPathFromChild.get(stepsToEmptySepFromChildCluster+1));
			if (separator.getNodes().isEmpty()) {
				emptySeparatorInShortestPath = separator;
				break;
			}
		}
    	
    	if (emptySeparatorInShortestPath == null) {
    		// No empty separator, so all clusters in path shall be marked for modification;
    		return shortestPathFromChild;
    	} 
    	
    	// we will remove empty separators and connect the child's cluster and parent's cluster directly
    	// in order to do so, we need to decide which one will become the parent cluster (and the other must be transformed in order to become a root of subtree)
    	// By default, the cluster containing parent node will become a parent cluster as well
    	Clique clusterToBecomeParent = shortestPathFromChild.get(shortestPathFromChild.size()-1);	
    	// child cluster needs to be reorganized, so that the cluster containing child node will become the root of the subtree after removing empty separator.
    	Clique clusterToBecomeChild  = shortestPathFromChild.get(0); 
    	
    	// check if we can find an empty separator from the end of the path (i.e. from parent node's cluster) in shorter steps
    	int stepsToEmptySepFromParentCluster = 0;					// if this is shorter, then we may want to use this.
    	for (int i = shortestPathFromChild.size() - 1; i > 0 ; stepsToEmptySepFromParentCluster++, i--) {
    		if (stepsToEmptySepFromParentCluster >= stepsToEmptySepFromChildCluster) {
    			// no need to look further, because we know that we could get the empty separator from child's cluster in fewer or equal steps
    			break;
    		}
    		Separator separator = decompositionTree.getSeparator(shortestPathFromChild.get(i-1), shortestPathFromChild.get(i));
    		if (separator.getNodes().isEmpty()) {
    			// found empty separator in shorter steps
    			emptySeparatorInShortestPath = separator;
    			// this means it's easier to reorganize parent node's cluster to become a root of subtree if we remove this empty separator
    			clusterToBecomeChild  = shortestPathFromChild.get(shortestPathFromChild.size()-1); 
    			clusterToBecomeParent = shortestPathFromChild.get(0);	// the other cluster will become the parent of the reorganized cluster
    			break;
    		}
    	}
    	
    	
    	// If the clusters are already directly connected by the empty separator, we don't need to reconnect clusters (i.e. no need to substitute separator). Check such condition.
    	if ( !( ( emptySeparatorInShortestPath.getClique1().equals(clusterToBecomeParent) && emptySeparatorInShortestPath.getClique2().equals(clusterToBecomeChild) )
    			|| ( emptySeparatorInShortestPath.getClique2().equals(clusterToBecomeParent) && emptySeparatorInShortestPath.getClique1().equals(clusterToBecomeChild) ) ) ) {
    		
    		// The clusters are not directly connected, so we need to reconnect clusters and thus substitute separator.
    		// We also need to do the same for the original junction tree, so extract separator and cliques from original junction tree and perform same modifications.
    		
    		// First, find the equivalent empty separator from original junction tree. 
    		Separator originalEmptySeparatorInShortestPath = null;
    		// clusters can represent many cliques, so search separators between each possible pair of cliques
    		for (Clique cliqueInCluster1 : clusterToOriginalCliqueMap.get(emptySeparatorInShortestPath.getClique1())) {		// clusterToOriginalCliqueMap translates a cluster to collection of cliques
    			for (Clique cliqueInCluster2 : clusterToOriginalCliqueMap.get(emptySeparatorInShortestPath.getClique2())) { // iterating on the other cluster
    				Separator separator = originalJunctionTree.getSeparator(cliqueInCluster1, cliqueInCluster2);
    				if (separator != null && separator.getNodes().isEmpty()) {
    					// found the empty separator between cliques in each cluster
    					originalEmptySeparatorInShortestPath = separator;
    					break; // It should be unique, because they are tree structures. 
    				}
    			}
    			// If we found the separator (which is unique), we don't need to keep searching for other pairs.
    			if (originalEmptySeparatorInShortestPath != null) {
    				break;	
    			}
			}
    		// assert that we found the respective separator in original junction tree
    		if (originalEmptySeparatorInShortestPath == null || !originalEmptySeparatorInShortestPath.getNodes().isEmpty()) {
    			throw new RuntimeException(
    					"Unable to find the empty separator in original junction tree corresponding to the following separator in max prime subgraph decomposition: " 
    				   + emptySeparatorInShortestPath
				   );
    		}
    		
    		// remove empty separator from the max prime subgraph decomposition tree
    		decompositionTree.removeSeparator(emptySeparatorInShortestPath);
    		// do the same for the original junction tree
    		originalJunctionTree.removeSeparator(originalEmptySeparatorInShortestPath);	
    		
    		// also disconnect the clusters
    		if (emptySeparatorInShortestPath.getClique1().getParent() != null
    				&& emptySeparatorInShortestPath.getClique1().getParent().equals(emptySeparatorInShortestPath.getClique2())) {
    			
    			emptySeparatorInShortestPath.getClique1().setParent(null);
    			emptySeparatorInShortestPath.getClique2().removeChild(emptySeparatorInShortestPath.getClique1());
    			
    			// do the same for the original cliques;
    			originalEmptySeparatorInShortestPath.getClique1().setParent(null);
    			originalEmptySeparatorInShortestPath.getClique2().removeChild(originalEmptySeparatorInShortestPath.getClique1());
    			
    		} else {
    			emptySeparatorInShortestPath.getClique2().setParent(null);
    			emptySeparatorInShortestPath.getClique1().removeChild(emptySeparatorInShortestPath.getClique2());
    			
    			// do the same for the original cliques;
    			originalEmptySeparatorInShortestPath.getClique2().setParent(null);
    			originalEmptySeparatorInShortestPath.getClique1().removeChild(originalEmptySeparatorInShortestPath.getClique2());
    		}
    		
    		// make one of the clusters (the one whose path to empty separator is shorter) a root cluster
    		// if one of the clusters is already a root, then use it as a child of the other cluster
    		if (clusterToBecomeParent.getParent() == null) {
    			// clusterToBeReorganized shall be the one to become a parent cluster instead 
    			Clique aux = clusterToBecomeParent;
    			clusterToBecomeParent = clusterToBecomeChild;
    			clusterToBecomeChild = aux;
    		} 
    		

    		// Also extract the respective cliques (the ones that we will connect together) from original junction tree. 
    		
    		// Extract the clique to become child. The clique to become child is the root of the cliques in the cluster to become child.
    		Clique originalCliqueToBecomeChild = clusterToOriginalCliqueMap.get(clusterToBecomeChild).iterator().next();	// cluster supposedly has at least 1 clique in it	
    		// Now, find the clique that is in the same cluster, and also it is an ancestor of all the other cliques in same cluster.
    		while (originalCliqueToBecomeChild.getParent() != null																 // stop if reached global root
    				&& clusterToOriginalCliqueMap.get(clusterToBecomeChild).contains(originalCliqueToBecomeChild.getParent())) { // also stop if there is no more ancestor in the same cluster
    			// go up in hierarchy until we find global root or root of the subtree of cliques in the same cluster
    			originalCliqueToBecomeChild = originalCliqueToBecomeChild.getParent();
    		}
    		
    		// Similarly, extract the clique to become parent. It is any leaf of the cliques in the cluster to become parent.
    		Clique originalCliqueToBecomeParent = clusterToOriginalCliqueMap.get(clusterToBecomeParent).iterator().next();	// again, cluster supposedly has at least 1 clique in it
    		// Look for a clique that has no child in the same cluster
    		while (originalCliqueToBecomeParent.getChildren() != null && originalCliqueToBecomeParent.getChildren().size() > 0				// stop if reached global leaf
    				&& clusterToOriginalCliqueMap.get(clusterToBecomeParent).contains(originalCliqueToBecomeParent.getChildren().get(0))) { // also stop if there is no more descendant in same cluster
    			// Go down in hierarchy until we have no more children. It can be any leaf (of the subtree in cluster), so I'm always picking the 1st child
    			originalCliqueToBecomeParent = originalCliqueToBecomeParent.getChildren().get(0);
    		}
    		
    		// reorganize cluster, so that the one we have will become a root of the subtree
    		if (clusterToBecomeChild.getParent() != null) {
    			// do it if this cluster is not the root already
    			((JunctionTree)decompositionTree).moveCliqueToRoot(clusterToBecomeChild);
    			// do the same for the clique in original junction tree
    			((JunctionTree)originalJunctionTree).moveCliqueToRoot(originalCliqueToBecomeChild);
    		} // Or else, the cluster is a root. If so, the clique in original Junction tree should also be a root, so no need for modifications in original junction tree as well.
    		
    		// just an assertion
    		if (clusterToBecomeChild.getParent() != null) {
    			throw new RuntimeException("Unable to rebuild maximum prime subgraph decomposition tree in order to make " 
    										+ clusterToBecomeChild + " a root of the subtree it belongs to.");
    		}
    		// same assertion for original junction tree
    		if (originalCliqueToBecomeChild.getParent() != null) {
    			throw new RuntimeException("Unable to rebuild junction tree in order to make " 
    		                              	+ originalCliqueToBecomeChild + " a root in the subtree it belongs to.");
    		}
    		
    		// and connect child node's cluster with parent node's cluster
    		clusterToBecomeParent.addChild(clusterToBecomeChild);
    		clusterToBecomeChild.setParent(clusterToBecomeParent);
    		// same modification to original junction tree
    		originalCliqueToBecomeParent.addChild(originalCliqueToBecomeChild);
    		originalCliqueToBecomeChild.setParent(originalCliqueToBecomeParent);
    		
    		// and also insert the new separator
			decompositionTree.addSeparator(new StubSeparator(clusterToBecomeParent, clusterToBecomeChild));	// stub separator will not initialize probability table
			// again, same modification to original junction tree
			originalJunctionTree.addSeparator(new Separator(originalCliqueToBecomeParent, originalCliqueToBecomeChild, false)); // false:=parent/children's links shall not be re-included
			
			// some algorithms expect the global root to be the 1st clique in the list, so reorder.
			Clique globalRoot = clusterToBecomeParent;
			// find global root in max prime subgraph decomposition
			while (globalRoot.getParent() != null) {
				globalRoot = globalRoot.getParent();
			}
			int indexOfGlobalRoot = decompositionTree.getCliques().indexOf(globalRoot);
			if (indexOfGlobalRoot > 0) {
				// move it to the 1st position in the list of cliques
				Collections.swap(decompositionTree.getCliques(), 0, indexOfGlobalRoot);
			}
			
			// do the same for the original junction tree
			globalRoot = originalCliqueToBecomeParent;
			// again, find global root in original junction tree
			while (globalRoot.getParent() != null) {
				globalRoot = globalRoot.getParent();
			}
			indexOfGlobalRoot = originalJunctionTree.getCliques().indexOf(globalRoot);
			if (indexOfGlobalRoot > 0) {
				// move it to the 1st position in the list of cliques
				Collections.swap(originalJunctionTree.getCliques(), 0, indexOfGlobalRoot);
			}
			
    	} 	// else, the clusters are already connected by this empty separator, thus we don't need any changes in structure
    	
    	// accordingly to Julia Florez's paper, the empty separator between the two clusters must now include the parent node
//    	newSeparator.getNodes().add(parent); // no need to include node into separator's potential table, because stub separator is for storing the structure only, not the potentials
    	// Update: the above separator is never used again, so I found it's worthless to add new node in separator.
    	
    	// only the parent and child cluster shall be marked for modification, because they are directly connected now;
    	List<Clique> ret = new ArrayList<Clique>(2);
    	ret.add(clusterToBecomeChild);
    	ret.add(clusterToBecomeParent);
    	return ret;
	}
    

	/**
     * This method identifies which clusters in a maximum subgraph decomposition tree needs to be modified
     * because of an edge/arc being deleted from the network.
     * @param deletedEdge : the new edge/arc to be deleted
     * @param originalJunctionTree : the junction tree of probabilities
     * that is being reused in the dynamic junction tree compilation algorithm
     * @param decompositionTree : this is the maximum subgraph decomposition tree
     * obtained from {@link #getMaximumPrimeSubgraphDecompositionTree(IJunctionTree)}.
     * This object can have its content modified after calling this method.
	 * @param clusterToOriginalCliqueMap : this map associates clusters in max prime subgraph decomposition tree to cliques in original junction tree. 
     * It it used to retrieve cliques of original junction tree related to the cluster currently being evaluated.
     * @return clusters in the decomposition tree that needs to be modified
     * @see #runDynamicJunctionTreeCompilation()
     * @see #getClustersToModifyAddNode(INode, IJunctionTree)
     * @see #getClustersToModifyIncludeEdge(Edge, IJunctionTree)
     * @see #getClustersToModifyRemoveNode(INode, IJunctionTree)
     */
    protected Collection<Clique> treatRemoveEdge( Edge deletedEdge, IJunctionTree originalJunctionTree, IJunctionTree decompositionTree, Map<Clique, Collection<Clique>> clusterToOriginalCliqueMap) {
		// TODO Auto-generated method stub
    	throw new UnsupportedOperationException("Current version of dynamic junction tree compilation does not handle arc deletion.");
	}

    /**
     * This method identifies which clusters in a maximum subgraph decomposition tree needs to be modified
     * by a new edge/arc.
     * @param includedEdge : the new edge/arc to be included
     * @param originalJunctionTree : the junction tree of probabilities
     * that is being reused in the dynamic junction tree compilation algorithm
     * @param decompositionTree : this is the maximum subgraph decomposition tree
     * obtained from {@link #getMaximumPrimeSubgraphDecompositionTree(IJunctionTree)}.
     * This object can have its content modified after calling this method.
     * @param clusterToOriginalCliqueMap : this map associates clusters in max prime subgraph decomposition tree to cliques in original junction tree. 
     * It it used to retrieve cliques of original junction tree related to the cluster currently being evaluated.
     * @return clusters in the decomposition tree that needs to be modified
     * @see #runDynamicJunctionTreeCompilation()
     * @see #getClustersToModifyAddNode(INode, IJunctionTree)
     * @see #getClustersToModifyIncludeEdge(Edge, IJunctionTree)
     * @see #getClustersToModifyRemoveEdge(Edge, IJunctionTree)
     */
    protected Collection<Clique> treatRemoveNode( INode deletedNode, IJunctionTree originalJunctionTree, IJunctionTree decompositionTree, Map<Clique, Collection<Clique>> clusterToOriginalCliqueMap) {
    	// TODO Auto-generated method stub
    	throw new UnsupportedOperationException("Current version of dynamic junction tree compilation does not handle node deletion.");
	}

    /**
     * This method identifies which clusters in a maximum subgraph decomposition tree needs to be modified
     * by a new edge/arc.
     * @param includedEdge : the new edge/arc to be included
     * @param originalJunctionTree : the junction tree of probabilities
     * that is being reused in the dynamic junction tree compilation algorithm
     * @param decompositionTree : this is the maximum subgraph decomposition tree
     * obtained from {@link #getMaximumPrimeSubgraphDecompositionTree(IJunctionTree)}.
     * This object can have its content modified after calling this method.
     * @param clusterToOriginalCliqueMap : this map associates clusters in max prime subgraph decomposition tree to cliques in original junction tree. 
     * It it used to retrieve cliques of original junction tree related to the cluster currently being evaluated.
     * @return clusters in the decomposition tree that needs to be modified
     * @see #runDynamicJunctionTreeCompilation()
     * @see #getClustersToModifyIncludeEdge(Edge, IJunctionTree)
     * @see #getClustersToModifyRemoveEdge(Edge, IJunctionTree)
     * @see #getClustersToModifyRemoveNode(INode, IJunctionTree)
     */
    protected Collection<Clique> treatAddNode( INode includedNode, IJunctionTree originalJunctionTree, IJunctionTree decompositionTree, Map<Clique, Collection<Clique>> clusterToOriginalCliqueMap) {
		// TODO Auto-generated method stub
    	throw new UnsupportedOperationException("Current version of dynamic junction tree compilation does not handle node inclusion.");
	}

	/**
	 * Obtains new moralization arcs (arcs of conditional dependences) that will result from arc inclusion.
     * @param includedEdges : edges that were included in previous network. This will be used together with
     * the current network in order to retrieve which nodes were parents of some common child.
     * @param oldNet : previous network. This will be used to check which parents were not present in old network. 
     * @param newNet : current network. This will be used to retrieve existing parents. 
     * @return a collection of edges (not real edges in the network) that represents connections between moral parents
     * (i.e. conditional dependence between parents with a common child) that was included because of new arcs being added
     * to the network. Edges returned by this method are not to be added to the network, because the moralization phase of junction tree
     * compilation should do it automatically.
     * @see #runDynamicJunctionTreeCompilation()
     */
	protected Collection<Edge> getIncludedMoralArcs( ProbabilisticNetwork oldNet, ProbabilisticNetwork newNet, Collection<Edge> includedEdges) {
		
		Collection<Edge> ret = new ArrayList<Edge>();
		
		// this will be used to check if there is a path between two parents, so that we can test whether we can add arcs without generating cycles.
		MSeparationUtility mSeparationUtility = MSeparationUtility.newInstance();
		
		// for each children in new arc, check parents pairwise and see if they had common child in previous net
		for (Edge edge : includedEdges) {
			Node newChild = edge.getDestinationNode();
			Node oldChild = oldNet.getNode(newChild.getName());
			Node newParent = edge.getOriginNode();
			Node oldParent = oldNet.getNode(newParent.getName());
			for (Node parentToBeMoralized : newChild.getParents()) {
				if (parentToBeMoralized.equals(newParent)) {
					continue;	// do not attempt to create arc to the node itself
				}
				if (parentToBeMoralized.isParentOf(newParent) 
						|| parentToBeMoralized.isChildOf(newParent)) {
					continue;	// do not try to moralize if it is already connected.
				}
				if (oldChild != null && oldParent != null) {
					// check if the parent already had common child
					Node oldParentToBeMoralized = oldNet.getNode(parentToBeMoralized.getName());
					// check if they have had some children in common. If so, they were already moralized in old net (so we don't need new moral arc)
					// use an array list, so that internal comparison uses Object#equals(Object), because it will do name comparison
					List<Node> childrenInCommon = new ArrayList<Node>((List)oldParent.getChildNodes());
					childrenInCommon.removeAll((List)oldParentToBeMoralized.getChildNodes());
					if (!childrenInCommon.isEmpty()) {
						// they had some child in common, so no need to treat this pair. Go to next pair of parents
						continue;
					}
				}
				
				// this will be the edge to be included for moralization (will not be actually included, 
				// it's just for the algorithm to understand that this is a change in conditional (in)dependence)
				Edge edgeForMoralization = null;
				// just make sure we won't create cycles by adding this new arc (this is just for backward compatibility with directed graphs in general)
				if (mSeparationUtility.getRoutes(parentToBeMoralized, newParent, null, null, 1).isEmpty()) {
					// there is no directed route from parentToBeMoralized to newParent, so we can create link parentToBeMoralized -> newParent
					edgeForMoralization = new Edge(parentToBeMoralized, newParent);
				} else {
					// there is directed route from parentToBeMoralized to newParent, so we cannot create link parentToBeMoralized -> newParent. 
					// Thus, create in opposite direction
					edgeForMoralization = new Edge(newParent, parentToBeMoralized);
				}
				// make sure we don't add redundant edges
				if (!ret.contains(edgeForMoralization) && !includedEdges.contains(edgeForMoralization)) {
					// TODO check if the above if is redundant and/or can be optimized
//					edgeForMoralization.setDirection(false);	// TODO check if this is necessary
					ret.add(edgeForMoralization);
				}
			}
		}
		return ret;
	}

	/**
     * @param oldNet : the network before changes. This will be used together with
     * the current network in order to retrieve which nodes were parents of some common child.
     * @param newNet : current network. This will be used to retrieve existing parents. 
	 * @param deletedEdges : edges to be considered that were deleted from old net.
     * @return a collection of edges (not real edges in the network) that represents connections between moral parents
     * (i.e. conditional dependence between parents with a common child) that was deleted because of arcs being deleted
     * from the network.
     * @see #runDynamicJunctionTreeCompilation()
     */
	protected Collection<? extends Edge> getDeletedMoralArcs( ProbabilisticNetwork oldNet, ProbabilisticNetwork newNet, Collection<Edge> deletedEdges) {
    	throw new UnsupportedOperationException("Current version of dynamic junction tree compilation does not handle deletion of arcs.");
//		return Collections.EMPTY_LIST;
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
		getMarkovArc().clear();
		setMarkovArcCpy((List<Edge>)SetToolkit.clone(net.getEdges()));
	
		// remove the list of edges for information
		int sizeArcos = getMarkovArcCpy().size() - 1;
		for (int i = sizeArcos; i >= 0; i--) {
			auxArco = getMarkovArcCpy().get(i);
			if (auxArco.getDestinationNode().getType()
				== Node.DECISION_NODE_TYPE) {
				getMarkovArcCpy().remove(i);
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
						if ((net.hasEdge(auxPai1, auxPai2,  getMarkovArcCpy()) == -1)
							&& (net.hasEdge(auxPai1, auxPai2, getMarkovArc()) == -1)) {
							auxArco = new Edge(auxPai1, auxPai2);
							if (net.isCreateLog()) {
								net.getLogManager().append(
									auxPai1.getName()
										+ " - "
										+ auxPai2.getName()
										+ "\n");
							}
							getMarkovArc().add(auxArco);
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
     */
	public void triangularize(ProbabilisticNetwork net) {

		Node aux;
		List<Node> auxNodes;
		List<Node> nodeList = net.getNodes();

		if (net.isCreateLog()) {
			net.getLogManager().append(resource.getString("triangulateLabel"));
		}
		auxNodes = SetToolkit.clone(nodeList);
		
		// remove utility nodes from auxNodes
		Set<Node> nodesToRemove = new HashSet<Node>();
		for (Node node : auxNodes) {
			if (node.getType() == Node.UTILITY_NODE_TYPE) {
				nodesToRemove.add(node);
			}
		}
		auxNodes.removeAll(nodesToRemove);
		
		// reset copy of nodes
		net.getNodesCopy().clear();
		List<Node> copiaNos = net.getNodesCopy();
		
		List<INode> decisionNodes = getSortedDecisionNodes();
		
		// initialize copy of nodes
		copiaNos = SetToolkit.clone(auxNodes);
		int sizeDecisao = decisionNodes.size();
		for (int i = 0; i < sizeDecisao; i++) {
			aux = (Node) decisionNodes.get(i);
			auxNodes.remove(aux);
			auxNodes.removeAll(aux.getParentNodes());
		}

		net.setNodeEliminationOrder(new ArrayList<Node>(copiaNos.size()));
		List<Node> eliminationOrder = net.getNodeEliminationOrder();

		while (minimumWeightElimination(auxNodes, net));

		//        int index;
		for (int i = decisionNodes.size() - 1; i >= 0; i--) {
			aux = (Node) decisionNodes.get(i);
			eliminationOrder.add(aux);
			int sizeAdjacentes = aux.getAdjacents().size();
			for (int j = 0; j < sizeAdjacentes; j++) {
				Node v = aux.getAdjacents().get(j);
				v.getAdjacents().remove(aux);
			}
			if (net.isCreateLog()) {
				net.getLogManager().append(
					"\t" + eliminationOrder.size() + " " + aux.getName() + "\n");
			}

			auxNodes = SetToolkit.clone(aux.getParents());
			auxNodes.removeAll(decisionNodes);
			auxNodes.removeAll(eliminationOrder);
			for (int j = 0; j < i; j++) {
				Node decision = (Node) decisionNodes.get(j);
				auxNodes.removeAll(decision.getParents());
			}

			while (minimumWeightElimination(auxNodes, net)) ;
		}
		
		makeAdjacents(net);
	}

	/**
	 * Sets up node adjacency 
	 * @param net
	 */
	protected void makeAdjacents(ProbabilisticNetwork net) {
		// resets the adjacency information
    	for (Node node : net.getNodes()) {
			node.clearAdjacents();
		}
    	for (int z = markovArc.size() - 1; z >= 0; z--) {
			Edge auxArco = markovArc.get(z);
			auxArco.getOriginNode().getAdjacents().add(
				auxArco.getDestinationNode());
			auxArco.getDestinationNode().getAdjacents().add(
				auxArco.getOriginNode());
		}
    	
    	List<Edge> markovArcCpy = this.getMarkovArcCpy();
    	for (int z = markovArcCpy.size() - 1; z >= 0; z--) {
			Edge auxArco = markovArcCpy.get(z);
			if (auxArco.getDestinationNode().getType()
				== Node.UTILITY_NODE_TYPE) {
				markovArcCpy.remove(z);
			} else {
				auxArco.getOriginNode().getAdjacents().add(
					auxArco.getDestinationNode());
				auxArco.getDestinationNode().getAdjacents().add(
					auxArco.getOriginNode());
			}
		}
    }

	/**
	 * Sub-routine for {@link #triangularize(ProbabilisticNetwork)}.
	 * It eliminates the nodes in the graph by using minimum weight heuristics.
	 * First, it eliminates nodes whose adjacent nodes are pairwise connected.
	 * After that, if there are more nodes in the graph, it eliminates them using the
	 * minimum weight heuristic.
	 *
	 * @param  nodes  collection of nodes.
	 * 
	 */
	protected boolean minimumWeightElimination(List<Node> nodes, ProbabilisticNetwork net) {
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
				net.getNodeEliminationOrder().add(auxNode);
				if (net.isCreateLog()) {
					net.getLogManager().append(
						"\t" + net.getNodeEliminationOrder().size() + " " + auxNode.getName() + "\n");
				}
			}
		}
	
		if (nodes.size() > 0) {
			Node auxNo = weight(nodes); //auxNo: clique with maximum weight.
			net.getNodeEliminationOrder().add(auxNo);
			if (net.isCreateLog()) {
				net.getLogManager().append(
					"\t" + net.getNodeEliminationOrder().size() + " " + auxNo.getName() + "\n");
			}
			eliminateNode(auxNo, nodes, net); //Elimine no e reduza grafo.
			return true;
		}
		
		return false;
	}
	
	/**
	 * Method used inside {@link #triangularize(ProbabilisticNetwork)}
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
	 * Method used inside {@link #triangularize(ProbabilisticNetwork)}
	 * in order to eliminate nodes and reduce the graph.
	 * It includes necessary arcs in order to do so.
	 * 
	 *@param  node      node to be eliminated
	 *@param  nodes  available nodes
	 *
	 */
	private void eliminateNode(Node node, List<Node> nodes, ProbabilisticNetwork net) {	
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
					getMarkovArc().add(auxArco);
					auxNode1.getAdjacents().add(auxNode2);
					auxNode2.getAdjacents().add(auxNode1);			
					
//					System.out.println(auxArco);
				}
			}
		}
	
		for (int i = node.getAdjacents().size() - 1; i >= 0; i--) {
			Node auxNo1 = node.getAdjacents().get(i);
			//boolean removed = auxNo1.getAdjacents().remove(no);
			//assert removed;
			auxNo1.getAdjacents().remove(node);
		}
		nodes.remove(node);
	}

	/**
	 * This method is used inside {@link #triangularize(ProbabilisticNetwork)}
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
			
			Node aux;
			
			int sizeNos = net.getNodeCount();
			for (int i = 0; i < sizeNos; i++) {
				aux = (Node) net.getNodes().get(i);
				if (aux.getType() == Node.UTILITY_NODE_TYPE
						&& aux.getChildren().size() != 0) {
					throw new Exception(
							resource.getString("variableName")
							+ aux
							+ resource.getString("hasChildName"));
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
	 * nodes are the nodes assumed to be true in the condicional evidence. The first node in this list will be the main parent of
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
		
		// prepare junction tree so that we can manipulate cliques
		IJunctionTree junctionTree = net.getJunctionTree();
		
		// find the smallest clique containing all the parents
		int smallestSize = Integer.MAX_VALUE;
		Clique smallestCliqueContainingAllParents = null;
		for (Clique clique : junctionTree.getCliquesContainingAllNodes(parentNodes, Integer.MAX_VALUE)) {
			if (!clique.getNodes().contains(virtualNode) && (clique.getProbabilityFunction().tableSize() < smallestSize)) {
				smallestCliqueContainingAllParents = clique;
				smallestSize = clique.getProbabilityFunction().tableSize();
			}
		}
		
		// if could not find smallest clique, the arguments are inconsistent
		if (smallestCliqueContainingAllParents == null) {
			throw new IllegalArgumentException(this.getResource().getString("noCliqueForNodes") + parentNodes);
		}
		
		// reorder parent nodes, so that the order matches the nodes in smallestCliqueContainingAllParents.
		List<INode> orderedParentNodes = new ArrayList<INode>();
		for (INode parent : smallestCliqueContainingAllParents.getNodes()) {
			if (parentNodes.contains(parent)) {
				orderedParentNodes.add(parent);
			}
		}
		
		// create clique for the virtual node and parents
		Clique cliqueOfVirtualNode = new Clique();
		cliqueOfVirtualNode.getNodes().add(virtualNode);
		cliqueOfVirtualNode.getNodes().addAll((List)orderedParentNodes);
		cliqueOfVirtualNode.getProbabilityFunction().addVariable(virtualNode);
		for (INode parentNode : orderedParentNodes) {
			cliqueOfVirtualNode.getProbabilityFunction().addVariable(parentNode);
		}
		cliqueOfVirtualNode.setInternalIdentificator(junctionTree.getCliques().size());
		
		// add clique to junction tree, so that the algorithm can handle the clique correctly
		junctionTree.getCliques().add(cliqueOfVirtualNode);
		// create separator between the clique of parent nodes and virtual node (the separator should contain all parents)
		Separator separatorOfVirtualCliqueAndParents = new Separator(smallestCliqueContainingAllParents , cliqueOfVirtualNode);
		separatorOfVirtualCliqueAndParents.setNodes(new ArrayList<Node>((List)orderedParentNodes));
		for (INode parentNode : orderedParentNodes) {
			separatorOfVirtualCliqueAndParents.getProbabilityFunction().addVariable(parentNode);
		}
		junctionTree.addSeparator(separatorOfVirtualCliqueAndParents);
		separatorOfVirtualCliqueAndParents.setInternalIdentificator(-(junctionTree.getSeparators().size()+1));
		// just to guarantee that the network is fresh
		net.resetNodesCopy();
		
		// now, let's link the nodes with the cliques
		cliqueOfVirtualNode.getAssociatedProbabilisticNodes().add(virtualNode);
		virtualNode.setAssociatedClique(cliqueOfVirtualNode);
		
		// this is not necessary for ProbabilisticNode, but other types of nodes may need explicit initialization of the marginals
		virtualNode.initMarginalList();
		
		
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
		
		// update the marginal values (we only updated clique/separator potentials, thus, the marginals still have the old values if we do not update)
//		virtualNode.updateMarginal();
//		virtualNode.copyMarginal();
		
		// set finding always to the first state (because the second state is just a dummy state)
		virtualNode.addFinding(0);

		// prepare a collection containing the clique of virtual node and respective separator, so that we can store them and delete them from JT when prompted
		Set<IRandomVariable> sepAndCliqueSet = new HashSet<IRandomVariable>();
		sepAndCliqueSet.add(cliqueOfVirtualNode);
		sepAndCliqueSet.add(separatorOfVirtualCliqueAndParents);
		
		// register new virtual node (so that it can be removed later)
		this.getVirtualNodesToCliquesAndSeparatorsMap().put(virtualNode, sepAndCliqueSet);
		
		return virtualNode;
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
				for (IRandomVariable cliqueOrSep : getVirtualNodesToCliquesAndSeparatorsMap().get(virtualNode)) {
					if (cliqueOrSep instanceof Clique) {
						// remove this clique from parent
						Clique clique = (Clique) cliqueOrSep;
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
						
						// remove the clique containing the virtual node
						List<Clique> cliques = this.getNet().getJunctionTree().getCliques();
						indexToRemove = 0;
						for (Clique cliqueToCompare : this.getNet().getJunctionTree().getCliques()) {
							// do this comparison instead of equals, which is a name comparison
							if (cliqueToCompare.getInternalIdentificator() == cliqueOrSep.getInternalIdentificator()) {
								break;
							}
							indexToRemove++;
						}
						cliques.remove(indexToRemove);
					} else {
						this.getNet().getJunctionTree().removeSeparator((Separator)cliqueOrSep);
					}
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
	 */
	public IJunctionTreeBuilder getJunctionTreeBuilder() {
		return junctionTreeBuilder;
	}


	/**
	 * @param junctionTreeBuilder the junctionTreeBuilder to set
	 */
	public void setJunctionTreeBuilder(IJunctionTreeBuilder junctionTreeBuilder) {
		this.junctionTreeBuilder = junctionTreeBuilder;
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
	 * This method is used in {@link #sortDecisionNodes(Graph)} and {@link #triangularize(ProbabilisticNetwork)} 
	 * in order to trace decision nodes and its order.
	 * 
	 * @return the sortedDecisionNodes
	 */
	public List<INode> getSortedDecisionNodes() {
		return sortedDecisionNodes;
	}

	/**
	 * This method is used in {@link #sortDecisionNodes(Graph)} and {@link #triangularize(ProbabilisticNetwork)} 
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

	/**
	 * This is the edges of a moralized bayesian network
	 * @see JunctionTreeAlgorithm#moralize(ProbabilisticNetwork)
	 * @return the markovArc
	 */
	public List<Edge> getMarkovArc() {
		return markovArc;
	}

	/**
	 * This is the edges of a moralized bayesian network
	 * @see JunctionTreeAlgorithm#moralize(ProbabilisticNetwork)
	 * @param markovArc the markovArc to set
	 */
	public void setMarkovArc(List<Edge> markovArc) {
		this.markovArc = markovArc;
	}

	/**
	 * @return the markovArcCpy
	 */
	public List<Edge> getMarkovArcCpy() {
		return markovArcCpy;
	}

	/**
	 * @param markovArcCpy the markovArcCpy to set
	 */
	public void setMarkovArcCpy(List<Edge> markovArcCpy) {
		this.markovArcCpy = markovArcCpy;
	}

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
				ProbabilisticNode newNode = ((ProbabilisticNode)node).basicClone();
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
			
			// instantiate junction tree and copy content
			if (originalNet.getJunctionTree() != null) {
				this.setJunctionTree(new JunctionTree());
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
							Debug.println(getClass(), "Could not clone separator between " + newClique1 + " and " + newClique2);
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
				newNode.updateMarginal();
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
	
	/** This is used in {@link StubClique} as the default content of utility tables. It is static in order to avoid using unnecessary memory */
	public static final PotentialTable DEFAULT_SINGLETON_UTILITY_TABLE = new UtilityTable();
	/** This is used in {@link StubClique} as the default content of potential tables. It is static in order to avoid unnecessary memory garbage */
	public static final PotentialTable DEFAULT_SINGLETON_POTENTIAL_TABLE = new ProbabilisticTable();

	
	/** 
	 * This is a clique that will not use potential tables {@link Clique#getProbabilityFunction()}. 
	 * @see {@link JunctionTreeAlgorithm#getMaximumPrimeSubgraphDecompositionTree(IJunctionTree)} 
	 */
	class StubClique extends Clique {
		private static final long serialVersionUID = 9157001174566229956L;
		/**
		 * This is just a constructor which overwrites superclass.
		 * @param cliqueProbability: will be ignored and set to null.
		 * @param cliqueUtility: will be ignored and set to DEFAULT_SINGLETON_UTILITY_TABLE
		 */
		public StubClique(PotentialTable cliqueProbability, PotentialTable cliqueUtility) {
			super(cliqueProbability, DEFAULT_SINGLETON_UTILITY_TABLE);
			this.setProbabilityFunction(null);
		}

		/**
		 * This is just a constructor which overwrites superclass.
		 * @see StubClique#StubClique(PotentialTable, PotentialTable)
		 */
		public StubClique(PotentialTable cliqueProb) { this(cliqueProb, DEFAULT_SINGLETON_UTILITY_TABLE); }

		/**
		 * This is just a constructor which overwrites superclass.
		 * It calls {@link StubClique#StubClique(PotentialTable)} with {@link JunctionTreeAlgorithm#DEFAULT_SINGLETON_POTENTIAL_TABLE}
		 * in order to avoid unnecessary garbage collection in {@link StubClique#StubClique(PotentialTable)}
		 * because of automatic instantiation of tables when null is passed as argument.
		 */
		public StubClique() { this(DEFAULT_SINGLETON_POTENTIAL_TABLE); }

		/**
		 * Just add nodes to {@link #getNodesList()}, without changing clique potentials
		 * @see unbbayes.prs.bn.Clique#join(unbbayes.prs.bn.Clique)
		 */
		public void join(Clique cliqueToJoin) {
			// basic assertion
			if (cliqueToJoin == null || cliqueToJoin.getNodesList() == null) {
				return;
			}
			
			// add disjoint nodes in this clique
			for (Node nodeInCliqueToJoin : cliqueToJoin.getNodesList()) {
				if (!this.getNodesList().contains(nodeInCliqueToJoin)) {
					// add in same ordering
					this.getNodesList().add(nodeInCliqueToJoin);
				}
			}
		}
		
	}
	
	/** 
	 * This is a separator that will not use potential tables {@link Separator#getProbabilityFunction()}. 
	 * @see {@link JunctionTreeAlgorithm#getMaximumPrimeSubgraphDecompositionTree(IJunctionTree)} 
	 */
	class StubSeparator extends Separator {
		private static final long serialVersionUID = 7371081543252487376L;
		/**
		 * Instantiates a separator that will not use potential tables.
		 * @param clique1 : parent clique 
		 * @param clique2 : child clique
		 * @see Separator#Separator(Clique, Clique, PotentialTable, PotentialTable)
		 */
		public StubSeparator(Clique clique1, Clique clique2) {
			super(clique1, clique2, DEFAULT_SINGLETON_POTENTIAL_TABLE, DEFAULT_SINGLETON_UTILITY_TABLE, false);	// do not update the relations between the cliques automatically
			this.setProbabilityFunction(null);
		}

		
	}
	
	/**
	 * Obtains a new junction tree structure (potentials won't be filled) 
	 * that can be build by performing a maximum prime subgraph decomposition
	 * of the current Bayes net and current junction tree.
	 * Such decomposition can be obtained by joining cliques.
	 * The resulting junction tree can be used in other algorithms in order to
	 * identify portions of the original junction tree that won't be changed after
	 *  changes in the structure of the related Bayes net.
	 * @param originalJunctionTree : the junction tree to be referenced in order to build the prime subgraph decomposition
	 * @param clusterToOriginalCliqueMap : a cluster ({@link Clique}) in the prime subgraph decomposition tree will be related to 1 or many
	 * cliques in the original junction tree. This mapping associates the original cliques to a cluster generated from them.
	 * This argument is an output argument.
	 * @return a junction tree with no potentials filled (i.e. cliques only represent set of nodes, not a table of globally consistent joint probabilities).
	 * @throws IllegalAccessException from {@link IJunctionTreeBuilder#buildJunctionTree(Graph)}
	 * @throws InstantiationException  from {@link IJunctionTreeBuilder#buildJunctionTree(Graph)}
	 * @see Separator#isComplete()
	 */
	public IJunctionTree getMaximumPrimeSubgraphDecompositionTree(IJunctionTree originalJunctionTree, Map<Clique, Collection<Clique>> clusterToOriginalCliqueMap) throws InstantiationException, IllegalAccessException {
		// builder to be used in order to instantiate a new junction tree
		IJunctionTreeBuilder junctionTreeBuilder = getJunctionTreeBuilder();
		if (junctionTreeBuilder == null) {
			// use a default builder if nothing was specified
			junctionTreeBuilder = DEFAULT_JUNCTION_TREE_BUILDER;
		}
		
		
		// obtain the root clique, so that we can start copying cliques from it
		Clique root = null;
		for (Clique clique : originalJunctionTree.getCliques()) {
			if (clique.getParent() == null) {
				root = clique;
				break;	// we assume there is only 1 root clique
			}
		}
		
		// make sure the mapping is not null, because we are going to use it in recursive call
		if (clusterToOriginalCliqueMap == null) {
			clusterToOriginalCliqueMap = new HashMap<Clique, Collection<Clique>>();
		}
		
		// instantiate junction tree to return, and recursively copy cliques and separators
		IJunctionTree ret = junctionTreeBuilder.buildJunctionTree(getNetwork());
		recursivelyFillMaxPrimeSubgraphDecompositionTree(originalJunctionTree, ret, root, clusterToOriginalCliqueMap);
		
		
		return ret;
	}

	/**
	 * Recursively visit cliques from root to leaves in order to fill the junction tree with copies of cliques and separators,
	 * but cliques will be merged accordingly to maximum prime subgraph decomposition criteria
	 * (i.e. if {@link Separator#isComplete()} is false, then we must merge the cliques connected by such separator).
	 * @param junctionTreeToRead : the original junction tree being read.
	 * @param currentCliqueToRead : 
	 * This clique must belong to the original junction tree being read.
	 * Current recursive call will consider this clique and children
	 * (i.e. in current recursive call, maximum prime subgraph decomposition criteria will potentially join this clique to children, but never to its parent). 
	 * Next recursive call will invoke this method for children of this clique.
	 * @param junctionTreeToFill : this junction tree will be filled (thus, this is an input and output argument)
	 * @param decompositionToJunctionTreeMap : a cluster ({@link Clique}) in the prime subgraph decomposition tree will be related to 1 or many
	 * cliques in the original junction tree. This mapping associates them.
	 * This argument is an output argument.
	 * @param parentCliqueCreatedInPreviousCall : clique generated by previous recursive call (i.e. this clique will become the parent of cliques generated in current call).
	 * Set this to null if the current clique being visited is the root.
	 * This clique will be in the target junction tree (i.e. the junction tree to be filled).
	 * @return the root clique of the tree created by this recursive call. Null if nothing was created.
	 * @see Clique#join(Clique)
	 */
	private Clique recursivelyFillMaxPrimeSubgraphDecompositionTree(IJunctionTree junctionTreeToRead, IJunctionTree junctionTreeToFill, Clique currentCliqueToRead, Map<Clique, Collection<Clique>> decompositionToJunctionTreeMap) {
		
		// basic assertion
		if (currentCliqueToRead == null || junctionTreeToRead == null) {
			return null;
		}
		
		// this method needs a junction tree to fill, because it is used to remember which cliques/separators were handled already.
		if (junctionTreeToFill == null) {
			// use empty junction tree if nothing was specified.
			try {
//				junctionTreeToFill = DEFAULT_JUNCTION_TREE_BUILDER.buildJunctionTree(getNet());
				junctionTreeToFill = DEFAULT_JUNCTION_TREE_BUILDER.buildJunctionTree(null);
			} catch (Exception e) {
				throw new RuntimeException(e);	// TODO stop using exception translation
			}
		}
		
		// copy current clique (do not copy its potential table, though)
		StubClique currentCliqueToFill = new StubClique();	// use a stub, which won't use potential tables
		currentCliqueToFill.setIndex(currentCliqueToRead.getIndex());								  // just for backward compatibility
		currentCliqueToFill.setInternalIdentificator(currentCliqueToRead.getInternalIdentificator()); // just for backward compatibility
		currentCliqueToFill.setNodesList(new ArrayList<Node>(currentCliqueToRead.getNodesList()));	  // clone the list (so that we don't modify original list)
		currentCliqueToFill.setParent(null);		// make sure this is initialized with null value
		
		decompositionToJunctionTreeMap.put(currentCliqueToFill, new HashSet<Clique>());
		decompositionToJunctionTreeMap.get(currentCliqueToFill).add(currentCliqueToRead);
		
		// Do a depth-first recursive call to children.
		// A depth-first will guarantee that my grandchildren were handled before current clique;
		// thus, I don't need to check whether I should merge current clique with its grandchildren after merging current clique to its children
		// (because if the grandchildren exist --i.e. was not merged to children -- then it means the separator was incomplete, so we don't need to merge them anyway)
		if (currentCliqueToRead.getChildren() != null) {
			// iterate on children (to make recursive calls on each child)
			for (Clique childCliqueToRead : currentCliqueToRead.getChildren()) {
				if (childCliqueToRead == null) {
					continue;	// ignore null values
				}
				
				// do a in-depth recursive call with the child clique as pivot
				Clique childCliqueToFill = this.recursivelyFillMaxPrimeSubgraphDecompositionTree( 	// the returned clique is actually the root of the subtree created by this call
						junctionTreeToRead, 		// still access the same junction tree
						junctionTreeToFill, 		// still write to same junction tree
						childCliqueToRead, 			// set the child clique as the current clique to visit
						decompositionToJunctionTreeMap
					);
				
				// get the separator between the original parent and child
				Separator separatorToRead = junctionTreeToRead.getSeparator(currentCliqueToRead, childCliqueToRead);
				if (separatorToRead == null) {
					throw new IllegalArgumentException("Clique " + childCliqueToRead + " is a child of clique " + currentCliqueToRead + ", but no separator was found in between.");
				}
				
				// check if we shall merge clique
				if (!separatorToRead.isComplete()) { // needs to merge cliques
					
					// move children of the child (being merged) to my children (i.e. convert grand children to children)
					for (Clique grandChild : childCliqueToFill.getChildren()) {
						// link current clique to/from grand child
						currentCliqueToFill.addChild(grandChild); 	// add grandchild to my list of children
						grandChild.setParent(currentCliqueToFill);	// set current clique as parent of grandchild
						
						// prepare to replace separator
						Separator oldSeparator = junctionTreeToFill.getSeparator(childCliqueToFill, grandChild);	// the separtor to be replaced
						if (oldSeparator == null) {
							throw new IllegalArgumentException("Clique " + grandChild + " is expected to be a child of clique " + childCliqueToFill + ", but no separator was created in previous recursive call.");
						}
						
						// create new instance of separator, because unfortunately we cannot change existing separator
						StubSeparator newSeparator = new StubSeparator(currentCliqueToFill, grandChild);	// new separator is from current clique to grandchild
						newSeparator.setInternalIdentificator(oldSeparator.getInternalIdentificator());		// just for backward compatibility
						newSeparator.setNodes(oldSeparator.getNodes());	// can use reference, because old separator will be removed anyway
						
						// now, replace the separator
						junctionTreeToFill.removeSeparator(oldSeparator);
						junctionTreeToFill.addSeparator(newSeparator);
					}
					
					// no need to disassociate child clique from current clique, because we never associated them anyway (they are only associated in the else clause below)
					
					// we are going to merge child to current clique, so move all mapping of the child to the mapping of current clique
					decompositionToJunctionTreeMap.get(currentCliqueToFill).addAll(decompositionToJunctionTreeMap.get(childCliqueToFill));
					decompositionToJunctionTreeMap.remove(childCliqueToFill);	// remove child from mapping
					
					// merge child to current clique (the current clique will become a large clique containing nodes from both cliques)
					currentCliqueToFill.join(childCliqueToFill);
					
					// also needs to remove the joined child from the junction tree being filled, because the recursive call has inserted it in the junction tree
					junctionTreeToFill.getCliques().remove(childCliqueToFill);
					
				} else {	// no need to merge cliques
					
					// create separator between the current clique and child clique
					StubSeparator newSeparator = new StubSeparator(currentCliqueToFill, childCliqueToFill);	// new separator is from current clique to grandchild
					newSeparator.setInternalIdentificator(separatorToRead.getInternalIdentificator());		// just for backward compatibility
					newSeparator.setNodes(separatorToRead.getNodes());	// can use reference, because old separator will be removed anyway
					
					// add separator to the junction tree being filled
					junctionTreeToFill.addSeparator(newSeparator);
					
					// creating new separators won't automatically associate the current clique with child clique, so associate them
					currentCliqueToFill.addChild(childCliqueToFill);
					childCliqueToFill.setParent(currentCliqueToFill);
					
				}
				
			}
		}
		
		// include copied clique to target junction tree
		junctionTreeToFill.getCliques().add(currentCliqueToFill);
		
		// return the generated clique
		return currentCliqueToFill;
	}

//	/**
//	 * @return the set of nodes detected when {@link #run()} was executed.
//	 * This is used later in {@link #run()} again in order to detect modifications in structure.
//	 * @see #setDynamicJunctionTreeNetSizeThreshold(int)
//	 * @see #getDynamicJunctionTreeNetSizeThreshold()
//	 */
//	public Collection<INode> getNodesPreviousRun() {
//		return nodesPreviousRun;
//	}
//
//	/**
//	 * @param nodesPreviousRun the set of nodes detected when {@link #run()} was executed.
//	 * This is used later in {@link #run()} again in order to detect modifications in structure.
//	 * @see #setDynamicJunctionTreeNetSizeThreshold(int)
//	 * @see #getDynamicJunctionTreeNetSizeThreshold()
//	 */
//	public void setNodesPreviousRun(Collection<INode> nodesPreviousRun) {
//		this.nodesPreviousRun = nodesPreviousRun;
//	}

	/**
	 * @return {@link #run()} will use dynamic junction tree compilation if number of nodes is above this value.
	 * @see #getNodesPreviousRun()
	 * @see #getEdgesPreviousRun()
	 */
	public int getDynamicJunctionTreeNetSizeThreshold() {
		return dynamicJunctionTreeNetSizeThreshold;
	}

	/**
	 * @param dynamicJunctionTreeNetSizeThreshold : {@link #run()} will use dynamic junction tree compilation if number of nodes is above this value.
	 * @see #getNetPreviousRun()
	 */
	public void setDynamicJunctionTreeNetSizeThreshold(
			int dynamicJunctionTreeNetSizeThreshold) {
		this.dynamicJunctionTreeNetSizeThreshold = dynamicJunctionTreeNetSizeThreshold;
		// clear the network cache
		this.setNetPreviousRun(null);
	}

	/**
	 * @return Copy of the network used when {@link #run()} was executed the previous time
	 * @see #setDynamicJunctionTreeNetSizeThreshold(int)
	 * @see #getDynamicJunctionTreeNetSizeThreshold()
	 */
	public ProbabilisticNetwork getNetPreviousRun() {
		return netPreviousRun;
	}

	/**
	 * @param netPreviousRun : Copy of the network used when {@link #run()} was executed the previous time
	 * @see #setDynamicJunctionTreeNetSizeThreshold(int)
	 * @see #getDynamicJunctionTreeNetSizeThreshold()
	 */
	public void setNetPreviousRun(ProbabilisticNetwork netPreviousRun) {
		this.netPreviousRun = netPreviousRun;
	}

	/**
	 * @return true if {@link #run()} shall throw exception when dynamic junction tree compilation ({@link #runDynamicJunctionTreeCompilation()}) fails. 
	 *  When false, failures in {@link #runDynamicJunctionTreeCompilation()} will just trigger ordinal junction tree compilation.
	 * @see #runDynamicJunctionTreeCompilation()
	 * @see #getDynamicJunctionTreeNetSizeThreshold()
	 * @see ProbabilisticNetwork#compile()
	 */
	public boolean isToHaltOnDynamicJunctionTreeFailure() {
		return isToHaltOnDynamicJunctionTreeFailure;
	}

	/**
	 * @param isToHaltOnDynamicJunctionTreeFailure : 
	 * true if {@link #run()} shall throw exception when dynamic junction tree compilation ({@link #runDynamicJunctionTreeCompilation()}) fails. 
	 * When false, failures in {@link #runDynamicJunctionTreeCompilation()} will just trigger ordinal junction tree compilation.
	 * @see #runDynamicJunctionTreeCompilation()
	 * @see #getDynamicJunctionTreeNetSizeThreshold()
	 * @see ProbabilisticNetwork#compile()
	 */
	public void setToHaltOnDynamicJunctionTreeFailure(
			boolean isToHaltOnDynamicJunctionTreeFailure) {
		this.isToHaltOnDynamicJunctionTreeFailure = isToHaltOnDynamicJunctionTreeFailure;
	}

//	/**
//	 * @return set of arcs (edges) detected when {@link #run()} was executed previous time.
//	 * This is used later in {@link #run()} again in order to detect modifications in structure.
//	 * @see #setDynamicJunctionTreeNetSizeThreshold(int)
//	 * @see #getDynamicJunctionTreeNetSizeThreshold()
//	 */
//	public Collection<Edge> getEdgesPreviousRun() {
//		return edgesPreviousRun;
//	}
//
//	/**
//	 * @param edgesPreviousRun : set of arcs (edges) detected when {@link #run()} was executed previous time.
//	 * This is used later in {@link #run()} again in order to detect modifications in structure.
//	 * @see #setDynamicJunctionTreeNetSizeThreshold(int)
//	 * @see #getDynamicJunctionTreeNetSizeThreshold()
//	 */
//	public void setEdgesPreviousRun(Collection<Edge> edgesPreviousRun) {
//		this.edgesPreviousRun = edgesPreviousRun;
//	}

	
}
