/**
 * 
 */
package unbbayes.prs.medg.ssid;

import unbbayes.prs.Graph;
import unbbayes.prs.INode;

/**
 * Implementations of this interface generates a new instance of a node given
 * another node as argument.
 * @author Shou Matsumoto
 *
 */
public interface INodeTranslator {

	/**
	 * Obtains another instance of node, using a different representation.
	 * This is mostly used for converting a node to another format of node.
	 * @param node : node to be translated
	 * @param network : net containing the node
	 * @return a new node
	 * @see ISimpleSSIDNodeTranslator
	 */
	public INode translate(INode node, Graph network);
}
