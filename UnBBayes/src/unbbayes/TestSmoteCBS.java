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

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 11/10/2006
 */
public class TestSmoteCBS {
	public TestSmoteCBS() {
		try {
			run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new TestSmoteCBS();
	}

	public void run() throws Exception {
		/* Data set characteristics */
		String trainFileName = "c:/m1t.txt";
		String testFileName = "c:/m1av.txt";
		byte classIndex = 10;
		byte counterIndex = 11;

		/* Set relative probabilities - probabilistic models */
		boolean relativeProb = true;
//		boolean relativeProb = false;
		
		/* Opens the training set */
		InstanceSet trainData = openFile(trainFileName, counterIndex);
		if (trainData == null) {
			String exceptionMsg = "Couldn't open test data " + trainFileName;
			throw new Exception(exceptionMsg);
		}
		trainData.setClassIndex(classIndex);
		
		/*
		 *******************************************************
		 * Clusterize the training set separated by each class *
		 *******************************************************
		 */
		
		/* Number of clusters desired with numeric clusterization */ 
		int k = 5;

		/* 
		 * The minimum accepted % of change in each iteration in the numeric
		 * clusterization.
		 */
		double kError = 1.001f;

		/* Similarity threshold for the Squeezer algorithm */
		double sSqueezer = 6.0f;
		
		/* Similarity threshold for the CEBMDC algorithm */
		double sCEBMDC = 1.6f;

		/* Clusterize */
		ArrayList clustersFramework = 
			clusterizeClasses(trainData, classIndex, k, kError, sSqueezer, sCEBMDC);
		int[][][] clusters = (int[][][]) clustersFramework.get(0);
		int[] numClusters = (int[]) clustersFramework.get(1);
		double[][] clustersSize = (double[][]) clustersFramework.get(2);
		int[][] assignmentMatrix = (int[][]) clustersFramework.get(3);
		
		/* Apply Cluster-Based Oversampling to the training data */
//		ClusterBasedOversampling cbo = new ClusterBasedOversampling(trainData);
//		cbo.run(clusters, numClusters, clustersSize, assignmentMatrix);
		
		/* Apply undersampling */
		int i = 5;
		float originalDist[] = distribution(trainData);
		double proportion = originalDist[0] * (float) i;
		proportion = proportion / (originalDist[1] * (double) (10 - i));
//		Sampling.simpleSampling(trainData, Math.sqrt(1/proportion), 0, false);
//		Sampling.undersampling(trainData, Math.sqrt(1/proportion)*1.93268, 0, false);

		/* Check proportion */
		originalDist = distribution(trainData);
		proportion = originalDist[0] * (float) i;
		proportion = proportion / (originalDist[1] * (double) (10 - i));

		/* Apply Cluster-Based SMOTE to the training data */
		ClusterBasedSmote cbs = new ClusterBasedSmote(trainData);
		cbs.setOptionDiscretize(false);
		cbs.setOptionDistanceFunction((byte) 1);
		cbs.setOptionFixedGap(true);
		cbs.setOptionNominal((byte) 0);
		trainData = cbs.run(clusters, numClusters, clustersSize, assignmentMatrix);
		
		/* Apply simple oversampling */
//		int i = 5;
//		float originalDist[] = distribution(trainData);
//		double proportion = originalDist[0] * (float) i;
//		proportion = proportion / (originalDist[1] * (double) (10 - i));
////		Sampling.oversampling(trainData, proportion, 1);
//		Sampling.simpleSampling(trainData, proportion, 1, false);

		/* Build classifier */
		Classifier classifier = new NaiveBayes();
//		Classifier classifier = new C45();
		classifier.buildClassifier(trainData);

		/* Opens the test set */
		InstanceSet testData = openFile(testFileName, counterIndex);
		if (testData == null) {
			String exceptionMsg = "Couldn't open test data " + testFileName;
			throw new Exception(exceptionMsg);
		}
		testData.setClassIndex(classIndex);

		if (classifier instanceof DistributionClassifier) {
			if (relativeProb) {
				((DistributionClassifier)classifier).setRelativeClassification();
			} else {
				((DistributionClassifier)classifier).setNormalClassification();
			}
		}

		/* Evaluate the model */
		Evaluation eval = new Evaluation(testData, classifier);
		eval.evaluateModel(classifier, testData);
		
		/* Print out the SE */
		float sensibility = (float) eval.truePositiveRate(1) * 1000;
		sensibility = (int) sensibility;
		sensibility = sensibility / 1000;
		float specificity = (float) eval.truePositiveRate(0) * 1000;
		specificity = (int) specificity;
		specificity = specificity / 1000;
		float SE = sensibility * specificity * 1000;
		SE = (int) SE;
		SE = SE / 1000;
		
		System.out.print(sensibility + "	");
		System.out.print(specificity + "	");
		System.out.println(SE);
	}
	
	private InstanceSet openFile(String fileName, int counterIndex) throws IOException {
		File file = new File(fileName);
		TxtLoader loader = new TxtLoader(file, -1);
		
		/* If the dataset is compacted */
		loader.setCounterAttribute(counterIndex);
		
		while (loader.getInstance()) {
			/* Wait while instances are loaded */
		}
		if (loader != null) {
			InstanceSet instanceSet = loader.getInstanceSet();

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
	private ArrayList clusterizeClasses(InstanceSet instanceSet, int classIndex,
			int k, double kError, double sSqueezer, double sCEBMDC) throws Exception {
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
//			cebmdc.setUseAverageSimilarity(false);

		int[] numericClusters;
		int[] nominalClusters;
		
		int numClasses = instanceSet.getAttribute(classIndex).numValues();
		int[][][] clusters = new int[numClasses][][];
		int[] numClusters = new int[numClasses];
		double[][] clustersSize = new double[numClasses][];
		int[][] assignmentMatrix = new int[numClasses][];

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