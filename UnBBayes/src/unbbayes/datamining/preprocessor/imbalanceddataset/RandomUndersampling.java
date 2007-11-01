package unbbayes.datamining.preprocessor.imbalanceddataset;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.evaluation.batchEvaluation.PreprocessorParameters;

/**
 * This class implements random undersampling and oversampling algorithms.
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 26/08/2006
 */
public class RandomUndersampling extends Batch {

	private static boolean useRatio = true;
	private static boolean useK = false;
	private static boolean useOverThresh = false;
	private static boolean usePosThresh = false;
	private static boolean useNegThresh = false;
	private static boolean useCleaning = false;
	
	public RandomUndersampling(InstanceSet instanceSet) {
		this(instanceSet, null);
	}

	public RandomUndersampling(InstanceSet instanceSet,
			PreprocessorParameters parameters) {
		super(useRatio, useK, useOverThresh, usePosThresh, useNegThresh,
				useCleaning, instanceSet, parameters);
		preprocessorName = "Oversampling";
	}

	private boolean remove;
	private boolean[] deleteIndex;
	
	/**
	 * InitializePreprocessors an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. Should any instance be removed, a new valid array
	 * of instances <code>instancesIDs</code> will be returned.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @return
	 */
	public void runAll() {
		int numInstances = instanceSet.numInstances;

		/* Choose all instances for the sampling process */
		int[] instancesIDs = new int[numInstances];
		for (int i = 0; i < numInstances; i++) {
			instancesIDs[i] = i;
		}
		run(instancesIDs);
	}
	
	/**
	 * InitializePreprocessors an instanceSet. The amount of increase or decrease is controlled
	 * by the <code>proportion</code> parameter, which will be multiplied by
	 * the counter attribute. Should any instance be removed, a new valid array
	 * of instances <code>instancesIDs</code> will be returned.
	 * 
	 * @param instanceSet
	 * @param proportion
	 * @param interestingClass
	 * @return
	 */
	protected void run() {
		Instance[] instances = instanceSet.instances;
		int numInstances = instanceSet.numInstances;

		/* Choose the instancesIDs for the sampling process */
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		int classIndex = instanceSet.classIndex;
		for (int inst = 0; inst < numInstances; inst++) {
			if (instances[inst].data[classIndex] == interestingClass) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int[] instancesIDs = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		
		run(instancesIDs);
	}
	
	/**
	 * InitializePreprocessors an instanceSet. The amount of increase or decrease is controlled
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
	public void run(int[] instancesIDs) {
		if (instanceSet.isCompacted()) {
			instancesIDs = Utils.uncompactInstancesIDs(instancesIDs, instanceSet);
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
	
	public void setInstanceSet(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
	}

	public void setRemove(boolean remove) {
		this.remove = remove;
	}

	public void setDeleteIndex(boolean[] deleteIndex) {
		this.deleteIndex = deleteIndex;
	}

	@Override
	protected void initializeBatch(InstanceSet instanceSet) {
		setRemove(true);
		setInstanceSet(instanceSet);
		setInterestingClass(positiveClass);
	}

}