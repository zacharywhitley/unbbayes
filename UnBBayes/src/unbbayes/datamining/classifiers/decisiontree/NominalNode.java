package unbbayes.datamining.classifiers.decisiontree;

import unbbayes.datamining.datamanipulation.*;
import java.util.*;

/**
 * Class representing a nominal node of the decision tree
 * 
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 */
public class NominalNode extends Node
{
	/** value position on splitAttribute */
	private int attributeValue;
	
	//--------------------------------CONSTRUCTORS--------------------------------//
	
	/** 
	 * Constructor that creates a nominal node without children
	 * 
	 * @param splitAttribute attribute used for splitting
	 * @param attributeValue value position on splitAttribute
	 */	
	public NominalNode(Attribute splitAttribute, int attributeValue)
	{
		super(splitAttribute);
		this.attributeValue = attributeValue;
	}
	
	/** 
	 * Constructor that creates a nominal node with children
	 * 
	 * @param splitAttribute attribute used for splitting
	 * @param attributeValue value position of splitAttribute
	 * @param children list of children (Node or Leaf type)
	 */
	public NominalNode(Attribute splitAttribute, int attributeValue, ArrayList children)
	{
		this(splitAttribute, attributeValue);
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
