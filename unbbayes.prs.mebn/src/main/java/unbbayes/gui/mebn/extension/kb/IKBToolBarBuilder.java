package unbbayes.gui.mebn.extension.kb;

import javax.swing.JToolBar;

import unbbayes.prs.mebn.kb.KnowledgeBase;

public interface IKBToolBarBuilder{

	public JToolBar getToolBar();
	
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
