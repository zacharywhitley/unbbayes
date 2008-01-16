package unbbayes.gui.mebn;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import unbbayes.controller.MEBNController;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;

/**
 * Arvore de variaveis ordinarias utilizada para que o usuario adicione
 * variaveis ordinarias como argumentos em um resident node. 
 * A unica acao que o usuario pode fazer em um no da arvore � clicar duas 
 * vezes para que este seja adicionado como argumento no residente. 
 * 
 * @author Laecio
 *
 */
public class OVariableTreeForArgumentEdition extends OVariableTree{

    private OrdinaryVariable oVariableSelected = null; 	
	private MEBNController mebnController; 
    
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.resources.GuiResources");
    	
	
	public OVariableTreeForArgumentEdition(final MEBNController controller){
		super(controller); 
		mebnController = controller; 
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
					
					try{	
						controller.addOrdinaryVariableInResident(ordinaryVariable);
					}
					catch(OVariableAlreadyExistsInArgumentList e1){
							JOptionPane.showMessageDialog(null, resource.getString("oVariableAlreadyIsArgumentError"), resource.getString("operationError"), JOptionPane.ERROR_MESSAGE);
					}
					catch(ArgumentNodeAlreadySetException e2){
						e2.printStackTrace(); 
					}
						oVariableSelected = null; 
						
					} else if (e.getClickCount() == 1) {
						
						oVariableSelected = ordinaryVariable;
						mebnController.setOVariableSelectedInMFragTree(oVariableSelected); 
						
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
