/**
 * 
 */
package unbbayes.prs.mebn.extension;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.extension.IPluginNode;
import unbbayes.prs.mebn.IMultiEntityNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.exception.MFragDoesNotExistException;

/**
 * A plugin node must extend this class, so that UnBBayes-MEBN can understand that
 * currently selected node shall have special treatment.
 * Besides, UnBBayes's basic GUI architecture expects all nodes to extend {@link unbbayes.prs.Node}
 * (so, a plugin node must extend some subclass of Node and implement this interface)
 * @author Shou Matsumoto
 *
 *@see unbbayes.util.mebn.extension.manager.MEBNPluginNodeManager
 */
public interface IMEBNPluginNode extends IPluginNode, IMultiEntityNode {
	
	/**
	 * Tells the node to use this mediator (the mediator is usually a
	 * controller {@link unbbayes.controller.mebn.MEBNController})
	 * @param mediator
	 */
	public void setMediator(IMEBNMediator mediator);
	
	/**
	 * This method will be called when this node is added to a MFrag.
	 * @param mfrag
	 * @throws MFragDoesNotExistException 
	 */
	public void onAddToMFrag(MFrag mfrag) throws MFragDoesNotExistException;
}
