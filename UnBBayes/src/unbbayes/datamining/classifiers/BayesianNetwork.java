package unbbayes.datamining.classifiers;

import unbbayes.datamining.datamanipulation.*;
import unbbayes.jprs.jbn.*;

/**
 * Class implementing a Bayesian Network.
 *
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version $1.0 $ (17/02/2002)
 */
public class BayesianNetwork extends BayesianLearning
{	private Attribute classAttribute;

	private ProbabilisticNode classNode;

	private ProbabilisticNetwork net;

	private int numNodes;

	public BayesianNetwork(ProbabilisticNetwork net) throws Exception
	{	this.net = net;
		this.net.compile();
		numNodes = net.getNos().size();
	}

	private boolean compareClasses(Attribute classAttribute,ProbabilisticNode node)
	{	boolean equals = false;
		if (classAttribute.getAttributeName().equals(node.getName()))
		{	int numValues = classAttribute.numValues();
			classNode = node;
			equals = true;
			for (int i=0; i<numValues; i++)
			{	if (!classAttribute.value(i).equals(node.getStateAt(i)))
				{	equals = false;
					classNode = null;
					break;
				}
			}
		}
		return equals;
	}

	/**
   	* Generates the classifier.
   	*
   	* @param instances Set of instances serving as training data
   	* @exception Exception if the classifier has not been generated successfully
   	*/
  	public void buildClassifier(InstanceSet instances) throws Exception
	{	}

  	/**
  	 * Calculates the class membership probabilities for the given test instance.
  	 *
  	 * @param instance the instance to be classified
  	 * @return predicted class probability distribution
  	 * @exception Exception if distribution can't be computed
  	 */
  	public float[] distributionForInstance(Instance instance) throws Exception
	{	int numClasses = classNode.getStatesSize();
		float[] probs = new float[numClasses];
  	  	int i;

		net.initialize();

		InstanceSet data = instance.getDataset();
		int numAttributes = data.numAttributes();
		for (i=0;i<numAttributes;i++)
		{	Attribute att = data.getAttribute(i);
			if (!instance.isMissing(i) && (!att.equals(classAttribute)))
			{	ProbabilisticNode node = (ProbabilisticNode)net.getNode(att.getAttributeName());
				node.addFinding(instance.getValue(i));
			}
		}
		net.updateEvidences();
		for (i=0;i<numClasses;i++)
		{	probs[i] = (float)classNode.getMarginalAt(i);
		}
		return probs;
  	}

	/**
   	* Returns a description of the classifier.
   	*
   	* @return a description of the classifier as a string.
   	*/
	public String toString()
	{	try
		{	StringBuffer text = new StringBuffer("Bayesian Network\n");
      		int numNodes = net.getNos().size();
			for (int i=0; i<numNodes; i++)
			{	ProbabilisticNode node = (ProbabilisticNode)net.getNodeAt(i);
				text.append(node+"\n");
			}
			return text.toString();
    	}
		catch (Exception e)
		{	return resource.getString("exception5");
    	}
	}

	public Attribute getClassAttribute()
	{	return classAttribute;
	}

	public void setClassAttribute(Attribute classAttribute) throws Exception
	{	int i;
		// Procura pela classe
		boolean result = false;
		for (i=0; i<numNodes; i++)
		{	result = compareClasses(classAttribute,(ProbabilisticNode)net.getNodeAt(i));
			if (result == true)
			{	this.classAttribute = classAttribute;
				break;
			}
		}
		if (result == false)
		{	throw new Exception("Attributo classe não encontrado na rede");
		}
	}

}