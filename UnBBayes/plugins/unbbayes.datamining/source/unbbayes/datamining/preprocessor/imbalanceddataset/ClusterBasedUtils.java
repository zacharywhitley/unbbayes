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
package unbbayes.datamining.preprocessor.imbalanceddataset;

import java.util.ArrayList;
import java.util.Hashtable;

import unbbayes.datamining.clustering.CEBMDC;
import unbbayes.datamining.clustering.Kmeans;
import unbbayes.datamining.clustering.Squeezer;
import unbbayes.datamining.datamanipulation.InstanceSet;
import unbbayes.datamining.datamanipulation.NearestNeighbors;
import unbbayes.datamining.distance.Euclidean;
import unbbayes.datamining.distance.IDistance;
import unbbayes.datamining.evaluation.batchEvaluation.GlobalBatchParameters;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 14/02/2007
 */
public class ClusterBasedUtils {
	
	/** The current instanceSet */
	private InstanceSet instanceSet;

	/** Matrix of all clusters. Each row stores all instancesIDs of a cluster */
	private Hashtable<Integer, int[][]> clusters;
	
	/** Matrix of all clusters. Each row stores all instancesIDs of a cluster */
	private Hashtable<Integer, int[][][]> clustersByClass;
	
	private Hashtable<Integer, int[]> numClustersByClass;

	private Hashtable<Integer, int[][][]> smoteNN;

	private Hashtable<Integer, int[][][][]> smoteNNByClass;
	
	private Hashtable<Integer, double[][]> clustersSizeByClass;

	
	/** Minimum accepted % change in each iteration in numeric clusterization */
	protected static double kError;

	private int k;

	private NearestNeighbors nearestNeighbors;

	public ClusterBasedUtils(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
		initialize();
		
		/* Initialize arrays */
		clusters = new Hashtable<Integer, int[][]>();
		smoteNN = new Hashtable<Integer, int[][][]>();
		
		/* Initialize arrays by class */
		clustersByClass = new Hashtable<Integer, int[][][]>();
		numClustersByClass = new Hashtable<Integer, int[]>();
		clustersSizeByClass = new Hashtable<Integer, double[][]>();
		smoteNNByClass = new Hashtable<Integer, int[][][][]>();
	}
	
	public void clusterizeByClass(int k) throws Exception {
		int numClasses = instanceSet.numClasses();
		int[][][] clustersByClassAux = new int[numClasses][][];
		int[][][][] smoteNNByClassAux = new int[numClasses][][][];
		int[] numClustersByClassAux = new int[numClasses];
		double[][] clustersSizeByClassAux = new double[numClasses][];
		
		for (int classValue = 0; classValue < numClasses; classValue++) {
			clustersByClassAux[classValue] = clusterize(classValue, true);
			numClustersByClassAux[classValue] = clustersByClassAux[classValue].length;
			clustersSizeByClassAux[classValue] = new double[numClustersByClassAux[classValue]];
			
			int[] cluster;
			int counter;
			int inst;
			for (int i = 0; i < numClustersByClassAux[classValue]; i++) {
				cluster = clustersByClassAux[classValue][i];
				counter = 0;
				for (int j = 0; j < cluster.length; j++) {
					inst = cluster[j];
					counter += instanceSet.instances[inst].getWeight();
				}
				clustersSizeByClassAux[classValue][i] = counter;
			}
			
			smoteNNByClassAux[classValue] = buildSmoteNN(clustersByClassAux[classValue]);
		}
		
		clustersByClass.put(k, clustersByClassAux);
		smoteNNByClass.put(k, smoteNNByClassAux);
		numClustersByClass.put(k, numClustersByClassAux);
		clustersSizeByClass.put(k, clustersSizeByClassAux);
	}
	
	private void clusterize() throws Exception {
		int[][] clustersAux = clusterize(null, false);
		clusters.put(k, clustersAux);
		smoteNN.put(k, buildSmoteNN(clustersAux));
	}
	
	/**
	 * Return an array of integer with the clusterID of each instance of the
	 * class pointed by the parameter <code>classValue</code> in the order they
	 * appear in the instanceSet.
	 * 
	 * @param classValue
	 * @return
	 * @throws Exception 
	 */
	private int[][] clusterize(int classValue, boolean byClass) throws Exception {
		/* Choose the instancesIDs for the clustering process */
		int[] instancesPos = instanceSet.getInstancesPosFromClass(classValue);
		
		return clusterize(instancesPos, byClass);
	}
	
	private int[][] clusterize(int[] instancesPos, boolean byClass) throws Exception {
		int[] numericClusters = null;
		int[] nominalClusters = null;
		int[][] clusters = null;
		
		/* Clusterize the numeric attributes */
		ArrayList<Object> numericResult;
		if (instanceSet.isNumeric() || instanceSet.isMixed()) {
			numericResult = clusterizeKmeans(instancesPos, byClass);
			clusters = (int[][]) numericResult.get(0);
			numericClusters = (int[]) numericResult.get(1);
		}

		/* Clusterize the nominal attributes */
		ArrayList<Object> nominalResult;
		if (instanceSet.isNominal() || instanceSet.isMixed()) {
			nominalResult = clusterizeSqueezer(instancesPos, byClass);
			clusters = (int[][]) nominalResult.get(0);
			nominalClusters = (int[]) nominalResult.get(1);
		}

		/* Clusterize the both numeric and nominal attributes */
		ArrayList<Object> mixedResult;
		if (instanceSet.isMixed()) {
			mixedResult = clusterizeCEBMDC(instancesPos, byClass, numericClusters,
					nominalClusters);
			clusters = (int[][]) mixedResult.get(0);
		}
		
		return clusters;
	}

