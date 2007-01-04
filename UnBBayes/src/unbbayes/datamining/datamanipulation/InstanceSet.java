package unbbayes.datamining.datamanipulation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
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
public class InstanceSet implements Serializable {

	private static final long serialVersionUID = 1L;

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
	
	/** Random used in the sorting of the instances */
	private static Random rnd;

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
		
		/* Get information related to the attributes */
		numNominalAttributes = 0;
		numNumericAttributes = 0;
		numCyclicAttributes = 0;
		for (int att = 0; att < numAttributes; att++) {
			attributeType[att] = newAttributes[att].getAttributeType();
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
		this.classIndex = source.classIndex;
		this.relationName = source.relationName;
		this.counterIndex = source.counterIndex;
		if (source.counterAttributeName != null) {
			this.counterAttributeName = new String(source.counterAttributeName);
		}
		this.attributeType = source.attributeType.clone();
		this.numAttributes = source.numAttributes;
		this.numCyclicAttributes = source.numCyclicAttributes;
		this.numNominalAttributes = source.numNominalAttributes;
		this.numNumericAttributes = source.numNumericAttributes;
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
		}
	}

	/**
	 * Removes from this instanceSet those instances marked as <code>true
	 * </code> in the input array of indexes <code>remove</code>.
	 * 
	 * @param remove The index of the instance to be removed.
	 */
	public final void removeInstances(boolean[] remove) {
		/* Get the current size */
		int currentSize = numInstances;
		
		/* Count the number of instances to be removed */
		int numInstancesToBeRemoved = 0;
		for (int inst = 0; inst < currentSize; inst++) {
			if (remove[inst]) {
				++numInstancesToBeRemoved;
			}
		}
		
		/* Create new instanceSet */
		int newSize = currentSize - numInstancesToBeRemoved;
		Instance[] newInstanceSet = new Instance[newSize];

		/*
		 * Make a new instanceSet with only the instances not marked for 
		 * deletion.
		 */
		int newInstanceIndex = 0;
		for (int inst = 0; inst < currentSize; inst++) {
			if (remove[inst]) {
				/* Remove instance */
				--numInstances;
				numWeightedInstances -= instances[inst].data[counterIndex];
				continue;
			}
			newInstanceSet[newInstanceIndex] = instances[inst];
			++newInstanceIndex;
		}
		
		/* Set the current instanceSet to the new one just created */
		instances = newInstanceSet;
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

			int att;

			// attributes before index
			for (att = 0; att < index; att++) {
				newAttributes[att] = attributes[att];
				newAttributeType[att] = attributeType[att];
			}

			//attributes after index
			for (; att < numAttributes; att++) {
				newAttributes[att] = attributes[att + 1];
				newAttributes[att].setIndex(att);
				newAttributeType[att] = attributeType[att + 1];
			}
			attributes = newAttributes;
			attributeType = newAttributeType;
			
			/* Remove the attribute from the instances */
			for (int inst = 0; inst < numInstances; inst++) {
				instances[inst].removeAttribute(index);
			}
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
		att.setFinal(this);

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
		newInstance.setInstanceSet(this);
		numInstances++;
		numWeightedInstances += newInstance.data[counterIndex];
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
	 * attribute and return then as a vector. 
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
			if (attributeType[i] == NOMINAL) {
				attributes[i].setFinal(this);
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
		int[] instancesIDs = sort();
		
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
	}

	/**
	 * Builds a training and a test instanceSets from this current instanceSet.
	 * The test instanceSet will be created with <code>testSize</code>
	 * instances and the rest will remains for the training instanceSet. For
	 * matters of space, the current instanceSet will become the training
	 * instanceSet and <code>testSize</code> instances will be removed to the
	 * the test instanceSet (randomly selected). Then it returns the test
	 * instanceSet.
	 * 
	 * @param testSize The desired size of the new test instanceSet.
	 * @return The test instanceSet just created.
	 */
	public InstanceSet buildTrainTestSet(float proportion, boolean compact) {
		Random randomizer = new Random(new Date().getTime());
		int testSize = (int) (numInstances * proportion);
		
		int numClasses;
		int[] count = null;
		int[][] instancesIDs = null;
		
		if (this.classIndex != -1) {
			numClasses = numClasses();
	
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
		
		return testSet;
	}

	public void insertInstances(float[][] newInstanceSet) {
		int increaseSize = newInstanceSet.length;
		int newSize = increaseSize + numInstances;
		Instance[] newInstances = new Instance[newSize];
		int inst;
		
		/* First, copy the instances from the current instanceSet */
		for (inst = 0; inst < numInstances; inst++) {
			newInstances[inst] = instances[inst];
		}

		/* Next, copy the instances from the new instanceSet */
		for (inst = 0; numInstances < newSize; numInstances++, inst++) {
			Instance newInstance = new Instance(newInstanceSet[inst]);
			newInstances[numInstances] = newInstance;
			newInstance.setInstanceSet(this);
			numWeightedInstances += newInstanceSet[inst][counterIndex];
		}
		
		instances = newInstances;

		/* Now compact the instanceSet */
//		compact();
	}


	
	/*------------------- Quicksort - start ---------------------*/
	
	/**
	 * Sorts a given array of objects in ascending order and returns an
	 * array of integers with the positions of the elements of the
	 * original array in the sorted array. 
	 * 
	 * @param array This array is not changed by the method!
	 * @param cmp A comparable object.
	 * @return An array of integers with the positions in the sorted array.
	 */
	public int[] sort() {
		int[] index = new int[numInstances];

		rnd = new Random(new Date().getTime());
		
		for (int i = 0; i < index.length; i++) {
			index[i] = i;
		}
		qsort(index, 0, numInstances - 1);
		
		return index;
	}

	private void qsort(int[] index, int begin, int end) {
		if (end > begin) {
			int pos = partition(index, begin, end);
			
			qsort(index, begin, pos - 1);
			qsort(index, pos + 1,  end);
		}
	}
	
	private int partition(int[] index, int begin, int end) {
		int pos = begin + rnd.nextInt(end - begin + 1);
		int pivot = index[pos];
		
		swap(index, pos, end);
		
		for (int i = pos = begin; i < end; ++ i) {
			if (!greaterThan(index[i], pivot)) {
				swap(index, pos++, i);
			}
		}
		swap(index, pos, end);
		
		return pos;
	}
	
	private void swap(int[] index, int i, int j) {
		int tmp = index[i];
		index[i] = index[j];
		index[j] = tmp;
	}
	
	private boolean greaterThan(int inst1, int inst2) {
		float[] v1 = instances[inst1].data;
		float[] v2 = instances[inst2].data;

		for (int i = 0; i < numAttributes; i++) {
			if (v1[i] > v2[i]) {
				return true;
			} else if (v1[i] < v2[i]) {
				return false;
			}
		}
		if (inst1 > inst2) {
			return true;
		}
		
		return false;
	}

	/*------------------- Quicksort - end ---------------------*/

	
	public void setCounterIndex(int counterIndex) {
		this.counterIndex = counterIndex;
	}

	public int getCounterIndex() {
		return counterIndex;
	}

}