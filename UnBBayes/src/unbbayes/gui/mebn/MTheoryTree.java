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
import javax.swing.tree.TreePath;

import unbbayes.controller.IconController;
import unbbayes.controller.NetworkController;
import unbbayes.gui.GraphAction;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.util.ArrayMap;

/**
 * 
 */

public class MTheoryTree extends JTree {

	private static final long serialVersionUID = 7557085958154752664L;

	private MultiEntityBayesianNetwork net;

	private boolean[] expandedNodes;

	private ArrayMap<Object, MFrag> mFragMap = new ArrayMap<Object, MFrag>();
	private ArrayMap<Object, ResidentNode> residentNodeMap = new ArrayMap<Object, ResidentNode>();
	private ArrayMap<Object, InputNode> inputNodeMap = new ArrayMap<Object, InputNode>();
	private ArrayMap<Object, ContextNode> contextNodeMap = new ArrayMap<Object, ContextNode>(); 
	private ArrayMap<Object, Object> nodeMap = new ArrayMap<Object, Object>(); 	
	
	private JPopupMenu popup = new JPopupMenu();
	
	private JPopupMenu popupMFrag = new JPopupMenu(); 

	protected IconController iconController = IconController.getInstance();

    private final NetworkController controller;	
    
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
  
	
	/*public MTheoryTree(final NetworkWindow netWindow) {
		net = netWindow.getMultiEntityBayesianNetwork();*/
	public MTheoryTree(final NetworkController controller) {
		
		this.controller = controller; 
		this.net = (MultiEntityBayesianNetwork)controller.getNetwork();

		/*----------------- build tree --------------------------*/ 
		
		setCellRenderer(new MTheoryTreeCellRenderer());
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(net.getName());
	    DefaultTreeModel model = new DefaultTreeModel(root);
	    setModel(model);
	    createTree();
	    createPopupMenu();
	    createPopupMenuMFrag(); 	    

		/*---------------- listeners ---------------------------*/ 
		
	    addMouseListener(new MouseAdapter() {
			
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
					
					if (nodeLeaf instanceof MFrag){
						if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
							popupMFrag.setEnabled(true);
							popupMFrag.show(e.getComponent(),e.getX(),e.getY());
						} else if (e.getClickCount() == 2
								&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
							controller.getMebnController().setCurrentMFrag(mFragMap.get(node)); 
						} else if (e.getClickCount() == 1) {
							//TODO acao para clique simples na MFrag. 
						}
					}
					else{
						
					}
					
					
				} 
				else { //Not is a leaf 
					
					Object nodeLeaf = nodeMap.get(node); 
					if (nodeLeaf instanceof MFrag){
						if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
							popupMFrag.setEnabled(true);
							popupMFrag.show(e.getComponent(),e.getX(),e.getY());
						} else if (e.getClickCount() == 2
								&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
							controller.getMebnController().setCurrentMFrag(mFragMap.get(node)); 
						} else if (e.getClickCount() == 1) {
							//TODO acao para clique simples na MFrag. 
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
							DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel()
							.getRoot();
							int index = root.getIndex(node);
							expandedNodes[index] = !expandedNodes[index];
						}
					}
				}
			}
		});
	    
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
				//TODO Action
			}
		}); 		
		
		itemContext.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				controller.getScreen().getGraphPane().setAction(GraphAction.CREATE_CONTEXT_NODE); 
			}
		}); 
		
		itemInput.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				controller.getScreen().getGraphPane().setAction(GraphAction.CREATE_INPUT_NODE); 				
			}
		}); 
		
		itemResident.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				controller.getScreen().getGraphPane().setAction(GraphAction.CREATE_RESIDENT_NODE); 								
			}
		}); 		
		
		popupMFrag.add(itemDelete); 
		popupMFrag.add(itemContext); 
		popupMFrag.add(itemResident); 
		popupMFrag.add(itemInput); 
	}
	
	private void createPopupMenu() {
		
		JMenuItem itemAddDomainMFrag = new JMenuItem(resource.getString("menuAddDomainMFrag"));
		itemAddDomainMFrag.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {   
            	controller.getMebnController().insertDomainMFrag("DomainMFrag " + net.getMFragCount()); 
            }
        });
		
		JMenuItem itemAddFindingMFrag = new JMenuItem(resource.getString("menuAddFindingMFrag"));
		itemAddFindingMFrag.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {   
            	net.addDomainMFrag(new DomainMFrag("FindingMFrag " + net.getMFragCount(), net));
            	updateTree();
            }
        });
		
        popup.add(itemAddDomainMFrag);
        popup.add(itemAddFindingMFrag);
	}

	private void createTree() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
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
			    	DefaultMutableTreeNode treeNodeChild = new DefaultMutableTreeNode(inputNode.getName());
			    	treeNode.add(treeNodeChild); 
			    	inputNodeMap.put(treeNodeChild, inputNode); 
					nodeMap.put(treeNodeChild, inputNode);     	
			    }
			    
				List<ContextNode> contextNodeList = ((DomainMFrag)mFrag).getContextNodeList(); 
			    for(ContextNode contextNode: contextNodeList){
			    	DefaultMutableTreeNode treeNodeChild = new DefaultMutableTreeNode(contextNode.getName());
			    	treeNode.add(treeNodeChild); 
			    	contextNodeMap.put(treeNodeChild, contextNode); 
					nodeMap.put(treeNodeChild, contextNode);     	
			    }			    
			}
		}
		expandedNodes = new boolean[net.getMFragCount()];
	}

	private class MTheoryTreeCellRenderer extends DefaultTreeCellRenderer {

		/** Serialization runtime version number */
		private static final long serialVersionUID = 0;

		private ImageIcon folderSmallIcon = iconController.getFolderSmallIcon();

		/*private ImageIcon folderSmallDisabledIcon = iconController
				.getFolderSmallDisabledIcon();*/

		private ImageIcon yellowBallIcon = iconController.getYellowBallIcon();

		private ImageIcon greenBallIcon = iconController.getGreenBallIcon();

		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);
			
			Object obj = nodeMap.get((DefaultMutableTreeNode) value);
			
			
			if (leaf) {
				/*DefaultMutableTreeNode parent = (DefaultMutableTreeNode) (((DefaultMutableTreeNode) value)
						.getParent());
				Object obj = mFragMap.get((DefaultMutableTreeNode) parent);*/

				if (obj != null) {
					
					if (obj instanceof ResidentNode){ 
						setIcon(yellowBallIcon);
						}
										
					else{
						if (obj instanceof InputNode){
							setIcon(greenBallIcon); 
						}
						else{ 
							if (obj instanceof ContextNode){
						       setIcon(greenBallIcon);
							}
							else{ //mFrag
								setIcon(folderSmallIcon); 
							}
						}
					}
				}
				
			} else {
				setOpenIcon(folderSmallIcon);
				setClosedIcon(folderSmallIcon);
				setIcon(folderSmallIcon);
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
			    	DefaultMutableTreeNode treeNodeChild = new DefaultMutableTreeNode(inputNode.getName());
			    	treeNode.add(treeNodeChild); 
			    	inputNodeMap.put(treeNodeChild, inputNode); 
					nodeMap.put(treeNodeChild, inputNode);     	
			    }
			    
				List<ContextNode> contextNodeList = ((DomainMFrag)mFrag).getContextNodeList(); 
			    for(ContextNode contextNode: contextNodeList){
			    	DefaultMutableTreeNode treeNodeChild = new DefaultMutableTreeNode(contextNode.getName());
			    	treeNode.add(treeNodeChild); 
			    	contextNodeMap.put(treeNodeChild, contextNode); 
					nodeMap.put(treeNodeChild, contextNode);     	
			    }			    
			}
		}
		
		/*
		for (MFrag mFrag : mFragList) {
			DefaultMutableTreeNode treeNode = findUserObject(mFrag.getName(), root);
			if (treeNode == null) {
				treeNode = new DefaultMutableTreeNode(mFrag.getName());
				root.add(treeNode);
			}
			mFragMap.put(treeNode, mFrag);
			//TODO USAR ESSA IDÉIA PARA ADICIONAR OS NÓS DE CADA MFRAG
			/*
			int statesSize = node.getStatesSize();
			for (int j = 0; j < statesSize; j++) {
				String label;
				if (treeVariable.getType() == Node.PROBABILISTIC_NODE_TYPE) {
					label = node.getStateAt(j) + ": "
							+ nf.format(treeVariable.getMarginalAt(j) * 100.0);
				} else {
					label = node.getStateAt(j) + ": "
							+ nf.format(treeVariable.getMarginalAt(j));
				}
				treeNode.add(new DefaultMutableTreeNode(label));
			}
			*/
			
			
		/*}*/
		
		
		
		restoreTree();
		((DefaultTreeModel) getModel()).reload(root);
		restoreTree();
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

	/**
	 * Adiciona uma evidencia no estado especificado.
	 * 
	 * @param caminho
	 *            caminho do estado a ser setado para 100%;
	 * @see TreePath
	 */
	private void treeDoubleClick(DefaultMutableTreeNode treeNode) {
		/*DefaultMutableTreeNode parent = (DefaultMutableTreeNode) ((treeNode)
				.getParent());
		Object obj = nodeMap.get((DefaultMutableTreeNode) parent);
		if (obj != null) {
			TreeVariable node = (TreeVariable) obj;

			// Só propaga nós de descrição
			if (node.getInformationType() == Node.DESCRIPTION_TYPE) {
				for (int i = 0; i < parent.getChildCount(); i++) {
					DefaultMutableTreeNode auxNode = (DefaultMutableTreeNode) parent
							.getChildAt(i);
					auxNode.setUserObject(node.getStateAt(i) + ": 0");
				}

				if (node.getType() == Node.PROBABILISTIC_NODE_TYPE) {
					treeNode.setUserObject(node.getStateAt(parent
							.getIndex(treeNode))
							+ ": 100");
				} else {
					treeNode.setUserObject(node.getStateAt(parent
							.getIndex(treeNode))
							+ ": **");
				}
				node.addFinding(parent.getIndex(treeNode));
				((DefaultTreeModel) getModel()).reload(parent);
			}
		}*/
	}
}