/**
 * 
 */
package unbbayes.util.extension.bn.inference;

import unbbayes.prs.Graph;
import unbbayes.prs.INode;

/**
 * Classes implementing this interface shall be able to check if a node has  
 * Independence of Causal Influence (ICI) and to handle it property for optimization.
 * @author Shou Matsumoto
 * @see ICIFactorizationJunctionTreeAlgorithm
 */
public interface ICINodeFactorizationHandler {

	/**
	 * Check if node has Independence of Causal Influence (ICI)
	 * @param node : the node to test
	 * @param net : network containing node
	 * @return true if ICI. False otherwise.
	 */
	public boolean isICICompatible(INode node, Graph net);
	
	/**
	 * Factorize, or divorce parents, or make temporal transformation
	 * of parents.
	 * @param node
	 * @param net
	 * @see #isICICompatible(INode, Graph)
	 */
	public void treatICI(INode node, Graph net);
	
	/**
	 * Undo the change performed by {@link #treatICI(INode, Graph)}
	 */
	public void undo();
	
}
