package unbbayes.gui.mebn;

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
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import unbbayes.controller.FormulaTreeController;
import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.controller.NetworkController;
import unbbayes.gui.mebn.auxiliary.ToolKitForGuiMebn;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MFrag;

public class FormulaEditionPane extends JPanel {
	
	JToolBar jtbOperator;
	JPanel upPanel; 
	JPanel downPanel; 	
	
	JScrollPane jspFormulaTree; 
	JToolBar jtbSelectArgTree;
	JPanel argTreePanel;
	JScrollPane jspArgTreePanel; 
	
	JPanel jpOperandos;  
	
	//buttons of the jpOperator 
	
	JButton btnEqualTo; 	
	JButton btnAnd; 
	JButton btnOr;
	JButton btnNot;
	JButton btnImplies;
	JButton btnIf; 	
	JButton btnExists;
	JButton btnForAll; 	
	
	JButton btnOVariableTree; 
	JButton btnNodeTree; 
	JButton btnEntityTree; 
	JButton btnSkolenTree; 
	
	private JPanel variablePanel; 
	
	NetworkController controller; 
	MEBNController mebnController; 
	FormulaTreeController formulaTreeController;
	MFrag mFrag; 
	ContextNode contextNode; 
	
	CardLayout cardLayout; 
	JPanel jpArgTree; 
	
	/** Load resource file from this package */
	private static ResourceBundle resource =
		ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
	protected IconController iconController = IconController.getInstance();
	
	public FormulaEditionPane(NetworkController _controller, ContextNode context){
		
		super(); 
		
		this.setBorder(ToolKitForGuiMebn.getBorderForTabPanel("Context Node")); 
		
		controller = _controller; 
		mebnController = _controller.getMebnController(); 
		mFrag = mebnController.getCurrentMFrag(); 
		contextNode = context;
		
		btnEqualTo = new JButton(iconController.getEqualIcon());  	
		btnAnd = new JButton(iconController.getAndIcon())  ; 
		btnOr = new JButton(iconController.getOrIcon())  ;
		btnNot = new JButton(iconController.getNotIcon());  
		btnImplies = new JButton(iconController.getImpliesIcon());  
		btnIf = new JButton(iconController.getIffIcon());   	
		btnExists = new JButton(iconController.getExistsIcon())  ;
		btnForAll = new JButton(iconController.getForallIcon())  ;
		
		btnEqualTo.setToolTipText(resource.getString("andToolTip")); 
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
		
		formulaTreeController = new FormulaTreeController(_controller, contextNode, this);	    
		jspFormulaTree = new JScrollPane(formulaTreeController.getFormulaTree()); 
		
		btnOVariableTree = new JButton(iconController.getOVariableNodeIcon());  
		btnNodeTree = new JButton(iconController.getNodeNodeIcon());  
		btnEntityTree = new JButton(iconController.getEntityNodeIcon());   
		btnSkolenTree = new JButton(iconController.getSkolenNodeIcon());   
		
		jtbSelectArgTree = new JToolBar(); 
		jtbSelectArgTree.setLayout(new GridLayout(1,4)); 
		jtbSelectArgTree.add(btnOVariableTree);
		jtbSelectArgTree.add(btnNodeTree);
		jtbSelectArgTree.add(btnEntityTree);
		jtbSelectArgTree.add(btnSkolenTree);	
		
		jtbSelectArgTree.setFloatable(false); 
		
		upPanel = new JPanel(new BorderLayout()); 
		upPanel.add(jtbOperator, BorderLayout.NORTH);	    
		upPanel.add(jspFormulaTree, BorderLayout.CENTER);
		
		
		cardLayout = new CardLayout(); 
		jpArgTree = new JPanel(cardLayout);
		jpArgTree.add("NodeTab", replaceByNode()); 
		jpArgTree.add("OVariableTab", replaceByOVariable()); 
		variablePanel = formulaTreeController.getFormulaTree().replaceByVariable(); 
		jpArgTree.add("VariableTab", variablePanel); 
		jpArgTree.add("EntityTab", replaceByEntity()); 
		
		cardLayout.show(jpArgTree, "NodeTab"); 
		
		downPanel = new JPanel(new BorderLayout()); 
		downPanel.add(jtbSelectArgTree, BorderLayout.NORTH);
		downPanel.add(jpArgTree, BorderLayout.CENTER); 
		
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
		
		constraints.gridx = 0; 
		constraints.gridy = 1; 
		constraints.gridwidth = 1; 
		constraints.gridheight = 1; 
		constraints.weightx = 0;
		constraints.weighty = 40; 
		constraints.fill = GridBagConstraints.BOTH; 
		constraints.anchor = GridBagConstraints.CENTER; 
		gridbag.setConstraints(downPanel, constraints); 
		this.add(downPanel);
		
		addListeners(); 
		
	}
	
