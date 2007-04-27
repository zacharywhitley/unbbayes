package unbbayes.prs.mebn.context;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.context.enumSubType;
import unbbayes.prs.mebn.context.enumType;


/**
 * Nodo of the tree of the formula. Have the information
 * about what type of things can replace it.  
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */

public class NodeFormulaTree{
	
	private String name; 
	private String mnemonic; 
	private enumType type;
	private enumSubType subType; 
	private Object nodeVariable; 
	private ArrayList<NodeFormulaTree> children; 
	
	/**
	 * create a new node formula tree
	 * @param typeNode type of the node (what go to fill its)
	 * @param nodeVariable object that fill the node 
	 */
	public NodeFormulaTree(String _name, enumType _type, enumSubType _subType, Object _nodeVariable){
		name = _name; 
		type = _type; 
		subType = _subType; 
		nodeVariable = _nodeVariable;
		children = new ArrayList<NodeFormulaTree>(); 
	}
	
	public void addChild(NodeFormulaTree child){
		children.add(child); 
	}
	
	public void removeChild(NodeFormulaTree child){
		children.remove(child); 
	}
	
	public void removeAllChildren(){
		children.clear(); 
	}
	
	public List<NodeFormulaTree> getChildren(){
		return this.children; 
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
	
	public String getMnemonic() {
		return mnemonic;
	}
	
	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}
	
	/**
	 * Método utilizado para se obter a forma textual da fórmula 
	 * representada por este nó. Funciona recursivamente, chamando
	 * o mesmo método para obter as formulas dos operandos. 
	 */
	public String getFormulaViewText(){
		
		switch(type){
		
		case OPERANDO:
			return name; 
		
		case SIMPLE_OPERATOR: 
			
			if(children.size() == 2){
				return "( " + children.get(0).getFormulaViewText() + " " + mnemonic + " " + children.get(1).getFormulaViewText() + " )" ;
			}
			else{
				if(children.size() == 1){
					return "( " + mnemonic + " " + children.get(0).getFormulaViewText() + " )"; 
				}
				else return " "; 
			}
			
		case QUANTIFIER_OPERATOR:
			
			String formula = ""; 
			
			formula+= "( " + mnemonic; 
			
			// exemplar list
			
			formula+= "( "; 
			NodeFormulaTree exemplarList = children.get(0); 
			int exemplarListSize = exemplarList.getChildren().size(); 
			for(int i = 0; i< exemplarListSize; i++){
				formula+= exemplarList.getChildren().get(i).getName();
				if(i < exemplarListSize - 1) formula+= ","; 
			}
			formula+= " )"; 
			
			// formula 
			
			formula+= children.get(1).getFormulaViewText(); 
			
			formula+= ")"; 
			
			return formula;
			
		default: 
			return " "; 
		
		}
	}	
	
}