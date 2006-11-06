package unbbayes.datamining.evaluation;

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
		// TODO DESCOMENTAR E CORRIGIR ERRO
		// data.randomize(data.getRandomNumberGenerator((new
		// Date()).getTime()));
		// if (data.getClassAttribute().isNominal()) {
		// data.stratify();
		// }
		double bestPctCorrect = 0.0;
		int bestModel = 0;
		// Do the folds
		for (int i = 0; i < numFolds; i++) {
			InstanceSet train = trainCV(data, numFolds, i);
			classifier.buildClassifier(train);
			InstanceSet test = testCV(data, numFolds, i);
			Evaluation eval = new Evaluation(test);
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
		// TODO DESCOMENTAR E CORRIGIR ERRO
		// instances.copyInstances(0, train, 0, first);
		// instances.copyInstances(first + numInstForFold, train, first,
		// numInstances - first - numInstForFold);

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
		// TODO DESCOMENTAR E CORRIGIR ERRO
		//instances.copyInstances(first, test, 0, numInstForFold);
		return test;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
