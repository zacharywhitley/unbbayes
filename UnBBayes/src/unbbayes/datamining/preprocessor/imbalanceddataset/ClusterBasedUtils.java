package unbbayes.datamining.preprocessor.imbalanceddataset;

import java.util.ArrayList;
import java.util.Arrays;

import unbbayes.TestsetUtils;
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
 * @date 14/02/2007
 */
public class ClusterBasedUtils {
	
	/** The current instanceSet */
	protected InstanceSet instanceSet;

	/** The current set of input instances */
	protected Instance[] instances;

	/** Number of instances in the instance set */
	protected int numInstances;

	/** Number of classes in the instance set */
	protected int numClasses;

	/** 
	 * The index of the dataset's column that represents the counter attribute.
	 */
	protected int counterIndex;

	/** 
	 * The index of the dataset's column that represents the class attribute.
	 * Assume the value -1 in case there is no class attribute.
	 */
	protected int classIndex;

	/** Matrix of all clusters. Each row stores all instancesIDs of a cluster */
	protected int[][][] clusters;
	
	protected int[] numClusters;
	
	protected double[][] clustersSize;
	
	/** The class given to each cluster according to its distribution */
	protected int[] clustersClass;
	
	/** Class of most common settings */
	protected TestsetUtils testsetUtils;

	/** Number of clusters desired for the numeric clusterization */ 
	int k;
	
	/**
	 * The minimum accepted % of change in each iteration in the numeric
	 * clusterization.
	 */
	protected double kError;

	/**
	 * Array of booleans that tells which instances should be removed.
	 */
	protected boolean[] deleteIndex;

	protected int negativeClass;

	protected int positiveClass;

	
	/**
	 * SMOTE
	 */
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

	private int[][][][] smoteNN;
	
	public ClusterBasedUtils(InstanceSet instanceSet, TestsetUtils testsetUtils) {
		this.instanceSet = instanceSet;
		this.testsetUtils = testsetUtils;
		positiveClass = testsetUtils.getPositiveClass();
		k = testsetUtils.getK();
		negativeClass = testsetUtils.getNegativeClass();
		instances = instanceSet.instances;
		numInstances = instanceSet.numInstances;
		counterIndex = instanceSet.counterIndex;
		numClasses = instanceSet.numClasses();
		deleteIndex = new boolean[numInstances];
		classIndex = instanceSet.classIndex;
		if (classIndex != -1) {
			numClasses = instanceSet.getAttribute(classIndex).numValues();
		} else {
			numClasses = 0;
		}
		
		/* 
		 * The minimum accepted % of change in each iteration in the numeric
		 * clusterization.
		 */
		kError = 1.001f;
		
		/* Set SMOTE options */
		smote = new Smote(null);
		smote.setOptionDiscretize(optionDiscretize);
		smote.setOptionDistanceFunction(optionDistanceFunction);
		smote.setOptionFixedGap(optionFixedGap);
		smote.setOptionNominal(optionNominal);
		smote.setInstanceSet(instanceSet);

		classClusterized = new boolean[numClasses];
		Arrays.fill(classClusterized, false);

		/* Initialize arrays */
		clusters = new int[numClasses][][];
		numClusters = new int[numClasses];
		clustersSize = new double[numClasses][];
		smoteNN = new int[numClasses][][][];
	}
	
	public void clusterizeByClass() throws Exception {
		int numClasses = instanceSet.numClasses();
		for (int classValue = 0; classValue < numClasses; classValue++) {
			clusterizeByClass(classValue);
		}
	}
	
	public static int[][][] clusterizeByClass(InstanceSet instanceSet,
			TestsetUtils testsetUtils)
	throws Exception {
		int numClasses = instanceSet.numClasses();
		int[][][] clusters = new int[numClasses][][];
		for (int classValue = 0; classValue < numClasses; classValue++) {
			ArrayList<Object> result = clusterize(classValue, true, instanceSet,
					testsetUtils);
			
			clusters[classValue] = (int[][]) result.get(0);
		}
		return clusters;
	}
	
	public void clusterizeByClass(int classValue) throws Exception {
		if (classClusterized[classValue]) {
			return;
		}
		
		ArrayList<Object> result = clusterize(classValue, true, instanceSet,
				testsetUtils);
		
		clusters[classValue] = (int[][]) result.get(0);
		numClusters[classValue] = clusters[classValue].length;
		clustersSize[classValue] = new double[numClusters[classValue]];
		int[] cluster;
		int counter;
		int inst;
		for (int i = 0; i < numClusters[classValue]; i++) {
			cluster = clusters[classValue][i];
			counter = 0;
			for (int j = 0; j < cluster.length; j++) {
				inst = cluster[j];
				counter += instanceSet.instances[inst].getWeight();
			}
			clustersSize[classValue][i] = counter;
		}
		smoteNN[classValue] = buildSmoteNN(clusters[classValue], instanceSet, smote);
		classClusterized[classValue] = true;
	}
	
