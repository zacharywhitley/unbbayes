package unbbayes.datamining.classifiers;

import unbbayes.datamining.datamanipulation.*;

public class Node 
{
	/** Attribute used for splitting. */
	private Attribute splitAttribute;
	
	/** node value's position in the values array of splitAttribute */
	private int attributeValue; 
  	
  	
	public Node(Attribute newSplitAttribute, int newAttributeValue)
	{
		splitAttribute = newSplitAttribute;
		attributeValue = newAttributeValue;
	}
  	
	public String toString()
	{
		return splitAttribute.getAttributeName() + " = " + splitAttribute.value(attributeValue);
	}
	
	public Attribute getAttribute()
	{
		return splitAttribute;
	}
	
	public String getAttributeName()
	{
		return splitAttribute.getAttributeName();
	}
}
