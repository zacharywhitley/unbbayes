package unbbayes.gui.mebn;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import unbbayes.controller.NetworkController;
import unbbayes.prs.mebn.OrdinaryVariable;

/**
 * Tree that add to the tree of ordinary variables the 
 * action of selection of one variable.
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */

public class OVariableTreeForOVariableEdition extends OVariableTree{
	
	private OrdinaryVariable oVariableActive; 
	
	public OVariableTreeForOVariableEdition(final NetworkController controller){
		super(controller); 
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
						
					} else if (e.getClickCount() == 2
							&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
						
						controller.getMebnController().selectOVariableInEdit(ordinaryVariable); 
						oVariableActive = ordinaryVariable; 						
						
						
					} else if (e.getClickCount() == 1) {
						
						controller.getMebnController().selectOVariableInEdit(ordinaryVariable); 
						oVariableActive = ordinaryVariable; 
					}
				} 
				else {
					//Never...
				}
			}
		}); 
	}
	
	public OrdinaryVariable getOVariableActive(){
		return oVariableActive; 
	}

	public void setOVariableActive(OrdinaryVariable ov){
		oVariableActive = ov; 
	}
	
}
