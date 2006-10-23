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

import unbbayes.datamining.datamanipulation.InstanceSet;

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
	private int numAttributes;

	/**
	 * 
	 * @param instanceSet
	 */
	public Euclidean(InstanceSet instanceSet) {
		numAttributes = instanceSet.numAttributes;
	}
	/**
	 * Calculates the euclidian distance between two instances.
	 * 
	 * @param vector1 The first instance's values.
	 * @param vector2 The second instance's values.
	 */
	public float distanceValue(float[] vector1, float[] vector2) {
		float result = 0;

		for (int i = 0; i < numAttributes; i++) {
			result += (vector1[i] - vector2[i]) * (vector1[i] - vector2[i]);
		}
		return (float) Math.sqrt(result);

	} //distanceValue()

} //Euclidean