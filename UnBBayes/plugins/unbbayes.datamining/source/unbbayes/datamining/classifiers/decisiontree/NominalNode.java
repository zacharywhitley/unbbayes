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
package unbbayes.datamining.classifiers.decisiontree;

import java.util.ArrayList;

import unbbayes.datamining.datamanipulation.Attribute;

/**
 * Class representing a nominal node of the decision tree
 * 
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 * @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 */
public class NominalNode extends Node
{
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
	/** value position on splitAttribute */
	private int attributeValue;
	
	//--------------------------------CONSTRUCTORS--------------------------------//
	
	/** 
	 * Constructor that creates a nominal node without children
	 * 
	 * @param splitAttribute attribute used for splitting
	 * @param attributeValue value position on splitAttribute
	 * @param parentDistribution TODO
	 */	
	public NominalNode(Attribute splitAttribute, int attributeValue,
			float[] distribution) {
		super(splitAttribute);
		this.attributeValue = attributeValue;
		this.distribution = distribution;
	}
	
	/** 
	 * Constructor that creates a nominal node with children
	 * 
	 * @param splitAttribute attribute used for splitting
	 * @param attributeValue value position of splitAttribute
	 * @param children list of children (Node or Leaf type)
	 */
	public NominalNode(NominalNode parent, ArrayList<Object> children) {
		this(parent.splitAttribute, parent.attributeValue, parent.distribution);
		this.children = children;
	}
	
	//--------------------------------BASIC FUNCIONS------------------------------//
	
	/**
	 * Returns a string representing the nominal node on the tree
	 * 
	 * @return string representing the nominal node on the tree
	 */
	public String toString()
	{
		return splitAttribute.getAttributeName() + " = " + splitAttribute.value((int)attributeValue);
	}
	
	/**
	 * Returns the value position on splitAttribute
	 * 
	 * @return the value position on splitAttribute
	 */	
	public int getAttributeValue()
	{
		return attributeValue;
	}
}
