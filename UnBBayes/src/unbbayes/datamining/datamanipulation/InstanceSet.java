package unbbayes.datamining.datamanipulation;

import java.io.*;
import java.util.*;

/**
 *  Class for handling a set of instances.
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class InstanceSet
{	/** The dataset's name. */
  	private String relationName;

	/** The attribute information. */
  	private ArrayList attributes;

	/** The instances. */
  	private ArrayList instanceSet;

	/** The class attribute's index
	* -1 let the index of the class
   	* attribute be undefined.
	*/
  	private int classIndex;
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.datamining.datamanipulation.resources.DataManipulationResource");

  	/**
   	* Returns the relation's name.
   	*
   	* @return The relation's name as a string
   	*/
  	public final String getRelationName()
	{	return relationName;
  	}

  	/**
   	* Sets the relation's name.
   	*
   	* @param newName New relation name.
   	*/
  	public final void setRelationName(String newName)
	{	relationName = newName;
  	}

    /** 
	* Sets the instance set. Does not
   	* check if the instances are compatible with the dataset.
	*
	* @param newInstances New instance set
	*/
	public final void setInstances(ArrayList newInstances)
    {   instanceSet = newInstances;
    }

	/**
   	* Returns the number of attributes.
   	*
   	* @return The number of attributes as an integer
   	*/
  	public final int numAttributes()
	{	return attributes.size();
  	}
	
	public final void removeAttribute(int index)
	{	attributes.remove(index);
	}

	/**
   	* Returns the number of class labels.
   	*
   	* @return The number of class labels as an integer
   	* @exception UnassignedClassException if the class is not set
   	*/
  	public final int numClasses()
	{	if (classIndex < 0)
		{	throw new UnassignedClassException(resource.getString("runtimeException2"));
    	}
    	if (!getClassAttribute().isNominal())
		{	return 1;
    	}
		else
		{	return getClassAttribute().numValues();
    	}
  	}

	/**
   	* Returns the number of instances in the dataset.
   	*
   	* @return The number of instances in the dataset as an integer
   	*/
  	public final int numInstances()
	{	return instanceSet.size();
  	}
	
	public final int numWeightedInstances()
	{	int numInstances = numInstances();
		int result = 0;
		for (int i=0;i<numInstances;i++)
		{	Instance instance = (Instance)getInstance(i);
			result += instance.getWeight();	
		}
		return result;
	}

	/**
   	* Returns an attribute.
   	*
   	* @param index The attribute's index
   	* @return The attribute at the given position
   	*/
  	public final Attribute getAttribute(int index)
	{	return (Attribute) attributes.get(index);
  	}

	/**
   	* Returns an enumeration of all the attributes except the class attribute.
   	*
   	* @return Enumeration of all the attributes.
   	*/
  	public final Enumeration enumerateAttributes()
	{	return new ArrayListEnumeration(attributes, classIndex);
  	}

	/**
   	* Returns an enumeration of all instances in the dataset.
   	*
   	* @return Enumeration of all instances in the dataset
   	*/
  	public final Enumeration enumerateInstances()
	{	return new ArrayListEnumeration(instanceSet);
  	}

	/**
   	* Returns the instance at the given position.
   	*
   	* @param index Instance's index
   	* @return The instance at the given position
   	*/
  	public final Instance getInstance(int index)
	{	return (Instance)instanceSet.get(index);
  	}

	/**
	*	Insert an attribute to current Dataset
	*
	*	@param att Attribute
	*/
	public void insertAttribute(Attribute att)
	{	attributes.add(att);
	}
	
	/**
   	* Inserts an attribute at the given position (0 to 
   	* numAttributes()) and sets all values to be missing.
   	*
   	* @param att The attribute to be inserted
   	* @param pos The attribute's position
   	* @exception IllegalArgumentException if the given index is out of range
   	*/
  	public void setAttributeAt(Attribute att, int position) 
	{	if ((position < 0) || (position > attributes.size())) 
		{	throw new IllegalArgumentException(resource.getString("setAttributeAtException"));
    	}
    	attributes.set(position, att);
		int numInstances = numInstances();
    	for (int i = 0; i < numInstances; i++) 
		{	getInstance(i).setValue(att,Instance.MISSING_VALUE);			
		}		
  	}

	/**
	*	Insert an instance to current Dataset
	*
	*	@param newInstance New instance
	*/
	public void insertInstance(Instance newInstance)
	{	instanceSet.add(newInstance);
	}

	/**
   	* 	Compactifies the set of instances. Decreases the capacity of
   	* 	the set so that it matches the number of instances in the set.
   	*/
  	public final void compactify()
	{	instanceSet.trimToSize();
  	}

	/**
   	* Sets the class attribute.
   	*
   	* @param att Attribute to be the class
   	*/
  	public final void setClass(Attribute att)
	{	classIndex = att.getIndex();
  	}

	/**
   	* Returns the class attribute's index. Returns negative number
   	* if it's undefined.
   	*
   	* @return The class index as an integer
   	*/
  	public final int getClassIndex()
	{	return classIndex;
  	}

  	/**
   	* Sets the class index of the set.
   	* If the class index is negative there is assumed to be no class.
   	* (ie. it is undefined)
   	*
   	* @param classIndex New class index
   	* @exception IllegalArgumentException if the class index is bigger than number of
	* attributes or < 0
   	*/
  	public final void setClassIndex(int classIndex)
	{	if (classIndex >= numAttributes())
		{	throw new IllegalArgumentException(resource.getString("setClassIndexException") + classIndex);
    	}
    	this.classIndex = classIndex;
  	}

	/**
   	* Returns the class attribute.
   	*
   	* @return The class attribute
   	* @exception UnassignedClassException if the class is not set
   	*/
  	public final Attribute getClassAttribute()
	{	if (classIndex < 0)
		{	throw new UnassignedClassException(resource.getString("runtimeException2"));
    	}
    	return getAttribute(classIndex);
  	}

	/**
   	* Adds one instance to the end of the set.
   	* Aumotically increases the
   	* size of the dataset if it is not large enough. Does not
   	* check if the instance is compatible with the dataset.
   	*
   	* @param instance Instance to be added
   	*/
  	public final void add(Instance instance)
	{	instance.setDataset(this);
    	instanceSet.add(instance);
  	}

	/** Default constructor. Set class index to be undefined. */
	public InstanceSet()
	{	attributes = new ArrayList();
		instanceSet = new ArrayList(50);
		classIndex = -1;		
	}

	/**
   	* Constructor copying all instances and references to
   	* the header information from the given set of instances.
	* Makes a deep copy.
   	*
   	* @param instances Set to be copied
   	*/
  	public InstanceSet(InstanceSet dataset)
	{	this(dataset, dataset.numInstances());
		dataset.copyInstances(0, this, dataset.numInstances());
  	}

	/**
   	* Constructor creating an empty set of instances. Copies references
   	* to the header information from the given set of instances. Sets
   	* the capacity of the set of instances to 0 if its negative.
	* Makes a deep copy.
   	*
   	* @param instances Instances from which the header information is to be taken
   	* @param capacity The capacity of the new dataset
   	*/
  	public InstanceSet(InstanceSet dataset, int capacity)
	{	if (capacity < 0)
		{	capacity = 0;
    	}

		classIndex = dataset.classIndex;
    	relationName = dataset.relationName;
    	attributes = (ArrayList)(dataset.attributes.clone());
    	instanceSet = new ArrayList(capacity);		
  	}
	
  	/**
  	 * Creates a new set of instances by copying a 
  	 * subset of another set.
  	 *
  	 * @param source The set of instances from which a subset 
  	 * is to be created
  	 * @param first The index of the first instance to be copied
  	 * @param toCopy The number of instances to be copied
  	 * @exception IllegalArgumentException if first and toCopy are out of range
  	 */
  	public InstanceSet(InstanceSet source, int first, int toCopy) 
	{ this(source, toCopy);
  
  	  if ((first < 0) || ((first + toCopy) > source.numInstances())) 
	  { throw new IllegalArgumentException(resource.getString("outOfRange"));
  	  }
  	  source.copyInstances(first, this, toCopy);
  	}  
	
	/**
   	* Removes all instances with a missing class value
   	* from the dataset.
   	*
   	* @exception UnassignedClassException if class is not set
   	*/
  	public final void deleteWithMissingClass()
	{	if (classIndex < 0)
		{	throw new UnassignedClassException(resource.getString("runtimeException2"));
    	}
    	deleteWithMissing(classIndex);
  	}

	/**
   	* Removes all instances with missing values for a particular
   	* attribute from the dataset.
   	*
   	* @param attIndex Attribute's index
   	*/
  	public final void deleteWithMissing(int attIndex)
	{	int numInstances = numInstances();
		ArrayList newInstances = new ArrayList(numInstances);

    	for (int i = 0; i < numInstances; i++)
		{	if (!getInstance(i).isMissing(attIndex))
			{	newInstances.add(getInstance(i));
      		}
    	}
    	instanceSet = newInstances;
  	}

	/**
   	* Copies instances from one set to the end of another
   	* one.
   	*
   	* @param from Position of the first instance to be copied
   	* @param dest Destination for the instances
   	* @param num Number of instances to be copied
   	*/
  	public void copyInstances(int from, InstanceSet dest, int num)
	{	for (int j = 0; j < num; j++)
		{	short[] by = new short[numAttributes()];
			for (int i=0; i<numAttributes(); i++)
			{	by[i] = getInstance(j).getValue(i);
			}
			Instance ins = new Instance(getInstance(j).getWeight(),by);
			dest.add(ins);
    	}
				
  	}

  	/**
   	* Calculates summary statistics on the values that appear in this
   	* set of instances for a specified attribute.
   	*
   	* @param index The index of the attribute to summarize.
   	* @return An AttributeStats object with it's fields calculated.
   	*/
  	public AttributeStats getAttributeStats(int index)
  	{	AttributeStats result;
    	if (getAttribute(index).isNominal())
		{	result = new AttributeStats(AttributeStats.NOMINAL,getAttribute(index).numValues());
    	}
    	else
		{	result = new AttributeStats(AttributeStats.NUMERIC,0);
    	}
		int numWeightedInstances = numWeightedInstances();
    	result.setTotalCount(numWeightedInstances);
		int numInstances = numInstances();

		short[] attVals = attributeToShortArray(index);
		int[] sorted = Utils.sort(attVals);

		int currentCount = 0;
		float currentValue = Instance.missingValue();
		for (int j = 0; j < numInstances; j++)
		{	Instance current = getInstance(sorted[j]);
			if (current.isMissing(index))
	  		{	result.setMissingCount(result.getMissingCount()+current.getWeight());
			}
			else
			{	if (getAttribute(index).isNominal())
				{	if (Utils.eq(current.getValue(index), currentValue))
	  				{	currentCount += current.getWeight();
					}
	  				else
	  				{	result.addDistinct(currentValue, currentCount);
						currentCount = current.getWeight();
						currentValue = current.getValue(index);
					}
				}
				else
				{	if (Utils.eq(Float.parseFloat(current.stringValue(index)), currentValue))
	  				{	currentCount += current.getWeight();
					}
	  				else
	  				{	result.addDistinct(currentValue, currentCount);
						currentCount = current.getWeight();
						currentValue = Float.parseFloat(current.stringValue(index));
      				}
				}
			}
		}
		result.addDistinct(currentValue, currentCount);
		result.setDistinctCount(result.getDistinctCount() - 1); // So we don't count "missing" as a value
		return result;
  	}

	/**
   	* Gets the value of all instances in this dataset for a particular
   	* attribute. Useful in conjunction with Utils.sort to allow iterating
   	* through the dataset in sorted order for some attribute.
   	*
   	* @param index The index of the attribute.
   	* @return An array containing the value of the desired attribute for
   	* each instance in the dataset.
   	*/
  	public short[] attributeToShortArray(int index)
	{	short [] result = new short[numInstances()];
    	for (int i = 0; i < result.length; i++)
		{	result[i] = getInstance(i).getValue(index);
    	}
    	return result;
  	}

	/**
   	* Shuffles the instances in the set so that they are ordered 
   	* randomly.
   	*
   	* @param random A random number generator
   	*/
  	public final void randomize(Random random) 
	{	int numInstances = numInstances();
		for (int j = numInstances - 1; j > 0; j--)
      		swap(j,(int)(random.nextDouble()*(double)j));
  	}
	
  	/**
  	 * Swaps two instances.
  	 *
  	 * @param first Index of the first element
  	 * @param second Index of the second element
  	 */
  	public final void swap(int first, int second) 
	{	Object temp = instanceSet.get(first);
  
  	  	instanceSet.set(first,instanceSet.get(second));
		instanceSet.set(second,temp);
  	}
	
	/** Verifies if there is any numeric attribute in the dataset.
		@return True if there is at leats one numeric attribute in the dataset
	*/
	public boolean checkNumericAttributes()
    { int numAttributes = numAttributes();
	  for(int i=0; i<numAttributes; i++)
      {  if (getAttribute(i).isNumeric())
         {  return true;
         } 
      }
      return false;
    }
	
	/** Verifies if an specific attribute is numeric
		@return True if it is a numeric attribute
	*/
	public boolean checkNumericAttribute(Attribute att)
    { if (getAttribute(att.getIndex()).isNumeric())
      {  return true;
      } 
      return false;
    }
	
	/**
   	* Returns the dataset as a string in ARFF format.
   	*
   	* @return the dataset in ARFF format as a string
   	*/
  	public final String toString()
	{	StringBuffer text = new StringBuffer();

    	text.append("@relation " + relationName + "\n\n");
    	for (int i = 0; i < numAttributes(); i++)
		{	text.append(getAttribute(i) + "\n");
    	}
    	text.append("\n@data\n");
    	for (int i = 0; i < numInstances(); i++)
		{	text.append(getInstance(i));
      		if (i < numInstances() - 1)
			{	text.append('\n');
      		}
    	}
    	return text.toString();
  	}
}



