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
import unbbayes.controller.mebn.MEBNController;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.util.ArrayMap;

/**
 * Lista as variaveis ordinarias atualmente presentes como argumento
 * em um resident node. 
 * 
 * @author Laecio
 *
 */
public class ResidentOVariableTree extends JTree{

    private MultiEntityBayesianNetwork net;
    
	private ArrayMap<Object, OrdinaryVariable> ordinaryVariableMap = new ArrayMap<Object, OrdinaryVariable>();
    private List<OrdinaryVariable> ordinaryVariableList = new ArrayList<OrdinaryVariable>();
    private IResidentNode residentNodeActive; 
	
    private OrdinaryVariable oVariableSelected = null; 
    
    
	protected IconController iconController = IconController.getInstance();
	
	private final MEBNController mebnController; 
	
	/**
	 * Tree with the ordinary variables arguments of a resident node. 
	 * Used for ediction of this list. 
	 * 
	 * @param controller
	 * @param resident
	 */
	public ResidentOVariableTree(final MEBNController controller, ResidentNode resident) {
		
		this.mebnController = controller; 
		this.net = controller.getMultiEntityBayesianNetwork();
        this.residentNodeActive = resident; 
		
		// set up node icons
		setCellRenderer(new OrdinaryVariableTreeCellRenderer());
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(resident.getName());
		DefaultTreeModel model = new DefaultTreeModel(root);
		
		setModel(model);
		
		createTree();
		
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
				
				OrdinaryVariable ordinaryVariable = ordinaryVariableMap.get(node); 
							
				if (node.isLeaf() && (ordinaryVariable != null)) {
				
					if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
						
					} else if (e.getClickCount() == 1
							&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
						
						oVariableSelected = ordinaryVariable; 
						mebnController.setOVariableSelectedInResidentTree(oVariableSelected); 
						
					} 
				} 
				else {
				
					oVariableSelected = null; 
					
			 	}
			}
			
		
	});		
		
		super.treeDidChange();
		//expandTree();
	}
	
	
	private void createTree() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
		
		ordinaryVariableList = residentNodeActive.getOrdinaryVariableList(); 
		
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
		private ImageIcon greenNodeIcon = iconController.getGreenNodeIcon(); 

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
				setIcon(greenNodeIcon);			    	   
			}
			return this;
		}
	}

		
		/**
		 * Atualiza os nodos da arvore. 
		 */
		public void updateTree() {
			
			DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
			
			root.removeAllChildren(); 
			ordinaryVariableMap.clear(); 
			
			ordinaryVariableList = residentNodeActive.getOrdinaryVariableList(); 
			
			for (OrdinaryVariable ordinaryVariable : ordinaryVariableList){
				
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(ordinaryVariable.getName()); 
				ordinaryVariableMap.put(node, ordinaryVariable); 
				
				root.add(node); 
				
			}
			
			oVariableSelected = null; 
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
		
		public OrdinaryVariable getOVariableSelected(){
			return oVariableSelected; 
		}
		
		public void setOVariableSelected(OrdinaryVariable ov){
			oVariableSelected = ov; 
		}
		
	}