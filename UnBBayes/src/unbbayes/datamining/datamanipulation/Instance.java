package unbbayes.datamining.datamanipulation;

import java.util.*;

/**
 * Class for handling an instance. All values (numeric or nominal) are internally
 * stored as byte numbers. The stored value is the index of the
 * corresponding nominal value in the attribute's definition.
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class Instance
{	/** Constant representing a missing value. */
  	public final static byte MISSING_VALUE = -1;

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.datamining.datamanipulation.resources.DataManipulationResource");

  	/**
   	* The dataset the instance has access to.  Null if the instance
   	* doesn't have access to any dataset.  Only if an instance has
   	* access to a dataset, it knows about the actual attribute types.
   	*/
  	protected InstanceSet dataset;

  	/** The instance's attribute values. */
  	protected byte[] attValues;

	/** The instance's weight. */
  	protected int weight;

  	/**
   	* Constructor that copies the attribute values from
   	* the given instance. Reference to the dataset is set to null.
   	* (ie. the instance doesn't have access to information about the
   	* attribute types).
   	*
   	* @param instance Instance from which the attribute
	* values are to be copied
   	*/
  	public Instance(Instance instance)
	{	attValues = instance.attValues;
		weight = instance.weight;
    	dataset = null;
  	}

  	/**
   	* Constructor that inititalizes instance variable with given
   	* values. Reference to the dataset is set to null. (ie. the instance
   	* doesn't have access to information about the attribute types).
	* Weight is set to 1.
	*
   	* @param attValues An array of attribute values
   	*/
  	public Instance(byte[] attValues)
	{	this.attValues = attValues;
		weight = 1;
    	dataset = null;
  	}

	/**
   	* Constructor that inititalizes instance variable with given
   	* values. Reference to the dataset is set to null. (ie. the instance
   	* doesn't have access to information about the attribute types)
   	*
   	* @param weight the instance's weight
   	* @param attValues a vector of attribute values
   	*/
  	public Instance(int weight, byte[] attValues)
  	{	this.attValues = attValues;
		this.weight = weight;
    	dataset = null;
  	}

	/**
   	* Sets the weight of an instance.
   	*
   	* @param weight the weight
   	*/
  	public final void setWeight(int weight)
	{	this.weight = weight;
  	}

	/**
   	* Returns the instance's weight.
   	*
   	* @return the instance's weight as a double
   	*/
  	public final int getWeight()
	{	return weight;
  	}

	/**
   	* Returns the class attribute's index.
   	*
   	* @return The class index as an integer
   	* @exception UnassignedDatasetException if instance doesn't have access to a dataset
   	*/
  	public final int getClassIndex()
	{	if (dataset == null)
		{	throw new UnassignedDatasetException(resource.getString("runtimeException1"));
    	}
    	return dataset.getClassIndex();
  	}

  	/**
   	* Tests if an instance's class is missing.
   	*
   	* @return True if the instance's class is missing
   	* @exception UnassignedClassException if the class is not set or the instance doesn't
   	* have access to a dataset
   	*/
  	public final boolean classIsMissing()
	{	if (getClassIndex() < 0)
		{	throw new UnassignedClassException(resource.getString("runtimeException2"));
    	}
    	return isMissing(getClassIndex());
  	}

  	/**
   	* Returns an instance's class value in internal format. (ie. as a
   	* byte number)
   	*
   	* @return The corresponding value as a byte (It returns the
   	* value's index as a byte).
   	* @exception UnassignedClassException if the class is not set or the instance doesn't
   	* have access to a dataset
   	*/
  	public final byte classValue()
	{	if (getClassIndex() < 0)
		{	throw new UnassignedClassException(resource.getString("runtimeException2"));
    	}
    	return getValue(getClassIndex());
  	}

  	/**
   	* Produces a shallow copy of this instance. The copy has
   	* access to the same dataset. (if you want to make a copy
   	* that doesn't have access to the dataset, use
   	* <code>new Instance(instance)</code>
   	*
   	* @return the shallow copy
   	*/
  	public Object copy()
	{	Instance result = new Instance(this);
    	result.dataset = dataset;
    	return result;
  	}

  	/**
   	* Tests if a specific value is "missing".
   	*
   	* @param attIndex Attribute's index
   	*/
  	public final boolean isMissing(int attIndex)
	{	if (MISSING_VALUE == attValues[attIndex])
		{	return true;
    	}
    	return false;
  	}

  	/**
   	* Tests if a specific value is "missing".
   	* The given attribute has to belong to a dataset.
   	*
   	* @param att Attribute
   	*/
  	public final boolean isMissing(Attribute att)
	{	return isMissing(att.getIndex());
  	}

  	/**
   	* Tests if the given value codes "missing".
   	*
   	* @param val Value to be tested
   	* @return true if val codes "missing"
   	*/
  	public static final boolean isMissingValue(byte val)
	{	if (MISSING_VALUE == val)
		{	return true;
    	}
    	return false;
  	}


  	/**
   	* Returns the byte that codes "missing".
   	*
   	* @return the byte that codes "missing"
   	*/
  	public static final byte missingValue()
	{	return MISSING_VALUE;
  	}

  	/**
  	 * Sets the class value of an instance to be "missing".
  	 *
  	 * @exception UnassignedClassException if the class is not set
  	 * @exception UnassignedDatasetException if the instance doesn't
  	 * have access to a dataset
  	 */
  	public void setClassMissing()
	{	if (getClassIndex() < 0)
		{	throw new UnassignedClassException(resource.getString("runtimeException2"));
  	  	}
  	  	setMissing(getClassIndex());
  	}

  	/**
  	 * Sets a specific value to be "missing".
  	 *
  	 * @param attIndex the attribute's index
  	 */
  	public void setMissing(int attIndex)
	{	setValue(attIndex, MISSING_VALUE);
  	}

  	/**
   	* Sets the reference to the dataset. Does not check if the instance
   	* is compatible with the dataset. Note: the dataset does not know
   	* about this instance. If the structure of the dataset's header
   	* gets changed, this instance will not be adjusted automatically.
   	*
   	* @param instances Reference to the dataset
   	*/
  	public final void setDataset(InstanceSet instances)
	{	dataset = instances;
  	}

  	/**
   	* Returns the reference to the dataset.
   	*
   	* @return instances Reference to the dataset
   	*/
  	public final InstanceSet getDataset()
	{	return dataset;
  	}

	/**
   	* Returns the value of an attribute for the instance.
   	*
   	* @param attIndex Attribute's index
   	* @return The value as a string
   	* @exception UnassignedDatasetException if the instance doesn't belong
   	* to a dataset.
   	*/
  	public String stringValue(int attIndex)
	{	if (dataset == null)
		{	throw new UnassignedDatasetException(resource.getString("runtimeException1"));
    	}
    	return dataset.getAttribute(attIndex).value((int) getValue(attIndex));
  	}

  	/**
   	* Returns the value of an attribute for the instance.
   	*
   	* @param att Attribute
   	* @return The value as a string
   	* @exception UnassignedDatasetException if the instance doesn't belong
   	* to a dataset.
   	*/
  	public String stringValue(Attribute att)
	{	return stringValue(att.getIndex());
  	}

  	/**
   	* Returns the description of one instance. If the instance
   	* doesn't have access to a dataset, it returns the internal
   	* byte values.
   	*
   	* @return The instance's description as a string
   	*/
  	public String toString()
	{	StringBuffer text = new StringBuffer();

    	for (int i = 0; i < attValues.length; i++)
		{	if (i > 0) text.append(",");
      			text.append(toString(i));
    	}
		text.append(" W "+weight);
    	return text.toString();
  	}

  	/**
   	* Returns the description of one value of the instance as a
   	* string. If the instance doesn't have access to a dataset, it
   	* returns The internal byte value.
   	*
   	* @param attIndex Attribute's index
   	* @return The value's description as a string
   	*/
  	public String toString(int attIndex)
	{	StringBuffer text = new StringBuffer();

   		if (isMissing(attIndex))
		{	text.append("?");
   		}
		else
		{	if (dataset == null)
			{	text.append(attValues[attIndex]);
     		}
			else
			{	text.append(stringValue(attIndex));
     		}
   		}
   		return text.toString();
  	}

  	/**
   	* Returns the description of one value of the instance as a
   	* string. If the instance doesn't have access to a dataset it
   	* returns the internal byte value.
   	* The given attribute has to belong to a dataset.
   	*
   	* @param att Attribute
   	* @return The value's description as a string
   	*/
  	public String toString(Attribute att)
	{	return toString(att.getIndex());
  	}

  	/**
   	* Returns an instance's attribute value in internal format.
   	*
   	* @param attIndex Attribute's index
   	* @return The specified value as a byte
   	*/
  	public final byte getValue(int attIndex)
	{	return attValues[attIndex];
  	}

	/**
   	* Sets an instance's attribute value in internal format.
   	*
   	* @param attIndex Attribute's index
	* @param newValue New value as a byte
   	*/
  	public final void setValue(int attIndex, byte newValue)
	{	attValues[attIndex] = newValue;
	}

  	/**
   	* Returns an instance's attribute value in internal format.
   	* The given attribute has to belong to a dataset.
   	*
   	* @param att Attribute
   	* @return The specified value as a byte
   	*/
  	public final byte getValue(Attribute att)
	{	return getValue(att.getIndex());
  	}

	/**
   	* Sets an instance's attribute value in internal format.
   	* The given attribute has to belong to a dataset.
	*
   	* @param att Attribute
	* @param newValue New value as a byte
   	*/
  	public final void setValue(Attribute att, byte newValue)
	{	attValues[att.getIndex()] = newValue;
	}
	
	public final void removeAttribute(int index)
	{
		byte[] newValues = new byte[attValues.length-1];
		int j=0;
		for (int i=0;i<attValues.length;i++)
		{
			if(index!=i)
			{
				newValues[j]=attValues[i];
				j++;
			}
		}
		attValues = newValues;
	}

}
