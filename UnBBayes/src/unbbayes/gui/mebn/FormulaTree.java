package unbbayes.gui.mebn;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import unbbayes.controller.FormulaTreeController;
import unbbayes.controller.IconController;
import unbbayes.controller.NetworkController;
import unbbayes.gui.GraphAction;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
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
	
	private enum enumNode{
		EMPTY, 
		OVARIABLE, 
		NODE, 
		ENTITY, 
		SKOLEN, 
		
		OPERATOR, 
		FORMULA, 
		VARIABLE_SEQUENCE,
		VARIABLE
	}
	
	/* types of operators */
	
	private enum enumOperator{
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
	
    private MultiEntityBayesianNetwork net;
    
	private MFrag mfragActive; 
	
	protected IconController iconController = IconController.getInstance();
	
	private final NetworkController controller;	
	
    private JPopupMenu popupOperando = new JPopupMenu(); 
	
    private JPopupMenu popupOperator = new JPopupMenu(); 
    
    ContextNode contextNode; 
    
    DefaultMutableTreeNode root;     
    
    /**
     * 
     * 
     * @param controller
     */
    
	public FormulaTree(final NetworkController controller, ContextNode _contextNode) {
		
		this.controller = controller; 
		this.net = (MultiEntityBayesianNetwork)controller.getNetwork();
        this.mfragActive = net.getCurrentMFrag(); 
        
        formulaTreeController = new FormulaTreeController(this); 
        
        contextNode = _contextNode; 
        
        if(contextNode.getFormulaTree() == null){
        	NodeFormulaTree rootFormula = new NodeFormulaTree("formula", enumNode.FORMULA, null);  
        	root = new DefaultMutableTreeNode(rootFormula);    
        	createTree();
        	contextNode.setFormulaTree(root); 
        }
        else{
        	buildTree(); 
        }
		
        
    	DefaultTreeModel model = new DefaultTreeModel(root);
    	
    	setModel(model);
    	
    	// set up node icons
    	setCellRenderer(new FormulaTreeCellRenderer());	
    	
    	updateTree(); 
    	
		createPopupMenuFormula(); 
		createPopupMenuOperator(); 
		
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
					if ((nodeFormulaActive.getTypeNode() == enumNode.FORMULA) || 
							(nodeFormulaActive.getTypeNode() == enumNode.EMPTY)) {
						popupOperando.setEnabled(true);
						popupOperando.show(e.getComponent(),e.getX(),e.getY());
					}else{
						if (nodeFormulaActive.getTypeNode() == enumNode.OPERATOR) {
							popupOperator.setEnabled(true);
							popupOperator.show(e.getComponent(),e.getX(),e.getY());
						}
					}
					
				} else if (e.getClickCount() == 2
						&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {						
					
					
				} else if (e.getClickCount() == 1) {
					
				}
				
			}
			
		
	});		
		
		super.treeDidChange();
		//expandTree();
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
		
        private ImageIcon folderSmallIcon = iconController.getFolderSmallIcon(); 
		
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

			if (nodeFormula.getTypeNode() == enumNode.EMPTY){
			    setIcon(emptyNodeIcon);
			    return this; 
			}
			
			if (nodeFormula.getTypeNode() == enumNode.FORMULA){
			    setIcon(folderSmallIcon);
			    return this; 
			}	
			
			if (nodeFormula.getTypeNode() == enumNode.OPERATOR){

				setIcon(folderSmallIcon);
			    return this; 
			}	
			
			if (nodeFormula.getTypeNode() == enumNode.VARIABLE_SEQUENCE){
			    setIcon(folderSmallIcon);
			    return this; 
			}					
			
			return this; 
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
			itemSkolen.setArmed(false); 
			menuOperando.add(itemSkolen); 
			
			itemAnd.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae){
				    BuiltInRV builtInRV = new BuiltInRVAnd(); 
				    addOperatorInTree(builtInRV); 
				}
			});		
			
			itemOr.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae){
				    BuiltInRV builtInRV = new BuiltInRVOr(); 
				    addOperatorInTree(builtInRV); 
				}
			});				
			
			itemNot.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae){
				    BuiltInRV builtInRV = new BuiltInRVNot(); 
				    addOperatorInTree(builtInRV); 
				}
			});	
			
			itemEqual.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae){
				    BuiltInRV builtInRV = new BuiltInRVEqualTo(); 
				    addOperatorInTree(builtInRV); 
				}
			});						

			itemIff.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae){
				    BuiltInRV builtInRV = new BuiltInRVIff(); 
				    addOperatorInTree(builtInRV); 
				}
			});						
			
			itemImplies.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae){
				    BuiltInRV builtInRV = new BuiltInRVImplies(); 
				    addOperatorInTree(builtInRV); 
				}
			});						
			
			itemForall.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae){
				    BuiltInRV builtInRV = new BuiltInRVForAll(); 
				    addQuantifierInTree(builtInRV); 
				}
			});						
			
			itemExists.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae){
				    BuiltInRV builtInRV = new BuiltInRVExists(); 
				    addQuantifierInTree(builtInRV); 
				}
			});						
			
			itemNode.addActionListener(new ActionListener(){
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
			
			itemEntity.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae){
					controller.getScreen().getGraphPane().setAction(GraphAction.CREATE_CONTEXT_NODE); 
				}
			}); 
			
			itemOVariable.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae){
					
				     JFrame teste = new JFrame("OrdinaryVariable"); 
				     
				     JPanel painel = new JPanel(new BorderLayout()); 

				     OVariableTreeMFrag oVariableTreeMFrag = new OVariableTreeMFragArgument(controller); 
				     JScrollPane jspOVariableTreeMFrag = new JScrollPane(oVariableTreeMFrag); 
				     painel.add(jspOVariableTreeMFrag, BorderLayout.NORTH); 

				     teste.setContentPane(painel);
				     teste.pack();
				     teste.setVisible(true); 
				     teste.setLocationRelativeTo(null); 
				     teste.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
				
				}
			}); 
			
			itemDelete.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent ae){
				
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode)nodeActive.getParent(); 
					
					/* primeira formula */
					
					if(parent == null){
						NodeFormulaTree rootFormula = new NodeFormulaTree("formula", enumNode.FORMULA, null); 					
						nodeActive.setUserObject(rootFormula); 
						return; 
					}
					
					NodeFormulaTree nodeFormulaParent = (NodeFormulaTree)parent.getUserObject(); 
				    
					/* operando */
					if(nodeFormulaParent.getTypeNode() == enumNode.OPERATOR){
						
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
						NodeFormulaTree rootFormula = new NodeFormulaTree("formula", enumNode.FORMULA, null); 					
						nodeActive.setUserObject(rootFormula); 
						nodeActive.removeAllChildren(); 
						updateTree(); 
						return; 
					}
					
					NodeFormulaTree nodeFormulaParent = (NodeFormulaTree)parent.getUserObject(); 
				    
					if(nodeFormulaParent.getTypeNode() == enumNode.OPERATOR){
						
						int indiceNewChild = parent.getChildCount();
						
						NodeFormulaTree operandoChild = new NodeFormulaTree("op_" + (indiceNewChild + 1) , enumNode.EMPTY, null); 
					    nodeActive.setUserObject(operandoChild); 
					    nodeActive.removeAllChildren(); 
					    updateTree(); 
					}
					
				}
			}); 
			
			popupOperator.add(itemDelete); 
		}		
		
		public void addOperatorInTree(BuiltInRV builtInRV){

			NodeFormulaTree nodeFormula = (NodeFormulaTree)nodeActive.getUserObject(); 
		    NodeFormulaTree operandoChild; 
		    DefaultMutableTreeNode nodeChild; 
		    
			nodeFormula.setName(builtInRV.getName()); 
		    nodeFormula.setNodeVariable(builtInRV);
		    nodeFormula.setTypeNode(enumNode.OPERATOR); 
		    
		    for (int i = 1; i <= builtInRV.getNumOperandos(); i++){
		        operandoChild = new NodeFormulaTree("op_" + i, enumNode.EMPTY, null); 
		        nodeChild = new DefaultMutableTreeNode(operandoChild); 
		        nodeActive.add(nodeChild); 
		    }
		       
		    updateTree(); 
		    
		}
		
		public void addQuantifierInTree(BuiltInRV builtInRV){
			
			NodeFormulaTree nodeFormula = (NodeFormulaTree)nodeActive.getUserObject(); 
		    
			NodeFormulaTree nodeFormulaTree; 
		    DefaultMutableTreeNode node; 
		    
			nodeFormula.setName(builtInRV.getName()); 
		    nodeFormula.setNodeVariable(builtInRV);
		    nodeFormula.setTypeNode(enumNode.OPERATOR); 
		    
		    /* adicionar nodo para insercao da sequencia de variaveis */
		    nodeFormulaTree = new NodeFormulaTree("Var", enumNode.VARIABLE_SEQUENCE, null); 
		    node = new DefaultMutableTreeNode(nodeFormulaTree); 
		    nodeActive.add(node); 
		
		    /* adicionar nodo para a insercao da formula */
		    nodeFormulaTree = new NodeFormulaTree("Formula", enumNode.FORMULA, null); 
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
			enumNode typeNode;
			Object nodeVariable; 
			
			/**
			 * create a new nodo formula tree
			 * @param typeNode type of the node (what go to fill its)
			 * @param nodeVariable object that fill the node 
			 */
			
			public NodeFormulaTree(String name, enumNode typeNode, Object nodeVariable){
				this.name = name; 
				this.typeNode = typeNode; 
				this.nodeVariable = nodeVariable; 
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
			
			public enumNode getTypeNode(){
				return typeNode; 
			}
			
			public void setTypeNode(enumNode typeNode){
				this.typeNode = typeNode; 
			}
			
			public Object getNodeVariable(){
				return nodeVariable; 
			}
			
			public void setNodeVariable(Object nodeVariable){
				this.nodeVariable = nodeVariable; 
			}
		}
		
	}


