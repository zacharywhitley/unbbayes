/**
 * 
 */
package unbbayes.prs.mebn.extension;

import unbbayes.prs.extension.IPluginNode;

/**
 * A plugin node must extend this class, so that UnBBayes-MEBN can understand that
 * currently selected node shall have special treatment.
 * Besides, UnBBayes's basic GUI architecture expects all nodes to extend {@link unbbayes.prs.Node}
 * (so, a plugin node must extend some subclass of Node and implement this interface)
 * @author Shou Matsumoto
 *
 *@see unbbayes.util.mebn.extension.manager.MEBNPluginNodeManager
 */
public interface IMEBNPluginNode extends IPluginNode {

}
