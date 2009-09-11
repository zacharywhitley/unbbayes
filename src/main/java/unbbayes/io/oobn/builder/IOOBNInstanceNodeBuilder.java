/**
 * 
 */
package unbbayes.io.oobn.builder;

import unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper;
import unbbayes.io.builder.INodeBuilder;
import unbbayes.prs.oobn.IOOBNClass;

/**
 * @author Shou Matsumoto
 *
 */
public interface IOOBNInstanceNodeBuilder extends INodeBuilder {
	
	/**
	 * Builds a new instance of instance node
	 * @return instance of Node
	 */
	public OOBNNodeGraphicalWrapper buildInstanceNode(IOOBNClass oobnClass);
}
