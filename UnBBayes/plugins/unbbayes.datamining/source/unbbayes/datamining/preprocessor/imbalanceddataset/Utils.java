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

import unbbayes.datamining.datamanipulation.InstanceSet;

/**
 *
 * @author Emerson Lopes Machado - emersoft@conectanet.com.br
 * @date 26/08/2007
 */
public class Utils {

	public static void removeMarkedInstances(InstanceSet instanceSet,
			boolean[] deleteIndex) {
		/* Remove those negative instances marked for removal */
		instanceSet.removeInstances(deleteIndex);
	}

	public static int[] uncompactInstancesIDs(int[] instancesIDs,
			InstanceSet instanceSet) {
		int size = instancesIDs.length;
		ArrayList<Integer> aux = new ArrayList<Integer>();
		int inst;
		int weight;
		
		for (int i = 0; i < size; i++) {
			inst = instancesIDs[i];
			weight = (int) instanceSet.instances[inst].getWeight();
			for (int j = 0; j < weight; j++) {
				aux.add(inst);
			}
		}
		
		size = aux.size();
		int[] newInstancesIDs = new int[size];
		for (int i = 0; i < size; i++) {
			newInstancesIDs[i] = aux.get(i);
		}
		
		return newInstancesIDs;
	}

}

