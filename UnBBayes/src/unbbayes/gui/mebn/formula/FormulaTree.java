package unbbayes.gui.mebn.formula;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import unbbayes.controller.FormulaTreeController;
import unbbayes.controller.IconController;
import unbbayes.gui.mebn.formula.NodeFormulaTree;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.builtInRV.BuiltInRVAnd;
import unbbayes.prs.mebn.builtInRV.BuiltInRVEqualTo;
import unbbayes.prs.mebn.builtInRV.BuiltInRVExists;
import unbbayes.prs.mebn.builtInRV.BuiltInRVForAll;
import unbbayes.prs.mebn.builtInRV.BuiltInRVIff;
import unbbayes.prs.mebn.builtInRV.BuiltInRVImplies;
import unbbayes.prs.mebn.builtInRV.BuiltInRVNot;
import unbbayes.prs.mebn.builtInRV.BuiltInRVOr;
import unbbayes.prs.mebn.entity.Entity;

/** 
 * Tree that represents the formula of a context node. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * */

/*-----------------------------------------------------------------------------------------
 
 Gramática da FOL suportada: 
 
 <Fórmula> := 
 <Variavel Aleatoria> ( <OVariable, OVariable...> )
 |  <Fórmula> <Conectivo> <Fórmula>
 |  <Quantificador> <Exemplar_Sequence> <Fórmula>
 | NOT <Fórmula>
 |  <Termo> EQUALTO <Termo>
 
 <Termo> := [Variável Aleatória]
 | <Constante> 
 | <Variável>
 
 <Conectivo> := IMPLIES | AND | OR | IFF
 
 <Quantificador> := FORALL | EXISTS
 
 <Constante> := [Entidades]
 
 <Variavel> := [OVariable] 
 
 <Exemplar_Sequence> := [OVariable, OVariable, OVariable...]
 
 ----------------------------------------------------------------------*/

public class FormulaTree extends JTree{
	
	private FormulaTreeController formulaTreeController; 
	
	BuilderMenuNode builderMenuNode;
	
	private ContextNode contextNode; 
	
	private DefaultMutableTreeNode root;     
	private DefaultTreeModel model; 
	
	private NodeFormulaTree nodeFormulaActive; 
	private DefaultMutableTreeNode nodeActive; 
	
	/**
	 * @param _controller The controller of this tree
	 * @param _contextNode The contextNode where this tree is the formula
	 */
	
	public FormulaTree(FormulaTreeController _controller, ContextNode _contextNode) {
		
		formulaTreeController = _controller;    
		contextNode = _contextNode; 
		builderMenuNode = new BuilderMenuNode(_controller); 
		
		if(contextNode.getFormulaTree() == null){
			NodeFormulaTree rootFormula = new NodeFormulaTree("formula", enumType.FORMULA, 	enumSubType.NOTHING, null);  
			root = new DefaultMutableTreeNode(rootFormula);    
			createTree();
			contextNode.setFormulaTree(root); 
		}
		else{
			buildTree(); 
		}
		
		model = new DefaultTreeModel(root);
		
		setModel(model);
		
		// set up node icons
		setCellRenderer(new FormulaTreeCellRenderer());	
		
		updateTree(); 
		
		/*------------------ Adicionar listeners -----------------------*/
		
		addMouseListener(new MouseListenerTree()); 
		
		super.treeDidChange();
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
		
		root = contextNode.getFormulaTree();  
		
	}
	
	
	/**
	 * Remonta a arvore.
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
	
	public void addOperatorAnd(){
		BuiltInRV builtInRV = new BuiltInRVAnd(); 
		addSimpleOperatorInTree(builtInRV, enumSubType.AND); 			
	}
	
	public void addOperatorOr(){
		BuiltInRV builtInRV = new BuiltInRVOr(); 
		addSimpleOperatorInTree(builtInRV, enumSubType.OR); 	
	}
	
	public void addOperatorNot(){
		BuiltInRV builtInRV = new BuiltInRVNot(); 
		addSimpleOperatorInTree(builtInRV, enumSubType.NOT); 
	}
	
	public void addOperatorEqualTo(){
		BuiltInRV builtInRV = new BuiltInRVEqualTo(); 
		addSimpleOperatorInTree(builtInRV,enumSubType.EQUALTO); 
	}
	
	public void addOperatorIf(){
		BuiltInRV builtInRV = new BuiltInRVIff(); 
		addSimpleOperatorInTree(builtInRV,enumSubType.IFF); 
	}
	
	public void addOperatorImplies(){
		BuiltInRV builtInRV = new BuiltInRVImplies(); 
		addSimpleOperatorInTree(builtInRV, enumSubType.IMPLIES); 
	}
	
	public void addOperatorForAll(){
		BuiltInRV builtInRV = new BuiltInRVForAll(); 
		addQuantifierOperatorInTree(builtInRV, enumSubType.FORALL); 
	}
	
	public void addOperatorExists(){
		BuiltInRV builtInRV = new BuiltInRVExists(); 
		addQuantifierOperatorInTree(builtInRV, enumSubType.EXISTS); 
	}
	
	/**
	 * Add a simple operator in the place of the node active of the tree. 
	 * - If this node already is a operator, your actual childs will be removed
	 * and a new operador with new empty chils will be create. 
	 * 
	 * @param builtInRV builtIn of operator
	 * @param subType type of the operator
	 */
	public void addSimpleOperatorInTree(BuiltInRV builtInRV, enumSubType subType){
		
		NodeFormulaTree nodeFormula = (NodeFormulaTree)nodeActive.getUserObject(); 
		NodeFormulaTree operandoChild; 
		DefaultMutableTreeNode nodeChild; 
		
		/* check if the nodeActive of the tree permit this operation */
		if(nodeFormula.getTypeNode() == enumType.VARIABLE_SEQUENCE){
			//TODO levantar excessão ou impedir que este tipo de situação ocorra... 
		}
		
		/* turn the node active for the operator */
		nodeActive.removeAllChildren(); 
		nodeFormula.setName(builtInRV.getName()); 
		nodeFormula.setNodeVariable(builtInRV);
		nodeFormula.setTypeNode(enumType.SIMPLE_OPERATOR); 
		nodeFormula.setSubTypeNode(subType); 
		
		for (int i = 1; i <= builtInRV.getNumOperandos(); i++){
			operandoChild = new NodeFormulaTree("op_" + i, enumType.OPERANDO, enumSubType.NOTHING, null); 
			nodeChild = new DefaultMutableTreeNode(operandoChild); 
			nodeActive.add(nodeChild); 
			nodeFormula.addChildren(operandoChild); 
		}
		
		updateTree(); 
		
	}
	
