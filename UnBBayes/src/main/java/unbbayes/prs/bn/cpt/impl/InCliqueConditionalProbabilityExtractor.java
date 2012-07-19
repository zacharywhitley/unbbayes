/**
 * 
 */
package unbbayes.prs.bn.cpt.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.IProbabilityFunction;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.Separator;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor;
import unbbayes.util.Debug;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * Implementation of {@link IArbitraryConditionalProbabilityExtractor} which
 * assumes that all nodes is within the same clique.
 * @author Shou Matsumoto
 *
 */
public class InCliqueConditionalProbabilityExtractor implements
		IArbitraryConditionalProbabilityExtractor {

	/**
	 * default constructor is made protected to at least allow inheritance.
	 * @deprecated use {@link #newInstance()} instead
	 */
	protected InCliqueConditionalProbabilityExtractor() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * default constructor method
	 * @return
	 */
	public static IArbitraryConditionalProbabilityExtractor newInstance() {
		return new InCliqueConditionalProbabilityExtractor();
	}

	/**
	 * Extracts conditional probability assuming that every nodes are in the same clique.
	 * It assumes that {@link unbbayes.prs.bn.JunctionTreeAlgorithm} was run prior to this method.
	 * @param algorithm : will be ignored.
	 * @see unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor#buildCondicionalProbability(unbbayes.prs.INode, java.util.List, unbbayes.prs.Graph, unbbayes.util.extension.bn.inference.IInferenceAlgorithm)
	 * @throws NoCliqueException when there is no clique satisfying input conditions.
	 */
	public IProbabilityFunction buildCondicionalProbability(INode mainNode, List<INode> parentNodes, Graph net, IInferenceAlgorithm algorithm) throws NoCliqueException {
		// assertion
		if (mainNode == null) {
			throw new NullPointerException("mainNode == null");
		}
		
		// guarantee that parentNodes != null
		if (parentNodes == null) {
			parentNodes = new ArrayList<INode>();
		}
		
		// this implementation does not allow a node conditioned to itself, or the conditions to contain the same node twice. Check it by finding duplicates
		if (!parentNodes.isEmpty()) {
			Set<INode> setToCheckDuplicates = new HashSet<INode>();
			setToCheckDuplicates.add(mainNode);
			for (INode condition : parentNodes) {
				if (setToCheckDuplicates.contains(condition)) {
					// TODO use resource files
					throw new IllegalArgumentException(condition + " is a duplicate condition for " + mainNode);
				}
				setToCheckDuplicates.add(condition);
			}
		}
		
		PotentialTable ret = new ProbabilisticTable();
		ret.addVariable(mainNode);
		
		// If there are no parents, then it is just "marginal" probability instead of "conditional" probability
		if (parentNodes.size() <= 0) {
			// fill ret with marginal probability
			for (int i = 0; i < ret.tableSize(); i++) { // Note: ret.tableSize() == mainNode.getStatesSize() == size of the marginal
				ret.setValue(i, ((TreeVariable) mainNode).getMarginalAt(i));
			}
			return ret;
		}
		
		// add parentNodes in the same order
		for (INode node : parentNodes) {
			ret.addVariable(node);
		}
		
		// find clique containing all variables
		Clique clique = null;
		if (net instanceof SingleEntityNetwork) {
			// Note: parentNodes != null && mainNode != null at this point
			Set<INode> nodes = new HashSet<INode>(parentNodes);
			nodes.add(mainNode);
			nodes.remove(null);	// do not allow null content
			clique = this.getCliqueContainingAllNodes((SingleEntityNetwork) net, nodes);
		}
		if (clique == null) {
			// TODO use resource files
			// there is no clique with mainNode and parentNodes simultaneously
//			String message = "Clique {" + mainNode;
//			for (INode node : parentNodes) {
//				message += ", " + node;
//			}
//			message += "} == null";
			String message = "No clique containing the following nodes simultaneously:\n " + mainNode;
			for (INode node : parentNodes) {
				message += ", " + node;
			}
			throw new NoCliqueException(message);
		}
		
		int retIndex = 0;	// this index is used for filling values of ret (see following "for" loop)

		/*
		 * Iterate over states of parents.
		 * E.G. given that mainNode (A) has 2 states (a1, a2), and there are 2 parents (B and C) both with 2 states ({b1,b2}, {c1,c2}), then:
		 * -------------------------------------------------------------------------------------
		 *|		|c1					| c1				| c2				| c2				|
		 *|		|b1					| b2				| b1				| b2				|
		 * -------------------------------------------------------------------------------------
		 *|a1	|<P(A=a1|B=b1,C=c1)>|<P(A=a1|B=b2,C=c1)>|<P(A=a1|B=b1,C=c2)>|<P(A=a1|B=b2,C=c2)>|
		 *|a2	|<P(A=a2|B=b1,C=c1)>|<P(A=a2|B=b2,C=c1)>|<P(A=a2|B=b1,C=c2)>|<P(A=a2|B=b2,C=c2)>|
		 * -------------------------------------------------------------------------------------
		 * 
		 * The conditional probability <P(A=a2|B=b1,C=c1)> is the marginal of A=a2 after adding B=b1 and C=c1 as evidences.
		 * 
		 * If all variables are in a same clique, there is no need to propagate to other cliques.
		 */
		for (Integer[] evidenceIndexes : new SharedArrayParentStatesIndexIterator(parentNodes)) {
			
			// clone clique potential, so that it won't change the original potential
			PotentialTable cloneCliqueTable = (PotentialTable) clique.getProbabilityFunction().clone();
			
			// set parents as evidences. Note: stateIndexes.length == parentNodes.size()
			for (int parentIndex = 0; parentIndex < evidenceIndexes.length; parentIndex++) {
				
				// prepare parameter of method cloneCliqueTable.updateEvidences
				float evidenceMarginal[] = new float[parentNodes.get(parentIndex).getStatesSize()];
				
				// the marginal of a finding is 1 for the evidence state and 0 for all other states.
				for (int i = 0; i < evidenceMarginal.length; i++) {
					evidenceMarginal[i] = 0f;
				}
				evidenceMarginal[evidenceIndexes[parentIndex]] = 1f;	// evidenceIndexes[parentIndex] has the index of the evidence state
				
				// add evidence to clique potential. 
				// We do not need to propagate to other cliques, because we assume all nodes (mainNode and parentNodes) are in the same clique
				cloneCliqueTable.updateEvidences(evidenceMarginal, cloneCliqueTable.indexOfVariable((Node) parentNodes.get(parentIndex)));
			}
			if (algorithm == null 
					|| ( (algorithm instanceof JunctionTreeAlgorithm) && ((JunctionTreeAlgorithm)algorithm).isAlgorithmWithNormalization() ) ) {
				// an algorithm was not specified, or it does not use normalized junction tree. 
				// So by default we normalize, because we inserted evidences
				cloneCliqueTable.normalize(); 
			}
			
			// prepare index of mainNode in the clique potential, so that we can obtain its marginal
			int indexOfMainNode =  cloneCliqueTable.indexOfVariable((Node) mainNode);
			
			// extract marginal of mainNode
			for (int indForMarginal = 0; indForMarginal < clique.getProbabilityFunction().getVariablesSize(); indForMarginal++) {
				if (indForMarginal != indexOfMainNode) {
					cloneCliqueTable.removeVariable(clique.getProbabilityFunction().getVariableAt(indForMarginal));
				}
			}
			
			// fill ret with extracted marginal. Increment retIndex as well. 
			// Note: ret.tableSize() % cloneCliqueTable.tableSize() = 0
			// (i.e. we will never reach a condition where ret is filled completely before cloneCliqueTable is read completely)
			for (int i = 0; (i < cloneCliqueTable.tableSize() ); retIndex++, i++) {
				ret.setValue(retIndex, cloneCliqueTable.getValue(i));
			}
		}
		
		
		return ret;
	}
	
	/** 
	 * Thrown by {@link InCliqueConditionalProbabilityExtractor#buildCondicionalProbability(INode, List, Graph, IInferenceAlgorithm)} 
	 * when there is no clique satisfying input conditions.
	 */
	public class NoCliqueException extends IllegalArgumentException {
		private static final long serialVersionUID = -5056812711980844824L;
		public NoCliqueException() { super(); }
		public NoCliqueException(String message, Throwable cause) { super(message, cause); }
		public NoCliqueException(String s) { super(s); }
		public NoCliqueException(Throwable cause) { super(cause); }
	}
	
	/**
	 * This is an iterator for iterating over all possible states of a collection of nodes.
	 * The first node in "parents" will be iterated most. 
	 * The returned iterator will contain indexes of states of the parents.
	 * The indexes are represented as a shared array of Integer. Because it is shared, a change in its value will modify 
	 * 
	 * For example, if parents = {A,B,C}, and the possible values of A={a1,a2}, B={b1,b2}, and C={c1,c2}; then the 
	 * returned iterator will be 
	 * 
	 * {[0,0,0], [1,0,0], [0,1,0], [1,1,0], [0,0,1], [1,0,1], [0,1,1], [1,1,1]};
	 * 
	 * which means 
	 * 
	 * {[a1,b1,c1], [a2,b1,c1], [a1,b2,c1], [a2,b2,c1],[a1,b1,c2],[a2,b1,c2],[a1,b2,c2],[a2,b2,c2]}.
	 * 
	 * The a
	 * 
	 */
	protected class SharedArrayParentStatesIndexIterator implements Iterator<Integer[]>, Iterable<Integer[]> {

		private Integer array[];
		private final List<INode> parents;

		public SharedArrayParentStatesIndexIterator(List<INode> parentNodes) {
			if (parentNodes == null) {
				throw new NullPointerException("parents == null");
			}
			if (parentNodes.size() <= 0) {
				throw new IllegalArgumentException("parents.size() == 0");
			}
			this.parents = parentNodes;
			array = new Integer[parentNodes.size()];
			for (int i = 0; i < array.length; i++) {
				array[i] = 0;	// guarantee that everything starts from 0
			}
			array[0] = -1;	// a call to next() will set array[0] = 0;
		}
		
		public boolean hasNext() {
			for (int i = 0; i < array.length; i++) {
				if (array[i] < parents.get(i).getStatesSize() - 1) {
					// found at least 1 index below maximum.
					return true;
				}
			}
			// all indexes were at their maximum
			return false;
		}

		public Integer[] next() {
			for (int i = 0; i < array.length; i++) {
				array[i]++;	// the first call shall set array[0] to 0.
				if (array[i] < parents.get(i).getStatesSize()) {
					// there are more states for that parent.
					break;	// no need to increment indexes for next parents
				} else {
					// it was the last state. 
					array[i] = 0;	// Reset state of current parent
					// go to next parent
					continue;
				}
			}
			return array;
		}

		public void remove() {}

		public Iterator<Integer[]> iterator() {
			return this;
		}
		
	}
	
	/**
	 * Uses the tree structure of cliques in order to recursively visit only cliques containing
	 * mainNode.
	 * @param mainNode : only cliques containing this node will be evaluated (so, this is the primary filter).
	 * @param includedParentNodes : only nodes in cliques containing these nodes and mainNode simultaneously will be returned.
	 * @param currentClique : current clique recursively visited. Parent and children of this clique will be visited in next recursive call.
	 * @param visitedCliques : cliques in this set will not be re-visited
	 * @return nodes in cliques containing mainNode and includedParentNodes simultaneously (this list will also include includedParentNodes). 
	 * The mainNode will not be included in the returned list.  
	 * If empty, there is no clique containing mainNode and includedParentNodes simultaneously.
	 */
	private Set<INode> getValidConditionNodesRec(INode mainNode, List<INode> includedParentNodes, Clique currentClique, Set<Clique> visitedCliques) {
		Set<INode> ret = new HashSet<INode>();
		if (visitedCliques == null) {
			visitedCliques = new HashSet<Clique>();
		} else if (visitedCliques.contains(currentClique)) {
			return ret;
		}
		visitedCliques.add(currentClique);
		if (currentClique.getNodes() != null && currentClique.getNodes().contains(mainNode)) {
			if (currentClique.getNodes().containsAll(includedParentNodes)) {
				ret.addAll(currentClique.getNodes());
				ret.remove(mainNode);
			}
			if (currentClique.getParent() != null) {
				ret.addAll(getValidConditionNodesRec(mainNode, includedParentNodes, currentClique.getParent(), visitedCliques));
			}
			if (currentClique.getChildren() != null) {
				for (Clique childClique : currentClique.getChildren()) {
					ret.addAll(getValidConditionNodesRec(mainNode, includedParentNodes, childClique, visitedCliques));
				}
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor#getValidConditionNodes(unbbayes.prs.INode, java.util.List, unbbayes.prs.Graph, unbbayes.util.extension.bn.inference.IInferenceAlgorithm)
	 */
	public List<INode> getValidConditionNodes(INode mainNode, List<INode> includedParentNodes, Graph net, 
			IInferenceAlgorithm algorithm) {	// algorithm is ignored
		if (includedParentNodes != null && mainNode != null && includedParentNodes.contains(mainNode)) {
			throw new IllegalArgumentException(mainNode + " cannot be conditioned to itself.");
		}
		try {
			// optimization: use associated clique/separator and adjacent ones
			if (mainNode instanceof TreeVariable) {
				TreeVariable treeVar = (TreeVariable) mainNode;
				IRandomVariable cliqueOrSep = treeVar.getAssociatedClique();
				if (cliqueOrSep instanceof Separator) {
					Separator separator = (Separator) cliqueOrSep;
					// visit parent and children of separator.getClique1() recursively
					Set<INode> conditionNodes = this.getValidConditionNodesRec(mainNode, includedParentNodes, separator.getClique1(), null);
					// visit parent and children of separator.getClique2() recursively
					conditionNodes.addAll(this.getValidConditionNodesRec(mainNode, includedParentNodes, separator.getClique2(), null));
					return new ArrayList<INode>(conditionNodes);
				} else if (cliqueOrSep instanceof Clique) {
					// visit parents and child cliques recursively
					return new ArrayList<INode>(this.getValidConditionNodesRec(mainNode, includedParentNodes, (Clique) cliqueOrSep, null));
				}
			}
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(),e);
		}
		Set<INode> ret = new HashSet<INode>();	// use set, so that no repetition is allowed
		if (net != null && (net instanceof SingleEntityNetwork)) {
			// prepare input arguments
			Set<INode> nodes = new HashSet<INode>();
			if (includedParentNodes != null) {
				nodes.addAll(includedParentNodes);
			}
			if (mainNode != null) {
				// do not allow null content
				nodes.add(mainNode);
			}
			nodes.remove(null); // do not allow null content
			// extract all cliques satisfying conditions
			Collection<Clique> cliques = this.getCliquesContainingAllNodes((SingleEntityNetwork) net, nodes, Integer.MAX_VALUE);
			// append nodes which are inside the obtained cliques
			if (cliques != null) {
				for (Clique clique : cliques) {
					ret.addAll(clique.getNodes());
				}
			}
		}
		// do not include itself as valid condition (i.e. a node should never depend to itself).
		ret.remove(mainNode);
		return new ArrayList<INode>(ret);	// convert set to list
	}
	
	/**
	 * This is a wrapper for {@link #getCliquesContainingAllNodes(SingleEntityNetwork, Collection, 1)}
	 * @see #getCliquesContainingAllNodes(SingleEntityNetwork, Collection, int)
	 */
	public Clique getCliqueContainingAllNodes(SingleEntityNetwork singleEntityNetwork, Collection<INode> nodes) {
		Collection<Clique> cliques = this.getCliquesContainingAllNodes(singleEntityNetwork, nodes, 1);
		if (cliques == null || cliques.isEmpty()) {
			return null;
		}
		return  cliques.iterator().next();
	}
	
	/**
	 * @param net : network containing the nodes and the clique to be returned
	 * @param nodes : nodes to be considered as a filter condition.
	 * @param maxCount : maximum number of cliques to be returned.
	 * @return cliques containing all the specified nodes.
	 */
	public Collection<Clique> getCliquesContainingAllNodes(SingleEntityNetwork singleEntityNetwork, Collection<INode> nodes, int maxCount) {
		return singleEntityNetwork.getJunctionTree().getCliquesContainingAllNodes(nodes, maxCount);
//		Collection<Clique> ret = new HashSet<Clique>();
//		if (singleEntityNetwork != null && maxCount > 0) {
//			for (Clique auxClique : singleEntityNetwork.getJunctionTree().getCliques()) {
//				if (auxClique == null) {
//					Debug.println(getClass(), singleEntityNetwork + " has a null clique.");
//					continue;
//				}
//				if (nodes == null || nodes.isEmpty()) {
//					// no filtering
//					ret.add(auxClique);
//				} else if (auxClique.getNodes() != null		// auxClique != null at this point 
//						&& auxClique.getNodes().containsAll(nodes)) {	// nodes != null at this point
//					ret.add(auxClique);
//				}
//				if (ret.size() >= maxCount) {
//					break;
//				}
//			}
//		}
//		return ret;
	}

}
