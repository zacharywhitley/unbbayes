package unbbayes.gui.mebn;

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
import javax.swing.JFrame;
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
import unbbayes.controller.NetworkController;
import unbbayes.gui.GraphAction;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.builtInRV.BuiltInRVAnd;
import unbbayes.prs.mebn.builtInRV.BuiltInRVEqualTo;
import unbbayes.prs.mebn.builtInRV.BuiltInRVExists;
import unbbayes.prs.mebn.builtInRV.BuiltInRVForAll;
import unbbayes.prs.mebn.builtInRV.BuiltInRVIff;
import unbbayes.prs.mebn.builtInRV.BuiltInRVImplies;
import unbbayes.prs.mebn.builtInRV.BuiltInRVNot;
import unbbayes.prs.mebn.builtInRV.BuiltInRVOr;

/** 
 * Tree of the formula 
 * */

/*-----------------------------------------------------------------------------------------
  
A construcao da logica de primeira ordem suportada aqui segue 
 a seguinte sintaxe: 
 
 formula := formula conectivo formula
            quantificador variavel_sequence, ... formula+.
            NOT formula

 termo :=   constante 
            variavel 

 conectivo := AND
              OR
              IMPLIES
              IFF
              EQUALTO 

 quantificador := FORALL
                  EXISTS

 constante := entity
              node
              ovariable
              skolen

 variavel_sequente:= variavel [,...]

 variavel := ? 

 ----------------------------------------------------------------------*/

public class FormulaTree extends JTree{

	/* types of the nodes */
	protected enum enumType{
		EMPTY, 
		SIMPLE_OPERATOR,
		QUANTIFIER_OPERATOR, 
		FORMULA, 
		VARIABLE_SEQUENCE,
		VARIABLE, 
		OPERANDO
	}
	
	/* sub types of the nodes */
	protected enum enumSubType{
	
		/* DON'T CARE */
		NOTHING, 
		
		/* OPERANDO */
		OVARIABLE, 
		NODE, 
		ENTITY, 
		VARIABLE, 
		SKOLEN, 
				
		/* OPERATOR */
		AND, 
		OR, 
		NOT, 
		EQUALTO, 
		IMPLIES, 
		IFF, 
		FORALL, 
		EXISTS
	}
	
	NodeFormulaTree nodeFormulaActive; 
	DefaultMutableTreeNode nodeActive; 
	
	FormulaTreeController formulaTreeController; 
	
	protected IconController iconController = IconController.getInstance();
	
    private JPopupMenu popupOperando = new JPopupMenu(); 
	
    private JPopupMenu popupOperator = new JPopupMenu(); 
    
    private JPopupMenu popupVariable = new JPopupMenu(); 
    
    ContextNode contextNode; 
    
    DefaultMutableTreeNode root;     
    
    /**
     * 
     * 
     * @param controller
     */
    
