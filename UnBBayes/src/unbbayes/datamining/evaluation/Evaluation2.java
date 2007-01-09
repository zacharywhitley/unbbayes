package unbbayes.datamining.evaluation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.ResourceBundle;

import unbbayes.controller.IProgress;
import unbbayes.datamining.classifiers.Classifier;
import unbbayes.datamining.classifiers.DistributionClassifier;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Options;
import unbbayes.datamining.datamanipulation.Utils;

/**
 * Class for evaluating machine learning classsifiers
 * 
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version $1.0 $ (17/02/2002)
 */
public class Evaluation2 implements IProgress {
	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle
			.getBundle("unbbayes.datamining.classifiers.resources.ClassifiersResource");

	/** The number of classes. */
	private int numClasses;

	/** All incorrectly classified instances. */
	private int incorrect;

	/** All correctly classified instances. */
	private int correct;

	/** All unclassified instances. */
	private int unclassified;

	/** All instances that had no class assigned to them. */
	private int missingClass;

	/** All instances that had a class assigned to them. */
	private int withClass;

	/** Is the class nominal or numeric? */
	private boolean classIsNominal;

	/** Array for storing the confusion matrix. */
	private int[][] confusionMatrix;

	/** Sum of squared errors. */
	private double sumSqrErr;

	/** The names of the classes. */
	private String[] classNames;

	/** The instance set that will be manipulated. */
	private InstanceSet data;

	/** Confidence limits for the normal distribution */
	private double[] confidenceLimits = { 3.09, 2.58, 2.33, 1.65, 1.28, 0.84,
			0.25 };

	private String[] confidenceProbs = { "99.8%", "99%", "98%", "90%", "80%",
			"60%", "20%" };

	private int confidenceLimit = 100;

	private int numInstances;

	private int counter;

	private Classifier classifier;

	// private float[][] propagationResults;

	/**
	 * Initializes all the counters for the evaluation.
	 * 
	 * @param data
	 *            set of training instances, to get some header information and
	 *            prior class distribution information
	 * @exception Exception
	 *                if the class is not defined
	 */
	public Evaluation2(InstanceSet data) throws Exception {
		this.data = data;
		numClasses = data.numClasses();
		numInstances = data.numInstances();
		counter = 0;
		classIsNominal = data.getClassAttribute().isNominal();
		confidenceLimit = Options.getInstance().getConfidenceLimit();
		if (classIsNominal) {
			confusionMatrix = new int[numClasses][numClasses];
			classNames = new String[numClasses];
			for (int i = 0; i < numClasses; i++) {
				classNames[i] = data.getClassAttribute().value(i);
			}
		}
	}

	public Evaluation2(InstanceSet data, Classifier classifier) throws Exception {
		this(data);
		this.classifier = classifier;
	}

	/**
	 * Evaluates the classifier on a given set of instances.
	 * 
	 * @param classifier
	 *            machine learning classifier
	 * @exception Exception
	 *                if model could not be evaluated successfully
	 */
	public void evaluateModel(Classifier classifier) throws Exception {
		for (int i = 0; i < numInstances; i++) {
			if ((i % 50000) == 0) {
				String currentHour = (new SimpleDateFormat("HH:mm:ss - "))
						.format(new Date());
				System.out.println("instância = " + i + " hora = "
						+ currentHour);
			}
			evaluateModelOnce(classifier, data.getInstance(i));
		}
	}

	/**
	 * Evaluates the classifier on a given set of instances.
	 * 
	 * @param classifier
	 *            machine learning classifier
	 * @param testData
	 *            set of test instances for evaluation
	 * @exception Exception
	 *                if model could not be evaluated successfully
	 */
	public void evaluateModel(Classifier classifier, InstanceSet testData)
			throws Exception {
		int numInstances = testData.numInstances();
		for (int i = 0; i < numInstances; i++) {
			evaluateModelOnce(classifier, testData.getInstance(i));
		}
	}

