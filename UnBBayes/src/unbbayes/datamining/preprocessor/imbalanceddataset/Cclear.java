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
public class Cclear {
	
	/** The current instanceSet */
	protected InstanceSet instanceSet;

	/** The current set of input instances */
	protected Instance[] instances;

	/** Number of instances in the instance set */
	protected int numInstances;

	/** Matrix of all clusters. Each row stores all instancesIDs of a cluster */
	protected int[][] clusters;
	protected int[][] clustersOriginal;
	
	/** The class given to each cluster according to its distribution */
	protected int[] clustersClass;
	
	/** Class of most common settings */
	protected TestsetUtils testsetUtils;

	/** Number of clusters created */
	private int numClusters;
	
	private float[] clustersPositiveFrequency;
	
	/**
	 * Array of booleans that tells which instances should be removed.
	 */
	protected boolean[] deleteIndex;

	protected int negativeClass;

	protected int positiveClass;

	
	/**********************************************/
	/** Options for SMOTE - START *****************/
	/**********************************************/
	
	/** SMOTE */
	Smote smote;

	/**
	 * Set it <code>true</code> to discretize the synthetic value created for
	 * the new instance. 
	 */
	boolean optionDiscretize = false;
	
	/** 
	 * 0: copy nominal attributes from the current instance
	 * 1: copy from the nearest neighbors
	 */
	byte optionNominal = 0;
	
	/**
	 * The gap is a random number between 0 and 1 wich tells how far from the
	 * current instance and how near from its nearest neighbor the new instance
	 * will be interpolated.
	 * The optionFixedGap, if true, determines that the gap will be fix for all
	 * attributes. If set to false, a new one will be drawn for each attribute.
	 */
	boolean optionFixedGap = true;
	
	/** 
	 * Distance function:
	 * 0: Hamming
	 * 1: HVDM
	 */
	byte optionDistanceFunction = 1;

	/**********************************************/
	/** Options for SMOTE - END *******************/
	/**********************************************/


	protected boolean[] classClusterized;
	private int[][][] smoteNN;
	private int[][][] smoteNNOriginal;

	private float[] clusterQtd;

	public Cclear(InstanceSet instanceSet, TestsetUtils testsetUtils) {
		this.instanceSet = instanceSet;
		this.testsetUtils = testsetUtils;
		positiveClass = testsetUtils.getPositiveClass();
		negativeClass = testsetUtils.getNegativeClass();
		instances = instanceSet.instances;
		numInstances = instanceSet.numInstances;

		classClusterized = new boolean[2];
		Arrays.fill(classClusterized, false);
		
		/* Start smote parameters */
		smote = new Smote(null);
		smote.setOptionDiscretize(optionDiscretize);
		smote.setOptionDistanceFunction(optionDistanceFunction);
		smote.setOptionFixedGap(optionFixedGap);
		smote.setOptionNominal(optionNominal);
	}
	
	public void run(InstanceSet train, int ratio, boolean clean)
	throws Exception {
		if (instanceSet.isNominal()) {
//			TODO
//			throws("Pau!");
		}
		
		clusterize(train);
		
		oversampling(train, train.positiveFrequency(positiveClass), ratio);

		if (clean) {
			clusterize(train);
			
			float positiveThreshold = 0.7f;
			float negativeThreshold = 0.7f;
			
			clean(positiveThreshold, positiveClass);
			clean(1 - negativeThreshold + 0.000000004, negativeClass);
			
//			float positiveThreshold = train.positiveFrequency(positiveClass) * 1.4f;
//			float negativeThreshold = train.positiveFrequency(positiveClass) * 0.6f;
//			clean(positiveThreshold, positiveClass);
//			clean(negativeThreshold + 0.000000004, negativeClass);
			
			/* Remove instances marked for deletion by the cleaning process */
			removeMarkedInstances();
		}
	}
	
