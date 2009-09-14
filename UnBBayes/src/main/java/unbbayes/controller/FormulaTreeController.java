/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.controller;

import unbbayes.gui.mebn.FormulaEditionPane;
import unbbayes.gui.mebn.formula.FormulaViewTree;
import unbbayes.gui.mebn.formula.exception.FormulaTreeConstructionException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ResidentNodePointer;
import unbbayes.prs.mebn.context.EnumSubType;
import unbbayes.prs.mebn.context.EnumType;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.StateLink;

/**
 * Controller for the formulaEditionPane
 * 
 * (Model: context node). 
 * 
 * @author Laecio Lima dos Santos
 *
 */
public class FormulaTreeController {
	
	private FormulaViewTree formulaViewTree;
	private MEBNController mebnController; 
	private ContextNode contextNode; 
	private FormulaEditionPane formulaEditionPane; 
	
	public FormulaTreeController(MEBNController _controller, 
			ContextNode context, FormulaEditionPane _formulaEditionPane){
		
		this.mebnController = _controller; 
		this.contextNode = context; 
		this.formulaEditionPane = _formulaEditionPane; 
		
		this.formulaViewTree = new FormulaViewTree(this, context); 
		
	}
	
	public void setContextNodeFormula(NodeFormulaTree formula){
		contextNode.setFormulaTree(formula); 
	}
	
	public FormulaViewTree getFormulaTree(){
		return formulaViewTree; 
	}
	
	public void addOperatorAnd()throws Exception{
		formulaViewTree.addOperatorAnd(); 
		mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorOr()throws Exception{
		formulaViewTree.addOperatorOr(); 
		mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorNot()throws Exception{
		formulaViewTree.addOperatorNot(); 
		mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorEqualTo()throws Exception{
		formulaViewTree.addOperatorEqualTo(); 
		mebnController.updateFormulaActiveContextNode(); 
	}	
	
	public void addOperatorIf() throws Exception{
		formulaViewTree.addOperatorIf(); 
		mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorImplies()throws Exception{
		formulaViewTree.addOperatorImplies();
		mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorForAll()throws Exception{
		formulaViewTree.addOperatorForAll(); 
		mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorExists()throws Exception{
		formulaViewTree.addOperatorExists(); 
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
	 * Add one ordinary variable in the formula tree (replace
	 * the actual node of the formula for the ordinary variable)
	 * 
	 * @param ov
	 */
	public void addOVariable(OrdinaryVariable ov) throws FormulaTreeConstructionException{
		
		NodeFormulaTree nodePlace = formulaViewTree.getNodeFormulaActive();  
		
		if(nodePlace != null){

			if(nodePlace.getTypeNode() == EnumType.VARIABLE_SEQUENCE){
				NodeFormulaTree nodeExemplar = new NodeFormulaTree(ov.getName(), EnumType.VARIABLE, EnumSubType.VARIABLE, ov); 
				nodePlace.addChild(nodeExemplar); 
				formulaViewTree.addNewNodeInTree(nodeExemplar);  
			}

			else{
				nodePlace.setName(ov.getName()); 
				nodePlace.setNodeVariable(ov);
				nodePlace.setTypeNode(EnumType.OPERAND); 
				nodePlace.setSubTypeNode(EnumSubType.OVARIABLE);
			}
			formulaViewTree.updateTree(); 
		}
	}	
	
	
	/**
	 * add one ordinary variable in the formula tree (replace
	 * the actual node of the formula for the ordinary variable)
	 * 
	 * @param ov
	 */
	public void addNode(ResidentNode node){
		
		NodeFormulaTree nodePlace = formulaViewTree.getNodeFormulaActive();   

		if(nodePlace != null){
			nodePlace.setName(node.getName()); 

			ResidentNodePointer residentNodePointer = new ResidentNodePointer(node, contextNode); 
			nodePlace.setNodeVariable(residentNodePointer);

			nodePlace.setTypeNode(EnumType.OPERAND); 
			nodePlace.setSubTypeNode(EnumSubType.NODE); 

			formulaViewTree.updateTree();
		}
	}	
	
	/**
	 * 
	 *
	 */
	public void addExemplar(OrdinaryVariable ov){
		
		NodeFormulaTree nodePlace = formulaViewTree.getNodeFormulaActive();  
		
	}	
	
	public void addEntity(Entity entity){
		
		NodeFormulaTree nodePlace = formulaViewTree.getNodeFormulaActive();  
		
		if(nodePlace != null){
			nodePlace.setName(entity.getName()); 
			nodePlace.setNodeVariable(entity);
			nodePlace.setTypeNode(EnumType.OPERAND); 
			nodePlace.setSubTypeNode(EnumSubType.ENTITY); 

			formulaViewTree.updateTree();
		}
	}	
	
	public void addStateLink(StateLink link){
		
     	NodeFormulaTree nodePlace = formulaViewTree.getNodeFormulaActive();  

     	if(nodePlace != null){
     		nodePlace.setName(link.getState().getName()); 
     		nodePlace.setNodeVariable(link);
     		nodePlace.setTypeNode(EnumType.OPERAND); 
     		nodePlace.setSubTypeNode(EnumSubType.ENTITY); 

     		formulaViewTree.updateTree();
     	}
		
	}
	
	public void showArgumentPanel(NodeFormulaTree nodeFormulaActive){
		
		formulaEditionPane.setArgumentSelectionTab((ResidentNodePointer)nodeFormulaActive.getNodeVariable());
		
	}

	public ContextNode getContextNode() {
		return contextNode;
	}

	public void setContextNode(ContextNode contextNode) {
		this.contextNode = contextNode;
	}
	
	/**
	 * Update the text of formula in: 
	 * - node context
	 * - tab painel of Context node. 
	 */
	public void updateFormulaText(){
		contextNode.updateLabel();
		mebnController.updateFormulaActiveContextNode(); 
	}
	
}
