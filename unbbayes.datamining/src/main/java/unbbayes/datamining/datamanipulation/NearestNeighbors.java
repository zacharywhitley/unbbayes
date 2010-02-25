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

import unbbayes.datamining.distance.Distance;
import unbbayes.datamining.distance.HVDM;
import unbbayes.datamining.evaluation.batchEvaluation.GlobalBatchParameters;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 03/09/2007
 */
public class NearestNeighbors {

	private InstanceSet instanceSet;
	private Instance[] instances;
	private int classIndex;
	private int numInstances;
	private int[][] nearestNeighborsIDs;
	private byte optionDistanceFunction;
	private int numNN = 5;
	
	public NearestNeighbors(InstanceSet instanceSet) {
		this.instanceSet = instanceSet;
		numInstances = instanceSet.numInstances();
		instances = instanceSet.instances;
		classIndex = instanceSet.classIndex;
		initialize();
	}
	
	public NearestNeighbors(InstanceSet instanceSet,
			byte optionDistanceFunction) {
		this(instanceSet);
		this.optionDistanceFunction = optionDistanceFunction;
	}
	
	public int[][] build(int interestingClass)
	throws Exception {
		int numInstances = instanceSet.numInstances();
		
		int counter = 0;
		int instancesIDsTmp[] = new int[numInstances];
		
		for (int inst = 0; inst < numInstances; inst++) {
			if (instances[inst].data[classIndex] == interestingClass) {
				instancesIDsTmp[counter] = inst;
				++counter;
			}
		}
		int instancesIDs[] = new int[counter];
		for (int i = 0; i < counter; i++) {
			instancesIDs[i] = instancesIDsTmp[i];
		}
		
		return build(instancesIDs);
	}

	/**
	 * SMOTE oversamples a complete dataset. It creates new cases
	 * based on the existing ones. These new cases are randomly interpolated
	 * between each instance and its numNN nearest neighbors, also chosen by random.
	 * 
	 * @param proportion: Desired proportion of new instances
	 * @throws Exception 
	 */
	public int[][] buildNNAll() throws Exception {
		int instancesIDs[] = new int[numInstances];
		
		for (int inst = 0; inst < numInstances; inst++) {
			instancesIDs[inst] = inst;
		}
		
		return build(instancesIDs);
	}

	public int[][] build(int[] instancesIDs)
	throws Exception {
		int numInstancesIDs = instancesIDs.length;

		nearestNeighborsIDs = new int[numInstancesIDs][numNN];
		float[][] nearestNeighborsDistance = new float[numInstancesIDs][numNN];
		
//		/* File with nearest neighbors to open */
//		File fileTest;
//		if (optionDistanceFunction == HAMMING) {
//			fileName = fileName + "_nearestHamming_" + interestingClass + ".idx";
//		} else {
//			fileName = fileName + "_nearestHVDM_" + interestingClass + ".idx";
//		}
//		fileTest = new File(fileName);
//		RandomAccessFile file;
//		
//		if(fileTest.exists()) {
//			/* Initialize from existing file */
//			file = new RandomAccessFile(fileTest, "r");
//			for (int i = 0; i < numInstancesIDs; i++) {
//				for (int d = 0; d < 2 * numNN; d += 2) {
//					nearestNeighborsIDs[i][d] = file.readInt();
//					nearestNeighborsIDs[i][d + 1] = file.readFloat();
//				}
//			}
//			/* Return with the nearest neighbors read from file */
//			return;
//		}

		/* Initialize 'nearestNeighborsIDs */
		for (int i = 0; i < numInstancesIDs; i++) {
			for (int j = 0; j < numNN; j++) {
				nearestNeighborsIDs[i][j] = -1;
				nearestNeighborsDistance[i][j] = Float.POSITIVE_INFINITY;
			}
		}
		
		/* Get distance function */
		Distance distance;
		int normFactor = 4;
		distance = new HVDM(instanceSet, normFactor);
		((HVDM) distance).setOptionDistanceFunction(optionDistanceFunction);
		
		instances = instanceSet.instances;
		float dist = 0;
		int distGreaterID = 0;
		float distGreater;
		int instI;
		int instJ;
		for (int i = 0; i < numInstancesIDs; i++) {
			instI = instancesIDs[i];
			distGreater = Float.POSITIVE_INFINITY;
			for (int j = 0; j < numInstancesIDs; j++) {
				instJ = instancesIDs[j];
				dist = distance.distanceValue(instances[instI].data,
						instances[instJ].data);
				/* Check if the 'instJ' is a nearest neighbor */
				if (dist < distGreater) {
					/* 
					 * We've found a new nearest neighbor. Pop the farthest
					 * neighbor up and insert this new nearest neighbor.
					 */
					nearestNeighborsIDs[i][distGreaterID] = instJ;
					nearestNeighborsDistance[i][distGreaterID] = dist;
					distGreater = 0;
					
					/* 
					 * Find the new farthest neighbor and its distance from
					 * 'intI'.
					 */
					for (int d = 0; d < numNN; d++) {
						if (distGreater < nearestNeighborsDistance[i][d]) {
							distGreater = nearestNeighborsDistance[i][d];
							nearestNeighborsIDs[i][distGreaterID] = instJ;
							distGreaterID = d;
						}
					}
				}
			}
		}
		
//		/* Save nearestneighbors */
//		file = new RandomAccessFile(fileTest, "rw");
//		for (int i = 0; i < numInstancesIDs; i++) {
//			for (int d = 0; d < 2 * numNN; d += 2) {
//				file.writeInt((int) nearestNeighborsIDs[i][d]);
//				file.writeFloat(nearestNeighborsIDs[i][d + 1]);
//			}
//		}
		return nearestNeighborsIDs;
	}

	private void initialize() {
		optionDistanceFunction = 
			GlobalBatchParameters.getInstance().getOptionDistanceFunction();
	}

}

