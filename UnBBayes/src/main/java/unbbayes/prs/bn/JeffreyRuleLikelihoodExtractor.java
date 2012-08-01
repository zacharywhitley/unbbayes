/**
 * 
 */
package unbbayes.prs.bn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor;
import unbbayes.util.Debug;

/**
 * This is the implementation of {@link ILikelihoodExtractor} to be used
 * for soft evidences. 
 * This class uses Jeffrey's rule to convert soft evidences to likelihood ratios,
 * so that {@link JunctionTreeAlgorithm} can handle it as virtual nodes
 * @author Shou Matsumoto
 *
 */
public class JeffreyRuleLikelihoodExtractor implements ILikelihoodExtractor {
	
	private IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = InCliqueConditionalProbabilityExtractor.newInstance();

	/**
	 * Default constructor is protected in order to at least allow inheritance.
	 * @deprecated use {@link #newInstance()} instead.
	 */
	protected JeffreyRuleLikelihoodExtractor() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Default constructor method
	 * @return
	 */
	public static ILikelihoodExtractor newInstance() {
		return new JeffreyRuleLikelihoodExtractor();
	}

	/**
	 * Converts a soft evidence (probability user expects)
	 * to a likelihood ratio, so that {@link JunctionTreeAlgorithm#addVirtualNode(List)}
	 * can be used to implement soft evidences.
	 * This basically uses {@link TreeVariable#getLikelihood()} as the expected probability,
	 * and {@link TreeVariable#getMarginalAt(int)} as the actual probability, and the likelihood ratio
	 * is calculated as "expected probability" / "actual probability".
	 * @see unbbayes.prs.bn.ILikelihoodExtractor#extractLikelihoodRatio(java.util.List)
	 * TODO change this method so that it can calculate the likelihood ratio for
	 * a conditional evidence (when nodes has more than 1 element)
	 */
	public float[] extractLikelihoodRatio(Graph graph, INode node) {
		if (node == null) {
			throw new IllegalArgumentException("nodes == null || nodes.isEmpty()");
		}
		
		TreeVariable mainNode = (TreeVariable)node;
		
//		if (mainNode.getLikelihoodParents() != null && !mainNode.getLikelihoodParents().isEmpty()) {
//			throw new IllegalArgumentException("Conditional soft evidence not supported yet");
//		}
		
		// extract the main node
		
		// this is the probability user expects
		float expectedProbability[] = mainNode.getLikelihood();
		
		// this is going to be the ratio. 
		float ratio[] = new float[expectedProbability.length];
		
		// Note: expectedProbability.length == ratio.length == length of a line in the CPT
		
		
		// prepare current probability distribution (just the marginal probability, if this is not a conditional soft evidence)
		float[] currentProbability = this.getCurrentProbability(graph, mainNode);
		if (currentProbability == null) {
			// TODO use resource files
			throw new RuntimeException("Could not extract current probability from " + node + " in " + graph);
		}
		
		float total = 0;	// this value will be used to normalize ratio
		// calculate ratio
		for (int i = 0; i < ratio.length; i++) {
			// The actual probability can be retrieved from the main node's marginal
			ratio[i] = expectedProbability[i] / currentProbability[i];
			total += ratio[i];
			
			/*
			 * We shall normalize probs of states of main node.
			 * E.g. for P(A|B), then the main node is A. Suppose states of A = {a1, a2} and B = {b1, b2}, then we shall normalize like the following:
			 * 
			 * Original "table" (the actual vector is unidimensional, though)
			 * ---------------
			 * |    |b1  | b2 |
			 * ----------------
			 * |a1  |.9	 | 99 |
			 * |a2  |.5	 | 13 |
			 * ----------------
			 * ----------------
			 * |Tot |113.4    |
			 * ----------------
			 * 
			 * Normalized
			 * ----------------------
			 * |    |b1       |   b2     |
			 * --------------------------
			 * |a1  |.9/113.4 | 99/113.4 |
			 * |a2  |.5/113.4 | 13/113.4 |
			 * --------------------------
			 */
			// the following code is only necessary if we want to normalize by column, not by the whole table.
//			if (((i+1) % node.getStatesSize()) == 0) {
//				// i+1 is pointing to first element on the column (thus, i is the last element in the column). Normalize
//				for (int j = i-(node.getStatesSize()-1); j <= i; j++) {
//					// normalize the last "n" ratio (n is the quantity of states of the main node - i.e. the number of lines in the table)
//					ratio[j] /= total;
//				}
//				// reset total, because total is for 1 column (the next column will have different total)
//				total = 0;
//			}
		}
		
		// normalize ratio
		for (int i = 0; i < ratio.length; i++) {
			ratio[i] /= total;
		}
		
		return ratio;
	}
	