	private void oversampling(InstanceSet instanceSet, float positiveThreshold,
			int ratio) throws Exception {
		/* Assign each cluster`s class according to the positiveThreshold */
		assignClassCluster(positiveThreshold);
		
		int[] cluster = null;
		
		/*
		 * Compute proportion. First, count the number of positive cases. Then
		 * compute how many cases for each positive case must be created so we
		 * get a balance between positive and negative class.
		 */
		int counter = 0;
		int inst;
		for (int clusterID = 0; clusterID < numClusters; clusterID++) {
			if (clustersClass[clusterID] == positiveClass) {
				cluster = clusters[clusterID];
				for (int i = 0; i < cluster.length; i++) {
					inst = cluster[i];
					if (instances[inst].getClassValue() == positiveClass) {
						counter += instanceSet.instances[inst].getWeight();
					}
				}
			}
		}
		float[] dist = instanceSet.getClassDistribution(false);
		float proportion = dist[negativeClass] - dist[positiveClass] + counter;
		proportion *= (float) ratio;
		proportion /= counter * (float) (10 - ratio);

		for (int clusterID = 0; clusterID < numClusters; clusterID++) {
			/* Smote only the positive class */
			if (clustersClass[clusterID] == positiveClass) {
				cluster = smoteCluster(clusterID, proportion, positiveClass);
				if (cluster != null) {
					clusters[clusterID] = cluster;
					clusterQtd[clusterID] = cluster.length;
				}
			}
		}
	}
	
	public int[] smoteCluster(int clusterID, double proportion, int classValue)
	throws Exception {
		/* Build the instancesIDs for the sampling process */
		int counter = 0;
		int[] cluster = clusters[clusterID];
		int clusterLength = cluster.length;
		int[] instancesIDsTmp = new int[clusterLength];
		int[][] smoteNNTmp = new int[clusterLength][];
		int inst;
		for (int i = 0; i < clusterLength; i++) {
			inst = cluster[i];
			if (instanceSet.getInstance(inst).getClassValue() == classValue) {
				instancesIDsTmp[counter] = inst;
				smoteNNTmp[counter] = smoteNN[clusterID][i];
				++counter;
			}
		}
		
		if (counter < 1) {
			/* The input cluster contains no positive cases */
			return null;
		}
		
		int[] instancesIDs = new int[counter];
		int[][] smoteNN = new int[counter][];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
			smoteNN[i] = smoteNNTmp[i];
		}
		int[] newInstances = null;
		smote.setNearestNeighborsIDs(smoteNN);
		newInstances = smote.run(instancesIDs, proportion);

		/* Create a new cluster adding the new smoted instances */
		int numNewInstances = newInstances.length;
		int newClusterSize = numNewInstances + clusterLength;
		int[] newCluster = new int[newClusterSize];
		for (int i = 0; i < clusterLength; i++) {
			newCluster[i] = cluster[i];
		}
		for (int i = clusterLength; i < newClusterSize; i++) {
			newCluster[i] = newInstances[i - clusterLength];
		}
		
