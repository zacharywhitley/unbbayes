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
package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityConteiner;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;

/**
 *
 */

public class ResidentNode extends MultiEntityNode implements ITabledVariable {
	
	private static final long serialVersionUID = 8497908054569004909L;

	private List<OrdinaryVariable> ordinaryVariableList; 
	
	private List<ResidentNodePointer> listPointers; 

	public static final int OBJECT_ENTITY = 0; 
	public static final int CATEGORY_RV_STATES = 1; 
	public static final int BOOLEAN_RV_STATES = 2; 
	private int typeOfStates = CATEGORY_RV_STATES; 
	
	private int numNextArgument = 0; 
	
	public ResidentNode(){
		
		super(); 
		listPointers = new ArrayList<ResidentNodePointer>(); 
		ordinaryVariableList = new ArrayList<OrdinaryVariable>(); 
		
	}
	
	public void addResidentNodePointer(ResidentNodePointer pointer){
		listPointers.add(pointer); 
	}
	
	public void removeResidentNodePointer(ResidentNodePointer pointer){
		listPointers.remove(pointer); 
	}
	
	/**
	 *@see unbbayes.prs.bn.ITabledVariable#getPotentialTable()
	 */
	public PotentialTable getPotentialTable() {
		return null;
	}
	
	/**
	 * Add a ov in the list of arguments in this resident node
	 * 
	 * @param ov
	 * @throws ArgumentNodeAlreadySetException
	 * @throws OVariableAlreadyExistsInArgumentList
	 */
	public void addArgument(OrdinaryVariable ov) throws ArgumentNodeAlreadySetException, 
	OVariableAlreadyExistsInArgumentList{
		
		if(ordinaryVariableList.contains(ov)){
			throw new OVariableAlreadyExistsInArgumentList(); 
		}
		else{
			ordinaryVariableList.add(ov); 
			ov.addIsOVariableOfList(this); 
		}
	}
	
	/**
	 * Delete the extern references for this node
	 * 
	 * - Ordinary Variables
	 */
	public void delete(){
		while(!ordinaryVariableList.isEmpty()){
			ordinaryVariableList.remove(0).removeIsOVariableOfList(this); 
		}
	}
	
	public void removeArgument(OrdinaryVariable ov){
		
		ordinaryVariableList.remove(ov);
		//ov.removeIsOVariableOfList(this); -> deve ser feito pela classe que chama. 
		
		for(Argument argument: super.getArgumentList()){
			if(argument.getOVariable() == ov){
				super.removeArgument(argument); 
				return; 
			}
		}
	}
	
	/**
	 * 
	 * @param ov
	 * @return
	 */
	public boolean containsArgument(OrdinaryVariable ov){
		return ordinaryVariableList.contains(ov); 
	}
	
	public List<OrdinaryVariable> getOrdinaryVariableList(){
		return ordinaryVariableList; 
	}

	public OrdinaryVariable getOrdinaryVariableByName(String name){
		for(OrdinaryVariable ov: ordinaryVariableList){
			if(ov.getName().equals(name)){
				return ov; 
			}
		}
		return null; 
	}
	
	/**
	 * @return A list with all the ordinary variables ordereables present in this node.
	 */
	public List<OrdinaryVariable> getOrdinaryVariablesOrdereables(){
		
		List<OrdinaryVariable> ovOrdereableList = new ArrayList<OrdinaryVariable>();
		ObjectEntityConteiner oeConteiner = this.getMFrag().getMultiEntityBayesianNetwork().getObjectEntityContainer();
		
		for(OrdinaryVariable ov: this.getOrdinaryVariableList()){
			ObjectEntity oe = oeConteiner.getObjectEntityByType(ov.getValueType()); 
			if(oe.isOrdereable()){
				ovOrdereableList.add(ov);
			}
		}
		
		return ovOrdereableList;
	}
	
	public int getTypeOfStates() {
		return typeOfStates;
	}

	public void setTypeOfStates(int typeOfStates) {
		this.typeOfStates = typeOfStates;
	}
	
	public String toString() {
		return name;
	}
	
}

