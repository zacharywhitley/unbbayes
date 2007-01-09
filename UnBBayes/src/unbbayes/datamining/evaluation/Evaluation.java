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
import unbbayes.datamining.datamanipulation.UnassignedClassException;
import unbbayes.datamining.datamanipulation.Utils;

/**
 * Class for evaluating machine learning classsifiers
 *
 *	@author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *	@version $1.0 $ (17/02/2002)
 */
public class Evaluation implements IProgress {
	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("" +
			"unbbayes.datamining.classifiers.resources.ClassifiersResource");

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

	/** Array for storing the confusion matrix. 
	 * [trueClass][predictedClass]
	 */
	private int [][] confusionMatrix;

	/** Sum of squared errors. */
	private double sumSqrErr;

	/** The names of the classes. */
	private String [] classNames;

	/** The instance set that will be manipulated. */
	private InstanceSet instanceSet;

	/** Confidence limits for the normal distribution */
	private double[] confidenceLimits = {3.09,2.58,2.33,1.65,1.28,0.84,0.25};

	//private String[] confidenceProbs = {"0.1%","0.5%","1%","5%","10%","20%","40%"};
	private String[] confidenceProbs = {"99.8%","99%","98%","90%","80%","60%","20%"};

	private int confidenceLimit = 100;

	private int numInstances;
	private int counter;

	private Classifier classifier;

	//private float[][] propagationResults;
	//private byte[][] propagation;

	/**
	 * Initializes all the counters for the evaluation.
	 *
	 * @param instanceSet set of training instances, to get some header
	 * information and prior class distribution information
	 * @exception Exception if the class is not defined
	 */
	public Evaluation(InstanceSet instanceSet) throws Exception
	{
	this.instanceSet = instanceSet;
	numClasses = instanceSet.numClasses();
	numInstances = instanceSet.numInstances();
	counter=0;
	classIsNominal = instanceSet.getClassAttribute().isNominal();
	confidenceLimit = Options.getInstance().getConfidenceLimit();
	if (classIsNominal)
	{
		confusionMatrix = new int [numClasses][numClasses];
		classNames = new String [numClasses];
		for(int i = 0; i < numClasses; i++)
		{
		classNames[i] = instanceSet.getClassAttribute().value(i);
		}
	}
	}

	public Evaluation(InstanceSet instanceSet,Classifier classifier) throws Exception
	{
		this(instanceSet);
		this.classifier = classifier;
		/*if (classifier instanceof DistributionClassifier)
		{
			propagationResults = new float[numInstances][numClasses];
			propagation = new byte[numInstances][2];
		}*/
	}

	/**
	 * Evaluates the classifier on a given set of instances.
	 *
	 * @param classifier machine learning classifier
	 * @exception Exception if model could not be evaluated
	 * successfully
	 */
	public void evaluateModel(Classifier classifier) throws Exception
	{
		/*if (classifier instanceof DistributionClassifier)
		{
			propagationResults = new float[numInstances][numClasses];
			propagation = new byte[numInstances][2];
		}*/	
		for	(int i = 0; i < numInstances; i++)
			{	 if ((i%50000)==0)
				{	 String currentHour = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
					System.out.println("instância = "+i+" hora = "+currentHour);
				}
				evaluateModelOnce(classifier,instanceSet.getInstance(i));
			}
	}

	/**
	 * Evaluates the classifier on a given set of instances.
	 *
	 * @param classifier machine learning classifier
	 * @param testData set of test instances for evaluation
	 * @exception Exception if model could not be evaluated
	 * successfully
	 */
	public void evaluateModel(Classifier classifier,InstanceSet testData) throws Exception
	{	
		/*if (classifier instanceof DistributionClassifier)
		{
			propagationResults = new float[numInstances][numClasses];
			propagation = new byte[numInstances][2];
		}*/
		int numInstances = testData.numInstances();
		for (int i = 0; i < numInstances; i++)
		{	evaluateModelOnce(classifier,testData.getInstance(i));
		}
	}

