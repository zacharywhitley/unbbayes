package unbbayes.datamining.evaluation;

import java.util.Date;

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
public class TestFold {

	/**
	 * first: samplingID second: classifierID third and fourth: rocPoints
	 */
	private float[][][][] rocPointsAvg;
	private float[][][][] rocPointsStdDev;
	private float[][][][][] rocPointsProbsTemp;

	/**
	 * first: samplingID second: classifierID third: meand and stdDev
	 */
	private double[][][] auc;
	private double[][][] aucTemp;

	private String[] samplingName;
	private int numSamplings;
	private int numClassifiers;

	private int positiveClass;

	/** Class of most common settings */
	private TestsetUtils testsetUtils;

	private Samplings samplings;
	private Indexes indexes;

	private int pos;
	private int numRoundsTotal;

	private int classIndex;
	private int counterIndex;
	private int samplingID;
	private boolean multiClass;
	private float[][] classDistribution;

	public TestFold(int numFolds, int numRounds, TestsetUtils testsetUtils) {
		this.testsetUtils = testsetUtils;
		this.numClassifiers = Classifiers.getNumClassifiers();
		positiveClass = testsetUtils.getPositiveClass();
		numRoundsTotal = numFolds * numRounds;
		multiClass = testsetUtils.isMultiClass();
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
		
		/* Initialize arrays and Indexes class */
		initialize(originalTrain);
		
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
			System.out.print((new java.text.SimpleDateFormat("HH:mm:ss")).format(new Date()));
			System.out.println(": samplingID: " + ssID);
			for (int k = kStart; k < kEnd; k += 3) {
				for (int ratio = ratioStart; ratio < ratioEnd; ratio++) {
					++numSamplings;
					samplingType = runAux(originalTrain, ssID, ratio, test, k);
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

		if (!multiClass && pos == numRoundsTotal) {
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
			@SuppressWarnings("unused")
			boolean stop = true;
		}
		
		/* Set sampling name */
		samplingName[samplingID] = "k = " + k + "\t";
		samplingName[samplingID] += "ratio = " + ratio * 10 + "%\t";
		samplingName[samplingID] += samplings.getSamplingName(ssID);
		
		/* Classify and evaluate sample */
		classifyEvaluate(train, test, samplingID);
		
		/* Store class distribution */
		classDistribution[samplingID] = train.getClassDistribution();
		
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
		float[] distribution = train.getClassDistribution(false);
		
		classifier = Classifiers.newClassifier(classfID);
		Classifiers.buildClassifier(train, classifier,
				distribution, testsetUtils);

		probs = evaluateClassifier(classifier, test, samplingID, classfID);

		if (!multiClass) {
			/* ROC and AUC only available for binary class datasets. Sorry! */
			rocPointsProbsTemp[samplingID][classfID][pos] = ROCAnalysis
					.computeROCPoints(probs, test, positiveClass);
	
			aucTemp[samplingID][classfID][pos] = ROCAnalysis.computeAUC(
					probs, test, positiveClass) * 100;
			
			if (aucTemp[samplingID][classfID][pos] < 50) {
				@SuppressWarnings("unused")
				boolean fudeu = true;
			}
		}
	}

	/**
	 * Evaluate model and build confusion matrix (inside Indexes class). Also
	 * compute the probability of an instance being from the positive class
	 * for each instance of the input test set.
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

		/*
		 * Get probabilistic classifier's estimate that instance i is positive.
		 */
		numInstances = test.numInstances();
		float[] probs = new float[numInstances];
		for (int inst = 0; inst < numInstances; inst++) {
			instance = test.getInstance(inst);

			dist = classifier.distributionForInstance(instance);
			probs[inst] = dist[positiveClass];

			actualClass = (int) instance.data[classIndex];
			predictedClass = Utils.maxIndex(dist);
			weight = instance.data[counterIndex];

			indexes.insert(samplingID, classfID, pos, actualClass,
					predictedClass, weight);
		}

		return probs;
	}

	private void average() {
		rocPointsAvg = new float[numSamplings][numClassifiers][][];
		rocPointsStdDev = new float[numSamplings][numClassifiers][][];
		auc = new double[numSamplings][numClassifiers][];
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
				} else {
					auc[i][j] = new double[2];
					auc[i][j][0] = aucTemp[i][j][0];
					auc[i][j][1] = 0;
				}
			}
		}
	}
	
	private void initialize(InstanceSet instanceSet) throws Exception {
		/* 
		 * The arrays and the Indexes class are only started once for each
		 * dataset. Check if the arrays has already been started.
		 */
		if (rocPointsProbsTemp == null) {
			/* Start the arrays for roc and auc */
			rocPointsProbsTemp = 
				new float[numSamplings][numClassifiers][numRoundsTotal][][];
			aucTemp = new double[numSamplings][numClassifiers][numRoundsTotal];
			samplingName = new String[numSamplings];
			
			/* Start the Indexes class */
			indexes = new Indexes(instanceSet, numSamplings, numClassifiers,
					numRoundsTotal);
			
			/* Array for storing the class distribution of each sampling */
			classDistribution = new float[numSamplings][];
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

	public Samplings getSamplings() {
		return samplings;
	}

	public String getSamplingName(int samplingID) {
		return samplingName[samplingID];
	}

	public int getNumSamplings() {
		return numSamplings;
	}

	public int getNumClassifiers() {
		return numClassifiers;
	}

	public Indexes getIndexes() {
		return indexes;
	}

	public int getClassDistribution(int samplingID, int classID) {
		return (int) classDistribution[samplingID][classID];
	}
	
}
