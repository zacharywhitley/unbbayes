package unbbayes.gui.mebn.extension.kb.triplestore;

import javax.swing.JToolBar;

import unbbayes.gui.mebn.extension.kb.IKBToolBarBuilder;
import unbbayes.prs.mebn.kb.KnowledgeBase;

public class TriplestoreToolBarBuilder implements IKBToolBarBuilder{

	KnowledgeBase kb; 
	
	TripleStoreToolBar triplestoreBar; 
	
	@Override
	public JToolBar getToolBar() {
		if(triplestoreBar == null){
			triplestoreBar = new TripleStoreToolBar(); 
		}
		return triplestoreBar ;
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
