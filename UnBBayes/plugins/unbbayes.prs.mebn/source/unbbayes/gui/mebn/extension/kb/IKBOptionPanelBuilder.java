/**
 * 
 */
package unbbayes.gui.mebn.extension.kb;

import javax.swing.JComponent;

import unbbayes.prs.mebn.kb.KnowledgeBase;

/**
 * This interface builds a panel to be an option panel for a given Knowledge
 * base. This is a part of MEBN's knowledge base's plugin infrastructure.
 * @author Shou Matsumoto
 *
 */
public interface IKBOptionPanelBuilder {
		
	/**
	 * Obtains the panel containing a form to edit
	 * attributes of the knowledge base set by
	 * {@link #setKB(KnowledgeBase)}.
	 * You may consider this method as a builder too.
	 * 
	 * @return : a panel for options. If null, it will be ignored.
	 * @see #setKB(KnowledgeBase)
	 */
	public JComponent getPanel();
	
	/**
	 * Commits the changes done at the panel obtained from {@link #getPanel()}.
	 * In another words, it collects the attributes from {@link #getPanel()}
	 * and fills ths Knowledge base set by {@link #setKB(KnowledgeBase)}.
	 * @see #getPanel()
	 * @see #setKB(KnowledgeBase)
	 */
	public void commitChanges();
	
	/**
	 * Discards the changes done at the panel obtained from {@link #getPanel()}.
	 * In another words, it resets the values of the attributes of {@link #getPanel()}
	 * @see #getPanel()
	 */
	public void discardChanges();
	
	/**
	 * Sets the knowledge base to be altered by the panel obtained from 
	 * {@link #getPanel()}
	 * @param kb
	 * @see #getPanel()
	 */
	public void setKB(KnowledgeBase kb);
	
	/**
	 * The knowledge base obtained by this method must be 
	 * the knowledge base edited by {@link #getPanel()},
	 * which is mostly the knowledge base set by {@link #setKB(KnowledgeBase)}
	 * (unless the implementation has chosen not to do so).
	 * 
	 * @return the currently managed Knowledge Base.
	 * @see #setKB(KnowledgeBase)
	 * @see #getPanel()
	 */
	public KnowledgeBase getKB();
}
