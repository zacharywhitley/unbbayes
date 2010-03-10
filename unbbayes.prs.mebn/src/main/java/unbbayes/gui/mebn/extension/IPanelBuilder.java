/**
 * 
 */
package unbbayes.gui.mebn.extension;

import javax.swing.JComponent;

/**
 * This is a general purpose interface for
 * classes which builds a panel.
 * This is mostly used by the plugin framework
 * in order to build option panels.
 * @author Shou Matsumoto
 *
 */
public interface IPanelBuilder {
	
	/**
	 * Obtains the panel containing a form to edit
	 * attributes of the managed component (e.g. knowledge base
	 * or ssbn generation algorithm)
	 * You may consider this method as a builder too.
	 * 
	 * @return : a panel for options. If null, it will be ignored.
	 */
	public JComponent getPanel();
	
}
