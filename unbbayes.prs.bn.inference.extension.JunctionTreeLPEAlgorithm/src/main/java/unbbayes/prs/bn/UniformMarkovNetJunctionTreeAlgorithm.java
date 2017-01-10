/**
 * 
 */
package unbbayes.prs.bn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.util.SetToolkit;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener;

/**
 * This is a junction tree algorithm which assumes that arcs are undirected,
 * no moralization arcs will be added when generating a junction tree, 
 * and decision nodes will be virtually treated as if they were probabilistic nodes.
 * @author Shou Matsumoto
 */
public class UniformMarkovNetJunctionTreeAlgorithm extends JunctionTreeAlgorithm {

	private boolean lazyInitCliqueTables = true;
	
	private IInferenceAlgorithmListener defaultLazyInitListener = new IInferenceAlgorithmListener() {
		public void onBeforePropagate(IInferenceAlgorithm algorithm) {
			if (isLazyInitCliqueTables()) {
				try {
					setLazyInitCliqueTables(false);
					addVariablesToCliqueAndSeparatorTables((ProbabilisticNetwork) algorithm.getNetwork(), ((ProbabilisticNetwork)algorithm.getNetwork()).getJunctionTree());
				} catch (RuntimeException t) {
					setLazyInitCliqueTables(true);
					throw t;
				}
				setLazyInitCliqueTables(true);
			}
		}
		public void onBeforeRun(IInferenceAlgorithm algorithm) {}
		public void onBeforeReset(IInferenceAlgorithm algorithm) {}
		public void onAfterRun(IInferenceAlgorithm algorithm) {}
		public void onAfterReset(IInferenceAlgorithm algorithm) {}
		public void onAfterPropagate(IInferenceAlgorithm algorithm) {}
	};
	
	/**
	 * Default constructor
	 * @see #UniformMarkovNetJunctionTreeAlgorithm(ProbabilisticNetwork)
	 */
	public UniformMarkovNetJunctionTreeAlgorithm() {
		setDecisionTotalOrderRequired(false);
		// add a lazy init listener at the beginning of list of listeners
		getInferenceAlgorithmListeners().add(0, getDefaultLazyInitListener());
	}

	/**
	 * Constructor initializing fields
	 * @param net
	 * @see #setNet(ProbabilisticNetwork)
	 */
	public UniformMarkovNetJunctionTreeAlgorithm(ProbabilisticNetwork net) {
		super(net);
		setDecisionTotalOrderRequired(false);
		// add a lazy init listener at the beginning of list of listeners
		getInferenceAlgorithmListeners().add(0, getDefaultLazyInitListener());
	}
	

	/* (non-Javadoc)
	 * @see unbbayes.util.extension.bn.inference.IInferenceAlgorithm#getName()
	 */
	public String getName() {
		return "UniformMarkovNetJunctionTree";
	}
	
	/**
	 * Creates {@link IJunctionTreeCommand} in the same way of {@link JunctionTreeAlgorithm},
	 * but consistency checks regarding decision nodes and CPTs will be ignored.
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#initConsistencyCommandList()
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
		
		
		return ret;
	}

	

	/**
	 * Invoke {@link #makeAdjacents(ProbabilisticNetwork)} without performing moralization of parents.
	 * Also initializes {@link ProbabilisticNetwork#getEdgesCopy()}
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#moralize(unbbayes.prs.bn.ProbabilisticNetwork)
	 */
    public void moralize(ProbabilisticNetwork net) {
		
		// reset adjacency info
		for (Node node : net.getNodes()) {
			node.clearAdjacents();
		}
	
		net.getMarkovArcs().clear();
		
		Collection<Edge> markovArcsToBeForced = net.getMarkovArcsToBeForced();
		if (markovArcsToBeForced != null && !markovArcsToBeForced.isEmpty()) {
			net.getMarkovArcs().addAll(markovArcsToBeForced);
		}
		
		net.setEdgesCopy(SetToolkit.clone(net.getEdges()));
		
		makeAdjacents(net);
		
	}
    