	private ArrayList<Object> clusterizeSqueezer(int[] instancesPos,
			boolean byClass) throws Exception {
		/* Set the options for the Squeezer algorithm */
		Squeezer squeezer = new Squeezer(instanceSet);
		squeezer.setUseAverageSimilarity(true);
		
		/* Clusterize the nominal attributes */
		if (byClass) {
			squeezer.clusterize(instancesPos);
		} else {
			squeezer.clusterize();
		}

		/* Get the cluster results */
		int[][] clusters = squeezer.getClusters();
		int[] assignmentMatrix = squeezer.getAssignmentMatrix();
		
		ArrayList<Object> result = new ArrayList<Object>(2);
		result.add(clusters);
		result.add(assignmentMatrix);
		
		return result;
	}

	private ArrayList<Object> clusterizeKmeans(int[] instancesPos,
			boolean byClass) throws Exception {
		/* Set the options for the Kmeans algorithm */
		int normFactor = 4;
		IDistance distance = new Euclidean(instanceSet, normFactor, false);
		Kmeans kmeans = new Kmeans(instanceSet);
		kmeans.setOptionDistance(distance);
		kmeans.setError(kError);
		kmeans.setNumClusters(k);
		
		/* Clusterize the numeric attributes */
		if (byClass) {
			kmeans.clusterize(instancesPos);
		} else {
			kmeans.clusterize();
		}

		/* Get the cluster results */
		int[][] clusters = kmeans.getClusters();
		int[] assignmentMatrix = kmeans.getAssignmentMatrix();
		
		ArrayList<Object> result = new ArrayList<Object>(2);
		result.add(clusters);
		result.add(assignmentMatrix);
		
		return result;
	}

	private ArrayList<Object> clusterizeCEBMDC(int[] instancesPos,
			boolean byClass, int[] numericClusters, int[] nominalClusters)
	throws Exception {
		/* Algorithm for clustering mixed attributes */
		/* Set the options for the CEBMDC algorithm */
		CEBMDC cebmdc = new CEBMDC(instanceSet);
		double[] weight = {instanceSet.numNumericAttributes,
				instanceSet.numNominalAttributes};
		cebmdc.setWeight(weight);
		cebmdc.setUseAverageSimilarity(true);

		
		/* Clusterize the both numeric and nominal attributes */
		cebmdc.setNumericClustersInput(numericClusters);
		cebmdc.setNominalClustersInput(nominalClusters);
		cebmdc.clusterize();
		
		/* Get the cluster results */
		int[][] clusters = cebmdc.getClusters();
		
		ArrayList<Object> result = new ArrayList<Object>(2);
		result.add(clusters);
		result.add(null);
		
		return result;
	}

	private int[][][] buildSmoteNN(int[][] clusters) throws Exception {
		if (instanceSet.isNumeric() || instanceSet.isMixed()) {
			int numClusters = clusters.length;
			int[][][] smoteNN = new int[numClusters][][];
			
			/* Build nearest neighbors for each cluster */
			for (int clusterID = 0; clusterID < numClusters; clusterID++) {
				smoteNN[clusterID] = nearestNeighbors.build(clusters[clusterID]);
			}
			return smoteNN;
		}
		return null;
	}

	public void setKError(double kError) {
		ClusterBasedUtils.kError = kError;
	}

	public int[][] getClusters(int k) throws Exception {
		this.k = k;
		if (!clusters.containsKey(k)) {
			clusterize();
		}
		return clusters.get(k).clone();
	}

	public int[][][] getClustersByClass(int k) throws Exception {
		this.k = k;
		if (!clustersByClass.containsKey(k)) {
			clusterizeByClass(k);
		}
		return clustersByClass.get(k).clone();
	}

	public int getNumClusters(int k) throws Exception {
		this.k = k;
		if (!clusters.containsKey(k)) {
			clusterize();
		}
		return clusters.get(k).length;
	}

	public int[] getNumClustersByClass(int k) throws Exception {
		this.k = k;
		if (!clustersByClass.containsKey(k)) {
			clusterizeByClass(k);
		}
		return numClustersByClass.get(k).clone();
	}

	public int[][][] getSmoteNN(int k) throws Exception {
		this.k = k;
		if (!smoteNN.containsKey(k)) {
			clusterize();
		}
		return smoteNN.get(k).clone();
	}

	public int[][][][] getSmoteNNByClass(int k) throws Exception {
		this.k = k;
		if (!smoteNNByClass.containsKey(k)) {
			clusterizeByClass(k);
		}
		return smoteNNByClass.get(k).clone();
	}

	public double[][] getClustersSizeByClass(int k) throws Exception {
		this.k = k;
		if (!clustersSizeByClass.containsKey(k)) {
			clusterizeByClass(k);
		}
		return clustersSizeByClass.get(k).clone();
	}

	private void initialize() {
		kError = GlobalBatchParameters.getInstance().getKError();
		nearestNeighbors = new NearestNeighbors(instanceSet);
	}

}