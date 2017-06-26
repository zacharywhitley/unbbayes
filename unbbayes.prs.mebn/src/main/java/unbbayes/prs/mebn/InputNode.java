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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.OVDontIsOfTypeExpected;
import unbbayes.util.Debug;

public class InputNode extends MultiEntityNode {

	private static final long serialVersionUID = 1L;
	
	private MFrag mFrag;
	
	private List<ResidentNode> residentNodeChildList;
	
	private ResidentNodePointer residentNodePointer; 

	/*
	 * These two variables (inputInstanceOfRV and inputInstanceOfNode) have an
	 * 'or' relationship. That means that if this input node is an input
	 * instance of RV, than it is not from a node. The opposite is also true. In
	 * other words, if one is not null the other must be null.
	 */
	
	// TODO Verify if it is ever used. It seams that UnBBayes is not ready for this.
	private BuiltInRV inputInstanceOfRV;
	
	private ResidentNode inputInstanceOfNode;
	
	private static Color color = new Color(220, 220, 220); 	
	
	//DON'T USE THIS CONSTRUCTOR! IS ONLY TEMPORARY FOR CLEAR THE TESTS
	@Deprecated
	public InputNode(){
		
	}
	
	public InputNode(String name, MFrag mFrag){
		super(); 
		
		setName(name); 
		setLabel(" "); 
		//by young
		setColor(new Color(220, 220, 220));

		this.mFrag = mFrag;
		
		residentNodeChildList = new ArrayList<ResidentNode>(); 
		
		size.x = 100;
		size.y = 60; 
	}
	
	/**
	 * Atualiza o texto do label apresentado pelo no... 
	 * O label de um n� de input contem o nome do resident ou 
	 * built in o qual este n� representa.
	 */
	
	//by young
	public String updateLabel(){
		
		String newLabel = ""; 
		
		Object inputInstanceOf = getInputInstanceOf();
		
		if(inputInstanceOf != null){
			if(inputInstanceOf instanceof IResidentNode){
				ResidentNodePointer pointer = getResidentNodePointer();
			
				
				newLabel+= pointer.getResidentNode().getName(); 
				newLabel+= "("; 
				
				for(OrdinaryVariable ov: pointer.getOrdinaryVariableList()){
					
					if(ov!=null) newLabel+= ov.getName(); 
					else newLabel+= " ";
					
					newLabel+= ","; 
				}
				
				//delete the last virgle
				if(pointer.getOrdinaryVariableList().size() > 0){
					newLabel = newLabel.substring(0, newLabel.length() - 1); 
				}
				
				newLabel+= ")"; 
				
				setLabel(newLabel); 
			}
			else{
				setLabel(((BuiltInRV)inputInstanceOf).getName()); 
			}
		}
		else{
			setLabel(" "); 
		}
		
		return newLabel;
	}	
	
	
	
	
	
	
	/**
	 * Update the resident node pointer. 
	 * This is necessary if the list of arguments of the resident pointed for
	 * this pointer change. After the update the list of arguments will 
	 * be empty.
	 */
	public void updateResidentNodePointer(){
		if (residentNodePointer != null){
			residentNodePointer = new ResidentNodePointer(residentNodePointer.getResidentNode(), this); 
		}
	}
	
	/**
	 * set the input instance of this node for the BuiltInRV 
	 * @param rv
	 */
	public void setInputInstanceOf(BuiltInRV rv){
		inputInstanceOfNode = null; 
		inputInstanceOfRV = rv; 

		rv.addInputInstance(this); 
		updateLabel();
		
		residentNodePointer = null; 	
	}
	
	/** 
	 * set the input instance of this node for the ResidentNode or BuiltInRV
	 * @param node
	 * @throws OVDontIsOfTypeExpected 
	 * @throws ArgumentNodeAlreadySetException 
	 */
	public void setInputInstanceOf(ResidentNode node) throws OVDontIsOfTypeExpected, ArgumentNodeAlreadySetException{
		
		if(inputInstanceOfNode != null){
			if(inputInstanceOfNode == node){
				return; //OK... work already do. 
			}else{
				//By, by... this node don't will be more referent to the resident setted
				inputInstanceOfNode.removeInputInstanceFromList(this); 
			}
		}
		
		inputInstanceOfRV = null; 
		inputInstanceOfNode = node; 
		if (node == null) {
			updateLabel();
			return;
		}
		
		residentNodePointer = new ResidentNodePointer(node, this);
//		residentNodePointer.
		int i = 0;
		for(OrdinaryVariable ovar: node.getOrdinaryVariableList()){
			Argument arg = new Argument("", this);
			arg.setOVariable(ovar);
			arg.setArgNumber(i);
			this.addArgument(arg);
			residentNodePointer.addOrdinaryVariable(ovar, i);
			i++;
		}
		node.addInputInstanceFromList(this); 
		updateLabel(); 
	}	
	