	@SuppressWarnings("unchecked")
	public static int[][] clusterizeAll2(InstanceSet instanceSet,
			TestsetUtils testsetUtils)
	throws Exception {
		ArrayList<Object> result = clusterize(testsetUtils.getPositiveClass(),
				false, instanceSet, testsetUtils);
		
		return (int[][]) result.get(0);
	}
	
	@SuppressWarnings("unchecked")
	public static int[][] clusterizeAll2(InstanceSet instanceSet,
			int[] instancesPos, TestsetUtils testsetUtils)
	throws Exception {
		ArrayList<Object> result;
		result = clusterize(instancesPos, true, instanceSet, testsetUtils);
		
		return (int[][]) result.get(0);
	}
	
	/**
	 * Return an array of integer with the clusterID of each instance of the
	 * class pointed by the parameter <code>classValue</code> in the order they
	 * appear in the instanceSet.
	 * 
	 * @param classValue
	 * @param testsetUtils
	 * @return
	 * @throws Exception 
	 */
	private static ArrayList<Object> clusterize(int classValue, boolean byClass,
			InstanceSet instanceSet, TestsetUtils testsetUtils) throws Exception {
		/* Choose the instancesIDs for the clustering process */
		int[] instancesPos = instanceSet.getInstancesPosFromClass(classValue);
		
		return clusterize(instancesPos, byClass, instanceSet, testsetUtils);
	}
	
	private static ArrayList<Object> clusterize(int[] instancesPos, boolean byClass,
			InstanceSet instanceSet, TestsetUtils testsetUtils) throws Exception {
		/* Set the options for the Kmeans algorithm */
		int normFactor = 4;
		IDistance distance = new Euclidean(instanceSet, normFactor, false);
		Kmeans kmeans = new Kmeans(instanceSet);
		kmeans.setOptionDistance(distance);
		kmeans.setError(testsetUtils.getKError());
		kmeans.setNumClusters(testsetUtils.getK());
		
		/* Set the options for the numeric Squeezer algorithm */
		float similarityThreshold = testsetUtils.getSimilarityThreshold();

		/* Set the options for the Squeezer algorithm */
		Squeezer squeezer = new Squeezer(instanceSet);
		squeezer.setUseAverageSimilarity(true);
		
		/* Algorithm for clustering mixed attributes */
		/* Set the options for the CEBMDC algorithm */
		CEBMDC cebmdc = new CEBMDC(instanceSet);
		double[] weight = {instanceSet.numNumericAttributes,
				instanceSet.numNominalAttributes};
		cebmdc.setWeight(weight);
		cebmdc.setUseAverageSimilarity(true);

		int[] numericClusters;
		int[] nominalClusters;
		
		/* Clusterize the numeric attributes */
		numericClusters = null;
		if (instanceSet.isNumeric() || instanceSet.isMixed()) {
			if (byClass) {
				kmeans.clusterize(instancesPos);
			} else {
				kmeans.clusterize();
			}
			numericClusters = kmeans.getAssignmentMatrix();
		}

		/* Clusterize the nominal attributes */
		nominalClusters = null;
		if (instanceSet.isNominal() || instanceSet.isMixed()) {
			if (byClass) {
				squeezer.clusterize(instancesPos);
			} else {
				squeezer.clusterize();
			}
			nominalClusters = squeezer.getAssignmentMatrix();
		}

		int[][] clusters = null;

		/* Clusterize the both numeric and nominal attributes */
		if (instanceSet.isMixed()) {
			cebmdc.setNumericClustersInput(numericClusters);
			cebmdc.setNominalClustersInput(nominalClusters);
			cebmdc.clusterize();
			
			/* Get the cluster results */
			clusters = cebmdc.getClusters();
		} else if (instanceSet.isNumeric()) {
			/* Get the cluster results */
			clusters = kmeans.getClusters();
		} else if (instanceSet.isNominal()) {
			/* Get the cluster results */
			clusters = squeezer.getClusters();
		}
		
		ArrayList<Object> result = new ArrayList<Object>(2);
		result.add(clusters);
		
		return result;
	}

	public static int[][][] buildSmoteNN(int[][] clusters,
			InstanceSet instanceSet, Smote smote)
	throws Exception {
		if (instanceSet.isNumeric() || instanceSet.isMixed()) {
			int numClusters = clusters.length;
			int[][][] smoteNN = new int[numClusters][][];
			
			/* Build nearest neighbors for each cluster */
			for (int clusterID = 0; clusterID < numClusters; clusterID++) {
				smoteNN[clusterID] = smote.buildNN(clusters[clusterID], 5);
			}
			return smoteNN;
		}
		return null;
	}

