package unbbayes.datamining.evaluation;

import java.util.Date;
import java.util.Random;

import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.datamanipulation.InstanceSet;

public class CrossValidation implements ITrainingMode {

	protected int numFolds;

	public CrossValidation(int folds) {
		this.numFolds = folds;
	}

	public int getFolds() {
		return numFolds;
	}

	/**
	 * Performs a (stratified if class is nominal) cross-validation for a
	 * classifier on a set of instances.
	 * 
	 * @exception Exception
	 *                if a classifier could not be generated successfully or the
	 *                class is not defined
	 */
	public void crossValidateModel(InstanceSet data, Classifier classifier)
			throws Exception {
		 data.randomize(new Random(new Date().getTime()));
//		 TODO DESCOMENTAR E CORRIGIR ERRO
//		 if (data.getClassAttribute().isNominal()) {
//			 data.stratify();
//		 }
		double bestPctCorrect = 0.0;
		int bestModel = 0;
		// Do the folds
		for (int i = 0; i < numFolds; i++) {
			InstanceSet train = trainCV(data, numFolds, i);
			classifier.buildClassifier(train);
			InstanceSet test = testCV(data, numFolds, i);
			Evaluation2 eval = new Evaluation2(test);
			eval.evaluateModel(classifier);
			if (eval.pctCorrect() > bestPctCorrect) {
				bestPctCorrect = eval.pctCorrect();
				bestModel = i;
				System.out.println("best model=" + i);
			}
		}
		// train again the best model to be used
		InstanceSet train = trainCV(data, numFolds, bestModel);
		classifier.buildClassifier(train);
	}

	/**
	 * Creates the training set for one fold of a cross-validation on the
	 * dataset.
	 * 
	 * @param instances
	 *            set of training instances
	 * @param numFolds
	 *            the number of folds in the cross-validation. Must be greater
	 *            than 1.
	 * @param numFold
	 *            0 for the first fold, 1 for the second, ...
	 * @return the training set for a fold
	 * @exception IllegalArgumentException
	 *                if the number of folds is less than 2 or greater than the
	 *                number of instances.
	 */
	protected InstanceSet trainCV(InstanceSet instances, int numFolds,
			int numFold) {
		int numInstForFold, first, offset;
		int numInstances = instances.numInstances();
		InstanceSet train;

		if (numFolds < 2) {
			throw new IllegalArgumentException(
					"Number of folds must be at least 2!");
		}
		if (numFolds > numInstances) {
			throw new IllegalArgumentException(
					"Can't have more folds than instances!");
		}
		numInstForFold = numInstances / numFolds;
		if (numFold < numInstances % numFolds) {
			numInstForFold++;
			offset = numFold;
		} else {
			offset = numInstances % numFolds;
		}
		train = new InstanceSet(instances, (numInstances - numInstForFold));
		first = numFold * (numInstances / numFolds) + offset;
		
		/* 
		 * Build training set with all instances from the input instanceSet
		 * except those instances chosen to the test set: numInstForFold
		 * instances starting from the 'first' instance. 
		 */
		/* First, copy those instances before the 'first' instance */
		int start = 0;
		numInstances = first;
		instances.copyInstances(start, train, numInstances);
		
		/* Next, copy those instances after 'first + numInstForFold' */
		start = first + numInstForFold;
		numInstances = numInstances - start;
		instances.copyInstances(start, train, numInstances);

		return train;
	}

	/**
	 * Creates the test set for one fold of a cross-validation on the dataset.
	 * 
	 * @param instances
	 *            set of training instances
	 * @param numFolds
	 *            the number of folds in the cross-validation. Must be greater
	 *            than 1.
	 * @param numFold
	 *            0 for the first fold, 1 for the second, ...
	 * @return the test set of instances
	 * @exception IllegalArgumentException
	 *                if the number of folds is less than 2 or greater than the
	 *                number of instances.
	 */
	protected InstanceSet testCV(InstanceSet instances, int numFolds,
			int numFold) {
		int numInstForFold, first, offset;
		int numInstances = instances.numInstances();
		InstanceSet test;

		if (numFolds < 2) {
			throw new IllegalArgumentException(
					"Number of folds must be at least 2!");
		}
		if (numFolds > numInstances) {
			throw new IllegalArgumentException(
					"Can't have more folds than instances!");
		}
		numInstForFold = numInstances / numFolds;
		if (numFold < numInstances % numFolds) {
			numInstForFold++;
			offset = numFold;
		} else
			offset = numInstances % numFolds;
		test = new InstanceSet(instances, numInstForFold);
		first = numFold * (numInstances / numFolds) + offset;
		instances.copyInstances(first, test, numInstForFold);
		return test;
	}

}
