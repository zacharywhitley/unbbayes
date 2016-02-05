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
package unbbayes.controller.umpst;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import unbbayes.gui.umpst.implementation.FormulaEditionPane;
import unbbayes.gui.umpst.implementation.FormulaViewTreePane;
import unbbayes.gui.umpst.implementation.NecessaryConditionEditPanel;
import unbbayes.gui.umpst.implementation.exception.FormulaTreeConstructionException;
import unbbayes.model.umpst.implementation.EnumSubType;
import unbbayes.model.umpst.implementation.EnumType;
import unbbayes.model.umpst.implementation.EventNCPointer;
import unbbayes.model.umpst.implementation.EventVariableObjectModel;
import unbbayes.model.umpst.implementation.NecessaryConditionVariableModel;
import unbbayes.model.umpst.implementation.NodeFormulaTree;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;
import unbbayes.model.umpst.rule.RuleModel;

/**
 * Controller for the formulaEditionPane
 * 
 * (Model: context node). 
 * 
 * @author Laecio Lima dos Santos
 *
 */
public class FormulaTreeController {
	
	private NecessaryConditionEditPanel ncEditPanel;
	private FormulaViewTreePane formulaViewTreePane;
	private FormulaEditionPane formulaEditionPane; 
	private RuleModel rule;
	private NecessaryConditionVariableModel ncVariableModel;
	private NecessaryConditionVariableModel ncVariableActive;
	
	public FormulaTreeController(NecessaryConditionEditPanel ncEditPanel, RuleModel rule, 
			FormulaEditionPane formulaEditionPane, NecessaryConditionVariableModel ncVariableModel, boolean editTree){
		
		this.rule = rule;
		this.ncEditPanel = ncEditPanel;
		this.formulaEditionPane = formulaEditionPane;
		this.ncVariableModel = ncVariableModel;
		
		DefaultTreeModel model;
		if (editTree) {			
			model = new DefaultTreeModel(buildTree());
		} else {		
			NodeFormulaTree rootFormula = new NodeFormulaTree("formula", EnumType.FORMULA, 	EnumSubType.NOTHING, null);
			DefaultMutableTreeNode rootTreeView = new DefaultMutableTreeNode(rootFormula);
			model = new DefaultTreeModel(rootTreeView);
			ncVariableModel.setFormulaTree(rootFormula);
		}
		formulaViewTreePane = new FormulaViewTreePane(rule, this, ncVariableModel, model);
	}
	
	public DefaultMutableTreeNode buildTree() {
		NodeFormulaTree rootTreeFormula = ncVariableModel.getFormulaTree();
		DefaultMutableTreeNode rootTreeView = new DefaultMutableTreeNode(rootTreeFormula);		
		buildChildren(rootTreeFormula, rootTreeView);
		return rootTreeView;
	}
	
	public DefaultMutableTreeNode buildChildren(NodeFormulaTree nodeFormulaFather, DefaultMutableTreeNode nodeTreeFather){		
		for(NodeFormulaTree child: nodeFormulaFather.getChildren()){
			DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(child); 
			nodeTreeFather.add(treeNode);
			buildChildren(child, treeNode); 
		}
		return nodeTreeFather;
	}

	/**
	 * add one ordinary variable in the formula tree (replace
	 * the actual node of the formula for the ordinary variable)
	 * 
	 * @param ov
	 */
	public void addNode(EventVariableObjectModel rv){
		
		NodeFormulaTree nodePlace = formulaViewTreePane.getNodeFormulaActive(); 

		if(nodePlace != null){
			EventNCPointer eventNCPointer = new EventNCPointer(nodePlace, ncVariableModel, rv);
			nodePlace.setName(eventNCPointer.getEventVariable().getRelationship());

			// TODO change to better form to separate between cause and effect variable
//			String residentNodePointer = new ResidentNodePointer(node, contextNode);
			nodePlace.setNodeVariable(eventNCPointer);

			nodePlace.setTypeNode(EnumType.OPERAND); 
			nodePlace.setSubTypeNode(EnumSubType.NODE); 

			formulaViewTreePane.updateTree();
		}
	}
	
