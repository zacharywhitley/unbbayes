package unbbayes.datamining.classifiers;

import java.util.*;

import unbbayes.datamining.datamanipulation.*;

/**
 *  Abstract Bayesian Classifier. All schemes that works with bayesian classifiers extends this class.
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (17/02/2002)
 */
public abstract class BayesianLearning extends Classifier
{	/**
   	* Predicts the class memberships for a given instance. If
   	* an instance is unclassified, the returned array elements
   	* must be all zero. If the class is numeric, the array
   	* must consist of only one element, which contains the
   	* predicted value.
   	*
   	* @param instance the instance to be classified
   	* @return an array containing the estimated membership
   	* probabilities of the test instance in each class (this
   	* should sum to at most 1)
   	* @exception Exception if distribution could not be
   	* computed successfully
   	*/
  	public abstract float[] distributionForInstance(Instance instance) throws Exception;

  	/** Load resources file from this package */
	protected static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.datamining.classifiers.resources.ClassifiersResource");

	/**
   	* Classifies the given test instance. The instance has to belong to a
   	* dataset when it's being classified.
   	*
   	* @param instance the instance to be classified
   	* @return the predicted most likely class for the instance or
   	* Instance.missingValue() if no prediction is made
   	* @exception Exception if an error occurred during the prediction
   	*/
  	public short classifyInstance(Instance instance) throws Exception
	{	float[] dist = distributionForInstance(instance);
    	if (dist == null)
		{	throw new Exception(resource.getString("nullPrediction"));
    	}
    	switch (instance.getDataset().getClassAttribute().getAttributeType())
		{	case Attribute.NOMINAL: float max = 0;
      								short maxIndex = 0;
      								for (int i = 0; i < dist.length; i++)
									{	if (dist[i] > max)
										{	maxIndex = (short)i;
	  										max = dist[i];
										}
      								}
      								if (max > 0)
									{	return maxIndex;
      								}
									else
									{	return Instance.missingValue();
      								}

			case Attribute.NUMERIC: return (short)Utils.maxIndex(dist);

			default:				return Instance.missingValue();
    	}
  	}
}
