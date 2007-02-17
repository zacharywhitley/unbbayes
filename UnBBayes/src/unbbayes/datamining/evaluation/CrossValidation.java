package unbbayes.datamining.evaluation;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.ResourceBundle;

import unbbayes.TestsetUtils;
import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.classifiers.decisiontree.DecisionTreeLearning;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Utils;

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
		/* Check if the instanceSet is compacted */
		if (instanceSet.isCompacted()) {
			throw new IllegalArgumentException("cross validation only " +
					"works with non compacted instanceSet!");
		}
		
		if (instanceSet.getClassAttribute().isNominal()) {
			instanceSet.stratify(numFolds);
		} else {
			instanceSet.randomize(new Random(new Date().getTime()));
		}
		
		Evaluation eval = new Evaluation(instanceSet);

		/* Do the folds */
		for (int i = 0; i < numFolds; i++) {
			InstanceSet train = trainCV(instanceSet, numFolds, i);
			classifier.buildClassifier(train);
			InstanceSet test = testCV(instanceSet, numFolds, i);
			eval.evaluateModel(classifier, test);
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
		/* Check if the instanceSet is compacted */
		if (instanceSet.isCompacted()) {
			throw new IllegalArgumentException("cross validation only " +
					"works with non compacted instanceSet!");
		}
		
		if (instanceSet.getClassAttribute().isNominal()) {
			instanceSet.stratify(numFolds);
		} else {
			instanceSet.randomize(new Random(new Date().getTime()));
		}
		
		/* Do the folds */
		int numInstances = instanceSet.numInstances;
		float[] probs = new float[numInstances];
		float[] dist;
		int inst = 0;
		for (int fold = 0; fold < numFolds; fold++) {
			InstanceSet train = trainCV(instanceSet, numFolds, fold);
			classifier.buildClassifier(train);
			InstanceSet test = testCV(instanceSet, numFolds, fold);
			
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
	 * 
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
		
		InstanceSet train = null;
		InstanceSet test;

		TestFold testFold = new TestFold(numFolds, numRounds, testsetUtils);
		
		for (int round = 0; round < numRounds; round++) {
			/* Randomize and stratify the instanceSet */
			if (instanceSet.classIsNominal()) {
				instanceSet.stratify(numFolds);
			} else {
				instanceSet.randomize(new Random(new Date().getTime()));
			}
			
			/* Compute rocPoints and auc */
			for (int fold = 0; fold < numFolds; fold++) {
				train = trainCV(instanceSet, numFolds, fold);
				test = testCV(instanceSet, numFolds, fold);

				testFold.run(train, test);
			}
		}
		
		return testFold;
	}

	/**
	 * Performs a (stratified if class is nominal) cross-validation for a
	 * classifier on a set of instances and returns its average roc points.
	 * 
	 * @exception Exception
	 *                if a classifier could not be generated successfully or the
	 *                class is not defined
	 */
	public static TestFold getEvaluatedProbabilities(InstanceSet train,
			InstanceSet test, int numRounds, TestsetUtils testsetUtils)
	throws Exception {
		TestFold testFold = new TestFold(1, numRounds, testsetUtils);
		
		for (int round = 0; round < numRounds; round++) {
			testFold.run(train, test);
		}
		
		return testFold;
	}

	/**
	 * Creates the training set for one fold of a cross-validation
	 * on the dataset.
	 *
	 * @param instances set of training instances
	 * @param numFolds the number of folds in the cross-validation. Must
	 * be greater than 1.
	 * @param currentFold 0 for the first fold, 1 for the second, ...
	 * @return the training set for a fold
	 * @exception IllegalArgumentException if the number of folds is less than
	 * 2 or greater than the number of instances.
	 */
	public static InstanceSet trainCV(InstanceSet instanceSet, int numFolds,
			int currentFold) {
		int numInstPerFold, first, offset;
		int numInstances = instanceSet.numInstances();
		InstanceSet train;
	
		if (numFolds < 2) {
			throw new IllegalArgumentException(resource.getString("folds2"));
		}
		if (numFolds > numInstances) {
			throw new IllegalArgumentException(resource.getString("moreFolds"));
		}
		
	    numInstPerFold = numInstances / numFolds;
		if (currentFold < numInstances % numFolds) {
			numInstPerFold++;
			offset = currentFold;
		} else {
			offset = numInstances % numFolds;
		}
		
		int trainSize = numInstances - numInstPerFold;
		train = new InstanceSet(instanceSet, trainSize);
		
	    first = currentFold * (numInstances / numFolds) + offset;
		
		instanceSet.copyInstancesTo(train, 0, first);
		instanceSet.copyInstancesTo(train, first + numInstPerFold,
				numInstances - first - numInstPerFold);
	
		return train;
	}

	/**
	 * Creates the test set for one fold of a cross-validation on
	 * the dataset.
	 *
	 * @param instances set of training instances
	 * @param numFolds the number of folds in the cross-validation. Must
	 * be greater than 1.
	 * @param currentFold 0 for the first fold, 1 for the second, ...
	 * @return the test set of instances
	 * @exception IllegalArgumentException if the number of folds is less than
	 * 2 or greater than the number of instances.
	 */
	public static InstanceSet testCV(InstanceSet instanceSet, int numFolds,
			int currentFold) {
		int numInstPerFold, first, offset;
		int numInstances = instanceSet.numInstances();
		InstanceSet test;
	
		if (numFolds < 2) {
			throw new IllegalArgumentException(resource.getString("folds2"));
		}
		if (numFolds > numInstances) {
			throw new IllegalArgumentException(resource.getString("moreFolds"));
		}
		
		numInstPerFold = numInstances / numFolds;
		if (currentFold < numInstances % numFolds) {
			numInstPerFold++;
			offset = currentFold;
		} else {
			offset = numInstances % numFolds;
		}
		
		test = new InstanceSet(instanceSet, numInstPerFold);
		
		first = currentFold * (numInstances / numFolds) + offset;
		instanceSet.copyInstancesTo(test, first, numInstPerFold);
		
		return test;
	}

}
