package unbbayes.gui.umpst.implementation;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import unbbayes.controller.umpst.FormulaTreeControllerUMP;
import unbbayes.model.umpst.implementation.OrdinaryVariableModel;
import unbbayes.model.umpst.rule.RuleModel;
import unbbayes.util.ArrayMap;

public class OVTreeForReplaceInFormula extends JTree {
	
	private RuleModel rule;
	private FormulaEditionPane formulaEditionPane;
	private FormulaTreeControllerUMP formulaTreeController;
	
	private ArrayMap<Object, Object> ordinaryVariableMap = new ArrayMap<Object, Object>();
		
	private DefaultMutableTreeNode root;
	private DefaultTreeModel model;
	
	public OVTreeForReplaceInFormula(RuleModel rule, FormulaEditionPane 
			formulaEditionPane) {
		super();
		this.rule = rule;
		this.formulaEditionPane = formulaEditionPane;
		
		formulaTreeController = formulaEditionPane.getFormulaTreeController();
		
		root = new DefaultMutableTreeNode("Rule " + rule.getId());
		createOVTree();
		
		model = new DefaultTreeModel(root);
		setModel(model);
		
		addListeners();
		super.treeDidChange();
	}
	
	public void addListeners(){
		addMouseListener(new MouseAdapter(){			
			public void mousePressed(MouseEvent e) {				
				int selRow = getRowForLocation(e.getX(), e.getY());
				if (selRow == -1) {
					return;
				}
				
				TreePath selPath = getPathForLocation(e.getX(), e.getY());
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath
				.getLastPathComponent();

				Object ovId = ordinaryVariableMap.get(node);		
				
				if (node.isLeaf() && (ovId != null)) {
					if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
						//Nothing
					} else if (e.getClickCount() == 2
							&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
						try{
							formulaTreeController.addOVariable(
									getOrdinaryVariable(ovId.toString()));	
						   formulaTreeController.updateFormulaActiveContextNode(); 
						}
						catch(Exception ex){
							//TODO colocar dialogo de erro. 
							ex.printStackTrace(); 
						}
					} else if (e.getClickCount() == 1) {
						//Nothing
					}
				} 
				else {
					//Never...
				}
			}
		}); 
	}
	
	public OrdinaryVariableModel getOrdinaryVariable(String id) {		
		for (int i = 0; i < rule.getOrdinaryVariableList().size(); i++) {
			if (id.equals(rule.getOrdinaryVariableList().get(i).getId())) {
				return rule.getOrdinaryVariableList().get(i);
			}
		}
		return null;
	}
	
	public void createOVTree() {
		root.removeAllChildren();
		
		for (int i = 0; i < rule.getOrdinaryVariableList().size(); i++) {
			OrdinaryVariableModel ov = rule.getOrdinaryVariableList().get(i);
			
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(ov.getVariable() 
					+ " (" + ov.getTypeEntity() + ")");
			root.add(node);
			ordinaryVariableMap.put(node, ov.getId());
		}
		updateTree();
	}
	
	/**
	 * Rebuild the tree.
	 */
	public void updateTree() {		
		for (int i = 0; i < getRowCount(); i++){
			expandRow(i);
		}
	}
}
