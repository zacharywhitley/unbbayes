/**
 * 
 */
package unbbayes.prs.bn;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.id.UtilityTable;
import unbbayes.util.Debug;
import unbbayes.util.dseparation.impl.MSeparationUtility;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithmListener;

/**
 * This is a customization of {@link JunctionTreeAlgorithm} which dynamically (incrementally)
 * builds (compiles) a junction tree given the junction tree previously built.
 * @author Shou Matsumoto
 *
 */
public class IncrementalJunctionTreeAlgorithm extends JunctionTreeAlgorithm {
	

	/** This is used in {@link StubClique} as the default content of utility tables. It is static in order to avoid using unnecessary memory */
	public static final PotentialTable DEFAULT_SINGLETON_UTILITY_TABLE = new UtilityTable();
	/** This is used in {@link StubClique} as the default content of potential tables. It is static in order to avoid unnecessary memory garbage */
	public static final PotentialTable DEFAULT_SINGLETON_POTENTIAL_TABLE = new ProbabilisticTable();
	
	/** A builder that instantiates an instance of {@link LoopyJunctionTree}, which enables loopy belief propagation of cliques when {@link LoopyJunctionTree#isLoopy()} is true. */
	public static final IJunctionTreeBuilder DEFAULT_LOOPY_JT_BUILDER = new IJunctionTreeBuilder() {
		public IJunctionTree buildJunctionTree(Graph network) throws InstantiationException, IllegalAccessException {
			return new LoopyJunctionTree();
		}
	};


	/** {@link #run()} will use dynamic junction tree compilation if number of nodes is above this value */
	private int dynamicJunctionTreeNetSizeThreshold = 100;//Integer.MAX_VALUE;	// setting to large values will disable dynamic junction tree compilation
	
	/** Set this to true if you need {@link #run()} to throw exception when {@link #runDynamicJunctionTreeCompilation()} fails. */
	private boolean isToHaltOnDynamicJunctionTreeFailure = false;
	

	/** Copy of the network used when {@link #run()} was executed the previous time */
	private ProbabilisticNetwork netPreviousRun = null;
	
	private int loopyBPCliqueSizeThreshold = Integer.MAX_VALUE; //16384;
	
	
	private ICliqueSplitter cliqueSplitter = SINGLE_CYCLE_HALF_SIZE_SEPARATOR_CLIQUE_SPLITTER;
	
	private boolean isToSplitVirtualNodeClique = false;
	
	private boolean isToCreateVirtualNode = false;
	
	/** By default, disable dynamic jt compilation if it is using loopy bp */
	private boolean isToCompileNormallyWhenLoopy = false;//true;
	
	/** If true, the loopy BP algorithm and JT compilation does not guarantee that a node and all its parents will be in the same clique */
	private boolean isToUseAgressiveLoopyBP = false;
	
	/** if true, then {@link #runDynamicJunctionTreeCompilation()} will
	 * compile the junction tree normally without using incremental junction tree compilation if
	 * {@link LoopyJunctionTree#isLoopy()} and the changes are only arc removals. If false, then
	 * {@link #runDynamicJunctionTreeCompilation()} will run incremental junction tree compilation always. */
	private boolean isToCompileNormallyWhenLoopyAndDeletingArcs = true;
	

	/**
	 * This clique splitter will split the clique in the way that
	 * the 1st clique will have the first N variables that fills desired size,
	 * the second clique will have the next M variables starting from (N-N/2)-th variable
	 * (so that the separator between the K-th and (K+1)th variable has N/2 variables),
	 * and the last clique will have variables in common with the 1st clique (so that it will
	 * generate a cycle of common variables).
	 * <br/> <br/>
	 * For example, suppose the provided clique is {A,B,C,D,E,F,G} (with all binary variables), 
	 * and the desired size is 2^3 (so, each resulting clique shall have 3 variables). Then, this object
	 * will create the following cliques:
	 * <br/> <br/>
	 * {A,B,C},{C,D,E}, {E,F,G},{G,A,B}
	 */
	public static final ICliqueSplitter SINGLE_CYCLE_HALF_SIZE_SEPARATOR_CLIQUE_SPLITTER = new ICliqueSplitter() {
		public List<Clique> splitClique(Clique cliqueToSplit, IJunctionTree jt, int desiredSize) {
			
			// prepare the list to return already
			List<Clique> ret = new ArrayList<Clique>();
			
			// extract the nodes to be included in the cliques
			// we'll edit clique tables first, so keep the ordering of variables the same of original clique table
			List<Node> nodesList = cliqueToSplit.getProbabilityFunction().variableList;	
			final int numNodes = nodesList.size();	// a local variable just to avoid invoking List#size() multiple times
			
			// iterate on nodes
			for (int nodeIndex = 0; nodeIndex < numNodes; ) {
				
				// instantiate the clique in advance
				Clique clique = new Clique();
				
				// also extract the clique table in advance, so that we can fill it immediately
				PotentialTable currentCliqueTable = clique.getProbabilityFunction();
				
				// add nodes to clique table until we reach desired table size
				while (currentCliqueTable.tableSize() < desiredSize) {
					// using mod (i.e. "%") in nodeIndex, so that if nodeIndex exceeds size of nodesList, then we go back to first few nodes again.
					currentCliqueTable.addVariable(nodesList.get(nodeIndex % numNodes));	
					nodeIndex++;
					// stop iteration if by adding the next node it will exceed desired table size
					if ( ( currentCliqueTable.tableSize() * nodesList.get(nodeIndex % numNodes).getStatesSize() ) > desiredSize) {
						if ((nodeIndex - 1) < numNodes) { 
							// step back nodeIndex, so that the clique of next iteration has some nodes in common
							nodeIndex -= currentCliqueTable.variableCount()/2;	// last half (truncated) of the variables in current clique will be in common with next clique
						} // or else, we already completed a "loop" (i.e. created a clique containing last few nodes and first few nodes)
							
						break;
					}
				}
				
				// content of new clique table shall be filled with original clique table (summing out unnecessary variables);
				PotentialTable sumOutOriginalCliqueTable = cliqueToSplit.getProbabilityFunction().getTemporaryClone();	// use a clone, so that we keep original untouched
				// check which variables we shall sum out from original clique table in order to retain the variables in new clique table
				List<INode> nodesToSumOut = new ArrayList<INode>(sumOutOriginalCliqueTable.variableList);
				nodesToSumOut.removeAll(currentCliqueTable.variableList);	// the list will contain variables in original clique that is not in current clique
				// sum out unnecessary variables
				for (INode nodeToSumOut : nodesToSumOut) {
					sumOutOriginalCliqueTable.removeVariable(nodeToSumOut);
				}
				// just an assertion
				if (sumOutOriginalCliqueTable.tableSize() != currentCliqueTable.tableSize()) {
					throw new RuntimeException("Expected clique table's size of " + currentCliqueTable.variableList + " is " + sumOutOriginalCliqueTable.tableSize() + ", but found " + currentCliqueTable.tableSize());
				}
				// copy values
				System.arraycopy(sumOutOriginalCliqueTable.getValues(), 0, currentCliqueTable.getValues(), 0, currentCliqueTable.tableSize());
				
				// check if this is the last clique (the one which completes a loop).
				if (nodeIndex >= numNodes) {
					// it has variables in some order that is different from original clique table, 
					// so we'd better reorder the variables to keep the same ordering
					currentCliqueTable.variableList = new ArrayList<Node>(sumOutOriginalCliqueTable.variableList);
				}
				
				// fill clique with the nodes we just included in clique table
				clique.getNodesList().addAll(currentCliqueTable.variableList);
				Collections.reverse(clique.getNodesList());	// the order of nodes in clique is the inverse of variables in clique table
				
				ret.add(clique);
			}
			
			return ret;
		}
	};
	
	/**
	 * Default constructor.
	 * The network to compile must be set before calling other methods of this class,
	 * by calling {@link #setNetwork(unbbayes.prs.Graph)} or {@link #setNet(ProbabilisticNetwork)}
	 */
	public IncrementalJunctionTreeAlgorithm() {
		super();
	}

