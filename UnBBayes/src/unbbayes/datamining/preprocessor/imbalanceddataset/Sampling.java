package unbbayes.datamining.preprocessor.imbalanceddataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 * This class implements random undersampling and oversampling algorithms.
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 26/08/2006
 */
public class Sampling {

	/*--------------------- oversampling - start ----------------------*/
	
	/**
	 * Samplings an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute.
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
	 * Samplings an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute.
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
	 * Samplings an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. Only those instances indicated by the array
	 * <code>instancesIDs</code> will be sampled.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @param instancesIDs Must be sorted ascending!!!
	 * @return
	 */
	public static void oversampling(InstanceSet instanceSet, double proportion,
			int[] instancesIDs) {
		if (instanceSet.numInstances < 1 || instancesIDs.length < 1) {
			return;
		}
		
		if (instanceSet.isCompacted()) {
			instancesIDs = uncompactInstancesIDs(instancesIDs, instanceSet);
		}
		
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
		increaseSize = (int) ((proportion - 1) * currentSize) + 1;
		
		/* Randomly oversample */
		Random randomizer = new Random();
		for (int i = 0; i < increaseSize; i++) {
			inst = instancesIDs[randomizer.nextInt(numInstances)];
			++instanceSet.instances[inst].data[counterIndex];
			++instanceSet.numWeightedInstances;
		}
		instanceSet.getClassDistribution(true);
	}

	/*--------------------- oversampling - end ----------------------*/

	
	/*--------------------- undersampling - start ----------------------*/

	/**
	 * Samplings an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. Should any instance be removed, a new valid array
	 * of instances <code>instancesIDs</code> will be returned.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @return
	 */
	public static void undersampling(InstanceSet instanceSet,
			double proportion, boolean remove, boolean[] deleteIndex) {
		int numInstances = instanceSet.numInstances;

		/* Choose all instances for the sampling process */
		int[] instancesIDs = new int[numInstances];
		for (int i = 0; i < numInstances; i++) {
			instancesIDs[i] = i;
		}
		undersampling(instanceSet, proportion, instancesIDs, remove,
				deleteIndex);
	}
	
	/**
	 * Samplings an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @param classValue
	 * @return
	 */
	public static void undersampling(InstanceSet instanceSet,
			double proportion, int classValue, boolean remove) {
		undersampling(instanceSet, proportion, classValue, remove, null);
	}
	
	/**
	 * Samplings an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. Should any instance be removed, a new valid array
	 * of instances <code>instancesIDs</code> will be returned.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @param classValue
	 * @return
	 */
	public static void undersampling(InstanceSet instanceSet,
			double proportion, int classValue, boolean remove,
			boolean[] deleteIndex) {
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
		
		undersampling(instanceSet, proportion, instancesIDs, remove,
				deleteIndex);
	}
	
	/**
	 * Samplings an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. Only those instances indicated by the array
	 * <code>instancesIDs</code> will be sampled. Should any instance be
	 * removed, a new valid array of instances <code>instancesIDs</code> will
	 * be returned.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @param instancesIDs Must be sorted ascending!!!
	 * @return
	 */
	public static void undersampling(InstanceSet instanceSet, double proportion,
			int[] instancesIDs, boolean remove, boolean[] deleteIndex) {
		if (instanceSet.isCompacted()) {
			instancesIDs = uncompactInstancesIDs(instancesIDs, instanceSet);
		}
		
		int counterIndex = instanceSet.counterIndex;
		int numInstancesIDs = instancesIDs.length;
		int inst = 0;

		/* Get current instanceSet size */
		int currentSize = 0;
		for (int i = 0; i < numInstancesIDs; i++) {
			inst = instancesIDs[i];
			currentSize += instanceSet.instances[inst].data[counterIndex];
		}
		
		/* Get decreasing size */
		int decreaseSize;
		decreaseSize = Math.round((float) (1 - proportion) * currentSize);
		
		/* Array that tells which instances must be removed (weight < 1) */ 
		if (deleteIndex == null) {
			deleteIndex = new boolean[instanceSet.numInstances];
			Arrays.fill(deleteIndex, false);
		}
		
		/* Randomly undersample */
		Random randomizer = new Random(new Date().getTime());
		int deleteCounter = 0;
		int instIDs;
		int lastID;
		while (deleteCounter < decreaseSize) {
			instIDs = randomizer.nextInt(numInstancesIDs);
			inst = instancesIDs[instIDs];
			
			if (deleteIndex[inst]) {
				@SuppressWarnings("unused")
				int x = 0;
			}
			/* Decrease from the total weight of the instanceSet */
			--instanceSet.numWeightedInstances;
			
			/* Decrease the weight of the chosen instance */
			--instanceSet.instances[inst].data[counterIndex];
			
			/* Check if removal of the chosen instance is needed */
			if (instanceSet.instances[inst].data[counterIndex] <= 0) {
				deleteIndex[inst] = true;
				lastID = instancesIDs[numInstancesIDs - 1];
				instancesIDs[instIDs] = lastID;
				--numInstancesIDs;
			}
			++deleteCounter;
		}

		/* If there are instances to be removed */
		if (deleteCounter > 0 && remove) {
			/* Remove instances marked to be removed */
			instanceSet.removeInstances(deleteIndex);
		}
	}
	
