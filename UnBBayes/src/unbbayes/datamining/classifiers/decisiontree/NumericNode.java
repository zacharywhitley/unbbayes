package unbbayes.datamining.classifiers.decisiontree;

import unbbayes.datamining.datamanipulation.*;

public class NumericNode extends Node 
{
	/** value used to split attribute */
	private double splitValue;
	
	/** indicates if this node is relative to values more or less than attribute value */
	private boolean isMoreThanAttributeValue;
	
	public NumericNode(Attribute splitAttribute, double splitValue, boolean isMoreThanAttributeValue)
	{
		super(splitAttribute);
		this.splitValue = splitValue;
		this.isMoreThanAttributeValue = isMoreThanAttributeValue;
	}
		
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
	
	public double getSplitValue()
	{
		return splitValue;
	}
}
