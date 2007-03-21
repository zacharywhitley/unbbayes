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
 
 <Fórmula> := <Fórmula Atômica>
 |  <Fórmula> <Conectivo> <Fórmula>
 |  <Quantificador> <Variável_Sequente> <Fórmula>
 | ~ <Fórmula>
 | ( <Fórmula>)
 
 <Fórmula Atômica> := <Predicado> ( <Termo>, ... )
 |  <Termo> = <Termo> 
 
 <Termo> := [Variável Aleatória]
 | <Constante> 
 | <Variável>
 
 <Conectivo> := implies | ^ | v | iff
 
 <Quantificador> := for all | exists
 
 <Constante> := [Entidades]
 
 <Variavel> := [OVariable] 
 
 <Variavel_Sequence> := [OVariable] | <Variable_Sequence> , [OVariable]
 
 <Predicado> := [Variável Aleatória]
 
 ----------------------------------------------------------------------*/

public class FormulaTree extends JTree{
	
	private FormulaTreeController formulaTreeController; 
	
	private JPopupMenu popupOperando = new JPopupMenu(); 
	private JPopupMenu popupOperator = new JPopupMenu(); 
	private JPopupMenu popupVariable = new JPopupMenu(); 
	
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
		
		createPopupMenuFormula(); 
		createPopupMenuOperator(); 
		createPopupMenuVariable(); 
		
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
	
	
	private void createPopupMenuFormula(){
		
		JMenu menuOperator =  new JMenu("addOperator");
		JMenu menuOperando = new JMenu("addOperando");
		
		JMenuItem itemDelete = new JMenuItem("delete"); 
		
		JMenuItem itemNode =  new JMenuItem("addNode");
		JMenuItem itemEntity =    new JMenuItem("addEntity"); 
		JMenuItem itemOVariable = new JMenuItem("addOVariable");
		JMenuItem itemVariable = new JMenuItem("addVariable"); 
		JMenuItem itemSkolen = new JMenuItem("addSkolen");
		
		JMenuItem itemAnd = new JMenuItem("and"); 
		JMenuItem itemOr = new JMenuItem("or");
		JMenuItem itemNot = new JMenuItem("not");
		JMenuItem itemEqual = new JMenuItem("equal"); 
		JMenuItem itemIff = new JMenuItem("iff"); 
		JMenuItem itemImplies = new JMenuItem("Implies"); 
		JMenuItem itemForall = new JMenuItem("For All"); 
		JMenuItem itemExists = new JMenuItem("Exists"); 
		
		menuOperator.add(itemAnd); 
		menuOperator.add(itemOr); 
		menuOperator.add(itemNot); 
		menuOperator.add(itemEqual); 
		menuOperator.add(itemIff); 
		menuOperator.add(itemImplies); 
		menuOperator.add(itemForall); 
		menuOperator.add(itemExists); 
		
		menuOperando.add(itemNode); 
		menuOperando.add(itemOVariable); 
		menuOperando.add(itemEntity); 
		menuOperando.add(itemVariable); 
		itemSkolen.setArmed(false); 
		menuOperando.add(itemSkolen); 
		
		itemAnd.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.addOperatorAnd();  
			}
		});		
		
		itemOr.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.addOperatorOr();    
			}
		});				
		
		itemNot.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.addOperatorNot(); 
			}
		});	
		
		itemEqual.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.addOperatorEqualTo(); 
			}
		});						
		
		itemIff.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.addOperatorIf(); 
			}
		});						
		
		itemImplies.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.addOperatorImplies(); 
			}
		});						
		
		itemForall.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.addOperatorForAll(); 
			}
		});						
		
		itemExists.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				formulaTreeController.addOperatorExists(); 
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
		
		itemDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode)nodeActive.getParent(); 
				
				/* primeira formula */
				
				if(parent == null){
					NodeFormulaTree rootFormula = new NodeFormulaTree("formula", enumType.FORMULA, enumSubType.NOTHING, null); 					
					nodeActive.setUserObject(rootFormula); 
					return; 
				}
				
				NodeFormulaTree nodeFormulaParent = (NodeFormulaTree)parent.getUserObject(); 
				
				/* operando */
				if(nodeFormulaParent.getTypeNode() == enumType.SIMPLE_OPERATOR){
					
				}
				
				/* variavel: mais problematico */
				
				
				/* formula (quantificador) */
				
			}
		}); 
		
		itemSkolen.setEnabled(false); 
		
		popupOperando.add(menuOperator);
		popupOperando.add(menuOperando); 
		popupOperando.add(itemDelete); 
	}
	
	private void createPopupMenuOperator(){
		
		JMenuItem itemDelete = new JMenuItem("delete"); 
		
		itemDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode)nodeActive.getParent(); 
				
				/* primeira formula */
				
				if(parent == null){
					NodeFormulaTree rootFormula = new NodeFormulaTree("formula", enumType.FORMULA, enumSubType.NOTHING, null); 					
					nodeActive.setUserObject(rootFormula); 
					nodeActive.removeAllChildren(); 
					updateTree(); 
					return; 
				}
				
				NodeFormulaTree nodeFormulaParent = (NodeFormulaTree)parent.getUserObject(); 
				
				if((nodeFormulaParent.getTypeNode() == enumType.SIMPLE_OPERATOR) || (nodeFormulaParent.getTypeNode() == enumType.QUANTIFIER_OPERATOR) ){
					
					int indiceNewChild = parent.getChildCount();
					
					NodeFormulaTree operandoChild = new NodeFormulaTree("op_" + (indiceNewChild + 1) , enumType.EMPTY, enumSubType.NOTHING, null); 
					nodeActive.setUserObject(operandoChild); 
					nodeActive.removeAllChildren(); 
					updateTree(); 
				}
				
			}
		}); 
		
		popupOperator.add(itemDelete); 
	}		
	
	private void createPopupMenuVariable(){
		
		JMenuItem itemAdd = new JMenuItem("add"); 
		
		itemAdd.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				addVariable(); 
				updateTree(); 
			}
		}); 
		
		popupVariable.add(itemAdd); 
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
			operandoChild = new NodeFormulaTree("op_" + i, enumType.EMPTY, enumSubType.NOTHING, null); 
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
	 * Adiciona uma variavel na arvore da formula. A variavel deve
	 * ser inserida em um pai do tipo enumType.VARIABLE_SEQUENCE
	 *
	 */
	public void addVariable(){
		
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
		
		nodePlace.setName(ov.getName()); 
		nodePlace.setNodeVariable(ov);
		nodePlace.setTypeNode(enumType.OPERANDO); 
		nodePlace.setSubTypeNode(enumSubType.OVARIABLE); 
		
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
					popupOperando.setEnabled(true);
					popupOperando.show(e.getComponent(),e.getX(),e.getY());
					break; 
					
				case SIMPLE_OPERATOR: 
					popupOperator.setEnabled(true);
					popupOperator.show(e.getComponent(),e.getX(),e.getY());
					break; 
					
				case QUANTIFIER_OPERATOR: 
					popupOperator.setEnabled(true);
					popupOperator.show(e.getComponent(),e.getX(),e.getY());
					break; 						
					
				case VARIABLE_SEQUENCE: 
					popupVariable.setEnabled(true); 
					popupVariable.show(e.getComponent(),e.getX(),e.getY());
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
}

