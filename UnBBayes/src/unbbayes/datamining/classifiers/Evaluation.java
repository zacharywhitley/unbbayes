package unbbayes.datamining.classifiers;

import java.util.*;
import unbbayes.datamining.datamanipulation.*;

/**
 * Class for evaluating machine learning classsifiers
 *
 *  @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 *  @version $1.0 $ (17/02/2002)
 */
public class Evaluation
{ /** Load resource file from this package */
  private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.datamining.classifiers.resources.ClassifiersResource");
  
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
  private int [][] confusionMatrix;
  
  /** The names of the classes. */
  private String [] classNames;

  /** The instance set that will be manipulated. */
  private InstanceSet data;

  	/**
	* Initializes all the counters for the evaluation.
   	*
   	* @param data set of training instances, to get some header
   	* information and prior class distribution information
   	* @exception Exception if the class is not defined
   	*/
  	public Evaluation(InstanceSet data) throws Exception
	{	this.data = data;
		numClasses = data.numClasses();
    	classIsNominal = data.getClassAttribute().isNominal();
		if (classIsNominal) 
		{	confusionMatrix = new int [numClasses][numClasses];
			classNames = new String [numClasses];
      		for(int i = 0; i < numClasses; i++) 
			{	classNames[i] = data.getClassAttribute().value(i);	
      		}
		}
    }