    /**
     * Makes triangulation but without treating decision nodes in a special manner.
     * @see unbbayes.prs.bn.JunctionTreeAlgorithm#triangulate(unbbayes.prs.bn.ProbabilisticNetwork)
     */
	public List<INode> triangulate(ProbabilisticNetwork net) {

		List<Node> nodeList = net.getNodes();

		if (net.isCreateLog()) {
			net.getLogManager().append(resource.getString("triangulateLabel"));
		}
		List<Node> auxNodes = SetToolkit.clone(nodeList);
		
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
		
		List<INode> nodeEliminationOrder = new ArrayList<INode>(nodesCopy.size());
		
		// fill nodeEliminationOrder until there is no more nodes that can be eliminated in triangulation process
		while (minimumWeightElimination(auxNodes, net, nodeEliminationOrder));

		makeAdjacents(net);
		
		return nodeEliminationOrder;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#buildJunctionTree(unbbayes.prs.bn.ProbabilisticNetwork, java.util.List)
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
		this.addVariablesToCliqueAndSeparatorTables(net, junctionTree);
		this.associateCliques(net, junctionTree);
		
		// fill all cliques, separators, and marginals with uniform distribution
		setUniform(net, junctionTree);
		

		if (net.isCreateLog()) {
			Thread t = new Thread(new Runnable() {
				public void run() {
					net.getLogManager().append("\n\n*****************   Network size  *********************\n");
					net.getLogManager().append("Nodes: " + net.getNodeCount() + "\n");
					net.getLogManager().append("Edges: " + net.getEdges().size());
					net.getLogManager().append("\n*******************************************************\n\n");
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
	 * fill all cliques, separators, and marginals with uniform distribution
	 * @param net : marginals of nodes in here will be filled with uniform.
	 * @param junctionTree : clique/separator tables in here will be filled with uniform distribution
	 */
	protected void setUniform(ProbabilisticNetwork net, IJunctionTree junctionTree) {
		// fill clique/separators
		if (junctionTree != null) {
			if (junctionTree.getCliques() != null) {
				for (Clique clique : junctionTree.getCliques()) {
					PotentialTable table = clique.getProbabilityFunction();
					int size = table.tableSize();
					for (int i = 0; i < size; i++) {
						table.setValue(i, 1f/((float)size));
					}
				}
			}
			if (junctionTree.getSeparators() != null) {
				for (Separator sep : junctionTree.getSeparators()) {
					PotentialTable table = sep.getProbabilityFunction();
					int size = table.tableSize();
					for (int i = 0; i < size; i++) {
						table.setValue(i, 1f/((float)size));
					}
				}
			}
		}
		// fill marginals
		if (net != null && net.getNodes() != null) {
			for (Node node : net.getNodes()) {
				if (node instanceof TreeVariable) {
					TreeVariable var = (TreeVariable) node;
					var.initMarginalList();
					int statesSize = var.getStatesSize();
					for (int i = 0; i < statesSize; i++) {
						var.setMarginalAt(i, 1f/((float)statesSize));
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#addVariablesToCliqueAndSeparatorTables(unbbayes.prs.bn.ProbabilisticNetwork, unbbayes.prs.bn.IJunctionTree)
	 */
	protected void addVariablesToCliqueAndSeparatorTables(ProbabilisticNetwork net,	IJunctionTree junctionTree) {
		if (isLazyInitCliqueTables()) {
			return;
		}
		super.addVariablesToCliqueAndSeparatorTables(net, junctionTree);
	}

	/**
	 * @return the lazyInitCliqueTables
	 */
	public boolean isLazyInitCliqueTables() {
		return lazyInitCliqueTables;
	}

	/**
	 * @param lazyInitCliqueTables the lazyInitCliqueTables to set
	 */
	public void setLazyInitCliqueTables(boolean lazyInitCliqueTables) {
		this.lazyInitCliqueTables = lazyInitCliqueTables;
	}

	/**
	 * @return the defaultLazyInitListener
	 */
	public IInferenceAlgorithmListener getDefaultLazyInitListener() {
		return defaultLazyInitListener;
	}

	/**
	 * @param defaultLazyInitListener the defaultLazyInitListener to set
	 */
	public void setDefaultLazyInitListener(IInferenceAlgorithmListener defaultLazyInitListener) {
		this.defaultLazyInitListener = defaultLazyInitListener;
	}
	

}
