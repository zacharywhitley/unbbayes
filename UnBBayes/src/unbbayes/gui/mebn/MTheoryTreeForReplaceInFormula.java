package unbbayes.gui.mebn;


import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import unbbayes.controller.FormulaTreeController;
import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
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
	
	protected IconController iconController = IconController.getInstance();
	
	private final MEBNController controller;	
	
	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
	
	private FormulaTreeController formulaTreeController; 
	
	/**
	 * 
	 * @param controller
	 */
	
	public MTheoryTreeForReplaceInFormula(final MEBNController controller, FormulaTreeController _formulaTreeController) {
		
		this.controller = controller; 
		this.net = controller.getMultiEntityBayesianNetwork();
		
		formulaTreeController = _formulaTreeController; 
		
		/*----------------- build tree --------------------------*/ 
		
		setCellRenderer(new MTheoryTreeCellRenderer());
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(net.getName());
		DefaultTreeModel model = new DefaultTreeModel(root);
		setModel(model);
		createTree();
		
		this.setRootVisible(false); 
		this.putClientProperty("JTree.lineStyle", "None");

		
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
							formulaTreeController.addNode((ResidentNode)nodeLeaf); 
							controller.updateFormulaActiveContextNode(); 
						} else if (e.getClickCount() == 1) {
							
														
						}
					}
					else{
						
						if(nodeLeaf instanceof Entity){
							if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
								
							} else if (e.getClickCount() == 2
									&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
								formulaTreeController.addEntity((Entity)nodeLeaf); 
								controller.updateFormulaActiveContextNode(); 
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
							formulaTreeController.addNode((ResidentNode)nodeLeaf); 
							controller.updateFormulaActiveContextNode(); 
							
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

