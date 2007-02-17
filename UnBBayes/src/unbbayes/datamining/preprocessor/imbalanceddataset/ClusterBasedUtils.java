package unbbayes.datamining.preprocessor.imbalanceddataset;

import java.util.Arrays;

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
	
	/** The class given to each cluster according to its distribution */
	protected int[] clusterClass;
	
	/** Number of clusters created */
	protected int[] numClusters;
	
	/** Weighted number of instances in each cluster created */
	protected double[][] clustersSize;

	/**
	 * Data assignment to clusters. Each position corresponds to the cluster id
	 * of an instance in the same order it appears in the input instanceSet.
	 */
	protected int[][] assignmentMatrix;

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

	
	/**********************************************/
	/** Options for SMOTE - START *****************/
	/**********************************************/
	
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

	private int[][][] smoteNN;
	
	public ClusterBasedUtils(InstanceSet instanceSet, int positiveClass, int k) {
		this.positiveClass = positiveClass;
		this.instanceSet = instanceSet;
		this.k = k;
		negativeClass = Math.abs(1 - positiveClass);
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

		classClusterized = new boolean[numClasses];
		Arrays.fill(classClusterized, false);

		/* Initialize arrays */
		clusters = new int[numClasses][][];
		numClusters = new int[numClasses];
		clustersSize = new double[numClasses][];
		assignmentMatrix = new int[numClasses][];
	}
	
	public void setClusters(ClusterBasedUtils clustersInfo) {
		clusters = clustersInfo.getClusters();
		numClusters = clustersInfo.getNumClusters();
		clustersSize = clustersInfo.getClustersSize();
		assignmentMatrix = clustersInfo.getAssignmentMatrix();
	}

	public void clusterizeByClass() throws Exception {
		int numClasses = instanceSet.numClasses();
		for (int classValue = 0; classValue < numClasses; classValue++) {
			clusterize(classValue);
		}
	}
	
	public void clusterize(int classValue) throws Exception {
		clusterize(classValue, true);
	}
	
	public void clusterize() throws Exception {
		clusterize(0, false);
	}
	
	/**
	 * Return an array of integer with the clusterID of each instance of the
	 * class pointed by the parameter <code>classValue</code> in the order they
	 * appear in the instanceSet.
	 * 
	 * @param classValue
	 * @return
	 * @throws Exception 
	 */
	public void clusterize(int classValue, boolean byClass) throws Exception {
		boolean numeric = false;
		boolean nominal = false;
		boolean mixed = false;
		
		if (classClusterized[classValue]) {
			return;
		}
		
		/* Check if the instanceSet contains any numeric attribute */
		if (instanceSet.numNumericAttributes > 0) {
			numeric = true;
		}

		/* Set the options for the Kmeans algorithm */
		int normFactor = 4;
		IDistance distance = new Euclidean(instanceSet, normFactor, false);
		Kmeans kmeans = new Kmeans(instanceSet);
		kmeans.setOptionDistance(distance);
		kmeans.setError(kError);
		kmeans.setNumClusters(k);
		
		/*
		 * Check if the instanceSet contains any nominal attribute, besides the
		 * class attribute.
		 */
		if (instanceSet.numCyclicAttributes > 0 ||
			instanceSet.numNominalAttributes > 1 ||
			(!instanceSet.classIsNominal() &&
				instanceSet.numNominalAttributes > 0)) {
			nominal = true;
		}
		
		/* Check if the instanceSet contains mixed attributes */
		if (nominal && numeric) {
			mixed = true;
		}
		
		/* Set the options for the Squeezer algorithm */
		Squeezer squeezer = new Squeezer(instanceSet);
		squeezer.setUseAverageSimilarity(true);
//		squeezer.setUseAverageSimilarity(false);
//		squeezer.setS(sSqueezer);
		
		/* Algorithm for clustering mixed attributes */
		/* Set the options for the CEBMDC algorithm */
		CEBMDC cebmdc = new CEBMDC(instanceSet);
		double[] weight = {instanceSet.numNumericAttributes,
				instanceSet.numNominalAttributes};
		cebmdc.setWeight(weight);
		cebmdc.setUseAverageSimilarity(true);
//		cebmdc.setUseAverageSimilarity(false);
//		cebmdc.setS(sCEBMDC);

		int[] numericClusters;
		int[] nominalClusters;
		
		/* Clusterize the numeric attributes */
		numericClusters = null;
		if (numeric) {
			if (byClass) {
				kmeans.clusterize(classValue);
			} else {
				kmeans.clusterize();
			}
			numericClusters = kmeans.getAssignmentMatrix();
		}

		/* Clusterize the nominal attributes */
		nominalClusters = null;
		if (nominal) {
			if (byClass) {
				squeezer.clusterize(classValue);
			} else {
				squeezer.clusterize();
			}
			nominalClusters = squeezer.getAssignmentMatrix();
		}

		/* Clusterize the both numeric and nominal attributes */
		if (mixed) {
			cebmdc.setNumericClustersInput(numericClusters);
			cebmdc.setNominalClustersInput(nominalClusters);
			cebmdc.clusterize();
			
			/* Get the cluster results */
			clusters[classValue] = cebmdc.getClusters();
			numClusters[classValue] = cebmdc.getNumClusters();
			clustersSize[classValue] = cebmdc.getClustersSize();
			assignmentMatrix[classValue] = cebmdc.getAssignmentMatrix();
		} else if (numeric) {
			/* Get the cluster results */
			clusters[classValue] = kmeans.getClusters();
			numClusters[classValue] = kmeans.getNumClusters();
			clustersSize[classValue] = kmeans.getClustersSize();
			assignmentMatrix[classValue] = kmeans.getAssignmentMatrix();
		} else if (nominal) {
			/* Get the cluster results */
			clusters[classValue] = squeezer.getClusters();
			numClusters[classValue] = squeezer.getNumClusters();
			clustersSize[classValue] = squeezer.getClustersSize();
			assignmentMatrix[classValue] = squeezer.getAssignmentMatrix();
		}
		
		classClusterized[classValue] = true;
		
		if (classValue == positiveClass) {
			int[][] clusters = this.clusters[classValue];
			int numClusters = this.numClusters[classValue];
			
			/* Build nearest neighbors for each cluster */
			for (int clusterID = 0; clusterID < numClusters; clusterID++) {
				smoteNN[clusterID] = smote.buildNN(clusters[clusterID], 5);
			}
		}
	}

	protected void smoteCluster(int clusterID, double proportion,
			int classValue) throws Exception {
		int[] cluster = clusters[classValue][clusterID];
		smote.setNearestNeighborsIDs(smoteNN[clusterID]);
		smote.run(cluster, proportion);
	}

	protected void smoteCluster(int clusterID, double proportion)
	throws Exception {
		/* Choose the instancesIDs for the sampling process */
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		int[] cluster = clusters[0][clusterID];
		int clusterLength = cluster.length;
		int inst;
		for (int i = 0; i < clusterLength; i++) {
			inst = cluster[i];
			if (instanceSet.getInstance(inst).classValue() == positiveClass) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int[] instancesIDs = new int[counter];
		int[][] smoteNN = new int[counter][];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
			smoteNN[i] = this.smoteNN[clusterID][i];
		}
		smote.setNearestNeighborsIDs(smoteNN);
		smote.run(instancesIDs, proportion);
	}

	protected void oversampleCluster(int clusterIndex, double proportion,
			int classValue, boolean simplesampling) {
		int[] cluster = clusters[classValue][clusterIndex].clone();
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
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		for (int inst = 0; inst < numInstances; inst++) {
			if (assignmentMatrix[0][inst] == clusterIndex) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int[] instancesIDs = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		if (simplesampling) {
			Sampling.simplesampling(instanceSet, proportion, instancesIDs,
					false);
		} else {
			Sampling.oversampling(instanceSet, proportion, instancesIDs);
		}
	}

	protected void undersampleCluster(int clusterIndex, double proportion,
			int classValue, boolean simplesampling) {
		/* Choose the instancesIDs for the sampling process */
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		int[] cluster = clusters[0][clusterIndex];
		int clusterLength = cluster.length;
		int inst;
		for (int i = 0; i < clusterLength; i++) {
			inst = cluster[i];
			if (instanceSet.getInstance(inst).classValue() == positiveClass) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int[] instancesIDs = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		if (simplesampling) {
			Sampling.simplesampling(instanceSet, proportion, instancesIDs,
					false);
		} else {
			Sampling.undersampling(instanceSet, proportion, instancesIDs,
					false);
		}
	}

	public void setInstanceSet(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
		smote.setInstanceSet(instanceSet);
	}
	
	/**
	 * @return the clusters
	 */
	public int[][][] getClusters() {
		return clusters;
	}

	/**
	 * @param numClusters the numClusters to set
	 */
	public void setNumClusters(int[] numClusters) {
		this.numClusters = numClusters;
	}

	/**
	 * @return the numClusters
	 */
	public int[] getNumClusters() {
		return numClusters;
	}

	/**
	 * @return the clustersSize
	 */
	public double[][] getClustersSize() {
		return clustersSize;
	}

	/**
	 * @return the assignmentMatrix
	 */
	public int[][] getAssignmentMatrix() {
		return assignmentMatrix;
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