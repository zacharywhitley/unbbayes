package unbbayes.datamining.datamanipulation;

import java.io.*;
import java.util.*;

/**
 * Class for handling an attribute. <p>
 *
 * Two attribute types are supported:
 * <ul>
 *    <li> numeric: <ul>
 *         This type of attribute represents a floating-point number.
 *    </ul>
 *    <li> nominal: <ul>
 *         This type of attribute represents a fixed set of nominal values.
 *    </ul>
 * </ul>
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class Attribute implements Serializable
{	
	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

	/** Constant set for numeric attributes. */
  	public final static int NUMERIC = 0;

  	/** Constant set for nominal attributes. */
  	public final static int NOMINAL = 1;

  	/** The attribute's name. */
  	private String attributeName;

  	/** The attribute's type. */
  	private int attributeType;

  	/** The attribute's values */
  	private String[] attributeValues;

	/** Mapping of values to indices */
  	private Hashtable hashtable;

  	/** The attribute's index. */
  	private int index;

  	/**
   	* Constructor for attributes.
   	*
   	* @param attributeName Name for the attribute
   	* @param attributeValues A arrayList of strings denoting the
   	* attribute values.
	* @param attributeType Type of this attribute (Nominal or Numeric)
	*/
  	public Attribute(String attributeName,String[] attributeValues,int attributeType)
	{	this.attributeName = attributeName;
    	index = -1;
    	this.attributeValues = attributeValues;
      	if (attributeValues != null)
		{
			int numValues = numValues();
			hashtable = new Hashtable(numValues);
      		for (int i = 0; i < numValues; i++)
			{	hashtable.put(attributeValues[i], new Integer(i));
      		}
		}
		else
		{	hashtable = null;
		}
		this.attributeType = attributeType;
  	}

  	/**
   	* Constructor for attributes with a particular index.
   	*
   	* @param attributeName Name for the attribute
   	* @param attributeValues ArrayList of strings denoting the attribute values.
   	* @param attributeType Type of this attribute (Nominal or Numeric)
	* @param index The attribute's index
	*/
  	public Attribute(String attributeName,String[] attributeValues,int attributeType,int index)
	{	this(attributeName, attributeValues, attributeType);
		this.index = index;
	}

  	/**
   	* Returns an enumeration of all the attribute's values
   	*
   	* @return Enumeration of all the attribute's values
   	*/
  	public final Enumeration enumerateValues()
	{
          if (attributeValues != null)
          {
            final ArrayListEnumeration ale = new ArrayListEnumeration(attributeValues);
            return new Enumeration ()
            {
                public boolean hasMoreElements()
                {
                  return ale.hasMoreElements();
                }

                public Object nextElement()
                {
                  Object oo = ale.nextElement();
                  return oo;
            	}
            };
          }
          return null;
  	}

  	/**
   	* Tests if given attribute is equal to this attribute.
   	*
   	* @param other The Object to be compared to this attribute
   	* @return True if the given attribute is equal to this attribute
   	*/
  	public final boolean equals(Object other)
	{	if ((other == null) || !(other.getClass().equals(this.getClass())))
		{	return false;
    	}
    	Attribute att = (Attribute) other;
    	if (!attributeName.equals(att.attributeName))
		{	return false;
    	}
    	if (attributeType != att.attributeType)
		{	return false;
    	}
    	if (numValues() != att.numValues())
		{	return false;
    	}
    	for (int i = 0; i < numValues(); i++)
		{	if (!attributeValues[i].equals(att.attributeValues[i]))
			{	return false;
      		}
    	}
    	return true;
  	}

  	/**
   	* Returns the index of this attribute.
   	*
   	* @return The index of this attribute
   	*/
  	public final int getIndex()
	{	return index;
  	}

	/**
   	* Returns the index of a given attribute value. (The index of
   	* the first occurence of this value.)
   	*
   	* @param value The value for which the index is to be returned
   	* @return The index of the given attribute value, -1 if the value can't be found
	*/
  	public final int indexOfValue(String value)
	{	if (hashtable == null)
      		return -1;
    	Object store = value;
    	store = hashtable.get(store);
		if (store == null)
			return -1;
		else
		{	Integer integer = new Integer(store.toString());
			return integer.intValue();
		}

  	}

  	/**
   	* Test if the attribute is nominal.
   	*
   	* @return true if the attribute is nominal
   	*/
  	public final boolean isNominal()
	{	return (attributeType == NOMINAL);
  	}

  	/**
   	* Tests if the attribute is numeric.
   	*
   	* @return true if the attribute is numeric
   	*/
  	public final boolean isNumeric()
	{	return (attributeType == NUMERIC);
  	}

  	/**
   	* Returns the attribute's name.
   	*
   	* @return the attribute's name as a string
   	*/
  	public final String getAttributeName()
	{	return attributeName;
  	}

  	/**
   	* Returns the number of attribute values.
   	*
   	* @return the number of attribute values
   	*/
  	public final int numValues()
	{	if (attributeValues == null)
		{	return 0;
    	}
		else
		{	return attributeValues.length;
    	}
  	}

  	/**
   	* Returns a description of this attribute in ARFF format.
   	*
   	* @return a description of this attribute as a string
   	*/
  	public final String toString()
	{
          StringBuffer text = new StringBuffer();
          text.append("@attribute " + attributeName + " ");
          if (isNominal())
          {
            text.append('{');
            Enumeration enumeration = enumerateValues();
            if (enumeration!=null)
            {
              while (enumeration.hasMoreElements())
              {
                text.append(enumeration.nextElement());
                if (enumeration.hasMoreElements())
                  text.append(',');
              }
            }
            text.append('}');
          }
          else
          {
            if (isNumeric())
            {
              text.append("numeric");
            }
          }
          return text.toString();
  	}

	/**
   	* Returns the attribute's type as an integer.
   	*
   	* @return the attribute's type.
   	*/
  	public final int getAttributeType()
	{	return attributeType;
  	}

  	/**
   	* Returns a value of a attribute.
   	*
   	* @param valIndex The value's index
   	* @return the attribute's value as a string. If value is not found returns ""
   	*/
  	public final String value(int valIndex)
	{	if (attributeValues != null && valIndex >= 0 && valIndex <= numValues())
		{	String val = attributeValues[valIndex];
      		return val;
		}
		else
		{	return "";
    	}
  	}

	/**
   	* Adds an attribute value.
   	*
   	* @param value The attribute value
   	*/
  	public final void addValue(String value)
	{
          if (hashtable == null)
          {
            hashtable = new Hashtable();

          }

          String[] newValues = new String[numValues()+1];
          if (attributeValues!=null)
          {
            System.arraycopy(attributeValues,0,newValues,0,numValues());
          }
          newValues[numValues()] = value;
          attributeValues = newValues;
          hashtable.put(value, new Integer(numValues() - 1));
	}

  	/**
   	* Produces a shallow copy of this attribute.
   	*
   	* @return a copy of this attribute with the same index
   	*/
  	public Object copy()
  	{	Attribute copy = new Attribute(attributeName,attributeValues,attributeType,index);
    	return copy;
	}

	/**
   	* Produces a shallow copy of this attribute with a new name.
   	*
   	* @param newName Name of the new attribute
   	* @return a copy of this attribute with the same index
   	*/
  	public final Attribute copy(String newName)
	{	Attribute copy = new Attribute(newName,attributeValues,attributeType,index);
    	return copy;
  	}

  	/**
   	* Removes a value of a nominal attribute.
   	*
   	* @param index The value's index
   	*/
  	//public final void delete(int index)
	//{	attributeValues.remove(index);
    //  	Hashtable hash = new Hashtable(hashtable.size());
    //  	Enumeration enum = hashtable.keys();
    //  	while (enum.hasMoreElements())
	//	{	String string = (String)enum.nextElement();
	//		Integer valIndexObject = (Integer)hashtable.get(string);
	//		int valIndex = valIndexObject.intValue();
	//		if (valIndex > index)
	//		{	hash.put(string, new Integer(valIndex - 1));
	//		}
	//		else if (valIndex < index)
	//		{	hash.put(string, valIndexObject);
	//		}
    //	}
    //  	hashtable = hash;
  	//}

  	/**
   	* Sets the index of this attribute.
   	*
   	* @param index Index of this attribute
   	*/
  	public final void setIndex(int index)
	{	this.index = index;
  	}

  	/**
   	* Sets a value of an attribute.
   	*
   	* @param index The value's index
   	* @param string The value
   	*/
  	//public final void setValue(int index, String string)
	//{	if (attributeType == NUMERIC)
	//	{	try
	//		{	Float.parseFloat(string);
	//		}
	//		catch (NumberFormatException nfe)
	//		{	throw new RuntimeException(resource.getString("setValueException"));
	//		}
	//	}
	//	String store = string;
    //	hashtable.remove(attributeValues[index]);
    //  	attributeValues[index] = store;
    //  	hashtable.put(store, new Integer(index));
  	//}

	/**
   	* Changes the type of an attribute.
   	*
   	* @param attributeType New attribute type
   	*/
  	public void setAttributeType(int attributeType)
	{	this.attributeType = attributeType;
	}

	/**
    * Returns the attribute's values.
    *
    * @return the attribute's values.
    */
    public String[] getAttributeValues()
    {
    	String[] values = new String[attributeValues.length];
    	System.arraycopy(attributeValues,0,values,0,attributeValues.length);
    	return values;
    }
}

