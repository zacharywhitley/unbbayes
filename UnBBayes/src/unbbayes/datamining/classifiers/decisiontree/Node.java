package unbbayes.datamining.classifiers.decisiontree;

import java.io.Serializable;
import java.util.ArrayList;

import unbbayes.datamining.datamanipulation.Attribute;

/**
 * Class representing the node of the decision tree
 * 
 * @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 * @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 */
public class Node implements Serializable
{
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;		
	
	/** Attribute used for splitting. */
	protected Attribute splitAttribute;
	/** Node children on the tree */
	protected ArrayList<Object> children = new ArrayList<Object>();

	//instrumentation data
	/** keep attributes and instances used */
	protected SplitObject split;
	/** info gains */
	protected double[] infoGains;
	/** data obtained from numeric attributes */
	protected ArrayList numericDataList;
	
	protected float[] distribution;
	
	//--------------------------------CONSTRUCTORS--------------------------------//

	/** 
	 * Constructor that creates a node without children
	 * 
	 * @param splitAttribute attribute used for splitting
	 */	
	public Node(Attribute splitAttribute)
	{
		this.splitAttribute = splitAttribute;
	}
	
	/** 
	 * Constructor that creates a node without children
	 * 
	 * @param splitAttribute attribute used for splitting
	 */	
	public Node(Attribute splitAttribute, float[] distribution) {
		this.splitAttribute = splitAttribute;
		this.distribution = distribution;
//		
//		if (distribution == null) {
//			@SuppressWarnings("unused") boolean pau = true;
//		}
	}
	
			
	/**
	 * Constructor that creates a node with some children
	 * 
	 * @param splitAttribute attribute used for splitting
	 * @param children list of children (Node or Leaf type)
	 */
	public Node(Attribute splitAttribute, ArrayList<Object> children)
	{
		this.splitAttribute = splitAttribute;
		this.children = children;
	}
	
	//--------------------------------BASIC FUNCIONS------------------------------//

	/**
	 * Returns the split attribute relative to the node
	 * 
	 * @return the split attribute relative to the node
	 */
	public Attribute getAttribute()
	{
		return splitAttribute;
	}
	
	/**
	 * Returns the name of the split attribute
	 * 
	 * @return name of the split attribute
	 */
	public String getAttributeName()
	{
		return splitAttribute.getAttributeName();
	}
	
	/**
	 * Returns a string representing the node on the tree
	 * 
	 * @return string representing the node on the tree
	 */
	public String toString()
	{
		return getAttributeName();
	}
	
	//-------------------------INSTRUMENTATION FUNCTIONS-------------------------//

	/**
	 * Set data obtained on the tree construction relative to the node
	 * 
	 * @param split data relative to the instance set used on splitting
	 * @param infoGains info gains calculated for each possible split attribute
	 * of the node.
	 * @param numericDataList list of NumericData objects, one for each numeric 
	 * attribute
	 */
	public void setInstrumentationData(SplitObject split, double[] infoGains, ArrayList numericDataList)
	{
		this.split = split;
		this.infoGains = infoGains;
		this.numericDataList = numericDataList;
	}
	
	/**
	 * Returns data relative to the instance set used on splitting
	 * 
	 * @return data relative to the instance set used on splitting
	 */
	public SplitObject getSplitData()
	{
		return split;
	}
	
	/**
	 * Returns the info gains calculated for each possible split attribute
	 * of the node.
	 * 
	 * @return the info gains calculated for each possible split attribute
	 * of the node.
	 */
	public double[] getInfoGains()
	{
		return infoGains;
	}
	
	/**
	 * Returns the list of NumericData objects, one for each numeric 
	 * attribute
	 * 
	 * @return the list of NumericData objects, one for each numeric 
	 * attribute
	 */
	public ArrayList getNumericDataList()
	{
		return numericDataList;
	}
	
	//-----------------------------CHILDREN FUNCTIONS-----------------------------//

	/**
	 * Adds a new children to the node
	 * 
	 * @param obj new children, Node or Leaf type;
	 */
	public void add(Object obj)
	{
		children.add(obj);
	}
	
	/** Removes all the children */
	public void removeChildren()
	{
		children = new ArrayList<Object>();
	}
	
	/** 
	 * Returns a list of the node's children
	 * 
	 * @return list of node's children - Node or Leaf type  
	 */ 
	public ArrayList getChildren()
	{
		return (ArrayList)children.clone();
	}
}

