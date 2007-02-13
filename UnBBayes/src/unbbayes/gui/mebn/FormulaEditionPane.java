package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.Color;
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

public class FormulaEditionPane extends JPanel {

	JToolBar jtbOperator;
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
	
	public FormulaEditionPane(NetworkController _controller, ContextNode context){
		
		super(); 
		
		setLayout(new BorderLayout());
		
		this.setBorder(ToolKitForGuiMebn.getBorderForTabPanel("Context Node")); 
		
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

	    upPanel.add(jtbOperator); 
        
	    formulaTree = new FormulaTree(_controller, contextNode ); 
	    jspFormulaTree = new JScrollPane(formulaTree); 
 
	    this.add("North", upPanel);	    
	    
	    this.add("Center", jspFormulaTree); 
	    
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
	    this.add("South", jtbSelectArgTree); 
	    
	    addListeners(); 
	      	    
	}

	/**
	 *  Create a empty painel 
	 *  */
	
	public FormulaEditionPane(){
		
	}
		
	public void update(){
		
	}
	
	public void addListeners(){

	    btnAnd.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    BuiltInRV builtInRV = new BuiltInRVAnd(); 
			    formulaTree.addSimpleOperatorInTree(builtInRV); 
			}
		});		
		
		btnOr.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    BuiltInRV builtInRV = new BuiltInRVOr(); 
			    formulaTree.addSimpleOperatorInTree(builtInRV); 
			}
		});				
		
		btnNot.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    BuiltInRV builtInRV = new BuiltInRVNot(); 
			    formulaTree.addSimpleOperatorInTree(builtInRV); 
			}
		});	
		
		btnEqualTo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    BuiltInRV builtInRV = new BuiltInRVEqualTo(); 
			    formulaTree.addSimpleOperatorInTree(builtInRV); 
			}
		});						

		btnIf.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    BuiltInRV builtInRV = new BuiltInRVIff(); 
			    formulaTree.addSimpleOperatorInTree(builtInRV); 
			}
		});						
		
		btnImplies.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    BuiltInRV builtInRV = new BuiltInRVImplies(); 
			    formulaTree.addSimpleOperatorInTree(builtInRV); 
			}
		});						
		
		btnForAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    BuiltInRV builtInRV = new BuiltInRVForAll(); 
			    formulaTree.addQuantifierOperatorInTree(builtInRV); 
			}
		});						
		
		btnExists.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
			    BuiltInRV builtInRV = new BuiltInRVExists(); 
			    formulaTree.addQuantifierOperatorInTree(builtInRV); 
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
	