	/**
	 * Overwrite this method if you want {@link #extractLikelihoodRatio(Graph, INode)} to obtain current probability in 
	 * a different manner
	 * @param graph : graph containing node
	 * @param node : node containing the likelihood
	 * @return : the current probability values. If this is not a conditional soft evidence, then the value is built based on {@link TreeVariable#getMarginalAt(int)}.
	 * Note that this value will be used to fill the first row in a virtual node's CPT, so the size of this return value is:
	 * - if this is an unconditional evidence, then node.getStatesSize()
	 * - if this is a conditional evidence, then it is the productory of the quantity of states of all nodes 
	 * (i.e. node.stateSize() * node.getLikelihoodParents().get(0).stateSize() * node.getLikelihoodParents().get(1).stateSize() * ...).
	 * It will return null if {@link #getConditionalProbabilityExtractor()} could not be used to build the current conditional probability distribution.
	 * @see #extractLikelihoodParents(Graph, INode) : this is called in order to get node.getLikelihoodParents().
	 */
	protected float[] getCurrentProbability(Graph graph, TreeVariable node) {
		float[] ret = null;	// value to return
		
		if (node.getLikelihoodParents() == null || node.getLikelihoodParents().isEmpty()) {
			ret = new float[node.getStatesSize()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = node.getMarginalAt(i);
			}
		} else {
			IProbabilityFunction table = this.getConditionalProbabilityExtractor().buildCondicionalProbability(node, this.extractLikelihoodParents(graph, node), graph, null);
			if (table instanceof PotentialTable) {
				PotentialTable potTable = (PotentialTable) table;
				ret = new float[potTable.tableSize()];
				for (int i = 0; i < ret.length; i++) {
					ret[i] = potTable.getValue(i);
				}
			}
		}
		
		return ret;
//		throw new RuntimeException("!(table instanceof PotentialTable)");
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.bn.ILikelihoodExtractor#extractLikelihoodParents(unbbayes.prs.Graph, unbbayes.prs.INode)
	 */
	public List<INode> extractLikelihoodParents(Graph graph, INode node) {
		List<INode> ret = null;
		try {
			ret = new ArrayList<INode>(((TreeVariable)node).getLikelihoodParents());
		} catch (ClassCastException e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		if (ret == null) {
			// return empty list instead of null
			ret = new ArrayList<INode>();
		}
		return ret;
	}

	/**
	 * This object is used in {@link #getCurrentProbability(int, Graph, TreeVariable)} in order
	 * to obtain what is the current probability distribution of the network, so that a ratio
	 * can be calculated between the current values and desired values.
	 * @param conditionalProbabilityExtractor the conditionalProbabilityExtractor to set
	 */
	public void setConditionalProbabilityExtractor(
			IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor) {
		this.conditionalProbabilityExtractor = conditionalProbabilityExtractor;
	}

	/**
	 * This object is used in {@link #getCurrentProbability(int, Graph, TreeVariable)} in order
	 * to obtain what is the current probability distribution of the network, so that a ratio
	 * can be calculated between the current values and desired values.
	 * @return the conditionalProbabilityExtractor
	 */
	public IArbitraryConditionalProbabilityExtractor getConditionalProbabilityExtractor() {
		return conditionalProbabilityExtractor;
	}

}
