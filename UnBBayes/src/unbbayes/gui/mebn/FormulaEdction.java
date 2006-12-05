package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.controller.NetworkController;
import unbbayes.gui.GraphAction;
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

public class FormulaEdction extends JPanel {

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
	
	NetworkController controller; 
	MEBNController mebnController; 
	MFrag mFrag; 
	ContextNode contextNode; 

	/** Load resource file from this package */
	private static ResourceBundle resource =
		ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
    protected IconController iconController = IconController.getInstance();
	
	public FormulaEdction(NetworkController _controller, ContextNode context){
		
		super(); 
		
		setLayout(new BorderLayout());
		
		controller = _controller; 
		mebnController = _controller.getMebnController(); 
	    mFrag = mebnController.getCurrentMFrag(); 
	    contextNode = context;
	    
	    upPanel = new JPanel(new GridLayout(0,1)); 
	    
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
		
	    jtbOperator1 = new JToolBar(); 
	    jtbOperator1.add(btnEqualTo); 
	    jtbOperator1.add(btnAnd); 
	    jtbOperator1.add(btnOr); 
	    jtbOperator1.add(btnNot); 
	    jtbOperator1.setFloatable(false);     
	    
	    upPanel.add(jtbOperator1); 
	    
	    jtbOperator2 = new JToolBar(); 	    
	    jtbOperator2.add(btnImplies); 
	    jtbOperator2.add(btnIf); 
	    jtbOperator2.add(btnExists); 
	    jtbOperator2.add(btnForAll); 	
	    jtbOperator2.setFloatable(false); 
	    
	    upPanel.add(jtbOperator2); 
        
	    formulaTree = new FormulaTree(_controller, contextNode ); 
	    jspFormulaTree = new JScrollPane(formulaTree); 
 
	    this.add("North", upPanel);	    
	    
	    this.add("Center", jspFormulaTree); 
	    
		btnOVariableTree = new JButton(iconController.getOVariableNodeIcon());  
		btnNodeTree = new JButton(iconController.getNodeNodeIcon());  
		btnEntityTree = new JButton(iconController.getEntityNodeIcon());   
		btnSkolenTree = new JButton(iconController.getSkolenNodeIcon());   
	    
	    jtbSelectArgTree = new JToolBar(); 
	    jtbSelectArgTree.add(btnOVariableTree);
	    jtbSelectArgTree.add(btnNodeTree);
	    jtbSelectArgTree.add(btnEntityTree);
	    jtbSelectArgTree.add(btnSkolenTree);	    
	    
	    jtbSelectArgTree.setFloatable(false); 
	    this.add("South", jtbSelectArgTree); 
	    
	    addListeners(); 
	      	    
	}

	/**
	 *  Create a empty painel 
	 *  */
	
	public FormulaEdction(){
		
	}
		
	public void update(){
		
	}
	
	public void addListeners(){

	    btnAnd.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    BuiltInRV builtInRV = new BuiltInRVAnd(); 
			    formulaTree.addOperatorInTree(builtInRV); 
			}
		});		
		
		btnOr.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    BuiltInRV builtInRV = new BuiltInRVOr(); 
			    formulaTree.addOperatorInTree(builtInRV); 
			}
		});				
		
		btnNot.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    BuiltInRV builtInRV = new BuiltInRVNot(); 
			    formulaTree.addOperatorInTree(builtInRV); 
			}
		});	
		
		btnEqualTo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    BuiltInRV builtInRV = new BuiltInRVEqualTo(); 
			    formulaTree.addOperatorInTree(builtInRV); 
			}
		});						

		btnIf.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    BuiltInRV builtInRV = new BuiltInRVIff(); 
			    formulaTree.addOperatorInTree(builtInRV); 
			}
		});						
		
		btnImplies.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    BuiltInRV builtInRV = new BuiltInRVImplies(); 
			    formulaTree.addOperatorInTree(builtInRV); 
			}
		});						
		
		btnForAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    BuiltInRV builtInRV = new BuiltInRVForAll(); 
			    formulaTree.addQuantifierInTree(builtInRV); 
			}
		});						
		
		btnExists.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    BuiltInRV builtInRV = new BuiltInRVExists(); 
			    formulaTree.addQuantifierInTree(builtInRV); 
			}
		});						
		
						
		
		
		
		
		btnNodeTree.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			     
				 JFrame teste = new JFrame("NodeTree"); 
			     
			     JPanel painel = new JPanel(new BorderLayout()); 

			     MTheoryTree mTheoryTree = new MTheoryTree(controller); 
			     JScrollPane jspMTheoryTree = new JScrollPane(mTheoryTree); 
			     painel.add(jspMTheoryTree, BorderLayout.NORTH); 

			     teste.setContentPane(painel);
			     teste.pack();
			     teste.setVisible(true); 
			     teste.setLocationRelativeTo(null); 
			     teste.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
			
			}
		}); 		
		
		btnEntityTree.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				controller.getScreen().getGraphPane().setAction(GraphAction.CREATE_CONTEXT_NODE); 
			}
		}); 
		
		btnOVariableTree.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			     formulaTree.replaceByOVariable(); 
			}
		}); 
		
		btnSkolenTree.setEnabled(false); 
			    
	    
	}
	
}
	

