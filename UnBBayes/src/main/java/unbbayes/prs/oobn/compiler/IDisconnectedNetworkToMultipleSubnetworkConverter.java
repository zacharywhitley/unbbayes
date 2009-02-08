/**
 * 
 */
package unbbayes.prs.oobn.compiler;

import java.util.Collection;

import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.msbn.SubNetwork;
import unbbayes.prs.oobn.IOOBNNode;

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
	
	
	/**
	 * Verifies if a node will become a disconnected single node by
	 * {@link IDisconnectedNetworkToMultipleSubnetworkConverter#generateSubnetworks(Network)}
	 * (when generating multiple subnetworks). In that case, the node may be ignored, since
	 * it will have no influence to the total joint probability function.
	 * 
	 * Those ignorable nodes might not be added to the result subnetworks by 
	 * {@link IDisconnectedNetworkToMultipleSubnetworkConverter#generateSubnetworks(Network)}.
	 * If you do not want to ignore such nodes, you should implement this method in order 
	 * to return false.
	 * 
	 * @param node : the node to be analyzed
	 * @return : true if the node will become a disconnected node. False otherwise.
	 */
	public boolean isIgnorableNode(Node node);
	
}
