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

        private int[][][] counts;
        private int[] priors;

	//--------------------------------------------------------------------//

	/** ID3Utils constructor */
	public ClassifierUtils(InstanceSet inst)
	{
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
	}

        /** Returns the computed priors

            @return the computed priors.
        */
        public int[] getPriors()
        {	return priors;
        }

        /** Returns the computed counts for nominal attributes

            @return the computed counts.
        */
        public int[][][] getCounts()
        {	return counts;
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
        double infoGain = computeEntropy(priors,totalSum);			//Entropy(S)
        double[] resultGain = new double[counts.length];
        Arrays.fill(resultGain,infoGain);
        for (int i=0;i<counts.length;i++)
        {

          for (int j=0;j<counts[i].length;j++)
          {
            double temp = computeEntropy(counts[i][j],totalSum);
            resultGain[i] -= temp;
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
}
