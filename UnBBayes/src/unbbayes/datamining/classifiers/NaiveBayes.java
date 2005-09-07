package unbbayes.datamining.classifiers;

import java.util.*;

import unbbayes.datamining.datamanipulation.*;
import unbbayes.prs.*;
import unbbayes.prs.bn.*;

/**
 * Class implementing an Naive Bayes classifier.
 *
 * @author Mário Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version $1.0 $ (17/02/2002)
 */
public class NaiveBayes extends DistributionClassifier
{	/** Load resources file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.datamining.classifiers.resources.ClassifiersResource");

	/** All the counts for nominal attributes. */
	private float[][][] counts;

	/** The prior probabilities of the classes. */
	private float[] priors;

	/** The instances used for training. */
	private InstanceSet instances;
  	
	private ProbabilisticNode classAtt;
	private int width = 50;
	private int k = 0,i=0,j=0;
	private ProbabilisticNetwork net = new ProbabilisticNetwork("NaiveBayes");
	
	private int numAtt;
	private int numClasses;
	private int numInstances;
	private int numValues;
	private int classIndex;
	private Attribute[] attributes;
	private int attIndex;

	/**
	* Generates the classifier.
	*
	* @param instances Set of instances serving as training data
	* @exception Exception if the classifier has not been generated successfully
	*/
	public void buildClassifier(InstanceSet inst) throws Exception
	{	
		instances = inst;
		numAtt = inst.numAttributes();
		numClasses = instances.numClasses();
		numInstances = instances.numInstances();
		attributes = instances.getAttributes();
		classIndex = inst.getClassAttribute().getIndex();
		attIndex=0;
		float sum;

		boolean bool = inst.checkNumericAttributes();
		if (bool == true)
			throw new Exception(resource.getString("numericAttributesException"));

		
		// Reserve space
		counts = new float[numClasses][numAtt - 1][0];
		priors = new float[numClasses];
		for (i = 0; i < numAtt; i++)
		{
			if (attributes[i].getIndex() != classIndex)
			{
				for (j = 0; j < numClasses; j++)
				{	counts[j][attIndex] = new float[attributes[i].numValues()];
				}
				attIndex++;
			}
		}

		// Compute counts and sums
		for (i = 0; i < numInstances; i++)
		{
			Instance instance = (Instance) instances.getInstance(i);
			if (!instance.classIsMissing())
			{	
				attIndex=0;
				for (j = 0; j < numAtt; j++)
				{
					if ((attributes[j].getIndex() != classIndex)&&(!instance.isMissing(attributes[j])))
					{
						counts[(int)instance.classValue()][attIndex][(int)instance.getValue(attributes[j])] += instance.getWeight();
						attIndex++;
					}

				}															
				priors[(int)instance.classValue()] += instance.getWeight();
			}
		}
		
		attIndex=0;
		// Normalize counts
		for (k = 0; k < numAtt; k++)
		{
			if (attributes[k].getIndex() != classIndex)
			{
				for (j = 0; j < numClasses; j++)
				{	sum = Utils.sum(counts[j][attIndex]);
					numValues = attributes[k].numValues();
					for (i = 0; i < numValues; i++)
					{	counts[j][attIndex][i] = (counts[j][attIndex][i] + 1) / (sum + (float)numValues);
					}
				}
				attIndex++;
			}
			      	
		}
    		

		// Normalize priors
		sum = Utils.sum(priors);
		for (j = 0; j < numClasses; j++)
		{	priors[j] = (priors[j] + 1)	/ (sum + (float)numClasses);      	
		}
      	
		// compute bayesian network
		createProbabilisticNodeClass(attributes[classIndex]);
		k=0;
		for(int counter=0; counter<numAtt; counter++)
		{   if (attributes[counter].getIndex() != classIndex)
			{   	
				createProbabilisticNode(attributes[counter]);											
			}
		}      	
	}
  	
