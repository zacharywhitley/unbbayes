/**
 * 
 */
package unbbayes.prs.bn.inference.extension;

import java.util.Map;

import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * Classes implementing this interface represents junction trees 
 * for explanation problems (i.e. obtaining a set of states which
 * is most likely to happen given evidences).
 * @author Shou Matsumoto
 *
 */
public interface IExplanationJunctionTree {

	/**
	 * @param graph : the graph containing the nodes to be analyzed. It is assumed that 
	 * the graph was already compiled by algorithm
	 * @param algorithm : the algorithm which has compiled the graph.
	 * @return nodes and indexes of most probable states. The state can be retrieved by calling {@link INode#getStateAt(int)}
	 */
	public Map<INode, Integer> calculateExplanation(Graph graph, IInferenceAlgorithm algorithm);
	
	/**
	 * 
	 * @param states : these states will be considered as evidences while calculating probability (the probability 
	 * will be calculated assuming these states as 100%).
	 * @param graph : the graph containing the nodes to be analyzed. It is assumed that 
	 * the graph was already compiled by algorithm
	 * @param algorithm : the algorithm which has compiled the graph.
	 * @return the joint probability value (probability assuming that the states happens).
	 */
	public double calculateJointProbability(Map<INode, Integer> states, Graph graph, IInferenceAlgorithm algorithm);
	
}
