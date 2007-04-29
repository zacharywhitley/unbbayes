package unbbayes.gui.mebn.formula;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import unbbayes.controller.FormulaTreeController;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.context.enumSubType;
import unbbayes.prs.mebn.context.enumType;

/**
 * Cria o menu para um nó da arvore de fórmula. 
 * @author Laecio
 *
 */

public class BuilderMenuNode {
	
	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");	
	
	private JMenuItem itemDelete = new JMenuItem("delete"); 
	
	private JMenuItem itemNode = new JMenuItem("addNode");
	private JMenuItem itemEntity = new JMenuItem("addEntity"); 
	private JMenuItem itemOVariable = new JMenuItem("addOVariable");
	private JMenuItem itemVariable = new JMenuItem("addVariable"); 
	private JMenuItem itemExemplar = new JMenuItem("addExemplar"); 
	private JMenuItem itemSkolen = new JMenuItem("addSkolen");
	
	private JMenuItem itemAnd = new JMenuItem("and"); 
	private JMenuItem itemOr = new JMenuItem("or");
	private JMenuItem itemNot = new JMenuItem("not");
	private JMenuItem itemEqual = new JMenuItem("equal"); 
	private JMenuItem itemIff = new JMenuItem("iff"); 
	private JMenuItem itemImplies = new JMenuItem("Implies"); 
	private JMenuItem itemForall = new JMenuItem("For All"); 
	private JMenuItem itemExists = new JMenuItem("Exists"); 
	
	private JMenuItem itemAddExemplar = new JMenuItem("addExemplar"); 
	
	private JPopupMenu popupFormula; 
	private JPopupMenu popupOperando; 
	private JPopupMenu popupExemplarList;
	private JPopupMenu popupExemplar; 
	private JPopupMenu popupOperator; 
	
	private FormulaTreeController formulaTreeController; 
	
