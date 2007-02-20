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
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import unbbayes.controller.FormulaTreeController;
import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.controller.NetworkController;
import unbbayes.gui.GraphAction;
import unbbayes.gui.mebn.FormulaTree.enumSubType;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.builtInRV.BuiltInRVAnd;
import unbbayes.prs.mebn.builtInRV.BuiltInRVEqualTo;
import unbbayes.prs.mebn.builtInRV.BuiltInRVExists;
import unbbayes.prs.mebn.builtInRV.BuiltInRVForAll;
import unbbayes.prs.mebn.builtInRV.BuiltInRVIff;
import unbbayes.prs.mebn.builtInRV.BuiltInRVImplies;
import unbbayes.prs.mebn.builtInRV.BuiltInRVNot;
import unbbayes.prs.mebn.builtInRV.BuiltInRVOr;

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
        //jpArgTree.add("VariableTab", formulaTree.replaceByVariable()); 
        
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
	
	public void addListeners(){

	    btnAnd.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    formulaTreeController.addOperatorAnd(); 
			}
		});		
		
		btnOr.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.addOperatorOr(); 
			}
		});				
		
		btnNot.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.addOperatorNot(); 
			}
		});	
		
		btnEqualTo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.addOperatorEqualTo(); 
			}
		});						

		btnIf.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.addOperatorIf(); 
			}
		});						
		
		btnImplies.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.addOperatorImplies(); 
			}
		});						
		
		btnForAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.addOperatorForAll(); 
			}
		});						
		
		btnExists.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.addOperatorExists(); 
			}
		});	
		
		
		btnNodeTree.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			     setNodeTabActive(); 
			}
		}); 		
		
		btnEntityTree.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				controller.getScreen().getGraphPane().setAction(GraphAction.CREATE_CONTEXT_NODE); 
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
     
     MTheoryTreeReplaceInFormula mTheoryTree = new MTheoryTreeReplaceInFormula(controller, formulaTreeController.getFormulaTree()); 
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
	
}
	

