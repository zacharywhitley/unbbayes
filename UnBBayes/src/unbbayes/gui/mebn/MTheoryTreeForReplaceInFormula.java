package unbbayes.gui.mebn;


import java.awt.Color;
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
import unbbayes.gui.mebn.formula.FormulaTree;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.util.ArrayMap;

/**
 * Show the nodes residents of the MTheory for the user select 
 * and use in the formula of the context node
 */
public class MTheoryTreeForReplaceInFormula extends JTree {
	
	private static final long serialVersionUID = 7557085958154752664L;
	
	private MultiEntityBayesianNetwork net;
	
	private boolean[] expandedNodes;
	
	private ArrayMap<Object, MFrag> mFragMap = new ArrayMap<Object, MFrag>();
	private ArrayMap<Object, ResidentNode> residentNodeMap = new ArrayMap<Object, ResidentNode>();
	private ArrayMap<Object, Object> nodeMap = new ArrayMap<Object, Object>(); 	
	
	private Object objectSelected; 
	
	private JPopupMenu popup = new JPopupMenu();
	
	private JPopupMenu popupMFrag = new JPopupMenu(); 
	
	protected IconController iconController = IconController.getInstance();
	
	private final NetworkController controller;	
	
	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
	private FormulaTree formulaTree; 
	
	/**
	 * 
	 * @param controller
	 */
	
	public MTheoryTreeForReplaceInFormula(final NetworkController controller, FormulaTree _formulaTree) {
		
		this.controller = controller; 
		this.net = (MultiEntityBayesianNetwork)controller.getNetwork();
		
		formulaTree = _formulaTree; 
		
		/*----------------- build tree --------------------------*/ 
		
		setCellRenderer(new MTheoryTreeCellRenderer());
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(net.getName());
		DefaultTreeModel model = new DefaultTreeModel(root);
		setModel(model);
		createTree();
		
		this.setRootVisible(false); 
		this.putClientProperty("JTree.lineStyle", "None");
		
		createPopupMenu();
		createPopupMenuMFrag(); 	    
		
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
					objectSelected = nodeLeaf; 
					
					if (nodeLeaf instanceof Node){
						if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
							
							
						} else if (e.getClickCount() == 2
								&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
							formulaTree.addNode((Node)nodeLeaf); 
							controller.getMebnController().updateFormulaActiveContextNode(); 
						} else if (e.getClickCount() == 1) {
							
														
						}
					}
					else{
						
						if(nodeLeaf instanceof Entity){
							if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
								
							} else if (e.getClickCount() == 2
									&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
								formulaTree.addEntity((Entity)nodeLeaf); 
							} else if (e.getClickCount() == 1) {
								
															
							}	
						}
						
					}
					
					
				} 
				else { //Not is a leaf 
					Object nodeLeaf = nodeMap.get(node); 
					objectSelected = nodeLeaf; 
					
					if (nodeLeaf instanceof Node){
						if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
						} else if (e.getClickCount() == 2
								&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
							formulaTree.addNode((Node)nodeLeaf); 
							controller.getMebnController().updateFormulaActiveContextNode(); 
							
						} else if (e.getClickCount() == 1) {
							
						}
					}
					else{
						
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
				controller.getMebnController().removeDomainMFrag((DomainMFrag) objectSelected); 
				updateTree(); 
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
		
		popup.add(itemAddDomainMFrag);
	}
	
	private void createTree() {
		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
		List<MFrag> mFragList = net.getMFragList();
		for (MFrag mFrag : mFragList) {
			DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(mFrag.getName());
			root.add(treeNode);
			mFragMap.put(treeNode, mFrag);
			nodeMap.put(treeNode, mFrag); 
			
			if(mFrag instanceof DomainMFrag){
				
				List<DomainResidentNode> residentNodeList = ((DomainMFrag)mFrag).getDomainResidentNodeList(); 
				for(ResidentNode residentNode: residentNodeList){
					DefaultMutableTreeNode treeNodeChild = new DefaultMutableTreeNode(residentNode.getName());
					treeNode.add(treeNodeChild); 
					residentNodeMap.put(treeNodeChild, residentNode); 
					nodeMap.put(treeNodeChild, residentNode);
					for(Entity state: residentNode.getPossibleValueList()){
						DefaultMutableTreeNode treeNodeState = new DefaultMutableTreeNode(state.getName()); 
						treeNodeChild.add(treeNodeState); 
						nodeMap.put(treeNodeState, state); 
					}
				}  
			}
		}
		
		expandedNodes = new boolean[net.getMFragCount()];
		expandTree(); 
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
		nodeMap.clear(); 
		
		List<MFrag> mFragList = net.getMFragList();
		
		for (MFrag mFrag : mFragList) {
			DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(mFrag.getName());
			root.add(treeNode);
			mFragMap.put(treeNode, mFrag);
			nodeMap.put(treeNode, mFrag); 
			
			if(mFrag instanceof DomainMFrag){
				
				List<DomainResidentNode> residentNodeList = ((DomainMFrag)mFrag).getDomainResidentNodeList(); 
				for(ResidentNode residentNode: residentNodeList){
					DefaultMutableTreeNode treeNodeChild = new DefaultMutableTreeNode(residentNode.getName());
					treeNode.add(treeNodeChild); 
					residentNodeMap.put(treeNodeChild, residentNode); 
					nodeMap.put(treeNodeChild, residentNode);    
					
					for(Entity state: residentNode.getPossibleValueList()){
						DefaultMutableTreeNode treeNodeState = new DefaultMutableTreeNode(state.getName()); 
						treeNodeChild.add(treeNodeState); 
						nodeMap.put(treeNodeState, state); 
					}
				}
				
			}
		}
		
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
	
	

	private class MTheoryTreeCellRenderer extends DefaultTreeCellRenderer {
		
		/** Serialization runtime version number */
		private static final long serialVersionUID = 0;
		
		private ImageIcon residentNodeIcon = iconController.getYellowNodeIcon(); 
		private ImageIcon orangeNodeIcon = iconController.getOrangeNodeIcon(); 
		private ImageIcon mTheoryNodeIcon = iconController.getMTheoryNodeIcon(); 
		private ImageIcon stateIcon = iconController.getStateIcon(); 
		
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			
			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);
			
			Object obj = nodeMap.get((DefaultMutableTreeNode) value);
			
			if (leaf) {
				
				if (obj != null) {
					
					if (obj instanceof ResidentNode){ 
						setIcon(residentNodeIcon);
					}
					else if (obj instanceof MFrag){ 
						setIcon(orangeNodeIcon); 
						this.setForeground(Color.BLUE); 
					}else if (obj instanceof Entity){ 
						setIcon(stateIcon); 
					}
					else{
						setIcon(mTheoryNodeIcon); 
					}
				}
				
			} else {
				if (obj instanceof MFrag){ 
					setOpenIcon(orangeNodeIcon);
					setClosedIcon(orangeNodeIcon);
					setIcon(orangeNodeIcon);
					this.setForeground(Color.BLUE); 
				}
				else if (obj instanceof ResidentNode){ 
					setIcon(residentNodeIcon);
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
	
}

