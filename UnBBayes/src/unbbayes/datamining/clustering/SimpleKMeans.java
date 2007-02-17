/*
 *		This program is free software; you can redistribute it and/or modify
 *		it under the terms of the GNU General Public License as published by
 *		the Free Software Foundation; either version 2 of the License, or
 *		(at your option) any later version.
 *
 *		This program is distributed in the hope that it will be useful,
 *		but WITHOUT ANY WARRANTY; without even the implied warranty of
 *		MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 *		GNU General Public License for more details.
 *
 *		You should have received a copy of the GNU General Public License
 *		along with this program; if not, write to the Free Software
 *		Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *		SimpleKMeans.java
 *		Copyright (C) 2000 Mark Hall
 *
 */
package unbbayes.datamining.clustering;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.Utils;

/**
 * Cluster data using the k means algorithm.
  *
 * @author Mark Hall (mhall@cs.waikato.ac.nz)
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision$
 * <br>
 * Adapted to UnBMiner by Emerson Lopes Machado (emersoft@conectanet.com.br) in
 * 2007/02/15.
 */
public class SimpleKMeans extends Clustering {
	/**
	 * holds the cluster centroids
	 */
	private InstanceSet centroids;

	/**
	 * Holds the standard deviations of the numeric attributes in each cluster
	 */
	private InstanceSet clustersStdDev;

	
	/**
	 * The number of instances in each cluster
	 */
	private int[] clustersSize;

	/**
	 * attribute min values
	 */
	private double[] minValue;
	
	/**
	 * attribute max values
	 */
	private double[] maxValue;

	/**
	 * Keep track of the number of iterations completed before convergence
	 */
	private int iterations = 0;

	/**
	 * Holds the squared errors for all clusters
	 */
	private double[] squaredErrors;

	/**
	 * Default constructor for this class.
	 * 
	 * @param instanceSet
	 */
	public SimpleKMeans(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
	}

	/**
	 * Generates a clusterer. Has to initialize all fields of the clusterer
	 * that are not being set via options.
	 *
	 * @param data set of instances serving as training data 
	 * @throws Exception if the clusterer has not been 
	 * generated successfully
	 */
	public void run() throws Exception {
		iterations = 0;

		InstanceSet instances = new InstanceSet(instanceSet);

		minValue = new double [instances.numAttributes()];
		maxValue = new double [instances.numAttributes()];
		for (int i = 0; i < instances.numAttributes(); i++) {
			minValue[i] = maxValue[i] = Double.NaN;
		}
		
		centroids = new InstanceSet(instances, numClusters);
		assignmentMatrix = new int [instances.numInstances()];

		for (int i = 0; i < instances.numInstances(); i++) {
			updateMinMax(instances.getInstance(i));
		}
		
		/** Choose k initial centroids */
//		Random RandomO = new Random(new Date().getTime());
//		int instIndex;
//		HashMap initC = new HashMap();
//		DecisionTable.hashKey hk = null;
//
//		for (int j = instances.numInstances() - 1; j >= 0; j--) {
//			instIndex = RandomO.nextInt(j+1);
//			hk = new DecisionTable.hashKey(instances.getInstance(instIndex), 
//						 instances.numAttributes(), true);
//			if (!initC.containsKey(hk)) {
//	centroids.insertInstance(instances.getInstance(instIndex));
//	initC.put(hk, null);
//			}
//			instances.swap(j, instIndex);
//			
//			if (centroids.numInstances() == numClusters) {
//	break;
//			}
//		}

		numClusters = centroids.numInstances();
		
		int i;
		boolean converged = false;
		int emptyClusterCount;
		InstanceSet[] tempI = new InstanceSet[numClusters];
		squaredErrors = new double [numClusters];
		while (!converged) {
			emptyClusterCount = 0;
			iterations++;
			converged = true;
			for (i = 0; i < instances.numInstances(); i++) {
				Instance toCluster = instances.getInstance(i);
				int newC = clusterProcessedInstance(toCluster, true);
				if (newC != assignmentMatrix[i]) {
					converged = false;
				}
				assignmentMatrix[i] = newC;
			}
			
			// update centroids
			centroids = new InstanceSet(instances, numClusters);
			for (i = 0; i < numClusters; i++) {
				tempI[i] = new InstanceSet(instances, 0);
			}
			for (i = 0; i < instances.numInstances(); i++) {
				tempI[assignmentMatrix[i]].insertInstance(instances.getInstance(i));
			}
			for (i = 0; i < numClusters; i++) {
				float[] vals = new float[numAttributes + 1];
				if (tempI[i].numInstances() == 0) {
					// empty cluster
					emptyClusterCount++;
				} else {
					for (int j = 0; j < numAttributes; j++) {
						vals[j] = (float) tempI[i].meanOrMode(j);
					}
					vals[numAttributes] = 1;
					centroids.insertInstance(new Instance(vals));
				}
			}

			if (emptyClusterCount > 0) {
				numClusters -= emptyClusterCount;
				tempI = new InstanceSet[numClusters];
			}
			if (!converged) {
				squaredErrors = new double [numClusters];
			}
		}
		clustersStdDev = new InstanceSet(instances, numClusters);
		clustersSize = new int [numClusters];
		for (i = 0; i < numClusters; i++) {
			float[] vals2 = new float[numAttributes + 1];
			for (int j = 0; j < numAttributes; j++) {
				if (instances.getAttribute(j).isNumeric()) {
					vals2[j] = (float) Math.sqrt(tempI[i].variance(j));
				} else {
					vals2[j] = Instance.missingValue();
				}	
			}
			vals2[numAttributes] = 1;
			clustersStdDev.insertInstance(new Instance(vals2));
			clustersSize[i] = tempI[i].numInstances();
		}
	}

