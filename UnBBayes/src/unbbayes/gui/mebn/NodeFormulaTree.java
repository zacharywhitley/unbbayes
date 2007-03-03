package unbbayes.gui.mebn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.gui.mebn.formula.enumSubType;
import unbbayes.gui.mebn.formula.enumType;


/**
 * Nodo of the tree of the formula. Have the information
 * about what type of things can replace it.  
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */

public class NodeFormulaTree{
	
	String name; 
	enumType type;
	enumSubType subType; 
	Object nodeVariable; 
	ArrayList<NodeFormulaTree> childList; 
	
	/**
	 * create a new nodo formula tree
	 * @param typeNode type of the node (what go to fill its)
	 * @param nodeVariable object that fill the node 
	 */
	
	public NodeFormulaTree(String _name, enumType _type, enumSubType _subType, Object _nodeVariable){
		name = _name; 
		type = _type; 
		subType = _subType; 
		nodeVariable = _nodeVariable;
		childList = new ArrayList<NodeFormulaTree>(); 
	}
	
	public void addChildren(NodeFormulaTree children){
		childList.add(children); 
	}
	
	public void removeChildren(NodeFormulaTree children){
		childList.remove(children); 
	}
	
	public List<NodeFormulaTree> getChildrenList(){
		return this.childList; 
	}
	
	public String toString(){
		return name; 
	}
	
	public String getName(){
		return name; 
	}
	
	public void setName(String name){
		this.name = name; 
	}
	
	public enumType getTypeNode(){
		return type; 
	}
	
	public void setTypeNode(enumType _type){
		this.type = _type; 
	}

	public enumSubType getSubTypeNode(){
		return subType; 
	}
	
	public void setSubTypeNode(enumSubType _subType){
		this.subType = _subType; 
	}
	
				
	
	
	public Object getNodeVariable(){
		return nodeVariable; 
	}
	
	public void setNodeVariable(Object nodeVariable){
		this.nodeVariable = nodeVariable; 
	}
}