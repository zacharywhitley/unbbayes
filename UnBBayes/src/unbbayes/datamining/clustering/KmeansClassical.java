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

import java.util.Hashtable;
import java.util.Random;

import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.distance.IDistance;

/**
 * Implements </b>K-Means Classical</b> data clustering method.
 * 
 * @version $Revision$ $Date$
 * 
 * @author <a href="mailto:hugoiver@yahoo.com.br">Hugo Iver V. Gon&ccedil;alves</a>
 * @author <a href="mailto:rodbra@pop.com.br">Rodrigo C. M. Coimbra</a>
 * <br>
 * modified by Emerson Lopes Machado (emersoft@conecttanet.com.br) for working
 * with UnBMiner.
 */
public class KmeansClassical  {
	
	/** A matrix containing in each row the coordinates of final clusters. */
	private float[][] clusters;

	/**
	 * Distance matrix of data points to clusters. Each row is a cluster and
	 * each column a data point in the same order they appear in the input
	 * matrix.
	 */
	private double[][] distanceMatrix;
	
	/**
	 * Data assignment to clusters. Each row is a cluster and each column a data
	 * point in the same order they appear in the input matrix.
	 */
	private int[][] assignmentMatrix;
	
	/**
	 * Method for calculate distance between two vectors.
	 */
	private IDistance distance;
	
	/** The current dataset */
	private InstanceSet instanceSet;

	/** Number of desired clusters */
	private int k;
	
	/** Number of instances in the instance set */
	private int numInstances;

	/**
	 * Creates a new <code>KmeansClassical</code>
	 * 
	 * @param distance The distance method
	 * @param instanceSet The input dataset
	 */
	public KmeansClassical() {
	} //KmeansClassical()
	
	/**
	 * @see neuralnetworktoolkit.clustering.IClustering#clusterize(double[][],
	 *      int, double)
	 */
	public int[][] clusterize(IDistance distance, InstanceSet instanceSet, int k,
			double error) {
		boolean go;
		boolean modified = false;
		double localError = Double.MAX_VALUE;
		double instantError = Double.MAX_VALUE;

		this.distance = distance;
		this.instanceSet = instanceSet;
		this.k = k;
		numInstances = instanceSet.numInstances();

		/* Initialie the centroids */
		clusters = initialize();

		do {
			go = false;
			// Calculate distance matrix.
			distanceMatrix = calculateDistanceMatrix();

			// Assign points to clusters.
			assignmentMatrix = assignPoints();

			// Recalculate centroids.
			clusters = calculateCentroids();

			instantError = calculateError();
			if (Double.compare(1.001, localError / instantError) < 0) {
				localError = instantError;
				go = true;
			}

		} while (go);

		return null;
	} //clusterize()
	
	/**
	 * Initializes the clusters matrix.
	 * 
	 * @param k
	 *            Number of clusters.
	 * @param input
	 *            Data to clusterize.
	 * 
	 * @return Clusters matrix with initial values.
	 */
	private float[][] initialize() {
		Random randomizer;
		Hashtable<String, Integer> hash;
		int filled;
		int key;
		float[][] result;
		int numAttributes = instanceSet.numAttributes();
		Instance instance;
		numAttributes = instanceSet.numAttributes();
		
		randomizer = new Random();
		hash = new Hashtable<String, Integer>();
		result = new float[k][numAttributes];
		filled = 0;

		while (filled < k) {
			key = randomizer.nextInt(numInstances);
			
			/* Test if the current <code>key</code> instance has been used
			 * if not, use this instance as a base for the initial k clusters
			 */ 
			if (!hash.containsKey(String.valueOf(key))) {
				instance = instanceSet.getInstance(key);
				for (int att = 0; att < numAttributes; att++) {
					if (instanceSet.getAttribute(att).isNumeric()) {
						/* Get the float value */
						result[filled][att] = instance.data[att];
					} else {
						/* Get the internal representation value */
						result[filled][att] = instance.getValue(att);
					}
				}

				/* Set the current <code>key</code> as used */
				hash.put(String.valueOf(key), new Integer(key));
				filled++;
			}
		}
		return result;

	} //initialize()
	
	/**
	 * Calculates distance matrix.
	 * 
	 * @param input
	 *            Data to clusterize.
	 * 
	 * @return Distance matrix.
	 */
	private double[][] calculateDistanceMatrix() {
		double[][] result = new double[k][numInstances];
		float[][] input = new float[k][numInstances];
		Instance instance;
		int numAttributes = instanceSet.numAttributes();

		for (int i = 0; i < k; i++) {
			for (int j = 0; j < numInstances; j++) {
				instance = instanceSet.getInstance(j);
				/* Get float values of the instance */
				for (int att = 0; att < numAttributes; att++) {
					if (instanceSet.getAttribute(att).isNominal()) {
						/* Get the internal representation value */
						input[j][att] = instance.getValue(att);
					} else {
						/* Get the number value */
						input[j][att] = instance.data[att];
					}
				}
				/* Get the distance between the j instance and the k cluster */
				result[i][j] = distance.distanceValue(clusters[i], input[j]);
			}
		}
		return result;
		
	} //calculateDistanceMatrix()
	
	/**
	 * Selects assign points to clusters.
	 * 
	 * @return Assign points.
	 */
	private int[][] assignPoints() {
		int selected = 0;
		int[][] result;
		double least;

		result = new int[distanceMatrix.length][distanceMatrix[0].length];

		for (int j = 0; j < distanceMatrix[0].length; j++) {
			least = Double.MAX_VALUE;

			for (int i = 0; i < distanceMatrix.length; i++) {
				if (distanceMatrix[i][j] < least) {
					least = distanceMatrix[i][j];
					selected = i;

				}

			}
			result[selected][j] = 1;

		}
		return result;

	} //assignPoints()
	
	/**
	 * Calculates new centroids values.
	 * 
	 * @param input
	 *            Data to clusterize.
	 * 
	 * @return New centroids values.
	 */
	private float[][] calculateCentroids() {
		float[][] result = new float[assignmentMatrix.length][input[0].length];
		float[] sum = new float[input[0].length];
		int membersCounter;

		for (int i = 0; i < assignmentMatrix.length; i++) {
			// Put zeros in sum.
			for (int j = 0; j < sum.length; j++) {
				sum[j] = 0;
				
			}
			
			membersCounter = 0;
			for (int j = 0; j < assignmentMatrix[0].length; j++) {
				if (assignmentMatrix[i][j] == 1) {
					membersCounter++;
					for (int k = 0; k < input[0].length; k++) {
						// Do for input j.
						sum[k] += input[j][k];
						
					}
					
				}

			}
			
			for (int j = 0; j < input[0].length; j++) {
				sum[j] /= membersCounter;
				result[i][j] = sum[j];
			}

		}
		return result;
		
	} //calculateCentroids()
	
	/**
	 * Calculates error in clustering.
	 * 
	 * @param input Data to clusterize.
	 * 
	 * @return Error in clustering.
	 */
	private double calculateError() {
		double result = 0;

		for (int i = 0; i < assignmentMatrix.length; i++) {
			for (int j = 0; j < assignmentMatrix[0].length; j++) {
				if (assignmentMatrix[i][j] == 1) {
					result += Math.pow(distance.distanceValue(input[j],
							clusters[i]), 2);
				}

			}

		}
		return result;

	} //calculateError()

} //KmeansClassical
