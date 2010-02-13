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
package unbbayes.gui.mebn.formula;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import unbbayes.controller.IconController;
import unbbayes.prs.mebn.context.EnumSubType;
import unbbayes.prs.mebn.context.EnumType;
import unbbayes.prs.mebn.context.NodeFormulaTree;

public class FormulaTreeCellRenderer extends DefaultTreeCellRenderer {
	
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;
	
	protected IconController iconController = IconController.getInstance();
	
	protected ImageIcon folderSmallIcon = iconController.getOpenIcon(); 
	
	protected ImageIcon andIcon = iconController.getAndIcon(); 
	
	protected ImageIcon orIcon = iconController.getOrIcon(); 
	
	protected ImageIcon notIcon = iconController.getNotIcon(); 
	
	protected ImageIcon equalIcon = iconController.getEqualIcon(); 
	
	protected ImageIcon impliesIcon = iconController.getImpliesIcon(); 
	
	protected ImageIcon forallIcon = iconController.getForallIcon(); 
	
	protected ImageIcon existsIcon = iconController.getExistsIcon(); 
	
	protected ImageIcon iffIcon = iconController.getIffIcon(); 
	
	protected ImageIcon entityNodeIcon = iconController.getEntityInstanceIcon(); 
	
	protected ImageIcon stateIcon = iconController.getStateIcon(); 
	
	protected ImageIcon ovariableNodeIcon = iconController.getOVariableNodeIcon(); 
	
	protected ImageIcon nodeNodeIcon = iconController.getNodeNodeIcon(); 
	
	protected ImageIcon skolenNodeIcon = iconController.getSkolenNodeIcon(); 
	
	protected ImageIcon emptyNodeIcon = iconController.getEmptyNodeIcon(); 	        
	
	protected ImageIcon yellowBallIcon = iconController.getYellowBallIcon(); 
	
	protected ImageIcon orangeBallIcon = iconController.getOrangeNodeIcon(); 
	
	protected ImageIcon hierarchyBallIcon = iconController.getHierarchyIcon(); 
	
	protected ImageIcon boxVariablesIcon = iconController.getBoxVariablesIcon(); 
	
	public FormulaTreeCellRenderer(){
		super(); 
		
	}
	
	
	/**
	 * Return a tree cell for the object value. 
	 */
	
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		
		super.getTreeCellRendererComponent(tree, value, sel, expanded,
				leaf, row, hasFocus);
		
		if(value == null){
			return this; 
		}			
		
		NodeFormulaTree nodeFormula = (NodeFormulaTree)(((DefaultMutableTreeNode)value).getUserObject());
		
		EnumType type = nodeFormula.getTypeNode(); 
		EnumSubType subType = nodeFormula.getSubTypeNode();
		
		switch(type){
		
		case EMPTY: 
			setIcon(emptyNodeIcon);
			return this; 
			
			
		case FORMULA: 
			setIcon(hierarchyBallIcon);
			return this; 
			
		case SIMPLE_OPERATOR: 
			
			switch(subType){
			
			case AND:
				setIcon(andIcon); 
				return this; 
				
			case OR: 
				setIcon(orIcon); 
				return this; 
				
			case NOT: 
				setIcon(notIcon); 
				return this; 
				
			case EQUALTO:
				setIcon(equalIcon); 
				return this; 					
				
			case IMPLIES:
				setIcon(impliesIcon); 
				return this; 					
				
			case IFF:
				setIcon(iffIcon); 
				return this; 					
				
			default:
				return this; 
			
			}
		case QUANTIFIER_OPERATOR: 
			switch(subType){
			case FORALL:
				setIcon(forallIcon); 
				return this; 					
				
			case EXISTS: 	
				setIcon(existsIcon); 
				return this; 				
				
			default:
				return this; 
			
			}				
			
			
		case VARIABLE_SEQUENCE:
			setIcon(boxVariablesIcon);
			return this; 
			
			
		case OPERAND:
			switch(subType){
			
			case OVARIABLE:
				setIcon(ovariableNodeIcon); 
				return this; 
				
			case NODE: 
				setIcon(nodeNodeIcon); 
				return this; 
				
			case ENTITY: 
				setIcon(stateIcon); 
				return this; 
				
			case VARIABLE: 
				setIcon(orangeBallIcon); 
				return this; 	
				
				/*	
				 case SKOLEN:
				 setIcon(equalIcon); 
				 return this; 						
				 */	
				
			default:
				return this; 
			
			}
			
		case VARIABLE:
			setIcon(orangeBallIcon); 
			return this; 
			
		default: 	
			setIcon(folderSmallIcon);
		return this; 
		
		}
		
	}
	
	
}
