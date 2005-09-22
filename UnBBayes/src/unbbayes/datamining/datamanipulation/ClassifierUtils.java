package unbbayes.datamining.datamanipulation;

import java.util.*;

import unbbayes.datamining.classifiers.decisiontree.*;

/**
 * Class implementing information gain method and other related actions
 *
 *  @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 *
 */
public class ClassifierUtils 
{
	/** stores the calculated logs */
	private HashMap<Double,Double> logmap;
	/** value of ln(2) to be used in the log2 function*/
	private static final double LN2 = Math.log(2);
	/** instance set used in the methods */
	private InstanceSet instances;
	
	//----------------------------------------------------------------------//

	/**
	 * Default constructor
	 * 
	 * @param inst instance set used for the classification functions
	 */
	public ClassifierUtils(InstanceSet inst)
	{
		instances = inst;
		logmap = new HashMap<Double,Double>();
		Double zeroDouble = new Double(0);
		if(logmap.containsKey(zeroDouble))
			logmap.put(zeroDouble,zeroDouble);
		if(logmap.containsKey(zeroDouble))
			logmap.put(new Double(-0),zeroDouble);		
	}
	
	//--------------------------------------------------------------------//

	/**
	 * Calculates a*log(a) base 2
	 * 
	 * @param a a number greater than 0.0
	 * @return a*log(a) base 2
	 */
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
	 * Splits an instance set based on an attribute
	 * 
	 * @param split object representing an instance set
	 * @param attIndex attribute index relative to the split 
	 * @return objects representing each instance set part
	 */
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
		float[] numInstancesPerValue = new float[numValues];
		ArrayList missingValueInstances = new ArrayList(); 
		for (int i=0;i<numInst;i++)
		{
		  instance = getInstance(inst,i); 
		  if(!instance.isMissing(att[attIndex].intValue()))
		  {
			  numDataset = (int)instance.getValue(att[attIndex].intValue());
			  data[numDataset].add(inst.get(i));
			  numInstancesPerValue[numDataset] += instance.getWeight();
		  }
		  else
		  {
			missingValueInstances.add(instance);
		  }
		}
		
		//put every instance with missing value in every split data arraylist
		Instance newInstance;
		float completeInstancesSum = Utils.sum(numInstancesPerValue);
		for(int i=0;i<numValues;i++)
		{
			for(int j=0;j<missingValueInstances.size();j++)
			{
				if(numInstancesPerValue[i]!=0)
				{
					instance = (Instance)missingValueInstances.get(j);
					newInstance = new Instance(instance);
					newInstance.setDataset(instance.getDataset());
					newInstance.setWeight(instance.getWeight()*numInstancesPerValue[i]/completeInstancesSum); 
					data[i].add(newInstance);
				}
			}
		}
				
		//create the split objects with the new attributes and an arraylist relative to a value
		SplitObject[] splitObject = new SplitObject[numValues];
		for (int i=0;i<numValues;i++)
		{
			splitObject[i] = new SplitObject(data[i],newAttributes);
		}
		
