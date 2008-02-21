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
package unbbayes.gui.mebn.formula;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import unbbayes.controller.FormulaTreeController;
import unbbayes.gui.mebn.formula.exception.FormulaTreeConstructionException;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
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
import unbbayes.prs.mebn.context.NodeFormulaTree;

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

public class FormulaViewTree extends JTree{
	
	private FormulaTreeController formulaTreeController; 
	
	BuilderMenuNode builderMenuNode;
	
	private ContextNode contextNode; 
	
	private DefaultMutableTreeNode rootTreeView;     
	private DefaultTreeModel model; 
	
	private NodeFormulaTree rootTreeFormula; 
	
	private NodeFormulaTree nodeFormulaActive; 
	private DefaultMutableTreeNode nodeActive; 
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
	/**
	 * @param _controller The controller of this tree
	 * @param _contextNode The contextNode where this tree is the formula
	 */
	
	public FormulaViewTree(FormulaTreeController _controller, ContextNode _contextNode) {
		
		//TODO this class don't is a controller... all methods that change the model 
		//shoud stay in the FormulaTreeController. 
		
		formulaTreeController = _controller;    
		contextNode = _contextNode; 
		builderMenuNode = new BuilderMenuNode(_controller); 
		
		if(contextNode.getFormulaTree() == null){
			NodeFormulaTree rootFormula = new NodeFormulaTree("formula", EnumType.FORMULA, 	EnumSubType.NOTHING, null);  
			rootTreeView = new DefaultMutableTreeNode(rootFormula);    
			createTree();
			contextNode.setFormulaTree(rootFormula); 
		}
		else{
			buildTree(); 
		}
		
		model = new DefaultTreeModel(rootTreeView);
		
		setModel(model);
		
		// set up node icons
		setCellRenderer(new FormulaTreeCellRenderer());	
		
		updateTree(); 
		
		/*------------------ Adicionar listeners -----------------------*/
		
		addMouseListener(new MouseListenerTree()); 
		
		treeDidChange();
	}
	
	/**
	 * Create a new formula tree. 
	 * 
	 * @param root
	 */
	public void recriateTree(){
		NodeFormulaTree rootFormula = new NodeFormulaTree("formula", EnumType.FORMULA, EnumSubType.NOTHING, null); 	
		DefaultMutableTreeNode nodeTree =  new DefaultMutableTreeNode(rootFormula); 
		rootTreeView = new DefaultMutableTreeNode(rootFormula); 
		contextNode.setFormulaTree(rootFormula); 
		model = new DefaultTreeModel(rootTreeView);
		setModel(model);
		updateTree(); 
	}
	
	/**
	 * 
	 *
	 */
	