	public void addQuantifierOperatorInTree(BuiltInRV builtInRV, enumSubType subType){
		
		NodeFormulaTree nodeFormula = (NodeFormulaTree)nodeActive.getUserObject(); 
		
		NodeFormulaTree nodeFormulaTree; 
		DefaultMutableTreeNode node; 
		
		nodeFormula.setName(builtInRV.getName()); 
		nodeFormula.setNodeVariable(builtInRV);
		nodeFormula.setTypeNode(enumType.QUANTIFIER_OPERATOR);
		nodeFormula.setSubTypeNode(subType); 
		
		/* adicionar nodo para insercao da sequencia de variaveis */
		nodeFormulaTree = new NodeFormulaTree("Var", enumType.VARIABLE_SEQUENCE, enumSubType.NOTHING, null); 
		node = new DefaultMutableTreeNode(nodeFormulaTree); 
		nodeActive.add(node);
		nodeFormula.addChildren(nodeFormulaTree); 
		
		
		/* adicionar nodo para a insercao da formula */
		nodeFormulaTree = new NodeFormulaTree("Formula", enumType.FORMULA, enumSubType.NOTHING, null); 
		node = new DefaultMutableTreeNode(nodeFormulaTree); 
		nodeActive.add(node); 
		nodeFormula.addChildren(nodeFormulaTree); 
		
		updateTree(); 
		
	}
	
	/**
	 * Painel que mostra quais são as variaveis disponiveis para
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
			if(nodeFormula.getTypeNode() == enumType.QUANTIFIER_OPERATOR){
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
					nodeFormulaActive.setTypeNode(enumType.OPERANDO); 
					nodeFormulaActive.setSubTypeNode(enumSubType.VARIABLE);
					
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
		while(((NodeFormulaTree)nodeActual.getUserObject()).getTypeNode() != enumType.QUANTIFIER_OPERATOR){
			nodeActual = (DefaultMutableTreeNode)nodeActual.getParent(); 
		}
		
		nodeQuantifier = (NodeFormulaTree)(nodeActual.getUserObject()); 
	
		/* descer pegando todas as variaveis e armazenando na árvore de variáveis */
		nodeActual.getChildCount(); 
		
		
		
		return exemplarPainel; 
		
		
	}
	
	
	/**
	 * add one ordinary variable in the formula tree (replace
	 * the actual node of the formula for the ordinary variable)
	 * 
	 * @param ov
	 */
	public void addNode(Node node){
		
		NodeFormulaTree nodePlace = (NodeFormulaTree)nodeActive.getUserObject(); 
		
		nodePlace.setName(node.getName()); 
		nodePlace.setNodeVariable(node);
		nodePlace.setTypeNode(enumType.OPERANDO); 
		nodePlace.setSubTypeNode(enumSubType.NODE); 
		
		updateTree(); 
	}	
	
	/**
	 * 
	 *
	 */
	public void addExemplar(){
		
		NodeFormulaTree nodePlace = (NodeFormulaTree)nodeActive.getUserObject();
		String name = "Teste"; 
		
		NodeFormulaTree variable = new NodeFormulaTree(name, enumType.VARIABLE, enumSubType.NOTHING, null);
		DefaultMutableTreeNode nodeVariable = new DefaultMutableTreeNode(variable); 
		nodeActive.add(nodeVariable); 
		
		nodePlace.addChildren(variable); 
	}	
	
	public void addEntity(Entity entity){
		
		NodeFormulaTree nodePlace = (NodeFormulaTree)nodeActive.getUserObject(); 
		
		nodePlace.setName(entity.getName()); 
		nodePlace.setNodeVariable(entity);
		nodePlace.setTypeNode(enumType.OPERANDO); 
		nodePlace.setSubTypeNode(enumSubType.ENTITY); 
		
		updateTree(); 			
	}
	
	/**
	 * add one ordinary variable in the formula tree (replace
	 * the actual node of the formula for the ordinary variable)
	 * 
	 * @param ov
	 */
	public void addOVariable(OrdinaryVariable ov){
		
		NodeFormulaTree nodePlace = (NodeFormulaTree)nodeActive.getUserObject(); 
		
		if(nodePlace.getTypeNode() == enumType.VARIABLE_SEQUENCE){
			
			//NodeFormulaTree exemplar = new NodeFormulaTree()
			
			
		}
		else{
		   nodePlace.setName(ov.getName()); 
		   nodePlace.setNodeVariable(ov);
		   nodePlace.setTypeNode(enumType.OPERANDO); 
		   nodePlace.setSubTypeNode(enumSubType.OVARIABLE); 
		}
		updateTree(); 
		
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
					
				case OPERANDO: 
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
					
				default:
					break;
				}
				
				
			} else if (e.getClickCount() == 2
					&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {						
				
				
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
}

