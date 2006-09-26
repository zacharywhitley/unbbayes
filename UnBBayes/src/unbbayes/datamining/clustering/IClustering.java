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

package unbbayes.datamining.clustering;

import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.distance.IDistance;

/**
 * Defines data clustering methods.
 * 
 * @version $Revision$ $Date$
 * 
 * @author <a href="mailto:hugoiver@yahoo.com.br">Hugo Iver V. Gon&ccedil;alves</a>
 * @author <a href="mailto:rodbra@pop.com.br">Rodrigo C. M. Coimbra</a>
 * 
 * modified by <a href="mailto:emersoft@conecttanet.com.br">Emerson Lopes Machado
 * for working with UnBMiner
 */
public interface IClustering {
	
//	/**
//	 * Clusterize data on <code>input</code> into <code>k</code> clusters
//	 * with <code>error</code> clustering error.
//	 * 
//	 * @param input
//	 *            Data to clusterize.
//	 * @param k
//	 *            Number of clusters to generate.
//	 * @param error
//	 *            Error on clustering.
//	 */
	
	/**
	 * 
	 */
	public void clusterize(IDistance distance, InstanceSet instanceSet, int k,
			double error);
	
	/**
	 * Returns the matrix with the coordinates of final clusters.
	 * 
	 * @return Returns the clusters.
	 */
	public int[][] getClusters();

	/**
	 * Sets the matrix with the coordinates of final clusters.
	 * 
	 * @param clusters
	 *            The clusters to set.
	 */
	public void setClusters(int[][] clusters);

} //IClustering
