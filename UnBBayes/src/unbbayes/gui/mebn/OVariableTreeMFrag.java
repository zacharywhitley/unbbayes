package unbbayes.gui.mebn;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import unbbayes.controller.IconController;
import unbbayes.controller.NetworkController;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.util.ArrayMap;


/**
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 0.1 (11/14/2006)
 */

public abstract class OVariableTreeMFrag extends JTree{
	
	private MultiEntityBayesianNetwork net;
	
	protected ArrayMap<Object, OrdinaryVariable> ordinaryVariableMap = new ArrayMap<Object, OrdinaryVariable>();
	private List<OrdinaryVariable> ordinaryVariableList = new ArrayList<OrdinaryVariable>();
	private MFrag mfragActive; 
	
	protected IconController iconController = IconController.getInstance();
	
	protected final NetworkController controller;	
	
	public OVariableTreeMFrag(final NetworkController controller) {
		
		this.controller = controller; 
		this.net = (MultiEntityBayesianNetwork)controller.getNetwork();
		this.mfragActive = net.getCurrentMFrag(); 
		
		// set up node icons
		setCellRenderer(new OrdinaryVariableTreeCellRenderer());
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(mfragActive.getName());
		DefaultTreeModel model = new DefaultTreeModel(root);
		
		setModel(model);
		
		createTree();
		
		addListeners(); 
		
		super.treeDidChange();
		
		//expandTree();
	}
	
	
	private void createTree() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
		
		ordinaryVariableList = net.getCurrentMFrag().getOrdinaryVariableList(); 
		
		for (OrdinaryVariable ordinaryVariable : ordinaryVariableList){
			
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(ordinaryVariable.getName()); 
			ordinaryVariableMap.put(node, ordinaryVariable); 
			
			root.add(node); 
			
		}
		
		expandRow(0); 
		
	}
	
	private class OrdinaryVariableTreeCellRenderer extends DefaultTreeCellRenderer {
		
		/** Serialization runtime version number */
		private static final long serialVersionUID = 0;
		
		private ImageIcon grayBorderBox = iconController.getGrayBorderBoxIcon();
		private ImageIcon orangeNodeIcon = iconController.getOrangeNodeIcon(); 
		
		/**
		 * Return a tree cell for the object value. 
		 */
		
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			
			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);
			
			Object obj = ordinaryVariableMap.get((DefaultMutableTreeNode) value);
			
			if (leaf) {
				if (obj != null) {
					setIcon(grayBorderBox);								
				}
			}else {
				setIcon(orangeNodeIcon);			    	   
				
			}
			return this;
		}
	}
	
	
	/**
	 * Atualiza as marginais na árvore desejada.
	 */
	public void updateTree() {
		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
		root.removeAllChildren(); 
		
		ordinaryVariableMap.clear(); 
		
		ordinaryVariableList = mfragActive.getOrdinaryVariableList(); 
		
		for (OrdinaryVariable ordinaryVariable : ordinaryVariableList){
			
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(ordinaryVariable.getName()); 
			ordinaryVariableMap.put(node, ordinaryVariable); 
			root.add(node); 
		}
		
		((DefaultTreeModel) getModel()).reload(root);
		expandRow(0); 
		
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
	
	public abstract void addListeners(); 
	
	
};		
