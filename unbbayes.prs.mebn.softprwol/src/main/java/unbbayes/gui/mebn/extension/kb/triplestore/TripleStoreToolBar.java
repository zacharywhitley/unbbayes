package unbbayes.gui.mebn.extension.kb.triplestore;

import javax.swing.JButton;
import javax.swing.JToolBar;

import unbbayes.prs.mebn.kb.KnowledgeBase;

/**
 * Tool Bar with options to work with triplestore. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 *
 */
public class TripleStoreToolBar extends JToolBar{

	KnowledgeBase kb; 
	
	public TripleStoreToolBar(){
		super(); 
		add(new JButton("Configure"));  //Maybe on inicial panel
		add(new JButton("Connect"));    //Maybe on inicial panel 
		add(new JButton("LoadTBox")); 
		add(new JButton("Query")); 
	}
	
	public KnowledgeBase getKb() {
		return kb;
	}

	public void setKb(KnowledgeBase kb) {
		this.kb = kb;
	}

	
}