	public FormulaTree(FormulaTreeController controller, ContextNode _contextNode) {
		
        formulaTreeController = controller;    
        contextNode = _contextNode; 
        
        /* Context node ainda não possui formula! Criar um nodo formula (a partir 
         * deste se faz a expansao para a formula adicionando novos nodos)
         */
        
        if(contextNode.getFormulaTree() == null){
        	NodeFormulaTree rootFormula = new NodeFormulaTree("formula", enumType.FORMULA, 	enumSubType.NOTHING, null);  
        	root = new DefaultMutableTreeNode(rootFormula);    
        	createTree();
        	contextNode.setFormulaTree(root); 
        }
        else{
        	/* Apenas reconstruir a arvore */
        	buildTree(); 
        }
		
        
    	DefaultTreeModel model = new DefaultTreeModel(root);
    	
    	setModel(model);
    	
    	// set up node icons
    	setCellRenderer(new FormulaTreeCellRenderer());	
    	
    	updateTree(); 
    	
		createPopupMenuFormula(); 
		createPopupMenuOperator(); 
		createPopupMenuVariable(); 
		
		/*------------------ Adicionar listeners -----------------------*/
		
		addMouseListener(new MouseAdapter(){
			
			public void mousePressed(MouseEvent e) {
				
				int selRow = getRowForLocation(e.getX(), e.getY());
				if (selRow == -1) {
					return;
				}
				
				TreePath selPath = getPathForLocation(e.getX(), e.getY());
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath
				.getLastPathComponent();
				
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
			
		
	});		
		
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
	
	private class FormulaTreeCellRenderer extends DefaultTreeCellRenderer {
		
		/** Serialization runtime version number */
		private static final long serialVersionUID = 0;
		
		
		
        private ImageIcon folderSmallIcon = iconController.getOpenIcon(); 
		
    	protected ImageIcon andIcon = iconController.getAndIcon(); 
    	
    	protected ImageIcon orIcon = iconController.getOrIcon(); 
    	
    	protected ImageIcon notIcon = iconController.getNotIcon(); 
    	
    	protected ImageIcon equalIcon = iconController.getEqualIcon(); 
    	
    	protected ImageIcon impliesIcon = iconController.getImpliesIcon(); 
    	
    	protected ImageIcon forallIcon = iconController.getForallIcon(); 
    	
    	protected ImageIcon existsIcon = iconController.getExistsIcon(); 
    	
    	protected ImageIcon iffIcon = iconController.getIffIcon(); 
    	
    	protected ImageIcon entityNodeIcon = iconController.getEntityNodeIcon(); 
    	
    	protected ImageIcon ovariableNodeIcon = iconController.getOVariableNodeIcon(); 
    	
    	protected ImageIcon nodeNodeIcon = iconController.getNodeNodeIcon(); 
    	
    	protected ImageIcon skolenNodeIcon = iconController.getSkolenNodeIcon(); 
    		
    	protected ImageIcon emptyNodeIcon = iconController.getEmptyNodeIcon(); 	        
        
        protected ImageIcon yellowBallIcon = iconController.getYellowBallIcon(); 
    	
        protected ImageIcon orangeBallIcon = iconController.getOrangeNodeIcon(); 
        
        protected ImageIcon hierarchyBallIcon = iconController.getHierarchyIcon(); 
        
        protected ImageIcon boxVariablesIcon = iconController.getBoxVariablesIcon(); 
        
        
		/**
		 * Return a tree cell for the object value. 
		 */
		
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			
			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);
	
			if(value == null){
				return this; 
			}			
			
			NodeFormulaTree nodeFormula = (NodeFormulaTree)(((DefaultMutableTreeNode)value).getUserObject());

			enumType type = nodeFormula.getTypeNode(); 
			enumSubType subType = nodeFormula.getSubTypeNode();
			
			switch(type){
			
			case EMPTY: 
				setIcon(emptyNodeIcon);
				return this; 
				
				
			case FORMULA: 
				setIcon(hierarchyBallIcon);
				return this; 
				
			case SIMPLE_OPERATOR: 
				
				switch(subType){
				
				case AND:
				   setIcon(andIcon); 
				   return this; 
					
				case OR: 
					setIcon(orIcon); 
					return this; 
					
				case NOT: 
					setIcon(notIcon); 
					return this; 
					
				case EQUALTO:
					setIcon(equalIcon); 
					return this; 					
					
				case IMPLIES:
					setIcon(impliesIcon); 
					return this; 					
					
				case IFF:
					setIcon(iffIcon); 
					return this; 					
					
                default:
                	return this; 
				
				}
			case QUANTIFIER_OPERATOR: 
				switch(subType){
				case FORALL:
					setIcon(forallIcon); 
					return this; 					
					
				case EXISTS: 	
					setIcon(existsIcon); 
					return this; 				
					
                default:
                	return this; 
				
				}				
				
				
			case VARIABLE_SEQUENCE:
				setIcon(boxVariablesIcon);
				return this; 
				
				
			case OPERANDO:
				switch(subType){
				
				case OVARIABLE:
				   setIcon(ovariableNodeIcon); 
				   return this; 
					
				case NODE: 
					setIcon(nodeNodeIcon); 
					return this; 
					
				case ENTITY: 
					setIcon(entityNodeIcon); 
					return this; 
				
				case VARIABLE: 
					setIcon(orangeBallIcon); 
					return this; 	
					
				/*	
				case SKOLEN:
					setIcon(equalIcon); 
					return this; 						
				*/	
					
                default:
                	return this; 
				
				}
			
			case VARIABLE:
				setIcon(orangeBallIcon); 
				return this; 
				
			default: 	
				setIcon(folderSmallIcon);
			    return this; 
			
			}
			
		}
		
		
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
					replaceByVariable(nodeActive); 
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
		
		public void addSimpleOperatorInTree(BuiltInRV builtInRV, enumSubType subType){

			NodeFormulaTree nodeFormula = (NodeFormulaTree)nodeActive.getUserObject(); 
		    NodeFormulaTree operandoChild; 
		    DefaultMutableTreeNode nodeChild; 
		    
			nodeFormula.setName(builtInRV.getName()); 
		    nodeFormula.setNodeVariable(builtInRV);
		    nodeFormula.setTypeNode(enumType.SIMPLE_OPERATOR); 
		    nodeFormula.setSubTypeNode(subType); 
		    
		    for (int i = 1; i <= builtInRV.getNumOperandos(); i++){
		        operandoChild = new NodeFormulaTree("op_" + i, enumType.EMPTY, enumSubType.NOTHING, null); 
		        nodeChild = new DefaultMutableTreeNode(operandoChild); 
		        nodeActive.add(nodeChild); 
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
		
		    /* adicionar nodo para a insercao da formula */
		    nodeFormulaTree = new NodeFormulaTree("Formula", enumType.FORMULA, enumSubType.NOTHING, null); 
		    node = new DefaultMutableTreeNode(nodeFormulaTree); 
		    nodeActive.add(node); 
		
		    updateTree(); 
		    
		}
		
		/**
		 * Nodo of the tree of the formula. Have the information
		 * about what type of things can replace it.  
		 * 
		 * @author Laecio Lima dos Santos (laecio@gmail.com)
		 */
		
		private class NodeFormulaTree{
			
			String name; 
			enumType type;
			enumSubType subType; 
			Object nodeVariable; 
			
			/**
			 * create a new nodo formula tree
			 * @param typeNode type of the node (what go to fill its)
			 * @param nodeVariable object that fill the node 
			 */
			
			public NodeFormulaTree(String _name, enumType _type, enumSubType _subType, Object _nodeVariable){
				name = _name; 
				type = _type; 
				subType = _subType; 
				nodeVariable = _nodeVariable; 
			}
			
			public String toString(){
				return name; 
			}
			
			public String getName(){
				return name; 
			}
			
			public void setName(String name){
				this.name = name; 
			}
			
			public enumType getTypeNode(){
				return type; 
			}
			
			public void setTypeNode(enumType _type){
				this.type = _type; 
			}

			public enumSubType getSubTypeNode(){
				return subType; 
			}
			
			public void setSubTypeNode(enumSubType _subType){
				this.subType = _subType; 
			}
			
						
			
			
			public Object getNodeVariable(){
				return nodeVariable; 
			}
			
			public void setNodeVariable(Object nodeVariable){
				this.nodeVariable = nodeVariable; 
			}
		}
	
		/**
		 * Painel que mostra quais são as variaveis disponiveis para
		 * se escolher uma a ser utilizada como operando na formula. 
		 *
		 * NodeTree deve ser um nodo do tipo enumType.VARIABLE.
		 *  
		 */
		
		public JPanel replaceByVariable(DefaultMutableTreeNode nodeTree){
			
			//JFrame variableList; 
			//variableList = new JFrame("Variable"); 
		     
		     JPanel painel = new JPanel(new BorderLayout()); 
		     
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
			String name = "X"; 
			
			NodeFormulaTree variable = new NodeFormulaTree(name, enumType.VARIABLE, enumSubType.NOTHING, null);
			DefaultMutableTreeNode nodeVariable = new DefaultMutableTreeNode(variable); 
			nodeActive.add(nodeVariable); 
			
			
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
		
	}