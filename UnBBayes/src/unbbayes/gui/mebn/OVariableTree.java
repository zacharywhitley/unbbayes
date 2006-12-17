package unbbayes.gui.mebn;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import unbbayes.controller.IconController;
import unbbayes.controller.NetworkController;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.util.ArrayMap;


/**
 * Arvore de variaveis ordinarias de uma MFrag. Não contem os listeners (estes
 * devem ser criados na classe concreta que estender esta de forma que a mesma
 * arvore possa ser aproveitada onde for necessaria 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 (11/14/2006)
 */

public abstract class OVariableTree extends JTree{
	
	private MultiEntityBayesianNetwork net;
	
	protected ArrayMap<Object, OrdinaryVariable> ordinaryVariableMap = new ArrayMap<Object, OrdinaryVariable>();
	private List<OrdinaryVariable> ordinaryVariableList = new ArrayList<OrdinaryVariable>();
	private MFrag mfragActive; 
	
	protected IconController iconController = IconController.getInstance();
	
	protected final NetworkController controller;	
	
	public OVariableTree(final NetworkController controller) {
		
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
		
	}
	
	/*
	 * cria a arvore a partir da lista de variaveis ordinarias da MFrag. 
     */
	
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
	 * Atualiza as variaveis ordinarias presentes na arvore
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
	
	/**
	 * adiciona os listeners para os nodos da arvore. (a classe que estender
	 * esta deve colocar as acoes pertinentes aos evendos de mouse) 
	 */
	
	protected abstract void addListeners(); 
	
	
};		
