package unbbayes.datamining.evaluation;

import unbbayes.TestsetUtils;
import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.classifiers.decisiontree.DecisionTreeLearning;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Utils;
import unbbayes.datamining.preprocessor.imbalanceddataset.ClusterBasedSmote;
import unbbayes.datamining.preprocessor.imbalanceddataset.ClusterBasedUndersampling;
import unbbayes.datamining.preprocessor.imbalanceddataset.ClusterBasedUtils;
import unbbayes.datamining.preprocessor.imbalanceddataset.Smote;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 16/02/2007
 */
public class TestFold {
	
	/**
	 * first: sampleID
	 * second: classifierID
	 * third and fourth: rocPoints
	 */
	private float[][][][] rocPointsProbs;
	float[][][][][] rocPointsProbsTemp;
	
	/**
	 * first: sampleID
	 * second: classifierID
	 * third: meand and stdDev
	 */
	private double[][][] auc;
	double[][][] aucTemp;
	
	/**
	 * first: sampleID
	 * second: classifierID
	 * third: meand and stdDev
	 */
	private double[][][] sensitivity;
	double[][][] sensitivityTemp;
	
	/**
	 * first: sampleID
	 * second: classifierID
	 * third: meand and stdDev
	 */
	private double[][][] specificity;
	double[][][] specificityTemp;
	
	/**
	 * first: sampleID
	 * second: classifierID
	 * third: meand and stdDev
	 */
	private double[][][] SE;
	double[][][] SETemp;
	
	private int numSamples;
	
	private int numClassifiers;
	
	private int positiveClass;
	
	private TestsetUtils testsetUtils;
	
	private int pos;
	
	private int numRoundsTotal;
	
	public TestFold(int numFolds, int numRounds, TestsetUtils testsetUtils) {
		this.testsetUtils = testsetUtils;
		numSamples = Samples.getNumSamples();
		numClassifiers = Classifiers.getNumClassifiers();
		positiveClass = testsetUtils.getPositiveClass();
		pos = 0;
		numRoundsTotal = numFolds * numRounds;
		
		/* Initialize arrays */
		rocPointsProbsTemp = new float[numSamples][numClassifiers][numRoundsTotal][][];
		aucTemp = new double[numSamples][numClassifiers][numRoundsTotal];
		sensitivityTemp = new double[numSamples][numClassifiers][numRoundsTotal];
		specificityTemp = new double[numSamples][numClassifiers][numRoundsTotal];
		SETemp = new double[numSamples][numClassifiers][numRoundsTotal];
	}
	
	public void run(InstanceSet originalTrain, InstanceSet test)
	throws Exception {
		/* Distribution of the original instanceSet */
		float[] originalDist = TestsetUtils.distribution(originalTrain);
		
		Classifier classifier;
		float[] probs;
		float[] distribution;
		
		Smote smote = testsetUtils.getSmote();
		smote.setInstanceSet(originalTrain);
		smote.buildNN(5, positiveClass);
		
		int k = testsetUtils.getK();
		
		ClusterBasedSmote cbs;
		cbs = new ClusterBasedSmote(originalTrain, positiveClass, k);
		cbs.setOptionDiscretize(false);
		cbs.setOptionDistanceFunction((byte) 1);
		cbs.setOptionFixedGap(true);
		cbs.setOptionNominal((byte) 0);
		cbs.clusterizeByClass();
		
		ClusterBasedUndersampling cbu;
		cbu = new ClusterBasedUndersampling(originalTrain, positiveClass, k);
		cbu.clusterize();
		
		/* Loop through all sample strategies */
		for (int sampleID = 0; sampleID < numSamples; sampleID++) {
			/* Sample train fold */
			InstanceSet train = new InstanceSet(originalTrain);
			
			/* Sample training instanceSet */
			testsetUtils.sample(train, sampleID, 5, originalDist,
					cbs, cbu);
			
			distribution = TestsetUtils.distribution(train);

			for (int i = 0; i < numClassifiers; i++) {
				classifier = Classifiers.buildClassifier(train, i, distribution);
				
				probs = evaluateClassifier(classifier, test);
				
				rocPointsProbsTemp[sampleID][i][pos] = 
					ROCAnalysis.computeROCPoints(probs, test, positiveClass);
				aucTemp[sampleID][i][pos] =
					ROCAnalysis.computeAUC(probs, test, positiveClass) * 100;
				
				/* Evaluate the model with accuracy */
				Evaluation eval = new Evaluation(test, classifier);
				eval.evaluateModel(classifier, test);
				sensitivityTemp[sampleID][i][pos] = eval.getSensitivity(1) * 100;
				specificityTemp[sampleID][i][pos] = eval.getSpecificity(1) * 100;
				SETemp[sampleID][i][pos] = sensitivityTemp[sampleID][i][pos];
				SETemp[sampleID][i][pos] *= specificityTemp[sampleID][i][pos];
				SETemp[sampleID][i][pos] /= 100;
			}
		}
		++pos;
		
		if (pos == numRoundsTotal) {
			runFinalizer(rocPointsProbsTemp, aucTemp);
		}
	}

