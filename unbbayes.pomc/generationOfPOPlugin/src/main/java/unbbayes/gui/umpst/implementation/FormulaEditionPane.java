package unbbayes.gui.umpst.implementation;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import unbbayes.controller.IconController;
import unbbayes.controller.umpst.FormulaTreeControllerUMP;
import unbbayes.model.umpst.implementation.EventNCPointer;
import unbbayes.model.umpst.implementation.NecessaryConditionVariableModel;
import unbbayes.model.umpst.project.UMPSTProject;
import unbbayes.model.umpst.rule.RuleModel;

/**
 * This class build the pane for edition of the formula of 
 * one ContextNode. The formula is a tree structure. 
 * @author Diego Marques
 */

public class FormulaEditionPane extends JPanel {
	
	JToolBar jtbOperator;
	JPanel upPanel; 
	JPanel argsPanel;
	
	private JScrollPane jspFormulaTree;
//	JToolBar jtbSelectArgTree;
//	JPanel argTreePanel;
//	JScrollPane jspArgTreePanel; 
//	
//	JPanel jpOperandos;  
	
	//buttons of the jpOperator 
	JButton btnEqualTo; 	
	JButton btnAnd; 
	JButton btnOr;
	JButton btnNot;
	JButton btnImplies;
	JButton btnIf; 	
	JButton btnExists;
	JButton btnForAll;
	
