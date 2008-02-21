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

import java.util.List;

public class InputNode extends MultiEntityNode {

	private List<ResidentNode> residentNodeChildList;

	/*
	 * These two variables (inputInstanceOfRV and inputInstanceOfNode) have an
	 * 'or' relationship. That means that if this input node is an input
	 * instance of RV, than it is not from a node. The opposite is also true. In
	 * other words, if one is not null the other must be null.
	 */
	
	// TODO Verify if it is ever used. It seams that UnBBayes is not ready for this.
	private BuiltInRV inputInstanceOfRV;
	
	private ResidentNode inputInstanceOfNode;
	
	public InputNode(){
		super(); 
	}
	
	/**
	 * set the input instance of this node for the BuiltInRV 
	 * @param rv
	 */
	public void setInputInstanceOf(BuiltInRV rv){
		inputInstanceOfNode = null; 
		inputInstanceOfRV = rv; 

	}
	
	/** 
	 * set the input instance of this node for the ResidentNode or BuiltInRV
	 * @param node
	 */
	public void setInputInstanceOf(ResidentNode node){
		inputInstanceOfRV = null; 
		inputInstanceOfNode = node; 
		
	}	
	
	/**
	 * set the input instance of propriety for null... 
	 */
	public void setInputInstanceOf(){
		inputInstanceOfRV = null; 
		inputInstanceOfNode = null; 
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
	 * Add a resident node in the list of resident node childs. 
	 * @param node: the node that will be add. 
	 */
	public void addResidentNodeChild(ResidentNode node){
		this.residentNodeChildList.add(node); 
	}

	/**
	 * Method responsible for deleting this input node. 
	 *
	 */
	public void delete() {
		
		residentNodeChildList = null; 
		inputInstanceOfNode = null;
	}


}
