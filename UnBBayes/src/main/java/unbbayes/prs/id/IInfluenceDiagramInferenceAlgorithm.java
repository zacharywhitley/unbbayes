/**
 * 
 */
package unbbayes.prs.id;

import java.util.List;

import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.util.extension.bn.inference.IInferenceAlgorithm;

/**
 * Algorithms implementing this interface can compile influence diagrams
 * (i.e. networks with {@link DecisionNode} and/or {@link UtilityNode}).
 * @author Shou Matsumoto
 *
 */
public interface IInfluenceDiagramInferenceAlgorithm extends IInferenceAlgorithm {
	
	/**
	 * @return true if this algorithm requires that {@link DecisionNode} are totally ordered.
	 * False otherwise.
	 * @see #sortDecisionNodes(Graph)
	 */
	boolean isDecisionTotalOrderRequired();
	
	/**
	 * This method should assert that if {@link #isDecisionTotalOrderRequired()} is true, then
	 * it will make changes in the graph to guarantee total order of {@link DecisionNode}.
	 * If {@link #isDecisionTotalOrderRequired()} is false, then this method may simply return the list of decision nodes.
	 * @param graph : the network where {@link DecisionNode} will be searched, and new arcs will be eventually created.
	 * @throws Exception  : if the ordering of decision nodes could not be established.
	 * @returns decision nodes properly ordered
	 * @see #isDecisionTotalOrderRequired()
	 */
	public List<INode> sortDecisionNodes(Graph graph) throws Exception;
}
