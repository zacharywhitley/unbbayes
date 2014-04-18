/**
 * 
 */
package unbbayes.gui.table.extension;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import unbbayes.prs.Node;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.NoisyMaxNode;
import unbbayes.prs.bn.ProbabilisticNode;

/**
 * Builds a panel to edit CPT of noisy-max nodes
 * @author Shou Matsumoto
 * @see NoisyMaxNode
 */
public class NoisyMaxPanelBuilder implements IProbabilityFunctionPanelBuilder {

	private Node node;

	/**
	 * Default constructor must be public in order to allow plugin infrastructure to easily instantiate class
	 */
	public NoisyMaxPanelBuilder() {	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder#setProbabilityFunctionOwner(unbbayes.prs.Node)
	 */
	public void setProbabilityFunctionOwner(Node node) {
		this.node = node;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder#getProbabilityFunctionOwner()
	 */
	public Node getProbabilityFunctionOwner() {
		return node;
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder#buildProbabilityFunctionEditionPanel()
	 */
	public JPanel buildProbabilityFunctionEditionPanel() {
		JPanel ret = new JPanel(new BorderLayout(0,0));
		Node node = getProbabilityFunctionOwner();
		if (node.getStatesSize() < 2 || node.getParentNodes().size() < 2) {
			// do not render panel if we cannot use noisy-max at all
			ret.add(new JLabel("This version requires at least 2 parents and 2 states for noisy-max distribution."), BorderLayout.CENTER);
//			ret.setBorder(new TitledBorder("Noisy-max parameters"));
		} else  if (node != null && (node instanceof ProbabilisticNode)) {
			// build the noisy max table GUI
			JTable jTable = new NoisyMaxPotentialTableGUI( ((ProbabilisticNode)node).getProbabilityFunction() ).makeTable();
			ret.add(
					new JScrollPane(
							jTable
					)
					,
					BorderLayout.CENTER
				);
			ret.setSize(jTable.getWidth(), jTable.getHeight()+70);
			ret.setPreferredSize(new Dimension(jTable.getWidth(), jTable.getHeight()+70));
			ret.setBorder(new TitledBorder("Required noisy-max parameters. Pressing \"Enter\" after inserting a new probability automatically overwrites original CPT."));
			ret.setToolTipText("Parents' null states are assumed to be their 1st states, and node's last state will be automatically set to 1-previous values.");
		}
		return ret;
	}

}
