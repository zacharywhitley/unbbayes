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
package unbbayes.gui.umpst.implementation;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import unbbayes.controller.umpst.FormulaTreeControllerUMP;
import unbbayes.gui.umpst.implementation.exception.FormulaTreeConstructionException;
import unbbayes.model.umpst.implementation.NecessaryConditionVariableModel;
import unbbayes.model.umpst.implementation.NodeFormulaTreeUMP;
import unbbayes.model.umpst.rule.RuleModel;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.builtInRV.BuiltInRVAnd;
import unbbayes.prs.mebn.builtInRV.BuiltInRVEqualTo;
import unbbayes.prs.mebn.builtInRV.BuiltInRVExists;
import unbbayes.prs.mebn.builtInRV.BuiltInRVForAll;
import unbbayes.prs.mebn.builtInRV.BuiltInRVIff;
import unbbayes.prs.mebn.builtInRV.BuiltInRVImplies;
import unbbayes.prs.mebn.builtInRV.BuiltInRVNot;
import unbbayes.prs.mebn.builtInRV.BuiltInRVOr;
import unbbayes.prs.mebn.context.EnumSubType;
import unbbayes.prs.mebn.context.EnumType;

/** 
 * View Tree that represents the formula of a context node. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * */

/*-----------------------------------------------------------------------------------------
 
 Gramática da FOL suportada: 
 
 <Fórmula> := 
 <Variavel Aleatoria> ( <OVariable, OVariable...> )
 |  <Fórmula> <Conectivo> <Fórmula>
 |  <Quantificador> <Exemplar_Sequence> <F�rmula>
 | NOT <F�rmula>
 |  <Termo> EQUALTO <Termo>
 
 <Termo> := [Variável Aleatoria]
 | <Constante> 
 | <Variável>
 
 <Conectivo> := IMPLIES | AND | OR | IFF
 
 <Quantificador> := FORALL | EXISTS
 
 <Constante> := [Entidades]
 
 <Variavel> := [OVariable] 
 
 <Exemplar_Sequence> := [OVariable, OVariable, OVariable...]
 
 ----------------------------------------------------------------------*/

public class FormulaViewTreePane extends JTree {
	private RuleModel rule;	
	private FormulaTreeControllerUMP formulaTreeController;	
	private NecessaryConditionVariableModel ncVariableModel;
	
