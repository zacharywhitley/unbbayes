package unbbayes.datamining.preprocessor.imbalanceddataset;

import java.util.Arrays;

import unbbayes.TestsetUtils;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 09/11/2006
 */
public class ClusterBasedUndersampling extends ClusterBasedUtils {
	
	public ClusterBasedUndersampling(InstanceSet instanceSet, int positiveClass,
			int k) {
		super(instanceSet, positiveClass, k);
	}
	
	public void run(InstanceSet instanceSet, float positiveRate,
			boolean doSmote, boolean clean, boolean under, boolean over)
	throws Exception {
		setInstanceSet(instanceSet);

		/* Clusterize */
		clusterize();
		
		/* Assign the class to each cluster according to the positiveRate*/
		assignClassCluster(positiveRate);
		
		/* Check proportion */
		float[] dist = TestsetUtils.distribution(instanceSet);
		
		/* Set no instance to be removed */
		Arrays.fill(deleteIndex, false);

		float downWeight = 0;
		
		/* UnderSample majority class */
		if (under) {
			downWeight = clean(negativeClass);
			dist[negativeClass] -= downWeight;
		}
		
		/* 
		 * Clean negative class (remove positive instances from negative
		 * clusters)
		 */
		if (clean) {
			/* 
			 * We don´t want to remove to much positive instances. Thus,
			 * we decrease the positiveRate.
			 */
			assignClassCluster(positiveRate / 3);
			
			downWeight += clean(positiveClass);
			dist[positiveClass] -= downWeight;
		}
		
		/* Oversample */
		if (over) {
			float proportion = dist[negativeClass] / dist[positiveClass];
			if (proportion > 1) {
				overSample(proportion, doSmote);
			}
		}
		
		/* Remove those negative instances marked for removal */
		instanceSet.removeInstances(deleteIndex);
	}

	private void assignClassCluster(float positiveRate) throws Exception {
		/* Assign a class to each cluster according to its distribution */
		clusterClass = new int[numClusters[0]];
		int classValue;
		for (int clusterID = 0; clusterID < numClusters[0]; clusterID++) {
			classValue = assignClassCluster(clusterID, positiveRate);
			clusterClass[clusterID] = classValue;
		}
	}
	
	private void overSample(float proportion, boolean doSmote)
	throws Exception {
		for (int clusterID = 0; clusterID < numClusters[0]; clusterID++) {
			/* Oversample only the positive class */
			if (clusterClass[clusterID] == positiveClass) {
				if (doSmote) {
					smoteCluster(clusterID, proportion);
				} else {
					oversampleCluster(clusterID, proportion, false);
				}
			}
		}
	}

	private float clean(int classValue) {
		int clusterClassDesired;
		int instanceClassDesired;
		if (classValue == negativeClass) {
			/* Remove negative instances */
			clusterClassDesired = positiveClass;
			instanceClassDesired = negativeClass;
		} else {
			/* Remove positive instances */
			clusterClassDesired = negativeClass;
			instanceClassDesired = positiveClass;
		}
		float downWeight = 0;
		int clusterLength;
		int inst;
		Instance instance;
		for (int clusterID = 0; clusterID < numClusters[0]; clusterID++) {
			if (clusterClass[clusterID] == clusterClassDesired) {
				clusterLength = clusters[0][clusterID].length;
				for (int i = 0; i < clusterLength; i++) {
					inst = clusters[0][clusterID][i];
					instance = instanceSet.getInstance(inst);
					if (instance.classValue() == instanceClassDesired) {
						deleteIndex[inst] = true;
						downWeight += instance.getWeight();
					}
				}
			}
		}
		
		return downWeight;
	}

	private int assignClassCluster(int clusterID, double positiveRate) {
		int classValue;
		double numPositives = 0;
		
		Instance instance;
		int inst;
		int clusterLength = clusters[0][clusterID].length;
		for (int i = 0; i < clusterLength; i++) {
			inst = clusters[0][clusterID][i];
			instance = instanceSet.getInstance(inst);
			classValue = instance.classValue();
			if (classValue == positiveClass) {
				numPositives += instance.getWeight();
			}
		}
		float clusterPositiveRate;
		double clusterSize = clustersSize[0][clusterID];
		clusterPositiveRate = (float) (numPositives / clusterSize);
		
		/* Check if the cluster should be taken as a positive cluster */
		if (clusterPositiveRate > positiveRate) {
			return positiveClass;
		} else {
			return negativeClass;
		}
	}

}