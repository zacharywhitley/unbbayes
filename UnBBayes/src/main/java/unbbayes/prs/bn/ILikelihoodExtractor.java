/**
 * 
 */
package unbbayes.prs.bn;

import java.util.List;

import unbbayes.prs.INode;

/**
 * Objects of classes implementing this interface are able to extract the likelihood
 * ratio from a list of nodes.
 * @author Shou Matsumoto
 * @see JunctionTreeAlgorithm#clearVirtualNodes()
 * @see JunctionTreeAlgorithm#extractLikelihoodRatio(List)
 */
public interface ILikelihoodExtractor {
	
	/**
	 * Extract the likelihood ratio from a list of nodes.
	 * @param nodes
	 * @return likelihood ratio
	 */
	public float[] extractLikelihoodRatio(List<INode> nodes);
	
}
