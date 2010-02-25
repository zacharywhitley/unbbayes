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
package unbbayes.datamining.datamanipulation;

import java.util.Hashtable;

/**
 * A Utility class that contains summary information on an
 * the values that appear in a dataset for a particular attribute.
 *
 *	@author M�rio Henrique Paes Vieira (mariohpv@bol.com.br)
 *	@version $1.0 $ (16/02/2002)
 */
public class AttributeStats  {	 
	/** The number of missing values */
	private int missingCount = 0;
	private int missingCountWeighted = 0;
	
	/** The number of distinct values */
	private int distinctCount = 0;
	
	/** Stats on numeric value distributions */
	private Stats numericStats;
	
	/** Counts of each nominal value */
	private int[] nominalCounts;
	public float[] nominalCountsWeighted;
	
	/** Constant set for numeric attributes. */
	private final static byte NUMERIC = 0;

	/** Constant set for nominal attributes. */
	private final static byte NOMINAL = 1;

	/** Constant set for cyclic numeric attributes. */
	private final static byte CYCLIC = 2;

	/** 
	 * Stores the type of an attribute:
	 * 0 - Numeric
	 * 1 - Nominal
	 * 2 - Cyclic numeric
	 */
	private byte attributeType;

	/** The attribute information. */
	private Attribute attribute;
	
	/** The current instanceSet */
	private InstanceSet instanceSet;
	
	/** 
	 * Constructor that defines the type of Attribute will be manipulated
	 * and the number of values associated with this Attribute. If
	 * Attribute is numeric numValues will not be considerated.
	 * 
	 * @param attributeType Type of Attribute (Nominal or Numeric)
	 * @param numValues Number of values associated with an Attribute
	 */
	public AttributeStats(InstanceSet instanceSet, Attribute attribute) {
		this.instanceSet = instanceSet;
		this.attribute = attribute;
		attributeType = attribute.getAttributeType();

		if (attributeType == NOMINAL) {
			/* The attribute is nominal */
			int numValues = attribute.numValues();
			nominalCounts = new int[numValues];
			nominalCountsWeighted = new float[numValues];
			computeNominalStatistics();
		} else {
			/* The attribute is numeric */
			computeNumericStatistics();
		}
	}
	
	private void computeNominalStatistics() {
		int att = attribute.getIndex();
		int numInstances = instanceSet.numInstances();
		int counterIndex = instanceSet.counterIndex;
		int numValues = attribute.numValues();
		int weight;
		float value;
		int[] countWeightResults = new int[numValues + 1];
		int[] countResults = new int[numValues + 1];
		int missingIndex = numValues;

		for (int inst = 0; inst < numInstances; inst++) {
			value = instanceSet.instances[inst].data[att];
			weight = (int) instanceSet.instances[inst].data[counterIndex];
			if (Instance.isMissingValue(value)) {
				/* The value is missing. Increment missing counter */
				countWeightResults[missingIndex] += weight;
				++countResults[missingIndex];
			} else {
				/* The value is present. Increment counter */
				countWeightResults[(int) value] += weight;
				++countResults[(int) value];
			}
		}
		
		missingCount = countResults[missingIndex];
		missingCountWeighted = countWeightResults[missingIndex];

		for (int inst = 0; inst < numValues; inst++) {
			nominalCounts[inst] = countResults[inst];
			nominalCountsWeighted[inst] = countWeightResults[inst];
			distinctCount++;
		}
	}

	private void computeNumericStatistics() {
		int att = attribute.getIndex();
		int counterIndex = instanceSet.counterIndex;

		numericStats = new Stats(instanceSet, att);

		/* Compute the number of distinct values and missing values */
		Hashtable<Float, Integer> hashtable = new Hashtable<Float, Integer>();
		int numInstances = instanceSet.numInstances;
		float value;
		
		distinctCount = 0;
		missingCount = 0;
		missingCountWeighted = 0;
		for (int inst = 0; inst < numInstances; inst++) {
			value = instanceSet.instances[inst].data[att];
			if (Instance.isMissingValue(value)) {
				++missingCount;
				missingCountWeighted += 
					instanceSet.instances[inst].data[counterIndex];
			} else if (!hashtable.containsKey(value)) {
				/* New value! Increase counter */
				++distinctCount;
				hashtable.put(value, 0);
			}
		}
	}

	/** 
	 * Returns the number of distinct values
	 * 
	 * @return Number of distinct values	
	 */
	public int getDistinctCount()
	{	return distinctCount;
	}
	
	/** 
	 * Returns the number of missing values
	 * 
	 * @return Number of missing values
	 */
	public int getMissingCount() {
		return missingCount;
	}
	
	/** 
	 * Returns the number of missing values
	 * 
	 * @return Number of missing values
	 */
	public int getMissingCountWeighted() {
		return missingCountWeighted;
	}
	
	/** 
	 * Return the number of counts for each nominal value. If the
	 * referenced attribute is numeric, returns null.
	 * 
	 * @return Counts for each nominal value
	 */
	public int[] getNominalCounts() {
		if (attributeType == NUMERIC) {
			return null;
		}
		
		return nominalCounts;
	}
	
	/** 
	 * Return the weighted number of counts for each nominal value. If the
	 * referenced attribute is numeric, returns null.
	 * 
	 * @return Counts for each nominal value
	 */
	public float[] getNominalCountsWeighted() {
		if (attributeType == NUMERIC) {
			return null;
		}
		
		return nominalCountsWeighted.clone();
	}
	
	/** 
	 * Return a Stats object with some simple numeric statics. If the
	 * referenced attribute is nominal, returns null.
	 * @return Simple statistics
	 */
	public Stats getNumericStats() {
		if (attributeType == NOMINAL) {
			return null;
		}
		
		return numericStats;
	}	
		
	/**
	 * Returns a string summarising the stats so far.
	 *
	 * @return The summary string
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("Missing Count " + missingCount + '\n');
		result.append("Missing Count Weighted " + missingCountWeighted + '\n');
		result.append("Distinct Count " + distinctCount + '\n');
		result.append("Counts ");
		for (int i=0;i<nominalCounts.length;i++) {
			result.append(nominalCounts[i]+" ");
		}
		result.append("\n");
		result.append("Counts Weighted");
		for (int i=0;i<nominalCountsWeighted.length;i++) {
			result.append(nominalCountsWeighted[i]+" ");
		}
		result.append("\n");
		
		return result.toString();
	}
}
