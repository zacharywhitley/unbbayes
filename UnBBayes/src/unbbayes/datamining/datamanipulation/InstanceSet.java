package unbbayes.datamining.datamanipulation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Class for handling a set of instances.
 *
 * @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br) - first version
 * @version $1.0 $ (16/02/2002)
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br - second version
 * @version $2.0 $ (16/10/2006)
 * @date 27/09/2006
 */
public class InstanceSet implements Serializable {

	private static final long serialVersionUID = 1L;

	/*** The precision parameter used for numeric attributes */
	protected static final float DEFAULT_NUM_PRECISION = 0.01f;

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

	/** Constant set for numeric attributes and instanceSets. */
	public final static byte NUMERIC = 0;

	/** Constant set for nominal attributes and instanceSets. */
	public final static byte NOMINAL = 1;

	/** Constant set for cyclic numeric attributes. */
	public final static byte CYCLIC = 2;

	/** Constant set for mixed instanceSets. */
	public final static byte MIXED = 2;

	/** Number of attributes */
	public int numAttributes;

	/** Number of nominal attributes */
	public int numNominalAttributes;

	/** Number of numeric attributes */
	public int numNumericAttributes;
	
	/** Number of cyclic attributes */
	public int numCyclicAttributes;
	
	/** The collection of instances */
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

	/** Load resource file for this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes"
			+ ".datamining.datamanipulation.resources.DataManipulationResource");

	/**
	 * Position of the next instance to be inserted when this instanceSet is
	 * under contruction and total number of instances when this instanceSet is
	 * ready.
	 */
	public int numInstances = 0;

	/** Total number of instances considering their weights */
	public int numWeightedInstances;

	/** Tells if this instanceSet is compacted */
	private boolean compactedFile;

	private boolean[] attributeHasChanged;

	private AttributeStats[] attributeStats;

	/** 
	 * Stores the type of this instanceSet:
	 * 
	 * 0 - Numeric
	 * 1 - Nominal
	 * 2 - Mixed
	 */
	private int instanceSetType;

	/** Random used in the sorting of the instances */
	private static Random rnd;

	private boolean hasChanged;

	private float[] distribution;

	/** Maps the instancesIDs to the actual instances */
	public Hashtable<Integer, Integer> instancesIDs;

	/**
	 * Constructor creating an empty set of instances.
	 * Set class index to be undefined. Sets
	 * the capacity of the set of instances to 0 if it's negative.
	 *
	 * @param capacity The capacity of the new dataset.
	 * @param newAttributes Array with the attributes.
	 * @param counterIndex Index of the counter attribute.
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
		
		/* The counter attribute is always the last column */
		counterIndex = numAttributes;

		instances = new Instance[capacity];
		attributeType = new byte[numAttributes];
		instancesIDs = new Hashtable<Integer, Integer>();
		
		/* Get information related to the attributes */
		numNominalAttributes = 0;
		numNumericAttributes = 0;
		numCyclicAttributes = 0;
		for (int att = 0; att < numAttributes; att++) {
			attributeType[att] = newAttributes[att].getAttributeType();
			attributes[att].setInstanceSet(this);
			switch (attributeType[att]) {
				case NOMINAL:
					++numNominalAttributes;
					break;
				case NUMERIC:
					++numNumericAttributes;
					break;
				case CYCLIC:
					++numCyclicAttributes;
					break;
			}
		}
		
		/* Set flag that tells that an attribute has been changed */
		attributeHasChanged = new boolean[numAttributes];
		Arrays.fill(attributeHasChanged, true);
		hasChanged = true;
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
		classIndex = source.classIndex;
		relationName = source.relationName;
		counterIndex = source.counterIndex;
		if (source.counterAttributeName != null) {
			this.counterAttributeName = new String(source.counterAttributeName);
		}
		numAttributes = source.numAttributes;
		numCyclicAttributes = source.numCyclicAttributes;
		numNominalAttributes = source.numNominalAttributes;
		numNumericAttributes = source.numNumericAttributes;
		instanceSetType = source.instanceSetType;

		attributeHasChanged = new boolean[numAttributes];
		attributeType = new byte[numAttributes];
		attributes = new Attribute[numAttributes];
		instances = new Instance[capacity];
		attributeStats = new AttributeStats[numAttributes];
		instancesIDs = new Hashtable<Integer, Integer>();
		
		for (int att = 0; att < numAttributes; att++) {
			attributes[att] = new Attribute(source.attributes[att]);
			attributes[att].setInstanceSet(this);
			attributeType[att] = source.attributeType[att];
			attributeHasChanged[att] = true;
		}
		
