package unbbayes.datamining.classifiers;

import java.util.*;

import unbbayes.datamining.datamanipulation.*;

/**
 * Class implementing an Naive Bayes classifier.
 *
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version $1.0 $ (17/02/2002)
 */
public class NaiveBayes extends BayesianLearning
{	/** Load resources file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.datamining.classifiers.resources.ClassifiersResource");

	/** All the counts for nominal attributes. */
  	private float [][][] counts;

  	/** The means for numeric attributes. */
  	private float [][] means;

  	/** The standard deviations for numeric attributes. */
  	private float [][] devs;

  	/** The prior probabilities of the classes. */
  	private float [] priors;

  	/** The instances used for training. */
  	private InstanceSet instances;

  	private Attribute classAttribute;

	/** Constant for normal distribution. */
  	private static float NORM_CONST = (float)Math.sqrt(2 * Math.PI);

	/**
   	* Generates the classifier.
   	*
   	* @param instances Set of instances serving as training data
   	* @exception Exception if the classifier has not been generated successfully
   	*/
  	public void buildClassifier(InstanceSet inst) throws Exception
	{	int attIndex = 0;
    	float sum;

    	if (inst.getClassAttribute().isNumeric())
		{	throw new Exception(resource.getString("exception1"));
    	}

		instances = new InstanceSet(inst);
		classAttribute = instances.getClassAttribute();

		// Reserve space
    	counts = new float[instances.numClasses()][instances.numAttributes() - 1][0];
    	means = new float[instances.numClasses()][instances.numAttributes() - 1];
    	devs = new float[instances.numClasses()][instances.numAttributes() - 1];
    	priors = new float[instances.numClasses()];
    	Enumeration enum = instances.enumerateAttributes();
    	while (enum.hasMoreElements())
		{	Attribute attribute = (Attribute) enum.nextElement();
      		if (attribute.isNominal())
			{	for (int j = 0; j < instances.numClasses(); j++)
				{	counts[j][attIndex] = new float[attribute.numValues()];
				}
      		}
			else
			{	for (int j = 0; j < instances.numClasses(); j++)
				{	counts[j][attIndex] = new float[1];
				}
      		}
      		attIndex++;
    	}

		// Compute counts and sums
    	Enumeration enumInsts = instances.enumerateInstances();
    	while (enumInsts.hasMoreElements())
		{	Instance instance = (Instance) enumInsts.nextElement();
      		if (!instance.classIsMissing())
			{	Enumeration enumAtts = instances.enumerateAttributes();
				attIndex = 0;
				while (enumAtts.hasMoreElements())
				{	Attribute attribute = (Attribute) enumAtts.nextElement();
	  				if (!instance.isMissing(attribute))
					{	if (attribute.isNominal())
						{	counts[(int)instance.classValue()][attIndex][(int)instance.getValue(attribute)] += instance.getWeight();
	    				}
						else
						{	means[(int)instance.classValue()][attIndex] += (Float.parseFloat(instance.stringValue(attribute))*instance.getWeight());
	      					counts[(int)instance.classValue()][attIndex][0] += instance.getWeight();
	    				}
	  				}
	  				attIndex++;
				}
				priors[(int)instance.classValue()] += instance.getWeight();
      		}
    	}

    	// Compute means
    	Enumeration enumAtts = instances.enumerateAttributes();
    	attIndex = 0;
    	while (enumAtts.hasMoreElements())
		{	Attribute attribute = (Attribute) enumAtts.nextElement();
      		if (attribute.isNumeric())
			{	for (int j = 0; j < instances.numClasses(); j++)
				{	if (counts[j][attIndex][0] < 2)
					{	throw new Exception(resource.getString("attribute") + attribute.getAttributeName() + resource.getString("exception2") +	instances.getClassAttribute().value(j));
	  				}
	  				means[j][attIndex] /= counts[j][attIndex][0];
				}
      		}
      		attIndex++;
    	}

    	// Compute standard deviations
    	enumInsts = instances.enumerateInstances();
    	while (enumInsts.hasMoreElements())
		{	Instance instance = (Instance) enumInsts.nextElement();
      		if (!instance.classIsMissing())
			{	enumAtts = instances.enumerateAttributes();
				attIndex = 0;
				while (enumAtts.hasMoreElements())
				{	Attribute attribute = (Attribute) enumAtts.nextElement();
	  				if (!instance.isMissing(attribute))
					{	if (attribute.isNumeric())
						{	float value = Float.parseFloat(instance.stringValue(attribute));
							devs[(int)instance.classValue()][attIndex] += (means[(int)instance.classValue()][attIndex] - value) * (means[(int)instance.classValue()][attIndex] - value);
	    				}
	  				}
	  				attIndex++;
				}
      		}
    	}
    	enumAtts = instances.enumerateAttributes();
    	attIndex = 0;
    	while (enumAtts.hasMoreElements())
		{	Attribute attribute = (Attribute) enumAtts.nextElement();
      		if (attribute.isNumeric())
			{	for (int j = 0; j < instances.numClasses(); j++)
				{	if (devs[j][attIndex] <= 0)
					{	throw new Exception(resource.getString("attribute") + attribute.getAttributeName() + resource.getString("exception3") + instances.getClassAttribute().value(j));
	  				}
	  				else
					{	devs[j][attIndex] /= counts[j][attIndex][0] - 1;						
	    				devs[j][attIndex] = (float)Math.sqrt(devs[j][attIndex]);
	  				}
				}
      		}
      		attIndex++;
    	}

    	// Normalize counts
    	enumAtts = instances.enumerateAttributes();
    	attIndex = 0;
    	while (enumAtts.hasMoreElements())
		{	Attribute attribute = (Attribute) enumAtts.nextElement();
      		if (attribute.isNominal())
			{	for (int j = 0; j < instances.numClasses(); j++)
				{	sum = Utils.sum(counts[j][attIndex]);
	  				for (int i = 0; i < attribute.numValues(); i++)
					{	counts[j][attIndex][i] = (counts[j][attIndex][i] + 1) / (sum + (float)attribute.numValues());
	  				}
				}
      		}
      		attIndex++;
    	}

    	// Normalize priors
    	sum = Utils.sum(priors);
		for (int j = 0; j < instances.numClasses(); j++)
      		priors[j] = (priors[j] + 1)	/ (sum + (float)instances.numClasses());
  	}

