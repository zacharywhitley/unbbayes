package unbbayes.datamining.datamanipulation;

import java.io.Serializable;
import java.util.ResourceBundle;

/**
 * Class for handling an instance. All values (numeric or nominal) are
 * internally stored as float numbers. The stored value is the index of the
 * corresponding nominal value in the attribute's definition.
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class Instance implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("" +
			"unbbayes.datamining.datamanipulation.resources." +
			"DataManipulationResource");

	/**
	 * The instanceSet the instance has access to.  Null if the instance
	 * doesn't have access to any instanceSet.  Only if an instance has
	 * access to a instanceSet, it knows about the actual attribute types.
	 */
	private InstanceSet instanceSet;

	/** 
	 * The instance from the dataset of the instanceSet to wich this is
	 * pointing to.
	 */
	public float[] data;

	/** 
	 * The index of the dataset's column that represents the counter attribute.
	 * Assumes always the last column of the internal dataset as the counter
	 * attribute.
	 */
	protected int counterIndex;

	/**
	 * The number of attributes.
	 */
	private int numAttributes;

	/** Constant representing a missing value. */
	public final static float MISSING_VALUE = Float.NaN;

	/**
 	 * Constructor that copies the attribute values from
 	 * the given instance. Reference to the dataset is set to null.
 	 * (ie. the instance doesn't have access to information about the
 	 * attribute types).
 	 *
 	 * @param instance Instance from which the attribute
	 * values are to be copied
 	 */
	public Instance(Instance instance) {
		data = instance.data.clone();
		numAttributes = instance.numAttributes;
		counterIndex = instance.counterIndex;
		instanceSet = null;
	}

	/**
	 * 
	 *
	 */
	public Instance(int numAttributes) {
		this.numAttributes = numAttributes;
		
		/* Allocates space for the attribute values and weight */
		data = new float[numAttributes + 1];
		counterIndex = numAttributes;
		instanceSet = null;
	}

	/**
	 * Create an instance and set the input <code>data</code> vector as its
	 * <code>data</code> vector. Note that changings to the input
	 * <code>data</code> vector do affect this instance <code>data</code>
	 * vector. Reference to the dataset is set to null. (ie, the instance
 	 * doesn't have access to information about its attributes).
	 *
 	 * @param attValues An array of attribute values
 	 */
	public Instance(float[] data) {
		this.data = data;
		instanceSet = null;
		
		/* Set the number of attributes (number of columns - 1) */
		numAttributes = data.length - 1;
		
		/* Set counter index to the data's last column */
		counterIndex = numAttributes;
	}

	/**
	 * Sets the weight of an instance.
	 *
	 * @param weight the weight
	 */
	public final void setWeight(float weight) {
		data[counterIndex] = weight;
	}

	/**
	 * Returns the instance's weight.
	 *
	 * @return the instance's weight as a double
	 */
	public final float getWeight() {
		return data[counterIndex];
	}

	/**
	 * Returns the class attribute's index.
	 *
	 * @return The class index as an integer
	 * @exception UnassignedDatasetException if instance doesn't have access
	 * to a instanceSet.
	 */
	public final int getClassIndex() {
		return instanceSet.classIndex;
	}

	/**
	 * Tests if an instance's class is missing.
	 *
	 * @return True if the instance's class is missing
	 * @exception UnassignedClassException if the class is not set or the
	 * instance doesn't have access to a instanceSet
	 */
	public final boolean classIsMissing() {
		if (instanceSet.classIndex < 0) {
			throw new UnassignedClassException(resource.getString("" +
					"runtimeException2"));
		}
		return data[instanceSet.classIndex] == MISSING_VALUE;
	}

	/**
	 * Returns an instance's class value in internal format. (ie. as a
	 * float number)
	 *
	 * @return The corresponding value as a float (It returns the
	 * value's index as a float).
	 * @exception UnassignedClassException if the class is not set or the
	 * instance doesn't have access to a instanceSet
	 */
	public final int classValue() {
		if (instanceSet.classIndex < 0) {
			throw new UnassignedClassException(resource.getString("" +
					"runtimeException2"));
		}
		return (int) data[instanceSet.classIndex];
	}

	/**
	 * Tests if a specific value is "missing".
	 *
	 * @param attIndex Attribute's index
	 */
	public final boolean isMissing(int attIndex) {
		return data[attIndex] == MISSING_VALUE;
	}

	/**
	 * Tests if a specific value is "missing".
	 * The given attribute has to belong to a instanceSet.
	 *
	 * @param att Attribute
	 */
	public final boolean isMissing(Attribute att) {
		return data[att.getIndex()] == MISSING_VALUE;
	}

	/**
	 * Tests if the given value codes "missing".
	 *
	 * @param val Value to be tested
	 * @return true if val codes "missing"
	 */
	public static final boolean isMissingValue(float val) {
		 return Float.isNaN(val);
	}


	/**
	 * Returns the float that codes "missing".
	 *
	 * @return the float that codes "missing"
	 */
	public static final float missingValue() {
		return MISSING_VALUE;
	}

	/**
	 * Sets the class value of an instance to be "missing".
	 *
	 * @exception UnassignedClassException if the class is not set
	 * @exception UnassignedDatasetException if the instance doesn't
	 * have access to a instanceSet
	 */
	public void setClassMissing() {
		if (instanceSet.classIndex < 0) {
			throw new UnassignedClassException(resource.getString("" +
					"runtimeException2"));
		}
		setMissing(instanceSet.classIndex);
	}

	/**
	 * Sets a specific value to be "missing".
	 *
	 * @param attIndex the attribute's index
	 */
	public void setMissing(int attIndex) {
		data[attIndex] = MISSING_VALUE;
	}

	/**
	 * Sets a specific value to be "missing".
	 *
	 * @param att The desired attribute
	 */
	public void setMissing(Attribute att) {
		data[att.getIndex()] = MISSING_VALUE;
	}

	/**
	 * Returns the reference to the instanceSet.
	 *
	 * @return instances Reference to the instanceSet
	 */
	public final InstanceSet getInstanceSet() {
		return instanceSet;
	}

	/**
	 * Returns the value at an attribute position for this instance.
	 *
	 * @param attIndex Attribute's index
	 * @return The value as a string
	 */
	public String stringValue(int attIndex) {
		if (instanceSet.attributeType[attIndex] == instanceSet.NOMINAL) {
			return instanceSet.attributes[attIndex].value((int) data[attIndex]);
		}
		return String.valueOf(data[attIndex]);
	}

	/**
	 * Returns the value of an attribute for the instance.
	 *
	 * @param att Attribute
	 * @return The value as a string
	 */
	public String stringValue(Attribute att) {
		return stringValue(att.getIndex());
	}

	/**
	 * Returns the description of one instance.
	 *
	 * @return The instance's description as a string
	 */
	public String toString() {
		StringBuffer text = new StringBuffer();
		
		/* Get the attributes' values */
		if (numAttributes > 1) {
			for (int i = 0; i < numAttributes; i++) {
				if (i != 0) {
					text.append(",");
				}
				if (data[i] == MISSING_VALUE) {
					text.append("?");
				} else {
					text.append(stringValue(i));
				}
			}
		}
		
		/* Get the weight */
		text.append(" W " + data[counterIndex]);
		
		return text.toString();
	}

	/**
	 * Returns an instance's attribute value in internal format.
	 *
	 * @param attIndex Attribute's index
	 * @return The specified value as a float
	 */
	public final float getValue(int attIndex) {
		return data[attIndex];
	}

	/**
	 * Sets an instance's attribute value in internal format.
	 *
	 * @param attIndex Attribute's index
	* @param newValue New value as a float
	 */
	public final void setValue(int attIndex, float newValue) {
		data[attIndex] = newValue;
	}

	/**
	 * Returns an instance's attribute value in internal format.
	 * The given attribute has to belong to a instanceSet.
	 *
	 * @param att Attribute
	 * @return The specified value as a float
	 */
	public final float getValue(Attribute att) {
		return data[att.getIndex()];
	}

	/**
	 * Sets an instance's attribute value in internal format.
	 * The given attribute has to belong to a instanceSet.
	*
	 * @param att Attribute
	* @param newValue New value as a float
	 */
	public final void setValue(Attribute att, float newValue) {
		data[att.getIndex()] = newValue;
	}
	
	/**
	 * Increase the weight of this instance with the specified value.
	 * 
	 * @param weight: the desired amount to add to the weight of this instance.
	 */
	public void addWeight(float weight) {
		data[counterIndex] += weight;
	}

	/**
	 * Sets the index of this instance.
	 * @param index Index of this instance
	 * 
	 */
	public final void setInstance(float[] data) {
		this.data = data;
	}
	
	public final Instance clone() {
		Instance instance = new Instance(this);
		instance.instanceSet = instanceSet;
		return instance;
	}

	/**
	 * Binds this instance to the informed instanceSet.
	 * 
	 * @param instanceSet
	 */
	public void setInstanceSet(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
	}
	
	/**
	 * Removes an attribute.
	 * 
	 * @param index
	 */
	public void removeAttribute(int index) {
		for (int inst = index; inst < data.length - 1; inst++) {
			data[inst] = data[inst + 1];
		}
		--numAttributes;
		--counterIndex;
	}
	
}