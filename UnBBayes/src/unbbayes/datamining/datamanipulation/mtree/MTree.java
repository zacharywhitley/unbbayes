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
 