	/*--------------------- undersampling - end ----------------------*/
	
	
	/*--------------------- simplesampling - start ----------------------*/

	/**
	 * Samplings an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute.	 * 
	 * @param instanceSet
	 * @param proportion
	 * @return
	 */
	public static void simplesampling(InstanceSet instanceSet,
			double proportion, boolean remove) {
		int numInstances = instanceSet.numInstances;

		/* Choose all instances for the sampling process */
		int[] instancesIDs = new int[numInstances];
		for (int i = 0; i < numInstances; i++) {
			instancesIDs[i] = i;
		}
		
		simplesampling(instanceSet, proportion, instancesIDs, remove);
	}
	
	/**
	 * Samplings an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. Only those instances from the class <code>
	 * classValue</code> will be sampled.
	 *  
	 * @param instanceSet
	 * @param proportion
	 * @param classValue
	 * @return
	 */
	public static void simplesampling(InstanceSet instanceSet,
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
		
		simplesampling(instanceSet, proportion, instancesIDs, remove);
	}
	
	/**
	 * Samplings an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. Only those instances indicated by the array
	 * <code>instancesIDs</code> will be sampled.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @param instancesIDs Must be sorted ascending!!!
	 * @return
	 */
	public static void simplesampling(InstanceSet instanceSet, double proportion,
			int[] instancesIDs, boolean remove) {
		int counterIndex = instanceSet.counterIndex;
		int numInstancesIDs = instancesIDs.length;
		int inst = 0;

		/* Array that tells which instances must be removed (weight < 1) */ 
		boolean[] deleteIndex = new boolean[instanceSet.numInstances];
		Arrays.fill(deleteIndex, false);
		
		boolean delete = false;
		int weight;
		int newWeight;
		int difference;
		for (int i = 0; i < numInstancesIDs; i++) {
			inst = instancesIDs[i];
			
			weight = (int) instanceSet.instances[inst].data[counterIndex];
			newWeight = Math.round((float) proportion * weight);
			difference = newWeight - weight;
			
			/* Increase/decrease from the total weight of the instanceSet */
			instanceSet.numWeightedInstances += difference;
			
			/* Increase/decrease the weight of the chosen instance */
			instanceSet.instances[inst].data[counterIndex] = newWeight;
			
			/* Check if removal of the chosen instance is needed */
			if (newWeight <= 0) {
				deleteIndex[inst] = true;
				delete = true;
			}
		}

		/* If there are instances to be removed */
		if (delete && remove) {
			/* Remove instances marked to be removed */
			instanceSet.removeInstances(deleteIndex);
		}
	}

	/*--------------------- simpleSampling - end ----------------------*/

	
	
	public static void limitWeight(InstanceSet instanceSet, int limit) {
		limitWeight(instanceSet, limit, -1);
	}
	
	public static void limitWeight(InstanceSet instanceSet, int limit,
			int classValue) {
		int classIndex = instanceSet.classIndex;
		int counterIndex = instanceSet.counterIndex;
		int numInstances= instanceSet.numInstances();
		double difference;

		numInstances = instanceSet.numInstances();
		for (int i = 0; i < numInstances; i++) {
			if (instanceSet.instances[i].data[classIndex] == classValue
					|| classValue == -1) {
				difference = instanceSet.instances[i].data[counterIndex] - limit;
				if (difference > 0) {
					instanceSet.numWeightedInstances -= difference;
					instanceSet.instances[i].data[counterIndex] = limit;
				}
			}
		}
	}

	private static int[] uncompactInstancesIDs(int[] instancesIDs,
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