  	/**
  	 * Calculates the class membership probabilities for the given test instance.
  	 *
  	 * @param instance the instance to be classified
  	 * @return predicted class probability distribution
  	 * @exception Exception if distribution can't be computed
  	 */
  	public float[] distributionForInstance(Instance instance) throws Exception
	{	int numClasses = instances.numClasses();
		float[] probs = new float[numClasses];
  	  	int attIndex;

  	  	for (int j = 0; j < numClasses; j++)
		{	probs[j] = 1;
  	    	Enumeration enumAtts = instances.enumerateAttributes();
  	    	attIndex = 0;
  	    	while (enumAtts.hasMoreElements())
			{	Attribute attribute = (Attribute) enumAtts.nextElement();
  				if (!instance.isMissing(attribute))
				{	if (attribute.isNominal())
					{	probs[j] *= counts[j][attIndex][(int)instance.getValue(attribute)];
  	  				}
					else
					{	float value = Float.parseFloat(instance.stringValue(attribute));
						probs[j] *= normalDens(value,means[j][attIndex],devs[j][attIndex]);
					}
  				}
  				attIndex++;
  	    	}
  	    	probs[j] *= priors[j];
  	  	}

  	  	// Normalize probabilities
  	  	Utils.normalize(probs);

  	  	return probs;
  	}

	/**
   	* Returns a description of the classifier.
   	*
   	* @return a description of the classifier as a string.
   	*/
  	public String toString()
	{	if (instances == null)
		{	//return "Naive Bayes : "+resource.getString("exception4");
			return nullInstancesString();
    	}
    	try
		{	StringBuffer text = new StringBuffer("Naive Bayes");
      		int attIndex;

      		for (int i = 0; i < instances.numClasses(); i++)
			{	text.append("\n\n"+resource.getString("class") + " " + instances.getClassAttribute().value(i) + ": P(C) = " + Utils.doubleToString(priors[i], 10, 8) + "\n\n");
				Enumeration enumAtts = instances.enumerateAttributes();
				attIndex = 0;
				while (enumAtts.hasMoreElements())
				{	Attribute attribute = (Attribute) enumAtts.nextElement();
	  				text.append(resource.getString("attribute")+" " + attribute.getAttributeName() + "\n");
	  				if (attribute.isNominal())
					{	for (int j = 0; j < attribute.numValues(); j++)
						{	text.append(attribute.value(j) + "\t");
	    				}
	    				text.append("\n");
	    				for (int j = 0; j < attribute.numValues(); j++)
	      					text.append(Utils.doubleToString(counts[i][attIndex][j], 10, 8) + "\t");
	  				}
					else
					{	text.append(resource.getString("mean")+": "+ Utils.doubleToString(means[i][attIndex], 10, 8) + "\t");
	    				text.append(resource.getString("stdev")+": "+ Utils.doubleToString(devs[i][attIndex], 10, 8));
	  				}
	  				text.append("\n\n");
	  				attIndex++;
				}
      		}
      		return text.toString();
    	}
		catch (Exception e)
		{	return resource.getString("exception5");
    	}
  	}

	private String nullInstancesString()
	{	try
		{	StringBuffer text = new StringBuffer("Naive Bayes");
      		for (int i = 0; i < priors.length; i++)
			{	text.append("\n\n"+resource.getString("class") + " " + i + ": P(C) = " + Utils.doubleToString(priors[i], 10, 8) + "\n\n");
				if (counts != null)
				{	for (int attIndex=0; attIndex<counts[i].length; attIndex++)
					{	text.append(resource.getString("attribute")+" " + attIndex + "\n");
						for (int j = 0; j < counts[i][attIndex].length; j++)
						{	text.append(j + "\t");
	    				}
	    				text.append("\n");
	    				for (int j = 0; j < counts[i][attIndex].length; j++)
	      					text.append(Utils.doubleToString(counts[i][attIndex][j], 10, 8) + "\t");
						text.append("\n\n");
					}
				}
      		}
      		return text.toString();
    	}
		catch (Exception e)
		{	return resource.getString("exception5");
    	}
	}

	/** Returns the computed priors

	    @return the computed priors.
	*/
	public float[] getPriors()
	{	return priors;
	}

	public void setPriors(float[] priors)
	{	this.priors = priors;
	}

	/** Returns the computed counts for nominal attributes

	    @return the computed counts.
	*/
	public float[][][] getCounts()
	{	return counts;
	}

	public void setCounts(float[][][] counts)
	{	this.counts = counts;
	}

	public Attribute getClassAttribute()
	{	return classAttribute;
	}

	public void setClassAttribute(Attribute classAttribute)
	{	this.classAttribute = classAttribute;
	}

	/**
	 * Density function of normal distribution.
	 */
	private float normalDens(float x, float mean, float stdDev)
	{	float diff = x - mean;

	  	return (float) ((1 / (NORM_CONST * stdDev)) * Math.exp(-(diff * diff / (2 * stdDev * stdDev))));
	}

}