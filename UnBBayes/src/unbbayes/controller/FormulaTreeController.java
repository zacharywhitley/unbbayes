package unbbayes.controller;

import javax.swing.tree.DefaultMutableTreeNode;

import unbbayes.gui.mebn.FormulaEditionPane;
import unbbayes.gui.mebn.formula.FormulaTree;
import unbbayes.gui.mebn.formula.exception.FormulaTreeConstructionException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ResidentNodePointer;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.context.enumSubType;
import unbbayes.prs.mebn.context.enumType;
import unbbayes.prs.mebn.entity.Entity;

public class FormulaTreeController {

	private FormulaTree formulaTree;
	private MEBNController mebnController; 
	private ContextNode contextNode; 
	private FormulaEditionPane formulaEditionPane; 
	
	public FormulaTreeController(MEBNController _controller, 
			                     ContextNode context, FormulaEditionPane _formulaEditionPane){
	
		this.mebnController = _controller; 
		this.contextNode = context; 
		this.formulaEditionPane = _formulaEditionPane; 
	
		formulaTree = new FormulaTree(this, context); 
	
	}
	
	public void setContextNodeFormula(DefaultMutableTreeNode formula){
		contextNode.setFormulaTree(formula); 
	}
	
	public FormulaTree getFormulaTree(){
		
		return formulaTree; 
	
	}
	
	public void addOperatorAnd()throws Exception{
		 formulaTree.addOperatorAnd(); 
		 mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorOr()throws Exception{
		formulaTree.addOperatorOr(); 
		 mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorNot()throws Exception{
		formulaTree.addOperatorNot(); 
		 mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorEqualTo()throws Exception{
		formulaTree.addOperatorEqualTo(); 
		 mebnController.updateFormulaActiveContextNode(); 
	}	

	public void addOperatorIf() throws Exception{
		 formulaTree.addOperatorIf(); 
		 mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorImplies()throws Exception{
		formulaTree.addOperatorImplies();
		 mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorForAll()throws Exception{
		formulaTree.addOperatorForAll(); 
		mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorExists()throws Exception{
		formulaTree.addOperatorExists(); 
		mebnController.updateFormulaActiveContextNode(); 
	}	
	
	public void setNodeChoiceActive(){
		formulaEditionPane.setNodeTabActive();
	}
	
	public void setOVariableChoiveActive(){
		formulaEditionPane.setOVariableTabActive(); 
	}
	
	public void setVariableChoiceActive(){
		formulaEditionPane.setVariableTabActive(); 
	}
	
	/**
	 * add one ordinary variable in the formula tree (replace
	 * the actual node of the formula for the ordinary variable)
	 * 
	 * @param ov
	 */
	public void addOVariable(OrdinaryVariable ov) throws FormulaTreeConstructionException{
		
		NodeFormulaTree nodePlace = formulaTree.getNodeFormulaActive();  
		
		if(nodePlace.getTypeNode() == enumType.VARIABLE_SEQUENCE){
			NodeFormulaTree nodeExemplar = new NodeFormulaTree(ov.getName(), enumType.VARIABLE, enumSubType.VARIABLE, ov); 
			nodePlace.addChild(nodeExemplar); 
			formulaTree.addNewNodeInTree(nodeExemplar);  
		}
		
		else{
		   nodePlace.setName(ov.getName()); 
		   nodePlace.setNodeVariable(ov);
		   nodePlace.setTypeNode(enumType.OPERANDO); 
		   nodePlace.setSubTypeNode(enumSubType.OVARIABLE);
		}
		formulaTree.updateTree(); 
		
	}	
	
	
	/**
	 * add one ordinary variable in the formula tree (replace
	 * the actual node of the formula for the ordinary variable)
	 * 
	 * @param ov
	 */
	public void addNode(ResidentNode node){
		
		NodeFormulaTree nodePlace = formulaTree.getNodeFormulaActive();   
		
		nodePlace.setName(node.getName()); 
		
		ResidentNodePointer residentNodePointer = new ResidentNodePointer(node); 
		nodePlace.setNodeVariable(residentNodePointer);
		
		nodePlace.setTypeNode(enumType.OPERANDO); 
		nodePlace.setSubTypeNode(enumSubType.NODE); 
		
		formulaTree.updateTree(); 
	}	
	
	/**
	 * 
	 *
	 */
	public void addExemplar(OrdinaryVariable ov){
		
		NodeFormulaTree nodePlace = formulaTree.getNodeFormulaActive();  
		
	}	
	
	public void addEntity(Entity entity){
		
		NodeFormulaTree nodePlace = formulaTree.getNodeFormulaActive();  
		
		nodePlace.setName(entity.getName()); 
		nodePlace.setNodeVariable(entity);
		nodePlace.setTypeNode(enumType.OPERANDO); 
		nodePlace.setSubTypeNode(enumSubType.ENTITY); 
		
		formulaTree.updateTree(); 			
	}	
	
	public void showArgumentPanel(NodeFormulaTree nodeFormulaActive){
		
		formulaEditionPane.setArgumentSelectionTab((ResidentNodePointer)nodeFormulaActive.getNodeVariable());
		
	}
	
}