	/**
	 *  Create a empty painel 
	 *  */
	
	public FormulaEditionPane(){
		
	}
	
	public void update(){
		
	}
	
	public void setNodeTabActive(){
		
		cardLayout.show(jpArgTree, "NodeTab"); 
		
	}
	
	public void setOVariableTabActive(){
		
		cardLayout.show(jpArgTree, "OVariableTab"); 
		
	}	
	
	public void setVariableTabActive(){
		
		jpArgTree.remove(variablePanel); 
		variablePanel = replaceByOVariable(); 
		jpArgTree.add("VariableTab", variablePanel);
		cardLayout.show(jpArgTree, "VariableTab"); 
		
	}	
	
	public void setEntityTabActive(){
		
		cardLayout.show(jpArgTree, "EntityTab"); 
		
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
		
		
		btnNodeTree.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				setNodeTabActive(); 
			}
		}); 		
		
		btnEntityTree.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				setEntityTabActive(); 
			}
		}); 
		
		btnOVariableTree.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				setOVariableTabActive(); 
			}
		}); 
		
		btnSkolenTree.setEnabled(false); 
		
		
	}
	
	/**
	 * open the tree for selected the ordinary variable
	 * that replaces the actual node selected. 
	 */
	
	public JPanel replaceByNode(){
		
		JPanel painel = new JPanel(new BorderLayout()); 
		
		MTheoryTreeForReplaceInFormula mTheoryTree = new MTheoryTreeForReplaceInFormula(controller, formulaTreeController.getFormulaTree()); 
		JScrollPane jspOVariableTreeMFrag = new JScrollPane(mTheoryTree); 
		painel.add(jspOVariableTreeMFrag, BorderLayout.CENTER); 
		
		return painel; 
	}		
	
	/**
	 * open the tree for selected the ordinary variable
	 * that replaces the actual node selected. 
	 */
	
	public JPanel replaceByOVariable(){
		
		JPanel painelOVariableSelection = new JPanel(new BorderLayout()); 
		
		OVariableTreeForReplaceInFormula oVariableTreeMFragReplaceInFormula = new OVariableTreeForReplaceInFormula(controller, formulaTreeController.getFormulaTree()); 
		JScrollPane jspOVariableTreeMFrag = new JScrollPane(oVariableTreeMFragReplaceInFormula); 
		painelOVariableSelection.add(jspOVariableTreeMFrag, BorderLayout.NORTH); 
		
		return painelOVariableSelection; 
		
	}
	
	public JPanel replaceByEntity(){
		
		JPanel painelEntitySelection = new JPanel(new BorderLayout()); 
		
		EntityListForReplaceInFormula entityList = new EntityListForReplaceInFormula(controller, formulaTreeController); 
		JScrollPane jspEntityList = new JScrollPane(entityList); 
		painelEntitySelection.add(jspEntityList, BorderLayout.NORTH); 
		return painelEntitySelection; 
		
	}
	
	public void showErrorMessage(String msg){
		JOptionPane.showMessageDialog(null, msg , resource.getString("error"), JOptionPane.ERROR_MESSAGE);	
	}
	
}


