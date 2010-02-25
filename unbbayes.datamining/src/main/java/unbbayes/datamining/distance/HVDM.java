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
		classIndex = instanceSet.classIndex;
		numClasses = 0;
		if (classIndex != -1) {
			numClasses = instanceSet.numClasses();
		}
		attributeType = instanceSet.attributeType;
		attributeStats = instanceSet.getAttributeStats();
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
		if (instanceSet.numNominalAttributes > 1 || 
				(instanceSet.numNominalAttributes == 1 && 
						instanceSet.classIsNominal())) {
			distribution = Utils.computeNominalDistributions(instanceSet);
		}
	}
	
	/** 
	 * Set the distance function desired.
	 * <ul>
	 * <li> 0: Hamming
	 * <li> 1: HVDM
	 * </ul>
	 */
	public void setOptionDistanceFunction(byte optionDistanceFunction) {
		this.optionDistanceFunction = optionDistanceFunction;
	}
	
	public float distanceValue(float[] vector1, float[] vector2) {
		int attIndex = 0;
		double attDif;
		double distance = 0;

		for (int att = 0; att < numAttributes; att++) {
			attDif = 0;

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
				attDif = distanceNominal(vector1, vector2, att, attIndex);
				
				/* Get next nominal attribute */
				++attIndex;
			} else if (attributeType[att] == InstanceSet.NUMERIC) {
				/* The attribute is numeric. Apply Euclidian distance */
				attDif = (vector1[att] - vector2[att]);
				
				/* Normalize 'attDif' */
				attDif = attDif / attNorm[att];
			} else if (attributeType[att] == InstanceSet.CYCLIC) {
				/*
				 * The attribute is cyclic. If the absolute of 'attDif' is greater
				 * than half way between minimun and maximum value of this
				 * attribute, we take its complementary.
				 */
				attDif = Math.abs(vector1[att] - vector2[att]);
				if (attDif > attHalfRangeValue[att]) {
					attDif = attRangeValue[att] - attDif;
				}

				/* Normalize 'attDif' */
				attDif = attDif / attNorm[att];
				
			}			
			/* Add to 'distante' the square of the difference */
			distance += attDif * attDif;
		}
		distance = Math.sqrt(distance);
		
		return (float) distance;
	}

	private double distanceNominal(float[] vector1, float[] vector2, int att,
			int attIndex) {
		double attDif = 0;
		float aux1;
		float aux2;
		AttributeStats attStats;
		int index;
		
		if (optionDistanceFunction == HVDM) {
			/* Apply VDM distance */
			attStats = attributeStats[att];
			for (int k = 0; k < numClasses; k++) {
				/* Get relative frequency for 'vector1' */
				index = (int) vector1[att];
				aux1 = distribution[k][attIndex][index];
				aux1 /= attStats.nominalCountsWeighted[index];

				/* Get relative frequency for 'vector2' */
				index = (int) vector2[att];
				aux2 = distribution[k][attIndex][index];
				aux2 /= attStats.nominalCountsWeighted[index];
				
				/* Calculate the difference */
				attDif += (aux1 - aux2) * (aux1 - aux2);
			}
		} else if (optionDistanceFunction == HAMMING) {
			/* Apply Hamming distance */
			if (vector1[att] != vector2[att]) {
				attDif = 1;
			}
		}
		
		return attDif;
	}
}