	/**
   	* Evaluates the classifier on a given set of instances.
   	*
   	* @param classifier machine learning classifier
   	* @exception Exception if model could not be evaluated
   	* successfully
   	*/
  	public void evaluateModel(Classifier classifier) throws Exception
	{	for (int i = 0; i < data.numInstances(); i++)
		{	evaluateModelOnce(classifier,data.getInstance(i));
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
	{	int numInstances = testData.numInstances();
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
  	public short evaluateModelOnce(Classifier classifier,Instance instance) throws Exception
	{	Instance classMissing = instance;
    	short pred=0;
    	if (classIsNominal)
		{	if (classifier instanceof BayesianLearning)
			{	float[] dist = ((BayesianLearning)classifier).distributionForInstance(classMissing);
				pred = (short)Utils.maxIndex(dist);
				updateStatsForClassifier(dist,instance);
      		}
			else
			{	pred = classifier.classifyInstance(classMissing);
				updateStatsForClassifier(makeDistribution(pred),instance);
      		}
    	}
		else
		{	//Class is numeric
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
  	private float[] makeDistribution(short predictedClass)
	{	float[] result = new float[numClasses];
  	  	if (Instance.isMissingValue(predictedClass))
		{	return result;
  	  	}
  	  	if (classIsNominal)
		{	result[(int)predictedClass] = 1.0f;
  	  	}
		else
		{	result[0] = predictedClass;
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

    	try
		{	text.append(resource.getString("correctly"));
	  		text.append(Utils.doubleToString(correct(), 12, 4) + " " + Utils.doubleToString(pctCorrect(),12, 4) + " %\n");
	  		text.append(resource.getString("incorrectly"));
	  		text.append(Utils.doubleToString(incorrect(), 12, 4) + " " + Utils.doubleToString(pctIncorrect(),12, 4) +" %\n");

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
  private void updateStatsForClassifier(float[] predictedDistribution,Instance instance) throws Exception
  {	int actualClass = (int)instance.classValue();
    
    if (!instance.classIsMissing())
	{	// Determine the predicted class (doesn't detect multiple classifications)
      	int predictedClass = -1;
      	float bestProb = 0.0f;
      	for(int i = 0; i < numClasses; i++)
		{	if (predictedDistribution[i] > bestProb)
			{	predictedClass = i;
	  			bestProb = predictedDistribution[i];
			}
      	}

      	withClass += instance.getWeight();

      	// Update counts when no class was predicted
      	if (predictedClass < 0)
		{	unclassified += instance.getWeight();
			return;
      	}

		// Update other stats
		confusionMatrix[actualClass][predictedClass] += instance.getWeight();
      	if (predictedClass != actualClass)
	  	{	incorrect += instance.getWeight();
      	}
	  	else
	  	{	correct += instance.getWeight();
      	}
    }
	else
	{	missingClass += instance.getWeight();
    }
  }

  /**
   * Performs a (stratified if class is nominal) cross-validation
   * for a classifier on a set of instances.
   *
   * @param classifier the classifier with any options set.
   * @param numFolds the number of folds for the cross-validation
   * @exception Exception if a classifier could not be generated
   * successfully or the class is not defined
   */
  public void crossValidateModel(Classifier classifier,int numFolds) throws Exception
  {	// Make a copy of the data we can reorder
    //InstanceSet instances = new InstanceSet(data);
    InstanceSet instances = new InstanceSet(data,data.numInstances());
	int numInstances = data.numInstances();
	for (int i=0;i<numInstances;i++)
	{	Instance inst = data.getInstance(i);
		int originalWeight = inst.getWeight();
		inst.setWeight(1);
		for (int j=0;j<originalWeight;j++)			
			instances.add(inst);
	}
	data = new InstanceSet(instances);
	
	instances.randomize(new Random(42));
	if (instances.getClassAttribute().isNominal())
	{	stratify(instances,numFolds);
    }
    // Do the folds
    for (int i = 0; i < numFolds; i++)
	{	InstanceSet train = trainCV(instances,numFolds, i);
      	classifier.buildClassifier(train);
      	InstanceSet test = testCV(instances,numFolds, i);
      	evaluateModel(classifier, test);
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
    {  offset = numInstances % numFolds;
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
	ArrayList newVec = new ArrayList(numInstances);
    int start = 0, j;

    // create stratified batch
    while (newVec.size() < numInstances) 
	{	j = start;
      	while (j < numInstances) 
		{	newVec.add(instances.getInstance(j));
			j += numFolds;
      	}
      	start++;
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
   * true/false positive rate.  Should be
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
	text.append("\nTP Rate   FP Rate   TN Rate   FN Rate\n");
    for(int i = 0; i < numClasses; i++) 
	{ text.append(Utils.doubleToString(truePositiveRate(i), 7, 3)).append("   ");
      text.append(Utils.doubleToString(falsePositiveRate(i), 7, 3)).append("    ");
      text.append(Utils.doubleToString(trueNegativeRate(i), 7, 3)).append("   ");
      text.append(Utils.doubleToString(falseNegativeRate(i), 7, 3)).append("    ");
      text.append(classNames[i]).append('\n');
    }
    return text.toString();
  }
  
  /**
   * Calculate the number of true positives with respect to a particular class. 
   * This is defined as correctly classified positives
   *
   * @param classIndex the index of the class to consider as "positive"
   * @return the number of true positives
   */
  public int numTruePositives(int classIndex) 
  { int correct = 0;
    for (int j = 0; j < numClasses; j++) 
	{ 	if (j == classIndex) 
		{	correct += confusionMatrix[classIndex][j];
      	}
    }
    return correct;
  }

  /**
   * Calculate the true positive rate with respect to a particular class. 
   * This is defined correctly classified positives divided by total positives
   *
   * @param classIndex the index of the class to consider as "positive"
   * @return the true positive rate
   */
  public double truePositiveRate(int classIndex) 
  { double correct = 0, total = 0;
    for (int j = 0; j < numClasses; j++) 
	{ 	if (j == classIndex) 
		{	correct += confusionMatrix[classIndex][j];
      	}
      	total += confusionMatrix[classIndex][j];
    }
    if (total == 0) 
	{ return 0;
    }
    return correct / total;
  }

  /**
   * Calculate the number of true negatives with respect to a particular class. 
   * This is defined as correctly classified negatives 
   *
   * @param classIndex the index of the class to consider as "negative"
   * @return the number of true negatives
   */
  public int numTrueNegatives(int classIndex) 
  { int correct = 0;
    for (int i = 0; i < numClasses; i++) 
	{ 	if (i != classIndex) 
		{	for (int j = 0; j < numClasses; j++) 
			{	if (j != classIndex) 
				{	correct += confusionMatrix[i][j];
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
   * @param classIndex the index of the class to consider as "negative"
   * @return the true negative rate
   */
  public double trueNegativeRate(int classIndex) 
  { double correct = 0, total = 0;
    for (int i = 0; i < numClasses; i++) 
	{ 	if (i != classIndex) 
		{	for (int j = 0; j < numClasses; j++) 
			{	if (j != classIndex) 
				{	correct += confusionMatrix[i][j];
	  			}
	  			total += confusionMatrix[i][j];
			}
      	}
    }
    if (total == 0) 
	{ return 0;
    }
    return correct / total;
  }

  /**
   * Calculate number of false positives with respect to a particular class. 
   * This is defined as incorrectly classified negatives
   *
   * @param classIndex the index of the class to consider as "positive"
   * @return number of false positives
   */
  public int numFalsePositives(int classIndex) 
  { int incorrect = 0;
    for (int i = 0; i < numClasses; i++) 
	{ 	if (i != classIndex) 
		{	for (int j = 0; j < numClasses; j++) 
			{	if (j == classIndex) 
				{	incorrect += confusionMatrix[i][j];
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
   * @param classIndex the index of the class to consider as "positive"
   * @return the false positive rate
   */
  public double falsePositiveRate(int classIndex) 
  { double incorrect = 0, total = 0;
    for (int i = 0; i < numClasses; i++) 
	{	if (i != classIndex) 
		{	for (int j = 0; j < numClasses; j++) 
			{	if (j == classIndex) 
				{	incorrect += confusionMatrix[i][j];
	  			}
	  			total += confusionMatrix[i][j];
			}
      	}
    }
    if (total == 0) 
	{ return 0;
    }
    return incorrect / total;
  }

  /**
   * Calculate number of false negatives with respect to a particular class. 
   * This is defined as incorrectly classified positives
   *
   * @param classIndex the index of the class to consider as "negative"
   * @return the number of false negatives
   */
  public int numFalseNegatives(int classIndex) 
  { int incorrect = 0;
    for (int i = 0; i < numClasses; i++) 
	{	if (i == classIndex) 
		{	for (int j = 0; j < numClasses; j++) 
			{	if (j != classIndex) 
				{	incorrect += confusionMatrix[i][j];
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
   * @param classIndex the index of the class to consider as "negative"
   * @return the false negative rate
   */
  public double falseNegativeRate(int classIndex) 
  { double incorrect = 0, total = 0;
    for (int i = 0; i < numClasses; i++) 
	{	if (i == classIndex) 
		{	for (int j = 0; j < numClasses; j++) 
			{	if (j != classIndex) 
				{	incorrect += confusionMatrix[i][j];
	  			}
	  			total += confusionMatrix[i][j];
			}
      	}
    }
    if (total == 0) 
	{ return 0;
    }
    return incorrect / total;
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

    for(int i = 0; i< numClasses; i++) 
	{	for(int j = 0; j < numClasses; j++) 
		{	text.append(" ").append("  "+confusionMatrix[i][j]);
      	}
      	text.append("   =   ").append(classNames[i]).append("\n");
    }
    return text.toString();
  }

}