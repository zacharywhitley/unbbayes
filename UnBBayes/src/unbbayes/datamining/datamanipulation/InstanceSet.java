package unbbayes.datamining.datamanipulation;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.ResourceBundle;

/**
 *  Class for handling a set of instances.
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (16/02/2002)
 */
public class InstanceSet
{	/** The dataset's name. */
  	private String relationName;

        private String counterAttributeName;

	/** The attribute information. */
  	private Attribute[] attributes;
  	private AttributeStats[] attributeStats;

	/** The instances. */
  	private Instance[] instanceSet;

	/** The class attribute's index
	* -1 let the index of the class
   	* attribute be undefined.
	*/
  	private int classIndex;

	/** Load resource file from this package */
  	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.datamining.datamanipulation.resources.DataManipulationResource");

  	/** Position of the next instance to be inserted */
  	private int nextInstancePosition = 0;

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

        public final String getCounterAttributeName()
        {
          return counterAttributeName;
        }

        public void setCounterAttributeName(String counterAttributeName)
        {
          this.counterAttributeName = counterAttributeName;
        }

    /**
	* Sets the instance set. Does not
   	* check if the instances are compatible with the dataset.
	*
	* @param newInstances New instance set
	*/
	public final void setInstances(Instance[] newInstances)
    {   instanceSet = newInstances;
    }

	/**
   	* Returns the number of attributes.
   	*
   	* @return The number of attributes as an integer
   	*/
  	public final int numAttributes()
	{	return attributes.length;
  	}

  	public Attribute[] getAttributes()
  	{
  		return attributes;
  	}

        public final void removeInstance(int numInstance)
        {
          int numInstances = numInstances();
          if((numInstance>=0)&&(numInstance<numInstances))
          {
                  Instance[] newInstanceSet = new Instance[numInstances-1];

                  // instances before index
                  int i;
                  for(i=0;i<numInstance;i++)
                  {
                          newInstanceSet[i] = instanceSet[i];
                  }

                  //instances after index
                  while(i<(numInstances-1))
                  {
                          newInstanceSet[i]=instanceSet[i+1];
                          i++;
                  }

                  instanceSet = newInstanceSet;
          }
        }