	/** Cria o nó classe */
	private void createProbabilisticNodeClass(Attribute att)
	{   ProbabilisticNode no = new ProbabilisticNode();
		no.setDescription(att.getAttributeName());
		no.setName(att.getAttributeName());
		numValues = att.numValues();
		for (i=0;i<numValues;i++)
		{   
			no.appendState(""+att.value(i));
		}
		if (numAtt == 1)
		{   no.setPosition(50,30);
		}
		else
		{   no.setPosition(50 + ((numAtt-2) * 50),30);
		}
		PotentialTable tab = no.getPotentialTable();
		tab.addVariable(no);
		for (i=0;i<numValues;i++)
		{   
			tab.setValue(i,priors[i]);
		}
		net.addNode(no);
		classAtt = no;
	}

	/** Cria os nós filhos */
	private void createProbabilisticNode(Attribute att)
	{   ProbabilisticNode no = new ProbabilisticNode(); // Criação do nó
		no.setDescription(att.getAttributeName());
		no.setName(att.getAttributeName());

		numValues = att.numValues();
		for (i=0;i<numValues;i++)
		{   
			no.appendState(""+att.value(i));
		}

		PotentialTable tab = no.getPotentialTable();  // Criação do Tabela de probabilidades
		tab.addVariable(no);
		no.setPosition(width,100);
		width += 100;
		net.addNode(no);
		Edge arco = new Edge(classAtt,no);
		net.addEdge(arco);

		// Inserção dos valores na tabela de probabilidades
		int[] coord = new int[numClasses];
		for (j=0;j<numClasses;j++)
		{   for (i=0;i<numValues;i++)
			{   coord[0] = i;
				coord[1] = j;
				tab.setValue(coord,counts[j][k][i]);
			}
		}
		k++;
	}


	/**
	 * Calculates the class membership probabilities for the given test instance.
	 *
	 * @param instance the instance to be classified
	 * @return predicted class probability distribution
	 * @exception Exception if distribution can't be computed
	 */
	public float[] distributionForInstance(Instance instance) throws Exception
	{	float[] probs = new float[numClasses];

		for (j = 0; j < numClasses; j++)
		{	probs[j] = 1;
  	    	
			for (i = 0; i < numAtt; i++)
			{
				if ((attributes[i].getIndex() != classIndex)&&(!instance.isMissing(attributes[i])))
				{
					probs[j] *= counts[j][i][(int)instance.getValue(attributes[i])];
				}
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
		{	StringBuilder text = new StringBuilder("Naive Bayes");
      		
			for (i = 0; i < numClasses; i++)
			{	text.append("\n\n"+resource.getString("class") + " " + instances.getClassAttribute().value(i) + ": P(C) = " + Utils.doubleToString(priors[i], 10, 8) + "\n\n");
				
				for (k = 0; k < numAtt; k++)
				{
					if (attributes[k].getIndex() != classIndex)
					{
						text.append(resource.getString("attribute")+" " + attributes[k].getAttributeName() + "\n");
						numValues = attributes[k].numValues();
						for (j = 0; j < numValues; j++)
						{	text.append(attributes[k].value(j) + "\t");
						}
						text.append("\n");
						for (j = 0; j < numValues; j++)
							text.append(Utils.doubleToString(counts[i][k][j], 10, 8) + "\t");
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

	private String nullInstancesString()
	{	try
		{	StringBuilder text = new StringBuilder("Naive Bayes");
			for (i = 0; i < priors.length; i++)
			{	text.append("\n\n"+resource.getString("class") + " " + i + ": P(C) = " + Utils.doubleToString(priors[i], 10, 8) + "\n\n");
				if (counts != null)
				{	for (int attIndex=0; attIndex<counts[i].length; attIndex++)
					{	text.append(resource.getString("attribute")+" " + attIndex + "\n");
						for (j = 0; j < counts[i][attIndex].length; j++)
						{	text.append(j + "\t");
						}
						text.append("\n");
						for (j = 0; j < counts[i][attIndex].length; j++)
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
	
	/** Retorna a rede bayesiana ProbabilisticNetwork
	  @return Um rede ProbabilisticNetwork
	*/
	public ProbabilisticNetwork getProbabilisticNetwork()
	{	  return net;
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
}