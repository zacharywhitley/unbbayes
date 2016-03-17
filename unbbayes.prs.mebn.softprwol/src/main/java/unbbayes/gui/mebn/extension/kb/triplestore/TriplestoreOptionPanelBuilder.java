package unbbayes.gui.mebn.extension.kb.triplestore;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import unbbayes.gui.mebn.extension.kb.IKBOptionPanelBuilder;
import unbbayes.prs.mebn.kb.KnowledgeBase;

public class TriplestoreOptionPanelBuilder extends JScrollPane implements IKBOptionPanelBuilder {

	private KnowledgeBase kb;
	
	public TriplestoreOptionPanelBuilder(){
		
	}
	
	@Override
	public JComponent getPanel() {
		JComponent panel = new JPanel(); 
		return panel;
	}

	@Override
	public void commitChanges() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void discardChanges() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setKB(KnowledgeBase kb) {
		if (this.kb == kb) {
			// no change. Do nothing
			return;
		}
		this.kb = kb;
	}

	@Override
	public KnowledgeBase getKB() {
		return kb;
	}

}
