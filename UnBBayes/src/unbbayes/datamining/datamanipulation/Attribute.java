package unbbayes.datamining.datamanipulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;

/**
 * Class for handling an attribute. <p>
 *
 * Two attribute types are supported:
 * <ul>
 *		<li> numeric: <ul>
 *				 This type of attribute represents a floating-point number.
 *		</ul>
 *		<li> nominal: <ul>
 *				 This type of attribute represents a fixed set of nominal values.
 *		</ul>
 * </ul>
 *
 *	@author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *	@version $1.0 $ (16/02/2002)
. */
public class Attribute implements Serializable {	
	/** Serialization runtime version number. */
	private static final long serialVersionUID = 0;	
	
	/** Constant set for numeric attributes. */
	public final static byte NUMERIC = 0;

	/** Constant set for nominal attributes. */
	public final static byte NOMINAL = 1;

	/** Constant set for cyclic numeric attributes. */
	public final static byte CYCLIC = 2;

	/** 
	 * Stores the type of an attribute:
	 * 0 - Numeric
	 * 1 - Nominal
	 * 2 - Cyclic numeric
	 */
	private byte attributeType;

	/** The attribute's name. */
	private String attributeName;

	/** True if the values of this attribute are Strings. */
	private boolean isString;
								
	/** The attribute's String values. */
	private String[] stringValues;
	
	/** Used to temporarily store the String values of this attribute. */
	private ArrayList<String> stringValuesTemp;

	/** The attribute's number values. */
	private float[] numberValues;
	
	/** Used to temporarily store the String values of this attribute. */
	private ArrayList<Float> numberValuesTemp;

	/** The number of different values of this attribute. */
	private int numValues;

	/** Mapping of String values to indices. */
	private Hashtable<String, Integer> hashtableString;

	/** Mapping of number values to indices. */
	private Hashtable<Float, Integer> hashtableNumber;

	/** The column attIndex of the dataset referenced by this attribute. */
	private int attIndex;

	/** Load resource file from this package. */
	private static ResourceBundle resource = ResourceBundle.getBundle("" +
			"unbbayes.datamining.datamanipulation.resources." +
			"DataManipulationResource");

	/** The instanceSet to which this attribute is related to */ 
	private InstanceSet instanceSet;
	
	/**
	 * Constructor for nominal attribute with String values.
	 *
	 * @param attributeName Name of this attribute.
	 * @param stringValues ArrayList of Strings denoting the attribute values.
	 * @param attIndex The attribute's position at the instanceSet.
	 */
	public Attribute(String attributeName, String[] stringValues, int attIndex) {
		this.stringValues = stringValues;
		this.attIndex = attIndex;
		numValues = stringValues.length;
		hashtableString = new Hashtable<String, Integer>(numValues);
		for (int i = 0; i < numValues; i++) {
			hashtableString.put(stringValues[i], new Integer(i));
		}
		this.attributeName = attributeName;
		this.stringValues = stringValues;
		attributeType = NOMINAL;
		attIndex = -1;
		isString = true;
	}

	/**
	 * Constructor for nominal attribute with number values.
	 *
	 * @param attributeName Name of this attribute.
	 * @param numberValues A arrayList of floats denoting the attribute values.
	 * @param attIndex The attribute's position at the instanceSet.
	 */
	public Attribute(String attributeName, float[] numberValues, int attIndex) {
		this.attIndex = attIndex;
		this.numberValues = numberValues;
		numValues = numberValues.length;
		hashtableNumber = new Hashtable<Float, Integer>(numValues);
		for (int i = 0; i < numValues; i++) {
			hashtableNumber.put(numberValues[i], new Integer(i));
		}
		this.attributeName = attributeName;
		this.numberValues = numberValues;
		attributeType = NOMINAL;
		attIndex = -1;
		isString = false;
	}

	/**
	 * Constructor for numeric or nominal attribute.
	 * 
	 * @param attributeName Name of this attribute.
	 * @param attributeType Type of this attribute.
	 * @param isString Set that attribute values are string values. 
	 * @param numValues Maximum number of values added to this attribute.
	 */
	public Attribute(String attributeName, byte attributeType,
			boolean isString, int numValues) {
		this.attributeName = attributeName;
		this.attributeType = attributeType;
		this.isString = isString;
		attIndex = -1;
		if (attributeType == NOMINAL) {
			if (isString) {
				hashtableString = new Hashtable<String, Integer>(numValues);
				stringValuesTemp = new ArrayList<String>();
			} else {
				hashtableNumber = new Hashtable<Float, Integer>(numValues);
				numberValuesTemp = new ArrayList<Float>();
			}
		}
	}

