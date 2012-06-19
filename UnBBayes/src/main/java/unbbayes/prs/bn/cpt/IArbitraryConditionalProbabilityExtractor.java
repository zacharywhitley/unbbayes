/**
 * 
 */
package unbbayes.prs.bn.cpt;

import java.util.List;

import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.bn.Clique;
import unbbayes.prs.bn.IProbabilityFunction;
import unbbayes.prs.bn.JunctionTree;
import unbbayes.prs.bn.JunctionTreeAlgorithm;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * Classes implementing this interface shall be able to dynamically
 * create a table of conditional probabilities for a node
 * and arbitrary parents.
 * @author Shou Matsumoto
 *
 */
public interface IArbitraryConditionalProbabilityExtractor {

	/**
	 * 
	 * @param mainNode : the main variable for the conditional probability
	 * @param parentNodes : the conditions. Use {@link #getValidConditionNodes(INode, List, Graph, IInferenceAlgorithm)} in order
	 * to obtain the available values.
	 * @param net : network containing mainNode and parentNodes. This is used for extracting
	 * any information not directly related to mainNode and parentNode
	 * @param algorithm : inference algorithm used for estimating the conditional
	 * probability. If set to null, it will be assumed that a {@link JunctionTreeAlgorithm}
	 * was used previously for setting up a set of {@link Clique}, and potentials of such
	 * {@link Clique}, available from net ({@link SingleEntityNetwork}) and {@link SingleEntityNetwork#getJunctionTree()}
	 * and {@link JunctionTree#getCliques()} will be used for calculating the 
	 * conditional probabilities.
	 * @return a probabilistic function representing the conditional probability 
	 * P(mainNode | parentNodes)
	 */
	public IProbabilityFunction buildCondicionalProbability(INode mainNode, List<INode> parentNodes, Graph net, IInferenceAlgorithm algorithm);
	
	/**
	 * This method can be used to obtain valid values for the parentNodes of {@link #buildCondicionalProbability(INode, List, Graph, IInferenceAlgorithm)}.
	 * @param mainNode : the main variable to be considered for obtaining valid condition nodes.
	 * @param includedParentNodes : a filter. This method will attempt to find a solution which includes these nodes.
	 * @param net : network containing mainNode and includedParentNodes. This is used for extracting
	 * any information not directly related to mainNode and includedParentNodes.
	 * @param algorithm : inference algorithm used for estimating the valid set of parents. 
	 * If set to null, {@link JunctionTreeAlgorithm} is the default.
	 * @return list of condition nodes which is supposedly valid for being used as parentNodes in {@link #buildCondicionalProbability(INode, List, Graph, IInferenceAlgorithm)}
	 */
	public List<INode> getValidConditionNodes(INode mainNode, List<INode> includedParentNodes, Graph net, IInferenceAlgorithm algorithm);
}
