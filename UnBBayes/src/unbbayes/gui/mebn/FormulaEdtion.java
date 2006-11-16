package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import unbbayes.controller.MEBNController;
import unbbayes.controller.NetworkController;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MFrag;

public class FormulaEdtion extends JPanel {

	JToolBar jtbOperator1;
	JToolBar jtbOperator2; 
	FormulaTree formulaTree; 
	JPanel upPanel; 
	JPanel downPanel; 	
	
	JScrollPane jspFormulaTree; 
	JToolBar jtbSelectArgTree;
	JPanel argTreePanel;
	JScrollPane jspArgTreePanel; 
	
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
	
	MEBNController mebnController; 
	MFrag mFrag; 
	ContextNode contextNode; 
	
	public FormulaEdtion(NetworkController _controller, ContextNode context){
		
		super(); 
		
		GridBagLayout gridbag = new GridBagLayout(); 
		GridBagConstraints constraints = new GridBagConstraints(); 
		
		setLayout(gridbag);
		
		mebnController = _controller.getMebnController(); 
	    mFrag = mebnController.getCurrentMFrag(); 
	    contextNode = context;
	    
	    upPanel = new JPanel(new BorderLayout()); 
	    
	    downPanel = new JPanel(new BorderLayout()); 
	    
		btnEqualTo = new JButton(" = ");  	
		btnAnd = new JButton("&& ");  ; 
		btnOr = new JButton("|| ");  ;
		btnNot = new JButton(" ! ");  ;
		btnImplies = new JButton(" ->");  ;
		btnIf = new JButton("if ");  ; 	
		btnExists = new JButton("ext");  ;
		btnForAll = new JButton("all");  ; 
	    
	    jtbOperator1 = new JToolBar(); 
	    jtbOperator1.add(btnEqualTo); 
	    jtbOperator1.add(btnAnd); 
	    jtbOperator1.add(btnOr); 
	    jtbOperator1.add(btnNot); 
	    jtbOperator1.setFloatable(false); 
	    jtbOperator1.setOrientation(JToolBar.VERTICAL); 	    
	    
	    upPanel.add("East", jtbOperator1); 
	    
	    jtbOperator2 = new JToolBar(); 	    
	    jtbOperator2.add(btnImplies); 
	    jtbOperator2.add(btnIf); 
	    jtbOperator2.add(btnExists); 
	    jtbOperator2.add(btnForAll); 	
	    jtbOperator2.setFloatable(false); 
	    jtbOperator2.setOrientation(JToolBar.VERTICAL); 
	    
	    upPanel.add("West", jtbOperator2); 
	    
	    /*
	    constraints.gridx = 0; 
	    constraints.gridy = 0; 
	    constraints.gridwidth = 1; 
	    constraints.gridheight = 1; 
	    constraints.weightx = 100; 
	    constraints.weighty = 10; 
	    constraints.fill = GridBagConstraints.BOTH; 
	    constraints.anchor = GridBagConstraints.NORTH; 
	    gridbag.setConstraints(jtbOperator1, constraints); 
	    this.add(jtbOperator1);
	    
	    constraints.gridx = 0; 
	    constraints.gridy = 1; 
	    constraints.gridwidth = 1; 
	    constraints.gridheight = 1; 
	    constraints.weightx = 100; 
	    constraints.weighty = 10; 
	    constraints.fill = GridBagConstraints.BOTH; 
	    constraints.anchor = GridBagConstraints.NORTH; 
	    gridbag.setConstraints(jtbOperator2, constraints); 
	    this.add(jtbOperator2);	    
	    */
	    
	    formulaTree = new FormulaTree(); 
	    jspFormulaTree = new JScrollPane(); 
	    upPanel.add("Center", jspFormulaTree); 
	    
	    
	    constraints.gridx = 0; 
	    constraints.gridy = 0; 
	    constraints.gridwidth = 1; 
	    constraints.gridheight = 1; 
	    constraints.weightx = 0; 
	    constraints.weighty = 50; 
	    constraints.fill = GridBagConstraints.BOTH; 
	    constraints.anchor = GridBagConstraints.NORTH; 	
	    gridbag.setConstraints(upPanel, constraints); 	    
	    this.add(upPanel);
	    
		btnOVariableTree = new JButton("ovt");  
		btnNodeTree = new JButton("ndt");  
		btnEntityTree = new JButton("ett");   
		btnSkolenTree = new JButton("skt");   
	    
	    jtbSelectArgTree = new JToolBar(); 
	    jtbSelectArgTree.add(btnOVariableTree);
	    jtbSelectArgTree.add(btnNodeTree);
	    jtbSelectArgTree.add(btnEntityTree);
	    jtbSelectArgTree.add(btnSkolenTree);	    
	    
	    jtbSelectArgTree.setFloatable(false); 
	    downPanel.add("North", jtbSelectArgTree); 
	    
	    /*
	    constraints.gridx = 0; 
	    constraints.gridy = 3; 
	    constraints.gridwidth = 1; 
	    constraints.gridheight = 1; 
	    constraints.weightx = 0; 
	    constraints.weighty = 10; 
	    constraints.fill = GridBagConstraints.BOTH; 
	    constraints.anchor = GridBagConstraints.NORTH; 
	    gridbag.setConstraints(jtbSelectArgTree, constraints); 	    
	    this.add(jtbSelectArgTree);	   
	    */
	    
	    argTreePanel = new JPanel();
	    jspArgTreePanel = new JScrollPane(argTreePanel); 
	    downPanel.add("Center", jspArgTreePanel);
	    
	    
	    
	    constraints.gridx = 0; 
	    constraints.gridy = 1; 
	    constraints.gridwidth = 1; 
	    constraints.gridheight = 1; 
	    constraints.weightx = 0; 
	    constraints.weighty = 50; 
	    constraints.fill = GridBagConstraints.BOTH; 
	    constraints.anchor = GridBagConstraints.NORTH; 
	    gridbag.setConstraints(downPanel, constraints); 	    
	    this.add(downPanel);	    	    
	}

	/**
	 *  Create a empty painel 
	 *  */
	
	public FormulaEdtion(){
		
	}
		
	public void update(){
		
	}
	
}
	

