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
package unbbayes.datamining.clustering;

import unbbayes.datamining.datamanipulation.Attribute;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 * CEBMDC (Cluster Ensemble Based Mixed Data Clustering). This framework was
 * proposed by Zengyou He, Xiaofe I Xu and Shengchum Deng. It clusters both
 * nominal and numeric data. The idea behind it is to construct a meta cluster
 * of the output of two different clustering algorithm. First, the original 
 * dataset is divided in two subsets: a pure numeric dataset and a pure nominal
 * dataset. Then, these two subsets are clusterized by any of the many existing
 * clustering algorithms. Finally, the cluster results of both subsets are
 * combined and clusterized as a nominal set.
 * The authors utilized in their framework the Squeezer algorithm for clustering
 * nominal datasets. The same algorithm is used in this implementation as well.
 * More info: http://arxiv.org/ftp/cs/papers/0509/0509011.pdf
 * 
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 2006/11/07
 */
public class CEBMDC extends Clustering {
	/** 
	 * Weight of the attributes in the final cluster construction:
	 * <ul>
	 * <li><code>weight[0]</code>: number of numeric attributes.
	 * <li><code>weight[1]</code>: number of nominal attributes.
	 * </ul>
	 */
	double[] weight;

	/**
	 * The similarity threshold, wich says how similar an <code>I</code>
	 * instance and the mode of the <code>C</code> cluster must be in order
	 * to the <code>I</code> instance be added to the <code>C</code> cluster.
	 */
	double s;
	
	/**
	 * The assignment matrix from the clusterization of the numeric attributes.
	 */ 
	private int[] numericClusters;
	
	/**
	 * The assignment matrix from the clusterization of the nominal attributes.
	 */ 
	private int[] nominalClusters;

	/**
	 * Set the similarity threshold automatically:
	 * True - set automatically;
	 * False - set manually.
	 */
	private boolean useAverageSimilarity;

	/**
	 * Default constructor for this class.
	 * 
	 * @param instanceSet
	 */
	public CEBMDC(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
	}

	protected void run() throws Exception {
		/* Merge the numeric and nominal cluster's results in one matrix */
		InstanceSet mixedData = buildMixedMatrix();
		
		/* Clusterize the mixed attributes */
		Squeezer squeezer = new Squeezer(mixedData);
		squeezer.setS(s);
		squeezer.setUseAverageSimilarity(useAverageSimilarity);
		squeezer.setWeight(weight);
		squeezer.clusterize(instancesIDs);
		clusters = squeezer.clusters;
		numClusters = squeezer.numClusters;
		clustersSize = squeezer.clustersSize;
		assignmentMatrix = squeezer.assignmentMatrix;
	}
	
	private InstanceSet buildMixedMatrix() {
		/* Message thrown as an exception in case of wrong arguments */
		boolean exception = false;
		String exceptionMsg = "The numeric and nominal clusters are not from" +
				" the same instances!";

		/* Check if numeric and nominal clusters are from the same instances */
		if (numericClusters.length != nominalClusters.length ||
				numericClusters.length != numInstances ||
				nominalClusters.length != numInstances ) {
			exception = true;
		} else {
			for (int i = 0; i < numInstances; i++) {
				if (numericClusters[i] == -1 && nominalClusters[i] != -1) {
					exception = true;
				} 
				if (numericClusters[i] != -1 && nominalClusters[i] == -1) {
					exception = true;
				}
			}
		}

		/* Throw exception if there is problems with the parameters */
		if (exception) throw new IllegalArgumentException(exceptionMsg);
		
		/* Count the number of instances to be clusterized */
		int counter = 0;
		for (int i = 0; i < numInstances; i++) {
			if (numericClusters[i] != -1) {
				++counter;
			}
		}
		instancesIDs = new int[counter];

		/* Merge numeric and nominal clusters together */
		numInstances = numericClusters.length;
		int inst = 0;
		for (int i = 0; i < numInstances; i++) {
			if (numericClusters[i] != -1) {
				instancesIDs[inst] = i;
				++inst;
			}
		}
		
		/* Build the mixedData InstanceSet */
		Attribute[] newAttributes = new Attribute[2];
		newAttributes[0] = new Attribute("", InstanceSet.NUMERIC, false, 0, 0);
		newAttributes[1] = new Attribute("", InstanceSet.NUMERIC, false, 0, 1);
		
		InstanceSet mixedData = new InstanceSet(numInstances, newAttributes);
		
		/* Add the current instance to the mixedData InstanceSet */
		for (int i = 0; i < numInstances; i++) {
			float[] instance = new float[3];
			instance[0] = numericClusters[i];
			instance[1] = nominalClusters[i];
			instance[2] = instances[i].data[counterIndex];
			mixedData.insertInstance(instance);
		}
		
		/* 
		 * The metacluster algorithm uses only nominal attributes. Then, we
		 * must fake the mixedData InstanceSet to present two nominal 
		 * attributes. 
		 */
		mixedData.numNominalAttributes = 2;
		mixedData.attributeType[0] = InstanceSet.NOMINAL;
		mixedData.attributeType[1] = InstanceSet.NOMINAL;

		return mixedData;
	}

	/**
	 * Set the weight that the numeric and nominal input assignment matrix will
	 * have in this metaclusterizing process:
	 * <ul>
	 * <li><code>weight[0]</code>: number of numeric attributes.
	 * <li><code>weight[1]</code>: number of nominal attributes.
	 * </ul>
	 * @param weight
	 */
	public void setWeight(double[] weight) {
		this.weight = weight;
	}
	
	/**
	 * Set the similarity threshold, wich says how similar an <code>I</code>
	 * instance and the mode of the <code>C</code> cluster must be in order
	 * to the <code>I</code> instance be added to the <code>C</code> cluster.
	 *  
	 * @param s
	 */
	public void setS(double s) {
		this.s = s;
	}

	/**
	 * Input with the assignment matrix from the clusterization of the numeric
	 * attributes.
	 * 
	 * @param numericClusters
	 */
	public void setNumericClustersInput(int[] numericClusters) {
		this.numericClusters = numericClusters;
	}

	/**
	 * Input with the assignment matrix from the clusterization of the nominal
	 * attributes.
	 * 
	 * @param numericClusters
	 */
	public void setNominalClustersInput(int[] nominalClusters) {
		this.nominalClusters = nominalClusters;
	}

	/**
	 * Set the similarity threshold automatically:
	 * True - set automatically;
	 * False - set manually.
	 * 
	 * @param useAverageSimilarity
	 */
	public void setUseAverageSimilarity(boolean useAverageSimilarity) {
		this.useAverageSimilarity = useAverageSimilarity;
	}
	
}