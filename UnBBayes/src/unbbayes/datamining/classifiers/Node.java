package unbbayes.datamining.classifiers;

import java.io.Serializable;
import unbbayes.datamining.datamanipulation.*;

public class Node implements Serializable
{
	/** Attribute used for splitting. */
	private Attribute splitAttribute;

	/** node value:
	 * position in the values array of splitAttribute for nominal attribute
	 * value used to split attribute instances for numeric attribute */
	private double attributeValue;

	/** indicates if this node is relative to values more or less than attribute value
	 * valid only for numeric attribute */
	private boolean isMoreThanAttributeValue;

	//---------------------------------------------------------------------//

  	/** constructor for nominal attribute */
  	public Node(Attribute splitAttribute, double attributeValue)
	{
		this.splitAttribute = splitAttribute;
		this.attributeValue = attributeValue;
	}

	/** constructor for numeric attribute */
	public Node(Attribute splitAttribute, double attributeValue, boolean isMoreThanAttributeValue)
	{
		this.splitAttribute = splitAttribute;
		this.attributeValue = attributeValue;
		this.isMoreThanAttributeValue = isMoreThanAttributeValue;
	}

	public String toString()
	{
		if(splitAttribute.isNominal())
		{
			return splitAttribute.getAttributeName() + " = " + splitAttribute.value((int)attributeValue);
		}
		else
		{
			if(isMoreThanAttributeValue)
			{
				return splitAttribute.getAttributeName() + " >= " + attributeValue;
			}

			else
			{
				return splitAttribute.getAttributeName() + " < " + attributeValue;
			}
		}

	}

	public Attribute getAttribute()
	{
		return splitAttribute;
	}

	public String getAttributeName()
	{
		return splitAttribute.getAttributeName();
	}

	public double getAttributeValue()
	{
		return attributeValue;
	}
}
