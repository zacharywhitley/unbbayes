/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008, 2011 Universidade de Brasilia - http://www.unb.br
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import unbbayes.controller.IconController;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.draw.UShapeMFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.util.ArrayMap;
import unbbayes.util.ResourceController;

/**
 * Tree showing all MFrags available in the MTheory with options on how to 
 * visualize them (rounded border, etc).
 * 
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 * @version 1.0 06/18/2011 - (feature:3317031)
 */

public class MTheoryViewTree extends JTree {

	private static final long serialVersionUID = 1L;
	private MultiEntityBayesianNetwork net;

	/*
	 * Contains the relation between the nodes of the tree and the objects of
	 * MTheory that it represent. 
	 */
	private ArrayMap<DefaultMutableTreeNode, Object> nodeTreeMap = new ArrayMap<DefaultMutableTreeNode, Object>(); 	
	private ArrayMap<Object, DefaultMutableTreeNode> inverseNodeMap = new ArrayMap<Object, DefaultMutableTreeNode>();
	
	protected IconController iconController = IconController.getInstance();

	private static final String POG = "this_is_a_pog_for_permited_names_really_" +
			"very_very_large_because_of_a_bug_in_java_5_that_dont_atualize_the_" +
			"size_of_the_label_of_tree";  
	
	private CheckNode root; 
	
    private final MTheoryGraphPane graphPane;
    
	/** Load resource file from this package */
  	private ResourceBundle resource;
  
  	/**
  	 * 
  	 * @param controller 
  	 * 
  	 * Pre-requisites: controller should have a MEBN
  	 */
  	
	public MTheoryViewTree(final MEBNController controller, MTheoryGraphPane graphPane) {
		
		this.net = controller.getMultiEntityBayesianNetwork();
		this.graphPane = graphPane; 
		
		this.resource = ResourceController.newInstance().getBundle(
        		unbbayes.gui.mebn.resources.Resources.class.getName(),
    			Locale.getDefault(),
    			this.getClass().getClassLoader());
		
		/*----------------- build tree --------------------------*/ 
		setCellRenderer(new MTheoryTreeCellRenderer());
		
		root = new CheckNode(POG);
		
		
	    DefaultTreeModel model = new DefaultTreeModel(root);
	    setModel(model);
	    
	    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	    
	    putClientProperty("JTree.lineStyle", "Angled");
		addMouseListener(new NodeSelectionListener(this));

		scrollPathToVisible(new TreePath(root.getPath()));
	    
		root.setUserObject(net.getName());
		
		root.setSelected(true);
		
	    createTree();
	    
		scrollPathToVisible(new TreePath(root.getPath()));
	    
	    super.treeDidChange();
	}
	
	private void createTree() {
		
		List<UShapeMFrag> mFragList = graphPane.getShapes();
		
		for (UShapeMFrag mFrag : mFragList) {
			
			CheckNode mFragTreeNode = new CheckNode(mFrag);
			root.add(mFragTreeNode);
			inverseNodeMap.put(mFrag, mFragTreeNode); 
			nodeTreeMap.put(mFragTreeNode, mFrag); 
			mFragTreeNode.setSelected(true);
			
			String option = "Show Title Border";
			CheckNode treeNodeChild = new CheckNode(option);
			mFragTreeNode.add(treeNodeChild);
			nodeTreeMap.put(treeNodeChild, option);     
			inverseNodeMap.put(option, treeNodeChild);
			treeNodeChild.setSelected(true);
			mFrag.showTitleBorder = true;
			
			option = "Show Body Border";
			treeNodeChild = new CheckNode(option);
			mFragTreeNode.add(treeNodeChild);
			nodeTreeMap.put(treeNodeChild, option);     
			inverseNodeMap.put(option, treeNodeChild);
			treeNodeChild.setSelected(true);
			mFrag.showBodyBorder = true;
			
			option = "Show Round Border";
			treeNodeChild = new CheckNode(option);
			mFragTreeNode.add(treeNodeChild);
			nodeTreeMap.put(treeNodeChild, option);     
			inverseNodeMap.put(option, treeNodeChild);
			treeNodeChild.setSelected(true);
			mFrag.showBodyBorder = true;
			
		}
		
		for(int i = 0 ; i < this.getRowCount(); i++){
			this.expandRow(i); 
		}
	}
	
