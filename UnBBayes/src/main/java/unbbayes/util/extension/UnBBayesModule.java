
package unbbayes.util.extension;

import javax.swing.JInternalFrame;

import unbbayes.gui.IPersistenceAwareWindow;
import unbbayes.io.BaseIO;
import unbbayes.prs.Graph;

/**
 * Plugins for UnBBayes core is expected to extend this class.
 * @author Shou Matsumoto
 * @version 16-10-2009
 *
 */
public abstract class UnBBayesModule extends JInternalFrame implements
		IPersistenceAwareWindow {

	/**
	 * Idem a super("Plugin", true, true, true, true);
	 * @see JInternalFrame
	 * 
	 */
	public UnBBayesModule() {
		super("Plugin", true, true, true, true);
	}
	
	/**
	 * @param title
	 * @see JInternalFrame
	 */
	public UnBBayesModule(String title) {
		super(title, true, true, true, true);
	}
	



	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getInternalFrame()
	 */
	public JInternalFrame getInternalFrame() {
		return this;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.IPersistenceAwareWindow#getSavingMessage()
	 */
	public String getSavingMessage() {
		return "Save";
	}

	
}
