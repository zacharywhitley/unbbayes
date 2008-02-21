/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.datamining.evaluation.batchEvaluation;

import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Utils;
import unbbayes.datamining.evaluation.ROCAnalysis;
import unbbayes.datamining.evaluation.batchEvaluation.model.Evaluations;

/**
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 16/02/2007
 */
public class FoldEvaluation {

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
	private String[] samplingParameters;
	private int numBatchIterations;
	private int numClassifiers;

	private int positiveClass;

	private InitializePreprocessors preprocessor;
	private Indexes indexes;

	private int pos;
	private int numRoundsTotal;

	private int classIndex;
	private int counterIndex;
	private int samplingID;
	private boolean multiClass;
	private float[][] classDistribution;
	private boolean buildROC;
	private boolean computeAUC;
	private PreprocessorParameters[] preprocessors;

	public FoldEvaluation(int numFolds, int numRounds, PreprocessorParameters[] preprocessors,
			Evaluations evaluations) {
		this.preprocessors = preprocessors;
		buildROC = evaluations.isBuildROC();
		computeAUC = evaluations.isComputeAUC();
		
		this.numClassifiers = Classifiers.getNumClassifiers();
		numRoundsTotal = numFolds * numRounds;
		pos = 0;
	}
	
	public void run(InstanceSet originalTrain, InstanceSet test,
			int positiveClass)
	throws Exception {
		samplingID = 0;
		
		multiClass = originalTrain.isMultiClass();
		
		/* Create Simplesampling strategies */
		preprocessor = new InitializePreprocessors(originalTrain, preprocessors);
		
		/* Get number of preprocessors */
		numBatchIterations = preprocessor.getTotalNumBatchIterations();
		
		/* Initialize arrays and Indexes class */
		initialize(originalTrain);
		
		/* Build ROC curve */
		buildROC = true;
		
		/* Do it */
		int[] batchID;
		InstanceSet train;
		for (samplingID = 0; samplingID < numBatchIterations; samplingID++) {
			/* Get copy of original instanceSet */
			train = new InstanceSet(originalTrain);

			batchID = preprocessor.getNextBatchID();
			runAux(train, test, batchID);
		}
		
		++pos;

		if (!multiClass && pos == numRoundsTotal) {
			average();
		}
	}
	
//	public void run(InstanceSet originalTrain, InstanceSet test)
//			throws Exception {
//		samplingID = 0;
//		
//		/* Create Simplesampling strategies */
//		preprocessor = new InitializePreprocessors(originalTrain);
//		
//		/* Get number of preprocessors */
//		numBatchIterations = preprocessor.getTotalNumBatchIterations();
//		
//		/* Initialize arrays and Indexes class */
//		initialize(originalTrain);
//		
//		/* Build ROC curve */
//		buildROC = true;
//		
//		/* Do it */
//		int[] batchID;
//		InstanceSet train;
//		for (samplingID = 0; samplingID < numBatchIterations; samplingID++) {
//			/* Get copy of original instanceSet */
//			train = new InstanceSet(originalTrain);
//
//			batchID = preprocessor.getNextBatchID();
//			runAux(train, test, batchID);
//		}
//		
//		++pos;
//
//		if (!multiClass && pos == numRoundsTotal) {
//			average();
//		}
//	}
//	
//	public void runBest(InstanceSet originalTrain, InstanceSet test)
//	throws Exception {
//		int numFolds = 5;
//		Folds folds = new Folds(originalTrain, numFolds);
//		InstanceSet trainBest = folds.getTrain(0);
//		InstanceSet testBest = folds.getTest(1);
//		
//		FoldEvaluation aux = new FoldEvaluation(1, 1);
//		InstanceSet[] bestParameters;
//		bestParameters = aux.getBestParameters(trainBest, testBest);
//		
//		@SuppressWarnings("unused") boolean ema = true;
//		
//		samplingID = 0;
//		
//		/* Create Simplesampling strategies */
//		preprocessor = new InitializePreprocessors(originalTrain);
//		
//		/* Get number of preprocessors */
//		numBatchIterations = preprocessor.getNumPreprocessors();
//		
//		/* Initialize arrays and Indexes class */
//		initialize(originalTrain);
//		
//		/* Build ROC curve */
//		buildROC = true;
//		
//		/* Do it */
//		InstanceSet train;
//		for (samplingID = 0; samplingID < numBatchIterations; samplingID++) {
//			samplingName[samplingID] = preprocessor.getPreprocessorName(samplingID);
////			preprocessorStringParameters[samplingID] =
////				preprocessor.getPreprocessorStringParameters();
//			
//			train = bestParameters[samplingID];
//			
//			/* Classify and evaluate sample */
//			classifyEvaluate(train, test);
//			
//			/* Store class distribution */
//			classDistribution[samplingID] = train.getClassDistribution();
//		}
//		
//		++pos;
//
//		if (!multiClass && pos == numRoundsTotal) {
//			average();
//		}
//	}
//	
//	public void runBest2(InstanceSet originalTrain, InstanceSet test)
//	throws Exception {
//		int numFolds = 5;
//		Folds folds = new Folds(originalTrain, numFolds);
//		InstanceSet trainBest = folds.getTrain(0);
//		InstanceSet testBest = folds.getTest(0);
//		
//		FoldEvaluation aux = new FoldEvaluation(1, 1);
//		ArrayList<int[][]> bestParameters;
//		bestParameters = aux.getBestParameters2(trainBest, testBest);
//		
//		@SuppressWarnings("unused") boolean ema = true;
//		
//		samplingID = 0;
//		
//		/* Create Simplesampling strategies */
//		preprocessor = new InitializePreprocessors(originalTrain);
//		
//		/* Get number of preprocessors */
//		numBatchIterations = preprocessor.getNumPreprocessors();
//		
//		/* Initialize arrays and Indexes class */
//		initialize(originalTrain);
//		
//		/* Build ROC curve */
//		buildROC = true;
//		
//		/* Do it */
//		int[] batchID;
//		InstanceSet train;
//		for (samplingID = 0; samplingID < numBatchIterations; samplingID++) {
//			/* Get copy of original instanceSet */
//			train = new InstanceSet(originalTrain);
//
//			batchID = bestParameters.get(samplingID)[0];
//			runAux(train, test, batchID);
//
//			samplingName[samplingID] = preprocessor.getPreprocessorName();
////			preprocessorStringParameters[samplingID] =
////				preprocessor.getPreprocessorStringParameters();
//			
//			/* Classify and evaluate sample */
//			classifyEvaluate(train, test);
//			
//			/* Store class distribution */
//			classDistribution[samplingID] = train.getClassDistribution();
//		}
//		
//		++pos;
//
//		if (!multiClass && pos == numRoundsTotal) {
//			average();
//		}
//	}
//	
//	private InstanceSet[] getBestParameters(InstanceSet originalTrain, InstanceSet test)
//	throws Exception {
//		samplingID = 0;
//		
//		preprocessor = new InitializePreprocessors(originalTrain);
//		
//		/* Get number of preprocessors */
//		numPreprocessors = preprocessor.getNumPreprocessors();
//		
//		/* Get total number of all batch iterations */
//		numBatchIterations = preprocessor.getTotalNumBatchIterations();
//		
//		/* Initialize arrays and Indexes class */
//		initialize(originalTrain);
//		
//		/* Do not build ROC curve */
//		buildROC = false;
//		
//		@SuppressWarnings("unchecked")
//		ArrayList<Integer>[] preprocessorIDs = new ArrayList[numPreprocessors];
//		for (int i = 0; i < numPreprocessors; i++) {
//			preprocessorIDs[i] = new ArrayList<Integer>();
//		}
//		
//		/* Do it */
//		int[] batchID;
//		ArrayList<int[]> batchIDs = new ArrayList<int[]>();
//		InstanceSet[] bestTrainingset = new InstanceSet[numPreprocessors];
//		double[] bestAuc = new double[numPreprocessors];
//		int preprocessorID;
//		InstanceSet train;
//		for (samplingID = 0; samplingID < numBatchIterations; samplingID++) {
//			/* Get copy of original instanceSet */
//			train = new InstanceSet(originalTrain);
//
//			batchID = preprocessor.getNextBatchID();
//			runAux(train, test, batchID);
//			batchIDs.add(batchID);
//			preprocessorID = preprocessor.getPreprocessorID();
//			preprocessorIDs[preprocessorID].add(samplingID);
//			if (aucTemp[samplingID][0][pos] > bestAuc[preprocessorID]) {
//				bestAuc[preprocessorID] = aucTemp[samplingID][0][pos];
//				bestTrainingset[preprocessorID] = train;
//			}
//		}
//		
//		return bestTrainingset;
//	}
//
//	private ArrayList<int[][]> getBestParameters2(InstanceSet originalTrain, InstanceSet test)
//	throws Exception {
//		samplingID = 0;
//		
//		preprocessor = new InitializePreprocessors(originalTrain);
//		
//		/* Get number of preprocessors */
//		numPreprocessors = preprocessor.getNumPreprocessors();
//		
//		/* Get total number of all batch iterations */
//		numBatchIterations = preprocessor.getTotalNumBatchIterations();
//		
//		/* Initialize arrays and Indexes class */
//		initialize(originalTrain);
//		
//		/* Do not build ROC curve */
//		buildROC = false;
//		
//		@SuppressWarnings("unchecked")
//		ArrayList<Integer>[] preprocessorIDs = new ArrayList[numPreprocessors];
//		for (int i = 0; i < numPreprocessors; i++) {
//			preprocessorIDs[i] = new ArrayList<Integer>();
//		}
//		
//		/* Do it */
//		int[] batchID;
//		ArrayList<int[]> batchIDs = new ArrayList<int[]>();
//		InstanceSet train;
//		int preprocessorID;
//		for (samplingID = 0; samplingID < numBatchIterations; samplingID++) {
//			/* Get copy of original instanceSet */
//			train = new InstanceSet(originalTrain);
//
//			batchID = preprocessor.getNextBatchID();
//			runAux(train, test, batchID);
//			batchIDs.add(batchID);
//			preprocessorID = preprocessor.getPreprocessorID();
//			preprocessorIDs[preprocessorID].add(samplingID);
//		}
//		
//		/* Choose best values for each preprocessor */
//		@SuppressWarnings("unchecked")
//		ArrayList<int[][]> bestPreprocessorBatchID = new ArrayList();
//		ArrayList<Integer> aux;
//		int[][] best;
//		for (int i = 0; i < numPreprocessors; i++) {
//			aux = preprocessorIDs[i];
//			best = getBestPreprocessorBatchID(aux.toArray(), batchIDs);
//			bestPreprocessorBatchID.add(best);
//		}
//		
//		return bestPreprocessorBatchID;
//	}
//
//	private int[][] getBestPreprocessorBatchID(Object[] samplingIDs, ArrayList<int[]> batchID) {
//		int numSamplingIDs = samplingIDs.length;
//		int samplingID;
//		int[][] best;
//		double bestAUC;
//		double currentAUC;
//		
//		best = new int[numClassifiers][];
//		for (int classfID = 0; classfID < numClassifiers; classfID++) {
//			samplingID = (Integer) samplingIDs[0];
//			bestAUC = aucTemp[samplingID][classfID][pos];
//			best[classfID] = batchID.get(samplingID);
//			for (int i = 1; i < numSamplingIDs; i++) {
//				samplingID = (Integer) samplingIDs[i];
//				currentAUC = aucTemp[samplingID][classfID][pos];
//				if (currentAUC > bestAUC) {
//					bestAUC = currentAUC;
//					best[classfID] = batchID.get(samplingID);
//				}
//			}
//		}
//		
//		return best;
//	}

