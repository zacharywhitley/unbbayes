/**
 * 
 */
package unbbayes.prs.bn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
  	

	/**
	 * Default constructor for plugin support
	 */
	public JunctionTreeAlgorithm() {
		super();
		// initialize commands for checkConsistency
		this.setVerifyConsistencyCommandList(this.initConsistencyCommandList());
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
			Node auxNo = weight(nodes); //auxNo: clique de peso m�ｽnimo.
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
	 * The new virtual node is just a child of parentNode (which must have 2 states as well).
	 * This method is based on page 9 of the paper "Soft Evidential Update for Probabilistic Multiagent Systems"
	 * @author Alexandre Martins
	 * TODO incorporate in {@link #propagate()} and allow multiple states.
	 */
	public void addVirtualNode(SingleEntityNetwork net, Node parentNode, float evidenceValueState0, float evidenceValueState1, int evidenceStateIndex) throws Exception {
		//no virtual, para propagacao da evidencia incerta
		ProbabilisticNode virtualNode = new ProbabilisticNode();
		//V de virtual
		virtualNode.setName("V"+parentNode.getName());
		virtualNode.setDescription("Virtual "+parentNode.getDescription());
		//adiciono os mesmos estados presentes no no pai
		virtualNode.appendState(parentNode.getStateAt(0) );
		virtualNode.appendState(parentNode.getStateAt(1));
		net.addNode(virtualNode);
		
		//crio a tabela de potencial do no virtual
		PotentialTable virtualNodeCPT = virtualNode.getProbabilityFunction();
		virtualNodeCPT.addVariable(virtualNode);
		net.addEdge(new Edge(parentNode,virtualNode));
		//a tabela de variaveis e setada com valores 1, visto que ela nao sera necessaria durante o processo,
		//pois a tabela de potencial do clique virtual sera calculada diretamente pelos valores fornecidos
		virtualNodeCPT.setValue(0, 1);
		virtualNodeCPT.setValue(1, 1);
		virtualNodeCPT.setValue(2, 1);
		virtualNodeCPT.setValue(3, 1);
		
		//crio o clique virtual
		Clique auxClique = new Clique();
		auxClique.getNodes().add(virtualNode);
		auxClique.getNodes().add(parentNode);
		auxClique.getProbabilityFunction().addVariable(virtualNode);
		auxClique.getProbabilityFunction().addVariable(parentNode);
		
		//a tabela de variaveis do clique virtual contem as variaveis na seguinte sequencia: noPai,noV
		//como apenas a variavel noV recebera o finding, interessam, a cada finding, apenas as posicoes 0 e 2(em caso do finding for para o estado 0) 
		//ou as posicoes 1 e 3(em caso do finding for para o estado 1). As posicoes que nao interessam sao setadas da mesma forma que as que interessam, 
		//porem elas serao zeradas apos o finging, nao influenciando em nada
		auxClique.getProbabilityFunction().setValue(0, evidenceValueState0);
		auxClique.getProbabilityFunction().setValue(1, evidenceValueState0);
		auxClique.getProbabilityFunction().setValue(2, evidenceValueState1);
		auxClique.getProbabilityFunction().setValue(3, evidenceValueState1);
		
		IJunctionTree junctionTree = net.getJunctionTree();
		
		junctionTree.getCliques().add(auxClique);
		
		//busco o menor clique que contenha o no pai (e que nao contenha o no virtual, ou seja, ele nao seleciona o clique que acabou de ser criado)
		//para ser utilizado na criacao do separador
		int smallestSize = Integer.MAX_VALUE;
		Clique smallestClique = null;
		Clique clique;
		for (int c2 = 0; c2 < junctionTree.getCliques().size(); c2++) {
			clique = (Clique) junctionTree.getCliques().get(c2);
			if (clique.getNodes().contains(parentNode) && !clique.getNodes().contains(virtualNode) && (clique.getProbabilityFunction().tableSize() < smallestSize)) {
				smallestClique = clique;
				smallestSize = auxClique.getProbabilityFunction().tableSize();
			}
		}
		
		//crio o separador entre o clique pai e o virtual
		Separator sep = new Separator(smallestClique , auxClique);
		ArrayList<Node> node = new ArrayList<Node>();
		node.add(parentNode);
		sep.setNodes(node);
		sep.getProbabilityFunction().addVariable(parentNode);
		//seto os valores para 1
		sep.getProbabilityFunction().setValue(0, 1f);
		sep.getProbabilityFunction().setValue(1, 1f);
		junctionTree.addSeparator(sep);

		//marginalizo a variavel pai do clique que esta associado a ela
		//o menor clique selecionado acima tambem poderia ser utilizado para essa marginalizacao
		//nao utilizei o clique associado na criacao do separador pois ele pode ser um separador
		IRandomVariable parentClique = ((ProbabilisticNode)parentNode).getAssociatedClique();
		PotentialTable auxTab = (PotentialTable) ((PotentialTable)parentClique.getProbabilityFunction()).clone();
        int index = auxTab.indexOfVariable(parentNode);
        int size = parentClique.getProbabilityFunction().variableCount();
        for (int i = 0; i < size; i++) {
            if (i != index) {
            	auxTab.removeVariable(parentClique.getProbabilityFunction().getVariableAt(i));
            }
        }
        //faz o mesmo que a passagem de mensagens entre o clique pai e o virtual durante as fases de coleta e distribui
        //durante a passagem de mensagem do clique virtual para o clique pai(coleta),a
        //tab. de potencial do clique pai nao sera alterada,podendo essa etapa ser desconsiderada
        //atualizo as probabilidades da tabela de potencial do no virtual com os valores q foram marginalizados(distribui)
        //preferi fazer as fases coleta e distribui nesse momento, economizando na passagem de mensagens pela rede
        (auxClique.getProbabilityFunction()).opTab(auxTab, PotentialTable.PRODUCT_OPERATOR);
        int tableSize = auxTab.tableSize();
        //atualizo as probabilidades da tabela de potencial do separador com os valores q foram marginalizados(distribui)
        for (int i = 0; i < tableSize; i++) {
        	sep.getProbabilityFunction().setValue(i, auxTab.getValue(i));
    	}
				
		//necessario para que o novo no seje utilizado no processo de update(metodo updateEvidences)
		net.resetNodesCopy();
		//associo o novo no ao novo clique, para posterior busca do clique associado ao no
		auxClique.getAssociatedProbabilisticNodes().add(virtualNode);
		//associo o novo clique ao novo no, para fins de marginalizacao
		virtualNode.setAssociatedClique(auxClique);
		//inicio a variavel que ira receber os valores da marginalizacao, caso nao seja inicializada, lanca NullPointerException
		virtualNode.initMarginalList();
		//adiciona a evidencia no no correspondente
		virtualNode.addFinding(evidenceStateIndex);
		
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

	
	
}
