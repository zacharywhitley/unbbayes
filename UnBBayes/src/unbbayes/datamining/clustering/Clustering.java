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

/**
 * Implements common features for data clustering methods. All data clustering
 * methods classes must extend this class.
 * 
 * @version $Revision$ $Date$
 * 
 * @author <a href="mailto:hugoiver@yahoo.com.br">Hugo Iver V. Gon&ccedil;alves</a>
 * @author <a href="mailto:rodbra@pop.com.br">Rodrigo C. M. Coimbra</a>
 * 
 * modified by <a href="mailto:emersoft@conecttanet.com.br">Emerson Lopes Machado
 * for working with UnBMiner
 */
public abstract class Clustering {

	/** A matrix containing in each row the coordinates of final clusters. */
	protected float[][] clusters;

	/**
	 * @see neuralnetworktoolkit.clustering.IClustering#getClusters()
	 */
	public float[][] getClusters() {
		return clusters;
		
	} //getClusters()

	/**
	 * @see neuralnetworktoolkit.clustering.IClustering#setClusters(double[][])
	 */
	public void setClusters(float[][] clusters) {
		this.clusters = clusters;
		
	} //setClusters()
	
} //Clustering
