package unbbayes.datamining.classifiers;

import java.util.*;

import unbbayes.datamining.datamanipulation.*;

/**
 *  Abstract Bayesian Classifier. All schemes that works with bayesian classifiers extends this class.
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (17/02/2002)
 */
public abstract class DistributionClassifier extends Classifier
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

	protected float[] originalDistribution;
        private int[] classValues;
        private float[] probabilities;
        public static final int NORMAL_CLASSIFICATION = 0;
        public static final int RELATIVE_FREQUENCY_CLASSIFICATION = 1;
        public static final int ABSOLUTE_FREQUENCY_CLASSIFICATION = 2;
        private int classificationType = 0;

        public float[] getOriginalDistribution()
        {   return originalDistribution;
        }

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
	{   float[] dist = distributionForInstance(instance);
            if (dist == null)
            {	throw new Exception(resource.getString("nullPrediction"));
            }
            else
            {   switch (classificationType)
                {   case NORMAL_CLASSIFICATION :              return (byte)Utils.maxIndex(dist);
                    case RELATIVE_FREQUENCY_CLASSIFICATION :  int i,maxIndex = -1;
                                                              float max = Float.MIN_VALUE;
                                                              float local;
                                                              for (i=0;i<dist.length;i++)
                                                              {   local = dist[i]/originalDistribution[i];
                                                                  if (local > max)
                                                                  {   max = local;
                                                                      maxIndex = i;
                                                                  }
                                                              }
                                                              return (byte)maxIndex;
                    case ABSOLUTE_FREQUENCY_CLASSIFICATION :  int j;
                                                              for (j=0; j<classValues.length; j++)
                                                              {   int actualValue = classValues[j];
                                                                  //System.out.println("value "+actualValue);
                                                                  if (dist[actualValue] >= probabilities[j])
                                                                  {   //System.out.println("Predic "+dist[actualValue]);
                                                                      //System.out.println("User "+probabilities[j]);
                                                                  return (byte)actualValue;
                                                                  }
                                                              }
                                                              return Instance.MISSING_VALUE;
                    default:		                      throw new Exception("Classification type not known");
                }
            }
      }

	public byte classifyInstance(float[] dist) throws Exception
	{   
		if (dist == null)
		{	
			throw new Exception(resource.getString("nullPrediction"));
		}
		else
		{   
			switch (classificationType)
			{   
				case NORMAL_CLASSIFICATION :              	return (byte)Utils.maxIndex(dist);
				case RELATIVE_FREQUENCY_CLASSIFICATION :  	int i,maxIndex = -1;
															float max = Float.MIN_VALUE;
															float local;
															for (i=0;i<dist.length;i++)
															{   
																local = dist[i]/originalDistribution[i];
																if (local > max)
																{   
																	max = local;
																	maxIndex = i;
																}
															}
															return (byte)maxIndex;
				case ABSOLUTE_FREQUENCY_CLASSIFICATION :  	int j;
															for (j=0; j<classValues.length; j++)
															{   
																int actualValue = classValues[j];
																if (dist[actualValue] >= probabilities[j])
																{
																	return (byte)actualValue;
																}
															}
															return Instance.MISSING_VALUE;
				default:		                      		throw new Exception("Classification type not known");
			}
		}
	}

      public void setNormalClassification()
      {   classificationType = NORMAL_CLASSIFICATION;
      }

      public void setRelativeClassification()
      {   classificationType = RELATIVE_FREQUENCY_CLASSIFICATION;
      }

      public void setAbsoluteClassification(int[] classValues,float[] probabilities)
      {   classificationType = ABSOLUTE_FREQUENCY_CLASSIFICATION;
          this.classValues = classValues;
          this.probabilities = probabilities;
      }
}
