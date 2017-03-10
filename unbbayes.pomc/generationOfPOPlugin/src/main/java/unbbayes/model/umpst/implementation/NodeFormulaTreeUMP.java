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

import unbbayes.prs.mebn.context.EnumSubType;
import unbbayes.prs.mebn.context.EnumType;
import unbbayes.prs.mebn.context.NodeFormulaTree;


/**
 * Node of the tree of the formula. Have the information
 * about what type of things can replace it.  
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */

public class NodeFormulaTreeUMP extends NodeFormulaTree{
	
	private String name; 
	private String mnemonic; 
	private EnumType type;
	private EnumSubType subType; 
	private Object nodeVariable; 
	private ArrayList<NodeFormulaTreeUMP> children; 
	
	/**
	 * create a new node formula tree
	 * @param typeNode type of the node (what go to fill its)
	 * @param nodeVariable object that fill the node 
	 */
	public NodeFormulaTreeUMP(String _name, EnumType _type, EnumSubType _subType, Object _nodeVariable){
		super(_name, _type, _subType, _nodeVariable);
		name = _name; 
		type = _type; 
		subType = _subType; 
		nodeVariable = _nodeVariable;
		setChildrenUMP(new ArrayList<NodeFormulaTreeUMP>()); 
	}
	
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
	
	public void addChild(NodeFormulaTreeUMP child){
		getChildrenUMP().add(child); 
	}
	
	public void removeChild(NodeFormulaTreeUMP child){
		getChildrenUMP().remove(child); 
	}
	
	public void removeAllChildren(){
		getChildrenUMP().clear(); 
	}
	
	public Object getNodeVariable(){
		return nodeVariable; 
	}
	
	public void setNodeVariable(Object nodeVariable){
		this.nodeVariable = nodeVariable; 
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
	
	public String getMnemonic() {
		return mnemonic;
	}
	
	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}
	
//	public NodeFormulaTree mapFormulaToMEBN() {
//		switch(type){
//		case OPERAND:
//			switch(subType){
//			
//			case NODE:
//				EventNCPointer eventPointer = (EventNCPointer)nodeVariable;
//				eventPointer.getEventVariable().get
//				
//				/*TODO convert this event to resident node*/
////				this.setNodeVariable(eventPointer);
//				return this;
//			}
//		}
//	}
	
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
			if(getChildrenUMP().size() == 2){
				return "( " + getChildrenUMP().get(0).getFormulaViewText() + " " + mnemonic + " " + getChildrenUMP().get(1).getFormulaViewText() + " )" ;
			}
			else{
				if(getChildrenUMP().size() == 1){
					return "( " + mnemonic + " " + getChildrenUMP().get(0).getFormulaViewText() + " )"; 
				}
				else return " "; 
			}
			
		case QUANTIFIER_OPERATOR:
			
			String formula = ""; 
			
			formula+= "( " + mnemonic; 
			
			// exemplar list
			
			formula+= "( "; 
			NodeFormulaTreeUMP exemplarList = getChildrenUMP().get(0); 
			int exemplarListSize = exemplarList.getChildrenUMP().size(); 
			for(int i = 0; i< exemplarListSize; i++){
				formula+= exemplarList.getChildrenUMP().get(i).getName();
				if(i < exemplarListSize - 1) formula+= ","; 
			}
			formula+= " )"; 
			
			// formula 
			
			formula+= getChildrenUMP().get(1).getFormulaViewText(); 
			
			formula+= ")"; 
			
			return formula;
			
		default: 
			return " "; 
		
		}
	}

	/**
	 * @return the children
	 */
	public ArrayList<NodeFormulaTreeUMP> getChildrenUMP() {
		return children;
	}

	/**
	 * @param children the children to set
	 */
	public void setChildrenUMP(ArrayList<NodeFormulaTreeUMP> children) {
		this.children = children;
	}	
	
}