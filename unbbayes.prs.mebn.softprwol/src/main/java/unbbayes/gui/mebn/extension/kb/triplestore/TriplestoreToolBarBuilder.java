package unbbayes.gui.mebn.extension.kb.triplestore;

import javax.swing.JToolBar;

import unbbayes.gui.mebn.extension.kb.IKBToolBarBuilder;
import unbbayes.prs.mebn.kb.KnowledgeBase;

public class TriplestoreToolBarBuilder implements IKBToolBarBuilder{

	KnowledgeBase kb; 
	
	TriplestoreToolBar triplestoreBar; 
	
	@Override
	public JToolBar getToolBar() {
		
		if(triplestoreBar == null){
			triplestoreBar = new TriplestoreToolBar(); 
		}
		
		return triplestoreBar; 
	}

	@Override
	public void setKB(KnowledgeBase kb) {
		this.kb = kb;
		
		if(triplestoreBar!=null){
			triplestoreBar.setKb(kb);
		}
		
	}

	@Override
	public KnowledgeBase getKB() {
		return kb;
	}
	
}
