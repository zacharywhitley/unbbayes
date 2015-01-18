/**
 * 
 */
package unbbayes.prs.bn.cpt.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Network;
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

	/** Default implementation of {@link CliqueEvidenceUpdater}. This is used by {@link #buildCondicionalProbability(INode, List, Graph, IInferenceAlgorithm)} */
	public static final CliqueEvidenceUpdater DEFAULT_CLIQUE_EVIDENCE_UPDATER = new CliqueEvidenceUpdater() {
		public void updateEvidenceWithinClique(Clique clique, PotentialTable cliqueTable, float[] marginalMultiplier, Node nodeWithEvidence) {
			cliqueTable.updateEvidences(marginalMultiplier, cliqueTable.indexOfVariable(nodeWithEvidence));
		}
	};
	
	private boolean isToOptimizeFullCliqueConditionalProbEvaluation = true;

	private boolean isToJoinCliquesWhenNoCliqueFound = false;

	/**
	 * default constructor is made protected to at least allow inheritance.
	 * @deprecated use {@link #newInstance()} instead
	 */
	protected InCliqueConditionalProbabilityExtractor() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Default constructor method.
	 * {@link #setToJoinCliquesWhenNoCliqueFound(boolean)} will be set to false by default.
	 * @see #newInstance(boolean)
	 */
	public static IArbitraryConditionalProbabilityExtractor newInstance() {
		return newInstance(false);
	}
	
	/**
	 * default constructor method initializing fields.
	 * @param isToJoinCliquesWhenNoCliqueFound : {@link #setToJoinCliquesWhenNoCliqueFound(boolean)} will be set to this value
	 */
	public static IArbitraryConditionalProbabilityExtractor newInstance(boolean isToJoinCliquesWhenNoCliqueFound) {
		InCliqueConditionalProbabilityExtractor ret = new InCliqueConditionalProbabilityExtractor();
		ret.setToJoinCliquesWhenNoCliqueFound(isToJoinCliquesWhenNoCliqueFound);
		return ret;
	}
	
	/**
	 * Classes implementing this interface will be used by {@link InCliqueConditionalProbabilityExtractor#buildCondicionalProbability(INode, List, Graph, IInferenceAlgorithm, CliqueEvidenceUpdater)}
	 * in order to update clique potentials.
	 * @author Shou Matsumoto
	 */
	public interface CliqueEvidenceUpdater {
		/**
		 * @param clique : clique whose potentials will be updated
		 * @param cliqueTable : the potential table of the clique. This is passed separately from the clique, so that
		 * clones can be used
		 * @param marginalMultiplier : will be multiplied to clique potentials in order to update clique potentials
		 * @param nodeWithEvidence : marginalMultiplier is a marginal for this node.
		 */
		public void updateEvidenceWithinClique(Clique clique, PotentialTable cliqueTable, float marginalMultiplier[], Node nodeWithEvidence);
	}

	/**
	 * Extracts conditional probability assuming that every nodes are in the same clique.
	 * It assumes that {@link unbbayes.prs.bn.JunctionTreeAlgorithm} was run prior to this method.
	 * @param algorithm : will be ignored.
	 * @see unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor#buildCondicionalProbability(unbbayes.prs.INode, java.util.List, unbbayes.prs.Graph, unbbayes.util.extension.bn.inference.IInferenceAlgorithm)
	 * @throws NoCliqueException when there is no clique satisfying input conditions.
	 */
	public IProbabilityFunction buildCondicionalProbability(INode mainNode, List<INode> parentNodes, Graph net, IInferenceAlgorithm algorithm) throws NoCliqueException {
		return this.buildCondicionalProbability(mainNode, parentNodes, net, algorithm, DEFAULT_CLIQUE_EVIDENCE_UPDATER);
	}
	/**
	 * Extracts conditional probability assuming that every nodes are in the same clique.
	 * It assumes that {@link unbbayes.prs.bn.JunctionTreeAlgorithm} was run prior to this method.
	 * @param algorithm : will be ignored.
	 * @param cliqueEvidenceUpdater : this will be used to update evidences locally in clique
	 * @see unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor#buildCondicionalProbability(unbbayes.prs.INode, java.util.List, unbbayes.prs.Graph, unbbayes.util.extension.bn.inference.IInferenceAlgorithm)
	 * @throws NoCliqueException when there is no clique satisfying input conditions.
	 */
	public IProbabilityFunction buildCondicionalProbability(INode mainNode, List<INode> parentNodes, Graph net, IInferenceAlgorithm algorithm, CliqueEvidenceUpdater cliqueEvidenceUpdater) throws NoCliqueException {
//		this.setToOptimizeFullCliqueConditionalProbEvaluation(false);
//		PotentialTable ret2 = (PotentialTable) this.buildCondicionalProbability(mainNode, parentNodes, net, algorithm, cliqueEvidenceUpdater, null);
//		this.setToOptimizeFullCliqueConditionalProbEvaluation(true);
//		PotentialTable ret = (PotentialTable) this.buildCondicionalProbability(mainNode, parentNodes, net, algorithm, cliqueEvidenceUpdater, null);
//		for (int i = 0; i < ret.tableSize(); i++) {
//			if (Math.abs(ret.getValue(i) - ret2.getValue(i)) > 0.0001) {
//				throw new RuntimeException("Optimization yielded different values");
//			}
//		}
//		return ret;
		return this.buildCondicionalProbability(mainNode, parentNodes, net, algorithm, cliqueEvidenceUpdater, null);
	}
	
	/**
	 * This is the same as {@link #buildCondicionalProbability(INode, List, Graph, IInferenceAlgorithm, CliqueEvidenceUpdater)},
	 * but we can specify which clique to use.
	 * @param mainNode
	 * @param parentNodes
	 * @param net
	 * @param algorithm
	 * @param cliqueEvidenceUpdater
	 * @param clique : if set to null, a clique containing mainNode and parentNodes will be selected.
	 * @return
	 * @throws NoCliqueException
	 */
	public IProbabilityFunction buildCondicionalProbability(INode mainNode, List<INode> parentNodes, Graph net, IInferenceAlgorithm algorithm, CliqueEvidenceUpdater cliqueEvidenceUpdater, Clique clique) throws NoCliqueException {
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
		if (clique == null) {
			if (net instanceof SingleEntityNetwork) {
				// Note: parentNodes != null && mainNode != null at this point
				Set<INode> nodes = new HashSet<INode>(parentNodes);
				nodes.add(mainNode);
				nodes.remove(null);	// do not allow null content
				clique = this.getCliqueContainingAllNodes((SingleEntityNetwork) net, nodes);
			}
		} else if (!clique.getNodes().contains(mainNode) || !clique.getNodes().containsAll(parentNodes)) {
			throw new NoCliqueException(clique + " should contain " + mainNode + " and " + parentNodes);
		}
		
		// check if we can simply join a few cliques in order to get conditional probability
		if (clique == null && isToJoinCliquesWhenNoCliqueFound() && !parentNodes.isEmpty()) { 
			// We'll try to get a set of cliques that will together contain all nodes, because could not find a single clique containing all the nodes. 
			// Note that if parentNodes is empty, the method was unable to find any clique containing mainNode. We'll throw exception in such case.
			
			// get all cliques that we know they contain main node
			Collection<Clique> cliquesWithMainNode = this.getCliquesContainingAllNodes((SingleEntityNetwork) net, Collections.singletonList(mainNode), Integer.MAX_VALUE);
			
			// We will fill the following list with cliques that contains main node and also parent nodes
			List<Clique> cliquesAlsoWithSomeOfTheParents = new ArrayList<Clique>(cliquesWithMainNode.size());	
			
			// will use this list to see what parents are remaining to complete set of cliques that together manages main node and parents
			List<INode> remainingParents = new ArrayList<INode>(parentNodes);	
			remainingParents.remove(null);	// make sure there is no null element.
			
			// find clique that has intersection with parentsToFind, insert to clique cliqueSetForNodes, remove intersection, and do again
			for (Clique candidateClique : cliquesWithMainNode) {
				if (remainingParents.isEmpty()) {
					break;
				}
				if (remainingParents.removeAll(candidateClique.getNodesList())) { // if true, then there is intersection
					cliquesAlsoWithSomeOfTheParents.add(candidateClique);
				}
			}
			
			// check that we found cliques and the cliques are a complete set (i.e. contains main node and all parents if considered together)
			if (cliquesAlsoWithSomeOfTheParents.isEmpty() || !remainingParents.isEmpty()) {
				throw new NoCliqueException("There is no complete set of cliques containing main node " + mainNode + " and any of the parents  " + parentNodes + " simultaneously. "
						+ (remainingParents==null?"":("Parents not found: " + remainingParents)));
			}
			
			// get a clone of the the 1st clique in the set, because we'll join the other cliques in it.
			if (net instanceof Network) {
				clique = cliquesAlsoWithSomeOfTheParents.get(0).clone((Network) net);	// will clone clique, but reuse nodes in it
			} else {
				clique = cliquesAlsoWithSomeOfTheParents.get(0).clone(null);	// this will clone clique and also the nodes in it
			}
			cliquesAlsoWithSomeOfTheParents.remove(0);	// make sure we don't double-count the clique we just cloned
			
			// attempt to create a larger clique that includes all nodes
			for (Clique cliqueToJoin : cliquesAlsoWithSomeOfTheParents) {
				// join all other cliques to the cloned clique
				clique.join(cliqueToJoin);
			}
			
			// just let the remaining code to handle joined clique as if it is some existing clique in the junction tree
			
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
		
		// note: at this point, clique contains mainNode and all parentNodes
		
		// check if we can use an optimization which is applicable when getting conditional probabilities in "normalized" context 
		// (e.g. clique potentials contains normalized local joint prob) using all nodes within the same clique
//		if (isToOptimizeFullCliqueConditionalProbEvaluation && cliqueEvidenceUpdater == DEFAULT_CLIQUE_EVIDENCE_UPDATER && clique.getNodesList().size() == parentNodes.size() + 1
//				&& (algorithm == null || ( (algorithm instanceof JunctionTreeAlgorithm) && ((JunctionTreeAlgorithm)algorithm).isAlgorithmWithNormalization() ) ) ) {
		if (isToOptimizeFullCliqueConditionalProbEvaluation && clique.getNodesList().size() == parentNodes.size() + 1 ) {
			// this is just an optimization we can do when we are using default evidence updater (for probabilities with normalization) 
			// and the specified nodes (mainNode and parentNodes) comprises the clique entirely (i.e. clique has only the specified nodes).
			// The condition (algorithm == null || ( (algorithm instanceof JunctionTreeAlgorithm) && ((JunctionTreeAlgorithm)algorithm).isAlgorithmWithNormalization() ) )
			// indicates that the algorithm passed in the argument must use normalized values in clique tables
			
			// step 1: read clique table and re-organize the clique potentials accordingly to the new ordering of nodes (mainNode as pivot and regarding the ordering of nodes in parentNodes)
			
			//extract the clique potential to be read
			PotentialTable cliqueTable = clique.getProbabilityFunction();	
			
			// assert that the size of the tables matches
			if (cliqueTable.tableSize() != ret.tableSize()) {
				throw new RuntimeException("Inconsistency: attempted to generate conditional table of variable " + ret.getVariableAt(0) + " given " + parentNodes 
						+ " with table size = " + ret.tableSize() + ", but the clique table of associated clique " + clique + " had size = " + cliqueTable.tableSize()
						+ ", although a table with same variables should have the same size.");
			}
			
			// assert that the 1st variable is the main node
			if (!ret.getVariableAt(0).equals(mainNode)) {
				throw new RuntimeException("This class assumes that the 1st variable in the potential table is the main node (" + mainNode 
						+ "). This is not happening, so you may be using an incompatible version.");
			}
			
			// array which will map the index of a variable in ret to cliqueTable. 
			int[] mappingOfIndexesOfRetToCliqueTable = new int[ret.variableCount()]; //mappingOfIndexesOfRetToCliqueTable[x] = y means that ret.getVariableAt(x) cliqueTable.getVariableAt(y)
			for (int i = 0; i < mappingOfIndexesOfRetToCliqueTable.length; i++) {
				mappingOfIndexesOfRetToCliqueTable[i] = cliqueTable.indexOfVariable((Node) ret.getVariableAt(i));
			}
			
			// prepare variables
			int[] statesInCliqueTable = new int[cliqueTable.variableCount()];	// array which will contain what are the states of the variables related to the current index in clique table
			int[] statesInNewTable = new int[ret.variableCount()];				// array which will contain what are the states of the variables related to the current index in return table
			
			for (int i = 0; i < cliqueTable.tableSize(); i++) {
				// obtain what are the states of the variables related to current index in clique table
				statesInCliqueTable = cliqueTable.getMultidimensionalCoord(i,statesInCliqueTable);
				// fill statesInNewTable with the indexes in ret (this will basically result in a reordered version of statesInCliqueTable)
				for (int j = 0; j < statesInNewTable.length; j++) {
					statesInNewTable[j] = statesInCliqueTable[mappingOfIndexesOfRetToCliqueTable[j]];
				}
				// fill ret using statesInNewTable as the index
				ret.setValue(statesInNewTable, cliqueTable.getValue(i));
			}
			
			// we can now dispose the vectors, because won't use them anymore (let GC do the work)
			statesInCliqueTable = null;
			statesInNewTable = null;
			mappingOfIndexesOfRetToCliqueTable  = null;
			
			// step 2 : normalize by "column" in order to become conditional probability instead of joint probability
			
			/*
			 * E.G. given that mainNode (A) has 2 states (a1, a2), and there are 2 parents (B and C) both with 2 states ({b1,b2}, {c1,c2}), then:
			 * -------------------------------------------------------------------------------------
			 *|		|c1					| c1				| c2				| c2				|
			 *|		|b1					| b2				| b1				| b2				|
			 * -------------------------------------------------------------------------------------
			 *|a1	|<P(A=a1,B=b1,C=c1)>|<P(A=a1,B=b2,C=c1)>|<P(A=a1,B=b1,C=c2)>|<P(A=a1,B=b2,C=c2)>|
			 *|a2	|<P(A=a2,B=b1,C=c1)>|<P(A=a2,B=b2,C=c1)>|<P(A=a2,B=b1,C=c2)>|<P(A=a2,B=b2,C=c2)>|
			 * -------------------------------------------------------------------------------------
			 * 
			 * Will be converted to
			 * 
			 * -------------------------------------------------------------------------------------
			 *|		|c1					| c1				| c2				| c2				|
			 *|		|b1					| b2				| b1				| b2				|
			 * -------------------------------------------------------------------------------------
			 *|a1	|<P(A=a1|B=b1,C=c1)>|<P(A=a1|B=b2,C=c1)>|<P(A=a1|B=b1,C=c2)>|<P(A=a1|B=b2,C=c2)>|
			 *|a2	|<P(A=a2|B=b1,C=c1)>|<P(A=a2|B=b2,C=c1)>|<P(A=a2|B=b1,C=c2)>|<P(A=a2|B=b2,C=c2)>|
			 * -------------------------------------------------------------------------------------
			 * 
			 * By doing: 
			 * 		<P(A=a1|B=b1,C=c1)> = <P(A=a1,B=b1,C=c1)> / (<P(A=a1,B=b1,C=c1)> + <P(A=a2,B=b1,C=c1)>); (normalizing over column c1b1)
			 * 		<P(A=a2|B=b1,C=c1)> = <P(A=a2,B=b1,C=c1)> / (<P(A=a1,B=b1,C=c1)> + <P(A=a2,B=b1,C=c1)>); (normalizing over column c1b1)
			 * 		<P(A=a1|B=b2,C=c1)> = <P(A=a1,B=b2,C=c1)> / (<P(A=a1,B=b2,C=c1)> + <P(A=a2,B=b2,C=c1)>); (normalizing over column c1b2)
			 * 		<P(A=a2|B=b2,C=c1)> = <P(A=a2,B=b2,C=c1)> / (<P(A=a1,B=b2,C=c1)> + <P(A=a2,B=b2,C=c1)>); (normalizing over column c1b2)
			 * And so on...
			 */
			if (algorithm == null 
					|| ( (algorithm instanceof JunctionTreeAlgorithm) && ((JunctionTreeAlgorithm)algorithm).isAlgorithmWithNormalization() ) ) {
				
				int statesSizeOfMainNode = mainNode.getStatesSize();	// number of states of main node is the number of "lines" in the table
				// tracks sum of the current column, so that the value is used during column-wise normalization
				float sumOfColumn = 0;
				for (int i = 0; i < ret.tableSize(); i++) {
					sumOfColumn += ret.getValue(i);	
					// check whether we reached the last element of the "column"
					if ((i+1) % statesSizeOfMainNode == 0) {
						// reached last element of column, so normalize current column
						if (sumOfColumn <= 0f) {
							// this is an impossible state, so normalization shall do nothing (the column is filled with zero anyway)
						} else {
							// normalize as usual
							for (int indexWithinColumn = i - statesSizeOfMainNode + 1; indexWithinColumn <= i; indexWithinColumn++) {
								ret.setValue(indexWithinColumn, ret.getValue(indexWithinColumn)/sumOfColumn); // normalize and put value
							}
						}
						// reset sum of column, because we reached the end of column
						sumOfColumn = 0f;
					}
				}
			}
			
		} else {
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
				PotentialTable cloneCliqueTable = (PotentialTable) clique.getProbabilityFunction().getTemporaryClone();
				
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
					cliqueEvidenceUpdater.updateEvidenceWithinClique(clique, cloneCliqueTable, evidenceMarginal, (Node) parentNodes.get(parentIndex));
//				cloneCliqueTable.updateEvidences(evidenceMarginal, cloneCliqueTable.indexOfVariable((Node) parentNodes.get(parentIndex)));
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

	/**
	 * If true, {@link #buildCondicionalProbability(INode, List, Graph, IInferenceAlgorithm, CliqueEvidenceUpdater, Clique)}
	 * will use a special optimization when {@link CliqueEvidenceUpdater} is the {@link #DEFAULT_CLIQUE_EVIDENCE_UPDATER}
	 * and the {@link IInferenceAlgorithm} is a {@link JunctionTreeAlgorithm},  {@link JunctionTreeAlgorithm#isAlgorithmWithNormalization()} == true,
	 * and all nodes in the clique are used in the conditional probability evaluation.
	 * @return the isToOptimizeFullCliqueConditionalProbEvaluation
	 */
	public boolean isToOptimizeFullCliqueConditionalProbEvaluation() {
		return this.isToOptimizeFullCliqueConditionalProbEvaluation;
	}

	/**
	 * If true, {@link #buildCondicionalProbability(INode, List, Graph, IInferenceAlgorithm, CliqueEvidenceUpdater, Clique)}
	 * will use a special optimization when {@link CliqueEvidenceUpdater} is the {@link #DEFAULT_CLIQUE_EVIDENCE_UPDATER}
	 * and the {@link IInferenceAlgorithm} is a {@link JunctionTreeAlgorithm},  {@link JunctionTreeAlgorithm#isAlgorithmWithNormalization()} == true,
	 * and all nodes in the clique are used in the conditional probability evaluation.
	 * @param isToOptimizeFullCliqueConditionalProbEvaluation the isToOptimizeFullCliqueConditionalProbEvaluation to set
	 */
	public void setToOptimizeFullCliqueConditionalProbEvaluation(
			boolean isToOptimizeFullCliqueConditionalProbEvaluation) {
		this.isToOptimizeFullCliqueConditionalProbEvaluation = isToOptimizeFullCliqueConditionalProbEvaluation;
	}

	/**
	 * If this is true, then {@link #buildCondicionalProbability(INode, List, Graph, IInferenceAlgorithm)} will
	 * try to join existing cliques if no clique containing all nodes simultaneously is found.
	 * @return the isToJoinCliquesWhenNoCliqueFound
	 */
	public boolean isToJoinCliquesWhenNoCliqueFound() {
		return isToJoinCliquesWhenNoCliqueFound;
	}

	/**
	 * If this is true, then {@link #buildCondicionalProbability(INode, List, Graph, IInferenceAlgorithm)} will
	 * try to join existing cliques if no clique containing all nodes simultaneously is found.
	 * @param isToJoinCliquesWhenNoCliqueFound the isToJoinCliquesWhenNoCliqueFound to set
	 */
	public void setToJoinCliquesWhenNoCliqueFound(
			boolean isToJoinCliquesWhenNoCliqueFound) {
		this.isToJoinCliquesWhenNoCliqueFound = isToJoinCliquesWhenNoCliqueFound;
	}

}