	public final void removeAttribute(int index)
	{
		int numAttributes = numAttributes();
		if((index>=0)&&(index<numAttributes))
		{
			//new attributes without index attribute
			Attribute[] newAttributes = new Attribute[numAttributes-1];
			AttributeStats[] newAttributeStats = new AttributeStats[numAttributes-1];


			// attributes before index
			int i;
			for(i=0;i<index;i++)
			{
				newAttributes[i] = attributes[i];
				newAttributeStats[i] = attributeStats[i];
			}

			//attributes after index
			while(i<newAttributes.length)
			{
				newAttributes[i]=attributes[i+1];
				newAttributes[i].setIndex(i);
				newAttributeStats[i] = attributeStats[i+1];
				i++;
			}

			attributes = newAttributes;
			attributeStats = newAttributeStats;
		}
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
	{	return instanceSet.length;
  	}

	public final int numWeightedInstances()
	{	int numInstances = numInstances();
		int result = 0;
		for (int i=0;i<numInstances;i++)
		{	Instance instance = getInstance(i);
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
	{	return attributes[index];
  	}

	/**
   	* Returns an enumeration of all the attributes except the class attribute.
   	*
   	* @return Enumeration of all the attributes.
   	*/
  	public final Enumeration enumerateAttributes()
	{
		return new ArrayListEnumeration(attributes, classIndex);
  	}

	/**
   	* Returns an enumeration of all instances in the dataset.
   	*
   	* @return Enumeration of all instances in the dataset
   	*/
  	public final Enumeration enumerateInstances()
	{
		return new ArrayListEnumeration(instanceSet);
  	}

	/**
   	* Returns the instance at the given position.
   	*
   	* @param index Instance's index
   	* @return The instance at the given position
   	*/
  	public final Instance getInstance(int index)
	{	return instanceSet[index];
  	}

	/**
   	* Inserts an attribute at the given position (0 to
   	* numAttributes()) and sets all values to be missing. IllegalArgumentException if the given index is out of range
   	*
   	* @param att The attribute to be inserted
   	* @param position The attribute's position
   	*/
  	public void setAttributeAt(Attribute att, int position)
	{	if ((position < 0) || (position > numAttributes()))
		{	throw new IllegalArgumentException(resource.getString("setAttributeAtException"));
    	}
    	attributes[position] = att;
    	attributeStats[position] = null;
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
	{
		instanceSet[nextInstancePosition] = newInstance;
		nextInstancePosition++;
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
    	instanceSet[nextInstancePosition] = instance;//mudei2
    	nextInstancePosition++;//mudei2
  	}

	/**
   	* Constructor creating an empty set of instances.
    * Set class index to be undefined. Sets
   	* the capacity of the set of instances to 0 if it's negative.
	*
   	* @param capacity The capacity of the new dataset
   	*/
  	public InstanceSet(int capacity, Attribute[] newAttributes)
	{
          if (capacity < 0)
          {
            capacity = 0;
          }
          attributes = newAttributes;
		  attributeStats = new AttributeStats[attributes.length];
          instanceSet = new Instance[capacity];
          classIndex = -1;
  	}

  	/**
   	* Constructor copying all instances and references to
   	* the header information from the given set of instances.
	* Makes a deep copy.
   	*
   	* @param dataset Set to be copied
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
   	* @param dataset Instances from which the header information is to be taken
   	* @param capacity The capacity of the new dataset
   	*/
  	public InstanceSet(InstanceSet dataset, int capacity)
	{	if (capacity < 0)
		{	capacity = 0;
    	}

		classIndex = dataset.classIndex;
    	relationName = dataset.relationName;
    	attributes = new Attribute[dataset.numAttributes()];
    	System.arraycopy(dataset.attributes, 0, attributes, 0, numAttributes());
    	attributeStats = new AttributeStats[attributes.length];
    	instanceSet = new Instance[capacity];
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
	{	ArrayList<Instance> newInstances = new ArrayList<Instance>();
		for (int i = 0; i < numInstances(); i++)
		{	if (!getInstance(i).isMissing(attIndex))
			{	newInstances.add(getInstance(i));
			}
    	}
    	Instance[] newInst = new Instance[newInstances.size()];
    	for (int i=0;i<newInst.length;i++)
    	{
    		newInst[i]=(Instance)newInstances.get(i);
    	}
    	instanceSet = newInst;
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
			//Instance ins = new Instance(getInstance(j).getWeight(),by);
			Instance ins = new Instance((int)getInstance(j).getWeight(),by);
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
  	{
  		return attributeStats[index];
  	}

	public AttributeStats[] getAllAttributeStats()
	{
		int numWeightedInstances = numWeightedInstances();
		boolean attFlag = false;
		for (int i=0;i<attributeStats.length;i++)
		{
			if (attributeStats[i]==null)
			{
				attFlag = true;
				if (getAttribute(i).isNominal())
				{
					attributeStats[i] = new AttributeStats(AttributeStats.NOMINAL,getAttribute(i).numValues());
				}
				else
				{
					attributeStats[i] = new AttributeStats(AttributeStats.NUMERIC,getAttribute(i).numValues());
				}
			}
		}
		if (attFlag)
		{
			int numAttributes = numAttributes();
			int[][] countWeightResults = new int[numAttributes][];
			int[][] countResults = new int[numAttributes][];
			for (int i=0;i<numAttributes;i++)
			{
				countWeightResults[i] = new int[getAttribute(i).numValues()+1];
				countResults[i] = new int[getAttribute(i).numValues()+1];
			}
			int numInstances = numInstances();
			for (int j = 0; j < numInstances; j++)
			{
				Instance current = getInstance(j);
				int instanceWeight = (int)current.getWeight();
				for (int i=0;i<numAttributes;i++)
				{
					if (current.isMissing(i))
					{
						countWeightResults[i][((countWeightResults[i]).length)-1]+=instanceWeight;
						countResults[i][((countResults[i]).length)-1]++;
					}
					else
					{
						countWeightResults[i][current.getValue(i)]+=instanceWeight;
						countResults[i][current.getValue(i)]++;
					}
				}
			}
			for (int i=0;i<numAttributes;i++)
			{
				attributeStats[i].setMissingCount(countResults[i][((countResults[i]).length)-1]);
				attributeStats[i].setMissingCountWeighted(countWeightResults[i][((countWeightResults[i]).length)-1]);
				Attribute tempAtt = getAttribute(i);
				if (tempAtt.isNominal())
				{
					for (int j = 0; j < tempAtt.numValues(); j++)
					{
						attributeStats[i].addDistinct(j,countResults[i][j],countWeightResults[i][j]);
					}
				}
				else
				{
					for (int j = 0; j < tempAtt.numValues(); j++)
					{
						attributeStats[i].addDistinct(Float.parseFloat(tempAtt.value(j)),j,countResults[i][j],countWeightResults[i][j]);
					}
				}
			}
		}
		return attributeStats;
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
  	public short[] attributeToByteArray(int index)
	{	short[] result = new short[numInstances()];
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

          public final void sortInstancesByAttribute(Attribute att)
          {
            sortInstancesByAttribute(att.getIndex());
          }

          public final void sortInstancesByAttribute(int attIndex)
          {
            int i,j;
            int numInstances = numInstances();
            for(j=1; j<numInstances; j++)
            {
              Instance key = instanceSet[j];
              i = j-1;
              while (i>-1 && (instanceSet[i].getValue(attIndex)>key.getValue(attIndex)))
              {
                instanceSet[i+1] = instanceSet[i];
                i--;
              }
              instanceSet[i+1] = key;
            }
          }


  	/**
  	 * Swaps two instances.
  	 *
  	 * @param first Index of the first element
  	 * @param second Index of the second element
  	 */
  	public final void swap(int first, int second)
	{	Instance temp = instanceSet[first];
  	  	instanceSet[first] = instanceSet[second];
		instanceSet[second] = temp;
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
         *
         * @param att An attribute
         * @return True if it is a numeric attribute
	*/
	public boolean checkNumericAttribute(Attribute att)
        {   if (getAttribute(att.getIndex()).isNumeric())
            {   return true;
            }
            return false;
        }

        /**
         * Verifies if an specific attribute is numeric
         *
         * @param attIndex An attribute index
         * @return True if the attribute is numeric
         */
        public boolean checkNumericAttribute(int attIndex)
        {   if (getAttribute(attIndex).isNumeric())
            {   return true;
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