	/**
	 * Constructor for numeric or nominal attribute with a particular attIndex.
	 * 
	 * @param attributeName Name of this attribute.
	 * @param attributeType Type of this attribute.
	 * @param isString Set that attribute values are string values. 
	 * @param numValues Maximum number of values added to this attribute.
	 * @param attIndex The attribute's position at the instanceSet.
	 */
	public Attribute(String attributeName, byte attributeType,
			boolean isString, int numValues, int attIndex) {
		this(attributeName, attributeType, isString, numValues);
		this.attIndex = attIndex;
	}

	/**
	 * Returns an enumeration of all the attribute's values
	 *
	 * @return Enumeration of all the attribute's values
	 */
	public final Enumeration enumerateValues() {
		if (attributeType == NOMINAL) {
			if (stringValues != null || numberValues != null) {
				final ArrayListEnumeration ale;
				String[] enumValues;
				if (stringValues != null) {
					enumValues = stringValues;
				} else {
					enumValues = new String[numValues];
					for (int i = 0; i < numValues; i++) {
						/* Check if the fraction part of the value can be ignored */
						int auxValue = (int) numberValues[i];
						if (numberValues[i] == (float) auxValue) {
							/* The stored value is an integer value */
							enumValues[i] = String.valueOf(auxValue);
						} else {
							enumValues[i] = String.valueOf(numberValues[i]);
						}
					}
				}
				ale = new ArrayListEnumeration(enumValues);
				
				return new Enumeration () {
					public boolean hasMoreElements() {
						return ale.hasMoreElements();
					}
	
					public Object nextElement() {
						Object oo = ale.nextElement();
						return oo;
					}
				};
			}
		}
		
		return null;
	}

	/**
	 * Returns the attIndex of this attribute.
	 *
	 * @return The attIndex of this attribute
	 */
	public final int getIndex() {
		return attIndex;
	}

	/**
	 * Returns the attIndex of a given attribute value. (The attIndex of
	 * the first occurence of this value.)
	 *
	 * @param value The value for which the attIndex is to be returned
	 * @return The attIndex of the given attribute value, -1 if the value
	 * can't be found.
	 */
	public final int indexOfValue(String key) {
		if (hashtableString != null) {
			Integer attIndex = hashtableString.get(key);
			if (attIndex != null) {
				return attIndex.intValue();
			}
		}
		return -1;
	}

	/**
	 * Returns the attIndex of a given attribute value. (The attIndex of
	 * the first occurence of this value.)
	 *
	 * @param value The value for which the attIndex is to be returned
	 * @return The attIndex of the given attribute value, -1 if the value can't
	 * be found.
	 */
	public final int indexOfValue(Float key) {
		if (hashtableNumber != null) {
			Integer attIndex = hashtableNumber.get(key);
			if (attIndex != null) {
				return attIndex.intValue();
			}
		}
		return -1;
	}

	/**
	 * Tests if the attribute is nominal.
	 *
	 * @return true if the attribute is nominal.
	 */
	public final boolean isNominal() {
		return attributeType == NOMINAL;
	}

	/**
	 * Tests if the attribute is numeric.
	 *
	 * @return true if the attribute is numeric.
	 */
	public final boolean isNumeric() {
		return attributeType == NUMERIC;
	}

	/**
	 * Tests if the attribute is cyclic numeric.
	 *
	 * @return true if the attribute is cyclic numeric.
	 */
	public boolean isCyclic() {
		return attributeType == CYCLIC;
	}

	/**
	 * Tests if the attribute's values are Strings.
	 *
	 * @return true if the attribute's values are Strings.
	 */
	public boolean isString() {
		return isString;
	}

	/**
	 * Returns the attribute's name.
	 *
	 * @return the attribute's name as a string
	 */
	public final String getAttributeName() {
		return attributeName;
	}

	/**
	 * Returns the number of attribute values.
	 *
	 * @return the number of attribute values
	 */
	public final int numValues() {
		if (attributeType == NOMINAL) {
			return numValues;
		}
		return 0;
	}

	/**
	 * Returns a description of this attribute in ARFF format.
	 *
	 * @return a description of this attribute as a string
	 */
	public final String toString() {
		StringBuffer text = new StringBuffer();
		
		text.append("@attribute " + attributeName + " ");
		if (isNominal()) {
			text.append('{');
			Enumeration enumeration = enumerateValues();
			if (enumeration!=null) {
				while (enumeration.hasMoreElements()) {
					text.append(enumeration.nextElement());
					if (enumeration.hasMoreElements()) {
						text.append(',');
					}
				}
			}
			text.append('}');
		} else if (isNumeric()) {
			text.append("numeric");
		} else if (isCyclic()) {
			text.append("cyclic");
		}
		return text.toString();
	}

