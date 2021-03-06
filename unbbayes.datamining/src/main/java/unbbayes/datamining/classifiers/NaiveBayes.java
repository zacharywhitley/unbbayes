/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.datamining.classifiers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ResourceBundle;

import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.AttributeStats;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Utils;
import unbbayes.prs.Edge;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.exception.InvalidParentException;

/**
 * Class implementing an Naive Bayes classifier.
 *
 * @author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 * @version $1.0 $ (17/02/2002)
 */
public class NaiveBayes extends DistributionClassifier implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Load resources file from this package */
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.datamining.classifiers.resources.ClassifiersResource.class.getName());

	/** All the nominalCounts for nominal attributes. */
	private float[][][] nominalCounts;

	/** The prior probabilities of the classes. */
	private float[] priors;

	private double[][] stdDevPerClass;
	
	private double[][] meanPerClass;

	/** The instanceSet used for training. */
	private InstanceSet instanceSet;
  	
	private ProbabilisticNode classAtt;
	private int width = 50;

	private ProbabilisticNetwork net = new ProbabilisticNetwork("NaiveBayes");
	
	private int numAttributes;
	private int numClasses;
	private int numValues;
	private int classIndex;
	private Attribute[] attributes;
	private byte[] attributeType;
	private int attIndex;
	private byte nominalCounter;
	private byte numericCounter;
	private float MISSING_VALUE;

	private boolean normalize = true;

	private float[] precision;
	
	/* Generates the classifier.
	 * @param instanceSet Set of instanceSet serving as training data
	 * @exception Exception if the classifier has not been generated successfully
	*/
	public void buildClassifier(InstanceSet instanceSet) throws Exception {
		this.instanceSet = instanceSet;
		numClasses = instanceSet.numClasses();
		attributes = instanceSet.attributes;
		attributeType = instanceSet.attributeType;
		classIndex = instanceSet.classIndex;
		MISSING_VALUE = Instance.MISSING_VALUE;
		byte[] attributeType = instanceSet.attributeType;
		numAttributes = instanceSet.numAttributes;

		/* Calculate the number of nominal attributes */
		int numNominalAttributes = instanceSet.numNominalAttributes;
		
		/* Calculate the number of numeric attributes */
		int numNumericAttributes = numAttributes - numNominalAttributes;

		/*
		 * The class attribute is also a nominal. Exclude the class from the
		 * nominal attribute counting
		 */
		--numNominalAttributes;
		
		/* Get nominal distributions per class for all nominal attributes */
		nominalCounts = Utils.computeNominalDistributions(instanceSet);
		
		/* Get mean and standard deviation per class for all numeric attributes */
		attIndex = 0;
		ArrayList<double[]> stdDevMeanPerClass;
		stdDevPerClass = new double[numNumericAttributes][];
		meanPerClass = new double[numNumericAttributes][];
		precision = instanceSet.computePrecision();
		
		if (instanceSet.numInstances > 1) {
			for (int att = 0; att < numAttributes; att++) {
				if (attributeType[att] != InstanceSet.NOMINAL) {
					stdDevMeanPerClass = Utils.stdDevMeanPerClass(instanceSet, att);
					stdDevPerClass[attIndex] = stdDevMeanPerClass.get(0);
					meanPerClass[attIndex] = stdDevMeanPerClass.get(1);
					attIndex++;
				}
			}
		} else {
			for (int att = 0; att < numAttributes; att++) {
				if (attributeType[att] != InstanceSet.NOMINAL) {
					stdDevPerClass[attIndex] = new double[2];
					meanPerClass[attIndex] = new double[2];
					attIndex++;
				}
			}
		}
	
		/* Get the class distribution */
		AttributeStats[] attributeStats = instanceSet.getAttributeStats();

		/* Set the original class distribution of the instance set */
		if (originalDistribution == null) {
			priors = attributeStats[classIndex].getNominalCountsWeighted();
		} else {
			priors = originalDistribution;
		}
		
		/* Normalize nominal distribution */
		float sum;
		for (int att = 0; att < numNominalAttributes; att++) {
			for (int k = 0; k < numClasses; k++) {
				/* Sum of all counts of attribute 'att' values for the class 'k' */
				sum = Utils.sum(nominalCounts[k][att]);
				
				numValues = nominalCounts[k][att].length;
				for (int i = 0; i < numValues; i++) {
					/* Laplace estimator: ensures always Prob > 0 */
					++nominalCounts[k][att][i];
					sum += numValues;
					
					/* Normalize */
					nominalCounts[k][att][i] /= sum ;
				}
			}
		}
    		
		/* Normalize class priors */
		sum = Utils.sum(priors);
		double aux;
		for (int k = 0; k < numClasses; k++) {
			/* Laplace estimator: ensures always Prob > 0 */
			++priors[k];
			sum += numClasses;

			/* Normalize */
			aux = priors[k];
			aux /= sum;
			priors[k] = (float) aux;      	
		}
      	
		/* 
		 * Compute bayesian network
		 */
		
		/* Create class node */
		createProbabilisticNodeClass();
		
		/* Create attribute nodes (skip the class attribute) */
		nominalCounter = 0;
		numericCounter = 0;
		for(int att = 0; att < numAttributes; att++) {
			if (att != classIndex) {
					createProbabilisticNode(att, attributeType[att]);											
			}
		}
		sum = 0;
	}
  	
	/**
	 * Create the class node.
	 * 
	 * @param att attribute
	 */
	private void createProbabilisticNodeClass() {
		Attribute att = attributes[classIndex];
		ProbabilisticNode node = new ProbabilisticNode();
		node.setDescription(att.getAttributeName());
		node.setName(att.getAttributeName());
		numValues = att.numValues();
		
		for (int i = 0; i < numValues; i++) {   
			node.appendState(att.value(i));
		}
		
		if (numAttributes == 1) {
			node.setPosition(50, 30);
		} else {
			node.setPosition(50 + ((numAttributes - 2) * 50), 30);
		}
		
		PotentialTable tab = node.getProbabilityFunction();
		tab.addVariable(node);
		for (int i = 0; i < numValues; i++) {   
			tab.setValue(i, priors[i]);
		}
		net.addNode(node);
		classAtt = node;
	}

	/**
	 * Creates an attribute node.
	 * 
	 * @param att attribute
	 * @throws InvalidParentException 
	 */
	private void createProbabilisticNode(int att, byte attributeType) throws InvalidParentException {
		Attribute attribute = attributes[att];
		ProbabilisticNode node = new ProbabilisticNode();
		node.setDescription(attribute.getAttributeName());
		node.setName(attribute.getAttributeName());
		numValues = attribute.numValues();

		for (int i = 0; i < numValues; i++) {   
			node.appendState(attribute.value(i));
		}

		/* Cria��o do Tabela de probabilidades */
		PotentialTable tab = node.getProbabilityFunction();
		tab.addVariable(node);
		node.setPosition(width, 100);
		width += 100;
		net.addNode(node);
		Edge arco = new Edge(classAtt, node);
		net.addEdge(arco);

		/* Check the type of node's attribute */
		if (attributeType == InstanceSet.NOMINAL) {
			/* 
			 * The attribute is nominal. Get the nominal distribution and insert
			 * state values in the probability table
			 */
			int[] coord = new int[numClasses];
			for (int k = 0; k < numClasses; k++) {
				for (int i = 0; i < numValues; i++) {
					coord[0] = i;
					coord[1] = k;
					tab.setValue(coord, nominalCounts[k][nominalCounter][i]);
				}
			}
			++nominalCounter;
		} else {
			/* 
			 * The attribute is numeric. Set the mean and standard deviation of
			 * the node.
			 */
			node.setMean(meanPerClass[numericCounter]);
			node.setStandardDeviation(stdDevPerClass[numericCounter]);
			++numericCounter;
		}
	}

	/**
	 * Calculates the class membership probabilities for the given test instance.
	 *
	 * @param instance the instance to be classified
	 * @return predicted class probability distribution
	 * @exception Exception if distribution can't be computed
	 */
	public float[] distributionForInstance(Instance instance) throws Exception {
		float[] inst = instance.data;
		double[] probsAux = new double[numClasses];
		double stdDev;
		double mean;
		double aux;
		double maxProb = -1;

		for (int k = 0; k < numClasses; k++) {
			probsAux[k] = priors[k];
			numericCounter = 0;
			nominalCounter = 0;
			for (int att = 0; att < numAttributes; att++) {
				/* Skip class attribute missing value */
				if (att != classIndex && inst[att] != MISSING_VALUE) {
					if (attributeType[att] == InstanceSet.NOMINAL) {
						/* Nominal attribute */
						probsAux[k] *= nominalCounts[k][nominalCounter]
						                             [(int) inst[att]];
						++nominalCounter;
					} else {
						/* Numeric attribute */
						stdDev = stdDevPerClass[numericCounter][k];
						mean = meanPerClass[numericCounter][k];
//						probsAux[k] *= Utils.normalDensityFunction(inst[att],
//								stdDev, mean);
						aux = Utils.getProbability(inst[att], mean, stdDev,
								precision[numericCounter]);
						aux = Math.max(1e-75, aux);
						probsAux[k] *= aux;
						++numericCounter;
					}
				}
			}
			if (probsAux[k] > maxProb) {
				maxProb = probsAux[k];
			}
		}

		/* 
		 * Check if maximun value is less than 1e-75 (Danger of probability
		 * underflow).
		 */
		if (normalize) {
			if (maxProb < 1e-75) {
				for (int k = 0; k < numClasses; k++) {
					probsAux[k] *= 1e75;
				}
			}
		} else {
			for (int k = 0; k < numClasses; k++) {
				probsAux[k] *= 1e61;
			}
		}
		
		float[] probs = new float[numClasses];
		int size = probsAux.length;
		if (normalize) {
			/* Normalize probabilities */
			double sum = Utils.sum(probsAux);
			if (sum > 0) {
				for (int att = 0; att < size; att++) {
					aux = probsAux[att];
					aux /= sum;
					probs[att] = (float) aux;
				}
			}
		} else {
			for (int att = 0; att < size; att++) {
				probs[att] = (float) probsAux[att];
			}
		}
		if (Float.isNaN(probs[0]) || Float.isNaN(probs[1])) {
			@SuppressWarnings("unused")
			boolean fudeu = true;
		}
		
		return probs;
	}

	public double[] distributionForInstanceEma(Instance instance)
	throws Exception {
		float[] inst = instance.data;
		double[] probsAux = new double[numClasses];
		double stdDev;
		double mean;
		double aux;
		double maxProb = -1;

		for (int k = 0; k < numClasses; k++) {
			probsAux[k] = priors[k];
			numericCounter = 0;
			nominalCounter = 0;
			for (int att = 0; att < numAttributes; att++) {
				/* Skip class attribute missing value */
				if (att != classIndex && inst[att] != MISSING_VALUE) {
					if (attributeType[att] == InstanceSet.NOMINAL) {
						/* Nominal attribute */
						probsAux[k] *= nominalCounts[k][nominalCounter]
						                             [(int) inst[att]];
						++nominalCounter;
					} else {
						/* Numeric attribute */
						stdDev = stdDevPerClass[numericCounter][k];
						mean = meanPerClass[numericCounter][k];
//						probsAux[k] *= Utils.normalDensityFunction(inst[att],
//								stdDev, mean);
						aux = Utils.getProbability(inst[att], mean, stdDev,
								precision[numericCounter]);
						aux = Math.max(1e-75, aux);
						probsAux[k] *= aux;
						++numericCounter;
					}
				}
			}
			if (probsAux[k] > maxProb) {
				maxProb = probsAux[k];
			}
		}

		return probsAux;
	}

	/**
	* Returns a description of the classifier.
	*
	* @return a description of the classifier as a string.
	*/
	public String toString()
	{	if (instanceSet == null)
		{	//return "Naive Bayes : "+resource.getString("exception4");
			return nullInstancesString();
		}
		try
		{	StringBuffer text = new StringBuffer("Naive Bayes");
      		
			for (int i = 0; i < numClasses; i++)
			{	text.append("\n\n"+resource.getString("class") + " " + instanceSet.getClassAttribute().value(i) + ": P(C) = " + Utils.doubleToString(priors[i], 10, 8) + "\n\n");
				
				for (int k = 0; k < numAttributes; k++)
				{
					if (attributes[k].getIndex() != classIndex)
					{
						text.append(resource.getString("attribute")+" " + attributes[k].getAttributeName() + "\n");
						numValues = attributes[k].numValues();
						for (int j = 0; j < numValues; j++)
						{	text.append(attributes[k].value(j) + "\t");
						}
						text.append("\n");
						for (int j = 0; j < numValues; j++)
							text.append(Utils.doubleToString(nominalCounts[i][k][j], 10, 8) + "\t");
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
		{	StringBuffer text = new StringBuffer("Naive Bayes");
			for (int i = 0; i < priors.length; i++)
			{	text.append("\n\n"+resource.getString("class") + " " + i + ": P(C) = " + Utils.doubleToString(priors[i], 10, 8) + "\n\n");
				if (nominalCounts != null)
				{	for (int attIndex=0; attIndex<nominalCounts[i].length; attIndex++)
					{	text.append(resource.getString("attribute")+" " + attIndex + "\n");
						for (int j = 0; j < nominalCounts[i][attIndex].length; j++)
						{	text.append(j + "\t");
						}
						text.append("\n");
						for (int j = 0; j < nominalCounts[i][attIndex].length; j++)
							text.append(Utils.doubleToString(nominalCounts[i][attIndex][j], 10, 8) + "\t");
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
	public ProbabilisticNetwork getProbabilisticNetwork() {
		return net;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

}