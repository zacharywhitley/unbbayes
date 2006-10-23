package unbbayes.datamining.distance;

import unbbayes.datamining.datamanipulation.AttributeStats;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Stats;
import unbbayes.datamining.datamanipulation.Utils;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 12/09/2006
 */
public class HVDM extends Distance {
	
	/** The number of attributes of the dataset */
	private int numAttributes;
	
	/** Number of instances in the instance set */
	private int numInstances;

	/** Number of classes in the instance set */
	private int numClasses;

	private double attNorm[];
	private float distribution[][][];
	private AttributeStats attributeStats[];
	private byte attributeType[];
	
	/** Range value of an attribute. Used for cyclic attributes */
	private float attRangeValue[];
	
	/** 
	 * Half of the range value. Used to speed up calculations in cyclic
	 * attributes
	 */
	private float attHalfRangeValue[];
	
	/** 
	 * The index of the dataset's column that represents the class attribute.
	 * Assume the value -1 in case there is no class attribute.
	 */
	public int classIndex;

	/** 
	 * The index of the dataset's column that represents the counter attribute.
	 * Assumes always the last column of the internal dataset as the counter
	 * attribute.
	 */
	public int counterIndex;

	/** 
	 * Distance function desired.
	 * <ul>
	 * <li> 0: Hamming
	 * <li> 1: HVDM
	 * </ul>
	 */
	private byte optionDistanceFunction;
	private byte HAMMING = 0;
	private byte HVDM = 1;
	
	/**
	 * Constructor for this class. Set all pertinent fields and compute all
	 * necessary statistic for HVDM distance calculation.
	 * The parameter <code>normFactor</code> will be multiplied by the standard
	 * deviation of each numeric attribute and the resulting value will be
	 * utilized to normalizing the distance value of each numeric attribute.
	 * 
	 * @param instanceSet
	 * @param normFactor The desired normalizer factor.
	 */
	public HVDM(InstanceSet instanceSet, int normFactor) throws Exception {
		numAttributes = instanceSet.numAttributes();
		numInstances = instanceSet.numInstances();
		numClasses = instanceSet.numClasses();
		classIndex = instanceSet.classIndex;
		attributeType = instanceSet.attributeType;
		attributeStats = instanceSet.computeAttributeStats();
		counterIndex = instanceSet.counterIndex;

		attNorm = new double[numInstances];
		attRangeValue = new float[numInstances];
		attHalfRangeValue = new float[numInstances];

		Stats stats;
		
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
				attNorm[att] = stats.getStdDev() * normFactor;
			} else if (instanceSet.attributeType[att] == InstanceSet.CYCLIC) {
				/* The attribute is cyclic */
				
				/* Get the min, max, range and half range values */
				stats = attributeStats[att].getNumericStats();
				attNorm[att] = stats.getStdDev() * normFactor;
				attRangeValue[att] = stats.getMax() - stats.getMin() + 1;
				attHalfRangeValue[att] = attRangeValue[att] / 2;
			} else if (instanceSet.attributeType[att] == InstanceSet.NOMINAL) {
				/* The attribute is nominal */
			}
		}
		
		/* Compute nominal distributions */
		distribution = Utils.computeNominalDistributions(instanceSet);
	}
	
	public void setOptionDistanceFunction(byte optionDistanceFunction) {
		this.optionDistanceFunction = optionDistanceFunction;
	}
	
	public float distanceValue(float[] vector1, float[] vector2) {
		float distance = 0;
		double dif;
		int attIndex = 0;
		float aux1;
		float aux2;
		int index;
		AttributeStats attStats;

		for (int att = 0; att < numAttributes; att++) {
			dif = 0;

			/* Skip the class attribute */
			if (att == classIndex) {
				continue;
			}

			/* Skip equal values */
			if (vector1[att] == vector2[att]) {
				if (attributeType[att] == InstanceSet.NOMINAL) {
					/* Get next nominal attribute */
					++attIndex;
				}
				continue;
			}

			if (attributeType[att] == InstanceSet.NOMINAL) {
				/* The attribute is nominal */
				if (optionDistanceFunction == HVDM) {
					/* Apply VDM distance */
					attStats = attributeStats[att];
					for (int k = 0; k < numClasses; k++) {
						/* Get relative frequency for 'vector1' */
						index = (int) vector1[att];
						aux1 = distribution[k][attIndex][index];
						aux1 = aux1 / attStats.nominalCountsWeighted[index];
	
						/* Get relative frequency for 'vector2' */
						index = (int) vector2[att];
						aux2 = distribution[k][attIndex][index];
						aux2 = aux2 / attStats.nominalCountsWeighted[index];
						
						/* Calculate the difference */
						dif += (aux1 - aux2) * (aux1 - aux2);
					}
				} else if (optionDistanceFunction == HAMMING) {
					/* Apply Hamming distance */
					if (vector1[att] != vector2[att]) {
						dif = 1;
					}
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

