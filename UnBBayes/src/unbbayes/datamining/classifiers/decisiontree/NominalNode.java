package unbbayes.datamining.classifiers.decisiontree;

import java.util.ArrayList;

import unbbayes.datamining.datamanipulation.Attribute;

/**
 * Class representing a nominal node of the decision tree
 * 
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
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
