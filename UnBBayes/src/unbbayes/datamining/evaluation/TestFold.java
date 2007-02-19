package unbbayes.datamining.evaluation;

import java.util.Arrays;

import unbbayes.TestsetUtils;
import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.classifiers.decisiontree.DecisionTreeLearning;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Utils;
import unbbayes.datamining.preprocessor.imbalanceddataset.ClusterBasedSmote;
import unbbayes.datamining.preprocessor.imbalanceddataset.ClusterBasedUndersampling;
import unbbayes.datamining.preprocessor.imbalanceddataset.Smote;

/**
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 16/02/2007
 */
public class TestFold {

	/**
	 * first: sampleID second: classifierID third and fourth: rocPoints
	 */
	private float[][][][] rocPointsProbs;

	float[][][][][] rocPointsProbsTemp;

	/**
	 * first: sampleID second: classifierID third: meand and stdDev
	 */
	private double[][][] auc;

	private double[][][] aucTemp;

	/**
	 * first: sampleID second: classifierID third: meand and stdDev
	 */
	private double[][][] globalError;

	private double[][][] globalErrorTemp;

	/**
	 * first: sampleID second: classifierID third: meand and stdDev
	 */
	private double[][][] sensitivity;

	private double[][][] sensitivityTemp;

	/**
	 * first: sampleID second: classifierID third: meand and stdDev
	 */
	private double[][][] specificity;

	private double[][][] specificityTemp;

	/**
	 * first: sampleID second: classifierID third: meand and stdDev
	 */
	private double[][][] SE;

	private double[][][] SETemp;

	/**
	 * Array for storing the confusion matrix. [trueClass][predictedClass]
	 */
	private int[][] confusionMatrix;

	private int numSamples;

	private int numClassifiers;

	private int positiveClass;

	private int negativeClass;

	private TestsetUtils testsetUtils;

	private int pos;

	private int numRoundsTotal;

	private int classIndex;

	private int counterIndex;

	public TestFold(int numFolds, int numRounds, TestsetUtils testsetUtils) {
		this.testsetUtils = testsetUtils;
		numSamples = Samples.getNumSamples();
		numClassifiers = Classifiers.getNumClassifiers();
		positiveClass = testsetUtils.getPositiveClass();
		negativeClass = testsetUtils.getNegativeClass();
		pos = 0;
		numRoundsTotal = numFolds * numRounds;

		/* Initialize arrays */
		rocPointsProbsTemp = new float[numSamples][numClassifiers][numRoundsTotal][][];
		aucTemp = new double[numSamples][numClassifiers][numRoundsTotal];
		globalErrorTemp = new double[numSamples][numClassifiers][numRoundsTotal];
		sensitivityTemp = new double[numSamples][numClassifiers][numRoundsTotal];
		specificityTemp = new double[numSamples][numClassifiers][numRoundsTotal];
		SETemp = new double[numSamples][numClassifiers][numRoundsTotal];
		confusionMatrix = new int[2][2];
	}