	private DefaultMutableTreeNode rootTreeView;
	private DefaultMutableTreeNode nodeActive;
	private NodeFormulaTreeUMP nodeFormulaActive;
	private NodeFormulaTreeUMP rootTreeFormula;
	private DefaultTreeModel model;
	
	
	/** Load resource file from this package */
	private ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.gui.umpst.resources.Resources.class.getName());	
	
	public FormulaViewTreePane(RuleModel rule, FormulaTreeControllerUMP formulaTreeController, 
			NecessaryConditionVariableModel _ncVariableModel, DefaultTreeModel _model) {
		super(_model);
		this.model = _model;
		this.rule = rule;
		this.ncVariableModel = _ncVariableModel;
		this.formulaTreeController = formulaTreeController;
		
		buildTree();
		
		setModel(model);
		
		// set up node icons
		setCellRenderer(new FormulaTreeCellRenderer());
		updateTree();
		addMouseListener(new MouseListenerTree());		
		treeDidChange();
	}
	
	public void buildTree() {
		rootTreeFormula = ncVariableModel.getFormulaTree();
		rootTreeView = new DefaultMutableTreeNode(rootTreeFormula);		
		buildChildren(rootTreeFormula, rootTreeView);
	}
	
	public void buildChildren(NodeFormulaTreeUMP nodeFormulaFather, DefaultMutableTreeNode nodeTreeFather){		
		for(NodeFormulaTreeUMP child: nodeFormulaFather.getChildrenUMP()){
			DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(child); 
			nodeTreeFather.add(treeNode);
			buildChildren(child, treeNode); 
		}
	}
	
	private class MouseListenerTree extends MouseAdapter{
		
		public void mousePressed(MouseEvent e) {			
			int selRow = getRowForLocation(e.getX(), e.getY());
			if (selRow == -1) {
				return;
			}
			
			TreePath selPath = getPathForLocation(e.getX(), e.getY());
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
			
			nodeActive = node; 
			nodeFormulaActive = (NodeFormulaTreeUMP)node.getUserObject(); 
			
			if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {				
				switch(nodeFormulaActive.getTypeNode()){				
				case FORMULA: 
				case EMPTY: 
//					builderMenuNode.buildPopupFormula().show(e.getComponent(),e.getX(),e.getY()); 
					break; 
					
				case OPERAND: 
//					builderMenuNode.buildPopupOperando().show(e.getComponent(),e.getX(),e.getY()); 
					break; 
					
				case SIMPLE_OPERATOR: 
//					builderMenuNode.buildPopupOperator().show(e.getComponent(),e.getX(),e.getY()); 
					break; 
					
				case QUANTIFIER_OPERATOR: 
//					builderMenuNode.buildPopupOperator().show(e.getComponent(),e.getX(),e.getY()); 
					break; 						
					
				case VARIABLE_SEQUENCE: 
//					builderMenuNode.buildPopupExemplarList().show(e.getComponent(),e.getX(),e.getY()); 
					break; 
				
				case VARIABLE: 
//					builderMenuNode.buildPopupExemplar().show(e.getComponent(),e.getX(),e.getY()); 
					
				default:
					break;
				}
				
				
			} else if (e.getClickCount() == 2 && e.getModifiers() == MouseEvent.BUTTON1_MASK) {
				switch(nodeFormulaActive.getTypeNode()){
				case OPERAND: 
					switch(nodeFormulaActive.getSubTypeNode()){
					case NODE: 
						formulaTreeController.showArgumentPanel(nodeFormulaActive); 
					}
					break; 
					
				default:
					break;
				}
				
			} else if (e.getClickCount() == 1) {
				
			}
		}
	}
	
	/**
	 * The operands used in this methods is defined by MEBN plug-in.
	 * @throws Exception
	 */
	
	public void addOperatorExists() throws Exception{
		BuiltInRV builtInRV = new BuiltInRVExists(); 
		addQuantifierOperatorInTree(builtInRV, EnumSubType.EXISTS); 
	}
	
	public void addOperatorForAll() throws Exception{
		BuiltInRV builtInRV = new BuiltInRVForAll(); 
		addQuantifierOperatorInTree(builtInRV, EnumSubType.FORALL); 
	}
	
	public void addOperatorImplies() throws Exception{
		BuiltInRV builtInRV = new BuiltInRVImplies(); 
		addSimpleOperatorInTree(builtInRV, EnumSubType.IMPLIES); 
	}
	
	public void addOperatorIf() throws Exception{
		BuiltInRV builtInRV = new BuiltInRVIff(); 
		addSimpleOperatorInTree(builtInRV,EnumSubType.IFF); 
	}
	
	public void addOperatorEqualTo()  throws Exception{
		BuiltInRV builtInRV = new BuiltInRVEqualTo(); 
		addSimpleOperatorInTree(builtInRV,EnumSubType.EQUALTO); 
	}
	
	public void addOperatorNot()  throws Exception{
		BuiltInRV builtInRV = new BuiltInRVNot(); 
		addSimpleOperatorInTree(builtInRV, EnumSubType.NOT); 
	}
	
	public void addOperatorOr()  throws Exception{
		BuiltInRV builtInRV = new BuiltInRVOr(); 
		addSimpleOperatorInTree(builtInRV, EnumSubType.OR); 	
	}
	
	public void addOperatorAnd() throws Exception{
		BuiltInRV builtInRV = new BuiltInRVAnd(); 
		addSimpleOperatorInTree(builtInRV, EnumSubType.AND);
	}
	
	/**
	 * Add a quantifier operator in the place of the node active of the tree. 
	 * - If this node already is a operator, your actual childs will be removed
	 * and a new operador will be create. 
	 * - The <nodeActive> may not be of <enumType> VARIABLE_SEQUENCE (Exception)
	 * @param builtInRV builtIn of operator
	 * @param subType type of the operator
	 */	
	
	public void addQuantifierOperatorInTree(BuiltInRV builtInRV, EnumSubType subType) throws FormulaTreeConstructionException{
		
		NodeFormulaTreeUMP nodeFormula = (NodeFormulaTreeUMP)nodeActive.getUserObject(); 
		
		NodeFormulaTreeUMP nodeFormulaTree; 
		DefaultMutableTreeNode node; 
		
		/* check if the nodeActive of the tree permit this operation */
		if(nodeFormula.getTypeNode() == EnumType.VARIABLE_SEQUENCE){
			throw new FormulaTreeConstructionException(resource.getString("notOperator")); 
		}
		
		nodeActive.removeAllChildren(); 
		nodeFormula.removeAllChildren(); 
		nodeFormula.setName(builtInRV.getName()); 
		nodeFormula.setMnemonic(builtInRV.getMnemonic()); 
		nodeFormula.setNodeVariable(builtInRV);
		nodeFormula.setTypeNode(EnumType.QUANTIFIER_OPERATOR);
		nodeFormula.setSubTypeNode(subType); 
		
		// Add node to insert variable sequence
		nodeFormulaTree = new NodeFormulaTreeUMP("Var", EnumType.VARIABLE_SEQUENCE, EnumSubType.NOTHING, null); 
		node = new DefaultMutableTreeNode(nodeFormulaTree); 
		nodeActive.add(node);
		nodeFormula.addChild(nodeFormulaTree); 
		
		// Add node to insert formula
		nodeFormulaTree = new NodeFormulaTreeUMP("Formula", EnumType.FORMULA, EnumSubType.NOTHING, null); 
		node = new DefaultMutableTreeNode(nodeFormulaTree); 
		nodeActive.add(node); 
		nodeFormula.addChild(nodeFormulaTree); 
		
		updateTree(); 
	}
	
	
	/**
	 * Add a simple operator in the place of the node active of the tree. 
	 * - If this node already is a operator, your actual child will be removed
	 * and a new operator with new empty child will be create. 
	 * - The <nodeActive> may not be of <enumType> VARIABLE_SEQUENCE (Exception)
	 * @param builtInRV builtIn of operator
	 * @param subType type of the operator
	 */
	public void addSimpleOperatorInTree(BuiltInRV builtInRV, EnumSubType subType) throws FormulaTreeConstructionException{
//		
		NodeFormulaTreeUMP nodeFormula = (NodeFormulaTreeUMP)nodeActive.getUserObject(); 
		NodeFormulaTreeUMP operandoChild; 
		DefaultMutableTreeNode nodeChild;	
		
		/* check if the nodeActive of the tree permit this operation */
		if(nodeFormula.getTypeNode() == EnumType.VARIABLE_SEQUENCE){
			throw new FormulaTreeConstructionException(resource.getString("notOperator")); 
		}
		
		/* turn the node active for the operator */
		nodeActive.removeAllChildren(); 
		nodeFormula.removeAllChildren(); 
		
		nodeFormula.setName(builtInRV.getName());
		nodeFormula.setMnemonic(builtInRV.getMnemonic()); 
		nodeFormula.setNodeVariable(builtInRV);
		nodeFormula.setTypeNode(EnumType.SIMPLE_OPERATOR); 
		nodeFormula.setSubTypeNode(subType);
		
		for (int i = 1; i <= builtInRV.getNumOperandos(); i++){
			operandoChild = new NodeFormulaTreeUMP("op_" + i, EnumType.OPERAND, EnumSubType.NOTHING, null); 
			nodeChild = new DefaultMutableTreeNode(operandoChild); 
			nodeActive.add(nodeChild); 
			nodeFormula.addChild(operandoChild); 
		}		
		updateTree();	
	}
	
	/**
	 * Rebuild the tree.
	 */
	public void updateTree() {		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();		
		((DefaultTreeModel) getModel()).reload(root);
		
		for (int i = 0; i < getRowCount(); i++){
			expandRow(i);
		}
	}

	private void createTree() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
		expandRow(0);
	}

	/**
	 * @return the model
	 */
	public DefaultTreeModel getModel() {
		return model;
	}
	
	/**
	 * @param model the model to set
	 */
	public void setModel(DefaultTreeModel model) {
		this.model = model;
	}
	
	public NodeFormulaTreeUMP getNodeFormulaActive() {
		return nodeFormulaActive;
	}

	public void setNodeFormulaActive(NodeFormulaTreeUMP nodeFormulaActive) {
		this.nodeFormulaActive = nodeFormulaActive;
	}

	public DefaultMutableTreeNode getNodeActive() {
		return nodeActive;
	}

	public void setNodeActive(DefaultMutableTreeNode nodeActive) {
		this.nodeActive = nodeActive;
	}
	
    public void addNewNodeInTree(NodeFormulaTreeUMP node){
    	nodeActive.add(new DefaultMutableTreeNode(node));     	
    }
		
}

