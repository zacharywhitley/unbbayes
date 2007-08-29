package unbbayes.gui.mebn;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.gui.GraphAction;
import unbbayes.gui.GraphPane;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.util.ArrayMap;

/**
 * Tree of the components of the MTheory. Show the MFrags and your 
 * nodes: resident nodes, input nodes and context nodes. 
 */

public class MTheoryTree extends JTree {

	private static final long serialVersionUID = 7557085958154752664L;

	private MultiEntityBayesianNetwork net;

	private boolean[] expandedNodes;

	private ArrayMap<Object, MFrag> mFragMap = new ArrayMap<Object, MFrag>();
	private ArrayMap<Object, ResidentNode> residentNodeMap = new ArrayMap<Object, ResidentNode>();
	private ArrayMap<Object, InputNode> inputNodeMap = new ArrayMap<Object, InputNode>();
	private ArrayMap<Object, ContextNode> contextNodeMap = new ArrayMap<Object, ContextNode>(); 
	
	/* 
	 * Contem a relação entre os nós da árvore e os elementos da MTheory que 
	 * estes representam. 
	 */
	private ArrayMap<DefaultMutableTreeNode, Object> nodeMap = new ArrayMap<DefaultMutableTreeNode, Object>(); 	
	
	private Object objectSelected; 
	
	private JPopupMenu popup = new JPopupMenu();
	
	private JPopupMenu popupMFrag = new JPopupMenu(); 
	private JPopupMenu popupNode = new JPopupMenu(); 
	//private JPopupMenu popupResidentNode = new JPopupMenu(); 
	//private JPopupMenu popupContextNode = new JPopupMenu(); 
	//private JPopupMenu popupInputNode = new JPopupMenu(); 
	

	protected IconController iconController = IconController.getInstance();

	private DefaultMutableTreeNode root; 
	
    private final MEBNController controller;	
    private final GraphPane graphPane;
    
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
  
  	
  	/**
  	 * 
  	 * @param controller
  	 */
  	
	public MTheoryTree(final MEBNController controller, GraphPane graphPane) {
		
		this.controller = controller; 
		this.net = controller.getMultiEntityBayesianNetwork();
		this.graphPane = graphPane; 
		
		/*----------------- build tree --------------------------*/ 
		
		setCellRenderer(new MTheoryTreeCellRenderer());
		root = new DefaultMutableTreeNode(net.getName());
	    DefaultTreeModel model = new DefaultTreeModel(root);
	    setModel(model);
	    createTree();
	    
	    createPopupMenu();
	    createPopupMenuMFrag(); 
	    //createPopupMenuResident(); 
	    //createPopupMenuInput(); 
	    //createPopupMenuContext(); 
	    createPopupMenuNode(); 
	    
	    addMouseListener(new MousePressedListener()); 
	    	    
		super.treeDidChange();
		
		expandTree();
	}
	