	private void createTree() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
		expandRow(0); 	
	}
	
	
	/**
	 * Reconstroi a arvore de formula de um contextNode. 
	 *
	 */
	public void buildTree(){
		
		rootTreeFormula = contextNode.getFormulaTree();
		
		rootTreeView = new DefaultMutableTreeNode(rootTreeFormula);  
		
		buildChildren(rootTreeFormula, rootTreeView); 
		
	}
	
	public void buildChildren(NodeFormulaTree nodeFormulaFather, DefaultMutableTreeNode nodeTreeFather){
		
		for(NodeFormulaTree child: nodeFormulaFather.getChildren()){
			DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(child); 
			nodeTreeFather.add(treeNode);
			buildChildren(child, treeNode); 
		}
		
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
	

	private DefaultMutableTreeNode findUserObject(String treeNode,
			DefaultMutableTreeNode root) {
		Enumeration e = root.breadthFirstEnumeration();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e
			.nextElement();
			if (node.getUserObject().toString().equals(treeNode)) {
				return node;
			}
		}
		return null;
	}
	
	
	public void addOperatorAnd() throws Exception{
		BuiltInRV builtInRV = new BuiltInRVAnd(); 
		addSimpleOperatorInTree(builtInRV, EnumSubType.AND);
	}
	
	public void addOperatorOr()  throws Exception{
		BuiltInRV builtInRV = new BuiltInRVOr(); 
		addSimpleOperatorInTree(builtInRV, EnumSubType.OR); 	
	}
	
	public void addOperatorNot()  throws Exception{
		BuiltInRV builtInRV = new BuiltInRVNot(); 
		addSimpleOperatorInTree(builtInRV, EnumSubType.NOT); 
	}
	
	public void addOperatorEqualTo()  throws Exception{
		BuiltInRV builtInRV = new BuiltInRVEqualTo(); 
		addSimpleOperatorInTree(builtInRV,EnumSubType.EQUALTO); 
	}
	
	public void addOperatorIf() throws Exception{
		BuiltInRV builtInRV = new BuiltInRVIff(); 
		addSimpleOperatorInTree(builtInRV,EnumSubType.IFF); 
	}
	
	public void addOperatorImplies()throws Exception{
		BuiltInRV builtInRV = new BuiltInRVImplies(); 
		addSimpleOperatorInTree(builtInRV, EnumSubType.IMPLIES); 
	}
	
	public void addOperatorForAll() throws Exception{
		BuiltInRV builtInRV = new BuiltInRVForAll(); 
		addQuantifierOperatorInTree(builtInRV, EnumSubType.FORALL); 
	}
	
	public void addOperatorExists() throws Exception{
		BuiltInRV builtInRV = new BuiltInRVExists(); 
		addQuantifierOperatorInTree(builtInRV, EnumSubType.EXISTS); 
	}
	
	/**
	 * Add a simple operator in the place of the node active of the tree. 
	 * - If this node already is a operator, your actual childs will be removed
	 * and a new operador with new empty chils will be create. 
	 * - The <nodeActive> don't may be of <enumType> VARIABLE_SEQUENCE (Exception)
	 * @param builtInRV builtIn of operator
	 * @param subType type of the operator
	 */
	public void addSimpleOperatorInTree(BuiltInRV builtInRV, EnumSubType subType) throws FormulaTreeConstructionException{
		
		NodeFormulaTree nodeFormula = (NodeFormulaTree)nodeActive.getUserObject(); 
		NodeFormulaTree operandoChild; 
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
			operandoChild = new NodeFormulaTree("op_" + i, EnumType.OPERAND, EnumSubType.NOTHING, null); 
			nodeChild = new DefaultMutableTreeNode(operandoChild); 
			nodeActive.add(nodeChild); 
			nodeFormula.addChild(operandoChild); 
		}
		
		updateTree(); 
		
	}
	
	/**
	 * Add a quantifier operator in the place of the node active of the tree. 
	 * - If this node already is a operator, your actual childs will be removed
	 * and a new operador will be create. 
	 * - The <nodeActive> don't may be of <enumType> VARIABLE_SEQUENCE (Exception)
	 * @param builtInRV builtIn of operator
	 * @param subType type of the operator
	 */	
	
	public void addQuantifierOperatorInTree(BuiltInRV builtInRV, EnumSubType subType) throws FormulaTreeConstructionException{
		
		NodeFormulaTree nodeFormula = (NodeFormulaTree)nodeActive.getUserObject(); 
		
		NodeFormulaTree nodeFormulaTree; 
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
		
		/* adicionar nodo para insercao da sequencia de variaveis */
		nodeFormulaTree = new NodeFormulaTree("Var", EnumType.VARIABLE_SEQUENCE, EnumSubType.NOTHING, null); 
		node = new DefaultMutableTreeNode(nodeFormulaTree); 
		nodeActive.add(node);
		nodeFormula.addChild(nodeFormulaTree); 
		
		/* adicionar nodo para a insercao da formula */
		nodeFormulaTree = new NodeFormulaTree("Formula", EnumType.FORMULA, EnumSubType.NOTHING, null); 
		node = new DefaultMutableTreeNode(nodeFormulaTree); 
		nodeActive.add(node); 
		nodeFormula.addChild(nodeFormulaTree); 
		
		updateTree(); 
	}
	
	/**
	 * Painel que mostra quais s�o as variaveis disponiveis para
	 * se escolher uma a ser utilizada como operando na formula. 
	 *
	 * NodeTree deve ser um nodo do tipo enumType.VARIABLE.
	 *  
	 */
	
	public JPanel replaceByVariable(){
		
		JPanel painel = new JPanel(new BorderLayout()); 
		
		if (nodeActive == null) return painel; 
		
		DefaultMutableTreeNode nodeTree = nodeActive; 
		TreeNode nodeAux = nodeTree.getParent();  
		TreeNode variableListNode; 
		NodeFormulaTree nodeFormula; 
		boolean testeEnd = false; 
		
		/* contem os nodos correspondentes as variaveis disponiveis */
		final ArrayList<TreeNode> listVariables = new ArrayList<TreeNode>(); 
		
		/* 
		 * percorrer a arvore de baixo para cima montando uma lista 
		 * contendo todas as variaveis possiveis de escolha pelo 
		 * usuario. 
		 */
		while(testeEnd == false){
			nodeFormula = (NodeFormulaTree)((DefaultMutableTreeNode)nodeAux).getUserObject(); 	    	 
			if(nodeFormula.getTypeNode() == EnumType.QUANTIFIER_OPERATOR){
				variableListNode = nodeAux.getChildAt(0); 
				int numChild = variableListNode.getChildCount(); 
				for (int i = 0; i < numChild; i++){
					listVariables.add(variableListNode.getChildAt(i));	 		    			 
				}
			}
			if(nodeAux.getParent() == null){
				testeEnd = true; 
			}
			else{
				nodeAux = nodeAux.getParent(); 
			}
		}
		
		/* Montagem da lista com os nomes das variaveis... */ 
		
		String variablesNames[] = new String[listVariables.size()]; 
		int i = 0; 
		
		for(TreeNode node: listVariables){
			nodeFormula = (NodeFormulaTree)((DefaultMutableTreeNode)node).getUserObject(); 
			variablesNames[i] = nodeFormula.getName(); 
			i++; 
		}
		
		final JList list = new JList(variablesNames);
		
		list.addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e) {
				
				if ((e.getModifiers() == MouseEvent.BUTTON1_MASK) && (e.getClickCount() == 2)){
					int selectedIndex = list.getSelectedIndex(); 
					
					TreeNode treeNodeSelected = listVariables.get(selectedIndex); 
					NodeFormulaTree nodePlace = (NodeFormulaTree)((DefaultMutableTreeNode)treeNodeSelected).getUserObject(); 
					NodeFormulaTree nodeFormulaActive = (NodeFormulaTree)((DefaultMutableTreeNode)nodeActive).getUserObject(); 
					
					nodeFormulaActive.setName(nodePlace.getName()); 
					nodeFormulaActive.setNodeVariable(nodePlace);
					nodeFormulaActive.setTypeNode(EnumType.OPERAND); 
					nodeFormulaActive.setSubTypeNode(EnumSubType.VARIABLE);
					
					updateTree(); 
					
				}
				
			}
		});
		
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(100, 200));
		
		painel.add(listScroller);
		
		return painel; 
	}
	
	public JPanel replaceByExemplar(){
		
		JPanel exemplarPainel = new JPanel(new BorderLayout()); 
        
		
		if (nodeActive == null) return exemplarPainel; 
		
		DefaultMutableTreeNode nodeActual = nodeActive; 
		DefaultMutableTreeNode variableTreeRoot; 
		JTree variableTree = new JTree(); 
		
		NodeFormulaTree nodeFormulaActual = (NodeFormulaTree)nodeActive.getUserObject(); 
		NodeFormulaTree nodeQuantifier; 
		
		/* retornar ao nodo que contem o quantificador */
		while(((NodeFormulaTree)nodeActual.getUserObject()).getTypeNode() != EnumType.QUANTIFIER_OPERATOR){
			nodeActual = (DefaultMutableTreeNode)nodeActual.getParent(); 
		}
		
		nodeQuantifier = (NodeFormulaTree)(nodeActual.getUserObject()); 
	
		/* descer pegando todas as variaveis e armazenando na �rvore de vari�veis */
		nodeActual.getChildCount(); 
		
		return exemplarPainel; 
		
		
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
			nodeFormulaActive = (NodeFormulaTree)node.getUserObject(); 
			
			if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
				
				switch(nodeFormulaActive.getTypeNode()){
				
				case FORMULA: 
				case EMPTY: 
					builderMenuNode.buildPopupFormula().show(e.getComponent(),e.getX(),e.getY()); 
					break; 
					
				case OPERAND: 
					builderMenuNode.buildPopupOperando().show(e.getComponent(),e.getX(),e.getY()); 
					break; 
					
				case SIMPLE_OPERATOR: 
					builderMenuNode.buildPopupOperator().show(e.getComponent(),e.getX(),e.getY()); 
					break; 
					
				case QUANTIFIER_OPERATOR: 
					builderMenuNode.buildPopupOperator().show(e.getComponent(),e.getX(),e.getY()); 
					break; 						
					
				case VARIABLE_SEQUENCE: 
					builderMenuNode.buildPopupExemplarList().show(e.getComponent(),e.getX(),e.getY()); 
					break; 
				
				case VARIABLE: 
					builderMenuNode.buildPopupExemplar().show(e.getComponent(),e.getX(),e.getY()); 
					
				default:
					break;
				}
				
				
			} else if (e.getClickCount() == 2
					&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {						
                
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

	public NodeFormulaTree getNodeFormulaActive() {
		return nodeFormulaActive;
	}

	public void setNodeFormulaActive(NodeFormulaTree nodeFormulaActive) {
		this.nodeFormulaActive = nodeFormulaActive;
	}

	public DefaultMutableTreeNode getNodeActive() {
		return nodeActive;
	}

	public void setNodeActive(DefaultMutableTreeNode nodeActive) {
		this.nodeActive = nodeActive;
	}
	
    public void addNewNodeInTree(NodeFormulaTree node){
    	nodeActive.add(new DefaultMutableTreeNode(node));     	
    }
	
}

