package unbbayes.datamining.datamanipulation;

import java.util.*;

/**
 * Class implementing information gain method and other related actions
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 *  @version $1.0 $ (16/02/2002)
 */
public class ClassifierUtils
{
	/** stores the calculated logs */
	private static HashMap logmap;
	/** Load resources file for internacionalization */
	private ResourceBundle resource;
	/** value of ln(2) to be used in the log2 function*/
	private static final double LN2 = Math.log(2);
	/** values used to split numeric attributes, should be 0 for nominal attributes*/
	private double[] splitValues;

    private int[][][] counts;
    private int[] priors;
    private InstanceSet instances;

	//--------------------------------------------------------------------//

	/** ID3Utils constructor */
	public ClassifierUtils(InstanceSet inst)
	{
		instances = inst;
		logmap = new HashMap();
        Double zeroDouble = new Double(0);
        if(logmap.containsKey(zeroDouble))
        	logmap.put(zeroDouble,zeroDouble);
        if(logmap.containsKey(zeroDouble))
            logmap.put(new Double(-0),zeroDouble);
		resource = ResourceBundle.getBundle("unbbayes.datamining.classifiers.resources.ClassifiersResource");


             // Reserve space
                int numClasses = inst.numClasses();
                priors = new int[numClasses];
                counts = new int[inst.numAttributes() - 1][][];
                int attIndex = 0;
                Enumeration enum = inst.enumerateAttributes();
                while (enum.hasMoreElements())
                {
                  Attribute attribute = (Attribute) enum.nextElement();
                  counts[attIndex] = new int[attribute.numValues()][numClasses];
                  attIndex++;
                }


                // Compute counts and sums
                Enumeration enumInsts = inst.enumerateInstances();
                while (enumInsts.hasMoreElements())
                {
                  Instance instance = (Instance) enumInsts.nextElement();
                  if (!instance.classIsMissing())
                  {
                    Enumeration enumAtts = inst.enumerateAttributes();
                    attIndex = 0;
                    while (enumAtts.hasMoreElements())
                    {
                      Attribute attribute = (Attribute) enumAtts.nextElement();
                      if (!instance.isMissing(attribute))
                      {
                        //if (attribute.isNominal())
                        //{
                          counts[attIndex][(int)instance.getValue(attribute)][(int)instance.classValue()] += instance.getWeight();
                        //}
                        /*else
                        {
                          means[(int)instance.classValue()][attIndex] += (Float.parseFloat(instance.stringValue(attribute))*instance.getWeight());
                          counts[(int)instance.classValue()][attIndex][0] += instance.getWeight();
                        }*/
                      }
                      attIndex++;
                    }
                    priors[(int)instance.classValue()] += instance.getWeight();
                  }
                }
                splitValues = new double[instances.getAttributes().length];
	}
	
	//--------------------------------------------------------------------//

    /** Returns the computed priors
       @return the computed priors.
    */
    public int[] getPriors()
    {	
    	return priors;
    }
        
    //--------------------------------------------------------------------//

    /** Returns the computed counts for nominal attributes
        @return the computed counts.
    */
    public int[][][] getCounts()
    {	
    	return counts;
    }

    //--------------------------------------------------------------------//