	/**
	 * Returns a value of an attribute.
	 *
	 * @param valIndex The value's attIndex
	 * @return the attribute's value as a string. If value is not found returns ""
	 */
	public final String value(int valIndex) {
		if (attributeType == NOMINAL) {
			if (valIndex >= 0 && valIndex <= numValues) {
				if (stringValues != null) {
					return stringValues[valIndex];
				} else if (numberValues != null) {
					/* Check if the fraction part of the value can be ignored */
					int auxValue = (int) numberValues[valIndex];
					if (numberValues[valIndex] == (float) auxValue) {
						/* The stored value is an integer value */
						return String.valueOf(auxValue);
					}
					/* The stored value is a float value */
					return String.valueOf(numberValues[valIndex]);
				}
			}
		}
		return "";
	}

	/**
	 * Adds an nominal attribute value.
	 *
	 * @param value The nominal attribute value.
	 */
	public final int addValue(String value) {
		int key = indexOfValue(value);
		if (key == -1) {
			/* The input value is new. */
			key = numValues;
			stringValuesTemp.add(value);
			hashtableString.put(value, new Integer(key));
			++numValues;
		}
		return key;
	}

	/**
	 * Adds an nominal attribute value.
	 *
	 * @param value The nominal attribute value.
	 */
	public final int addValue(float value) {
		int key = indexOfValue(value);
		if (key == -1) {
			/* The input value is new. */
			key = numValues;
			numberValuesTemp.add(value);
			hashtableNumber.put(value, new Integer(key));
			++numValues;
		}
		return key;
	}

	/**
	 * Sets the attIndex of this attribute.
	 *
	 * @param attIndex Index of this attribute
	 */
	public final void setIndex(int attIndex) {
		this.attIndex = attIndex;
	}

	/**
	 * Returns the attribute's values.
	 *
	 * @return the attribute's values.
	 */
	public String[] getDistinticNominalValues() {
		if (attributeType == NOMINAL) {
			if (!isString) {
				String[] values = new String[numberValues.length];
				for (int i = 0; i < numValues; i++) {
					values[i] = String.valueOf(numberValues[i]);
				}
				return values;
			}
			return stringValues.clone(); 
		}
		
		return null;
	}

	/**
	 * Returns the attribute's values.
	 *
	 * @return the attribute's values.
	 */
	public float[] getDistinticNumericValues() {
		if (attributeType == NUMERIC) {
			if (numberValues != null) {
				return numberValues.clone();
			} else {
				int numInstances = instanceSet.numInstances;
				float value;
				
				ArrayList<Float> numberValuesAux = new ArrayList<Float>();
				hashtableNumber = new Hashtable<Float, Integer>(numInstances);
				for (int i = 0; i < numInstances; i++) {
					value = instanceSet.instances[i].data[attIndex];
					if (!hashtableNumber.contains(value)) { 
						hashtableNumber.put(value, new Integer(i));
						numberValuesAux.add(value);
					}
				}
				numValues = numberValuesAux.size();
				numberValues = new float[numValues];
				for (int i = 0; i < numValues; i++) {
					numberValues[i] = numberValuesAux.get(i);
				}
				
				Arrays.sort(numberValues);
			}
		}
		
		return null;
	}

	/** 
	 * Returns the type of this attribute:
	 * 0 - Numeric
	 * 1 - Nominal
	 * 2 - Numeric Cyclic
	 */
	public byte getAttributeType() {
		return attributeType;
	}

	/**
	 * Finalize the construction of this attribute. All values stored in the
	 * hashtable are destroyed. Only affects nominal attributes. 
	 */
	public void setFinal() {
		if (isString) {
			/* Check if it's been finalized already */
			if (stringValuesTemp == null) {
				/* Finalized already. Just return */
				return;
			}
			
			/* Construct the final String vector of values */
			stringValues = new String[numValues];
			for (int i = 0; i < numValues; i++) {
				stringValues[i] = stringValuesTemp.get(i);
			}
			
			/* 
			 * Free up the auxiliar structures used in the construction of this
			 * attribute.
			 */
			hashtableString.clear();
			hashtableString = null;
			stringValuesTemp.clear();
			stringValuesTemp = null;
		} else {
			/* Check if it's been finalized already */
			if (numberValuesTemp == null) {
				/* Finalized already. Just return */
				return;
			}
			
			/* Construct the final float vector of values */
			numberValues = new float[numValues];
			for (int i = 0; i < numValues; i++) {
				numberValues[i] = numberValuesTemp.get(i);
			}
			
			/* 
			 * Free up the auxiliar structures used in the construction of this
			 * attribute.
			 */
			hashtableNumber.clear();
			hashtableNumber = null;
			numberValuesTemp.clear();
			numberValuesTemp = null;
		}
	}
	
	/**
	 * Return the instanceSet to which this attribute is related to.
	 * 
	 * @return
	 */
	public InstanceSet getInstanceSet() {
		return instanceSet;
	}
	
	public void setInstanceSet(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
	}
	
}