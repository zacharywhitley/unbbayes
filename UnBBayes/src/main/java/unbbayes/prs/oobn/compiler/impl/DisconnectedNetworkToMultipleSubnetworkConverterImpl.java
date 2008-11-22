/**
 * 
 */
package unbbayes.prs.oobn.compiler.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import unbbayes.prs.Edge;
import unbbayes.prs.Network;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.msbn.SubNetwork;
import unbbayes.prs.oobn.compiler.IDisconnectedNetworkToMultipleSubnetworkConverter;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class DisconnectedNetworkToMultipleSubnetworkConverterImpl implements
		IDisconnectedNetworkToMultipleSubnetworkConverter {


	protected DisconnectedNetworkToMultipleSubnetworkConverterImpl() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Default constructor method
	 * @return
	 */
	public static DisconnectedNetworkToMultipleSubnetworkConverterImpl newInstance() {
		return new DisconnectedNetworkToMultipleSubnetworkConverterImpl();
	}
	
	/**
	 * Runs recursively in order to insert a Node, its parents and its children
	 * into a given Subnetwork
	 * @param currentNode: a node to be analized recursively
	 * @param currentDestinationNode a copy of currentNode, which was already added to networkToAddInto
	 * @param networkToAddInto: network which currentNode will be added
	 * @param referenceNetwork: original network used to find out edges
	 */
	private void visitRecursively(Node currentNode, /*Node currentDestinationNode, */ SubNetwork networkToAddInto, Network referenceNetwork){
		
		// if this node is already visited, stop search
//		if (networkToAddInto.getNodes().contains(currentNode)) {
//			return;
//		}
		
		// add the current node's clone
//		Node currentNodeCpy = (Node)((ProbabilisticNode)currentNode).clone();
//		networkToAddInto.addNode(currentNodeCpy);
		
//		networkToAddInto.addNode(currentNode);
		
//		// reset node's color
//		try {
//			if (currentNode instanceof OOBNNodeGraphicalWrapper) {
//				((OOBNNodeGraphicalWrapper)currentNodeCpy).setInputColor(((OOBNNodeGraphicalWrapper)currentNode).getDescriptionColor());
//				((OOBNNodeGraphicalWrapper)currentNodeCpy).setInstanceColor(((OOBNNodeGraphicalWrapper)currentNode).getDescriptionColor());
//				((OOBNNodeGraphicalWrapper)currentNodeCpy).setInstanceInputColor(((OOBNNodeGraphicalWrapper)currentNode).getDescriptionColor());
//				((OOBNNodeGraphicalWrapper)currentNodeCpy).setInstanceOutputColor(((OOBNNodeGraphicalWrapper)currentNode).getDescriptionColor());
//				((OOBNNodeGraphicalWrapper)currentNodeCpy).setPrivateColor(((OOBNNodeGraphicalWrapper)currentNode).getDescriptionColor());
//			}
//		} catch (Exception e) {
//			// Of cource, this is not a very crucial thing to do, so, ignore exception
//			Debug.println(this.getClass(), "Error changing color of " + currentNodeCpy.getName(), e);
//		}
		
		// adds edges from parents to current node
		for (Node parent : currentNode.getParents()) {
			// check if parent is already added
			// this check is going to be OK because ArrayList uses equals to check it and Node uses name to check equality
			if (networkToAddInto.getNodes().contains(parent)) {
				continue;
			}
			Edge edge = referenceNetwork.getEdge(parent, currentNode);
			if (edge != null) {
//				Node parentCpy = (Node)((ProbabilisticNode)parent).clone();
//				networkToAddInto.addNode(parentCpy);
//				networkToAddInto.getEdges().add(new Edge(parentCpy, currentDestinationNode));
				networkToAddInto.addNode(parent);
				networkToAddInto.getEdges().add(edge);
				// do recursive visit
				this.visitRecursively(parent, /*parentCpy,*/ networkToAddInto, referenceNetwork);
			} else {
				// there is a parent without edge
				throw new NullPointerException(parent.getName() + "->" + currentNode.getName());
			}
		}
		
		// adds edges from current node to children
		for (Node child : currentNode.getChildren()) {
			// check if child is already added
			// this check is going to be OK because ArrayList uses equals to check it and Node uses name to check equality
			if (networkToAddInto.getNodes().contains(child)) {
				continue;
			}
			Edge edge = referenceNetwork.getEdge(currentNode, child);
			if (edge != null) {
//				Node childCpy = (Node)((ProbabilisticNode)child).clone();
//				networkToAddInto.addNode(childCpy);
//				networkToAddInto.getEdges().add(new Edge(currentDestinationNode, childCpy));
				networkToAddInto.addNode(child);
				networkToAddInto.getEdges().add(edge);
				// do recursive visit
				this.visitRecursively(child, /*childCpy,*/ networkToAddInto, referenceNetwork);
			} else {
				// there is a parent without edge
				throw new NullPointerException(currentNode.getName() + "->" + child.getName());
			}
		}
		
		
	}
	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.oobn.compiler.IDisconnectedNetworkToMultipleSubnetworkConverter#generateSubnetworks(unbbayes.prs.Network)
	 */
	public Collection<SubNetwork> generateSubnetworks(Network net) {
		
		Set<SubNetwork> ret = new HashSet<SubNetwork>();
		
		// set of already analyzed nodes
		// this is an array list in order to use equals() to compare 2 nodes
		Collection<Node> treatedNodes = new ArrayList<Node>();
		
		try {
			int subnetworkCounter = 0;
			for (Node currentNode : net.getNodes()) {
				if (!treatedNodes.contains(currentNode)) {
					// this is the new network to be added to return
					SubNetwork networkToAddInto = null;
					if (subnetworkCounter <= 0) {
						// I dont want the 1st one to have a name concatenated with number
						// That guarantees that if network is not fragmented, then the name is not changed
						networkToAddInto = new SubNetwork(net.getName());
					} else {
						networkToAddInto = new SubNetwork(net.getName() + "_" + subnetworkCounter);
					}
					// fill new network
//					Node currentNodeCpy = (Node)((ProbabilisticNode)currentNode).clone();
//					networkToAddInto.addNode(currentNodeCpy);
					networkToAddInto.addNode(currentNode);
					this.visitRecursively(currentNode, /*currentNodeCpy,*/  networkToAddInto, net);
					// add a new network into return
					ret.add(networkToAddInto);
					// mark every node of new network as treated
					treatedNodes.addAll(networkToAddInto.getNodes());
					// update subnetwork counter
					subnetworkCounter++;
				}
			}
		} catch (Exception e) {
			// at least let's return what we could get by now...
			Debug.println(this.getClass(), "Error fragmenting disconnected network", e);			
			return ret;
		}
		
		
		// if we managed to get here, no error has occurred
		return ret;
	}

}
