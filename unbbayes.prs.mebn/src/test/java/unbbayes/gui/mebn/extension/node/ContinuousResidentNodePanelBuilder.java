/**
 * 
 */
package unbbayes.gui.mebn.extension.node;

import javax.swing.JLabel;
import javax.swing.JPanel;

import unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder;
import unbbayes.prs.Node;

/**
 * @author Shou Matsumoto
 *
 */
public class ContinuousResidentNodePanelBuilder implements
		IProbabilityFunctionPanelBuilder {

	private Node probabilityFunctionOwner;
	
	/**
	 * 
	 */
	public ContinuousResidentNodePanelBuilder() {
		// TODO Auto-generated constructor stub
	}

	
	/* (non-Javadoc)
	 * @see unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder#buildProbabilityFunctionEditionPanel()
	 */
	public JPanel buildProbabilityFunctionEditionPanel() {
		JPanel ret = new JPanel();
		ret.add(new JLabel(this.getProbabilityFunctionOwner().getName()));
		return ret;
	}


	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder#getProbabilityFunctionOwner()
	 */
	public Node getProbabilityFunctionOwner() {
		return probabilityFunctionOwner;
	}


	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder#setProbabilityFunctionOwner(unbbayes.prs.Node)
	 */
	public void setProbabilityFunctionOwner(Node probabilityFunctionOwner) {
		this.probabilityFunctionOwner = probabilityFunctionOwner;
	}

}