		return newCluster;
	}

	private void assignClassCluster(double positiveThreshold) throws Exception {
		/* Assign a class to each cluster according to its distribution */
		clustersClass = new int[numClusters];
		int classValue;
		for (int clusterID = 0; clusterID < numClusters; clusterID++) {
			classValue = assignClassCluster(clusterID, positiveThreshold);
			clustersClass[clusterID] = classValue;
		}
		
		@SuppressWarnings("unused")
		boolean pause = true;
	}

	
	

	/*************************************************************************/
	/**************************** Clean method *******************************/
	
	private void clean(double cleanFactor, int classValue) throws Exception {
		/* Compute cluster distribution (if neeeded) */
		updateClustersPositiveFrequency();
		
		/* Clean the clusters */
		assignClassCluster(cleanFactor);
		
		clean(classValue);
	}
	
	private void clean(int clusterClass) throws Exception {
		int clusterClassDesired;
		int instanceClassDesired;
		if (clusterClass == positiveClass) {
			/* Clean positive clusters (remove negative instances) */
			clusterClassDesired = positiveClass;
			instanceClassDesired = negativeClass;
		} else {
			/* Clean negative clusters (remove positive instances) */
			clusterClassDesired = negativeClass;
			instanceClassDesired = positiveClass;
		}
		int clusterLength;
		int inst;
		Instance instance;
		int[] cluster;
		for (int clusterID = 0; clusterID < numClusters; clusterID++) {
			if (clustersClass[clusterID] == clusterClassDesired) {
				cluster = clusters[clusterID];
				clusterLength = cluster.length;
				for (int i = 0; i < clusterLength; i++) {
					inst = cluster[i];
					instance = instanceSet.getInstance(inst);
					if (instance.getClassValue() == instanceClassDesired) {
						deleteIndex[inst] = true;
					}
				}
			}
		}
	}

	public void removeMarkedInstances() {
		ClusterBasedUtils.removeMarkedInstances(instanceSet, deleteIndex);
		
		/* Initialize deleteIndex array (set no instance to be removed) */
		deleteIndex = new boolean[instanceSet.numInstances];
		Arrays.fill(deleteIndex, false);
	}

	
	

	/*************************************************************************/
	/************************** Auxiliary methods ****************************/
	
	private void smoteNN(InstanceSet instanceSet) throws Exception {
		if (instanceSet.isNumeric() || instanceSet.isMixed()) {
			/* Build smote nearest neighbors */
			smoteNN = ClusterBasedUtils.buildSmoteNN(clusters, instanceSet, smote);
			smoteNNOriginal = smoteNN.clone();
		}
	}

	public void clusterize(InstanceSet instanceSet) throws Exception {
		initialize(instanceSet);
		
		/* Build clusters */
		clusters = ClusterBasedUtils.clusterizeAll2(instanceSet, testsetUtils);
		numClusters = clusters.length;
		clusterQtd = new float[numClusters];
		updateClustersPositiveFrequency();

		/* Build smote nearest neighbors */
		smoteNN = ClusterBasedUtils.buildSmoteNN(clusters, instanceSet, smote);
	}

	private int assignClassCluster(int clusterID, double positiveThreshold) {
		/* Check if the cluster should be taken as a positive cluster */
		if (clustersPositiveFrequency[clusterID] >= positiveThreshold) {
			return positiveClass;
		} else {
			return negativeClass;
		}
	}

	protected void updateClustersPositiveFrequency() {
		int numClusters = clusters.length;
		clustersPositiveFrequency = new float[numClusters];
		float dist;
		
		for (int clusterID = 0; clusterID < numClusters; clusterID++) {
			dist = computeClustersPositiveFrequency(clusterID);
			clustersPositiveFrequency[clusterID] = dist;
		}
		@SuppressWarnings("unused")
		boolean pause = true;
	}
	
	private float computeClustersPositiveFrequency(int clusterID) {
		float numPositives = 0;
		float clusterSize = 0;
		Instance instance;
		int inst;
		int[] cluster = clusters[clusterID];
		int clusterLength = cluster.length;
		float weight;
		for (int i = 0; i < clusterLength; i++) {
			inst = cluster[i];
			if (!deleteIndex[inst]) {
				instance = instanceSet.getInstance(inst);
				weight = instance.getWeight();
				if (instance.getClassValue() == positiveClass) {
					numPositives += weight;
				}
				clusterSize += weight;
			}
		}
		clusterQtd[clusterID] = clusterSize;
		
		return numPositives / clusterSize;
	}

	private void storeClusterAndNN() {
		smoteNNOriginal = smoteNN;
		clustersOriginal = clusters;
	}

	private void restoreClusterAndNN(InstanceSet instanceSet) {
		initialize(instanceSet);
		smoteNN = smoteNNOriginal.clone();
		clusters = clustersOriginal.clone();
		numClusters = clusters.length;
		updateClustersPositiveFrequency();
	}
	
	private void initialize(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
		smote.setInstanceSet(instanceSet);
		numInstances = instanceSet.numInstances;
		instances = instanceSet.instances;
		
		/* Initialize deleteIndex array (set no instance to be removed) */
		deleteIndex = new boolean[numInstances];
		Arrays.fill(deleteIndex, false);
	}
	
}