	private float[] evaluateClassifier(Classifier classifier, InstanceSet test)
	throws Exception {
		int numInstances;
		float[] dist;
		Instance instance;
		int numClasses;
		
		/* 
		 * Get probabilistic classifier's estimate that instance i is
		 * positive.
		 */
		numInstances = test.numInstances();
		numClasses = test.numClasses();
		float[] probs = new float[numInstances];
		if (classifier instanceof DistributionClassifier) {
			DistributionClassifier distClassifier;
			distClassifier = ((DistributionClassifier) classifier);
			
			for (int inst = 0; inst < numInstances; inst++) {
				instance = test.getInstance(inst);
				dist = distClassifier.distributionForInstance(instance);
				probs[inst] = dist[positiveClass];
			}
		} else {
			DecisionTreeLearning treeClassifier;
			treeClassifier = ((DecisionTreeLearning) classifier);
			
			for (int inst = 0; inst < numInstances; inst++) {
				instance = test.getInstance(inst);
				probs[inst] = treeClassifier.positiveClassProb(instance,
						positiveClass, numClasses);
			}
			
//			ArrayList<Object> aux;
//			aux = ROCAnalysis.computeROCPointsDecisionTree(classifier,
//					test, positiveClass);
//			aux.add(probs);
//			aux.add(rocPoints);
		}
		return probs;
	}

	private void runFinalizer(float[][][][][] rocPointsProbsTemp,
			double[][][] aucTemp) {
		rocPointsProbs = new float[numSamples][numClassifiers][][];
		auc = new double[numSamples][numClassifiers][];
		sensitivity = new double[numSamples][numClassifiers][];
		specificity = new double[numSamples][numClassifiers][];
		SE = new double[numSamples][numClassifiers][];
		float[][] rocPoints = {{0,0},{1,1}};
		float[] aux;
		
		for (int i = 0; i < numSamples; i++) {
			rocPointsProbs[i] = new float[numClassifiers][][];
			auc[i] = new double[numClassifiers][];
			for (int j = 0; j < numClassifiers; j++) {
				/* Average the 'numRoundsTotal' roc curves */
//				rocPointsProbs[i][j] = 
//					ROCAnalysis.averageROCPoints(rocPointsProbsTemp[i][j],
//						numRoundsTotal);
				rocPointsProbs[i][j] = rocPoints;

				if (numRoundsTotal > 1) {
					/* Average the 'numFolds' aucs */
					auc[i][j] = Utils.computeMeanStdDev(aucTemp[i][j]);
					sensitivity[i][j] = Utils.computeMeanStdDev(sensitivityTemp[i][j]);
					specificity[i][j] = Utils.computeMeanStdDev(specificityTemp[i][j]);
					SE[i][j] = Utils.computeMeanStdDev(SETemp[i][j]);
				} else {
					auc[i][j] = new double[2];
					auc[i][j][0] = aucTemp[i][j][0];
					auc[i][j][1] = 0;
					
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

