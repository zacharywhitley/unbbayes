package unbbayes.datamining.classifiers.decisiontree;

import unbbayes.datamining.datamanipulation.*;

public class NominalNode extends Node
{
	/**position in the values array of splitAttribute*/
	private int attributeValue;
	
	public NominalNode(Attribute splitAttribute, int attributeValue)
	{
		super(splitAttribute);
		this.attributeValue = attributeValue;
	}
	
	public String toString()
	{
		return splitAttribute.getAttributeName() + " = " + splitAttribute.value((int)attributeValue);
	}
		
	public double getAttributeValue()
	{
		return attributeValue;
	}
}