	/**
	 * set the input instance of propriety for null... 
	 */
	public void setInputInstanceOf(){
		inputInstanceOfRV = null; 
		inputInstanceOfNode = null; 
		
		updateLabel(); 
		
		residentNodePointer = null; 
	}

	/** 
	 * return the value of the property input instance of 
	 * @return one BuiltInRV or one ResidentNode
	 */	
	public Object getInputInstanceOf(){
		if (inputInstanceOfNode != null){
			return inputInstanceOfNode; 
		}
		else{
			return inputInstanceOfRV;
		}
	}		
	
	

	/**
	 * Remove the node of the resident node child list. 
	 */
	public void removeResidentNodeChild(ResidentNode node){
		residentNodeChildList.remove(node);
		node.removeInputNodeFatherList(this);
	}	
	
	public void addResidentNodeChild(ResidentNode resident){
		residentNodeChildList.add(resident); 
		resident.addInputNodeFather(this); 
	}
	
	public List<ResidentNode> getResidentNodeChildList(){
		return residentNodeChildList; 
	}
	
	/**
	 * This method will simply delegate to {@link #getResidentNodePointer()}
	 * @return : the list of ordinary variables used in the arguments of this input node
	 * @see #getOrdinaryVariablesInArguments()
	 */
	public Vector<OrdinaryVariable> getOrdinaryVariableList() {
		return residentNodePointer.getOrdinaryVariableList();
	}
	
	/**
	 * This method will simply delegate to {@link #getOrdinaryVariableList()}
	 * @see unbbayes.prs.mebn.MultiEntityNode#getOrdinaryVariablesInArgument()
	 * @see #getOrdinaryVariableList()
	 */
	public List<OrdinaryVariable> getOrdinaryVariablesInArgument() {
		Vector<OrdinaryVariable> ordinaryVariableList = getOrdinaryVariableList();
		if (ordinaryVariableList == null) {
			return Collections.EMPTY_LIST;
		}
		return ordinaryVariableList;
	}
	
	public OrdinaryVariable getOrdinaryVariableByIndex(int index){
		if((residentNodePointer.getNumberArguments() < index) || index < 0){
			return null; 
		}
		else{
			return residentNodePointer.getArgument(index);
		}
	}
	
	
	
	public OrdinaryVariable getOrdinaryVariableByName(String name){
		for(OrdinaryVariable ordinaryVariable: residentNodePointer.getOrdinaryVariableList()){
			if(ordinaryVariable.getName().equals(name)){
				return ordinaryVariable; 
			}
		}
		return null; 
	}
	
	/**
	 * Search by name.
	 * 
	 * Searches for a OV within input node, looks for the bound OV from ResidentNode, and returns it.
	 * 
	 * For example:
	 * 
	 * 		Suppose this input node has 2 OVs: "s" as Starship and "tPrev" as Time.
	 * 		Suppose this input node is bound to a resident node containing also 2 OVs: "st" as Starship and "t" as Time.
	 * 			OBS. In this case, "s" is bound to "st" and "tPrev" is bound to "t"
	 * 
	 * 		By searching for "s", this method will return "st". By searching for "tPrev", this method will return "t".
	 *  
	 * Please, note that this method relies in the order of {@link ResidentNode#getOrdinaryVariableList()} representing
	 * the bindings between input and resident nodes.
	 *  
	 * @param name
	 * @return
	 */
	public OrdinaryVariable getOrdinaryVariableBoundToResidentNode(String inputOvName) {
		try {
			OrdinaryVariable inputOV = this.getOrdinaryVariableByName(inputOvName);
			return this.getResidentNodePointer().getResidentNode().getOrdinaryVariableByIndex(this.getOrdinaryVariableList().indexOf(inputOV));
		} catch (Exception e) {
			Debug.println(this.getClass(), e.getMessage(), e);
		}
		return null;
	}
	
	public Vector<Type> getTypesOfOrdinaryVariableList() {
		return residentNodePointer.getTypesOfOrdinaryVariableList(); 
	}
	
	//TODO este método deve virar privado! O residentNodePointer deve ficar encapsulado no input Node!
	// Oooops! Changing this method's visibility to private is not enough, since many classes need
	// to see what resident node is bound to this input node. There is a need for a major refactoring,
	// in order to truly encapsulate residentNodePointer. A simple change of visibility is definitely not enough
	public ResidentNodePointer getResidentNodePointer() {
		return residentNodePointer;
	}
	
	
	public MFrag getMFrag(){
		return mFrag; 
	}
	
	
	
	
	
	/**
	 * Method responsible for deleting this input node. 
	 *
	 */
	public void delete() {
		
		while(!residentNodeChildList.isEmpty()){
			ResidentNode resident = residentNodeChildList.get(0); 
			removeResidentNodeChild(resident); 
			mFrag.removeEdgeByNodes(this, resident);
		}
		
		mFrag.removeInputNode(this); 
	}
	
	public String toString(){
		if (residentNodePointer != null){
			return residentNodePointer.getResidentNode().toString(); 
		}else{
			return " "; 
		}
	}


}
