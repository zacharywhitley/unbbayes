package unbbayes.datamining.datamanipulation;

import java.util.*;

import unbbayes.datamining.classifiers.decisiontree.*;

/**
 * Class implementing information gain method and other related actions
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 *  @version $1.0 $ (16/02/2002)
 */
public class ClassifierUtils {
	/** stores the calculated logs */
	private HashMap logmap;
	/** value of ln(2) to be used in the log2 function*/
	private static final double LN2 = Math.log(2);
	/** instance set used in the methods */
	private InstanceSet instances;
	
	//----------------------------------------------------------------------//

	public ClassifierUtils(InstanceSet inst)
	{
		instances = inst;
		logmap = new HashMap();
		Double zeroDouble = new Double(0);
		if(logmap.containsKey(zeroDouble))
			logmap.put(zeroDouble,zeroDouble);
		if(logmap.containsKey(zeroDouble))
			logmap.put(new Double(-0),zeroDouble);		
	}
	
	//----------------------------------------------------------------------//

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

	public SplitObject[] splitData(SplitObject split,int attIndex)
	{
		//old attributes' indexes
		Integer[] att = split.getAttributes();
		//number of attributes
		int numAtt = att.length;
		//new attributes' indexes
		Integer[] newAttributes = new Integer[numAtt-1];
		//old instances' indexes
		ArrayList inst = split.getInstances();
		//number of values from split attribute
		int numValues = instances.getAttribute(att[attIndex].intValue()).numValues();
				
		//copy old attributes array to new attributes array without split attribute		
		for(int i=0, j=0;i<numAtt;i++)
		{
			if (attIndex!=i)
			{
				newAttributes[j]=att[i];
				j++;
			}
		}
				
		//create ArrayList of ArrayLists, one for each split atribute value 
		ArrayList[] data = new ArrayList[numValues];
		for (int i=0;i<numValues;i++)
		{
			data[i] = new ArrayList();
		}
				
		//puts an instance in each arrayList, depending of this value for the split attribute
		Instance instance;
		int numDataset;
		int numInst = inst.size();
		for (int i=0;i<numInst;i++)
		{
			instance = instances.getInstance(((Integer)inst.get(i)).intValue());
			numDataset = (int)instance.getValue(att[attIndex].intValue());
			data[numDataset].add(inst.get(i));
		}
		
		//create the split objects with the new attributes and an arraylist relative to a value
		SplitObject[] splitObject = new SplitObject[numValues];
		for (int i=0;i<numValues;i++)
		{
			splitObject[i] = new SplitObject(data[i],newAttributes);
		}
		
		return splitObject;
	}

	//--------------------------------------------------------------------//

	/**
	* Computes information gain for an attribute.
	*
	* @param split set of indexes used for the computation
	* @return Information gain for the given attribute and data
	*/
	  public double[] computeInfoGain(SplitObject split)
	  {
		return computeInfoGain(split, new double[split.getAttributes().length], new ArrayList());  	
	  }
	  