		return splitObject;
	}
	
	//---------------------------------------------------------------------//
	
	/**
	 * Splits a dataset according to the values of a numeric attribute.
	 *
	 * @param split index for data which is to be split
	 * @param attIndex index for the attribute
	 * @param splitValue attribute value used to split data 
	 * @return The instance sets produced by the split
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
			
		//arrange instances according to split value
		ArrayList instancesMoreThan = new ArrayList();
		ArrayList instancesLessThan = new ArrayList();
		ArrayList missingValueInstances = new ArrayList();
		float[] numInstancesPerValue = new float[2];
		for(int i=0;i<actualInst.size();i++)
		{
			instance = getInstance(actualInst,i);
			if(!instance.isMissing(actualAtt[attIndex].intValue()))
			{
				if(Double.parseDouble(values[instance.getValue(actualAtt[attIndex].intValue())])>=splitValue)
				{
					instancesMoreThan.add(actualInst.get(i));
					numInstancesPerValue[0] += instance.getWeight(); 								
				}
				else
				{
					instancesLessThan.add(actualInst.get(i));
					numInstancesPerValue[1] += instance.getWeight();
				}
			}
			else
			{
				missingValueInstances.add(instance);
			}
		}
		
		//put every instance with missing value in every split data arraylist
		Instance newInstance;
		float completeInstancesSum = Utils.sum(numInstancesPerValue);
		for(int i=0;i<missingValueInstances.size();i++)
		{
			instance = (Instance)missingValueInstances.get(i);
			if(numInstancesPerValue[0]!=0)
			{
				newInstance = new Instance(instance);
				newInstance.setDataset(instance.getDataset());
				newInstance.setWeight(instance.getWeight()*numInstancesPerValue[0]/completeInstancesSum); 
				instancesMoreThan.add(newInstance);
			}
			if(numInstancesPerValue[1]!=0)
			{
				newInstance = new Instance(instance);
				newInstance.setDataset(instance.getDataset());
				newInstance.setWeight(instance.getWeight()*numInstancesPerValue[1]/completeInstancesSum); 
				instancesLessThan.add(newInstance);
			}
		}
			
		splitData[0] = new SplitObject(instancesMoreThan,newAttributes);
		splitData[1] = new SplitObject(instancesLessThan,newAttributes);
		
		return splitData;	
	} 

	//--------------------------------------------------------------------//

	/**
	* Computes information gain for the instance set attributes.
	*
	* @param split object representing an instance set
	* @return Information gain for the given attribute and data
	*/
	  public double[] computeInfoGain(SplitObject split)
	  {
		return computeInfoGain(split, new double[split.getAttributes().length], new ArrayList());  	
	  }
	  
	//------------------------------------------------------------------//
	
	/**
	 * Computes information gain for the instance set attributes.
	 *
	 * @param split object representing an instance set
	 * @param splitValues cut values obtained in computation
	 * @param numericDataList numeric data obtained in the computation of numeric attributes 
	 * @return Information gains for the split attributes
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
		float[][][] counts = new float[att.length - 1][][];
		//number of instances without missing values for each attribute
		float[] totalSums = new float[att.length-1];
		//total class distribution without considering missing values
		float[] totalCounts = new float[numClassValues];
		//infoGains obtained
		double[] resultGain = new double[att.length-1];
		//stores if the attributes has missing values or not
		boolean[] hasMissingValues = new boolean[att.length-1];
		
		//initialize counts  
		//for each attribute...
		for(int i=0,attIndex=0;i<att.length;i++)
		{
			if (att[i].intValue()!=classIndex)
			{
				counts[attIndex] = new float[instances.getAttribute(att[i].intValue()).numValues()][numClassValues];
				attIndex++;				
			}
		}
		
		//Compute counts
		//for each instance...
		Instance instance;
		int attributeIndex;
		Arrays.fill(hasMissingValues,false);
		//for each instance...
		for (int i=0;i<numInst;i++)
		{
			instance = getInstance(inst,i); 
			if (!instance.classIsMissing())
			{
				//for each attribute...
				for (int j=0,attIndex=0;j<att.length;j++)
				{
					attributeIndex = att[j].intValue();
					if (attributeIndex!=classIndex)
					{
						if (!instance.isMissing(attributeIndex))
						{
							counts[attIndex][(int)instance.getValue(attributeIndex)][(int)instance.classValue()] += instance.getWeight();
						}
						else
						{
							hasMissingValues[attIndex]=true;
						}
													
						attIndex++;
					}					
				}
				totalCounts[instance.classValue()] += instance.getWeight();
			}
		}
		
		//data for attributes without missing values
		float totalSum = Utils.sum(totalCounts);
		double totalInfoGain = computeEntropy(totalCounts,totalSum);
		
		//computes entropy(S) for each attribute
		//different for each attribute because of the possibility of missing values
		float[] attributeCounts = new float[numClassValues];
		double infoGain;
		        
		for(int i=0;i<counts.length;i++)
		{
			if(hasMissingValues[i])
			{
				Arrays.fill(attributeCounts,0);
				for(int j=0;j<counts[i].length;j++)
				{
					for(int k=0;k<counts[i][j].length;k++)
					{
						attributeCounts[k] += counts[i][j][k];
					}
				}
				totalSums[i] = Utils.sum(attributeCounts);
				infoGain = computeEntropy(attributeCounts,totalSums[i]);
			}
			else
			{
				totalSums[i] = totalSum;
				infoGain = totalInfoGain;
			}
			
			resultGain[i] = infoGain;
		}
		
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
					resultGain[i] -= computeEntropy(counts[i][j],totalSums[i]);	
				}
			}
			
			//if attribute is numeric...
			else
			{	
				//array with possible values
				double[] values; 
				//array with classes distribution for each attribute value,
				float[][] classesDistribution;
				
				//gets values effectively used sorted
				String[] oldValues = instances.getAttribute(att[i].intValue()).getAttributeValues();
				ArrayList valuesTemp = new ArrayList();
				for(int x=0;x<inst.size();x++)
				{
					instance = getInstance(inst,x);
					if(!instance.isMissing(att[i].intValue()))
					{
						if(!valuesTemp.contains(oldValues[instance.getValue(att[i].intValue())]))
						{
							valuesTemp.add(oldValues[instance.getValue(att[i].intValue())]);
						}
					}
				}
				values = new double[valuesTemp.size()];
				for (int x=0;x<valuesTemp.size();x++)
				{
					values[x] = Double.parseDouble((String)valuesTemp.get(x));
				}
				Arrays.sort(values);
				
				classesDistribution = new float[values.length][numClassValues];
				float[] missingValuesDistribution = new float[numClassValues];
				
				//fill classes distribution
				//for each instance...
				for(int x=0;x<inst.size();x++)
				{
					instance = getInstance(inst,x);
					if (!instance.isMissing(att[i].intValue()))
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
					else
					{
						missingValuesDistribution[(int)instance.classValue()] += instance.getWeight();
					}
				}					
							        						
				//search for the minimum entropy
				float[] distribution1, distribution2;
				double minimumEntropy = Integer.MAX_VALUE;
				double entropy; 
				double minimumValue = Integer.MAX_VALUE;
				float[] sumPart1, sumPart2;
				double actualValue;
				NumericData numericData = new NumericData(i,missingValuesDistribution); 
				//for each attribute value...
				for(int x=1;x<values.length;x++)
				{
					distribution1 = classesDistribution[x-1];
					distribution2 = classesDistribution[x];
					sumPart1 = new float[numClassValues]; 
					sumPart2 = new float[numClassValues];
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
						
						entropy = computeEntropy(sumPart1,totalSums[i]) + computeEntropy(sumPart2,totalSums[i]);
						
						actualValue = (double)(values[x-1]+values[x])/2.0d;
						
						numericData.addData(actualValue,resultGain[i]-entropy,sumPart1,sumPart2);
												
						if (minimumEntropy>entropy)
						{
							minimumEntropy = entropy;
							minimumValue = actualValue;
						}
					}
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
	* Computes the entropy of a distribution.
	*
	* @param classValues classes distribution used to compute the entropy
	* @param totalSum total number of instances from the instance set 
	* @return Entropy of the classes distribution
	*/
	public double computeEntropy(float[] classValues,float totalSum)
	{
	  double entropy = 0;
	  float sum = 0;
	  for (int i=0;i<classValues.length;i++)
	  {
		entropy -= xlog2(classValues[i]);
		sum += classValues[i];
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
		float numInstances = getNumberOfInstances(split); 
		    	
		//for each value...
		float numInstancesSplit;
		float totalNumInstancesSplit = 0;
		double splitInfo = xlog2(numInstances);
		for (int i=0; i < numValues; i++)
		{	
			numInstancesSplit = getNumberOfInstances(splitData[i]);
			if (numInstancesSplit > 0)
			{
				splitInfo -= xlog2(numInstancesSplit);
			}
			totalNumInstancesSplit += numInstancesSplit;
		}
		
		//work with missing values
		if(totalNumInstancesSplit<numInstances)
		{
			splitInfo -= xlog2(numInstances-totalNumInstancesSplit);
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
	
	/**
	 * Returns the number of instances from a split object
	 * 
	 * @param split object representing an instance set
	 * @return number of instances from split
	 */
	public float getNumberOfInstances(SplitObject split)
	{
		float numInstances = 0;
		ArrayList actualInst = split.getInstances();
		Instance instance;
		for(int i=0;i<actualInst.size();i++)
		{
			instance = getInstance(actualInst,i);
			numInstances += instance.getWeight();	
		}
		
		return numInstances;
	}
	
	//---------------------------------------------------------------------//
	
	/**
	  * Gets the instance from an index relative to a list of indexes
	  * 
	  * @param inst list of instances
	  * @param i instance index
	  * @return instance from index i
	  */
	public Instance getInstance(ArrayList inst, int i)
	{
		if(inst.get(i) instanceof Integer)
		{
			return instances.getInstance(((Integer)inst.get(i)).intValue());
		}
		else
		{
			return (Instance)inst.get(i);
		}
	}
	
	//---------------------------------------------------------------------//
  	
	/**
	 * Checks if two classes distributions refers to the same class
	 * @param distribution1: one of the distributions to be evaluated
	 * @param distribution2: one of the distributions to be evaluated
	 * @return boolean value indicating if distributions refers to the same class or not 
	 */
	public static boolean hasSameClass(float[] distribution1, float[] distribution2)
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
	
	//----------------------------------------------------------------------//

	/**
	 * Returns the logarithm for base 2 of a double value. Special cases:
	 * If the argument is NaN or less than zero, then the result is NaN.
	 * If the argument is positive infinity, then the result is positive infinity.
	 * If the argument is positive zero or negative zero, then the result is negative infinity.
	 *
	 * @param a a number greater than 0.0.
	 * @return The value log2 a, the natural logarithm of a.
	 */
	public static double log2(double a)
	{
	  return Math.log(a)/LN2;
	}
	

	//----------------------------------------------------------------------//

	/**
	 * Returns the information gain for one possible position for the breakpoint in a discretization procedure
	 * 
	 * @param beforeInfoPoint the values for all classes before the breakpoint.
	 * @param afterInfoPoint the values for all classes after the breakpoint.
	 * @return A value between 0 and 1, the information gain generated.
	 */
	public double computeNumericInfo(float[] beforeInfoPoint,float[] afterInfoPoint)
	{
		float sum = 0;
		float beforeSum = 0;
		float afterSum = 0;
		for (float i : beforeInfoPoint) {
			beforeSum += i;
		}
		for (float i : afterInfoPoint) {
			afterSum += i;
		}
		sum = beforeSum + afterSum;
		
		double beforeEntropy = computeEntropy(beforeInfoPoint,beforeSum);
		double afterEntropy = computeEntropy(afterInfoPoint,afterSum);
		
		return (beforeSum / sum * beforeEntropy) + (afterSum / sum * afterEntropy);
	}
	
	//-------------------------------------------------------------------------//
	
	/**
	 * Returns the number of non class distribution components 
	 * 
	 * @param distribution distribution to be evaluated
	 * @param classIndex index of class value
	 * @return number of non class distribution components 
	 */
	public static double sumNonClassDistribution(float[] distribution, int classIndex)
	{
		int sum = 0;
		for(int i=0;i<distribution.length;i++)
		{
			if(i!=classIndex)
			{
				sum += distribution[i];
			}
		}
		return sum;
	}
}
