package unbbayes.gui.mebn.formula;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import unbbayes.controller.IconController;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.context.enumSubType;
import unbbayes.prs.mebn.context.enumType;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;

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
		
		enumType type = nodeFormula.getTypeNode(); 
		enumSubType subType = nodeFormula.getSubTypeNode();
		
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
			
			
		case OPERANDO:
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
