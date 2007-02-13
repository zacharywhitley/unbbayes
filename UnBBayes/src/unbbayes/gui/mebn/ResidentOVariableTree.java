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
import unbbayes.controller.MEBNController;
import unbbayes.controller.NetworkController;
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
    private ResidentNode residentNodeActive; 
	
    private OrdinaryVariable oVariableSelected = null; 
    
    
	protected IconController iconController = IconController.getInstance();
	
	private final MEBNController mebnController; 
	
	public ResidentOVariableTree(final NetworkController controller, ResidentNode resident) {
		
		this.mebnController = controller.getMebnController(); 
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
		
	}