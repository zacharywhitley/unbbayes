/**
 * 
 */
package unbbayes.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;

import unbbayes.controller.IconController;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.bn.cpt.IArbitraryConditionalProbabilityExtractor;
import unbbayes.prs.bn.cpt.impl.InCliqueConditionalProbabilityExtractor;
import unbbayes.prs.bn.cpt.impl.NormalizeTableFunction;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class SoftEvidenceDialogBuilder extends LikelihoodEvidenceDialogBuilder {

	private JTable potentialJTable;
	private IArbitraryConditionalProbabilityExtractor conditionalProbabilityExtractor = InCliqueConditionalProbabilityExtractor.newInstance();
	
//	private List<INode> conditionants = new ArrayList<INode>();
	private JButton addNodeButton;
	private JButton removeNodeButton;
	private JTable table;
	private ProbabilisticTable lastProbabilisticTable;
	private JButton okButton;
	private JButton cancelButton;
	
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

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.LikelihoodEvidenceDialogBuilder#buildMainPanel(unbbayes.prs.Graph, unbbayes.prs.INode, javax.swing.JTable)
	 */
	protected JPanel buildMainPanel(final Graph graph, final INode nodeToAddLikelihood, JTable table) {
		final JPanel ret = new JPanel(new BorderLayout());
		
		
		
//		JLabel label = new JLabel(nodeToAddLikelihood.toString());
//		ret.add(label, BorderLayout.WEST);
		
		JScrollPane jsp = new JScrollPane(table);
		jsp.getViewport().getView().setSize(table.getSize());
		jsp.getViewport().setViewSize(table.getSize());
		jsp.setBorder(new TitledBorder(getResource().getString("softEvidence")));
		ret.add(jsp, BorderLayout.CENTER);
		
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.LikelihoodEvidenceDialogBuilder#buildDialog(unbbayes.prs.Graph, unbbayes.prs.INode, java.awt.Component)
	 */
	public JDialog buildDialog(final Graph graph, final INode nodeToAddLikelihood, final Component owner) {
		
		// the dialog itself
		JDialog ret = null;
		
		// extract window of the owner by going up to the parents
		for (Component window = owner; window != null;) {
			if (window instanceof Frame) {
				ret = new JDialog((Frame)window, nodeToAddLikelihood.toString(), true);	
				break;
			} else if (window instanceof Dialog) {
				ret = new JDialog((Dialog)window, nodeToAddLikelihood.toString(), true);
				break;
			}
			window = window.getParent();
		}
		if (ret == null) {
			ret = new JDialog((Frame)null, nodeToAddLikelihood.toString(), true);	
		}
		
		if (nodeToAddLikelihood instanceof TreeVariable) {
			TreeVariable treeVariable = (TreeVariable) nodeToAddLikelihood;
			if (!treeVariable.hasLikelihood()) {
				// force the treeVariable.getLikelihoodParents() to reset
				treeVariable.resetLikelihood();
			}
		}
		
		this.fillDialog(ret, graph, nodeToAddLikelihood);
		this.fillDialogActionListeners(ret, graph, nodeToAddLikelihood);
		
		return ret;
	}
	
	/**
	 * Fill the content of a dialog. This is used in {@link #buildDialog(Graph, INode, Component)}
	 * @param dialog
	 * @param graph
	 * @param nodeToAddLikelihood
	 */
	protected void fillDialog(JDialog dialog, final Graph graph, final INode nodeToAddLikelihood) {
		
		// buildMainTable throws IllegalArgumentException if buildTableForLikelihoodEvidenceInput throws IllegalArgumentException.
		// this happens when we attempt to add an invalid condition to soft evidence of nodeToAddLikelihood.
		// so, we are doing buildMainTable first, so that all other routines are called after guaranteeing that conditions are consistent
		setTable(this.buildMainTable( graph, nodeToAddLikelihood));
		
		// by now, the conditions are consistent
		
		// destroy all previous content
		dialog.getContentPane().removeAll();
		
		// panel containing tool bar, label and table
		JPanel mainPanel = this.buildMainPanel( graph, nodeToAddLikelihood, getTable());
		
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.setResizable(true);
		
		dialog.getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		
		JToolBar toolBar = new JToolBar(getResource().getString("addFindingTip"), JToolBar.HORIZONTAL);
		toolBar.add(new JLabel(getResource().getString("addConditionsSoftEvidence")));
		
		setAddNodeButton(new JButton(IconController.getInstance().getMoreIcon()));
		getAddNodeButton().setToolTipText(getResource().getString("addNodeInConditionalEvidence"));
		toolBar.add(getAddNodeButton());
		
		setRemoveNodeButton(new JButton(IconController.getInstance().getLessIcon()));
		getRemoveNodeButton().setToolTipText(getResource().getString("removeNodeInConditionalEvidence"));
		toolBar.add(getRemoveNodeButton());
		
		dialog.getContentPane().add(toolBar, BorderLayout.NORTH);
		
		JPanel okCancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		setOkButton(new JButton(getResource().getString("confirmLabel")));
		getOkButton().setToolTipText(getResource().getString("confirmToolTip"));
		okCancelPanel.add(getOkButton());

		setCancelButton(new JButton(getResource().getString("cancelLabel")));
		getCancelButton().setToolTipText(getResource().getString("cancelToolTip"));
		okCancelPanel.add(getCancelButton());
		
		dialog.getContentPane().add(okCancelPanel, BorderLayout.SOUTH);
		
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.pack();
	}
	
	/**
	 * Fill listeners of components created in {@link #fillDialog(JDialog, Graph, INode)}
	 * @param dialog
	 * @param graph
	 * @param nodeToAddLikelihood
	 */
	protected void fillDialogActionListeners(final JDialog dialog, final Graph graph, final INode nodeToAddLikelihood) {
		// prepare possible options for nodeToAddLikelihood
		getAddNodeButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Node option = (Node) JOptionPane.showInputDialog(
						dialog, 
						getResource().getString("nodeName"), 
						getResource().getString("selectOneVariable"), 
						JOptionPane.QUESTION_MESSAGE, 
						IconController.getInstance().getYellowBallIcon(), 
						graph.getNodes().toArray(), 
						graph.getNodes().get(0)
				);
				if (option != null) {
					if (nodeToAddLikelihood instanceof TreeVariable) {	
						Debug.println(getClass(), "New parent for " + nodeToAddLikelihood + " is " + option);
						TreeVariable node = (TreeVariable) nodeToAddLikelihood;
						if (!node.getLikelihoodParents().contains(option)) {
							node.getLikelihoodParents().add(option);
							try {
								fillDialog(dialog, graph, nodeToAddLikelihood);
								fillDialogActionListeners(dialog, graph, nodeToAddLikelihood);
								dialog.repaint();
							} catch (Exception e2) {
								e2.printStackTrace();
								JOptionPane.showMessageDialog(dialog, e2.getMessage(), getResource().getString("error"), JOptionPane.WARNING_MESSAGE);
								// undo
								node.getLikelihoodParents().remove(option);
								// rebuild
								fillDialog(dialog, graph, nodeToAddLikelihood);
								fillDialogActionListeners(dialog, graph, nodeToAddLikelihood);
								dialog.repaint();
							}
						}
					}
				}
			}
		});
		
		// listener for removing last condition
		getRemoveNodeButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// remove the last condition from nodeToAddLikelihood
					if (nodeToAddLikelihood instanceof TreeVariable) {	
						TreeVariable node = (TreeVariable) nodeToAddLikelihood;
						if (!node.getLikelihoodParents().isEmpty()) {
							// only remove is there is something to remove
							node.getLikelihoodParents().remove(node.getLikelihoodParents().size()-1);
							// update dialog
							fillDialog(dialog, graph, nodeToAddLikelihood);
							fillDialogActionListeners(dialog, graph, nodeToAddLikelihood);
							dialog.repaint();
						}
					}
				} catch (Exception e2) {
					e2.printStackTrace();
					JOptionPane.showMessageDialog(dialog, e2.getMessage(), getResource().getString("error"), JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		
		
		// add lister invoked when closing dialog 
		dialog.addComponentListener(this.buildLikelihoodEvidenceDialogComponentListener(graph, nodeToAddLikelihood, getTable(), null));
		
		// commit changes and fill nodeToAddLikelihood.likelihood
		getOkButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (nodeToAddLikelihood instanceof TreeVariable) {
					TreeVariable node = (TreeVariable) nodeToAddLikelihood;
					// getLastProbabilisticTable is holding user edit. First, normalize it
					new NormalizeTableFunction().applyFunction(getLastProbabilisticTable());
					
					// we are doing soft evidence, but the JeffreyRuleLikelihoodExtractor can convert soft evidence to likelihood evidence
					// thus, we are adding the values of soft evidence in the likelihood evidence's vector 
					// (JeffreyRuleLikelihoodExtractor will then use Jeffrey rule to change its contents again)
					// The likelihood evidence vector is the user input CPT converted to a uni-dimensional array
					float likelihood[] = new float[getLastProbabilisticTable().tableSize()];
					for (int i = 0; i < likelihood.length; i++) {
						likelihood[i] = getLastProbabilisticTable().getValue(i);
					}
					
					// the node.getLikelihoodParents() is already filled with correct parents. So, keep it as is.
					node.addLikeliHood(likelihood, node.getLikelihoodParents());
					
					// close dialog
					dialog.setVisible(false);
					dialog.dispose();
				}
			}
		});
		
		// event on cancel button
		getCancelButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// this will call component listener created in buildLikelihoodEvidenceDialogComponentListener, because it is a "hide" dialog event
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.LikelihoodEvidenceDialogBuilder#buildTableForLikelihoodEvidenceInput(unbbayes.prs.Graph, unbbayes.prs.INode)
	 */
	protected ProbabilisticTable buildTableForLikelihoodEvidenceInput(Graph graph, INode nodeToAddLikelihood) {
		if (nodeToAddLikelihood instanceof TreeVariable) {
			TreeVariable treeVariable = (TreeVariable) nodeToAddLikelihood;
			setLastProbabilisticTable((ProbabilisticTable) this.getConditionalProbabilityExtractor().buildCondicionalProbability(nodeToAddLikelihood, treeVariable.getLikelihoodParents(), graph, null));
			return getLastProbabilisticTable();
		}
		setLastProbabilisticTable((ProbabilisticTable) this.getConditionalProbabilityExtractor().buildCondicionalProbability(nodeToAddLikelihood, null, graph, null));
		return getLastProbabilisticTable();
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




//	/**
//	 * @param conditionants the conditionants to set
//	 */
//	public void setConditionants(List<INode> conditionants) {
//		this.conditionants = conditionants;
//	}
//
//
//
//
//	/**
//	 * @return the conditionants
//	 */
//	public List<INode> getConditionants() {
//		return conditionants;
//	}




	/**
	 * Button to add condition in condicional soft evidence. 
	 * @param addNodeButton the addNodeButton to set
	 */
	public void setAddNodeButton(JButton addNodeButton) {
		this.addNodeButton = addNodeButton;
	}




	/**
	 * Button to add condition in condicional soft evidence. 
	 * @return the addNodeButton
	 */
	public JButton getAddNodeButton() {
		return addNodeButton;
	}




	/**
	 * Button to remove condition in condicional soft evidence.
	 * @param removeNodeButton the removeNodeButton to set
	 */
	public void setRemoveNodeButton(JButton removeNodeButton) {
		this.removeNodeButton = removeNodeButton;
	}




	/**
	 * Button to remove condition in condicional soft evidence.
	 * @return the removeNodeButton
	 */
	public JButton getRemoveNodeButton() {
		return removeNodeButton;
	}




	/* (non-Javadoc)
	 * @see unbbayes.gui.LikelihoodEvidenceDialogBuilder#buildLikelihoodEvidenceDialogComponentListener(unbbayes.prs.Graph, unbbayes.prs.INode, javax.swing.JTable, javax.swing.JOptionPane)
	 */
	protected ComponentListener buildLikelihoodEvidenceDialogComponentListener(Graph graph, final INode nodeToAddLikelihood, JTable table, JOptionPane optPane) {
		return new ComponentListener() {
			public void componentShown(ComponentEvent e) {}
			public void componentResized(ComponentEvent e) {}
			public void componentMoved(ComponentEvent e) {}
			public void componentHidden(ComponentEvent e) {
				if (nodeToAddLikelihood instanceof TreeVariable) {
					TreeVariable treeVariable = (TreeVariable) nodeToAddLikelihood;
					if (!treeVariable.hasLikelihood()) {
						// force it to clean treeVariable.getLikelihoodParents()
						treeVariable.resetLikelihood();
					}
				}
			}
		};
	}




	/**
	 * This is the table created in {@link #fillDialog(JDialog, Graph, INode)} based on
	 * {@link #buildTableForLikelihoodEvidenceInput(Graph, INode)}
	 * @param table the table to set
	 */
	public void setTable(JTable table) {
		this.table = table;
	}




	/**
	 * This is the table created in {@link #fillDialog(JDialog, Graph, INode)} based on
	 * {@link #buildTableForLikelihoodEvidenceInput(Graph, INode)}
	 * @return the table
	 */
	public JTable getTable() {
		return table;
	}




	/**
	 * This is the last dummy table created in {@link #buildTableForLikelihoodEvidenceInput(Graph, INode)}
	 * @param lastProbabilisticTable the lastProbabilisticTable to set
	 */
	public void setLastProbabilisticTable(ProbabilisticTable lastProbabilisticTable) {
		this.lastProbabilisticTable = lastProbabilisticTable;
	}




	/**
	 * This is the last dummy table created in {@link #buildTableForLikelihoodEvidenceInput(Graph, INode)}
	 * @return the lastProbabilisticTable
	 */
	public ProbabilisticTable getLastProbabilisticTable() {
		return lastProbabilisticTable;
	}




	/**
	 * @param okButton the okButton to set
	 */
	public void setOkButton(JButton okButton) {
		this.okButton = okButton;
	}




	/**
	 * @return the okButton
	 */
	public JButton getOkButton() {
		return okButton;
	}




	/**
	 * @param cancelButton the cancelButton to set
	 */
	public void setCancelButton(JButton cancelButton) {
		this.cancelButton = cancelButton;
	}




	/**
	 * @return the cancelButton
	 */
	public JButton getCancelButton() {
		return cancelButton;
	}





}