	/** Load resource file from this package */
	private ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.gui.umpst.resources.Resources.class.getName());
	private RuleModel rule;
	private NecessaryConditionVariableModel ncVariableModel;
	private UMPSTProject umpstProject;
	private String formula;
	
	private FormulaTreeControllerUMP formulaTreeController;
	private FormulaViewTreePane formulaViewTreePane;
	
	protected IconController iconController = IconController.getInstance();
	
	public FormulaEditionPane(NecessaryConditionEditPanel ncEditPanel, UMPSTProject umpstProject, 
			RuleModel rule, NecessaryConditionVariableModel ncVariableModel, boolean editTree){
		
		super();
		this.umpstProject = umpstProject;
		this.rule = rule;
		this.ncVariableModel = ncVariableModel;
		
		btnEqualTo = new JButton(iconController.getEqualIcon());  	
		btnAnd = new JButton(iconController.getAndIcon())  ; 
		btnOr = new JButton(iconController.getOrIcon())  ;
		btnNot = new JButton(iconController.getNotIcon());  
		btnImplies = new JButton(iconController.getImpliesIcon());  
		btnIf = new JButton(iconController.getIffIcon());   	
		btnExists = new JButton(iconController.getExistsIcon())  ;
		btnForAll = new JButton(iconController.getForallIcon())  ;
		
		btnEqualTo.setToolTipText(resource.getString("equalToToolTip")); 
		btnAnd.setToolTipText(resource.getString("andToolTip"))  ; 
		btnOr.setToolTipText(resource.getString("orToolTip"))  ;
		btnNot.setToolTipText(resource.getString("notToolTip"));  
		btnImplies.setToolTipText(resource.getString("impliesToolTip"));  
		btnIf.setToolTipText(resource.getString("iffToolTip"));   	
		btnExists.setToolTipText(resource.getString("existsToolTip"))  ;
		btnForAll.setToolTipText(resource.getString("forallToolTip"))  ;
		
		jtbOperator = new JToolBar(); 
		jtbOperator.setLayout(new GridLayout(2,4));
		jtbOperator.add(btnEqualTo); 
		jtbOperator.add(btnAnd); 
		jtbOperator.add(btnOr); 
		jtbOperator.add(btnNot); 
		jtbOperator.add(btnImplies); 
		jtbOperator.add(btnIf); 
		jtbOperator.add(btnExists); 
		jtbOperator.add(btnForAll); 	
		jtbOperator.setFloatable(false);
		
		formulaTreeController = new FormulaTreeControllerUMP(ncEditPanel, rule, this, ncVariableModel, editTree);		
		jspFormulaTree = new JScrollPane(formulaTreeController.getFormulaViewTreePane());
		
		upPanel = new JPanel(new BorderLayout()); 
		upPanel.add(jtbOperator, BorderLayout.NORTH);	    
		upPanel.add(jspFormulaTree, BorderLayout.CENTER);		
		
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
		gridbag.setConstraints(upPanel, constraints); 
		this.add(upPanel);
		
//		formulaTreeController = new FormulaTreeController(umpstProject, rule, this);	    
//		jspFormulaTree = new JScrollPane(formulaTreeController.getFormulaTree());
		
//		JTextArea text = new JTextArea(30, 5);
//		jspFormulaTree = new JScrollPane();
		
//		this.add(jspFormulaTree);
		
		addListeners(); 
		
	}
	
	public void setArgumentSelectionTab(EventNCPointer eventNCPointer){		
		argsPanel = new ArgumentsTypedPane(this, rule, eventNCPointer); 
		JScrollPane scroll = new JScrollPane(argsPanel); 
//		jpArgTree.add("ResidentArgsTab", scroll); 
//		cardLayout.show(jpArgTree, "ResidentArgsTab"); 
		
	}
	
	public void addListeners(){
		
		btnAnd.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				try{
					formulaTreeController.addOperatorAnd();
				}
				catch(Exception e){
					showErrorMessage(e.getMessage()); 
				}
			}
		});		
		
		btnOr.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				try{
					formulaTreeController.addOperatorOr(); 
				}
				catch(Exception e){
					showErrorMessage(e.getMessage()); 					
				}
			}
		});				
		
		btnNot.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				try{
					formulaTreeController.addOperatorNot(); 
				}
				catch(Exception e){
					showErrorMessage(e.getMessage()); 					
				}
			}
		});	
		
		btnEqualTo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				try{
					formulaTreeController.addOperatorEqualTo(); 
				}
				catch(Exception e){
					showErrorMessage(e.getMessage()); 					
				}
			}
		});						
		
		btnIf.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				try{
					formulaTreeController.addOperatorIf(); 
				}
				catch(Exception e){
					showErrorMessage(e.getMessage()); 					
				}
			}
		});						
		
		btnImplies.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				try{
					formulaTreeController.addOperatorImplies(); 
				}
				catch(Exception e){
					showErrorMessage(e.getMessage()); 					
				}
			}
		});						
		
		btnForAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				try{
					formulaTreeController.addOperatorForAll(); 
				}
				catch(Exception e){
					showErrorMessage(e.getMessage()); 					
				}
			}
		});						
		
		btnExists.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				try{
					formulaTreeController.addOperatorExists(); 
				}
				catch(Exception e){
					showErrorMessage(e.getMessage()); 					
				}
			}
		});		
	}
	
//	public void setArgumentSelectionTab(ResidentNodePointer residentPointer){		
//		argsPanel = new ArgumentsTypedPane(contextNode, residentPointer, mebnController); 
//		JScrollPane scroll = new JScrollPane(argsPanel); 
//		jpArgTree.add("ResidentArgsTab", scroll); 
//		cardLayout.show(jpArgTree, "ResidentArgsTab");		
//	}
	
	public void showErrorMessage(String msg){
		JOptionPane.showMessageDialog(null, msg , resource.getString("error"), JOptionPane.ERROR_MESSAGE);	
	}

	/**
	 * @return the formulaTreeController
	 */
	public FormulaTreeControllerUMP getFormulaTreeController() {
		return formulaTreeController;
	}

	/**
	 * @param formulaTreeController the formulaTreeController to set
	 */
	public void setFormulaTreeController(FormulaTreeControllerUMP formulaTreeController) {
		this.formulaTreeController = formulaTreeController;
	}

	/**
	 * @return the formula
	 */
	public String getFormula() {
		return formula;
	}

	/**
	 * @param formula the formula to set
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}	
}


