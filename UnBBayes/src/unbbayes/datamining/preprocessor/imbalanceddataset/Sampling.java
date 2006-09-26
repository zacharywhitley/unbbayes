package unbbayes.datamining.preprocessor.imbalanceddataset;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 * This class implements simple undersampling and oversampling algorithms and
 * random ones.
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 26/08/2006
 */
public class Sampling {
	
	public static void simpleSampling(InstanceSet instanceSet, float proportion,
			int classValue) {
		Instance instance;
		int numInstances;
		float weight;

		testParameters(instanceSet, proportion, classValue);

		numInstances = instanceSet.numInstances();
		for (int i = 0; i < numInstances; i++) {
			instance = instanceSet.getInstance(i);
			if (instance.classValue() == classValue) {
				weight = instance.getWeight();
				weight = Math.round(weight * proportion);
				instance.setWeight(weight);
			}
		}
	}

	public static void limitWeight(InstanceSet instanceSet, int limit,
			int classValue) {
		Instance instance;
		int numInstances;

		testParameters(instanceSet, limit, classValue);

		numInstances = instanceSet.numInstances();
		for (int i = 0; i < numInstances; i++) {
			instance = instanceSet.getInstance(i);
			if (instance.classValue() == classValue) {
				if (instance.getWeight() > limit) {
					instance.setWeight(limit);
				}
			}
		}
	}

	private static void testParameters(InstanceSet instanceSet, float proportion, int classIndex) {
		/* Message thrown as an exception in case of wrong arguments */ 
		String exceptionMsg = "";
		
		/* Test the parameters */
		if (instanceSet == null) exceptionMsg = "The instanceSet is null!";
		if (proportion <= 0) exceptionMsg = "proportion must be greater than 0!";
		
		/* Throw exception if there is problems with the parameters */
		if (exceptionMsg != "") throw new IllegalArgumentException(exceptionMsg);
	}		

}

