/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import unbbayes.prs.Edge;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.AssetNetwork;
import unbbayes.prs.bn.AssetNode;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.IJunctionTree;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.Separator;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.cpt.impl.UniformTableFunction;
import unbbayes.prs.exception.InvalidParentException;


/**
 * This class contains methods in common between {@link AssetAwareInferenceAlgorithm} and
 * {@link AssetPropagationInferenceAlgorithm}
 * @author Shou Matsumoto
 *
 */
public abstract class AbstractAssetNetAlgorithm extends JunctionTreeLPEAlgorithm implements IAssetNetAlgorithm {

//	/** This is the default object for {@link #getCptNormalizer()} */
//	public static final ITableFunction DEFAULT_CPT_NORMALIZER = new NormalizeTableFunction();
//	
//	private ITableFunction cptNormalizer = DEFAULT_CPT_NORMALIZER;

	public AbstractAssetNetAlgorithm() {
		super();
	}

	public AbstractAssetNetAlgorithm(ProbabilisticNetwork net) {
		super(net);
	}
	
	
	/**
	 * This method simply uses {@link #findJunctionTreePath(Clique, Clique)} and then removes
	 * the few initial and last cliques if they also contains the nodes respectively from "from" or "to" lists.
	 * In other words, it finds a path between any clique containing "from" to any clique containing "to",
	 * and then remove redundant cliques in the path (because other cliques near the beginning or near the end can also 
	 * contain nodes respectively in "from" or "to"). In a tree structure, the path after
	 * removing the "redundant" cliques is supposedly the shortest.
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#findShortestJunctionTreePath(java.util.Collection, java.util.Collection)
	 */
	public List<Clique> findShortestJunctionTreePath(Collection<INode> from, Collection<INode> to) {
		// first, check presence of junction tree and cliques
		if (getNet() == null || getNet().getJunctionTree() == null 
				|| getNet().getJunctionTree().getCliques() == null
				|| getNet().getJunctionTree().getCliques().isEmpty()) {
			// NOTE: AssetAwareInferenceAlgorithm#getNet() is supposedly the probabilistic network, 
			// and AssetPropagationInferenceAlgorithm#getNet() is supposedly the asset net
			return null;
		}
		// if the arguments are null or empty, return null
		if (from == null || to == null || from.isEmpty() || to.isEmpty()) {
			return null;
		}
		
		// if there is a clique containing all nodes, then simply return it
		Collection<INode> fromAndTo = new ArrayList<INode>(from);
		fromAndTo.addAll(to);
		// find clique containing all nodes
		List<Clique> cliquesContainingAllNodes = getNet().getJunctionTree().getCliquesContainingAllNodes(fromAndTo, Integer.MAX_VALUE);
		if (cliquesContainingAllNodes != null && !cliquesContainingAllNodes.isEmpty()) {
			// there are cliques containing all nodes. Return the smallest one.
			Clique smallest = cliquesContainingAllNodes.get(0);
			for (int i = 1; i < cliquesContainingAllNodes.size(); i++) {
				if (cliquesContainingAllNodes.get(i).getNodesList().size() < smallest.getNodesList().size()) {
					smallest = cliquesContainingAllNodes.get(i);
				}
			}
			return Collections.singletonList(smallest);
		} else {
			// dispose the collections which will not be used anymore
			fromAndTo = null;
			cliquesContainingAllNodes = null;
			// let the garbage collector do the rest
		}
		
		// extract any clique containing from
		List<Clique> cliqueFrom = getNet().getJunctionTree().getCliquesContainingAllNodes(from, 1);
		if (cliqueFrom == null || cliqueFrom.isEmpty()) {
			return null;	// there is no clique containing all of these nodes simultaneously
		}
		// extract any clique containing to
		List<Clique> cliqueTo = getNet().getJunctionTree().getCliquesContainingAllNodes(to, 1);
		if (cliqueTo == null || cliqueTo.isEmpty()) {
			return null;	// there is no clique containing all of these nodes simultaneously
		}
		
		// at this point, cliqueFrom and cliqueTo are not empty
		
		// get the path (potentially with "redundant" cliques)
		List<Clique> path = this.findJunctionTreePath(cliqueFrom.get(0), cliqueTo.get(0));
		
		// truncate the path accordingly to presence of "redundant" cliques
		if (path != null) {
			// remove cliques containing from (potentially at the beginning of the path)
			while (!path.isEmpty() && path.get(0).getNodesList().containsAll(from)) {
				// remove clique from path and also remember what was the clique closest to the end
				cliqueFrom.set(0, path.remove(0));
			}
			// remove cliques containing to (potentially at the end of the path)
			while (!path.isEmpty() && path.get(path.size()-1).getNodesList().containsAll(to)) {
				// remove clique from path and also remember what was the clique closest to the beginnning
				cliqueTo.set(0, path.remove(path.size()-1));
			}
		}
		
		// at this point, path does not contain cliques containing from or to, 
		// cliqueFrom.get(0) contains the clique containing "from" nearest to the clique containing "to",
		// and cliqueTo.get(0) contains the clique containing "to" nearest to the clique containing "from".
		
		// make sure the path contains cliqueFrom.get(0) and cliqueTo.get(0)
		try {
			path.add(0, cliqueFrom.get(0));
			path.add(cliqueTo.get(0));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return path;
	}
	

	/**
	 * Finds the shortest path between two cliques in a probabilistic junction tree.
	 * @param from : the clique to start from
	 * @param to : the clique to end
	 * @return : list of cliques in the path between from to to, but *NOT CONTAINING THEM*.
	 * Null means there is no path (this is supposedly impossible, because all cliques are connected,
	 * although some separators may be empty). Empty list means that there is direct connection between the cliques.
	 */
	protected List<Clique> findJunctionTreePath(Clique from, Clique to) {
		// check presence of junction tree and cliques
		if (getNet() == null || getNet().getJunctionTree() == null 
				|| getNet().getJunctionTree().getCliques() == null
				|| getNet().getJunctionTree().getCliques().isEmpty()) {
			// NOTE: AssetAwareInferenceAlgorithm#getNet() is supposedly the probabilistic network, 
			// and AssetPropagationInferenceAlgorithm#getNet() is supposedly the asset net
			return null;
		}
		// if the argument has null, return null
		if (from == null || to == null) {
			return null;
		}
		
		// the size of the path should be smaller than quantity of all cliques
		int maxPathSize = getRelatedProbabilisticNetwork().getJunctionTree().getCliques().size();
		
		// call recursive
		return this.visitCliques(from, to, new ArrayList<Clique>(maxPathSize), new HashSet<Clique>(maxPathSize));
		
	}
	
	
	/**
	 * Recursive depth first search to visit cliques.
	 * A depth first search should be enough, because in a tree structure only 1 path
	 * is supposed to exist between two cliques.
	 * @param from : the clique to start from
	 * @param to : the clique to end
	 * @param processedPath: path currently being processed by the recursive call
	 * @param deadCliques : cliques that were finished evaluating (which are known not to be in the path)
	 * @return list of cliques in the path between "from" to "to", but not containing them.
	 * Null means there is no path (this is supposedly impossible, because all cliques are connected,
	 * although some separators may be empty). Empty list means that there is direct connection between the cliques.
	 */
	private List<Clique> visitCliques(Clique from, Clique to, List<Clique> processedPath, Set<Clique> deadCliques) {
		// TODO reduce temporary memory usage
		
		// if the arguments are the same, return immediately
		if (from.equals(to) || from.getInternalIdentificator() == to.getInternalIdentificator()) {
			return null;
		}
		
		
		
		// mark the current clique as "evaluated", but don't use processedPath directly, since we don't want it to be an output parameter
		List<Clique> processingPath = new ArrayList<Clique>(processedPath);
		processingPath.add(from);
		
		// initialize the set of adjacent cliques = children + parent
		Set<Clique> adjacentSet = new HashSet<Clique>();
		if (from.getChildren() != null) {
			adjacentSet.addAll(from.getChildren());
		}
		if (from.getParent() != null) {
			adjacentSet.add(from.getParent());
		}
		
		// initialize a set of "dead" (i.e. verified that there is no path) cliques for my scope
		// note that if a clique is dead for my scope (currently processing path), it may not be dead for my upper scope (another path)
		// that's why I must create deadCliques for my scope
		Set<Clique> deadCliquesForMyScope = new HashSet<Clique>(deadCliques);
		
		for (Clique adjacent : adjacentSet) {
			
			if (deadCliques.contains(adjacent)) {
				// we know dead nodes have no path to the setTo...
				continue;
			}
			if (processingPath.contains(adjacent)) {
				// this is a cicle. Ignore this sub-path
				continue;
			}
			if (to.equals(adjacent) || to.getInternalIdentificator() == adjacent.getInternalIdentificator()) {
				// path found!
				return new ArrayList<Clique>(0);
			}
			
			// recursive call
			List<Clique> ret = visitCliques(adjacent, to, processingPath, deadCliquesForMyScope);
			if (ret == null) {
				// we recursively know that there is no path from adjacent to setTo, so, it is dead
				deadCliquesForMyScope.add(adjacent);
			} else {
				ret.add(0,adjacent);
				return ret;
			}
		}
		
		return null;
	}
	
//	/**
//	 * Simply calls {@link #addEdgesToNet(Map, boolean)}
//	 * @see unbbayes.prs.bn.inference.extension.AbstractAssetNetAlgorithm#addEdgesToNet(unbbayes.prs.INode, java.util.List, boolean)
//	 */
//	public List<Edge> addEdgesToNet(INode child, List<INode> parents, boolean isToOptimizeForProbNetwork) throws UnsupportedOperationException, IllegalArgumentException, InvalidParentException {
//		if (child == null || parents == null || parents.isEmpty()) {
//			return Collections.emptyList();
//		}
//		return this.addEdgesToNet(Collections.singletonMap(child, parents), isToOptimizeForProbNetwork);
//	}

	/**
	 * This method will attempt to connect nodes in {@link #getNet()}.
	 * CAUTION: it is not guaranteed that the resulting junction tree will be the same of the probabilistic junction tree
	 * of {@link #getRelatedProbabilisticNetwork()}, so the caller must ensure that the edges are also created
	 * in the {@link #getRelatedProbabilisticNetwork()}, resulting in the same junction tree structure.
	 * <br/>
	 * <br/>
	 * The following possibilities are handled:
	 * <br/>
	 * 0 - All nodes (parents and child) belongs to the same clique: only add edge and don't change junction tree.
	 * <br/>
	 * 1 - All parents belong to the same clique, but child doesn't belong to parents' clique:
	 * <br/>
	 * 2 - Any of the parents doesn't belong to the same clique: the approach of 1.2 will be used
	 * in order to include all nodes to same clique.
	 * <br/>
	 * <br/>
	 * The conditional probability tables (CPT) of each node ({@link ProbabilisticNode#getProbabilityFunction()})
	 * after adding the edge will become uniform.
	 * TODO : allow any CPT after adding edge
	 * @throws InvalidParentException 
	 * @see unbbayes.prs.bn.inference.extension.IAssetNetAlgorithm#addEdgesToNet(unbbayes.prs.INode, java.util.List)
	 */
	public List<Edge> addEdgesToNet(Map<INode, List<INode>> nodeAndParents, boolean isToOptimizeForProbNetwork, List<Edge> virtualArcs)
			throws UnsupportedOperationException, IllegalArgumentException,InvalidParentException {
		
		if (virtualArcs != null && !virtualArcs.isEmpty()) {
			// TODO handle virtualArcs
			throw new UnsupportedOperationException("Virtual arcs are not supported yet.");
		}
		// if isToOptimizeForProbNetwork, then special treatment is necessary
		if (isToOptimizeForProbNetwork) {
			return this.addNodesToCliqueWhenNotConsideringAssets(nodeAndParents);
		}
		
		// initial assertion
		if (nodeAndParents == null || nodeAndParents.isEmpty()) {
			return Collections.emptyList();
		}
		
		// prepare list to return (this list will be filled during the loop)
		List<Edge> ret = new ArrayList<Edge>();	
		
		// extract the network to work with
		ProbabilisticNetwork net = this.getNet();
		
		// if isToOptimizeForProbNetwork == false, then simply iterate over nodeAndParents.
		for (Entry<INode, List<INode>> entry : nodeAndParents.entrySet()) {
			// extract the nodes in the current iteration
			INode child = entry.getKey();
			List<INode> parents = entry.getValue();
			
			if (child == null 
					|| parents == null
					|| parents.isEmpty()) {
				// it does not specify an arc, so we do not need to handle it
				continue;
			}
			
			// needs to remove duplicates
			if (parents.size() > 1) {
				List<INode> nonDuplicateParents = new ArrayList<INode>(parents.size());
				for (INode parent : parents) {
					if (!nonDuplicateParents.contains(parent)) {
						// ignore duplicates, but do not ignore original ordering of nodes
						nonDuplicateParents.add(parent);
					}
				}
				parents = nonDuplicateParents;
			}
			
			
			// extract the cliques containing parents (if any)
			List<Clique> parentCliques = net.getJunctionTree().getCliquesContainingAllNodes(parents, Integer.MAX_VALUE);
			if (parentCliques != null && !parentCliques.isEmpty()) { // case 0 or 1
				// check if we can handle this case as case 0: both child and parents are in same clique
				boolean hasAllNodesInSameClique = false;
				// try to find child node in parentCliques (since parentCliques supposedly has all parent nodes, then if we find any with child, it contains all nodes)
				for (Clique clique : parentCliques) {
					if (clique.getNodesList().contains(child)) {
						hasAllNodesInSameClique = true;
						break;
					}
				}
				if (!hasAllNodesInSameClique) {
					// case 1: parents are in same clique, but child is not
					this.addNodesToCliqueWhenAllParentsInSameClique(child, parents, parentCliques, net);
				}
				// case 0: simply add the edge and return without changing junction tree -> will be handled outside this if-clause
			} else {
				// case 2: some of the parents are not in the same clique
				this.addNodesToCliqueWhenParentsInDifferentClique(child, parents, net);
			}
			
			// at this point, junction trees were properly handled (cases 0, 1, or 2). We now just need to add the edge objects into the Bayes net object.
			for (INode parent : parents) {
				Edge edge = new Edge((Node)parent, (Node)child);
				net.addEdge(edge);
				ret.add(edge);
			}
			
			// force CPT to become uniform
			if (child instanceof ProbabilisticNode) {
				new UniformTableFunction().applyFunction((ProbabilisticTable) ((ProbabilisticNode)child).getProbabilityFunction());
			}
		}
		
		
		return ret;
	}

	

	/**
	 * Implements case 1 of {@link #addEdgesToNet(INode, List)} - All parents belong to the same clique, but child doesn't belong to parents' clique:
	 * <br/>
	 * 1.1 One of the cliques are root of a disconnected subtree: 
	 * in this case, the disconnected root clique (which is supposedly a child of the global root clique)
	 * will be disconnected from the root and then re-connected as a child of the other clique.
	 * <br/>
	 * 1.2 - Both cliques are not a root of a disconnected subtree:
	 * A clique with smaller quantity of nodes will be selected and each node in the smaller clique
	 * will be added to all cliques and separators within the junction tree path that connects
	 * the two cliques
	 * This will potentially generate duplicate cliques, causing sub-optimal performance, but
	 * the numerical values will be supposedly consistent.
	 * <br/>
	 * @param child : the child node (whose new arcs/edges will be pointing to)
	 * @param parents : the parent nodes (whose new arcs/edges will be pointing from)
	 * @param parentCliques : cliques containing all parents
	 * @param net : network being considered (owner of child, parents, and parentCliques)
	 */
	protected void addNodesToCliqueWhenAllParentsInSameClique(INode child, List<INode> parents, List<Clique> parentCliques, ProbabilisticNetwork net) {
		// extract cliques of child, but cliques of child must be the clique containing child and any of its parent.
		// Otherwise a clique containing child and its children may be selected (such clique is not suitable to be used in order to add more parents)
		List<INode> childAndAnyParent = new ArrayList<INode>(2);
		childAndAnyParent.add(child);
		if (child.getParentNodes() != null && !child.getParentNodes().isEmpty()) {
			childAndAnyParent.add(child.getParentNodes().get(0));
		}
		List<Clique> childCliques = net.getJunctionTree().getCliquesContainingAllNodes(childAndAnyParent, Integer.MAX_VALUE);
		if (childCliques == null || childCliques.isEmpty()) {
			throw new IllegalArgumentException("Junction tree of network " + net + " does not contain clique with node " + child + " and any of its parents (if any).");
		}
		
		// check if it is case 1.1 (at least one of the cliques is a root of a subtree)
		// TODO current version does not move global root cliques (a global root is global root forever). Make changes so that global root cliques can be substituted as well
		
		//extract the root clique of subtree
		Clique localRootOfSubtree = null;
		Clique newParentOfLocalRootClique = null;	// clique to become the new parent of rootOfSubtree
		for (Clique clique : childCliques) {
			if (clique.getParent() != null && net.getJunctionTree().getSeparator(clique, clique.getParent()).getNodes().isEmpty()) {	// a disconnected clique connected to root with empty separator
				// this is not global root and it is a local root (clique connected to parent with empty separator)
				localRootOfSubtree = clique;
				// set newParentClique as the smallest clique in parentCliques
				for (Clique parentClique : parentCliques) {
					if (newParentOfLocalRootClique == null || newParentOfLocalRootClique.getNodesList().size() > parentClique.getNodesList().size()) {
						newParentOfLocalRootClique = parentClique;
					}
				}
				break;
			}
		}
		if (localRootOfSubtree == null) {
			// could not find global/local root clique containing child. Find from parents
			for (Clique clique : parentCliques) {
				if (clique.getParent() != null && net.getJunctionTree().getSeparator(clique, clique.getParent()).getNodes().isEmpty()) {	// a disconnected clique connected to root with empty separator
					// this is a local root (clique connected to parent with empty separator)
					localRootOfSubtree = clique;
					// set newParentClique as the smallest clique in childCliques
					for (Clique childClique : childCliques) {
						if (newParentOfLocalRootClique == null || newParentOfLocalRootClique.getNodesList().size() > childClique.getNodesList().size()) {
							newParentOfLocalRootClique = childClique;
						}
					}
					break;
				}
			}
		}
		
		if (localRootOfSubtree != null) {
			// 1.1: One of the cliques are root of a disconnected subtree.
			
			// remove empty separator from the junction tree in order to disconnect local root from its parent
			Separator sep = net.getJunctionTree().getSeparator(localRootOfSubtree, localRootOfSubtree.getParent());
			if (sep.getProbabilityFunction().tableSize() > 1) {
				throw new IllegalStateException(sep + " is supposed to be an empty separator between " + localRootOfSubtree + " and " + localRootOfSubtree.getParent());
			}
			net.getJunctionTree().removeSeparator(sep);
			// actually disconnect the local root from its parent
			localRootOfSubtree.getParent().removeChild(localRootOfSubtree);
			
			// create separator connecting the two cliques. This should also update parent and children of the affected cliques
			Separator newSep = new Separator(newParentOfLocalRootClique,localRootOfSubtree);
			newSep.setInternalIdentificator(sep.getInternalIdentificator());	// reuse internal identification ID
			// Note: this separator is initially empty, because there is no common node between them yet
			net.getJunctionTree().addSeparator(newSep);
			
			// add parent nodes to separator
			for (INode parent : parents) {
				newSep.getNodes().add((Node) parent);
				PotentialTable table = newSep.getProbabilityFunction();
				table.addVariable(parent);
				if (parent instanceof AssetNode || net instanceof AssetNetwork) {
					// asset separators are usually filled with a default value
					for (int i = 0; i < table.tableSize(); i++) {
						// this is supposedly asset separator
						table.setValue(i, getDefaultInitialAssetTableValue());
					}
				} else {	
					if (parent.getStatesSize() != table.tableSize()) {
						throw new RuntimeException("Node " + parent + " has size " + parent.getStatesSize()
								+ " and separator " + newSep + " has size " + table.tableSize()
								+ ", but they were supposed to be the same, therefore with same size.");
					}
					// table supposedly contains only parent, so fill it with its current marginal
					for (int i = 0; i < table.tableSize(); i++) {
						table.setValue(i, ((TreeVariable)parent).getMarginalAt(i));
					}
				}
			}
			
			for (INode parent : parents) {
				// add parent node to newParentOfLocalRootClique if it does not contain it already
				if (!newParentOfLocalRootClique.getNodesList().contains(parent)) {
					newParentOfLocalRootClique.getNodesList().add((Node) parent);
					PotentialTable table = newParentOfLocalRootClique.getProbabilityFunction();
					table.addVariable(parent);
					if (!(net instanceof AssetNetwork) && (parent instanceof ProbabilisticNode)) {
						// adjust probability accordingly to current marginal
						// prepare temporary table to be used for multiplying cells of table by reusing code already available
						ProbabilisticTable tableForMultiplication = new ProbabilisticTable();
						tableForMultiplication.addVariable(parent);
						if (parent.getStatesSize() == tableForMultiplication.tableSize()) {
							for (int i = 0; i < tableForMultiplication.tableSize(); i++) {
								tableForMultiplication.setValue(i, ((ProbabilisticNode)parent).getMarginalAt(i));
							}
						} else {
							throw new RuntimeException("Unconditional table of node" + parent + " should have size " + parent.getStatesSize() + ", but was " + tableForMultiplication.tableSize());
						}
						// multiply clique potential with marginal of node being added
						table.opTab(tableForMultiplication, PotentialTable.PRODUCT_OPERATOR);
						// we supposedly don't need to normalize it, if it was normalized before the change
					}
				}
				// add parent node to rootOfSubtree if it does not contain it already
				if (!localRootOfSubtree.getNodesList().contains(parent)) {
					localRootOfSubtree.getNodesList().add((Node) parent);
					PotentialTable table = localRootOfSubtree.getProbabilityFunction();
					table.addVariable(parent);
					if (!(net instanceof AssetNetwork) && (parent instanceof ProbabilisticNode)) {
						// adjust probability accordingly to current marginal
						// prepare temporary table to be used for multiplying cells of table by reusing code already available
						ProbabilisticTable tableForMultiplication = new ProbabilisticTable();
						tableForMultiplication.addVariable(parent);
						if (parent.getStatesSize() == tableForMultiplication.tableSize()) {
							for (int i = 0; i < tableForMultiplication.tableSize(); i++) {
								tableForMultiplication.setValue(i, ((ProbabilisticNode)parent).getMarginalAt(i));
							}
						} else {
							throw new RuntimeException("Unconditional table of node" + parent + " should have size " + parent.getStatesSize() + ", but was " + table.tableSize());
						}
						// multiply clique potential with marginal of node being added
						table.opTab(tableForMultiplication, PotentialTable.PRODUCT_OPERATOR);
						// we supposedly don't need to normalize it, if it was normalized before the change
					}
				}
			}
		} else {
			/*
			 * 1.2 - Both cliques are not a root of a disconnected subtree:
			 * A clique with smaller quantity of nodes will be selected and each node in the smaller clique
			 * will be added to all cliques and separators within the junction tree path that connects
			 * the two cliques
			 * This will potentially generate duplicate cliques, causing sub-optimal performance, but
			 * the numerical values will be supposedly consistent.
			 */
			// find all cliques forming a path from shortestParentClique to shortestChildClique
			List<Clique> treePath = findShortestJunctionTreePath(parents, childAndAnyParent);
			// NOTE: this list supposedly includes the clique with parents and the clique with child
			if (treePath == null || treePath.isEmpty()) {
				throw new IllegalStateException("Could not find junction tree path from " + parents + " to " + child 
						+ ". This is probably due to unexpected format of junction tree.");
			}
			
			// use the smaller of the two cliques (one containing parent nodes and the other containing the child node) as pivot (the clique containing nodes to be included in the path)
			Clique pivotClique = null;
			if (treePath.get(0).getNodesList().size() > treePath.get(treePath.size()-1).getNodesList().size()) {
				pivotClique = treePath.get(treePath.size()-1);
			} else {
				pivotClique = treePath.get(0);
			}
			
			// add nodes of pivot clique to all cliques in the treePath, in order to simulate connection
			// TODO this is sub-optimal. Find another way to update junction tree without breaking asset's consistency
			
			// start iterating on the path, but first treat the 1st clique in the path separately 
			// (because it's the only clique we don't have a clique and associated separator "before" it)
			Clique previousCliqueInTreePath = treePath.get(0);	// this clique will be the "previously" handled clique after we start iteration on the path
			if (!previousCliqueInTreePath.equals(pivotClique)) {
				// add the nodes in the pivot
				for (Node node : pivotClique.getNodesList()) {
					if (!previousCliqueInTreePath.getNodesList().contains(node)) {
						previousCliqueInTreePath.getNodesList().add(node);
						// include to potential table too
						previousCliqueInTreePath.getProbabilityFunction().addVariable(node);
						// Note: this assumes that adding a node to ProbabilityTable will simply replicate columns
						if (!(net instanceof AssetNetwork) && (node instanceof ProbabilisticNode)) {
							// adjust probability accordingly to current marginal
							// prepare temporary table to be used for multiplying cells of table by reusing code already available
							ProbabilisticTable tableForMultiplication = new ProbabilisticTable();
							tableForMultiplication.addVariable(node);
							if (node.getStatesSize() == tableForMultiplication.tableSize()) {
								for (int i = 0; i < tableForMultiplication.tableSize(); i++) {
									tableForMultiplication.setValue(i, ((ProbabilisticNode)node).getMarginalAt(i));
								}
							} else {
								throw new RuntimeException("Unconditional table of node" + node + " should have size " + node.getStatesSize() + ", but was " + tableForMultiplication.tableSize());
							}
							// multiply clique potential with marginal of node being added
							previousCliqueInTreePath.getProbabilityFunction().opTab(tableForMultiplication, PotentialTable.PRODUCT_OPERATOR);
							// we supposedly don't need to normalize it, if it was normalized before the change
						}
					}
				}
			}
			
			// and then, treat other cliques and separators. Start iteration from 2nd element, because we just handled 1st element separately
			for (int i = 1; i < treePath.size(); i++) {
				Clique currentCliqueInTreePath = treePath.get(i);
				
				// extract separator between the previously handled clique and the clique currently being handled
				Separator sep = net.getJunctionTree().getSeparator(previousCliqueInTreePath, currentCliqueInTreePath);
				if (sep == null) {
					throw new RuntimeException("Could not find separator (potentially empty) between " + previousCliqueInTreePath 
							+ " and " + currentCliqueInTreePath + ". This may be due to a bug or an inconsistent junction tree.");
				}
				
				// add nodes in separator
				for (Node node : pivotClique.getNodesList()) {
					// only needs to add node if it is not there already
					if (!sep.getNodes().contains(node)) {
						sep.getNodes().add(node);
						// add to potential table too
						sep.getProbabilityFunction().addVariable(node);
						// this assumes that adding a node to ProbabilityTable will simply replicate columns
						if (!(net instanceof AssetNetwork) && (node instanceof ProbabilisticNode)) {
							// adjust probability accordingly to current marginal
							// prepare temporary table to be used for multiplying cells of table by reusing code already available
							ProbabilisticTable tableForMultiplication = new ProbabilisticTable();
							tableForMultiplication.addVariable(node);
							if (node.getStatesSize() == tableForMultiplication.tableSize()) {
								for (int j = 0; j < tableForMultiplication.tableSize(); j++) {
									tableForMultiplication.setValue(j, ((ProbabilisticNode)node).getMarginalAt(j));
								}
							} else {
								throw new RuntimeException("Unconditional table of node" + node + " should have size " + node.getStatesSize() + ", but was " + tableForMultiplication.tableSize());
							}
							// multiply clique potential with marginal of node being added
							sep.getProbabilityFunction().opTab(tableForMultiplication, PotentialTable.PRODUCT_OPERATOR);
							// we supposedly don't need to normalize it, if it was normalized before the change
						}
					}
				}
				
				// add nodes in current clique
				for (Node node : pivotClique.getNodesList()) {
					// only needs to add node if it is not there already
					if (!currentCliqueInTreePath.getNodesList().contains(node)) {
						currentCliqueInTreePath.getNodesList().add(node);
						// add node to potential table too
						currentCliqueInTreePath.getProbabilityFunction().addVariable(node);
						//  this assumes that adding a node to ProbabilityTable will simply replicate columns
						if (!(net instanceof AssetNetwork) && (node instanceof ProbabilisticNode)) {
							// adjust probability accordingly to current marginal
							// prepare temporary table to be used for multiplying cells of table by reusing code already available
							ProbabilisticTable tableForMultiplication = new ProbabilisticTable();
							tableForMultiplication.addVariable(node);
							if (node.getStatesSize() == tableForMultiplication.tableSize()) {
								for (int j = 0; j < tableForMultiplication.tableSize(); j++) {
									tableForMultiplication.setValue(j, ((ProbabilisticNode)node).getMarginalAt(j));
								}
							} else {
								throw new RuntimeException("Unconditional table of node" + node + " should have size " + node.getStatesSize() + ", but was " + tableForMultiplication.tableSize());
							}
							// multiply clique potential with marginal of node being added
							currentCliqueInTreePath.getProbabilityFunction().opTab(tableForMultiplication, PotentialTable.PRODUCT_OPERATOR);
							// we supposedly don't need to normalize it, if it was normalized before the change
						}
					}
				}
				// current clique is the previous clique in next iteration
				previousCliqueInTreePath = currentCliqueInTreePath;
			}
			
			// TODO check whether a propagation is necessary to assure global consistency (although it is unlikely to be necessary).
		}
	}
	
	/**
	 * Implements case 2 of {@link #addEdgesToNet(INode, List)} - any of the parents doesn't belong to the same clique.
	 * @param child : the child node (whose new arcs/edges will be pointing to)
	 * @param parents : the parent nodes (whose new arcs/edges will be pointing from)
	 * @param net : network being considered (owner of child, parents, and parentCliques)
	 */
	protected void addNodesToCliqueWhenParentsInDifferentClique(INode child, List<INode> parents, ProbabilisticNetwork net) {
		// first, connect the parents
		for (int i = 0; i < parents.size()-1; i++) {
			for (int j = i+1; j < parents.size(); j++) {
				// TODO this is very inefficient. Find another approach (e.g. use greedy algorithm in order to split parents into blocks)
				List<INode> singletonParent = Collections.singletonList(parents.get(i));
				this.addNodesToCliqueWhenAllParentsInSameClique(parents.get(j), singletonParent, net.getJunctionTree().getCliquesContainingAllNodes(singletonParent, Integer.MAX_VALUE), net);
			}
		}
		// and then simply call the method for each parent, one by one
		for (INode parent : parents) {
			// TODO this is very inefficient. Find another approach (e.g. use greedy algorithm in order to split parents into blocks)
			List<INode> singletonParent = Collections.singletonList(parent);
			this.addNodesToCliqueWhenAllParentsInSameClique(child, singletonParent, net.getJunctionTree().getCliquesContainingAllNodes(singletonParent, Integer.MAX_VALUE), net);
		}
	}
	
	/**
	 * This method can be used to obtain a clique containing most of the nodes
	 * passed in its argument.
	 * This is used in {@link #addNodesToCliqueWhenNotConsideringAssets(INode, List, ProbabilisticNetwork)}
	 * in order to get a clique in the old junction tree which is most similar to a clique in the new junction tree
	 * (so that clique potentials can be filled easier).
	 * @param nodes: nodes that this method will use in order to attempt to find
	 * a clique containing most of them.
	 * @param jt: the junction tree where the search will be performed
	 * @return a clique which may contain all the nodes. If it does not
	 * contain all the nodes, it will contain nodes whose the size of the intersection with
	 * the passed argument is as large as possible.
	 * It will return null if no node or no network or no junction tree was specified, or when the junction tree does not contain the specified node.
	 */
	protected Clique getCliqueContainingMostOfNodes(Collection<INode> nodes, IJunctionTree jt) {
		// initial assertions
		if (jt == null ) {	
			return null;
		}
		
		List<Clique> cliquesContainingMostOfNodes = jt.getCliquesContainingMostOfNodes(nodes);
		
		// return null if nothing was returned by jt, or else return the 1st clique
		return (cliquesContainingMostOfNodes == null || cliquesContainingMostOfNodes.isEmpty())?null:cliquesContainingMostOfNodes.get(0);
	}

	/**
	 * This method implements {@link #addEdgesToNet(INode, List, boolean)}
	 * for the case when the boolean argument is true.
	 * Basically, this method relies on UnBBayes' default junction tree generation algorithm
	 * and creates a new junction tree, and then fills the new cliques with 
	 * joint probabilities obtainable from the old junction tree. 
	 * The following steps are performed:
	 * <br/>
	 * <br/>
	 * 1 - The cpts are updated based on current clique potentials;
	 * <br/>
	 * 2 - The edges are created;
	 * <br/>
	 * 3 - Because new edges would simply grow the CPTs, the new cpt is simply filled by copying existing columns into the new columns, so that
	 * the child is initially independent to the new parent;
	 * <br/>
	 * 4 - the new network is compiled (the junction tree is re-generated) based on the new structure and cpts, so that
	 * a near-optimal collection of cliques is guaranteed by using the same heuristics of a normal junction tree compilation;
	 * <br/>
	 * <br/>
	 * CAUTION: this method does not perform any synchronization with asset networks
	 * (e.g. keeping same clique structures, mappings, or indexes), because
	 * it is supposed to be a method to be used when not considering assets.
	 * Therefore, avoid using this method when the algorithm is handling assets,
	 * or implementations overwriting this method (or the caller of this method) shall make necessary changes 
	 * regarding assets.
	 * <br/>
	 * It will use {@link #getRelatedProbabilisticNetwork()} as the network to add edges.
	 * <br/>
	 * It will use {@link #run()} in order to compile the network.
	 * <br/>
	 * This method also assumes that if you absorbed a node by calling {@link #setAsPermanentEvidence(Map, boolean)} setting
	 * to delete evidence nodes, then the parents are automatically connected.
	 * @param nodeAndParents : a mapping from the child node (whose new arcs/edges will be pointing to)
	 * to its parent nodes (whose new arcs/edges will be pointing from).
	 * @return list of edges created by this method.
	 * @throws InvalidParentException : exception thrown inherently from {@link ProbabilisticNetwork#addEdge(Edge)}
	 */
	public List<Edge> addNodesToCliqueWhenNotConsideringAssets(Map<INode, List<INode>> nodeAndParents) throws InvalidParentException {
		
		// basic assertion
		if (nodeAndParents == null 
				|| nodeAndParents.isEmpty()) {
			return Collections.emptyList();
		}
		
		// extract the network to be used in this method
		ProbabilisticNetwork net = getRelatedProbabilisticNetwork();
		if ( net == null ) {
			return Collections.emptyList();
		}
		
		// update the CPTs of all nodes
		this.updateCPTBasedOnCliques();
		
		// the following code was commented out because we won't need them if we are re-generating the clique potentials based on updated CPTs
		
//		// extract old junction tree (JT) from network now (before compiling new JT), because we will use its clique potentials
//		IJunctionTree oldJT = net.getJunctionTree();
//		
//		// keep track of old marginals, because they will be lost if we recompile current net by calling "run()"
//		Map<INode, float[]> mapOldMarginal = new HashMap<INode, float[]>();
//		for (INode iNode : net.getNodes()) {
//			if (iNode instanceof TreeVariable) {
//				TreeVariable node = (TreeVariable) iNode;
//				float[] marginal = new float[node.getStatesSize()];
//				for (int i = 0; i < node.getStatesSize(); i++) {
//					marginal[i] = node.getMarginalAt(i);
//				}
//				mapOldMarginal.put(node, marginal);
//			}
//		}
		
		// this is the list to be returned
		List<Edge> ret = new ArrayList<Edge>();
		
		// iterate over the mapping
		for (Entry<INode, List<INode>> entry : nodeAndParents.entrySet()) {
			// extract the nodes in the mapping entry
			INode child = entry.getKey();
			List<INode> parents = entry.getValue();
			
			// assertions about content of map
			if (child == null 
					|| parents == null
					|| parents.isEmpty() ) {
				continue;
			}
			
			// add the edges (arcs)
			for (INode parent : parents) {
				Edge edge = new Edge((Node)parent, (Node)child);
				net.addEdge(edge);	// this method supposedly avoids duplicates already
				// TODO this will include the edge in ret even though net already contained it. Change it in order to include edge only when new edge is actually included to net
				ret.add(edge);
			}
		}
		
		// make sure all CPTs of the affected children are kept normalized after insertion of all edges
//		for (INode child : nodeAndParents.keySet()) {
//			// TODO check if this is really necessary
//			getCptNormalizer().applyFunction((ProbabilisticTable) ((ProbabilisticNode)child).getProbabilityFunction());
//		}
		
		
		// compile junction tree, considering the new edges
		try {
			run();
		} catch (Throwable e) {
			// undo all changes in arcs
			for (Edge edge : ret) {
				net.removeEdge(edge);
			}
			// return JT back to its original stage
//			run();
			throw new RuntimeException(e);
		}
		
		
		// all the following code was commented out, because we don't need to udpate clique potentials, since we appended edges to cpts which were updated based on the old clique potentials
		
//		// this is the new junction tree, with new edges
//		IJunctionTree newJT = net.getJunctionTree();
//
//		if (oldJT != null && oldJT.getCliques() != null && !oldJT.getCliques().isEmpty()) { 
//			
//			// there is an old JT to be used in order to fill clique potentials of new JT
//			for (Clique newClique : newJT.getCliques()) {
//				
//				// for each clique in new JT, find most similar clique in old JT
//				Clique oldClique = getCliqueContainingMostOfNodes((Collection)newClique.getNodesList(), oldJT);
//				if (oldClique == null) {
//					throw new RuntimeException("The old junction tree does not contain any of the nodes " + newClique.getNodesList());
//				}
//				
//				// clone the old clique table, because we don't want to change originals at this point (other iterations may want to use the originals)
//				PotentialTable oldTable = (PotentialTable) oldClique.getProbabilityFunction().clone();
//				
//				// marginalize out (sum out) from old clique some nodes not used in new clique currently being evaluated
//				for (INode oldCliqueNode : oldClique.getNodesList()) {
//					if (!newClique.getNodesList().contains(oldCliqueNode)) {
//						// this node is in oldTable, but won't be used in new clique, so marginalize out
//						oldTable.removeVariable(oldCliqueNode);
//						// it is assumed that cliques don't have duplicate nodes
//					}
//				}
//				
//				// TODO this is wrong, and will only work for independent nodes!
//				
//				// add nodes in old clique which are present in new clique but not in old clique
//				for (INode newCliqueNode : newClique.getNodesList()) {
//					if (oldTable.getVariableIndex((Node) newCliqueNode) < 0) {
//						// this node is in new clique, but not in old clique, 
//						// so add into oldTable with proper transformation in order to get a clique potential of the variables in new clique
//						oldTable.addVariable(newCliqueNode);
//						
//						// by adding node, we shall multiply the clique table with the marginal of the node being added.
//						// So prepare temporary table to be used for multiplying cells of table by reusing code already available
//						ProbabilisticTable tableForMultiplication = new ProbabilisticTable();
//						tableForMultiplication.addVariable(newCliqueNode);
//						
//						// obtain what was the marginal of the node before run() 
//						float[] marginal = mapOldMarginal.get(newCliqueNode);
//						
//						// fill tableForMultiplication with the obtained marginal
//						if (marginal != null && marginal.length == tableForMultiplication.tableSize()) {
//							for (int i = 0; i < tableForMultiplication.tableSize(); i++) {
//								tableForMultiplication.setValue(i, marginal[i]);
//							}
//						} else {
//							throw new RuntimeException("Could not apply marginal probability " + marginal + " to unconditional table of node " + newCliqueNode);
//						}
//						
//						// multiply clique potential with marginal of node being added
//						oldTable.opTab(tableForMultiplication, PotentialTable.PRODUCT_OPERATOR);
//						// we supposedly don't need to normalize it, if it was normalized before the change
//
//					}
//				}
//				
//				// make sure table is normalized
//				oldTable.normalize();
//				
//				// copy clique potential from old to new clique
//				PotentialTable newTable = newClique.getProbabilityFunction();
//				if (newTable.tableSize() == oldTable.tableSize()) {
//					// also make sure the variables are in same ordering
//					
//					newTable.setValues(oldTable.getValues());
//				} else {
//					// because we removed extra nodes from oldTable and added nodes of newTable to oldTable, they should have the same size
//					throw new RuntimeException("A bug was found in the method for filling clique potentials of clique " + newClique 
//							+ " based on clique " + oldClique + ". The adjusted clique size were different, and respectively "
//							+ newTable.tableSize() + " and " + oldTable.tableSize());
//				}
//				
//			}
//			
//		} // else: network was not compiled yet, so we do not need to copy clique content from old JT
//		
//		
//		// update new separators. This can be done simply by marginalizing out (sum) from neighbor cliques (because global consistency supposedly holds)
//		for (Separator sep : newJT.getSeparators()) {
//			if (sep.getNodes().size() <= 0) {
//				// there is no need to update empty separators
//				continue;
//			}
//			
//			// obtain one of the neighbor clique (any one will do the job)
//			Clique clique = sep.getClique1();
//			
//			// obtain a clone of the clique potential (so that we can marginalize out some variables without changing original)
//			PotentialTable cliqueTable = (PotentialTable) clique.getProbabilityFunction().clone();
//			
//			// marginalize out nodes not present in separator
//			for (Node nodeInClique : clique.getNodesList()) {
//				if (!sep.getNodes().contains(nodeInClique)) {
//					cliqueTable.removeVariable(nodeInClique);
//				}
//			}
//			
//			// fill separator table with the values in the marginalized table
//			PotentialTable sepTable = sep.getProbabilityFunction();
//			if (cliqueTable.tableSize() == sepTable.tableSize()) {
//				sepTable.setValues(cliqueTable.getValues());
//			} else {
//				// because we removed extra nodes from cliqueTable, they should have the same size
//				throw new RuntimeException("A bug was found in the method for marginalizing clique table of  " + clique 
//						+ " in order to get potentials of separator " + sep + ". The table size were different, and respectively "
//						+ cliqueTable.tableSize() + " and " + sepTable.tableSize());
//			}
//			
//			// TODO check if global consistency still holds
//		}
//		
//		// update marginal probabilities
//		for (Node node : net.getNodes()) {
//			if (node instanceof ProbabilisticNode) {
//				ProbabilisticNode pnode = (ProbabilisticNode) node;
//				pnode.setMarginalProbabilities(mapOldMarginal.get(pnode));
//			}
//		}
		
		// return the generated edges
		return ret;
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
	 * This enum specifies known types of evidence, which can be normal hard evidence ({@link #HARD_EVIDENCE}), which
	 * sets some state to 100%, or "negative" hard evidence ({@link #NEGATIVE_HARD_EVIDENCE}), which 
	 * sets some states to 0%, or soft evidence ({@link #SOFT_EVIDENCE}), which sets states to something in between 0% and 100%
	 * @author Shou Matsumoto
	 * @see AbstractAssetNetAlgorithm#getEvidenceType(List)
	 */
	public static enum EvidenceType {HARD_EVIDENCE, SOFT_EVIDENCE, NEGATIVE_HARD_EVIDENCE};
	
	/**
	 * Verifies if the specified probability distribution indicates a "negative" hard evidence.
	 * A "negative" hard evidence happens when states are explicitly indicated to become %, and other
	 * states are either specified as {@link #isUnspecifiedProb(Float)} or 100%.
	 * @param prob: the list of probabilities (or unspecified values which {@link #isUnspecifiedProb(Float)} == true).
	 * @return: true if this is negative finding, false if this is positive finding, and null if this is soft evidence.
	 * @see #isUnspecifiedProb(Float)
	 * @throws IllegalArgumentException: if the input format is invalid (is none of the 3 possible types of evidence).
	 * @see #getResolvedState(List)
	 */
	public EvidenceType getEvidenceType(List<Float> prob) {
		Integer resolvedState = this.getResolvedState(prob);
		if (resolvedState == null) {
			return EvidenceType.SOFT_EVIDENCE;
		} else if (resolvedState < 0) {
			return EvidenceType.NEGATIVE_HARD_EVIDENCE;
		} 
		return EvidenceType.HARD_EVIDENCE;
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

//	/**
//	 * This object is responsible for normalizing CPTs at {@link #addNodesToCliqueWhenNotConsideringAssets(Map)}
//	 * @return the cptNormalizer
//	 */
//	public ITableFunction getCptNormalizer() {
//		return cptNormalizer;
//	}
//
//	/**
//	 * This object is responsible for normalizing CPTs at {@link #addNodesToCliqueWhenNotConsideringAssets(Map)}
//	 * @param cptNormalizer the cptNormalizer to set
//	 */
//	public void setCptNormalizer(ITableFunction cptNormalizer) {
//		this.cptNormalizer = cptNormalizer;
//	}
	
//	/**
//	 * Uses {@link #joinCliqueTables(Clique, Clique, Separator)} 
//	 * and {@link #findShortestJunctionTreePath(Collection, Collection)} in order
//	 * to obtain potentials of a set of nodes which reflects the current
//	 * junction tree.
//	 * @param nodes : nodes to be included.
//	 * @param junctionTree : the junction tree to obtain potentials (the clique tables).
//	 * If null, the junction tree of {@link #getNet()} will be used
//	 * @return
//	 */
//	public PotentialTable getPotentialTableFromNodes(List<INode> nodes, IJunctionTree junctionTree) {
//		if (junctionTree == null) {
//			if (this.getNet() == null || this.getNet().getJunctionTree() == null) {
//				return null;
//			}
//			junctionTree = this.getNet().getJunctionTree();
//		}
//		
//		Clique cliqueContainingMostOfNodes = this.getCliqueContainingMostOfNodes(nodes, junctionTree);
//		
//		this.findShortestJunctionTreePath(from, to);
//	}
	
//	/**
//	 * Merges : two clique potential tables.
//	 * This can be used in {@link #addNodesToCliqueWhenNotConsideringAssets(Map)}
//	 * in order to generate a potential table which contain all nodes of two cliques
//	 * (and then, {@link PotentialTable#removeVariable(INode, boolean)} can be used
//	 * to delete excess node) in order to create clique potential of desired nodes.
//	 * @param to : basically, nodes in the other clique will be inserted to the clique table of this clique.
//	 * @param from : basically, nodes in this clique will be inserted to the clique table of the other clique.
//	 * @param sep: separator connecting the two cliques. Nodes in this separator are expected to
//	 * be common to the 2 cliques, and they will be used in order to adjust conditional
//	 * probabilities of the resulting table.
//	 * @return : a potential table containing variables from both tables. It is not the same object of either "to" or "from" clique's tables.
//	 */
//	public PotentialTable joinCliqueTables(Clique to, Clique from, Separator sep) {
//		// basic assertions
//		if (to == null || from == null) {
//			throw new NullPointerException("Two cliques must be specified");
//		}
//		if (sep != null
//				&& !( (sep.getClique1().equals(from) && sep.getClique2().equals(to)) 
//						|| (sep.getClique1().equals(to) && sep.getClique2().equals(from)) ) ) {
//			throw new IllegalArgumentException("The specified separator "+ sep +" should connect the specified cliques " + to + " and " + from);
//		}
//		
//		// extract the clique table from the "from" clique. Use clone because we'd not like to change the original
//		PotentialTable fromTable = (PotentialTable) from.getProbabilityFunction().clone();
//		
//		// extract the clique table from the "to" clique. Use clone because we'd not like to change the original
//		PotentialTable toTable = (PotentialTable) to.getProbabilityFunction().clone();
//		
//		// insert the variables of fromTable to the toTable
//		for (int i = 0; i < fromTable.getVariablesSize(); i++) {
//			if (!(fromTable.getVariableAt(i) instanceof ProbabilisticNode)) {
//				continue; // ignore non-prob nodes
//			}
//			ProbabilisticNode node = (ProbabilisticNode) fromTable.getVariableAt(i);
//			int indexOfVariable = toTable.indexOfVariable(node);
//			if (indexOfVariable < 0) {
//				// toTable does not have this variable, so add it
//				toTable.addVariable(node);
//				
//				// if there is no common variable in separator, then the two nodes are supposedly independent, so we can simply multiply by marginal
//				if (sep.getNodes().size() <= 0) {
//					ProbabilisticTable tableForMultiplication = new ProbabilisticTable();
//					tableForMultiplication.addVariable(node);
//					
//					// fill tableForMultiplication with the obtained marginal
//					if (node.getStatesSize() == tableForMultiplication.tableSize()) {
//						for (int j = 0; j < tableForMultiplication.tableSize(); j++) {
//							tableForMultiplication.setValue(j, node.getMarginalAt(j));
//						}
//					} else {
//						throw new RuntimeException("Probabilistic table containing only node  " + node + " should have size " + node.getStatesSize() 
//								+ ", but was " + tableForMultiplication.tableSize());
//					}
//					
//					// multiply clique potential with marginal of node being added
//					toTable.opTab(tableForMultiplication, PotentialTable.PRODUCT_OPERATOR);
//				}
//			}
//		}
//		
//		// multiply by fromTable and divide by separator, if nodes are dependent
//		if (sep.getNodes().size() > 0) {
//			toTable.opTab(fromTable, PotentialTable.PRODUCT_OPERATOR);
//			toTable.opTab(sep.getProbabilityFunction(), PotentialTable.DIVISION_OPERATOR);
//		}
//		
//		// TODO check if we need normalization
//		
//		return toTable;
//	}


}
