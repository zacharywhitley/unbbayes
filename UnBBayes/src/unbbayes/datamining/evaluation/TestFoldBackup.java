package unbbayes.datamining.evaluation;

import java.util.Arrays;

import unbbayes.TestsetUtils;
import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Utils;

/**
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 16/02/2007
 */
public class TestFoldBackup {

	/**
	 * first: samplingID second: classifierID third and fourth: rocPoints
	 */
	private float[][][][] rocPointsAvg;
	private float[][][][] rocPointsStdDev;

	float[][][][][] rocPointsProbsTemp;

	/**
	 * first: samplingID second: classifierID third: meand and stdDev
	 */
	private double[][][] auc;

	private double[][][] aucTemp;

	/**
	 * first: samplingID second: classifierID third: meand and stdDev
	 */
	private double[][][] globalError;

	private double[][][] globalErrorTemp;

	/**
	 * first: samplingID second: classifierID third: meand and stdDev
	 */
	private double[][][] sensitivity;

	private double[][][] sensitivityTemp;

	/**
	 * first: samplingID second: classifierID third: meand and stdDev
	 */
	private double[][][] specificity;

	private double[][][] specificityTemp;

	/**
	 * first: samplingID second: classifierID third: meand and stdDev
	 */
	private double[][][] SE;

	private double[][][] SETemp;
	
	private String[] samplingName;

	/**
	 * Array for storing the confusion matrix. [trueClass][predictedClass]
	 */
	private int[][] confusionMatrix;

	private int numSamplings;

	private int numClassifiers;

	private int positiveClass;

	private int negativeClass;

	/** Class of most common settings */
	private TestsetUtils testsetUtils;

	private int pos;

	private int numRoundsTotal;

	private int classIndex;

	private int counterIndex;

	private Samplings samplings;

	private int samplingID;
	
	private int numClasses;

	public TestFoldBackup(int numFolds, int numRounds, TestsetUtils testsetUtils) {
		this.testsetUtils = testsetUtils;
		this.numClassifiers = Classifiers.getNumClassifiers();
		positiveClass = testsetUtils.getPositiveClass();
		negativeClass = testsetUtils.getNegativeClass();
		numRoundsTotal = numFolds * numRounds;
		pos = 0;
	}
	
	public void run(InstanceSet originalTrain, InstanceSet test)
			throws Exception {
		/* Distribution of the original instanceSet */
		float[] originalDist = originalTrain.getClassDistribution(false);

		/* Create Sampling strategies */
		samplings = new Samplings(originalTrain, originalDist, testsetUtils);
		
		/* Initialize ratios */
		int ratioStart = testsetUtils.getRatioStart();
		int ratioEnd = testsetUtils.getRatioEnd();
		int numRatios = ratioEnd - ratioStart;
		
		/* Initialize k's */
		int kStart = testsetUtils.getKstart();
		int kEnd = testsetUtils.getKend();
		int numKs = kEnd - kStart;
		
		/* Set number of samplings */
		int numSamplingStrategies = samplings.getNumSamplings();
		numSamplings = numSamplingStrategies * numRatios * numKs;
		
		numClasses = originalTrain.numClasses();
			
		initializeArrays();
		
		/*
		 * 0: no cluster and no ratio;
		 * 1: no cluster;
		 * 2: cluster and ratio
		 */
		int samplingType = 0;

		/* Sampling strategies with ratio and with clusters */
		samplingID = 0;
		numSamplings = 0;
		for (int ssID = 0; ssID < numSamplingStrategies; ssID++) {
			for (int k = kStart; k < kEnd; k += 1) {
				for (int ratio = ratioStart; ratio < ratioEnd; ratio++) {
					samplingType = runAux(originalTrain, ssID, ratio, test, k);
					++numSamplings;
					if (samplingType == 0) {
						break;
					}
				}
				if (samplingType == 0 || samplingType == 1) {
					break;
				}
			}
		}
		++pos;

		if (pos == numRoundsTotal) {
			average();
		}
	}
	
