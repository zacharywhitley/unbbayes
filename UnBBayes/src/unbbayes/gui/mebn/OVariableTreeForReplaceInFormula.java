package unbbayes.gui.mebn;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import unbbayes.controller.NetworkController;
import unbbayes.gui.mebn.formula.FormulaTree;
import unbbayes.prs.mebn.OrdinaryVariable;

//TODO aplicar pattern... 

/**
 * Tree for the user selected what o variable he
 * want for substitute the atual place selected of
 * the formula tree. 
 * 
 * @author Laecio
 *
 */
public class OVariableTreeForReplaceInFormula extends OVariableTree{

	private OrdinaryVariable oVariableActive; 	
	private FormulaTree formulaTree; 
	
	public OVariableTreeForReplaceInFormula(final NetworkController controller, FormulaTree _formulaTree){
		super(controller); 
		formulaTree = _formulaTree; 
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

				OrdinaryVariable ordinaryVariable = ordinaryVariableMap.get(node); 				
				
				if (node.isLeaf() && (ordinaryVariable != null)) {
					

					if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
						//Nothing
					} else if (e.getClickCount() == 2
							&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
						
						formulaTree.addOVariable(ordinaryVariable); 
						controller.getMebnController().updateFormulaActiveContextNode(); 
						
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
	
}
