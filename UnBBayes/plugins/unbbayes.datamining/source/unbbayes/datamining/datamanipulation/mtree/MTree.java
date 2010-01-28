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
package unbbayes.datamining.datamanipulation.mtree;

/**
*
* @author Emerson Lopes Machado - emersoft@conectanet.com.br
* @date 18/08/2006
*/
import java.util.List;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;

public class MTree {
 
	private InstanceSet instanceSet;
	 
	private PageFile file;
	 
	private int rootNode;
	 
	private List neighborsList;
	 
	private byte promotePartitionFunction;
	 
	public void MTree(int nodeMinSize, int nodeMaxSize) {
	}
	 
	public void MTree(int nodeMinSize, int nodeMaxSize, String fileName) {
	}
	 
	public void MTree(String fileName) {
	}
	 
	public int[] nearestNeighborIDs(Instance instance, int k) {
		return null;
	}
	
	public int[] nearestNeighborIDs(Instance instance, int k, int classIndex) {
		// TODO Auto-generated method stub
		return null;
	}
	 
	public Instance[] nearestNeighbor(Instance instance, int k) {
		return null;
	}
	 
	public Instance[] nearestNeighbor(Instance instance, int k, int classIndex) {
		// TODO Auto-generated method stub
		return null;
	}
	 
	public void close() {
	}
	 
	private int[] nearestNeighbor(int node, MTObject mTObject, int radius) {
		return null;
	}

}
 
