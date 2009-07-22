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
package unbbayes.gui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import unbbayes.controller.IconController;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.util.ArrayMap;
import unbbayes.util.ResourceController;

/**
 * @author Mário Henrique Paes Vieira
 * @version 1.0
 */
public class EvidenceTree extends JTree {
	
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;	
	
	private SingleEntityNetwork net;
	private NumberFormat nf;
	private boolean[] expandedNodes;
	private ArrayMap<Object, Node> objectsMap = new ArrayMap<Object, Node>();
    protected IconController iconController = IconController.getInstance();

    private final NetworkWindow netWindow; 
    
	private ResourceBundle resource = ResourceController.RS_GUI; 
	
	//by young
	public String strMode;
	public static final String MODE_USE_NAME		= "UseName";
	public static final String MODE_USE_DESC		= "UseDescription"; 
	
		
	public EvidenceTree(SingleEntityNetwork sen, final NetworkWindow netWindow) {
		
		this.net = sen;
		this.netWindow = netWindow; 
		strMode = MODE_USE_NAME;
		
		nf = NumberFormat.getInstance(Locale.US);
		nf.setMaximumFractionDigits(2);

		// set up node icons
		setCellRenderer(new EvidenceTreeCellRenderer());

		//trata os eventos de mouse para a arvore de evidencias
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int selRow = getRowForLocation(e.getX(), e.getY());
				if (selRow == -1) {
					return;
				}

				TreePath selPath = getPathForLocation(e.getX(), e.getY());
				DefaultMutableTreeNode node =
					(DefaultMutableTreeNode) selPath.getLastPathComponent();

				if (node.isLeaf()) {
					if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
						showLikelihood(
							(DefaultMutableTreeNode) node.getParent());
					} else if (
						e.getClickCount() == 2
							&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
						treeDoubleClick(node);
						//by young
						Node newNode = getNodeMap(node);
						netWindow.getGraphPane().compiled(false, newNode);
					}
				} else {
					if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
						showLikelihood(node);
					}
					if (e.getClickCount() == 1) {
						Node newNode = getNodeMap(node);
						if (newNode != null) {
							netWindow.getGraphPane().selectNode(newNode);
							//by young
							netWindow.getGraphPane().compiled(false, newNode);
						}
					} else if (e.getClickCount() == 2) {
						DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
						int index = root.getIndex(node);
						expandedNodes[index] = ! expandedNodes[index];
						/*
						Node newNode = getNodeMap(node);
						if (newNode != null) {
						  index = net.getNodeIndex(newNode.getName());
						  System.out.println(newNode+" "+index);
						  expandedNodes[index] = ! expandedNodes[index];
						}
						*/
					}
				}
			}
		});
		super.treeDidChange();

	}

	private class EvidenceTreeCellRenderer extends DefaultTreeCellRenderer {

		/** Serialization runtime version number */
		private static final long serialVersionUID = 0;	
		
		private ImageIcon folderSmallIcon = iconController.getFolderSmallIcon();
		private ImageIcon folderSmallDisabledIcon = iconController.getFolderSmallDisabledIcon();
		private ImageIcon yellowBallIcon = iconController.getYellowBallIcon();
		private ImageIcon greenBallIcon = iconController.getGreenBallIcon();

		public Component getTreeCellRendererComponent(
			JTree tree,
			Object value,
			boolean sel,
			boolean expanded,
			boolean leaf,
			int row,
			boolean hasFocus) {
			super.getTreeCellRendererComponent(
				tree,
				value,
				sel,
				expanded,
				leaf,
				row,
				hasFocus);
			if (leaf) {
				DefaultMutableTreeNode parent =
					(DefaultMutableTreeNode) (((DefaultMutableTreeNode) value)
						.getParent());
				Object obj = objectsMap.get((DefaultMutableTreeNode) parent);
				if (obj != null) {
					Node node = (Node) obj;
					if (node.getInformationType() == Node.DESCRIPTION_TYPE) {
						setIcon(yellowBallIcon);
					} else {
						setIcon(greenBallIcon);
					}
				} else {
					setIcon(yellowBallIcon);
				}
			} else {
				Object obj = objectsMap.get((DefaultMutableTreeNode) value);
				if (obj != null) {
					Node node = (Node) obj;
					if (node.getInformationType() == Node.DESCRIPTION_TYPE) {
						setOpenIcon(folderSmallIcon);
						setClosedIcon(folderSmallIcon);
						setIcon(folderSmallIcon);
					} else {
						setOpenIcon(folderSmallDisabledIcon);
						setClosedIcon(folderSmallDisabledIcon);
						setIcon(folderSmallDisabledIcon);
					}
				} else {
					setOpenIcon(folderSmallIcon);
					setClosedIcon(folderSmallIcon);
					setIcon(folderSmallIcon);
				}
			}
			return this;
		}
	}
	
	//by young
	public void setMode( String str )
	{
		strMode = str;
		updateTree(false);
	}
	
	//by young
	public String getMode()
	{
		return strMode;
	}

	/**
	 *  Collapses every single nodes from a tree
	 *
	 * @param  tree  a <code>JTree</code> representing a BN in a tree format
	 *
	 * @since
	 * @see            JTree
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
	 *  Expande todos os nos da arvore desejada.
	 *
	 * @param  arvore  uma <code>JTree</code> que representa a rede Bayesiana em
	 *      forma de arvore.
	 * @since
	 * @see            JTree
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
		for (int i = expandedNodes.length-1; i >= 0; i--) {
			if (expandedNodes[i]) {
				expandRow(i);
			} else {
				collapseRow(i);
			}
		}
	}
	
	/**
	 * Reset the evidence tree to its default.
	 */
	public void resetTree() {
		expandedNodes = null;
	}

	/**
	 *  Atualiza as marginais na arvore desejada.
	 *
	 * @param  arvore  uma <code>JTree</code> que representa a arvore a ser
	 *      atualizada
	 * @since
	 * @see            JTree
	 */
	public void updateTree(boolean reset) {

		if (expandedNodes == null) {
			expandedNodes = new boolean[net.getNodesCopy().size()];
			for (int i = 0; i < expandedNodes.length; i++) {
				expandedNodes[i] = true;
			}
		}

		DefaultMutableTreeNode root =
			(DefaultMutableTreeNode) getModel().getRoot();
		root.removeAllChildren();
		objectsMap.clear();
		DefaultTreeModel model =
			new DefaultTreeModel(
				(DefaultMutableTreeNode) net
					.getHierarchicTree()
					.copyTree()
					.getModel()
					.getRoot());
	
		this.setModel(model);
		
		root = (DefaultMutableTreeNode) getModel().getRoot();
		
		//by young
		root.removeAllChildren();
		
		ArrayList<Node> nodes = net.getNodesCopy();
		int size = nodes.size();
		for (int i = 0; i < size; i++) {
			Node node = (Node) nodes.get(i);
			TreeVariable treeVariable = (TreeVariable) node;
			DefaultMutableTreeNode treeNode = null;
			
			//by young
			if( getMode() == MODE_USE_NAME )
				treeNode = findUserObject(node.getName(), root);
			else
			if( getMode() == MODE_USE_DESC )
				treeNode = findUserObject(node.getDescription(), root);
			
			if (treeNode == null) 
			{
				if( getMode() == MODE_USE_NAME )
					treeNode = new DefaultMutableTreeNode(node.getName());
				else
				if( getMode() == MODE_USE_DESC )
					treeNode = new DefaultMutableTreeNode(node.getDescription());
				
			 
				root.add(treeNode);
			}
			
			objectsMap.put(treeNode, node);
			
			int statesSize = node.getStatesSize();
			
			System.out.println("new tree node "+ treeNode.toString() );
			
			for (int j = 0; j < statesSize; j++) {
				String label;
				if (treeVariable.getType() == Node.PROBABILISTIC_NODE_TYPE) {
					label =
						node.getStateAt(j)
							+ ": "
							+ nf.format(treeVariable.getMarginalAt(j) * 100.0);
				} else {
					label =
						node.getStateAt(j)
							+ ": "
							+ nf.format(treeVariable.getMarginalAt(j));
				}
				
				treeNode.add(new DefaultMutableTreeNode(label));
				
				System.out.println("new tree node sub "+ label );
			}
			 
		}
		restoreTree();
		((DefaultTreeModel) getModel()).reload(root);
		restoreTree();
		
		
		//by young
		netWindow.getGraphPane().compiled(reset, null);
	}
	
	//by young, modified
	public void selectTreeItemByNode(Node n) 
	{
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();
		DefaultMutableTreeNode treeNode = null;
		
		if( getMode() == MODE_USE_NAME )
			treeNode = findUserObject(n.getName(), root);
		else
		if( getMode() == MODE_USE_DESC )
			treeNode = findUserObject(n.getDescription(), root);
		
		if( treeNode != null )
		{
			TreePath tp = new TreePath(treeNode.getPath());
	        expandPath(tp);
			scrollPathToVisible(tp);
			setSelectionPath(tp);
		}
	}
	
	//by young 
	public void selectTreeItemByState(Node node, String stateName) 
	{
		DefaultMutableTreeNode parent = findUserObject(node);
		for (int i = 0; i < parent.getChildCount(); i++) 
		{
			DefaultMutableTreeNode auxNode = (DefaultMutableTreeNode) parent.getChildAt(i);
			
			int n = parent.getIndex(auxNode);
			if( node.getStateAt(n) == stateName )
			{
				treeDoubleClick( auxNode );
				return;
			}			
		}
	}
	

	/**
	 * Modifica o formato de numeros
	 *
	 * @param local localidade do formato de numeros.
	 */
	public void setNumberFormat(Locale local) {
		nf = NumberFormat.getInstance(local);
	}

	private DefaultMutableTreeNode findUserObject(Node node)
	{
		String name = null;
		if( getMode() == MODE_USE_NAME )
			name = node.getName();
		else
		if( getMode() == MODE_USE_DESC )
			name = node.getDescription();
		
		return findUserObject( name );
	}
	
	private DefaultMutableTreeNode findUserObject(String treeNode)
	{
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();
		return findUserObject( treeNode, root );
	}
	
	private DefaultMutableTreeNode findUserObject(
		String treeNode,
		DefaultMutableTreeNode root) {
		Enumeration e = root.breadthFirstEnumeration();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node =
				(DefaultMutableTreeNode) e.nextElement();
			if (node.getUserObject().toString().equals(treeNode)) {
				return node;
			}
		}
		return null;
	}

	/**
	 *  Adiciona uma evidencia no estado especificado.
	 *
	 * @param  caminho  caminho do estado a ser setado para 100%;
	 * @see             TreePath
	 */
	private void treeDoubleClick(DefaultMutableTreeNode treeNode) {
		DefaultMutableTreeNode parent =
			(DefaultMutableTreeNode) ((treeNode).getParent());
		Object obj = objectsMap.get((DefaultMutableTreeNode) parent);
		if (obj != null) {
			TreeVariable node = (TreeVariable) obj;

			//So propaga nos de descricao
			if (node.getInformationType() == Node.DESCRIPTION_TYPE) {
				for (int i = 0; i < parent.getChildCount(); i++) {
					DefaultMutableTreeNode auxNode =
						(DefaultMutableTreeNode) parent.getChildAt(i);
					auxNode.setUserObject(node.getStateAt(i) + ": 0");
				}

				if (node.getType() == Node.PROBABILISTIC_NODE_TYPE) {
					//by young2
					((ProbabilisticNode)node).setFinding(node.getStateAt(parent.getIndex(treeNode)));
					
					treeNode.setUserObject(						
						node.getStateAt(parent.getIndex(treeNode)) + ": 100");
				} else {
					treeNode.setUserObject(
						node.getStateAt(parent.getIndex(treeNode)) + ": **");
				}
				node.addFinding(parent.getIndex(treeNode));
				((DefaultTreeModel) getModel()).reload(parent);
				
				//by young
				netWindow.getGraphPane().selectNode(node);
			}
		}
	}

	/**
	 *  Abre uma nova janela modal para inserir os dados para serem usados no
	 *  likelihood.
	 *
	 * @param  caminho  um <code>TreePath <code>dizendo a posicao do mouse.
	 * @since
	 * @see             TreePath
	 */
	private void showLikelihood(DefaultMutableTreeNode node) {
		ProbabilisticNode auxVP = (ProbabilisticNode) objectsMap.get(node);
		if ((auxVP != null)
			&& (auxVP.getInformationType() == Node.DESCRIPTION_TYPE)) {
			int i;
			JPanel panel = new JPanel();
			JTable table = new JTable(auxVP.getStatesSize(), 2);
			for (i = 0; i < auxVP.getStatesSize(); i++) {
				table.setValueAt(auxVP.getStateAt(i), i, 0);
				table.setValueAt("100", i, 1);
			}
			JLabel label = new JLabel(auxVP.toString());
			panel.add(label);
			panel.add(table);
			if (JOptionPane
				.showConfirmDialog(
					this.netWindow.getDesktopPane(),
					panel,
					resource.getString("likelihoodName"),
					JOptionPane.OK_CANCEL_OPTION)
				== JOptionPane.OK_OPTION) {
				DefaultMutableTreeNode auxNode;

				float[] values = new float[auxVP.getStatesSize()];

				try {
					for (i = 0; i < auxVP.getStatesSize(); i++) {
						values[i] =
							nf
								.parse((String) table.getValueAt(i, 1))
								.floatValue();
					}
				} catch (ParseException e) {
					System.err.println(e.getMessage());
					return;
				}

				double maxValue = values[0];
				for (i = 1; i < auxVP.getStatesSize(); i++) {
					if (maxValue < values[i]) {
						maxValue = values[i];
					}
				}

				if (maxValue == 0.0) {
					System.err.println("likelihoodException");
					return;
				}

				for (i = 0; i < auxVP.getStatesSize(); i++) {
					values[i] /= maxValue;
				}

				for (i = 0; i < values.length && values[i] == 1; i++);
				if (i == values.length) {
					return;
				}

				String str;
				auxVP.addLikeliHood(values);
				for (i = 0; i < node.getChildCount(); i++) {
					auxNode = (DefaultMutableTreeNode) node.getChildAt(i);
					str = (String) auxNode.getUserObject();
					auxNode.setUserObject(
						str.substring(0, str.lastIndexOf(':') + 1)
							+ nf.format(values[i] * 100));
				}
				((DefaultTreeModel) getModel()).reload(node);
			}
		}
	}

	public Node getNodeMap(DefaultMutableTreeNode node) {
		Object obj = objectsMap.get(node);
		if (obj != null) {
			return (Node) obj;
		}
		return null;
	}
}