	public BuilderMenuNode(FormulaTreeController _formulaTreeController){
		formulaTreeController = _formulaTreeController; 
		addListener(); 
	}
	
	
	private void addListener(){
		itemAnd.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				try{
					formulaTreeController.addOperatorAnd();
				}
				catch(Exception e){
					showErrorMessage(e.getMessage());	
				}
			}
		});		
		
		itemOr.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				try{
					formulaTreeController.addOperatorOr();    
				}
				catch(Exception e){
					showErrorMessage(e.getMessage());						
				}
			}
		});				
		
		itemNot.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				try{
					formulaTreeController.addOperatorNot();
				}
				catch(Exception e){
					showErrorMessage(e.getMessage());						
				}
			}
		});	
		
		itemEqual.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				try{
					formulaTreeController.addOperatorEqualTo(); 
				}
				catch(Exception e){
					showErrorMessage(e.getMessage());							
				}
			}
		});						
		
		itemIff.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				try{
					formulaTreeController.addOperatorIf(); 
				}
				catch(Exception e){
					showErrorMessage(e.getMessage());						
				}
			}
		});						
		
		itemImplies.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				try{
					formulaTreeController.addOperatorImplies(); 
				}
				catch(Exception e){
					showErrorMessage(e.getMessage());						
				}
			}
		});						
		
		itemForall.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				try{
					formulaTreeController.addOperatorForAll(); 
				}
				catch(Exception e){
					showErrorMessage(e.getMessage());						
				}
			}
		});						
		
		itemExists.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				try{
					formulaTreeController.addOperatorExists(); 
				}
				catch(Exception e){
					showErrorMessage(e.getMessage());						
				}
			}
		});		
		
		itemNode.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.setNodeChoiceActive(); 
			}
		}); 		
		
		itemEntity.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				//controller.getScreen().getGraphPane().setAction(GraphAction.CREATE_CONTEXT_NODE); 
			}
		}); 
		
		itemOVariable.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.setOVariableChoiveActive(); 
			}
		}); 
		
		itemVariable.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.setVariableChoiceActive(); 
			}
		}); 	
		
		itemExemplar.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.setVariableChoiceActive(); 
			}
		}); 
		
		itemDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode)formulaTreeController.getFormulaTree().getNodeActive().getParent(); 
				NodeFormulaTree nodeFormulaParent = (NodeFormulaTree)parent.getUserObject(); 
				NodeFormulaTree nodeFormula = formulaTreeController.getFormulaTree().getNodeFormulaActive(); 
				
				/* Caso 1: raiz */
				if(parent == null){
					NodeFormulaTree rootFormula = new NodeFormulaTree("formula", enumType.FORMULA, enumSubType.NOTHING, null); 	
					DefaultMutableTreeNode nodeTree =  new DefaultMutableTreeNode(rootFormula); 
					formulaTreeController.setContextNodeFormula(rootFormula); 
					formulaTreeController.getFormulaTree().setNodeActive(nodeTree); 
					return; 
				}
				
				/* Caso 2: Operando */
				if(nodeFormula.getTypeNode() == enumType.OPERANDO){
					nodeFormula.setSubTypeNode(enumSubType.NOTHING); 
					nodeFormula.setName("op"); 
					nodeFormula.setNodeVariable(null); 
					nodeFormula.setMnemonic(""); 
					return; 
				}
				
				/* Caso 3: operador */
				
				if((nodeFormula.getTypeNode() == enumType.SIMPLE_OPERATOR)||
					(nodeFormula.getTypeNode() == enumType.QUANTIFIER_OPERATOR)){
					nodeFormula.setSubTypeNode(enumSubType.NOTHING); 
					nodeFormula.setName("op"); 
					nodeFormula.setNodeVariable(null); 
					nodeFormula.setMnemonic(""); 
					nodeFormula.removeAllChildren(); 
					DefaultMutableTreeNode nodeTree = formulaTreeController.getFormulaTree().getNodeActive(); 
					nodeTree.removeAllChildren(); 
					return;                   						
				}
				
				/* variavel: mais problematico */
				if(nodeFormula.getTypeNode() == enumType.VARIABLE){
					
					nodeFormulaParent.removeChild(nodeFormula); 
					parent.remove(formulaTreeController.getFormulaTree().getNodeActive()); 
					return; 
				}
				
				
				
			}
		}); 
		
		itemSkolen.setEnabled(false); 
		
	}
	
	public JPopupMenu buildPopupFormula(){
		
		if (popupFormula == null){
			
			popupFormula = new JPopupMenu(); 
			popupFormula.add(itemAnd); 
			popupFormula.add(itemOr); 
			popupFormula.add(itemNot); 
			popupFormula.add(itemEqual); 
			popupFormula.add(itemImplies); 
			popupFormula.add(itemIff); 
			popupFormula.add(itemForall); 
			popupFormula.add(itemExists); 
			
		}
		
		return popupFormula;
	}
	
	public JPopupMenu buildPopupOperando(){
		
		if(popupOperando == null){
			popupOperando = new JPopupMenu(); 
			
			popupOperando.add(itemDelete); 
			popupOperando.addSeparator(); 
			
			popupOperando.add(itemNode); 
			popupOperando.add(itemEntity); 
			popupOperando.add(itemVariable); 
			popupOperando.addSeparator();
			
			popupOperando.add(itemAnd); 
			popupOperando.add(itemOr); 
			popupOperando.add(itemNot); 
			popupOperando.add(itemEqual); 
			popupOperando.add(itemImplies); 
			popupOperando.add(itemIff); 
			popupOperando.add(itemForall); 
			popupOperando.add(itemExists); 
			
		}
		
		return popupOperando; 
		
	}
	
	public JPopupMenu buildPopupExemplarList(){
		
		if(popupExemplarList == null){
			popupExemplarList = new JPopupMenu();  
		}
		
		popupExemplarList.add(itemExemplar); 
		
		return popupExemplarList; 
		
	}
	
	public JPopupMenu buildPopupExemplar(){
		
		if(popupExemplar == null){
			popupExemplar = new JPopupMenu();
			
			popupExemplar.add(itemDelete); 
			popupExemplar.addSeparator(); 
			popupExemplar.add(itemAddExemplar); 
		
		}
		
		return popupExemplar; 
		
	}
	
	public JPopupMenu buildPopupOperator(){
		
		if(popupOperator == null){
			popupOperator = new JPopupMenu(); 
			
			popupOperator.add(itemDelete); 
			popupOperator.addSeparator(); 
			
			popupOperator.add(itemAnd); 
			popupOperator.add(itemOr); 
			popupOperator.add(itemNot); 
			popupOperator.add(itemEqual); 
			popupOperator.add(itemImplies); 
			popupOperator.add(itemIff); 
			popupOperator.add(itemForall); 
			popupOperator.add(itemExists); 
		}
		
		return popupOperator; 
		
	}	
	
	public void showErrorMessage(String msg){
		JOptionPane.showMessageDialog(null, msg , resource.getString("error"), JOptionPane.ERROR_MESSAGE);	
	}
	
}
