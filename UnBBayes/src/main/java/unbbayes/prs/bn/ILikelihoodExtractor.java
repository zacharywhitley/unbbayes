/**
 * 
 */
package unbbayes.prs.bn;

import java.util.List;

import unbbayes.prs.Graph;
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
	 * Extract the likelihood ratio from a nodes.
	 * @param node
	 * @param graph : the graph containing node. This object may be used for extracting global information about node.
	 * @return likelihood ratio
	 */
	public float[] extractLikelihoodRatio(Graph graph, INode node);
	
	/**
	 * If the likelihood to be extracted in {@link #extractLikelihoodRatio(Graph, INode)}
	 * is a function of other variables, then this method extracts all variables related to that likelihood ratio.
	 * For instance, if {@link #extractLikelihoodParents(Graph, INode)} represents a conditional soft evidence,
	 * then this method will extract all nodes representing the conditions of that conditional soft evidence.
	 * @param graph
	 * @param node
	 * @return
	 */
	public List<INode> extractLikelihoodParents(Graph graph, INode node);
}