	/**
	 * Evaluates the classifier on a single instance.
	 * 
	 * @param classifier
	 *            machine learning classifier
	 * @param instance
	 *            the test instance to be classified
	 * @return the prediction made by the clasifier
	 * @exception Exception
	 *                if model could not be evaluated successfully
	 */
	public int evaluateModelOnce(Classifier classifier, Instance instance)
			throws Exception {
		Instance classMissing = instance;
		int pred = 0;
		if (classIsNominal) {
			if (classifier instanceof DistributionClassifier) {
				float[] dist = ((DistributionClassifier) classifier)
						.distributionForInstance(classMissing);
				// propagationResults[counter] = dist;

				pred = ((DistributionClassifier) classifier)
						.classifyInstance(dist);
				updateStatsForClassifier(pred, instance);
			} else {
				pred = classifier.classifyInstance(classMissing);
				updateStatsForClassifier(pred, instance);
			}
		} else { // Class is numeric
			System.out.println("numeric class");
		}
		return pred;
	}

	/**
	 * Convert a single prediction into a probability distribution with all zero
	 * probabilities except the predicted value which has probability 1.0;
	 * 
	 * @param predictedClass
	 *            the index of the predicted class
	 * @return the probability distribution
	 */
	private float[] makeDistribution(int predictedClass) {
		float[] result = new float[numClasses];
		if (Instance.isMissingValue(predictedClass)) {
			return result;
		}
		if (classIsNominal) {
			result[(int) predictedClass] = 1.0f;
		} else {
			result[0] = predictedClass;
		}
		return result;
	}

	/**
	 * Outputs the performance statistics in summary form. Lists number (and
	 * percentage) of instances classified correctly, incorrectly and
	 * unclassified. Outputs the total number of instances classified, and the
	 * number of instances (if any) that had no class value provided.
	 * 
	 * @return the summary as a String
	 */
	public String toString() {
		StringBuilder text = new StringBuilder(resource.getString("summary"));

		// computeROCCurve();

		try {
			text.append(resource.getString("correctly"));
			text.append(Utils.doubleToString(correct(), 12, 4) + " "
					+ Utils.doubleToString(pctCorrect(), 12, 4) + " %\n");
			if (withClass >= confidenceLimit)
				text.append(correctConfidence());
			text.append(resource.getString("incorrectly"));
			text.append(Utils.doubleToString(incorrect(), 12, 4) + " "
					+ Utils.doubleToString(pctIncorrect(), 12, 4) + " %\n");
			if (withClass >= confidenceLimit)
				text.append(incorrectConfidence());
			text.append("Quadratic loss function\t\t\t\t"
					+ Utils.doubleToString((sumSqrErr / withClass), 17, 4)
					+ "\n");

			if (Utils.gr(unclassified(), 0)) {
				text.append(resource.getString("unclassified"));
				text.append(Utils.doubleToString(unclassified(), 12, 4) + " "
						+ Utils.doubleToString(pctUnclassified(), 12, 4)
						+ "%\n");
			}

			text.append(resource.getString("totalNumber"));
			text.append(Utils.doubleToString(withClass, 12, 4) + "\n");

			if (missingClass > 0) {
				text.append(resource.getString("unknownInstances"));
				text.append(Utils.doubleToString(missingClass, 12, 4) + "\n");
			}
		} catch (Exception ex) { // Should never occur since the class is
									// known to be nominal here
			System.err.println("A bug in Evaluation class");
		}

		return text.toString();
	}

	/**
	 * Update the numeric accuracy measures. For numeric classes, the accuracy
	 * is between the actual and predicted class values. For nominal classes,
	 * the accuracy is between the actual and predicted class probabilities.
	 * 
	 * @param predicted
	 *            the predicted values
	 * @param actual
	 *            the actual value
	 * @param weight
	 *            the weight associated with this prediction
	 */
	private void updateNumericScores(float[] predicted, float[] actual,
			float weight) {
		float diff;
		double partialSumSqrErr = 0;
		for (int i = 0; i < numClasses; i++) {
			diff = predicted[i] - actual[i];
			partialSumSqrErr += diff * diff;
		}
		sumSqrErr += weight * partialSumSqrErr;
	}