		hasChanged = true;
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
		source.copyInstancesTo(this, 0, source.numInstances);
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
		source.copyInstancesTo(this, first, numToCopy);
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
	public InstanceSet(InstanceSet source, int[] instancesIDs) {
		this(source, instancesIDs.length);
		source.copyInstancesTo(this, instancesIDs);
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
		if (counterAttributeName != null){
			this.counterAttributeName = counterAttributeName;
		}
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
	 * 
	 * @param index The index of the instance to be removed.
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
			
			Arrays.fill(attributeHasChanged, true);
			hasChanged = true;
		}
	}

	/**
	 * Removes from this instanceSet those instances marked as <code>true
	 * </code> in the input array of indexes <code>deleteIndex</code>.
	 * 
	 * @param deleteIndex The index of the instance to be removed.
	 */
	public final void removeInstances(int[] instancesIDs) {
		boolean[] deleteIndex = new boolean[numInstances];
		Arrays.fill(deleteIndex, false);
		
		for (int i = 0; i < instancesIDs.length; i++) {
			deleteIndex[instancesIDs[i]] = true;
		}
		removeInstances(deleteIndex);
	}
	
	/**
	 * Removes from this instanceSet those instances marked as <code>true
	 * </code> in the input array of indexes <code>deleteIndex</code>.
	 * 
	 * @param deleteIndex The index of the instance to be removed.
	 */
	public final void removeInstances(boolean[] deleteIndex) {
		/* Get the current size */
		int currentSize = instances.length;
		int deleteSize = deleteIndex.length;
		
		/* Count the number of instances to be removed */
		int numInstancesToBeRemoved = 0;
		for (int inst = 0; inst < deleteSize; inst++) {
			if (deleteIndex[inst]) {
				++numInstancesToBeRemoved;
			}
		}
		
		if (numInstancesToBeRemoved == 0) {
			return;
		}
		
		/* Create new instanceSet */
		int newSize = currentSize - numInstancesToBeRemoved;
		Instance[] newInstanceSet = new Instance[newSize];

		/*
		 * Make a new instanceSet with only the instances not marked for 
		 * deletion.
		 */
		instancesIDs = new Hashtable<Integer, Integer>(newSize);
		int newInstanceIndex = 0;
		for (int inst = 0; inst < deleteSize; inst++) {
			if (deleteIndex[inst]) {
				/* Remove instance */
				--numInstances;
				numWeightedInstances -= instances[inst].data[counterIndex];
				continue;
			}
			newInstanceSet[newInstanceIndex] = instances[inst];
			instancesIDs.put(instances[inst].getInstanceID(), newInstanceIndex);
			++newInstanceIndex;
		}
		
		if (deleteSize < currentSize) {
			for (int inst = deleteSize; inst < currentSize; inst++) {
				newInstanceSet[newInstanceIndex] = instances[inst];
				instancesIDs.put(instances[inst].getInstanceID(), newInstanceIndex);
				++newInstanceIndex;
			}
		}
		
		/* Set the current instanceSet to the new one just created */
		instances = newInstanceSet;
		
		Arrays.fill(attributeHasChanged, true);
		hasChanged = true;
	}

	/**
	 * Removes the attribute specified by <code>index</code> from this 
	 * instanceSet.
	 * @param index
	 */
	public final void removeAttribute(int index) {
		if (index >= 0 && index < numAttributes) {
			/* Update information about attributes */
			switch (attributeType[index]) {
				case NOMINAL:
					--numNominalAttributes;
					break;
				case NUMERIC:
					--numNumericAttributes;
					break;
				case CYCLIC:
					--numCyclicAttributes;
					break;
			}

			/* Update the current number of attributes */
			--numAttributes;
			
			//new attributes without index attribute
			Attribute[] newAttributes = new Attribute[numAttributes];
			byte[] newAttributeType = new byte[numAttributes];
			boolean[] newAttributeHasChanged = new boolean[numAttributes];
			
			int att;

			// attributes before index
			for (att = 0; att < index; att++) {
				newAttributes[att] = attributes[att];
				newAttributeType[att] = attributeType[att];
				newAttributeHasChanged[att] = attributeHasChanged[att];
			}

			//attributes after index
			for (; att < numAttributes; att++) {
				newAttributes[att] = attributes[att + 1];
				newAttributes[att].setIndex(att);
				newAttributeType[att] = attributeType[att + 1];
				newAttributeHasChanged[att] = attributeHasChanged[att + 1];
			}
			attributes = newAttributes;
			attributeType = newAttributeType;
			attributeHasChanged = newAttributeHasChanged;
			
			/* Remove the attribute from the instances */
			for (int inst = 0; inst < numInstances; inst++) {
				instances[inst].removeAttribute(index);
			}
			
			hasChanged = true;
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
	 * Returns the instance at the given position.
	 *
	 * @param index Instance's index
	 * @return The instance at the given position
	 */
	public final Instance getInstanceByID(int instanceID) {
		return instances[instancesIDs.get(instanceID)];
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
		attributes[position].setInstanceSet(this);
		att.setFinal();
		attributeHasChanged[position] = true;
		
		for (int i = 0; i < numInstances; i++) {
			instances[i].setMissing(att);
		}
		
		hasChanged = true;
	}

	/**
	 * Insert an instance to current Datasets
	 * 
	 * @param newInstance New instance
	 */
	public void insertInstance(Instance newInstance) {
		instances[numInstances] = newInstance;
		newInstance.setInstanceSet(this);
		instancesIDs.put(newInstance.getInstanceID(), numInstances);
		numInstances++;
		numWeightedInstances += newInstance.data[counterIndex];
		
		Arrays.fill(attributeHasChanged, true);
		hasChanged = true;
	}
	
	/**
	 * Insert an instance to current Datasets
	 * 
	 * @param newInstance New instance
	 */
	public void insertInstance(float[] values) {
		insertInstance(new Instance(values, numInstances));
	}
	
	/**
	 * Sets the class attribute.
	 *
	 * @param att Attribute to be the class
	 */
	public final void setClass(Attribute att) {
		classIndex = att.getIndex();
		
		hasChanged = true;
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
		
		hasChanged = true;
	}

	/**
	 * Check if multiclass dataset.
	 */
	public boolean isMultiClass() {
		if (numClasses() > 2) {
			return true;
		} else {
			return false;
		}
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
		
		Arrays.fill(attributeHasChanged, true);
		hasChanged = true;
	}

	/**
	 * Copies instances from one set to the end of another one. It copies all
	 * instances if the specified quantity of instances is greater than the
	 * number of instances of this instanceSet or is less than 0.
	 * 
	 * @param from Position of the first instance to be copied
	 * @param dest Destination for the instances
	 * @param qtd Number of instances to be copied
	 */
	public void copyInstancesTo(InstanceSet destination, int start, int qtd) {
		if (qtd > numInstances || qtd < 0) {
			qtd = numInstances;
		}
		int inst = start;
		int counter = 0;
		while (counter  < qtd) {
			destination.insertInstance(new Instance(instances[inst]));
			++inst;
			++counter;
		}
		Arrays.fill(destination.attributeHasChanged, true);
		destination.hasChanged = true;
	}

	/**
	 * Copies instances from one set to the end of another one. It copies all
	 * instances specified in the array of int instancesIDs. It does not check
	 * if the instances specified in the input array exists.
	 * 
	 * @param from Position of the first instance to be copied
	 * @param instancesIDs The instances index positions desired
	 */
	public void copyInstancesTo(InstanceSet destination, int[] instancesIDs) {
		int numInstancesIDs = instancesIDs.length;
		int inst;
		for (int i = 0; i < numInstancesIDs; i++) {
			inst = instancesIDs[i];
			destination.insertInstance(new Instance(instances[inst]));
		}
		
		Arrays.fill(destination.attributeHasChanged, true);
		destination.hasChanged = true;
	}

	/**
	 * Calculates summary statistics on the values that appear in each
	 * attribute and return then as a vector. 
	 * 
	 * @return An AttributeStats object with it's fields calculated.
	 */
	public AttributeStats[] getAttributeStats() {
		if (attributeStats == null) {
			attributeStats = new AttributeStats[numAttributes];
			Arrays.fill(attributeHasChanged, true);
		}
		
		for (int att = 0; att < numAttributes; att++) {
			if (attributeHasChanged[att]) {
				attributeStats[att] = new AttributeStats(this, attributes[att]);
			}
		}
		Arrays.fill(attributeHasChanged, false);
		
		return attributeStats;
	}

	/**
	 * Calculates summary statistics on the values that appear in each
	 * attribute and return then as a vector. 
	 * 
	 * @return An AttributeStats object with it's fields calculated.
	 */
	public AttributeStats getAttributeStats(int attIndex) {
		if (attributeStats == null || attributeStats[attIndex] == null) {
			attributeStats = new AttributeStats[numAttributes];
			Arrays.fill(attributeHasChanged, true);
		}
		
		attributeStats[attIndex] = new AttributeStats(this, attributes[attIndex]);
		attributeHasChanged[attIndex] = false;

		return attributeStats[attIndex];
	}

	/**
	 * Shuffles the instances in the set so that they are ordered randomly
	 *
	 * @param random A random number generator
	 */
	public final void randomize(Random random) {
		int index;
		
		instancesIDs = new Hashtable<Integer, Integer>(numInstances);
		for (int i = numInstances - 1; i > 0; i--) {
			/* Randomly get an instance index */ 
			index = (int) (random.nextDouble() * (double) i);
			
			/* swap index with the current instance */
			Instance temp = instances[i];
			instances[i] = instances[index];
			instances[index] = temp;
			instancesIDs.put(instances[index].getInstanceID(), index);
		}
	}

	/**
	 * Returns a shuffled index of the instanceSet so that they are ordered
	 * randomly.
	 *
	 * @param random A random number generator
	 */
	public final int[] randomizeIndex(Random random) {
		int[] index = new int[numInstances];

		for (int i = numInstances - 1; i > 0; i--) {
			/* Randomly get an instance index */ 
			index[i] = (int) (random.nextDouble() * (double) i);
		}
		
		return index;
	}

	/**
	 * Swaps two instances.
	 *
	 * @param first Index of the first element
	 * @param second Index of the second element
	 */
	public final void swap(int first, int second) {
		instancesIDs.remove(instances[first].getInstanceID());
		instancesIDs.remove(instances[second].getInstanceID());
		Instance temp = instances[first];
		instances[first] = instances[second];
		instances[second] = temp;
		instancesIDs.put(instances[first].getInstanceID(), first);
		instancesIDs.put(instances[second].getInstanceID(), second);
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
	 * input instance values. Returns -1 if no instance is found. 
	 * 
	 * @param instance The input instance.
	 * @return The index of the first instance of the dataset that matches the
	 * input instance or -1 if none matches.
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
			attributes[i].setInstanceSet(this);
			if (attributeType[i] == NOMINAL) {
				attributes[i].setFinal();
			}
		}
		setInstanceSetType();
		
		Arrays.fill(attributeHasChanged, true);
		hasChanged = true;
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
	 *  Compacts this instanceSet. The idea is very simple and efficient: First
	 *  we get a sorted index of this instanceSet (quicksort). A sorted index
	 *  assures us that all equal instances (instances with equal attributes'
	 *  values) are all packed together. With the sorted index, we identify
	 *  all packs (subset with equal instances) and sum up their weight. Then
	 *  for each pack, we set to its head (its first instance) the total pack's
	 *  weight. All other instances are marked to be removed (and removed
	 *  aterwards).
	 */ 
	public void compact() {
		/* Get array with the sorted index of the instanceSet */
		int[] instancesIDs = sortAscending();
		
		/* First instance of a pack */
		int head;

		/* Auxiliar that tells when a match occurs between two instances */
		boolean match;
		
		/* The total weight of all instances inside a pack */
		float packWeight;
		
		/* 
		 * Array that tells which instances are not packs' head instances and
		 * will be removed afterwards.
		 */ 
		boolean[] remove = new boolean[numInstances];
		Arrays.fill(remove, false);
		
		/* 
		 * Loop through the sorted index finding each pack, computing its
		 * weight (the sum of its instances), setting to its head (first
		 * instance) the pack's weight and marking all other instances to be
		 * removed.
		 */ 
		int inst = 0;
		float[] headInstance;
		float[] currentInstance;
		while (inst < numInstances) {
			/* Find the tail (last instance) of the current pack */
			match = true;
			head = inst;
			packWeight = instances[instancesIDs[head]].data[counterIndex];
			++inst;
			while (match && inst < numInstances) {
				/* 
				 * Compare the 'currentInstance' instance with the current
				 * pack's head instance.
				 */ 
				currentInstance = instances[instancesIDs[inst]].data;
				headInstance = instances[instancesIDs[head]].data;
				for (int att = 0; att < numAttributes; att++) {
					if (currentInstance[att] != headInstance[att]) {
						match = false;
						break;
					}
				}
				
				/* 
				 * Check if the 'currentInstance' instance matches with the
				 * current pack's first instance.
				 */
				if (match) {
					/* Update the total pack's weight */
					packWeight += currentInstance[counterIndex];
					
					/* 
					 * The method 'removeInstances' will remove the packed
					 * instances and decrease the value of numWeightedInstances.
					 * Then, it's necessary to increase its value according, so
					 * it keeps the right number of weighted instances.
					 */
					numWeightedInstances += currentInstance[counterIndex];
					
					/* Mark the current instance to be removed afterwards */
					remove[instancesIDs[inst]] = true;
					
					/* Get the next instance */
					++inst;
				}
			}
			
			/* The pack's head gets the total pack's weight */
			instances[instancesIDs[head]].data[counterIndex] = packWeight;
		}
		
		/* Remove those instances marked to be removed */
		removeInstances(remove);
		
		hasChanged = true;
	}

	/**
	 * Builds a training and a test instanceSets from this current instanceSet.
	 * The test instanceSet will be created with <code>testSize</code>
	 * instances and the rest will remains for the training instanceSet. For
	 * matters of space, the current instanceSet will become the training
	 * instanceSet and <code>testSize</code> instances will be removed to the
	 * the test instanceSet (randomly selected). Then it returns the test
	 * instanceSet.
	 * @param classIndex TODO
	 * @param testSize The desired size of the new test instanceSet.
	 * 
	 * @return The test instanceSet just created.
	 */
	public InstanceSet buildTrainTestSet(float proportion, boolean compact, int classIndex) {
		Random randomizer = new Random(new Date().getTime());
		int testSize = Math.round(numInstances * proportion);
		
		int numClasses;
		int[] count = null;
		int[][] instancesIDs = null;
		
		if (classIndex != -1) {
			numClasses = attributes[classIndex].numValues();
	
//			/* For safety purpouses */
//			testSize += numClasses;
				
			/* Count instances per class */
			count = new int[numClasses];
			Arrays.fill(count, 0);
			for (int classValue, inst = 0; inst < numInstances; inst++) {
				classValue = (int) instances[inst].data[classIndex];
				++count[classValue];
			}
			
			/* Separate the instances by their classes */
			instancesIDs = new int[numClasses][];
			for (int classValue = 0; classValue < numClasses; classValue++) {
				instancesIDs[classValue] = new int[count[classValue]];
			}
			Arrays.fill(count, 0);
			for (int classValue, inst = 0; inst < numInstances; inst++) {
				classValue = (int) instances[inst].data[classIndex];
				instancesIDs[classValue][count[classValue]] = inst;
				++count[classValue];
			}
		} else {
			numClasses = 1;
			count = new int[1];
			count[0] = numInstances;
			instancesIDs = new int[1][numInstances];
			for (int inst = 0; inst < numInstances; inst++) {
				instancesIDs[0][inst] = inst;
			}
		}
		
		/* Create test instanceSet with no instances */
		InstanceSet testSet = new InstanceSet (this, testSize);
		testSet.setCounterAttributeName(counterAttributeName);

		/* Array that tells which instance will pertain to the test set */ 
		boolean[] testSetIndex = new boolean[numInstances];
		Arrays.fill(testSetIndex, false);

		/* Auxiliary array: tells when an instance has not been chosen yet */
		boolean[] notUsed = new boolean[numInstances];
		Arrays.fill(notUsed, true);
		
		/* 
		 * Randomly choose the instances for the test set using a stratified
		 * approach for the class
		 */
		int index = 0;
		int counter;
		int inst;
		int max;
		for (int classValue = 0; classValue < numClasses; classValue++) {
			/* Choose randomly the instances to the test set */
			counter = 0;
			max = Math.round((count[classValue] * proportion));
			while (counter < max) {
				inst = randomizer.nextInt(count[classValue]);
				inst = instancesIDs[classValue][inst];
				if (notUsed[inst]) {
					testSet.instances[index] = instances[inst];
					testSet.instances[index].setInstanceSet(testSet);
					testSet.numInstances++;
					testSet.numWeightedInstances += 
						instances[inst].data[counterIndex];
					testSetIndex[inst] = true;
					++counter;
					++index;
					
					/* Discard the instance 'inst' */
					notUsed[inst] = false;
				}
			}
		}
		
		/* Remove all instances marked to be removed */
		removeInstances(testSetIndex);
		
		/* Compact both training and test instanceSets */
		if (compact) {
			compact();
			testSet.compact();
		}
		
		Arrays.fill(attributeHasChanged, true);
		Arrays.fill(testSet.attributeHasChanged, true);
		hasChanged = true;
		testSet.hasChanged = true;

		return testSet;
	}

	/**
	 * Sample down the current instanceSet to the desired proportion. If it has
	 * a class attribute it will be used to make a stratified sample.
	 *  
	 * @param proportion
	 * @param compact
	 * @param classIndex TODO
	 */
	public void buildSample(float proportion, boolean compact, int classIndex) {
		/* 
		 * The proportion variable has a different meaning here. It tells
		 * how much from the instanceSet should be removed. Then, the input
		 * proportion must be changed to reflect this mean. 
		 */
		proportion = 1 - proportion;
		
		Random randomizer = new Random(new Date().getTime());
		
		int numClasses;
		int[] count = null;
		int[][] instancesIDs = null;
		
		if (classIndex != -1) {
			numClasses = attributes[classIndex].numValues();
	
			/* Count instances per class */
			count = new int[numClasses];
			Arrays.fill(count, 0);
			for (int classValue, inst = 0; inst < numInstances; inst++) {
				classValue = (int) instances[inst].data[classIndex];
				++count[classValue];
			}
			
			/* Separate the instances by their classes */
			instancesIDs = new int[numClasses][];
			for (int classValue = 0; classValue < numClasses; classValue++) {
				instancesIDs[classValue] = new int[count[classValue]];
			}
			Arrays.fill(count, 0);
			for (int classValue, inst = 0; inst < numInstances; inst++) {
				classValue = (int) instances[inst].data[classIndex];
				instancesIDs[classValue][count[classValue]] = inst;
				++count[classValue];
			}
		} else {
			numClasses = 1;
			count = new int[1];
			count[0] = numInstances;
			instancesIDs = new int[1][numInstances];
			for (int inst = 0; inst < numInstances; inst++) {
				instancesIDs[0][inst] = inst;
			}
		}
		
		/* Array that tells which instance will pertain to the test set */ 
		boolean[] removeIndex = new boolean[numInstances];
		Arrays.fill(removeIndex, false);

		/* Auxiliary array: tells when an instance has not been chosen yet */
		boolean[] notUsed = new boolean[numInstances];
		Arrays.fill(notUsed, true);
		
		/* 
		 * Randomly choose the instances for the test set using a stratified
		 * approach for the class
		 */
		int index = 0;
		int counter;
		int inst;
		int max;
		for (int classValue = 0; classValue < numClasses; classValue++) {
			/* Choose randomly the instances to the test set */
			counter = 0;
			max = Math.round((count[classValue] * proportion));
			while (counter < max) {
				inst = randomizer.nextInt(count[classValue]);
				inst = instancesIDs[classValue][inst];
				if (notUsed[inst]) {
					removeIndex[inst] = true;
					++counter;
					++index;
					
					/* Discard the instance 'inst' */
					notUsed[inst] = false;
				}
			}
		}
		
		/* Remove all instances marked to be removed */
		removeInstances(removeIndex);
		
		/* Compact both training and test instanceSets */
		if (compact) {
			compact();
		}
		
		Arrays.fill(attributeHasChanged, true);
		hasChanged = true;
	}

	public int[] insertInstances(float[][] newInstanceSet) {
		int increaseSize = newInstanceSet.length;
		int oldSize = numInstances;
		int[] newInstancesIDs = new int[increaseSize];
		
		/* First, increase size */
		sizeUp(increaseSize);

		/* Next, insert the instances from the new instanceSet */
		for (int i = 0; i < increaseSize; i++) {
			insertInstance(newInstanceSet[i]);

			/* Create array of the new instances' index */
			newInstancesIDs[i] = oldSize + i;
		}
		
		/* Now compact the instanceSet */
//		compact();

		Arrays.fill(attributeHasChanged, true);
		hasChanged = true;
		
		return newInstancesIDs;
	}


	
	/*------------------- Quicksort - start ---------------------*/
	
	/**
	 * Returns an index of this instanceSet with the instances' position 
	 * sorted in ascending way. It does not change the position of the 
	 * instances in this instanceSet. Should two instances be equal, they will
	 * appear in resulting index in the same order they appear in this
	 * instanceSet (in other words, this method is stable).
	 * 
	 * @return An array of integers with the positions of this instanceSet
	 * sorted in ascending way.
	 */
	public int[] sortAscending() {
		return sort(false, -1);
	}

	/**
	 * Returns an index of this instanceSet with the instances' position 
	 * sorted in ascending way according to the attribute specified as a
	 * parameter. It does not change the position of the 
	 * instances in this instanceSet. Should two instances be equal, they will
	 * appear in resulting index in the same order they appear in this
	 * instanceSet (in other words, this method is stable).
	 * 
	 * @param att The attribute index wich will the instances be sorted by
	 * @return An array of integers with the positions of this instanceSet
	 * sorted in ascending way.
	 */
	public int[] sortAscending(int att) {
		return sort(false, att);
	}

	/**
	 * Returns an index of this instanceSet with the instances' position 
	 * sorted in descending way. It does not change the position of the 
	 * instances in this instanceSet. Should two instances be equal, they will
	 * appear in resulting index in the same order they appear in this
	 * instanceSet (in other words, this method is stable).
	 * 
	 * @return An array of integers with the positions of this instanceSet
	 * sorted in descending way.
	 */
	public int[] sortDescending() {
		return sort(true, -1);
	}

	/**
	 * Returns an index of this instanceSet with the instances' position 
	 * sorted in descending way according to the attribute specified as a
	 * parameter. It does not change the position of the 
	 * instances in this instanceSet. Should two instances be equal, they will
	 * appear in resulting index in the same order they appear in this
	 * instanceSet (in other words, this method is stable).
	 * 
	 * @param att The attribute index wich will the instances be sorted by
	 * @return An array of integers with the positions of this instanceSet
	 * sorted in descending way.
	 */
	public int[] sortDescending(int att) {
		return sort(true, att);
	}

	private int[] sort(boolean descending, int att) {
		float[][][] arrays = new float[numInstances][][];
		for (int inst = 0; inst < numInstances; inst++) {
			arrays[inst] = new float[2][];
			arrays[inst][0] = instances[inst].data;
			float[] aux = {inst};
			arrays[inst][1] = aux;
		}
		if (att == -1) {
			sort(arrays);
		} else {
			sort(arrays, att);
		}
		
		int[] index = new int[numInstances];
		
		if (descending) {
			for (int inst = 0; inst < numInstances; inst++) {
				index[inst] = (int) arrays[numInstances - inst - 1][1][0];
			}
		} else {
			for (int inst = 0; inst < numInstances; inst++) {
				index[inst] = (int) arrays[inst][1][0];
			}
		}
		
		return index;
	}

	private void sort(float[][][] array) {
		Arrays.sort(array, new Comparator<float[][]>() {
			public int compare(final float[][] arg0, final float[][] arg1) {
				float[][] p1 = (float[][]) arg0;
				float[][] p2 = (float[][]) arg1;
				float x;
				
				for (int i = 0; i < numAttributes; i++) {
					x = p1[0][i] - p2[0][i];
					if (x < 0) {
						return -1;
					} else if (x > 0) {
						return 1;
					}
				}
				
				/* 
				 * The two instances are equal. Check their position in the
				 * instanceSet.
				 */
				x = p1[1][0] - p2[1][0];
				if (x < 0) {
					return -1;
				} else if (x > 0) {
					return 1;
				}
				
				/* It will never happen */
				return 0;
			}
		});
	}

	private void sort(float[][][] array, final int att) {
		Arrays.sort(array, new Comparator<float[][]>() {
			public int compare(final float[][] arg0, final float[][] arg1) {
				float[][] p1 = (float[][]) arg0;
				float[][] p2 = (float[][]) arg1;
				float x;
				
				x = p1[0][att] - p2[0][att];
				if (x < 0) {
					return -1;
				} else if (x > 0) {
					return 1;
				}
				return 0;
			}
		});
	}

	/*------------------- Quicksort - end ---------------------*/

	
	public void setCounterIndex(int counterIndex) {
		this.counterIndex = counterIndex;
		
		hasChanged = true;
	}

	public int getCounterIndex() {
		return counterIndex;
	}

	public void setCompacted(boolean compactedFile) {
		this.compactedFile = compactedFile;
	}	
	
	public boolean isCompacted() {
		return compactedFile;
	}

	public void stratify(int numFolds) {
		int[] instancesIDs = new int[numInstances];
		int numClasses = numClasses();
		
		/* Indexes the instanceSet separated by its instances class */
		int[][] stratifiedIndex = new int[numClasses][];
		
		/* Counter of each class */
		int[] counter = new int[numClasses];
		Arrays.fill(counter, 0);
		
		/* Count each class separatedly */
		for (int classLabel, inst = 0; inst < numInstances; inst++) {
			classLabel = (int) instances[inst].data[classIndex];
			++counter[classLabel];
		}
		
		/* Create index of instances separated by class */
		for (int classLabel = 0; classLabel < numClasses; classLabel++) {
			stratifiedIndex[classLabel] = new int[counter[classLabel]];
		}
		
		/* Separate the instances by class */
		Arrays.fill(counter, 0);
		for (int classLabel, inst = 0; inst < numInstances; inst++) {
			classLabel = (int) instances[inst].data[classIndex];
			stratifiedIndex[classLabel][counter[classLabel]] = inst;
			++counter[classLabel];
		}

		/* Randomize each class group's instances */
		for (int classLabel = 0; classLabel < numClasses; classLabel++) {
			rnd = new Random(new Date().getTime());
			Utils.randomize(stratifiedIndex[classLabel], rnd);
		}
		
		/* Create the maxPerClassPerFold counter */
		int[][] maxPerFoldPerClass = new int[numFolds][numClasses];
		int aux;
		maxPerFoldPerClass[0] = new int[numClasses];
		Arrays.fill(maxPerFoldPerClass[0], 0);
		for (int classLabel = 0; classLabel < numClasses; classLabel++) {
			aux = counter[classLabel] / numFolds;
			maxPerFoldPerClass[0][classLabel] = aux;
			if (0 < (counter[classLabel] % numFolds)) {
				++maxPerFoldPerClass[0][classLabel];
			}
		}
		for (int fold = 1; fold < numFolds; fold++) {
			maxPerFoldPerClass[fold] = new int[numClasses];
			Arrays.fill(maxPerFoldPerClass[fold], 0);
			for (int classLabel = 0; classLabel < numClasses; classLabel++) {
				aux = counter[classLabel] / numFolds;
				if ((fold) < (counter[classLabel] % numFolds)) {
					++aux;
				}
				aux += maxPerFoldPerClass[fold - 1][classLabel];
				maxPerFoldPerClass[fold][classLabel] = aux;
			}
		}
		
		/* Zero the counter */
		Arrays.fill(counter, 0);
		
		/* Create stratified instancesIDs array */
		int num = 0;
		int inst;
		int max;
		for (int fold = 0; fold < numFolds; fold++) {
			/* Populate each fold */
			for (int classLabel = 0; classLabel < numClasses; classLabel++) {
				max = maxPerFoldPerClass[fold][classLabel];
				while (counter[classLabel] < max) {
					inst = stratifiedIndex[classLabel][counter[classLabel]];
					instancesIDs[num] = inst;
					++counter[classLabel];
					++num;
				}
			}
		}

		/* Set instances' position according to the stratified instancesIDs */
		this.instancesIDs = new Hashtable<Integer, Integer>(numInstances);
		Instance[] newInstances = new Instance[numInstances];
		for (int i = 0; i < numInstances; i++) {
			inst = instancesIDs[i];
			newInstances[i] = instances[inst];
			this.instancesIDs.put(newInstances[i].getInstanceID(), i);
		}
		instances = newInstances;
		
		/* Test */
//		int[][] ema = new int[numClasses][numFolds];
//		int classValue;
//		int fold;
//		for (int i = 0; i < numInstances; i++) {
//			fold = i / (numInstances / numFolds);
//			classValue = (int) instances[i].data[classIndex];
//			++ema[classValue][fold];
//		}
//		@SuppressWarnings("unused")
//		boolean pause = true;
	}

	public boolean classIsNominal() {
		return attributes[classIndex].isNominal();
	}

	/**
	 * Returns the mean (mode) for a numeric (nominal) attribute as
	 * a floating-point value. Returns 0 if the attribute is neither nominal nor 
	 * numeric. If all values are missing it returns zero.
	 *
	 * @param attIndex the attribute's index (index starts with 0)
	 * @return the mean or the mode
	 */
	public double meanOrMode(int attIndex) {
		double result;
		double found;
		double value;
		double weight;
		int [] counts;

		if (attributes[attIndex].isNumeric()) {
			result = found = 0;
			for (int i = 0; i < numInstances(); i++) {
				if (!instances[i].isMissing(attIndex)) {
					value = instances[i].data[attIndex];
					weight = instances[i].data[counterIndex];
					found += weight;
					result += value * weight;
				}
			}
			if (found <= 0) {
				return 0;
			} else {
				return result / found;
			}
		} else if (attributes[attIndex].isNominal()) {
			counts = new int[attributes[attIndex].numValues()];
			for (int j = 0; j < numInstances(); j++) {
				value = instances[j].data[attIndex];
				weight = instances[j].data[counterIndex];
				counts[(int) value] += weight;
			}
			return (double) Utils.maxIndex(counts);
		} else {
			return 0;
		}
	}

	/**
	 * Returns the mean (mode) for a numeric (nominal) attribute as a
	 * floating-point value.	Returns 0 if the attribute is neither
	 * nominal nor numeric.	If all values are missing it returns zero.
	 *
	 * @param att the attribute
	 * @return the mean or the mode 
	 */
	public double meanOrMode(Attribute att) {
		return meanOrMode(att.getIndex());
	}
	
	/**
	 * Computes the variance for a numeric attribute.
	 *
	 * @param attIndex the numeric attribute (index starts with 0)
	 * @return the variance if the attribute is numeric
	 * @throws IllegalArgumentException if the attribute is not numeric
	 */
	public double variance(int attIndex) {
		double sum = 0;
		double sumSquared = 0;
		double sumOfWeights = 0;
		double value;
		double weight;

		if (!attributes[attIndex].isNumeric()) {
			throw new IllegalArgumentException("Can't compute variance" +
					" because attribute is not numeric!");
		}
		
		for (int i = 0; i < numInstances(); i++) {
			if (!instances[i].isMissing(attIndex)) {
				value = instances[i].data[attIndex];
				weight = instances[i].data[counterIndex];
				sum += value * weight;
				sumSquared += value * value * weight;
				sumOfWeights += weight;
			}
		}

		if (sumOfWeights <= 1) {
			return 0;
		}

		double result = sumSquared - (sum * sum / sumOfWeights);
		result /= (sumOfWeights - 1);

		/* We don't want negative variance */
		if (result < 0) {
			return 0;
		} else {
			return result;
		}
	}

	/**
	 * Computes the variance for a numeric attribute.
	 *
	 * @param att the numeric attribute
	 * @return the variance if the attribute is numeric
	 * @throws IllegalArgumentException if the attribute is not numeric
	 */
	public double variance(Attribute att) {
		return variance(att.getIndex());
	}
	
	/** 
	 * Stores the type of this instanceSet:
	 * 
	 * 0 - Numeric
	 * 1 - Nominal
	 * 2 - Mixed
	 */
	public void setInstanceSetType() {
		boolean numeric = false;
		boolean nominal = false;
		boolean mixed = false;
		
		/* Check if the instanceSet contains any numeric attribute */
		if (numNumericAttributes > 0) {
			numeric = true;
		}

		/*
		 * Check if the instanceSet contains any nominal attribute, besides the
		 * class attribute.
		 */
		if (numCyclicAttributes > 0 ||
			numNominalAttributes > 1 ||
			(classIndex > -1 && !classIsNominal() &&
				numNominalAttributes > 0)) {
			nominal = true;
		}
		
		/* Check if the instanceSet contains mixed attributes */
		if (nominal && numeric) {
			mixed = true;
		}
		
		if (mixed) {
			instanceSetType = MIXED;
		} else if (nominal) {
			instanceSetType = NOMINAL;
		} else {
			instanceSetType = NUMERIC;
		}
	}

	public int getInstanceSetType() {
		return instanceSetType;
	}
	
	public boolean isNumeric() {
		return instanceSetType == NUMERIC;
	}
	
	public boolean isNominal() {
		return instanceSetType == NOMINAL;
	}
	
	public boolean isMixed() {
		return instanceSetType == MIXED;
	}
	
	public float[] getClassDistribution() {
		if (distribution != null) {
			return distribution.clone();
		} else {
			return getClassDistribution(true);
		}
	}
	
	public float[] getClassDistribution(boolean force) {
		/* Check if this instanceSet has been changed */
		if (hasChanged || force) {
			int numClasses = numClasses();
			distribution = new float[numClasses];
			
			for (int i = 0; i < numClasses; i++) {
				distribution[i] = 0;
			}
			
			int classValue;
			for (int i = 0; i < numInstances; i++) {
				classValue = (int) instances[i].data[classIndex];
				distribution[classValue] += instances[i].data[counterIndex];
			}
			
			hasChanged = false;
		}

		return distribution.clone();
	}

	public float getClassFrequency(int classValue) {
		return  getClassDistribution(false)[classValue] / numWeightedInstances;
	}

	public void setAttributeHasChanged(int att) {
		this.attributeHasChanged[att] = true;
	}

	/** 
	 * Determine the estimator numeric precision from differences between
	 * adjacent values.
	 */
	public float[] computePrecision() {
		int[] instancesIDs = new int[numInstances];
		
		for (int i = 0; i < numInstances; i++) {
			instancesIDs[i] = i;
		}
		
		return computePrecision(instancesIDs);
	}

	/** 
	 * Determine the estimator numeric precision from differences between
	 * adjacent values.
	 */
	public float[] computePrecision(int[] instancesIDs) {
		float[] precision = new float[numNumericAttributes];
		int[] pos;
		Hashtable<Integer, Integer> validInstances;
		validInstances = new Hashtable<Integer, Integer>();
		int inst;
		float lastValue = 0;
		int distinct;
		float value;
		float deltaSum;
		int attIndex;
		
		for (int i = 0; i < instancesIDs.length; i++) {
			inst = instancesIDs[i];
			validInstances.put(inst, i);
		}
		
		attIndex = 0;
		for (int att = 0; att < numNumericAttributes; att++) {
			if (attributeType[att] != NUMERIC) {
				continue;
			}
			
			precision[attIndex] = DEFAULT_NUM_PRECISION;
				
			pos = sortAscending(att);
			
			distinct = 0;
			deltaSum = 0;
			lastValue = instances[pos[0]].data[att];
			for (int i = 1; i < numInstances; i++) {
				inst = pos[i];
				
				if (!validInstances.containsKey(inst)) {
					continue;
				}
				
				value = instances[inst].data[att];
				
				if (value != lastValue) {
					deltaSum += value - lastValue;
					lastValue = value;
					++distinct;
				}
			}
			if (distinct > 0) {
				precision[attIndex] = deltaSum / distinct;
			}
			++attIndex;
		}
		
		return precision;
	}

	public int[] getArrayOfInstancesPos(int[] misclassifiedInstancesIDs) {
		int[] instancesPos = new int[numInstances];
		
		for (int i = 0; i < numInstances; i++) {
			instancesPos[i] = instancesIDs.get(misclassifiedInstancesIDs[i]);
		}
		
		return instancesPos;
	}

	public int getInstancePos(int instanceID) {
		if (instancesIDs.containsKey(instanceID)) {
			return instancesIDs.get(instanceID);
		} else {
			return -1;
		}
	}

	public int[] getInstancesPosFromClass(int classValue) {
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		for (int inst = 0; inst < numInstances; inst++) {
			if (instances[inst].data[classIndex] == classValue) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int[] instancesPos = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesPos[i] = instancesIDsTmp[i];
		}
		
		return instancesPos;
	}

	public void rebuildInstancesIDs() {
		instancesIDs = new Hashtable<Integer, Integer>();
		for (int i = 0; i < numInstances; i++) {
			instancesIDs.put(i, i);
			instances[i].assignID(i);
		}
	}
	
	public void assume(InstanceSet instanceSet) {
		this.relationName = instanceSet.relationName;
		this.counterAttributeName = instanceSet.counterAttributeName;
		this.attributes = instanceSet.attributes;
		this.attributeType = instanceSet.attributeType;
		this.numAttributes = instanceSet.numAttributes;
		this.numNominalAttributes = instanceSet.numNominalAttributes;
		this.numNumericAttributes = instanceSet.numNumericAttributes;
		this.numCyclicAttributes = instanceSet.numCyclicAttributes;
		this.instances = instanceSet.instances;
		this.classIndex = instanceSet.classIndex;
		this.counterIndex = instanceSet.counterIndex;
		this.numInstances = instanceSet.numInstances;
		this.numWeightedInstances = instanceSet.numWeightedInstances;
		this.compactedFile = instanceSet.compactedFile;
		this.attributeHasChanged = instanceSet.attributeHasChanged;
		this.attributeStats = instanceSet.attributeStats;
		this.instanceSetType = instanceSet.instanceSetType;
		this.hasChanged = instanceSet.hasChanged;
		this.distribution = instanceSet.distribution;
		this.instancesIDs = instanceSet.instancesIDs;
	}

	/**
	 * Only works for two class problem.
	 * 
	 * @return positive class
	 */
	public int getPositiveClass() {
		getClassDistribution();
		if (distribution[0] < distribution[1]) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * Only works for two class problem.
	 * 
	 * @return negative class
	 */
	public int getNegativeClass() {
		return Math.abs(1 - getPositiveClass());
	}

}