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
package unbbayes.model.umpst.implementation;

import java.util.ArrayList;
import java.util.List;


/**
 * Node of the tree of the formula. Have the information
 * about what type of things can replace it.  
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */

public class NodeFormulaTree{
	
	private String name; 
	private String mnemonic; 
	private EnumType type;
	private EnumSubType subType; 
	private Object nodeVariable; 
	private ArrayList<NodeFormulaTree> children; 
	
	/**
	 * create a new node formula tree
	 * @param typeNode type of the node (what go to fill its)
	 * @param nodeVariable object that fill the node 
	 */
	public NodeFormulaTree(String _name, EnumType _type, EnumSubType _subType, Object _nodeVariable){
		name = _name; 
		type = _type; 
		subType = _subType; 
		nodeVariable = _nodeVariable;
		children = new ArrayList<NodeFormulaTree>(); 
	}
	
//	public Set<OrdinaryVariable> getVariableList(){
//		
//		Set<OrdinaryVariable> set = new HashSet<OrdinaryVariable>(); 
//		
//		switch(type){
//		
//		case OPERAND:
//			switch(subType){
//			
//			case OVARIABLE:
//				set.add((OrdinaryVariable)this.getNodeVariable());
//				break;
//				
//			case NODE:
//				ResidentNodePointer node = (ResidentNodePointer)this.getNodeVariable(); 
//				set.addAll(node.getOrdinaryVariableList()); 
//			    break; 
//			}
//			
//			break;
//		
//		}
//		
//		for(NodeFormulaTree child: this.getChildren()){
//			set.addAll(child.getVariableList()); 
//		}
//		
//		return set; 
//	}
//	
//	public Set<OrdinaryVariable> getExemplarList(){
//		
//		return null; 
//	}
	
	/*
	 * Formulas validas: 
	 * 
	 * - Todas as formulas da lógica de primeira ordem desde que as variáveis 
	 *  estejam devidamente preenchidas. 
	 *  - Para formulas com váriaveis livres, apenas as no formato: 
	 *        Node(arguments) = Entity, 
	 *    onde a lista dos argumentos é corretamente informada. (uma pesquisa 
	 *    deverá retornar as entities que podem ser resultado desta fórmula).      
	 */
	public boolean isFormulaValida(){
		return true; 
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
	
	public EnumType getTypeNode(){
		return type; 
	}
	
	public void setTypeNode(EnumType _type){
		this.type = _type; 
	}
	
	public EnumSubType getSubTypeNode(){
		return subType; 
	}
	
	public void setSubTypeNode(EnumSubType _subType){
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
	 * M�todo utilizado para se obter a forma textual da f�rmula 
	 * representada por este n�. Funciona recursivamente, chamando
	 * o mesmo m�todo para obter as formulas dos operandos. 
	 */
	public String getFormulaViewText(){
		switch(type){
		case OPERAND:
			switch(subType){
			
			case NODE:
				EventNCPointer eventPointer = (EventNCPointer)nodeVariable;
//				System.out.println(eventPointer.getOvArgumentList().size());
//				ResidentNodePointer pointer = (ResidentNodePointer)nodeVariable;
//				String returnName = name + "(";
//				int numberAguments = pointer.getNumberArguments(); 
//				for(int i = 0; i < numberAguments - 1; i++){
//					if(pointer.getArgument(i) != null){
//					   returnName+= pointer.getArgument(i).getName();
//					}
//					returnName+= ","; 
//				}
//				if(numberAguments > 0){
//					if(pointer.getArgument(numberAguments - 1) != null){		
//					    returnName+= pointer.getArgument(numberAguments - 1).getName(); // without ","
//					}
//				}
//				return returnName + ")"; 
				return name;
	
			default: 
				return name; 
			}
			
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