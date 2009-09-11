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
package unbbayes.metaphor;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import unbbayes.controller.IconController;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.util.ArrayMap;

/**
 * @author Mario Henrique Paes Vieira
 * @version 1.0
 */
public class MetaphorTree extends JTree
{
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
	private class StateObject
	{   private ProbabilisticNode node;
	private int stateIndex = -1;
	private int check = CHECK_EMPTY;
	
	public StateObject(ProbabilisticNode node,int stateIndex, int check)
	{   this.node = node;
	this.stateIndex = stateIndex;
	this.check = check;
	}
	
	public int getStateIndex()
	{   return stateIndex;
	}
	
	public void setStateIndex(int stateIndex)
	{   this.stateIndex = stateIndex;
	}
	
	public int getCheck()
	{   return check;
	}
	
	public void setCheck(int check)
	{   this.check = check;
	}
	
	public ProbabilisticNode getProbabilisticNode()
	{   return node;
	}
	}
	
	private class MetaphorTreeCellRenderer extends javax.swing.tree.DefaultTreeCellRenderer
	{
		/** Serialization runtime version number */
		private static final long serialVersionUID = 0;			
		
		ImageIcon yesIcon = iconController.getYesStateIcon();
		ImageIcon noIcon = iconController.getNoStateIcon();
		ImageIcon emptyIcon = iconController.getEmptyStateIcon();
		ImageIcon evidenciasIcon = iconController.getMoreIcon();
		ImageIcon folderSmallIcon = iconController.getFolderSmallIcon();
		
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{   super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;
		if (leaf)
		{   Object obj = objectsMap.get(treeNode);
		if (obj instanceof StateObject)
		{   StateObject stateObject = (StateObject)obj;
		int check = stateObject.getCheck();
		setIcon((check == CHECK_YES) ? yesIcon : ((check == CHECK_NO) ? noIcon : emptyIcon));
		}
		}
		else
		{   Object obj = objectsMap.get(treeNode);
		if (obj instanceof Node)
		{   setIcon(evidenciasIcon);
		}
		this.setOpenIcon(folderSmallIcon);
		this.setClosedIcon(folderSmallIcon);
		}
		return this;
		}
	}
	
	public static final int CHECK_YES = 1;
	public static final int CHECK_NO = -1;
	public static final int CHECK_EMPTY = 0;
	
	private ProbabilisticNetwork net = null;
	private boolean showProbability = false;
	private ArrayMap<DefaultMutableTreeNode, Object> objectsMap = new ArrayMap<DefaultMutableTreeNode, Object>();
	private NumberFormat nf;
	protected IconController iconController = IconController.getInstance();
	
	protected MetaphorTree()
	{   setShowsRootHandles(true);
	setSelectionModel(null);
	setRootVisible(false);
	this.setAutoscrolls(true);
	setCellRenderer(new MetaphorTreeCellRenderer());
	addMouseListener(new MouseAdapter()
			{   public void mouseClicked(java.awt.event.MouseEvent evt)
			{   methaphorTreeMouseClicked(evt);
			}
			});
	nf = NumberFormat.getInstance(Locale.US);
	nf.setMaximumFractionDigits(4);
	}
	
	public MetaphorTree(ProbabilisticNetwork net)
	{	this(net,false);
	}
	
	public MetaphorTree(ProbabilisticNetwork net, boolean showProbability)
	{	this();
	this.showProbability = showProbability;
	setProbabilisticNetwork(net);
	}
	
