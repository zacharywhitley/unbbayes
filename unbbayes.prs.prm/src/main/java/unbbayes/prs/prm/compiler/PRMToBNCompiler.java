/**
 * 
 */
package unbbayes.prs.prm.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.controller.prm.IDatabaseController;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.builder.IProbabilisticNetworkBuilder;
import unbbayes.prs.builder.impl.DefaultProbabilisticNetworkBuilder;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.prm.IAttributeDescriptor;
import unbbayes.prs.prm.IAttributeValue;
import unbbayes.prs.prm.IDependencyChainSolver;
import unbbayes.prs.prm.IPRM;
import unbbayes.prs.prm.IPRMObject;
import unbbayes.prs.prm.cpt.compiler.IPRMCPTCompiler;
import unbbayes.prs.prm.cpt.compiler.PRMCPTCompiler;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * Default compiler of PRM project. It converts PRM to BN
 * 
 * @author Shou Matsumoto
 * 
 */
public class PRMToBNCompiler implements IPRMCompiler,
		IBNInferenceAlgorithmHolder {

	private IProbabilisticNetworkBuilder networkBuilder;

	private IPRMCPTCompiler cptCompiler;

	private IInferenceAlgorithm bnCompilationAlgorithm;

	/**
	 * At least one constructor must be visible to subclasses to allow
	 * inheritance
	 */
	protected PRMToBNCompiler() {
		this.networkBuilder = DefaultProbabilisticNetworkBuilder.newInstance();
		this.cptCompiler = PRMCPTCompiler.newInstance();
		this.bnCompilationAlgorithm = new JunctionTreeAlgorithm();
	}

	/**
	 * Default construction method
	 * 
	 * @return
	 */
	public static PRMToBNCompiler newInstance() {
		PRMToBNCompiler ret = new PRMToBNCompiler();
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * unbbayes.prs.prm.compiler.IPRMCompiler#compile(unbbayes.controller.prm
	 * .IDatabaseController, unbbayes.prs.prm.IPRM, java.util.Collection)
	 */
	public Graph compile(IDatabaseController databaseController, IPRM prm,
			Collection<IAttributeValue> query) {

		// Input assertion
		if (prm == null) {
			return null;
		}

		// TODO ALPHA version allows only single node query.
		if (query == null || query.size() != 1) {
			throw new IllegalArgumentException(
					"ALPHA version only allows a query to a single node. Select exactly 1 node to start compiling PRM.");
		}

		// Network to be returned
		ProbabilisticNetwork resultNet = networkBuilder.buildNetwork(prm
				.getName());

		// Build nodes (with no edges yet). We consider only nodes having a path
		// to the query node
		// TODO Use some D-separation to avoid creation of unnecessary nodes
		// TODO ALPHA version allows only single node query.
		// nodes we are working on - this is basically a map from
		// IAttributeValue to pairs of (IAttributeValue, ProbabilisticNode)
		// We are using maps to speed up search
		Map<IAttributeValue, PRMNode> workingNodes = new HashMap<IAttributeValue, PRMNode>();

		// Fill related nodes with query. (Currently only one query supported)
		// it also add query.iterator().next() to workingNodes
		fillRelatedNodes(query.iterator().next(), workingNodes);

		// Add working nodes to the network.
		for (PRMNode prmNode : workingNodes.values()) {
			resultNet.addNode(prmNode.getBnNode());
		}

		// Build edges (consider only 1 direction - node to parent - to avoid
		// repetition)
		for (PRMNode node : workingNodes.values()) {
			for (INode parentNode : node.getParentNodes()) { // use buffered
																// parents
				Edge edge = new Edge(workingNodes.get(parentNode).getBnNode(),
						node.getBnNode());
				try {
					resultNet.addEdge(edge);
				} catch (InvalidParentException e) {
					throw new RuntimeException("Could not add edge from "
							+ node.getWrappedPrmNode() + " to " + parentNode, e);
				}
			}
		}

		// Build a map from ProbabilisticNode to IAttributeValue by reversing
		// workingNodes.
		// This is possible because PRMNode/ProbabilisticNode and
		// IAttributeValue is 1-1 mapping
		Map<INode, IAttributeValue> inverseWorkingNodesMap = new HashMap<INode, IAttributeValue>();
		for (IAttributeValue key : workingNodes.keySet()) {
			inverseWorkingNodesMap.put(workingNodes.get(key).getBnNode(), key); // put
																				// almost
																				// as
																				// inverse
																				// mapping
		}

		// Build CPT
		for (PRMNode workingNode : workingNodes.values()) {
			// ignore the returned value (IPRobabilityFunction), because it
			// supposedly works on-place
			// (it is a in/out argument) passed indirectly from
			// workingNode.getBnNode()
			cptCompiler.compileCPT(workingNode.getWrappedPrmNode(),
					workingNode.getBnNode(), inverseWorkingNodesMap);
		}

		// set up findings of the resulting BN
		try {
			compileBN(resultNet);
			fillFindings(workingNodes.values(), resultNet);
			propagateFindings(resultNet);
		} catch (Exception e) {
			// ignore any exception, because we can still show the uncompiled BN
			e.printStackTrace();
		}

		return resultNet;
	}

	/**
	 * This method is called at the end of
	 * {@link #compile(IDatabaseController, IPRM, Collection)}. Basically, it
	 * calls {@link #getBnCompilationAlgorithm()} and
	 * {@link IInferenceAlgorithm#propagate()}. Update evidences of Bayesian
	 * Network.
	 * 
	 * @param net
	 */
	protected void propagateFindings(ProbabilisticNetwork net) {
		if (net != null) {
			bnCompilationAlgorithm.setNetwork(net);
			// the code below is almost equivalent to net.updateEvidences();
			bnCompilationAlgorithm.propagate();
		}
	}

	/**
	 * Compile the generated BN This method is called at the end of
	 * {@link #compile(IDatabaseController, IPRM, Collection)}. Basically, it
	 * calls {@link #getBnCompilationAlgorithm()} and
	 * {@link IInferenceAlgorithm#run()}.
	 * 
	 * @param net
	 *            network generated by
	 *            {@link #compile(IDatabaseController, IPRM, Collection)}
	 * @throws Exception
	 */
	protected void compileBN(ProbabilisticNetwork net) {
		if (net != null) {
			bnCompilationAlgorithm.setNetwork(net);
			// the code below is almost equivalent to net.compile();
			bnCompilationAlgorithm.run();
			// the code below is almost equivalent to net.initialize();
			bnCompilationAlgorithm.reset();
		}
	}

	/**
	 * Fill up the findings. This method is called at the end of
	 * {@link #compile(IDatabaseController, IPRM, Collection)}
	 * 
	 * @param workingNodes
	 *            : pairs of {@link PRMNode} and {@link IAttributeValue}.
	 * @param net
	 *            : network generated by
	 *            {@link #compile(IDatabaseController, IPRM, Collection)}
	 */
	protected void fillFindings(Collection<PRMNode> workingNodes,
			ProbabilisticNetwork net) {
		// find evidences (nodes containing known values ->
		// (IAttributeValue#getValue() != null))
		for (PRMNode workingNode : workingNodes) {
			if ((workingNode == null) || (workingNode.getBnNode() == null)
					|| (workingNode.getWrappedPrmNode() == null)
					|| (workingNode.getWrappedPrmNode().getValue() == null)) {
				// this is not a finding or, even, this is not a consistent PRM
				// node... Ignore
				continue;
			}
			// set up known value
			TreeVariable bnNode = workingNode.getBnNode();
			String knownValue = workingNode.getWrappedPrmNode().getValue();
			for (int i = 0; i < bnNode.getStatesSize(); i++) {
				if (bnNode.getStateAt(i).equals(knownValue)) {
					bnNode.addFinding(i);
					break;
				}
			}
		}
	}

	/**
	 * Obtains recursively the ancestors and descendants of a prm node, building
	 * a network connected to the parameter "value"
	 * 
	 * @param value
	 *            : the center node (all nodes obtained by this method must have
	 *            a path to it)
	 * @param evaluated
	 *            : in/out argument. Nodes already having a path to "value".
	 */
	private void fillRelatedNodes(IAttributeValue value,
			Map<IAttributeValue, PRMNode> evaluated) {

		// Assertion of input/output
		if (value == null || evaluated == null) {
			return;
		}

		// if evaluated contains value, it is not evaluated
		if (evaluated.get(value) != null
				&& value.equals(evaluated.get(value).getWrappedPrmNode())) {
			return;
		}

		// Create the value/node pair
		PRMNode pair = new PRMNode(value, (ProbabilisticNode) networkBuilder
				.getProbabilisticNodeBuilder().buildNode());
		pair.getBnNode().setName(pair.getWrappedPrmNode().toString());
		// TODO use a more decent way to set variable position
		pair.getBnNode().setPosition(Math.random() * 400, Math.random() * 300);

		// Build state
		for (int i = 0; i < pair.getWrappedPrmNode().getAttributeDescriptor()
				.getStatesSize(); i++) {
			pair.getBnNode().appendState(
					pair.getWrappedPrmNode().getAttributeDescriptor()
							.getStateAt(i));
		}

		// TODO refactor core to automatically add itself to the table it owns,
		// instead of forcing callers to do it manually
		// ... it seems that we have to add states before touching to
		// probability function...
		pair.getBnNode().getProbabilityFunction().addVariable(pair.getBnNode());

		// mark as evaluated
		evaluated.put(value, pair);

		// Iterate over children. It is not buffered, so start a query to chain
		// solver
		for (IAttributeValue attributeValue : value.getDependencyChainSolver()
				.solveChildren(value)) {
			if (!evaluated.keySet().contains(attributeValue)) {
				// recursive call
				this.fillRelatedNodes(attributeValue, evaluated);
			}
		}

		// Iterate over parents (use buffered values)
		for (INode inode : pair.getParentNodes()) {
			if (!evaluated.keySet().contains(inode)) {
				// recursive call
				this.fillRelatedNodes((IAttributeValue) inode, evaluated);
			}
		}

	}

	/**
	 * This class is a temporary representation of nodes of PRM in a compilation
	 * process. It contains both references to {@link IAttributeValue} and
	 * references to the {@link ProbabilisticNode}s of the resulting
	 * {@link unbbayes.prs.bn.ProbabilisticNetwork}. It also wraps
	 * {@link IAttributeValue}. This is basically a IAttributeValue containing a
	 * reference to a {@link ProbabilisticNode} and organized in a graph
	 * structure (the same structure of the resulting network). It also stores
	 * the parents locally (buffer), so that we do not need to use
	 * {@link #getDependencyChainSolver()} to solve parents
	 * 
	 * @author Shou Matsumoto
	 * 
	 */
	protected class PRMNode implements IAttributeValue {

		private IAttributeValue prmNode;
		private ProbabilisticNode bnNode;
		private List<INode> parentNodes;

		public PRMNode(IAttributeValue prmNode, ProbabilisticNode bnNode) {
			super();
			this.prmNode = prmNode;
			this.bnNode = bnNode;
			// fill buffer of parent nodes
			this.parentNodes = new ArrayList<INode>(prmNode
					.getDependencyChainSolver().solveParents(prmNode));
		}

		/**
		 * @return the prmNode
		 */
		public IAttributeValue getWrappedPrmNode() {
			return prmNode;
		}

		/**
		 * @param prmNode
		 *            the prmNode to set
		 */
		public void setWrappedPrmNode(IAttributeValue prmNode) {
			this.prmNode = prmNode;
		}

		/**
		 * @return the bnNode
		 */
		public ProbabilisticNode getBnNode() {
			return bnNode;
		}

		/**
		 * @param bnNode
		 *            the bnNode to set
		 */
		public void setBnNode(ProbabilisticNode bnNode) {
			this.bnNode = bnNode;
		}

		/**
		 * @param arg0
		 * @throws InvalidParentException
		 * @see unbbayes.prs.INode#addChildNode(unbbayes.prs.INode)
		 */
		public void addChildNode(INode arg0) throws InvalidParentException {
			prmNode.addChildNode(arg0);
		}

		/**
		 * @param arg0
		 * @throws InvalidParentException
		 * @see unbbayes.prs.INode#addParentNode(unbbayes.prs.INode)
		 */
		public void addParentNode(INode arg0) throws InvalidParentException {
			prmNode.addParentNode(arg0);
		}

		/**
		 * @param arg0
		 * @see unbbayes.prs.INode#appendState(java.lang.String)
		 */
		public void appendState(String arg0) {
			prmNode.appendState(arg0);
		}

		/**
		 * @return
		 * @see unbbayes.prs.INode#getAdjacentNodes()
		 */
		public List<INode> getAdjacentNodes() {
			// TODO implement
			throw new RuntimeException("Not implemented yet");
		}

		/**
		 * @return
		 * @see unbbayes.prs.prm.IAttributeValue#getAttributeDescriptor()
		 */
		public IAttributeDescriptor getAttributeDescriptor() {
			return prmNode.getAttributeDescriptor();
		}

		/**
		 * @return
		 * @see unbbayes.prs.INode#getChildNodes()
		 */
		public List<INode> getChildNodes() {
			// TODO implement
			throw new RuntimeException("Not implemented yet");
		}

		/**
		 * @return
		 * @see unbbayes.prs.prm.IAttributeValue#getContainerObject()
		 */
		public IPRMObject getContainerObject() {
			return prmNode.getContainerObject();
		}

		/**
		 * @return
		 * @see unbbayes.prs.prm.IAttributeValue#getDependencyChainSolver()
		 */
		public IDependencyChainSolver getDependencyChainSolver() {
			return prmNode.getDependencyChainSolver();
		}

		/**
		 * @return
		 * @see unbbayes.prs.INode#getDescription()
		 */
		public String getDescription() {
			return prmNode.getDescription();
		}

		/**
		 * @return
		 * @see unbbayes.prs.INode#getName()
		 */
		public String getName() {
			return prmNode.getName();
		}

		/**
		 * This is a buffer of parent nodes. By filling a buffer, we do not need
		 * to use {@link #getDependencyChainSolver()} at each access.
		 * 
		 * @return instances of {@link IAttributeValue}
		 * @see unbbayes.prs.INode#getParentNodes()
		 */
		public List<INode> getParentNodes() {
			return this.parentNodes;
		}

		/**
		 * @param arg0
		 * @return
		 * @see unbbayes.prs.INode#getStateAt(int)
		 */
		public String getStateAt(int arg0) {
			return prmNode.getStateAt(arg0);
		}

		/**
		 * @return
		 * @see unbbayes.prs.INode#getStatesSize()
		 */
		public int getStatesSize() {
			return prmNode.getStatesSize();
		}

		/**
		 * @return
		 * @see unbbayes.prs.INode#getType()
		 */
		public int getType() {
			return prmNode.getType();
		}

		/**
		 * @return
		 * @see unbbayes.prs.prm.IAttributeValue#getValue()
		 */
		public String getValue() {
			return prmNode.getValue();
		}

		/**
		 * @param arg0
		 * @see unbbayes.prs.INode#removeChildNode(unbbayes.prs.INode)
		 */
		public void removeChildNode(INode arg0) {
			prmNode.removeChildNode(arg0);
		}

		/**
		 * 
		 * @see unbbayes.prs.INode#removeLastState()
		 */
		public void removeLastState() {
			prmNode.removeLastState();
		}

		/**
		 * @param arg0
		 * @see unbbayes.prs.INode#removeParentNode(unbbayes.prs.INode)
		 */
		public void removeParentNode(INode arg0) {
			prmNode.removeParentNode(arg0);
		}

		/**
		 * @param arg0
		 * @see unbbayes.prs.INode#removeStateAt(int)
		 */
		public void removeStateAt(int arg0) {
			prmNode.removeStateAt(arg0);
		}

		/**
		 * @param attributeDescriptor
		 * @see unbbayes.prs.prm.IAttributeValue#setAttributeDescriptor(unbbayes.prs.prm.IAttributeDescriptor)
		 */
		public void setAttributeDescriptor(
				IAttributeDescriptor attributeDescriptor) {
			prmNode.setAttributeDescriptor(attributeDescriptor);
		}

		/**
		 * @param arg0
		 * @see unbbayes.prs.INode#setChildNodes(java.util.List)
		 */
		public void setChildNodes(List<INode> arg0) {
			prmNode.setChildNodes(arg0);
		}

		/**
		 * @param prmObject
		 * @see unbbayes.prs.prm.IAttributeValue#setContainerObject(unbbayes.prs.prm.IPRMObject)
		 */
		public void setContainerObject(IPRMObject prmObject) {
			prmNode.setContainerObject(prmObject);
		}

		/**
		 * @param solver
		 * @see unbbayes.prs.prm.IAttributeValue#setDependencyChainSolver(unbbayes.prs.prm.IDependencyChainSolver)
		 */
		public void setDependencyChainSolver(IDependencyChainSolver solver) {
			prmNode.setDependencyChainSolver(solver);
		}

		/**
		 * @param arg0
		 * @see unbbayes.prs.INode#setDescription(java.lang.String)
		 */
		public void setDescription(String arg0) {
			prmNode.setDescription(arg0);
		}

		/**
		 * @param arg0
		 * @see unbbayes.prs.INode#setName(java.lang.String)
		 */
		public void setName(String arg0) {
			prmNode.setName(arg0);
		}

		/**
		 * This is a buffer of parent nodes. By filling a buffer, we do not need
		 * to use {@link #getDependencyChainSolver()} at each access.
		 * 
		 * @param arg0
		 * @see unbbayes.prs.INode#setParentNodes(java.util.List)
		 */
		public void setParentNodes(List<INode> arg0) {
			this.parentNodes = arg0;
		}

		/**
		 * @param arg0
		 * @param arg1
		 * @see unbbayes.prs.INode#setStateAt(java.lang.String, int)
		 */
		public void setStateAt(String arg0, int arg1) {
			prmNode.setStateAt(arg0, arg1);
		}

		/**
		 * @param arg0
		 * @see unbbayes.prs.INode#setStates(java.util.List)
		 */
		public void setStates(List<String> arg0) {
			prmNode.setStates(arg0);
		}

		/**
		 * @param value
		 * @see unbbayes.prs.prm.IAttributeValue#setValue(java.lang.String)
		 */
		public void setValue(String value) {
			prmNode.setValue(value);
		}

	}

	/** 	 
     * Builders to help constructing correct instances of {@link Network} and {@link Node} 	 
     * @return the networkBuilder 	 
     */ 	 
    public IProbabilisticNetworkBuilder getNetworkBuilder() { 	 
            return networkBuilder; 	 
    } 	 
	 
    /** 	 
     * @param networkBuilder the networkBuilder to set 	 
     */ 	 
    public void setNetworkBuilder(IProbabilisticNetworkBuilder networkBuilder) { 	 
            this.networkBuilder = networkBuilder; 	 
    } 	 
	 
    /** 	 
     * @return the cptCompiler 	 
     */ 	 
    public IPRMCPTCompiler getCptCompiler() { 	 
            return cptCompiler; 	 
    } 	 
	 
    /** 	 
     * @param cptCompiler the cptCompiler to set 	 
     */ 	 
    public void setCptCompiler(IPRMCPTCompiler cptCompiler) { 	 
            this.cptCompiler = cptCompiler; 	 
    }
    
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * unbbayes.prs.prm.compiler.IBNInferenceAlgorithmHolder#getBNInferenceAlgorithm
	 * ()
	 */
	public IInferenceAlgorithm getBNInferenceAlgorithm() {
		return this.bnCompilationAlgorithm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * unbbayes.prs.prm.compiler.IBNInferenceAlgorithmHolder#setBNInferenceAlgorithm
	 * (unbbayes.util.extension.bn.inference.IInferenceAlgorithm)
	 */
	public void setBNInferenceAlgorithm(IInferenceAlgorithm inferenceAlgorithm) {
		this.bnCompilationAlgorithm = inferenceAlgorithm;
	}

}
