/**
 * 
 */
package unbbayes.gui.umpst.implementation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

/**
 * Panel to select Random variable or ordinary variable during formula edition
 * @author Diego Marques
 */
public class FormulaVariablePane extends JPanel {	
	
	private static final long serialVersionUID = 1L;
	private UMPSTProject umpstProject;
	private RuleModel rule;
	
	private RVTreeForReplaceInFormula rvTree;
	private OVTreeForReplaceInFormula ovTree;
	private FormulaEditionPane formulaEditionPane;
	
	private CardLayout cardLayout;
	private JPanel variablePanel;
	private JPanel rvPanel;
	private JPanel ovPanel;
	private JPanel attPanel;
	
	private JPanel jpArgTree;
	private JToolBar jtbVariable;
	private JButton btnRV;
	private JButton btnOV;
	private JButton btnAtt;
	
	/** Load resource file from this package */
	private ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.gui.umpst.resources.Resources.class.getName());

	public FormulaVariablePane(UMPSTProject umpstProject, RuleModel rule, FormulaEditionPane formulaEditionPane) {
		super();
		this.umpstProject = umpstProject;
		this.rule = rule;
		this.formulaEditionPane = formulaEditionPane;
		
		btnRV = new JButton("RV");
		btnOV = new JButton("OV");
		btnAtt = new JButton("A");
		
		jtbVariable = new JToolBar();
		jtbVariable.setLayout(new GridLayout(1, 3));
		jtbVariable.add(btnRV);
		jtbVariable.add(btnOV);
		jtbVariable.add(btnAtt);
		jtbVariable.setFloatable(false);
		
		replaceByRVPanel();
		replaceByOVPanel();
		attPanel = replaceByAttPanel();
//		createVariablePanel();
		cardLayout = new CardLayout(); 
		jpArgTree = new JPanel(cardLayout);
		jpArgTree.add("RVTree", rvPanel);
		jpArgTree.add("OVTree", ovPanel);
		jpArgTree.add("AttTree", attPanel);
		cardLayout.show(jpArgTree, "RVTree");
		
		variablePanel = new JPanel(new BorderLayout());
		variablePanel.add(jtbVariable, BorderLayout.NORTH);
		variablePanel.add(jpArgTree, BorderLayout.CENTER);
		
		GridBagLayout gridbag = new GridBagLayout(); 
		GridBagConstraints constraints = new GridBagConstraints(); 
		
		this.setLayout(gridbag); 
		
		constraints.gridx = 0; 
		constraints.gridy = 0; 
		constraints.gridwidth = 1; 
		constraints.gridheight = 1;
		constraints.weightx = 100;
		constraints.weighty = 60; 
		constraints.fill = GridBagConstraints.BOTH; 
		constraints.anchor = GridBagConstraints.CENTER; 
		gridbag.setConstraints(variablePanel, constraints); 
		this.add(variablePanel);
		
		addListeners();
	}
	
//	public void createVariablePanel() {
//		cardLayout = new CardLayout(); 
//		jpArgTree = new JPanel(cardLayout);
//		jpArgTree.add("RVTree", replaceByRVNode());
//		jpArgTree.add("OVTree", replaceByOVNode());
//		jpArgTree.add("AttTree", replaceByAttNode());
//		jpArgTree.revalidate();
//		cardLayout.show(jpArgTree, "RVTree");
//	}
	
	public void replaceByRVPanel() {
		rvPanel = new JPanel(new BorderLayout());
		rvTree = new RVTreeForReplaceInFormula(rule, formulaEditionPane);
		rvPanel.add(rvTree);
	}
	
	public void replaceByOVPanel() {
		ovPanel = new JPanel(new BorderLayout());
		ovTree = new OVTreeForReplaceInFormula(rule, formulaEditionPane);
		ovPanel.add(ovTree);
	}
	
	public JPanel replaceByAttPanel() {
		JPanel attPanel = new JPanel(new BorderLayout());
		return attPanel;
	}
	
	public void addListeners() {
		btnRV.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {				
//				setRVTabActive();
//				rvPanel.remove(rvTree);
//				RVTreeForReplaceInFormula rv = new RVTreeForReplaceInFormula(rule);
//				rvPanel.add(rv);
				rvPanel.remove(rvTree);
				rvTree = new RVTreeForReplaceInFormula(rule, formulaEditionPane);
				rvPanel.add(rvTree);
				rvPanel.revalidate();
				jpArgTree.revalidate();
				cardLayout.show(jpArgTree, "RVTree");
				
			}
		});
		
		btnOV.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				ovPanel.remove(ovTree);
				ovTree = new OVTreeForReplaceInFormula(rule, formulaEditionPane);
				ovPanel.add(ovTree);
				ovPanel.revalidate();
				jpArgTree.revalidate();
				cardLayout.show(jpArgTree, "OVTree");
			}
		});
		
		btnAtt.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				setAttTabActive();
			}
		});
	}
	
	public void setRVTabActive() {		
		cardLayout.show(jpArgTree, "RVTree");
	}
	
	public void setOVTabActive() {
		cardLayout.show(jpArgTree, "OVTree");
	}
	
	public void setAttTabActive() {
		cardLayout.show(jpArgTree, "AttTree");
	}
	
	public void showErrorMessage(String msg){
		JOptionPane.showMessageDialog(null, msg , resource.getString("error"), JOptionPane.ERROR_MESSAGE);	
	}

}
