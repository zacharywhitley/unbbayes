/**
 * 
 */
package unbbayes.prs.bn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JPanel;

import unbbayes.controller.INetworkMediator;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.util.Debug;
import unbbayes.util.SetToolkit;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener;
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
public class JunctionTreeAlgorithm implements IInferenceAlgorithm {
	
	private static ResourceBundle generalResource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.controller.resources.ControllerResources.class.getName(),
			Locale.getDefault(),
			JunctionTreeAlgorithm.class.getClassLoader());
	
	private ProbabilisticNetwork net;
	
	private InferenceAlgorithmOptionPanel optionPanel;
	
	private IJunctionTreeBuilder junctionTreeBuilder = new DefaultJunctionTreeBuilder();

	/** Load resource file from util */
  	private static ResourceBundle utilResource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.util.resources.UtilResources.class.getName());
	

  	/** Load resource file from this package */
  	protected static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.prs.bn.resources.BnResources.class.getName());

	private List<IJunctionTreeCommand> verifyConsistencyCommandList;
  	
	private List<INode> sortedDecisionNodes = new ArrayList<INode>();

	private List<Edge> markovArc = new ArrayList<Edge>();

	private List<Edge> markovArcCpy = new ArrayList<Edge>();

	private List<IInferenceAlgorithmListener> inferenceAlgorithmListeners = new ArrayList<IInferenceAlgorithmListener>();

	private String virtualNodePrefix = "V_";

	private Map<INode,Collection<IRandomVariable>> virtualNodesToCliquesAndSeparatorsMap = new HashMap<INode,Collection<IRandomVariable>>();

	private float virtualNodePositionRandomness = 400;
  	
	private ILikelihoodExtractor likelihoodExtractor = LikelihoodExtractor.newInstance();
	
	
	/**
	 * Default constructor for plugin support
	 */
	public JunctionTreeAlgorithm() {
		super();
		// initialize commands for checkConsistency
		this.setVerifyConsistencyCommandList(this.initConsistencyCommandList());
		
		// add dynamically changeable behavior (i.e. routines that are not "mandatory", so it is interesting to be able to disable them when needed)
		this.getInferenceAlgorithmListeners().add(new IInferenceAlgorithmListener() {
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
											evidenceNodes.addAll(jt.getLikelihoodExtractor().extractLikelihoodParents(getNetwork(), node));
											// create the virtual node
											INode virtual = null;
											try {
												virtual = jt.addVirtualNode(getNetwork(), evidenceNodes);
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
		});
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
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
			listener.onBeforeRun(this);
		}
		if (this.getNet() == null
				|| this.getNet().getNodes().size() == 0) {
			throw new IllegalStateException(resource.getString("EmptyNetException"));
		}
		try {
			// TODO gradually migrate all compile routines to here
			this.getNet().compile();
//			if (this.getNet().getNodeCount() == 0) {
//				throw new Exception(resource.getString("EmptyNetException"));
//			}
//			if (this.getNet().isCreateLog()) {
//				this.getNet().getLogManager().reset();
//			}
//			this.verifyConsistency(this.getNet());
//			this.moralize(this.getNet());
//			this.triangularize(this.getNet());		
//			
//			this.getNet().compileJT(this.getJunctionTreeBuilder().buildJunctionTree(this.getNet()));
			
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
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
			listener.onAfterRun(this);
		}
	}
	
    /**
     * Performs moralization (for each node, link its parents with arcs) of the network.
     * @param net
     */
    protected void moralize(ProbabilisticNetwork net) {
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
	protected void triangularize(ProbabilisticNetwork net) {

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
		List<Node> oe = net.getNodeEliminationOrder();

		while (minimumWeightElimination(auxNodes, net));

		//        int index;
		for (int i = decisionNodes.size() - 1; i >= 0; i--) {
			aux = (Node) decisionNodes.get(i);
			oe.add(aux);
			int sizeAdjacentes = aux.getAdjacents().size();
			for (int j = 0; j < sizeAdjacentes; j++) {
				Node v = aux.getAdjacents().get(j);
				v.getAdjacents().remove(aux);
			}
			if (net.isCreateLog()) {
				net.getLogManager().append(
					"\t" + oe.size() + " " + aux.getName() + "\n");
			}

			auxNodes = SetToolkit.clone(aux.getParents());
			auxNodes.removeAll(decisionNodes);
			auxNodes.removeAll(oe);
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
			Node auxNo = weight(nodes); //auxNo: clique de peso m�ｽ�ｽnimo.
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

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#propagate()
	 */
	public void propagate() {
		for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
			listener.onBeforePropagate(this);
		}
		try {
			this.getNet().updateEvidences();
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
		for (Clique clique : junctionTree.getCliques()) {
			if (clique.getNodes().containsAll(parentNodes) && !clique.getNodes().contains(virtualNode) && (clique.getProbabilityFunction().tableSize() < smallestSize)) {
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
		
		// add clique to junction tree, so that the algorithm can handle the clique correctly
		junctionTree.getCliques().add(cliqueOfVirtualNode);
		
		// create separator between the clique of parent nodes and virtual node (the separator should contain all parents)
		Separator separatorOfVirtualCliqueAndParents = new Separator(smallestCliqueContainingAllParents , cliqueOfVirtualNode);
		separatorOfVirtualCliqueAndParents.setNodes(new ArrayList<Node>((List)orderedParentNodes));
		for (INode parentNode : orderedParentNodes) {
			separatorOfVirtualCliqueAndParents.getProbabilityFunction().addVariable(parentNode);
		}
		junctionTree.addSeparator(separatorOfVirtualCliqueAndParents);
		
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
		virtualNode.updateMarginal();
		virtualNode.copyMarginal();
		
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
			getNetwork().removeNode((Node) virtualNode);
			
			// remove clique/separator from junction tree
			for (IRandomVariable cliqueOrSep : getVirtualNodesToCliquesAndSeparatorsMap().get(virtualNode)) {
				if (cliqueOrSep instanceof Clique) {
					// remove this clique from parent
					Clique clique = (Clique) cliqueOrSep;
					clique.getParent().removeChild(clique);
				}
				this.getNet().getJunctionTree().getCliques().remove(cliqueOrSep);
				this.getNet().getJunctionTree().getSeparators().remove(cliqueOrSep);
			}
		}
		getVirtualNodesToCliquesAndSeparatorsMap().clear();
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

	
	
}
