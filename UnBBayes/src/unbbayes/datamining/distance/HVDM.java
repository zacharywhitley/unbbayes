package unbbayes.datamining.distance;

import unbbayes.datamining.datamanipulation.AttributeStats;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Stats;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 12/09/2006
 */
public class HVDM extends Distance {
	
	/** The number of attributes of the dataset */
	private int numAttributes;
	
	/** The current dataset */
	private InstanceSet instanceSet;
	
	/** Number of instances in the instance set */
	private int numInstances;

	/** Number of classes in the instance set */
	private int numClasses;

	private float attNorm[];
	private float distribution[][][];
	private AttributeStats attributeStats[];
	private byte attributeType[];
	private int numNominalAttribute;
	
	/** Range value of an attribute. Used for cyclic attributes */
	private float attRangeValue[];
	
	/** 
	 * Half of the range value. Used to speed up calculations in cyclic
	 * attributes
	 */
	private float attHalfRangeValue[];
	
	/** The dataset values. */
	private float[][] dataset;
	
	/** 
	 * The index of the dataset's column that represents the class attribute.
	 * Assume the value -1 in case there is no class attribute.
	 */
	public byte classIndex;

	/** 
	 * The index of the dataset's column that represents the counter attribute.
	 * Assumes always the last column of the internal dataset as the counter
	 * attribute.
	 */
	public byte counterIndex;

	public HVDM(InstanceSet instanceSet, int normFactor) {
		this.instanceSet = instanceSet;
		numAttributes = instanceSet.numAttributes();
		numInstances = instanceSet.numInstances();
		numClasses = instanceSet.numClasses();
		dataset = instanceSet.dataset;
		classIndex = instanceSet.classIndex;
		attributeType = instanceSet.attributeType;
		attributeStats = instanceSet.computeAttributeStats();
		counterIndex = instanceSet.counterIndex;

		attNorm = new float[numInstances];
		attRangeValue = new float[numInstances];
		attHalfRangeValue = new float[numInstances];

		Stats stats;
		
		numNominalAttribute = 0;
		for (int att = 0; att < numAttributes; att++) {
			/* Skip the class attribute */
			if (att == classIndex) {
				continue;
			}

			/* Check if the attribute is numeric */ 
			if (attributeType[att] == InstanceSet.NUMERIC) {
				/* The attribute is numeric */
				
				/* Get standard deviation and compute normalization factor */
				stats = attributeStats[att].getNumericStats();
				attNorm[att] = stats.getStdDev() * (float) normFactor;
			} else if (instanceSet.attributeType[att] == InstanceSet.CYCLIC) {
				/* The attribute is cyclic */
				
				/* Get the min, max, range and half range values */
				stats = attributeStats[att].getNumericStats();
				attRangeValue[att] = stats.getMax() - stats.getMin() + 1;
				attHalfRangeValue[att] = attRangeValue[att] / 2;
			} else if (instanceSet.attributeType[att] == InstanceSet.NOMINAL) {
				/* The attribute is nominal */
				++numNominalAttribute;
			}
		}
		computeNominalDistributions();
	}
	
	/**
	 * Compute the necessary frequencies for all nominal attributes. 
	 */
	private void computeNominalDistributions() {
		int attIndex = 0;
		int numValues;
		int classValue;
		int instValue;
		float weight;

		distribution = new float[numNominalAttribute][][];

		for (int att = 0; att < numAttributes; att++) {
			/* Skip the class attribute */
			if (att == classIndex) {
				continue;
			}

			/* Check if the current attribute is nominal */
			if (attributeType[att] == InstanceSet.NOMINAL) {
				numValues = instanceSet.attributes[att].numValues();
				distribution[attIndex] = new float[numValues][numClasses];
				
				/* Zero all distribution values */
				for (int i = 0; i < numValues; i++) {
					for (int j = 0; j < numClasses + 1; j++) {
						distribution[attIndex][i][j] = 0;
					}
				}
				
				/* Compute distribution values */
				for (int inst = 0; inst < numInstances; inst++) {
					instValue = (int) dataset[inst][inst];
					classValue = (int) dataset[inst][classIndex];
					weight = dataset[inst][counterIndex];
					distribution[attIndex][instValue][classValue] += weight;
				}
				
				/* Next nominal attribute index */
				++attIndex;
			}
		}
	}

	public float distanceValue(float[] vector1, float[] vector2) {
		float distance = 0;
		float dif;
		int attIndex = 0;
		float aux1;
		float aux2;
		int index;
		AttributeStats attStats;

		for (int att = 0; att < numAttributes; att++) {
			/* Skip the class attribute */
			if (att == classIndex) {
				continue;
			}

			dif = 0;
			if (attributeType[att] == InstanceSet.NOMINAL) {
				/* The attribute is nominal. Apply VDM distance */
				attStats = attributeStats[att];
				for (int i = 0; i < numClasses; i++) {
					/* Get relative frequency for 'vector1' */
					index = (int) vector1[att];
					aux1 = distribution[attIndex][index][i];
					aux1 = aux1 / attStats.nominalCountsWeighted[index];

					/* Get relative frequency for 'vector2' */
					index = (int) vector2[att];
					aux2 = distribution[attIndex][index][i];
					aux2 = aux2 / attStats.nominalCountsWeighted[index];
					
					/* Calculate the difference */
					dif += (aux1 - aux2) * (aux1 - aux2);
				}
				
				/* Get next nominal attribute */
				++attIndex;
			} else if (attributeType[att] == InstanceSet.NUMERIC) {
				/* The attribute is numeric. Apply Euclidian distance */
				dif = (vector1[att] - vector2[att]);
				
				/* Normalize 'dif' */
				dif = dif / attNorm[att];
			} else if (attributeType[att] == InstanceSet.CYCLIC) {
				/*
				 *  The attribute is cyclic. If the absolute of 'dif' is greater
				 * than half way between minimun and maximum value of this
				 * attribute, we take its complementary.
				 */
				dif = Math.abs(vector1[att] - vector2[att]);
				if (dif > attHalfRangeValue[att]) {
					dif = dif - attRangeValue[att];
				}

				/* Normalize 'dif' */
				dif = dif / attNorm[att];
				
			}			
			/* Add to 'distante' the square of the difference */
			distance += dif * dif;
		}
		return (float) Math.sqrt(distance);
	}
}

