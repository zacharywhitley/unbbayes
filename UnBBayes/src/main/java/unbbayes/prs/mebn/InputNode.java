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
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import unbbayes.draw.DrawTwoBaseRectangle;
import unbbayes.prs.mebn.entity.Type;

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
	
	private DrawTwoBaseRectangle drawInputNode; 
	
	private static Color color = new Color(220, 220, 220); 	
	
	//DON'T USE THIS CONSTRUCTOR! IS ONLY TEMPORARY FOR CLEAR THE TESTS
	@Deprecated
	public InputNode(){
		
	}
	
	public InputNode(String name, MFrag mFrag){
		super(); 
		
		setName(name); 
		setLabel(" "); 
		
		this.mFrag = mFrag;
		
		residentNodeChildList = new ArrayList<ResidentNode>(); 
		
		size.x = 100;
		size.y = 20; 
		drawInputNode = new DrawTwoBaseRectangle(position, size);
		drawElement.add(drawInputNode);	
	}
	
	
	
	
	
	
	
	@Override
	public void setSelected(boolean b) {
		drawInputNode.setSelected(b);
		super.setSelected(b);
	}    		
	
	/**
	 *  Gets all generative input node node's color.
	 *
	 * @return The color of all generative input node's color.
	 */
	public static Color getColor() {
		return color;
	}
	
	/**
	 *  Sets the new color for all generative input node node.
	 *
	 * @return The new color of all generative input node in RGB.
	 */
	public static void setColor(int c) {
		color = new Color(c);
	}		
	
	@Override
	public void paint(Graphics2D graphics) {
		drawInputNode.setFillColor(getColor());
		super.paint(graphics);
	}	
	
	
	/**
	 * Atualiza o texto do label apresentado pelo no... 
	 * O label de um n� de input contem o nome do resident ou 
	 * built in o qual este n� representa.
	 */
	
	public void updateLabel(){
		
		Object inputInstanceOf = getInputInstanceOf();
		
		if(inputInstanceOf != null){
			if(inputInstanceOf instanceof ResidentNode){
				ResidentNodePointer pointer = getResidentNodePointer();
				String newLabel = ""; 
				
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
	 */
	public void setInputInstanceOf(ResidentNode node){
		
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
		
		residentNodePointer = new ResidentNodePointer(node, this);
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
	
	
	public Vector<OrdinaryVariable> getOrdinaryVariableList() {
		return residentNodePointer.getOrdinaryVariableList();
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
	
	public Vector<Type> getTypesOfOrdinaryVariableList() {
		return residentNodePointer.getTypesOfOrdinaryVariableList(); 
	}
	
	//TODO este método deve virar privado! O residentNodePointer deve ficar encapsulado no input Node!
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
