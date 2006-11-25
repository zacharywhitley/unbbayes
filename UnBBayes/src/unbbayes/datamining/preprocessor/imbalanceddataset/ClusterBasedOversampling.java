package unbbayes.datamining.preprocessor.imbalanceddataset;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 09/11/2006
 */
public class ClusterBasedOversampling {
	/** The current instanceSet */
	private InstanceSet instanceSet;

	/** The current set of input instances */
	private Instance[] instances;

	/** Number of instances in the instance set */
	private int numInstances;

	/** Number of classes in the instance set */
	private int numClasses;

	/** 
	 * The index of the dataset's column that represents the counter attribute.
	 */
	private int counterIndex;

	/** 
	 * The index of the dataset's column that represents the class attribute.
	 * Assume the value -1 in case there is no class attribute.
	 */
	private int classIndex;

	/** Matrix of all clusters. Each row stores all instancesIDs of a cluster */
	private int[][][] clusters;
	
	/** Number of clusters created */
	private int[] numClusters;
	
	/** Weighted number of instances in each cluster created */
	private double[][] clustersSize;

	/**
	 * Data assignment to clusters. Each position corresponds to the cluster id
	 * of an instance in the same order it appears in the input instanceSet.
	 */
	private int[][] assignmentMatrix;

	public ClusterBasedOversampling(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
		instances = instanceSet.instances;
		numInstances = instanceSet.numInstances;
		counterIndex = instanceSet.counterIndex;
		classIndex = instanceSet.classIndex;
		numClasses = instanceSet.getAttribute(classIndex).numValues();
	}
	
	public void run(int[][][] clusters, int[] numClusters,
			double[][] clustersSize, int[][] assignmentMatrix) {
		this.clusters = clusters;
		this.numClusters = numClusters;
		this.clustersSize = clustersSize;
		this.assignmentMatrix = assignmentMatrix;
		
		/* Discover the majority class */
		double[] count = new double[numClasses];
		for (int classValue, inst = 0; inst < numInstances; inst++) {
			classValue = (int) instances[inst].data[classIndex];
			count[classValue] += instances[inst].data[counterIndex];
		}
		double majorityClassQtd = 0;
		int majorityClass = 0;
		for (int c = 0; c < numClasses; c++) {
			if (count[c] > majorityClassQtd) {
				majorityClassQtd = count[c];
				majorityClass = c;
			}
		}
		
		/* Pick the biggest cluster of the majority class */
		int biggestClusterIndex = 0;
		double biggestClusterSize = 0;
		int numClustersAux = numClusters[majorityClass];
		for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
			if (clustersSize[majorityClass][clusterID] > biggestClusterSize) {
				biggestClusterSize = clustersSize[majorityClass][clusterID];
				biggestClusterIndex = clusterID;
			}
		}
		
		/* 
		 * Oversample the clusters of the majority class to the same size of
		 * its biggest cluster.
		 */
		double finalSize;
		count = clustersSize[majorityClass];
		numClustersAux = numClusters[majorityClass];
		for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
			if (clusterID != biggestClusterIndex) {
				finalSize = biggestClusterSize / count[clusterID];
				oversampleCluster(clusterID, finalSize, majorityClass);
			}
		}
		
		/* Compute the new size of the majority class */
		double majorityClassSize = 0;
		for (int inst = 0; inst < numInstances; inst++) {
			if (instances[inst].data[classIndex] == majorityClass) {
				majorityClassSize += instances[inst].data[counterIndex];
			}
		}
		
		/* Now we oversample the clusters of the other classes one by one */
		double newSizePerCluster;
		for (int classValue = 0; classValue < numClasses; classValue++) {
			if (classValue != majorityClass) {
				numClustersAux = numClusters[classValue];
				newSizePerCluster = majorityClassSize / numClustersAux;
				for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
					finalSize = newSizePerCluster;
					finalSize /= clustersSize[classValue][clusterID];
					oversampleCluster(clusterID, finalSize, classValue);
				}
			}
		}
		clustersSize = new double[numClasses][];
		for (int classValue = 0; classValue < numClasses; classValue++) {
			clustersSize[classValue] = new double[numClusters[classValue]];
			double clusterSize;
			int inst;
			int clusterQtd;
			numClustersAux = numClusters[classValue];
			for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
				clusterQtd = clusters[classValue][clusterID].length;
				clusterSize = 0;
				for (int i = 0; i < clusterQtd; i++) {
					inst = clusters[classValue][clusterID][i];
					clusterSize += instances[inst].data[counterIndex];
				}
				clustersSize[classValue][clusterID] = clusterSize;
			}
		}
		int ema = 0;
	}
	
	private void oversampleCluster(int clusterIndex, double proportion,
			int classValue) {
		/* Choose the instancesIDs for the sampling process */
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		for (int inst = 0; inst < numInstances; inst++) {
			if (assignmentMatrix[classValue][inst] == clusterIndex) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int[] instancesIDs = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		Sampling.oversampling(instanceSet, proportion, instancesIDs);
	}

}