	private String correctConfidence() {
		double lowerBound, upperBound, z;
		double f = (double) correct / (double) withClass;
		double n = (double) (numInstances());
		double mediumTerm, initialTerm;
		StringBuilder sb = new StringBuilder(
				"Correct confidence limits\nPr[c]\t   z\n");
		for (int i = 0; i < confidenceLimits.length; i++) {
			z = confidenceLimits[i];
			initialTerm = initialTerm(f, z, n);
			mediumTerm = mediumTerm(f, z, n);
			lowerBound = (initialTerm - mediumTerm) / finalTerm(z, n);
			upperBound = (initialTerm + mediumTerm) / finalTerm(z, n);
			sb.append(confidenceProbs[i] + "\t["
					+ Utils.doubleToString(lowerBound, 4, 4) + " , "
					+ Utils.doubleToString(upperBound, 4, 4) + "]\n");
		}
		return sb.toString();
	}

	private String incorrectConfidence() {
		double lowerBound, upperBound, z;
		double f = (double) incorrect / (double) withClass;
		double n = (double) (numInstances());
		double mediumTerm, initialTerm;
		StringBuilder sb = new StringBuilder(
				"Incorrect confidence limits\nPr[c]\t   z\n");
		for (int i = 0; i < confidenceLimits.length; i++) {
			z = confidenceLimits[i];
			initialTerm = initialTerm(f, z, n);
			mediumTerm = mediumTerm(f, z, n);
			lowerBound = (initialTerm - mediumTerm) / finalTerm(z, n);
			upperBound = (initialTerm + mediumTerm) / finalTerm(z, n);
			sb.append(confidenceProbs[i] + "\t["
					+ Utils.doubleToString(lowerBound, 4, 4) + " , "
					+ Utils.doubleToString(upperBound, 4, 4) + "]\n");
		}
		return sb.toString();
	}

	private double finalTerm(double z, double n) {
		return 1.0d + (Math.pow(z, 2.0d) / n);
	}

	private double initialTerm(double f, double z, double n) {
		return f + (Math.pow(z, 2.0d) / (2.0d * n));
	}

	private double mediumTerm(double f, double z, double n) {
		return (z * Math.sqrt((f / n) - (Math.pow(f, 2.0d) / n)
				+ ((Math.pow(z, 2.0d)) / (4.0d * (Math.pow(n, 2.0d))))));
	}

	/**
	 * Gets the number of test instances that had a known class value
	 * 
	 * @return the number of test instances with known class
	 */
	public final int numInstances() {
		return withClass;
	}

	/**
	 * Gets the number of instances incorrectly classified (that is, for which
	 * an incorrect prediction was made).
	 * 
	 * @return the number of incorrectly classified instances
	 */
	public final int incorrect() {
		return incorrect;
	}

	/**
	 * Gets the percentage of instances incorrectly classified (that is, for
	 * which an incorrect prediction was made).
	 * 
	 * @return the percent of incorrectly classified instances (between 0 and
	 *         100)
	 */
	public final double pctIncorrect() {
		return 100 * (double) incorrect / (double) withClass;
	}

	/**
	 * Gets the number of instances correctly classified (that is, for which a
	 * correct prediction was made).
	 * 
	 * @return the number of correctly classified instances
	 */
	public final int correct() {
		return correct;
	}

	/**
	 * Gets the percentage of instances correctly classified (that is, for which
	 * a correct prediction was made).
	 * 
	 * @return the percent of correctly classified instances (between 0 and 100)
	 */
	public final double pctCorrect() {
		return 100 * (double) correct / (double) withClass;
	}

	/**
	 * Gets the number of instances not classified (that is, for which no
	 * prediction was made by the classifier).
	 * 
	 * @return the number of unclassified instances
	 */
	public final int unclassified() {
		return unclassified;
	}

