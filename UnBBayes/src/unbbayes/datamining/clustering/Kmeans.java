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

import java.util.Arrays;
import java.util.Random;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.distance.IDistance;

/**
 * Implements </b>K-Means Classical</b> data clustering method that clusterize
 * the instances in <code>numClusters</code> clusters. The algorithm converges when the
 * distance of the clusters' centroids to its instances is reduced to less than
 * <code>error%</code> in the last iteration.
 * 
 * @version $Revision$ $Date$
 * 
 * @author <a href="mailto:hugoiver@yahoo.com.br">Hugo Iver V. Gon&ccedil;alves</a>
 * @author <a href="mailto:rodbra@pop.com.br">Rodrigo C. M. Coimbra</a>
 * <br>
 * Adapted to UnBMiner by Emerson Lopes Machado (emersoft@conectanet.com.br) in
 * 2006/10/06.
 */
public class Kmeans extends Clustering {
	
	/** Matrix containing in each row the coordinates of final the clusters. */
	private float[][] centroids;

	/**
	 * Distance matrix of data points to clusters. Each row is a cluster and
	 * each column a data point in the same order they appear in the input
	 * matrix.
	 */
	private double[][] distanceMatrix;
	
	/**
	 * Method for calculating distance between two data points.
	 */
	private IDistance distance;
	
	/** The minimum accepted % of change in each iteration */
	private double error;

	/**
	 * Default constructor for this class.
	 * 
	 * @param instanceSet
	 */
	public Kmeans(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
	}

	protected void run() throws Exception {
		/* Check if there is at least one numeric attribute */
		if (numNumericAttributes < 1) {
			throw new Exception("K-means needs at least one numeric attribute");
		}
		
		boolean go;
		double localError = Double.MAX_VALUE;
		double instantError = Double.MAX_VALUE;

		/* Initialie the centroids */
		centroids = initialize();

		do {
			go = false;
			/* Calculate distance matrix */
			distanceMatrix = calculateDistanceMatrix();

			/* Assign instances to clusters */
			assignmentMatrix = assignPoints();

			/* Recalculate centroids */
			centroids = calculateCentroids();

			instantError = calculateError();
			if (instantError != 0) {
				if (Double.compare(error, localError / instantError) < 0) {
					localError = instantError;
					go = true;
				}
			}
		} while (go);
	}
	
	/**
	 * Initializes the centroids matrix.
	 * 
	 * @return Clusters matrix with initial values.
	 */
	private float[][] initialize() {
		float[][] centroids = new float[numClusters][numNumericAttributes];
		float[] candidate = new float[numNumericAttributes];
		Random randomizer = new Random();
		int filled = 0;
		int inst;
		int attIndex;
		boolean exists;

		while (filled < numClusters) {
			
			/* Get the candidate */
			inst = instancesIDs[randomizer.nextInt(numInstances)];
			attIndex = 0;
			for (int att = 0; att < numAttributes; att++) {
				if (instanceSet.attributeType[att] == NUMERIC) {
					candidate[attIndex] = instances[inst].data[att];
					++attIndex;
				}
			}
			
			/* 
			 * Test if the current <code>candidate</code> instance has been used.
			 * If not, use this instance as a base for the initial numClusters centroids
			 */
			exists = false;
			for (int c = 0; c < filled; c++) {
				for (int att = 0; att < numNumericAttributes; att++) {
					if (centroids[c][att] == candidate[att]) {
						exists = true;
						break;
					}
				}
				if (exists) break;
			}
			
			if (!exists) {
				for (int att = 0; att < numNumericAttributes; att++) {
					centroids[filled][att] = candidate[att];
				}
				filled++;
			}
		}
		
		return centroids;
	}
	