	//------------------------------------------------------------------//
	/**
	 * Computes information gain for an attribute.
	 *
	 * @param split set of indexes used for the computation
	 * @param att Attribute
	 * @param splitValues cut values obtained in computation
	 * @param numericDataList numeric data obtained in the computation of numeric attributes 
	 * @return Information gain for the given attribute and data
	 */
	  public double[] computeInfoGain(SplitObject split, double[] splitValues, ArrayList numericDataList)
	  {
	  	//attributes' indexes
		Integer[] att = split.getAttributes();
		//instances' indexes
		ArrayList inst = split.getInstances();
		//number of class values
		int numClassValues = instances.numClasses();
		//number of instances
		int numInst = inst.size();
		//class attribute's index
		int classIndex = instances.getClassIndex();
		//class values distribution for each value of each attribute 
		int[][][] counts = new int[att.length - 1][][];
		//total class values distribution   
		int[] priors = new int[numClassValues];
		
		//splitValues = new double[att.length];
		//numericDataList = new ArrayList();
		   
		//initialize counts  
		int attIndex = 0;
		//for each attribute...
		for(int i=0;i<att.length;i++)
		{
			if (att[i].intValue()!=classIndex)
			{
				counts[attIndex] = new int[instances.getAttribute(att[i].intValue()).numValues()][numClassValues];
				attIndex++;				
			}
		}
		
		//Compute counts and priors
		//for each instance...
		Instance instance;
		int attributeIndex;
		for (int i=0;i<numInst;i++)
		{
			instance = instances.getInstance(((Integer)inst.get(i)).intValue());
			if (!instance.classIsMissing())
			{
				attIndex = 0;
				//for each attribute...
				for (int j=0;j<att.length;j++)
				{
					attributeIndex = att[j].intValue();
					if (attributeIndex!=classIndex && !instance.isMissing(attributeIndex))
					{
						counts[attIndex][(int)instance.getValue(attributeIndex)][(int)instance.classValue()] += instance.getWeight();	
						attIndex++;
					}					
				}
				priors[(int)instance.classValue()] += instance.getWeight();
			}
		}
		
		int totalSum = Utils.sum(priors);
		double infoGain = computeEntropy(priors,totalSum);	//entropy(S)
		double[] resultGain = new double[counts.length];
		Arrays.fill(resultGain,infoGain);
		
		if (counts.length==0)
		{
			resultGain = new double[1];
			return resultGain;
		}
		        
		//for each attribute...
		for (int i=0;i<counts.length;i++)
		{
			//if attribute is nominal...
			if(instances.getAttribute(att[i].intValue()).isNominal())
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
				double[] values; 
				//array with classes distribution for each attribute value,
				int[][] classesDistribution;
				
				//gets values effectively used sorted
				String[] oldValues = instances.getAttribute(att[i].intValue()).getAttributeValues();
				ArrayList valuesTemp = new ArrayList();
				for(int x=0;x<inst.size();x++)
				{
					instance = instances.getInstance(((Integer)inst.get(x)).intValue());
					if(!valuesTemp.contains(oldValues[instance.getValue(att[i].intValue())]))
					{
						valuesTemp.add(oldValues[instance.getValue(att[i].intValue())]);
					}
				}
				values = new double[valuesTemp.size()];
				for (int x=0;x<valuesTemp.size();x++)
				{
					values[x] = Double.parseDouble((String)valuesTemp.get(x));
				}
				Arrays.sort(values);
				
				classesDistribution = new int[values.length][numClassValues];
				
				//fill classes distribution
				//for each instance...
				for(int x=0;x<inst.size();x++)
				{
					instance = instances.getInstance(((Integer)inst.get(x)).intValue());
					if ((!instance.classIsMissing())&&!instance.isMissing(att[i].intValue()))
					{
						//for each value...
						for(int y=0;y<values.length;y++)
						{
							if(values[y]==Double.parseDouble(oldValues[instance.getValue(att[i].intValue())]))
							{
								classesDistribution[y][(int)instance.classValue()] += instance.getWeight();
								break;
							}
						}
					}
				}					
							        						
				//search for the minimum entropy
				double value1 = values[0], value2;
				int[] distribution1, distribution2;
				double minimumEntropy = Integer.MAX_VALUE;
				double entropy; 
				double minimumValue = Integer.MAX_VALUE;
				int[] sumPart1, sumPart2;
				double actualValue;
				NumericData numericData = new NumericData(i); 
				//for each attribute value...
				for(int x=1;x<values.length;x++)
				{
					value2 = values[x];
					distribution1 = classesDistribution[x-1];
					distribution2 = classesDistribution[x];
					sumPart1 = new int[numClassValues]; 
					sumPart2 = new int[numClassValues];
					entropy = 0; 
										
					//if two values has the same class, there is no evaluation
					if(!ClassifierUtils.hasSameClass(distribution1,distribution2))
					{
						for(int y=0;y<x;y++)
						{
							sumPart1 = Utils.arraysSum(sumPart1,classesDistribution[y]);							
						}
						for(int y=x;y<values.length;y++)
						{
							sumPart2 = Utils.arraysSum(sumPart2,classesDistribution[y]);
						}
						
						entropy = computeEntropy(sumPart1,totalSum) + computeEntropy(sumPart2,totalSum);
						
						actualValue = (double)(values[x-1]+values[x])/2.0d;
						
						numericData.addData(actualValue,resultGain[i]-entropy,sumPart1,sumPart2);
												
						if (minimumEntropy>entropy)
						{
							minimumEntropy = entropy;
							minimumValue = actualValue;
						}
					}
					
					value1 = value2;					
				}
				
				if(minimumEntropy==Integer.MAX_VALUE)
				{
					resultGain[i] = 0;
				}
				else
				{
					resultGain[i] -= minimumEntropy;
					splitValues[i] = minimumValue;
				}
				
				numericDataList.add(numericData);
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
	
	/**
	* Computes split information to be used in gain ratio 
	*
	* @param att attribute used to split information
	* @return split information, 1 if split information is 0
	*/
	public double computeSplitInformation(SplitObject split, int splitAttIndex) throws Exception
	{	
		//attribute's indexes
		Integer[] actualAtt = split.getAttributes();
		//split object splitted based on the split attribute
		SplitObject[] splitData = splitData(split,splitAttIndex);
		//split attribute
		Attribute splitAtt = instances.getAttribute(actualAtt[splitAttIndex].intValue());
		//number of values from split attribute 
		int numValues = splitAtt.numValues();
		
		//gets the number of instances
		int numInstances = getNumberOfInstances(split); 
		double splitInfo = xlog2(numInstances);
    	
		//for each value...
		int numInstancesSplit;
		for (int i = 0; i < numValues; i++)
		{	
			numInstancesSplit = getNumberOfInstances(splitData[i]);
			if (numInstancesSplit > 0)
			{
				splitInfo -= xlog2(numInstancesSplit);
			}
		}
		splitInfo /= numInstances;
		
		if (splitInfo != 0)
		{
			return splitInfo;
		}
		else
		{
			return 1;
		}
	}
	
	//---------------------------------------------------------------------//
	
	public int getNumberOfInstances(SplitObject split)
	{
		int numInstances = 0;
		ArrayList actualInst = split.getInstances();
		int instIndex;
		for(int i=0;i<actualInst.size();i++)
		{
			instIndex = ((Integer)actualInst.get(i)).intValue(); 
			numInstances += instances.getInstance(instIndex).getWeight();	
		}
		
		return numInstances;
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
	
	/**
	* Splits a dataset according to the values of a numeric attribute.
	*
	* @param split index for data which is to be split
	* @param attIndex index for the attribute
	* @param splitValue attribute value used to split data 
	* @return The sets of instances produced by the split
	*/
	public SplitObject[] splitNumericData(SplitObject split,int attIndex, double splitValue)
	{
		Instance instance;
		Integer[] actualAtt = split.getAttributes();
		ArrayList actualInst = split.getInstances();
		SplitObject[] splitData = new SplitObject[2];
		Attribute att = instances.getAttribute(actualAtt[attIndex].intValue());
		String[] values = att.getAttributeValues();
		
		//take off the split attribute from the new attributes
		Integer[] newAttributes = new Integer[actualAtt.length-1];
		for(int i=0, j=0;i<actualAtt.length;i++)
		{
			if (attIndex!=i)
			{
				newAttributes[j]=actualAtt[i];
				j++;
			}
		}
			
		ArrayList instancesMoreThan = new ArrayList();
		ArrayList instancesLessThan = new ArrayList();
		
		for(int i=0;i<actualInst.size();i++)
		{
			instance = instances.getInstance(((Integer)actualInst.get(i)).intValue());
			if(Double.parseDouble(values[instance.getValue(actualAtt[attIndex].intValue())])>=splitValue)
			{
				instancesMoreThan.add(actualInst.get(i));								
			}
			else
			{
				instancesLessThan.add(actualInst.get(i));
			}
		}
			
		splitData[0] = new SplitObject(instancesMoreThan,newAttributes);
		splitData[1] = new SplitObject(instancesLessThan,newAttributes);
		
		return splitData;	
	}
}