	public void run(InstanceSet originalTrain, InstanceSet test)
			throws Exception {
		classIndex = test.classIndex;
		counterIndex = test.counterIndex;

		/* Distribution of the original instanceSet */
		float[] originalDist = TestsetUtils.distribution(originalTrain);

		Classifier classifier;
		float[] probs;
		float[] distribution;

		int k = testsetUtils.getK();

		float learningRate = testsetUtils.getLearningRate();
		float momentum = testsetUtils.getMomentum();
		float threshold;
		
		Smote smote = testsetUtils.getSmote();
		smote.setInstanceSet(originalTrain);
		smote.buildNN(5, positiveClass);

		ClusterBasedSmote cbs;
		cbs = new ClusterBasedSmote(originalTrain, positiveClass, k);
		cbs.setOptionDiscretize(false);
		cbs.setOptionDistanceFunction((byte) 1);
		cbs.setOptionFixedGap(true);
		cbs.setOptionNominal((byte) 0);
		cbs.clusterizeByClass();

		ClusterBasedUndersampling cbu;
		cbu = new ClusterBasedUndersampling(originalTrain, positiveClass, k);
		cbu.clusterizeAll(positiveClass);

		/* Loop through all sample strategies */
		for (int sampleID = 0; sampleID < numSamples; sampleID++) {
			/* Sample train fold */
			InstanceSet train = new InstanceSet(originalTrain);

			/* Sample training instanceSet */
			testsetUtils.sample(train, sampleID, 5, originalDist, cbs, cbu);

			distribution = TestsetUtils.distribution(train);
//			threshold = distribution[positiveClass] / (distribution[0] + distribution[1]);
			threshold = -1;
			
			for (int classfID = 0; classfID < numClassifiers; classfID++) {
				classifier = Classifiers.buildClassifier(train, classfID,
						distribution, positiveClass, learningRate, momentum,
						threshold);

				probs = evaluateClassifier(classifier, test, sampleID, classfID);

				rocPointsProbsTemp[sampleID][classfID][pos] = ROCAnalysis
						.computeROCPoints(probs, test, positiveClass);
				aucTemp[sampleID][classfID][pos] = ROCAnalysis.computeAUC(
						probs, test, positiveClass) * 100;
			}
		}
		++pos;

		if (pos == numRoundsTotal) {
			runFinalizer(rocPointsProbsTemp, aucTemp);
		}
	}

