package unbbayes.gui.mebn;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import unbbayes.controller.NetworkController;
import unbbayes.prs.mebn.OrdinaryVariable;

public class OVariableTreeMFragArgument extends OVariableTreeMFrag{

	public OVariableTreeMFragArgument(final NetworkController controller){
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
				
				if (node.isLeaf()) {
					
					OrdinaryVariable ordinaryVariable = ordinaryVariableMap.get(node); 
					
					if (e.getModifiers() == MouseEvent.BUTTON3_MASK) {
						
					} else if (e.getClickCount() == 2
							&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
						
						controller.getMebnController().addOrdinaryVariableInResident(ordinaryVariable); 
						
					} else if (e.getClickCount() == 1) {
						
						
					}
				} 
				else {
					//Never...
				}
			}
		}); 
	}
	
	
}
