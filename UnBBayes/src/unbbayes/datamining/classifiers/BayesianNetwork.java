package unbbayes.datamining.classifiers;

import java.util.Enumeration;

import unbbayes.datamining.datamanipulation.*;
import unbbayes.jprs.jbn.*;
import unbbayes.util.*;

/**
 * Class implementing a Bayesian Network.
 *
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version $1.0 $ (17/02/2002)
 */
public class BayesianNetwork extends BayesianLearning
{	private Attribute classAttribute;

	private ProbabilisticNode classNode;
        private int classIndex = -1;

	private ProbabilisticNetwork net;

	private int numNodes;

        private int[] indexAttributes;

	public BayesianNetwork(ProbabilisticNetwork net,InstanceSet instanceSet) throws Exception
	{	this.net = net;
		this.net.compile();
		numNodes = net.size();
                indexAttributes = new int[instanceSet.numAttributes()];
                Enumeration enum = instanceSet.enumerateAttributes();
                int i = 0;
                String attributeName;
                while (enum.hasMoreElements())
                {   attributeName = ((Attribute)enum.nextElement()).getAttributeName();
                    indexAttributes[i] = net.getNodeIndex(attributeName);
                    if (indexAttributes[i] == -1)
                    {   throw new Exception("Atributo não encontrado na rede: "+attributeName);
                    }
                    i++;
                }
	}

	private boolean compareClasses(Attribute classAttribute,ProbabilisticNode node)
	{	boolean equals = false;
		if (((classAttribute.getAttributeName().compareToIgnoreCase(node.getName())) == 0) &&
                    (classAttribute.numValues() == node.getStatesSize()))
                {	classNode = node;
			equals = true;
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

		if (classIndex < 0)
                {   throw new Exception("Classe não definida.");
                }

                net.initialize();

		        NodeList nodes = net.getNos();
                int size = nodes.size();
                int j;
                for (j=0; j<size; j++)
                {   int actualNode = indexAttributes[j];
                    if (actualNode != classIndex)
                    {   ((TreeVariable)nodes.get(actualNode)).addFinding(instance.getValue(j));
                    }
                }

                net.updateEvidences();

                for (j=0;j<numClasses;j++)
		{	probs[j] = (float)classNode.getMarginalAt(j);
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
      		        int numNodes = net.size();
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
                                this.classIndex = net.getNodeIndex(classAttribute.getAttributeName());
				break;
			}
		}
		if (result == false)
		{	throw new Exception("Attributo classe não encontrado na rede");
		}
	}

}