	public int runAux(InstanceSet originalTrain, int ssID, int ratio,
			InstanceSet test, int k)
	throws Exception {
		int samplingType = -1;
		
		/* Get copy of original instanceSet */
		InstanceSet train;
		train = new InstanceSet(originalTrain);

		/* Sample training instanceSet */
		testsetUtils.setK(k);
		try {
			samplingType = samplings.buildSample(train, ssID, ratio);
		} catch (Exception e) {
			/* Do nothing. Just continue! */
			boolean stop = true;
		}
		
		/* Set sampling name */
		samplingName[samplingID] = "k = " + k + "\t";
		samplingName[samplingID] += "ratio = " + ratio * 10 + "%\t";
		samplingName[samplingID] += samplings.getSamplingName(ssID);
		
		/* Classify and evaluate sample */
		classifyEvaluate(train, test, samplingID);
		
		++samplingID;

		return samplingType;
	}

	public void classifyEvaluate(InstanceSet train, InstanceSet test,
			int samplingID)
	throws Exception {
		for (int classfID = 0; classfID < numClassifiers; classfID++) {
			classifyEvaluate(train, test, samplingID, classfID);
		}
	}
	
	public void classifyEvaluate(InstanceSet train, InstanceSet test,
			int samplingID, int classfID)
	throws Exception {
		Classifier classifier;
		float[] probs;
		float learningRate = testsetUtils.getLearningRate();
		float momentum = testsetUtils.getMomentum();
		float[] distribution = train.getClassDistribution(false);
		
		classifier = Classifiers.newClassifier(classfID);
		Classifiers.buildClassifier(train, classifier,
				distribution, testsetUtils);

		probs = evaluateClassifier(classifier, test, samplingID, classfID);

		rocPointsProbsTemp[samplingID][classfID][pos] = ROCAnalysis
				.computeROCPoints(probs, test, positiveClass);

		aucTemp[samplingID][classfID][pos] = ROCAnalysis.computeAUC(
				probs, test, positiveClass) * 100;
		
		if (aucTemp[samplingID][classfID][pos] < 50) {
			@SuppressWarnings("unused")
			boolean fudeu = true;
		}
	}

	public void classifyEvaluateForMisclassify(InstanceSet train,
			InstanceSet test, int samplingID, int classfID)
	throws Exception {
		Classifier classifier;
		float[] probs;
		float[] distribution = train.getClassDistribution(false);
		
		classifier = Classifiers.newClassifier(classfID);
		Classifiers.buildClassifier(train, classifier,
				distribution, testsetUtils);

		probs = evaluateClassifier(classifier, test, samplingID, 0);

		rocPointsProbsTemp[samplingID][0][pos] = ROCAnalysis
				.computeROCPoints(probs, test, positiveClass);
		aucTemp[samplingID][0][pos] = ROCAnalysis.computeAUC(
				probs, test, positiveClass) * 100;
		
		if (aucTemp[samplingID][0][pos] < 50) {
			@SuppressWarnings("unused")
			boolean fudeu = true;
		}
	}

