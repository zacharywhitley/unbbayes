package unbbayes.datamining.preprocessor.imbalanceddataset;

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
		testParameters(instanceSet, proportion, classValue);

		int classIndex = instanceSet.classIndex;
		int counterIndex = instanceSet.counterIndex;
		int numInstances= instanceSet.numInstances();
		float weight;

		for (int i = 0; i < numInstances; i++) {
			if (instanceSet.instances[i].data[classIndex] == classValue) {
				weight = instanceSet.instances[i].data[counterIndex];
				weight = Math.round(weight * proportion);
				instanceSet.instances[i].data[counterIndex] = weight;
			}
		}
	}

	public static void limitWeight(InstanceSet instanceSet, int limit,
			int classValue) {
		testParameters(instanceSet, limit, classValue);

		int classIndex = instanceSet.classIndex;
		int counterIndex = instanceSet.counterIndex;
		int numInstances= instanceSet.numInstances();

		numInstances = instanceSet.numInstances();
		for (int i = 0; i < numInstances; i++) {
			if (instanceSet.instances[i].data[classIndex] == classValue) {
				if (instanceSet.instances[i].data[counterIndex] > limit) {
					instanceSet.instances[i].data[counterIndex] = limit;
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

