package unbbayes.datamining.datamanipulation.mtree;

import unbbayes.datamining.datamanipulation.Instance;
import unbbayes.datamining.datamanipulation.InstanceSet;

/**
*
* @author Emerson Lopes Machado - emersoft@conectanet.com.br
* @date 18/08/2006
*/
public class MTObject {
 
	private Instance instance;
	 
	private int instanceID;
	 
	private static double maxDistance;
	 
	private InstanceSet instanceSet;
	 
	public void MTObject(InstanceSet instanceSet, double maxDistance) {
	}
	 
	double distanceFrom(MTObject other) {
		return 0;
	}
	 
	double getMaxDistance() {
		return 0;
	}
	 
	int getInstanceID() {
		return 0;
	}
	 
}
 