	/**
   	* Computes information gain for an attribute.
   	*
   	* @param data Data for which info gain is to be computed
   	* @param att Attribute
   	* @return Information gain for the given attribute and data
   	*/
      public double[] computeInfoGain()
      {
        int totalSum = Utils.sum(priors);
        double infoGain = computeEntropy(priors,totalSum);	//entropy(S)		
        double[] resultGain = new double[counts.length];
        Arrays.fill(resultGain,infoGain);
        
        //for each attribute...
        for (int i=0;i<counts.length;i++)
        {
        	//if attribute is nominal...
        	if (instances.getAttribute(i).isNominal())
        	{
        		for (int j=0;j<counts[i].length;j++)
          		{
            		resultGain[i] -= computeEntropy(counts[i][j],totalSum);
          		}
        	}
        	
        	//if attribute is numeric...
        	else
        	{
        		//array with possible values
				String[] values;
				//array with classes distribution for each value,
				int[][] classesDistribution;;
				
				//makes arrays sorted 	
				values = instances.getAttribute(i).getAttributeValues();
				int numClasses = counts[i][0].length; 
				classesDistribution = new int[values.length][numClasses];
				HashMap classesDistributionMap = new HashMap();
				for(int x=0;x<values.length;x++)
				{
					classesDistributionMap.put(values[x],counts[i][x]);
				}
				Arrays.sort(values);
				for(int x=0;x<values.length;x++)
				{
					classesDistribution[x] = (int[])classesDistributionMap.get(values[x]);
				}
			        						
				//search for the minimum entropy
				String value1 = values[0], value2;
				int[] distribution1, distribution2;
				double minimumEntropy = Integer.MAX_VALUE;
				double entropy; 
				double minimumValue = Integer.MAX_VALUE;
				int[] sumPart1, sumPart2; 
				//for each attribute value...
				for(int x=1;x<values.length;x++)
				{
					value2 = values[x];
					distribution1 = classesDistribution[x-1];
					distribution2 = classesDistribution[x];
					sumPart1 = new int[numClasses]; 
					sumPart2 = new int[numClasses];
					entropy = 0; 
										
					//if two values has the same class, there is no evaluation
					if(!ClassifierUtils.hasSameClass(distribution1,distribution2))
					{
						for(int y=0;y<x;y++)
						{
							sumPart1 = ClassifierUtils.arraysSum(sumPart1,classesDistribution[y]);							
						}
						for(int y=x;y<values.length;y++)
						{
							sumPart2 = ClassifierUtils.arraysSum(sumPart2,classesDistribution[y]);
						}
						
						entropy = computeEntropy(sumPart1,totalSum) + computeEntropy(sumPart2,totalSum);
						
						if (minimumEntropy>entropy)
						{
							minimumEntropy = entropy;
							minimumValue = (Double.parseDouble(values[x-1])+Double.parseDouble(values[x]))/2.0d;
						}
					}
										
					value1 = value2;					
				}
				
				resultGain[i] -= minimumEntropy;
				splitValues[i] = minimumValue;
	       	}
        }
        return resultGain;
      }

      //--------------------------------------------------------------------//

      /**
      * Computes the entropy of a dataset.
      *
      * @param data Data for which entropy is to be computed
      * @return Entropy of the data's class distribution
      */
      public double computeEntropy(int[] classValues,int totalSum)
      {
        double entropy=0;
        int sum=0;
        for (int i=0;i<classValues.length;i++)
        {
          entropy-=xlog2(classValues[i]);
          sum+=classValues[i];
        }
        return (entropy + xlog2(sum))/totalSum;
      }


  	//--------------------------------------------------------------------//

  	/*public double computeGainRatio(InstanceSet data, Attribute att) throws Exception
  	{	// Compute split info
  		int numInstances = data.numWeightedInstances();
  		double splitInfo = 0;
		if (att.isNominal())
		{
			splitInfo = xlog2(numInstances);
			InstanceSet[] splitData = splitData(data, att);
    		int numValues = att.numValues();
			for (int j = 0; j < numValues; j++)
  			{	int numInstancesSplit = splitData[j].numWeightedInstances();
				if (numInstancesSplit > 0)
  				{
  					splitInfo -= xlog2(numInstancesSplit);
				}
    		}
    		splitInfo /= numInstances;
		}

		// Compute gain ratio
		double gainRatio;
		if (splitInfo != 0)
		{
			gainRatio = computeInfoGain(data,att)/splitInfo;
		}
		else
		{
			gainRatio = computeInfoGain(data,att);
		}
		return gainRatio;
  	}*/

  	//--------------------------------------------------------------------//

  	/**
   	* Returns the logarithm for base 2 of a double value. Special cases:
   	* If the argument is NaN or less than zero, then the result is NaN.
   	* If the argument is positive infinity, then the result is positive infinity.
   	* If the argument is positive zero or negative zero, then the result is negative infinity.
   	*
   	* @param a - a number greater than 0.0.
   	* @return The value log2 a, the natural logarithm of a.
   	*/
  	public static double log2(double a)
  	{
  		return Math.log(a)/LN2;
  	}
  	
  	//--------------------------------------------------------------------//

	public double xlog2(double a)
	{
		Double aDouble = new Double(a);
		
		if(a==0)
		{
			return 0;
		}
		else if(logmap.containsKey(aDouble))
		{
			return ((Double)(logmap.get(aDouble))).doubleValue();
		}
		else
		{
			double newLog = a*Math.log(a)/LN2;
			logmap.put(aDouble,new Double(newLog));
			return newLog;
		}
	}
  	//--------------------------------------------------------------------//