	public void setProbabilisticNetwork(ProbabilisticNetwork net)
	{   DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();
	if (net != null)
	{   if (!net.equals(this.net))
	{   this.net = net;
	root.removeAllChildren();
	objectsMap.clear();
	DefaultTreeModel model = new DefaultTreeModel((DefaultMutableTreeNode)net.getHierarchicTree().getModel().getRoot());
	this.setModel(model);
	root = (DefaultMutableTreeNode) getModel().getRoot();
	ArrayList<Node> nos = net.getDescriptionNodes();
	int size = nos.size();
	for (int i = 0; i < size; i++)
	{   Node node = (Node) nos.get(i);
	DefaultMutableTreeNode treeNode = findUserObject(node.getDescription(),root);
	if (treeNode != null)
	{   objectsMap.put(treeNode, node);
	int statesSize = node.getStatesSize();
	for (int j = 0; j < statesSize; j++)
	{   DefaultMutableTreeNode stateNode = new DefaultMutableTreeNode(node.getStateAt(j) + (showProbability ? " " + nf.format(((TreeVariable)node).getMarginalAt(j) * 100.0) + "%" : ""));
	treeNode.add(stateNode);
	objectsMap.put(stateNode,new StateObject((ProbabilisticNode)node, j, CHECK_EMPTY));
	}
	}
	else
	{   DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node.getDescription());
	objectsMap.put(newNode, node);
	root.add(newNode);
	}
	}
	}
	}
	else
	{   this.net = null;
	root.removeAllChildren();
	objectsMap.clear();
	}
	}
	
	private DefaultMutableTreeNode findUserObject(String treeNode,DefaultMutableTreeNode root)
	{   Enumeration e = root.breadthFirstEnumeration();
	while (e.hasMoreElements())
	{   DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.nextElement();
	if (node.getUserObject().toString().equals(treeNode))
		return node;
	}
	return null;
	}
	
	public ProbabilisticNetwork getProbabilisticNetwork() {
		return net;
	}
	
	public void setShowProbability(boolean showProbability)
	{	if (showProbability != this.showProbability)
	{	this.showProbability = showProbability;
	ProbabilisticNetwork temp = net;
	setProbabilisticNetwork(null);
	setProbabilisticNetwork(temp);
	}
	}
	
	public boolean getShowProbability()
	{	return showProbability;
	}
	
	public void propagate()
	{   try
	{   net.initialize();
	int count = getRowCount();
	for (int i = 0; i < count; i++)
	{   TreePath path = getPathForRow(i);
	DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)path.getLastPathComponent();
	Object obj = objectsMap.get(treeNode);
	if (obj instanceof StateObject)
	{   StateObject stateObject = (StateObject)obj;
	if (stateObject.getCheck() == CHECK_YES)
	{   ProbabilisticNode node = stateObject.getProbabilisticNode();
	node.addFinding(stateObject.getStateIndex());
	}
	}
	}
	net.updateEvidences();
	}
	catch (Exception e)
	{}
	/*if (showProbability)
	 {	ProbabilisticNetwork temp = net;
	 setProbabilisticNetwork(null);
	 setProbabilisticNetwork(temp);
	 }*/
	}
	
	private void methaphorTreeMouseClicked(java.awt.event.MouseEvent evt) {
		TreePath clickedPath = getPathForLocation(evt.getX(), evt.getY());
		if (clickedPath != null) {
			DefaultMutableTreeNode clickedNode = (DefaultMutableTreeNode)(clickedPath.getLastPathComponent());
			if (clickedNode != null && clickedNode.isLeaf()) {
				Object obj = objectsMap.get(clickedNode);
				if (obj instanceof StateObject) {
					DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)clickedNode.getParent();
					Enumeration childrenEnum = parentNode.children();
					StateObject yesChecked = null;
					ArrayList<StateObject> noCheckeds = new ArrayList<StateObject>(),
					emptyCheckeds = new ArrayList<StateObject>();
					while (childrenEnum.hasMoreElements()) {
						DefaultMutableTreeNode child = (DefaultMutableTreeNode)childrenEnum.nextElement();
						if (!child.equals(clickedNode)) {
							if (((StateObject)objectsMap.get(child)).getCheck() == CHECK_YES) {
								yesChecked = (StateObject)objectsMap.get(child);
							}
							else if (((StateObject)objectsMap.get(child)).getCheck() == CHECK_NO) {
								noCheckeds.add((StateObject)objectsMap.get(child));
							}
							else {
								emptyCheckeds.add((StateObject)objectsMap.get(child));
							}
						}
					}
					if (SwingUtilities.isLeftMouseButton(evt)) {
						if (((StateObject)obj).getCheck() == CHECK_YES) {
							((StateObject)obj).setCheck(CHECK_EMPTY);
							for (int i = 0; i < noCheckeds.size(); i++) {
								((StateObject)noCheckeds.get(i)).setCheck(CHECK_EMPTY);
							}
						}
						else {
							((StateObject)obj).setCheck(CHECK_YES);
							if (yesChecked != null) {
								yesChecked.setCheck(CHECK_NO);
							}
							for (int i = 0; i < emptyCheckeds.size(); i++) {
								((StateObject)emptyCheckeds.get(i)).setCheck(CHECK_NO);
							}
						}
					}
					if (SwingUtilities.isRightMouseButton(evt)) {
						if (((StateObject)obj).getCheck() == CHECK_NO) {
							((StateObject)obj).setCheck(CHECK_EMPTY);
							if (yesChecked != null) {
								yesChecked.setCheck(CHECK_EMPTY);
							}
						}
						else if (noCheckeds.size() < (parentNode.getChildCount() - 1)) {
							((StateObject)obj).setCheck(CHECK_NO);
							if (noCheckeds.size() == (parentNode.getChildCount() - 2)) {
								((StateObject)emptyCheckeds.get(0)).setCheck(CHECK_YES);
							}
						}
					}
					repaint();
				}
			}
		}
	}
	
	/**
	 *  Expande todos os nos da �rvore.
	 *
	 * @see            JTree
	 */
	public void expandTree()
	{   for (int i = 0; i < getRowCount(); i++)
	{   expandRow(i);
	}
	}
	
	/**
	 *  Retrai todos os nos da �rvore.
	 *
	 * @see            JTree
	 */
	public void collapseTree()
	{   for (int i = 0; i < getRowCount(); i++)
	{   collapseRow(i);
	}
	}
	
	/**
	 * Modifica o formato de numeros
	 *
	 * @param local localidade do formato de numeros.
	 */
	public void setNumberFormat(Locale local)
	{   nf = NumberFormat.getInstance(local);
	}
	
}