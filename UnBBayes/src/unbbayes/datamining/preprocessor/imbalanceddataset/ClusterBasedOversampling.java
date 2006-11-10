package unbbayes.datamining.preprocessor.imbalanceddataset;

import unbbayes.datamining.clustering.CEBMDC;
import unbbayes.datamining.clustering.Kmeans;
import unbbayes.datamining.clustering.Squeezer;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.distance.Euclidean;
import unbbayes.datamining.distance.IDistance;

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

	/** Similarity threshold for the CEBMDC algorithm */
	private float s;

	/** Similarity threshold for the Squeezer algorithm */
	private float sSqueezer;

	public ClusterBasedOversampling(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
		instances = instanceSet.instances;
		numInstances = instanceSet.numInstances;
		counterIndex = instanceSet.counterIndex;
		classIndex = instanceSet.classIndex;
	}
	
	public void run() {
		/* Make clusters of the instanceSet separate for each class value */
		int numClasses = instanceSet.getAttribute(classIndex).numValues();
		int[][] clusters = new int[numClasses][];
		for (int classValue = 0; classValue < numClasses; classValue++) {
			clusters[classValue] = clusterizeClass(classValue);
		}
		
		/* Discover the majority class */
		float[] count = new float[numClasses];
		int classValue;
		for (int inst = 0; inst < numInstances; inst++) {
			classValue = (int) instances[inst].data[classIndex];
			count[classValue] += instances[inst].data[counterIndex];
		}
		float majorityClassQtd = 0;
		int majorityClass = 0;
		for (int c = 0; c < numClasses; c++) {
			if (count[c] > majorityClassQtd) {
				majorityClassQtd = count[c];
				majorityClass = c;
			}
		}
		
		/* Pick the biggest cluster of the majority class */
		int[] majorityClassCluster = clusters[majorityClass];
		int numClusters = majorityClassCluster.length;
		count = new float[numClusters];
		int clusterIndex;
		for (int inst = 0; inst < numInstances; inst++) {
			clusterIndex = majorityClassCluster[inst];
			count[clusterIndex] += instances[inst].data[counterIndex];
		}
		int biggestClusterIndex = 0;
		float biggestClusterQtd = 0;
		for (clusterIndex = 0; clusterIndex < numClusters; clusterIndex++) {
			if (biggestClusterQtd > count[clusterIndex]) {
				biggestClusterQtd = count[clusterIndex];
				biggestClusterIndex = clusterIndex;
			}
		}
		
		/* 
		 * Match the size of clusters of the other majority class with its
		 * biggest cluster.
		 */
		
	}
	
	private int[] clusterizeClass(int classValue) {
		/* Clusterize the numeric attributes first */
		int normFactor = 4;
		IDistance distance = new Euclidean(instanceSet, normFactor);
		Kmeans kmeans = new Kmeans(instanceSet);
		kmeans.setOptionDistance(distance);
		int[] numericClusters = kmeans.clusterize(5, 1.001f);
		
		Squeezer squeezer = new Squeezer(instanceSet);
		int[] nominalClusters = squeezer.clusterize(sSqueezer);

		float[] weight = {instanceSet.numNumericAttributes,
				instanceSet.numNominalAttributes};

		CEBMDC cebmdc = new CEBMDC(instanceSet);
		
		return cebmdc.clusterize(numericClusters, nominalClusters, weight, s);
	}
}