	/**
	 * Gets the percentage of instances not classified (that is, for which no
	 * prediction was made by the classifier).
	 * 
	 * @return the percent of unclassified instances (between 0 and 100)
	 */
	public final double pctUnclassified() {
		return 100 * (double) unclassified / (double) withClass;
	}

	/**
	 * Updates all the statistics about a classifier performance for the current
	 * test instance.
	 * 
	 * @param predictedDistribution
	 *            the probabilities assigned to each class
	 * @param instance
	 *            the instance to be classified
	 * @exception Exception
	 *                if the class of the instance is not set
	 */
	private void updateStatsForClassifier(
			int predictedClass/* float[] predictedDistribution */,
			Instance instance) throws Exception {
		if (!instance.classIsMissing()) { /*
											 * float[] result = new
											 * float[numClasses]; if
											 * (Instance.isMissingValue(predictedClass)) {
											 * return result; } if
											 * (classIsNominal) {
											 * result[(int)predictedClass] =
											 * 1.0f; } else { result[0] =
											 * predictedClass; } return result;
											 */

			// Determine the predicted class (doesn't detect multiple
			// classifications)
			/*
			 * int predictedClass = -1; float bestProb = 0.0f; for(int i = 0; i <
			 * numClasses; i++) { if (predictedDistribution[i] > bestProb) {
			 * predictedClass = i; bestProb = predictedDistribution[i]; } }
			 */

			withClass += instance.getWeight();

			// Update counts when no class was predicted
			if (predictedClass < 0) {
				unclassified += instance.getWeight();
				return;
			}

			int actualClass = instance.classValue();
			updateNumericScores(
					makeDistribution(predictedClass)/* predictedDistribution */,
					makeDistribution(actualClass), instance.getWeight());

			// propagation[counter][0] = actualClass;
			// propagation[counter][1] = predictedClass;

			// Update other stats
			confusionMatrix[actualClass][predictedClass] += instance
					.getWeight();

			if (predictedClass != actualClass) {
				incorrect += instance.getWeight();
			} else {
				correct += instance.getWeight();
			}
		} else {
			missingClass += instance.getWeight();
		}
	}

