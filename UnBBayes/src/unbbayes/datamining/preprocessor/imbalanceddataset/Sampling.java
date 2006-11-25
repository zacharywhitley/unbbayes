package unbbayes.datamining.preprocessor.imbalanceddataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

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
	
	/**
	 * Samples an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. If the parameter <code>remove </code> is true
	 * then those instances with the counter attribute set to 0 by the sampling
	 * process will be removed and the new valid array of instances <code>
	 * instancesIDs</code> will be returned. If the parameter <code>remove
	 * </code> is set to false then no instance will be removed.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @param remove
	 * @return
	 */
	public static int[] simpleSampling(InstanceSet instanceSet,
			double proportion, boolean remove) {
		int numInstances = instanceSet.numInstances;

		/* Choose all instances for the sampling process */
		int[] instancesIDs = new int[numInstances];
		for (int i = 0; i < numInstances; i++) {
			instancesIDs[i] = i;
		}
		return simpleSampling(instanceSet, proportion, instancesIDs, remove);
	}
	
	/**
	 * Samples an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. If the parameter <code>remove </code> is true
	 * then those instances with the counter attribute set to 0 by the sampling
	 * process will be removed and the new valid array of instances <code>
	 * instancesIDs</code> will be returned. If the parameter <code>remove
	 * </code> is set to false then no instance will be removed. Only instances
	 * of the class <code>classValue</code> will be sampled.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @param classValue
	 * @param remove
	 * @return
	 */
	public static int[] simpleSampling(InstanceSet instanceSet,
			double proportion, int classValue, boolean remove) {
		Instance[] instances = instanceSet.instances;
		int numInstances = instanceSet.numInstances;

		/* Choose the instancesIDs for the sampling process */
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		int classIndex = instanceSet.classIndex;
		for (int inst = 0; inst < numInstances; inst++) {
			if (instances[inst].data[classIndex] == classValue) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int[] instancesIDs = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		return simpleSampling(instanceSet, proportion, instancesIDs, remove);
	}
	
	/**
	 * Samples an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. Only those instances indicated by the array
	 * <code>instancesIDs</code> will be sampled. If the parameter <code>remove
	 * </code> is true then those instances with the counter attribute set to 0
	 * by the sampling process will be removed and the new valid array of
	 * instances <code>instancesIDs</code> will be returned. If the parameter
	 * <code>remove</code> is set to false then no instance will be removed and
	 * the input <code>instancesIDs</code> will be returned.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @param instancesIDs Must be sorted ascending!!!
	 * @param remove
	 * @return
	 */
	public static int[] simpleSampling(InstanceSet instanceSet, double proportion,
			int[] instancesIDs, boolean optionRemove) {
		int counterIndex = instanceSet.counterIndex;
		int numInstances = instancesIDs.length;
		double weight;
		float newWeight;
		int inst = 0;

		/* Array that tells which instances must be removed (weight < 1) */ 
		boolean[] deleteIndex = new boolean[numInstances];
		Arrays.fill(deleteIndex, false);
		
		/* Randomly sample the instanceIDs */
		int deleteCounter = 0;
		for (int i = 0; i < numInstances; i++) {
			inst = instancesIDs[i];
			weight = instanceSet.instances[inst].data[counterIndex];
			newWeight = Math.round(weight * proportion);
			if (newWeight > 0) {
				instanceSet.instances[inst].data[counterIndex] = newWeight;
				instanceSet.numWeightedInstances += (newWeight - weight);
			} else {
				deleteIndex[i] = true;
				++deleteCounter;
			}
		}
		
		/* If there are instances to be removed */
		if (optionRemove && deleteCounter > 0) {
			/* Remove instances marked to be removed */
			instanceSet.removeInstances(deleteIndex);

			/* Return null if all instances have been removed */
			if (deleteCounter == numInstances) {
				return null;
			}
			
			/* Create new array with the valid instancesIDs */
			int[] newInstancesIDs = new int[numInstances - deleteCounter];
			inst = 0;
			for (int i = 0; i < numInstances; i++) {
				if (!deleteIndex[i]) {
					newInstancesIDs[inst] = instancesIDs[i];
					++inst;
				}
			}
			return newInstancesIDs;
		}
		
		/* Return the input instancesIDs if no instance has been removed */
		return instancesIDs;
	}

	/**
	 * Samples an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. If the parameter <code>remove </code> is true
	 * then those instances with the counter attribute set to 0 by the sampling
	 * process will be removed and the new valid array of instances <code>
	 * instancesIDs</code> will be returned.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @return
	 */
	public static void oversampling(InstanceSet instanceSet, double proportion) {
		int numInstances = instanceSet.numInstances;

		/* Choose all instances for the sampling process */
		int[] instancesIDs = new int[numInstances];
		for (int i = 0; i < numInstances; i++) {
			instancesIDs[i] = i;
		}
		oversampling(instanceSet, proportion, instancesIDs);
	}
	
	/**
	 * Samples an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. If the parameter <code>remove </code> is true
	 * then those instances with the counter attribute set to 0 by the sampling
	 * process will be removed and the new valid array of instances <code>
	 * instancesIDs</code> will be returned.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @param classValue
	 * @return
	 */
	public static void oversampling(InstanceSet instanceSet,
			double proportion, int classValue) {
		Instance[] instances = instanceSet.instances;
		int numInstances = instanceSet.numInstances;

		/* Choose the instancesIDs for the sampling process */
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		int classIndex = instanceSet.classIndex;
		for (int inst = 0; inst < numInstances; inst++) {
			if (instances[inst].data[classIndex] == classValue) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int[] instancesIDs = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		oversampling(instanceSet, proportion, instancesIDs);
	}
	
	/**
	 * Samples an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. Only those instances indicated by the array
	 * <code>instancesIDs</code> will be sampled. If the parameter <code>remove
	 * </code> is true then those instances with the counter attribute set to 0
	 * by the sampling process will be removed and the new valid array of
	 * instances <code>instancesIDs</code> will be returned.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @param instancesIDs Must be sorted ascending!!!
	 * @return
	 */
	public static void oversampling(InstanceSet instanceSet, double proportion,
			int[] instancesIDs) {
		int counterIndex = instanceSet.counterIndex;
		int numInstances = instancesIDs.length;
		int inst = 0;

		/* Get current size */
		int currentSize = 0;
		for (int i = 0; i < numInstances; i++) {
			inst = instancesIDs[i];
			currentSize += instanceSet.instances[inst].data[counterIndex];
		}
		
		/* Get increase size */
		int increaseSize;
		increaseSize = (int) (proportion - 1) * currentSize;
		
		/* Randomly oversample */
		Random randomizer = new Random();
		for (int i = 0; i < increaseSize; i++) {
			inst = instancesIDs[randomizer.nextInt(numInstances)];
			++instanceSet.instances[inst].data[counterIndex];
		}
	}

	/**
	 * Samples an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. If the parameter <code>remove </code> is true
	 * then those instances with the counter attribute set to 0 by the sampling
	 * process will be removed and the new valid array of instances <code>
	 * instancesIDs</code> will be returned.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @return
	 */
	public static int[] undersampling(InstanceSet instanceSet,
			double proportion, boolean remove) {
		int numInstances = instanceSet.numInstances;

		/* Choose all instances for the sampling process */
		int[] instancesIDs = new int[numInstances];
		for (int i = 0; i < numInstances; i++) {
			instancesIDs[i] = i;
		}
		return undersampling(instanceSet, proportion, instancesIDs, remove);
	}
	
	/**
	 * Samples an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. If the parameter <code>remove </code> is true
	 * then those instances with the counter attribute set to 0 by the sampling
	 * process will be removed and the new valid array of instances <code>
	 * instancesIDs</code> will be returned.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @param classValue
	 * @return
	 */
	public static int[] undersampling(InstanceSet instanceSet,
			double proportion, int classValue, boolean remove) {
		Instance[] instances = instanceSet.instances;
		int numInstances = instanceSet.numInstances;

		/* Choose the instancesIDs for the sampling process */
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		int classIndex = instanceSet.classIndex;
		for (int inst = 0; inst < numInstances; inst++) {
			if (instances[inst].data[classIndex] == classValue) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int[] instancesIDs = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		return undersampling(instanceSet, proportion, instancesIDs, remove);
	}
	
	/**
	 * Samples an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. Only those instances indicated by the array
	 * <code>instancesIDs</code> will be sampled. If the parameter <code>remove
	 * </code> is true then those instances with the counter attribute set to 0
	 * by the sampling process will be removed and the new valid array of
	 * instances <code>instancesIDs</code> will be returned.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @param instancesIDs Must be sorted ascending!!!
	 * @return
	 */
	public static int[] undersampling(InstanceSet instanceSet, double proportion,
			int[] instancesIDs, boolean optionRemove) {
		int counterIndex = instanceSet.counterIndex;
		int numInstances = instancesIDs.length;
		int inst = 0;

		/* Get current instanceSet size */
		int currentSize = 0;
		for (int i = 0; i < numInstances; i++) {
			inst = instancesIDs[i];
			currentSize += instanceSet.instances[inst].data[counterIndex];
		}
		
		/* Get decreasing size */
		int decreaseSize;
		decreaseSize = Math.round((float) (1 - proportion) * currentSize);
		
		/* Array that tells which instances must be removed (weight < 1) */ 
		boolean[] deleteIndex = new boolean[numInstances];
		Arrays.fill(deleteIndex, false);
		
		/* Randomly undersample */
		Random randomizer = new Random();
		int deleteCounter = 0;
		while (deleteCounter < decreaseSize) {
			inst = instancesIDs[randomizer.nextInt(numInstances)];
			if (!deleteIndex[inst]) {
				--instanceSet.instances[inst].data[counterIndex];
				if (instanceSet.instances[inst].data[counterIndex] < 1) {
					deleteIndex[inst] = true;
					continue;
				}
				++deleteCounter;
			}
		}

		/* If there are instances to be removed */
		if (optionRemove && deleteCounter > 0) {
			/* Remove instances marked to be removed */
			instanceSet.removeInstances(deleteIndex);

			/* Return null if all instances have been removed */
			if (deleteCounter == numInstances) {
				return null;
			}
			
			/* Create new array with the valid instancesIDs */
			int[] newInstancesIDs = new int[numInstances - deleteCounter];
			inst = 0;
			for (int i = 0; i < numInstances; i++) {
				if (!deleteIndex[i]) {
					newInstancesIDs[inst] = instancesIDs[i];
					++inst;
				}
			}
			return newInstancesIDs;
		}
		
		/* Return the input instancesIDs if no instance has been removed */
		return instancesIDs;
	}

	public static void limitWeight(InstanceSet instanceSet, int limit,
			int classValue) {
		int classIndex = instanceSet.classIndex;
		int counterIndex = instanceSet.counterIndex;
		int numInstances= instanceSet.numInstances();
		double difference;

		numInstances = instanceSet.numInstances();
		for (int i = 0; i < numInstances; i++) {
			if (instanceSet.instances[i].data[classIndex] == classValue) {
				difference = instanceSet.instances[i].data[counterIndex] - limit;
				if (difference > 0) {
					instanceSet.numWeightedInstances -= difference;
					instanceSet.instances[i].data[counterIndex] = limit;
				}
			}
		}
	}
	
}