	public int runAux(InstanceSet train, InstanceSet test, int[] batchID)
	throws Exception {
		/* Sample training instanceSet */
		int preprocessorID = 0;
		try {
			preprocessor.applyPreprocessor(train, batchID);
		} catch (Exception e) {
			/* Do nothing. Just continue! */
			e.printStackTrace();
			@SuppressWarnings("unused")
			boolean pau = true;
		}
		
		samplingName[samplingID] = preprocessor.getPreprocessorName();
		samplingParameters[samplingID] = preprocessor.getPreprocessorParameters();
		
		/* Classify and evaluate sample */
		classifyEvaluate(train, test);
		
		/* Store class distribution */
		classDistribution[samplingID] = train.getClassDistribution();
		
		return preprocessorID;
	}

	private void classifyEvaluate(InstanceSet train, InstanceSet test)
	throws Exception {
		float[] probs;
		for (int classfID = 0; classfID < numClassifiers; classfID++) {
			probs = classifyEvaluate(train, test, classfID);

			if (!multiClass) {
				/* ROC and AUC only available for binary class datasets. Sorry! */
				if (buildROC) {
					rocPointsProbsTemp[samplingID][classfID][pos] = ROCAnalysis
							.computeROCPoints(probs, test, positiveClass);
				}
		
				if (computeAUC) {
					aucTemp[samplingID][classfID][pos] = ROCAnalysis.computeAUC(
							probs, test, positiveClass) * 100;
				}
			}
		}
	}
	
