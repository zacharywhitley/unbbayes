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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import unbbayes.controller.mebn.MEBNController;
import unbbayes.prs.mebn.OrdinaryVariable;

/**
 * Tree that add to the tree of ordinary variables the 
 * action of selection of one variable.
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */

public class OVariableTreeForOVariableEdition extends OVariableTree{
	
	private OrdinaryVariable oVariableActive; 
	
	public OVariableTreeForOVariableEdition(final MEBNController controller){
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
						
						controller.selectOVariableInEdit(ordinaryVariable); 
						oVariableActive = ordinaryVariable; 						
						
						
					} else if (e.getClickCount() == 1) {
						
						controller.selectOVariableInEdit(ordinaryVariable); 
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
