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
 * Class representing a numeric node of the decision tree
 * 
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 * @author Mario Henrique Paes Vieira (mariohpv@bol.com.br)
 */
public class NumericNode extends Node 
{
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
	/** value used to split attribute values */
	private double splitValue;
	
	/** defines if this node is relative to values more or less than split value */
	private boolean isMoreThanAttributeValue;
	
	//--------------------------------CONSTRUCTORS--------------------------------//
	
	/** 
	 * Constructor that creates a numeric node without children
	 * 
	 * @param splitAttribute attribute used for splitting
	 * @param splitValue value used to split attribute values
	 * @param isMoreThanAttributeValue true if this node is relative 
	 * to values more than the split value 
	 */	
	public NumericNode(Attribute splitAttribute, double splitValue,
			boolean isMoreThanAttributeValue, float[] distribution) {
		super(splitAttribute);
		this.splitValue = splitValue;
		this.isMoreThanAttributeValue = isMoreThanAttributeValue;
		this.distribution = distribution;
	}
	
	/** 
	 * Constructor that creates a numeric node with children
	 * 
	 * @param splitAttribute attribute used for splitting
	 * @param splitValue value used to split attribute values
	 * @param isMoreThanAttributeValue true if this node is relative 
	 * to values more than the split value
	 * @param children list of children (Node or Leaf type)
	 */
	public NumericNode(NumericNode parent, ArrayList<Object> children) {
		this(parent.splitAttribute, parent.splitValue,
				parent.isMoreThanAttributeValue, parent.distribution);
		this.children = children;
	}
	
	//--------------------------------BASIC FUNCIONS------------------------------//
	
	/**
	 * Returns a string representing the numeric node on the tree
	 * 
	 * @return string representing the numeric node on the tree
	 */	
	public String toString()
	{
		if(isMoreThanAttributeValue)
		{
			return splitAttribute.getAttributeName() + " >= " + splitValue;
		}
		else
		{
			return splitAttribute.getAttributeName() + " < " + splitValue;
		}
	}
	
	/**
	 * Returns the value to split attribute values
	 * 
	 * @return the value to split attribute values
	 */	
	public double getSplitValue()
	{
		return splitValue;
	}
	
	/**
	 * Returns if this node is relative to values more or less than split value
	 * 
	 * @return true is this node is more relative to split value, false otherwise
	 */
	public boolean isMoreThanAttributeValue()
	{
		return isMoreThanAttributeValue;
	}
}
