package unbbayes.datamining.preprocessor.imbalanceddataset;

import java.util.ArrayList;

import unbbayes.datamining.clustering.CEBMDC;
import unbbayes.datamining.clustering.Kmeans;
import unbbayes.datamining.clustering.Squeezer;
import unbbayes.datamining.datamanipulation.AttributeStats;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.distance.Euclidean;
import unbbayes.datamining.distance.IDistance;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 09/11/2006
 */
public class ClusterBasedSmote {
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

	/**
	 * SMOTE
	 */
	Smote smote;

	/**********************************************/
	/** Options for SMOTE - START *****************/
	/**********************************************/
	
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

	private AttributeStats[] attributeStats;
	
	/**********************************************/
	/** Options for SMOTE - END *******************/
	/**********************************************/
	
	public ClusterBasedSmote(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
		instances = instanceSet.instances;
		numInstances = instanceSet.numInstances;
		counterIndex = instanceSet.counterIndex;
		classIndex = instanceSet.classIndex;
		if (classIndex != -1) {
			numClasses = instanceSet.getAttribute(classIndex).numValues();
		} else {
			numClasses = 0;
		}

		/* Compute the statistics for all attributes */
		attributeStats = instanceSet.getAttributeStats(false);
	}
	
	public InstanceSet run() throws Exception {
		/*
		 *******************************************************
		 * Clusterize the training set separated by each class *
		 *******************************************************
		 */
		
		/* Number of clusters desired for the numeric clusterization */ 
		int k = 5;

		/* 
		 * The minimum accepted % of change in each iteration in the numeric
		 * clusterization.
		 */
		double kError = 1.001f;

		/* Similarity threshold for the Squeezer algorithm */
		double sSqueezer = 0f;
		
		/* Similarity threshold for the CEBMDC algorithm */
		double sCEBMDC = 0f;

		/* Clusterize classes */
		clusterizeClasses(classIndex, k, kError, sSqueezer, sCEBMDC);
		
		return run(clusters, numClusters, clustersSize, assignmentMatrix);
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
	private void clusterizeClasses(int classIndex, int k, double kError,
			double sSqueezer, double sCEBMDC) throws Exception {
		boolean numeric = false;
		boolean nominal = false;
		boolean mixed = false;
		
		/* Algorithm for clustering numeric attributes */
		if (instanceSet.numNumericAttributes > 0) {
			numeric = true;
		}

		/* Set the options for the Kmeans algorithm */
		int normFactor = 4;
		IDistance distance = new Euclidean(instanceSet, normFactor);
		Kmeans kmeans = new Kmeans(instanceSet);
		kmeans.setOptionDistance(distance);
		kmeans.setError(kError);
		kmeans.setNumClusters(k);
		
		/* Algorithm for clustering nominal attributes */
		if (instanceSet.numNumericAttributes > 0 || 
				instanceSet.numCyclicAttributes > 0) {
			if (numeric) mixed = true;
		}
		
		/* Set the options for the Squeezer algorithm */
		Squeezer squeezer = new Squeezer(instanceSet);
		squeezer.setUseAverageSimilarity(true);
//		squeezer.setUseAverageSimilarity(false);
		squeezer.setS(sSqueezer);
		nominal = true;
		
		/* Algorithm for clustering mixed attributes */
		/* Set the options for the CEBMDC algorithm */
		CEBMDC cebmdc = new CEBMDC(instanceSet);
		cebmdc.setS(sCEBMDC);
		double[] weight = {instanceSet.numNumericAttributes,
				instanceSet.numNominalAttributes};
		cebmdc.setWeight(weight);
		cebmdc.setUseAverageSimilarity(true);
//		cebmdc.setUseAverageSimilarity(false);

		int[] numericClusters;
		int[] nominalClusters;
		
		clusters = new int[numClasses][][];
		numClusters = new int[numClasses];
		clustersSize = new double[numClasses][];
		assignmentMatrix = new int[numClasses][];

		for (int classValue = 0; classValue < numClasses; classValue++) {
			/* Clusterize the numeric attributes */
			numericClusters = null;
			if (numeric) {
				kmeans.clusterize(classValue);
				numericClusters = kmeans.getAssignmentMatrix();
			}

			/* Clusterize the nominal attributes */
			nominalClusters = null;
			if (nominal) {
				squeezer.clusterize(classValue);
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
		}
	}


	public InstanceSet run(int[][][] clusters, int[] numClusters,
			double[][] clustersSize, int[][] assignmentMatrix) throws Exception {
		this.clusters = clusters;
		this.numClusters = numClusters;
		this.clustersSize = clustersSize;
		this.assignmentMatrix = assignmentMatrix;
		
		/* Set SMOTE options */
		smote = new Smote(null);
		smote.setOptionDiscretize(optionDiscretize);
		smote.setOptionDistanceFunction(optionDistanceFunction);
		smote.setOptionFixedGap(optionFixedGap);
		smote.setOptionNominal(optionNominal);
		smote.setInstanceSet(instanceSet);

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
//		smote.buildNN(5, majorityClass);
//		count = clustersSize[majorityClass];
//		numClustersAux = numClusters[majorityClass];
//		for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
//			if (clusterID != biggestClusterIndex) {
//				finalSize = biggestClusterSize / count[clusterID];
//				--finalSize;
////				oversampleCluster(clusterID, finalSize, majorityClass);
//				smoteCluster(clusterID, finalSize, majorityClass, attributeStats);
//			}
//		}

		/* Reset the variables */
		instances = instanceSet.instances;
		
		/* Compute the new size of the majority class */
		double majorityClassSize = 0;
		for (int inst = 0; inst < instanceSet.numInstances; inst++) {
			if (instances[inst].data[classIndex] == majorityClass) {
				majorityClassSize += instances[inst].data[counterIndex];
			}
		}
		
		/* Now we oversample the clusters of the other classes one by one */
		double newSizePerCluster;
		for (int classValue = 0; classValue < numClasses; classValue++) {
			if (classValue != majorityClass) {
				smote.buildNN(5, classValue);
				numClustersAux = numClusters[classValue];
				newSizePerCluster = majorityClassSize / numClustersAux;
				for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
					finalSize = newSizePerCluster;
					finalSize /= clustersSize[classValue][clusterID];
					--finalSize;
					if (finalSize > 1) {
//						oversampleCluster(clusterID, finalSize, classValue);
						smoteCluster(clusterID, finalSize, classValue,
								attributeStats);
					}
				}
			}
		}
		
//		// Only for testing purpouses
//		clustersSize = new double[numClasses][];
//		for (int classValue = 0; classValue < numClasses; classValue++) {
//			clustersSize[classValue] = new double[numClusters[classValue]];
//			double clusterSize;
//			int inst;
//			int clusterQtd;
//			numClustersAux = numClusters[classValue];
//			for (int clusterID = 0; clusterID < numClustersAux; clusterID++) {
//				clusterQtd = clusters[classValue][clusterID].length;
//				clusterSize = 0;
//				for (int i = 0; i < clusterQtd; i++) {
//					inst = clusters[classValue][clusterID][i];
//					clusterSize += instances[inst].data[counterIndex];
//				}
//				clustersSize[classValue][clusterID] = clusterSize;
//			}
//		}
		
		return instanceSet;
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

	private void smoteCluster(int clusterIndex, double proportion,
			int classValue, AttributeStats[] attributeStats) {
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
		smote.run(instancesIDs, proportion);
	}

	private void smoteCluster(int clusterIndex, double proportion,
			int[] assignmentMatrix, AttributeStats[] attributeStats) {
		/* Choose the instancesIDs for the sampling process */
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		for (int inst = 0; inst < numInstances; inst++) {
			if (assignmentMatrix[inst] == clusterIndex) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int[] instancesIDs = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		smote.run(instancesIDs, proportion);
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