	/**
	 * Default constructor initializing fields.
	 * @param net
	 * @see #getNet()
	 * @see #setNet(ProbabilisticNetwork)
	 */
	public IncrementalJunctionTreeAlgorithm(ProbabilisticNetwork net) {
		super(net);
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
	

	/**
	 * @return {@link #run()} will use dynamic junction tree compilation if number of nodes is above this value.
	 * @see #getNodesPreviousRun()
	 * @see #getEdgesPreviousRun()
	 */
	public int getDynamicJunctionTreeNetSizeThreshold() {
		return dynamicJunctionTreeNetSizeThreshold;
	}

	/**
	 * This method updates the value {@link #getDynamicJunctionTreeNetSizeThreshold()}.
	 * If this value is set higher than the current number of nodes in {@link #getNet()},
	 * then 
	 * @param dynamicJunctionTreeNetSizeThreshold : {@link #run()} will use dynamic junction tree compilation if number of nodes is above this value.
	 * @see #getNetPreviousRun()
	 */
	public void setDynamicJunctionTreeNetSizeThreshold(
			int dynamicJunctionTreeNetSizeThreshold) {
		this.dynamicJunctionTreeNetSizeThreshold = dynamicJunctionTreeNetSizeThreshold;
		if (getNet() != null ) {
			if (dynamicJunctionTreeNetSizeThreshold >= getNet().getNodeCount()) {
				// clear the network cache
				this.setNetPreviousRun(null);
			} else if (this.getNetPreviousRun() == null) {
				// current number of nodes is higher than threshold, and we don't have a network before the previous run.
				// we need to initialize it, so that next call to #run() can use dynamic JT compilation
				this.fillNetPreviousRun();
			}
		}
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

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#run()
	 */
	public void run() throws IllegalStateException {
		
		// if this is true, we will not attempt to use dynamic junction tree compilation.
		// I'm using a boolean var here instead of putting it directly in the if-clause, 
		// because if an exception if thrown, I want to set this to true and compile junction tree normally
		boolean isToCompileNormally = this.getNet().getNodes().size() <= getDynamicJunctionTreeNetSizeThreshold()
									|| getNetPreviousRun() == null	// if there is no way to retrieve what were the changes from previous net, then just compile normally
									// if there is no junction tree to reuse, then do not use dynamic junction tree compilation
									|| getJunctionTree() == null
									|| getJunctionTree().getCliques() == null
									|| getJunctionTree().getCliques().isEmpty();
		
		// TODO run incremental JT compilation for influence diagrams as well
//		if (!isToCompileNormally
//				&& getNet().isID()) {
//			isToCompileNormally = true;
//		}
		
		// disable incremental/dynamic compilation if this is loopy and it is configured to disable it in such case
		if (!isToCompileNormally
				&& isToCompileNormallyWhenLoopy()
				&& getJunctionTree().isUsingApproximation()) {
			isToCompileNormally = true;
//			((LoopyJunctionTree)getJunctionTree()).setLoopy(false);	// reset the configuration, so that this becomes true only when explicitly set to true
			// the above line is not necessary, because when compiling normally the old instance of LoopyJunctionTree is discarded anyway
		}
		
		// check if we should use dynamic junction tree compilation
		if ( !isToCompileNormally ) {
			try {
				// make sure all internal IDs were initialized
				this.initInternalIdentificators();
				
				for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
					listener.onBeforeRun(this);
				}
				if (this.getNet() == null
						|| this.getNet().getNodes().size() == 0) {
					throw new IllegalStateException(resource.getString("EmptyNetException"));
				}
				
				this.runDynamicJunctionTreeCompilation();
				

				for (IInferenceAlgorithmListener listener : this.getInferenceAlgorithmListeners()) {
					listener.onAfterRun(this);
				}
			} catch (Throwable e) {
				Debug.println(getClass(), "Unable to dynamically compile junction tree. Compiling normally...", e);
				if (isToHaltOnDynamicJunctionTreeFailure()) {
					// throw exception if this algorithm is configured to do so
					throw new RuntimeException("Unable to dynamically compile junction tree.", e);
				}
				isToCompileNormally = true;	// set the flag, so that we can run the normal junction tree compilation in the next if-clause
			}
		} 
		
		if (isToCompileNormally) {
			// run ordinary junction tree compilation
			super.run();
		}
		
		if (isConditionForClusterLoopyBPSatisfied(getNet())) {
			this.buildLoopyCliques(getNet());
		}
		
		
		// update backup of network if necessary
		if (this.getNet().getNodes().size() > getDynamicJunctionTreeNetSizeThreshold()) {
			this.fillNetPreviousRun();
		}
		
	}
	

	/**
	 * This method extends the superclass method in order to keep track of which arcs were included during
	 * triangulation (i.e. during elimination). If clique sizes gets larger than {@link #getLoopyBPCliqueSizeThreshold()}
	 * due to these arcs, then such arcs will be removed, {@link SingleEntityNetwork#getMarkovArcs()}
	 * and {@link Node#getAdjacents()} will be properly updated. 
	 * Deletion of such triangulation arcs may cause cliques not to be connected in {@link #strongTreeMethod(ProbabilisticNetwork, List, IJunctionTree)},
	 * so {@link #buildLoopyCliques(ProbabilisticNetwork)} shall be called afterwards in such cases.
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#triangulate(unbbayes.prs.bn.ProbabilisticNetwork)
	 * @see SingleEntityNetwork#getMarkovArcs()
	 * @see Node#getAdjacents()
	 * @see LoopyJunctionTree#setLoopy(boolean)
	 */
	public List<INode> triangulate(ProbabilisticNetwork net) {
		
		// influence diagram (ID) is not supported by loopy BP anyway, so check if this is an ID
		boolean isInfluenceDiagram = net.isID();
		
		// backup the content of markov arcs, because we'll check what are the new arcs included during triangulation
		Set<Edge> markovArcsBeforeTriangulation = Collections.EMPTY_SET;
		if (!isInfluenceDiagram) {
			markovArcsBeforeTriangulation = new HashSet<Edge>(net.getMarkovArcs());
		}
		
		// run triangulation
		List<INode> nodeEliminationOrder = super.triangulate(net);
		
		if (isInfluenceDiagram) {
			// don't do anything special if this is an influence diagram.
			return nodeEliminationOrder;
		}
		
		// obtain the arcs created during triangulation
		List<Edge> arcsCreatedInTriangulation = new ArrayList<Edge>(net.getMarkovArcs().size()-markovArcsBeforeTriangulation.size());
		for (Edge arc : net.getMarkovArcs()) {
			if (!markovArcsBeforeTriangulation.contains(arc)) {
				arcsCreatedInTriangulation.add(arc);
			}
		}
		
		// check if the size of cliques related to the triangulation arcs are likely to cause large clique sizes
		boolean isTriangulationCausingLargeCliques = false;
		for (Edge arc : arcsCreatedInTriangulation) {
			// the largest clique caused by this arc is the product space of all nodes adjacent to the node connected by this arc
			
			// first, check with one of the node in arc
			Node nodeInArc = arc.getOriginNode();
			
			// this will be the size of the product space of states of current node and all nodes adjacent to it
			int stateSpace = nodeInArc.getStatesSize(); 
			int eliminationOrderOfNodeInArc = nodeEliminationOrder.indexOf(nodeInArc);	// this is the order of elimination used when triangulating this node
			for (Node adjacentNode : nodeInArc.getAdjacents()) {
				if (nodeEliminationOrder.indexOf(adjacentNode) > eliminationOrderOfNodeInArc) { // only nodes in later position in elimination order are included in cliques
					stateSpace *= adjacentNode.getStatesSize(); 
				}
			}
			
			// if any triangulation arc can cause a clique to be larger than a threshold, then it is sufficient condition to start loopy BP
			if (stateSpace > getLoopyBPCliqueSizeThreshold()) {
				isTriangulationCausingLargeCliques = true;
				break;
			}
			
			// well, check the same condition with the other node in arc too
			nodeInArc = arc.getDestinationNode();
			stateSpace = nodeInArc.getStatesSize(); 
			eliminationOrderOfNodeInArc = nodeEliminationOrder.indexOf(nodeInArc);	// this is the order of elimination used when triangulating this node
			for (Node adjacentNode : nodeInArc.getAdjacents()) {
				if (nodeEliminationOrder.indexOf(adjacentNode) > eliminationOrderOfNodeInArc) { // only nodes in later position in elimination order are included in cliques
					stateSpace *= adjacentNode.getStatesSize();
				}
			}
			
			// if any triangulation arc can cause a clique to be larger than a threshold, then it is sufficient condition to start loopy BP
			if (stateSpace > getLoopyBPCliqueSizeThreshold()) {
				isTriangulationCausingLargeCliques = true;
				break;
			}
			
		}
		
		// a condition to trigger loopy BP in cluster structure was satisfied, so undo the triangulation
		if (isTriangulationCausingLargeCliques) {
			// remove the arcs created during triangulation
//			net.getMarkovArcs().removeAll(arcsCreatedInTriangulation);
			/*
			 * if we are using leaf->root ordering of node elimination, then we don't need the moralization,
			 * because a child node is always explicitly connected with all its parents, and if it is eliminated first,
			 * then the corresponding #clique method will first generate cliques of nodes adjacent to nodes eliminated first
			 * (and nodes adjacent to nodes eliminated first -- i.e. leaves/children -- are always the entire set of its parents).
			 */
			net.getMarkovArcs().clear(); 
			
			// rebuild the list of neighbors/adjacency of each node, considering that some arcs were removed
			// by removing the arcs and rebuilding the adjacency information, the other methods won't consider arcs created by triangulation
			makeAdjacents(net);
			
			// extract all probabilistic nodes
			List<Node> nodesYetToAddress = new ArrayList<Node>(net.getNodes());
			
			// convert the node elimination order to consider leaves/children first, and then the parents/root
			nodeEliminationOrder.clear();
			while (!nodesYetToAddress.isEmpty()) {
				
				// find a leaf node
				int numNodes = nodesYetToAddress.size();	// extract the size it in advance, so that we don't call the getter on every iteration
				INode leaf = null; 	// the node to look for
				int indexOfLeaf;	// index (in the list nodesYetToAddress) of the node to look for
				for (indexOfLeaf = 0; indexOfLeaf < numNodes; indexOfLeaf++) {
					
					leaf = nodesYetToAddress.get(indexOfLeaf);	// current node in iteration
					
					// check if this is really a leaf node
					if (leaf.getChildNodes().isEmpty()) { 
						// if this is a leaf in original BN, this is immediately a leaf
						break;	// just use the 1st one we found
					} else {
						// or if it becomes a leaf by removing all eliminated nodes from list of children, then it is also a leaf in current scope.
						List<INode> children = new ArrayList<INode>(leaf.getChildNodes());	// use a copy, so that we don't modify the original
						children.retainAll(nodesYetToAddress);	// remove all eliminated nodes
						if (children.isEmpty()) {
							break; // just use the 1st one we found
						}
					}
					
				}
				
				// eliminate it
				nodeEliminationOrder.add(leaf);
				nodesYetToAddress.remove(indexOfLeaf);
			}
		}
		
		return nodeEliminationOrder;	
	}
	
	
	/**
	 * Checks for conditions to activate cluster (clique) loopy BP.
	 * If conditions could not be tested, this will return false anyway.
	 * @param net : network to check for conditions.
	 * @return true if conditions are satisfied. False otherwise.
	 * @see #setLoopy(boolean)
	 * @see #isToUseAgressiveLoopyBP()
	 * @see #triangulate(ProbabilisticNetwork)
	 */
	public boolean isConditionForClusterLoopyBPSatisfied( ProbabilisticNetwork net) {
		
		// basic assertions
		if (net == null || net.getJunctionTree() == null || net.getJunctionTree().getCliques() == null) {
			return false;	// false is the default value.
		}
		
		if (isToUseAgressiveLoopyBP()) {
			// activate loopy BP if clique size got too large
			List<Clique> largestCliques = getLargestCliques(net);
			if (largestCliques != null &&  !largestCliques.isEmpty() 
					&& ( largestCliques.get(0).getProbabilityFunction().tableSize() > getLoopyBPCliqueSizeThreshold() ) ) {
				return true;
			}
		} else {
			// if we are not using aggressive loopy BP structure, then the condition for a loopy structure
			// is to have some cliques disconnected from other cliques due to #triangulate removing arcs created by node elimination 
			if (net.getJunctionTree().getCliques().size() > 1) {
				// count the number of cliques with null parents. If more than 1, then there are cliques disconnected from the rest (a condition to require #buildLoopyCliques)
				int numCliquesWithNoParents = 0;
				for (Clique clique : net.getJunctionTree().getCliques()) {
					if (clique.getParent() == null) {
						numCliquesWithNoParents++;
					}
				}
				return numCliquesWithNoParents > 1;
			}
		}
		
		// by default, loopy BP shall not be used
		return false;
	}

	
	


	/**
	 * This method will update {@link #setNetPreviousRun(ProbabilisticNetwork)}
	 * with a clone of the current network in {@link #getNet()}.
	 * This will avoid cloning the {@link #getJunctionTree()}, though.
	 * @see #getNetPreviousRun()
	 * @see #getDynamicJunctionTreeNetSizeThreshold()
	 * @see #setDynamicJunctionTreeNetSizeThreshold(int)
	 * @see #run()
	 */
	protected void fillNetPreviousRun() {
		// disconnect junction tree from network to clone, because there's no need for a clone of the junction tree.
		IJunctionTree junctionTreeNotToIncludeInClone = getJunctionTree();
		getNet().setJunctionTree(null);	// disconnect net with junction tree, so that junction tree is not cloned in next method
		// clone the network, but do not clone the junction tree
		ProbabilisticNetwork cloneProbabilisticNetwork = this.cloneProbabilisticNetwork(getNet());
		this.setNetPreviousRun(cloneProbabilisticNetwork);
		getNet().setJunctionTree(junctionTreeNotToIncludeInClone);	// restore junction tree
	}

	/**
	 * @param probabilisticNetwork : the network whose {@link Clique} belongs to.
	 * The {@link Clique} will be extracted from {@link ProbabilisticNetwork#getJunctionTree()}
	 * and then from {@link IJunctionTree#getCliques()}.
	 * @return List of the cliques with largest size in {@link #getJunctionTree()}.
	 * This will return multiple elements if there are cliques with same size.
	 */
	public List<Clique> getLargestCliques(ProbabilisticNetwork probabilisticNetwork) {
		// basic assertions
		if (probabilisticNetwork == null 
				|| probabilisticNetwork.getJunctionTree() == null 
				|| probabilisticNetwork.getJunctionTree().getCliques() == null
				|| probabilisticNetwork.getJunctionTree().getCliques().isEmpty()) {
			return Collections.emptyList();
		}
		
		// the variable to return
		List<Clique> largestCliques = new ArrayList<Clique>();
		
		// keeps track of the size of the largest cliques we know so far
		int largestCliqueSize = -1;
		
		// iterate on cliques to check largest size
		for (Clique currentCliqueInIteration : probabilisticNetwork.getJunctionTree().getCliques()) {
			
			// extract size of the current clique in iteration
			int currentCliqueSize = currentCliqueInIteration.getProbabilityFunction().tableSize();
			
			// compare clique table size
			if (currentCliqueSize >= largestCliqueSize) {
				if (currentCliqueSize > largestCliqueSize) {
					// found clique strictly larger than the largest we know so far, so delete what we know so far
					largestCliques.clear();	
					largestCliqueSize = currentCliqueSize;
				}
				largestCliques.add(currentCliqueInIteration);
			}
		}
		
		return largestCliques;
	}

	/**
	 * If {@link #isToUseAgressiveLoopyBP()} is true, then this method will call {@link #splitCliqueAndAddToJT(Clique, LoopyJunctionTree, int)} in order
	 * to reduce the maximum size of cliques and generate cliques with loops. In the same case, this will also call {@link LoopyJunctionTree#setLoopy(boolean)} to true in order
	 * to explicitly indicate that the clique structures are loopy now.
	 * <br/>
	 * <br/>
	 * If {@link #isToUseAgressiveLoopyBP()} is false, then this method will connect cliques that remained disconnected after {@link #strongTreeMethod(ProbabilisticNetwork, List, IJunctionTree)}
	 * due to presence of loopy cliques caused by arcs not included in {@link #addChordAndEliminateNode(Node, List, ProbabilisticNetwork)} 
	 * @param probabilisticNetwork
	 * @see #triangulate(ProbabilisticNetwork)
	 */
	public void buildLoopyCliques(ProbabilisticNetwork probabilisticNetwork) {
		// extract the junction tree
		LoopyJunctionTree jt = null;
		try {
			jt = (LoopyJunctionTree) probabilisticNetwork.getJunctionTree();
		} catch (ClassCastException e) {
			throw new UnsupportedOperationException("Current version only allows loopy BP in clique/cluster structure if the network is associated with an instance of " + LoopyJunctionTree.class.getName(), e);
		}
		
		if (jt == null) {
			throw new IllegalStateException("No junction tree was compiled prior to this method's invokation. Please provide a network which was compiled already.");
		}
		
		// explicitly indicate that this junction tree is not a tree, and it has loops...
		jt.setLoopy(true);	// this should enable loopy BP
		
		if (isToUseAgressiveLoopyBP()) {
			
			// split largest cliques
			for (Clique largeClique : getLargestCliques(probabilisticNetwork)) {
				this.splitCliqueAndAddToJT(largeClique, jt, getLoopyBPCliqueSizeThreshold());
			}
			
		} else { // search for disconnected cliques and connect them to proper cliques (this may lead to loops in cluster structure);
			// search for empty separators
			List<Separator> emptySeparators = new ArrayList<Separator>(jt.getSeparators().size());
			for (Separator separator : jt.getSeparators()) {
				if (separator.getNodesList().isEmpty()) {
					emptySeparators.add(separator);
				}
			}
			
			// this will be the ID of the next separator to be created by this method. 
			int nextSeparatorId = -(jt.getSeparators().size() + 1); // Each time this value is used, it shall be updated (to avoid duplicate ids)
			
			// 1. Delete empty separators
			for (Separator emptySeparator : emptySeparators) {
				// disconnect relation child->parents
				jt.removeParent(emptySeparator.getClique1(), emptySeparator.getClique2());
				// disconnect relation parent->children
				emptySeparator.getClique1().removeChild(emptySeparator.getClique2());
				// delete the separator from JT and from all its indexes
				jt.removeSeparator(emptySeparator);
			}
			
			// 2. Identify groups of disconnected sub-tree. Make index of joint variables
			List<Entry<Set<INode>,Set<Clique>>> nodesInSubtreeToCliquesInSubreeMap = getDisconnectedSubtreeFromJT(jt);
			
			// 3. For each pair of block (subtree), check if there is intersection. If so, connect a clique in block to a clique in another block. 
			for (int i = 0; i < nodesInSubtreeToCliquesInSubreeMap.size()-1; i++) {
				for (int j = i+1; j < nodesInSubtreeToCliquesInSubreeMap.size(); j++) {
					
					// iterate the data for current pair of subtrees
					Set<INode> nodesInSubTree1 = nodesInSubtreeToCliquesInSubreeMap.get(i).getKey();
					Set<INode> nodesInSubTree2 = nodesInSubtreeToCliquesInSubreeMap.get(j).getKey();
					
					// if there is no intersection, then we can ignore this pair
					if (!Collections.disjoint(nodesInSubTree1, nodesInSubTree2)) {
						Set<Clique> cliquesInSubTree1 = nodesInSubtreeToCliquesInSubreeMap.get(i).getValue();
						Set<Clique> cliquesInSubTree2 = nodesInSubtreeToCliquesInSubreeMap.get(j).getValue();
						// pairwise connect cliques in subtree 1 to subtree 2
						for (Clique parentClique : cliquesInSubTree1) {
							for (Clique childClique : cliquesInSubTree2) {
								// obtain the intersection of these two cliques
								List<Node> intersection = new ArrayList<Node>(parentClique.getNodesList());
								intersection.retainAll(childClique.getNodesList());
								
								// ignore cliques not having any common node
								if (intersection.isEmpty()) {
									continue;	
								}
								
								//don't connect cliques if there is a path between them containing the intersection already;
								if (!jt.getPathContainingNodes(childClique, parentClique, (List)intersection).isEmpty()) {
									continue;
								}
								
								
								// Direction of separator must follow same order of cliques (i.e. alpha order -- the order it appears in the list in JT).
								if (jt.getCliques().indexOf(parentClique) > jt.getCliques().indexOf(childClique)) {
									// just swap child and parent cliques
									Clique varForSwap = childClique;
									childClique = parentClique;
									parentClique = varForSwap;
								}
								
								// create the new separator
								Separator newSeparator = new Separator(parentClique, childClique, false);	// false := don't update list of parents/children of these cliques yet
								newSeparator.setInternalIdentificator(nextSeparatorId--);
								newSeparator.setNodes(intersection);
								
								// we also need to update the separator table, or else algorithms will think the separator is empty
								PotentialTable probabilityTable = newSeparator.getProbabilityFunction();	// initialize table of probabilities
								PotentialTable utilityTable = newSeparator.getUtilityTable();				// also init utility table for backward compatibility
								// include the variables to both tables
								for (Node node : intersection) {
									probabilityTable.addVariable(node);
									utilityTable.addVariable(node);
								}
								jt.initBelief(newSeparator);	// initialize the content of tables
								
								// don't forget to include the new separator to the junction tree object
								jt.addSeparator(newSeparator);
								
								// set parentClique a parent of childClique
								jt.addParent(parentClique, childClique);
								// set childClique a child of parentClique
								parentClique.addChild(childClique);
								
								// associate nodes to new separator if separator is smaller than clique/separator whose node is currently associated
								for (Node node : intersection) {
									if (node instanceof TreeVariable) {
										TreeVariable var = (TreeVariable) node;
										// extract clique or separator associated with this node (i.e. smallest clique/separator containing this node known so far)
										IRandomVariable cliqueOrSeparator = var.getAssociatedClique();
										// check if it is still smaller than the separator we just created
										if (cliqueOrSeparator.getProbabilityFunction() instanceof PotentialTable) {
											PotentialTable table = (PotentialTable) cliqueOrSeparator.getProbabilityFunction();
											if (table.tableSize() > probabilityTable.tableSize()) {
												// the clique/separator associated with this node is larger than new separator,
												// so this node should now be associated to new separator (smaller) so that marginalization gets faster
												var.setAssociatedClique(newSeparator);
											}
										}
									}
								}
								
							}	// end of for each clique
						}	// end of for  each clique
					}	// else subtrees are disjoint
				} // end of for each subtree
			}	// end of for each subtree
			
			// 4. if there are more than 1 root yet, pick 1st root (alpha order) and create empty Separator to another root.
			List<Clique> rootCliques =  getCliquesWithNoParents(jt);
			
			if (rootCliques != null && !rootCliques.isEmpty()) {
				
				Clique parentClique = rootCliques.get(0);	// the 1st clique is supposedly to be the 1st one appearing in jt
				
				for (int i = 1; i < rootCliques.size(); i++) {
					
					Clique childClique = rootCliques.get(i);
					
					// create the new empty separator
					Separator newSeparator = new Separator(parentClique, childClique, false);
					newSeparator.setInternalIdentificator(nextSeparatorId--);
					jt.addSeparator(newSeparator );	// false := don't update list of parents/children of these cliques yet
					
					// set parentClique a parent of childClique by updating the list of parents in childClique
					jt.addParent(parentClique, childClique);
					
					// set childClique a child of parentClique by updating the list of children in parent clique
					parentClique.addChild(childClique);
				}
			}
			
			// make sure the belief tables are updated after cliques are connected
			try {
//				jt.setInitialized(false);	// force it to stop using cache
//				jt.initBeliefs();
				jt.initConsistency();	// instead of reseting all beliefs (which was supposedly done when the jt was compiled), we just need to propagate and set global consistency
				// we also need to initialize the cache of marginals, because they may have been changed
				updateMarginals(probabilisticNetwork);
			} catch (Exception e) {
				throw new RuntimeException(e); // TODO stop using exception translation
			}
		}
		
	}
	
	/**
	 * @param jt : instance of {@link JunctionTree} whose {@link Clique} will be accessed.
	 * @return : cliques whose {@link JunctionTree#getParents(Clique)} is empty.
	 * The order in {@link JunctionTree#getCliques()} will be kept.
	 */
	public List<Clique> getCliquesWithNoParents(JunctionTree jt) {
		
		// basic assertion
		if (jt == null || jt.getCliques() == null) {
			return Collections.EMPTY_LIST;
		}
		
		// prepare the list to be returned
		List<Clique> ret = new ArrayList<Clique>();
		
		// search for cliques which satisfy condition. Keep original order
		for (Clique clique : jt.getCliques()) {
			if (jt.getParents(clique).isEmpty()) {
				ret.add(clique);
			}
		}
		
		return ret;
	}

	/**
	 * @param jt : junction tree to look for cliques. 
	 * @return
	 * a list of pairs representing a disconnected subtree in the junction tree. 
	 * The 1st element in the pair is the set of all nodes contained in the subtree.
	 * The 2nd element in the pair is the set of cliques in the subtree.
	 * A disconnected subtree is a subtree whose root is a clique with null parent.
	 * @see #buildLoopyCliques(ProbabilisticNetwork)
	 */
	private List<Entry<Set<INode>, Set<Clique>>> getDisconnectedSubtreeFromJT( LoopyJunctionTree jt) {
		if (jt == null || jt.getCliques() == null || jt.getCliques().isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		List<Entry<Set<INode>, Set<Clique>>>  ret = new ArrayList<Map.Entry<Set<INode>,Set<Clique>>>(jt.getCliques().size());
		
		for (Clique clique : jt.getCliques()) {
			if (!jt.getParents(clique).isEmpty()) {
				continue;	// only consider cliques with no parent (i.e. root of subtree)
			}
			// found a clique with no parent. This is the root of the subtree.
			// prepare set to be filled with nodes contained in this subtree
			Set<INode> nodesInSubtree = new HashSet<INode>();
			// obtain all cliques that can be reached from this root, and also fill set of nodes contained in subtree
			Set<Clique> cliquesInSubtree = getCliquesInSubtreeRecursively(clique, nodesInSubtree);
			// include the new entry in the list to be returned
			ret.add(new AbstractMap.SimpleEntry<Set<INode>, Set<Clique>>(nodesInSubtree, cliquesInSubtree));
		}
		
		return ret;
	}

	/**
	 * Recursively visits the hierarchy below {@link Clique#getChildren()}.
	 * @param root : root of the subtree to look for children.
	 * @param nodesInSubtree : this is an output argument to be filled with all nodes contained in the subtree.
	 * @return : a set of all cliques found below the provided clique, with the clique inclusively.
	 */
	private Set<Clique> getCliquesInSubtreeRecursively(Clique root, Set<INode> nodesInSubtree) {
		
		Set<Clique> ret = new HashSet<Clique>();	// prepare the set to return
		
		ret.add(root);	// the set to return must include the provided root
		
		// visit the root (i.e. fill the set of nodes in this root)
		if (nodesInSubtree != null) {
			for (Node node : root.getNodesList()) {
				nodesInSubtree.add(node);
			}
		}
		
		// recursively visit children
		for (Clique child : root.getChildren()) {
			// this should also properly fill nodesInSubtree
			ret.addAll(this.getCliquesInSubtreeRecursively(child, nodesInSubtree));
		}
		
		return ret;
	}

	/**
	 * Split a clique (and creates a loop between them) until the size of the clique is smaller than or equals to 
	 * the loopyBPCliqueSizeThreshold.
	 * 
	 * @param originalClique : the clique to be split. Separators will be adjusted.
	 * @param jt : the junction tree used to obtain separators between clique to be separated and other connected cliques
	 * @param desiredCliqueSize : resulting cliques shall have this size
	 * @return the cliques and separators generated by this method
	 */
	protected List<IRandomVariable> splitCliqueAndAddToJT(Clique originalClique, LoopyJunctionTree jt, int desiredCliqueSize) {
		
		// TODO find out a way to split which keeps loopy BP performance/convergence acceptable
		
		// just an initial assertion
		if (originalClique == null
				|| jt == null) {
			return Collections.EMPTY_LIST;
		}
		
		// extract the clique table in advance, because we'll access it frequently
		PotentialTable cliqueTable = originalClique.getProbabilityFunction();
		
		// another condition to do nothing
		if (cliqueTable.tableSize() <= desiredCliqueSize) {
			return Collections.EMPTY_LIST;
		}
		
		
		/*
		 * Example: split {ABCDE} given cluster structure:
		 * 
		 * {XABCD}<ABCD>{ABCDE}<BCDE>{BCDEY}
		 * 
		 * Expected result:
		 * 
		 *          {XABCD}
		 *      <ABC> <CD>   <AB>
		 *   {ABC}<C>{CDE}<E>{EAB}--"<AB>{ABC}"
		 *      <BC>  <CDE>  <EB>
		 *          {BCDEY}
		 */
		

		// extract the object to be used to split the clique (without connecting separators)
		ICliqueSplitter splitter = getCliqueSplitter();
		if (splitter == null) {
			// use a default one if none was specified
			splitter = SINGLE_CYCLE_HALF_SIZE_SEPARATOR_CLIQUE_SPLITTER;
		}
		
		// generate smaller cliques by splitting the clique.
		// do this before making any change in JT, because implementations may need such information.
		List<Clique> generatedCliques = splitter.splitClique(originalClique, jt, desiredCliqueSize);
		
		List<Separator> generatedSeparators = new ArrayList<Separator>();	// this list will be filled with separators generated in this method
		
		// extract parents of original clique
		List<Clique> parentCliques = new ArrayList<Clique>(jt.getParents(originalClique));	// use a clone, to allow modification
		// disconnect clique from parents 
		if (parentCliques != null) {
			for (Clique parentClique : parentCliques) {
				// extract the separator between the cliques
				Separator separator = jt.getSeparator(parentClique, originalClique);
				if (separator == null) {
					throw new IllegalArgumentException(parentClique + " is supposed to be a parent clique of " + originalClique + ", but no separator was found in between.");
				}
				// delete the separator from the jt
				jt.removeSeparator(separator);	// this will not remove the references between cliques, so we need to explicitly remove them
				// delete the reference in the mapping of parents
				jt.removeParent(parentClique, originalClique);
				// also remove the reference to original clique from list of children of parent clique
				parentClique.removeChild(originalClique);
			}
		}
		
		// extract children of original clique
		List<Clique> childCliques = new ArrayList<Clique>(originalClique.getChildren());	// use a clone to avoid concurrent modification
		// disconnect clique from children
		if (childCliques != null) {
			for (Clique childClique : childCliques) {
				// extract the separator between the cliques
				Separator separator = jt.getSeparator(originalClique, childClique);
				if (separator == null) {
					throw new IllegalArgumentException(childClique + " is supposed to be a child clique of " + originalClique + ", but no separator was found in between.");
				}
				// delete the separator from the jt
				jt.removeSeparator(separator);	// this will not remove the references between cliques, so we need to explicitly remove them
				// delete the reference in the mapping of parents
				jt.removeParent(originalClique, childClique);
				// also remove the reference in list of children
				originalClique.removeChild(childClique);
			}
		}
		
		// create a mapping from a node in generatedCliques to clique with smallest size, so that we can change references of nodes to original clique without iterating multiple times
		Map<INode, Clique> nodeToSmallestCliqueMapping = new HashMap<INode, Clique>();
		for (Clique currentClique : generatedCliques) {
			// create a mapping for all nodes used in any clique in generatedCliques
			for (Node node : currentClique.getNodesList()) {
				// check if node is already mapped
				Clique smallestClique = nodeToSmallestCliqueMapping.get(node);
				if (smallestClique == null) {
					// node is not mapped yet, so just create a new mapping 
					// (and current clique is the smallest clique we know so far -- because there is no other known clique with the node yet)
					nodeToSmallestCliqueMapping.put(node, currentClique); 
				} else if (currentClique.getProbabilityFunction().tableSize() < smallestClique.getProbabilityFunction().tableSize()) {
					// check if current clique is smaller than the one in the map. If so, substitute
					nodeToSmallestCliqueMapping.put(node, currentClique);
				}
			}
		}
		
		// use the above mapping in order to make the associated nodes to reference the new cliques instead
		for (Node associatedNode : originalClique.getAssociatedProbabilisticNodesList()) {
			// update the reference from associated node to clique (so that it points to new cliques instead)
			Clique smallestCliqueWithNode = nodeToSmallestCliqueMapping.get(associatedNode);	// use the map to get the smallest clique containing the node
			if (smallestCliqueWithNode != null) {
				((TreeVariable) associatedNode).setAssociatedClique(smallestCliqueWithNode);
				// include the reference from new clique to this node
				smallestCliqueWithNode.getAssociatedProbabilisticNodesList().add(associatedNode);
			} else {
				throw new RuntimeException("Cliques " + generatedCliques + " generated from " + originalClique + " did not contain node " + associatedNode);
			}
		}
		
		// remove original clique from junction tree
		jt.removeCliques(Collections.singletonList(originalClique));
		
		// include new cliques in junction tree in advance (we'll generate separators later)
		jt.getCliques().addAll(generatedCliques);
		
		// fully (pairwise) connect generated cliques with plausible separators.
		// Use same ordering in the list as the parent/child order of the cliques
		// (i.e. if a clique appear first in list, then it is an ancestor of cliques that appear after it in the list).
		for (int i = 0; i < generatedCliques.size()-1; i++) {
			for (int j = i+1; j < generatedCliques.size(); j++) {
				Separator separator = this.connectCliquesAndAddSeparator(generatedCliques.get(i), generatedCliques.get(j), jt);
				if (separator != null) {
					generatedSeparators.add(separator);
				} else {
					throw new RuntimeException("Unable to generate separator between " + generatedCliques.get(i) + " and " + generatedCliques.get(j));
				}
			}
		}
		
		// connect generated cliques with original parents;
		for (Clique parent : parentCliques) {
			for (Clique child : generatedCliques) {
				Separator separator = this.connectCliquesAndAddSeparator(parent, child, jt);
				if (separator != null) {
					generatedSeparators.add(separator);
				} else {
					throw new RuntimeException("Unable to generate separator between " + parent + " and " + child);
				}
			}
		}
		
		// connect generated cliques with original children;
		for (Clique child : childCliques) {
			for (Clique parent : generatedCliques) {
				Separator separator = this.connectCliquesAndAddSeparator(parent, child, jt);
				if (separator != null) {
					generatedSeparators.add(separator);
				} else {
					throw new RuntimeException("Unable to generate separator between " + parent + " and " + child);
				}
			}
		}
		
		// update internal ids, especially because we created new separators/cliques.
		this.updateCliqueAndSeparatorInternalIdentificators(jt);
		
		// return cliques and separators generated in this method
		List<IRandomVariable> ret = new ArrayList<IRandomVariable>(generatedCliques.size() + generatedSeparators.size());
		ret.addAll(generatedCliques);
		ret.addAll(generatedSeparators);
		return ret;
	}

	/**
	 * This will update the references to parent/child cliques
	 * by updating {@link LoopyJunctionTree#getParents(Clique)} and {@link Clique#getChildren()},
	 * and it will also generate a new {@link Separator} between the provided cliques and include
	 * it in the junction tree.
	 * @param parent : one of the cliques to be connected. This will be come the parent clique in hierarchy
	 * @param child : the other clique to be connected. This will become the child clique.
	 * @param jt : the junction tree where the new separator will be included.
	 * @return the separator generated by this method
	 */
	protected Separator connectCliquesAndAddSeparator(Clique parent, Clique child, LoopyJunctionTree jt) {
		// basic assertions
		if (parent == null || child == null || jt == null) {
			return null;	// there is nothing to do
		}
		
		// keep track of variables in parent that are not in child
		List<INode> differentVars = new ArrayList<INode>(parent.getNodesList());	// use a clone, because we don't want to modify original list
		differentVars.removeAll(child.getNodesList());
		// also extract common variables;
		List<INode> commonVars = new ArrayList<INode>(parent.getNodesList());	// use a clone, because we don't want to modify original list
		commonVars.removeAll(differentVars);
		
		
		// create a separator with the common variables;
		Separator sep = new Separator(parent, child, null, null, false);
		sep.getNodes().addAll((List)commonVars);	// the order of variables is supposedly the same of parent clique
		sep.setInternalIdentificator(-jt.getSeparators().size()-1);
		
		// sum out variables from parent clique table
		PotentialTable cliqueTable = (PotentialTable) parent.getProbabilityFunction().getTemporaryClone();	// use clone so that we don't affect original table
		for (INode varToSumOut : differentVars) {
			// this shall sum out the variable
			cliqueTable.removeVariable(varToSumOut);
		}
		
		// update the separator table by reading from parent's clique table;
		PotentialTable separatorTable = sep.getProbabilityFunction();
		for (INode var : cliqueTable.variableList) {	
			// keep same ordering of variables
			separatorTable.addVariable(var);
		}
		// copy the content of the table (after sum out) too
		System.arraycopy(cliqueTable.getValues(), 0, separatorTable.getValues(), 0, separatorTable.tableSize());
		
		// include separator in JT (this will not update references between cliques yet);
		jt.addSeparator(sep);
		
		// update reference from child to parent;
		jt.addParent(parent, child);
		
		// update reference from parent to child;
		parent.addChild(child);
		
		return sep;
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
		
		// basic assertion: there should be no cycles in net
		if (newNet.hasCycle()) {
			throw new IllegalStateException("Cannot compile a network with cycles.");
		}
		
		// store what nodes were added/deleted to/from the network
		Collection<INode> nodesToDelete = new HashSet<INode>();		// this will *not* contain nodes that were already removed from junction tree
		Collection<INode> allDeletedNodes = new HashSet<INode>();	// this will also contain nodes that were already removed from junction tree
		Collection<INode> nodesToAdd = new HashSet<INode>();		// this will *not* contain nodes that were already included in junction tree
		// store what arcs were added/deleted
		Collection<Edge> edgesToDelete = new HashSet<Edge>();
		Collection<Edge> edgesToAdd = new HashSet<Edge>();			// this will *not* contain edges that overwrites a moralization arc previously present
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
					// cannot find node in new net, so it was deleted.
					allDeletedNodes.add(oldNode);
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
					// do not consider arcs to/from nodes that were already deleted from junction tree, because they cannot be handled by this algorithm anyway;
					if ( ( allDeletedNodes.contains(oldEdge.getOriginNode()) && !nodesToDelete.contains(oldEdge.getOriginNode()) )
							|| ( allDeletedNodes.contains(oldEdge.getDestinationNode()) && !nodesToDelete.contains(oldEdge.getDestinationNode()) )) {
						// the node was already deleted from junction tree if it is in the set of deleted nodes, but it is not marked for deletion now
						continue; // this case usually happens when multiple objects accesses the junction tree and makes modifications;
					}
					
					// the edges specified here must use node instances in new net, so search newNet for names of nodes in oldEdge and create a new edge
					edgesToDelete.add(new Edge(newNet.getNode(oldEdge.getOriginNode().getName()), newNet.getNode(oldEdge.getDestinationNode().getName())));
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
//		if (!edgesToDelete.isEmpty()) { // I'm assuming that if we never deleted any edge, there is no old moral arc deleted too
//			edgesToDelete.addAll(getDeletedMoralArcs(oldNet, newNet, edgesToDelete));
//		}
		// no need for the above code, because Separator#isComplete(Collection) is considering moralized arcs
		
		// This algorithm also needs to track which moral connections (i.e. implicit connections between parents with common child) were created because of new arcs.
		if (!allConsideredEdges.isEmpty()) { // I'm assuming that if we never included any edge, there is no new moral arc too
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
		// and this is the inverse mapping of clusterToOriginalCliqueMap, because we'll need it for some methods
		Map<Clique, Clique> originalCliqueToClusterMap = new HashMap<Clique, Clique>();
		try {
			// the junction tree here needs to be the old junction tree, prior to modifications.
			decompositionTree = getMaximumPrimeSubgraphDecompositionTree(junctionTree, clusterToOriginalCliqueMap, originalCliqueToClusterMap); // this should also fill the maps
		} catch (Exception e) {
			throw new RuntimeException("Unable to obtain maximum prime subgraph decomposition tree from the current junction tree.",e);
		}
		
//		// invert the clusterToOriginalCliqueMap and write it to originalCliqueToClusterMap
//		for (Entry<Clique, Collection<Clique>> clusterToOriginalCliques : clusterToOriginalCliqueMap.entrySet()) {
//			for (Clique originalClique : clusterToOriginalCliques.getValue()) {
//				originalCliqueToClusterMap.put(originalClique, clusterToOriginalCliques.getKey());
//			}
//		}
		
		// check if we satisfy some conditions to compile the network without using dynamic/incremental compilation in order to return to non-approximate structure
		if (isToCompileNormallyWhenLoopyAndDeletingArcs() && junctionTree.isUsingApproximation()) { // junctionTree.isUsingApproximation() should be more generic than LoopyJunctionTree#isLoopy()
			if (nodesToAdd.isEmpty()
					&& nodesToDelete.isEmpty()
					&& edgesToAdd.isEmpty()
					&& !edgesToDelete.isEmpty()) {
				// if the only changes are arc removal, then compile normally
				super.run();
				return;
			}
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
			clustersToModify.addAll(this.treatRemoveEdge(deletedEdge, junctionTree, decompositionTree, originalCliqueToClusterMap));
		}
		
		// if all clusters are marked for modification, simply run ordinary junction tree compilation
		if (clustersToModify.size() == decompositionTree.getCliques().size()) {
			super.run();
			return;
		}
		
		// We don't need the list of modification in net structure anymore. 
		// Release them, because we'll have a memory-intense operation (compilation of junction trees of max prime subgraphs) now
//		nodesToAdd.clear(); 
		nodesToAdd = null;
//		allDeletedNodes.clear(); 
		allDeletedNodes = null;
//		nodesToDelete.clear() ; 
		nodesToDelete  = null;
//		edgesToAdd.clear(); 
		edgesToAdd = null;
//		edgesToDelete.clear() ; 
		edgesToDelete  = null;
//		allConsideredEdges.clear(); 
		allConsideredEdges = null;
		
		
		// for each connected marked clusters, retrieve the nodes in prime subnet decomposition and compile junction tree for each of subnets
		Map<IJunctionTree, Collection<Clique>> compiledSubnetToClusterMap = this.getCompiledPrimeDecompositionSubnets(clustersToModify, decompositionTree, originalCliqueToClusterMap);

		// this shall change the ids of the separators in the newly generated junction trees, so that the ids are globally unique (this shall improve code safety)
		this.setUniqueIDsToCliquesAndSeparators(compiledSubnetToClusterMap, junctionTree);
		
		// aggregate the junction tree of the subnets to the original junction tree (removing unnecessary cliques)
		for (Entry<IJunctionTree, Collection<Clique>> entry : compiledSubnetToClusterMap.entrySet()) {
			this.aggregateJunctionTree(junctionTree, entry.getKey(), entry.getValue(), 
//					decompositionTree, 
					clusterToOriginalCliqueMap, originalCliqueToClusterMap);
		}
		
		this.updateCliqueAndSeparatorInternalIdentificators(junctionTree);
		
		// needs to make clique/separator potentials globally consistent again.
		try {
//			this.updateNodeCliqueAssociation(newNet, junctionTree);
			this.associateCliques(newNet, junctionTree);
			if (junctionTree instanceof JunctionTree) {
				((JunctionTree) junctionTree).setInitialized(false);
			}
			junctionTree.initBeliefs();
		} catch (Exception e) {
			throw new RuntimeException("Unable to initialize beliefs after aggregating local junction trees.", e);
		}
		
		
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
	 * This method iterates on all junction trees in the parameter compiledSubnetToClusterMap in order to change
	 * the {@link Separator#getInternalIdentificator()} and {@link Clique#getInternalIdentificator()}, so that they become globally unique
	 * @param compiledSubnetToClusterMap : cliques and separators in these junction trees will be treated.
	 * @param junctionTree : this is the junction tree where the junction trees in compiledSubnetToClusterMap will be
	 * aggregated to by the method {@link #aggregateJunctionTree(IJunctionTree, IJunctionTree, Collection, Map, Map)}.
	 * This will be referenced because the new IDs shall not conflict with the IDs in this junction tree.
	 */
	private void setUniqueIDsToCliquesAndSeparators( Map<IJunctionTree, Collection<Clique>> compiledSubnetToClusterMap, IJunctionTree junctionTree) {
		
		// get a reasonable estimate for the 1st globally exclusive id for cliques and separators
		int globalCliqueId = 0;
		int globalSeparatorId = -1;
		if (junctionTree != null && junctionTree.getCliques() != null) {
			globalCliqueId = junctionTree.getCliques().size();
			globalSeparatorId = -(junctionTree.getSeparators().size() + 1);
		}
		
		// iterate on all junction trees
		if (compiledSubnetToClusterMap != null) {
			for (IJunctionTree newJunctionTree : compiledSubnetToClusterMap.keySet()) {
				if (newJunctionTree != null) {
					// iterate on all cliques
					if (newJunctionTree.getCliques() != null) {
						for (Clique clique : newJunctionTree.getCliques()) {
							// change the id to a global id
							clique.setInternalIdentificator(globalCliqueId++);	// the id of the next clique will be "current id + 1"
						}
					}
					
					// similarly, iterate on all separators
					if (newJunctionTree.getSeparators() != null) {
						for (Separator separator : newJunctionTree.getSeparators()) {
							// change the id to a global id
							separator.setInternalIdentificator(globalSeparatorId--);	// the id of the next separator will be "current id - 1"
						}
					}
				}
			}
		}
	}

//	/**
//	 * This method will clean cliques from nodes belonging to {@link Clique#getAssociatedProbabilisticNodesList()}
//	 * of multiple cliques (because two cliques must not have intersection of {@link Clique#getAssociatedProbabilisticNodesList()}).
//	 * @param net : the network where the nodes will be extracted from
//	 * @param junctionTree : junction tree from where the cliques will be extracted from
//	 */
//    protected void updateNodeCliqueAssociation(ProbabilisticNetwork net, IJunctionTree junctionTree) {
//    	// TODO call JunctionTreeAlgorithm#associateCliques(net, junctionTree) instead
//    	
//    	// keep track of what is the smallest clique containing the node known so far
//		Map<INode, Clique> nodeToSmallestAssociatedCliqueMap = new HashMap<INode, Clique>();
//		
//		// iterate on cliques to check for redundancy
//		for (Clique currentClique : junctionTree.getCliques()) {
//			// iterate on for associated nodes
//			for (Node associatedNode : new ArrayList<Node>(currentClique.getAssociatedProbabilisticNodesList())) {	// iterate on a clone, because we'll modify the original
//				
//				// if clique does not contain all the parents of this node, then immediately remove it, because it's inconsistent
//				if (!currentClique.getNodesList().containsAll(associatedNode.getParentNodes())) {
//					currentClique.getAssociatedProbabilisticNodesList().remove(associatedNode);
//					continue;	// no need to handle this node anymore, because it is like it did not exist from the beginning
//				}
//				
//				// check if there is a mapping already. If so, current node was present in some previous clique, so we need to remove from one of them
//				Clique cliqueInMap = nodeToSmallestAssociatedCliqueMap.get(associatedNode);
//				if (cliqueInMap != null) { // current node was found in more than 1 clique.
//					// check which clique is smaller, and remove node from larger
//					if (currentClique.getProbabilityFunction().tableSize() < cliqueInMap.getProbabilityFunction().tableSize()) {
//						// remove node from mapped clique
//						cliqueInMap.getAssociatedProbabilisticNodesList().remove(associatedNode);
//						// update mapping, because current clique is the smallest clique (containing the node) we know so far
//						nodeToSmallestAssociatedCliqueMap.put(associatedNode, currentClique);
//					} else {
//						// remove node from current clique, because the clique we found previously is smaller than current one
//						currentClique.getAssociatedProbabilisticNodesList().remove(associatedNode);
//						// and keep the map untouched, because the map still has the smallest clique associated with the node
//					}
//				} else {
//					// simply add new mapping
//					nodeToSmallestAssociatedCliqueMap.put(associatedNode, currentClique);
//				}
//			}
//		}
//	}
    

	/**
     * Recursively collects nodes of current cluster and connected clusters marked in clustersToCompile.
     * Connected clusters are clusters related with {@link Clique#getParent()} and {@link Clique#getChildren()}.
     * @param cluster : current cluster in recursive call
     * @param clustersToCompile : if cluster is not in this set, stop recursive calls.
     * @param processedClusters : stores which clusters were processed already, so that we don't process the same cluster twice.
     * @param decompositionTree  : the max prime subgraph decomposition tree where the clusters belong
     * @return : set of all nodes in current cluster and all connected clusters;
     * @see #getCompiledPrimeDecompositionSubnets(Collection)
     * @see #runDynamicJunctionTreeCompilation()
     */
	private Set<INode> getNodesInConnectedClustersRecursively(Clique cluster, Collection<Clique> clustersToCompile, Set<Clique> processedClusters, JunctionTree decompositionTree) {
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
		for (Clique parent : decompositionTree.getParents(cluster)) {
			ret.addAll(this.getNodesInConnectedClustersRecursively(parent, clustersToCompile, processedClusters, decompositionTree));
		}
		
		// call recursive for child clusters
		for (Clique childCluster : cluster.getChildren()) {
			ret.addAll(this.getNodesInConnectedClustersRecursively(childCluster, clustersToCompile, processedClusters, decompositionTree));
		}
		
		return ret;
	}


	
	/**
	 * For nodes in each connected clusters marked for modification, compile a junction tree.
     * @param clustersToCompile : these are the clusters in prime subgraph decomposition tree to be used
	 * @param decompositionTree : the max prime subgraph decomposition tree where the clusters belong
	 * @param originalCliqueToClusterMap : a mapping from cliques in original junction tree to clusters in decompositonTree.
	 * This will be used to check whether {@link TreeVariable#getAssociatedClique()} is a clique in clustersToCompile
	 * (by doing this, we can check whether {@link TreeVariable#getAssociatedClique()} shall be re-mapped to new junction cliques generated by this method).
     * @return map from the generated junction trees to clusters that has generated the respective junction tree. 
     * By extracting the keys, you can obtain the set of junction trees generated from the argument.
     * By obtaining the values, you can obtain which cluster originated the junction tree
     * @throws InvalidParentException  when failing to clone arcs while generating a clone of the subnetwork.
     * @see #runDynamicJunctionTreeCompilation()
     * @see IJunctionTree#initBeliefs()
     * @see #getNet()
     */
    protected Map<IJunctionTree,Collection<Clique>> getCompiledPrimeDecompositionSubnets( Collection<Clique> clustersToCompile, IJunctionTree decompositionTree, Map<Clique, Clique> originalCliqueToClusterMap) throws InvalidParentException {
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
				Set<INode> originalNodes = getNodesInConnectedClustersRecursively(cluster, clustersToCompile, processedClusters, (JunctionTree) decompositionTree);	// this will also update processedClusters
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
				for (INode originalParent : originalNodes) {
					// extract the object in the subnet. They have the same name, but are different instances (because nodes in subnet are clones)
					Node parentInSubnet = subnet.getNode(originalParent.getName());
					// add edges that will complete the parents.
					for (INode originalChild : originalParent.getChildNodes()) {
						// check if child is in subnet
						Node childInSubnet = subnet.getNode(originalChild.getName());
						if (childInSubnet != null) {
							// both parent and child exist in subnet, so add arc
							Edge edge = new Edge(parentInSubnet, childInSubnet);
							subnet.addEdge(edge);	// this will also automatically update cpts, so we need to overwrite cpts later
						}
					}
				}
				
				// now that all arcs were created, process moralization arcs that needs to be forced 
				// (because some children may exist in original net but not in new subnet, but we want to keep the moralization link intact)
				
				// store connections between parents that are present in original net by moralization (needs to add explicitly, because new subnet may not contain the children).
				Map<Node, Set<Node>> nodesToForceMoralization = new HashMap<Node, Set<Node>>();	// this is to avoid double insertion
				
				// iterate on all children of nodes that are in subnet
				for (INode originalNodeAlsoInSubnet : originalNodes) {
					// check for children, because we need to moralize nodes in new subnet if they share same child in original net
					for (INode originalChild : originalNodeAlsoInSubnet.getChildNodes()) {
						// check if child is in subnet
						Node childInSubnet = subnet.getNode(originalChild.getName());
						if (childInSubnet == null) {
							// process arcs that must be explicitly added due to moralization in original network (which may not be present in new subnet, due to absense of some children)
							int numParents = originalChild.getParentNodes().size();
							// pair-wise process parents
							for (int i = 0; i < numParents-1; i++) {
								// extract one of the nodes
								Node parentToConnect1 = subnet.getNode(originalChild.getParentNodes().get(i).getName());
								// ignore nodes that are not present in new subnet
								if (parentToConnect1 == null) {
									continue;	
								}
								for (int j = i+1; j < numParents; j++) {
									// extract the other node
									Node parentToConnect2 = subnet.getNode(originalChild.getParentNodes().get(j).getName());
									// ignore nodes that are not present in new subnet
									if (parentToConnect2 == null) {
										continue;	
									}
									// never connect node to itself. This is unlikely to happen, though
									if (parentToConnect1.equals(parentToConnect2)) { // TODO check that it is safe to remove this condition
										continue; 
									}
									// ignore pairs that are connected already
									if (parentToConnect2.isChildOf(parentToConnect1)
											|| parentToConnect1.isChildOf(parentToConnect2)) {
										continue;
									}
									// avoid processing the same pair twice
									if ( ( nodesToForceMoralization.containsKey(parentToConnect1) && nodesToForceMoralization.get(parentToConnect1).contains(parentToConnect2) )
											|| ( nodesToForceMoralization.containsKey(parentToConnect2) && nodesToForceMoralization.get(parentToConnect2).contains(parentToConnect1) ) ) {
										continue;	
									}
									// at this point, we can add the new pair, because it is not in the mapping
									Set<Node> setInMapping = nodesToForceMoralization.get(parentToConnect1);
									if (setInMapping == null) {
										// this is a completely new entry in the mapping (i.e. parentToConnect1 is connected to no other node by moralization)
										setInMapping = new HashSet<Node>();	// so, update map
										nodesToForceMoralization.put(parentToConnect1, setInMapping);
									}
									// update the value in the map
									setInMapping.add(parentToConnect2);
								}
							}
						}
					}
				
				}
				
				// now, convert the map (of moralization links) to actual arcs. 
				if (!nodesToForceMoralization.isEmpty()) {
					Collection<Edge> moralizationArcs = new ArrayList<Edge>(nodesToForceMoralization.size());
					for (Entry<Node, Set<Node>> entry : nodesToForceMoralization.entrySet()) {
						for (Node node : entry.getValue()) {
							// TODO check if we need to set the edge as undirected
							moralizationArcs.add(new Edge(entry.getKey(), node));
						}
					}
					// this will make sure moralization arcs are kept when compiling subnet
					subnet.setMarkovArcsToBeForced(moralizationArcs);
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
				IncrementalJunctionTreeAlgorithm algorithm = new IncrementalJunctionTreeAlgorithm(subnet);	// but do not use dynamic junction tree compilation for the subnets
				// Make sure ordinal junction tree compilation is used (i.e. we don't call dynamic compilation again)
				algorithm.setDynamicJunctionTreeNetSizeThreshold(Integer.MAX_VALUE);	  // this shall guarantee that dynamic compilation is disabled
				subnet.setJunctionTree(null);											  // this will also guarantee dynamic compilation to be disabled
				algorithm.setJunctionTreeBuilder(getJunctionTreeBuilder());				  // reuse the same builder of junction tree.
				algorithm.setLoopyBPCliqueSizeThreshold(getLoopyBPCliqueSizeThreshold()); // reuse same configuration for loopy BP
//				algorithm.setToInitBeliefs(false);										  // disable default belief initialization (which multiplies CPT and runs belief propagation once)
				// finally, compile the subnet
				algorithm.run();
//				algorithm.initBeliefsWithoutPropagation();								  // simply initialize content of cliques and separators by using null values and product of CPT
				// assert that junction tree was compiled
				if (algorithm.getJunctionTree() == null || algorithm.getJunctionTree() != subnet.getJunctionTree()) {
					throw new RuntimeException("Unable to compile max prime subnet of cluster " + cluster);
				}
				
//				if (algorithm.getJunctionTree() instanceof LoopyJunctionTree) {
//					((LoopyJunctionTree)algorithm.getJunctionTree()).initParentMapping();
//				}
				
				// extract the network managed by this algorithm (i.e. this is the original network, and it will be used to replace nodes in cloned subnets with nodes in original net)
				ProbabilisticNetwork net = this.getNet();
				
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
						Node originalNode = net.getNode(cliqueNodes.get(i).getName());
						// however, don't include nodes whose parents are not totally included in this clique
//						if (clique.getNodesList().containsAll(originalNode.getParentNodes())) {
							cliqueNodes.set(i, originalNode );
//						} else {
//							Debug.println(getClass(), "Parents of node " + originalNode + " are " + originalNode.getParentNodes() +  ", and they are not totally included in clique " + clique);
//							cliqueNodes.remove(i);
//							i--;
//						}
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
					if ((originalNode instanceof TreeVariable)) {
						// Extract node in new subnet. I did not remove new nodes from subnet, so I can query subnet in order to get the respective new node
						TreeVariable newNode = (TreeVariable) subnet.getNode(originalNode.getName());
						
						// check if they were associated to cliques that will be substituted by the cliques generated by this method;
//						IRandomVariable originalClique = ((TreeVariable) originalNode).getAssociatedClique();
//						Clique associatedCluster = originalCliqueToClusterMap.get(originalClique);
//						if (clustersToCompile.contains(associatedCluster)  // these cliques will be substituted, then immediately include
//								|| ( ((PotentialTable)newNode.getAssociatedClique().getProbabilityFunction()).tableSize() 
//										< ((PotentialTable)originalClique.getProbabilityFunction()).tableSize() ) ) {	 // the cliques aren't to be substituted, but if new ones are smaller, then associate to smaller clique
							// subnet was compiled, so nodes in it are supposedly associated with some clique in new junction subtree.
							// use new node (which is associated to new cliques) to associate original node to new clique.
							((TreeVariable) originalNode).setAssociatedClique(newNode.getAssociatedClique());
//						}
					}
				}
				
				// activate loopy BP if clique size got too large
//				List<Clique> largestCliques = getLargestCliques(algorithm.getNet());
//				if (largestCliques != null && 
//						!largestCliques.isEmpty() 
//						&& ( largestCliques.get(0).getProbabilityFunction().tableSize() > getLoopyBPCliqueSizeThreshold() ) ) {
//					this.buildLoopyCliques(algorithm.getNet());
//					// also make sure the main junction tree is notified about presence of loops
//					try {
//						((LoopyJunctionTree)this.getJunctionTree()).setLoopy(true);
//					} catch (ClassCastException e) {
//						throw new UnsupportedOperationException("Current version only allows loopy BP in clique/cluster structure if the network is associated with an instance of " + LoopyJunctionTree.class.getName(), e);
//					}
//				}
				
				
				// add junction tree to the map to be returned.
				// Current cluster and all related clusters are supposedly in clustersProcessedInThisIteration
				ret.put(algorithm.getJunctionTree(), clustersProcessedInThisIteration);
			}
		}
    			
		return ret;
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
     * @param originalCliqueToClusterMap : this is just an inverse mapping of clusterToOriginalCliqueMap.
     * Set this to null in order to automatically build it from clusterToOriginalCliqueMap.
     * Set this to non-null in order to reuse an existing map, or to personalize the access from clique to cluster (by making the relationship asymmetric).
     * @see #runDynamicJunctionTreeCompilation()
     */
    protected void aggregateJunctionTree(IJunctionTree originalJunctionTree, IJunctionTree primeSubgraphJunctionTree, Collection<Clique> modifiedClusters,	// main parameters
			Map<Clique, Collection<Clique>> clusterToOriginalCliqueMap, Map<Clique, Clique> originalCliqueToClusterMap) {									// auxiliary parameters for reference

    	// basic assertions
    	if (originalJunctionTree == null || primeSubgraphJunctionTree == null || modifiedClusters == null || modifiedClusters.isEmpty()) {
    		return;	// can't do anything without the main arguments
    	}
    	
		// also build an inverse mapping from original clique to generated clusters in max prime subgraph decomposition tree if nothing was specified, 
    	// because we'll use it later to check if a clique was marked for modification
    	if (originalCliqueToClusterMap == null || originalCliqueToClusterMap.isEmpty()) {
    		originalCliqueToClusterMap = new HashMap<Clique, Clique>();
    		// invert the clusterToOriginalCliqueMap and write it to originalCliqueToClusterMap
    		for (Entry<Clique, Collection<Clique>> clusterToOriginalCliques : clusterToOriginalCliqueMap.entrySet()) {
    			for (Clique originalClique : clusterToOriginalCliques.getValue()) {
    				originalCliqueToClusterMap.put(originalClique, clusterToOriginalCliques.getKey());
    			}
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
    		for (Clique parent : ((LoopyJunctionTree)originalJunctionTree).getParents(modifiedClique)) {
    			if (parent != null
    					&& !modifiedClusters.contains(originalCliqueToClusterMap.get(parent))) {
    				// cluster related to this parent clique was not modified, so this is a separator between modified and not modified cliques
    				borderSeparators.add(originalJunctionTree.getSeparator(parent, modifiedClique));
    			}
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
    	
    	// prepare an id that will be used for separators substituting border separators
    	int borderSeparatorSubstitutorId = Integer.MIN_VALUE;
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
				if (borderSeparator.getNodesList().isEmpty()) {
					// Just get an arbitrary clique. Don't pass empty argument, because empty argument will simply return empty collection.
					newCliqueInMaxPrimeJunctionTree = primeSubgraphJunctionTree.getCliquesContainingMostOfNodes((Collection)unchangedOriginalClique.getNodesList()).get(0);
				} else {
					// Try to find clique to maximize intersection with separator.
					newCliqueInMaxPrimeJunctionTree = primeSubgraphJunctionTree.getCliquesContainingMostOfNodes((Collection)borderSeparator.getNodesList()).get(0);
				}
			} catch (Exception e) {
				throw new RuntimeException("Unable to find clique in max prime subgraph decomposition junction tree containing at least one node in " + unchangedOriginalClique);
			}
			
			// at this point, the new clique (with maximum intersection) shall not be null
			
			
			// if the two cliques are already connected, ignore the separator (delete it).
			// (this can happen when there are loops in the clique structure, and the modified clique has a clique with nodes previously belonging to more than 1 clique)
			if (originalJunctionTree.isUsingApproximation()) {
				Separator existingSeparator = originalJunctionTree.getSeparator(unchangedOriginalClique, newCliqueInMaxPrimeJunctionTree);
				if (existingSeparator != null 
							&& (existingSeparator.getClique1() == newCliqueInMaxPrimeJunctionTree || existingSeparator.getClique2() == newCliqueInMaxPrimeJunctionTree)	// use strict comparison, because equals uses the internal identificators, which may be overlapping
							&& (existingSeparator.getClique1() == unchangedOriginalClique || existingSeparator.getClique2() == unchangedOriginalClique)	// use strict comparison, because equals uses the internal identificators, which may be overlapping
						) { // if there is a separator between these cliques already...
					Debug.println(getClass(), "Separator " + borderSeparator + " will become redundant after incremental compilation (because of separator " + existingSeparator + "), thus it will not be considered.");
					// if any node is pointing to the old separator (with TreeVariable#getAssociatedClique()), then point to the new separator instead
					for (Node node : borderSeparator.getNodes()) {
						if ((node instanceof TreeVariable)
								&& ((TreeVariable)node).getAssociatedClique() == borderSeparator) {	// use exact instance equality, instead of Object#equal
							((TreeVariable)node).setAssociatedClique(existingSeparator);
						}
					}
					
					// disconnect the old child from parent before deleting the redundant separator
					if (borderSeparator.getClique1().getChildren().contains(borderSeparator.getClique2())) {
						borderSeparator.getClique1().removeChild(borderSeparator.getClique2());
						((LoopyJunctionTree)originalJunctionTree).removeParent(borderSeparator.getClique1(), borderSeparator.getClique2());
					} else {
						borderSeparator.getClique2().removeChild(borderSeparator.getClique1());
						((LoopyJunctionTree)originalJunctionTree).removeParent(borderSeparator.getClique2(), borderSeparator.getClique1());
					}
					
					// delete the redundant separator
					originalJunctionTree.removeSeparator(borderSeparator);
					
					continue;
				}
			}

			
			/*
			 * In terms of whether it was modified or not, the original junction tree looks like the following:
			 * 
			 * UnchangedClique --BorderSeparator--> {subtree of modified cliques} --{set of border separators}--> {set of other unchanged cliques}.
			 * 
			 * 		If the clique structures are loopy, then this may look like the following
			 * 			{set of UnchangedClique}--{set of border separators}--> {subtree of modified cliques} --{set of border separators}--> {set of other unchanged cliques}.
			 * 
			 * In this model, UnchangedClique is (or {set of UnchangedClique} are) an ancestor clique (parent, or parent of parent, or so on) of all cliques in {subtree of modified cliques} 
			 * and {set of other unchanged cliques}.
			 * The cliques in {subtree of modified cliques} will be substituted with the new junction tree compiled from the max prime subgraph.
			 * 
			 * Therefore, given a single border separator, there are only 2 possible scenarios.
			 * 
			 * 1 - The unchanged clique is a parent of the modified subtree.
			 * 	   In this case, a new clique (of the junction tree compiled from max prime subgraph) will become a child of the unchanged clique.
			 *     This is the case when we need to move the new clique to the root of the junction tree of the max prime subgraph,
			 *     in order to keep the hierarchy consistent (e.g. keep unique root).
			 * 
			 * 2 - The unchanged clique is a child of the modified subtree. This happens to all the other border separators.
			 *     In this case, a new clique (of the junction tree compiled from max prime subgraph) will become a parent of the unchanged clique.
			 */
			
			// Connect the new clique to the original unmodified clique, by the border separator; 
			List<Clique> parentsOfModifiedOriginalClique = ((LoopyJunctionTree)originalJunctionTree).getParents(modifiedOriginalClique);
			if (parentsOfModifiedOriginalClique != null && parentsOfModifiedOriginalClique.contains(unchangedOriginalClique)) {	
				// case 1 - new clique becomes a child of original unchanged clique
				
				// move the new clique to root of its junction tree, so that it gets easier to connect to original junction tree just by adding it to list of children
				((JunctionTree)primeSubgraphJunctionTree).moveCliqueToRoot(newCliqueInMaxPrimeJunctionTree);
				if (newCliqueInMaxPrimeJunctionTree.getParent() != null) {
					throw new RuntimeException("Unable to move clique " + newCliqueInMaxPrimeJunctionTree 
							+ " to root of junction tree fragment generated from max prime subgraph decomposition.");
				}
				
				// if new clique and border separator are the same (i.e. represents same joint space), then join original (unchanged) clique and new clique;
				if (borderSeparator.getNodes().size() == newCliqueInMaxPrimeJunctionTree.getNodesList().size()
						&& borderSeparator.getNodes().containsAll(newCliqueInMaxPrimeJunctionTree.getNodesList())) {
					// if they have the same size, and one is included in other, then they are the same
					
					// join cliques. The unchanged original clique will remain, and the new clique (the one obtained from max prime subtree) will be deleted
					unchangedOriginalClique.join(newCliqueInMaxPrimeJunctionTree);
					
					// move the references of associated nodes of newCliqueInMaxPrimeJunctionTree to the joined clique
					for (Node node : newCliqueInMaxPrimeJunctionTree.getAssociatedProbabilisticNodesList()) {
						if ((node instanceof TreeVariable)
								&& ((TreeVariable) node).getAssociatedClique().equals(newCliqueInMaxPrimeJunctionTree)) {
							((TreeVariable) node).setAssociatedClique(unchangedOriginalClique);
						}
					}
					
					// set the joined clique as parent of all children of the clique to delete
					for (Clique childClique : new ArrayList<Clique>(newCliqueInMaxPrimeJunctionTree.getChildren())) { // using a clone, because original list will be altered in the loop
						
						// inherit children of the clique that is going to be "deleted" (actually, it simply won't be included to original junction tree)
						unchangedOriginalClique.addChild(childClique);
						if (originalJunctionTree instanceof LoopyJunctionTree) {
							((LoopyJunctionTree)originalJunctionTree).addParent(unchangedOriginalClique, childClique);
						}
						if (primeSubgraphJunctionTree instanceof LoopyJunctionTree) {
							((LoopyJunctionTree) primeSubgraphJunctionTree).removeParent(newCliqueInMaxPrimeJunctionTree, childClique);
						} // else child clique was modified anyway
						// mark joined clique as the main parent node. This works also when JT is not loopy
						childClique.setParent(unchangedOriginalClique);
						
						// also create separator with this child clique, 
						
						// we need to substitute the separator, so it links to the new joined clique (unchangedOriginalClique), instead of childClique
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

						// if any node is pointing to the old separator (with TreeVariable#getAssociatedClique()), then point to the new separator instead
						for (Node node : newSeparator.getNodes()) {
							if ((node instanceof TreeVariable)
									&& ((TreeVariable)node).getAssociatedClique() == separatorToDelete) {	// use exact instance equality, instead of Object#equal
								((TreeVariable)node).setAssociatedClique(newSeparator);
							}
						}
						
						// finally, include separator to original junction tree
						originalJunctionTree.addSeparator(newSeparator);
					}
					
					// remove new clique from max prime subtree. This will make sure it won't be referenced later
					// just to make sure references from the clique are also removed
//					newCliqueInMaxPrimeJunctionTree.setParent(null);		
					newCliqueInMaxPrimeJunctionTree.getChildren().clear();	
					// call the general method to remove clique
					primeSubgraphJunctionTree.removeCliques(Collections.singletonList(newCliqueInMaxPrimeJunctionTree));
					
				} else {
					// set new clique as a child of old unchanged clique
//					newCliqueInMaxPrimeJunctionTree.setParent(unchangedOriginalClique);
					((LoopyJunctionTree)originalJunctionTree).addParent(unchangedOriginalClique, newCliqueInMaxPrimeJunctionTree);
					unchangedOriginalClique.addChild(newCliqueInMaxPrimeJunctionTree);
					
					// The border separator needs to be replaced, because we cannot change the cliques it points to/from;
					Separator newSeparator = new Separator(unchangedOriginalClique, newCliqueInMaxPrimeJunctionTree, false);
					// copy content of border separator
					newSeparator.setInternalIdentificator(borderSeparatorSubstitutorId++);
					newSeparator.setNodes(borderSeparator.getNodes());
					
					// Again, just reuse the same instance of table of border separator, because we'll discard the border separator anyway.
					newSeparator.setProbabilityFunction(borderSeparator.getProbabilityFunction());
					
					// Again, separators between new cliques (of junction trees built from max prime subgraph) and original cliques 
					// (of the junction tree we had before running dynamic compilation) needs to be filled with 1 (the null value in multiplication and division), 
					// so that when we run initial propagation (for global consistency) the local probabilities of original cliques and new cliques gets consistently 
					// merged without being biased by content of old separator table.
//					newSeparator.getProbabilityFunction().fillTable(1f);
					// TODO check if the above is necessary

					// if any node is pointing to the old separator (with TreeVariable#getAssociatedClique()), then point to the new separator instead
					for (Node node : newSeparator.getNodes()) {
						if ((node instanceof TreeVariable)
								&& ((TreeVariable)node).getAssociatedClique() == borderSeparator) {	// use exact instance equality, instead of Object#equal
							((TreeVariable)node).setAssociatedClique(newSeparator);
						}
					}
					
					// this will substitute the border separator, once the previous border separator is deleted
					originalJunctionTree.addSeparator(newSeparator);
					
				}
				
				// the border separator needs to be deleted regardless of whether the new clique was merged/joined or not
				originalJunctionTree.removeSeparator(borderSeparator);
				
				// disconnect the old child from parent
				unchangedOriginalClique.removeChild(modifiedOriginalClique);
				//modifiedOriginalClique.setParent(null);
				((LoopyJunctionTree)originalJunctionTree).removeParent(unchangedOriginalClique, modifiedOriginalClique);
				
				
			} else {
				// case 2 - new clique becomes a parent of original unchanged clique
				
				// disconnect unchanged clique (child) from its parent to be substituted
				// remove link from child to parent
				((LoopyJunctionTree)originalJunctionTree).removeParent(modifiedOriginalClique, unchangedOriginalClique);
				// remove link from parent to child (the opposite direction)
				modifiedOriginalClique.removeChild(unchangedOriginalClique);
				
				// if new clique and border separator are the same (i.e. represents same joint space), then join original (unchanged) clique and new clique;
				if (borderSeparator.getNodes().size() == newCliqueInMaxPrimeJunctionTree.getNodesList().size()
						&& borderSeparator.getNodes().containsAll(newCliqueInMaxPrimeJunctionTree.getNodesList())) {
					// if they have the same size, and one is included in other, then they are the same
				
					// join cliques. The new clique will remain, and the unchanged original clique will be deleted
					newCliqueInMaxPrimeJunctionTree.join(unchangedOriginalClique);
					
					// move the references of associated nodes of unchangedOriginalClique to the joined clique
					for (Node node : unchangedOriginalClique.getAssociatedProbabilisticNodesList()) {
						if ((node instanceof TreeVariable)
								&& ((TreeVariable) node).getAssociatedClique().equals(unchangedOriginalClique)) {
							((TreeVariable) node).setAssociatedClique(newCliqueInMaxPrimeJunctionTree);
						}
					}
					
					// set the resulting (joined) clique as a parent of all children of the clique being deleted
					for (Clique childClique : new ArrayList<Clique>(unchangedOriginalClique.getChildren())) {	// using a clone, because of concurrent modification
						
						// inherit children of the clique that is going to be "deleted" (actually, it simply won't be included to original junction tree)
						newCliqueInMaxPrimeJunctionTree.addChild(childClique);
						if (originalJunctionTree instanceof LoopyJunctionTree) {
							((LoopyJunctionTree)originalJunctionTree).addParent(newCliqueInMaxPrimeJunctionTree, childClique);
							((LoopyJunctionTree) originalJunctionTree).removeParent(unchangedOriginalClique, childClique);
						}
						// mark joined clique as the main parent node. This works also when JT is not loopy
						childClique.setParent(newCliqueInMaxPrimeJunctionTree);
						
						
						// also create separator with this child clique, 
						
						// but before that, we need to delete separator from the original junction tree, so that it won't be referenced later
						Separator separatorToDelete = originalJunctionTree.getSeparator(unchangedOriginalClique, childClique);
						originalJunctionTree.removeSeparator(separatorToDelete);
						
						// now, create the separator in the original net
						Separator newSeparator = new Separator(newCliqueInMaxPrimeJunctionTree, childClique, false);
						
						// copy content of deleted separator
						newSeparator.setInternalIdentificator(borderSeparatorSubstitutorId++);
						newSeparator.setNodes(separatorToDelete.getNodes());
						
						// just reusing the same instance of table, because the old separator was deleted anyway
						newSeparator.setProbabilityFunction(separatorToDelete.getProbabilityFunction());
						
						// Again and again... Separators between new cliques (of junction trees built from max prime subgraph) and original cliques 
						// (of the junction tree we had before running dynamic compilation) needs to be filled with 1 (the null value in multiplication and division), 
						// so that when we run initial propagation (for global consistency) the local probabilities of original cliques and new cliques gets consistently 
						// merged without being biased by content of old separator table.
//						newSeparator.getProbabilityFunction().fillTable(1f);
						// TODO check if the above is necessary

						// if any node is pointing to the old separator (with TreeVariable#getAssociatedClique()), then point to the new separator instead
						for (Node node : newSeparator.getNodes()) {
							if ((node instanceof TreeVariable)
									&& ((TreeVariable)node).getAssociatedClique() == separatorToDelete) {	// use exact instance equality, instead of Object#equal
								((TreeVariable)node).setAssociatedClique(newSeparator);
							}
						}
						
						// finally, include separator to original junction tree
						originalJunctionTree.addSeparator(newSeparator);
					}
					

					// needs to delete the unchanged clique (the one absorbed by the new clique) from original join tree, because newCliqueInMaxPrimeJunctionTree will take its role
					unchangedOriginalClique.getChildren().clear();
					originalJunctionTree.removeCliques(Collections.singletonList(unchangedOriginalClique));
					
				} else {
					// set new clique as a parent of old unchanged clique
					newCliqueInMaxPrimeJunctionTree.addChild(unchangedOriginalClique);
//					unchangedOriginalClique.setParent(newCliqueInMaxPrimeJunctionTree);
					((LoopyJunctionTree)originalJunctionTree).addParent(newCliqueInMaxPrimeJunctionTree, unchangedOriginalClique);
					
					// Again, the border separator needs to be replaced, because we cannot change the cliques it points to/from;
					Separator newSeparator = new Separator(newCliqueInMaxPrimeJunctionTree, unchangedOriginalClique, false);
					// copy content of border separator
					newSeparator.setInternalIdentificator(borderSeparatorSubstitutorId++);
					newSeparator.setNodes(borderSeparator.getNodes());
					
					// again, reuse the same instance of border separator's table, because the border separator will be deleted anyway
					newSeparator.setProbabilityFunction(borderSeparator.getProbabilityFunction());
					
					// Again and again and again... Separators between new cliques (of junction trees built from max prime subgraph) and original cliques 
					// (of the junction tree we had before running dynamic compilation) needs to be filled with 1 (the null value in multiplication and division), 
					// so that when we run initial propagation (for global consistency) the local probabilities of original cliques and new cliques gets consistently 
					// merged without being biased by content of old separator table.
//					newSeparator.getProbabilityFunction().fillTable(1f);

					// if any node is pointing to the old separator (with TreeVariable#getAssociatedClique()), then point to the new separator instead
					for (Node node : newSeparator.getNodes()) {
						if ((node instanceof TreeVariable)
								&& ((TreeVariable)node).getAssociatedClique() == borderSeparator) {	// use exact instance equality, instead of Object#equal
							((TreeVariable)node).setAssociatedClique(newSeparator);
						}
					}
					
					// replace the border separator
					originalJunctionTree.addSeparator(newSeparator);
				}
				
				// the border separator needs to be deleted regardless of whether the new clique was merged/joined or not
				originalJunctionTree.removeSeparator(borderSeparator);
				
			}
		}	// end of iteration on border separators
    	
    	

    	// Now, we need to delete all the modified (old) cliques and separators from original junction tree;
    	
    	// first, delete the separators that pairwise connects modified cliques (because we only deleted the border separators)
//    	Set<Separator> separatorsToDelete = new HashSet<Separator>(); // this is to avoid deleting and reading concurrently (which may cause exceptions)
//    	for (Separator separator : originalJunctionTree.getSeparators()) {
//			if (modifiedCliques.contains(separator.getClique1())
//					|| modifiedCliques.contains(separator.getClique2())) {
//				// this is a separator connecting cliques to be deleted (this is a sufficient condition), so delete it
//				separatorsToDelete.add(separator);
//			}
//		} // TODO assuming that number of modified cliques is small, isn't it faster to iterate on pairs of modified cliques?
//    	
//    	// use the IJunctionTree#removeSeparator(Separator), so that internal indexes (for faster access) are also removed
//    	for (Separator separator : separatorsToDelete) {
//			originalJunctionTree.removeSeparator(separator);
//		}
    	// we don't need the above code anymore, because it was included in JunctionTree#removeCliques(Collection)
    	
    	// then, delete all old (modified) cliques
    	originalJunctionTree.removeCliques(modifiedCliques);
    	
    	// Do not forget to include all the new cliques into the original junction tree;
    	for (Clique newClique : primeSubgraphJunctionTree.getCliques()) {
    		// TODO should we check for duplicates?
			originalJunctionTree.getCliques().add(newClique);
		}
    	// do the same for separators in max prime subtree;
    	for (Separator separator : primeSubgraphJunctionTree.getSeparators()) {
			originalJunctionTree.addSeparator(separator);
			// also make sure to update mapping of parents in LoopyJunctionTree
			((LoopyJunctionTree)originalJunctionTree).addParent(separator.getClique1(), separator.getClique2());
		}
    	
    	// if we join a loopy junction tree to the original junction tree, then the original junction tree will also become loopy
    	if ((primeSubgraphJunctionTree instanceof LoopyJunctionTree ) && ((LoopyJunctionTree)primeSubgraphJunctionTree).isLoopy() ) {
    		// TODO handle case when original junction tree is not a LoopyJunctionTree;
    		((LoopyJunctionTree)originalJunctionTree).setLoopy(true);
    	}
    	
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
    	// Cannot use IJunctionTree#getCliquesContainingAllNodes(Collection,int), because it assumes the node is associated with only 1 clique
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
    	
    	// if loopy, we need to consider all path between the clusters 
    	if (originalJunctionTree.isUsingApproximation()) {	
    		// Pick paths between child and parent clusters. Can be paths between arbitrary clusters containing the nodes, so pick the 1st clusters from each
    		Collection<List<Clique>> paths = ((JunctionTree)decompositionTree).getPaths(childClusters.get(0), parentClusters.get(0));	
    		
    		// TODO shorten path depending on implementation
    		// TODO reassemble clique graph if all path between the two cliques contain empty separator
    		
    		// mark for modification all clusters in all path between the two clusters
    		Set<Clique> clustersMarkedForModiciation = new HashSet<Clique>();
    		for (List<Clique> path : paths) {
				clustersMarkedForModiciation.addAll(path);
			}
    		return clustersMarkedForModiciation;
    	}
    	
    	// at this point, the clique structure is not a graph (it's supposedly a junction tree)
    	
    	// find shortest path between clusters of child node and clusters of parent node
    	List<Clique> shortestPathFromChild = null;  			// shortest path from the cluster of child node to the cluster of parent node
    	for (Clique clusterWithChildNode : childClusters) {
    		for (Clique clusterWithParentNode : parentClusters) {
    			List<Clique> currentPath = decompositionTree.getPath(clusterWithChildNode, clusterWithParentNode);
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
    	
    	
    	// Find the equivalent empty separator from original junction tree. 
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
    	
    	// The following separators will be the ones that connect the parent node's clique/cluster and child node's clique/cluster.
    	// A node will be included to them later, so that they are not kept empty, 
    	// because we want to make sure cliques/clusters between parent node and child node are kept connected,
    	// but if we keep separators empty they may be disconnected later when calling this method for another arc.
    	Separator newSeparatorInMaxPrimeDecomposition = emptySeparatorInShortestPath;			// the next if clause will check if empty sep in shortest path connects parent and child nodes
    	Separator newSeparatorInOriginalJunctionTree  = originalEmptySeparatorInShortestPath;
    	
		// If the clusters are already directly connected by the empty separator, we don't need to reconnect clusters (i.e. no need to substitute separator). Check such condition.
		if ( !( ( emptySeparatorInShortestPath.getClique1().equals(clusterToBecomeParent) && emptySeparatorInShortestPath.getClique2().equals(clusterToBecomeChild) )
    			|| ( emptySeparatorInShortestPath.getClique2().equals(clusterToBecomeParent) && emptySeparatorInShortestPath.getClique1().equals(clusterToBecomeChild) ) ) ) {
    		
    		// The clusters are not directly connected, so we need to reconnect clusters and thus substitute separator.
    		// We also need to do the same for the original junction tree, so extract separator and cliques from original junction tree and perform same modifications.
    		
    		// remove empty separator from the max prime subgraph decomposition tree
    		decompositionTree.removeSeparator(emptySeparatorInShortestPath);
    		// do the same for the original junction tree
    		originalJunctionTree.removeSeparator(originalEmptySeparatorInShortestPath);	
    		
    		// also disconnect the clusters connected by the empty separator
    		if (emptySeparatorInShortestPath.getClique1().getParent() != null
//    				&& emptySeparatorInShortestPath.getClique1().getParent().equals(emptySeparatorInShortestPath.getClique2())) {
    				&& ((LoopyJunctionTree)decompositionTree).getParents(emptySeparatorInShortestPath.getClique1()).contains(emptySeparatorInShortestPath.getClique2())) {
    			((LoopyJunctionTree)decompositionTree).removeParent(emptySeparatorInShortestPath.getClique2(), emptySeparatorInShortestPath.getClique1());
//    			emptySeparatorInShortestPath.getClique1().setParent(null);
    			emptySeparatorInShortestPath.getClique2().removeChild(emptySeparatorInShortestPath.getClique1());
    			
    			// do the same for the original cliques;
    			((LoopyJunctionTree)originalJunctionTree).removeParent(originalEmptySeparatorInShortestPath.getClique2(), originalEmptySeparatorInShortestPath.getClique1());
//    			originalEmptySeparatorInShortestPath.getClique1().setParent(null);
    			originalEmptySeparatorInShortestPath.getClique2().removeChild(originalEmptySeparatorInShortestPath.getClique1());
    			
    		} else {
    			((LoopyJunctionTree)decompositionTree).removeParent(emptySeparatorInShortestPath.getClique1(), emptySeparatorInShortestPath.getClique2());
//    			emptySeparatorInShortestPath.getClique2().setParent(null);
    			emptySeparatorInShortestPath.getClique1().removeChild(emptySeparatorInShortestPath.getClique2());
    			
    			// do the same for the original cliques;
    			((LoopyJunctionTree)originalJunctionTree).removeParent(originalEmptySeparatorInShortestPath.getClique1(), originalEmptySeparatorInShortestPath.getClique2());
//    			originalEmptySeparatorInShortestPath.getClique2().setParent(null);
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
//    		clusterToBecomeChild.setParent(clusterToBecomeParent);
    		((LoopyJunctionTree)decompositionTree).addParent(clusterToBecomeParent, clusterToBecomeChild);
    		// same modification to original junction tree
    		originalCliqueToBecomeParent.addChild(originalCliqueToBecomeChild);
//    		originalCliqueToBecomeChild.setParent(originalCliqueToBecomeParent);
    		((LoopyJunctionTree)originalJunctionTree).addParent(originalCliqueToBecomeParent, originalCliqueToBecomeChild);
    		
    		// and also insert the new empty separator. No need to update TreeVariable#getAssociatedClique of nodes in this separator, because there is no node at all
    		newSeparatorInMaxPrimeDecomposition = new StubSeparator(clusterToBecomeParent, clusterToBecomeChild);
    		newSeparatorInMaxPrimeDecomposition.setInternalIdentificator(emptySeparatorInShortestPath.getInternalIdentificator());	// reuse ID from deleted separator
			decompositionTree.addSeparator(newSeparatorInMaxPrimeDecomposition);	// stub separator will not initialize probability table
			// again, same modification to original junction tree
			newSeparatorInOriginalJunctionTree = new Separator(originalCliqueToBecomeParent, originalCliqueToBecomeChild, false); // false:=parent/children's links shall not be re-included
			newSeparatorInOriginalJunctionTree.setInternalIdentificator(originalEmptySeparatorInShortestPath.getInternalIdentificator());	// reuse ID from deleted separator
			originalJunctionTree.addSeparator(newSeparatorInOriginalJunctionTree); 
			
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
    	

    	// accordingly to Julia Florez's paper, the empty separator between the two clusters must now include the parent node.
    	// This is in order to avoid disconnecting cliques that were connected in this method 
    	// (because calling this method multiple times may eventually disconnect and connect the cliques to other portions, if we leave the separator empty).
    	newSeparatorInMaxPrimeDecomposition.getNodes().add(parent); // no need to include node into separator's potential table, because stub separator is for storing the structure only, not the potentials
    	newSeparatorInOriginalJunctionTree.getNodes().add(parent);  // do this to original separator too. 
//    	newSeparatorInOriginalJunctionTree.getProbabilityFunction().addVariable(parent);
    	// No need to update content of separator table, because whether a separator is empty or not is checked by looking at Separator#getNodes(), not at the tables.
    	// besides, these separators will be discarded anyway (since the cliques connected here are marked for modification, thus will be substituted).
    	
    	// only the parent and child cluster shall be marked for modification, because they are directly connected now;
    	List<Clique> ret = new ArrayList<Clique>(2);
    	ret.add(clusterToBecomeParent);
    	ret.add(clusterToBecomeChild);
    	return ret;
	}
    

	/**
     * This method identifies which clusters in a maximum subgraph decomposition tree needs to be modified
     * because of an edge/arc being deleted from the network.
     * @param deletedEdge : the new edge/arc to be deleted. 
     * This {@link Edge} object simply represents a pair of nodes, thus it should not be an arc actually existing in some external network.
     * This MUST be an edge between instances of nodes in the new network
     * (i.e. it must not be the actual arc in old network, because instances of nodes in old network are not same object instances).
     * Therefore, the nodes specified by this arc must also be present in originalJunctionTree or decompositionTree.
     * @param originalJunctionTree : the junction tree of probabilities
     * that is being reused in the dynamic junction tree compilation algorithm
     * @param decompositionTree : this is the maximum subgraph decomposition tree
     * obtained from {@link #getMaximumPrimeSubgraphDecompositionTree(IJunctionTree)}.
     * This object can have its content modified after calling this method.
	 * @param originalCliqueToClusterMap : this map associates cliques in original junction tree to clusters in max prime subgraph decomposition tree. 
     * It it used to retrieve the cluster currently being evaluated from cliques of original junction tree associated with a node in deleted arc.
     * @return clusters in the decomposition tree that needs to be modified
     * @see #runDynamicJunctionTreeCompilation()
     * @see #getClustersToModifyAddNode(INode, IJunctionTree)
     * @see #getClustersToModifyIncludeEdge(Edge, IJunctionTree)
     * @see #getClustersToModifyRemoveNode(INode, IJunctionTree)
     */
	protected Collection<Clique> treatRemoveEdge( Edge deletedEdge, IJunctionTree originalJunctionTree, IJunctionTree decompositionTree, Map<Clique, Clique> originalCliqueToClusterMap) {
    	// basic assertions
    	if (deletedEdge == null) {
    		// nothing to do
    		return Collections.emptyList();
    	}
    	if (decompositionTree == null) {
    		throw new NullPointerException("Max prime subgraph decomposition tree was not specified.");
    	}
    	
    	// get the child node of the edge
    	Node childNode = deletedEdge.getDestinationNode();
    	if (childNode == null) {
    		throw new IllegalArgumentException(deletedEdge + " is pointing to null node.");
    	}
    	
////    	Clique currentCluster = originalJunctionTree.getCliquesContainingAllNodes((List)Collections.singletonList(childNode), 1).get(0);	// return exactly 1 element
//    	Clique currentCluster = originalCliqueToClusterMap.get(originalJunctionTree.getCliquesContainingAllNodes((List)Collections.singletonList(childNode), 1).get(0));
//    	if (currentCluster == null) {
//    		throw new NullPointerException("Found a null cluster when searching for node " + childNode + " in max prime subgraph decomposition tree.");
//    	}
//    	
//    	// the collection of clusters being marked
////    	Collection<Clique> ret = new HashSet<Clique>();
////    	
////    	treatRemoveEdgeClusterRecursive(Collections.singleton(deletedEdge), currentCluster, ret, decompositionTree);
////    	
////    	return ret;
//    	// the above code was removed, because the max prime subgraph decomposition tree is created on-the-fly, so the deleted edge is already considered when clusters were built.
//    	// that is: the current cluster already contains nodes in cliques/cluster connected by separators which became incomplete because of deleted edges, because the current cluster
//    	// was generated AFTER the arc was deleted.
//    	return Collections.singletonList(currentCluster);
    	
    	

    	// get the parent node of the edge
    	Node parentNode = deletedEdge.getOriginNode();
    	if (parentNode == null) {
    		throw new IllegalArgumentException(deletedEdge + " is coming from null node.");
    	}
    	
    	// get a cluster containing the child and parent node
    	List<INode> parentAndChild = new ArrayList<INode>(2);
    	parentAndChild.add(parentNode);
    	parentAndChild.add(childNode);
    	List<Clique> cliqueWithChildAndParent = originalJunctionTree.getCliquesContainingAllNodes(parentAndChild, 1);
    	if (cliqueWithChildAndParent != null && !cliqueWithChildAndParent.isEmpty()) {
    		return Collections.singletonList(originalCliqueToClusterMap.get(cliqueWithChildAndParent.get(0)));
    	}
    	
    	// at this point, there is no clique containing both nodes simultaneously. Return all clusters containing child node
    	Set<Clique> ret = new HashSet<Clique>();
    	List<Clique> cliquesWithChildNode = originalJunctionTree.getCliquesContainingAllNodes((List)Collections.singletonList(childNode), Integer.MAX_VALUE);
    	for (Clique clique : cliquesWithChildNode) {
			ret.add(originalCliqueToClusterMap.get(clique));
		}
    	return ret;
	}

//	/**
//	 * This is used in {@link #treatRemoveEdge(Edge, IJunctionTree, IJunctionTree, Map)} to
//	 * recursively find clusters connected to another cluster by a separator which became incomplete due to arcs being removed.
//	 * @param deletedEdges : arcs being deleted
//	 * @param currentCluster : cluster to check in current recursive call.
//	 * @param previousCluster : cluster checked in previous recursive call. This will be used to extract separator between calls.
//	 * The 1st call must use null for this value.
//	 * @param markedClusters : this is an input/output argument.
//	 * This collection contains clusters that were handled/marked already for modification. 
//	 * The 1st call must set this value to a modifiable empty collection.
//	 * @param decompositionTree : the max prime subgraph decomposition tree where the clusters belong. This will be used to extract separators.
//	 * @see Separator#isComplete()
//	 */
//    protected void treatRemoveEdgeClusterRecursive( Collection<Edge> deletedEdges, Clique currentCluster, Collection<Clique> markedClusters, IJunctionTree decompositionTree) {
//		
//    	// basic assertions
//    	if (deletedEdges == null || deletedEdges.isEmpty() 
//    			|| currentCluster == null) {
//    		return;	// nothing to do
//    	}
//    	if (decompositionTree == null) {
//    		throw new NullPointerException("The max prime subgraph decomposition tree must be specified.");
//    	}
//    	// make sure the collection of marked clusters is never null
//    	if (markedClusters == null) {
//    		markedClusters = new HashSet<Clique>();
//    	}
//    	
//    	// if current cluster is already marked, stop
//    	if (markedClusters.contains(currentCluster)) {
//    		return;
//    	}
//    	
//    	// mark current cluster for modification
//    	markedClusters.add(currentCluster);	
//    	
//    	// check parent clusters recursively
//    	if (decompositionTree instanceof JunctionTree) { // we may have a graph instead of tree, so use JunctionTree#getParents instead of Clique#getParent
//    		for (Clique parentCluster : ((JunctionTree) decompositionTree).getParents(currentCluster)) {
//    			
//    			// extract the separator between parent and current cluster, and check if the separator will be kept complete
//    			Separator sep = decompositionTree.getSeparator(parentCluster, currentCluster);
//    			if (sep == null) {
//    				throw new IllegalArgumentException("No separator between cluster " + parentCluster + " and " + currentCluster + " was found in max prime subgraph decomposition.");
//    			}
//    			
//				// check if the separator will become incomplete if we delete the edge
//    			if (!sep.isComplete(deletedEdges)) {
//    				// if it is not complete anymore, recursively mark parent cluster for modification
//    				treatRemoveEdgeClusterRecursive(deletedEdges, parentCluster, markedClusters, decompositionTree);
//    			}
//    	    	
//			}
//    	} else { // consider this a pure tree, so only treat single parent
//    		
//			// extract the separator between parent and current cluster, and check if the separator will be kept complete
//			Separator sep = decompositionTree.getSeparator(currentCluster.getParent(), currentCluster);
//			if (sep == null) {
//				throw new IllegalArgumentException("No separator between cluster " + currentCluster.getParent() + " and " + currentCluster + " was found in max prime subgraph decomposition.");
//			}
//			
//			// check if the separator will become incomplete if we delete the edge
//			if (!sep.isComplete(deletedEdges)) {
//				// if it is not complete anymore, recursively mark parent cluster for modification
//				treatRemoveEdgeClusterRecursive(deletedEdges, currentCluster.getParent(), markedClusters, decompositionTree);
//			}
//			
//    	}
//    	
//    	// check child clusters recursively
//    	for (Clique childCluster : currentCluster.getChildren()) {
//
//			// extract the separator between parent and current cluster, and check if the separator will be kept complete
//			Separator sep = decompositionTree.getSeparator(currentCluster, childCluster);
//			if (sep == null) {
//				throw new IllegalArgumentException("No separator between cluster " + childCluster + " and " + currentCluster + " was found in max prime subgraph decomposition.");
//			}
//			
//			// check if the separator will become incomplete if we delete the edge
//			if (!sep.isComplete(deletedEdges)) {
//				// if it is not complete anymore, recursively mark child cluster for modification
//				treatRemoveEdgeClusterRecursive(deletedEdges, childCluster, markedClusters, decompositionTree);
//			}
//		}
//    	
//	}

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
    	// basic assertions
    	if (deletedNode == null) {
    		// nothing to do
    		return Collections.emptyList();
    	}
    	if (decompositionTree == null || originalJunctionTree == null) {
    		throw new NullPointerException("Junction tree or max prime subgraph decomposition tree were not specified.");
    	}
    	
    	// just make sure the mapping is non-null
    	if (clusterToOriginalCliqueMap == null) {
    		clusterToOriginalCliqueMap = Collections.emptyMap();
    	}
    	
    	// simply remove node from clusters, cliques and separators containing the deleted node, and mark them for modification
    	
    	Collection<Clique> ret = new ArrayList<Clique>();
    	
    	// remove node from clusters (and from cliques in original JT)
    	// TODO only check for clusters/cliques associated with node, so that we don't need to iterate on all clusters/cliques
    	for (Clique cluster : decompositionTree.getCliques()) {
    		// remove node from cluster
    		if (cluster.getNodesList().remove(deletedNode)) { 
    			// if true, list was changed (it means that cluster contained the node)
    			ret.add(cluster);	// mark this cluster for modification
    			
    			// remove node from separators connected to this cluster
    			Set<Separator> separators = ((JunctionTree)decompositionTree).getSeparatorsMap().get(cluster);
    			for (Separator separator : separators) {
    				separator.getNodes().remove(deletedNode);
					// no need to remove from separator table, because they are not filled
				}
    			
    			
    			// make sure we can retrieve the cliques related to this cluster in decomposition tree
    			Collection<Clique> originalCliques = clusterToOriginalCliqueMap.get(cluster);
    			if (originalCliques == null || originalCliques.isEmpty()) {
    				throw new IllegalArgumentException("Cluster " + cluster + " in max prime subgraph decomposition tree is not associated with any clique in original Junction Tree.");
    			}
    			
    			for (Clique clique : originalCliques) {
    				// remove node from original cliques
    				if (clique.getNodesList().remove(deletedNode)){
    					// remove node from clique tables
    					clique.getProbabilityFunction().removeVariable(deletedNode);
    					
    					// Also remove node from separators in original JT;
    					separators = ((JunctionTree)originalJunctionTree).getSeparatorsMap().get(clique);
    					for (Separator separator : separators) {
    						if (separator.getNodes().remove(deletedNode)) {
    							// remove node from separator table
    							separator.getProbabilityFunction().removeVariable(deletedNode);
    						}
    					}
    				}
				}
    			// we don't need to remove from max prime subgraph decomposition tree, because they don't have clique tables
    		}
		}
    	
    	return ret;
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
    	// we'll just add a new clique and insert the new node
    	
    	// create the clique instance
    	Clique cliqueOfNewNode = new Clique();
		cliqueOfNewNode.getNodesList().add((Node)includedNode);
		cliqueOfNewNode.getProbabilityFunction().addVariable(includedNode);
		cliqueOfNewNode.setInternalIdentificator(originalJunctionTree.getCliques().size());
		// same for the decomposition tree
		Clique clusterOfNewNode = new StubClique();
		clusterOfNewNode.getNodesList().add((Node)includedNode);
//		clusterOfNewNode.getProbabilityFunction().addVariable(includedNode);
		clusterOfNewNode.setInternalIdentificator(cliqueOfNewNode.getInternalIdentificator());
		
		
		// extract the root cluster (cluster with no parents)
		Clique rootCluster = null;
		// do sequential search, but the root is likely to be the 1st cluster
		for (Clique cluster : decompositionTree.getCliques()) {
			if (cluster.getParent() == null) {
				rootCluster = cluster;
				break;
			}
		}
		if (rootCluster == null) {
			throw new RuntimeException("Inconsistent max prime subgraph decomposition tree structure: no root node was found.");
		}
		// similarly, extract the root clique in junction tree
		Clique rootClique = null;
//		for (Clique clique : clusterToOriginalCliqueMap.get(rootCluster)) {	// root clique is supposedly in root cluster
		for (Clique clique : originalJunctionTree.getCliques()) {
			if (clique.getParent() == null) {
				rootClique = clique;
				break;
			}
		}
		if (rootClique == null) {
			throw new RuntimeException("Inconsistent junction tree structure: no root node was found.");
		}
		
		// add clique to junction tree, so that the algorithm can handle the clique correctly
		originalJunctionTree.getCliques().add(cliqueOfNewNode);
		decompositionTree.getCliques().add(clusterOfNewNode);
		
		// create separator between the clique of parent nodes and virtual node (the separator should contain all parents)
		Separator separatorOfNewNode = new Separator(rootClique , cliqueOfNewNode, false);
		rootClique.addChild(cliqueOfNewNode);
		((LoopyJunctionTree)originalJunctionTree).addParent(rootClique, cliqueOfNewNode);
		separatorOfNewNode.setInternalIdentificator(-(originalJunctionTree.getSeparators().size()+1)); // internal identificator must be set before adding separator, because it is used as key
		originalJunctionTree.addSeparator(separatorOfNewNode);
		// do the same for max prime subgraph decomposition tree
		StubSeparator separatorOfNewNodeInPrimeDecompositon = new StubSeparator(rootCluster , clusterOfNewNode);
		// for stub separator, we need to explicitly set parent/child cliques
		rootCluster.addChild(clusterOfNewNode);
//		clusterOfNewNode.setParent(rootCluster);
		((LoopyJunctionTree)decompositionTree).addParent(rootCluster, clusterOfNewNode);
		separatorOfNewNodeInPrimeDecompositon.setInternalIdentificator(separatorOfNewNode.getInternalIdentificator());
		decompositionTree.addSeparator(separatorOfNewNodeInPrimeDecompositon);
		
		// now, let's link the nodes with the cliques/clusters
		cliqueOfNewNode.getAssociatedProbabilisticNodes().add((Node) includedNode);
		clusterOfNewNode.getAssociatedProbabilisticNodes().add((Node) includedNode);
		((TreeVariable)includedNode).setAssociatedClique(cliqueOfNewNode);
		// no need to associate node -> cluster
		
		// initialize the probabilities of clique and separator
		originalJunctionTree.initBelief(cliqueOfNewNode);
		originalJunctionTree.initBelief(separatorOfNewNode);	// this one sets all separator potentials to 1
		// no need to initialize potentials in max prime subgraph decomposition tree
		
		// store the potentials after propagation, so that the "reset" will restore these values
		cliqueOfNewNode.getProbabilityFunction().copyData();	
		separatorOfNewNode.getProbabilityFunction().copyData();
		
		// update the marginal values (we only updated clique/separator potentials, thus, the marginals still have the old values if we do not update)
		((TreeVariable)includedNode).updateMarginal();
		((TreeVariable)includedNode).copyMarginal();
    	
		// and finally, add the mapping
		clusterToOriginalCliqueMap.put(clusterOfNewNode, Collections.singletonList(cliqueOfNewNode));
				
    	return Collections.EMPTY_LIST;	// no need to tell which cliques were changed, because the change has been made already
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
		
		// this keeps track of what arcs were included to ret (it keeps trac of both directions, so that we don't add arc between two nodes in 2 directions).
		Map<Node, Set<Node>> retMap = new HashMap<Node, Set<Node>>();
		// the mapping must contain the includedEdges already
		for (Edge edge : includedEdges) {
			// add new mapping
			Set<Node> mapping = retMap.get(edge.getOriginNode());
			if (mapping == null) {
				mapping = new HashSet<Node>();
				retMap.put(edge.getOriginNode(), mapping);
			}
			mapping.add(edge.getDestinationNode());
			// add mapping in the reverse direction too
			mapping = retMap.get(edge.getDestinationNode());
			if (mapping == null) {
				mapping = new HashSet<Node>();
				retMap.put(edge.getDestinationNode(), mapping);
			}
			mapping.add(edge.getOriginNode());
		}
		
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
					if (oldParentToBeMoralized != null) {
						// check if they have had some children in common. If so, they were already moralized in old net (so we don't need new moral arc)
						// use an array list, so that internal comparison uses Object#equals(Object), because it will do name comparison
						List<Node> childrenInCommon = new ArrayList<Node>((List)oldParent.getChildNodes());
						childrenInCommon.retainAll((List)oldParentToBeMoralized.getChildNodes());
						if (!childrenInCommon.isEmpty()) {
							// they had some child in common, so no need to treat this pair. Go to next pair of parents
							continue;
						}
					}	// if oldParentToBeMoralized==null, then there is a new moral arc to add between the parentToBeMoralized and newParent
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
				if (!retMap.containsKey(edgeForMoralization.getOriginNode()) 	// there is no arc to/from the origin node
						|| !retMap.get(edgeForMoralization.getOriginNode()).contains(edgeForMoralization.getDestinationNode())) {	// there is an arc to/from origin node, but it is not to/from destination node
					// TODO check if the above if is redundant and/or can be optimized
//					edgeForMoralization.setDirection(false);	// TODO check if this is necessary
					ret.add(edgeForMoralization);
					// add new mapping regarding the new edge we just included in ret
					Set<Node> mapping = retMap.get(edgeForMoralization.getOriginNode());
					if (mapping == null) {
						mapping = new HashSet<Node>();
						retMap.put(edgeForMoralization.getOriginNode(), mapping);
					}
					mapping.add(edgeForMoralization.getDestinationNode());
					// add mapping in the reverse direction too
					mapping = retMap.get(edgeForMoralization.getDestinationNode());
					if (mapping == null) {
						mapping = new HashSet<Node>();
						retMap.put(edgeForMoralization.getDestinationNode(), mapping);
					}
					mapping.add(edgeForMoralization.getOriginNode());
				}
			}
		}
		return ret;
	}

//	/**
//     * @param oldNet : the network before changes. This will be used together with
//     * the current network in order to retrieve which nodes were parents of some common child.
//     * @param newNet : current network. This will be used to retrieve existing parents. 
//	 * @param deletedEdges : edges to be considered that were deleted from old net.
//     * @return a collection of edges (not real edges in the network) that represents connections between moral parents
//     * (i.e. conditional dependence between parents with a common child) that was deleted because of arcs being deleted
//     * from the network.
//     * @see #runDynamicJunctionTreeCompilation()
//     */
//	protected Collection<? extends Edge> getDeletedMoralArcs( ProbabilisticNetwork oldNet, ProbabilisticNetwork newNet, Collection<Edge> deletedEdges) {
//    	throw new UnsupportedOperationException("Current version of dynamic junction tree compilation does not handle deletion of arcs.");
////		return Collections.EMPTY_LIST;
//	}



	
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
	 * @deprecated use {@link IncrementalJunctionTreeAlgorithm#getMaximumPrimeSubgraphDecompositionTree(IJunctionTree, Map, Map)} instead.
	 */
	@Deprecated
	public IJunctionTree getMaximumPrimeSubgraphDecompositionTree(IJunctionTree originalJunctionTree, Map<Clique, Collection<Clique>> clusterToOriginalCliqueMap) throws InstantiationException, IllegalAccessException {
		return this.getMaximumPrimeSubgraphDecompositionTree(originalJunctionTree, clusterToOriginalCliqueMap, null);
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
	 * This argument is an output argument. If null, then it will simply be ignored.
	 * @param cliqueToGeneratedClusterMap : this is an inverse mapping of clusterToOriginalCliqueMap.
	 * This is also an output argument. If null, then it will simply be ignored.
	 * @return a junction tree with no potentials filled (i.e. cliques only represent set of nodes, not a table of globally consistent joint probabilities).
	 * @throws IllegalAccessException from {@link IJunctionTreeBuilder#buildJunctionTree(Graph)}
	 * @throws InstantiationException  from {@link IJunctionTreeBuilder#buildJunctionTree(Graph)}
	 * @see Separator#isComplete()
	 */
	public IJunctionTree getMaximumPrimeSubgraphDecompositionTree(IJunctionTree originalJunctionTree, 
																Map<Clique, Collection<Clique>> clusterToOriginalCliqueMap, 
																Map<Clique, Clique> cliqueToGeneratedClusterMap) 
																throws InstantiationException, IllegalAccessException {
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
		
		// make sure the mappings are not null, because we are going to use them in recursive call
		if (clusterToOriginalCliqueMap == null) {
			clusterToOriginalCliqueMap = new HashMap<Clique, Collection<Clique>>();
		}
		if (cliqueToGeneratedClusterMap == null) {
			cliqueToGeneratedClusterMap = new HashMap<Clique, Clique>();
		}
		
		// instantiate junction tree to return, and recursively copy cliques and separators
		IJunctionTree ret = junctionTreeBuilder.buildJunctionTree(getNetwork());
		recursivelyFillMaxPrimeSubgraphDecompositionTree(originalJunctionTree, ret, root, clusterToOriginalCliqueMap, cliqueToGeneratedClusterMap);	// this shall also fill the mappings
		
		if (ret instanceof LoopyJunctionTree) {
			((LoopyJunctionTree) ret).initParentMapping();
		}
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
	 * @param cliqueToGeneratedClusterMap : this is an inverse mapping of clusterToOriginalCliqueMap.
	 * This is also an output argument.
	 * @param parentCliqueCreatedInPreviousCall : clique generated by previous recursive call (i.e. this clique will become the parent of cliques generated in current call).
	 * Set this to null if the current clique being visited is the root.
	 * This clique will be in the target junction tree (i.e. the junction tree to be filled).
	 * @return the root clique of the tree created by this recursive call. Null if nothing was created.
	 * @see Clique#join(Clique)
	 */
	private Clique recursivelyFillMaxPrimeSubgraphDecompositionTree(IJunctionTree junctionTreeToRead, IJunctionTree junctionTreeToFill, Clique currentCliqueToRead, 
			Map<Clique, Collection<Clique>> decompositionToJunctionTreeMap,
			Map<Clique, Clique> cliqueToGeneratedClusterMap) {
		
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
		
		// check if current clique was handled already (this can happen if we are using loopy clique structure)
		Clique currentClusterToFill = cliqueToGeneratedClusterMap.get(currentCliqueToRead);	
		if (currentClusterToFill == null) {	// if not mapped, then it was not handled yet
			// copy current clique (do not copy its potential table, though)
			currentClusterToFill = new StubClique(); // use a stub, which won't use potential tables
			currentClusterToFill.setIndex(currentCliqueToRead.getIndex());								  // just for backward compatibility
			currentClusterToFill.setInternalIdentificator(currentCliqueToRead.getInternalIdentificator()); // just for backward compatibility
			currentClusterToFill.setNodesList(new ArrayList<Node>(currentCliqueToRead.getNodesList()));	  // clone the list (so that we don't modify original list)
			currentClusterToFill.setParent(null);		// make sure this is initialized with null value
			
			decompositionToJunctionTreeMap.put(currentClusterToFill, new HashSet<Clique>());
			decompositionToJunctionTreeMap.get(currentClusterToFill).add(currentCliqueToRead);
			cliqueToGeneratedClusterMap.put(currentCliqueToRead, currentClusterToFill);
		} else {
			// no need to treat it again
			return currentClusterToFill;
		}
		
		
		
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
						decompositionToJunctionTreeMap,
						cliqueToGeneratedClusterMap
					);
				
				// update reference on each iteration, because the above recursive call may have updated the content if we are using loopy clique structure
				currentClusterToFill = cliqueToGeneratedClusterMap.get(currentCliqueToRead);
				
				// get the separator between the original parent and child
				Separator separatorToRead = junctionTreeToRead.getSeparator(currentCliqueToRead, childCliqueToRead);
				if (separatorToRead == null) {
					throw new IllegalArgumentException("Clique " + childCliqueToRead + " is a child of clique " + currentCliqueToRead + ", but no separator was found in between.");
				}
				
				// check if we shall merge clique
				if (!separatorToRead.isComplete()) { // needs to merge cliques
					
					// move children of the child (being merged) to my children (i.e. convert grand children to children)
					for (Clique grandChild : new ArrayList<Clique>(childCliqueToFill.getChildren())) {	// use a cloned list, because the content of original list may be changed in this loop
						// link current clique to/from grand child
						if (!junctionTreeToRead.isUsingApproximation()
								|| !currentClusterToFill.getChildren().contains(grandChild)) {
							// if we are not using approximation, insert immediately. 
							// If we are using approximation, but the clusters are not connected already, then connect them
							currentClusterToFill.addChild(grandChild); 	// add grandchild to my list of children
						}
						if (junctionTreeToFill instanceof LoopyJunctionTree) {
							LoopyJunctionTree loopyJunctionTree = (LoopyJunctionTree) junctionTreeToFill;
							loopyJunctionTree.removeParent(childCliqueToFill, grandChild);
							if (!loopyJunctionTree.getParents(grandChild).contains(currentClusterToFill)) {
								// avoid duplicate insertion
								loopyJunctionTree.addParent(currentClusterToFill, grandChild);
							}
						} else {
							grandChild.setParent(currentClusterToFill);	// set current clique as parent of grandchild
						}
						
						// prepare to replace separator. Extract the one to be replaced.
						Separator oldSeparator = junctionTreeToFill.getSeparator(childCliqueToFill, grandChild);	// the separtor to be replaced
						if (oldSeparator == null) {
							throw new IllegalArgumentException("Clique " + grandChild + " is expected to be a child of clique " + childCliqueToFill + ", but no separator was created in previous recursive call.");
						}
						
						// check if a separator between current clique and grand child already exists. This can happen if cliques are loopy
						Separator newSeparator = junctionTreeToFill.getSeparator(currentClusterToFill, grandChild);
						if (newSeparator != null) { // reuse existing one, because we don't want two separators connecting the same cliques
							if (!junctionTreeToRead.isUsingApproximation()) {
								// this is inconsistent, because this means there are two path to same clique
								throw new IllegalArgumentException("Separator " + newSeparator + " causes a duplicate path (loop), but the instance of " 
										+ IJunctionTree.class.getName() + " returns isUsingApproximation = false. This is inconsistent." );
							}
							// we are virtually merging two separators. Check which nodes need to be included to merged separator
							Set<Node> nodesNotInSeparatorToMerge = new HashSet<Node>(oldSeparator.getNodesList());
							nodesNotInSeparatorToMerge.removeAll(newSeparator.getNodesList());	// this now contains nodes in oldSeparator that are not in newSeparator
							newSeparator.getNodesList().addAll(nodesNotInSeparatorToMerge);
							// the internal identificator of the new separator should be different from the one we are removing right now
							// now, remove the old separator
							junctionTreeToFill.removeSeparator(oldSeparator);	// this may fail if there are duplicate separators with same internal identificators
						} else {
							// create new instance of separator, because unfortunately we cannot change existing separator
							newSeparator = new StubSeparator(currentClusterToFill, grandChild);	// new separator is from current clique to grandchild
							newSeparator.setInternalIdentificator(oldSeparator.getInternalIdentificator());		// just for backward compatibility
							newSeparator.setNodes(new ArrayList<Node>(oldSeparator.getNodes()));	// use a clone, just for precaution
							// now, remove the old separator
							junctionTreeToFill.removeSeparator(oldSeparator);
							// needs to remove the old separator before adding the new separator, because they use the same ID
							junctionTreeToFill.addSeparator(newSeparator);
						}
						
					} // end of for each grand children
					
					// in case of loopy clique structures, the child may also have parents which are not the current clique (a parent of my child may not be necessarily me)
					// so need to connect these parents to me, because the child will be deleted (merged) to me.
					if ((junctionTreeToRead instanceof LoopyJunctionTree)
							&& ((LoopyJunctionTree)junctionTreeToRead).isLoopy()	// original JT was loopy
							&& ((LoopyJunctionTree) junctionTreeToFill).getParents(childCliqueToFill).size() > 1) { // if there are parent clusters other than the currentClusterToFill, then we shall connect them
						
						for (Clique parentOfChildToFill : ((LoopyJunctionTree)junctionTreeToFill).getParents(childCliqueToFill)) {
							if (parentOfChildToFill.equals(currentClusterToFill)) {
								continue;	// iterate on all parents of my (currentClusterToFill's) child, but don't treat myself
							}
							
							// _if parent of merged cluster is antecessor of _new cluster, keep it an antecessor in order to avoid cycle;
							// one of the following will be the cluster to fill, and the other will be the parent of its child.
							Clique parentInSeparator = currentClusterToFill; 	// this will become a parent when the separator is created
							Clique childInSeparator = parentOfChildToFill; 		// this will become a child when the separator is created
							if (junctionTreeToFill.isPredecessor(parentOfChildToFill, currentClusterToFill)) {
								// invert the order if parentOfChildToFill is a predecessor already.
								parentInSeparator = parentOfChildToFill;		// this will become a parent when the separator is created
								childInSeparator = currentClusterToFill;		// this will become a child when the separator is created
							}
							
							// link the current cluster with the parent of its child
							if (!parentInSeparator.getChildren().contains(childInSeparator)) {
								parentInSeparator.addChild(childInSeparator); 	// this is either setting current cluster as a child of the parent of its child, or the inverse.
							}
							
							// if reached this point, they are expected to be an instance of loopy junction tree, so do a cast without a type check
							LoopyJunctionTree loopyJunctionTree = (LoopyJunctionTree) junctionTreeToFill;
							loopyJunctionTree.removeParent(parentOfChildToFill, childCliqueToFill);	// disconnect the child to its parent
							if (!loopyJunctionTree.getParents(childInSeparator).contains(parentInSeparator)) {
								// avoid duplicate insertion
								loopyJunctionTree.addParent(parentInSeparator, childInSeparator);
							}

							// extract the separator to be replaced 
							Separator oldSeparator = junctionTreeToFill.getSeparator(parentOfChildToFill, childCliqueToFill);	// the separtor to be replaced
							if (oldSeparator == null) {
								throw new IllegalArgumentException("Clique " + parentOfChildToFill + " is expected to be a parent of clique " + childCliqueToFill + ", but no separator was created in previous recursive call.");
							}
							
							
							// check if a separator between current clique and parent of its child already exists. This can happen if cliques are loopy
							Separator newSeparator = junctionTreeToFill.getSeparator(parentInSeparator, childInSeparator);
							if (newSeparator != null) { // reuse existing one, because we don't want two separators connecting the same cliques
								// we are virtually merging two separators. Check which nodes need to be included to merged separator
								Set<Node> nodesNotInSeparatorToMerge = new HashSet<Node>(oldSeparator.getNodesList());
								nodesNotInSeparatorToMerge.removeAll(newSeparator.getNodesList());	// this now contains nodes in oldSeparator that are not in newSeparator
								newSeparator.getNodesList().addAll(nodesNotInSeparatorToMerge);
								// now, remove the old separator
								junctionTreeToFill.removeSeparator(oldSeparator);
							} else {
								// create new instance of separator
								newSeparator = new StubSeparator(parentInSeparator, childInSeparator);	
								newSeparator.setInternalIdentificator(oldSeparator.getInternalIdentificator());		// just for backward compatibility
								newSeparator.setNodes(new ArrayList<Node>(oldSeparator.getNodes()));	// use a clone, just for precaution
								// now, remove the old separator before adding the new one, because we made them to have the same ids
								junctionTreeToFill.removeSeparator(oldSeparator);
								// finally, add the new separator
								junctionTreeToFill.addSeparator(newSeparator);
							}
						
						}	// end of for each parent of my child
						
					}
					
					
					// no need to disassociate child clique from current clique, because we never associated them anyway (they are only associated in the else clause below)
					
					// we are going to merge child to current clique, so move all mapping of the child to the mapping of current clique
					decompositionToJunctionTreeMap.get(currentClusterToFill).addAll(decompositionToJunctionTreeMap.get(childCliqueToFill));
					for (Clique originalCliques : decompositionToJunctionTreeMap.get(childCliqueToFill)) {
						cliqueToGeneratedClusterMap.put(originalCliques, currentClusterToFill);
					}
					
					decompositionToJunctionTreeMap.remove(childCliqueToFill);	// remove child from mapping
					
					// merge child to current clique (the current clique will become a large clique containing nodes from both cliques)
					currentClusterToFill.join(childCliqueToFill);
					
					// also needs to remove the joined child from the junction tree being filled, because the recursive call has inserted it in the junction tree
					if (junctionTreeToFill instanceof LoopyJunctionTree) {
						((LoopyJunctionTree) junctionTreeToFill).clearParents(childCliqueToFill);
					} else {
						childCliqueToFill.setParent(null);
					}
					childCliqueToFill.getChildren().clear();
					
					// argument false means: don't update internal indexes and ids (because they must be kept synchronized with original jt)
					junctionTreeToFill.removeCliques(Collections.singletonList(childCliqueToFill), false); 
					
				} else {	// no need to merge cliques
					// Check if a separator exists. This can happen when the cliques are loopy.
					Separator newSeparator = junctionTreeToFill.getSeparator(currentClusterToFill, childCliqueToFill);
					if (newSeparator == null) {
						// create separator between the current clique and child clique
						newSeparator = new StubSeparator(currentClusterToFill, childCliqueToFill);	// new separator is from current clique to grandchild
						newSeparator.setInternalIdentificator(separatorToRead.getInternalIdentificator());		// just for backward compatibility
						newSeparator.setNodes(new ArrayList<Node>(separatorToRead.getNodes()));	// use a clone, just for precaution
						
						// add separator to the junction tree being filled
						junctionTreeToFill.addSeparator(newSeparator);
						
						// creating new separators won't automatically associate the current clique with child clique, so associate them
						currentClusterToFill.addChild(childCliqueToFill);
						if (junctionTreeToFill instanceof LoopyJunctionTree) {
							((LoopyJunctionTree) junctionTreeToFill).addParent(currentClusterToFill, childCliqueToFill);
						} else {
							childCliqueToFill.setParent(currentClusterToFill);
						}
					} else if (!junctionTreeToRead.isUsingApproximation()) {
						// this is inconsistent, because this means there are two path to same clique
						throw new IllegalArgumentException("Separator " + newSeparator + " causes a duplicate path (loop), but the instance of " 
								+ IJunctionTree.class.getName() + " returns isUsingApproximation = false. This is inconsistent." );
					} // else we don't need to add new separator again
				}
				
			}
		}
		
		// include copied clique to target junction tree
		junctionTreeToFill.getCliques().add(currentClusterToFill);
		
		// return the generated clique
		return currentClusterToFill;
	}
	
	
	

	/**
	 * @return it will just delegate to {@link LoopyJunctionTree#isLoopy()}.
	 */
	public boolean isLoopy() {
		IJunctionTree jt = getJunctionTree();
		if ((jt != null)
				&& (jt instanceof LoopyJunctionTree)) {
			return ((LoopyJunctionTree) jt).isLoopy();
		}
		// if it is not associated with a LoopyJunctionTree, then by default consider that this doesn't have loops
		return false;
	}

	/**
	 * @param isLoopy : 
	 * if {@link #isLoopy()} was false and this is set to true, then {@link #buildLoopyCliques(ProbabilisticNetwork)} will
	 * be called in order to force the {@link #getJunctionTree()} to be loopy, and thus use loopy BP.
	 * <br/>
	 * if {@link #isLoopy()} was true and this is set to false, then {@link #run()} will be called with loopy BP and incremental JT compilation
	 * disabled.
	 * <br/>
	 * Otherwise, it will just delegate to {@link LoopyJunctionTree#setLoopy(boolean)}.
	 * 
	 * @see #getLargestCliques(ProbabilisticNetwork)
	 * @see #updateCPTBasedOnCliques()
	 * @see #setNetPreviousRun(ProbabilisticNetwork)
	 * @see #getLoopyBPCliqueSizeThreshold()
	 * @see #setLoopyBPCliqueSizeThreshold(int)
	 * 
	 * @throws ClassCastException if {@link #getJunctionTree()} is not an instance of {@link LoopyJunctionTree}.
	 */
	public void setLoopy(boolean isLoopy) {
//		if (!isLoopy() && isLoopy) {	// changing from non-loopy to loopy
////			// force loopy BP if clique size got too large
////			if (isConditionForClusterLoopyBPSatisfied(getNet())) {
////				if (isToUseAgressiveLoopyBP()) {
////					this.buildLoopyCliques(getNet());
////				} else {
////					this.setNetPreviousRun(null);	// this will ensure no cache will be used in this compilation (thus, dynamic JT compilation will not run)
////					this.run();
////				}
////			}
//			// make sure we use CPTs that are up-to-date (accordingly to clique potentials)
//			updateCPTBasedOnCliques();
//			this.setNetPreviousRun(null);	// this will ensure no cache will be used in this compilation (thus, dynamic JT compilation will not run)
//			this.run();
//		} else if (isLoopy() && !isLoopy) {	// changing from loopy to non-loopy
//			
//			// make sure we use CPTs that are up-to-date (accordingly to clique potentials)
//			updateCPTBasedOnCliques();
//			
//			// disable dynamic JT compilation
//			setNetPreviousRun(null);
//			
//			int loopyBPCliqueSizeThresholdBakup = this.getLoopyBPCliqueSizeThreshold();	// keep backup, in order to restore later
//			// disable loopy BP
//			this.setLoopyBPCliqueSizeThreshold(Integer.MAX_VALUE);
//			
//			// recompile JT
//			this.run();
//			
//			// restore backup
//			this.setLoopyBPCliqueSizeThreshold(loopyBPCliqueSizeThresholdBakup);
//		}
		
		if (isLoopy() == isLoopy) {
			// there were no change in configuration, so don't do anything
			return;
		}
		
		// just recompile network, instead of doing the above check (because isConditionForClusterLoopyBPSatisfied(getNet()) recompiles the JT anyway, so it's better than compiling twice)
		// make sure we use CPTs that are up-to-date (accordingly to clique potentials)
		updateCPTBasedOnCliques();
		
		// disable dynamic JT compilation
		setNetPreviousRun(null);
		
		int loopyBPCliqueSizeThresholdBakup = this.getLoopyBPCliqueSizeThreshold();	// keep backup, in order to restore later
		
		// disable loopy BP if changing from loopy to non-loopy
		if (!isLoopy) {
			this.setLoopyBPCliqueSizeThreshold(Integer.MAX_VALUE);
		}
		
		// recompile JT
		this.run();
		
		// restore backup
		this.setLoopyBPCliqueSizeThreshold(loopyBPCliqueSizeThresholdBakup);
		
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#setNet(unbbayes.prs.bn.ProbabilisticNetwork)
	 */
	public void setNet(ProbabilisticNetwork net) {
		super.setNet(net);
		net.setJunctionTreeBuilder(DEFAULT_LOOPY_JT_BUILDER);
	}

	/**
	 * @return if the max clique size of the junction tree is above this threshold, then
	 * {@link #run()} or {@link #runDynamicJunctionTreeCompilation()} will
	 * invoke {@link #buildLoopyCliques()} in order to start using approximation by loopy belief propagation.
	 */
	public int getLoopyBPCliqueSizeThreshold() {
		return loopyBPCliqueSizeThreshold;
	}

	/**
	 * @param loopyBPCliqueSizeThreshold the loopyBPCliqueSizeThreshold to set.
	 * If the max clique size of the junction tree is above this threshold, then
	 * {@link #run()} or {@link #runDynamicJunctionTreeCompilation()} will
	 * invoke {@link #buildLoopyCliques()} in order to start using approximation by loopy belief propagation.
	 */
	public void setLoopyBPCliqueSizeThreshold(int loopyBPCliqueSizeThreshold) {
		this.loopyBPCliqueSizeThreshold = loopyBPCliqueSizeThreshold;
	}

	/**
	 * @return the cliqueSplitter used in {@link #splitCliqueAndAddToJT(Clique, LoopyJunctionTree, int)} in order
	 * to generate cliques with desired size.
	 * @see #SINGLE_CYCLE_HALF_SIZE_SEPARATOR_CLIQUE_SPLITTER
	 * @see #setCliqueSplitter(ICliqueSplitter)
	 */
	public ICliqueSplitter getCliqueSplitter() {
		return cliqueSplitter;
	}

	/**
	 * @param cliqueSplitter : used in {@link #splitCliqueAndAddToJT(Clique, LoopyJunctionTree, int)} in order
	 * to generate cliques with desired size. Change this object in order to customize the way 
	 * small cliques are generated from larger ones.
	 * @see #SINGLE_CYCLE_HALF_SIZE_SEPARATOR_CLIQUE_SPLITTER
	 * @see #getCliqueSplitter()
	 */
	public void setCliqueSplitter(ICliqueSplitter cliqueSplitter) {
		this.cliqueSplitter = cliqueSplitter;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#setAsPermanentEvidence(java.util.Map, boolean)
	 */
	public void setAsPermanentEvidence(Map<INode, List<Float>> evidences, boolean isToDeleteNode) {
		super.setAsPermanentEvidence(evidences, isToDeleteNode);
		
		// if we are using loopy BP, check condition to return to exact inference
		returnToExactJunctionTree();
	}

	/**
	 * Check for conditions to return to exact inference (i.e. non-loopy bp in junction tree)
	 * and if conditions are satisfied, disable loopy BP and return to ordinal junction tree.
	 * @return true if there were changes. False otherwise.
	 * @see #isLoopy()
	 * @see #setLoopy(boolean)
	 */
	public boolean returnToExactJunctionTree() {
		if (!isLoopy()) {
			// do nothing if we are not doing loopy BP
			return false;
		}
		
		// other basic assertions
		if (getNet() == null || getNet().getJunctionTree() == null) {
			return false;
		}
		
		// check for conditions to stop loopy-BP. The conditions are the inverse of starting loopy-BP
		ProbabilisticNetwork netToCheck = this.cloneProbabilisticNetwork(getNet());	// use a clone, so that we don't change original
		
		// compile the junction tree without using incremental JT, but potentially using loopy JT
		IncrementalJunctionTreeAlgorithm algorithm = new IncrementalJunctionTreeAlgorithm(netToCheck);
		algorithm.setDynamicJunctionTreeNetSizeThreshold(Integer.MAX_VALUE);		// this will ensure dynamic JT is never used
		algorithm.setNetPreviousRun(null);											// this will also ensyre dynamic JT is not used
		if (isToUseAgressiveLoopyBP()) {
			algorithm.setLoopyBPCliqueSizeThreshold(Integer.MAX_VALUE);					// disable loopy JT structure if we want to use aggressive loopy BP
		} else {
			algorithm.setLoopyBPCliqueSizeThreshold(getLoopyBPCliqueSizeThreshold());	// reuse the same configuration
		}
		algorithm.run();
		
		// check conditions to use loopy BP in cluster structure
		if (!algorithm.isLoopy() 	// if it got loopy yet, then we did not satisfy condition
//				&& !isConditionForClusterLoopyBPSatisfied(algorithm.getNet())  // just to double-check
					) {	
			// TODO compiling a clone to check clique size, and then using setLoopy(false) to recompile exact JT is redundant
			
			// setting isLoopy from true to false this should recompile junction tree in non-loopy and non-incremental mode
			this.setLoopy(false);
			
			return true;	// indicate that there were changes
		} else {
			Debug.println(getClass(), "Unable to compile junction tree without using loopy or incremental JT");
		}
		
		// if we reached this point, then there were no changes in original junction tree.
		return false;
	}

	/** 
	 * Overwrites the method in superclass in order to generate/handle multiple cliques/separators with loops
	 * when using conditional soft/likelihood evidences with too many conditions. 
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#createCliqueAndSeparatorForVirtualNode(unbbayes.prs.bn.ProbabilisticNode, java.util.List, unbbayes.prs.bn.SingleEntityNetwork)
	 */
	protected Collection<IRandomVariable> createCliqueAndSeparatorForVirtualNode( ProbabilisticNode virtualNode, List<INode> parentNodes, SingleEntityNetwork net) {
		// basic assertion
		if (parentNodes == null || parentNodes.isEmpty() || net == null || net.getJunctionTree() == null || virtualNode == null) {
			Debug.println(getClass(), "Found null or empty argument when creating cliques and separators for virtual nodes in soft/likelihood evidences.");
			return Collections.EMPTY_LIST;
		}
		
		// check if we are using loopy BP
		LoopyJunctionTree junctionTree = null;
		try {
			// extract the instance of loopy JT (used in loopy BP)
			junctionTree = (LoopyJunctionTree)net.getJunctionTree();
		} catch (ClassCastException e) {
			// call superclass if we are not even using instance of loopy JT
			Debug.println(getClass(), "Not using instance of LoopyJunctionTree. Calling superclass method to build cliques and separators for virtual node.", e);
			return super.createCliqueAndSeparatorForVirtualNode(virtualNode, parentNodes, net);
		}
		
		
		// if there is a clique containing all nodes, or we are not using loopy BP, then we can use the superclass method too
		List<Clique> cliquesContainingAllNodes = junctionTree.getCliquesContainingAllNodes(parentNodes, 1); // just check if there is at least 1 clique satisfying condition
		if (!junctionTree.isLoopy()
				|| (cliquesContainingAllNodes != null && !cliquesContainingAllNodes.isEmpty())) {
			// call superclass if this is not loopy BP
			Debug.println(getClass(), "Not using loopy BP. Creating cliques/separators with virtual node normally.");
			Collection<IRandomVariable> ret = super.createCliqueAndSeparatorForVirtualNode(virtualNode, parentNodes, net);
			
			// extract the new cliques from ret and attempt to synchronize mapping of parents
			for (IRandomVariable rv : ret) {
				if (rv instanceof Clique) {
					Clique clique = (Clique) rv;
					if (clique.getParent() != null) {
						List<Clique> parents = junctionTree.getParents(clique);
						if (parents == null || parents.isEmpty()) {
							// clique.getParent() and unctionTree.getParents(clique) were inconsistent. Make them consistent
							junctionTree.addParent(clique.getParent(), clique);
						}
					}
					
				}
			}
			return ret;
		}
		
		
		// now, we have a special situation: we are using loopy BP, and there is no cluster containing all the nodes in the evidence.
		// So, we cannot create a clique with all the nodes, and such new clique to be connected to a single clique in original JT
		// we need approximation in the new clique (to be created for soft evidence) too.
		// the new clique will be connected to multiple cliques in original JT, and it will also be split accordingly to size limitation.
		// TODO instead of creating a big clique and then splitting it, create small cliques from scratch
		Debug.println(getClass(), "Using loopyBP and unable to find cluster with " + parentNodes + ". Distributing the finding across different clusters instead.");
		
		// just to guarantee that the network is fresh
		net.resetNodesCopy();
		
		//create a clique containing all nodes for this soft/virtual evidence;
		Clique cliqueOfVirtualNode = new Clique();
		cliqueOfVirtualNode.getNodesList().add(virtualNode);
		cliqueOfVirtualNode.getNodesList().addAll((List)parentNodes);
		cliqueOfVirtualNode.getProbabilityFunction().addVariable(virtualNode);
		for (INode parentNode : parentNodes) {
			cliqueOfVirtualNode.getProbabilityFunction().addVariable(parentNode);
		}
		cliqueOfVirtualNode.setInternalIdentificator(junctionTree.getCliques().size());
		
		// add clique to junction tree, so that the algorithm can handle the clique correctly
		junctionTree.getCliques().add(cliqueOfVirtualNode);
		
		// now, let's link the nodes with the cliques
		cliqueOfVirtualNode.getAssociatedProbabilisticNodes().add(virtualNode);
		virtualNode.setAssociatedClique(cliqueOfVirtualNode);
		
		// initialize the probabilities of clique
		junctionTree.initBelief(cliqueOfVirtualNode);
		
		cliqueOfVirtualNode.getProbabilityFunction().copyData(); // store the potentials, so that the "reset" will restore these values
		
		// extract cliques sharing any common variable with the new clique;
		Set<Clique> setWithCommonNodes = new HashSet<Clique>();	// using a set, just in order to avoid duplicates
		for (INode node : parentNodes) {
			setWithCommonNodes.addAll(junctionTree.getCliquesContainingAllNodes(Collections.singletonList(node), Integer.MAX_VALUE));
		}
		
		
		// artificially create separators between the new clique and all cliques in junction tree sharing any common variable with the new clique;
		List<Separator> separatorsCreated = new ArrayList<Separator>();
		for (Clique cliqueSharingNodes : setWithCommonNodes) {
			// create separator between the clique of parent nodes and virtual node (the separator should contain all parents)
			Separator separatorOfVirtualCliqueAndParents = new Separator(cliqueSharingNodes , cliqueOfVirtualNode, null, null, false);
			ArrayList<Node> nodesInSeparator = new ArrayList<Node>((List)parentNodes);	// parentNodes supposedly contains all nodes in cliqueOfVirtualNode but the virtual node
			nodesInSeparator.retainAll(cliqueSharingNodes.getNodesList());	// only use common variables between the cliques
			separatorOfVirtualCliqueAndParents.setNodes(nodesInSeparator);
			// update references to/from parent nodes
			junctionTree.addParent(cliqueSharingNodes, cliqueOfVirtualNode);
			cliqueSharingNodes.addChild(cliqueOfVirtualNode);
			// add nodes to separator table
			for (INode node : nodesInSeparator) {
				separatorOfVirtualCliqueAndParents.getProbabilityFunction().addVariable(node);
			}
			// include separator into junction tree
			junctionTree.addSeparator(separatorOfVirtualCliqueAndParents);
			separatorOfVirtualCliqueAndParents.setInternalIdentificator(-(junctionTree.getSeparators().size()+1));
			
			junctionTree.initBelief(separatorOfVirtualCliqueAndParents);	// this one sets all separator potentials to 1
			separatorOfVirtualCliqueAndParents.getProbabilityFunction().copyData(); // store the potentials, so that the "reset" will restore these values
			
			separatorsCreated.add(separatorOfVirtualCliqueAndParents);
		}
		
		// split the clique we just created if necessary. 
		if (isToSplitVirtualNodeClique()
				&& isToUseAgressiveLoopyBP()
				&& cliqueOfVirtualNode.getProbabilityFunction().tableSize()/2 > getLoopyBPCliqueSizeThreshold()) {	// /2 because I don't want to consider the virtual node -- with 2 states
			// This supposedly deletes the cliques and separators we created previously, so just return the new cliques/separtors;
			return this.splitCliqueAndAddToJT(cliqueOfVirtualNode, junctionTree, getLoopyBPCliqueSizeThreshold());
		} 
		
		
		// return the cliques and separators created without splitting the large clique;
		List<IRandomVariable> ret = new ArrayList<IRandomVariable>(separatorsCreated.size() + 1);	// size of list to return is no. of separators + the cliqueOfVirtualNode 
		ret.add(cliqueOfVirtualNode);
		ret.addAll(separatorsCreated);
		
		return ret;
	}
	
	

	/**
	 * @return the error margin which will be used when comparing probabilities in loopy BP.
	 * If a loop does not result in change in clique potential larger than this value, then a loop will stop.
	 * @see LoopyJunctionTree#getProbErrorMargin()
	 * @see LoopyJunctionTree#consistency(Clique, boolean)
	 * @see LoopyJunctionTree#absorb(Clique, Clique)
	 */
	public float getProbErrorMargin() {
		try {
			return ((LoopyJunctionTree)getJunctionTree()).getProbErrorMargin();
		} catch (ClassCastException e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		return Float.MIN_NORMAL;
	}

	/**
	 * @param probErrorMargin : the error margin which will be used when comparing probabilities in loopy BP.
	 * If a loop does not result in change in clique potential larger than this value, then a loop will stop.
	 * @see LoopyJunctionTree#setProbErrorMargin(float)
	 * @see LoopyJunctionTree#consistency(Clique, boolean)
	 * @see LoopyJunctionTree#absorb(Clique, Clique)
	 */
	public void setProbErrorMargin(float probErrorMargin) {
		try {
			((LoopyJunctionTree)getJunctionTree()).setProbErrorMargin(probErrorMargin);
		} catch (ClassCastException e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
	}


	/**
	 * @return the max number of iterations the loopy belief propagation in
	 * {@link LoopyJunctionTree#consistency(Clique, boolean)} will execute.
	 */
	public int getMaxLoopyBPIteration() {
		try {
			return ((LoopyJunctionTree)getJunctionTree()).getMaxLoopyBPIteration();
		} catch (ClassCastException e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		return 1;
	}

	/**
	 * @param maxLoopyBPIteration : the max number of iterations the loopy belief propagation in
	 * {@link LoopyJunctionTree#consistency(Clique, boolean)} will execute.
	 */
	public void setMaxLoopyBPIteration(int maxLoopyBPIteration) {
		try {
			((LoopyJunctionTree)getJunctionTree()).setMaxLoopyBPIteration(maxLoopyBPIteration);
		} catch (ClassCastException e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
	}
	
	/**
	 * @return the time in milliseconds allowed for {@link LoopyJunctionTree#consistency(Clique, boolean)}
	 * to run iterations in loopy Belief Propagation when {@link LoopyJunctionTree#isLoopy()} == true.
	 * After an iteration (a collect/propagate evidence loop) finishes, the time will be checked, 
	 * and the loopy BP will stop if the time exceeded this amount.
	 */
	public long getMaxLoopyBPTimeMillis() {
		try {
			return ((LoopyJunctionTree)getJunctionTree()).getMaxLoopyBPTimeMillis();
		} catch (ClassCastException e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		return Long.MAX_VALUE;
	}

	/**
	 * @param maxLoopyBPTimeMillis : the time in milliseconds allowed for {@link LoopyJunctionTree#consistency(Clique, boolean)}
	 * to run iterations in loopy Belief Propagation when {@link LoopyJunctionTree#isLoopy()} == true.
	 * After an iteration (a collect/propagate evidence loop) finishes, the time will be checked, 
	 * and the loopy BP will stop if the time exceeded this amount.
	 */
	public void setMaxLoopyBPTimeMillis(long maxLoopyBPTimeMillis) {
		try {
			((LoopyJunctionTree)getJunctionTree()).setMaxLoopyBPTimeMillis(maxLoopyBPTimeMillis);
		} catch (ClassCastException e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
	}

	/**
	 * @return the isToSplitVirtualNodeClique.
	 * If true, {@link #createCliqueAndSeparatorForVirtualNode(ProbabilisticNode, List, SingleEntityNetwork)} will
	 * also attempt to split cliques that were temporary created for soft evidence.
	 */
	public boolean isToSplitVirtualNodeClique() {
		return isToSplitVirtualNodeClique;
	}

	/**
	 * @param isToSplitVirtualNodeClique the isToSplitVirtualNodeClique to set.
	 * If true, {@link #createCliqueAndSeparatorForVirtualNode(ProbabilisticNode, List, SingleEntityNetwork)} will
	 * also attempt to split cliques that were temporary created for soft evidence.
	 */
	public void setToSplitVirtualNodeClique(boolean isToSplitVirtualNodeClique) {
		this.isToSplitVirtualNodeClique = isToSplitVirtualNodeClique;
	}

	/**
	 * This method extends superclass in order to implement soft/likelihood evidences without creating virtual nodes,
	 * if {@link #isToCreateVirtualNode()} is false.
	 * @see unbbayes.prs.bn.JunctionTreeAlgorithm#addVirtualNode(unbbayes.prs.Graph, java.util.List)
	 */
	public INode addVirtualNode(Graph graph, List<INode> parentNodes) throws Exception {
		if (isToCreateVirtualNode()) {
			return super.addVirtualNode(graph, parentNodes);
		}

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
		
		// init potential table to be multiplied with clique table
		PotentialTable tableForMultiplication = new ProbabilisticTable();
		
		// add variables to table
		for (INode parentNode : parentNodes) {
			tableForMultiplication.addVariable(parentNode);
		}
		
		// fill content of table based on likelihood
		tableForMultiplication.setValues(likelihood);
		tableForMultiplication.normalize();	// make sure this is normalized
		
		// prepare junction tree so that we can manipulate cliques
		IJunctionTree junctionTree = net.getJunctionTree();
		
		// find the smallest clique containing all the parents
		int smallestSize = Integer.MAX_VALUE;
		Clique smallestCliqueContainingAllParents = null;
		for (Clique clique : junctionTree.getCliquesContainingAllNodes(parentNodes, Integer.MAX_VALUE)) {
			if (clique.getProbabilityFunction().tableSize() < smallestSize) {
				smallestCliqueContainingAllParents = clique;
				smallestSize = clique.getProbabilityFunction().tableSize();
			}
		}
		
		// if could not find smallest clique, the arguments are inconsistent
		if (smallestCliqueContainingAllParents == null) {
			throw new IllegalArgumentException(getResource().getString("noCliqueForNodes") + parentNodes);
		}
		
		// extract the clique potential, so that we can multiply it with the table we created in this method
		PotentialTable cliqueTable = smallestCliqueContainingAllParents.getProbabilityFunction();
		
		// multiply table
		cliqueTable.opTab(tableForMultiplication, PotentialTable.PRODUCT_OPERATOR);
		
		// make sure table after operation is normalized
		cliqueTable.normalize();
		// TODO check if we really need to normalize, because JunctionTree#collectEvidence is supposedly doing it
		
		return null;
	}

	/**
	 * @return true if {@link #addVirtualNode(Graph, List)} will create virtual nodes for soft/likelihood evidences.
	 * False if such evidences will be inserted directly to cliques without using virtual nodes.
	 */
	public boolean isToCreateVirtualNode() {
		return isToCreateVirtualNode;
	}

	/**
	 * @param isToCreateVirtualNode : true if {@link #addVirtualNode(Graph, List)} will create virtual nodes for soft/likelihood evidences.
	 * False if such evidences will be inserted directly to cliques without using virtual nodes
	 */
	public void setToCreateVirtualNode(boolean isToCreateVirtualNode) {
		this.isToCreateVirtualNode = isToCreateVirtualNode;
	}

	/**
	 * @return true if {@link #run()} must disable dynamic JT compilation when {@link LoopyJunctionTree#isLoopy()} is true. False otherwise.
	 */
	public boolean isToCompileNormallyWhenLoopy() {
		return isToCompileNormallyWhenLoopy;
	}

	/**
	 * @param isToCompileNormallyWhenLoopy : set to true if {@link #run()} must disable dynamic JT compilation when 
	 * {@link LoopyJunctionTree#isLoopy()} is true. Set to false otherwise.
	 */
	public void setToCompileNormallyWhenLoopy(boolean isToCompileNormallyWhenLoopy) {
		this.isToCompileNormallyWhenLoopy = isToCompileNormallyWhenLoopy;
	}

	/**
	 * @return If true, the loopy BP algorithm and JT compilation does not guarantee that a node and all its parents will be in the same clique.
	 * If false, then only cliques resulting from new chords added during triangulation will be potentially split.
	 * @see #splitCliqueAndAddToJT(Clique, LoopyJunctionTree, int)
	 * @see #addChordAndEliminateNode(Node, List, ProbabilisticNetwork)
	 * @see #run()
	 * @see #returnToExactJunctionTree()
	 * @see #isConditionForClusterLoopyBPSatisfied(ProbabilisticNetwork)
	 * @see #buildLoopyCliques(ProbabilisticNetwork)
	 * @see #strongTreeMethod(ProbabilisticNetwork, List, IJunctionTree)
	 */
	public boolean isToUseAgressiveLoopyBP() {
		return isToUseAgressiveLoopyBP;
	}

	/**
	 * @param isToUseAgressiveLoopyBP : If true, the loopy BP algorithm and JT compilation does not guarantee that a node and all its parents will be in the same clique.
	 * If false, then only cliques resulting from new chords added during triangulation will be potentially split.
	 * @see #splitCliqueAndAddToJT(Clique, LoopyJunctionTree, int)
	 * @see #addChordAndEliminateNode(Node, List, ProbabilisticNetwork)
	 * @see #run()
	 * @see #returnToExactJunctionTree()
	 * @see #isConditionForClusterLoopyBPSatisfied(ProbabilisticNetwork)
	 * @see #buildLoopyCliques(ProbabilisticNetwork)
	 * @see #strongTreeMethod(ProbabilisticNetwork, List, IJunctionTree)
	 */
	public void setToUseAgressiveLoopyBP(boolean isToUseAgressiveLoopyBP) {
		this.isToUseAgressiveLoopyBP = isToUseAgressiveLoopyBP;
	}

	/**
	 * @return the isToCompileNormallyWhenLoopyAndDeletingArcs : if true, then {@link #runDynamicJunctionTreeCompilation()} will
	 * compile the junction tree normally without using incremental junction tree compilation if
	 * {@link IJunctionTree#isUsingApproximation()} and the changes are only arc removals. If false, then
	 * {@link #runDynamicJunctionTreeCompilation()} will run incremental junction tree compilation always.
	 */
	public boolean isToCompileNormallyWhenLoopyAndDeletingArcs() {
		return isToCompileNormallyWhenLoopyAndDeletingArcs;
	}

	/**
	 * @param isToCompileNormallyWhenLoopyAndDeletingArcs the isToCompileNormallyWhenLoopyAndDeletingArcs to set.
	 * If true, then {@link #runDynamicJunctionTreeCompilation()} will
	 * compile the junction tree normally without using incremental junction tree compilation if
	 * {@link IJunctionTree#isUsingApproximation()} and the changes are only arc removals. If false, then
	 * {@link #runDynamicJunctionTreeCompilation()} will run incremental junction tree compilation always.
	 */
	public void setToCompileNormallyWhenLoopyAndDeletingArcs(
			boolean isToCompileNormallyWhenLoopyAndDeletingArcs) {
		this.isToCompileNormallyWhenLoopyAndDeletingArcs = isToCompileNormallyWhenLoopyAndDeletingArcs;
	}



}