  	/**
  	* Splits a dataset according to the values of a nominal attribute.
  	*
  	* @param data Data which is to be split
  	* @param att Attribute to be used for splitting
  	* @return The sets of instances produced by the split
  	*/
  	public static InstanceSet[] splitData(InstanceSet data, Attribute att)
  	{
  		int numValues = att.numValues();
		InstanceSet[] splitData = new InstanceSet[numValues];
  		AttributeStats attributeStats = (data.getAllAttributeStats())[att.getIndex()];
		for (int j = 0; j < numValues; j++)
  		{
  			splitData[j] = new InstanceSet(data, (attributeStats.getNominalCounts())[j]);
  		}
		Enumeration instEnum = data.enumerateInstances();
    	while (instEnum.hasMoreElements())
  		{
  			Instance inst = (Instance) instEnum.nextElement();
    		splitData[(int) inst.getValue(att)].add(inst);
    	}
		return splitData;
  	}
  	
  	//---------------------------------------------------------------------//
  	
	/**Checks if two classes distributions refers to the same class
	 * @param distribution1: one of the distributions to be evaluated
	 * @param distribution2: one of the distributions to be evaluated
	 * @return boolean value indicating if distributions refers to the same class or not 
	 */
	public static boolean hasSameClass(int[] distribution1, int[] distribution2)
	{
		if(distribution1.length!=distribution2.length)
		{
			return false;
		}
		
		for(int i=0;i<distribution1.length;i++)
		{
			if((distribution1[i]==0)&(distribution2[i]!=0))
			{
				return false;
			}
			if((distribution1[i]!=0)&(distribution1[i]==0))
			{
				return false;
			}
		}
		
		return true;
	}
	
	//---------------------------------------------------------------------//
	
	/** applies a two arrays sum
	 * 	@param array1: one of the arrays to apply the sum
	 *  @param array2: one of the arrays to apply the sum
	 *  @return result of the sum, null if the arrays if of different sizes
	 */
	public static int[] arraysSum(int[] array1, int[] array2)
	{
		if (array1.length!=array2.length)
		{
			return null;
		}
		
		int[] newArray = new int[array1.length];
		for(int i=0;i<array1.length;i++)
		{
			newArray[i] = array1[i]+array2[i]; 
		}
		
		return newArray;
	}
	
	//---------------------------------------------------------------------//
	
	/**
	* Splits a dataset according to the values of a numeric attribute.
	*
	* @param data Data which is to be split
	* @param att Attribute to be used for splitting
	* @param splitValue attribute value used to split data 
	* @return The sets of instances produced by the split
	*/
	public static InstanceSet[] splitData(InstanceSet data, Attribute att, double splitValue)
	{
		InstanceSet[] splitData = new InstanceSet[2];
		int numInstancesMoreThan = 0,numInstancesLessThan = 0;
		int attIndex = att.getIndex();
		String[] values = att.getAttributeValues();
		Instance instance;
				
		//counts number of instances for each instance set
		for(int i=0;i<data.numInstances();i++)
		{
			instance = data.getInstance(i);
			if(Double.parseDouble(values[instance.getValue(attIndex)])>=splitValue)
			{
				numInstancesMoreThan++;								
			}
			else
			{
				numInstancesLessThan++;
			}
		}
		
		//initiates splitData
		splitData[0] = new InstanceSet(data, numInstancesMoreThan);
		splitData[1] = new InstanceSet(data, numInstancesLessThan);
		
		//fill splitData with apropriate instances in each part
		for(int i=0;i<data.numInstances();i++)
		{
			instance = data.getInstance(i);
			if(Double.parseDouble(values[instance.getValue(attIndex)])>=splitValue)
			{
				splitData[0].add(instance);								
			}
			else
			{
				splitData[1].add(instance);
			}
		}
		
		return splitData;	
	}
	
	//---------------------------------------------------------------------//
	
	/**gets the attribute at index position's split value. Valid for numeric attributes
	 * @param index attribute's index
	 * @return attribute's split value
	 */
	public double getSplitValue(int index)
	{
		return splitValues[index];
	}
}