	/**
	 * Calculates distance matrix.
	 * 
	 * @return Distance matrix.
	 */
	private double[][] calculateDistanceMatrix() {
		double[][] distanceMatrix = new double[numClusters][numInstances];
		float[] instance = new float[numNumericAttributes];
		float[] centroid;
		float dist;
		int attIndex;
		int inst;

		for (int clusterIndex = 0; clusterIndex < numClusters; clusterIndex++) {
			for (int i = 0; i < numInstances; i++) {
				inst = instancesIDs[i];
				/*
				 * Get the distance between the inst instance and the 
				 * 'clusterIndex'th centroid 
				 */
				attIndex = 0;
				for (int att = 0; att < numAttributes; att++) {
					if (attributeType[att] == NUMERIC) {
						instance[attIndex] = instances[inst].data[att];
						++attIndex;
					}
				}
				centroid = centroids[clusterIndex];
				dist = distance.distanceValue(centroid, instance);
				distanceMatrix[clusterIndex][i] = dist;
			}
		}
		return distanceMatrix;
	}
	
	/**
	 * Assign data to clusters.
	 * 
	 * @return Assign points.
	 */
	private int[] assignPoints() {
		int[] assignmentMatrix = new int[instanceSet.numInstances];
		double least;
		int selectedCluster = 0;
		int inst;
		
		/* Initialize the assignment matrix */
		Arrays.fill(assignmentMatrix, -1);
		
		for (int i = 0; i < numInstances; i++) {
			inst = instancesIDs[i];
			least = Double.MAX_VALUE;
			for (int clusterIndex = 0; clusterIndex < numClusters; clusterIndex++) {
				if (distanceMatrix[clusterIndex][i] < least) {
					least = distanceMatrix[clusterIndex][i];
					selectedCluster = clusterIndex;
				}
			}
			assignmentMatrix[inst] = selectedCluster;
		}
		return assignmentMatrix;
	}
	
	/**
	 * Calculates new centroids values.
	 * 
	 * @return New centroids values.
	 */
	private float[][] calculateCentroids() {
		float[][] centroids = new float[numClusters][numNumericAttributes];
		float[] sum = new float[numNumericAttributes];
		int membersCounter;
		int attIndex;
		float weight;
		int inst;

		for (int clusterIndex = 0; clusterIndex < numClusters; clusterIndex++) {
			/* Fill up 'sum' with 0 */
			Arrays.fill(sum, 0);
			
			membersCounter = 0;
			for (int i = 0; i < numInstances; i++) {
				inst = instancesIDs[i];
				if (assignmentMatrix[inst] == clusterIndex) {
					weight = instances[inst].data[counterIndex];
					membersCounter += weight;
					attIndex = 0;
					for (int att = 0; att < numAttributes; att++) {
						if (attributeType[att] == NUMERIC) {
							sum[attIndex] += instances[inst].data[att] * weight;
							++attIndex;
						}
					}
				}
			}
			for (int att = 0; att < numNumericAttributes; att++) {
				centroids[clusterIndex][att] = sum[att] / membersCounter;
			}
		}
		return centroids;
	}
	
	/**
	 * Calculates error in clustering.
	 * 
	 * @param input Data to clusterize.
	 * @return Error in clustering.
	 */
	private double calculateError() {
		double result = 0;
		float[] instance = new float[numNumericAttributes];
		int attIndex;
		int inst;

		for (int clusterIndex = 0; clusterIndex < numClusters; clusterIndex++) {
			for (int i = 0; i < numInstances; i++) {
				inst = instancesIDs[i];
				if (assignmentMatrix[inst] == clusterIndex) {
					attIndex = 0;
					for (int att = 0; att < numAttributes; att++) {
						if (attributeType[att] == NUMERIC) {
							instance[attIndex] = instances[inst].data[att];
							++attIndex;
						}
					}
					result += Math.pow(distance.distanceValue(instance,
							centroids[clusterIndex]), 2);
				}
			}
		}
		return result;
	}

	public void setNumClusters(int numClusters) {
		this.numClusters = numClusters;
	}
	
	public void setError(double error) {
		this.error = error;
	}
	
	public void setOptionDistance(IDistance distance) {
		this.distance = distance;
	}

}
