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
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.util.ArrayMap;

public class OVariableTreeResident extends JTree{


    private MultiEntityBayesianNetwork net;
    
	private ArrayMap<Object, OrdinaryVariable> ordinaryVariableMap = new ArrayMap<Object, OrdinaryVariable>();
    private List<OrdinaryVariable> ordinaryVariableList = new ArrayList<OrdinaryVariable>();
    private ResidentNode residentNodeActive; 
	
	protected IconController iconController = IconController.getInstance();
	
	private final NetworkController controller;	
	
	public OVariableTreeResident(final NetworkController controller, ResidentNode resident) {
		
		this.controller = controller; 
		this.net = (MultiEntityBayesianNetwork)controller.getNetwork();
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
						
					} else if (e.getClickCount() == 2
							&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
						
						controller.getMebnController().removeOrdinaryVariableInResident(ordinaryVariable); 
						
					} else if (e.getClickCount() == 1) {
						
					}
				} 
				else {
					//Never...
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
		 * Atualiza as marginais na árvore desejada.
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
			 
			 // Só propaga nós de descrição
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




	
	

