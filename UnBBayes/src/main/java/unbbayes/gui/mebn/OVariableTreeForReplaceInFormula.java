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

import unbbayes.controller.FormulaTreeController;
import unbbayes.controller.MEBNController;
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
	private FormulaTreeController formulaTreeController; 
	
	public OVariableTreeForReplaceInFormula(final MEBNController controller, FormulaTreeController _formulaTreeController){
		super(controller); 
		formulaTreeController = _formulaTreeController; 
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
						try{
						   formulaTreeController.addOVariable(ordinaryVariable); 
						   controller.updateFormulaActiveContextNode(); 
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

	
	
}
