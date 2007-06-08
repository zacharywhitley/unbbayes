package unbbayes.datamining.evaluation;

import java.util.ResourceBundle;

import unbbayes.TestsetUtils;
import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.datamanipulation.InstanceSet;

public class CrossValidation implements ITrainingMode {
	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("" +
			"unbbayes.datamining.classifiers.resources.ClassifiersResource");

	/**
	 * Performs a (stratified if class is nominal) cross-validation for a
	 * classifier on a set of instances.
	 * 
	 * @exception Exception
	 *                if a classifier could not be generated successfully or the
	 *                class is not defined
	 */
	public static void crossValidateModel(InstanceSet instanceSet,
			Classifier classifier, int numFolds) throws Exception {
		Evaluation evaluation = new Evaluation(instanceSet);

		/* Do the folds */
		Folds folds = new Folds(instanceSet, numFolds);
		InstanceSet train;
		InstanceSet test;
		for (int fold = 0; fold < numFolds; fold++) {
			train = folds.getTrain(fold);
			classifier.buildClassifier(train);
			test = folds.getTest(fold);
			evaluation.evaluateModel(classifier, test);
		}
	}

	/**
	 * Performs a (stratified if class is nominal) cross-validation for a
	 * classifier on a set of instances and returns its average roc points.
	 * 
	 * @exception Exception
	 *                if a classifier could not be generated successfully or the
	 *                class is not defined
	 */
	public static float[] getEvaluatedProbabilities(Classifier classifier,
			InstanceSet instanceSet, int positiveClass, int numFolds)
	throws Exception {
		/* Do the folds */
		Folds folds = new Folds(instanceSet, numFolds);
		int numInstances = instanceSet.numInstances;
		float[] probs = new float[numInstances];
		float[] dist;
		int inst = 0;
		InstanceSet train;
		InstanceSet test;
		for (int fold = 0; fold < numFolds; fold++) {
			train = folds.getTrain(fold);
			classifier.buildClassifier(train);
			test = folds.getTest(fold);
			
			/* 
			 * Get probabilistic classifier's estimate that instance i is
			 * positive.
			 */
			numInstances = test.numInstances();
			for (int i = 0; i < numInstances; i++) {
				dist = ((DistributionClassifier) classifier)
					.distributionForInstance(test.getInstance(i));
				probs[inst] = dist[positiveClass];
				++inst;
			}
		}
		
		return probs;
	}

	/**
	 * Performs a (stratified if class is nominal) cross-validation for a
	 * classifier on a set of instances and returns its average roc points.
	 * @param samplings TODO
	 * @exception Exception
	 *                if a classifier could not be generated successfully or the
	 *                class is not defined
	 */
	public static TestFold getEvaluatedProbabilities(InstanceSet instanceSet,
			int numFolds, int numRounds, TestsetUtils testsetUtils)
	throws Exception {
		/* Check if the instanceSet is compacted */
		if (instanceSet.isCompacted()) {
			throw new IllegalArgumentException("cross validation only " +
					"works with non compacted instanceSet!");
		}
		
		InstanceSet train;
		InstanceSet test;
		TestFold testFold = new TestFold(numFolds, numRounds, testsetUtils);
		
		for (int round = 0; round < numRounds; round++) {
			Folds folds = new Folds(instanceSet, numFolds);
			for (int fold = 0; fold < numFolds; fold++) {
				train = folds.getTrain(fold);
				test = folds.getTest(fold);
				testFold.run(train, test);
			}
		}
		
		return testFold;
	}

	/**
	 * Performs a (stratified if class is nominal) cross-validation for a
	 * classifier on a set of instances and returns its average roc points.
	 * @param samplings TODO
	 * 
	 * @exception Exception
	 *                if a classifier could not be generated successfully or the
	 *                class is not defined
	 */
	public static TestFold getEvaluatedProbabilities(InstanceSet train,
			InstanceSet test, int numRounds, TestsetUtils testsetUtils,
			Samplings samplings)
	throws Exception {
		TestFold testFold = new TestFold(1, numRounds, testsetUtils);
		
		for (int round = 0; round < numRounds; round++) {
			testFold.run(train, test);
		}
		
		return testFold;
	}

}