	/**
	 * Evaluate the model according to specificity, sensitivity and the SE rate (
	 * specificity * sensitivity). Also returns the probability of an instance
	 * being from the positive class for each instance of the input test set.
	 * 
	 * @param classifier
	 * @param test
	 * @param samplingID
	 * @param classfID
	 * @return probs
	 * @throws Exception
	 */
	private float[] evaluateClassifier(Classifier classifier, InstanceSet test,
			int samplingID, int classfID) throws Exception {
		classIndex = test.classIndex;
		counterIndex = test.counterIndex;
		int numInstances;
		float[] dist;
		Instance instance;
		int actualClass;
		int predictedClass;
		float weight;
		int[] total = new int[numClasses];
		Arrays.fill(total, 0);

//		/* Initiate the confusion matrix */
//		for (int i = 0; i < numClasses; i++) {
//			for (int j = 0; j < numClasses; j++) {
//				confusionMatrix[i][j] = 0;
//			}
//		}

		/*
		 * Get probabilistic classifier's estimate that instance i is positive.
		 */
		numInstances = test.numInstances();
		float[] probs = new float[numInstances];
		for (int inst = 0; inst < numInstances; inst++) {
			instance = test.getInstance(inst);
			weight = (int) instance.data[counterIndex];
			actualClass = (int) instance.data[classIndex];

			dist = classifier.distributionForInstance(instance);
			probs[inst] = dist[positiveClass];

			predictedClass = Utils.maxIndex(dist);

			confusionMatrix[actualClass][predictedClass] += weight;

			total[actualClass] += weight;
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

		if (SE < 50) {
			@SuppressWarnings("unused")
			boolean fudeu = true;
		}
		
		globalErrorTemp[samplingID][classfID][pos] = globalError * 100;
		sensitivityTemp[samplingID][classfID][pos] = sensitivity * 100;
		specificityTemp[samplingID][classfID][pos] = specificity * 100;
		SETemp[samplingID][classfID][pos] = SE * 100;

		return probs;
	}

	private void average() {
		rocPointsAvg = new float[numSamplings][numClassifiers][][];
		rocPointsStdDev = new float[numSamplings][numClassifiers][][];
		auc = new double[numSamplings][numClassifiers][];
		globalError = new double[numSamplings][numClassifiers][];
		sensitivity = new double[numSamplings][numClassifiers][];
		specificity = new double[numSamplings][numClassifiers][];
		SE = new double[numSamplings][numClassifiers][];
		float[][][] rocPointsAux;
		
		for (int i = 0; i < numSamplings; i++) {
			rocPointsAvg[i] = new float[numClassifiers][][];
			rocPointsStdDev[i] = new float[numClassifiers][][];
			auc[i] = new double[numClassifiers][];
			for (int j = 0; j < numClassifiers; j++) {
				/* Average the 'numRoundsTotal' roc curves */
				rocPointsAux = 
					ROCAnalysis.averageROCPoints(rocPointsProbsTemp[i][j]);
				rocPointsAvg[i][j] = rocPointsAux[0];
				rocPointsStdDev[i][j] = rocPointsAux[1];

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

	private void initializeArrays() {
		if (rocPointsProbsTemp == null) {
			rocPointsProbsTemp = new float[numSamplings][numClassifiers][numRoundsTotal][][];
			aucTemp = new double[numSamplings][numClassifiers][numRoundsTotal];
			globalErrorTemp = new double[numSamplings][numClassifiers][numRoundsTotal];
			sensitivityTemp = new double[numSamplings][numClassifiers][numRoundsTotal];
			specificityTemp = new double[numSamplings][numClassifiers][numRoundsTotal];
			SETemp = new double[numSamplings][numClassifiers][numRoundsTotal];
			samplingName = new String[numSamplings];
			confusionMatrix = new int[numClasses][numClasses];
		}
	}

	public float[][] getRocPointsAvg(int samplingID, int classifier) {
		return rocPointsAvg[samplingID][classifier];
	}

	public float[][] getRocPointsStdDev(int samplingID, int classifier) {
		return rocPointsStdDev[samplingID][classifier];
	}

	public double[] getAuc(int samplingID, int classifier) {
		return auc[samplingID][classifier];
	}

	public double[] getAucCrude(int samplingID, int classifier) {
		return aucTemp[samplingID][classifier];
	}

	public double[] getGlobalError(int samplingID, int classifier) {
		return globalError[samplingID][classifier];
	}

	public double[] getSensitivity(int samplingID, int classifier) {
		return sensitivity[samplingID][classifier];
	}

	public double[] getSpecificity(int samplingID, int classifier) {
		return specificity[samplingID][classifier];
	}

	public double[] getSE(int samplingID, int classifier) {
		return SE[samplingID][classifier];
	}

	public Samplings getSamplings() {
		return samplings;
	}

	public String getSamplingName(int samplingID) {
		return samplingName[samplingID];
	}

	public int getNumSamplings() {
		return numSamplings;
	}

}
