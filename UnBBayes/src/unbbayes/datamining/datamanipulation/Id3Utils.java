package unbbayes.datamining.datamanipulation;

import java.util.*;

/**
 * Class implementing information gain method and other related actions
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @author Danilo Balby Silva Castanheira (danbalby@yahoo.com)
 *  @version $1.0 $ (16/02/2002)
 */
public class Id3Utils
{
	/** stores the calculated logs */
	private HashMap logmap;
	/** Load resources file for internacionalization */
	private ResourceBundle resource;
	/** value of ln(2) to be used in the log2 function*/
	private static final double LN2 = Math.log(2);
	
	
	//--------------------------------------------------------------------//
	
	/** ID3Utils constructor */
	public Id3Utils()
	{
		logmap = new HashMap();
		resource = ResourceBundle.getBundle("unbbayes.datamining.classifiers.resources.ClassifiersResource");		
	}
	
	//--------------------------------------------------------------------//
	
	/**
   	* Computes information gain for an attribute.
   	*
   	* @param data Data for which info gain is to be computed
   	* @param att Attribute
   	* @return Information gain for the given attribute and data
   	*/
  	public double computeInfoGain(InstanceSet data, Attribute att) throws Exception
  	{	
  		int numInstances = data.numWeightedInstances();	
  		double infoGain = computeEntropy(data);			//Entropy(S)
  		int numInstancesSplit;			
  				
		//the execution only proceeds if the attribute values are nominal
		if (att.isNominal())
		{	
			//- Entropy(Svalue1) - Entropy(Svalue2)..
			int numValues = att.numValues();
			InstanceSet[] splitData = splitData(data, att);
			for (int j = 0; j < numValues; j++)
  			{	
  				numInstancesSplit = splitData[j].numWeightedInstances();
				if (numInstancesSplit > 0)
  				{	
  					infoGain -= ((double) numInstancesSplit / (double) numInstances) * computeEntropy(splitData[j]);
				}
    		}
		}
		else
		{
			throw new Exception(resource.getString("exception1"));
		}	
    	return infoGain;	//Entropy(S) - Entropy(Svalue1) - Entropy(Svalue2)..
  	}
  	
  	//--------------------------------------------------------------------//

  	public double computeGainRatio(InstanceSet data, Attribute att) throws Exception
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
  					//splitInfo += (-1 * ((double)numInstancesSplit/(double)numInstances) * log2((double)numInstancesSplit/(double)numInstances));
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
  	}
  	
  	//--------------------------------------------------------------------//

  	/**
  	* Computes the entropy of a dataset.
  	*
  	* @param data Data for which entropy is to be computed
  	* @return Entropy of the data's class distribution
  	*/
  	public double computeEntropy(InstanceSet data) throws Exception
  	{		
  		//Computes the number of instances for each class - classCounts
  		double [] classCounts = new double[data.numClasses()];
    	Enumeration instEnum = data.enumerateInstances();
    	while (instEnum.hasMoreElements())
  		{		
  			Instance inst = (Instance) instEnum.nextElement();
      		classCounts[(int) inst.classValue()] += inst.getWeight();
    	}
    	
    	//Computes the entropy: 
    	//(classCounts[1]*log2(classCounts[1])+...+classCounts[n]*log2(classCounts[n]))/numWeightedInstances + log2(numWeightedInstances)
    	double entropy = 0;
  		int numClasses = data.numClasses();
    	for (int j = 0; j < numClasses; j++)
  		{		
  			if (classCounts[j] > 0)
  			{		
  				//entropy -= classCounts[j] * log2(classCounts[j]);
				entropy -= xlog2(classCounts[j]);
      		}
    	}
    	//numWeightedInstances = number of instances for the current value of an attribute
    	int numWeightedInstances = data.numWeightedInstances();	
		//entropy /= (double) numWeightedInstances;
		//return entropy + log2(numWeightedInstances);
		return (entropy + xlog2(numWeightedInstances))/numWeightedInstances;
  	}
  
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
  	public double log2(double a)
  	{	
  		return Math.log(a)/LN2;			  		
  	}
  	
	public double xlog2(double a)
	{	
		Double aDouble = new Double(a);
		if(logmap.containsKey(aDouble))
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
  	* Splits a dataset according to the values of an attribute.
  	*
  	* @param data Data which is to be split
  	* @param att Attribute to be used for splitting
  	* @return The sets of instances produced by the split
  	*/
  	public static InstanceSet[] splitData(InstanceSet data, Attribute att)
  	{	
  		int numInstances = data.numInstances();
		InstanceSet[] splitData;
  		int numValues = att.numValues();
		splitData = new InstanceSet[numValues];
		for (int j = 0; j < numValues; j++)
  		{	
  			splitData[j] = new InstanceSet(data, numInstances);
  		}
    	Enumeration instEnum = data.enumerateInstances();
    	while (instEnum.hasMoreElements())
  		{	
  			Instance inst = (Instance) instEnum.nextElement();
    		splitData[(int) inst.getValue(att)].add(inst);
    	}
		return splitData;
  	}
}
