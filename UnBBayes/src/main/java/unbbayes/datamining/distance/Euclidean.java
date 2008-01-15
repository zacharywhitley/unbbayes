/*
 * $RCSfile$
 * $Revision$
 * $Date$
 *
 * NeuralNetworkToolkit
 * Copyright (C) 2004 University of Brasília
 *
 * This file is part of NeuralNetworkToolkit.
 *
 * NeuralNetworkToolkit is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * NeuralNetworkToolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NeuralNetworkToolkit; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA - 02111-1307 - USA.
 */

package unbbayes.datamining.distance;

import unbbayes.datamining.datamanipulation.AttributeStats;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Stats;
import unbbayes.datamining.datamanipulation.Utils;

/**
 * Implements the <b>Euclidean</b> method for calculating the distance between
 * two vectors.
 * 
 * @version $Revision$ $Date$
 * 
 * @author <a href="mailto:hugoiver@yahoo.com.br">Hugo Iver V. Gon&ccedil;alves</a>
 * @author <a href="mailto:rodbra@pop.com.br">Rodrigo C. M. Coimbra</a>
 */
public class Euclidean extends Distance {
	/** The current instanceSet */
	private InstanceSet instanceSet;

	/** Number of attributes in the instance set */
	private int numAttributes;

	/** Number of numeric attributes in the instance set */
	private int numNumericAttributes;
	
	private double attNorm[];

	/** Stores each attribute's mininum value */
	private double[] minValue;
	
	/** Stores each attribute's maximum value */
	private double[] maxValue;

	/** Normalize */
	private boolean normalize;
	
	/**
	 * Computes the Euclidean distance between two arrays of float.
	 * 
	 * @param instanceSet
	 * @param normFactor 
	 * @param normalize Set it true to normalize values
	 */
	public Euclidean(InstanceSet instanceSet, int normFactor,
			boolean normalize) {
		this.instanceSet = instanceSet;
		this.normalize = normalize;
		numAttributes = instanceSet.numAttributes;
		numNumericAttributes = instanceSet.numNumericAttributes;
		AttributeStats attributeStats[] = instanceSet.getAttributeStats();
		minValue = new double[numNumericAttributes];
		maxValue = new double[numNumericAttributes];
		
		/* Get standard deviation and compute normalization factor */
		Stats stats;
		attNorm = new double[instanceSet.numNumericAttributes];
		int attIndex = 0;
		for (int att = 0; att < numAttributes; att++) {
			/* Skip the class attribute */
			if (att == instanceSet.classIndex) {
				continue;
			}

			/* Skip not numeric attribute */
			if (instanceSet.attributeType[att] != InstanceSet.NUMERIC) {
				continue;
			}
			
			stats = attributeStats[att].getNumericStats();
			attNorm[attIndex] = stats.getStdDev() * normFactor;
			if (attNorm[attIndex] == 0) {
				/* Give low weight to this attribute */
				attNorm[attIndex] = Double.MAX_VALUE;
			}
			
			minValue[attIndex] = stats.getMin();
			maxValue[attIndex] = stats.getMax();
			
			++attIndex;
		}
	}
	
	/**
	 * Calculates the euclidian distance between two instances.
	 * 
	 * @param vector1 The first instance's values.
	 * @param vector2 The second instance's values.
	 */
	public float distanceValue(float[] vector1, float[] vector2) {
		double dist;
		double result = 0;

		if (normalize) {
			for (int att = 0; att < numNumericAttributes; att++) {
					dist = (norm(vector1[att], att) - norm(vector2[att], att));
					result += dist * dist;
			}
			return (float) result;
		} else {
			for (int att = 0; att < numNumericAttributes; att++) {
				dist = (vector1[att] - vector2[att]) / attNorm[att];
				result += dist * dist;
			}
			return (float) Math.sqrt(result);
		}
	}

	/**
	 * Normalizes a given value of a numeric attribute.
	 *
	 * @param x the value to be normalized
	 * @param i the attribute's index
	 * @return the normalized value
	 */
	private double norm(double x, int i) {
		if (Double.isNaN(minValue[i]) || Utils.eq(maxValue[i],minValue[i])) {
			return 0;
		} else {
			return (x - minValue[i]) / (maxValue[i] - minValue[i]);
		}
	}

}