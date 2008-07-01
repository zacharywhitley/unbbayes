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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import unbbayes.controller.IconController;
import unbbayes.controller.MEBNController;
import unbbayes.gui.GraphAction;
import unbbayes.gui.GraphPane;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.util.ArrayMap;
import unbbayes.util.ResourceController;

/**
 * Tree of the components of the MTheory. Show the MFrags and your 
 * nodes: resident nodes, input nodes and context nodes. 
 */

public class MTheoryTree extends JTree {

	private static final long serialVersionUID = 1L;
	private MultiEntityBayesianNetwork net;

	/*
	 * Contains the relation between the nodes of the tree and the objects of
	 * MTheory that it represent. 
	 */
	private ArrayMap<DefaultMutableTreeNode, Object> nodeTreeMap = new ArrayMap<DefaultMutableTreeNode, Object>(); 	
	private ArrayMap<Object, DefaultMutableTreeNode> inverseNodeMap = new ArrayMap<Object, DefaultMutableTreeNode>();
	
	private Object objectSelected; 
	
	private JPopupMenu popup = new JPopupMenu();
	
	private JPopupMenu popupMFrag = new JPopupMenu(); 
	private JPopupMenu popupNode = new JPopupMenu(); 
	

	protected IconController iconController = IconController.getInstance();

	private static final String POG = "this_is_a_pog_for_permited_names_really_" +
			"very_very_large_because_of_a_bug_in_java_5_that_dont_atualize_the_" +
			"size_of_the_label_of_tree";  
	
	private DefaultMutableTreeNode root; 
	
    private final MEBNController mebnController;	
    private final GraphPane graphPane;
    
	/** Load resource file from this package */
  	private static ResourceBundle resource =  ResourceController.RS_GUI;
  
  	/**
  	 * 
  	 * @param controller 
  	 * 
  	 * Pre-requisites: controller should have a MEBN
  	 */
  	
	public MTheoryTree(final MEBNController controller, GraphPane graphPane) {
		
		this.mebnController = controller; 
		this.net = controller.getMultiEntityBayesianNetwork();
		this.graphPane = graphPane; 
		
		/*----------------- build tree --------------------------*/ 
		
		setCellRenderer(new MTheoryTreeCellRenderer());
		
		root = new DefaultMutableTreeNode(POG);
		
		
	    DefaultTreeModel model = new DefaultTreeModel(root);
	    setModel(model);

		scrollPathToVisible(new TreePath(root.getPath()));
	    
		root.setUserObject(net.getName());
		
	    createTree();
	    
		scrollPathToVisible(new TreePath(root.getPath()));
	    
		//Popups for each node
	    createPopupMenu();
	    createPopupMenuMFrag(); 
	    createPopupMenuNode(); 
	    
	    addMouseListener(new MousePressedListener()); 
	    
	    super.treeDidChange();
	}
	
