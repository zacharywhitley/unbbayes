/**
 * 
 */
package unbbayes.prs.bn;

import java.util.List;

import unbbayes.prs.INode;

/**
 * This is the implementation of {@link ILikelihoodExtractor} to be used
 * for soft evidences. 
 * This class uses Jeffrey's rule to convert soft evidences to likelihood ratios,
 * so that {@link JunctionTreeAlgorithm} can handle it as virtual nodes
 * @author Shou Matsumoto
 *
 */
public class JeffreyRuleLikelihoodExtractor implements ILikelihoodExtractor {

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
	public float[] extractLikelihoodRatio(List<INode> nodes) {
		if (nodes == null || nodes.size() <= 0) {
			throw new IllegalArgumentException("nodes == null || nodes.isEmpty()");
		}
		// TODO support conditional soft evidence
		if (nodes.size() > 1) {
			throw new IllegalArgumentException("nodes.size() == " + nodes.size() + " not supported");
		}
		
		// extract the main node
		TreeVariable mainNode = (TreeVariable)nodes.get(0);
		
		// this is the probability user expects
		float expectedProbability[] = mainNode.getLikelihood();
		
		// this is the ratio. 
		float ratio[] = new float[mainNode.getStatesSize()];
		
		// Note: expectedProbability.length == mainNode.getStatesSize() == ratio.length == mainNode.marginalList.length
		
		float total = 0;	// this value will be used to normalize ratio
		
		// calculate ratio
		for (int i = 0; i < ratio.length; i++) {
			// The actual probability can be retrieved from the main node's marginal
			ratio[i] = expectedProbability[i] / mainNode.getMarginalAt(i);
			total += ratio[i];
		}
		
		// normalize ratio
		for (int i = 0; i < ratio.length; i++) {
			ratio[i] /= total;
		}
		
		return ratio;
	}

}
