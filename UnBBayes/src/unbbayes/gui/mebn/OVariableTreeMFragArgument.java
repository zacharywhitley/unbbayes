package unbbayes.gui.mebn;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import unbbayes.controller.NetworkController;
import unbbayes.prs.mebn.OrdinaryVariable;

/**
 * Arvore de variaveis ordinarias utilizada para que o usuario adicione
 * variaveis ordinarias como argumentos em um resident node. 
 * A unica acao que o usuario pode fazer em um no da arvore é clicar duas 
 * vezes para que este seja adicionado como argumento no residente. 
 * 
 * @author Laecio
 *
 */
public class OVariableTreeMFragArgument extends OVariableTreeMFrag{

    private OrdinaryVariable oVariableSelected = null; 	
	
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
						oVariableSelected = null; 
					} else if (e.getClickCount() == 2
							&& e.getModifiers() == MouseEvent.BUTTON1_MASK) {
						
						controller.getMebnController().addOrdinaryVariableInResident(ordinaryVariable); 
						oVariableSelected = null; 
						
					} else if (e.getClickCount() == 1) {
						
						oVariableSelected = ordinaryVariable; 
					}
				} 
				else {
					oVariableSelected = null; 
				}
			}
		}); 
	}
	
	public OrdinaryVariable getOVariableSelected(){
		return oVariableSelected; 
	}	
	
	
}