	private void createPopupMenuMFrag(){
		
		JMenuItem itemOpen =     new JMenuItem(resource.getString("menuOpen")); 
		JMenuItem itemDelete =   new JMenuItem(resource.getString("menuDelete")); 
		JMenuItem itemContext =  new JMenuItem(resource.getString("menuAddContext"));
		JMenuItem itemInput =    new JMenuItem(resource.getString("menuAddInput")); 
		JMenuItem itemResident = new JMenuItem(resource.getString("menuAddResident"));

		itemOpen.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				mebnController.setCurrentMFrag((MFrag)objectSelected);
			}
		}); 
		
		itemDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				mebnController.removeDomainMFrag((MFrag)objectSelected); 
				repaint(); 
			}
		}); 		
		
		itemContext.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				mebnController.setCurrentMFrag((MFrag)objectSelected);
				mebnController.setActionGraphCreateContextNode(); 
			}
		}); 
		
		itemInput.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				mebnController.setCurrentMFrag((MFrag)objectSelected);
				mebnController.setActionGraphCreateInputNode();  				
			}
		}); 
		
		itemResident.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				mebnController.setCurrentMFrag((MFrag)objectSelected);
				mebnController.setActionGraphCreateResidentNode();  								
			}
		}); 		
		
		popupMFrag.add(itemOpen); 
		popupMFrag.add(itemDelete); 
		popupMFrag.add(itemContext); 
		popupMFrag.add(itemResident); 
		popupMFrag.add(itemInput); 
	}

	private void createPopupMenuNode(){
		
		JMenuItem itemDelete =   new JMenuItem(resource.getString("menuDelete")); 

		itemDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				mebnController.deleteSelected(objectSelected); 
			}
		}); 		
		
		popupNode.add(itemDelete); 
	}
	
	private void createPopupMenu() {
		
		JMenuItem itemAddDomainMFrag = new JMenuItem(resource.getString("menuAddDomainMFrag"));
		itemAddDomainMFrag.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {   
            	mebnController.insertDomainMFrag(); 
            }
        });
		
        popup.add(itemAddDomainMFrag);
	}

	
	private void createTree() {
		
		List<MFrag> mFragList = net.getMFragList();
		
		for (MFrag mFrag : mFragList) {
			
			DefaultMutableTreeNode mFragTreeNode = new DefaultMutableTreeNode(POG);
			root.add(mFragTreeNode);
//			scrollPathToVisible(new TreePath(mFragTreeNode.getPath()));
			mFragTreeNode.setUserObject(mFrag); 
			inverseNodeMap.put(mFrag, mFragTreeNode); 
			nodeTreeMap.put(mFragTreeNode, mFrag); 

			//Resident Nodes
			List<ResidentNode> residentNodeList = mFrag.getResidentNodeList(); 
			for(ResidentNode residentNode: residentNodeList){
				DefaultMutableTreeNode treeNodeChild = new DefaultMutableTreeNode(POG);
				mFragTreeNode.add(treeNodeChild); 
//				scrollPathToVisible(new TreePath(treeNodeChild.getPath()));
				treeNodeChild.setUserObject(residentNode); 
				nodeTreeMap.put(treeNodeChild, residentNode);     
				inverseNodeMap.put(residentNode, treeNodeChild); 
			}

			//Input Nodes
			List<InputNode> inputNodeList = mFrag.getInputNodeList(); 
			for(InputNode inputNode: inputNodeList){
				DefaultMutableTreeNode treeNodeChild; 
				treeNodeChild = new DefaultMutableTreeNode(POG);
				mFragTreeNode.add(treeNodeChild); 
//				scrollPathToVisible(new TreePath(treeNodeChild.getPath()));
				treeNodeChild.setUserObject(inputNode); 
				nodeTreeMap.put(treeNodeChild, inputNode);     
				inverseNodeMap.put(inputNode, treeNodeChild); 
			}

			//Context Nodes
			List<ContextNode> contextNodeList = mFrag.getContextNodeList(); 
			for(ContextNode contextNode: contextNodeList){
				DefaultMutableTreeNode treeNodeChild = new DefaultMutableTreeNode(POG);
				mFragTreeNode.add(treeNodeChild); 
//				scrollPathToVisible(new TreePath(treeNodeChild.getPath()));
				treeNodeChild.setUserObject(contextNode); 
				nodeTreeMap.put(treeNodeChild, contextNode);  
				inverseNodeMap.put(contextNode, treeNodeChild); 
			}			    
			
		}
		
		for(int i = 0 ; i < this.getRowCount(); i++){
			this.expandRow(i); 
		}
	}

	/**
	 * Add a MFrag to the Tree how child of the Root node
	 */
	public void addMFrag(MFrag mFrag){
		DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(POG); 
		
		((DefaultTreeModel)getModel()).insertNodeInto(newChild, root, 
				root.getChildCount()); 
		
		scrollPathToVisible(new TreePath(newChild.getPath()));
		
		newChild.setUserObject(mFrag); 
		
		nodeTreeMap.put(newChild, mFrag); 
		inverseNodeMap.put(mFrag, newChild); 
		
		scrollPathToVisible(new TreePath(newChild.getPath()));
		repaint(); 
	}
	
	/**
	 * Add a node into the tree how child of a MFrag
	 * 
	 * @param mFrag
	 * @param node
	 */
	public void addNode(MFrag mFrag, Node node){

		DefaultMutableTreeNode nodeFather = inverseNodeMap.get(mFrag);
		DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(POG); 
		((DefaultTreeModel)getModel()).insertNodeInto(newChild, nodeFather, 
				nodeFather.getChildCount()); 

		scrollPathToVisible(new TreePath(newChild.getPath()));
		
		newChild.setUserObject(node); 
		
		nodeFather.add(newChild);
		nodeTreeMap.put(newChild, (Node)node); 
		inverseNodeMap.put(node, newChild); 
		
		scrollPathToVisible(new TreePath(newChild.getPath()));

		repaint(); 
	}
	
	public void removeMFrag(MFrag mFrag){
		DefaultMutableTreeNode treeNode = inverseNodeMap.get(mFrag); 
		
		//remove childs
		if(treeNode.getChildCount() != 0){
			for(int i = 0; i < treeNode.getChildCount(); i++){
				((DefaultTreeModel)getModel()).removeNodeFromParent(
						(MutableTreeNode)treeNode.getChildAt(0)); 
			}
		}
		
		//remove mFrag node
		((DefaultTreeModel)getModel()).removeNodeFromParent(treeNode); 

		repaint(); 
	}
	
	public void removeNode(Node node){
		DefaultMutableTreeNode treeNode = inverseNodeMap.get(node); 
		((DefaultTreeModel)getModel()).removeNodeFromParent(treeNode); 

		repaint(); 
	}
	
	public void renameNode(Node node){
		DefaultMutableTreeNode treeNode = inverseNodeMap.get(node); 
		scrollPathToVisible(new TreePath(treeNode.getPath()));
		repaint(); 
	}
	
	public void renameMFrag(MFrag mFrag){
		DefaultMutableTreeNode treeNode = inverseNodeMap.get(mFrag); 
		scrollPathToVisible(new TreePath(treeNode.getPath()));
		repaint(); 
	}
	
	public void renameMTheory(String name){
		root.setUserObject(name); 
		scrollPathToVisible(new TreePath(root.getPath()));
		repaint(); 
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
			DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) selPath
					.getLastPathComponent();

			if (mutableTreeNode.isLeaf()) {
				
				Object nodeLeaf = nodeTreeMap.get(mutableTreeNode); 
				objectSelected = nodeLeaf; 
				
				if (nodeLeaf instanceof MFrag){
					if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
						
						popupMFrag.setEnabled(true);
						popupMFrag.show(e.getComponent(),e.getX(),e.getY());
						
					} else if (e.getClickCount() == 2
							&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
						
						mebnController.setCurrentMFrag((MFrag)nodeLeaf); 
						
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
							 
							Object fatherNode = objectSelected;
							TreeNode treeNode = mutableTreeNode; 
							
							while (!(fatherNode instanceof MFrag)){
								treeNode = treeNode.getParent(); 
								fatherNode = nodeTreeMap.get(treeNode); 
							}
							
							mebnController.setCurrentMFrag((MFrag)fatherNode); 
							mebnController.selectNode((Node)objectSelected); 
							
						} else if (e.getClickCount() == 1) {
							
						}
					}					
				}
				
				
			} 
			else { //Not is a leaf 
				
				Object nodeLeaf = nodeTreeMap.get(mutableTreeNode); 
				objectSelected = nodeLeaf; 
				
				if (nodeLeaf instanceof MFrag){
					if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
						
						popupMFrag.setEnabled(true);
						popupMFrag.show(e.getComponent(),e.getX(),e.getY());
					
					} else if (e.getClickCount() == 2
							&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
						
						mebnController.setCurrentMFrag((MFrag)nodeLeaf);
						
					} else if (e.getClickCount() == 1) {
					    
						//Do nothing
						
					}
				}
				else{
					if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
						
						if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
							popup.setEnabled(true);
							popup.show(e.getComponent(),e.getX(),e.getY());
						}
						
					}
					if (e.getClickCount() == 1) {
						
						//Do nothing
						
					} else if (e.getClickCount() == 2) {

						//Do nothing
						
					}
				}
			}
		}
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
			
			Object obj = nodeTreeMap.get(value);
			
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

}