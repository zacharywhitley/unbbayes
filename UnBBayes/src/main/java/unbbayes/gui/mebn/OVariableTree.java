/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
import unbbayes.controller.MEBNController;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.util.ArrayMap;


/**
 * Tree of ordinary variables within a MFrag. It doesn't contain listeners (those
 * shall be created within a concrete class extending this one, making it possible
 * to reuse the same tree wherever possible).
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 (11/14/2006)
 */

public abstract class OVariableTree extends JTree{
	
	private MultiEntityBayesianNetwork net;
	
	protected ArrayMap<Object, OrdinaryVariable> ordinaryVariableMap = new ArrayMap<Object, OrdinaryVariable>();
	private List<OrdinaryVariable> ordinaryVariableList = new ArrayList<OrdinaryVariable>();
	private MFrag mfragActive; 
	
	protected IconController iconController = IconController.getInstance();
	
	protected final MEBNController controller;	
	
	
	public abstract void addListeners();
	
	public OVariableTree(MEBNController controller) {
		
		this.controller = controller; 
		this.net = controller.getMultiEntityBayesianNetwork();
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
			
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(ordinaryVariable.getName() + " (" + ordinaryVariable.getValueType() + ")"); 
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
	 * Updates the ordinary variables within the tree.
	 */	
	public void updateTree() {
		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
		root.removeAllChildren(); 
		
		ordinaryVariableMap.clear(); 
		
		ordinaryVariableList = mfragActive.getOrdinaryVariableList(); 
		
		for (OrdinaryVariable ordinaryVariable : ordinaryVariableList) {
			// TODO please, complete this method
		}
				
	}
}