	/**
	 * Evaluates the classifier on a single instance.
	 *
	 * @param classifier machine learning classifier
	 * @param instance the test instance to be classified
	 * @return the prediction made by the clasifier
	 * @exception Exception if model could not be evaluated
	 * successfully
	 */
	public float evaluateModelOnce(Classifier classifier,Instance instance) throws Exception {
		Instance classMissing = instance;
		float pred = 0;
		if (classIsNominal) {	 
			if (classifier instanceof DistributionClassifier) {	
				float[] dist = ((DistributionClassifier)classifier).distributionForInstance(classMissing);
				//propagationResults[counter] = dist;
				
				pred = ((DistributionClassifier)classifier).classifyInstance(dist);
				updateStatsForClassifier(pred, instance);
			} else {	
				pred = classifier.classifyInstance(classMissing);
				updateStatsForClassifier(pred, instance);
			}
		} else {
			//Class is numeric
			System.out.println("numeric class");
		}
		return pred;
	}

	/**
	 * Convert a single prediction into a probability distribution
	 * with all zero probabilities except the predicted value which
	 * has probability 1.0;
	 *
	 * @param predictedClass the index of the predicted class
	 * @return the probability distribution
	 */
	private float[] makeDistribution(float predictedClass) {
		float[] result = new float[numClasses];
		
		if (Instance.isMissingValue(predictedClass)) {
			return result;
		}
		if (classIsNominal) {
			result[(int)predictedClass] = 1.0f;
		} else {
			result[0] = predictedClass;
		}
		return result;
	}

	/**
	 * Outputs the performance statistics in summary form. Lists
	 * number (and percentage) of instances classified correctly,
	 * incorrectly and unclassified. Outputs the total number of
	 * instances classified, and the number of instances (if any)
	 * that had no class value provided.
	 *
	 * @return the summary as a String
	 */
	public String toString()
	{	StringBuffer text = new StringBuffer(resource.getString("summary"));

		//computeROCCurve();
		
		try
		{	text.append(resource.getString("correctly"));
			text.append(Utils.doubleToString(correct(), 12, 4) + " " + Utils.doubleToString(pctCorrect(),12, 4) + " %\n");
			if (withClass >= confidenceLimit)
				text.append(correctConfidence());
			text.append(resource.getString("incorrectly"));
			text.append(Utils.doubleToString(incorrect(), 12, 4) + " " + Utils.doubleToString(pctIncorrect(),12, 4) +" %\n");
			if (withClass >= confidenceLimit)
				text.append(incorrectConfidence());
			text.append("Quadratic loss function\t\t\t\t"+Utils.doubleToString((sumSqrErr/withClass), 17, 4)+"\n");

			if (Utils.gr(unclassified(), 0))
			{	text.append(resource.getString("unclassified"));
				text.append(Utils.doubleToString(unclassified(), 12,4) + " " + Utils.doubleToString(pctUnclassified(),12, 4) + "%\n");
			}

			text.append(resource.getString("totalNumber"));
			text.append(Utils.doubleToString(withClass, 12, 4) + "\n");

			if (missingClass > 0)
			{	text.append(resource.getString("unknownInstances"));
				text.append(Utils.doubleToString(missingClass, 12, 4) + "\n");
			}
		}
		catch (Exception ex)
		{	// Should never occur since the class is known to be nominal here
			System.err.println("A bug in Evaluation class");
		}

	return text.toString();
	}

	/**
	 * Update the numeric accuracy measures. For numeric classes, the
	 * accuracy is between the actual and predicted class values. For
	 * nominal classes, the accuracy is between the actual and
	 * predicted class probabilities.
	 *
	 * @param predicted the predicted values
	 * @param actual the actual value
	 * @param weight the weight associated with this prediction
	 */
	private void updateNumericScores(float[] predicted,float[] actual,float weight)
	{	 float diff;
		double partialSumSqrErr = 0;
		for(int i = 0; i < numClasses; i++)
		{	 diff = predicted[i] - actual[i];
			partialSumSqrErr += diff * diff;
		}
		sumSqrErr += weight * partialSumSqrErr;
	}

