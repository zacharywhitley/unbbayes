/**
 * 
 */
package unbbayes.gui.table.extension;

import javax.swing.JPanel;

import unbbayes.prs.INode;

/**
 * This is an interface to handle probability distribution's
 * graphical edition for nodes loaded from plugin manager.
 * It carries basically a node (INode) and a JPanel
 * to edit its probability function.
 * Plugins must implement this interface in order to 
 * permit probability function's visual edition.
 * @author Shou Matsumoto
 *
 */
public interface IPluginNodeProbabilityFunctionPanel {
	
	/**
	 * Sets the node owning the probability distribution function.
	 * @param node
	 */
	public void setProbabilityFunctionOwner(INode node);
	
	
	/**
	 * Obtains the JPanel used to edit the node's probability function
	 * graphically.
	 * @return : a JPanel used to edit node's probability function
	 */
	public JPanel getProbabilityFunctionEditionPanel();

}
