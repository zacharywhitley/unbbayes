/**
 * 
 */
package unbbayes.prs.oobn.compiler;

import java.util.Collection;

import unbbayes.prs.Network;
import unbbayes.prs.msbn.SubNetwork;

/**
 * 
 * This interface's implementation converts a network
 * with diconnected graphs into multiple connected sub-networks.
 * 
 * Sample:
 * 
 * Network:
 * 
 * (1)->(2)
 *  |
 *  v
 * (3) 	(4)->(5)
 * 
 * Is converted to:
 * 
 * SubNetwork1:
 * 
 * (1)->(2)
 *  |
 *  v
 * (3)
 * 
 * SubNetwork2:
 * 
 * (4)->(5)
 * 
 * @author Shou Matsumoto
 *
 */
public interface IDisconnectedNetworkToMultipleSubnetworkConverter {
	
	/**
	 * Converts a network
	 * with diconnected graphs into multiple connected sub-networks.
	 * If a sub-network is only a single node, the sub-network is ignored.
	 * 
	 * Sample:
	 * 
	 * Network:
	 * 
	 * (1)->(2)
	 *  |
	 *  v
	 * (3) 	(4)->(5)
	 * 
	 * Is converted to:
	 * 
	 * SubNetwork1:
	 * 
	 * (1)->(2)
	 *  |
	 *  v
	 * (3)
	 * 
	 * SubNetwork2:
	 * 
	 * (4)->(5)
	 * @param net: the network to be re-fragmented
	 * @return a collection of locally-connected sub-networks
	 */
	public Collection<SubNetwork> generateSubnetworks(Network net);
	
}
