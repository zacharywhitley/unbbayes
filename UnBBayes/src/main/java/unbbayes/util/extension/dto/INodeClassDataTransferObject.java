/**
 * 
 */
package unbbayes.util.extension.dto;

import javax.swing.ImageIcon;

import unbbayes.draw.UShape;
import unbbayes.gui.table.extension.IPluginNodeProbabilityFunctionPanel;
import unbbayes.prs.INode;

/**
 * This is a general interface for classes implementing
 * data transfer objects (objects that are created
 * in order to transfer data into and from several
 * architectural layers), associated with nodes
 * loaded from plugin managers (extensions).
 * 
 * It basically carries a node (INode), a shape
 * (UShape), an Icon, and a panel in order to render
 * forms to edit probability functions (e.g. probability tables)
 * 
 * @author Shou Matsumoto
 *
 */
public interface INodeClassDataTransferObject {

	/**
	 * Obtains the node being transferred
	 * @return
	 */
	public INode getNode();
	
	/**
	 * Sets the node being transferred
	 * @param node
	 */
	public void setNode(INode node);
	
	/**
	 * The shape class used to render the node inside the canvas.
	 * @return
	 */
	public UShape getShape();
	
	/**
	 * The shape class used to render the node inside the canvas.
	 * @param shape
	 */
	public void setShape(UShape shape);
	
	/**
	 * The object containing a panel used by UnBBayes to visually edit
	 * the probability function of a node.
	 * @return
	 */
	public IPluginNodeProbabilityFunctionPanel getProbabilityFunctionPanelHolder();
	
	/**
	 * The object containing a panel used by UnBBayes to visually edit
	 * the probability function of a node.
	 * @param panelHolder
	 */
	public void setProbabilityFunctionPanelHolder(IPluginNodeProbabilityFunctionPanel panelHolder);
	
	/**
	 * The icon of the node.
	 * This is usually used by the GUI in order to 
	 * render the cursor when a "add new plugin node" is
	 * called.
	 * @return
	 */
	public ImageIcon getIcon();
	
	/**
	 * The icon of the node.
	 * This is usually used by the GUI in order to 
	 * render the cursor when a "add new plugin node" is
	 * called.
	 * @param icon
	 */
	public void setIcon(ImageIcon icon);
	
	/**
	 * Icons used by the program to create custom cursors
	 * called when a user presses a "create new plugin node" button.
	 * @return
	 */
	public ImageIcon getCursorIcon();
	
	/**
	 * Icons used by the program to create custom cursors
	 * called when a user presses a "create new plugin node" button.
	 * @param cursorIcon
	 */
	public void setCursorIcon(ImageIcon cursorIcon);

	/**
	 * These methods can be used to store additional parameters into
	 * this DTO.
	 * @param key : cannot be null
	 * @return
	 */
	public Object getObject(String key);
	
	/**
	 * These methods can be used to store additional parameters into
	 * this DTO.
	 * @param key. Cannot be null
	 * @param object. If set to null, the key/object pair will be removed
	 */
	public void setObject(String key, Object object);
	
	
}
