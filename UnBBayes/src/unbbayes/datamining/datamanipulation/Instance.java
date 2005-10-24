package unbbayes.datamining.datamanipulation;

import java.util.*;

/**
 * Class for handling an instance. All values (numeric or nominal) are internally
 * stored as int numbers. The stored value is the index of the
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
  	//protected int[] attValues;

	/** The instance's weight. */
  	protected float weight;
  	
  	private int index;
  	
  	public int getIndex() {
  		return index;
  	}
  	
  	public void setIndex(int index) {
  		this.index = index;
  	}

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
	{	//attValues = instance.attValues;
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
  	/*public Instance(int[] attValues)
	{	this.attValues = attValues;
		weight = 1;
    	dataset = null;
  	}*/

	/**
   	* Constructor that inititalizes instance variable with given
   	* values. Reference to the dataset is set to null. (ie. the instance
   	* doesn't have access to information about the attribute types)
   	*
   	* @param weight the instance's weight
   	* @param attValues a vector of attribute values
   	*/
  	public Instance(int weight, Object[] attValues,InstanceSet dataset, int index)
  	{	
  		int size = dataset.getAttributes().length;
  		for (int i=0;i<size;i++) {
  	  		Attribute att = dataset.getAttribute(i);
  			if (att.attributeType == Attribute.Type.NOMINAL) {
  	  			dataset.getAttribute(i).byteValues[index] = ((Byte)attValues[i]).byteValue();  				
  			} else if (att.attributeType == Attribute.Type.NUMERIC) {
  	  			dataset.getAttribute(i).floatValues[index] = ((Float)attValues[i]).floatValue();  				  				
  			} else {
  				assert false : "Tipo de atribute não existente";
  			}
  		}
		this.dataset = dataset;
  		this.weight = weight;
  		this.index = index;
  	}

	/**
   	* Sets the weight of an instance.
   	*
   	* @param weight the weight
   	*/
  	public final void setWeight(float weight)
	{	this.weight = weight;
  	}

	/**
   	* Returns the instance's weight.
   	*
   	* @return the instance's weight as a double
   	*/
  	public final float getWeight()
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
   	* itn number)
   	*
   	* @return The corresponding value as a int (It returns the
   	* value's index as a int).
   	* @exception UnassignedClassException if the class is not set or the instance doesn't
   	* have access to a dataset
   	*/
  	public final byte classValue()
	{	if (getClassIndex() < 0)
		{	throw new UnassignedClassException(resource.getString("runtimeException2"));
    	}
    	return getByteValue(getClassIndex());
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
	{	
  		if (dataset.getAttribute(attIndex).attributeType == Attribute.Type.NOMINAL) {
  	  		if (MISSING_VALUE == dataset.getAttribute(attIndex).byteValues[0])
  			{	return true;
  	    	}
  	    	return false;  			
  		} else {
  	  		if (MISSING_VALUE == (byte)dataset.getAttribute(attIndex).floatValues[0])
  			{	return true;
  	    	}
  	    	return false;  			  			
  		}
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
  	public static final boolean isMissingValue(int val)
	{	if (MISSING_VALUE == val)
		{	return true;
    	}
    	return false;
  	}


  	/**
   	* Returns the int that codes "missing".
   	*
   	* @return the int that codes "missing"
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
	{	
  		if (this.dataset.getAttribute(attIndex).isNominal()) {
  	  		setByteValue(attIndex, MISSING_VALUE);  			
  		} else {
  			setFloatValue(attIndex, MISSING_VALUE);  			
  		}
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
		Attribute att = dataset.getAttribute(attIndex);
		if (att.isNominal()) {
	    	return att.value(getByteValue(attIndex));			
		} else {
			return att.floatValues[index]+"";
		}
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
   	* int values.
   	*
   	* @return The instance's description as a string
   	*/
  	public String toString()
	{	StringBuilder text = new StringBuilder();
		int size = dataset.getAttributes().length;
    	for (int i = 0; i < size; i++)
		{	if (i > 0) text.append(",");
      			text.append(toString(i));
    	}
		text.append(" W "+weight);
    	return text.toString();
  	}

  	/**
   	* Returns the description of one value of the instance as a
   	* string. If the instance doesn't have access to a dataset, it
   	* returns The internal int value.
   	*
   	* @param attIndex Attribute's index
   	* @return The value's description as a string
   	*/
  	public String toString(int attIndex)
	{	StringBuilder text = new StringBuilder();

   		if (isMissing(attIndex))
		{	text.append("?");
   		}
		else
		{	if (dataset == null)
			{	
				if (dataset.getAttribute(attIndex).attributeType == Attribute.Type.NOMINAL) {
					text.append(dataset.getAttribute(attIndex).byteValues[index]);
				} else {
					text.append(dataset.getAttribute(attIndex).floatValues[index]);					
				}
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
   	* returns the internal int value.
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
   	* @return The specified value as a int
   	*/
  	public final byte getByteValue(int attIndex)
	{	return dataset.getAttribute(attIndex).byteValues[index];
  	}
  	
  	public final float getFloatValue(int attIndex) {
  		return dataset.getAttribute(attIndex).floatValues[index];
  	}

	/**
   	* Sets an instance's attribute value in internal format.
   	*
   	* @param attIndex Attribute's index
	* @param newValue New value as a int
   	*/
  	public final void setByteValue(int attIndex, byte newValue)
	{	dataset.getAttribute(attIndex).byteValues[index] = newValue;
	}

  	public final void setFloatValue(int attIndex, float newValue)
	{	dataset.getAttribute(attIndex).floatValues[index] = newValue;
	}

  	/**
   	* Returns an instance's attribute value in internal format.
   	* The given attribute has to belong to a dataset.
   	*
   	* @param att Attribute
   	* @return The specified value as a int
   	*/
  	public final byte getByteValue(Attribute att)
	{	return getByteValue(att.getIndex());
  	}

	/**
   	* Sets an instance's attribute value in internal format.
   	* The given attribute has to belong to a dataset.
	*
   	* @param att Attribute
	* @param newValue New value as a int
   	*/
  	public final void setByteValue(Attribute att, byte newValue)
	{	att.byteValues[index] = newValue;
	}
	
  	public final void setByteValue(Attribute att, float newValue)
	{	att.floatValues[index] = newValue;
	}

  	public void dispose() {
	  	resource = null;
	  	dataset = null;
	}

}
