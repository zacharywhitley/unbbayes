package unbbayes.datamining.preprocessor.imbalanceddataset;

import java.util.ArrayList;

import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 26/08/2007
 */
public class Utils {

	public static void removeMarkedInstances(InstanceSet instanceSet,
			boolean[] deleteIndex) {
		/* Remove those negative instances marked for removal */
		instanceSet.removeInstances(deleteIndex);
	}

	public static int[] uncompactInstancesIDs(int[] instancesIDs,
			InstanceSet instanceSet) {
		int size = instancesIDs.length;
		ArrayList<Integer> aux = new ArrayList<Integer>();
		int inst;
		int weight;
		
		for (int i = 0; i < size; i++) {
			inst = instancesIDs[i];
			weight = (int) instanceSet.instances[inst].getWeight();
			for (int j = 0; j < weight; j++) {
				aux.add(inst);
			}
		}
		
		size = aux.size();
		int[] newInstancesIDs = new int[size];
		for (int i = 0; i < size; i++) {
			newInstancesIDs[i] = aux.get(i);
		}
		
		return newInstancesIDs;
	}

}