	/**
	 * Performs a (stratified if class is nominal) cross-validation for a
	 * classifier on a set of instances.
	 * 
	 * @param classifier
	 *            the classifier with any options set.
	 * @param numFolds
	 *            the number of folds for the cross-validation
	 * @exception Exception
	 *                if a classifier could not be generated successfully or the
	 *                class is not defined
	 */
	public void crossValidateModel(Classifier classifier, int numFolds)
			throws Exception {
		data.randomize(new Random(42));
		if (data.getClassAttribute().isNominal()) {
			// TODO DESCOMENTAR E CORRIGIR ERRO
			//data.stratify();
		}
		// Do the folds
		for (int i = 0; i < numFolds; i++) {
			InstanceSet train = trainCV(data, numFolds, i);
			classifier.buildClassifier(train);
			InstanceSet test = testCV(data, numFolds, i);
			evaluateModel(classifier, test);
		}
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
	public InstanceSet trainCV(InstanceSet instances, int numFolds, int numFold) {
		int numInstForFold, first, offset;
		int numInstances = instances.numInstances();
		InstanceSet train;

		if (numFolds < 2) {
			throw new IllegalArgumentException(resource.getString("folds2"));
		}
		if (numFolds > numInstances) {
			throw new IllegalArgumentException(resource.getString("moreFolds"));
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
	public InstanceSet testCV(InstanceSet instances, int numFolds, int numFold) {
		int numInstForFold, first, offset;
		int numInstances = instances.numInstances();
		InstanceSet test;

		if (numFolds < 2) {
			throw new IllegalArgumentException(resource.getString("folds2"));
		}
		if (numFolds > numInstances) {
			throw new IllegalArgumentException(resource.getString("moreFolds"));
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

	/**
	 * Returns a copy of the confusion matrix.
	 * 
	 * @return a copy of the confusion matrix as a two-dimensional array
	 */
	public double[][] confusionMatrix() {
		double[][] newMatrix = new double[confusionMatrix.length][0];

		for (int i = 0; i < confusionMatrix.length; i++) {
			newMatrix[i] = new double[confusionMatrix[i].length];
			System.arraycopy(confusionMatrix[i], 0, newMatrix[i], 0,
					confusionMatrix[i].length);
		}
		return newMatrix;
	}

	/**
	 * Generates a breakdown of the accuracy for each class, incorporating
	 * various information-retrieval statistics, such as true/false positive
	 * rate. Should be useful for ROC curves.
	 * 
	 * @return the statistics presented as a string
	 * @exception Exception
	 *                if the class is numeric
	 */
	public String toClassDetailsString() throws Exception {
		if (!classIsNominal) {
			throw new Exception(resource.getString("noMatrix"));
		}
		StringBuilder text = new StringBuilder(resource.getString("accuracy"));
		text.append("\nTP Rate   FP Rate   TN Rate   FN Rate   Class\n");
		for (int i = 0; i < numClasses; i++) {
			text.append(Utils.doubleToString(truePositiveRate(i), 7, 3))
					.append("   ");
			text.append(Utils.doubleToString(falsePositiveRate(i), 7, 3))
					.append("    ");
			text.append(Utils.doubleToString(trueNegativeRate(i), 7, 3))
					.append("   ");
			text.append(Utils.doubleToString(falseNegativeRate(i), 7, 3))
					.append("    ");
			text.append(classNames[i]).append('\n');
		}
		return text.toString();
	}

	/**
	 * Calculate the number of true positives with respect to a particular
	 * class. This is defined as correctly classified positives
	 * 
	 * @param classIndex
	 *            the index of the class to consider as "positive"
	 * @return the number of true positives
	 */
	public int numTruePositives(int classIndex) {
		int correct = 0;
		for (int j = 0; j < numClasses; j++) {
			if (j == classIndex) {
				correct += confusionMatrix[classIndex][j];
			}
		}
		return correct;
	}

	/**
	 * Calculate the true positive rate with respect to a particular class. This
	 * is defined correctly classified positives divided by total positives
	 * 
	 * @param classIndex
	 *            the index of the class to consider as "positive"
	 * @return the true positive rate
	 */
	public double truePositiveRate(int classIndex) {
		double correct = 0, total = 0;
		for (int j = 0; j < numClasses; j++) {
			if (j == classIndex) {
				correct += confusionMatrix[classIndex][j];
			}
			total += confusionMatrix[classIndex][j];
		}
		if (total == 0) {
			return 0;
		}
		return correct / total;
	}

	/**
	 * Calculate the number of true negatives with respect to a particular
	 * class. This is defined as correctly classified negatives
	 * 
	 * @param classIndex
	 *            the index of the class to consider as "negative"
	 * @return the number of true negatives
	 */
	public int numTrueNegatives(int classIndex) {
		int correct = 0;
		for (int i = 0; i < numClasses; i++) {
			if (i != classIndex) {
				for (int j = 0; j < numClasses; j++) {
					if (j != classIndex) {
						correct += confusionMatrix[i][j];
					}
				}
			}
		}
		return correct;
	}

	/**
	 * Calculate the true negative rate with respect to a particular class. This
	 * is defined as correctly classified negatives divided by total negatives
	 * 
	 * @param classIndex
	 *            the index of the class to consider as "negative"
	 * @return the true negative rate
	 */
	public double trueNegativeRate(int classIndex) {
		double correct = 0, total = 0;
		for (int i = 0; i < numClasses; i++) {
			if (i != classIndex) {
				for (int j = 0; j < numClasses; j++) {
					if (j != classIndex) {
						correct += confusionMatrix[i][j];
					}
					total += confusionMatrix[i][j];
				}
			}
		}
		if (total == 0) {
			return 0;
		}
		return correct / total;
	}

	/**
	 * Calculate number of false positives with respect to a particular class.
	 * This is defined as incorrectly classified negatives
	 * 
	 * @param classIndex
	 *            the index of the class to consider as "positive"
	 * @return number of false positives
	 */
	public int numFalsePositives(int classIndex) {
		int incorrect = 0;
		for (int i = 0; i < numClasses; i++) {
			if (i != classIndex) {
				for (int j = 0; j < numClasses; j++) {
					if (j == classIndex) {
						incorrect += confusionMatrix[i][j];
					}
				}
			}
		}
		return incorrect;
	}

	/**
	 * Calculate the false positive rate with respect to a particular class.
	 * This is defined as incorrectly classified negatives divided by total
	 * negatives
	 * 
	 * @param classIndex
	 *            the index of the class to consider as "positive"
	 * @return the false positive rate
	 */
	public double falsePositiveRate(int classIndex) {
		double incorrect = 0, total = 0;
		for (int i = 0; i < numClasses; i++) {
			if (i != classIndex) {
				for (int j = 0; j < numClasses; j++) {
					if (j == classIndex) {
						incorrect += confusionMatrix[i][j];
					}
					total += confusionMatrix[i][j];
				}
			}
		}
		if (total == 0) {
			return 0;
		}
		return incorrect / total;
	}

	/**
	 * Calculate number of false negatives with respect to a particular class.
	 * This is defined as incorrectly classified positives
	 * 
	 * @param classIndex
	 *            the index of the class to consider as "negative"
	 * @return the number of false negatives
	 */
	public int numFalseNegatives(int classIndex) {
		int incorrect = 0;
		for (int i = 0; i < numClasses; i++) {
			if (i == classIndex) {
				for (int j = 0; j < numClasses; j++) {
					if (j != classIndex) {
						incorrect += confusionMatrix[i][j];
					}
				}
			}
		}
		return incorrect;
	}

	/**
	 * Calculate the false negative rate with respect to a particular class.
	 * This is defined as incorrectly classified positives divided by total
	 * positives
	 * 
	 * @param classIndex
	 *            the index of the class to consider as "negative"
	 * @return the false negative rate
	 */
	public double falseNegativeRate(int classIndex) {
		double incorrect = 0, total = 0;
		for (int i = 0; i < numClasses; i++) {
			if (i == classIndex) {
				for (int j = 0; j < numClasses; j++) {
					if (j != classIndex) {
						incorrect += confusionMatrix[i][j];
					}
					total += confusionMatrix[i][j];
				}
			}
		}
		if (total == 0) {
			return 0;
		}
		return incorrect / total;
	}

	/**
	 * Outputs the performance statistics as a classification confusion matrix.
	 * For each class value, shows the distribution of predicted class values.
	 * 
	 * @return the confusion matrix as a String
	 * @exception Exception
	 *                if the class is numeric
	 */
	public String toMatrixString() throws Exception {
		StringBuilder text = new StringBuilder(resource.getString("matrix"));
		if (!classIsNominal) {
			throw new Exception(resource.getString("noMatrix"));
		}

		char[] IDChars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
				'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
				'w', 'x', 'y', 'z' };
		int IDWidth;
		boolean fractional = false;

		// Find the maximum value in the matrix
		// and check for fractional display requirement
		double maxval = 0;
		for (int i = 0; i < numClasses; i++) {
			for (int j = 0; j < numClasses; j++) {
				double current = confusionMatrix[i][j];
				if (current < 0) {
					current *= -10;
				}
				if (current > maxval) {
					maxval = current;
				}
				double fract = current - Math.rint(current);
				if (!fractional && ((Math.log(fract) / Math.log(10)) >= -2)) {
					fractional = true;
				}
			}
		}

		IDWidth = 1 + Math.max(
				(int) (Math.log(maxval) / Math.log(10) + (fractional ? 3 : 0)),
				(int) (Math.log(numClasses) / Math.log(IDChars.length)));
		for (int i = 0; i < numClasses; i++) {
			if (fractional) {
				text.append(" ").append(num2ByteID(i, IDChars, IDWidth - 3))
						.append("   ");
			} else {
				text.append(" ").append(num2ByteID(i, IDChars, IDWidth));
			}
		}
		text.append("   <-- classified as\n");
		for (int i = 0; i < numClasses; i++) {
			for (int j = 0; j < numClasses; j++) {
				text.append(" ").append(
						Utils.doubleToString(confusionMatrix[i][j], IDWidth,
								(fractional ? 2 : 0)));
			}
			text.append(" | ").append(num2ByteID(i, IDChars, IDWidth)).append(
					" = ").append(classNames[i]).append("\n");
		}

		return text.toString();
	}

	/**
	 * Method for generating indices for the confusion matrix.
	 * 
	 * @param num
	 *            integer to format
	 * @param IDChars
	 *            Number of chars in the new String
	 * @param IDWidth
	 *            The width of the new String
	 * @return the formatted integer as a string
	 */
	private String num2ByteID(int num, char[] IDChars, int IDWidth) {
		char ID[] = new char[IDWidth];
		int i;

		for (i = IDWidth - 1; i >= 0; i--) {
			ID[i] = IDChars[num % IDChars.length];
			num = num / IDChars.length - 1;
			if (num < 0) {
				break;
			}
		}
		for (i--; i >= 0; i--) {
			ID[i] = ' ';
		}
		return new String(ID);
	}

	public int maxCount() {
		return numInstances;
	}

	public boolean next() {
		if (counter == numInstances) {
			return false;
		} else {
			try {
				evaluateModelOnce(classifier, data.getInstance(counter));
				counter++;
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}

	public void cancel() {
		counter = 0;
	}

	public void computeROCCurve() {
		/*
		 * for (int i=0;i<numInstances;i++) { for (int j=0;j<numClasses;j++) {
		 * System.out.println("j = "+propagationResults[i][j]); }
		 * System.out.println("actualClass = "+propagation[i][0]);
		 * System.out.println("predictedClass = "+propagation[i][1]);
		 * System.out.println("i = "+i); }
		 */
		ArrayList[] vet1 = new ArrayList[numClasses];// float
		ArrayList[] vet2 = new ArrayList[numClasses];// boolean
		for (int j = 0; j < numClasses; j++) {
			vet1[j] = new ArrayList();
			vet2[j] = new ArrayList();
		}
		for (int i = 0; i < numInstances; i++) {
			// vet1[propagation[i][1]].add(new
			// Float(propagationResults[i][propagation[i][1]]));
			// vet2[propagation[i][1]].add(new
			// Boolean(propagation[i][0]==propagation[i][1]));
		}
		double[][] v3 = new double[numClasses][];
		boolean[][] v4 = new boolean[numClasses][];
		boolean[][] v5 = new boolean[numClasses][];
		for (int j = 0; j < numClasses; j++) {
			/*
			 * System.out.println("class = "+j); for(int i=0;i<vet1[j].size();i++) {
			 * System.out.println("Valor = "+vet1[j].get(i));
			 * System.out.println("Flag = "+vet2[j].get(i)); }
			 */
			v3[j] = new double[vet1[j].size()];
			v4[j] = new boolean[vet1[j].size()];
			v5[j] = new boolean[vet1[j].size()];
		}
		for (int j = 0; j < numClasses; j++) {
			for (int i = 0; i < v3[j].length; i++) {
				v3[j][i] = ((Float) vet1[j].get(i)).floatValue();
				v4[j][i] = ((Boolean) vet2[j].get(i)).booleanValue();
			}
		}
		/*
		 * for (int j=0;j<numClasses;j++) { int[] res = Utils.sort(v3[j]); for
		 * (int i=0;i<res.length;i++) { v5[i] = v4[res[i]]; } } for (int j=0;j<numClasses;j++) {
		 * System.out.println("Class = "+j); for(int i=0;i<v3[j].length;i++) {
		 * System.out.println("Valor = "+v3[j][i]); System.out.println("Flag =
		 * "+v5[j][i]); } }
		 */
	}

}