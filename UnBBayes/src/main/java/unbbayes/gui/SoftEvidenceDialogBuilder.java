/**
 * 
 */
package unbbayes.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;

import unbbayes.controller.IconController;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class SoftEvidenceDialogBuilder extends LikelihoodEvidenceDialogBuilder {

	private JTable potentialJTable;
	private IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = InCliqueConditionalProbabilityExtractor.newInstance();
	
	/**
	 * 
	 */
	public SoftEvidenceDialogBuilder() {
		// TODO Auto-generated constructor stub
	}

	
	
	
	/**
	 * @param potentialJTable the potentialJTable to set
	 */
	public void setPotentialJTable(JTable guiPotentialTable) {
		this.potentialJTable = guiPotentialTable;
	}


	/**
	 * @return the potentialJTable
	 */
	public JTable getPotentialJTable() {
		return potentialJTable;
	}

//	/*
//	 * (non-Javadoc)
//	 * @see unbbayes.gui.LikelihoodEvidenceDialogBuilder#buildMainPanel(unbbayes.prs.Graph, unbbayes.prs.INode, javax.swing.JTable)
//	 */
//	protected JPanel buildMainPanel(final Graph graph, final INode nodeToAddLikelihood, JTable table) {
//		final JPanel ret = super.buildMainPanel( graph, nodeToAddLikelihood, table);
//		
//		JToolBar toolBar = new JToolBar(getResource().getString("addFindingTip"), JToolBar.HORIZONTAL);
//		
//		JButton addNodeButton = new JButton(IconController.getInstance().getMoreIcon());
//		addNodeButton.setToolTipText(getResource().getString("addNodeInConditionalEvidence"));
//		toolBar.add(addNodeButton);
//		
//		// prepare possible options for nodeToAddLikelihood
//		
//		addNodeButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				Node option = (Node) JOptionPane.showInputDialog(
//						ret, 
//						getResource().getString("nodeName"), 
//						getResource().getString("selectOneVariable"), 
//						JOptionPane.QUESTION_MESSAGE, 
//						IconController.getInstance().getYellowBallIcon(), 
//						graph.getNodes().toArray(), 
//						graph.getNodes().get(0)
//					);
//				if (option != null) {
//					if (nodeToAddLikelihood instanceof TreeVariable) {
//						Debug.println(getClass(), "New parent for " + nodeToAddLikelihood + " is " + option);
//						TreeVariable node = (TreeVariable) nodeToAddLikelihood;
//						node.getLikelihoodParents().add(option);
//					}
//				}
//			}
//		});
//		
//		
//		JButton removeNodeButton = new JButton(IconController.getInstance().getLessIcon());
//		removeNodeButton.setToolTipText(getResource().getString("removeNodeInConditionalEvidence"));
//		toolBar.add(removeNodeButton);
//		
//		ret.add(toolBar, 0);
//		
//		return ret;
//	}



	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.LikelihoodEvidenceDialogBuilder#buildTableForLikelihoodEvidenceInput(unbbayes.prs.Graph, unbbayes.prs.INode)
	 */
	protected ProbabilisticTable buildTableForLikelihoodEvidenceInput(Graph graph, INode nodeToAddLikelihood) {
		if (nodeToAddLikelihood instanceof TreeVariable) {
			return (ProbabilisticTable) this.getConditionalProbabilityExtractor().buildCondicionalProbability(nodeToAddLikelihood, ((TreeVariable) nodeToAddLikelihood).getLikelihoodParents(), graph, null);
		}
		return (ProbabilisticTable) this.getConditionalProbabilityExtractor().buildCondicionalProbability(nodeToAddLikelihood, null, graph, null);
	}




	/**
	 * @param conditionalProbabilityExtractor the conditionalProbabilityExtractor to set
	 */
	public void setConditionalProbabilityExtractor(
			IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor) {
		this.conditionalProbabilityExtractor = conditionalProbabilityExtractor;
	}




	/**
	 * @return the conditionalProbabilityExtractor
	 */
	public IArbitraryConditionalProbabilityExtractor getConditionalProbabilityExtractor() {
		return conditionalProbabilityExtractor;
	}


}
