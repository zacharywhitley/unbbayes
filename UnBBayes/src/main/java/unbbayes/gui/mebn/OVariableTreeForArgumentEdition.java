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
 * Tree of ordinary variables for edit the list of arguments of a resident node. 
 * When the user click 2 times in a node, this node is add to the list of arguments. 
 * 
 * @author Laecio
 */
public class OVariableTreeForArgumentEdition extends OVariableTree{

    private OrdinaryVariable oVariableSelected = null; 	
	private MEBNController mebnController; 
    
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle(
  			unbbayes.gui.resources.GuiResources.class.getName());
    	
	
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