	class NodeSelectionListener extends MouseAdapter {
		JTree tree;

		NodeSelectionListener(JTree tree) {
			this.tree = tree;
		}

		public void mouseClicked(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			int row = tree.getRowForLocation(x, y);
			TreePath path = tree.getPathForRow(row);
			// TreePath path = tree.getSelectionPath();
			if (path != null) {
				CheckNode node = (CheckNode) path.getLastPathComponent();
				boolean isSelected = !(node.isSelected());
				node.setSelected(isSelected);
				
				Object object = nodeTreeMap.get(node);
				
				if (object != null) {
					// Unselect parent if one child was unselected
					if (!isSelected) {
						CheckNode parentNode = (CheckNode)node.getParent();
						do {
							parentNode.setSelected(isSelected, false);
							parentNode = (CheckNode)parentNode.getParent();
						} while (parentNode != null);
					}
					// If the object is an MFrag, then select all options
					if (object instanceof UShapeMFrag) {
//						graphPane.selectNode(((UShapeMFrag)object).getNode());
						((UShapeMFrag)object).setShowAll(isSelected);
						((UShapeMFrag)object).repaint();
					// Otherwise (un)select the specific option
					} else if (object.equals(resource.getString("showTitleBorder"))) {
						Object parent = nodeTreeMap.get(node.getParent());
						if (parent instanceof UShapeMFrag) {
							((UShapeMFrag)parent).showTitleBorder = isSelected;
							((UShapeMFrag)parent).repaint();
						}
					} else if (object.equals(resource.getString("showBodyBorder"))) {
						Object parent = nodeTreeMap.get(node.getParent());
						if (parent instanceof UShapeMFrag) {
							((UShapeMFrag)parent).showBodyBorder = isSelected;
							((UShapeMFrag)parent).repaint();
						}
					} else if (object.equals(resource.getString("showRoundBorder"))) {
						Object parent = nodeTreeMap.get(node.getParent());
						if (parent instanceof UShapeMFrag) {
							((UShapeMFrag)parent).showRoundBorder = isSelected;
							((UShapeMFrag)parent).repaint();
						}
					}
				// If it is the root node, select all options from all MFrags
				} else {
					for (UShapeMFrag shape : graphPane.getShapes()) {
						shape.setShowAll(isSelected);
						shape.repaint();
					}
				}
				
				((DefaultTreeModel) tree.getModel()).nodeChanged(node);
				tree.revalidate();
				tree.repaint();
			}
		}
	}
	

	
	private class MTheoryTreeCellRenderer extends JPanel implements TreeCellRenderer {

		/** Serialization runtime version number */
		private static final long serialVersionUID = 0;

		private ImageIcon mTheoryNodeIcon = iconController.getMTheoryNodeIcon(); 
		
		private ImageIcon mFragNodeIcon = iconController.getMFragIcon();
		
		protected JCheckBox check;
		
		protected TreeLabel label;

		public MTheoryTreeCellRenderer() {
			setLayout(null);
			add(check = new JCheckBox());
			add(label = new TreeLabel());
			this.setBackground(Color.WHITE);
			check.setBackground(Color.WHITE);
			label.setForeground(UIManager.getColor("Tree.textForeground"));
		}
		
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean isSelected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			
			String stringValue = tree.convertValueToText(value, isSelected,
					expanded, leaf, row, hasFocus);
			setEnabled(tree.isEnabled());
			check.setSelected(((CheckNode) value).isSelected());
			label.setFont(tree.getFont());
			label.setText(stringValue);
			label.setSelected(isSelected);
			label.setFocus(hasFocus);
			
			Object obj = nodeTreeMap.get(value);
			
			if (leaf) {
				label.setIcon(null);
			} else {
				if (obj instanceof UShapeMFrag) {
					label.setIcon(mFragNodeIcon);
				} else {
					label.setIcon(mTheoryNodeIcon);
				}
			}
			
			return this;
		}
		