	protected void smoteCluster(int clusterID, double proportion,
			int classValue) throws Exception {
		if (smoteNN[classValue] == null) {
			smoteNN[classValue] = 
				buildSmoteNN(clusters[classValue], instanceSet, smote);
		}
		int[] cluster = clusters[classValue][clusterID];
		int[][] smoteNN = this.smoteNN[classValue][clusterID];
		
		smote.setNearestNeighborsIDs(smoteNN);
		smote.run(cluster, proportion);
	}

	protected void oversampleCluster(int clusterIndex, double proportion,
			int classValue, boolean simplesampling) {
		int[] cluster = clusters[classValue][clusterIndex];
		if (simplesampling) {
			Sampling.simplesampling(instanceSet, proportion, cluster,
					false);
		} else {
			Sampling.oversampling(instanceSet, proportion, cluster);
		}
	}

	protected void oversampleCluster(int clusterIndex, double proportion,
			boolean simplesampling) {
		/* Choose the instancesIDs for the sampling process */
		int[] instancesIDs = chooseInstancesIDs(clusterIndex, positiveClass);

		if (simplesampling) {
			Sampling.simplesampling(instanceSet, proportion, instancesIDs,
					false);
		} else {
			Sampling.oversampling(instanceSet, proportion, instancesIDs);
		}
	}

	private int[] chooseInstancesIDs(int clusterIndex, int classValue) {
		/* Choose the instancesIDs for the sampling process */
		int counter = 0;
		int[] instancesIDsTmp = new int[numInstances];
		int[] cluster = clusters[classValue][clusterIndex];
		int clusterSize = cluster.length;
		Instance instance;
		int instanceClass;
		int inst;
		for (int i = 0; i < clusterSize; i++) {
			inst = cluster[i];
			if (deleteIndex[inst]) {
				continue;
			}
			instance = instanceSet.instances[inst];
			instanceClass = (int) instance.data[classIndex];
			if (instanceClass == classValue || classValue == -1) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int[] instancesIDs = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		
		return instancesIDs;
	}
	
	protected void undersampleCluster(int clusterIndex, double proportion,
			boolean simplesampling) {
		/* Choose the instancesIDs for the sampling process */
		int[] instancesIDs = chooseInstancesIDs(clusterIndex, negativeClass);

		if (simplesampling) {
			Sampling.simplesampling(instanceSet, proportion, instancesIDs,
					false);
		} else {
			Sampling.undersampling(instanceSet, proportion, instancesIDs,
					false, null);
		}
	}

	protected void setInstanceSet(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
		smote.setInstanceSet(instanceSet);
		numInstances = instanceSet.numInstances;
		instances = instanceSet.instances;

		/* Initialize deleteIndex array (set no instance to be removed) */
		deleteIndex = new boolean[instanceSet.numInstances];
		Arrays.fill(deleteIndex, false);
	}
	
	public static void removeMarkedInstances(InstanceSet instanceSet,
			boolean[] deleteIndex) {
		/* Remove those negative instances marked for removal */
		instanceSet.removeInstances(deleteIndex);
	}

	/**
	 * Set it <code>true</code> to discretize the synthetic value created for
	 * the new instance. 
	 * 
	 * @param optionDiscretize
	 */
	public void setOptionDiscretize(boolean optionDiscretize) {
		this.optionDiscretize = optionDiscretize;
	}
	
	/**
	 * 0: copy nominal attributes from the current instance
	 * 1: copy from the nearest neighbors
	 * 
	 * @param optionNominal
	 */
	public void setOptionNominal(byte optionNominal) {
		this.optionNominal = optionNominal;
	}
	
	/**
	 * The gap is a random number between 0 and 1 wich tells how far from the
	 * current instance and how near from its nearest neighbor the new instance
	 * will be interpolated.
	 * The optionFixedGap, if true, determines that the gap will be fix for all
	 * attributes. If set to false, a new one will be drawn for each attribute.
	 * 
	 * @param optionFixedGap
	 */
	public void setOptionFixedGap(boolean optionFixedGap) {
		this.optionFixedGap = optionFixedGap;
	}
	
	/**
	 * Distance function:
	 * 0: Hamming
	 * 1: HVDM
	 * 
	 * @param optionDistanceFunction
	 */
	public void setOptionDistanceFunction(byte optionDistanceFunction) {
		this.optionDistanceFunction = optionDistanceFunction;
	}
	
}