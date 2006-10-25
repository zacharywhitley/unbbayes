package unbbayes.datamining.datamanipulation;

import java.util.Enumeration;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Class for handling a set of instances.
 *
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version $1.0 $ (16/02/2002)
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 27/09/2006
 */
public class InstanceSet {
		
	/** The dataset's name. */
	private String relationName;

    /** Stores the name of the counter attribute */
	private String counterAttributeName;

	/** The attribute information. */
	public Attribute[] attributes;
	
	/** 
	 * Stores the type of an attribute:
	 * 0 - Numeric
	 * 1 - Nominal
	 * 2 - Cyclic numeric
	 */
	public byte[] attributeType;

	/** Constant set for numeric attributes. */
	public final static byte NUMERIC = 0;

	/** Constant set for nominal attributes. */
	public final static byte NOMINAL = 1;

	/** Constant set for cyclic numeric attributes. */
	public final static byte CYCLIC = 2;

	/** Number of attributes */
	public int numAttributes;

	/** Number of nominal attributes */
	public int numNominalAttributes;

	/** The collection of instances (compability purposes). */
	public Instance[] instances;

	/** 
	 * The index of the dataset's column that represents the class attribute.
	 * Assume the value -1 in case there is no class attribute.
	 */
	public int classIndex;

	/** 
	 * The index of the dataset's column that represents the counter attribute.
	 * Assumes always the last column of the internal dataset as the counter
	 * attribute.
	 */
	public int counterIndex;

	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes"
			+ ".datamining.datamanipulation.resources.DataManipulationResource");

	/**
	 * Position of the next instance to be inserted when this instanceSet is
	 * under contruction and total number of instances when this instanceSet is
	 * ready.
	 */
	public int numInstances = 0;

	/** Total number of instances considering their weights */
	private int numWeightedInstances;
	
	/**
	 * Constructor creating an empty set of instances.
     * Set class index to be undefined. Sets
	 * the capacity of the set of instances to 0 if it's negative.
	 *
	 * @param capacity The capacity of the new dataset
	 */
	public InstanceSet(int capacity, Attribute[] newAttributes) {
		/* Check capacity */
		if (capacity < 0) {
			capacity = 0;
		}
		
		attributes = newAttributes;
		numAttributes = attributes.length;
		numInstances = 0;
		numWeightedInstances = 0;
		classIndex = -1;
		counterIndex = numAttributes;

		instances = new Instance[capacity];
		attributeType = new byte[numAttributes];
		
		/* Get information related to the attributes */
		numNominalAttributes = 0;
		for (int att = 0; att < numAttributes; att++) {
			attributeType[att] = newAttributes[att].getAttributeType();
			if (attributeType[att] == NOMINAL) {
				++numNominalAttributes;
			}
		}
	}

	/**
	 * Constructor creating an empty set of instances. Copies references
	 * to the header information from the given set of instances. Sets
	 * the capacity of the set of instances to 0 if its negative.
	 * Makes a deep copy.
	 *
	 * @param source Instances from which the header information is to be taken
	 * @param capacity The capacity of the new dataset
	 */
	public InstanceSet(InstanceSet source, int capacity) {
		this (capacity, (source.attributes).clone());
		classIndex = source.classIndex;
		relationName = source.relationName;
	}

	/**
	 * Constructor copying all instances and references to
	 * the header information from the given set of instances.
	 * Makes a deep copy.
	 *
	 * @param source Set to be copied
	 */
	public InstanceSet(InstanceSet source) {
		this(source, source.numInstances);
		source.copyInstances(0, this, source.numInstances);
	}

	/**
	 * Creates a new set of instances by copying a
	 * subset of another set.
	 *
	 * @param source The set of instances from which a subset
	 * is to be created
	 * @param first The index of the first instance to be copied
	 * @param numToCopy The number of instances to be copied
	 * @exception IllegalArgumentException if first and numToCopy are out of range
	 */
	public InstanceSet(InstanceSet source, int first, int numToCopy) {
		this(source, numToCopy);

		if ((first < 0) || ((first + numToCopy) > source.numInstances)) {
			String exception = resource.getString("outOfRange");
			throw new IllegalArgumentException(exception);
		}
		source.copyInstances(first, this, numToCopy);
	}

	/**
	 * Returns the relation's name.
	 *
	 * @return The relation's name as a string
	 */
	public final String getRelationName() {
		return relationName;
	}

	/**
	 * Sets the relation's name.
	 *
	 * @param newName New relation name.
	 */
	public final void setRelationName(String newName) {
		relationName = newName;
	}

	/**
	 * Returns the name of counter attribute.
	 * 
	 * @return The name of counter attribute.
	 */
	public final String getCounterAttributeName() {
		return counterAttributeName;
	}

	public void setCounterAttributeName(String counterAttributeName) {
		this.counterAttributeName = counterAttributeName;
	}

	/**
	 * Returns the number of attributes.
	 *
	 * @return The number of attributes as an integer
	 */
	public final int numAttributes() {
		return numAttributes;
	}

	public Attribute[] getAttributes() {
		return attributes;
	}

	/**
	 * Removes the instance specified by <code>index</code> from this 
	 * instanceSet.
	 * @param index
	 */
	public final void removeInstance(int index) {
		if (index >= 0 && index < numInstances) {
			/* Update the current number of instances */
			numWeightedInstances -= instances[index].data[counterIndex];
			--numInstances;

			Instance[] newInstanceSet = new Instance[numInstances];
			int inst;

			/* Instances before index */
			for (inst = 0; inst < index; inst++) {
				newInstanceSet[inst] = instances[inst];
			}

			/* Instances after index */
			for (; inst < numInstances; inst++) {
				instances[inst] = instances[inst + 1];
				newInstanceSet[inst] = instances[inst + 1];
			}
			instances = newInstanceSet;
		}
	}

	/**
	 * Removes the attribute specified by <code>index</code> from this 
	 * instanceSet.
	 * @param index
	 */
	public final void removeAttribute(int index) {
		if (index >= 0 && index < numAttributes) {
			/* Update the current number of attributes */
			--numAttributes;
			
			//new attributes without index attribute
			Attribute[] newAttributes;
			newAttributes = new Attribute[numAttributes];
			int att;

			// attributes before index
			for (att = 0; att < index; att++) {
				newAttributes[att] = attributes[att];
			}

			//attributes after index
			for (; att < numAttributes; att++) {
				newAttributes[att]=attributes[att + 1];
				newAttributes[att].setIndex(att);
			}
			attributes = newAttributes;
		}
	}

	/**
	 * Returns the number of class labels.
	 *
	 * @return The number of class labels as an integer
	 * @exception UnassignedClassException if the class is not set
	 */
	public final int numClasses() {
		if (classIndex < 0) {
			String exception = resource.getString("runtimeException2");
			throw new UnassignedClassException(exception);
		}
		if (attributeType[classIndex] != NOMINAL) {
			return 1;
		}
		
		return attributes[classIndex].numValues();
	}

	/**
	 * 
	 * Returns the number of instances in the dataset without considering 
	 * their weights.
	 *
	 * @return The number of instances in the dataset as an integer
	 */
	public final int numInstances() {
		return numInstances;
	}

	/**
	 * 
	 * Returns the number of instances in the dataset considering their weights
	 *
	 * @return The number of instances in the dataset as an integer
	 */
	public final int numWeightedInstances() {
		return numWeightedInstances;
	}

	/**
	 * Returns an attribute.
	 *
	 * @param index The attribute's index
	 * @return The attribute at the given position
	 */
	public final Attribute getAttribute(int index) {
		return attributes[index];
	}

	/**
	 * Returns an enumeration of all the attributes except the class attribute.
	 *
	 * @return Enumeration of all the attributes.
	 */
	public final Enumeration enumerateAttributes() {
		return new ArrayListEnumeration(attributes, classIndex);
	}

	/**
	 * Returns an enumeration of all instances in the dataset.
	 *
	 * @return Enumeration of all instances in the dataset
	 */
	public final Enumeration enumerateInstances() {
		return new ArrayListEnumeration(instances);
	}

	/**
	 * Returns the instance at the given position.
	 *
	 * @param index Instance's index
	 * @return The instance at the given position
	 */
	public final Instance getInstance(int index) {
		return instances[index];
	}

	/**
	 * Set an attribute at the given position (0 to numAttributes) and 
	 * sets all values to be missing. IllegalArgumentException if the given 
	 * index is out of range
	 *
	 * @param att The attribute to be inserted
	 * @param position The attribute's position
	 */
	public void setAttributeAt(Attribute att, int position) {
		if (position < 0 || position > numAttributes) {
			String exception = resource.getString("setAttributeAtException");
			throw new IllegalArgumentException(exception);
		}
		attributes[position] = att;
		att.setFinal();

		for (int i = 0; i < numInstances; i++) {
			instances[i].setMissing(att);
		}
	}

	/**
	 * Insert an instance to current Dataset
	 * 
	 * @param newInstance New instance
	 */
	public void insertInstance(Instance newInstance) {
		instances[numInstances] = newInstance;
		numInstances++;
		numWeightedInstances += newInstance.data[counterIndex];
		newInstance.setInstanceSet(this);
	}
	
	/**
	 * Sets the class attribute.
	 *
	 * @param att Attribute to be the class
	 */
	public final void setClass(Attribute att) {
		classIndex = att.getIndex();
	}

	/**
	 * Returns the class attribute's index. Returns negative number
	 * if it's undefined.
	 *
	 * @return The class index as an integer
	 */
	public final int getClassIndex() {
		return classIndex;
	}

	/**
	 * Sets the class index of the set.
	 * If the class index is negative there is assumed to be no class.
	 * (ie. it is undefined)
	 *
	 * @param classIndex New class index
	 * @exception IllegalArgumentException if the class index is bigger than
	 * number of attributes or less than 0
	 */
	public final void setClassIndex(int classIndex) {
		if (classIndex >= numAttributes) {
			String exception = resource.getString("setClassIndexException");
			throw new IllegalArgumentException(exception + classIndex);
		}
		this.classIndex = classIndex;
	}

	/**
	 * Returns the class attribute.
	 *
	 * @return The class attribute
	 * @exception UnassignedClassException if the class is not set
	 */
	public final Attribute getClassAttribute() {
		if (classIndex < 0) {
			String exception = resource.getString("runtimeException2");
			throw new UnassignedClassException(exception);
		}
		return attributes[classIndex];
	}

	/**
	 * Removes all instances with a missing class value
	 * from the dataset.
	 *
	 * @exception UnassignedClassException if class is not set
	 */
	public final void deleteWithMissingClass() {
		if (classIndex < 0) {
			String exception = resource.getString("runtimeException2");
			throw new UnassignedClassException(exception);
		}
		deleteWithMissing(classIndex);
	}

	/**
	 * Removes all instances with missing values for a particular
	 * attribute from the dataset.
	 *
	 * @param attIndex Attribute's index
	 */
	public final void deleteWithMissing(int attIndex) {
		for (int inst = 0; inst < numInstances; inst++) {
			if (getInstance(inst).isMissing(attIndex)) {
				removeInstance(inst);
			}
		}
	}

	/**
	 * Copies instances from one set to the end of another one
	 *
	 * @param from Position of the first instance to be copied
	 * @param dest Destination for the instances
	 * @param num Number of instances to be copied
	 */
	public void copyInstances(int start, InstanceSet destination, int end) {
		for (int inst = start; inst < end; inst++) {
			destination.instances[destination.numInstances] = instances[inst].clone();
			destination.numInstances++;
			destination.numWeightedInstances += 
				instances[inst].data[counterIndex];
		}
	}

	/**
	 * Calculates summary statistics on the values that appear in each
	 * attribute and return then as an vector. 
	 *
	 * @param index The index of the attribute to summarize.
	 * @return An AttributeStats object with it's fields calculated.
	 */
	public AttributeStats[] computeAttributeStats() {
		AttributeStats[] attributeStats = new AttributeStats[numAttributes];
		
		for (int att = 0; att < numAttributes; att++) {
			attributeStats[att] = new AttributeStats(this, attributes[att]);
		}
		
		return attributeStats;
	}

	/**
	 * Shuffles the instances in the set so that they are ordered randomly
	 *
	 * @param random A random number generator
	 */
	public final void randomize(Random random) {
		int index;
		
		for (int i = numInstances - 1; i > 0; i--) {
			/* Randomly get an instance index */ 
			index = (int) (random.nextDouble() * (double) i);
			
			/* swap index with the current instance */
			Instance temp = instances[i];
			instances[i] = instances[index];
			instances[index] = temp;
		}
	}

	/**
	 * Swaps two instances.
	 *
	 * @param first Index of the first element
	 * @param second Index of the second element
	 */
	public final void swap(int first, int second) {
		Instance temp = instances[first];
		instances[first] = instances[second];
		instances[second] = temp;
	}

	/**
	 * Verifies if there is any numeric attribute in the dataset.
	 * @return True if there is at leats one numeric attribute in the dataset
	 */
	public boolean checkNumericAttributes() {
		for (int i = 0; i < numAttributes; i++) {
			if (getAttribute(i).isNumeric()) {
				return true;
			}
		}
		return false;
    }

	/**
	 * Returns the dataset as a string in ARFF format.
	 *
	 * @return the dataset in ARFF format as a string
	 */
	public final String toString() {
		StringBuffer text = new StringBuffer();

		text.append("@relation " + relationName + "\n\n");
		for (int i = 0; i < numAttributes; i++) {
			text.append(getAttribute(i) + "\n");
		}
		text.append("\n@data\n");
		
		int lastPosition = numInstances - 1;
		for (int i = 0; i < lastPosition; i++) {
			text.append(instances[i]);
			text.append('\n');
		}
		text.append(getInstance(lastPosition));
				
		return text.toString();
	}

	/**
	 * Returns the index of the first instance of the dataset that matches the
	 * input instance values.
	 * 
	 * @param instance The input instance.
	 * @return The index of the first instance of the dataset that matches the
	 * input instance.
	 */
	public int find(float[] data) {
		boolean equal = true;

		for (int inst = 0; inst < numInstances; inst++) {
			equal = true;
			for (int att = 0; att < numAttributes; att++) {
				if (instances[inst].data[att] != data[att]) {
					equal = false;
					break;
				}
			}
			if (equal) {
				return inst;
			}
		}
		return -1;
	}
	
	/**
	 * Returns an instance's class value in internal format. (ie. as a int
	 * number).
	 *
	 * @return The internal value representing the instance's class.
	 * @exception UnassignedClassException if the class is not set.
	 */
	public final int classValue(int inst) {
		if (classIndex < 0) {
			String exception = resource.getString("runtimeException2");
			throw new UnassignedClassException(exception);
		}
		return (int) instances[inst].data[classIndex];
	}

	/**
	 * Returns the string value of an attribute for an instance.
	 *
	 * @param attIndex Attribute's index
	 * @return The value as a string
	 */
	public String stringValue(int inst, int att) {
		return attributes[att].value((int) instances[inst].data[att]);
	}

	/**
	 * Finalizes the construction of all nominal attributes and frees up all
	 * auxiliar structures utilized by them. 
	 * 
	 */
	public void setFinal() {
		for (int i = 0; i < numAttributes; i++) {
			if (attributeType[i] == NOMINAL) {
				attributes[i].setFinal();
			}
		}
	}
	
	/**
	 * Increase the capacity of this instanceSet by the specified quantity. The
	 * capacity of this instanceSet is the number of instances it can hold.
	 * 
	 * @param num The quantity this instanceSet will grow up.
	 * @return
	 */
	public void sizeUp(int num) {
		Instance[] newInstances = new Instance[numInstances + num]; 
		
		for (int inst = 0; inst < numInstances; inst++) {
			newInstances[inst] = instances[inst];
		}
		
		instances = newInstances;
	}

	/**
	 * Set the set of instances to the informed vector of instances.
	 * 
	 * @param newVec
	 */
	public void setInstances(Instance[] newVec) {
		instances = newVec;
	}
}