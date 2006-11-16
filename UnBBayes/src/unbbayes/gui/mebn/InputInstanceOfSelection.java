package unbbayes.gui.mebn;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.util.ArrayMap;

/**
 * Apresenta o painel para o usuario escolher de qual nodo o input
 * ativo � instancia (apresenta uma arvore com todas as possibilidade, 
 * agrupadas por tipo: Resident e BuiltIn,...).  
 * 
 * @author Laecio
 * @version 0.1 (05/11/2006)
 */

public class InputInstanceOfSelection extends JTree{
	
	
	private MultiEntityBayesianNetwork net;
	
	private boolean[] expandedNodes;
	
	private ArrayMap<Object, ResidentNode> residentNodeMap = new ArrayMap<Object, ResidentNode>();
	private ArrayMap<Object, MFrag> mFragMap = new ArrayMap<Object, MFrag>(); 
	private ArrayMap<Object, Object> nodeMap = new ArrayMap<Object, Object>(); 
	
	protected IconController iconController = IconController.getInstance();
	
	private final NetworkController controller;	
	
	public InputInstanceOfSelection(final NetworkController controller) {
		
		this.controller = controller; 
		this.net = (MultiEntityBayesianNetwork)controller.getNetwork();
		
		// set up node icons
		setCellRenderer(new InputInstanceTreeCellRenderer());
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(net.getName());
		DefaultTreeModel model = new DefaultTreeModel(root);
		
		setModel(model);
		
		createTree();	    
		
		/* tratar os eventos de mouse, trocando o residente a qual o input
		 * se refere. 
		 */
		
		
		
		
		
		/*------------------ Adicionar listeners -----------------------*/
		
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
					
					ResidentNode residentNode = residentNodeMap.get(node); 
					
					if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
						
					} else if (e.getClickCount() == 2
							&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
						//TODO preencher o generativeInput... 
						GenerativeInputNode inputNode = (GenerativeInputNode)controller.getScreen().getGraphPane().getSelected(); 
						controller.getMebnController().setInputInstanceOf(inputNode, residentNode); 
						
					} else if (e.getClickCount() == 1) {
						
					}
				} 
				else {
				   if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
					
					
					
				   }
				   if (e.getClickCount() == 1) {
					
					
				  } else if (e.getClickCount() == 2) {
				  	DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel()
					.getRoot();
				  	int index = root.getIndex(node);
					expandedNodes[index] = !expandedNodes[index];
			 	}
			}
			}
		
	});		
		
		super.treeDidChange();
		expandTree();
	}
	
	
	private void createTree() {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
		
		List<DomainMFrag> domainMFragList = net.getDomainMFragList(); 
		
		for (DomainMFrag domainMFrag : domainMFragList){
			
			DefaultMutableTreeNode mFragTreeNode = new DefaultMutableTreeNode(domainMFrag.getName()); 
			mFragMap.put(mFragTreeNode, domainMFrag); 
			nodeMap.put(mFragTreeNode, domainMFrag); 
			
			root.add(mFragTreeNode); 
			
			List<DomainResidentNode> residentNodeList = domainMFrag.getDomainResidentNodeList(); 
			
			for (DomainResidentNode domainResidentNode : residentNodeList) {
				DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(domainResidentNode.getName());
				mFragTreeNode.add(treeNode);
				residentNodeMap.put(treeNode, domainResidentNode);
				nodeMap.put(treeNode, domainResidentNode); 				
			}
		}
		
		expandedNodes = new boolean[net.getMFragCount()];
	}
	
	private class InputInstanceTreeCellRenderer extends DefaultTreeCellRenderer {
		
		/** Serialization runtime version number */
		private static final long serialVersionUID = 0;

		private ImageIcon yellowNodeIcon = iconController.getYellowNodeIcon();
		private ImageIcon greenNodeIcon = iconController.getGreenNodeIcon(); 
		private ImageIcon blueNodeIcon = iconController.getBlueNodeIcon(); 
		
		private ImageIcon orangeNodeIcon = iconController.getOrangeNodeIcon(); 
		private ImageIcon mTheoryNodeIcon = iconController.getMTheoryNodeIcon(); 
		
		/**
		 * Return a tree cell for the object value. 
		 */
		
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);
			
			Object obj = nodeMap.get((DefaultMutableTreeNode) value);
			
			
			if (leaf) {
				/*DefaultMutableTreeNode parent = (DefaultMutableTreeNode) (((DefaultMutableTreeNode) value)
						.getParent());
				Object obj = mFragMap.get((DefaultMutableTreeNode) parent);*/

				if (obj != null) {
					
					if (obj instanceof ResidentNode){ 
						setIcon(greenNodeIcon);
						}
										
					else{
						if (obj instanceof InputNode){
							setIcon(blueNodeIcon); 
						}
						else{ 
							if (obj instanceof ContextNode){
						       setIcon(yellowNodeIcon);
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
		
		/**
		 * Retrai todos os n�s da �rvore desejada.
		 * 
		 * @param arvore
		 *            uma <code>JTree</code> que representa a rede Bayesiana em
		 *            forma de �rvore.
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
		 * Expande todos os n�s da �rvore desejada.
		 * 
		 * @param arvore
		 *            uma <code>JTree</code> que representa a rede Bayesiana em
		 *            forma de �rvore.
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
		 * Atualiza as marginais na �rvore desejada.
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
			
			List<DomainMFrag> domainMFragList = net.getDomainMFragList(); 
			
			for (DomainMFrag domainMFrag : domainMFragList){
				
				DefaultMutableTreeNode mFragTreeNode = new DefaultMutableTreeNode(domainMFrag.getName()); 
				mFragMap.put(mFragTreeNode, domainMFrag); 
				nodeMap.put(mFragTreeNode, domainMFrag); 
				
				root.add(mFragTreeNode); 
				
				List<DomainResidentNode> residentNodeList = domainMFrag.getDomainResidentNodeList(); 
				
				for (DomainResidentNode domainResidentNode : residentNodeList) {
					DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(domainResidentNode.getName());
					mFragTreeNode.add(treeNode);
					residentNodeMap.put(treeNode, domainResidentNode);
					nodeMap.put(treeNode, domainResidentNode); 				
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
		
		/**
		 * Adiciona uma evidencia no estado especificado.
		 * 
		 * @param caminho
		 *            caminho do estado a ser setado para 100%;
		 * @see TreePath
		 */
		private void treeDoubleClick(DefaultMutableTreeNode treeNode) {
			/*DefaultMutableTreeNode parent = (DefaultMutableTreeNode) ((treeNode)
			 .getParent());
			 Object obj = nodeMap.get((DefaultMutableTreeNode) parent);
			 if (obj != null) {
			 TreeVariable node = (TreeVariable) obj;
			 
			 // S� propaga n�s de descri��o
			  if (node.getInformationType() == Node.DESCRIPTION_TYPE) {
			  for (int i = 0; i < parent.getChildCount(); i++) {
			  DefaultMutableTreeNode auxNode = (DefaultMutableTreeNode) parent
			  .getChildAt(i);
			  auxNode.setUserObject(node.getStateAt(i) + ": 0");
			  }
			  
			  if (node.getType() == Node.PROBABILISTIC_NODE_TYPE) {
			  treeNode.setUserObject(node.getStateAt(parent
			  .getIndex(treeNode))
			  + ": 100");
			  } else {
			  treeNode.setUserObject(node.getStateAt(parent
			  .getIndex(treeNode))
			  + ": **");
			  }
			  node.addFinding(parent.getIndex(treeNode));
			  ((DefaultTreeModel) getModel()).reload(parent);
			  }
			  }*/
		}
		
		
	}