	private float[] classifyEvaluate(InstanceSet train, InstanceSet test,
			int classfID)
	throws Exception {
		Classifier classifier;
		float[] distribution = train.getClassDistribution(false);
		
		classifier = Classifiers.newClassifier(classfID);
		Classifiers.buildClassifier(train, classifier, distribution,
				positiveClass);

		return evaluateClassifier(classifier, test, samplingID, classfID);
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
		rocPointsAvg = new float[numBatchIterations][numClassifiers][][];
		rocPointsStdDev = new float[numBatchIterations][numClassifiers][][];
		auc = new double[numBatchIterations][numClassifiers][];
		float[][][] rocPointsAux;
		
		for (int i = 0; i < numBatchIterations; i++) {
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
				new float[numBatchIterations][numClassifiers][numRoundsTotal][][];
			aucTemp = new double[numBatchIterations][numClassifiers][numRoundsTotal];
			samplingName = new String[numBatchIterations];
			samplingParameters = new String[numBatchIterations];
			
			/* Start the Indexes class */
			indexes = new Indexes(instanceSet, numBatchIterations, numClassifiers,
					numRoundsTotal);
			
			/* Array for storing the class distribution of each sampling */
			classDistribution = new float[numBatchIterations][];
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

	public InitializePreprocessors getSamplings() {
		return preprocessor;
	}

	public String getSamplingName(int samplingID) {
		return samplingName[samplingID];
	}

	public String getSamplingParameters(int samplingID) {
		return samplingParameters[samplingID];
	}

	public int getNumBatchIterations() {
		return numBatchIterations;
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