	/**
	 * Add one ordinary variable in the formula tree (replace
	 * the actual node of the formula for the ordinary variable)
	 * 
	 * @param ov
	 */
	public void addOVariable(OrdinaryVariableModel ov) 
			throws FormulaTreeConstructionException{
		
		NodeFormulaTree nodePlace = formulaViewTreePane.getNodeFormulaActive();		
		if(nodePlace != null){
			if(nodePlace.getTypeNode() == EnumType.VARIABLE_SEQUENCE){
				NodeFormulaTree nodeExemplar = new NodeFormulaTree(ov.getVariable(), 
						EnumType.VARIABLE, EnumSubType.VARIABLE, ov); 
				nodePlace.addChild(nodeExemplar); 
				formulaViewTreePane.addNewNodeInTree(nodeExemplar);  
			}
			else{
				nodePlace.setName(ov.getVariable());
				nodePlace.setNodeVariable(ov);
				nodePlace.setTypeNode(EnumType.OPERAND); 
				nodePlace.setSubTypeNode(EnumSubType.VARIABLE);
			}
			formulaViewTreePane.updateTree();
		}
	}
	
	public void setNCNodeFormula(NodeFormulaTree formula){
		ncVariableModel.setFormulaTree(formula); 
	}
	
	public void addOperatorAnd()throws Exception{
		formulaViewTreePane.addOperatorAnd();
		updateFormulaActiveContextNode();
	}
	
	public void addOperatorOr()throws Exception{
		formulaViewTreePane.addOperatorOr(); 
		updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorNot()throws Exception{
		formulaViewTreePane.addOperatorNot(); 
		updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorEqualTo()throws Exception{
		formulaViewTreePane.addOperatorEqualTo(); 
		updateFormulaActiveContextNode(); 
	}	
	
	public void addOperatorIf() throws Exception{
		formulaViewTreePane.addOperatorIf(); 
		updateFormulaActiveContextNode();
	}
	
	public void addOperatorImplies()throws Exception{
		formulaViewTreePane.addOperatorImplies();
		updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorForAll()throws Exception{
		formulaViewTreePane.addOperatorForAll(); 
//		mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void addOperatorExists()throws Exception{
		formulaViewTreePane.addOperatorExists(); 
//		mebnController.updateFormulaActiveContextNode(); 
	}
	
	public void showArgumentPanel(NodeFormulaTree nodeFormulaActive){
		// TODO The user can select relationship that is not present in any group.		
//		formulaEditionPane.setArgumentSelectionTab(nodeFormulaActive.getName());
		formulaEditionPane.setArgumentSelectionTab(
				(EventNCPointer)nodeFormulaActive.getNodeVariable());
	}
	
	public void updateFormulaActiveContextNode() {
		String formula = ncVariableModel.updateLabel();
		formulaEditionPane.setFormula(formula);
		
//		mebnEditionPane.getNetworkWindow().getGraphPane().update();		
	}
	
	public void updateArgumentsOfObject(Object node){
		
		NodeFormulaTree nodePlace = formulaViewTreePane.getNodeFormulaActive();
		if (node instanceof EventNCPointer) {
			EventNCPointer eventPointer = (EventNCPointer)node;
			
			String sentence = null;
			if (eventPointer.getEventVariable().getRelationship() != null) {
				sentence = eventPointer.getEventVariable().getRelationship() + "(";
				for (int j = 0; j < eventPointer.getOvArgumentList().size(); j++) {
					sentence = sentence + eventPointer.getOvArgumentList().get(j).getVariable() + ", ";
				}
				int index = sentence.lastIndexOf(", ");
				sentence = sentence.substring(0, index);
				sentence = sentence + ")";
			}			
			nodePlace.setName(sentence);

//			// TODO change to better form to separate between cause and effect variable
			nodePlace.setNodeVariable((EventNCPointer)node);

			nodePlace.setTypeNode(EnumType.OPERAND); 
			nodePlace.setSubTypeNode(EnumSubType.NODE);
			formulaViewTreePane.updateTree();			
		}
	}

	/**
	 * @return the formulaViewTreePane
	 */
	public FormulaViewTreePane getFormulaViewTreePane() {
		return formulaViewTreePane;
	}


	/**
	 * @param formulaViewTreePane the formulaViewTreePane to set
	 */
	public void setFormulaViewTreePane(FormulaViewTreePane formulaViewTreePane) {
		this.formulaViewTreePane = formulaViewTreePane;
	}
}