	private void createPopupMenuMFrag(){
		
		JMenuItem itemDelete =   new JMenuItem(resource.getString("menuDelete")); 
		JMenuItem itemContext =  new JMenuItem(resource.getString("menuAddContext"));
		JMenuItem itemInput =    new JMenuItem(resource.getString("menuAddInput")); 
		JMenuItem itemResident = new JMenuItem(resource.getString("menuAddResident"));

		itemDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				controller.removeDomainMFrag((DomainMFrag) objectSelected); 
				updateTree(); 
			}
		}); 		
		
		itemContext.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				graphPane.setAction(GraphAction.CREATE_CONTEXT_NODE); 
			}
		}); 
		
		itemInput.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				graphPane.setAction(GraphAction.CREATE_INPUT_NODE); 				
			}
		}); 
		
		itemResident.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				graphPane.setAction(GraphAction.CREATE_RESIDENT_NODE); 								
			}
		}); 		
		
		popupMFrag.add(itemDelete); 
		popupMFrag.add(itemContext); 
		popupMFrag.add(itemResident); 
		popupMFrag.add(itemInput); 
	}

	/*
	private void createPopupMenuResident(){
		
		JMenuItem itemDelete =   new JMenuItem(resource.getString("menuDelete")); 

		itemDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				controller.getMebnController().deleteSelected(objectSelected); 
				updateTree(); 
			}
		}); 		
		
		popupResidentNode.add(itemDelete); 
	}	
	
	private void createPopupMenuInput(){
		
		JMenuItem itemDelete =   new JMenuItem(resource.getString("menuDelete")); 

		itemDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				controller.getMebnController().deleteSelected(objectSelected); 
				updateTree(); 
			}
		}); 		
		
		popupInputNode.add(itemDelete); 
	}	
	
	private void createPopupMenuContext(){
		
		JMenuItem itemDelete =   new JMenuItem(resource.getString("menuDelete")); 

		itemDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				controller.getMebnController().deleteSelected(objectSelected); 
				updateTree(); 
			}
		}); 		
		
		popupContextNode.add(itemDelete); 
	}
	*/		

	private void createPopupMenuNode(){
		
		JMenuItem itemDelete =   new JMenuItem(resource.getString("menuDelete")); 

		itemDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				controller.deleteSelected(objectSelected); 
				updateTree(); 
			}
		}); 		
		
		popupNode.add(itemDelete); 
	}
	
	private void createPopupMenu() {
		
		JMenuItem itemAddDomainMFrag = new JMenuItem(resource.getString("menuAddDomainMFrag"));
		itemAddDomainMFrag.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {   
            	controller.insertDomainMFrag(); 
            }
        });
		
        popup.add(itemAddDomainMFrag);
	}

	
	private void createTree() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
		buildNodesOfTree(net, root); 
		expandedNodes = new boolean[net.getMFragCount()];
	}

	private class MTheoryTreeCellRenderer extends DefaultTreeCellRenderer {

		/** Serialization runtime version number */
		private static final long serialVersionUID = 0;

		private ImageIcon contextNodeIcon = iconController.getGreenNodeIcon();
		private ImageIcon residentNodeIcon = iconController.getYellowNodeIcon(); 
		private ImageIcon inputNodeIcon = iconController.getGrayNodeIcon(); 
		
		private ImageIcon orangeNodeIcon = iconController.getOrangeNodeIcon(); 
		private ImageIcon mTheoryNodeIcon = iconController.getMTheoryNodeIcon(); 
		
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			
			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);
			
			Object obj = nodeMap.get(value);
			
			if (leaf) {

				if (obj != null) {
					
					if (obj instanceof ResidentNode){ 
						setIcon(residentNodeIcon);
						}
										
					else{
						if (obj instanceof InputNode){
							setIcon(inputNodeIcon); 
						}
						else{ 
							if (obj instanceof ContextNode){
						       setIcon(contextNodeIcon);
							}
							else{ 
                                if (obj instanceof MFrag){ 
								   setIcon(orangeNodeIcon); 
                                }
                                else{
                                	setIcon(mTheoryNodeIcon); 
                                }
							}
						}
					}
				}
				
			} else {
                if (obj instanceof MFrag){ 
    				setOpenIcon(orangeNodeIcon);
    				setClosedIcon(orangeNodeIcon);
    				setIcon(orangeNodeIcon);
                 }
                 else{
     				setOpenIcon(mTheoryNodeIcon);
    				setClosedIcon(mTheoryNodeIcon);                	 
                 	setIcon(mTheoryNodeIcon); 
                 }	
			}
			return this;
		}
	}

	/**
	 * Retrai todos os nós da árvore desejada.
	 * 
	 * @param arvore
	 *            uma <code>JTree</code> que representa a rede Bayesiana em
	 *            forma de árvore.
	 * @since
	 * @see JTree
	 */
	public void collapseTree() {
		for (int i = 0; i < getRowCount(); i++) {
			collapseRow(i);
		}

		for (int i = 0; i < expandedNodes.length; i++) {
			expandedNodes[i] = false;
		}
	}

	/**
	 * Expande todos os nós da árvore desejada.
	 * 
	 * @param arvore
	 *            uma <code>JTree</code> que representa a rede Bayesiana em
	 *            forma de árvore.
	 * @since
	 * @see JTree
	 */
	public void expandTree() {
		for (int i = 0; i < getRowCount(); i++) {
			expandRow(i);
		}

		for (int i = 0; i < expandedNodes.length; i++) {
			expandedNodes[i] = true;
		}
	}

	private void restoreTree() {
		for (int i = expandedNodes.length - 1; i >= 0; i--) {
			if (expandedNodes[i]) {
				expandRow(i);
			} else {
				collapseRow(i);
			}
		}
	}

	/**
	 * 
	 */
	public void updateTree() {
		if (expandedNodes == null) {
			expandedNodes = new boolean[net.getMFragCount()];
			for (int i = 0; i < expandedNodes.length; i++) {
				expandedNodes[i] = false;
			}
		}

		DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();
		root.removeAllChildren();
		mFragMap.clear();
		residentNodeMap.clear(); 
		inputNodeMap.clear(); 
		contextNodeMap.clear(); 
		nodeMap.clear(); 
		
		buildNodesOfTree(net, root); 
		
		restoreTree();
		((DefaultTreeModel) getModel()).reload(root);
		restoreTree();
	}

	private void buildNodesOfTree(MultiEntityBayesianNetwork mTheory, DefaultMutableTreeNode root){

		List<MFrag> mFragList = net.getMFragList();
		
		for (MFrag mFrag : mFragList) {
			DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(mFrag.getName());
			root.add(treeNode);
			mFragMap.put(treeNode, mFrag);
			nodeMap.put(treeNode, mFrag); 
			
			//TODO verificar se não pe melhor fazer um painel separado para as findings...
			if(mFrag instanceof DomainMFrag){
				
				List<DomainResidentNode> residentNodeList = ((DomainMFrag)mFrag).getDomainResidentNodeList(); 
			    for(ResidentNode residentNode: residentNodeList){
			    	DefaultMutableTreeNode treeNodeChild = new DefaultMutableTreeNode(residentNode.getName());
			    	treeNode.add(treeNodeChild); 
			    	residentNodeMap.put(treeNodeChild, residentNode); 
					nodeMap.put(treeNodeChild, residentNode);     	
			    }
				
			    List<GenerativeInputNode> inputNodeList = ((DomainMFrag)mFrag).getGenerativeInputNodeList(); 
			    for(GenerativeInputNode inputNode: inputNodeList){
			    	DefaultMutableTreeNode treeNodeChild; 
			    	Object inputInstanceOf = inputNode.getInputInstanceOf(); 
			    	
			    	if(inputInstanceOf != null){
			    		if (inputInstanceOf instanceof Node){
			    	       treeNodeChild = new DefaultMutableTreeNode(((Node)inputInstanceOf).getName());
			    		}
			    		else{
			    			//TODO InputInstanceOf a built in !!!!
			    			treeNodeChild = new DefaultMutableTreeNode(" ");
			    		}
			    	}
			    	else{
			    		treeNodeChild = new DefaultMutableTreeNode(" ");
			    	}
			    	
			    	treeNode.add(treeNodeChild); 
			    	inputNodeMap.put(treeNodeChild, inputNode); 
					nodeMap.put(treeNodeChild, inputNode);     	
			    }
			    
				List<ContextNode> contextNodeList = ((DomainMFrag)mFrag).getContextNodeList(); 
			    for(ContextNode contextNode: contextNodeList){
			    	DefaultMutableTreeNode treeNodeChild = new DefaultMutableTreeNode(contextNode);
			    	treeNode.add(treeNodeChild); 
			    	contextNodeMap.put(treeNodeChild, contextNode); 
					nodeMap.put(treeNodeChild, contextNode);     	
			    }			    
			}
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
	
	public void setMTheoryName(String name){
		root.setUserObject(name); 
	}
	
	/**
	 * Listener for the events of mouse in the tree
	 * @author Laecio Lima dos Santos
	 *
	 */
	private class MousePressedListener extends MouseAdapter{

		public void mousePressed(MouseEvent e) {
			
			int selRow = getRowForLocation(e.getX(), e.getY());
			if (selRow == -1) {
				return;
			}

			TreePath selPath = getPathForLocation(e.getX(), e.getY());
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath
					.getLastPathComponent();

			if (node.isLeaf()) {
				
				Object nodeLeaf = nodeMap.get(node); 
				objectSelected = nodeLeaf; 
				
				if (nodeLeaf instanceof MFrag){
					if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
						popupMFrag.setEnabled(true);
						popupMFrag.show(e.getComponent(),e.getX(),e.getY());
					} else if (e.getClickCount() == 2
							&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
						controller.setCurrentMFrag(mFragMap.get(node)); 
					} else if (e.getClickCount() == 1) {
						
					}
				}
				else{
					if (nodeLeaf instanceof MultiEntityNode){
						if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
							popupNode.setEnabled(true);
							popupNode.show(e.getComponent(),e.getX(),e.getY());
						} else if (e.getClickCount() == 2
								&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
							
							/* 
							 * ativar a MFrag do nodo como a ativa e selecionar o nodo
							 * no grafo desta. 
							 */ 
							Object fatherNode = objectSelected;
							TreeNode treeNode = node; 
							
							while (!(fatherNode instanceof MFrag)){
								treeNode = treeNode.getParent(); 
								fatherNode = nodeMap.get(treeNode); 
							}
							
							controller.showGraphMFrag((MFrag)fatherNode); 
							controller.selectNode((Node)objectSelected); 
							
						} else if (e.getClickCount() == 1) {
							
						}
					}					
				}
				
				
			} 
			else { //Not is a leaf 
				
				Object nodeLeaf = nodeMap.get(node); 
				objectSelected = nodeLeaf; 
				
				if (nodeLeaf instanceof MFrag){
					if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
						popupMFrag.setEnabled(true);
						popupMFrag.show(e.getComponent(),e.getX(),e.getY());
					} else if (e.getClickCount() == 2
							&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
						controller.setCurrentMFrag(mFragMap.get(node));
					} else if (e.getClickCount() == 1) {
					
					
					
					}
				}
				else{
					if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
						
						// PARECE NÃO ENTRAR AQUI... VERIFICAR...
						//if (e.isPopupTrigger()) {
						if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
							popup.setEnabled(true);
							popup.show(e.getComponent(),e.getX(),e.getY());
						}
						
					}
					if (e.getClickCount() == 1) {
						/*Node newNode = getNodeMap(node);
						 if (newNode != null) {
						 netWindow.getGraphPane().selectObject(newNode);
						 netWindow.getGraphPane().update();
						 }*/
						//TODO NÃO TEM ISSO NA MFRAG
					} else if (e.getClickCount() == 2) {

					}
				}
			}
		}
	}


}