	/**
	 * clusters an instance that has been through the filters
	 *
	 * @param instance the instance to assign a cluster to
	 * @param updateErrors if true, update the within clusters sum of errors
	 * @return a cluster number
	 */
	private int clusterProcessedInstance(Instance instance, boolean updateErrors) {
		double minDist = Integer.MAX_VALUE;
		int bestCluster = 0;
		for (int i = 0; i < numClusters; i++) {
			double dist = distance(instance, centroids.getInstance(i));
			if (dist < minDist) {
				minDist = dist;
				bestCluster = i;
			}
		}
		if (updateErrors) {
			squaredErrors[bestCluster] += minDist;
		}
		return bestCluster;
	}

	/**
	 * Classifies a given instance.
	 *
	 * @param instance the instance to be assigned to a cluster
	 * @return the number of the assigned cluster as an interger
	 * if the class is enumerated, otherwise the predicted value
	 * @throws Exception if instance could not be classified
	 * successfully
	 */
	public int clusterInstance(Instance instance) throws Exception {
		return clusterProcessedInstance(instance, false);
	}

	/**
	 * Calculates the distance between two instances
	 *
	 * @param first the first instance
	 * @param second the second instance
	 * @return the distance between the two given instances, between 0 and 1
	 */					
	private double distance(Instance first, Instance second) {	
		double distance = 0;
		int firstI, secondI;

		for (int p1 = 0, p2 = 0; p1 < numInstances || p2 < numInstances;) {
			if (p1 >= numInstances) {
				firstI = centroids.numAttributes();
			} else {
				firstI = p1; 
			}
			if (p2 >= numInstances) {
				secondI = centroids.numAttributes();
			} else {
				secondI = p2;
			}
//			if (firstI == centroids.classIndex()) {
//				p1++; continue;
//			} 
//			if (secondI == centroids.classIndex()) {
//				p2++; continue;
//			} 
			double diff;
			if (firstI == secondI) {
				diff = difference(firstI, first.getValue(p1), second.getValue(p2));
				p1++;
				p2++;
			} else if (firstI > secondI) {
				diff = difference(secondI, 0, second.getValue(p2));
				p2++;
			} else {
				diff = difference(firstI, first.getValue(p1), 0);
				p1++;
			}
			distance += diff * diff;
		}
		
		//return Math.sqrt(distance / centroids.numAttributes());
		return distance;
	}

	/**
	 * Computes the difference between two given attribute
	 * values.
	 * 
	 * @param index the attribute index
	 * @param val1 the first value
	 * @param val2 the second value
	 * @return the difference
	 */
	private double difference(int index, double val1, double val2) {
		return norm(val1, index) - norm(val2, index);
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

	/**
	 * Updates the minimum and maximum values for all the attributes
	 * based on a new instance.
	 *
	 * @param instance the new instance
	 */
	private void updateMinMax(Instance instance) {	
		for (int j = 0;j < centroids.numAttributes(); j++) {
			if (Double.isNaN(minValue[j])) {
				minValue[j] = instance.getValue(j);
				maxValue[j] = instance.getValue(j);
			} else {
				if (instance.getValue(j) < minValue[j]) {
					minValue[j] = instance.getValue(j);
				} else {
					if (instance.getValue(j) > maxValue[j]) {
						maxValue[j] = instance.getValue(j);
					}
				}
			}
		}
	}
	
	/**
	 * set the number of clusters to generate
	 *
	 * @param n the number of clusters to generate
	 * @throws Exception if number of clusters is negative
	 */
	public void setNumClusters(int numClusters) throws Exception {
		if (numClusters <= 0) {
			throw new Exception("Number of clusters must be > 0");
		}
		this.numClusters = numClusters;
	}

	/**
	 * Gets the the cluster centroids
	 * 
	 * @return the cluster centroids
	 */
	public InstanceSet getClusterCentroids() {
		return centroids;
	}

	/**
	 * Gets the standard deviations of the numeric attributes in each cluster
	 * 
	 * @return the standard deviations of the numeric attributes 
	 * 			in each cluster
	 */
	public InstanceSet getClusterStandardDevs() {
		return clustersStdDev;
	}

	/**
	 * Gets the squared error for all clusters
	 * 
	 * @return the squared error
	 */
	public double getSquaredError() {
		return Utils.sum(squaredErrors);
	}

	/**
	 * Gets the number of instances in each cluster
	 * 
	 * @return The number of instances in each cluster
	 */
	public int[] getClusterSizes() {
		return clustersSize;
	}

}