	private String correctConfidence()
	{	double lowerBound,upperBound,z;
	double f = (double)correct/(double)withClass;
	double n = (double)(numInstances());
	double finalTerm,mediumTerm,initialTerm;
	StringBuffer sb = new StringBuffer("Correct confidence limits\nPr[c]\t	 z\n");
	for (int i=0;i<confidenceLimits.length;i++)
	{	z = confidenceLimits[i];
		initialTerm = initialTerm(f,z,n);
		mediumTerm = mediumTerm(f,z,n);
		finalTerm = finalTerm(z,n);
		lowerBound = (initialTerm-mediumTerm)/finalTerm(z,n);
		upperBound = (initialTerm+mediumTerm)/finalTerm(z,n);
		sb.append(confidenceProbs[i]+"\t["+Utils.doubleToString(lowerBound, 4, 4)+" , "+Utils.doubleToString(upperBound, 4, 4)+"]\n");
	}
	return sb.toString();
	}

	private String incorrectConfidence()
	{	double lowerBound,upperBound,z;
	double f = (double)incorrect/(double)withClass;
	double n = (double)(numInstances());
	double finalTerm,mediumTerm,initialTerm;
	StringBuffer sb = new StringBuffer("Incorrect confidence limits\nPr[c]\t	 z\n");
	for (int i=0;i<confidenceLimits.length;i++)
	{	z = confidenceLimits[i];
		initialTerm = initialTerm(f,z,n);
		mediumTerm = mediumTerm(f,z,n);
		finalTerm = finalTerm(z,n);
		lowerBound = (initialTerm-mediumTerm)/finalTerm(z,n);
		upperBound = (initialTerm+mediumTerm)/finalTerm(z,n);
		sb.append(confidenceProbs[i]+"\t["+Utils.doubleToString(lowerBound, 4, 4)+" , "+Utils.doubleToString(upperBound, 4, 4)+"]\n");
	}
	return sb.toString();
	}

	private double finalTerm(double z,double n)
	{	return 1.0d+(Math.pow(z,2.0d)/n);
	}

	private double initialTerm(double f,double z,double n)
	{	return f+(Math.pow(z,2.0d)/(2.0d*n));
	}

	private double mediumTerm(double f,double z,double n)
	{	return (z*Math.sqrt((f/n)-(Math.pow(f,2.0d)/n)+((Math.pow(z,2.0d))/(4.0d*(Math.pow(n,2.0d))))));
	}

	/**
	 * Gets the number of test instances that had a known class value
	 *
	 * @return the number of test instances with known class
	 */
	public final int numInstances()
	{ return withClass;
	}

	/**
	 * Gets the number of instances incorrectly classified (that is, for
	 * which an incorrect prediction was made).
	 *
	 * @return the number of incorrectly classified instances
	 */
	public final int incorrect()
	{ return incorrect;
	}

	/**
	 * Gets the percentage of instances incorrectly classified (that is, for
	 * which an incorrect prediction was made).
	 *
	 * @return the percent of incorrectly classified instances
	 * (between 0 and 100)
	 */
	public final double pctIncorrect()
	{ return 100 * (double)incorrect / (double)withClass;
	}

	/**
	 * Gets the number of instances correctly classified (that is, for
	 * which a correct prediction was made).
	 *
	 * @return the number of correctly classified instances
	 */
	public final int correct()
	{ return correct;
	}

	/**
	 * Gets the percentage of instances correctly classified (that is, for
	 * which a correct prediction was made).
	 *
	 * @return the percent of correctly classified instances (between 0 and 100)
	 */
	public final double pctCorrect()
	{ return 100 * (double)correct / (double)withClass;
	}

	/**
	 * Gets the number of instances not classified (that is, for
	 * which no prediction was made by the classifier).
	 *
	 * @return the number of unclassified instances
	 */
	public final int unclassified()
	{ return unclassified;
	}

	/**
	 * Gets the percentage of instances not classified (that is, for
	 * which no prediction was made by the classifier).
	 *
	 * @return the percent of unclassified instances (between 0 and 100)
	 */
	public final double pctUnclassified()
	{ return 100 * (double)unclassified / (double)withClass;
	}

