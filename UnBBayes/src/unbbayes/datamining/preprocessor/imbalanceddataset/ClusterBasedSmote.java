package unbbayes.datamining.preprocessor.imbalanceddataset;

import java.util.Arrays;

import unbbayes.datamining.clustering.Clustering;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 09/11/2006
 */
public class ClusterBasedSmote extends ClusterBasedUtils {
	/**********************************************/
	/** Options for SMOTE - END *******************/
	/**********************************************/
	
	public ClusterBasedSmote(InstanceSet instanceSet, int positiveClass, int k) {
		super(instanceSet, positiveClass, k);
	}
	
	public void run(InstanceSet instanceSet, boolean overMajority, boolean cbo)
	throws Exception {
		setInstanceSet(instanceSet);

		/* Clusterize classes */
		clusterizeByClass();
		
		int numClustersAux = numClusters[negativeClass];
		double finalSize;

		if (overMajority) {
			/* Count the majority class */
			int majorityClassQtd = 0;
			for (int inst = 0; inst < numInstances; inst++) {
				if (instances[inst].data[classIndex] == negativeClass) {
					majorityClassQtd += instances[inst].data[counterIndex];
				}
			}
			
			/* Pick the biggest cluster of the majority class */
			int biggestClusterIndex = 0;
			double biggestClusterSize = 0;
			for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
				if (clustersSize[negativeClass][clusterID] > biggestClusterSize) {
					biggestClusterSize = clustersSize[negativeClass][clusterID];
					biggestClusterIndex = clusterID;
				}
			}
			
			/* 
			 * Oversample the clusters of the majority class to the same size of
			 * its biggest cluster.
			 */
			double[] count = clustersSize[negativeClass];
			numClustersAux = numClusters[negativeClass];
			for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
				if (clusterID != biggestClusterIndex) {
					finalSize = biggestClusterSize / count[clusterID];
					if (cbo) {
						oversampleCluster(clusterID, finalSize, negativeClass,
								false);
					} else {
						smoteCluster(clusterID, finalSize, negativeClass);
					}
				}
			}
		}

		/* Reset the variables */
		instances = instanceSet.instances;
		
		/* Compute the new size of the majority class */
		double majorityClassSize = 0;
		for (int inst = 0; inst < instanceSet.numInstances; inst++) {
			if (instances[inst].data[classIndex] == negativeClass) {
				majorityClassSize += instances[inst].data[counterIndex];
			}
		}
		
		/* Now we oversample the clusters of the other classes one by one */
		double newSizePerCluster;
		for (int classValue = 0; classValue < numClasses; classValue++) {
			if (classValue != negativeClass) {
				numClustersAux = numClusters[classValue];
				newSizePerCluster = majorityClassSize / numClustersAux;
				for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
					finalSize = newSizePerCluster;
					finalSize /= clustersSize[classValue][clusterID];
					if (finalSize > 1) {
						if (cbo) {
							oversampleCluster(clusterID, finalSize, classValue,
									false);
						} else {
							smoteCluster(clusterID, finalSize, classValue);
						}
					}
				}
			}
		}
	}
	
	public void runUndersampling(InstanceSet instanceSet, double proportion,
			boolean simplesampling)
	throws Exception {
		setInstanceSet(instanceSet);
		
		/* Clusterize classe */
		clusterize(negativeClass);
		
		/* Undersample the clusters of the majority class */
		int numClustersAux = numClusters[negativeClass];
		for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
			undersampleCluster(clusterID, proportion, negativeClass,
					simplesampling);
		}
		
		/* Array that tells which instances must be removed (weight < 1) */ 
		boolean[] deleteIndex = new boolean[instanceSet.numInstances];
		Arrays.fill(deleteIndex, false);
		
		/* Mark instances with weight < 1 for deletion */
		float weight;
		for (int inst = 0; inst < numInstances; inst++) {
			weight = instanceSet.instances[inst].data[counterIndex];
			if (weight < 1) {
				deleteIndex[inst] = true;
			}
		}
		
		/* Remove from instanceSet those instances marked for deletion */
		instanceSet.removeInstances(deleteIndex);
		
		/* Update references to instanceSet */
		instances = instanceSet.instances;
		numInstances = instanceSet.numInstances;
	}
	
	public void runOversampling(InstanceSet instanceSet, double proportion,
			boolean doSmote)
	throws Exception {
		setInstanceSet(instanceSet);
		
		/* Clusterize positive class */
		clusterize(positiveClass);
		
		int numClustersAux = numClusters[positiveClass];

		for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
			if (doSmote) {
				/* Smote the minority class */
				smoteCluster(clusterID, proportion, positiveClass);
			} else {
				/* Oversample the minority class */
				oversampleCluster(clusterID, proportion, positiveClass, false);
			}
		}
	}
	
}