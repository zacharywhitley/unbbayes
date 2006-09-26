package unbbayes.datamining.distance;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 12/09/2006
 */
public class HVDM {
	
	private InstanceSet instanceSet;
	private float norm[];
	private int numAttributes;
	
	public HVDM(InstanceSet instanceSet, int normFactor) {
		this.instanceSet = instanceSet;
		numAttributes = instanceSet.numAttributes();
		norm = computeNormFactor(normFactor);
	}
	
	private float[] computeNormFactor(int normFactor) {
		int numInstances = instanceSet.numInstances();
		
		for (int i = 0; i < numInstances; )


		return null;
	}

	/**
	 * 
	 * @param vector1
	 * @param vector2
	 * @param instanceSet
	 * @param vdmType
	 * 0 - 
	 * 1 -
	 * @return
	 */
	public float distanceValue(Instance instance1, Instance instance2) {
		float[] vector1 = new float[numAttributes];
		float[] vector2 = new float[numAttributes];
		
		for (int att = 0; att < numAttributes; att++) {
			if (instanceSet.getAttribute(att).isNominal()) {
				/* The attribute is nominal. Get the internal value */
				vector1[att] = instance1.getValue(att);
				vector2[att] = instance2.getValue(att);
			} else {
				/* The attribute is numeric. Get the real value */
				vector1[att] = instance1.floatValue(att);
				vector2[att] = instance2.floatValue(att);
			}
			
		}
		
		return distanceValue(vector1, vector2, instanceSet);
	}

	public float distanceValue(float[] vector1, float[] vector2, 
			InstanceSet instanceSet) {
		int numAttributes = instanceSet.numAttributes();
		float distance = 0;
		
		for (int att = 0; att < numAttributes; att++) {
			if (instanceSet.getAttribute(att).isNominal()) {
				/* The attribute is nominal. Apply VDM distance */
				distance = ;
			} else {
				/* The attribute is numeric. Apply Euclidian distance */
				distance = distance + (vector1[att] - vector[att]) / norm[att];
			}
			
		}
		return 0;
	}

}