		public Dimension getPreferredSize() {
			Dimension d_check = check.getPreferredSize();
			Dimension d_label = label.getPreferredSize();
			return new Dimension(d_check.width + d_label.width,
					(d_check.height < d_label.height ? d_label.height
							: d_check.height));
		}

		public void doLayout() {
			Dimension d_check = check.getPreferredSize();
			Dimension d_label = label.getPreferredSize();
			int y_check = 0;
			int y_label = 0;
			if (d_check.height < d_label.height) {
				y_check = (d_label.height - d_check.height) / 2;
			} else {
				y_label = (d_check.height - d_label.height) / 2;
			}
			check.setLocation(0, y_check);
			check.setBounds(0, y_check, d_check.width, d_check.height);
			label.setLocation(d_check.width, y_label);
			label.setBounds(d_check.width, y_label, d_label.width, d_label.height);
		}

		public void setBackground(Color color) {
			if (color instanceof ColorUIResource)
				color = null;
			super.setBackground(color);
		}
	}
	
	class TreeLabel extends JLabel {
		
		private static final long serialVersionUID = 2861292273309887871L;

		boolean isSelected;

		boolean hasFocus;

		public TreeLabel() {
		}

		public void setBackground(Color color) {
			if (color instanceof ColorUIResource)
				color = null;
			super.setBackground(color);
		}

		public void paint(Graphics g) {
			String str;
			if ((str = getText()) != null) {
				if (0 < str.length()) {
					if (isSelected) {
						g.setColor(UIManager
								.getColor("Tree.selectionBackground"));
					} else {
						g.setColor(UIManager.getColor("Tree.textBackground"));
					}
					Dimension d = getPreferredSize();
					int imageOffset = 0;
					Icon currentI = getIcon();
					if (currentI != null) {
						imageOffset = currentI.getIconWidth()
								+ Math.max(0, getIconTextGap() - 1);
					}
					g.fillRect(imageOffset, 0, d.width - 1 - imageOffset,
							d.height);
					if (hasFocus) {
						g.setColor(UIManager
								.getColor("Tree.selectionBorderColor"));
						g.drawRect(imageOffset, 0, d.width - 1 - imageOffset,
								d.height - 1);
					}
				}
			}
			super.paint(g);
		}

		public Dimension getPreferredSize() {
			Dimension retDimension = super.getPreferredSize();
			if (retDimension != null) {
				retDimension = new Dimension(retDimension.width + 3,
						retDimension.height);
			}
			return retDimension;
		}

		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
		}

		public void setFocus(boolean hasFocus) {
			this.hasFocus = hasFocus;
		}
	}

}

class CheckNode extends DefaultMutableTreeNode {

	private static final long serialVersionUID = -479248745316746635L;

	public final static int SINGLE_SELECTION = 0;

	public final static int DIG_IN_SELECTION = 4;

	protected int selectionMode;

	protected boolean isSelected;

	public CheckNode() {
		this(null);
	}

	public CheckNode(Object userObject) {
		this(userObject, true, false);
	}

	public CheckNode(Object userObject, boolean allowsChildren,
			boolean isSelected) {
		super(userObject, allowsChildren);
		this.isSelected = isSelected;
		setSelectionMode(DIG_IN_SELECTION);
	}

	public void setSelectionMode(int mode) {
		selectionMode = mode;
	}

	public int getSelectionMode() {
		return selectionMode;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;

		if ((selectionMode == DIG_IN_SELECTION) && (children != null)) {
			Enumeration e = children.elements();
			while (e.hasMoreElements()) {
				CheckNode node = (CheckNode) e.nextElement();
				node.setSelected(isSelected);
			}
		}
	}
	
	public void setSelected(boolean isSelected, boolean digIn) {
		this.isSelected = isSelected;

		if (digIn) {
			setSelected(isSelected);
		}
	}

	public boolean isSelected() {
		return isSelected;
	}

	// If you want to change "isSelected" by CellEditor,
	/*
	 * public void setUserObject(Object obj) { if (obj instanceof Boolean) {
	 * setSelected(((Boolean)obj).booleanValue()); } else {
	 * super.setUserObject(obj); } }
	 */

}