	/**
	 * Evaluate the model according to specificity, sensitivity and the SE rate (
	 * specificity * sensitivity). Also returns the probability of an instance
	 * being from the positive class for each instance of the input test set.
	 * 
	 * @param classifier
	 * @param test
	 * @param sampleID
	 * @param classfID
	 * @return probs
	 * @throws Exception
	 */
	private float[] evaluateClassifier(Classifier classifier, InstanceSet test,
			int sampleID, int classfID) throws Exception {
		int numInstances;
		float[] dist;
		Instance instance;
		int numClasses;
		int actualClass;
		int predictedClass;
		float weight;
		int[] total = new int[2];
		Arrays.fill(total, 0);

		/* Zero confusion matrix */
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				confusionMatrix[i][j] = 0;
			}
		}

		/*
		 * Get probabilistic classifier's estimate that instance i is positive.
		 */
		numInstances = test.numInstances();
		numClasses = test.numClasses();
		float[] probs = new float[numInstances];
		if (classifier instanceof DistributionClassifier) {
			DistributionClassifier distClassifier;
			distClassifier = ((DistributionClassifier) classifier);

			for (int inst = 0; inst < numInstances; inst++) {
				instance = test.getInstance(inst);
				weight = (int) instance.data[counterIndex];
				actualClass = (int) instance.data[classIndex];

				dist = distClassifier.distributionForInstance(instance);
				probs[inst] = dist[positiveClass];

				predictedClass = distClassifier.classifyInstance(dist);

				confusionMatrix[actualClass][predictedClass] += weight;

				total[actualClass] += weight;
			}
		} else {
			DecisionTreeLearning treeClassifier;
			treeClassifier = ((DecisionTreeLearning) classifier);

			for (int inst = 0; inst < numInstances; inst++) {
				instance = test.getInstance(inst);
				weight = (int) instance.data[counterIndex];
				actualClass = (int) instance.data[classIndex];

				probs[inst] = treeClassifier.positiveClassProb(instance,
						positiveClass);
				
				predictedClass = classifier.classifyInstance(instance);

				confusionMatrix[actualClass][predictedClass] += weight;

				total[actualClass] += weight;
			}

			// ArrayList<Object> aux;
			// aux = ROCAnalysis.computeROCPointsDecisionTree(classifier,
			// test, positiveClass);
			// aux.add(probs);
			// aux.add(rocPoints);
		}

		int tp = confusionMatrix[positiveClass][positiveClass];
		int fp = confusionMatrix[negativeClass][positiveClass];

		/* Global error */
		double globalError = confusionMatrix[0][1] + confusionMatrix[1][0];
		globalError /= test.numWeightedInstances;

		/* Sensitivity */
		double sensitivity = (double) tp / total[positiveClass];

		/* Specificity */
		double specificity = 1 - ((double) fp / total[negativeClass]);

		/* SE */
		double SE = sensitivity * specificity;

		globalErrorTemp[sampleID][classfID][pos] = globalError * 100;
		sensitivityTemp[sampleID][classfID][pos] = sensitivity * 100;
		specificityTemp[sampleID][classfID][pos] = specificity * 100;
		SETemp[sampleID][classfID][pos] = SE * 100;

		return probs;
	}

	private void runFinalizer(float[][][][][] rocPointsProbsTemp,
			double[][][] aucTemp) {
		rocPointsProbs = new float[numSamples][numClassifiers][][];
		auc = new double[numSamples][numClassifiers][];
		globalError = new double[numSamples][numClassifiers][];
		sensitivity = new double[numSamples][numClassifiers][];
		specificity = new double[numSamples][numClassifiers][];
		SE = new double[numSamples][numClassifiers][];
		float[][] rocPoints = { { 0, 0 }, { 1, 1 } };

		for (int i = 0; i < numSamples; i++) {
			rocPointsProbs[i] = new float[numClassifiers][][];
			auc[i] = new double[numClassifiers][];
			for (int j = 0; j < numClassifiers; j++) {
				/* Average the 'numRoundsTotal' roc curves */
				// rocPointsProbs[i][j] =
				// ROCAnalysis.averageROCPoints(rocPointsProbsTemp[i][j],
				// numRoundsTotal);
				rocPointsProbs[i][j] = rocPoints;

				if (numRoundsTotal > 1) {
					/* Average the 'numFolds' aucs */
					auc[i][j] = Utils.computeMeanStdDev(aucTemp[i][j]);
					globalError[i][j] = Utils
							.computeMeanStdDev(globalErrorTemp[i][j]);
					sensitivity[i][j] = Utils
							.computeMeanStdDev(sensitivityTemp[i][j]);
					specificity[i][j] = Utils
							.computeMeanStdDev(specificityTemp[i][j]);
					SE[i][j] = Utils.computeMeanStdDev(SETemp[i][j]);
				} else {
					auc[i][j] = new double[2];
					auc[i][j][0] = aucTemp[i][j][0];
					auc[i][j][1] = 0;

					globalError[i][j] = new double[2];
					globalError[i][j][0] = globalErrorTemp[i][j][0];
					globalError[i][j][1] = 0;

					sensitivity[i][j] = new double[2];
					sensitivity[i][j][0] = sensitivityTemp[i][j][0];
					sensitivity[i][j][1] = 0;

					specificity[i][j] = new double[2];
					specificity[i][j][0] = specificityTemp[i][j][0];
					specificity[i][j][1] = 0;

					SE[i][j] = new double[2];
					SE[i][j][0] = SETemp[i][j][0];
					SE[i][j][1] = 0;
				}
			}
		}
	}

	public float[][] getRocPointsProbs(int sampleID, int classifier) {
		return rocPointsProbs[sampleID][classifier];
	}

	public double[] getAuc(int sampleID, int classifier) {
		return auc[sampleID][classifier];
	}

	public double[] getGlobalError(int sampleID, int classifier) {
		return globalError[sampleID][classifier];
	}

	public double[] getSensitivity(int sampleID, int classifier) {
		return sensitivity[sampleID][classifier];
	}

	public double[] getSpecificity(int sampleID, int classifier) {
		return specificity[sampleID][classifier];
	}

	public double[] getSE(int sampleID, int classifier) {
		return SE[sampleID][classifier];
	}

}
