/*
 * Created on 24/05/2003
 *
 */
package unbbayes.datamining.datamanipulation;

import java.util.*;

import unbbayes.datamining.classifiers.*;
/**
 * @author Mário Henrique
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ClassifierUtils2 {
	/** stores the calculated logs */
	private HashMap logmap;
	/** value of ln(2) to be used in the log2 function*/
	private static final double LN2 = Math.log(2);
	private InstanceSet instances;

	public ClassifierUtils2(InstanceSet inst)
	{
		instances = inst;
				logmap = new HashMap();
				Double zeroDouble = new Double(0);
				if(logmap.containsKey(zeroDouble))
					logmap.put(zeroDouble,zeroDouble);
				if(logmap.containsKey(zeroDouble))
					logmap.put(new Double(-0),zeroDouble);		
		
	}

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
		Integer[] att = split.getAttributes();
		int numAtt = att.length;
		Integer[] newAttributes = new Integer[numAtt-1];
		int j=0;
		for(int i=0;i<numAtt;i++)
		{
			if (attIndex!=i)
			{
				newAttributes[j]=att[i];
				j++;
			}
		}
		int numData = instances.getAttribute(att[attIndex].intValue()).numValues();
		ArrayList[] data = new ArrayList[numData];
		for (int i=0;i<numData;i++)
		{
			data[i] = new ArrayList();
		}
		ArrayList inst = split.getInstances();
		int numInst = inst.size();
		for (int i=0;i<numInst;i++)
		{
			Instance instance = instances.getInstance(((Integer)inst.get(i)).intValue());
			int numDataset = (int)instance.getValue(att[attIndex].intValue());
			data[numDataset].add(inst.get(i));
		}
		SplitObject[] splitObject = new SplitObject[numData];
		for (int i=0;i<numData;i++)
		{
			splitObject[i] = new SplitObject();
			splitObject[i].setInstances(data[i]);
			splitObject[i].setAttributes(newAttributes);
		}
		return splitObject;
	}

	//--------------------------------------------------------------------//

	/**
	* Computes information gain for an attribute.
	*
	* @param data Data for which info gain is to be computed
	* @param att Attribute
	* @return Information gain for the given attribute and data
	*/
	  public double[] computeInfoGain(SplitObject split)
	  {
		Integer[] att = split.getAttributes();
		ArrayList inst = split.getInstances();
		int numClasses = instances.numClasses();
		int numInst = inst.size();
		int classIndex = instances.getClassIndex();
		
		// Reserve space
		int[][][] counts = new int[att.length - 1][][];
		int[] priors = new int[numClasses];
		   

		int attIndex = 0;
		for(int i=0;i<att.length;i++)
		{
			if (att[i].intValue()!=classIndex)
			{
				counts[attIndex] = new int[instances.getAttribute(att[i].intValue()).numValues()][numClasses];
				attIndex++;				
			}
		}
		
		// Compute counts and sums
		for (int i=0;i<numInst;i++)
		{
			Instance instance = instances.getInstance(((Integer)inst.get(i)).intValue());  
			if (!instance.classIsMissing())
			{
				attIndex = 0;
				for (int j=0;j<att.length;j++)
				{
					int attribute = att[j].intValue();
					if (attribute!=classIndex && !instance.isMissing(attribute))
					{
						counts[attIndex][(int)instance.getValue(attribute)][(int)instance.classValue()] += instance.getWeight();	
						attIndex++;
					}					
				}
				priors[(int)instance.classValue()] += instance.getWeight();
			}
		}
		
		int totalSum = Utils.sum(priors);
		double infoGain = computeEntropy(priors,totalSum);	//entropy(S)
		double[] resultGain = new double[counts.length];
		if (counts.length==0)
		{
			resultGain = new double[1];
			return resultGain;
		}
		Arrays.fill(resultGain,infoGain);
        
		//for each attribute...
		for (int i=0;i<counts.length;i++)
		{
			//if attribute is nominal...
			for (int j=0;j<counts[i].length;j++)
			{
				//if (counts[i][j]!=null)
				//{
					resultGain[i] -= computeEntropy(counts[i][j],totalSum);	
				//}				
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

}
