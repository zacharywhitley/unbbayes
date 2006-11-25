package unbbayes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.classifiers.Evaluation;
import unbbayes.datamining.classifiers.NaiveBayes;
import unbbayes.datamining.clustering.CEBMDC;
import unbbayes.datamining.clustering.Kmeans;
import unbbayes.datamining.clustering.Squeezer;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.TxtLoader;
import unbbayes.datamining.distance.Euclidean;
import unbbayes.datamining.distance.IDistance;
import unbbayes.datamining.preprocessor.imbalanceddataset.ClusterBasedSmote;
import unbbayes.datamining.preprocessor.imbalanceddataset.Sampling;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 11/10/2006
 */
public class Testset3 {
	public Testset3() {
		try {
			run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run2() throws Exception {
		String trainFileName = "c:/m1t.txt";
		byte classIndex = 10;
		byte counterIndex = 11;
//		String trainFileName = "c:/creditApproval.txt";
//		byte classIndex = 15;
//		byte counterIndex = -1;

		/* Opens the training set */
		InstanceSet trainData = openFile(trainFileName, counterIndex);
		if (trainData == null) {
			String exceptionMsg = "Couldn't open test data " + trainFileName;
			throw new Exception(exceptionMsg);
		}
		trainData.setClassIndex(classIndex);
		
//		float proportion = 838.66486f;
////		/* Undersampling */
////		Sampling.simpleSampling(trainData, (1 / proportion), 0);
//		
//		/* Oversampling */
//		Sampling.simpleSampling(trainData, proportion, 1);
//		
//		int normFactor = 4;
//		IDistance distance = new Euclidean(trainData, normFactor);
//		Kmeans kmeans = new Kmeans(trainData);
//		kmeans.setOptionDistance(distance);
//		int[] numericClusters = kmeans.clusterize(5, 1.001f);
//		
//		Squeezer squeezer = new Squeezer(trainData);
//		int[] nominalClusters = squeezer.clusterize(4f);
//
//		float[] weight = {trainData.numNumericAttributes, trainData.numNominalAttributes};
//		CEBMDC cebmdc = new CEBMDC(trainData);
//		cebmdc.clusterize(numericClusters, nominalClusters, weight, 1.8f);
//		int[] clusters = cebmdc.getAssignmentMatrix();
//		
//		/* cross class with cluster */
//		int numClusters = cebmdc.getNumClusters();
//		int[][] count = new int[numClusters][2];
////		Arrays.fill(count, 0);
//		for (int i = 0; i < clusters.length; i++) {
//			count[clusters[i]][(int) trainData.instances[i].data[classIndex]] +=  trainData.instances[i].data[counterIndex];
//		}
//		double ema = 0;
//		for (int i = 0; i < numClusters; i++) {
//			ema += Math.max(count[i][0], count[i][1]);
//		}
//		ema /= (double) trainData.numWeightedInstances;
		
		@SuppressWarnings("unused")
		int x = 0;
	}
	
	public void run() throws Exception {
		/* Data set characteristics */
		String trainFileName = "c:/hugo.txt";
		byte classIndex = -1;
		byte counterIndex = -1;

		/* Opens the training set */
		InstanceSet trainData = openFile(trainFileName, counterIndex);
		if (trainData == null) {
			String exceptionMsg = "Couldn't open test data " + trainFileName;
			throw new Exception(exceptionMsg);
		}
		
		/* Number of clusters desired with numeric clusterization */ 
		int k = 5;

		/* 
		 * The minimum accepted % of change in each iteration in the numeric
		 * clusterization.
		 */
		double kError = 1.001f;

		/* Clusterize the training set */
		ArrayList clustersFramework = clusterize(trainData, k, kError);
		int[][] clusters = (int[][]) clustersFramework.get(0);
		int numClusters = (Integer) clustersFramework.get(1);
		double[] clustersSize = (double[]) clustersFramework.get(2);
		int[] assignmentMatrix = (int[]) clustersFramework.get(3);
		
		/* Apply Cluster-Based SMOTE to the training data */
		ClusterBasedSmote cbs = new ClusterBasedSmote(trainData);
		cbs.setOptionDiscretize(false);
		cbs.setOptionDistanceFunction((byte) 1);
		cbs.setOptionFixedGap(true);
		cbs.setOptionNominal((byte) 0);
		trainData = cbs.run(clusters, numClusters, clustersSize, assignmentMatrix);
		
	}
	
	private InstanceSet openFile(String fileName, int counterIndex) throws IOException {
		File file = new File(fileName);
		TxtLoader loader = new TxtLoader(file);
		
		/* If the dataset is compacted */
		loader.setCounterAttribute(counterIndex);
		
		while (loader.getInstance()) {
			/* Wait while instances are loaded */
		}
		if (loader != null) {
			InstanceSet instanceSet = loader.getInstances();

			return instanceSet;
		}
		
		return null;
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
	private ArrayList clusterize(InstanceSet instanceSet, int k, double kError)
	throws Exception {
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
		nominal = true;
		
		/* Algorithm for clustering mixed attributes */
		/* Set the options for the CEBMDC algorithm */
		CEBMDC cebmdc = new CEBMDC(instanceSet);
		double[] weight = {instanceSet.numNumericAttributes,
				instanceSet.numNominalAttributes};
		cebmdc.setWeight(weight);
		cebmdc.setUseAverageSimilarity(true);

		int[] numericClusters;
		int[] nominalClusters;
		
		int[][] clusters = null;
		int numClusters = 0;
		double[] clustersSize = null;
		int[] assignmentMatrix = null;

		/* Clusterize the numeric attributes */
		numericClusters = null;
		if (numeric) {
			kmeans.clusterize();
			numericClusters = kmeans.getAssignmentMatrix();
		}

		/* Clusterize the nominal attributes */
		nominalClusters = null;
		if (nominal) {
			squeezer.clusterize();
			nominalClusters = squeezer.getAssignmentMatrix();
		}
		
		/* Clusterize the both numeric and nominal attributes */
		if (mixed) {
			cebmdc.setNumericClustersInput(numericClusters);
			cebmdc.setNominalClustersInput(nominalClusters);
			cebmdc.clusterize();
			
			/* Get the cluster results */
			clusters = cebmdc.getClusters();
			numClusters = cebmdc.getNumClusters();
			clustersSize = cebmdc.getClustersSize();
			assignmentMatrix = cebmdc.getAssignmentMatrix();
		} else if (numeric) {
			/* Get the cluster results */
			clusters = kmeans.getClusters();
			numClusters = kmeans.getNumClusters();
			clustersSize = kmeans.getClustersSize();
			assignmentMatrix = kmeans.getAssignmentMatrix();
		} else if (nominal) {
			/* Get the cluster results */
			clusters = squeezer.getClusters();
			numClusters = squeezer.getNumClusters();
			clustersSize = squeezer.getClustersSize();
			assignmentMatrix = squeezer.getAssignmentMatrix();
		}
		
		ArrayList<Object> result = new ArrayList<Object>(4);
		result.add(0, clusters);
		result.add(1, numClusters);
		result.add(2, clustersSize);
		result.add(3, assignmentMatrix);
		
		return result;
	}

	private float[] distribution(InstanceSet trainData) {
		int numInstances = trainData.numInstances();
		int numClasses = trainData.numClasses();
		int classIndex = trainData.classIndex;
		int counterIndex = trainData.counterIndex;
		float distribution[] = new float[numClasses];
//		float weight = 0;
		int classValue;
		
		for (int i = 0; i < numClasses; i++) {
			distribution[i] = 0;
		}
		
		for (int i = 0; i < numInstances; i++) {
			classValue = (int) trainData.instances[i].data[classIndex];
			distribution[classValue] += trainData.instances[i].data[counterIndex];
//			weight += dataset[i][counterIndex];
		}

//		for (int i = 0; i < numClasses; i++) {
//			distribution[i] /= weight;
//		}
		return distribution;
	}

}