	/**
	 * Updates all the statistics about a classifier performance for
	 * the current test instance.
	 *
	 * @param predictedDistribution the probabilities assigned to
	 * each class
	 * @param instance the instance to be classified
	 * @exception Exception if the class of the instance is not
	 * set
	 */
	private void updateStatsForClassifier(float predictedClass
			/*float[] predictedDistribution*/,Instance instance) throws Exception {
		if (!instance.classIsMissing()) {
			/*float[] result = new float[numClasses];
			if (Instance.isMissingValue(predictedClass))
			{	return result;
			}
			if (classIsNominal)
			{	result[(int)predictedClass] = 1.0f;
			}
			else
			{	result[0] = predictedClass;
			}
			return result;*/

			// Determine the predicted class (doesn't detect multiple classifications)
			/*int predictedClass = -1;
			float bestProb = 0.0f;
			for(int i = 0; i < numClasses; i++)
			{	if (predictedDistribution[i] > bestProb)
				{	predictedClass = i;
					bestProb = predictedDistribution[i];
				}
					}*/

			withClass += instance.getWeight();
		
			// Update counts when no class was predicted
			if (predictedClass < 0) {
				unclassified += instance.getWeight();
				return;
			}
			
			float actualClass = instance.classValue();
			updateNumericScores(makeDistribution(predictedClass)
								/*predictedDistribution*/,
								makeDistribution(actualClass),
								instance.getWeight());
	
			//propagation[counter][0] = actualClass;
			//propagation[counter][1] = predictedClass;
			
			// Update other stats
			confusionMatrix[(int) actualClass][(int) predictedClass] += 
				instance.getWeight();
	
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
	 * Creates the training set for one fold of a cross-validation
	 * on the dataset.
	 *
	 * @param instances set of training instances
	 * @param numFolds the number of folds in the cross-validation. Must
	 * be greater than 1.
	 * @param numFold 0 for the first fold, 1 for the second, ...
	 * @return the training set for a fold
	 * @exception IllegalArgumentException if the number of folds is less than 2
	 * or greater than the number of instances.
	 */
	public InstanceSet trainCV(InstanceSet instances, int numFolds, int numFold)
	{	int numInstForFold, first, offset;
	int numInstances = instances.numInstances();
	InstanceSet train;

	if (numFolds < 2)
	{	throw new IllegalArgumentException(resource.getString("folds2"));
	}
	if (numFolds > numInstances)
	{	throw new IllegalArgumentException(resource.getString("moreFolds"));
	}
	numInstForFold = numInstances / numFolds;
	if (numFold < numInstances % numFolds)
	{	numInstForFold++;
		offset = numFold;
	}
	else
	{	offset = numInstances % numFolds;
	}
	train = new InstanceSet(instances, (numInstances - numInstForFold));
	first = numFold * (numInstances / numFolds) + offset;
	instances.copyInstances(0, train, first);
	instances.copyInstances(first + numInstForFold, train, numInstances - first - numInstForFold);

	return train;
	}

	/**
	 * Creates the test set for one fold of a cross-validation on
	 * the dataset.
	 *
	 * @param instances set of training instances
	 * @param numFolds the number of folds in the cross-validation. Must
	 * be greater than 1.
	 * @param numFold 0 for the first fold, 1 for the second, ...
	 * @return the test set of instances
	 * @exception IllegalArgumentException if the number of folds is less than 2
	 * or greater than the number of instances.
	 */
	public InstanceSet testCV(InstanceSet instances,int numFolds, int numFold)
	{ int numInstForFold, first, offset;
	int numInstances = instances.numInstances();
	InstanceSet test;

	if (numFolds < 2)
	{ throw new IllegalArgumentException(resource.getString("folds2"));
	}
	if (numFolds > numInstances)
	{ throw new IllegalArgumentException(resource.getString("moreFolds"));
	}
	numInstForFold = numInstances / numFolds;
	if (numFold < numInstances % numFolds)
	{ numInstForFold++;
		offset = numFold;
	}
	else
		offset = numInstances % numFolds;
	test = new InstanceSet(instances, numInstForFold);
	first = numFold * (numInstances / numFolds) + offset;
	instances.copyInstances(first, test, numInstForFold);
	return test;
	}

	/**
	 * Stratifies a set of instances according to its class values
	 * if the class attribute is nominal (so that afterwards a
	 * stratified cross-validation can be performed).
	 *
	 * @param instances set of training instances
	 * @param numFolds the number of folds in the cross-validation
	 * @exception UnassignedClassException if the class is not set
	 */
	public final void stratify(InstanceSet instances, int numFolds)
	{ if (numFolds <= 0)
	{ throw new IllegalArgumentException(resource.getString("folds1"));
	}
	if (instances.getClassIndex() < 0)
	{ throw new UnassignedClassException(resource.getString("classNegative"));
	}
	if (instances.getClassAttribute().isNominal())
	{ // sort by class
		int index = 1;
		int numInstances = instances.numInstances();
		while (index < numInstances)
		{	Instance instance1 = instances.getInstance(index - 1);
		for (int j = index; j < numInstances; j++)
		{	Instance instance2 = instances.getInstance(j);
			if ((instance1.classValue() == instance2.classValue()) ||
				(instance1.classIsMissing() && instance2.classIsMissing()))
			{	instances.swap(index,j);
				index++;
			}
		}
		index++;
		}
		stratStep(instances,numFolds);
	}
	}

	/**
	 * Help function needed for stratification of set.
	 *
	 * @param instances set of training instances
	 * @param numFolds the number of folds for the stratification
	 */
	private void stratStep (InstanceSet instances, int numFolds)
	{ int numInstances = instances.numInstances();
	Instance[] newVec = new Instance[numInstances];
	int start = 0, j, i=0;

	// create stratified batch
	while (newVec.length < numInstances)
	{	j = start;
		while (j < numInstances)
		{	newVec[i]= instances.getInstance(j);
			j += numFolds;
		}
		start++;
		i++;
	}
	instances.setInstances(newVec);
	}

	/**
	 * Returns a copy of the confusion matrix.
	 *
	 * @return a copy of the confusion matrix as a two-dimensional array
	 */
	public double[][] confusionMatrix()
	{ double[][] newMatrix = new double[confusionMatrix.length][0];

	for (int i = 0; i < confusionMatrix.length; i++)
	{ newMatrix[i] = new double[confusionMatrix[i].length];
		System.arraycopy(confusionMatrix[i], 0, newMatrix[i], 0,
				 confusionMatrix[i].length);
	}
	return newMatrix;
	}

	/**
	 * Generates a breakdown of the accuracy for each class,
	 * incorporating various information-retrieval statistics, such as
	 * true/false positive rate.	Should be
	 * useful for ROC curves.
	 *
	 * @return the statistics presented as a string
	 * @exception Exception if the class is numeric
	 */
	public String toClassDetailsString() throws Exception
	{ if (!classIsNominal)
	{ throw new Exception(resource.getString("noMatrix"));
	}
	StringBuffer text = new StringBuffer(resource.getString("accuracy"));
	text.append("\nTP Rate	 FP Rate	 TN Rate	 FN Rate	 Class\n");
	for(int i = 0; i < numClasses; i++)
	{ text.append(Utils.doubleToString(truePositiveRate(i), 7, 3)).append("	 ");
		text.append(Utils.doubleToString(falsePositiveRate(i), 7, 3)).append("	");
		text.append(Utils.doubleToString(trueNegativeRate(i), 7, 3)).append(" 	");
		text.append(Utils.doubleToString(falseNegativeRate(i), 7, 3)).append("	");
		text.append(classNames[i]).append('\n');
	}
	return text.toString();
	}

	/**
	 * Calculate the number of true positives with respect to a particular class.
	 * This is defined as correctly classified positives
	 *
	 * @param positiveClass the index of the class to consider as "positive"
	 * @return the number of true positives
	 */
	public int numTruePositives(int positiveClass) {
		int correct = 0;
		for (int j = 0; j < numClasses; j++) {
			if (j == positiveClass) {
				correct += confusionMatrix[positiveClass][j];
			}
		}
		
		return correct;
	}

	/**
	 * Calculate the true positive rate with respect to a particular class.
	 * This is defined correctly classified positives divided by total positives
	 *
	 * @param positiveClass the index of the class to consider as "positive"
	 * @return the true positive rate
	 */
	public double truePositiveRate(int positiveClass) {
		double correct = 0, total = 0;
		for (int j = 0; j < numClasses; j++) {
			if (j == positiveClass) {
				correct += confusionMatrix[positiveClass][j];
			}
			total += confusionMatrix[positiveClass][j];
		}
		if (total == 0) {
			return 0;
		}
		return correct / total;
	}

	/**
	 * Calculate the number of true negatives with respect to a particular class.
	 * This is defined as correctly classified negatives
	 *
	 * @param positiveClass the index of the class to consider as "negative"
	 * @return the number of true negatives
	 */
	public int numTrueNegatives(int negativeClass) {
		int correct = 0;
		for (int i = 0; i < numClasses; i++) {
			if (i != negativeClass) {
				for (int j = 0; j < numClasses; j++) {
					if (j != negativeClass) {
						correct += confusionMatrix[i][j];
					}
				}
			}
		}
		
		return correct;
	}

	/**
	 * Calculate the true negative rate with respect to a particular class.
	 * This is defined as correctly classified negatives divided by total negatives
	 *
	 * @param negativeClass the index of the class to consider as "negative"
	 * @return the true negative rate
	 */
	public double trueNegativeRate(int negativeClass) {
		double correct = 0, total = 0;
		for (int i = 0; i < numClasses; i++) {
			if (i != negativeClass) {
				for (int j = 0; j < numClasses; j++) {
					if (j != negativeClass) {
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
	 * @param positiveClass the index of the class to consider as "positive"
	 * @return number of false positives
	 */
	public int numFalsePositives(int positiveClass) {
		int incorrect = 0;
		for (int i = 0; i < numClasses; i++) {
			if (i != positiveClass) {
				for (int j = 0; j < numClasses; j++) {
					if (j == positiveClass) {
						incorrect += confusionMatrix[i][j];
					}
				}
			}
		}
		
		return incorrect;
	}

	/**
	 * Calculate the false positive rate with respect to a particular class.
	 * This is defined as incorrectly classified negatives divided by total negatives
	 *
	 * @param positiveClass the index of the class to consider as "positive"
	 * @return the false positive rate
	 */
	public double falsePositiveRate(int positiveClass) {
		double incorrect = 0, total = 0;
		for (int i = 0; i < numClasses; i++) {
			if (i != positiveClass) {
				for (int j = 0; j < numClasses; j++) {
					if (j == positiveClass) {
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
	 * @param negativeClass the index of the class to consider as "negative"
	 * @return the number of false negatives
	 */
	public int numFalseNegatives(int negativeClass) {
		int incorrect = 0;
		for (int i = 0; i < numClasses; i++) {
			if (i == negativeClass) {
				for (int j = 0; j < numClasses; j++) {
					if (j != negativeClass) {
						incorrect += confusionMatrix[i][j];
					}
				}
			}
		}
		
		return incorrect;
	}

	/**
	 * Calculate the false negative rate with respect to a particular class.
	 * This is defined as incorrectly classified positives divided by total positives
	 *
	 * @param negativeClass the index of the class to consider as "negative"
	 * @return the false negative rate
	 */
	public double falseNegativeRate(int negativeClass) {
		double incorrect = 0, total = 0;
		for (int i = 0; i < numClasses; i++) {
			if (i == negativeClass) {
				for (int j = 0; j < numClasses; j++) {
					if (j != negativeClass) {
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
	 * Calculate the precision regarding to a desired class. Precision is
	 * defined as the number of True Positive  divided by the sum of the number
	 * of True Positive and the number of False Positive (TP/(TP+FP)).
	 * 
	 * @param positiveClass The index of the positive class.
	 * @return The precision.
	 */
	public double getPrecision(int positiveClass) {
		double numTruePositives = numTruePositives(positiveClass);
		double numFalsePositives = numFalsePositives(positiveClass);
		
		return numTruePositives / (numTruePositives + numFalsePositives);
	}
	
	/**
	 * Calculate the recall regarding to a desired class. Recall is the same as
	 * the True Positive rate.
	 * 
	 * @param positiveClass The index of the positive class.
	 * @return The recall.
	 */
	public double getRecall(int positiveClass) {
		return truePositiveRate(positiveClass);
	}
	
	/**
	 * Calculate the F-score regarding to a desired class. F-score is the
	 * precision multiplyed by the recall.
	 * 
	 * @param positiveClass The index of the positive class.
	 * @return The F-score.
	 */
	public double getFScore(int positiveClass) {
		return getPrecision(positiveClass) * getRecall(positiveClass);
	}
	
	/**
	 * Calculate the accuracy regarding to a desired class. Accuracy is defined
	 * as the number of True Positive plus the number of true negative divided
	 * by the number of cases ((TP+TN)/(P+N)).
	 * 
	 * @param positiveClass The index of the positive class.
	 * @return The accuracy.
	 */
	public double getAccuracy(int positiveClass) {
		double numTruePositives = numTruePositives(positiveClass);
		double numTrueNegatives = numTrueNegatives(positiveClass);
		
		return (numTruePositives + numTrueNegatives) / instanceSet.numInstances;
	}
	
	/**
	 * Calculate the sensitivity regarding to a desired class. Sensitivity is
	 * the same as the True Positive rate.
	 * 
	 * @param positiveClass The index of the positive class.
	 * @return The sensitivity.
	 */
	public double getSensitivity(int positiveClass) {
		return truePositiveRate(positiveClass);
	}
	
	/**
	 * Calculate the specificity regarding to a desired class. Specificity is
	 * the same as 1 - False Positive rate.
	 * 
	 * @param positiveClass The index of the positive class.
	 * @return The specificity.
	 */
	public double getSpecificity(int positiveClass) {
		return 1 - falsePositiveRate(positiveClass);
	}
	
	/**
	 * Calculate the positive predictive value regarding to a desired class.
	 * The positive predictive value is the same as the precision.
	 * 
	 * @param positiveClass The index of the positive class.
	 * @return The positive predictive value.
	 */
	public double getPositivePredictiveValue(int positiveClass) {
		return getPrecision(positiveClass);
	}
	
	
	/**
	 * Outputs the performance statistics as a classification confusion
	 * matrix. For each class value, shows the distribution of
	 * predicted class values.
	 *
	 * @return the confusion matrix as a String
	 * @exception Exception if the class is numeric
	 */
	public String toMatrixString() throws Exception
	{ StringBuffer text = new StringBuffer(resource.getString("matrix"));
	if (!classIsNominal)
	{ throw new Exception(resource.getString("noMatrix"));
	}

	char[] IDChars = {'a','b','c','d','e','f','g','h','i','j',
						'k','l','m','n','o','p','q','r','s','t',
						'u','v','w','x','y','z'};
	int IDWidth;
	boolean fractional = false;

	// Find the maximum value in the matrix
	// and check for fractional display requirement
	double maxval = 0;
	for(int i = 0; i < numClasses; i++)
	{	for(int j = 0; j < numClasses; j++)
		{	double current = confusionMatrix[i][j];
			if (current < 0)
			{	current *= -10;
			}
			if (current > maxval)
			{	maxval = current;
			}
			double fract = current - Math.rint(current);
			if (!fractional && ((Math.log(fract) / Math.log(10)) >= -2))
			{	fractional = true;
			}
		}
	}

	IDWidth = 1 + Math.max((int)(Math.log(maxval)/Math.log(10)+(fractional ? 3 : 0)),(int)(Math.log(numClasses) / Math.log(IDChars.length)));
	for(int i = 0; i < numClasses; i++)
	{	if (fractional)
		{	text.append(" ").append(num2ByteID(i,IDChars,IDWidth - 3)).append("	 ");
		}
		else
		{	text.append(" ").append(num2ByteID(i,IDChars,IDWidth));
		}
	}
	text.append("	 <-- classified as\n");
	for(int i = 0; i< numClasses; i++)
	{	for(int j = 0; j < numClasses; j++)
		{	text.append(" ").append(Utils.doubleToString(confusionMatrix[i][j],IDWidth,(fractional ? 2 : 0)));
		}
		text.append(" | ").append(num2ByteID(i,IDChars,IDWidth)).append(" = ").append(classNames[i]).append("\n");
	}

	return text.toString();
	}

	/**
	 * Method for generating indices for the confusion matrix.
	 *
	 * @param num integer to format
	 * @param IDChars Number of chars in the new String
	 * @param IDWidth The width of the new String
	 * @return the formatted integer as a string
	 */
	private String num2ByteID(int num,char [] IDChars,int IDWidth)
	{	char ID [] = new char [IDWidth];
	int i;

	for(i = IDWidth - 1; i >=0; i--)
	{	ID[i] = IDChars[num % IDChars.length];
		num = num / IDChars.length - 1;
		if (num < 0)
		{	break;
		}
	}
	for(i--; i >= 0; i--)
	{	ID[i] = ' ';
	}
	return new String(ID);
	}

	public int maxCount()
	{
		return numInstances;
	}

	public boolean next()
	{
		if (counter==numInstances)
		{
			return false;
		}
		else
		{
			try
			{
				evaluateModelOnce(classifier,instanceSet.getInstance(counter));
				counter++;
				return true;
			}
			catch(Exception e)
			{
				return false;
			}
		}
	}

	public void cancel()
	{
		counter=0;
	}
	
	public void computeROCCurve()
	{
		/*for (int i=0;i<numInstances;i++)
		{
			for (int j=0;j<numClasses;j++)
			{
				System.out.println("j = "+propagationResults[i][j]);
			}
			System.out.println("actualClass = "+propagation[i][0]);
			System.out.println("predictedClass = "+propagation[i][1]);
			System.out.println("i = "+i);
		}*/
		ArrayList[] vet1 = new ArrayList[numClasses];//float
		ArrayList[] vet2 = new ArrayList[numClasses];//boolean
		for (int j=0;j<numClasses;j++)
		{
			vet1[j] = new ArrayList();
			vet2[j] = new ArrayList();
		}
		for (int i=0;i<numInstances;i++)
		{
			//vet1[propagation[i][1]].add(new Float(propagationResults[i][propagation[i][1]]));
			//vet2[propagation[i][1]].add(new Boolean(propagation[i][0]==propagation[i][1]));
		}
		double[][] v3 = new double[numClasses][];
		boolean[][] v4 = new boolean[numClasses][];
		boolean[][] v5 = new boolean[numClasses][];
		for (int j=0;j<numClasses;j++)
		{
			/*System.out.println("class = "+j);
			for(int i=0;i<vet1[j].size();i++)
			{
				System.out.println("Valor = "+vet1[j].get(i));
				System.out.println("Flag = "+vet2[j].get(i));
			}*/
			v3[j] = new double[vet1[j].size()];
			v4[j] = new boolean[vet1[j].size()];
			v5[j] = new boolean[vet1[j].size()];
		}
		for (int j=0;j<numClasses;j++)
		{
			for(int i=0;i<v3[j].length;i++)
			{
				v3[j][i] = ((Float)vet1[j].get(i)).floatValue();
				v4[j][i] = ((Boolean)vet2[j].get(i)).booleanValue();
			}
		}				
		/*for (int j=0;j<numClasses;j++)
		{
			int[] res = Utils.sort(v3[j]);
			for (int i=0;i<res.length;i++)
			{
				v5[i] = v4[res[i]];	
			}
		}				
		for (int j=0;j<numClasses;j++)
		{
			System.out.println("Class = "+j);
			for(int i=0;i<v3[j].length;i++)
			{
				System.out.println("Valor = "+v3[j][i]);
				System.out.println("Flag = "+v5[j][i]);
			}
		}*/				
	}

}